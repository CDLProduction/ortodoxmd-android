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
    primary = MochaPeach,
    onPrimary = MochaCrust,
    secondary = MochaBlue,
    onSecondary = MochaCrust,
    error = MochaRed,
    onError = MochaCrust,
    background = MochaBase,
    onBackground = MochaText,
    surface = MochaMantle,
    onSurface = MochaText,
    surfaceVariant = MochaSurface1,
    onSurfaceVariant = MochaSubtext0,
    outline = MochaOverlay0
)

private val LightColorScheme = lightColorScheme(
    primary = LattePeach,
    onPrimary = LatteBase,
    secondary = LatteBlue,
    onSecondary = LatteBase,
    error = LatteRed,
    onError = LatteBase,
    background = LatteBase,
    onBackground = LatteText,
    surface = LatteMantle,
    onSurface = LatteText,
    surfaceVariant = LatteMantle,
    onSurfaceVariant = LatteSubtext0,
    outline = LatteSurface1
)

@Composable
fun OrtodoxmdandroidTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
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
        typography = AppTypography,
        content = content
    )
}