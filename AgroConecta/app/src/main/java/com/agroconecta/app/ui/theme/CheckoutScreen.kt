package com.agroconecta.app.ui.theme

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.webkit.WebView
import android.webkit.WebViewClient
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.agroconecta.app.data.model.CarritoItem
import com.agroconecta.app.data.model.Direccion
import com.agroconecta.app.viewmodel.TiendaViewModel

private val DarkSlate = Color(0xFF0F172A)
private val Slate500 = Color(0xFF64748B)
private val Slate400 = Color(0xFF94A3B8)
private val Slate300 = Color(0xFFCBD5E1)
private val Slate200 = Color(0xFFE2E8F0)
private val Slate100 = Color(0xFFF1F5F9)
private val Brand600 = Color(0xFF16A34A)
private val Brand500 = Color(0xFF22C55E)
private val Brand400 = Color(0xFF4ADE80)
private val Brand50 = Color(0xFFF0FDF4)
private val SurfacePure = Color(0xFFF8FAF9)
private val Blue600 = Color(0xFF2563EB)
private val Indigo500 = Color(0xFF6366F1)
private val Purple500 = Color(0xFF8B5CF6)
private val Amber500 = Color(0xFFF59E0B)
private val Red500 = Color(0xFFEF4444)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckoutScreen(
    items: List<CarritoItem>,
    subtotal: Double,
    tiendaVM: TiendaViewModel,
    onNavigateBack: () -> Unit,
    onPaymentSuccess: () -> Unit
) {
    val context = LocalContext.current
    var direccionSeleccionada by remember { mutableStateOf<Direccion?>(null) }
    var propina by remember { mutableStateOf(0.0) }
    var tipoEnvio by remember { mutableStateOf("ECONOMICO") }
    var showPagoWebView by remember { mutableStateOf(false) }
    var mpUrl by remember { mutableStateOf<String?>(null) }
    var isProcessing by remember { mutableStateOf(false) }
    var isVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { isVisible = true }

    val envio = if (tipoEnvio == "RAPIDO") 8000.0 else 3500.0
    val tarifa = subtotal * 0.10
    val total = subtotal + envio + tarifa + propina

    LaunchedEffect(Unit) { tiendaVM.cargarDirecciones() }

    // Pago WebView overlay
    if (showPagoWebView && mpUrl != null) {
        PagoWebView(url = mpUrl!!, onClose = { showPagoWebView = false }, onPaymentDone = {
            showPagoWebView = false
            onPaymentSuccess()
        })
        return
    }

    Scaffold(
        containerColor = SurfacePure,
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
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        var backScale by remember { mutableStateOf(1f) }
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(Slate100)
                                .graphicsLayer { scaleX = backScale; scaleY = backScale }
                                .clickable {
                                    backScale = 0.9f
                                    onNavigateBack()
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver", tint = DarkSlate, modifier = Modifier.size(22.dp))
                        }
                        LaunchedEffect(backScale) {
                            if (backScale != 1f) {
                                kotlinx.coroutines.delay(100)
                                backScale = 1f
                            }
                        }
                        Spacer(Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "Confirmar Pedido",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = DarkSlate,
                                letterSpacing = (-0.5).sp
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                val infiniteTransition = rememberInfiniteTransition(label = "topbar")
                                val pulseAnim by infiniteTransition.animateFloat(
                                    initialValue = 0.6f,
                                    targetValue = 1f,
                                    animationSpec = infiniteRepeatable(tween(1500), RepeatMode.Reverse)
                                )
                                Box(
                                    modifier = Modifier
                                        .size(7.dp)
                                        .clip(CircleShape)
                                        .background(Brand600.copy(alpha = pulseAnim))
                                )
                                Spacer(Modifier.width(6.dp))
                                Text(
                                    "Paso final",
                                    fontSize = 12.sp,
                                    color = Slate500,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState())) {
            // ===== STEPPER =====
            AnimatedVisibility(
                visible = isVisible,
                enter = fadeIn(tween(500, delayMillis = 200)) + slideInVertically(tween(500, delayMillis = 200, easing = EaseOutCubic), initialOffsetY = { -it / 6 })
            ) {
                Surface(color = Color.White, modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        StepIndicator(icon = Icons.Filled.ShoppingCart, label = "Carrito", isActive = false, isCompleted = true)
                        StepConnector(isActive = true)
                        StepIndicator(icon = Icons.Filled.LocalShipping, label = "Envio", isActive = true, isCompleted = true)
                        StepConnector(isActive = false)
                        StepIndicator(icon = Icons.Filled.Payment, label = "Pago", isActive = true, isCompleted = false)
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            // ===== DIRECCION =====
            AnimatedVisibility(
                visible = isVisible,
                enter = fadeIn(tween(500, delayMillis = 300)) + slideInVertically(tween(500, delayMillis = 300, easing = EaseOutCubic), initialOffsetY = { it / 6 })
            ) {
                Column {
                    Row(
                        modifier = Modifier.padding(horizontal = 20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            Modifier.size(40.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Brush.linearGradient(listOf(Brand600, Brand500))),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Filled.LocationOn, null, tint = Color.White, modifier = Modifier.size(20.dp))
                        }
                        Spacer(Modifier.width(12.dp))
                        Text(
                            "Direccion de entrega",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = DarkSlate,
                            letterSpacing = (-0.3.sp)
                        )
                    }
                    Spacer(Modifier.height(12.dp))

                    val direcciones = tiendaVM.direccionesUsuario
                    if (direcciones.isEmpty()) {
                        Surface(
                            color = Color.White,
                            shape = RoundedCornerShape(20.dp),
                            border = BorderStroke(1.dp, Slate200),
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                val infiniteTransition = rememberInfiniteTransition(label = "noDir")
                                val floatY by infiniteTransition.animateFloat(
                                    initialValue = -6f,
                                    targetValue = 6f,
                                    animationSpec = infiniteRepeatable(tween(2500, easing = EaseInOutCubic), RepeatMode.Reverse),
                                    label = "floatY"
                                )
                                Box(
                                    modifier = Modifier
                                        .size(80.dp)
                                        .clip(CircleShape)
                                        .background(
                                            Brush.radialGradient(
                                                colors = listOf(Brand600.copy(alpha = 0.06f), Color.Transparent)
                                            )
                                        )
                                        .graphicsLayer { translationY = floatY },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Filled.LocationOff, null, tint = Slate400, modifier = Modifier.size(36.dp))
                                }
                                Spacer(Modifier.height(16.dp))
                                Text(
                                    "Sin direcciones guardadas",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = DarkSlate,
                                    letterSpacing = (-0.2.sp)
                                )
                                Spacer(Modifier.height(6.dp))
                                Text(
                                    "Agrega una en Mi Perfil",
                                    fontSize = 13.sp,
                                    color = Slate500,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.heightIn(max = 180.dp),
                            contentPadding = PaddingValues(horizontal = 20.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            val principal = direcciones.find { it.esPrincipal == true }
                            val mostrada = if (principal != null) {
                                if (direccionSeleccionada == null) direccionSeleccionada = principal
                                listOf(principal)
                            } else direcciones

                            items(mostrada) { dir ->
                                val selected = direccionSeleccionada?.id == dir.id
                                val interactionSource = remember { MutableInteractionSource() }
                                val isPressed by interactionSource.collectIsPressedAsState()
                                val scale by animateFloatAsState(
                                    if (isPressed) 0.98f else 1f,
                                    spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow)
                                )

                                Surface(
                                    color = if (selected) Brand50 else Color.White,
                                    shape = RoundedCornerShape(20.dp),
                                    border = BorderStroke(
                                        if (selected) 2.dp else 1.dp,
                                        if (selected) Brand600 else Slate200
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .graphicsLayer { scaleX = scale; scaleY = scale }
                                        .clickable(interactionSource = interactionSource, indication = null) {
                                            direccionSeleccionada = dir
                                        }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(16.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(44.dp)
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(
                                                    if (selected)
                                                        Brush.linearGradient(listOf(Brand600, Brand500))
                                                    else
                                                        Brush.linearGradient(listOf(Slate100, Color.White))
                                                ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                Icons.Filled.LocationOn,
                                                null,
                                                tint = if (selected) Color.White else Brand600,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                        Spacer(Modifier.width(12.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                dir.alias ?: "Direccion",
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = DarkSlate,
                                                letterSpacing = (-0.2.sp)
                                            )
                                            Spacer(Modifier.height(3.dp))
                                            Text(
                                                dir.direccionCompleta ?: "",
                                                fontSize = 12.sp,
                                                color = Slate500,
                                                maxLines = 2,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                        if (selected) {
                                            Box(
                                                modifier = Modifier
                                                    .size(28.dp)
                                                    .clip(CircleShape)
                                                    .background(Brand600),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    Icons.Filled.Check,
                                                    null,
                                                    tint = Color.White,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            // ===== PRODUCTOS =====
            AnimatedVisibility(
                visible = isVisible,
                enter = fadeIn(tween(500, delayMillis = 400)) + slideInVertically(tween(500, delayMillis = 400, easing = EaseOutCubic), initialOffsetY = { it / 6 })
            ) {
                Column {
                    Row(
                        modifier = Modifier.padding(horizontal = 20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            Modifier.size(40.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Brush.linearGradient(listOf(Indigo500, Purple500))),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Filled.ShoppingBasket, null, tint = Color.White, modifier = Modifier.size(20.dp))
                        }
                        Spacer(Modifier.width(12.dp))
                        Text(
                            "Productos",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = DarkSlate,
                            letterSpacing = (-0.3.sp)
                        )
                        Spacer(Modifier.width(12.dp))
                        Surface(
                            color = Slate100,
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text(
                                "${items.size} items",
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Slate500
                            )
                        }
                    }
                    Spacer(Modifier.height(12.dp))

                    Column(
                        modifier = Modifier.padding(horizontal = 20.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items.forEach { item ->
                            Surface(
                                color = Color.White,
                                shape = RoundedCornerShape(16.dp),
                                border = BorderStroke(1.dp, Slate200),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(Brand50),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            "${item.cantidad}x",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Brand600
                                        )
                                    }
                                    Spacer(Modifier.width(12.dp))
                                    Text(
                                        item.nombre ?: "",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = DarkSlate,
                                        modifier = Modifier.weight(1f),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        letterSpacing = (-0.2.sp)
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        "$${item.total?.toInt() ?: 0}",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Brand600,
                                        letterSpacing = (-0.2.sp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            // ===== TIPO DE ENTREGA =====
            AnimatedVisibility(
                visible = isVisible,
                enter = fadeIn(tween(500, delayMillis = 400)) + slideInVertically(tween(500, delayMillis = 400, easing = EaseOutCubic), initialOffsetY = { it / 6 })
            ) {
                Column(Modifier.padding(horizontal = 20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(40.dp).clip(RoundedCornerShape(12.dp)).background(Brush.linearGradient(listOf(Brand600, Brand500))), contentAlignment = Alignment.Center) {
                            Icon(Icons.Filled.LocalShipping, null, tint = Color.White, modifier = Modifier.size(20.dp))
                        }
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text("Tipo de entrega", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = DarkSlate, letterSpacing = (-0.3.sp))
                            Text("Selecciona como quieres recibir tu pedido", fontSize = 12.sp, color = Slate500)
                        }
                    }
                    Spacer(Modifier.height(12.dp))

                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        // ECONOMICO
                        Card(
                            Modifier.weight(1f).clickable { tipoEnvio = "ECONOMICO" },
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = if (tipoEnvio == "ECONOMICO") Brand50 else Color.White),
                            border = BorderStroke(if (tipoEnvio == "ECONOMICO") 2.dp else 1.dp, if (tipoEnvio == "ECONOMICO") Brand400 else Slate200)
                        ) {
                            Column(Modifier.padding(14.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Outlined.LocalShipping, null, tint = if (tipoEnvio == "ECONOMICO") Brand600 else Slate400, modifier = Modifier.size(24.dp))
                                Spacer(Modifier.height(6.dp))
                                Text("Economico", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = if (tipoEnvio == "ECONOMICO") Brand600 else DarkSlate)
                                Text("2-3 dias", fontSize = 11.sp, color = Slate400)
                                Spacer(Modifier.height(2.dp))
                                Text("$3.500 COP", fontWeight = FontWeight.ExtraBold, fontSize = 15.sp, color = if (tipoEnvio == "ECONOMICO") Brand600 else DarkSlate)
                                Text("base", fontSize = 10.sp, color = Slate400)
                                if (tipoEnvio == "ECONOMICO") {
                                    Spacer(Modifier.height(4.dp)); Icon(Icons.Filled.CheckCircle, null, tint = Brand600, modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                        // RAPIDO
                        Card(
                            Modifier.weight(1f).clickable { tipoEnvio = "RAPIDO" },
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = if (tipoEnvio == "RAPIDO") Color(0xFFEFF6FF) else Color.White),
                            border = BorderStroke(if (tipoEnvio == "RAPIDO") 2.dp else 1.dp, if (tipoEnvio == "RAPIDO") Blue600.copy(alpha = 0.4f) else Slate200)
                        ) {
                            Column(Modifier.padding(14.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Filled.FlashOn, null, tint = if (tipoEnvio == "RAPIDO") Blue600 else Slate400, modifier = Modifier.size(24.dp))
                                Spacer(Modifier.height(6.dp))
                                Text("Rapido", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = if (tipoEnvio == "RAPIDO") Blue600 else DarkSlate)
                                Text("24 horas", fontSize = 11.sp, color = Slate400)
                                Spacer(Modifier.height(2.dp))
                                Text("$8.000 COP", fontWeight = FontWeight.ExtraBold, fontSize = 15.sp, color = if (tipoEnvio == "RAPIDO") Blue600 else DarkSlate)
                                Text("base", fontSize = 10.sp, color = Slate400)
                                if (tipoEnvio == "RAPIDO") {
                                    Spacer(Modifier.height(4.dp)); Icon(Icons.Filled.CheckCircle, null, tint = Blue600, modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                    }
                    Spacer(Modifier.height(6.dp))
                    Text("El costo final se ajusta segun distancia y peso de tu pedido", fontSize = 10.sp, color = Slate400, modifier = Modifier.align(Alignment.CenterHorizontally))
                }
            }

            Spacer(Modifier.height(16.dp))

            // ===== RESUMEN GLASSMORPHISM =====
            AnimatedVisibility(
                visible = isVisible,
                enter = fadeIn(tween(500, delayMillis = 500)) + slideInVertically(tween(500, delayMillis = 500, easing = EaseOutCubic), initialOffsetY = { it / 6 })
            ) {
                Surface(
                    color = Color.White.copy(alpha = 0.85f),
                    shape = RoundedCornerShape(24.dp),
                    shadowElevation = 0.dp,
                    border = BorderStroke(1.dp, Slate200),
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        // Header
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                Modifier.size(40.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Brush.linearGradient(listOf(Amber500, Color(0xFFF97316)))),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Filled.Receipt, null, tint = Color.White, modifier = Modifier.size(20.dp))
                            }
                            Spacer(Modifier.width(12.dp))
                            Text(
                                "Resumen del pedido",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = DarkSlate,
                                letterSpacing = (-0.3.sp)
                            )
                        }
                        Spacer(Modifier.height(16.dp))

                        // Glassmorphism summary box
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(
                                    Brush.linearGradient(
                                        colors = listOf(
                                            Brand600.copy(alpha = 0.06f),
                                            Brand500.copy(alpha = 0.03f)
                                        )
                                    )
                                )
                                .padding(16.dp)
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Subtotal", fontSize = 13.sp, color = Slate500)
                                    Text("$${subtotal.toInt()}", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = DarkSlate)
                                }
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Envio", fontSize = 13.sp, color = Slate500)
                                    Text("$${envio.toInt()}", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = DarkSlate)
                                }
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Tarifa (10%)", fontSize = 13.sp, color = Slate500)
                                    Text("$${tarifa.toInt()}", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = DarkSlate)
                                }
                            }
                        }

                        Spacer(Modifier.height(16.dp))

                        // Propina
                        Text("Propina (opcional)", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Slate500, letterSpacing = 0.3.sp)
                        Spacer(Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(modifier = Modifier.weight(1f)) {
                                PropinaButton("$0", propina == 0.0) { propina = 0.0 }
                            }
                            Box(modifier = Modifier.weight(1f)) {
                                PropinaButton("$2k", propina == 2000.0) { propina = 2000.0 }
                            }
                            Box(modifier = Modifier.weight(1f)) {
                                PropinaButton("$5k", propina == 5000.0) { propina = 5000.0 }
                            }
                        }
                        if (propina > 0) {
                            Spacer(Modifier.height(8.dp))
                            Text(
                                "+ $$propina propina",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Brand600,
                                modifier = Modifier.align(Alignment.End)
                            )
                        }

                        Spacer(Modifier.height(16.dp))

                        // Divider
                        Box(Modifier.fillMaxWidth().height(1.dp).background(Slate200))

                        Spacer(Modifier.height(16.dp))

                        // Total
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Total",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = DarkSlate,
                                letterSpacing = (-0.2.sp)
                            )
                            Text(
                                "$${total.toInt()}",
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Bold,
                                color = Brand600,
                                letterSpacing = (-0.5.sp)
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            // ===== ERROR DISPLAY =====
            if (tiendaVM.errorOperacion != null) {
                AnimatedVisibility(
                    visible = isVisible,
                    enter = fadeIn(tween(400)) + slideInVertically(tween(400, easing = EaseOutCubic), initialOffsetY = { it / 6 })
                ) {
                    Surface(
                        color = Color(0xFFFEF2F2),
                        shape = RoundedCornerShape(20.dp),
                        border = BorderStroke(1.dp, Color(0xFFFECACA)),
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFDC2626).copy(alpha = 0.1f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Filled.ErrorOutline,
                                    null,
                                    tint = Color(0xFFDC2626),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    "Error al procesar pago",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF991B1B)
                                )
                                Spacer(Modifier.height(2.dp))
                                Text(
                                    tiendaVM.errorOperacion ?: "",
                                    fontSize = 12.sp,
                                    color = Color(0xFFDC2626)
                                )
                            }
                            IconButton(
                                onClick = { tiendaVM.errorOperacion = null },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    Icons.Filled.Close,
                                    null,
                                    tint = Color(0xFF991B1B),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                }
            }

            // ===== PAGO BUTTON =====
            AnimatedVisibility(
                visible = isVisible,
                enter = fadeIn(tween(500, delayMillis = 600)) + slideInVertically(tween(500, delayMillis = 600, easing = EaseOutCubic), initialOffsetY = { it / 6 })
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    var btnScale by remember { mutableStateOf(1f) }
                    Button(
                        onClick = {
                            btnScale = 0.97f
                            isProcessing = true
                            tiendaVM.crearOrdenPago(
                                propina,
                                direccionSeleccionada?.direccionCompleta,
                                direccionSeleccionada?.latitud,
                                direccionSeleccionada?.longitud,
                                tipoEnvio
                            )
                        },
                        enabled = !isProcessing && items.isNotEmpty(),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Brand600,
                            contentColor = Color.White
                        ),
                        contentPadding = PaddingValues(vertical = 16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .graphicsLayer { scaleX = btnScale; scaleY = btnScale }
                    ) {
                        if (isProcessing) {
                            val infiniteTransition = rememberInfiniteTransition(label = "processing")
                            val rotation by infiniteTransition.animateFloat(
                                initialValue = 0f,
                                targetValue = 360f,
                                animationSpec = infiniteRepeatable(tween(1000, easing = LinearEasing))
                            )
                            CircularProgressIndicator(
                                modifier = Modifier
                                    .size(22.dp)
                                    .graphicsLayer { rotationZ = rotation },
                                strokeWidth = 2.5.dp,
                                color = Color.White
                            )
                            Spacer(Modifier.width(12.dp))
                            Text(
                                "Procesando...",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        } else {
                            Icon(
                                Icons.Filled.Payment,
                                null,
                                tint = Color.White,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(Modifier.width(12.dp))
                            Text(
                                "Pagar con Mercado Pago",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                    LaunchedEffect(btnScale) {
                        if (btnScale != 1f) {
                            kotlinx.coroutines.delay(100)
                            btnScale = 1f
                        }
                    }

                    // Security badge
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            Icons.Filled.Lock,
                            null,
                            tint = Slate400,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            "Pago 100% Seguro",
                            fontSize = 11.sp,
                            color = Slate400
                        )
                    }
                }
            }

            // Watch for MP URL from ViewModel
            LaunchedEffect(tiendaVM.mpUrl) {
                tiendaVM.mpUrl?.let { url ->
                    mpUrl = url
                    showPagoWebView = true
                    isProcessing = false
                    tiendaVM.mpUrl = null
                }
            }

            // Watch for errors
            LaunchedEffect(tiendaVM.errorOperacion) {
                if (tiendaVM.errorOperacion != null) {
                    isProcessing = false
                }
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
private fun StepIndicator(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    isActive: Boolean,
    isCompleted: Boolean
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(
                    if (isActive) Brush.linearGradient(listOf(Brand600, Brand500))
                    else if (isCompleted) Brush.linearGradient(listOf(Brand500, Brand500))
                    else Brush.linearGradient(listOf(Slate100, Slate100))
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                icon,
                null,
                tint = if (isActive || isCompleted) Color.White else Slate400,
                modifier = Modifier.size(16.dp)
            )
        }
        Spacer(Modifier.height(6.dp))
        Text(
            label,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = if (isActive) Brand600 else if (isCompleted) Brand500 else Slate400
        )
    }
}

@Composable
private fun StepConnector(isActive: Boolean) {
    Box(
        Modifier
            .height(2.dp)
            .width(36.dp)
            .background(if (isActive) Brand500 else Slate200)
    )
}

@Composable
private fun PropinaButton(label: String, selected: Boolean, onClick: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        if (isPressed) 0.95f else 1f,
        spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow)
    )

    Surface(
        color = if (selected) Brand600 else Color.White,
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(
            1.5.dp,
            if (selected) Brand600 else Slate200
        ),
        modifier = Modifier
            .fillMaxWidth()
            .height(44.dp)
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .clickable(interactionSource = interactionSource, indication = null) { onClick() }
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                label,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = if (selected) Color.White else Slate500
            )
        }
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun PagoWebView(url: String, onClose: () -> Unit, onPaymentDone: () -> Unit) {
    val context = LocalContext.current
    var isLoading by remember { mutableStateOf(true) }

    Box(modifier = Modifier.fillMaxSize().background(Color.White)) {
        AndroidView(
            factory = { ctx ->
                WebView(ctx).apply {
                    settings.javaScriptEnabled = true
                    settings.domStorageEnabled = true
                    webViewClient = object : WebViewClient() {
                        override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                            isLoading = true
                        }
                        override fun onPageFinished(view: WebView?, url: String?) {
                            isLoading = false
                            url?.let {
                                if (it.contains("/orden/success") || it.contains("/orden/pending")) {
                                    onPaymentDone()
                                }
                            }
                        }
                    }
                    loadUrl(url)
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        // Loading indicator
        if (isLoading) {
            Box(
                Modifier
                    .fillMaxSize()
                    .background(Color.White.copy(alpha = 0.9f)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    val infiniteTransition = rememberInfiniteTransition(label = "mpLoading")
                    val scale by infiniteTransition.animateFloat(
                        initialValue = 0.8f,
                        targetValue = 1.1f,
                        animationSpec = infiniteRepeatable(tween(800, easing = EaseInOutCubic), RepeatMode.Reverse),
                        label = "scale"
                    )
                    val alpha by infiniteTransition.animateFloat(
                        initialValue = 0.5f,
                        targetValue = 1f,
                        animationSpec = infiniteRepeatable(tween(800, easing = EaseInOutCubic), RepeatMode.Reverse),
                        label = "alpha"
                    )
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(Brand600.copy(alpha = alpha * 0.2f), Brand500.copy(alpha = alpha * 0.1f))
                                )
                            )
                            .graphicsLayer { scaleX = scale; scaleY = scale },
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            color = Brand600,
                            strokeWidth = 3.dp,
                            modifier = Modifier.size(40.dp)
                        )
                    }
                    Spacer(Modifier.height(20.dp))
                    Text(
                        "Cargando Mercado Pago...",
                        fontSize = 14.sp,
                        color = Slate500,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        // Close button
        IconButton(
            onClick = onClose,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .statusBarsPadding()
                .padding(12.dp)
                .size(44.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.95f))
                .shadow(4.dp, CircleShape)
        ) {
            Icon(Icons.Filled.Close, null, tint = DarkSlate, modifier = Modifier.size(22.dp))
        }
    }
}

@Composable
fun CompraExitosaScreen(onNavigateToTienda: () -> Unit, onNavigateToCompras: () -> Unit) {
    var isVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { isVisible = true }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SurfacePure),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        AnimatedVisibility(
            visible = isVisible,
            enter = fadeIn(tween(600)) + scaleIn(tween(600, easing = EaseOutBack))
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                val infiniteTransition = rememberInfiniteTransition(label = "success")
                val scale by infiniteTransition.animateFloat(
                    initialValue = 0.95f,
                    targetValue = 1.05f,
                    animationSpec = infiniteRepeatable(tween(1500, easing = EaseInOutCubic), RepeatMode.Reverse),
                    label = "scale"
                )
                val rotation by infiniteTransition.animateFloat(
                    initialValue = -5f,
                    targetValue = 5f,
                    animationSpec = infiniteRepeatable(tween(2000, easing = EaseInOutCubic), RepeatMode.Reverse),
                    label = "rotation"
                )

                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(Brand600.copy(alpha = 0.1f), Color.Transparent)
                            )
                        )
                        .graphicsLayer {
                            scaleX = scale
                            scaleY = scale
                            rotationZ = rotation
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(88.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(listOf(Brand50, Color.White))
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Filled.CheckCircle,
                            null,
                            tint = Brand600,
                            modifier = Modifier.size(56.dp)
                        )
                    }
                }

                Spacer(Modifier.height(32.dp))

                Text(
                    "Compra Exitosa!",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = DarkSlate,
                    letterSpacing = (-0.5.sp)
                )

                Spacer(Modifier.height(12.dp))

                Text(
                    "Tu pedido ha sido creado.\nTe notificaremos cuando el campesino lo confirme.",
                    fontSize = 15.sp,
                    color = Slate500,
                    textAlign = TextAlign.Center,
                    lineHeight = 22.sp
                )

                Spacer(Modifier.height(40.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    var btnScale1 by remember { mutableStateOf(1f) }
                    OutlinedButton(
                        onClick = {
                            btnScale1 = 0.95f
                            onNavigateToTienda()
                        },
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.5.dp, Brand600),
                        modifier = Modifier
                            .height(52.dp)
                            .graphicsLayer { scaleX = btnScale1; scaleY = btnScale1 }
                    ) {
                        Icon(
                            Icons.Filled.Store,
                            null,
                            tint = Brand600,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "Seguir comprando",
                            color = Brand600,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                    LaunchedEffect(btnScale1) {
                        if (btnScale1 != 1f) {
                            kotlinx.coroutines.delay(100)
                            btnScale1 = 1f
                        }
                    }

                    var btnScale2 by remember { mutableStateOf(1f) }
                    Button(
                        onClick = {
                            btnScale2 = 0.95f
                            onNavigateToCompras()
                        },
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Brand600,
                            contentColor = Color.White
                        ),
                        modifier = Modifier
                            .height(52.dp)
                            .graphicsLayer { scaleX = btnScale2; scaleY = btnScale2 }
                    ) {
                        Icon(
                            Icons.Filled.Receipt,
                            null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "Mis Compras",
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 14.sp
                        )
                    }
                    LaunchedEffect(btnScale2) {
                        if (btnScale2 != 1f) {
                            kotlinx.coroutines.delay(100)
                            btnScale2 = 1f
                        }
                    }
                }
            }
        }
    }
}
