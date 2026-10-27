package org.jakesnap.downloader.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val YellowBackground = Color(0xFFFFEB3B)
val BlueProgress = Color(0xFF2196F3)
val BlueButton = Color(0xFF1976D2)
val TextBlack = Color(0xFF212121)
val ErrorRed = Color(0xFFD32F2F)

private val LightColorScheme = lightColorScheme(
    primary = BlueButton,
    secondary = BlueProgress,
    background = YellowBackground,
    surface = YellowBackground,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = TextBlack,
    onSurface = TextBlack,
    error = ErrorRed
)

@Composable
fun JakesnapTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        content = content
    )
}
