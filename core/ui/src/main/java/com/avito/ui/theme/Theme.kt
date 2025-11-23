package com.avito.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = AvitoBlue,
    onPrimary = AvitoBlack,
    secondary = AvitoPurple,
    onSecondary = AvitoBlack,
    tertiary = AvitoLime,
    onTertiary = AvitoBlack,
    error = AvitoCoral,
    onError = AvitoBlack,
    background = AvitoBlack,
    onBackground = AvitoWhite,
    surface = AvitoDarkSurface,
    onSurface = AvitoWhite,
    surfaceVariant = AvitoDarkCard,
    onSurfaceVariant = AvitoWhite,
    outline = AvitoBlue.copy(alpha = 0.4f)
)

private val LightColorScheme = lightColorScheme(
    primary = AvitoBlue,
    onPrimary = AvitoWhite,
    secondary = AvitoPurple,
    onSecondary = AvitoWhite,
    tertiary = AvitoLime,
    onTertiary = AvitoBlack,
    error = AvitoCoral,
    onError = AvitoWhite,
    background = AvitoWhite,
    onBackground = AvitoBlack,
    surface = Color(0xFFF8F9FB),
    onSurface = AvitoBlack,
    surfaceVariant = Color(0xFFE8EEF5),
    onSurfaceVariant = AvitoBlack,
    outline = AvitoBlue.copy(alpha = 0.4f)
)

@Composable
fun AvitoTestTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}