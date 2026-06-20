package com.agroconecta.app.ui.theme

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.agroconecta.app.data.model.Producto
import com.agroconecta.app.data.model.UsuarioInfo
import com.agroconecta.app.ui.components.PullToRefreshWrapper
import com.agroconecta.app.ui.components.PremiumToastHost
import com.agroconecta.app.viewmodel.TiendaViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

fun imageUrl(producto: Producto): Any {
    val url = producto.imagenUrl ?: "default.png"
    return if (url.startsWith("http://") || url.startsWith("https://")) url
    else com.agroconecta.app.data.api.ApiConfig.IMAGES_URL + url
}

enum class CardSection { DESTACADOS, NUEVOS, OFERTAS, RECOMENDADOS, CERCA }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TiendaScreen(
    usuario: UsuarioInfo,
    tiendaVM: TiendaViewModel,
    onNavigateToPerfil: () -> Unit,
    onNavigateToDirecciones: () -> Unit,
    onNavigateToFavoritos: () -> Unit,
    onProductoClick: (Producto) -> Unit,
    onCarritoClick: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var showSearch by remember { mutableStateOf(false) }
    var showCategory by remember { mutableStateOf<String?>(null) }
    var showSearchResults by remember { mutableStateOf<String?>(null) }
    var verSeccion by remember { mutableStateOf<Pair<String, CardSection>?>(null) }
    var isVisible by remember { mutableStateOf(false) }
    var showLocationDialog by remember { mutableStateOf(false) }
    var isRefreshing by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { delay(100); isVisible = true; tiendaVM.cargarProductos(); tiendaVM.cargarDirecciones(); tiendaVM.cargarFavoritos() }

    val listState = rememberLazyListState()
    val isAtTop by remember { derivedStateOf { listState.firstVisibleItemIndex == 0 && listState.firstVisibleItemScrollOffset == 0 } }

    val gpsLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { grants ->
        if (grants.values.all { it }) { 
            scope.launch { 
                try { 
                    val loc = getCurrentLocation(context)
                    if (loc != null) { 
                        tiendaVM.userLat = loc.first
                        tiendaVM.userLng = loc.second 
                        showLocationDialog = false
                    } else {
                        showLocationDialog = true
                    }
                } catch (_: Exception) { 
                    showLocationDialog = true
                } 
            } 
        } else {
            showLocationDialog = true
        }
    }

    val hasLocationPermission = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
    val hasLocation = tiendaVM.userLat != 0.0 || tiendaVM.userLng != 0.0

    if (showLocationDialog) {
        LocationPermissionDialog(
            onDismiss = { showLocationDialog = false },
            onEnableLocation = {
                if (hasLocationPermission) {
                    scope.launch {
                        try {
                            val loc = getCurrentLocation(context)
                            if (loc != null) {
                                tiendaVM.userLat = loc.first
                                tiendaVM.userLng = loc.second
                                showLocationDialog = false
                            } else {
                                val intent = android.content.Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                                context.startActivity(intent)
                            }
                        } catch (_: Exception) {
                            val intent = android.content.Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                            context.startActivity(intent)
                        }
                    }
                } else {
                    gpsLauncher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
                }
            },
            onOpenSettings = {
                val intent = android.content.Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                context.startActivity(intent)
            }
        )
    }

    if (showSearch) {
        SearchOverlay(tiendaVM = tiendaVM, onDismiss = { showSearch = false; tiendaVM.buscarProductos(""); tiendaVM.seleccionarCategoria(null) },
            onProductoClick = onProductoClick, onVerTodos = { showSearch = false; showSearchResults = it },
            onCategoryClick = { showSearch = false; showCategory = it })
        return
    }
    showCategory?.let { cat ->
        SectionFullScreen(titulo = cat, productos = tiendaVM.todosLosProductos.filter { p -> p.categoria?.equals(cat, ignoreCase = true) == true },
            tiendaVM = tiendaVM, section = CardSection.DESTACADOS, onBack = { showCategory = null; tiendaVM.seleccionarCategoria(null) }, onProductoClick = onProductoClick)
        return
    }
    showSearchResults?.let { q ->
        SectionFullScreen(titulo = "Resultados: $q", productos = tiendaVM.todosLosProductos.filter { p -> p.nombre.contains(q, ignoreCase = true) },
            tiendaVM = tiendaVM, section = CardSection.DESTACADOS, onBack = { showSearchResults = null; tiendaVM.buscarProductos("") }, onProductoClick = onProductoClick)
        return
    }
    verSeccion?.let { (titulo, section) ->
        val items = when (section) { CardSection.DESTACADOS -> tiendaVM.getDestacadosFull(); CardSection.NUEVOS -> tiendaVM.getRecienCosechadosFull(); CardSection.OFERTAS -> tiendaVM.getOfertasFull(); CardSection.RECOMENDADOS -> tiendaVM.getRecomendadosFull(); CardSection.CERCA -> tiendaVM.getCercaDeTiFull() }
        SectionFullScreen(titulo = titulo, productos = items, tiendaVM = tiendaVM, section = section, onBack = { verSeccion = null }, onProductoClick = onProductoClick)
        return
    }

    if (tiendaVM.estaCargando && tiendaVM.todosLosProductos.isEmpty()) {
        Column(modifier = Modifier.fillMaxSize().background(Color(0xFFF8FAF8)).statusBarsPadding()) {
            PremiumHeader(tiendaVM = tiendaVM, onSearchTap = { showSearch = true }, onDireccionesTap = onNavigateToDirecciones, onCarritoClick = onCarritoClick)
            SkeletonGrid()
        }
        return
    }

    Scaffold(
        containerColor = Color(0xFFF8FAF8),
        bottomBar = {
            Surface(
                shadowElevation = 20.dp,
                color = Color.White,
                shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
                modifier = Modifier.drawBehind {
                    drawLine(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                Color(0xFF22C55E).copy(alpha = 0.15f),
                                Color(0xFF16A34A).copy(alpha = 0.08f),
                                Color(0xFF22C55E).copy(alpha = 0.15f)
                            )
                        ),
                        strokeWidth = 2f,
                        start = Offset(0f, 0f),
                        end = Offset(size.width, 0f)
                    )
                }
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().navigationBarsPadding().padding(horizontal = 8.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    BottomNavItem(Icons.Filled.Store, "Tienda", true, onClick = {})
                    BottomNavItem(Icons.Filled.FavoriteBorder, "Fav", false, badge = 0, onClick = onNavigateToFavoritos)
                    BottomNavItem(Icons.Outlined.ShoppingCart, "Carrito", false, badge = tiendaVM.cantidadItemsCarrito, onClick = onCarritoClick)
                    BottomNavItem(Icons.Filled.Person, "Perfil", false, onClick = onNavigateToPerfil)
                }
            }
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .drawBehind {
                        drawCircle(
                            color = Color(0xFF16A34A).copy(alpha = 0.025f),
                            radius = size.width * 0.6f,
                            center = Offset(size.width * 0.15f, size.height * 0.15f)
                        )
                        drawCircle(
                            color = Color(0xFFEAB308).copy(alpha = 0.02f),
                            radius = size.width * 0.4f,
                            center = Offset(size.width * 0.85f, size.height * 0.3f)
                        )
                        drawCircle(
                            color = Color(0xFF22C55E).copy(alpha = 0.015f),
                            radius = size.width * 0.5f,
                            center = Offset(size.width * 0.5f, size.height * 0.7f)
                        )
                    }
            )

            Box(modifier = Modifier.fillMaxSize()) {
                Column(modifier = Modifier.fillMaxSize()) {
                    PremiumHeader(tiendaVM = tiendaVM, onSearchTap = { showSearch = true }, onDireccionesTap = onNavigateToDirecciones, onCarritoClick = onCarritoClick)

                PullToRefreshWrapper(
                    isRefreshing = tiendaVM.refrescando,
                    onRefresh = { tiendaVM.refrescarTienda() },
                    isAtTop = isAtTop,
                    modifier = Modifier.fillMaxSize()
                ) {
                    LazyColumn(state = listState, modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 16.dp)) {
                    item {
                        HeroBanner()
                        Spacer(Modifier.height(20.dp))
                        CategoryPills(tiendaVM = tiendaVM, onCategoryClick = { showCategory = it }, onTodosClick = { showCategory = null; tiendaVM.seleccionarCategoria(null) })
                    }

                    item {
                        Spacer(Modifier.height(12.dp))
                        SectionHeaderPremium("Lo mas destacado", "Mejor calificados", Icons.Filled.Whatshot, Color(0xFFF97316), Color(0xFFFFF7ED)) { verSeccion = "Lo mas destacado" to CardSection.DESTACADOS }
                        Spacer(Modifier.height(8.dp))
                        ProductosRowNew(tiendaVM.getDestacados(), tiendaVM, onProductoClick, CardSection.DESTACADOS) { verSeccion = "Lo mas destacado" to CardSection.DESTACADOS }
                    }

                    item {
                        Spacer(Modifier.height(16.dp))
                        SectionHeaderPremium("Recien cosechados", "Del campo a tu mesa", Icons.Filled.Eco, Color(0xFF16A34A), Color(0xFFF0FDF4)) { verSeccion = "Recien cosechados" to CardSection.NUEVOS }
                        Spacer(Modifier.height(8.dp))
                        ProductosRowNew(tiendaVM.getRecienCosechados(), tiendaVM, onProductoClick, CardSection.NUEVOS) { verSeccion = "Recien cosechados" to CardSection.NUEVOS }
                    }

                    item {
                        Spacer(Modifier.height(16.dp))
                        SectionHeaderPremium("Ofertas especiales", "Precios imperdibles", Icons.Filled.LocalOffer, Color(0xFFEF4444), Color(0xFFFEF2F2)) { verSeccion = "Ofertas especiales" to CardSection.OFERTAS }
                        Spacer(Modifier.height(8.dp))
                        ProductosRowNew(tiendaVM.getOfertas(), tiendaVM, onProductoClick, CardSection.OFERTAS) { verSeccion = "Ofertas especiales" to CardSection.OFERTAS }
                    }

                    item {
                        Spacer(Modifier.height(16.dp))
                        SectionHeaderPremium("Recomendados para ti", "Basado en tus gustos", Icons.Filled.AutoAwesome, Color(0xFF7C3AED), Color(0xFFF5F3FF)) { verSeccion = "Recomendados para ti" to CardSection.RECOMENDADOS }
                        Spacer(Modifier.height(8.dp))
                        ProductosRowNew(tiendaVM.getRecomendados(), tiendaVM, onProductoClick, CardSection.RECOMENDADOS) { verSeccion = "Recomendados para ti" to CardSection.RECOMENDADOS }
                    }

                    item {
                        Spacer(Modifier.height(16.dp))
                        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFEFF6FF)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Filled.LocationOn, null, tint = Color(0xFF2563EB), modifier = Modifier.size(20.dp))
                                }
                                Spacer(Modifier.width(12.dp))
                                Column {
                                    Text("Cerca de ti", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF111827), letterSpacing = (-0.3).sp)
                                    Text("Productores en tu zona", fontSize = 12.sp, color = Color(0xFF94A3B8), fontWeight = FontWeight.Medium)
                                }
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(onClick = {
                                    if (hasLocationPermission && hasLocation) {
                                        scope.launch { 
                                            try { 
                                                val loc = getCurrentLocation(context)
                                                if (loc != null) { 
                                                    tiendaVM.userLat = loc.first
                                                    tiendaVM.userLng = loc.second 
                                                }
                                            } catch (_: Exception) {} 
                                        }
                                    } else {
                                        showLocationDialog = true
                                    }
                                }) { Icon(Icons.Filled.MyLocation, "GPS", tint = Color(0xFF16A34A), modifier = Modifier.size(20.dp)) }
                                TextButton(onClick = { 
                                    if (hasLocationPermission && hasLocation) {
                                        verSeccion = "Cerca de ti" to CardSection.CERCA 
                                    } else {
                                        showLocationDialog = true
                                    }
                                }) { Text("Ver todos", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF16A34A)) }
                            }
                        }
                        Spacer(Modifier.height(8.dp))
                        ProductosRowNew(tiendaVM.getCercaDeTi(), tiendaVM, onProductoClick, CardSection.CERCA) { verSeccion = "Cerca de ti" to CardSection.CERCA }
                    }
                    item { Spacer(Modifier.height(80.dp)) }
                }
                }
            }
            val toastState = remember { derivedStateOf { tiendaVM.toastEvent } }
            PremiumToastHost(toastState = toastState, onDismiss = { tiendaVM.dismissToast() }, modifier = Modifier.align(Alignment.TopCenter))
        }
        }
    }
}

@Composable
fun HeroBanner() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .height(160.dp)
            .clip(RoundedCornerShape(28.dp))
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color(0xFF064E3B),
                        Color(0xFF065F46),
                        Color(0xFF047857),
                        Color(0xFF059669)
                    )
                )
            )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .drawBehind {
                    drawCircle(
                        color = Color(0xFF22C55E).copy(alpha = 0.12f),
                        radius = size.width * 0.3f,
                        center = Offset(size.width * 0.85f, size.height * 0.2f)
                    )
                    drawCircle(
                        color = Color(0xFF10B981).copy(alpha = 0.08f),
                        radius = size.width * 0.25f,
                        center = Offset(size.width * 0.1f, size.height * 0.8f)
                    )
                    drawCircle(
                        color = Color.White.copy(alpha = 0.04f),
                        radius = size.width * 0.15f,
                        center = Offset(size.width * 0.6f, size.height * 0.5f)
                    )
                }
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.15f))
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.White.copy(alpha = 0.15f))
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "AGROCONECTA",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White.copy(alpha = 0.9f),
                    letterSpacing = 1.5.sp
                )
            }

            Spacer(Modifier.height(10.dp))

            Text(
                text = "Del campo colombiano",
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White,
                letterSpacing = (-0.5).sp,
                lineHeight = 26.sp
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "directo a tu mesa",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF86EFAC),
                    letterSpacing = (-0.5).sp,
                    lineHeight = 26.sp
                )
            }

            Spacer(Modifier.height(8.dp))

            Text(
                text = "Calidad premium, precios justos y sabor real.",
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White.copy(alpha = 0.65f),
                lineHeight = 16.sp
            )
        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 20.dp, bottom = 16.dp)
                .size(56.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Filled.Store,
                null,
                tint = Color.White.copy(alpha = 0.7f),
                modifier = Modifier.size(28.dp)
            )
        }
    }
}

@Composable
fun PremiumHeader(tiendaVM: TiendaViewModel, onSearchTap: () -> Unit, onDireccionesTap: () -> Unit, onCarritoClick: () -> Unit) {
    Surface(color = Color(0xFFF8FAF8), shadowElevation = 0.dp, modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(horizontal = 20.dp).padding(top = 12.dp, bottom = 4.dp).statusBarsPadding()) {
            // Connection status
            val dotColor = if (tiendaVM.conectado) Color(0xFF22C55E) else Color(0xFFEF4444)
            val statusText = if (tiendaVM.conectado) "Conectado" else "Sin conexion"
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 6.dp)) {
                Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(dotColor))
                Spacer(Modifier.width(6.dp))
                Text(statusText, fontSize = 11.sp, color = Color(0xFF94A3B8), fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
                // Boton de recarga rapida
                Surface(
                    color = if (tiendaVM.estaCargando) Color(0xFF22C55E).copy(alpha = 0.1f) else Color(0xFFF1F5F9),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.clickable { tiendaVM.refrescarTienda() }
                ) {
                    Row(modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                        if (tiendaVM.refrescando) {
                            CircularProgressIndicator(modifier = Modifier.size(12.dp), color = Color(0xFF22C55E), strokeWidth = 2.dp)
                            Spacer(Modifier.width(6.dp))
                            Text("Cargando...", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF22C55E))
                        } else {
                            Icon(Icons.Filled.Refresh, null, tint = Color(0xFF94A3B8), modifier = Modifier.size(14.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Recargar", fontSize = 11.sp, fontWeight = FontWeight.Medium, color = Color(0xFF94A3B8))
                        }
                    }
                }
            }
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    color = Color.White, shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.weight(1f).height(52.dp).clickable { onDireccionesTap() },
                    shadowElevation = 2.dp,
                    border = BorderStroke(1.dp, Color(0xFFF1F5F9))
                ) {
                    Row(modifier = Modifier.padding(horizontal = 14.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(
                                    brush = Brush.linearGradient(
                                        colors = listOf(Color(0xFFEF4444), Color(0xFFDC2626))
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Filled.LocationOn, null, tint = Color.White, modifier = Modifier.size(18.dp))
                        }
                        Spacer(Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Entregar en", fontSize = 10.sp, color = Color(0xFF94A3B8), fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp)
                            Spacer(Modifier.height(1.dp))
                            Text(
                                tiendaVM.direccionEntrega ?: "Selecciona tu ubicacion",
                                fontSize = 13.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF111827), maxLines = 1, overflow = TextOverflow.Ellipsis
                            )
                        }
                        Icon(Icons.Filled.KeyboardArrowDown, null, tint = Color(0xFF94A3B8), modifier = Modifier.size(18.dp))
                    }
                }
                Spacer(Modifier.width(10.dp))
                BadgedBox(badge = { if (tiendaVM.cantidadItemsCarrito > 0) Badge(containerColor = Color(0xFF16A34A), contentColor = Color.White) { Text("${tiendaVM.cantidadItemsCarrito}", fontSize = 10.sp, fontWeight = FontWeight.Bold) } }) {
                    Surface(
                        color = Color.White, shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.size(52.dp).clickable { onCarritoClick() },
                        shadowElevation = 2.dp,
                        border = BorderStroke(1.dp, Color(0xFFF1F5F9))
                    ) {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                            Icon(Icons.Outlined.ShoppingCart, "Carrito", tint = Color(0xFF111827), modifier = Modifier.size(22.dp))
                        }
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            Surface(
                color = Color.White, shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth().height(52.dp).clickable { onSearchTap() },
                shadowElevation = 2.dp,
                border = BorderStroke(1.dp, Color(0xFFF1F5F9))
            ) {
                Row(modifier = Modifier.padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFF0FDF4)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Filled.Search, null, tint = Color(0xFF16A34A), modifier = Modifier.size(16.dp))
                    }
                    Spacer(Modifier.width(12.dp))
                    Text("Buscar frutas, verduras...", fontSize = 14.sp, color = Color(0xFF94A3B8), modifier = Modifier.weight(1f), fontWeight = FontWeight.Medium)
                    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
                    val pulseScale by infiniteTransition.animateFloat(initialValue = 0.95f, targetValue = 1.05f, animationSpec = infiniteRepeatable(tween(1200), RepeatMode.Reverse), label = "pulse")
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(
                                brush = Brush.linearGradient(
                                    colors = listOf(Color(0xFFDCFCE7), Color(0xFFBBF7D0))
                                )
                            )
                            .graphicsLayer { scaleX = pulseScale; scaleY = pulseScale },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Filled.Mic, null, tint = Color(0xFF16A34A), modifier = Modifier.size(18.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun CategoryPills(tiendaVM: TiendaViewModel, onCategoryClick: (String) -> Unit, onTodosClick: () -> Unit) {
    val categorias = tiendaVM.obtenerCategorias()
    LazyRow(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), contentPadding = PaddingValues(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        item {
            val interactionSource = remember { MutableInteractionSource() }
            val isPressed by interactionSource.collectIsPressedAsState()
            val scale by animateFloatAsState(if (isPressed) 0.93f else 1f, spring(dampingRatio = Spring.DampingRatioMediumBouncy), label = "pill")
            val isAll = tiendaVM.categoriaSeleccionada == null
            Surface(
                color = if (isAll) Color(0xFF16A34A) else Color.White,
                shape = RoundedCornerShape(50),
                shadowElevation = if (isAll) 8.dp else 1.dp,
                modifier = Modifier.height(48.dp).graphicsLayer { scaleX = scale; scaleY = scale }.clickable(interactionSource, null) { onTodosClick() },
                border = if (!isAll) BorderStroke(1.dp, Color(0xFFE2E8F0)) else null
            ) {
                Row(modifier = Modifier.padding(horizontal = 20.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Apps, null, tint = if (isAll) Color.White else Color(0xFF64748B), modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Todos", fontSize = 13.sp, fontWeight = FontWeight.ExtraBold, color = if (isAll) Color.White else Color(0xFF64748B))
                }
            }
        }
        items(categorias) { cat ->
            val (icon, color) = getCategoryIconAndColor(cat)
            val interactionSource = remember { MutableInteractionSource() }
            val isPressed by interactionSource.collectIsPressedAsState()
            val scale by animateFloatAsState(if (isPressed) 0.93f else 1f, spring(dampingRatio = Spring.DampingRatioMediumBouncy), label = "pill")
            val isSelected = tiendaVM.categoriaSeleccionada == cat
            Surface(
                color = if (isSelected) color.copy(alpha = 0.1f) else Color.White,
                shape = RoundedCornerShape(50),
                shadowElevation = 1.dp,
                modifier = Modifier.height(48.dp).graphicsLayer { scaleX = scale; scaleY = scale }.clickable(interactionSource, null) { onCategoryClick(cat) },
                border = BorderStroke(1.dp, if (isSelected) color.copy(alpha = 0.3f) else Color(0xFFE2E8F0))
            ) {
                Row(modifier = Modifier.padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(26.dp)
                            .clip(CircleShape)
                            .background(color.copy(alpha = if (isSelected) 0.15f else 0.08f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(icon, null, tint = color, modifier = Modifier.size(14.dp))
                    }
                    Spacer(Modifier.width(8.dp))
                    Text(cat, fontSize = 13.sp, fontWeight = FontWeight.ExtraBold, color = if (isSelected) color else Color(0xFF64748B))
                }
            }
        }
    }
}

@Composable
fun SectionHeaderPremium(title: String, subtitle: String, icon: ImageVector, iconColor: Color, iconBgColor: Color, onVerTodos: () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(iconBgColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = iconColor, modifier = Modifier.size(20.dp))
            }
            Spacer(Modifier.width(12.dp))
            Column {
                Text(title, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF111827), letterSpacing = (-0.3).sp)
                Text(subtitle, fontSize = 12.sp, color = Color(0xFF94A3B8), fontWeight = FontWeight.Medium)
            }
        }
        Surface(
            color = iconBgColor,
            shape = RoundedCornerShape(50),
            modifier = Modifier.clickable { onVerTodos() }.height(32.dp)
        ) {
            Row(modifier = Modifier.padding(horizontal = 14.dp), verticalAlignment = Alignment.CenterVertically) {
                Text("Ver todos", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = iconColor)
                Spacer(Modifier.width(4.dp))
                Icon(Icons.AutoMirrored.Filled.ArrowForward, null, tint = iconColor, modifier = Modifier.size(14.dp))
            }
        }
    }
}

@Composable
fun ProductosRowNew(productos: List<Producto>, tiendaVM: TiendaViewModel, onProductoClick: (Producto) -> Unit, section: CardSection, onVerTodos: () -> Unit) {
    if (productos.isEmpty()) { Text("No hay productos", fontSize = 14.sp, color = Color(0xFF94A3B8), modifier = Modifier.padding(horizontal = 20.dp)); return }
    LazyRow(contentPadding = PaddingValues(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(14.dp)) {
        items(productos) { producto ->
            ProductoCardNew(producto = producto, tiendaVM = tiendaVM, onClick = { onProductoClick(producto) }, section = section, modifier = Modifier.width(220.dp))
        }
        item {
            val sectionColor = when (section) {
                CardSection.DESTACADOS -> Color(0xFFF97316)
                CardSection.NUEVOS -> Color(0xFF16A34A)
                CardSection.OFERTAS -> Color(0xFFEF4444)
                CardSection.RECOMENDADOS -> Color(0xFF7C3AED)
                CardSection.CERCA -> Color(0xFF2563EB)
            }
            val sectionBg = when (section) {
                CardSection.DESTACADOS -> Color(0xFFFFF7ED)
                CardSection.NUEVOS -> Color(0xFFF0FDF4)
                CardSection.OFERTAS -> Color(0xFFFEF2F2)
                CardSection.RECOMENDADOS -> Color(0xFFF5F3FF)
                CardSection.CERCA -> Color(0xFFEFF6FF)
            }
            Box(
                modifier = Modifier
                    .width(110.dp)
                    .height(260.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(sectionBg)
                    .clickable { onVerTodos() },
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(sectionColor),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, null, tint = Color.White, modifier = Modifier.size(24.dp))
                    }
                    Spacer(Modifier.height(12.dp))
                    Text("Ver", fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = sectionColor, textAlign = TextAlign.Center, lineHeight = 18.sp)
                    Text("todos", fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = sectionColor, textAlign = TextAlign.Center, lineHeight = 18.sp)
                }
            }
        }
    }
}

@Composable
fun ProductoCardNew(
    producto: Producto, tiendaVM: TiendaViewModel, onClick: () -> Unit,
    section: CardSection = CardSection.DESTACADOS, modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(if (isPressed) 0.95f else 1f, spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow), label = "scale")
    val (_, catColor) = getCategoryIconAndColor(producto.categoria ?: "Verduras")
    val prodId = producto.id ?: 0L
    val isFav = tiendaVM.esFavorito(prodId)

    Card(
        modifier = modifier.graphicsLayer { scaleX = scale; scaleY = scale }
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick),
        shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.border(1.dp, Color(0xFFF1F5F9), RoundedCornerShape(24.dp))) {
            Box(modifier = Modifier.fillMaxWidth().height(140.dp)) {
                AsyncImage(model = ImageRequest.Builder(LocalContext.current).data(imageUrl(producto)).crossfade(true).build(),
                    contentDescription = producto.nombre, contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                )
                Box(modifier = Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = 0.25f)))).clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)))

                val badgeText = when (section) {
                    CardSection.DESTACADOS -> "TOP VENTAS"
                    CardSection.NUEVOS -> "NUEVO"
                    CardSection.OFERTAS -> "OFERTA"
                    CardSection.RECOMENDADOS -> "PARA TI"
                    CardSection.CERCA -> "CERCA"
                }
                val badgeColor = when (section) {
                    CardSection.DESTACADOS -> Color(0xFFF97316)
                    CardSection.NUEVOS -> Color(0xFF16A34A)
                    CardSection.OFERTAS -> Color(0xFFEF4444)
                    CardSection.RECOMENDADOS -> Color(0xFF7C3AED)
                    CardSection.CERCA -> Color(0xFF2563EB)
                }
                Surface(color = Color.White.copy(alpha = 0.95f), shape = RoundedCornerShape(10.dp), modifier = Modifier.align(Alignment.TopStart).padding(10.dp)) {
                    Row(modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(badgeColor))
                        Spacer(Modifier.width(5.dp))
                        Text(badgeText, fontSize = 9.sp, fontWeight = FontWeight.ExtraBold, color = badgeColor, letterSpacing = 0.5.sp)
                    }
                }

                com.agroconecta.app.ui.components.FavoriteButtonSmall(
                    isFavorite = isFav,
                    onToggle = { tiendaVM.toggleFavorito(prodId) },
                    modifier = Modifier.align(Alignment.TopEnd).padding(10.dp)
                )

                if ((producto.promedioCalificacion ?: 0.0) > 0) {
                    val ratingGlow = rememberInfiniteTransition(label = "ratingGlow")
                    val glowAlpha by ratingGlow.animateFloat(
                        initialValue = 0.6f,
                        targetValue = 0.85f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(2000, easing = EaseInOutCubic),
                            repeatMode = RepeatMode.Reverse
                        ),
                        label = "glowAlpha"
                    )
                    Surface(color = Color.Black.copy(alpha = glowAlpha), shape = RoundedCornerShape(10.dp), modifier = Modifier.align(Alignment.BottomStart).padding(10.dp).shadow(4.dp, RoundedCornerShape(10.dp), ambientColor = Color(0xFFFBBF24).copy(alpha = 0.2f))) {
                        Row(modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Star, null, tint = Color(0xFFFBBF24), modifier = Modifier.size(13.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("${producto.promedioCalificacion}", fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
                        }
                    }
                }
            }

            Box(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(20.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFDCFCE7)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Filled.Person, null, tint = Color(0xFF16A34A), modifier = Modifier.size(12.dp))
                        }
                        Spacer(Modifier.width(5.dp))
                        Text(
                            producto.nombreCampesino ?: "Productor",
                            fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF64748B), maxLines = 1, overflow = TextOverflow.Ellipsis
                        )
                        Icon(Icons.Filled.Verified, null, tint = Color(0xFF3B82F6), modifier = Modifier.size(12.dp).padding(start = 3.dp))
                    }

                    Spacer(Modifier.height(6.dp))

                    Text(producto.nombre, fontSize = 15.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF111827), maxLines = 1, overflow = TextOverflow.Ellipsis, letterSpacing = (-0.2).sp)

                    Spacer(Modifier.height(4.dp))

                    if (!producto.municipioOrigen.isNullOrBlank()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.LocationOn, null, tint = Color(0xFFEF4444), modifier = Modifier.size(11.dp))
                            Spacer(Modifier.width(2.dp))
                            Text(producto.municipioOrigen, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF94A3B8), letterSpacing = 0.3.sp)
                        }
                        Spacer(Modifier.height(3.dp))
                    }

                    val frescura = tiendaVM.calcularCosechadoHace(producto.fechaCreacion)
                    if (frescura.isNotEmpty() || producto.stock > 0) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            if (frescura.isNotEmpty()) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(Color(0xFFF0FDF4))
                                        .padding(horizontal = 6.dp, vertical = 3.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Filled.Eco, null, tint = Color(0xFF16A34A), modifier = Modifier.size(10.dp))
                                        Spacer(Modifier.width(3.dp))
                                        Text(frescura, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFF15803D))
                                    }
                                }
                            }
                            if (producto.stock > 0) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(Color(0xFFEFF6FF))
                                        .padding(horizontal = 6.dp, vertical = 3.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Filled.Inventory, null, tint = Color(0xFF2563EB), modifier = Modifier.size(10.dp))
                                        Spacer(Modifier.width(3.dp))
                                        Text("${producto.stock} ${producto.unidad ?: "Kg"}", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2563EB))
                                    }
                                }
                            } else {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(Color(0xFFFEF2F2))
                                        .padding(horizontal = 6.dp, vertical = 3.dp)
                                ) {
                                    Text("Agotado", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFFEF4444))
                                }
                            }
                        }
                        Spacer(Modifier.height(8.dp))
                    }

                    if (section == CardSection.OFERTAS) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("$${(producto.precio * 1.15).toInt()}", fontSize = 11.sp, color = Color(0xFF94A3B8), textDecoration = TextDecoration.LineThrough)
                            Spacer(Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(Color(0xFFFEF2F2))
                                    .padding(horizontal = 5.dp, vertical = 2.dp)
                            ) {
                                Text("-15%", fontSize = 9.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFFEF4444))
                            }
                        }
                        Spacer(Modifier.height(2.dp))
                    }

                    Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                        Column {
                            Text("Precio", fontSize = 9.sp, color = Color(0xFF94A3B8), fontWeight = FontWeight.Bold, letterSpacing = 0.3.sp)
                            Spacer(Modifier.height(1.dp))
                            Row(verticalAlignment = Alignment.Bottom) {
                                Text("$${producto.precio.toInt()}", fontSize = 20.sp, fontWeight = FontWeight.Black, color = if (section == CardSection.OFERTAS) Color(0xFFEF4444) else Color(0xFF111827), letterSpacing = (-0.5).sp)
                                Spacer(Modifier.width(4.dp))
                                Text("/ ${producto.unidad ?: "Kg"}", fontSize = 11.sp, color = Color(0xFF94A3B8), fontWeight = FontWeight.Medium)
                            }
                        }

                        Box {
                            val btnInteraction = remember { MutableInteractionSource() }
                            val btnPressed by btnInteraction.collectIsPressedAsState()
                            val btnScale by animateFloatAsState(if (btnPressed) 0.88f else 1f, spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow), label = "btnScale")
                            Box(
                                modifier = Modifier.size(44.dp).graphicsLayer { scaleX = btnScale; scaleY = btnScale }.shadow(8.dp, CircleShape, ambientColor = Color(0xFF16A34A).copy(alpha = 0.35f), spotColor = Color(0xFF16A34A).copy(alpha = 0.25f)).clip(CircleShape).background(Brush.linearGradient(listOf(Color(0xFF22C55E), Color(0xFF16A34A)))).clickable(interactionSource = btnInteraction, indication = null) { tiendaVM.agregarAlCarrito(prodId, 1) },
                                contentAlignment = Alignment.Center
                            ) { Icon(Icons.Filled.Add, "Agregar", tint = Color.White, modifier = Modifier.size(22.dp)) }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BottomNavItem(icon: ImageVector, label: String, selected: Boolean, onClick: () -> Unit, badge: Int = 0) {
    val animScale by animateFloatAsState(if (selected) 1.05f else 1f, spring(dampingRatio = Spring.DampingRatioMediumBouncy), label = "nav")
    Column(horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.graphicsLayer { scaleX = animScale; scaleY = animScale }
            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = onClick).padding(horizontal = 12.dp, vertical = 4.dp)
    ) {
        BadgedBox(badge = { if (badge > 0) Badge(containerColor = Color(0xFF16A34A)) { Text("$badge", fontSize = 9.sp, color = Color.White) } }) {
            Box(
                modifier = Modifier
                    .size(if (selected) 40.dp else 36.dp)
                    .clip(
                        if (selected) RoundedCornerShape(14.dp) else CircleShape
                    )
                    .background(if (selected) Color(0xFFF0FDF4) else Color.Transparent),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, label, tint = if (selected) Color(0xFF16A34A) else Color(0xFF94A3B8), modifier = Modifier.size(if (selected) 22.dp else 20.dp))
            }
        }
        Spacer(Modifier.height(3.dp))
        Text(label, fontSize = 10.sp, fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium, color = if (selected) Color(0xFF16A34A) else Color(0xFF94A3B8))
    }
}

@Composable
fun SkeletonGrid() {
    val infiniteTransition = rememberInfiniteTransition(label = "skel")
    val alpha by infiniteTransition.animateFloat(initialValue = 0.3f, targetValue = 0.7f, animationSpec = infiniteRepeatable(tween(1200), RepeatMode.Reverse), label = "skel")
    Column(modifier = Modifier.padding(20.dp)) {
        Box(modifier = Modifier.fillMaxWidth().height(160.dp).clip(RoundedCornerShape(28.dp)).background(Color(0xFFE2E8F0).copy(alpha = alpha)))
        Spacer(Modifier.height(20.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { repeat(4) { Box(modifier = Modifier.width(80.dp).height(48.dp).clip(RoundedCornerShape(50)).background(Color(0xFFE2E8F0).copy(alpha = alpha))) } }
        Spacer(Modifier.height(30.dp))
        for (row in 0..1) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                repeat(2) {
                    Column(modifier = Modifier.weight(1f)) {
                        Box(modifier = Modifier.fillMaxWidth().height(140.dp).clip(RoundedCornerShape(24.dp)).background(Color(0xFFE2E8F0).copy(alpha = alpha)))
                        Spacer(Modifier.height(12.dp))
                        Box(modifier = Modifier.fillMaxWidth(0.8f).height(16.dp).clip(RoundedCornerShape(8.dp)).background(Color(0xFFE2E8F0).copy(alpha = alpha)))
                        Spacer(Modifier.height(8.dp))
                        Box(modifier = Modifier.fillMaxWidth(0.6f).height(12.dp).clip(RoundedCornerShape(8.dp)).background(Color(0xFFE2E8F0).copy(alpha = alpha)))
                        Spacer(Modifier.height(12.dp))
                        Box(modifier = Modifier.fillMaxWidth(0.5f).height(20.dp).clip(RoundedCornerShape(8.dp)).background(Color(0xFFE2E8F0).copy(alpha = alpha)))
                        Spacer(Modifier.height(20.dp))
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SectionFullScreen(titulo: String, productos: List<Producto>, tiendaVM: TiendaViewModel, section: CardSection, onBack: () -> Unit, onProductoClick: (Producto) -> Unit) {
    BackHandler(onBack = onBack)
    Scaffold(
        containerColor = Color(0xFFF8FAF8),
        topBar = {
            Surface(
                shadowElevation = 0.dp,
                color = Color.White,
                modifier = Modifier.border(1.dp, Color(0xFFF1F5F9))
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = 4.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color(0xFF111827))
                    }
                    Text(titulo, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF111827), letterSpacing = (-0.3).sp)
                }
            }
        }
    ) { p ->
        if (productos.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(p), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFF1F5F9)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Filled.SearchOff, null, tint = Color(0xFF94A3B8), modifier = Modifier.size(36.dp))
                    }
                    Spacer(Modifier.height(16.dp))
                    Text("No hay productos", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF111827))
                    Spacer(Modifier.height(4.dp))
                    Text("Intenta con otra categoria", fontSize = 13.sp, color = Color(0xFF94A3B8))
                }
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.padding(p)
            ) {
                items(items = productos, key = { it.id ?: 0L }) { producto ->
                    ProductoCardPremium(
                        producto = producto,
                        tiendaVM = tiendaVM,
                        onClick = { onProductoClick(producto) },
                        section = section
                    )
                }
            }
        }
    }
}

@Composable
fun ProductoCardPremium(
    producto: Producto,
    tiendaVM: TiendaViewModel,
    onClick: () -> Unit,
    section: CardSection = CardSection.DESTACADOS
) {
    val prodId = producto.id ?: 0L
    val isFav = tiendaVM.esFavorito(prodId)
    val pressInteraction = remember { MutableInteractionSource() }
    val isPressed by pressInteraction.collectIsPressedAsState()
    val pressScale by animateFloatAsState(if (isPressed) 0.96f else 1f, spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow), label = "cardScale")

    Card(
        modifier = Modifier.fillMaxWidth().graphicsLayer { scaleX = pressScale; scaleY = pressScale }.clickable(interactionSource = pressInteraction, indication = null, onClick = onClick),
        shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column {
            // Imagen con badges
            Box(modifier = Modifier.fillMaxWidth().aspectRatio(1f)) {
                AsyncImage(model = ImageRequest.Builder(LocalContext.current).data(imageUrl(producto)).crossfade(true).build(), contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)))
                // Favorito top-right
                com.agroconecta.app.ui.components.FavoriteButtonSmall(isFavorite = isFav, onToggle = { tiendaVM.toggleFavorito(prodId) }, modifier = Modifier.align(Alignment.TopEnd).padding(6.dp))
                // Categoria badge bottom-left
                if (!producto.categoria.isNullOrBlank()) {
                    Surface(color = Color.White.copy(alpha = 0.9f), shape = RoundedCornerShape(8.dp), modifier = Modifier.align(Alignment.BottomStart).padding(8.dp)) {
                        Text(producto.categoria!!.take(14), fontSize = 9.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF16A34A), modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                    }
                }
            }

            // Info
            Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp)) {
                Text(producto.nombre, fontSize = 13.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF111827), maxLines = 2, overflow = TextOverflow.Ellipsis, lineHeight = 16.sp)
                if (!producto.nombreCampesino.isNullOrBlank()) {
                    Text(producto.nombreCampesino!!, fontSize = 10.sp, fontWeight = FontWeight.Medium, color = Color(0xFF94A3B8), maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                Spacer(Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                    Column {
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text("$${producto.precio.toInt()}", fontSize = 17.sp, fontWeight = FontWeight.Black, color = Color(0xFF111827), letterSpacing = (-0.5).sp)
                            Spacer(Modifier.width(2.dp))
                            Text("/${producto.unidad ?: "Kg"}", fontSize = 10.sp, color = Color(0xFF94A3B8))
                        }
                        if ((producto.promedioCalificacion ?: 0.0) > 0) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.Star, null, tint = Color(0xFFFBBF24), modifier = Modifier.size(10.dp))
                                Spacer(Modifier.width(2.dp))
                                Text("${producto.promedioCalificacion}", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF94A3B8))
                            }
                        }
                    }
                    val btnInteraction = remember { MutableInteractionSource() }
                    val btnPressed by btnInteraction.collectIsPressedAsState()
                    val btnScale by animateFloatAsState(if (btnPressed) 0.85f else 1f, spring(stiffness = Spring.StiffnessLow), label = "addBtn")
                    Box(modifier = Modifier.size(36.dp).graphicsLayer { scaleX = btnScale; scaleY = btnScale }.clip(CircleShape).background(Brush.linearGradient(listOf(Color(0xFF22C55E), Color(0xFF16A34A)))).clickable(interactionSource = btnInteraction, indication = null) { tiendaVM.agregarAlCarrito(prodId, 1) }, contentAlignment = Alignment.Center
                    ) { Icon(Icons.Filled.Add, "Agregar", tint = Color.White, modifier = Modifier.size(18.dp)) }
                }
            }
        }
    }
}
@Composable
fun LocationPermissionDialog(
    onDismiss: () -> Unit,
    onEnableLocation: () -> Unit,
    onOpenSettings: () -> Unit
) {
    var isVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { isVisible = true }

    val infiniteTransition = rememberInfiniteTransition(label = "dialog")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.95f, targetValue = 1.05f,
        animationSpec = infiniteRepeatable(tween(2000), RepeatMode.Reverse), label = "pulse"
    )
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.1f, targetValue = 0.2f,
        animationSpec = infiniteRepeatable(tween(2000), RepeatMode.Reverse), label = "pulseAlpha"
    )

    androidx.compose.animation.AnimatedVisibility(
        visible = true,
        enter = fadeIn(tween(300)) + scaleIn(initialScale = 0.9f, animationSpec = tween(400, easing = EaseOutBack))
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.5f))
                .clickable(indication = null, interactionSource = remember { MutableInteractionSource() }) { onDismiss() },
            contentAlignment = Alignment.Center
        ) {
            androidx.compose.animation.AnimatedVisibility(
                visible = isVisible,
                enter = scaleIn(initialScale = 0.8f, animationSpec = tween(500, easing = EaseOutBack)) + 
                        fadeIn(tween(400)) +
                        slideInVertically(initialOffsetY = { it / 4 }, animationSpec = tween(500, easing = EaseOutCubic))
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 32.dp)
                        .clickable(indication = null, interactionSource = remember { MutableInteractionSource() }) {},
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 24.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .clip(CircleShape)
                                .background(
                                    brush = Brush.linearGradient(
                                        colors = listOf(Color(0xFFEFF6FF), Color(0xFFDBEAFE))
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(80.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF2563EB).copy(alpha = pulseAlpha))
                                    .graphicsLayer { scaleX = pulseScale; scaleY = pulseScale }
                            )
                            Icon(
                                Icons.Filled.LocationOn,
                                null,
                                tint = Color(0xFF2563EB),
                                modifier = Modifier.size(36.dp)
                            )
                        }

                        Spacer(Modifier.height(24.dp))

                        Text(
                            "Activa tu ubicacion",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF111827),
                            letterSpacing = (-0.5).sp,
                            textAlign = TextAlign.Center
                        )

                        Spacer(Modifier.height(10.dp))

                        Text(
                            "Para mostrarte productores cerca de ti, necesitamos acceso a tu ubicacion",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF64748B),
                            lineHeight = 20.sp,
                            textAlign = TextAlign.Center
                        )

                        Spacer(Modifier.height(28.dp))

                        Button(
                            onClick = onEnableLocation,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF2563EB)
                            ),
                            elevation = ButtonDefaults.buttonElevation(
                                defaultElevation = 8.dp,
                                pressedElevation = 4.dp
                            )
                        ) {
                            Icon(Icons.Filled.MyLocation, null, tint = Color.White, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(10.dp))
                            Text(
                                "Activar ubicacion",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }

                        Spacer(Modifier.height(12.dp))

                        TextButton(
                            onClick = onOpenSettings,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Filled.Settings, null, tint = Color(0xFF64748B), modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(
                                "Abrir ajustes",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF64748B)
                            )
                        }

                        Spacer(Modifier.height(8.dp))

                        TextButton(
                            onClick = onDismiss,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                "Ahora no",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF94A3B8)
                            )
                        }
                    }
                }
            }
        }
    }
}

fun getCategoryIconAndColor(cat: String): Pair<ImageVector, Color> = when {
    cat.lowercase().contains("verdura") || cat.lowercase().contains("hortaliza") -> Icons.Filled.Eco to Color(0xFF16A34A)
    cat.lowercase().contains("fruta") -> Icons.Filled.LocalDining to Color(0xFFEA580C)
    cat.lowercase().contains("tuberculo") || cat.lowercase().contains("raiz") || cat.lowercase().contains("raíz") -> Icons.Filled.Agriculture to Color(0xFFCA8A04)
    cat.lowercase().contains("lacteo") || cat.lowercase().contains("huevo") -> Icons.Filled.WaterDrop to Color(0xFF0284C7)
    cat.lowercase().contains("grano") || cat.lowercase().contains("cereal") -> Icons.Filled.Grass to Color(0xFF92400E)
    cat.lowercase().contains("cafe") || cat.lowercase().contains("café") || cat.lowercase().contains("cacao") -> Icons.Filled.Coffee to Color(0xFF6D4C41)
    else -> Icons.Filled.LocalOffer to Color(0xFF64748B)
}

private suspend fun getCurrentLocation(context: android.content.Context): Pair<Double, Double>? = withContext(Dispatchers.IO) {
    try {
        val lm = context.getSystemService(android.content.Context.LOCATION_SERVICE) as android.location.LocationManager
        for (p in listOf(android.location.LocationManager.GPS_PROVIDER, android.location.LocationManager.NETWORK_PROVIDER)) {
            @Suppress("MissingPermission") val loc = lm.getLastKnownLocation(p)
            if (loc != null) return@withContext Pair(loc.latitude, loc.longitude)
        }
        null
    } catch (_: Exception) { null }
}