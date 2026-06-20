package com.agroconecta.app.ui.theme

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.agroconecta.app.data.model.Pedido
import com.agroconecta.app.data.model.PedidoItem
import com.agroconecta.app.viewmodel.TiendaViewModel

private val DarkSlate = Color(0xFF0F172A)
private val Slate700 = Color(0xFF334155)
private val Slate500 = Color(0xFF64748B)
private val Slate400 = Color(0xFF94A3B8)
private val Slate200 = Color(0xFFE2E8F0)
private val Slate100 = Color(0xFFF1F5F9)
private val Brand600 = Color(0xFF16A34A)
private val Brand500 = Color(0xFF22C55E)
private val Brand50 = Color(0xFFF0FDF4)
private val SurfacePure = Color(0xFFFDFDFC)
private val Amber400 = Color(0xFFFBBF24)
private val Orange500 = Color(0xFFF97316)
private val Blue600 = Color(0xFF2563EB)
private val Red500 = Color(0xFFEF4444)
private val Purple600 = Color(0xFF7C3AED)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MisComprasScreen(
    tiendaVM: TiendaViewModel,
    onNavigateBack: () -> Unit
) {
    var tabSeleccionado by remember { mutableStateOf(0) }
    val tabs = listOf("Todos", "Pendientes", "En Camino", "Entregados", "Cancelados")

    LaunchedEffect(Unit) { tiendaVM.cargarPedidos() }

    val pedidos = tiendaVM.pedidos
    val filtrados = when (tabSeleccionado) {
        0 -> pedidos
        1 -> pedidos.filter { it.estado.equals("Pendiente", ignoreCase = true) }
        2 -> pedidos.filter { it.estado.equals("En Camino", ignoreCase = true) || it.estado.equals("Enviado", ignoreCase = true) }
        3 -> pedidos.filter { it.estado.equals("Entregado", ignoreCase = true) }
        4 -> pedidos.filter { it.estado.equals("Cancelado", ignoreCase = true) || it.estado.equals("Rechazado", ignoreCase = true) }
        else -> pedidos
    }

    val totalPedidos = pedidos.size
    val totalEntregados = pedidos.count { it.estado.equals("Entregado", ignoreCase = true) }
    val ahorroMes = tiendaVM.ahorroMes

    Scaffold(
        containerColor = SurfacePure,
        topBar = {
            Surface(shadowElevation = 2.dp, color = Color.White) {
                Row(modifier = Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = 8.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onNavigateBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver", tint = DarkSlate) }
                    Text("Mis Compras", fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = DarkSlate, modifier = Modifier.weight(1f))
                    if (pedidos.isNotEmpty()) {
                        Surface(color = Slate100, shape = RoundedCornerShape(12.dp)) {
                            Text("$totalPedidos pedidos", fontSize = 12.sp, color = Slate500, modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp))
                        }
                    }
                }
            }
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            if (tiendaVM.estaCargando && pedidos.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Brand600)
                }
                return@Column
            }

            // ===== STATS HEADER (siempre visible) =====
            Surface(color = Brand50, modifier = Modifier.fillMaxWidth()) {
                Row(modifier = Modifier.padding(16.dp), horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.CenterVertically) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Savings, null, tint = Brand600, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Ahorro del mes", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Slate500)
                        }
                        Text("$${ahorroMes.toInt()}", fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = Brand600)
                    }
                    Box(Modifier.width(1.dp).height(36.dp).background(Slate200))
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Total pedidos", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Slate500)
                        Text("$totalPedidos", fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = DarkSlate)
                    }
                    Box(Modifier.width(1.dp).height(36.dp).background(Slate200))
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Entregados", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Slate500)
                        Text("$totalEntregados", fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = DarkSlate)
                    }
                }
            }

            // ===== TABS (siempre visibles) =====
            SecondaryScrollableTabRow(
                selectedTabIndex = tabSeleccionado,
                containerColor = SurfacePure,
                contentColor = Brand600,
                edgePadding = 0.dp,
                modifier = Modifier.fillMaxWidth(),
                divider = { }
            ) {
                tabs.forEachIndexed { i, tab ->
                    Tab(
                        selected = tabSeleccionado == i,
                        onClick = { tabSeleccionado = i },
                        text = {
                            Text(tab, fontSize = 13.sp, fontWeight = if (tabSeleccionado == i) FontWeight.Bold else FontWeight.Medium,
                                color = if (tabSeleccionado == i) Brand600 else Slate500)
                        }
                    )
                }
            }

            // ===== CONTENT (empty or orders) =====
            if (tiendaVM.errorOperacion != null) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(32.dp)) {
                        Icon(Icons.Filled.ErrorOutline, null, tint = Red500, modifier = Modifier.size(48.dp))
                        Spacer(Modifier.height(16.dp))
                        Text(tiendaVM.errorOperacion ?: "Error", fontSize = 14.sp, color = Red500, textAlign = TextAlign.Center)
                        Spacer(Modifier.height(16.dp))
                        OutlinedButton(onClick = { tiendaVM.cargarPedidos() }, shape = RoundedCornerShape(14.dp)) {
                            Text("Reintentar", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            } else if (pedidos.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(32.dp)) {
                        Box(modifier = Modifier.size(80.dp).clip(CircleShape).background(Brand50), contentAlignment = Alignment.Center) {
                            Icon(Icons.AutoMirrored.Filled.ReceiptLong, null, tint = Brand600, modifier = Modifier.size(40.dp))
                        }
                        Spacer(Modifier.height(20.dp))
                        Text("Aun no tienes pedidos", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = DarkSlate)
                        Spacer(Modifier.height(8.dp))
                        Text("Explora la tienda y haz tu primer pedido.", fontSize = 14.sp, color = Slate500)
                        Spacer(Modifier.height(24.dp))
                        OutlinedButton(onClick = onNavigateBack, shape = RoundedCornerShape(14.dp), border = BorderStroke(1.5.dp, Brand600)) {
                            Icon(Icons.Filled.Store, null, tint = Brand600, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Ir a la tienda", color = Brand600, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            } else if (filtrados.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No hay pedidos en esta categoria", fontSize = 14.sp, color = Slate400)
                }
            } else {
                LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(filtrados, key = { it.id ?: 0L }) { pedido ->
                        PedidoCard(pedido = pedido)
                    }
                }
            }
        }
    }
}

@Composable
fun PedidoCard(pedido: Pedido) {
    val statusColor = when (pedido.estado?.lowercase()) {
        "pendiente" -> Orange500
        "en camino", "enviado" -> Blue600
        "entregado", "aprobado" -> Brand600
        "cancelado", "rechazado" -> Red500
        else -> Slate500
    }

    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header: order number + date + status
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text(pedido.numeroOrden ?: "Pedido #${pedido.id}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = DarkSlate)
                    pedido.fechaCreacion?.let {
                        val fecha = it.take(10)
                        Text(fecha, fontSize = 11.sp, color = Slate400)
                    }
                }
                Surface(color = statusColor.copy(alpha = 0.1f), shape = RoundedCornerShape(10.dp)) {
                    Text(pedido.estado ?: "Desconocido", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = statusColor,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp))
                }
            }

            Spacer(Modifier.height(10.dp))
            Box(Modifier.fillMaxWidth().height(1.dp).background(Slate100))

            // Items
            pedido.items?.take(4)?.forEach { item ->
                Spacer(Modifier.height(10.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier.size(44.dp).clip(RoundedCornerShape(12.dp)).background(Slate100),
                        contentAlignment = Alignment.Center
                    ) {
                        if (!item.imagenUrl.isNullOrBlank()) {
                            AsyncImage(
                                model = ImageRequest.Builder(LocalContext.current)
                                    .data(imageUrlStr(item.imagenUrl)).crossfade(true).build(),
                                contentDescription = null, contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            Icon(Icons.Filled.ShoppingBasket, null, tint = Slate400, modifier = Modifier.size(20.dp))
                        }
                    }
                    Spacer(Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(item.nombre ?: "Producto", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = DarkSlate, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text("${item.cantidad ?: 1}x $${item.precio?.toInt() ?: 0} / ${item.unidad ?: "Kg"}", fontSize = 11.sp, color = Slate400)
                    }
                    if (item.total != null) {
                        Text("$${item.total.toInt()}", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = DarkSlate)
                    }
                }
            }

            // "Show more" if more than 4 items
            if ((pedido.items?.size ?: 0) > 4) {
                Spacer(Modifier.height(6.dp))
                Text("...y ${(pedido.items?.size ?: 0) - 4} productos mas", fontSize = 11.sp, color = Slate400)
            }

            Spacer(Modifier.height(10.dp))
            Box(Modifier.fillMaxWidth().height(1.dp).background(Slate100))
            Spacer(Modifier.height(10.dp))

            // Footer: total + recibo
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text("Total", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Slate400, letterSpacing = 1.sp)
                    Text("$${pedido.total?.toInt() ?: 0}", fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = DarkSlate)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = { /* TODO: recibo PDF */ },
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        border = BorderStroke(1.dp, Slate200)
                    ) {
                        Icon(Icons.Filled.Description, null, tint = Slate500, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Recibo", fontSize = 11.sp, color = Slate500)
                    }
                }
            }
        }
    }
}

private fun imageUrlStr(url: String): Any =
    if (url.startsWith("http://") || url.startsWith("https://")) url else com.agroconecta.app.data.api.ApiConfig.IMAGES_URL + url
