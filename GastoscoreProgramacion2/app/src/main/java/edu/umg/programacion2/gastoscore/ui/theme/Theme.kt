package edu.umg.programacion2.gastoscore.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Colores para el modo oscuro
// Colores para el modo oscuro profesional
private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF64B5F6),
    onPrimary = Color(0xFF071522),

    secondary = Color(0xFF80CBC4),
    onSecondary = Color(0xFF10201F),

    tertiary = Color(0xFFFFCC80),
    onTertiary = Color(0xFF332000),

    background = Color(0xFF090D12),
    onBackground = Color(0xFFF1F5F9),

    surface = Color(0xFF151B23),
    onSurface = Color(0xFFF1F5F9),

    surfaceVariant = Color(0xFF222B36),
    onSurfaceVariant = Color(0xFFB0BEC5),

    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005)
)

// Colores para el modo claro
private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF1565C0),
    onPrimary = Color.White,

    secondary = Color(0xFF0288D1),
    onSecondary = Color.White,

    tertiary = Color(0xFF26A69A),
    onTertiary = Color.White,

    background = Color(0xFFF3F6FA),
    onBackground = Color(0xFF172033),

    surface = Color.White,
    onSurface = Color(0xFF172033),

    surfaceVariant = Color(0xFFE2EAF3),
    onSurfaceVariant = Color(0xFF64748B),

    error = Color(0xFFB3261E),
    onError = Color.White
)

@Composable
fun GastoscoreProgramacion2Theme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    // Selecciona los colores según el modo elegido
    val colorScheme = if (darkTheme) {
        DarkColorScheme
    } else {
        LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}