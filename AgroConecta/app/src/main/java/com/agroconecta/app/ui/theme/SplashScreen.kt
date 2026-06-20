package com.agroconecta.app.ui.theme

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun SplashScreen(onFinished: () -> Unit) {
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        visible = true
        delay(2200)
        onFinished()
    }

    val logoScale by animateFloatAsState(
        targetValue = if (visible) 1f else 0.3f,
        animationSpec = tween(800, easing = EaseOutBack),
        label = "logoScale"
    )
    val logoAlpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(600, delayMillis = 100),
        label = "logoAlpha"
    )
    val textAlpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(600, delayMillis = 400),
        label = "textAlpha"
    )
    val textOffset by animateFloatAsState(
        targetValue = if (visible) 0f else 30f,
        animationSpec = tween(700, delayMillis = 400, easing = EaseOutCubic),
        label = "textOffset"
    )
    val taglineAlpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(600, delayMillis = 700),
        label = "taglineAlpha"
    )
    val shimmerAlpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(800, delayMillis = 1000),
        label = "shimmerAlpha"
    )

    val infiniteTransition = rememberInfiniteTransition(label = "splash")
    val glowPulse by infiniteTransition.animateFloat(
        initialValue = 0.4f, targetValue = 0.8f,
        animationSpec = infiniteRepeatable(tween(2000), RepeatMode.Reverse),
        label = "glow"
    )
    val particlePhase by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(8000), RepeatMode.Restart),
        label = "particles"
    )
    val auroraShift by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(6000), RepeatMode.Reverse),
        label = "aurora"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF022C22),
                        Color(0xFF064E3B),
                        Color(0xFF065F46),
                        Color(0xFF047857)
                    )
                )
            )
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawAuroraBackground(this, auroraShift)
        }

        Canvas(modifier = Modifier.fillMaxSize()) {
            drawFloatingParticles(this, particlePhase)
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .drawBehind {
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color(0xFF22C55E).copy(alpha = glowPulse * 0.12f),
                                Color.Transparent
                            ),
                            center = Offset(size.width * 0.5f, size.height * 0.45f),
                            radius = size.width * 0.6f
                        )
                    )
                }
        )

        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .scale(logoScale)
                    .alpha(logoAlpha)
            ) {
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(RoundedCornerShape(28.dp))
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    Color(0xFF22C55E),
                                    Color(0xFF16A34A),
                                    Color(0xFF15803D)
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                brush = Brush.radialGradient(
                                    colors = listOf(
                                        Color.White.copy(alpha = 0.15f),
                                        Color.Transparent
                                    ),
                                    center = Offset(60f, 30f),
                                    radius = 80f
                                )
                            )
                    )
                    Icon(
                        imageVector = Icons.Filled.Eco,
                        contentDescription = "AgroConecta",
                        tint = Color.White,
                        modifier = Modifier.size(48.dp)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Box(
                    modifier = Modifier
                        .width(60.dp)
                        .height(3.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color(0xFF22C55E).copy(alpha = 0.6f),
                                    Color.Transparent
                                )
                            )
                        )
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.alpha(textAlpha)
            ) {
                Text(
                    text = "AgroConecta",
                    fontSize = 36.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    letterSpacing = (-1.2).sp,
                    modifier = Modifier.offset(y = textOffset.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Del campo colombiano directo a tu mesa",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White.copy(alpha = 0.6f),
                    letterSpacing = 0.3.sp,
                    modifier = Modifier.alpha(taglineAlpha)
                )
            }

            Spacer(modifier = Modifier.height(48.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.alpha(shimmerAlpha)
            ) {
                repeat(3) { index ->
                    val dotAlpha by infiniteTransition.animateFloat(
                        initialValue = 0.3f,
                        targetValue = 1f,
                        animationSpec = infiniteRepeatable(
                            tween(600, delayMillis = index * 200),
                            RepeatMode.Reverse
                        ),
                        label = "dot$index"
                    )
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = dotAlpha * 0.5f))
                    )
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 40.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            Text(
                text = "v1.0  ·  Hecho en Colombia",
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White.copy(alpha = 0.3f),
                letterSpacing = 0.5.sp,
                modifier = Modifier.alpha(shimmerAlpha)
            )
        }
    }
}

private fun drawAuroraBackground(scope: DrawScope, phase: Float) {
    val w = scope.size.width
    val h = scope.size.height

    scope.drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                Color(0xFF10B981).copy(alpha = 0.08f + phase * 0.04f),
                Color.Transparent
            ),
            center = Offset(w * 0.2f, h * 0.3f),
            radius = w * 0.5f
        )
    )
    scope.drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                Color(0xFF06B6D4).copy(alpha = 0.06f + (1f - phase) * 0.03f),
                Color.Transparent
            ),
            center = Offset(w * 0.8f, h * 0.2f),
            radius = w * 0.4f
        )
    )
    scope.drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                Color(0xFF22C55E).copy(alpha = 0.05f + phase * 0.03f),
                Color.Transparent
            ),
            center = Offset(w * 0.5f, h * 0.7f),
            radius = w * 0.6f
        )
    )
}

private fun drawFloatingParticles(scope: DrawScope, phase: Float) {
    val w = scope.size.width
    val h = scope.size.height
    val particles = listOf(
        Triple(0.15f, 0.25f, 3f),
        Triple(0.85f, 0.15f, 2.5f),
        Triple(0.3f, 0.7f, 2f),
        Triple(0.7f, 0.6f, 3.5f),
        Triple(0.5f, 0.4f, 2f),
        Triple(0.2f, 0.85f, 1.5f),
        Triple(0.9f, 0.75f, 2.5f),
        Triple(0.4f, 0.1f, 1.8f),
        Triple(0.6f, 0.9f, 2.2f),
        Triple(0.1f, 0.5f, 1.5f),
        Triple(0.75f, 0.35f, 2f),
        Triple(0.35f, 0.45f, 1.8f)
    )

    particles.forEachIndexed { index, (xFrac, yFrac, radius) ->
        val timeOffset = index * 0.5f
        val yOffset = (sin((phase + timeOffset) * 2.0 * PI) * 20.0).toFloat()
        val xOffset = (cos((phase + timeOffset) * 1.5 * PI) * 10.0).toFloat()
        val alpha = (0.15 + (sin((phase + timeOffset) * 2.0 * PI) * 0.5 + 0.5) * 0.2).toFloat()

        scope.drawCircle(
            color = Color.White.copy(alpha = alpha),
            radius = radius * (1f + (sin((phase + timeOffset) * PI) * 0.2).toFloat()),
            center = Offset(
                w * xFrac + xOffset,
                h * yFrac + yOffset
            )
        )
    }
}