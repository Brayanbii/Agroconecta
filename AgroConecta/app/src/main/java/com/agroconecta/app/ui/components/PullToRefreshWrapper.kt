package com.agroconecta.app.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

private val Green500 = Color(0xFF22C55E)
private val Green600 = Color(0xFF16A34A)

@Composable
fun PullToRefreshWrapper(
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
    isAtTop: Boolean = true,
    content: @Composable () -> Unit
) {
    var pullOffset by remember { mutableFloatStateOf(0f) }
    val threshold = 120f
    var shouldRefresh by remember { mutableStateOf(false) }

    LaunchedEffect(shouldRefresh) {
        if (shouldRefresh && !isRefreshing) {
            shouldRefresh = false
            pullOffset = threshold
            onRefresh()
            delay(400)
            pullOffset = 0f
        }
    }

    val animatedOffset by animateFloatAsState(
        targetValue = if (isRefreshing) threshold else pullOffset.coerceIn(0f, threshold),
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
    )
    val progress = (animatedOffset / threshold).coerceIn(0f, 1f)

    val connection = remember(isAtTop, isRefreshing) {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                if (pullOffset > 0f && available.y < 0f) {
                    pullOffset = (pullOffset + available.y).coerceAtLeast(0f)
                    return Offset(0f, available.y)
                }
                return Offset.Zero
            }

            override fun onPostScroll(consumed: Offset, available: Offset, source: NestedScrollSource): Offset {
                if (!isRefreshing && isAtTop && available.y > 0f && source == NestedScrollSource.Drag) {
                    pullOffset = (pullOffset + available.y * 0.35f).coerceAtMost(threshold * 2f)
                    return Offset(0f, available.y)
                }
                return Offset.Zero
            }

            override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity {
                if (pullOffset >= threshold && !isRefreshing) {
                    shouldRefresh = true
                } else {
                    pullOffset = 0f
                }
                return Velocity.Zero
            }
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "ptr")
    val rotation by infiniteTransition.animateFloat(0f, 360f, infiniteRepeatable(tween(1200, easing = LinearEasing), RepeatMode.Restart), label = "spin")

    Box(modifier = modifier.nestedScroll(connection)) {
        // Pull indicator at top
        if (progress > 0.02f || isRefreshing) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height((animatedOffset * 0.7f + if (isRefreshing) 56f else 0f).dp)
                    .then(if (isRefreshing) Modifier.background(Brush.verticalGradient(listOf(Green500.copy(alpha = 0.06f), Color.Transparent))) else Modifier)
                    .align(Alignment.TopCenter),
                contentAlignment = Alignment.Center
            ) {
                if (isRefreshing || shouldRefresh) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(modifier = Modifier.size(26.dp), color = Green500, strokeWidth = 2.5.dp)
                        Spacer(Modifier.height(6.dp))
                        Text("Recargando...", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Green600)
                    }
                } else if (progress > 0.1f) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Filled.Refresh, null,
                            tint = Green500.copy(alpha = progress),
                            modifier = Modifier.size((18 + 12 * progress).dp).graphicsLayer { rotationZ = rotation * progress }
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            if (progress > 0.75f) "Suelta para recargar" else "Tira para recargar",
                            fontSize = 11.sp,
                            fontWeight = if (progress > 0.75f) FontWeight.Bold else FontWeight.Medium,
                            color = if (progress > 0.75f) Green500.copy(alpha = progress) else Color(0xFF94A3B8).copy(alpha = progress)
                        )
                    }
                }
            }
        }

        content()
    }
}
