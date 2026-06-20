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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.agroconecta.app.viewmodel.TiendaViewModel

private val Emerald = Color(0xFF0E793D)
private val EmeraldDark = Color(0xFF0A5C2E)
private val EmeraldLight = Color(0xFFE8F5E9)
private val AppBackground = Color(0xFFF8FAF9)
private val Carbon = Color(0xFF111827)
private val Slate400 = Color(0xFF94A3B8)
private val Slate500 = Color(0xFF64748B)
private val Slate200 = Color(0xFFE2E8F0)
private val Slate100 = Color(0xFFF1F5F9)
private val Amber600 = Color(0xFFCA8A04)
private val Amber50 = Color(0xFFFFF7ED)
private val Amber400 = Color(0xFFFBBF24)
private val Blue600 = Color(0xFF2563EB)
private val Blue500 = Color(0xFF3B82F6)
private val Blue50 = Color(0xFFEFF6FF)
private val Green500 = Color(0xFF22C55E)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SipsaExplorerScreen(
    tiendaVM: TiendaViewModel,
    productoInicial: String = "",
    onNavigateBack: () -> Unit,
    onProductoSeleccionado: ((String) -> Unit)? = null
) {
    var busqueda by remember { mutableStateOf(productoInicial) }
    var resultadoBusqueda by remember { mutableStateOf<String?>(null) }
    var precioResultado by remember { mutableStateOf<String?>(null) }
    var isVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { isVisible = true }

    LaunchedEffect(Unit) { tiendaVM.cargarCatalogoSipsa() }

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
                                "Explorador de Precios",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = Carbon,
                                letterSpacing = (-0.5).sp
                            )
                            Text(
                                "Datos oficiales DANE/SIPSA",
                                fontSize = 13.sp,
                                color = Slate500,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.linearGradient(
                                        colors = listOf(Amber400, Amber600)
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Filled.QueryStats, null, tint = Color.White, modifier = Modifier.size(22.dp))
                        }
                    }
                }
            }
        }
    ) { padding ->
        AnimatedVisibility(
            visible = isVisible,
            enter = fadeIn(tween(500, delayMillis = 300)) + slideInVertically(tween(500, delayMillis = 300, easing = EaseOutCubic), initialOffsetY = { it / 6 })
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 20.dp, vertical = 20.dp)
            ) {
                // Search bar
                Surface(
                    color = Color.White,
                    shape = RoundedCornerShape(20.dp),
                    shadowElevation = 4.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
                    ) {
                        Icon(Icons.Filled.Search, null, tint = Slate400, modifier = Modifier.size(22.dp))
                        Spacer(Modifier.width(12.dp))
                        OutlinedTextField(
                            value = busqueda, onValueChange = { busqueda = it },
                            placeholder = { Text("Buscar producto... Ej: Papa, Tomate", color = Slate400, fontSize = 15.sp) },
                            modifier = Modifier.weight(1f).height(56.dp),
                            shape = RoundedCornerShape(16.dp),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color.Transparent,
                                unfocusedBorderColor = Color.Transparent,
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent
                            ),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                            textStyle = LocalTextStyle.current.copy(fontSize = 15.sp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Surface(
                            color = Emerald,
                            shape = RoundedCornerShape(16.dp),
                            shadowElevation = 2.dp,
                            modifier = Modifier
                                .size(48.dp)
                                .clickable {
                                    if (busqueda.isNotBlank()) {
                                        tiendaVM.consultarSipsa(busqueda)
                                        resultadoBusqueda = busqueda
                                    }
                                }
                        ) {
                            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                                Icon(Icons.Filled.Search, null, tint = Color.White, modifier = Modifier.size(22.dp))
                            }
                        }
                    }
                }

                // Search result
                if (tiendaVM.sipsaPrecio != null && resultadoBusqueda != null) {
                    Spacer(Modifier.height(20.dp))
                    AnimatedVisibility(
                        visible = true,
                        enter = fadeIn(tween(400)) + slideInVertically(tween(400, easing = EaseOutCubic), initialOffsetY = { it / 4 })
                    ) {
                        Surface(
                            color = Color.White,
                            shape = RoundedCornerShape(24.dp),
                            shadowElevation = 4.dp,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(24.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(48.dp)
                                            .clip(CircleShape)
                                            .background(
                                                Brush.linearGradient(
                                                    colors = listOf(Amber400, Amber600)
                                                )
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Filled.QueryStats, null, tint = Color.White, modifier = Modifier.size(24.dp))
                                    }
                                    Spacer(Modifier.width(16.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            "Precio de referencia",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = Amber600
                                        )
                                        Text(
                                            "DANE / SIPSA",
                                            fontSize = 11.sp,
                                            color = Slate500
                                        )
                                    }
                                    Surface(
                                        color = Amber50,
                                        shape = RoundedCornerShape(12.dp),
                                        border = BorderStroke(1.dp, Amber600.copy(alpha = 0.2f))
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(Icons.Filled.Info, null, tint = Amber600, modifier = Modifier.size(16.dp))
                                            Spacer(Modifier.width(6.dp))
                                            Text("Oficial", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Amber600)
                                        }
                                    }
                                }

                                Spacer(Modifier.height(16.dp))

                                Surface(
                                    color = Amber50,
                                    shape = RoundedCornerShape(18.dp),
                                    border = BorderStroke(1.5.dp, Amber600.copy(alpha = 0.15f)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(20.dp)) {
                                        Text(
                                            "\"$resultadoBusqueda\"",
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Carbon,
                                            letterSpacing = (-0.3).sp
                                        )
                                        Spacer(Modifier.height(12.dp))
                                        Divider(color = Amber600.copy(alpha = 0.15f), thickness = 1.dp)
                                        Spacer(Modifier.height(12.dp))
                                        Text(
                                            tiendaVM.sipsaPrecio ?: "",
                                            fontSize = 15.sp,
                                            color = Carbon,
                                            lineHeight = 22.sp
                                        )
                                    }
                                }

                                Spacer(Modifier.height(16.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Surface(
                                        color = Blue50,
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(Icons.Filled.Verified, null, tint = Blue500, modifier = Modifier.size(16.dp))
                                            Spacer(Modifier.width(6.dp))
                                            Text("Datos verificados", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Blue600)
                                        }
                                    }
                                    Surface(
                                        color = EmeraldLight,
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(Icons.Filled.Update, null, tint = Emerald, modifier = Modifier.size(16.dp))
                                            Spacer(Modifier.width(6.dp))
                                            Text("Actualizado", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Emerald)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(Modifier.height(24.dp))

                if (tiendaVM.estaCargando && tiendaVM.catalogoSipsa.isEmpty()) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(
                            color = Emerald,
                            strokeWidth = 3.dp,
                            modifier = Modifier.size(48.dp)
                        )
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.linearGradient(
                                        colors = listOf(Emerald, EmeraldDark)
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Filled.ListAlt, null, tint = Color.White, modifier = Modifier.size(20.dp))
                        }
                        Spacer(Modifier.width(14.dp))
                        Column {
                            Text(
                                "Catalogo de productos",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Carbon,
                                letterSpacing = (-0.3).sp
                            )
                            Text(
                                "${tiendaVM.catalogoSipsa.size} productos disponibles",
                                fontSize = 12.sp,
                                color = Slate500
                            )
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(tiendaVM.catalogoSipsa.filter { it.contains(busqueda, ignoreCase = true) }) { prod ->
                            SipsaProductItem(
                                producto = prod,
                                onClick = {
                                    busqueda = prod
                                    tiendaVM.consultarSipsa(prod)
                                    resultadoBusqueda = prod
                                    onProductoSeleccionado?.invoke(prod)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SipsaProductItem(
    producto: String,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        if (isPressed) 0.97f else 1f,
        spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow)
    )

    Surface(
        color = Color.White,
        shape = RoundedCornerShape(20.dp),
        shadowElevation = 2.dp,
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(EmeraldLight, Color.White)
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.LocalOffer, null, tint = Emerald, modifier = Modifier.size(22.dp))
            }
            Spacer(Modifier.width(16.dp))
            Text(
                producto,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                color = Carbon,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
                letterSpacing = (-0.2).sp
            )
            Spacer(Modifier.width(12.dp))
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(Slate100),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowForward, null, tint = Slate400, modifier = Modifier.size(18.dp))
            }
        }
    }
}
