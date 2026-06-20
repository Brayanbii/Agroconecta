package com.agroconecta.app.ui.theme

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.agroconecta.app.data.model.Producto
import com.agroconecta.app.data.model.UsuarioInfo
import com.agroconecta.app.ui.theme.AgroNaranja
import com.agroconecta.app.ui.theme.AgroVerde
import com.agroconecta.app.ui.theme.AgroVerdeClaro
import com.agroconecta.app.ui.theme.CampesinoPublicarForm
import com.agroconecta.app.viewmodel.AuthViewModel
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush

@Composable
fun ModernHeader(
    username: String,
    rol: String,
    onProfileClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(AgroVerde, AgroVerde.copy(alpha = 0.9f))
                )
            )
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "Hola, $username",
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = if (rol == "CAMPESINO") "🌾 Productor" else "🍎 Cliente",
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.8f)
            )
        }
        Box(
            modifier = Modifier
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.2f))
                .clickable { onProfileClick() }
                .padding(10.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(text = "👤", fontSize = 20.sp) // Cambiado a Avatar
        }
    }
}

@Composable
fun HomeScreen(
    usuario: UsuarioInfo,
    viewModel: AuthViewModel,
    onNavigateToPerfil: () -> Unit,
    onNavigateToFavoritos: () -> Unit
) {
    var tabSeleccionadaCampesino by remember { mutableStateOf(0) }
    var tabSeleccionadaCliente by remember { mutableStateOf(0) }
    val itemsCarritoReal = remember { mutableStateListOf<Pair<Producto, Int>>() }

    LaunchedEffect(Unit) {
        viewModel.cargarProductosDeXampp()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        ModernHeader(
            username = usuario.userName,
            rol = usuario.rol,
            onProfileClick = onNavigateToPerfil
        )

        // 2. CONTENIDO PRINCIPAL DINÁMICO
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            if (usuario.rol == "CAMPESINO") {
                when (tabSeleccionadaCampesino) {
                    0 -> CampesinoDashboard()
                    1 -> CampesinoInventario(viewModel)
                    2 -> CampesinoPublicarForm(viewModel = viewModel)
                }
            } else {
                when (tabSeleccionadaCliente) {
                    0 -> ClienteCatalogScreen(
                        viewModel = viewModel,
                        onAgregarAlCarrito = { producto ->
                            val existe = itemsCarritoReal.indexOfFirst { it.first.id == producto.id }
                            if (existe != -1) {
                                val anterior = itemsCarritoReal[existe]
                                itemsCarritoReal[existe] = Pair(anterior.first, anterior.second + 1)
                            } else {
                                itemsCarritoReal.add(Pair(producto, 1))
                            }
                        }
                    )
                    1 -> ClienteCarritoScreen(
                        items = itemsCarritoReal,
                        onLimpiarCarrito = { itemsCarritoReal.clear() }
                    )
                }
            }
        }

        // 3. BARRA DE NAVEGACIÓN INFERIOR PERSONALIZADA (Reemplaza NavigationBar para evitar errores de versión)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (usuario.rol == "CAMPESINO") {
                // Pestaña Inicio
                Column(
                    modifier = Modifier
                        .clickable { tabSeleccionadaCampesino = 0 }
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = "📊", fontSize = 22.sp)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Inicio",
                        fontSize = 11.sp,
                        color = if (tabSeleccionadaCampesino == 0) AgroVerde else Color.Gray,
                        fontWeight = if (tabSeleccionadaCampesino == 0) FontWeight.Bold else FontWeight.Normal
                    )
                }

                // Pestaña Inventario
                Column(
                    modifier = Modifier
                        .clickable { tabSeleccionadaCampesino = 1 }
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = "📦", fontSize = 22.sp)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Mi Inventario",
                        fontSize = 11.sp,
                        color = if (tabSeleccionadaCampesino == 1) AgroVerde else Color.Gray,
                        fontWeight = if (tabSeleccionadaCampesino == 1) FontWeight.Bold else FontWeight.Normal
                    )
                }

                // Pestaña Publicar
                Column(
                    modifier = Modifier
                        .clickable { tabSeleccionadaCampesino = 2 }
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = "➕", fontSize = 22.sp)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Publicar",
                        fontSize = 11.sp,
                        color = if (tabSeleccionadaCampesino == 2) AgroVerde else Color.Gray,
                        fontWeight = if (tabSeleccionadaCampesino == 2) FontWeight.Bold else FontWeight.Normal
                    )
                }
            } else {
                // Pestaña Catálogo
                Column(
                    modifier = Modifier
                        .clickable { tabSeleccionadaCliente = 0 }
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = "🔍", fontSize = 22.sp)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Catálogo",
                        fontSize = 11.sp,
                        color = if (tabSeleccionadaCliente == 0) AgroVerde else Color.Gray,
                        fontWeight = if (tabSeleccionadaCliente == 0) FontWeight.Bold else FontWeight.Normal
                    )
                }

                // Pestaña Favoritos
                Column(
                    modifier = Modifier
                        .clickable { onNavigateToFavoritos() }
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = "❤", fontSize = 22.sp)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Favoritos",
                        fontSize = 11.sp,
                        color = Color.Gray,
                        fontWeight = FontWeight.Normal
                    )
                }

                // Pestaña Carrito con Globo de Notificación Hecho a Mano
                Column(
                    modifier = Modifier
                        .clickable { tabSeleccionadaCliente = 1 }
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(contentAlignment = Alignment.TopEnd) {
                        Text(text = "🛒", fontSize = 22.sp)
                        if (itemsCarritoReal.isNotEmpty()) {
                            Box(
                                modifier = Modifier
                                    .offset(x = 10.dp, y = (-4).dp)
                                    .background(AgroNaranja, CircleShape)
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = itemsCarritoReal.size.toString(),
                                    color = Color.White,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Carrito",
                        fontSize = 11.sp,
                        color = if (tabSeleccionadaCliente == 1) AgroVerde else Color.Gray,
                        fontWeight = if (tabSeleccionadaCliente == 1) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }
    }
}

// ==========================================
//   VISTAS 1: MUNDO CAMPESINO (PRODUCTOR)
// ==========================================

@Composable
fun CampesinoDashboard() {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = AgroVerde),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "Panel de Productor",
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Finca AgroConecta",
                        color = Color.White,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier
                            .background(Color.White.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "✅", fontSize = 14.sp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Asociación Local Verificada",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }

        item {
            Text(
                text = "Resumen de Ventas Realistas",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.DarkGray
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                MetricCard(
                    title = "Ganancias",
                    value = "$240.000 COP",
                    emoji = "💰",
                    colorAccent = AgroVerde,
                    modifier = Modifier.weight(1f)
                )
                MetricCard(
                    title = "Entregas",
                    value = "8 Pedidos",
                    emoji = "🚚",
                    colorAccent = AgroNaranja,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(AgroVerdeClaro, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("📢", fontSize = 20.sp)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Consejo del SIPSA",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "La papa pastusa subió 5% esta semana. Buen momento para comercializar.",
                            fontSize = 12.sp,
                            color = Color.Gray,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MetricCard(
    title: String,
    value: String,
    emoji: String,
    colorAccent: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(colorAccent.copy(alpha = 0.1f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(text = emoji, fontSize = 18.sp)
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(text = title, fontSize = 12.sp, color = Color.Gray)
            Text(text = value, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.Black)
        }
    }
}

@Composable
fun CampesinoInventario(viewModel: AuthViewModel) {
    val productos = viewModel.listaProductosReales

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Mis Productos en Venta",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Inventario real cargado desde tu base de datos de XAMPP.",
            fontSize = 12.sp,
            color = Color.Gray
        )
        Spacer(modifier = Modifier.height(16.dp))

        if (viewModel.estaCargando) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = AgroVerde)
            }
        } else if (productos.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(text = "No has publicado productos en MySQL todavía.", color = Color.Gray, fontSize = 14.sp)
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(productos) { prod ->
                    val emojiItem = when {
                        prod.nombre.contains("Tomate", ignoreCase = true) -> "🍅"
                        prod.nombre.contains("Cebolla", ignoreCase = true) -> "🧅"
                        prod.nombre.contains("Papa", ignoreCase = true) -> "🥔"
                        prod.nombre.contains("Plátano", ignoreCase = true) -> "🍌"
                        prod.nombre.contains("Naranja", ignoreCase = true) -> "🍊"
                        prod.nombre.contains("Zanahoria", ignoreCase = true) -> "🥕"
                        else -> "🌾"
                    }

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(50.dp)
                                    .background(AgroVerdeClaro, RoundedCornerShape(8.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = emojiItem, fontSize = 24.sp)
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = prod.nombre, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                Text(text = "Stock: ${prod.stock} Kg disponibles", fontSize = 12.sp, color = Color.Gray)
                            }
                            Text(text = "$${prod.precio.toInt()} COP/Kg", fontWeight = FontWeight.Bold, color = AgroVerde, fontSize = 14.sp)
                        }
                    }
                }
            }
        }
    }
}

// CampesinoPublicarForm ahora está en CampesinoPublicarForm.kt


// ==========================================
//   VISTAS 2: MUNDO CLIENTE (COMPRADOR) - CATÁLOGO REDISEÑADO 2026
// ==========================================

@Composable
fun ClienteCatalogScreen(
    viewModel: AuthViewModel,
    onAgregarAlCarrito: (Producto) -> Unit
) {
    val catalogoProductos = viewModel.listaProductosReales
    var filtroBusqueda by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Header de búsqueda moderneado
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(AgroVerde, AgroVerde.copy(alpha = 0.95f))
                    )
                )
                .padding(16.dp)
        ) {
            Column {
                Text(
                    text = "🍎 Cosechas Fresh",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = filtroBusqueda,
                    onValueChange = { filtroBusqueda = it },
                    placeholder = { Text("Buscar verduras, frutas...") },
                    leadingIcon = { Text("🔍", fontSize = 18.sp) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    singleLine = true,
                    colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.White,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.5f),
                        focusedTextColor = Color.Black,
                        unfocusedTextColor = Color.Black,
                        cursorColor = AgroVerde,
                        focusedPlaceholderColor = Color.Gray,
                        unfocusedPlaceholderColor = Color.Gray.copy(alpha = 0.7f)
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "${catalogoProductos.size} productos disponibles",
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = Color.Gray,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        if (viewModel.estaCargando && catalogoProductos.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = AgroVerde)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(text = "Cargando cosechas...", color = Color.Gray, fontSize = 14.sp)
                }
            }
        } else if (catalogoProductos.isEmpty()) {
            EmptyStateView(
                emoji = "🥬",
                titulo = "Sin productos disponibles",
                mensaje = "Pronto habrá verduras frescas."
            )
        } else {
            val productosFiltrados = catalogoProductos.filter {
                it.nombre.contains(filtroBusqueda, ignoreCase = true)
            }

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp)
            ) {
                items(productosFiltrados) { prod ->
                   ProductoCard(
                        producto = prod,
                        onAgregar = { onAgregarAlCarrito(prod) }
                    )
                }
            }
        }
    }
}

@Composable
private fun ProductoCard(
    producto: Producto,
    onAgregar: () -> Unit
) {
    val emoji = when {
        producto.nombre.contains("Tomate", ignoreCase = true) -> "🍅"
        producto.nombre.contains("Cebolla", ignoreCase = true) -> "🧅"
        producto.nombre.contains("Papa", ignoreCase = true) -> "🥔"
        producto.nombre.contains("Plátano", ignoreCase = true) -> "🍌"
        producto.nombre.contains("Naranja", ignoreCase = true) -> "🍊"
        producto.nombre.contains("Zanahoria", ignoreCase = true) -> "🥕"
        producto.nombre.contains("Aguacate", ignoreCase = true) -> "🥑"
        producto.nombre.contains("Manzana", ignoreCase = true) -> "🍏"
        producto.nombre.contains("Uva", ignoreCase = true) -> "🍇"
        else -> "🥬"
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Imagen con fondo degradado
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(90.dp)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                AgroVerdeClaro.copy(alpha = 0.3f),
                                AgroVerdeClaro.copy(alpha = 0.1f)
                            )
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(text = emoji, fontSize = 42.sp)
            }
            
            Spacer(modifier = Modifier.height(10.dp))
            
            Text(
                text = producto.nombre,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = Color.DarkGray
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = "$${producto.precio.toInt()} COP",
                fontSize = 16.sp,
                fontWeight = FontWeight.ExtraBold,
                color = AgroVerde
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${producto.stock}kg",
                    fontSize = 11.sp,
                    color = Color.Gray
                )
                Text(
                    text = "x kg",
                    fontSize = 11.sp,
                    color = Color.Gray
                )
            }
            
            Spacer(modifier = Modifier.height(10.dp))
            
            Button(
                onClick = onAgregar,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(38.dp),
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AgroVerde,
                    contentColor = Color.White
                ),
                contentPadding = PaddingValues(0.dp)
            ) {
                Text(text = "➕ Agregar", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
private fun EmptyStateView(
    emoji: String,
    titulo: String,
    mensaje: String
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            Text(text = emoji, fontSize = 64.sp)
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = titulo,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.DarkGray
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = mensaje,
                fontSize = 14.sp,
                color = Color.Gray
            )
        }
    }
}

// ==========================================
//   CARRITO DE COMPRAS REDISEÑADO 2026
// ==========================================

@Composable
fun ClienteCarritoScreen(
    items: List<Pair<Producto, Int>>,
    onLimpiarCarrito: () -> Unit
) {
    val total = items.sumOf { it.first.precio * it.second }
    var ordenCreada by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Header moderneado
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(AgroVerde, AgroVerde.copy(alpha = 0.95f))
                    )
                )
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "🛒 Mi Carrito",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                if (items.isNotEmpty()) {
                    Text(
                        text = "${items.sumOf { it.second }} items",
                        fontSize = 14.sp,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }
            }
        }

        if (items.isEmpty() && !ordenCreada) {
            EmptyStateView(
                emoji = "🛒",
                titulo = "Carrito vacío",
                mensaje = "Agrega verduras frescas para tu pedido"
            )
        } else if (ordenCreada) {
            // Pantalla de éxito
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(24.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("🎉", fontSize = 64.sp)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "¡Pedido Confirmado!",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = AgroVerde
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "El productor receberá tu orden pronto.",
                            fontSize = 14.sp,
                            color = Color.Gray,
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        Button(
                            onClick = { ordenCreada = false },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = AgroVerde),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Seguir Comprando", color = Color.White)
                        }
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item { Spacer(modifier = Modifier.height(8.dp)) }
                
                items(items) { par ->
                    CarritoItemCard(
                        producto = par.first,
                        cantidad = par.second
                    )
                }
                
                item { Spacer(modifier = Modifier.height(8.dp)) }
            }

            // Footer de pago
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Subtotal",
                            fontSize = 14.sp,
                            color = Color.Gray
                        )
                        Text(
                            text = "$${total.toInt()} COP",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Envío",
                            fontSize = 14.sp,
                            color = Color.Gray
                        )
                        Text(
                            text = "Por definir",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = AgroNaranja
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(AgroVerdeClaro)
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Total",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "$${total.toInt()} COP",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = AgroVerde
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Button(
                        onClick = {
                            ordenCreada = true
                            onLimpiarCarrito()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(26.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AgroVerde)
                    ) {
                        Text(
                            text = "✅ Confirmar Pedido",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CarritoItemCard(
    producto: Producto,
    cantidad: Int
) {
    val emoji = when {
        producto.nombre.contains("Tomate", ignoreCase = true) -> "🍅"
        producto.nombre.contains("Cebolla", ignoreCase = true) -> "🧅"
        producto.nombre.contains("Papa", ignoreCase = true) -> "🥔"
        producto.nombre.contains("Plátano", ignoreCase = true) -> "🍌"
        producto.nombre.contains("Naranja", ignoreCase = true) -> "🍊"
        producto.nombre.contains("Zanahoria", ignoreCase = true) -> "🥕"
        else -> "🥬"
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Imagen
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .background(
                        AgroVerdeClaro.copy(alpha = 0.3f),
                        RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(text = emoji, fontSize = 28.sp)
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            // Info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = producto.nombre,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "$cantidad kg x $${producto.precio.toInt()}",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
            
            // Precio total
            Text(
                text = "$${(producto.precio * cantidad).toInt()}",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = AgroVerde
            )
        }
    }
}
