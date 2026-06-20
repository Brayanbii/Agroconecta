package com.agroconectago.app.ui

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.automirrored.outlined.Help
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
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.agroconectago.app.data.LocationTracker
import com.agroconectago.app.data.api.DeliveryRetrofitClient
import com.agroconectago.app.data.model.DeliveryUsuarioInfo
import com.agroconectago.app.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun DeliveryDashboardScreen(
    usuarioLogueado: DeliveryUsuarioInfo?,
    onLogout: () -> Unit,
    onVerRutas: () -> Unit = {},
    onMisViajes: () -> Unit = {},
    onMiPerfil: () -> Unit = {},
    onRutaSeleccionada: (Long) -> Unit = {}
) {
    var conectado by remember { mutableStateOf(false) }
    var menuAbierto by remember { mutableStateOf(false) }
    var isVisible by remember { mutableStateOf(false) }
    var rutasCercanas by remember { mutableStateOf<List<Map<String, Any?>>>(emptyList()) }
    var loadingRutas by remember { mutableStateOf(false) }
    val MAX_RUTAS_INLINE = 6
    LaunchedEffect(Unit) { delay(100); isVisible = true }

    val nombre = usuarioLogueado?.userName ?: "Agrosocio"
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val locationTracker = remember { LocationTracker(context) }
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted && conectado) {
            locationTracker.start()
        }
    }

    fun cargarRutas() {
        if (!conectado) return
        scope.launch {
            loadingRutas = true
            try {
                val loc = locationTracker.getCurrentLocation()
                val resp = DeliveryRetrofitClient.api.rutasDisponibles(
                    lat = loc?.first, lng = loc?.second, radioKm = 200.0
                )
                rutasCercanas = (resp["rutas"] as? List<*>)?.filterIsInstance<Map<String, Any?>>() ?: emptyList()
            } catch (_: Exception) {}
            loadingRutas = false
        }
    }

    fun toggleConexion() {
        conectado = !conectado
        if (conectado) {
            permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            locationTracker.start()
            cargarRutas()
        } else {
            rutasCercanas = emptyList()
            scope.launch { try { DeliveryRetrofitClient.api.marcarOffline() } catch (_: Exception) {} }
        }
    }

    val toggleOffset by animateFloatAsState(
        targetValue = if (conectado) 1f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        )
    )

    val infiniteTransition = rememberInfiniteTransition(label = "dash")

    val pulseScale by infiniteTransition.animateFloat(
        1f, 1.8f,
        infiniteRepeatable(tween(1500), RepeatMode.Reverse),
        label = "pulseScale"
    )
    val pulseAlphaBadge by infiniteTransition.animateFloat(
        0.4f, 0f,
        infiniteRepeatable(tween(1500), RepeatMode.Reverse),
        label = "pulseAlpha"
    )

    val orb1X by infiniteTransition.animateFloat(
        0.75f, 0.9f,
        infiniteRepeatable(tween(7000, easing = EaseInOutCubic), RepeatMode.Reverse),
        label = "orb1X"
    )
    val orb1Y by infiniteTransition.animateFloat(
        0.08f, 0.22f,
        infiniteRepeatable(tween(8000, easing = EaseInOutCubic), RepeatMode.Reverse),
        label = "orb1Y"
    )
    val orb2X by infiniteTransition.animateFloat(
        0.1f, 0.22f,
        infiniteRepeatable(tween(9000, easing = EaseInOutCubic), RepeatMode.Reverse),
        label = "orb2X"
    )
    val orb2Y by infiniteTransition.animateFloat(
        0.72f, 0.88f,
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

    val radarPulse1 by infiniteTransition.animateFloat(
        0f, 1f,
        infiniteRepeatable(tween(2200, easing = LinearEasing), RepeatMode.Restart),
        label = "radar1"
    )
    val radarPulse2 by infiniteTransition.animateFloat(
        0f, 1f,
        infiniteRepeatable(tween(2200, easing = LinearEasing, delayMillis = 733), RepeatMode.Restart),
        label = "radar2"
    )
    val radarPulse3 by infiniteTransition.animateFloat(
        0f, 1f,
        infiniteRepeatable(tween(2200, easing = LinearEasing, delayMillis = 1466), RepeatMode.Restart),
        label = "radar3"
    )

    val orbDisconnectedColor = Color(0xFF64748B)
    val orbConnectedColor = Color(0xFF10B981)
    val orbColor by animateColorAsState(
        targetValue = if (conectado) orbConnectedColor else orbDisconnectedColor,
        animationSpec = tween(900, easing = EaseInOutCubic),
        label = "orbColor"
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
                            colors = listOf(orbColor.copy(alpha = orbAlpha), Color.Transparent)
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
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(DeliveryBrand500.copy(alpha = orbAlpha * 0.6f), Color.Transparent)
                        ),
                        radius = size.width * 0.3f,
                        center = Offset(size.width * 0.5f, size.height * 0.45f)
                    )
                }
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            // ═══════════════════════════════════════════════════════
            // HEADER - GREETING + GLASS STATUS CAPSULE
            // ═══════════════════════════════════════════════════════
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 20.dp, end = 16.dp, top = 20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "Hola, $nombre",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Slate900,
                        letterSpacing = (-0.5).sp
                    )
                    Spacer(Modifier.height(10.dp))

                    val statusBg by animateColorAsState(
                        targetValue = if (conectado) Color(0xFF10B981).copy(alpha = 0.1f) else Slate100.copy(alpha = 0.7f),
                        animationSpec = tween(500),
                        label = "statusBg"
                    )
                    val statusBorder by animateColorAsState(
                        targetValue = if (conectado) Color(0xFF10B981).copy(alpha = 0.25f) else Color(0xFFE2E8F0),
                        animationSpec = tween(500),
                        label = "statusBorder"
                    )

                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(statusBg)
                            .border(1.dp, statusBorder, RoundedCornerShape(20.dp))
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(modifier = Modifier.size(18.dp), contentAlignment = Alignment.Center) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(if (conectado) Color(0xFF22C55E) else Slate300)
                            )
                            if (conectado) {
                                Box(
                                    modifier = Modifier
                                        .size(18.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF22C55E).copy(alpha = pulseAlphaBadge))
                                        .graphicsLayer { scaleX = pulseScale; scaleY = pulseScale }
                                )
                            }
                        }
                        Spacer(Modifier.width(6.dp))
                        Text(
                            if (conectado) "Conectado" else "Desconectado",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (conectado) Color(0xFF059669) else Slate400
                        )
                        if (conectado) {
                            Spacer(Modifier.width(5.dp))
                            Text(
                                "LIVE",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF10B981),
                                letterSpacing = 1.5.sp
                            )
                        }
                    }
                }

                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                colors = listOf(DeliveryBrand500, DeliveryBrand600)
                            )
                        )
                        .shadow(12.dp, CircleShape, spotColor = DeliveryBrand600.copy(alpha = 0.3f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        nombre.take(1).uppercase(),
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 18.sp,
                        color = Color.White
                    )
                }
            }

            // ═══════════════════════════════════════════════════════
            // CENTER - RADAR / ROUTES AREA
            // ═══════════════════════════════════════════════════════
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 16.dp),
                contentAlignment = Alignment.TopCenter
            ) {
                if (!conectado) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(110.dp)
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
                                modifier = Modifier.size(52.dp)
                            )
                        }
                        Spacer(Modifier.height(24.dp))
                        Text(
                            "Conectate para recibir viajes",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 20.sp,
                            color = Slate900,
                            textAlign = TextAlign.Center,
                            letterSpacing = (-0.3).sp
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "Activa el boton de abajo para\nempezar a recibir pedidos.",
                            fontSize = 14.sp,
                            color = Slate400,
                            textAlign = TextAlign.Center,
                            lineHeight = 20.sp
                        )
                    }
                } else if (loadingRutas) {
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
                } else if (rutasCercanas.isEmpty()) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier.size(190.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            if (conectado) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .drawBehind {
                                            val cx = size.width / 2f
                                            val cy = size.height / 2f
                                            val maxR = size.minDimension / 2f
                                            val minR = 40.dp.toPx()
                                            listOf(
                                                radarPulse1 to Color(0xFF10B981).copy(alpha = 0.12f),
                                                radarPulse2 to Color(0xFF10B981).copy(alpha = 0.09f),
                                                radarPulse3 to Color(0xFF10B981).copy(alpha = 0.06f)
                                            ).forEach { (progress, clr) ->
                                                val radius = minR + (maxR - minR) * progress
                                                val fade = (1f - progress).coerceAtLeast(0f)
                                                drawCircle(clr.copy(alpha = clr.alpha * fade), radius, Offset(cx, cy))
                                            }
                                        }
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .size(80.dp)
                                    .clip(RoundedCornerShape(28.dp))
                                    .background(
                                        Brush.linearGradient(
                                            colors = listOf(
                                                Color(0xFFECFEFF),
                                                DeliveryBrand50
                                            )
                                        )
                                    )
                                    .border(
                                        1.5.dp,
                                        DeliveryBrand400.copy(alpha = 0.3f),
                                        RoundedCornerShape(28.dp)
                                    )
                                    .shadow(16.dp, RoundedCornerShape(28.dp), spotColor = DeliveryBrand600.copy(alpha = 0.12f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Filled.Radar,
                                    contentDescription = null,
                                    tint = DeliveryBrand600,
                                    modifier = Modifier.size(40.dp)
                                )
                            }
                        }

                        Spacer(Modifier.height(20.dp))
                        Text(
                            "Escaneando fincas cercanas...",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 18.sp,
                            color = Slate900,
                            letterSpacing = (-0.2).sp
                        )
                        Text(
                            "Te notificaremos cuando haya pedidos",
                            fontSize = 13.sp,
                            color = Slate400
                        )
                        Spacer(Modifier.height(16.dp))
                        OutlinedButton(
                            onClick = { cargarRutas() },
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = DeliveryBrand600),
                            border = BorderStroke(1.2.dp, DeliveryBrand400.copy(alpha = 0.3f)),
                            modifier = Modifier.height(44.dp)
                        ) {
                            Icon(Icons.Filled.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Actualizar", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                        }
                    }
                } else {
                    Column(modifier = Modifier.fillMaxSize()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    "Rutas disponibles",
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 17.sp,
                                    color = Slate900,
                                    letterSpacing = (-0.2).sp
                                )
                                Text(
                                    "${rutasCercanas.size} pedidos activos cerca de ti",
                                    fontSize = 11.sp,
                                    color = Slate400
                                )
                            }
                            IconButton(
                                onClick = { cargarRutas() },
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Slate100)
                            ) {
                                Icon(
                                    Icons.Filled.Refresh,
                                    contentDescription = null,
                                    tint = Slate500,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        val mostradas = rutasCercanas.take(MAX_RUTAS_INLINE)
                        LazyColumn(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(mostradas) { ruta ->
                                DashboardRutaCardPremium(ruta, onClick = {
                                    val id = (ruta["id"] as? Number)?.toLong()
                                    if (id != null) onRutaSeleccionada(id)
                                })
                            }
                        }

                        if (rutasCercanas.size > MAX_RUTAS_INLINE) {
                            Spacer(Modifier.height(8.dp))
                            OutlinedButton(
                                onClick = onVerRutas,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp),
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = DeliveryBrand600),
                                border = BorderStroke(1.5.dp, DeliveryBrand400.copy(alpha = 0.4f))
                            ) {
                                Text(
                                    "Ver ${rutasCercanas.size - MAX_RUTAS_INLINE}+ rutas mas",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                                Spacer(Modifier.width(6.dp))
                                Icon(
                                    Icons.AutoMirrored.Filled.ArrowForward,
                                    contentDescription = null,
                                    tint = DeliveryBrand600,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }

            // ═══════════════════════════════════════════════════════
            // SUPER TOGGLE - FULL-WIDTH ELASTIC PILL
            // ═══════════════════════════════════════════════════════
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 16.dp)
            ) {
                val toggleBg by animateColorAsState(
                    targetValue = if (conectado) Color(0xFF10B981) else Color(0xFF1E293B),
                    animationSpec = tween(500),
                    label = "toggleBg"
                )
                val toggleGlow by animateColorAsState(
                    targetValue = if (conectado) Color(0xFF10B981).copy(alpha = 0.35f) else Color.Transparent,
                    animationSpec = tween(500),
                    label = "toggleGlow"
                )
                val knobExpand by animateFloatAsState(
                    targetValue = if (conectado) 0.55f else 0.44f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow
                    ),
                    label = "knobExpand"
                )

                val interactionSource = remember { MutableInteractionSource() }
                val isPressed by interactionSource.collectIsPressedAsState()
                val pressScale by animateFloatAsState(
                    targetValue = if (isPressed) 0.97f else 1f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow
                    ),
                    label = "pressScale"
                )

                BoxWithConstraints(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(62.dp)
                        .graphicsLayer { scaleX = pressScale; scaleY = pressScale }
                        .clip(RoundedCornerShape(31.dp))
                        .background(toggleBg)
                        .shadow(
                            elevation = 22.dp,
                            shape = RoundedCornerShape(31.dp),
                            spotColor = toggleGlow,
                            ambientColor = toggleGlow.copy(alpha = toggleGlow.alpha * 0.5f)
                        )
                        .clickable(
                            interactionSource = interactionSource,
                            indication = null
                        ) { toggleConexion() }
                ) {
                    val pillW = maxWidth
                    val knobW = pillW * knobExpand

                    Box(
                        modifier = Modifier
                            .offset {
                                IntOffset(
                                    x = (toggleOffset * (pillW - knobW).toPx()).roundToInt(),
                                    y = 0
                                )
                            }
                            .width(knobW)
                            .fillMaxHeight()
                            .padding(5.dp)
                            .clip(RoundedCornerShape(27.dp))
                            .background(Color.White)
                            .shadow(8.dp, RoundedCornerShape(27.dp), spotColor = Color.Black.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (conectado) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF10B981))
                                        .drawBehind {
                                            drawCircle(
                                                Color(0xFF10B981).copy(alpha = pulseAlphaBadge),
                                                size.width * 1.8f
                                            )
                                        }
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    "En linea",
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 15.sp,
                                    color = Color(0xFF059669),
                                    letterSpacing = (-0.2).sp
                                )
                            }
                        } else {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Filled.PowerSettingsNew,
                                    contentDescription = null,
                                    tint = Slate400,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    "Conectarse",
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 15.sp,
                                    color = Slate400,
                                    letterSpacing = (-0.2).sp
                                )
                            }
                        }
                    }

                    if (conectado) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(start = 18.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            Text(
                                "Buscando...",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color.White.copy(alpha = 0.6f)
                            )
                        }
                    }
                }

                Spacer(Modifier.height(18.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    IconButton(
                        onClick = { menuAbierto = true },
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.85f))
                            .border(1.dp, Color(0xFFE8ECEF).copy(alpha = 0.5f), CircleShape)
                            .shadow(10.dp, CircleShape, spotColor = Color(0x08000000))
                    ) {
                        Icon(
                            Icons.Filled.Menu,
                            contentDescription = "Menu",
                            tint = Slate600,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // DRAWER MENU - PRESERVED
    // ═══════════════════════════════════════════════════════════════
    if (menuAbierto) {
        Box(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.4f))
                    .clickable { menuAbierto = false }
            )

            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(280.dp)
                    .background(DeliverySurface)
                    .align(Alignment.CenterStart)
                    .shadow(16.dp)
                    .statusBarsPadding()
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(DeliveryBrand600)
                            .padding(20.dp)
                    ) {
                        Column {
                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(Color.White.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Filled.Motorcycle,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                            Spacer(Modifier.height(12.dp))
                            Text(
                                nombre,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 16.sp,
                                color = Color.White
                            )
                            Text(
                                usuarioLogueado?.email ?: "",
                                fontSize = 11.sp,
                                color = Color.White.copy(alpha = 0.7f)
                            )
                        }
                    }

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        DrawerItem(Icons.Outlined.Route, "Mis Viajes", "Historial de entregas") {
                            menuAbierto = false; onMisViajes()
                        }
                        DrawerItem(Icons.Outlined.AccountBalanceWallet, "Billetera", "Ganancias y retiros")
                        DrawerItem(Icons.Outlined.Star, "Mi Calificacion", "Reputacion como repartidor")
                        DrawerItem(Icons.Outlined.Person, "Mi Perfil", "Datos y documentos") {
                            menuAbierto = false; onMiPerfil()
                        }
                        DrawerItem(Icons.AutoMirrored.Outlined.Help, "Soporte", "Centro de ayuda")
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { menuAbierto = false; onLogout() }
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.Logout,
                            contentDescription = null,
                            tint = ErrorRed,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(12.dp))
                        Text(
                            "Cerrar sesion",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp,
                            color = ErrorRed
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun InfoChip(icon: ImageVector, text: String, color: Color, modifier: Modifier = Modifier) {
    Column(
        modifier
            .clip(RoundedCornerShape(14.dp))
            .background(Color.White)
            .border(1.dp, color.copy(alpha = 0.15f), RoundedCornerShape(14.dp))
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(22.dp))
        Spacer(Modifier.height(6.dp))
        Text(
            text,
            fontSize = 10.sp,
            fontWeight = FontWeight.SemiBold,
            color = Slate500,
            textAlign = TextAlign.Center,
            lineHeight = 14.sp
        )
    }
}

@Composable
private fun DrawerItem(icon: ImageVector, title: String, subtitle: String, onClick: () -> Unit = {}) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = Slate500, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(12.dp))
        Column {
            Text(title, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Slate700)
            Text(subtitle, fontSize = 11.sp, color = Slate400)
        }
        Spacer(Modifier.weight(1f))
        Icon(
            Icons.Filled.ChevronRight,
            contentDescription = null,
            tint = Slate300,
            modifier = Modifier.size(18.dp)
        )
    }
}

@Composable
private fun DashboardRutaCardPremium(ruta: Map<String, Any?>, onClick: () -> Unit) {
    val dist = (ruta["distanciaKm"] as? Number)?.toDouble() ?: 0.0
    val pago = (ruta["pagoTotalEstimado"] as? Number)?.toDouble() ?: 0.0
    val tipoVeh = ruta["tipoVehiculoRequerido"]?.toString() ?: "MOTO"
    val pedidos = ruta["pedidosCount"]?.toString() ?: "0"
    val peso = ruta["pesoTotalKg"]?.toString() ?: "0"

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val cardScale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "cardScale"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer { scaleX = cardScale; scaleY = cardScale }
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(14.dp))
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
                    fontSize = 18.sp
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        ruta["codigoRuta"]?.toString() ?: "",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 14.sp,
                        color = Slate900,
                        letterSpacing = (-0.2).sp
                    )
                    if (dist > 0) {
                        Spacer(Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(5.dp))
                                .background(Slate100)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
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
                Spacer(Modifier.height(2.dp))
                Text(
                    "${ruta["zonaOrigen"]?.toString() ?: ""} \u2192 ${ruta["zonaDestino"]?.toString() ?: ""}",
                    fontSize = 11.sp,
                    color = Slate400,
                    maxLines = 1
                )
                Spacer(Modifier.height(3.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "$pedidos ped \u00B7 ${peso}kg",
                        fontSize = 11.sp,
                        color = Slate500
                    )
                    Spacer(Modifier.width(10.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(5.dp))
                            .background(DeliveryBrand50)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            "$${String.format("%.0f", pago)}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = DeliveryBrand600
                        )
                    }
                }
            }
            Icon(
                Icons.Filled.ChevronRight,
                contentDescription = null,
                tint = Slate300,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
