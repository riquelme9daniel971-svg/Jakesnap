package org.jakesnap.downloader.ui

import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import org.jakesnap.downloader.R
import org.jakesnap.downloader.UiState
import org.jakesnap.downloader.ui.theme.BlueButton
import org.jakesnap.downloader.ui.theme.BlueProgress
import org.jakesnap.downloader.ui.theme.ErrorRed
import org.jakesnap.downloader.ui.theme.TextBlack
import org.jakesnap.downloader.ui.theme.YellowBackground

@Composable
fun JakesnapScreen(
    uiState: UiState,
    onRetry: () -> Unit,
    onOpenVideo: (Uri) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(YellowBackground)
    ) {
        when (uiState) {
            is UiState.Idle -> IdleScreen()
            is UiState.LoadingInfo -> LoadingInfoScreen()
            is UiState.Downloading -> DownloadingScreen(uiState)
            is UiState.Completed -> CompletedScreen(uiState, onOpenVideo)
            is UiState.Error -> ErrorScreen(uiState.message, onRetry)
        }
    }
}

@Composable
private fun IdleScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Spacer(modifier = Modifier.height(48.dp))
        Image(
            painter = painterResource(id = R.drawable.ic_launcher_foreground),
            contentDescription = "Jakesnap",
            modifier = Modifier
                .size(128.dp)
                .clip(RoundedCornerShape(24.dp))
        )
        Text(
            text = "Compartilhe um link do YouTube para baixar",
            color = TextBlack.copy(alpha = 0.6f),
            fontSize = 14.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 32.dp)
        )
    }
}

@Composable
private fun LoadingInfoScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator(color = BlueProgress)
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "Obtendo informações...",
            color = TextBlack,
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun DownloadingScreen(state: UiState.Downloading) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(32.dp))
        Thumbnail(state.thumbnail)
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = state.title,
            color = TextBlack,
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = formatDuration(state.durationSec),
            color = TextBlack.copy(alpha = 0.7f),
            fontSize = 14.sp
        )
        Spacer(modifier = Modifier.height(32.dp))
        ProgressSection(progress = state.progress, status = state.status)
    }
}

@Composable
private fun CompletedScreen(state: UiState.Completed, onOpenVideo: (Uri) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(32.dp))
        Thumbnail(state.thumbnail)
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = state.title,
            color = TextBlack,
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = formatDuration(state.durationSec),
            color = TextBlack.copy(alpha = 0.7f),
            fontSize = 14.sp
        )
        Spacer(modifier = Modifier.height(32.dp))
        ProgressSection(progress = 100f, status = "Download concluído!")
        Spacer(modifier = Modifier.height(40.dp))
        Button(
            onClick = { onOpenVideo(state.videoUri) },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = BlueButton)
        ) {
            Text(
                text = "▶ Abrir no Lark Player",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun ErrorScreen(message: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = message,
            color = ErrorRed,
            fontSize = 16.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        Spacer(modifier = Modifier.height(32.dp))
        Button(
            onClick = onRetry,
            colors = ButtonDefaults.buttonColors(containerColor = BlueButton),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.height(48.dp)
        ) {
            Text("Tentar novamente", color = Color.White)
        }
    }
}

@Composable
private fun Thumbnail(url: String?) {
    AsyncImage(
        model = ImageRequest.Builder(LocalContext.current)
            .data(url)
            .crossfade(true)
            .build(),
        contentDescription = "Thumbnail",
        contentScale = ContentScale.Crop,
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(16f / 9f)
            .shadow(6.dp, RoundedCornerShape(12.dp))
            .clip(RoundedCornerShape(12.dp))
            .background(Color.Black.copy(alpha = 0.1f))
    )
}

@Composable
private fun ProgressSection(progress: Float, status: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        LinearProgressIndicator(
            progress = { progress / 100f },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp)),
            color = BlueProgress,
            trackColor = Color.Black.copy(alpha = 0.1f)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = String.format("%.1f%%", progress),
            color = TextBlack,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = status,
            color = TextBlack.copy(alpha = 0.7f),
            fontSize = 14.sp
        )
    }
}

private fun formatDuration(seconds: Long): String {
    if (seconds <= 0) return ""
    val m = seconds / 60
    val s = seconds % 60
    return if (m > 0) "${m}m ${s}s" else "${s}s"
}
