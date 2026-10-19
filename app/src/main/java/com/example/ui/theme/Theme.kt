package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = YesminCyan,
    onPrimary = Color(0xFF001F28),
    primaryContainer = Color(0xFF004E5E),
    onPrimaryContainer = Color(0xFFB5EFFF),
    secondary = YesminPurple,
    onSecondary = Color(0xFF28065B),
    secondaryContainer = Color(0xFF40227C),
    onSecondaryContainer = Color(0xFFEADBFF),
    tertiary = YesminPink,
    onTertiary = Color(0xFF5C0028),
    tertiaryContainer = Color(0xFF860D40),
    onTertiaryContainer = Color(0xFFFFD9E2),
    background = BackgroundObsidian,
    onBackground = TextPrimary,
    surface = SurfaceDarkNavy,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceCard,
    onSurfaceVariant = TextSecondary,
    outline = SurfaceBorder,
    error = ErrorRed,
    onError = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF00687A),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFB5EFFF),
    onPrimaryContainer = Color(0xFF001F28),
    secondary = Color(0xFF6750A4),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFEADDFF),
    onSecondaryContainer = Color(0xFF21005D),
    tertiary = Color(0xFF984061),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFFFD9E2),
    onTertiaryContainer = Color(0xFF3E001D),
    background = Color(0xFFF8FAFC),
    onBackground = Color(0xFF0F172A),
    surface = Color.White,
    onSurface = Color(0xFF0F172A),
    surfaceVariant = Color(0xFFE2E8F0),
    onSurfaceVariant = Color(0xFF475569),
    outline = Color(0xFFCBD5E1),
    error = Color(0xFFBA1A1A),
    onError = Color.White
)

@Composable
fun YesminTheme(
    darkTheme: Boolean = true, // Default to futuristic dark HUD
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    YesminTheme(darkTheme = darkTheme, content = content)
}
