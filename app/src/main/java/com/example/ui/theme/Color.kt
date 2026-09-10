package com.example.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// Primary Ocean Blue & White Palette (#0077b6 base)
val OrangePrimary = Color(0xFF0077B6)
val OrangePrimaryDark = Color(0xFF005F9E)
val OrangePrimaryLight = Color(0xFF0096C7)
val OrangePrimaryContainer = Color(0xFFE0FAFF)
val OrangeOnPrimaryContainer = Color(0xFF03045E)
val OrangeSecondary = Color(0xFF00B4D8)

val WarmPeachSecondary = Color(0xFF00B4D8)
val WarmPeachSecondaryContainer = Color(0xFFE0FAFF)
val WarmPeachOnSecondaryContainer = Color(0xFF03045E)

val WarmAmberTertiary = Color(0xFFF59E0B)
val WarmAmberTertiaryContainer = Color(0xFFFEF3C7)
val WarmAmberOnTertiaryContainer = Color(0xFF78350F)

// Light Clean Background & Surfaces (Crisp White + Warm Accents)
val PureWhite = Color(0xFFFFFFFF)
val WarmOffWhite = Color(0xFFFAFAF8)
val WarmSurfaceVariant = Color(0xFFF6F3EE)
val WarmBorder = Color(0xFFEADBCE)
val WarmTextDark = Color(0xFF1C1917)
val WarmTextMuted = Color(0xFF78716C)

// Backward compatible aliases mapped to the Orange & White theme
val NavyPrimary = OrangePrimary
val NavyOnPrimary = PureWhite
val NavyPrimaryContainer = OrangePrimaryContainer
val NavyOnPrimaryContainer = OrangeOnPrimaryContainer

val TealSecondary = OrangePrimaryDark
val TealOnSecondary = PureWhite
val TealSecondaryContainer = WarmPeachSecondaryContainer
val TealOnSecondaryContainer = WarmPeachOnSecondaryContainer

val AmberTertiary = WarmAmberTertiary
val AmberOnTertiary = PureWhite
val AmberTertiaryContainer = WarmAmberTertiaryContainer
val AmberOnTertiaryContainer = WarmAmberOnTertiaryContainer

val SlateBackground = WarmOffWhite
val SlateOnBackground = WarmTextDark
val SlateSurface = PureWhite
val SlateOnSurface = WarmTextDark
val SlateSurfaceVariant = WarmSurfaceVariant
val SlateOnSurfaceVariant = WarmTextMuted
val SlateOutline = WarmBorder

// Solid Black & High-Contrast text for forms and inputs
val InputTextBlack = Color(0xFF111827)
val InputLabelDark = Color(0xFF374151)
val InputPlaceholderMuted = Color(0xFF9CA3AF)
val InputBorderDefault = Color(0xFFD1D5DB)

// Status colors aligned with warm theme
val SuccessGreen = Color(0xFF10B981)
val WarningAmber = Color(0xFFF59E0B)
val DangerRed = Color(0xFFEF4444)
val InfoSky = Color(0xFF38BDF8)
val PurpleAccent = Color(0xFFA855F7)
val PrimaryViolet = Color(0xFF6750A4)

// Beautiful Gradients
val VibrantOrangeGradient = Brush.horizontalGradient(
    colors = listOf(Color(0xFF0077B6), Color(0xFF0096C7), Color(0xFF00B4D8))
)
val SunsetOrangeGradient = Brush.horizontalGradient(
    colors = listOf(Color(0xFF005F9E), Color(0xFF0077B6), Color(0xFF0096C7))
)
val CardWarmGradient = Brush.verticalGradient(
    colors = listOf(Color(0xFFF0F9FF), Color(0xFFFFFFFF))
)
val HeroBannerGradient = Brush.linearGradient(
    colors = listOf(Color(0xFF0077B6), Color(0xFF0096C7), Color(0xFF00B4D8))
)

// Dark Palette (Warm Charcoal + Ocean Blue)
val DarkNavyPrimary = Color(0xFF00B4D8)
val DarkNavyOnPrimary = Color(0xFF1C1917)
val DarkNavyPrimaryContainer = Color(0xFF03045E)
val DarkNavyOnPrimaryContainer = Color(0xFFE0FAFF)

val DarkTealSecondary = Color(0xFF0096C7)
val DarkTealOnSecondary = Color(0xFFE0FAFF)
val DarkTealSecondaryContainer = Color(0xFF03045E)
val DarkTealOnSecondaryContainer = Color(0xFFE0FAFF)

val DarkAmberTertiary = Color(0xFFFBBF24)
val DarkAmberOnTertiary = Color(0xFF451A03)
val DarkAmberTertiaryContainer = Color(0xFF92400E)
val DarkAmberOnTertiaryContainer = Color(0xFFFEF3C7)

val DarkSlateBackground = Color(0xFF141210)
val DarkSlateOnBackground = Color(0xFFFAFAF9)
val DarkSlateSurface = Color(0xFF1F1D1A)
val DarkSlateOnSurface = Color(0xFFFAFAF9)
val DarkSlateSurfaceVariant = Color(0xFF2B2824)
val DarkSlateOnSurfaceVariant = Color(0xFFA8A29E)
val DarkSlateOutline = Color(0xFF44403C)


