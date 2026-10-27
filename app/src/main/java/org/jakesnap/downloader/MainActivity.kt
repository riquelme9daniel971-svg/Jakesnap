package org.jakesnap.downloader

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.core.view.WindowCompat
import com.yausername.youtubedl_android.YoutubeDL
import com.yausername.youtubedl_android.YoutubeDLException
import org.jakesnap.downloader.ui.JakesnapScreen
import org.jakesnap.downloader.ui.theme.JakesnapTheme

class MainActivity : ComponentActivity() {

    private val viewModel: DownloadViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        WindowCompat.setDecorFitsSystemWindows(window, false)

        // Initialize youtubedl-android
        try {
            YoutubeDL.getInstance().init(this)
        } catch (e: YoutubeDLException) {
            Log.e("Jakesnap", "Failed to init YoutubeDL", e)
        }

        handleIntent(intent)

        setContent {
            JakesnapTheme {
                val uiState by viewModel.uiState.collectAsState()
                JakesnapScreen(
                    uiState = uiState,
                    onRetry = { viewModel.retry() },
                    onOpenVideo = { uri -> viewModel.openVideo(uri) }
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        if (intent == null) return
        when (intent.action) {
            Intent.ACTION_SEND -> {
                if (intent.type == "text/plain") {
                    val sharedText = intent.getStringExtra(Intent.EXTRA_TEXT)
                    viewModel.handleSharedText(sharedText)
                }
            }
            Intent.ACTION_VIEW -> {
                // optional future support
            }
            else -> {
                // opened from launcher
                viewModel.handleSharedText(null)
            }
        }
    }
}
