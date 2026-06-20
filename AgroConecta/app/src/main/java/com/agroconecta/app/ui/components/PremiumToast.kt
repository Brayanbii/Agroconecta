package com.agroconecta.app.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

enum class ToastType {
    SUCCESS, ERROR, INFO, FAVORITE, CART
}

data class ToastData(
    val message: String,
    val type: ToastType,
    val icon: ImageVector? = null,
    val duration: Long = 1500L
)

@Composable
fun PremiumToast(
    toast: ToastData,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isVisible by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()
    
    LaunchedEffect(toast.duration) {
        kotlinx.coroutines.delay(toast.duration)
        isVisible = false
        kotlinx.coroutines.delay(400)
        onDismiss()
    }
    
    val infiniteTransition = rememberInfiniteTransition(label = "toast")
    val shimmer by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = EaseInOutCubic),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer"
    )
    
    var progress by remember { mutableFloatStateOf(1f) }
    LaunchedEffect(toast.duration) {
        val startTime = System.currentTimeMillis()
        while (true) {
            val elapsed = System.currentTimeMillis() - startTime
            progress = (1f - (elapsed.toFloat() / toast.duration)).coerceIn(0f, 1f)
            if (progress <= 0f) break
            kotlinx.coroutines.delay(16)
        }
    }

    val (bgColor, iconColor, defaultIcon) = when (toast.type) {
        ToastType.SUCCESS -> Triple(
            Brush.linearGradient(listOf(Color(0xFF10B981), Color(0xFF059669))),
            Color.White,
            Icons.Filled.CheckCircle
        )
        ToastType.ERROR -> Triple(
            Brush.linearGradient(listOf(Color(0xFFEF4444), Color(0xFFDC2626))),
            Color.White,
            Icons.Filled.Error
        )
        ToastType.INFO -> Triple(
            Brush.linearGradient(listOf(Color(0xFF3B82F6), Color(0xFF2563EB))),
            Color.White,
            Icons.Filled.Info
        )
        ToastType.FAVORITE -> Triple(
            Brush.linearGradient(listOf(Color(0xFFF43F5E), Color(0xFFE11D48))),
            Color.White,
            Icons.Filled.Favorite
        )
        ToastType.CART -> Triple(
            Brush.linearGradient(listOf(Color(0xFF22C55E), Color(0xFF16A34A))),
            Color.White,
            Icons.Filled.ShoppingCart
        )
    }

    val displayIcon = toast.icon ?: defaultIcon

    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn(animationSpec = tween(250, easing = EaseOutCubic)) +
                slideInVertically(
                    initialOffsetY = { -it },
                    animationSpec = tween(300, easing = EaseOutBack)
                ) +
                scaleIn(
                    initialScale = 0.9f,
                    animationSpec = tween(300, easing = EaseOutBack)
                ),
        exit = fadeOut(animationSpec = tween(400, easing = EaseInCubic)) +
               slideOutVertically(
                   targetOffsetY = { -it / 3 },
                   animationSpec = tween(400, easing = EaseInCubic)
               ) +
               scaleOut(
                   targetScale = 0.92f,
                   animationSpec = tween(400, easing = EaseInCubic)
               ),
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                shadowElevation = 12.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .background(bgColor)
                        .fillMaxWidth()
                ) {
                    ShimmerOverlay(shimmerProgress = shimmer, visible = toast.type == ToastType.SUCCESS || toast.type == ToastType.CART)
                    
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.White.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                displayIcon,
                                contentDescription = null,
                                tint = iconColor,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = toast.message,
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                lineHeight = 18.sp
                            )
                        }

                        IconButton(
                            onClick = { 
                                isVisible = false
                                scope.launch {
                                    kotlinx.coroutines.delay(400)
                                    onDismiss()
                                }
                            },
                            modifier = Modifier
                                .size(28.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color.White.copy(alpha = 0.15f))
                        ) {
                            Icon(
                                Icons.Filled.Close,
                                contentDescription = "Cerrar",
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                    
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(3.dp)
                            .background(Color.White.copy(alpha = 0.1f))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(progress)
                                .height(3.dp)
                                .background(Color.White.copy(alpha = 0.4f))
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ShimmerOverlay(shimmerProgress: Float, visible: Boolean) {
    if (!visible) return
    
    val shimmerWidth = 100.dp
    val offset = shimmerProgress * (shimmerWidth.value * 3)
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(
                Brush.horizontalGradient(
                    colors = listOf(
                        Color.Transparent,
                        Color.White.copy(alpha = 0.1f),
                        Color.Transparent
                    ),
                    startX = offset - shimmerWidth.value,
                    endX = offset
                )
            )
    )
}

@Composable
fun PremiumToastHost(
    toastState: State<ToastData?>,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val currentToast = toastState.value
    
    LaunchedEffect(currentToast) {
        currentToast?.let { toast ->
            kotlinx.coroutines.delay(toast.duration)
            onDismiss()
        }
    }
    
    Box(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(top = 8.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        currentToast?.let { toast ->
            key(toast.message + toast.type) {
                PremiumToast(
                    toast = toast,
                    onDismiss = onDismiss
                )
            }
        }
    }
}
