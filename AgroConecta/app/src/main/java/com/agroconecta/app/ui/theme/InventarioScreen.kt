package com.agroconecta.app.ui.theme

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.agroconecta.app.data.model.Producto
import com.agroconecta.app.ui.components.ToastData
import com.agroconecta.app.ui.components.ToastType
import com.agroconecta.app.ui.components.PremiumToastHost
import com.agroconecta.app.viewmodel.TiendaViewModel
import kotlinx.coroutines.delay

private val Emerald = Color(0xFF0E793D)
private val EmeraldDark = Color(0xFF0A5C2E)
private val EmeraldLight = Color(0xFFE8F5E9)
private val AppBackground = Color(0xFFF8FAF9)
private val Carbon = Color(0xFF111827)
private val Slate400 = Color(0xFF94A3B8)
private val Slate500 = Color(0xFF64748B)
private val Slate300 = Color(0xFFCBD5E1)
private val Slate200 = Color(0xFFE2E8F0)
private val Slate100 = Color(0xFFF1F5F9)
private val Red500 = Color(0xFFEF4444)
private val Red50 = Color(0xFFFEF2F2)
private val Red100 = Color(0xFFFEE2E2)
private val Orange500 = Color(0xFFF97316)
private val Orange50 = Color(0xFFFFF7ED)
private val Orange100 = Color(0xFFFFEDD5)
private val Green500 = Color(0xFF22C55E)
private val Green50 = Color(0xFFF0FDF4)
private val Green100 = Color(0xFFDCFCE7)
private val Blue500 = Color(0xFF3B82F6)
private val Blue600 = Color(0xFF2563EB)
private val Blue50 = Color(0xFFEFF6FF)
private val Amber500 = Color(0xFFF59E0B)
private val Amber50 = Color(0xFFFFFDE7)
private val Violet500 = Color(0xFF7C3AED)
private val Violet50 = Color(0xFFF3E8FF)

private val BASE_IMAGE_URL get() = com.agroconecta.app.data.api.ApiConfig.IMAGES_URL

enum class FiltroInventario { TODOS, AGOTADOS, POCO_STOCK, SUFICIENTE }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventarioScreen(tiendaVM: TiendaViewModel, onNavigateBack: () -> Unit) {
    LaunchedEffect(Unit) { tiendaVM.cargarMisProductosInventario() }

    val productos = tiendaVM.misProductosInventario
    var filtro by remember { mutableStateOf(FiltroInventario.TODOS) }
    val context = LocalContext.current
    var isVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { isVisible = true }

    val productosFiltrados = remember(productos.toList(), filtro) {
        when (filtro) {
            FiltroInventario.TODOS -> productos.toList()
            FiltroInventario.AGOTADOS -> productos.filter { it.stock == 0 }
            FiltroInventario.POCO_STOCK -> productos.filter { it.stock > 0 && it.stock < 10 }
            FiltroInventario.SUFICIENTE -> productos.filter { it.stock >= 10 }
        }
    }

    var stockTotal by remember(productos.toList()) { mutableStateOf(productos.sumOf { it.stock }) }
    var agotados by remember(productos.toList()) { mutableStateOf(productos.count { it.stock == 0 }) }
    var bajoStock by remember(productos.toList()) { mutableStateOf(productos.count { it.stock > 0 && it.stock < 10 }) }
    var valorInventario by remember(productos.toList()) { mutableStateOf(productos.sumOf { (it.precio * it.stock) }) }
    var saludInventario by remember(productos.toList()) {
        val total = productos.size
        val sanos = productos.count { it.stock >= 10 }
        mutableStateOf(if (total == 0) 0 else (sanos * 100 / total))
    }

    var stockInputs by remember { mutableStateOf(mapOf<Long, String>()) }

    LaunchedEffect(productos.toList()) {
        stockTotal = productos.sumOf { it.stock }
        agotados = productos.count { it.stock == 0 }
        bajoStock = productos.count { it.stock > 0 && it.stock < 10 }
        valorInventario = productos.sumOf { (it.precio.toDouble() * it.stock.toDouble()) }.let {
            Math.round(it * 100.0) / 100.0
        }
        val total = productos.size
        val sanos = productos.count { it.stock >= 10 }
        saludInventario = if (total == 0) 0 else (sanos * 100 / total)
    }

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
                            .padding(horizontal = 20.dp, vertical = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = onNavigateBack,
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(Slate100)
                        ) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Carbon, modifier = Modifier.size(22.dp))
                        }
                        Spacer(Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "Control de Inventario",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = Carbon,
                                letterSpacing = (-0.5).sp
                            )
                            Text(
                                "${productos.size} productos registrados",
                                fontSize = 13.sp,
                                color = Slate500,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            if (tiendaVM.inventarioCargando && productos.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(
                        color = Emerald,
                        strokeWidth = 3.dp,
                        modifier = Modifier.size(48.dp)
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    item {
                        AnimatedVisibility(
                            visible = isVisible,
                            enter = fadeIn(tween(500, delayMillis = 200)) + slideInVertically(tween(500, delayMillis = 200, easing = EaseOutCubic), initialOffsetY = { it / 6 })
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        brush = Brush.linearGradient(
                                            colors = listOf(Blue600, Violet500),
                                            start = Offset(0f, 0f),
                                            end = Offset(1f, 1f)
                                        )
                                    )
                                    .padding(horizontal = 24.dp, vertical = 28.dp)
                            ) {
                                Canvas(modifier = Modifier.fillMaxSize()) {
                                    drawCircle(
                                        color = Color.White.copy(alpha = 0.05f),
                                        radius = size.width * 0.3f,
                                        center = Offset(size.width * 0.8f, size.height * 0.3f)
                                    )
                                    drawCircle(
                                        color = Color.White.copy(alpha = 0.03f),
                                        radius = size.width * 0.2f,
                                        center = Offset(size.width * 0.2f, size.height * 0.7f)
                                    )
                                }

                                Column {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Surface(
                                            color = Color.White.copy(alpha = 0.2f),
                                            shape = RoundedCornerShape(16.dp),
                                            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.3f))
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(Icons.Filled.Warehouse, null, tint = Color.White, modifier = Modifier.size(16.dp))
                                                Spacer(Modifier.width(8.dp))
                                                Text("Bodega Virtual", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                            }
                                        }

                                        Surface(
                                            color = if (agotados > 0) Red500 else Color.White.copy(alpha = 0.2f),
                                            shape = RoundedCornerShape(18.dp)
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(
                                                    if (agotados > 0) Icons.Filled.Warning else Icons.Filled.CheckCircle,
                                                    null,
                                                    tint = Color.White,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                                Spacer(Modifier.width(8.dp))
                                                Column {
                                                    Text("Agotados", fontSize = 9.sp, fontWeight = FontWeight.SemiBold, color = Color.White.copy(alpha = 0.8f))
                                                    Text("$agotados", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                                }
                                            }
                                        }
                                    }
                                    Spacer(Modifier.height(12.dp))
                                    Text(
                                        "Control de Inventario",
                                        fontSize = 28.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        letterSpacing = (-0.5).sp
                                    )
                                    Text(
                                        "Maneja tu stock facilmente. Activa o pausa productos en un clic.",
                                        fontSize = 14.sp,
                                        color = Color.White.copy(alpha = 0.85f),
                                        lineHeight = 20.sp
                                    )
                                }
                            }
                        }
                    }

                    item {
                        AnimatedVisibility(
                            visible = isVisible,
                            enter = fadeIn(tween(500, delayMillis = 300)) + slideInVertically(tween(500, delayMillis = 300, easing = EaseOutCubic), initialOffsetY = { it / 6 })
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 20.dp, vertical = 20.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Surface(
                                        color = Color.White,
                                        shape = RoundedCornerShape(24.dp),
                                        shadowElevation = 4.dp,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(18.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Box(modifier = Modifier.size(60.dp), contentAlignment = Alignment.Center) {
                                                val animSalud by animateFloatAsState(
                                                    targetValue = saludInventario / 100f,
                                                    animationSpec = tween(800, easing = FastOutSlowInEasing)
                                                )
                                                val circumference = 150.8f
                                                val strokeColor = when {
                                                    saludInventario >= 75 -> Green500
                                                    saludInventario >= 50 -> Amber500
                                                    else -> Red500
                                                }
                                                Canvas(modifier = Modifier.fillMaxSize().graphicsLayer { rotationZ = -90f }) {
                                                    drawArc(
                                                        color = Slate200,
                                                        startAngle = 0f,
                                                        sweepAngle = 360f,
                                                        useCenter = false,
                                                        style = Stroke(width = 6.dp.toPx(), cap = StrokeCap.Round)
                                                    )
                                                    drawArc(
                                                        color = strokeColor,
                                                        startAngle = 0f,
                                                        sweepAngle = 360f * animSalud,
                                                        useCenter = false,
                                                        style = Stroke(width = 6.dp.toPx(), cap = StrokeCap.Round)
                                                    )
                                                }
                                                Text(
                                                    "$saludInventario%",
                                                    fontSize = 14.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Carbon
                                                )
                                            }
                                            Spacer(Modifier.width(14.dp))
                                            Column {
                                                Text("Salud", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Slate400)
                                                Text(
                                                    when {
                                                        saludInventario >= 75 -> "Excelente"
                                                        saludInventario >= 50 -> "Buena"
                                                        saludInventario >= 25 -> "Regular"
                                                        else -> "Critica"
                                                    },
                                                    fontSize = 13.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Carbon,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }
                                        }
                                    }

                                    Surface(
                                        color = Color.White,
                                        shape = RoundedCornerShape(24.dp),
                                        shadowElevation = 4.dp,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(18.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(48.dp)
                                                    .clip(RoundedCornerShape(16.dp))
                                                    .background(
                                                        Brush.linearGradient(
                                                            colors = listOf(Blue500, Blue600)
                                                        )
                                                    ),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(Icons.Filled.Inventory2, null, tint = Color.White, modifier = Modifier.size(24.dp))
                                            }
                                            Spacer(Modifier.width(14.dp))
                                            Column {
                                                Text("Productos", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Slate400)
                                                Text("${productos.size}", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Carbon)
                                            }
                                        }
                                    }
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Surface(
                                        color = Color.White,
                                        shape = RoundedCornerShape(24.dp),
                                        shadowElevation = 4.dp,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(18.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(48.dp)
                                                    .clip(RoundedCornerShape(16.dp))
                                                    .background(
                                                        Brush.linearGradient(
                                                            colors = listOf(Green500, Green500.copy(alpha = 0.8f))
                                                        )
                                                    ),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(Icons.Filled.Layers, null, tint = Color.White, modifier = Modifier.size(24.dp))
                                            }
                                            Spacer(Modifier.width(14.dp))
                                            Column {
                                                Text("Stock Total", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Slate400)
                                                Text("$stockTotal", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Carbon)
                                            }
                                        }
                                    }

                                    Surface(
                                        color = Color.White,
                                        shape = RoundedCornerShape(24.dp),
                                        shadowElevation = 4.dp,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(18.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(48.dp)
                                                    .clip(RoundedCornerShape(16.dp))
                                                    .background(
                                                        Brush.linearGradient(
                                                            colors = listOf(Amber500, Amber500.copy(alpha = 0.8f))
                                                        )
                                                    ),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(Icons.Filled.Savings, null, tint = Color.White, modifier = Modifier.size(24.dp))
                                            }
                                            Spacer(Modifier.width(14.dp))
                                            Column {
                                                Text("Valor Inventario", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Slate400)
                                                Text(
                                                    "\$${formatNumber(valorInventario)}",
                                                    fontSize = 16.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Carbon,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
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
                            enter = fadeIn(tween(500, delayMillis = 400)) + slideInVertically(tween(500, delayMillis = 400, easing = EaseOutCubic), initialOffsetY = { it / 6 })
                        ) {
                            LazyRow(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 20.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                item {
                                    FiltroChip(
                                        label = "Todos (${productos.size})",
                                        selected = filtro == FiltroInventario.TODOS,
                                        onClick = { filtro = FiltroInventario.TODOS },
                                        selectedColor = Carbon,
                                        modifier = Modifier.height(40.dp)
                                    )
                                }
                                item {
                                    FiltroChip(
                                        label = "Agotados ($agotados)",
                                        selected = filtro == FiltroInventario.AGOTADOS,
                                        onClick = { filtro = FiltroInventario.AGOTADOS },
                                        selectedColor = Red500,
                                        border = true,
                                        modifier = Modifier.height(40.dp)
                                    )
                                }
                                item {
                                    FiltroChip(
                                        label = "Poco ($bajoStock)",
                                        selected = filtro == FiltroInventario.POCO_STOCK,
                                        onClick = { filtro = FiltroInventario.POCO_STOCK },
                                        selectedColor = Orange500,
                                        border = true,
                                        modifier = Modifier.height(40.dp)
                                    )
                                }
                                item {
                                    FiltroChip(
                                        label = "Suficiente (${productos.size - agotados - bajoStock})",
                                        selected = filtro == FiltroInventario.SUFICIENTE,
                                        onClick = { filtro = FiltroInventario.SUFICIENTE },
                                        selectedColor = Green500,
                                        border = true,
                                        modifier = Modifier.height(40.dp)
                                    )
                                }
                            }
                        }
                    }

                    if (productosFiltrados.isEmpty()) {
                        item {
                            AnimatedVisibility(
                                visible = isVisible,
                                enter = fadeIn(tween(500, delayMillis = 500)) + scaleIn(tween(500, delayMillis = 500))
                            ) {
                                Surface(
                                    color = Color.White,
                                    shape = RoundedCornerShape(28.dp),
                                    shadowElevation = 4.dp,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 20.dp, vertical = 12.dp)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(48.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        val (icon, iconBg, iconTint, title, desc) = when (filtro) {
                                            FiltroInventario.AGOTADOS -> listOf(
                                                Icons.Filled.CheckCircle, Green50, Green500,
                                                "Excelente! Nada agotado",
                                                "No te preocupes, tienes existencias de todos tus productos activos."
                                            )
                                            FiltroInventario.POCO_STOCK -> listOf(
                                                Icons.Filled.ThumbUp, Blue50, Blue500,
                                                "Inventario Saludable",
                                                "No tienes productos en bajo stock por ahora. Tu negocio va por buen camino."
                                            )
                                            FiltroInventario.SUFICIENTE -> listOf(
                                                Icons.Filled.Inventory2, Orange50, Orange500,
                                                "Necesitas surtir",
                                                "Parece que no tienes productos con stock suficiente. Hora de cosechar!"
                                            )
                                            else -> listOf(
                                                Icons.Filled.Spa, Slate100, Slate400,
                                                "Bodega vacia",
                                                "Aun no tienes productos registrados en tu catalogo."
                                            )
                                        }
                                        Box(
                                            modifier = Modifier
                                                .size(88.dp)
                                                .clip(CircleShape)
                                                .background(
                                                    Brush.linearGradient(
                                                        colors = listOf(iconBg as Color, Color.White)
                                                    )
                                                ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                icon as androidx.compose.ui.graphics.vector.ImageVector,
                                                null,
                                                tint = iconTint as Color,
                                                modifier = Modifier.size(44.dp)
                                            )
                                        }
                                        Spacer(Modifier.height(20.dp))
                                        Text(
                                            title as String,
                                            fontSize = 20.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Carbon,
                                            letterSpacing = (-0.3).sp
                                        )
                                        Spacer(Modifier.height(8.dp))
                                        Text(
                                            desc as String,
                                            fontSize = 14.sp,
                                            color = Slate500,
                                            textAlign = TextAlign.Center,
                                            lineHeight = 20.sp
                                        )
                                    }
                                }
                            }
                        }
                    }

                    if (productosFiltrados.isNotEmpty()) {
                        items(productosFiltrados, key = { it.id ?: it.hashCode() }) { producto ->
                            InventarioProductoCard(
                                producto = producto,
                                stockInput = stockInputs[producto.id] ?: producto.stock.toString(),
                                onStockChange = { delta ->
                                    val currentStock = producto.stock
                                    val newStock = (currentStock + delta).coerceAtLeast(0)
                                    tiendaVM.actualizarStockProducto(
                                        id = producto.id ?: return@InventarioProductoCard,
                                        accion = "set",
                                        valor = newStock
                                    ) { success, stock ->
                                        if (success) {
                                            val nombre = producto.nombre
                                            val deltaSign = if (delta >= 0) "+$delta" else "$delta"
                                            tiendaVM.toastEvent = ToastData(
                                                message = "$nombre → Stock: $stock",
                                                type = if (delta >= 0) ToastType.SUCCESS else ToastType.INFO,
                                                duration = 2000L
                                            )
                                        } else {
                                            tiendaVM.toastEvent = ToastData(
                                                message = "Error al actualizar",
                                                type = ToastType.ERROR,
                                                duration = 2000L
                                            )
                                        }
                                    }
                                },
                                onManualStockChange = { newText ->
                                    val pid = producto.id ?: return@InventarioProductoCard
                                    stockInputs = stockInputs + (pid to newText)
                                },
                                onManualStockSubmit = { valueStr ->
                                    val value = valueStr.toIntOrNull()?.coerceAtLeast(0) ?: return@InventarioProductoCard
                                    tiendaVM.actualizarStockProducto(
                                        id = producto.id ?: return@InventarioProductoCard,
                                        accion = "set",
                                        valor = value
                                    ) { success, stock ->
                                        if (success) {
                                            stockInputs = stockInputs - producto.id
                                            tiendaVM.toastEvent = ToastData(
                                                message = "${producto.nombre} → Stock: $stock",
                                                type = ToastType.SUCCESS,
                                                duration = 2000L
                                            )
                                        } else {
                                            tiendaVM.toastEvent = ToastData(
                                                message = "Error al actualizar",
                                                type = ToastType.ERROR,
                                                duration = 2000L
                                            )
                                        }
                                    }
                                }
                            )
                        }
                    }

                    item { Spacer(Modifier.height(24.dp)) }
                }
            }
            val toastState = remember { derivedStateOf { tiendaVM.toastEvent } }
            PremiumToastHost(
                toastState = toastState,
                onDismiss = { tiendaVM.dismissToast() },
                modifier = Modifier.align(Alignment.TopCenter)
            )
        }
    }
}

@Composable
fun FiltroChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    selectedColor: Color = Carbon,
    border: Boolean = false,
    modifier: Modifier = Modifier
) {
    val bgColor = if (selected) selectedColor else Color.White
    val txtColor = if (selected) Color.White else Carbon
    val borderMod = if (!selected && border) Modifier.border(BorderStroke(1.5.dp, Slate200), RoundedCornerShape(14.dp)) else Modifier

    Surface(
        color = bgColor,
        shape = RoundedCornerShape(14.dp),
        modifier = modifier
            .then(borderMod)
            .clickable(onClick = onClick)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
            Text(
                label,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = txtColor,
                maxLines = 1
            )
        }
    }
}

@Composable
fun InventarioProductoCard(
    producto: Producto,
    stockInput: String,
    onStockChange: (Int) -> Unit,
    onManualStockChange: (String) -> Unit,
    onManualStockSubmit: (String) -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        if (isPressed) 0.98f else 1f,
        spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow)
    )

    val stock = producto.stock
    val isAgotado = stock == 0
    val isPocoStock = stock in 1..9
    val statusColor = when {
        isAgotado -> Red500
        isPocoStock -> Orange500
        else -> Green500
    }
    val statusBg = when {
        isAgotado -> Red50
        isPocoStock -> Orange50
        else -> Green50
    }
    val statusText = when {
        isAgotado -> "AGOTADO"
        isPocoStock -> "POCO STOCK"
        else -> "EN VENTA"
    }

    val cardBorderColor = when {
        isAgotado -> Red100
        isPocoStock -> Orange100
        else -> Slate200
    }

    // Auto-submit cuando el usuario deja de escribir
    var previousStockInput by remember { mutableStateOf(stockInput) }
    LaunchedEffect(stockInput) {
        if (stockInput != previousStockInput && stockInput.isNotBlank()) {
            delay(800) // Esperar 800ms después de que el usuario deje de escribir
            if (stockInput != previousStockInput) { // Verificar que no cambió nuevamente
                val value = stockInput.toIntOrNull()
                if (value != null && value >= 0 && value != stock) {
                    onManualStockSubmit(stockInput)
                }
                previousStockInput = stockInput
            }
        } else if (stockInput.isBlank()) {
            previousStockInput = stockInput
        }
    }

    Surface(
        color = Color.White,
        shape = RoundedCornerShape(28.dp),
        border = BorderStroke(1.5.dp, cardBorderColor),
        shadowElevation = 4.dp,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp)
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .clickable(interactionSource = interactionSource, indication = null) {}
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.Top) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(
                            Brush.linearGradient(
                                colors = listOf(Slate100, Color.White)
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    val imgUrl = if (!producto.imagenUrl.isNullOrBlank() && producto.imagenUrl != "default.png") {
                        if (producto.imagenUrl.startsWith("http")) producto.imagenUrl else BASE_IMAGE_URL + producto.imagenUrl
                    } else null
                    if (imgUrl != null) {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current).data(imgUrl).crossfade(true).build(),
                            contentDescription = producto.nombre,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(20.dp))
                        )
                    } else {
                        Icon(Icons.Filled.Eco, null, tint = Emerald, modifier = Modifier.size(36.dp))
                    }
                }
                Spacer(Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        producto.nombre,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = Carbon,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        letterSpacing = (-0.3).sp
                    )
                    Text(
                        producto.categoria ?: "",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Slate400
                    )
                    Spacer(Modifier.height(8.dp))
                    Surface(
                        color = statusBg,
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, statusColor.copy(alpha = 0.2f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                when {
                                    isAgotado -> Icons.Filled.Cancel
                                    isPocoStock -> Icons.Filled.Warning
                                    else -> Icons.Filled.CheckCircle
                                },
                                null,
                                tint = statusColor,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                statusText,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = statusColor
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            Surface(
                color = Slate100,
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Unidades", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Slate500)
                        Text("(${producto.unidad ?: "Kg"})", fontSize = 12.sp, color = Slate400)
                    }

                    Spacer(Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        var botonScale by remember { mutableStateOf(1f) }
                        Surface(
                            color = Color.White,
                            shape = RoundedCornerShape(16.dp),
                            shadowElevation = 2.dp,
                            modifier = Modifier
                                .size(56.dp)
                                .graphicsLayer { scaleX = botonScale; scaleY = botonScale }
                                .clickable {
                                    botonScale = 0.9f
                                    onStockChange(-1)
                                }
                        ) {
                            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                                Icon(Icons.Filled.Remove, null, tint = Slate500, modifier = Modifier.size(24.dp))
                            }
                        }
                        LaunchedEffect(botonScale) {
                            if (botonScale != 1f) {
                                delay(100)
                                botonScale = 1f
                            }
                        }

                        Spacer(Modifier.width(16.dp))

                        OutlinedTextField(
                            value = stockInput,
                            onValueChange = { newVal ->
                                val filtered = newVal.filter { it.isDigit() }
                                onManualStockChange(filtered)
                            },
                            modifier = Modifier.weight(1f).height(56.dp),
                            textStyle = LocalTextStyle.current.copy(
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = Carbon,
                                textAlign = TextAlign.Center
                            ),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number,
                                imeAction = ImeAction.Done
                            ),
                            singleLine = true,
                            shape = RoundedCornerShape(16.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Blue500,
                                unfocusedBorderColor = Slate200,
                                focusedContainerColor = Color.White,
                                unfocusedContainerColor = Color.White
                            )
                        )

                        Spacer(Modifier.width(16.dp))

                        var botonScaleP by remember { mutableStateOf(1f) }
                        Surface(
                            color = Emerald,
                            shape = RoundedCornerShape(16.dp),
                            shadowElevation = 2.dp,
                            modifier = Modifier
                                .size(56.dp)
                                .graphicsLayer { scaleX = botonScaleP; scaleY = botonScaleP }
                                .clickable {
                                    botonScaleP = 0.9f
                                    onStockChange(1)
                                }
                        ) {
                            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                                Icon(Icons.Filled.Add, null, tint = Color.White, modifier = Modifier.size(24.dp))
                            }
                        }
                        LaunchedEffect(botonScaleP) {
                            if (botonScaleP != 1f) {
                                delay(100)
                                botonScaleP = 1f
                            }
                        }
                    }

                    Spacer(Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        QuickStockButton("+5", 5, onStockChange)
                        Spacer(Modifier.width(10.dp))
                        QuickStockButton("+10", 10, onStockChange)
                        Spacer(Modifier.width(10.dp))
                        QuickStockButton("+50", 50, onStockChange)
                    }
                }
            }
        }
    }
}

@Composable
fun QuickStockButton(label: String, delta: Int, onStockChange: (Int) -> Unit) {
    Surface(
        color = Color.White,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.5.dp, Slate200),
        shadowElevation = 1.dp,
        modifier = Modifier.clickable { onStockChange(delta) }
    ) {
        Text(
            label,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = Slate500
        )
    }
}

private fun formatNumber(value: Double): String {
    val integerPart = value.toLong()
    val parts = mutableListOf<String>()
    var num = integerPart
    if (num == 0L) return "0"
    while (num > 0) {
        parts.add(0, (num % 1000).toString().padStart(if (parts.isEmpty()) 0 else 3, '0'))
        num /= 1000
    }
    return parts.joinToString(".")
}
