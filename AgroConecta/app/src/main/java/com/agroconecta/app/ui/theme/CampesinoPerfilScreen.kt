package com.agroconecta.app.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.animation.core.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Comment
import androidx.compose.material.icons.automirrored.filled.TrendingUp
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.agroconecta.app.data.model.Producto
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
private val Amber400 = Color(0xFFFBBF24)
private val Amber50 = Color(0xFFFFF7ED)
private val Blue600 = Color(0xFF2563EB)
private val Red500 = Color(0xFFEF4444)
private val SurfacePure = Color(0xFFFDFDFC)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CampesinoPerfilScreen(
    campesinoId: Long,
    tiendaVM: TiendaViewModel,
    onNavigateBack: () -> Unit,
    onProductoClick: (Producto) -> Unit
) {
    var categoriaFiltro by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(campesinoId) { tiendaVM.cargarPerfilCampesino(campesinoId) }

    val perfil = tiendaVM.campesinoPerfil
    val productos = tiendaVM.productosDelCampesino
    val categorias = productos.mapNotNull { it.categoria }.distinct().sorted()
    val productosFiltrados = if (categoriaFiltro == null) productos else productos.filter { it.categoria == categoriaFiltro }

    if (tiendaVM.estaCargando && perfil == null) {
        Box(Modifier.fillMaxSize().background(SurfacePure), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                CircularProgressIndicator(color = Brand600, strokeWidth = 3.dp)
                Spacer(Modifier.height(16.dp))
                Text("Cargando perfil...", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Slate400)
            }
        }
        return
    }

    if (perfil == null) {
        Box(Modifier.fillMaxSize().background(SurfacePure), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(32.dp)) {
                Box(modifier = Modifier.size(80.dp).clip(CircleShape).background(Slate100), contentAlignment = Alignment.Center) {
                    Icon(Icons.Filled.ErrorOutline, null, tint = Slate400, modifier = Modifier.size(40.dp))
                }
                Spacer(Modifier.height(20.dp))
                Text("No se pudo cargar el perfil", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = DarkSlate)
                Spacer(Modifier.height(6.dp))
                Text(tiendaVM.errorOperacion ?: "Error desconocido", fontSize = 13.sp, color = Slate400, textAlign = TextAlign.Center)
                Spacer(Modifier.height(24.dp))
                Button(onClick = { tiendaVM.cargarPerfilCampesino(campesinoId) }, shape = RoundedCornerShape(14.dp), colors = ButtonDefaults.buttonColors(containerColor = Brand600)) {
                    Icon(Icons.Filled.Refresh, null, tint = Color.White, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp)); Text("Reintentar", fontWeight = FontWeight.Bold, color = Color.White)
                }
                Spacer(Modifier.height(12.dp))
                TextButton(onClick = onNavigateBack) { Text("Volver a la tienda", color = Brand600, fontWeight = FontWeight.Bold) }
            }
        }
        return
    }

    Scaffold(containerColor = SurfacePure) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState())) {
            // ===== HERO BANNER =====
            Box(modifier = Modifier.fillMaxWidth().height(320.dp)) {
                // Background: farm photo or gradient
                if (!perfil.fotoFincaUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(farmImageUrl(perfil.fotoFincaUrl))
                            .crossfade(true)
                            .build(),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                    // Dark overlay for readability
                    Box(modifier = Modifier.fillMaxSize().background(
                        Brush.verticalGradient(listOf(Color.Black.copy(alpha = 0.3f), Color.Black.copy(alpha = 0.6f), Color.Black.copy(alpha = 0.9f)))
                    ))
                } else {
                    Box(modifier = Modifier.fillMaxSize().background(
                        brush = Brush.verticalGradient(listOf(Brand600, Color(0xFF064E3B), Color(0xFF0C3B2E)))
                    ))
                    Box(modifier = Modifier.fillMaxSize().drawBehind {
                        drawCircle(Color.White.copy(alpha = 0.06f), radius = 250f, center = Offset(size.width * 0.85f, -80f))
                        drawCircle(Color.White.copy(alpha = 0.04f), radius = 180f, center = Offset(size.width * 0.15f, size.height * 0.75f))
                        drawCircle(Color.White.copy(alpha = 0.03f), radius = 120f, center = Offset(size.width * 0.5f, size.height * 0.4f))
                    })
                }

                // Top bar
                Row(modifier = Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = 10.dp, vertical = 8.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                    IconButton(onClick = onNavigateBack, modifier = Modifier.size(42.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.15f))) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.White, modifier = Modifier.size(22.dp))
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Box(modifier = Modifier.size(42.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.15f)), contentAlignment = Alignment.Center) {
                            Icon(Icons.Filled.Share, null, tint = Color.White, modifier = Modifier.size(20.dp))
                        }
                        Box(modifier = Modifier.size(42.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.15f)), contentAlignment = Alignment.Center) {
                            Icon(Icons.Outlined.FavoriteBorder, null, tint = Color.White, modifier = Modifier.size(20.dp))
                        }
                    }
                }

                // Hero content
                Column(modifier = Modifier.align(Alignment.BottomStart).padding(20.dp).fillMaxWidth()) {
                    // "Productor Local" badge
                    Surface(color = Color.White.copy(alpha = 0.15f), shape = RoundedCornerShape(50), modifier = Modifier.padding(bottom = 12.dp)) {
                        Row(modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Agriculture, null, tint = Color.White.copy(alpha = 0.9f), modifier = Modifier.size(14.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Productor Local", fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, color = Color.White, letterSpacing = 1.sp)
                        }
                    }

                    Row(verticalAlignment = Alignment.Bottom, modifier = Modifier.fillMaxWidth()) {
                        // Avatar
                        Box(modifier = Modifier.size(90.dp).clip(RoundedCornerShape(28.dp)).background(Color.White).border(4.dp, Color.White, RoundedCornerShape(28.dp)).shadow(12.dp, RoundedCornerShape(28.dp)), contentAlignment = Alignment.Center) {
                            if (!perfil.fotoPerfil.isNullOrBlank() && perfil.fotoPerfil != "default.png" && perfil.fotoPerfil != "null") {
                                AsyncImage(
                                    model = ImageRequest.Builder(LocalContext.current)
                                        .data(profileImageUrl(perfil.fotoPerfil))
                                        .crossfade(true)
                                        .build(),
                                    contentDescription = null, contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            } else {
                                Box(modifier = Modifier.fillMaxSize().background(Brand50), contentAlignment = Alignment.Center) {
                                    Text(perfil.nombreCompleto?.take(1) ?: "A", fontSize = 32.sp, fontWeight = FontWeight.ExtraBold, color = Brand600)
                                }
                            }
                            // Verified badge
                            if ("VERIFICADO".equals(perfil.estadoVerificacion, ignoreCase = true)) {
                                Box(modifier = Modifier.align(Alignment.BottomEnd).size(28.dp).clip(CircleShape).background(Blue600).border(3.dp, Color.White, CircleShape), contentAlignment = Alignment.Center) {
                                    Icon(Icons.Filled.Check, null, tint = Color.White, modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                        Spacer(Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(perfil.nombreCompleto ?: "", fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = Color.White, letterSpacing = (-0.5).sp)
                            Spacer(Modifier.height(2.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.Store, null, tint = Brand500, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(6.dp))
                                Text(perfil.nombreFinca ?: "Finca Productora", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Brand500)
                            }
                            Spacer(Modifier.height(6.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.LocationOn, null, tint = Color.White.copy(alpha = 0.6f), modifier = Modifier.size(14.dp))
                                Spacer(Modifier.width(4.dp))
                                Text(perfil.municipioOrigen ?: "Colombia", fontSize = 12.sp, color = Color.White.copy(alpha = 0.7f))
                                if ("VERIFICADO".equals(perfil.estadoVerificacion, ignoreCase = true)) {
                                    Text("  |  ", fontSize = 12.sp, color = Color.White.copy(alpha = 0.3f))
                                    Icon(Icons.Filled.Verified, null, tint = Color.White.copy(alpha = 0.7f), modifier = Modifier.size(14.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text("Miembro Verificado", fontSize = 12.sp, color = Color.White.copy(alpha = 0.7f))
                                }
                            }
                        }
                    }
                }
            }

            Column(modifier = Modifier.padding(20.dp)) {
                // ===== STATS (Bento Grid) =====
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    // Calificacion
                    Surface(color = Color.White, shape = RoundedCornerShape(20.dp), border = BorderStroke(1.dp, Slate200), modifier = Modifier.weight(1f)) {
                        Column(modifier = Modifier.padding(14.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(modifier = Modifier.size(44.dp).clip(CircleShape).background(Amber50), contentAlignment = Alignment.Center) {
                                Icon(Icons.Filled.Star, null, tint = Amber400, modifier = Modifier.size(24.dp))
                            }
                            Spacer(Modifier.height(8.dp))
                            Text("${perfil.promedioCalificacion}", fontSize = 28.sp, fontWeight = FontWeight.ExtraBold, color = DarkSlate)
                            Text("Calificacion", fontSize = 9.sp, fontWeight = FontWeight.ExtraBold, color = Slate400, letterSpacing = 1.sp)
                            Spacer(Modifier.height(6.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                                repeat(5) { i -> Icon(Icons.Filled.Star, null, tint = if (i < perfil.promedioCalificacion.toInt()) Amber400 else Slate200, modifier = Modifier.size(12.dp)) }
                            }
                        }
                    }
                    // Entregados
                    Surface(color = Color.White, shape = RoundedCornerShape(20.dp), border = BorderStroke(1.dp, Slate200), modifier = Modifier.weight(1f)) {
                        Column(modifier = Modifier.padding(14.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(modifier = Modifier.size(44.dp).clip(CircleShape).background(Color(0xFFEFF6FF)), contentAlignment = Alignment.Center) {
                                Icon(Icons.Filled.Inventory2, null, tint = Blue600, modifier = Modifier.size(24.dp))
                            }
                            Spacer(Modifier.height(8.dp))
                            Text("${perfil.totalVendidos}", fontSize = 28.sp, fontWeight = FontWeight.ExtraBold, color = DarkSlate)
                            Text("Entregados", fontSize = 9.sp, fontWeight = FontWeight.ExtraBold, color = Slate400, letterSpacing = 1.sp)
                            Spacer(Modifier.height(6.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.AutoMirrored.Filled.TrendingUp, null, tint = Brand600, modifier = Modifier.size(12.dp))
                                Spacer(Modifier.width(3.dp))
                                Text("100% Exito", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Brand600)
                            }
                        }
                    }
                }

                Spacer(Modifier.height(24.dp))

                // ===== NUESTRA HISTORIA =====
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(32.dp).clip(CircleShape).background(Slate100), contentAlignment = Alignment.Center) {
                        Icon(Icons.AutoMirrored.Filled.Comment, null, tint = Slate400, modifier = Modifier.size(18.dp))
                    }
                    Spacer(Modifier.width(10.dp))
                    Text("Nuestra Historia", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = DarkSlate)
                }
                Spacer(Modifier.height(12.dp))
                Surface(color = Slate100, shape = RoundedCornerShape(20.dp), modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text(
                            perfil.descripcionFinca ?: "Este productor local se dedica al cultivo responsable y sostenible. Comprando sus productos apoyas directamente la economia del campo colombiano y recibes insumos frescos sin intermediarios.",
                            fontSize = 14.sp, color = Slate500, lineHeight = 22.sp
                        )
                        Spacer(Modifier.height(14.dp))
                        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Slate200))
                        Spacer(Modifier.height(14.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Column {
                                Text("Productos Activos", fontSize = 9.sp, fontWeight = FontWeight.ExtraBold, color = Slate400, letterSpacing = 1.sp)
                                Text("${perfil.totalProductos}", fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = DarkSlate)
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(color = Brand50, shape = RoundedCornerShape(8.dp)) {
                                    Row(modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp), verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Filled.VerifiedUser, null, tint = Brand600, modifier = Modifier.size(14.dp))
                                        Spacer(Modifier.width(4.dp))
                                        Text("Garantia de frescura", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Brand600)
    }
}
                            }
                        }
                    }
                }

                Spacer(Modifier.height(28.dp))

                // ===== CATALOGO DE PRODUCTOS =====
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) {
                    Column {
                        Text("Catalogo de Productos", fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = DarkSlate)
                        Text("Directo desde su finca, frescura garantizada.", fontSize = 12.sp, color = Slate400)
                    }
                }
                Spacer(Modifier.height(14.dp))

                // Category chips (horizontal scroll)
                if (categorias.isNotEmpty()) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = if (categoriaFiltro == null) Brand600 else Color.White,
                            border = if (categoriaFiltro != null) BorderStroke(1.dp, Slate200) else null,
                            shadowElevation = if (categoriaFiltro == null) 4.dp else 0.dp,
                            modifier = Modifier.clickable { categoriaFiltro = null }
                        ) {
                            Text("Todos", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = if (categoriaFiltro == null) Color.White else Slate500, modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))
                        }
                        categorias.forEach { cat ->
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = if (categoriaFiltro == cat) Brand600 else Color.White,
                                border = if (categoriaFiltro != cat) BorderStroke(1.dp, Slate200) else null,
                                shadowElevation = if (categoriaFiltro == cat) 4.dp else 0.dp,
                                modifier = Modifier.clickable { categoriaFiltro = if (categoriaFiltro == cat) null else cat }
                            ) {
                                Text(cat, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = if (categoriaFiltro == cat) Color.White else Slate500, modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))
                            }
                        }
                    }
                    Spacer(Modifier.height(18.dp))
                }

                // ===== PRODUCT GRID (manual 2-column) =====
                if (productosFiltrados.isEmpty()) {
                    Surface(color = Slate100, shape = RoundedCornerShape(24.dp), modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(48.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(modifier = Modifier.size(72.dp).clip(CircleShape).background(Brand50), contentAlignment = Alignment.Center) {
                                Icon(Icons.Filled.Agriculture, null, tint = Brand600.copy(alpha = 0.5f), modifier = Modifier.size(36.dp))
                            }
                            Spacer(Modifier.height(16.dp))
                            Text("Sin inventario disponible", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = DarkSlate)
                            Spacer(Modifier.height(4.dp))
                            Text("Este productor esta preparando su proxima cosecha.\nVuelve pronto para descubrir nuevos productos frescos.", fontSize = 13.sp, color = Slate400, textAlign = TextAlign.Center, lineHeight = 18.sp)
                        }
                    }
                } else {
                    val rows = productosFiltrados.chunked(2)
                    rows.forEach { row ->
                        Row(modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            row.forEach { producto ->
                                CampesinoProductCard(producto = producto, tiendaVM = tiendaVM, onClick = { onProductoClick(producto) }, modifier = Modifier.weight(1f))
                            }
                            if (row.size == 1) Spacer(Modifier.weight(1f))
                        }
                    }
                }

                Spacer(Modifier.height(40.dp))
            }
        }
    }
}

@Composable
fun CampesinoProductCard(producto: Producto, tiendaVM: TiendaViewModel, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(if (isPressed) 0.96f else 1f, spring(dampingRatio = Spring.DampingRatioMediumBouncy), label = "scale")
    val (catIcon, catColor) = getCategoryIconAndColor(producto.categoria ?: "Verduras")

    Surface(
        modifier = modifier.graphicsLayer { scaleX = scale; scaleY = scale }.clickable(interactionSource = interactionSource, indication = null, onClick = onClick),
        shape = RoundedCornerShape(20.dp), color = Color.White, shadowElevation = 3.dp, border = BorderStroke(1.dp, Slate200.copy(alpha = 0.5f))
    ) {
        Column {
            // Image
            Box(modifier = Modifier.fillMaxWidth().height(130.dp)) {
                AsyncImage(model = ImageRequest.Builder(LocalContext.current).data(imageUrl(producto)).crossfade(true).build(), contentDescription = producto.nombre, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)))
                // Category badge
                Surface(color = catColor.copy(alpha = 0.85f), shape = RoundedCornerShape(bottomEnd = 10.dp, topStart = 20.dp), modifier = Modifier.align(Alignment.TopStart)) {
                    Text(producto.categoria ?: "", fontSize = 9.sp, fontWeight = FontWeight.ExtraBold, color = Color.White, modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp))
                }
                // Favorite
                val prodId = producto.id ?: 0L; val isFav = tiendaVM.esFavorito(prodId)
                com.agroconecta.app.ui.components.FavoriteButtonSmall(
                    isFavorite = isFav,
                    onToggle = { tiendaVM.toggleFavorito(prodId) },
                    modifier = Modifier.align(Alignment.TopEnd).padding(6.dp)
                )
            }
            // Info
            Column(modifier = Modifier.padding(10.dp)) {
                Text(producto.nombre, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = DarkSlate, maxLines = 2, overflow = TextOverflow.Ellipsis, lineHeight = 16.sp)
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Star, null, tint = Amber400, modifier = Modifier.size(13.dp))
                    Spacer(Modifier.width(2.dp))
                    Text(if ((producto.promedioCalificacion ?: 0.0) > 0) "${producto.promedioCalificacion}" else "Nuevo", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = DarkSlate)
                    Text(" | ", fontSize = 10.sp, color = Slate200)
                    Text(if (producto.stock > 0) "${producto.stock} disp." else "Agotado", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = if (producto.stock > 0) Brand600 else Red500)
                }
                Spacer(Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column {
                        Text("Precio", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Slate400, letterSpacing = 0.5.sp)
                        Text("$${producto.precio.toInt()}", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = DarkSlate)
                    }
                    Box(modifier = Modifier.size(36.dp).clip(CircleShape).background(Slate100), contentAlignment = Alignment.Center) {
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, null, tint = Slate500, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }
    }
}

fun farmImageUrl(url: String?): Any {
    if (url.isNullOrBlank()) return ""
    return if (url.startsWith("http://") || url.startsWith("https://")) url
    else com.agroconecta.app.data.api.ApiConfig.IMAGES_URL + url
}

fun profileImageUrl(url: String?): Any {
    if (url.isNullOrBlank()) return ""
    return if (url.startsWith("http://") || url.startsWith("https://")) url
    else com.agroconecta.app.data.api.ApiConfig.IMAGES_URL + url
}
