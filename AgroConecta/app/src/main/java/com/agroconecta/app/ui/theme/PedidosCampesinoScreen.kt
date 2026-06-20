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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.agroconecta.app.data.model.VentaItem
import com.agroconecta.app.viewmodel.TiendaViewModel

private val Carbon = Color(0xFF111827)
private val Slate400 = Color(0xFF94A3B8)
private val Slate500 = Color(0xFF64748B)
private val Slate300 = Color(0xFFCBD5E1)
private val Slate200 = Color(0xFFE2E8F0)
private val Slate100 = Color(0xFFF1F5F9)
private val AppBackground = Color(0xFFF8FAF9)
private val Emerald = Color(0xFF0E793D)
private val Blue50 = Color(0xFFEFF6FF)
private val Blue500 = Color(0xFF3B82F6)
private val Orange500 = Color(0xFFF97316)
private val Green500 = Color(0xFF22C55E)
private val Red500 = Color(0xFFEF4444)
private val Teal500 = Color(0xFF14B8A6)
private val Indigo500 = Color(0xFF6366F1)
private val Indigo600 = Color(0xFF4F46E5)
private val Purple500 = Color(0xFF8B5CF6)

enum class TabPedido(val label: String, val key: String, val color: Color) {
    NUEVOS("Nuevos", "NUEVO", Orange500),
    PREPARACION("Preparacion", "PREPARADO", Green500),
    LISTOS("Listos recoger", "LISTO_PARA_RECOGER", Color(0xFFD97706)),
    EN_CAMINO("En camino", "ENVIADO", Blue500),
    ENTREGADOS("Entregados", "ENTREGADO", Teal500),
    CANCELADOS("Cancelados", "CANCELADO", Red500)
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun PedidosCampesinoScreen(tiendaVM: TiendaViewModel, onNavigateBack: () -> Unit, onNavigateToLogistica: (Long) -> Unit) {
    LaunchedEffect(Unit) { tiendaVM.cargarPedidosCampesino() }

    val data = tiendaVM.pedidosResponse
    var tabSeleccionado by remember { mutableStateOf(TabPedido.NUEVOS) }
    var isVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { isVisible = true }

    Scaffold(
        containerColor = AppBackground,
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
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Carbon, modifier = Modifier.size(22.dp))
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
                                "Gestion de Pedidos",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = Carbon,
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
                                        .background(Indigo500.copy(alpha = pulseAnim))
                                )
                                Spacer(Modifier.width(6.dp))
                                Text(
                                    "Pedidos activos",
                                    fontSize = 12.sp,
                                    color = Slate500,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.linearGradient(
                                        colors = listOf(Indigo500, Purple500)
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Filled.LocalShipping, null, tint = Color.White, modifier = Modifier.size(20.dp))
                        }
                    }
                }
            }
        }
    ) { padding ->
        if (tiendaVM.pedidosCargando && data == null) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
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
                                    colors = listOf(Indigo500.copy(alpha = alpha * 0.2f), Purple500.copy(alpha = alpha * 0.1f))
                                )
                            )
                            .graphicsLayer { scaleX = scale; scaleY = scale },
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            color = Indigo500,
                            strokeWidth = 3.dp,
                            modifier = Modifier.size(40.dp)
                        )
                    }
                    Spacer(Modifier.height(20.dp))
                    Text(
                        "Cargando pedidos...",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Slate400
                    )
                }
            }
            return@Scaffold
        }

        if (data == null || data.success == false) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                AnimatedVisibility(
                    visible = isVisible,
                    enter = fadeIn(tween(600)) + scaleIn(tween(600))
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
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
                                        colors = listOf(Indigo500.copy(alpha = 0.08f), Color.Transparent)
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
                                            colors = listOf(Slate100, Color.White)
                                        )
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Filled.ReceiptLong, null, tint = Slate400, modifier = Modifier.size(44.dp))
                            }
                        }
                        Spacer(Modifier.height(28.dp))
                        Text(
                            data?.error ?: "Sin datos",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = Carbon,
                            letterSpacing = (-0.3).sp
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "No hay informacion de pedidos disponible",
                            fontSize = 14.sp,
                            color = Slate500,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            }
            return@Scaffold
        }

        val ventas = data.ventas ?: emptyList()
        val conteos = data.conteos

        LazyColumn(modifier = Modifier.fillMaxSize().padding(padding)) {
            item {
                AnimatedVisibility(
                    visible = isVisible,
                    enter = fadeIn(tween(600, delayMillis = 150)) + slideInVertically(tween(600, delayMillis = 150, easing = EaseOutCubic), initialOffsetY = { it / 6 })
                ) {
                    val infiniteTransition = rememberInfiniteTransition(label = "hero")
                    val orbX by infiniteTransition.animateFloat(
                        initialValue = 0f,
                        targetValue = 20f,
                        animationSpec = infiniteRepeatable(tween(4000, easing = EaseInOutCubic), RepeatMode.Reverse),
                        label = "orbX"
                    )
                    val orbY by infiniteTransition.animateFloat(
                        initialValue = 0f,
                        targetValue = 15f,
                        animationSpec = infiniteRepeatable(tween(3500, easing = EaseInOutCubic), RepeatMode.Reverse),
                        label = "orbY"
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(Color(0xFF1E1B4B), Color(0xFF312E81), Color(0xFF4C1D95)),
                                    start = Offset(0f, 0f),
                                    end = Offset(1f, 1f)
                                )
                            )
                            .padding(24.dp)
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            drawCircle(
                                color = Color.White.copy(alpha = 0.04f),
                                radius = size.width * 0.35f,
                                center = Offset(size.width * 0.8f + orbX, size.height * 0.3f + orbY)
                            )
                            drawCircle(
                                color = Color.White.copy(alpha = 0.03f),
                                radius = size.width * 0.22f,
                                center = Offset(size.width * 0.15f - orbX * 0.5f, size.height * 0.7f - orbY * 0.3f)
                            )
                            drawCircle(
                                color = Color.White.copy(alpha = 0.02f),
                                radius = size.width * 0.15f,
                                center = Offset(size.width * 0.5f + orbX * 0.3f, size.height * 0.1f)
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Column(modifier = Modifier.weight(1f)) {
                                Surface(
                                    color = Color.White.copy(alpha = 0.12f),
                                    shape = RoundedCornerShape(20.dp),
                                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.2f))
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        val dotPulse by infiniteTransition.animateFloat(
                                            initialValue = 0.5f,
                                            targetValue = 1f,
                                            animationSpec = infiniteRepeatable(tween(1200), RepeatMode.Reverse),
                                            label = "dotPulse"
                                        )
                                        Box(
                                            modifier = Modifier
                                                .size(6.dp)
                                                .clip(CircleShape)
                                                .background(Color(0xFF818CF8).copy(alpha = dotPulse))
                                        )
                                        Spacer(Modifier.width(8.dp))
                                        Text("Logistica", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White.copy(alpha = 0.95f))
                                    }
                                }
                                Spacer(Modifier.height(16.dp))
                                Text(
                                    "Gestion de Pedidos",
                                    fontSize = 30.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White,
                                    letterSpacing = (-0.8).sp
                                )
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    "Administra el estado de los productos que te han comprado.",
                                    fontSize = 14.sp,
                                    color = Color.White.copy(alpha = 0.75f),
                                    lineHeight = 20.sp
                                )
                            }
                            Surface(
                                color = Color.White.copy(alpha = 0.12f),
                                shape = RoundedCornerShape(20.dp),
                                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.2f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 18.dp, vertical = 14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Filled.Notifications, null, tint = Orange500, modifier = Modifier.size(24.dp))
                                    Spacer(Modifier.width(12.dp))
                                    Column {
                                        Text("Atencion", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Color.White.copy(alpha = 0.7f))
                                        Text("${conteos?.nuevos ?: 0} Nuevos", fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = Color.White, letterSpacing = (-0.5.sp))
                                    }
                                }
                            }
                        }
                    }
                }
            }

            item {
                AnimatedVisibility(
                    visible = isVisible,
                    enter = fadeIn(tween(500, delayMillis = 300)) + slideInVertically(tween(500, delayMillis = 300, easing = EaseOutCubic), initialOffsetY = { it / 6 })
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(horizontal = 20.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        TabPedido.entries.forEach { tab ->
                            val count = when (tab) {
                                TabPedido.NUEVOS -> conteos?.nuevos ?: 0
                                TabPedido.PREPARACION -> conteos?.preparados ?: 0
                                TabPedido.LISTOS -> conteos?.listosParaRecoger ?: 0
                                TabPedido.EN_CAMINO -> conteos?.enCamino ?: 0
                                TabPedido.ENTREGADOS -> conteos?.entregados ?: 0
                                TabPedido.CANCELADOS -> conteos?.cancelados ?: 0
                            }
                            val selected = tabSeleccionado == tab
                            PremiumTabChip(
                                label = tab.label,
                                count = count,
                                selected = selected,
                                color = tab.color,
                                onClick = { tabSeleccionado = tab }
                            )
                        }
                    }
                }
            }

            val filtradas = ventas.filter { it.estado == tabSeleccionado.key }
            if (filtradas.isEmpty()) {
                item {
                    AnimatedVisibility(
                        visible = isVisible,
                        enter = fadeIn(tween(500, delayMillis = 400)) + scaleIn(tween(500, delayMillis = 400))
                    ) {
                        Surface(
                            color = Color.White,
                            shape = RoundedCornerShape(28.dp),
                            shadowElevation = 0.dp,
                            border = BorderStroke(1.dp, Slate200),
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 24.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(48.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                val infiniteTransition = rememberInfiniteTransition(label = "noData")
                                val floatY by infiniteTransition.animateFloat(
                                    initialValue = -6f,
                                    targetValue = 6f,
                                    animationSpec = infiniteRepeatable(tween(2500, easing = EaseInOutCubic), RepeatMode.Reverse),
                                    label = "floatY"
                                )
                                Box(
                                    modifier = Modifier
                                        .size(100.dp)
                                        .clip(CircleShape)
                                        .background(
                                            Brush.radialGradient(
                                                colors = listOf(Indigo500.copy(alpha = 0.06f), Color.Transparent)
                                            )
                                        )
                                        .graphicsLayer { translationY = floatY },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(72.dp)
                                            .clip(CircleShape)
                                            .background(
                                                Brush.linearGradient(
                                                    colors = listOf(Slate100, Color.White)
                                                )
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Filled.Inbox, null, tint = Slate400, modifier = Modifier.size(36.dp))
                                    }
                                }
                                Spacer(Modifier.height(20.dp))
                                Text(
                                    "No tienes pedidos ${tabSeleccionado.label.lowercase()}.",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Slate500
                                )
                            }
                        }
                    }
                }
            } else {
                items(filtradas) { venta ->
                    PremiumPedidoCard(venta, tiendaVM, onNavigateToLogistica)
                }
            }

            item { Spacer(Modifier.height(24.dp)) }
        }
    }
}

@Composable
private fun PremiumTabChip(
    label: String,
    count: Long,
    selected: Boolean,
    color: Color,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        if (isPressed) 0.95f else 1f,
        spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow)
    )

    Surface(
        color = if (selected) color else Color.White,
        shape = RoundedCornerShape(16.dp),
        border = if (!selected) BorderStroke(1.5.dp, Slate200) else null,
        modifier = Modifier
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .clickable(interactionSource = interactionSource, indication = null) { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                label,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = if (selected) Color.White else Slate500
            )
            Spacer(Modifier.width(8.dp))
            Surface(
                color = if (selected) Color.White.copy(alpha = 0.25f) else Slate100,
                shape = CircleShape
            ) {
                Text(
                    "$count",
                    modifier = Modifier.padding(horizontal = 9.dp, vertical = 3.dp),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (selected) Color.White else Slate500
                )
            }
        }
    }
}

@Composable
private fun PremiumPedidoCard(venta: VentaItem, tiendaVM: TiendaViewModel, onNavigateToLogistica: (Long) -> Unit) {
    var showConfirm by remember { mutableStateOf<String?>(null) }
    var showModalRepartidor by remember { mutableStateOf(false) }

    val estadoColor = when (venta.estado) {
        "NUEVO" -> Orange500
        "PREPARADO" -> Green500
        "LISTO_PARA_RECOGER" -> Color(0xFFD97706)
        "ENVIADO" -> Blue500
        "ENTREGADO" -> Teal500
        "CANCELADO" -> Red500
        else -> Slate500
    }

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        if (isPressed) 0.98f else 1f,
        spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow)
    )

    Surface(
        color = Color.White,
        shape = RoundedCornerShape(24.dp),
        shadowElevation = 0.dp,
        border = BorderStroke(1.dp, Slate200),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 5.dp)
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .clickable(interactionSource = interactionSource, indication = null) {}
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(verticalAlignment = Alignment.Top) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            "Pedido #${venta.ordenId ?: "—"}",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Carbon,
                            letterSpacing = (-0.2.sp)
                        )
                        Spacer(Modifier.width(10.dp))
                        Surface(
                            color = estadoColor.copy(alpha = 0.12f),
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, estadoColor.copy(alpha = 0.3f))
                        ) {
                            Text(
                                venta.estado,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = estadoColor
                            )
                        }
                    }
                    Spacer(Modifier.height(6.dp))
                    Text(
                        venta.fechaOrden?.take(10) ?: "",
                        fontSize = 12.sp,
                        color = Slate400,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(Modifier.height(10.dp))
                    Text(
                        venta.nombre,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Carbon,
                        letterSpacing = (-0.2.sp)
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            "${venta.cantidad ?: 0} x ",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Slate500
                        )
                        Text(
                            "\$${formatNum(venta.precio ?: 0.0)} / ${venta.unidad ?: "Kg"}",
                            fontSize = 14.sp,
                            color = Slate500,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(Modifier.width(10.dp))
                        Text(
                            "= ",
                            fontSize = 14.sp,
                            color = Slate500,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            "\$${formatNum(venta.total ?: 0.0)}",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Emerald,
                            letterSpacing = (-0.2.sp)
                        )
                    }
                }
            }
            Spacer(Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(listOf(Blue500.copy(alpha = 0.15f), Indigo500.copy(alpha = 0.1f)))
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        venta.clienteNombre?.firstOrNull()?.uppercase() ?: "?",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Blue500
                    )
                }
                Spacer(Modifier.width(10.dp))
                Text(
                    venta.clienteNombre ?: "Anonimo",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Slate500
                )
            }
            Spacer(Modifier.height(16.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                when (venta.estado) {
                    "NUEVO" -> {
                        PremiumActionBtn("Aceptar", Green500) { tiendaVM.cambiarEstadoPedido(venta.id ?: return@PremiumActionBtn, "PREPARADO") }
                        PremiumActionBtnCancel("Cancelar") { showConfirm = "CANCELADO" }
                    }
                    "PREPARADO" -> {
                        PremiumActionBtn("Listo recoger", Color(0xFFD97706)) { tiendaVM.cambiarEstadoPedido(venta.id ?: return@PremiumActionBtn, "LISTO_PARA_RECOGER") }
                        PremiumActionBtnCancel("Cancelar") { showConfirm = "CANCELADO" }
                    }
                    "LISTO_PARA_RECOGER" -> {
                        Column {
                            venta.codigoRuta?.let { ruta ->
                                Surface(color = Color(0xFFEEF2FF), shape = RoundedCornerShape(10.dp), border = BorderStroke(1.dp, Color(0xFFC7D2FE))) {
                                    Row(Modifier.padding(horizontal = 10.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Filled.Route, null, tint = Indigo500, modifier = Modifier.size(14.dp))
                                        Spacer(Modifier.width(6.dp))
                                        Text(ruta, fontSize = 13.sp, fontWeight = FontWeight.ExtraBold, color = Indigo500)
                                    }
                                }
                                Spacer(Modifier.height(4.dp))
                            }
                            venta.codigoRecogida?.let { pin ->
                                Surface(color = Color(0xFFFFF7ED), shape = RoundedCornerShape(10.dp), border = BorderStroke(1.dp, Color(0xFFFED7AA))) {
                                    Row(Modifier.padding(horizontal = 10.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Filled.Key, null, tint = Color(0xFFD97706), modifier = Modifier.size(14.dp))
                                        Spacer(Modifier.width(6.dp))
                                        Text("PIN: $pin", fontSize = 15.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFFD97706), letterSpacing = 3.sp)
                                    }
                                }
                            }
                            Spacer(Modifier.height(4.dp))
                            if (venta.repartidor != null) {
                                Surface(
                                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).clickable { showModalRepartidor = true },
                                    color = Blue500.copy(alpha = 0.08f), shape = RoundedCornerShape(12.dp), border = BorderStroke(1.dp, Blue500.copy(alpha = 0.2f))
                                ) {
                                    Row(Modifier.padding(horizontal = 12.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Filled.Motorcycle, null, tint = Blue500, modifier = Modifier.size(18.dp))
                                        Spacer(Modifier.width(8.dp))
                                        Text("Ver repartidor", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Blue500)
                                    }
                                }
                            } else {
                                Text("Esperando repartidor...", fontSize = 11.sp, color = Color(0xFFD97706), fontWeight = FontWeight.Medium)
                            }
                        }
                    }
                    "ENVIADO" -> {
                        PremiumActionBtnOutline("Ver Ruta", Blue500) { venta.id?.let { onNavigateToLogistica(it) } }
                        Surface(color = Blue500.copy(alpha = 0.1f), shape = RoundedCornerShape(12.dp)) {
                            Row(Modifier.padding(horizontal = 10.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.LocalShipping, null, tint = Blue500, modifier = Modifier.size(14.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("En camino", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Blue500)
                            }
                        }
                    }
                    else -> {
                        Surface(color = estadoColor.copy(alpha = 0.12f), shape = RoundedCornerShape(12.dp), border = BorderStroke(1.dp, estadoColor.copy(alpha = 0.25f))) {
                            Row(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.CheckCircle, null, tint = estadoColor, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(8.dp))
                                Text(venta.estado, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = estadoColor)
                            }
                        }
                    }
                }
            }
        }
    }

    if (showConfirm != null) {
        AlertDialog(
            onDismissRequest = { showConfirm = null },
            title = { Text("Confirmar", fontWeight = FontWeight.Bold) },
            text = { Text("Seguro que quieres cancelar este pedido?") },
            confirmButton = {
                Button(onClick = {
                    tiendaVM.cambiarEstadoPedido(venta.id ?: 0L, showConfirm!!)
                    showConfirm = null
                }, colors = ButtonDefaults.buttonColors(containerColor = Red500)) { Text("Cancelar Pedido", color = Color.White) }
            },
            dismissButton = { OutlinedButton(onClick = { showConfirm = null }) { Text("Volver") } }
        )
    }

    // Modal repartidor
    if (showModalRepartidor && venta.repartidor != null) {
        val rep = venta.repartidor!!
        AlertDialog(
            onDismissRequest = { showModalRepartidor = false },
            title = { Text("Repartidor Asignado", fontWeight = FontWeight.ExtraBold, color = Carbon) },
            text = {
                Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    // Ruta
                    venta.codigoRuta?.let { r ->
                        Surface(color = Color(0xFFEEF2FF), shape = RoundedCornerShape(10.dp)) {
                            Row(Modifier.padding(horizontal = 10.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.Route, null, tint = Indigo500, modifier = Modifier.size(14.dp))
                                Spacer(Modifier.width(6.dp))
                                Text(r, fontSize = 13.sp, fontWeight = FontWeight.ExtraBold, color = Indigo500)
                            }
                        }
                    }
                    // PIN
                    venta.codigoRecogida?.let { pin ->
                        Surface(color = Color(0xFFFFF7ED), shape = RoundedCornerShape(12.dp), border = BorderStroke(2.dp, Color(0xFFFED7AA))) {
                            Column(Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Codigo de recogida", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFFD97706))
                                Text(pin, fontSize = 32.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFFD97706), letterSpacing = 8.sp)
                                Text("Comparte este codigo solo cuando el repartidor haya llegado a la finca.", fontSize = 10.sp, color = Color(0xFFD97706).copy(alpha = 0.7f))
                            }
                        }
                    }
                    // Repartidor
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(48.dp).clip(CircleShape).background(Blue500.copy(alpha = 0.15f)), contentAlignment = Alignment.Center) {
                            Text(rep.nombre?.firstOrNull()?.uppercase() ?: "?", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = Blue500)
                        }
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(rep.nombre ?: "--", fontWeight = FontWeight.ExtraBold, fontSize = 15.sp, color = Carbon)
                            val stars = "★".repeat(((rep.calificacion ?: 0.0) * 2).toInt().coerceIn(0, 5))
                            val ratingText = "%.1f".format(rep.calificacion ?: 0.0)
                            Text("$stars $ratingText", fontSize = 12.sp, color = Color(0xFFEAB308))
                        }
                        IconButton(onClick = { rep.telefono?.let { /* intent call */ } }) {
                            Icon(Icons.Filled.Call, null, tint = Green500, modifier = Modifier.size(20.dp))
                        }
                    }
                    // Vehiculo
                    Surface(color = Slate100, shape = RoundedCornerShape(10.dp)) {
                        Row(Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.LocalShipping, null, tint = Slate400, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("${rep.tipoVehiculo ?: "--"} | ${rep.placa ?: "--"}", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = Slate500)
                        }
                    }
                    // Estado
                    Surface(color = Blue50, shape = RoundedCornerShape(10.dp)) {
                        Text("En camino a la finca", Modifier.padding(10.dp), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Blue500)
                    }
                }
            },
            confirmButton = { TextButton(onClick = { showModalRepartidor = false }) { Text("Cerrar", fontWeight = FontWeight.Bold) } },
            shape = RoundedCornerShape(24.dp),
            containerColor = Color.White
        )
    }
}

@Composable
private fun PremiumActionBtn(label: String, color: Color, onClick: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        if (isPressed) 0.95f else 1f,
        spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow)
    )

    Surface(
        color = color,
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .clickable(interactionSource = interactionSource, indication = null) { onClick() }
    ) {
        Text(
            label,
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 12.dp),
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
    }
}

@Composable
private fun PremiumActionBtnCancel(label: String, onClick: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        if (isPressed) 0.95f else 1f,
        spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow)
    )

    Surface(
        color = Color.White,
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.5.dp, Red500.copy(alpha = 0.35f)),
        modifier = Modifier
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .clickable(interactionSource = interactionSource, indication = null) { onClick() }
    ) {
        Text(
            label,
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 12.dp),
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = Red500
        )
    }
}

@Composable
private fun PremiumActionBtnOutline(label: String, color: Color, onClick: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        if (isPressed) 0.95f else 1f,
        spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow)
    )

    Surface(
        color = Color.White,
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.5.dp, color.copy(alpha = 0.35f)),
        modifier = Modifier
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .clickable(interactionSource = interactionSource, indication = null) { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Filled.Map, null, tint = color, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(8.dp))
            Text(label, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = color)
        }
    }
}

private fun formatNum(value: Double): String {
    val n = value.toLong()
    if (n >= 1000) {
        val parts = mutableListOf<String>()
        var num = n
        while (num > 0) { parts.add(0, (num % 1000).toString().padStart(3, '0')); num /= 1000 }
        return parts.joinToString(".")
    }
    return n.toString()
}