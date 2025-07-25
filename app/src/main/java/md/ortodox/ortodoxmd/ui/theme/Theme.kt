// In ui/theme/Theme.kt

package md.ortodox.ortodoxmd.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = GoldDark,
    onPrimary = CharcoalDark,
    secondary = RedDark,
    onSecondary = CharcoalDark,
    tertiary = RedDark, // Can be same as secondary or a different accent
    background = CharcoalDark,
    onBackground = TextPrimaryDark,
    surface = SurfaceDark,
    onSurface = TextPrimaryDark,
    surfaceVariant = SurfaceDark, // For cards, etc.
    onSurfaceVariant = TextSecondaryDark,
    error = RedDark,
    onError = CharcoalDark
)

private val LightColorScheme = lightColorScheme(
    primary = GoldLight,
    onPrimary = Color.White,
    secondary = RedLight,
    onSecondary = Color.White,
    tertiary = RedLight,
    background = ParchmentLight,
    onBackground = TextPrimaryLight,
    surface = SurfaceLight,
    onSurface = TextPrimaryLight,
    surfaceVariant = SurfaceLight, // For cards, etc.
    onSurfaceVariant = TextSecondaryLight,
    error = RedLight,
    onError = Color.White
)

@Composable
fun OrtodoxmdandroidTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
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
        typography = AppTypography, // Use our new typography
        content = content
    )
}