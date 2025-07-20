package md.ortodox.ortodoxmd.ui.theme

import androidx.compose.foundation.isSystemInDarkMode
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = Color(0xFF6200EE),
    primaryContainer = Color(0xFF3700B3),
    secondary = Color(0xFF03DAC5),
    secondaryContainer = Color(0xFF018786),
    onPrimary = Color.White,
    onSecondary = Color.Black
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFFBB86FC),
    primaryContainer = Color(0xFF3700B3),
    secondary = Color(0xFF03DAC5),
    secondaryContainer = Color(0xFF03DAC5),
    onPrimary = Color.Black,
    onSecondary = Color.Black
)

@Composable
fun OrtodoxMDTheme(
    darkTheme: Boolean = isSystemInDarkMode(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColors else LightColors

    MaterialTheme(
        colorScheme = colorScheme,
        typography = MaterialTheme.typography,  // Poți customiza typography dacă nevoie
        content = content
    )
}