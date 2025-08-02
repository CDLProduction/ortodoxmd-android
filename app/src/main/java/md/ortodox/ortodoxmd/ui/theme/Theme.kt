package md.ortodox.ortodoxmd.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = DarkAccent,
    onPrimary = DarkBackground, // Contrast bun pentru butoane
    secondary = DarkSecondary,
    onSecondary = DarkBackground,
    error = DarkHighlight,
    onError = DarkBackground,
    background = DarkBackground,
    onBackground = DarkText,
    surface = DarkCard, // Fundalul card-urilor
    onSurface = DarkText,
    surfaceVariant = DarkCard, // Fundal pentru elemente subtile
    onSurfaceVariant = DarkSubtleText, // Text secundar pe card-uri
    outline = DarkBorder // Culoarea contururilor
)

private val LightColorScheme = lightColorScheme(
    primary = LightAccent,
    onPrimary = LightCard, // Contrast bun pentru butoane
    secondary = LightSecondary,
    onSecondary = LightCard,
    error = LightHighlight,
    onError = LightCard,
    background = LightBackground,
    onBackground = LightText,
    surface = LightCard, // Fundalul card-urilor
    onSurface = LightText,
    surfaceVariant = LightCard, // Fundal pentru elemente subtile
    onSurfaceVariant = LightSubtleText, // Text secundar pe card-uri
    outline = LightBorder // Culoarea contururilor
)

@Composable
fun OrtodoxmdandroidTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color este disponibil pe Android 12+
    dynamicColor: Boolean = false, // Am setat pe 'false' pentru a folosi mereu tema noastră custom
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography, // Folosim tipografia definită în Type.kt
        content = content
    )
}