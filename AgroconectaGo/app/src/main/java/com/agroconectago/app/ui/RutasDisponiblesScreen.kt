package com.agroconectago.app.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.agroconectago.app.data.LocationTracker
import com.agroconectago.app.data.api.DeliveryRetrofitClient
import com.agroconectago.app.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun RutasDisponiblesScreen(
    onBack: () -> Unit,
    onRutaSeleccionada: (Long) -> Unit
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    var rutas by remember { mutableStateOf<List<Map<String, Any?>>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var isVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { delay(100); isVisible = true }

    val locationTracker = remember { LocationTracker(context) }

    fun cargarRutas() {
        scope.launch {
            loading = true
            try {
                val loc = locationTracker.getCurrentLocation()
                val resp = DeliveryRetrofitClient.api.rutasDisponibles(
                    lat = loc?.first,
                    lng = loc?.second,
                    radioKm = 200.0
                )
                val data = (resp["rutas"] as? List<*>)?.filterIsInstance<Map<String, Any?>>() ?: emptyList()
                rutas = data
            } catch (_: Exception) {}
            loading = false
        }
    }

    LaunchedEffect(Unit) { cargarRutas() }

    val infiniteTransition = rememberInfiniteTransition(label = "rutas")
    val orb1X by infiniteTransition.animateFloat(
        0.75f, 0.9f,
        infiniteRepeatable(tween(7000, easing = EaseInOutCubic), RepeatMode.Reverse),
        label = "orb1X"
    )
    val orb1Y by infiniteTransition.animateFloat(
        0.08f, 0.2f,
        infiniteRepeatable(tween(8000, easing = EaseInOutCubic), RepeatMode.Reverse),
        label = "orb1Y"
    )
    val orb2X by infiniteTransition.animateFloat(
        0.08f, 0.2f,
        infiniteRepeatable(tween(9000, easing = EaseInOutCubic), RepeatMode.Reverse),
        label = "orb2X"
    )
    val orb2Y by infiniteTransition.animateFloat(
        0.75f, 0.9f,
        infiniteRepeatable(tween(10000, easing = EaseInOutCubic), RepeatMode.Reverse),
        label = "orb2Y"
    )
    val orbAlpha by infiniteTransition.animateFloat(
        0.03f, 0.07f,
        infiniteRepeatable(tween(5000), RepeatMode.Reverse),
        label = "orbAlpha"
    )
    val orbAccentAlpha by infiniteTransition.animateFloat(
        0.01f, 0.04f,
        infiniteRepeatable(tween(6000), RepeatMode.Reverse),
        label = "orbAccentAlpha"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFFAFBFA),
                        Color(0xFFF5F7F5),
                        Color(0xFFF0F2F0)
                    )
                )
            )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .drawBehind {
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(DeliveryBrand600.copy(alpha = orbAlpha), Color.Transparent)
                        ),
                        radius = size.width * 0.55f,
                        center = Offset(size.width * orb1X, size.height * orb1Y)
                    )
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(DeliveryAccent600.copy(alpha = orbAccentAlpha), Color.Transparent)
                        ),
                        radius = size.width * 0.4f,
                        center = Offset(size.width * orb2X, size.height * orb2Y)
                    )
                }
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            // ═══════════════════════════════════════════════════════
            // HEADER - STAGGERED
            // ═══════════════════════════════════════════════════════
            AnimatedVisibility(
                visible = isVisible,
                enter = fadeIn(tween(600)) + slideInVertically(tween(600), initialOffsetY = { -20 })
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color.White.copy(alpha = 0.8f))
                            .border(1.dp, Color(0xFFE8ECEF).copy(alpha = 0.5f), RoundedCornerShape(14.dp))
                            .shadow(6.dp, RoundedCornerShape(14.dp), spotColor = Color(0x06000000))
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = null,
                            tint = Slate600,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "Rutas Disponibles",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 22.sp,
                            color = Slate900,
                            letterSpacing = (-0.4).sp
                        )
                        if (!loading && rutas.isNotEmpty()) {
                            Text(
                                "${rutas.size} pedidos listos para entregar",
                                fontSize = 12.sp,
                                color = Slate400,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                    IconButton(
                        onClick = { cargarRutas() },
                        modifier = Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(DeliveryBrand50)
                            .border(1.dp, DeliveryBrand400.copy(alpha = 0.3f), RoundedCornerShape(14.dp))
                    ) {
                        Icon(
                            Icons.Filled.Refresh,
                            contentDescription = null,
                            tint = DeliveryBrand600,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            // ═══════════════════════════════════════════════════════
            // STATS BAR - PREMIUM PILLS
            // ═══════════════════════════════════════════════════════
            AnimatedVisibility(
                visible = isVisible && !loading && rutas.isNotEmpty(),
                enter = fadeIn(tween(600, delayMillis = 150)) +
                        slideInVertically(tween(600, delayMillis = 150), initialOffsetY = { -10 })
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val totalPago = rutas.sumOf {
                        (it["pagoTotalEstimado"] as? Number)?.toDouble() ?: 0.0
                    }
                    val totalPeso = rutas.sumOf {
                        (it["pesoTotalKg"] as? Number)?.toDouble() ?: 0.0
                    }
                    StatsPill(
                        icon = Icons.Filled.Inventory2,
                        label = "${rutas.size} rutas",
                        color = DeliveryBrand600
                    )
                    StatsPill(
                        icon = Icons.Filled.Scale,
                        label = "${String.format("%.0f", totalPeso)} kg",
                        color = DeliveryAccent600
                    )
                    StatsPill(
                        icon = Icons.Filled.Payments,
                        label = "$${String.format("%.0f", totalPago)}",
                        color = Color(0xFFF59E0B)
                    )
                }
            }

            // ═══════════════════════════════════════════════════════
            // CONTENT AREA
            // ═══════════════════════════════════════════════════════
            if (loading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(
                            color = DeliveryBrand600,
                            modifier = Modifier.size(40.dp),
                            strokeWidth = 3.dp
                        )
                        Spacer(Modifier.height(16.dp))
                        Text(
                            "Buscando rutas cercanas...",
                            fontSize = 14.sp,
                            color = Slate400,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            } else if (rutas.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .clip(RoundedCornerShape(32.dp))
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(Slate100, Color(0xFFF1F5F9))
                                )
                            )
                            .border(2.dp, Color(0xFFE2E8F0), RoundedCornerShape(32.dp))
                            .shadow(12.dp, RoundedCornerShape(32.dp), spotColor = Color(0x06000000)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Outlined.Route,
                            contentDescription = null,
                            tint = Slate300,
                            modifier = Modifier.size(48.dp)
                        )
                    }
                    Spacer(Modifier.height(20.dp))
                    Text(
                        "No hay rutas disponibles",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 18.sp,
                        color = Slate500,
                        letterSpacing = (-0.2).sp
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Las rutas apareceran cuando haya\npedidos listos para despachar.",
                        fontSize = 13.sp,
                        color = Slate400,
                        textAlign = TextAlign.Center,
                        lineHeight = 18.sp
                    )
                    Spacer(Modifier.height(20.dp))
                    OutlinedButton(
                        onClick = { cargarRutas() },
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = DeliveryBrand600),
                        border = androidx.compose.foundation.BorderStroke(1.2.dp, DeliveryBrand400.copy(alpha = 0.3f)),
                        modifier = Modifier.height(44.dp)
                    ) {
                        Icon(Icons.Filled.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Reintentar", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 24.dp)
                ) {
                    items(rutas, key = { it["id"]?.hashCode() ?: it.hashCode() }) { ruta ->
                        val index = rutas.indexOf(ruta)
                        RutaCardPremium(
                            ruta = ruta,
                            animDelay = index * 80,
                            onSelect = {
                                onRutaSeleccionada(
                                    (ruta["id"] as? Double)?.toLong() ?: 0
                                )
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StatsPill(
    icon: ImageVector,
    label: String,
    color: Color
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(color.copy(alpha = 0.08f))
            .border(1.dp, color.copy(alpha = 0.15f), RoundedCornerShape(10.dp))
            .padding(horizontal = 10.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(14.dp))
        Spacer(Modifier.width(5.dp))
        Text(
            label,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}

@Composable
private fun RutaCardPremium(
    ruta: Map<String, Any?>,
    animDelay: Int,
    onSelect: () -> Unit
) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { delay(animDelay.toLong()); visible = true }

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "rutaScale"
    )

    val dist = (ruta["distanciaKm"] as? Number)?.toDouble() ?: 0.0
    val pago = (ruta["pagoTotalEstimado"] as? Number)?.toDouble() ?: 0.0
    val tipoVeh = ruta["tipoVehiculoRequerido"]?.toString() ?: "MOTO"
    val pedidos = ruta["pedidosCount"]?.toString() ?: "0"
    val peso = ruta["pesoTotalKg"]?.toString() ?: "0"
    val capacidad = ruta["capacidadMaximaKg"]?.toString() ?: "50"

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(500, easing = EaseOutCubic)) +
                slideInVertically(tween(500, easing = EaseOutCubic), initialOffsetY = { 20 })
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .graphicsLayer { scaleX = scale; scaleY = scale }
                .clickable(
                    interactionSource = interactionSource,
                    indication = null
                ) { onSelect() },
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            border = androidx.compose.foundation.BorderStroke(
                0.5.dp,
                Color(0xFFE8ECEF).copy(alpha = 0.5f)
            )
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(
                                Brush.linearGradient(
                                    colors = when (tipoVeh) {
                                        "CAMION" -> listOf(Color(0xFFFEF3C7), Color(0xFFFDE68A))
                                        "AUTOMOVIL" -> listOf(Color(0xFFDBEAFE), Color(0xFFBFDBFE))
                                        else -> listOf(DeliveryBrand50, Color(0xFFCCFBF1))
                                    }
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            when (tipoVeh) {
                                "CAMION" -> "\uD83D\uDE9B"
                                "AUTOMOVIL" -> "\uD83D\uDE97"
                                else -> "\uD83C\uDFCD\uFE0F"
                            },
                            fontSize = 20.sp
                        )
                    }
                    Spacer(Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                ruta["codigoRuta"]?.toString() ?: "",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 16.sp,
                                color = Slate900,
                                letterSpacing = (-0.2).sp
                            )
                            if (dist > 0) {
                                Spacer(Modifier.width(10.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(Slate100)
                                        .padding(horizontal = 7.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        "${String.format("%.0f", dist)} km",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Slate500
                                    )
                                }
                            }
                        }
                        Spacer(Modifier.height(3.dp))
                        Text(
                            "${ruta["zonaOrigen"]?.toString() ?: ""} \u2192 ${ruta["zonaDestino"]?.toString() ?: ""}",
                            fontSize = 12.sp,
                            color = Slate400,
                            maxLines = 1
                        )
                    }
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = DeliveryBrand400,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    InfoTagPremium(
                        icon = Icons.Filled.Inventory2,
                        text = "$pedidos pedidos",
                        color = DeliveryBrand600
                    )
                    InfoTagPremium(
                        icon = Icons.Filled.Scale,
                        text = "$peso / $capacidad kg",
                        color = DeliveryAccent600
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Filled.Payments,
                            contentDescription = null,
                            tint = Color(0xFFF59E0B),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            "$${String.format("%.0f", pago)}",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFFEAB308),
                            letterSpacing = (-0.2).sp
                        )
                    }
                }

                Spacer(Modifier.height(8.dp))

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(7.dp))
                        .background(
                            when (tipoVeh) {
                                "CAMION" -> Color(0xFFFEF3C7)
                                "AUTOMOVIL" -> Color(0xFFDBEAFE)
                                else -> Color(0xFFF0FDF4)
                            }
                        )
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Filled.Speed,
                            contentDescription = null,
                            tint = Slate500,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            when (tipoVeh) {
                                "CAMION" -> "Camion requerido"
                                "AUTOMOVIL" -> "Automovil requerido"
                                else -> "Moto requerida"
                            },
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Slate600
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun InfoTagPremium(
    icon: ImageVector,
    text: String,
    color: Color
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(color.copy(alpha = 0.06f))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(14.dp))
        Spacer(Modifier.width(5.dp))
        Text(
            text,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = color.copy(alpha = 0.8f)
        )
    }
}
