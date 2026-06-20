package com.agroconecta.app.ui.theme

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
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
private val SurfacePure = Color(0xFFFDFDFC)
private val Amber400 = Color(0xFFFBBF24)
private val Red500 = Color(0xFFEF4444)
private val Blue600 = Color(0xFF2563EB)
private val Emerald = Color(0xFF0E793D)
private val AppBackground = Color(0xFFF9FBF9)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDetailScreen(
    producto: Producto, tiendaVM: TiendaViewModel,
    onNavigateBack: () -> Unit, onNavigateToCampesino: (Long) -> Unit,
    onProductoClick: (Producto) -> Unit
) {
    var cantidad by remember { mutableIntStateOf(1) }
    var mostrarImagen by remember { mutableStateOf(false) }
    var imgIdx by remember { mutableIntStateOf(0) }
    var mostrarFormResena by remember { mutableStateOf(false) }
    var estrellasReview by remember { mutableIntStateOf(5) }
    var comentarioReview by remember { mutableStateOf("") }
    val snackbarHostState = remember { SnackbarHostState() }

    val imagenes = listOfNotNull(producto.imagenUrl, producto.imagenUrl2, producto.imagenUrl3, producto.imagenUrl4)
        .filter { it.isNotBlank() }.map { imgUrl(it) }

    LaunchedEffect(producto.id) {
        producto.id?.let { tiendaVM.cargarResenas(it); tiendaVM.verificarPuedeComentar(it) }
        producto.campesinoId?.let { tiendaVM.cargarProductosDelCampesino(it) }
    }
    LaunchedEffect(tiendaVM.mensajeOperacion) {
        tiendaVM.mensajeOperacion?.let { snackbarHostState.showSnackbar(it, duration = SnackbarDuration.Short); tiendaVM.mensajeOperacion = null }
    }
    LaunchedEffect(tiendaVM.errorOperacion) {
        tiendaVM.errorOperacion?.let { snackbarHostState.showSnackbar(it, duration = SnackbarDuration.Short); tiendaVM.errorOperacion = null }
    }

    val otrosDelCampesino = tiendaVM.productosDelCampesino.filter { it.id != producto.id }

    if (mostrarImagen && imagenes.isNotEmpty()) {
        Dialog(onDismissRequest = { mostrarImagen = false }, properties = DialogProperties(usePlatformDefaultWidth = false)) {
            Box(modifier = Modifier.fillMaxSize().background(Color.Black).clickable { mostrarImagen = false }) {
                AsyncImage(model = ImageRequest.Builder(LocalContext.current).data(imagenes[imgIdx]).crossfade(true).build(),
                    contentDescription = null, contentScale = ContentScale.Fit, modifier = Modifier.fillMaxSize().padding(16.dp))
                IconButton(onClick = { mostrarImagen = false }, modifier = Modifier.align(Alignment.TopEnd).statusBarsPadding().padding(8.dp).size(40.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.2f))) {
                    Icon(Icons.Filled.Close, null, tint = Color.White, modifier = Modifier.size(24.dp))
                }
                if (imagenes.size > 1) {
                    Row(modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        imagenes.forEachIndexed { idx, _ -> Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(if (idx == imgIdx) Color.White else Color.White.copy(alpha = 0.4f))) }
                    }
                }
            }
        }
    }

    Scaffold(
        containerColor = SurfacePure,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            Surface(shadowElevation = 2.dp, color = Color.White) {
                Row(modifier = Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = 8.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onNavigateBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = DarkSlate) }
                    Text("Detalle", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = DarkSlate, modifier = Modifier.weight(1f))
                    IconButton(onClick = { tiendaVM.toggleFavorito(producto.id ?: 0L) }) {
                        Icon(if (tiendaVM.esFavorito(producto.id ?: 0L)) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder, null,
                            tint = if (tiendaVM.esFavorito(producto.id ?: 0L)) Red500 else Slate500)
                    }
                }
            }
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState())) {
            // Image gallery
            Box(modifier = Modifier.fillMaxWidth().height(280.dp).background(Brand50)) {
                if (imagenes.isNotEmpty()) {
                    AsyncImage(model = ImageRequest.Builder(LocalContext.current).data(imagenes[imgIdx]).crossfade(true).build(),
                        contentDescription = producto.nombre, contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize().clickable { mostrarImagen = true })
                    if (imagenes.size > 1) {
                        Row(modifier = Modifier.align(Alignment.BottomCenter).padding(12.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            imagenes.forEachIndexed { idx, _ -> Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(if (idx == imgIdx) Brand600 else Slate200)) }
                        }
                    }
                } else {
                    Icon(Icons.Filled.Eco, null, tint = Brand600.copy(alpha = 0.1f), modifier = Modifier.fillMaxSize().padding(60.dp))
                }
                Box(modifier = Modifier.align(Alignment.TopEnd).padding(8.dp).clip(RoundedCornerShape(8.dp)).background(Color.White.copy(alpha = 0.85f)).padding(horizontal = 8.dp, vertical = 4.dp)) {
                    Icon(Icons.Filled.Fullscreen, null, tint = DarkSlate, modifier = Modifier.size(16.dp))
                }
            }

            Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                Spacer(Modifier.height(16.dp))
                if (producto.categoria != null) {
                    Surface(color = Brand50, shape = RoundedCornerShape(8.dp)) {
                        Text(producto.categoria, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Brand600, modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp))
                    }
                    Spacer(Modifier.height(8.dp))
                }
                Text(producto.nombre, fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = DarkSlate, letterSpacing = (-0.5).sp)
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    repeat(5) { i -> Icon(if (i < ((producto.promedioCalificacion ?: 0.0).toInt())) Icons.Filled.Star else Icons.Outlined.Star, null, tint = Amber400, modifier = Modifier.size(18.dp)) }
                    Spacer(Modifier.width(6.dp))
                    Text(if ((producto.promedioCalificacion ?: 0.0) > 0) "${producto.promedioCalificacion}" else "Nuevo", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = DarkSlate)
                    Text(" (${producto.totalResenas ?: 0} resenas)", fontSize = 13.sp, color = Slate400)
                }
                Spacer(Modifier.height(16.dp))

                // Bento Grid
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    BentoItem("Productor", producto.nombreCampesino ?: "AgroConecta", Icons.Filled.Person, Brand600, Modifier.weight(1f))
                    BentoItem("Origen", aproxLoc(producto.municipioOrigen).ifBlank { "Colombia" }, Icons.Filled.LocationOn, Blue600, Modifier.weight(1f))
                    BentoItem("Cosecha", tiendaVM.calcularCosechadoHace(producto.fechaCreacion).ifBlank { "Reciente" }, Icons.Filled.Eco, Brand600, Modifier.weight(1f))
                    BentoItem("Cultivo", producto.categoria ?: "Verduras", Icons.Filled.Agriculture, Color(0xFFCA8A04), Modifier.weight(1f))
                }

                Spacer(Modifier.height(20.dp))

                val uni = producto.unidad ?: "Kg"
                val totalPrecio = producto.precio * cantidad

                // Price Card
                Surface(color = Slate100, shape = RoundedCornerShape(18.dp), modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text("$${totalPrecio.toInt()}", fontSize = 32.sp, fontWeight = FontWeight.ExtraBold, color = DarkSlate)
                            Spacer(Modifier.width(6.dp))
                            Text("/ ${if (cantidad > 1) "$cantidad $uni" else "$cantidad $uni"}", fontSize = 14.sp, color = Slate400, modifier = Modifier.padding(bottom = 5.dp))
                        }
                        Text(if (producto.stock > 0) "${producto.stock} ${uni} disponibles" else "Agotado", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = if (producto.stock > 0) Brand600 else Red500)
                        Spacer(Modifier.height(12.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Cantidad:", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Slate700); Spacer(modifier = Modifier.weight(1f))
                            IconButton(onClick = { if (cantidad > 1) cantidad-- }, modifier = Modifier.size(34.dp).clip(CircleShape).background(Color.White).border(1.dp, Slate200, CircleShape)) { Icon(Icons.Filled.Remove, null, tint = DarkSlate, modifier = Modifier.size(18.dp)) }
                            Text("$cantidad", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = DarkSlate, modifier = Modifier.padding(horizontal = 16.dp))
                            IconButton(onClick = { if (cantidad < producto.stock) cantidad++ }, modifier = Modifier.size(34.dp).clip(CircleShape).background(Color.White).border(1.dp, Slate200, CircleShape)) { Icon(Icons.Filled.Add, null, tint = DarkSlate, modifier = Modifier.size(18.dp)) }
                        }
                        Spacer(Modifier.height(12.dp))
                        Button(onClick = { tiendaVM.agregarAlCarrito(producto.id ?: 0L, cantidad) }, enabled = producto.stock > 0,
                            shape = RoundedCornerShape(14.dp), colors = ButtonDefaults.buttonColors(containerColor = Brand600),
                            contentPadding = PaddingValues(vertical = 14.dp), modifier = Modifier.fillMaxWidth().height(52.dp).shadow(10.dp, RoundedCornerShape(14.dp), spotColor = Brand600.copy(alpha = 0.25f))
                        ) { Icon(Icons.Filled.AddShoppingCart, null, tint = Color.White, modifier = Modifier.size(18.dp)); Spacer(Modifier.width(8.dp)); Text("Agregar al carrito", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White) }
                    }
                }

                // Description
                if (!producto.descripcion.isNullOrBlank()) { Spacer(Modifier.height(16.dp)); Text("Descripcion", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = DarkSlate); Spacer(Modifier.height(4.dp)); Text(producto.descripcion, fontSize = 14.sp, color = Slate500, lineHeight = 22.sp) }

                Spacer(Modifier.height(20.dp))

                // Campesino Card
                Text("Campesino aliado", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = DarkSlate)
                Spacer(Modifier.height(8.dp))
                Surface(color = Brand50.copy(alpha = 0.5f), shape = RoundedCornerShape(20.dp), border = BorderStroke(1.dp, Brand500.copy(alpha = 0.2f)),
                    modifier = Modifier.fillMaxWidth().clickable { producto.campesinoId?.let { onNavigateToCampesino(it) } }) {
                    Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(52.dp).clip(CircleShape).background(Brand600.copy(alpha = 0.1f)), contentAlignment = Alignment.Center) { Icon(Icons.Filled.Person, null, tint = Brand600, modifier = Modifier.size(30.dp)) }
                        Spacer(Modifier.width(14.dp)); Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) { Text(producto.nombreCampesino ?: "AgroConecta", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = DarkSlate); if (producto.campesinoVerificado) { Spacer(Modifier.width(4.dp)); Icon(Icons.Filled.Verified, null, tint = Blue600, modifier = Modifier.size(18.dp)) } }
                            if (!producto.nombreFinca.isNullOrBlank()) Text("Finca ${producto.nombreFinca}", fontSize = 13.sp, color = Slate700, fontWeight = FontWeight.SemiBold)
                            if (!producto.descripcionFinca.isNullOrBlank()) Text(producto.descripcionFinca, fontSize = 11.sp, color = Slate400, maxLines = 2, overflow = TextOverflow.Ellipsis)
                            Text("${aproxLoc(producto.municipioOrigen)}, Colombia", fontSize = 11.sp, color = Slate400)
                        }
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, null, tint = Slate400, modifier = Modifier.size(20.dp))
                    }
                }

                // Mas de este campesino
                if (otrosDelCampesino.isNotEmpty()) {
                    Spacer(Modifier.height(24.dp)); Text("Mas de este campesino", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = DarkSlate); Spacer(Modifier.height(10.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(otrosDelCampesino) { prod ->
                            Card(modifier = Modifier.width(140.dp).clickable { onProductoClick(prod) }, shape = RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
                                Column {
                                    AsyncImage(model = ImageRequest.Builder(LocalContext.current).data(imgUrl(prod.imagenUrl ?: "")).crossfade(true).build(), contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxWidth().height(100.dp).clip(RoundedCornerShape(topStart = 14.dp, topEnd = 14.dp)))
                                    Column(modifier = Modifier.padding(8.dp)) { Text(prod.nombre, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = DarkSlate, maxLines = 2, overflow = TextOverflow.Ellipsis); Text("$${prod.precio.toInt()} / ${prod.unidad ?: "Kg"}", fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, color = Brand600) }
                                }
                            }
                        }
                    }
                }

                // Reviews
                Spacer(Modifier.height(24.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Resenas (${tiendaVM.resenasProductoSeleccionado.size})", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = DarkSlate)
                    TextButton(onClick = { mostrarFormResena = !mostrarFormResena }) { Icon(if (mostrarFormResena) Icons.Filled.Close else Icons.Filled.Add, null, tint = if (tiendaVM.puedeComentar) Brand600 else Slate400, modifier = Modifier.size(16.dp)); Spacer(Modifier.width(4.dp)); Text(if (mostrarFormResena) "Cancelar" else "Escribir resena", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = if (tiendaVM.puedeComentar) Brand600 else Slate400) }
                }
                if (mostrarFormResena && !tiendaVM.puedeComentar) {
                    Surface(color = Color(0xFFFFF7ED), shape = RoundedCornerShape(12.dp), border = BorderStroke(1.dp, Color(0xFFF59E0B).copy(alpha = 0.3f)), modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
                        Row(modifier = Modifier.padding(12.dp)) { Icon(Icons.Filled.Info, null, tint = Color(0xFFF59E0B), modifier = Modifier.size(20.dp)); Spacer(Modifier.width(10.dp)); Text(tiendaVM.mensajeReview ?: "Compra este producto para calificarlo", fontSize = 13.sp, color = DarkSlate) }
                    }
                }
                AnimatedVisibility(visible = mostrarFormResena && tiendaVM.puedeComentar, enter = expandVertically(tween(300)) + fadeIn(tween(300)), exit = shrinkVertically(tween(200)) + fadeOut(tween(200))) {
                    Surface(color = Slate100, shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) { Text("Puntaje:", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = DarkSlate); Spacer(Modifier.width(8.dp)); repeat(5) { star -> IconButton(onClick = { estrellasReview = star + 1 }, modifier = Modifier.size(28.dp)) { Icon(if (star < estrellasReview) Icons.Filled.Star else Icons.Outlined.Star, null, tint = if (star < estrellasReview) Amber400 else Slate400, modifier = Modifier.size(20.dp)) } } }
                            OutlinedTextField(value = comentarioReview, onValueChange = { comentarioReview = it }, placeholder = { Text("Tu comentario (opcional)", color = Slate400, fontSize = 13.sp) }, shape = RoundedCornerShape(12.dp), minLines = 2, modifier = Modifier.fillMaxWidth(), colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Brand600, unfocusedBorderColor = Slate200))
                            Spacer(Modifier.height(8.dp)); Button(onClick = { producto.id?.let { tiendaVM.guardarResena(it, estrellasReview, comentarioReview.ifBlank { null }) }; comentarioReview = ""; mostrarFormResena = false }, shape = RoundedCornerShape(10.dp), colors = ButtonDefaults.buttonColors(containerColor = Brand600), modifier = Modifier.align(Alignment.End)) { Text("Publicar", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White) }
                        }
                    }
                }
                if (tiendaVM.resenasProductoSeleccionado.isEmpty()) {
                    Surface(color = Slate100, shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) { Text("No hay resenas aun. Se el primero en opinar.", fontSize = 13.sp, color = Slate400, modifier = Modifier.padding(20.dp), textAlign = TextAlign.Center) }
                } else {
                    tiendaVM.resenasProductoSeleccionado.forEach { resena ->
                        Surface(color = Color.White, shape = RoundedCornerShape(14.dp), border = BorderStroke(1.dp, Slate200), modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text(resena.nombreAutor ?: "Anonimo", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = DarkSlate); Row { repeat(resena.estrellas ?: 0) { Icon(Icons.Filled.Star, null, tint = Amber400, modifier = Modifier.size(14.dp)) } } }
                                if (!resena.comentario.isNullOrBlank()) { Spacer(Modifier.height(4.dp)); Text(resena.comentario, fontSize = 12.sp, color = Slate500, lineHeight = 18.sp) }
                                resena.fecha?.let { Spacer(Modifier.height(4.dp)); Text(it, fontSize = 10.sp, color = Slate400) }
                            }
                        }
                    }
                }
                Spacer(Modifier.height(40.dp))
            }
        }
    }
}

@Composable fun BentoItem(label: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector, color: Color, modifier: Modifier = Modifier) {
    Surface(color = Slate100, shape = RoundedCornerShape(14.dp), modifier = modifier) {
        Column(modifier = Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icon, null, tint = color, modifier = Modifier.size(20.dp)); Spacer(Modifier.height(4.dp))
            Text(label, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Slate400, letterSpacing = 0.5.sp)
            Text(value, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = DarkSlate, maxLines = 2, overflow = TextOverflow.Ellipsis, textAlign = TextAlign.Center)
        }
    }
}

fun aproxLoc(municipio: String?): String = municipio?.split(",")?.firstOrNull()?.trim() ?: ""

fun imgUrl(url: String): Any = if (url.startsWith("http://") || url.startsWith("https://")) url else com.agroconecta.app.data.api.ApiConfig.IMAGES_URL + url
