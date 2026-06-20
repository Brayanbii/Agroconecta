package com.agroconecta.app.ui.theme

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
private val Red500 = Color(0xFFEF4444)
private val Red50 = Color(0xFFFEF2F2)
private val Orange500 = Color(0xFFF97316)
private val SurfacePure = Color(0xFFF8FAFC)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritosScreen(
    tiendaVM: TiendaViewModel,
    onProductoClick: (Producto) -> Unit,
    onNavigateBack: () -> Unit
) {
    val favoritos = tiendaVM.listaFavoritos
    var isVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        tiendaVM.cargarFavoritos()
        isVisible = true
    }

    Scaffold(
        containerColor = SurfacePure,
        topBar = {
            Surface(shadowElevation = 0.dp, color = Color.White) {
                Row(
                    modifier = Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = 8.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver", tint = DarkSlate)
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Mis Favoritos", fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = DarkSlate, letterSpacing = (-0.5).sp)
                        if (favoritos.isNotEmpty()) {
                            Text("${favoritos.size} producto${if (favoritos.size != 1) "s" else ""} guardado${if (favoritos.size != 1) "s" else ""}", fontSize = 12.sp, color = Slate400, fontWeight = FontWeight.Medium)
                        }
                    }
                }
            }
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            if (tiendaVM.estaCargando && favoritos.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Brand600)
                }
                return@Column
            }

            if (favoritos.isEmpty()) {
                // Premium Empty State
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(32.dp)) {
                        
                        val infiniteTransition = rememberInfiniteTransition(label = "empty")
                        val pulseScale by infiniteTransition.animateFloat(
                            initialValue = 0.95f, targetValue = 1.05f,
                            animationSpec = infiniteRepeatable(tween(2000), RepeatMode.Reverse), label = "pulse"
                        )
                        val pulseAlpha by infiniteTransition.animateFloat(
                            initialValue = 0.1f, targetValue = 0.2f,
                            animationSpec = infiniteRepeatable(tween(2000), RepeatMode.Reverse), label = "pulseAlpha"
                        )

                        Box(
                            modifier = Modifier.size(120.dp).clip(CircleShape)
                                .background(Brush.linearGradient(listOf(Red50, Color(0xFFFEE2E2)))),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier.size(120.dp).clip(CircleShape)
                                    .background(Red500.copy(alpha = pulseAlpha))
                                    .graphicsLayer { scaleX = pulseScale; scaleY = pulseScale }
                            )
                            Icon(Icons.Filled.FavoriteBorder, null, tint = Red500, modifier = Modifier.size(56.dp))
                        }
                        
                        Spacer(Modifier.height(32.dp))
                        
                        Text("Aun no tienes favoritos", fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = DarkSlate, letterSpacing = (-0.5).sp)
                        Spacer(Modifier.height(12.dp))
                        Text("Guarda tus productos preferidos tocando el\ncorazon en la tienda.", fontSize = 15.sp,
                            color = Slate500, textAlign = TextAlign.Center, lineHeight = 22.sp)
                        Spacer(Modifier.height(32.dp))
                        
                        Button(
                            onClick = onNavigateBack,
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Brand600),
                            modifier = Modifier.height(52.dp).padding(horizontal = 24.dp),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp)
                        ) {
                            Icon(Icons.Filled.Store, null, tint = Color.White, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(10.dp))
                            Text("Ir a la tienda", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        }
                    }
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(items = favoritos, key = { it.id ?: 0L }) { producto ->
                        FavoritoCardPremium(
                            producto = producto,
                            tiendaVM = tiendaVM,
                            onClick = { onProductoClick(producto) },
                            isVisible = isVisible
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun FavoritoCardPremium(
    producto: Producto,
    tiendaVM: TiendaViewModel,
    onClick: () -> Unit,
    isVisible: Boolean
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        if (isPressed) 0.95f else 1f,
        spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "scale"
    )
    val prodId = producto.id ?: 0L
    val isFav = tiendaVM.esFavorito(prodId)

    androidx.compose.animation.AnimatedVisibility(
        visible = isVisible,
        enter = scaleIn(initialScale = 0.9f, animationSpec = tween(500, easing = EaseOutCubic)) + 
                fadeIn(animationSpec = tween(400))
    ) {
        Card(
            modifier = Modifier
                .graphicsLayer { scaleX = scale; scaleY = scale }
                .clickable(interactionSource = interactionSource, indication = null, onClick = onClick),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(modifier = Modifier.border(1.dp, Color(0xFFF1F5F9), RoundedCornerShape(24.dp))) {
                // Imagen con gradiente
                Box(modifier = Modifier.fillMaxWidth().height(160.dp)) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(imageUrl(producto)).crossfade(true).build(),
                        contentDescription = producto.nombre,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                            .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                    )
                    Box(
                        modifier = Modifier.fillMaxSize()
                            .background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = 0.25f))))
                            .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                    )

                    // Badge de categoría
                    Surface(
                        color = Color.White.copy(alpha = 0.95f),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.align(Alignment.TopStart).padding(12.dp)
                    ) {
                        Text(
                            producto.categoria ?: "",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Brand600,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            letterSpacing = 0.3.sp
                        )
                    }

                    // Botón favorito (corazón rojo)
                    com.agroconecta.app.ui.components.FavoriteButton(
                        isFavorite = true,
                        onToggle = { tiendaVM.toggleFavorito(prodId) },
                        modifier = Modifier.align(Alignment.TopEnd).padding(12.dp),
                        size = 36.dp,
                        iconSize = 18.dp,
                        shadowElevation = 6.dp
                    )

                    // Rating
                    if ((producto.promedioCalificacion ?: 0.0) > 0) {
                        Surface(
                            color = Color.Black.copy(alpha = 0.75f),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.align(Alignment.BottomStart).padding(12.dp)
                        ) {
                            Row(modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.Star, null, tint = Amber400, modifier = Modifier.size(13.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("${producto.promedioCalificacion}", fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
                            }
                        }
                    }
                }

                // Info
                Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 14.dp)) {
                    // Vendedor
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier.size(20.dp).clip(CircleShape).background(Brand50),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Filled.Person, null, tint = Brand600, modifier = Modifier.size(11.dp))
                        }
                        Spacer(Modifier.width(6.dp))
                        Text(
                            producto.nombreCampesino ?: "Productor",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Slate500,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Icon(Icons.Filled.Verified, null, tint = Color(0xFF3B82F6), modifier = Modifier.size(11.dp).padding(start = 3.dp))
                    }

                    Spacer(Modifier.height(8.dp))

                    // Nombre
                    Text(
                        producto.nombre,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = DarkSlate,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        lineHeight = 20.sp,
                        letterSpacing = (-0.3).sp
                    )

                    Spacer(Modifier.height(6.dp))

                    // Ubicación
                    if (!producto.municipioOrigen.isNullOrBlank()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.LocationOn, null, tint = Red500, modifier = Modifier.size(11.dp))
                            Spacer(Modifier.width(3.dp))
                            Text(
                                producto.municipioOrigen,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Slate400,
                                letterSpacing = 0.3.sp
                            )
                        }
                        Spacer(Modifier.height(6.dp))
                    }

                    // Stock
                    if (producto.stock > 0) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Brand50)
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                "${producto.stock} ${producto.unidad ?: "Kg"} disp.",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Brand600
                            )
                        }
                        Spacer(Modifier.height(8.dp))
                    }

                    // Precio y botón
                    Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                        Column {
                            Text("Precio", fontSize = 10.sp, color = Slate400, fontWeight = FontWeight.Bold, letterSpacing = 0.3.sp)
                            Spacer(Modifier.height(2.dp))
                            Row(verticalAlignment = Alignment.Bottom) {
                                Text(
                                    "$${producto.precio.toInt()}",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Black,
                                    color = DarkSlate,
                                    letterSpacing = (-0.5).sp
                                )
                                Spacer(Modifier.width(4.dp))
                                Text("/ ${producto.unidad ?: "Kg"}", fontSize = 11.sp, color = Slate400, fontWeight = FontWeight.Medium)
                            }
                        }

                        // Botón agregar
                        Surface(
                            color = Brand600,
                            shape = CircleShape,
                            shadowElevation = 8.dp,
                            modifier = Modifier.size(42.dp).clickable { tiendaVM.agregarAlCarrito(prodId, 1) }
                        ) {
                            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                                Icon(Icons.Filled.Add, "Agregar", tint = Color.White, modifier = Modifier.size(20.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}
