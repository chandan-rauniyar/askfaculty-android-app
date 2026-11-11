package com.chandan.askfaculty.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColorScheme = lightColorScheme(
    primary = PrimaryOrange,
    onPrimary = OnPrimary,
    primaryContainer = PrimaryOrangeContainer,
    onPrimaryContainer = PrimaryOrangeDark,
    secondary = SecondaryBlue,
    onSecondary = OnSecondary,
    secondaryContainer = SecondaryBlueContainer,
    onSecondaryContainer = SecondaryBlue,
    background = AppBackground,
    surface = CardSurface,
    onSurface = TextPrimary,
    error = ErrorRed,
    outline = SurfaceBorder
)

@Composable
fun AskFacultyTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = Typography,
        content = content
    )
}
