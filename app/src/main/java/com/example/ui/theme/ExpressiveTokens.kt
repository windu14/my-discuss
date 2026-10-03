package com.example.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Material 3 Expressive Design Tokens (Post-2026 Adaptive Specs)
 * Encapsulates dynamic 8dp-grid spacing, asymmetric expressive shapes, and elevation depths.
 */
data class ExpressiveSpacing(
    val extraSmall: Dp = 4.dp,
    val small: Dp = 8.dp,
    val medium: Dp = 16.dp,
    val large: Dp = 24.dp,
    val extraLarge: Dp = 32.dp,
    val heroPadding: Dp = 40.dp
)

data class ExpressiveShapes(
    val morphSmall: RoundedCornerShape = RoundedCornerShape(8.dp),
    val morphMedium: RoundedCornerShape = RoundedCornerShape(16.dp),
    val morphLarge: RoundedCornerShape = RoundedCornerShape(24.dp),
    val morphPill: RoundedCornerShape = RoundedCornerShape(999.dp),
    // Distinctive M3 Expressive Asymmetrical Corner Radii
    val asymmetricCard: RoundedCornerShape = RoundedCornerShape(
        topStart = 28.dp,
        topEnd = 12.dp,
        bottomEnd = 28.dp,
        bottomStart = 12.dp
    ),
    val asymmetricBubbleAi: RoundedCornerShape = RoundedCornerShape(
        topStart = 4.dp,
        topEnd = 24.dp,
        bottomEnd = 24.dp,
        bottomStart = 24.dp
    ),
    val asymmetricBubbleUser: RoundedCornerShape = RoundedCornerShape(
        topStart = 24.dp,
        topEnd = 4.dp,
        bottomEnd = 24.dp,
        bottomStart = 24.dp
    )
)

val LocalExpressiveSpacing = staticCompositionLocalOf { ExpressiveSpacing() }
val LocalExpressiveShapes = staticCompositionLocalOf { ExpressiveShapes() }

object ExpressiveThemeTokens {
    val spacing: ExpressiveSpacing
        @Composable
        @ReadOnlyComposable
        get() = LocalExpressiveSpacing.current

    val shapes: ExpressiveShapes
        @Composable
        @ReadOnlyComposable
        get() = LocalExpressiveShapes.current
}
