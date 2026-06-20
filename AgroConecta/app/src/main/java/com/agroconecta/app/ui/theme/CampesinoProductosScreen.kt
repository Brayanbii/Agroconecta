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
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import androidx.compose.ui.platform.LocalContext
import com.agroconecta.app.data.model.Producto
import com.agroconecta.app.viewmodel.AuthViewModel
import com.agroconecta.app.viewmodel.TiendaViewModel

private fun buildImageUrl(producto: Producto): Any {
    val url = producto.imagenUrl ?: "default.png"
    return if (url.startsWith("http://") || url.startsWith("https://")) url
    else com.agroconecta.app.data.api.ApiConfig.IMAGES_URL + url
}

private val Emerald = Color(0xFF0E793D)
private val EmeraldDark = Color(0xFF0A5C2E)
private val EmeraldLight = Color(0xFFE8F5E9)
private val AppBackground = Color(0xFFF8FAF9)
private val Carbon = Color(0xFF111827)
private val Slate400 = Color(0xFF94A3B8)
private val Slate500 = Color(0xFF64748B)
private val Slate200 = Color(0xFFE2E8F0)
private val Slate100 = Color(0xFFF1F5F9)
private val Red500 = Color(0xFFEF4444)
private val Amber600 = Color(0xFFCA8A04)
private val Blue500 = Color(0xFF3B82F6)
private val Blue600 = Color(0xFF2563EB)
private val Blue50 = Color(0xFFEFF6FF)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CampesinoProductosScreen(
    authVM: AuthViewModel,
    tiendaVM: TiendaViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToForm: (Long?) -> Unit,
    onNavigateToSipsa: () -> Unit
) {
    LaunchedEffect(Unit) { authVM.cargarMisProductos() }

    val productos = authVM.listaProductosReales
    var eliminarId by remember { mutableStateOf<Long?>(null) }
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
                                "Mis Productos",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = Carbon,
                                letterSpacing = (-0.5).sp
                            )
                            Text(
                                "${productos.size} productos activos",
                                fontSize = 13.sp,
                                color = Slate500,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        IconButton(
                            onClick = { onNavigateToForm(null) },
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.linearGradient(
                                        colors = listOf(Emerald, EmeraldDark)
                                    )
                                )
                        ) {
                            Icon(Icons.Filled.Add, null, tint = Color.White, modifier = Modifier.size(22.dp))
                        }
                        Spacer(Modifier.width(8.dp))
                        IconButton(
                            onClick = onNavigateToSipsa,
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.linearGradient(
                                        colors = listOf(Blue500, Blue600)
                                    )
                                )
                        ) {
                            Icon(Icons.Filled.QueryStats, null, tint = Color.White, modifier = Modifier.size(22.dp))
                        }
                    }
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (authVM.estaCargando) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(
                        color = Emerald,
                        strokeWidth = 3.dp,
                        modifier = Modifier.size(48.dp)
                    )
                }
                return@Column
            }

            AnimatedVisibility(
                visible = isVisible,
                enter = fadeIn(tween(500, delayMillis = 200)) + slideInVertically(tween(500, delayMillis = 200, easing = EaseOutCubic), initialOffsetY = { it / 6 })
            ) {
                Surface(
                    color = Color.White,
                    shape = RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp),
                    shadowElevation = 8.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(320.dp)
                            .background(
                                brush = Brush.linearGradient(
                                    colors = listOf(Emerald, EmeraldDark),
                                    start = Offset(0f, 0f),
                                    end = Offset(1f, 1f)
                                )
                            )
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

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 24.dp, vertical = 24.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(Color.White.copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Filled.TrendingUp,
                                        null,
                                        tint = Color.White,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                                Spacer(Modifier.width(14.dp))
                                Column {
                                    Text(
                                        "Rendimiento de tu negocio",
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        letterSpacing = (-0.3).sp
                                    )
                                    Text(
                                        "Resumen de tu actividad",
                                        fontSize = 13.sp,
                                        color = Color.White.copy(alpha = 0.8f)
                                    )
                                }
                            }

                            Spacer(Modifier.height(24.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                ModernStatCard(
                                    value = "${productos.size}",
                                    label = "Productos",
                                    icon = Icons.Filled.Inventory2,
                                    modifier = Modifier.weight(1f)
                                )
                                ModernStatCard(
                                    value = "0",
                                    label = "Ventas hoy",
                                    icon = Icons.Filled.Today,
                                    modifier = Modifier.weight(1f)
                                )
                                ModernStatCard(
                                    value = "0",
                                    label = "Por enviar",
                                    icon = Icons.Filled.LocalShipping,
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            Spacer(Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                ModernStatCard(
                                    value = "0",
                                    label = "Mes actual",
                                    icon = Icons.Filled.CalendarMonth,
                                    modifier = Modifier.weight(1f)
                                )
                                ModernStatCard(
                                    value = "-",
                                    label = "Mas vendido",
                                    icon = Icons.Filled.Star,
                                    modifier = Modifier.weight(1f)
                                )
                                ModernStatCard(
                                    value = "-",
                                    label = "Rating",
                                    icon = Icons.Filled.Reviews,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
            }

            if (productos.isEmpty()) {
                Box(
                    Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    if (isVisible) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(32.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(120.dp)
                                    .clip(CircleShape)
                                    .background(
                                        Brush.linearGradient(
                                            colors = listOf(EmeraldLight, Color.White)
                                        )
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Filled.Agriculture,
                                    null,
                                    tint = Emerald,
                                    modifier = Modifier.size(60.dp)
                                )
                            }
                            Spacer(Modifier.height(28.dp))
                            Text(
                                "No tienes productos",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = Carbon,
                                letterSpacing = (-0.3).sp
                            )
                            Spacer(Modifier.height(10.dp))
                            Text(
                                "Publica tu primera cosecha y empieza a vender",
                                fontSize = 15.sp,
                                color = Slate500,
                                textAlign = TextAlign.Center,
                                lineHeight = 22.sp
                            )
                            Spacer(Modifier.height(32.dp))
                            Surface(
                                color = Emerald,
                                shape = RoundedCornerShape(20.dp),
                                shadowElevation = 8.dp,
                                modifier = Modifier
                                    .height(60.dp)
                                    .clickable { onNavigateToForm(null) }
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(horizontal = 28.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(Icons.Filled.Add, null, tint = Color.White, modifier = Modifier.size(22.dp))
                                    Spacer(Modifier.width(10.dp))
                                    Text(
                                        "Agregar producto",
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        fontSize = 16.sp
                                    )
                                }
                            }
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(productos, key = { it.id ?: 0L }) { prod ->
                        ProductoCard(
                            producto = prod,
                            onClick = { onNavigateToForm(prod.id) },
                            onEditClick = { onNavigateToForm(prod.id) },
                            onDeleteClick = { eliminarId = prod.id }
                        )
                    }
                }
            }
        }
    }

    if (eliminarId != null) {
        AlertDialog(
            onDismissRequest = { eliminarId = null },
            title = {
                Text(
                    "Eliminar producto",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            },
            text = {
                Text(
                    "Esta accion no se puede deshacer.",
                    fontSize = 15.sp,
                    color = Slate500
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        tiendaVM.eliminarProductoCampesino(eliminarId ?: 0L) {
                            authVM.cargarMisProductos()
                        }
                        eliminarId = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Red500),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.height(48.dp)
                ) {
                    Text("Eliminar", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { eliminarId = null },
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.height(48.dp),
                    border = BorderStroke(1.5.dp, Slate200)
                ) {
                    Text("Cancelar", fontWeight = FontWeight.SemiBold, fontSize = 15.sp, color = Slate500)
                }
            },
            shape = RoundedCornerShape(24.dp),
            containerColor = Color.White
        )
    }
}

@Composable
fun ModernStatCard(
    value: String,
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    Surface(
        color = Color.White.copy(alpha = 0.15f),
        shape = RoundedCornerShape(18.dp),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                icon,
                null,
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
            Spacer(Modifier.height(6.dp))
            Text(
                value,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                label,
                fontSize = 11.sp,
                color = Color.White.copy(alpha = 0.8f),
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun ProductoCard(
    producto: Producto,
    onClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        if (isPressed) 0.97f else 1f,
        spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow)
    )

    Surface(
        color = Color.White,
        shape = RoundedCornerShape(24.dp),
        shadowElevation = 4.dp,
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(EmeraldLight, Color.White)
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (!producto.imagenUrl.isNullOrBlank() && producto.imagenUrl != "default.png") {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(buildImageUrl(producto))
                            .crossfade(true)
                            .build(),
                        contentDescription = producto.nombre,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(20.dp)),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        Icons.Filled.Eco,
                        null,
                        tint = Emerald,
                        modifier = Modifier.size(36.dp)
                    )
                }
            }

            Spacer(Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    producto.nombre,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Carbon,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    letterSpacing = (-0.3).sp
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        color = EmeraldLight,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.height(24.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                producto.categoria ?: "",
                                fontSize = 11.sp,
                                color = Emerald,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            Icons.Filled.Inventory2,
                            null,
                            tint = Slate400,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            "Stock: ${producto.stock}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Emerald
                        )
                    }
                }

                Text(
                    "$${producto.precio.toInt()} / ${producto.unidad ?: "Kg"}",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Carbon,
                    letterSpacing = (-0.3).sp
                )
            }

            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IconButton(
                    onClick = onEditClick,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Slate100)
                ) {
                    Icon(
                        Icons.Filled.Edit,
                        null,
                        tint = Slate500,
                        modifier = Modifier.size(18.dp)
                    )
                }
                IconButton(
                    onClick = onDeleteClick,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFFEE2E2))
                ) {
                    Icon(
                        Icons.Filled.Delete,
                        null,
                        tint = Red500,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}
