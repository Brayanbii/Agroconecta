package com.agroconecta.app.ui.theme

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

val LocalAppRole = compositionLocalOf { "CLIENTE" }

val PremiumTypography = androidx.compose.material3.Typography(
    displayLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 57.sp,
        lineHeight = 64.sp,
        letterSpacing = (-2.0).sp
    ),
    displayMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 45.sp,
        lineHeight = 52.sp,
        letterSpacing = (-1.5).sp
    ),
    displaySmall = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 36.sp,
        lineHeight = 44.sp,
        letterSpacing = (-1.0).sp
    ),
    headlineLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 32.sp,
        lineHeight = 40.sp,
        letterSpacing = (-0.8).sp
    ),
    headlineMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp,
        lineHeight = 36.sp,
        letterSpacing = (-0.4).sp
    ),
    headlineSmall = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 24.sp,
        lineHeight = 32.sp,
        letterSpacing = (-0.2).sp
    ),
    titleLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = (-0.2).sp
    ),
    titleMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.sp
    ),
    titleSmall = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    ),
    bodySmall = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.2.sp
    ),
    labelLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        lineHeight = 18.sp,
        letterSpacing = 0.1.sp
    ),
    labelMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.2.sp
    ),
    labelSmall = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 10.sp,
        lineHeight = 14.sp,
        letterSpacing = 0.5.sp
    )
)

private val LightColorScheme = lightColorScheme(
    primary = AgroPrimary,
    onPrimary = AgroOnPrimary,
    primaryContainer = AgroPrimaryContainer,
    onPrimaryContainer = AgroOnBackground,
    background = AgroBackground,
    onBackground = AgroOnBackground,
    surface = AgroSurface,
    onSurface = AgroOnSurface,
    surfaceVariant = AgroSurfaceVariant,
    onSurfaceVariant = AgroOnSurfaceVariant,
    outline = AgroOutline
)

class PremiumColors(
    brand600: Color,
    brand500: Color,
    brand900: Color,
    glow: Color,
    shadow: Color,
    background: Color
) {
    var brand600 by mutableStateOf(brand600)
    var brand500 by mutableStateOf(brand500)
    var brand900 by mutableStateOf(brand900)
    var glow by mutableStateOf(glow)
    var shadow by mutableStateOf(shadow)
    var background by mutableStateOf(background)
}

val LocalPremiumColors = staticCompositionLocalOf<PremiumColors> {
    error("No PremiumColors provided")
}

@Composable
fun AgroConectaTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    role: String = "CLIENTE",
    content: @Composable () -> Unit
) {
    val targetBrand600 = if (role == "CAMPESINO") CampesinoBrand600 else ClienteBrand600
    val targetBrand500 = if (role == "CAMPESINO") CampesinoBrand500 else ClienteBrand500
    val targetBrand900 = if (role == "CAMPESINO") CampesinoBrand900 else ClienteBrand900
    val targetGlow = if (role == "CAMPESINO") CampesinoGlow else ClienteGlow
    val targetShadow = if (role == "CAMPESINO") CampesinoShadow else ClienteShadow
    val targetBg = if (darkTheme) Obsidian else CrispWhite

    val brand600 by animateColorAsState(targetBrand600, tween(500), label = "color")
    val brand500 by animateColorAsState(targetBrand500, tween(500), label = "color")
    val brand900 by animateColorAsState(targetBrand900, tween(500), label = "color")
    val glow by animateColorAsState(targetGlow, tween(500), label = "color")
    val shadow by animateColorAsState(targetShadow, tween(500), label = "color")
    val bg by animateColorAsState(targetBg, tween(500), label = "color")

    val premiumColors = remember { PremiumColors(brand600, brand500, brand900, glow, shadow, bg) }
    premiumColors.brand600 = brand600
    premiumColors.brand500 = brand500
    premiumColors.brand900 = brand900
    premiumColors.glow = glow
    premiumColors.shadow = shadow
    premiumColors.background = bg

    CompositionLocalProvider(
        LocalAppRole provides role,
        LocalPremiumColors provides premiumColors
    ) {
        MaterialTheme(
            colorScheme = LightColorScheme,
            typography = PremiumTypography,
            content = content
        )
    }
}