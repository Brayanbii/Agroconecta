package com.agroconecta.app.ui.theme

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.agroconecta.app.data.model.CarritoItem
import com.agroconecta.app.viewmodel.TiendaViewModel

private val DarkSlate = Color(0xFF0F172A)
private val Slate700 = Color(0xFF334155)
private val Slate500 = Color(0xFF64748B)
private val Slate400 = Color(0xFF94A3B8)
private val Slate300 = Color(0xFFCBD5E1)
private val Slate200 = Color(0xFFE2E8F0)
private val Slate100 = Color(0xFFF1F5F9)
private val Brand600 = Color(0xFF16A34A)
private val Brand500 = Color(0xFF22C55E)
private val Brand400 = Color(0xFF4ADE80)
private val Brand50 = Color(0xFFF0FDF4)
private val SurfacePure = Color(0xFFF8FAF9)
private val Red500 = Color(0xFFEF4444)
private val Red50 = Color(0xFFFEF2F2)
private val Indigo500 = Color(0xFF6366F1)
private val Purple500 = Color(0xFF8B5CF6)
private val Amber500 = Color(0xFFF59E0B)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CarritoScreen(
    tiendaVM: TiendaViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToCheckout: () -> Unit,
    onNavigateToTienda: () -> Unit
) {
    val context = LocalContext.current
    LaunchedEffect(Unit) { tiendaVM.cargarCarrito() }

    val items = tiendaVM.carritoItems
    val subtotal = tiendaVM.carritoSubtotal
    val envio = if (subtotal > 0) 3500.0 else 0.0
    val total = subtotal + envio
    var isVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { isVisible = true }

    // Optimistic local state
    var localItems by remember { mutableStateOf(items) }
    var localSubtotal by remember { mutableStateOf(subtotal) }
    LaunchedEffect(items, subtotal) {
        localItems = items
        localSubtotal = subtotal
    }

    Scaffold(
        containerColor = SurfacePure,
        topBar = {
            AnimatedVisibility(
                visible = isVisible,
                enter = fadeIn(tween(400)) + slideInVertically(tween(400, easing = EaseOutCubic), initialOffsetY = { -it / 4 })
            ) {
                Surface(
                    color = Color.White,
                    shadowElevation = 0.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .statusBarsPadding()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        var backScale by remember { mutableStateOf(1f) }
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(Slate100)
                                .graphicsLayer { scaleX = backScale; scaleY = backScale }
                                .clickable {
                                    backScale = 0.9f
                                    onNavigateBack()
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver", tint = DarkSlate, modifier = Modifier.size(22.dp))
                        }
                        LaunchedEffect(backScale) {
                            if (backScale != 1f) {
                                kotlinx.coroutines.delay(100)
                                backScale = 1f
                            }
                        }
                        Spacer(Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "Mi Carrito",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = DarkSlate,
                                letterSpacing = (-0.5).sp
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                val infiniteTransition = rememberInfiniteTransition(label = "topbar")
                                val pulseAnim by infiniteTransition.animateFloat(
                                    initialValue = 0.6f,
                                    targetValue = 1f,
                                    animationSpec = infiniteRepeatable(tween(1500), RepeatMode.Reverse)
                                )
                                Box(
                                    modifier = Modifier
                                        .size(7.dp)
                                        .clip(CircleShape)
                                        .background(Brand600.copy(alpha = pulseAnim))
                                )
                                Spacer(Modifier.width(6.dp))
                                Text(
                                    "${localItems.size} productos",
                                    fontSize = 12.sp,
                                    color = Slate500,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                        if (localItems.isNotEmpty()) {
                            var btnScale by remember { mutableStateOf(1f) }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(
                                        Brush.linearGradient(
                                            colors = listOf(Red500, Color(0xFFDC2626))
                                        )
                                    )
                                    .graphicsLayer { scaleX = btnScale; scaleY = btnScale }
                                    .clickable {
                                    btnScale = 0.95f
                                    localItems = emptyList()
                                    localSubtotal = 0.0
                                    tiendaVM.limpiarCarritoCompleto()
                                },
                                contentAlignment = Alignment.Center
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Filled.DeleteSweep, null, tint = Color.White, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(6.dp))
                                    Text("Vaciar", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                }
                            }
                            LaunchedEffect(btnScale) {
                                if (btnScale != 1f) {
                                    kotlinx.coroutines.delay(100)
                                    btnScale = 1f
                                }
                            }
                        }
                    }
                }
            }
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            if (tiendaVM.estaCargando && localItems.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        val infiniteTransition = rememberInfiniteTransition(label = "loading")
                        val scale by infiniteTransition.animateFloat(
                            initialValue = 0.8f,
                            targetValue = 1.1f,
                            animationSpec = infiniteRepeatable(tween(800, easing = EaseInOutCubic), RepeatMode.Reverse),
                            label = "scale"
                        )
                        val alpha by infiniteTransition.animateFloat(
                            initialValue = 0.5f,
                            targetValue = 1f,
                            animationSpec = infiniteRepeatable(tween(800, easing = EaseInOutCubic), RepeatMode.Reverse),
                            label = "alpha"
                        )
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.linearGradient(
                                        colors = listOf(Brand600.copy(alpha = alpha * 0.2f), Brand500.copy(alpha = alpha * 0.1f))
                                    )
                                )
                                .graphicsLayer { scaleX = scale; scaleY = scale },
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                color = Brand600,
                                strokeWidth = 3.dp,
                                modifier = Modifier.size(40.dp)
                            )
                        }
                        Spacer(Modifier.height(20.dp))
                        Text(
                            "Cargando carrito...",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Slate400
                        )
                    }
                }
                return@Column
            }

            // ===== STEPPER =====
            if (localItems.isNotEmpty()) {
                AnimatedVisibility(
                    visible = isVisible,
                    enter = fadeIn(tween(500, delayMillis = 200)) + slideInVertically(tween(500, delayMillis = 200, easing = EaseOutCubic), initialOffsetY = { -it / 6 })
                ) {
                    Surface(color = Color.White, modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            StepIndicator(icon = Icons.Filled.ShoppingCart, label = "Carrito", isActive = true, isCompleted = true)
                            StepConnector(isActive = false)
                            StepIndicator(icon = Icons.Filled.LocalShipping, label = "Envio", isActive = false, isCompleted = false)
                            StepConnector(isActive = false)
                            StepIndicator(icon = Icons.Filled.CheckCircle, label = "Confirmado", isActive = false, isCompleted = false)
                        }
                    }
                }
            }

            if (localItems.isEmpty()) {
                // ===== EMPTY STATE =====
                AnimatedVisibility(
                    visible = isVisible,
                    enter = fadeIn(tween(600)) + scaleIn(tween(600))
                ) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(32.dp)) {
                            val infiniteTransition = rememberInfiniteTransition(label = "empty")
                            val floatY by infiniteTransition.animateFloat(
                                initialValue = -8f,
                                targetValue = 8f,
                                animationSpec = infiniteRepeatable(tween(2000, easing = EaseInOutCubic), RepeatMode.Reverse),
                                label = "float"
                            )
                            Box(
                                modifier = Modifier
                                    .size(120.dp)
                                    .clip(CircleShape)
                                    .background(
                                        Brush.radialGradient(
                                            colors = listOf(Brand600.copy(alpha = 0.08f), Color.Transparent)
                                        )
                                    )
                                    .graphicsLayer { translationY = floatY },
                                contentAlignment = Alignment.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(88.dp)
                                        .clip(CircleShape)
                                        .background(
                                            Brush.linearGradient(
                                                colors = listOf(Brand50, Color.White)
                                            )
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Filled.ShoppingBasket, null, tint = Brand600, modifier = Modifier.size(44.dp))
                                }
                            }
                            Spacer(Modifier.height(28.dp))
                            Text(
                                "Tu canasta esta esperando",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = DarkSlate,
                                letterSpacing = (-0.3).sp
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                "No hay productos frescos seleccionados.\nLos campesinos tienen lo mejor listo para ti!",
                                fontSize = 14.sp,
                                color = Slate500,
                                textAlign = TextAlign.Center,
                                lineHeight = 20.sp
                            )
                            Spacer(Modifier.height(28.dp))
                            var btnScale by remember { mutableStateOf(1f) }
                            Button(
                                onClick = {
                                    btnScale = 0.97f
                                    onNavigateToTienda()
                                },
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Brand600),
                                contentPadding = PaddingValues(horizontal = 32.dp, vertical = 14.dp),
                                modifier = Modifier.graphicsLayer { scaleX = btnScale; scaleY = btnScale }
                            ) {
                                Icon(Icons.Filled.Store, null, tint = Color.White, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(8.dp))
                                Text("Explorar productos del campo", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 14.sp)
                            }
                            LaunchedEffect(btnScale) {
                                if (btnScale != 1f) {
                                    kotlinx.coroutines.delay(100)
                                    btnScale = 1f
                                }
                            }
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(localItems, key = { it.id ?: 0L }) { item ->
                        PremiumCarritoItemRow(
                            item = item,
                            onUpdateQuantity = { newQty ->
                                // Optimista: actualizar localmente al instante
                                val updated = localItems.map {
                                    if (it.id == item.id) it.copy(cantidad = newQty, total = (it.precio ?: 0.0) * newQty) else it
                                }
                                localItems = updated
                                localSubtotal = updated.sumOf { it.total ?: 0.0 }
                                // API en segundo plano
                                if (newQty < 1) {
                                    tiendaVM.eliminarDelCarrito(item.id ?: 0L)
                                } else {
                                    tiendaVM.actualizarCantidadCarrito(item.id ?: 0L, newQty)
                                }
                            },
                            onRemove = {
                                localItems = localItems.filter { it.id != item.id }
                                localSubtotal = localItems.sumOf { it.total ?: 0.0 }
                                tiendaVM.eliminarDelCarrito(item.id ?: 0L)
                            }
                        )
                    }
                }

                // ===== RESUMEN GLASSMORPHISM =====
                AnimatedVisibility(
                    visible = isVisible,
                    enter = fadeIn(tween(500, delayMillis = 300)) + slideInVertically(tween(500, delayMillis = 300, easing = EaseOutCubic), initialOffsetY = { it / 6 })
                ) {
                    GlassmorphismSummary(
                        subtotal = localSubtotal,
                        envio = if (localSubtotal > 0) 3500.0 else 0.0,
                        total = localSubtotal + (if (localSubtotal > 0) 3500.0 else 0.0),
                        onCheckout = onNavigateToCheckout
                    )
                }
            }
        }
    }
}

@Composable
private fun StepIndicator(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    isActive: Boolean,
    isCompleted: Boolean
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(
                    if (isActive) Brush.linearGradient(listOf(Brand600, Brand500))
                    else if (isCompleted) Brush.linearGradient(listOf(Brand500, Brand500))
                    else Brush.linearGradient(listOf(Slate100, Slate100))
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                icon,
                null,
                tint = if (isActive || isCompleted) Color.White else Slate400,
                modifier = Modifier.size(16.dp)
            )
        }
        Spacer(Modifier.width(8.dp))
        Text(
            label,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = if (isActive) Brand600 else if (isCompleted) Brand500 else Slate400
        )
    }
}

@Composable
private fun StepConnector(isActive: Boolean) {
    Box(
        Modifier
            .height(2.dp)
            .width(36.dp)
            .background(if (isActive) Brand500 else Slate200)
    )
}

@Composable
private fun GlassmorphismSummary(
    subtotal: Double,
    envio: Double,
    total: Double,
    onCheckout: () -> Unit
) {
    Surface(
        color = Color.White.copy(alpha = 0.85f),
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        shadowElevation = 8.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            // Glassmorphism header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                Brand600.copy(alpha = 0.08f),
                                Brand500.copy(alpha = 0.04f)
                            )
                        )
                    )
                    .padding(16.dp)
            ) {
                Column {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Subtotal", fontSize = 13.sp, color = Slate500)
                        Text("$${subtotal.toInt()}", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = DarkSlate)
                    }
                    Spacer(Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Envio", fontSize = 13.sp, color = Slate500)
                        Text(
                            if (subtotal > 0) "$${envio.toInt()}" else "Gratis",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Brand600
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // Divider
            Box(Modifier.fillMaxWidth().height(1.dp).background(Slate200))

            Spacer(Modifier.height(16.dp))

            // Total
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Total a Pagar", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = DarkSlate, letterSpacing = (-0.2.sp))
                Text("$${total.toInt()}", fontSize = 26.sp, fontWeight = FontWeight.Bold, color = Brand600, letterSpacing = (-0.5.sp))
            }

            Spacer(Modifier.height(20.dp))

            // Checkout button
            var btnScale by remember { mutableStateOf(1f) }
            Button(
                onClick = {
                    btnScale = 0.97f
                    onCheckout()
                },
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Brand600,
                    contentColor = Color.White
                ),
                contentPadding = PaddingValues(vertical = 16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .graphicsLayer { scaleX = btnScale; scaleY = btnScale }
            ) {
                Icon(Icons.Filled.ShoppingCartCheckout, null, tint = Color.White, modifier = Modifier.size(22.dp))
                Spacer(Modifier.width(12.dp))
                Text("Elegir Direccion de Envio", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
            LaunchedEffect(btnScale) {
                if (btnScale != 1f) {
                    kotlinx.coroutines.delay(100)
                    btnScale = 1f
                }
            }

            Spacer(Modifier.height(14.dp))

            // Security badge
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                Icon(Icons.Filled.Lock, null, tint = Slate400, modifier = Modifier.size(12.dp))
                Spacer(Modifier.width(4.dp))
                Text("Pago 100% Seguro", fontSize = 11.sp, color = Slate400)
            }
        }
    }
}

@Composable
fun PremiumCarritoItemRow(
    item: CarritoItem,
    onUpdateQuantity: (Int) -> Unit,
    onRemove: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        if (isPressed) 0.98f else 1f,
        spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow)
    )

    var removeScale by remember { mutableStateOf(1f) }
    val currentQty = item.cantidad ?: 1
    val stock = item.stock ?: 0

    Surface(
        color = Color.White,
        shape = RoundedCornerShape(24.dp),
        shadowElevation = 0.dp,
        border = BorderStroke(1.dp, Slate200),
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .clickable(interactionSource = interactionSource, indication = null) {}
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            // Image with glassmorphism effect
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        Brush.linearGradient(listOf(Slate100, Color.White))
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (!item.imagenUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current).data(cartImageUrl(item.imagenUrl)).crossfade(true).build(),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(16.dp))
                    )
                } else {
                    Icon(Icons.Filled.ShoppingBasket, null, tint = Slate400, modifier = Modifier.size(32.dp))
                }
            }

            Spacer(Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                if (item.categoria != null) {
                    Text(item.categoria, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Brand600, letterSpacing = 0.3.sp)
                }
                Text(
                    item.nombre ?: "",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = DarkSlate,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    letterSpacing = (-0.2.sp)
                )
                Spacer(Modifier.height(3.dp))
                Text("$${item.precio?.toInt() ?: 0} / ${item.unidad ?: "Kg"}", fontSize = 12.sp, color = Slate400, fontWeight = FontWeight.Medium)

                Spacer(Modifier.height(12.dp))

                // Quantity controls with animations
                Row(verticalAlignment = Alignment.CenterVertically) {
                    var minusScale by remember { mutableStateOf(1f) }
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Slate100)
                            .border(1.5.dp, Slate200, CircleShape)
                            .graphicsLayer { scaleX = minusScale; scaleY = minusScale }
                            .clickable {
                                minusScale = 0.9f
                                onUpdateQuantity(currentQty - 1)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Filled.Remove, null, tint = DarkSlate, modifier = Modifier.size(16.dp))
                    }
                    LaunchedEffect(minusScale) {
                        if (minusScale != 1f) {
                            kotlinx.coroutines.delay(100)
                            minusScale = 1f
                        }
                    }

                    // Animated quantity text
                    val qtyTransition = updateTransition(targetState = currentQty, label = "qty")
                    val qtyAlpha by qtyTransition.animateFloat(
                        transitionSpec = { tween(200) },
                        label = "qtyAlpha"
                    ) { if (it == currentQty) 1f else 0f }

                    Text(
                        "$currentQty",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = DarkSlate,
                        modifier = Modifier
                            .padding(horizontal = 14.dp)
                            .alpha(qtyAlpha)
                    )

                    var plusScale by remember { mutableStateOf(1f) }
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(listOf(Brand600, Brand500))
                            )
                            .graphicsLayer { scaleX = plusScale; scaleY = plusScale }
                            .clickable(enabled = currentQty < stock) {
                                plusScale = 0.9f
                                onUpdateQuantity(currentQty + 1)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Filled.Add, null, tint = Color.White, modifier = Modifier.size(16.dp))
                    }
                    LaunchedEffect(plusScale) {
                        if (plusScale != 1f) {
                            kotlinx.coroutines.delay(100)
                            plusScale = 1f
                        }
                    }
                }
                Text("Disponibles: $stock ${item.unidad ?: ""}", fontSize = 10.sp, color = Slate400, fontWeight = FontWeight.Medium)
            }

            Spacer(Modifier.width(10.dp))

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    "$${item.total?.toInt() ?: 0}",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Brand600,
                    letterSpacing = (-0.3.sp)
                )
                Spacer(Modifier.height(10.dp))
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(listOf(Red500.copy(alpha = 0.1f), Red500.copy(alpha = 0.05f)))
                        )
                        .graphicsLayer { scaleX = removeScale; scaleY = removeScale }
                        .clickable {
                            removeScale = 0.9f
                            onRemove()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.Delete, null, tint = Red500, modifier = Modifier.size(18.dp))
                }
                LaunchedEffect(removeScale) {
                    if (removeScale != 1f) {
                        kotlinx.coroutines.delay(100)
                        removeScale = 1f
                    }
                }
            }
        }
    }
}

fun cartImageUrl(url: String): Any =
    if (url.startsWith("http://") || url.startsWith("https://")) url else com.agroconecta.app.data.api.ApiConfig.IMAGES_URL + url
