package org.jakesnap.downloader

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import com.yausername.youtubedl_android.YoutubeDL
import com.yausername.youtubedl_android.YoutubeDLRequest
import com.yausername.youtubedl_android.YoutubeDLResponse
import com.yausername.youtubedl_android.mapper.VideoInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.OutputStream

object Downloader {
    private const val TAG = "JakesnapDownloader"

    data class VideoMeta(
        val title: String,
        val thumbnail: String?,
        val duration: Long,
        val url: String
    )

    data class DownloadResult(
        val fileUri: Uri,
        val filePath: String?
    )

    suspend fun getVideoInfo(url: String): VideoMeta = withContext(Dispatchers.IO) {
        val request = YoutubeDLRequest(url)
        request.addOption("--skip-download")
        request.addOption("--no-playlist")
        request.addOption("--no-warnings")
        request.addOption("--extractor-args", "youtube:player_client=android,web")
        request.addOption("--user-agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")

        val info: VideoInfo = YoutubeDL.getInstance().getInfo(request)
        VideoMeta(
            title = info.title ?: "Vídeo sem título",
            thumbnail = info.thumbnail,
            duration = info.duration?.toLong() ?: 0L,
            url = url
        )
    }

    suspend fun download(
        context: Context,
        url: String,
        title: String,
        onProgress: (Float, String) -> Unit
    ): DownloadResult = withContext(Dispatchers.IO) {
        val cacheDir = File(context.cacheDir, "jakesnap_downloads").apply { mkdirs() }
        val outputTemplate = File(cacheDir, "%(title).200B.%(ext)s").absolutePath

        val request = YoutubeDLRequest(url).apply {
            // Formato bem mais flexível
            addOption("-f", "best[height<=720]/bestvideo[height<=720]+bestaudio/best")
            addOption("--merge-output-format", "mp4")
            addOption("--concurrent-fragments", "8")
            addOption("--no-part")
            addOption("--no-mtime")
            addOption("--no-playlist")
            addOption("--no-warnings")
            addOption("--retries", "10")
            addOption("--fragment-retries", "10")
            addOption("--socket-timeout", "30")
            addOption("--extractor-args", "youtube:player_client=android,web")
            addOption("--user-agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
            addOption("-o", outputTemplate)
        }

        val response: YoutubeDLResponse = YoutubeDL.getInstance().execute(request, null) { progress, eta, line ->
            val status = when {
                line.contains("Merging", ignoreCase = true) -> "Juntando áudio e vídeo..."
                line.contains("Deleting", ignoreCase = true) -> "Finalizando..."
                progress >= 99f -> "Finalizando..."
                else -> "Baixando..."
            }
            onProgress(progress.coerceIn(0f, 100f), status)
            Log.d(TAG, "Progress: $progress% | $line")
        }

        if (response.exitCode != 0) {
            throw Exception("Erro no download: ${response.err.take(200)}")
        }

        val downloadedFile = cacheDir.listFiles()
            ?.filter { it.isFile && (it.extension.equals("mp4", true) || it.extension.equals("mkv", true) || it.extension.equals("webm", true) || it.extension.equals("m4a", true)) }
            ?.maxByOrNull { it.lastModified() }
            ?: throw Exception("Arquivo baixado não encontrado")

        val safeTitle = title.replace(Regex("[\\\\/:*?\"<>|]"), "_").take(80)
        val finalName = "${safeTitle}.mp4"

        val uri = saveToMediaStore(context, downloadedFile, finalName)
        try { downloadedFile.delete() } catch (_: Exception) {}

        DownloadResult(uri, null)
    }

    private fun saveToMediaStore(context: Context, sourceFile: File, displayName: String): Uri {
        val contentValues = ContentValues().apply {
            put(MediaStore.Video.Media.DISPLAY_NAME, displayName)
            put(MediaStore.Video.Media.MIME_TYPE, "video/mp4")
            put(MediaStore.Video.Media.RELATIVE_PATH, Environment.DIRECTORY_MOVIES + "/Jakesnap")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.Video.Media.IS_PENDING, 1)
            }
        }

        val resolver = context.contentResolver
        val collection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
        } else {
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        }

        val uri = resolver.insert(collection, contentValues)
            ?: throw Exception("Falha ao criar entrada no MediaStore")

        resolver.openOutputStream(uri)?.use { output: OutputStream ->
            FileInputStream(sourceFile).use { input ->
                input.copyTo(output)
            }
        } ?: throw Exception("Falha ao escrever arquivo")

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            contentValues.clear()
            contentValues.put(MediaStore.Video.Media.IS_PENDING, 0)
            resolver.update(uri, contentValues, null, null)
        }

        return uri
    }
}
