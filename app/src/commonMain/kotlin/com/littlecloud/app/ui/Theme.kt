package com.littlecloud.app.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import com.littlecloud.app.resources.*
import org.jetbrains.compose.resources.Font

data class LcColors(
    val bg: Color,
    val surface: Color,
    val line: Color,
    val ink: Color,
    val muted: Color,
    val accent: Color,
    val soft: Color,
    /** Rubrics (directions not said aloud), red as in the old books. */
    val rubric: Color,
    val on: Color,
    val dark: Boolean,
)

// The Carmelite habit: brown and white on a quiet stone ground.
val LightLc = LcColors(
    bg = Color(0xFFF2F0EB), surface = Color(0xFFFBFAF7), line = Color(0xFFDDD6CB),
    ink = Color(0xFF2A2420), muted = Color(0xFF665C53), accent = Color(0xFF6A4632),
    soft = Color(0xFFE8E0D4), rubric = Color(0xFF9B2D20), on = Color(0xFFFBFAF7), dark = false,
)
val DarkLc = LcColors(
    bg = Color(0xFF151311), surface = Color(0xFF201C19), line = Color(0xFF38322C),
    ink = Color(0xFFEDE6DD), muted = Color(0xFFA79C90), accent = Color(0xFFD2B28F),
    soft = Color(0xFF2E2721), rubric = Color(0xFFE0907E), on = Color(0xFF151311), dark = true,
)

val LocalLc = staticCompositionLocalOf { LightLc }
val LocalLcFont = staticCompositionLocalOf<FontFamily> { FontFamily.Serif }

val Lc: LcColors
    @Composable @ReadOnlyComposable get() = LocalLc.current

val LcFont: FontFamily
    @Composable @ReadOnlyComposable get() = LocalLcFont.current

/** Forum, bundled with the app (composeResources/font/forum_regular.ttf). */
@Composable
fun forumFamily(): FontFamily = FontFamily(Font(Res.font.forum_regular))

@Composable
fun LittleCloudTheme(dark: Boolean, content: @Composable () -> Unit) {
    val c = if (dark) DarkLc else LightLc
    val family = forumFamily()
    val scheme = if (dark) {
        darkColorScheme(
            primary = c.accent, onPrimary = c.on, background = c.bg, onBackground = c.ink,
            surface = c.surface, onSurface = c.ink, surfaceVariant = c.soft, onSurfaceVariant = c.muted,
            outline = c.line, secondary = c.accent, surfaceContainerHigh = c.surface, surfaceContainerHighest = c.soft,
        )
    } else {
        lightColorScheme(
            primary = c.accent, onPrimary = c.on, background = c.bg, onBackground = c.ink,
            surface = c.surface, onSurface = c.ink, surfaceVariant = c.soft, onSurfaceVariant = c.muted,
            outline = c.line, secondary = c.accent, surfaceContainerHigh = c.surface, surfaceContainerHighest = c.soft,
        )
    }
    val b = Typography()
    val typo = Typography(
        displayLarge = b.displayLarge.copy(fontFamily = family),
        displayMedium = b.displayMedium.copy(fontFamily = family),
        displaySmall = b.displaySmall.copy(fontFamily = family),
        headlineLarge = b.headlineLarge.copy(fontFamily = family),
        headlineMedium = b.headlineMedium.copy(fontFamily = family),
        headlineSmall = b.headlineSmall.copy(fontFamily = family),
        titleLarge = b.titleLarge.copy(fontFamily = family),
        titleMedium = b.titleMedium.copy(fontFamily = family),
        titleSmall = b.titleSmall.copy(fontFamily = family),
        bodyLarge = b.bodyLarge.copy(fontFamily = family),
        bodyMedium = b.bodyMedium.copy(fontFamily = family),
        bodySmall = b.bodySmall.copy(fontFamily = family),
        labelLarge = b.labelLarge.copy(fontFamily = family),
        labelMedium = b.labelMedium.copy(fontFamily = family),
        labelSmall = b.labelSmall.copy(fontFamily = family),
    )
    CompositionLocalProvider(LocalLc provides c, LocalLcFont provides family) {
        MaterialTheme(colorScheme = scheme, typography = typo, content = content)
    }
}
