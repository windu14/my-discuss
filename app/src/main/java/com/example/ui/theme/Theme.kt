package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalContext

private val ExpressiveDarkColorScheme = darkColorScheme(
    primary = ExpressivePrimaryDark,
    onPrimary = ExpressiveOnPrimaryDark,
    primaryContainer = ExpressivePrimaryContainerDark,
    onPrimaryContainer = ExpressiveOnPrimaryContainerDark,
    secondary = ExpressiveSecondaryDark,
    onSecondary = ExpressiveOnSecondaryDark,
    secondaryContainer = ExpressiveSecondaryContainerDark,
    onSecondaryContainer = ExpressiveOnSecondaryContainerDark,
    tertiary = ExpressiveTertiaryDark,
    onTertiary = ExpressiveOnTertiaryDark,
    tertiaryContainer = ExpressiveTertiaryContainerDark,
    onTertiaryContainer = ExpressiveOnTertiaryContainerDark,
    background = ExpressiveBackgroundDark,
    onBackground = ExpressiveOnBackgroundDark,
    surface = ExpressiveSurfaceDark,
    onSurface = ExpressiveOnSurfaceDark,
    surfaceContainerLowest = ExpressiveSurfaceContainerLowestDark,
    surfaceContainerLow = ExpressiveSurfaceContainerLowDark,
    surfaceContainer = ExpressiveSurfaceContainerDark,
    surfaceContainerHigh = ExpressiveSurfaceContainerHighDark,
    surfaceContainerHighest = ExpressiveSurfaceContainerHighestDark
)

private val ExpressiveLightColorScheme = lightColorScheme(
    primary = ExpressivePrimaryLight,
    onPrimary = ExpressiveOnPrimaryLight,
    primaryContainer = ExpressivePrimaryContainerLight,
    onPrimaryContainer = ExpressiveOnPrimaryContainerLight,
    secondary = ExpressiveSecondaryLight,
    onSecondary = ExpressiveOnSecondaryLight,
    secondaryContainer = ExpressiveSecondaryContainerLight,
    onSecondaryContainer = ExpressiveOnSecondaryContainerLight,
    tertiary = ExpressiveTertiaryLight,
    onTertiary = ExpressiveOnTertiaryLight,
    tertiaryContainer = ExpressiveTertiaryContainerLight,
    onTertiaryContainer = ExpressiveOnTertiaryContainerLight,
    background = ExpressiveBackgroundLight,
    onBackground = ExpressiveOnBackgroundLight,
    surface = ExpressiveSurfaceLight,
    onSurface = ExpressiveOnSurfaceLight,
    surfaceContainerLowest = ExpressiveSurfaceContainerLowestLight,
    surfaceContainerLow = ExpressiveSurfaceContainerLowLight,
    surfaceContainer = ExpressiveSurfaceContainerLight,
    surfaceContainerHigh = ExpressiveSurfaceContainerHighLight,
    surfaceContainerHighest = ExpressiveSurfaceContainerHighestLight
)

@Composable
fun DiskusikuTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> ExpressiveDarkColorScheme
        else -> ExpressiveLightColorScheme
    }

    CompositionLocalProvider(
        LocalExpressiveSpacing provides ExpressiveSpacing(),
        LocalExpressiveShapes provides ExpressiveShapes()
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = ExpressiveTypography,
            content = content
        )
    }
}
