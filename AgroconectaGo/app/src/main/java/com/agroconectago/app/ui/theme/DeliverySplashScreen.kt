package com.agroconectago.app.ui.theme

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Motorcycle
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

@Composable
fun DeliverySplashScreen(onFinished: () -> Unit) {
    // ═══════════════════════════════════════════════════════════════
    // LOGIC PRESERVED - SESSION CHECK & NAVIGATION
    // ═══════════════════════════════════════════════════════════════
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        visible = true
        delay(2200)
        onFinished()
    }

    // ═══════════════════════════════════════════════════════════════
    // ANIMATIONS - APPLE 2026 AESTHETIC
    // ═══════════════════════════════════════════════════════════════
    
    // Spring scale animation: 0.7f → 1.0f with elastic bounce
    val logoScale by animateFloatAsState(
        targetValue = if (visible) 1f else 0.7f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "logoScale"
    )
    
    // Glacial fade-in: 0f → 1f
    val logoAlpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(800, easing = EaseOutCubic),
        label = "logoAlpha"
    )
    
    // Subtle infinite pulse for loading indication (0.8f ↔ 1.0f)
    val infiniteTransition = rememberInfiniteTransition(label = "splash")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = EaseInOutCubic),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )
    
    // Soft glow animation
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = EaseInOutCubic),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow"
    )
    
    // Footer text fade-in
    val footerAlpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(1000, delayMillis = 600, easing = EaseOutCubic),
        label = "footerAlpha"
    )

    // ═══════════════════════════════════════════════════════════════
    // UI - ULTRA MINIMALIST PREMIUM
    // ═══════════════════════════════════════════════════════════════
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFFFFFFFF),
                        Color(0xFFFAFBFA),
                        Color(0xFFF5F7F5)
                    ),
                    center = Offset(0.5f, 0.5f),
                    radius = 1.5f
                )
            )
    ) {
        // Subtle organic background glow
        Box(
            modifier = Modifier
                .fillMaxSize()
                .drawBehind {
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                DeliveryBrand600.copy(alpha = glowAlpha * 0.08f),
                                Color.Transparent
                            )
                        ),
                        radius = size.width * 0.5f,
                        center = Offset(size.width * 0.5f, size.height * 0.5f)
                    )
                }
        )

        // Logo container - geometric center
        Box(
            modifier = Modifier
                .fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .graphicsLayer {
                        scaleX = logoScale
                        scaleY = logoScale
                        alpha = logoAlpha * pulseAlpha
                    }
                    .clip(RoundedCornerShape(28.dp))
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                DeliveryBrand500,
                                DeliveryBrand600,
                                DeliveryBrand900
                            )
                        )
                    )
                    .shadow(
                        elevation = 40.dp,
                        shape = RoundedCornerShape(28.dp),
                        spotColor = DeliveryBrand600.copy(alpha = glowAlpha),
                        ambientColor = DeliveryBrand600.copy(alpha = glowAlpha * 0.5f)
                    ),
                contentAlignment = Alignment.Center
            ) {
                // Glass overlay effect
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    Color.White.copy(alpha = 0.2f),
                                    Color.Transparent
                                ),
                                center = Offset(60f, 30f),
                                radius = 90f
                            )
                        )
                )
                
                Icon(
                    imageVector = Icons.Filled.Motorcycle,
                    contentDescription = "AgroConectaGo",
                    tint = Color.White,
                    modifier = Modifier.size(56.dp)
                )
            }
        }

        // "Hecho en Colombia" - subtle footer
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 48.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            Text(
                text = "Hecho en Colombia",
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF94A3B8),
                letterSpacing = 0.5.sp,
                modifier = Modifier.graphicsLayer { alpha = footerAlpha }
            )
        }
    }
}
