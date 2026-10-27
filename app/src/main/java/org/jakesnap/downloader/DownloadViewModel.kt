package org.jakesnap.downloader

import android.app.Application
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class UiState {
    object Idle : UiState()
    object LoadingInfo : UiState()
    data class Downloading(
        val title: String,
        val thumbnail: String?,
        val durationSec: Long,
        val progress: Float,
        val status: String
    ) : UiState()
    data class Completed(
        val title: String,
        val thumbnail: String?,
        val durationSec: Long,
        val videoUri: Uri
    ) : UiState()
    data class Error(val message: String, val url: String?) : UiState()
}

class DownloadViewModel(application: Application) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow<UiState>(UiState.Idle)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private var currentUrl: String? = null

    fun handleSharedText(text: String?) {
        if (text.isNullOrBlank()) {
            _uiState.value = UiState.Idle
            return
        }
        val url = extractYoutubeUrl(text)
        if (url == null) {
            _uiState.value = UiState.Error("Link inválido. Compartilhe um link do YouTube.", null)
            return
        }
        currentUrl = url
        startDownload(url)
    }

    fun retry() {
        currentUrl?.let { startDownload(it) }
    }

    private fun startDownload(url: String) {
        viewModelScope.launch {
            try {
                _uiState.value = UiState.LoadingInfo
                val meta = Downloader.getVideoInfo(url)

                _uiState.value = UiState.Downloading(
                    title = meta.title,
                    thumbnail = meta.thumbnail,
                    durationSec = meta.duration,
                    progress = 0f,
                    status = "Baixando..."
                )

                val result = Downloader.download(
                    context = getApplication(),
                    url = url,
                    title = meta.title
                ) { progress, status ->
                    val current = _uiState.value
                    if (current is UiState.Downloading) {
                        _uiState.value = current.copy(
                            progress = progress,
                            status = status
                        )
                    }
                }

                _uiState.value = UiState.Completed(
                    title = meta.title,
                    thumbnail = meta.thumbnail,
                    durationSec = meta.duration,
                    videoUri = result.fileUri
                )
            } catch (e: Exception) {
                e.printStackTrace()
                _uiState.value = UiState.Error(
                    message = e.message ?: "Erro desconhecido no download",
                    url = url
                )
            }
        }
    }

    private fun extractYoutubeUrl(text: String): String? {
        val regex = Regex(
            """(https?://)?(www\.)?(youtube\.com/watch\?v=|youtu\.be/|youtube\.com/shorts/|m\.youtube\.com/watch\?v=)[\w\-]+""",
            RegexOption.IGNORE_CASE
        )
        val match = regex.find(text)
        return match?.value?.let {
            if (it.startsWith("http")) it else "https://$it"
        }
    }

    fun openVideo(uri: Uri) {
        val context = getApplication<Application>()
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "video/mp4")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            // Fallback: try without mime
            val fallback = Intent(Intent.ACTION_VIEW, uri).apply {
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(fallback)
        }
    }
}
