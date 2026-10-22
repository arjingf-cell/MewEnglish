
package com.example.ngilizcequizapp

import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.Modifier
import androidx.compose.foundation.background
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

val LocalIsDarkMode = compositionLocalOf { false }

@Immutable
data class AppThemeColors(
    val background: Color,
    val backgroundAlt: Color,
    val surface: Color,
    val surfaceRaised: Color,
    val primary: Color,
    val primarySoft: Color,
    val text: Color,
    val textMuted: Color,
    val border: Color,
    val success: Color,
    val warning: Color,
    val danger: Color
)

object MewThemeEngine {
    val Light = AppThemeColors(
        background = Color(0xFFF8FAFC),
        backgroundAlt = Color(0xFFEAF7FF),
        surface = Color.White,
        surfaceRaised = Color(0xFFF5FBFF),
        primary = Color(0xFF1CB0F6),
        primarySoft = Color(0xFFE0F2FE),
        text = Color(0xFF1E293B),
        textMuted = Color(0xFF64748B),
        border = Color(0xFFE2E8F0),
        success = Color(0xFF2E9D78),
        warning = Color(0xFFF59E0B),
        danger = Color(0xFFE85D75)
    )

    val Dark = AppThemeColors(
        background = Color(0xFF07111D),
        backgroundAlt = Color(0xFF0D1B2A),
        surface = Color(0xFF111D2B),
        surfaceRaised = Color(0xFF172A3D),
        primary = Color(0xFF35C2FF),
        primarySoft = Color(0xFF12344A),
        text = Color(0xFFEAF6FF),
        textMuted = Color(0xFFA8BED2),
        border = Color(0xFF24445F),
        success = Color(0xFF55D6A7),
        warning = Color(0xFFFFC857),
        danger = Color(0xFFFF7A90)
    )

    fun colors(darkMode: Boolean): AppThemeColors = if (darkMode) Dark else Light
}

val LocalAppThemeColors = compositionLocalOf { MewThemeEngine.Light }

@Composable
fun Modifier.mewScreenBackground(): Modifier {
    val context = LocalContext.current
    val isSpaceTheme = remember(UserCosmeticsStore.stateRevision) {
        UserCosmeticsStore.isSpaceThemeActive(context)
    }
    val isKingdomTheme = remember(UserCosmeticsStore.stateRevision) {
        UserCosmeticsStore.isKingdomThemeActive(context)
    }
    if (isSpaceTheme || isKingdomTheme) {
        return background(Color.Transparent)
    }
    val colors = LocalAppThemeColors.current
    return background(Brush.verticalGradient(listOf(colors.background, colors.backgroundAlt)))
}
