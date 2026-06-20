package com.agroconecta.app.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun FavoriteButton(
    isFavorite: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 36.dp,
    iconSize: Dp = 18.dp,
    shadowElevation: Dp = 4.dp,
    backgroundColor: Color = Color.White.copy(alpha = 0.95f),
    activeColor: Color = Color(0xFFEF4444),
    inactiveColor: Color = Color(0xFF94A3B8)
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    var prevFavorite by remember { mutableStateOf(isFavorite) }
    var showBurst by remember { mutableStateOf(false) }

    LaunchedEffect(isFavorite) {
        if (isFavorite && !prevFavorite) {
            showBurst = true
        }
        prevFavorite = isFavorite
    }

    val pressScale by animateFloatAsState(
        targetValue = if (isPressed) 0.82f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "scale"
    )

    val burstScale by animateFloatAsState(
        targetValue = if (showBurst) 1f else 0f,
        animationSpec = if (showBurst) tween(600, easing = EaseOutCubic) else tween(0),
        label = "burst"
    )

    val burstAlpha by animateFloatAsState(
        targetValue = if (showBurst) 0f else 0f,
        animationSpec = if (showBurst) tween(600, delayMillis = 200, easing = EaseInCubic) else tween(0),
        label = "burstAlpha"
    )

    val ringScale by animateFloatAsState(
        targetValue = if (showBurst) 2.2f else 0f,
        animationSpec = if (showBurst) tween(500, easing = EaseOutCubic) else tween(0),
        label = "ring"
    )

    val ringAlpha by animateFloatAsState(
        targetValue = if (showBurst) 0f else 0f,
        animationSpec = if (showBurst) tween(500, delayMillis = 100, easing = EaseInCubic) else tween(0),
        label = "ringAlpha"
    )

    LaunchedEffect(showBurst) {
        if (showBurst) {
            kotlinx.coroutines.delay(700)
            showBurst = false
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "favorite")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = if (isFavorite) 0.25f else 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow"
    )

    val particleColors = listOf(
        Color(0xFFEF4444), Color(0xFFF43F5E), Color(0xFFFB7185),
        Color(0xFFFBBF24), Color(0xFFF97316), Color(0xFFEC4899)
    )

    Box(modifier = modifier.size(size + 20.dp), contentAlignment = Alignment.Center) {
        if (showBurst) {
            Canvas(modifier = Modifier.size(size + 20.dp)) {
                val center = Offset(this.size.width / 2, this.size.height / 2)
                val maxRadius = (size.toPx() / 2) * 1.8f

                for (i in 0 until 8) {
                    val angle = (i * 45.0) * PI / 180.0
                    val distance = maxRadius * burstScale
                    val px = center.x + (cos(angle) * distance).toFloat()
                    val py = center.y + (sin(angle) * distance).toFloat()
                    val particleAlpha = (1f - burstScale).coerceIn(0f, 1f)
                    val particleSize = (3.dp.toPx()) * (1f - burstScale * 0.5f)

                    drawCircle(
                        color = particleColors[i % particleColors.size].copy(alpha = particleAlpha),
                        radius = particleSize.coerceAtLeast(0.5f),
                        center = Offset(px, py)
                    )
                }

                val ringRadius = (size.toPx() / 2) * ringScale
                val ringStroke = (2.dp.toPx()) * (1f - ringScale / 2.2f)
                if (ringAlpha > 0f && ringStroke > 0f) {
                    drawCircle(
                        color = activeColor.copy(alpha = ringAlpha),
                        radius = ringRadius.coerceAtLeast(0f),
                        center = center,
                        style = Stroke(width = ringStroke.coerceAtLeast(0.5f))
                    )
                }
            }
        }

        Surface(
            color = backgroundColor,
            shape = CircleShape,
            shadowElevation = shadowElevation,
            modifier = Modifier
                .size(size)
                .graphicsLayer {
                    scaleX = pressScale
                    scaleY = pressScale
                }
                .clickable(
                    interactionSource = interactionSource,
                    indication = null
                ) { onToggle() }
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                if (isFavorite) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                            .background(activeColor.copy(alpha = glowAlpha))
                    )
                }

                AnimatedContent(
                    targetState = isFavorite,
                    transitionSpec = {
                        scaleIn(
                            initialScale = 0f,
                            animationSpec = spring(dampingRatio = Spring.DampingRatioHighBouncy, stiffness = Spring.StiffnessMedium)
                        ) + fadeIn(animationSpec = tween(150)) togetherWith
                        scaleOut(
                            targetScale = 0f,
                            animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium)
                        ) + fadeOut(animationSpec = tween(100))
                    },
                    label = "favoriteIcon"
                ) { isFav ->
                    Icon(
                        if (isFav) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                        contentDescription = if (isFav) "Quitar de favoritos" else "Agregar a favoritos",
                        tint = if (isFav) activeColor else inactiveColor,
                        modifier = Modifier.size(iconSize)
                    )
                }
            }
        }
    }
}

@Composable
fun FavoriteButtonLarge(
    isFavorite: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    FavoriteButton(
        isFavorite = isFavorite,
        onToggle = onToggle,
        modifier = modifier,
        size = 44.dp,
        iconSize = 22.dp,
        shadowElevation = 6.dp
    )
}

@Composable
fun FavoriteButtonSmall(
    isFavorite: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    FavoriteButton(
        isFavorite = isFavorite,
        onToggle = onToggle,
        modifier = modifier,
        size = 32.dp,
        iconSize = 16.dp,
        shadowElevation = 3.dp
    )
}
