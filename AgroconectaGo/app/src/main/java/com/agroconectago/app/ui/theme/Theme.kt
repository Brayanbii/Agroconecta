package com.agroconectago.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val DeliveryColorScheme = lightColorScheme(
    primary = DeliveryPrimary,
    onPrimary = DeliverySurface,
    primaryContainer = DeliveryPrimaryContainer,
    onPrimaryContainer = Slate900,
    background = DeliverySurface,
    onBackground = Slate900,
    surface = DeliverySurface,
    onSurface = DeliveryOnSurface,
    surfaceVariant = DeliveryCard,
    onSurfaceVariant = Slate500,
    outline = DeliveryOutline
)

@Composable
fun AgroconectaGoTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DeliveryColorScheme,
        typography = DeliveryTypography,
        content = content
    )
}
