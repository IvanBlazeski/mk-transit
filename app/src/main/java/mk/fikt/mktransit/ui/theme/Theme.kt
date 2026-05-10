package mk.fikt.mktransit.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = PrimaryBlue,
    onPrimary = TextOnPrimary,
    primaryContainer = PrimaryLightBlue,
    secondary = SecondaryYellow,
    onSecondary = TextPrimary,
    background = BackgroundLight,
    surface = SurfaceLight,
    error = ErrorRed,
    onBackground = TextPrimary,
    onSurface = TextPrimary,
)

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryLightBlue,
    onPrimary = TextPrimary,
    primaryContainer = PrimaryDarkBlue,
    secondary = SecondaryYellow,
    onSecondary = TextPrimary,
    background = BackgroundDark,
    surface = SurfaceDark,
    error = ErrorRed,
    onBackground = TextOnPrimary,
    onSurface = TextOnPrimary,
)

@Composable
fun MKTransitTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}