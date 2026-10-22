package com.meuglossario.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = Color(0xFF1565C0),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFD3E3FF),
    onPrimaryContainer = Color(0xFF001C3B),
    secondary = Color(0xFF4B607C),
    tertiary = Color(0xFF6C5677),
    background = Color(0xFFFDFBFF),
    surface = Color(0xFFFDFBFF),
    error = Color(0xFFBA1A1A)
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFFA3C9FF),
    onPrimary = Color(0xFF003060),
    primaryContainer = Color(0xFF004788),
    onPrimaryContainer = Color(0xFFD3E3FF),
    secondary = Color(0xFFB3C8E8),
    tertiary = Color(0xFFD8BDE3),
    background = Color(0xFF1A1C1E),
    surface = Color(0xFF1A1C1E),
    error = Color(0xFFFFB4AB)
)

@Composable
fun MeuGlossarioTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = appTypography(),
        content = content
    )
}
