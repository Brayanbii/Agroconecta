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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.agroconecta.app.data.model.DatoMensual
import com.agroconecta.app.data.model.FinanzasResponse
import com.agroconecta.app.data.model.MovimientoTx
import com.agroconecta.app.viewmodel.TiendaViewModel

private val Carbon = Color(0xFF111827)
private val Slate400 = Color(0xFF94A3B8)
private val Slate500 = Color(0xFF64748B)
private val Slate300 = Color(0xFFCBD5E1)
private val Slate200 = Color(0xFFE2E8F0)
private val Slate100 = Color(0xFFF1F5F9)
private val AppBackground = Color(0xFFF8FAF9)
private val Emerald = Color(0xFF0E793D)
private val Emerald700 = Color(0xFF047857)
private val Green500 = Color(0xFF22C55E)
private val Green50 = Color(0xFFF0FDF4)
private val Amber500 = Color(0xFFF59E0B)
private val Amber50 = Color(0xFFFFFDE7)
private val Blue500 = Color(0xFF3B82F6)
private val Blue50 = Color(0xFFEFF6FF)
private val Purple500 = Color(0xFF8B5CF6)
private val Purple50 = Color(0xFFF3E8FF)
private val Red500 = Color(0xFFEF4444)
private val Red50 = Color(0xFFFEF2F2)
private val Teal500 = Color(0xFF14B8A6)
private val Indigo500 = Color(0xFF6366F1)
private val Indigo600 = Color(0xFF4F46E5)
private val Cyan500 = Color(0xFF06B6D4)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FinanzasScreen(tiendaVM: TiendaViewModel, onNavigateBack: () -> Unit) {
    LaunchedEffect(Unit) { tiendaVM.cargarFinanzas() }

    val data = tiendaVM.finanzasResponse
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
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Carbon, modifier = Modifier.size(22.dp))
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
                                "AgroWallet",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = Carbon,
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
                                        .background(Green500.copy(alpha = pulseAnim))
                                )
                                Spacer(Modifier.width(6.dp))
                                Text(
                                    "Billetera activa",
                                    fontSize = 12.sp,
                                    color = Slate500,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.linearGradient(
                                        colors = listOf(Indigo500, Purple500)
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Filled.AccountBalanceWallet, null, tint = Color.White, modifier = Modifier.size(20.dp))
                        }
                    }
                }
            }
        }
    ) { padding ->
        if (tiendaVM.finanzasCargando && data == null) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    val infiniteTransition = rememberInfiniteTransition(label = "loading")
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
                                    colors = listOf(Indigo500.copy(alpha = alpha * 0.2f), Purple500.copy(alpha = alpha * 0.1f))
                                )
                            )
                            .graphicsLayer { scaleX = scale; scaleY = scale },
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            color = Indigo500,
                            strokeWidth = 3.dp,
                            modifier = Modifier.size(40.dp)
                        )
                    }
                    Spacer(Modifier.height(20.dp))
                    Text(
                        "Cargando finanzas...",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Slate400
                    )
                }
            }
            return@Scaffold
        }

        if (data == null || data.success == false) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                AnimatedVisibility(
                    visible = isVisible,
                    enter = fadeIn(tween(600)) + scaleIn(tween(600))
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        val infiniteTransition = rememberInfiniteTransition(label = "empty")
                        val floatY by infiniteTransition.animateFloat(
                            initialValue = -8f,
                            targetValue = 8f,
                            animationSpec = infiniteRepeatable(tween(2000, easing = EaseInOutCubic), RepeatMode.Reverse),
                            label = "float"
                        )
                        Box(
                            modifier = Modifier
                                .size(120.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.radialGradient(
                                        colors = listOf(Indigo500.copy(alpha = 0.08f), Color.Transparent)
                                    )
                                )
                                .graphicsLayer { translationY = floatY },
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(88.dp)
                                    .clip(CircleShape)
                                    .background(
                                        Brush.linearGradient(
                                            colors = listOf(Slate100, Color.White)
                                        )
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Filled.AccountBalanceWallet, null, tint = Slate400, modifier = Modifier.size(44.dp))
                            }
                        }
                        Spacer(Modifier.height(28.dp))
                        Text(
                            data?.error ?: "Sin datos",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = Carbon,
                            letterSpacing = (-0.3).sp
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "No hay informacion financiera disponible",
                            fontSize = 14.sp,
                            color = Slate500,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
            return@Scaffold
        }

        LazyColumn(modifier = Modifier.fillMaxSize().padding(padding), contentPadding = PaddingValues(bottom = 32.dp)) {
            item {
                AnimatedVisibility(
                    visible = isVisible,
                    enter = fadeIn(tween(600, delayMillis = 150)) + slideInVertically(tween(600, delayMillis = 150, easing = EaseOutCubic), initialOffsetY = { it / 6 })
                ) {
                    val infiniteTransition = rememberInfiniteTransition(label = "hero")
                    val orbX by infiniteTransition.animateFloat(
                        initialValue = 0f,
                        targetValue = 20f,
                        animationSpec = infiniteRepeatable(tween(4000, easing = EaseInOutCubic), RepeatMode.Reverse),
                        label = "orbX"
                    )
                    val orbY by infiniteTransition.animateFloat(
                        initialValue = 0f,
                        targetValue = 15f,
                        animationSpec = infiniteRepeatable(tween(3500, easing = EaseInOutCubic), RepeatMode.Reverse),
                        label = "orbY"
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(Color(0xFF1E1B4B), Color(0xFF312E81), Color(0xFF4C1D95)),
                                    start = Offset(0f, 0f),
                                    end = Offset(1f, 1f)
                                )
                            )
                            .padding(24.dp)
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            drawCircle(
                                color = Color.White.copy(alpha = 0.04f),
                                radius = size.width * 0.35f,
                                center = Offset(size.width * 0.8f + orbX, size.height * 0.3f + orbY)
                            )
                            drawCircle(
                                color = Color.White.copy(alpha = 0.03f),
                                radius = size.width * 0.22f,
                                center = Offset(size.width * 0.15f - orbX * 0.5f, size.height * 0.7f - orbY * 0.3f)
                            )
                            drawCircle(
                                color = Color.White.copy(alpha = 0.02f),
                                radius = size.width * 0.15f,
                                center = Offset(size.width * 0.5f + orbX * 0.3f, size.height * 0.1f)
                            )
                        }

                        Column {
                            Surface(
                                color = Color.White.copy(alpha = 0.12f),
                                shape = RoundedCornerShape(20.dp),
                                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.2f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    val dotPulse by infiniteTransition.animateFloat(
                                        initialValue = 0.5f,
                                        targetValue = 1f,
                                        animationSpec = infiniteRepeatable(tween(1200), RepeatMode.Reverse),
                                        label = "dotPulse"
                                    )
                                    Box(
                                        modifier = Modifier
                                            .size(6.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFF34D399).copy(alpha = dotPulse))
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Text("Modulo Financiero", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White.copy(alpha = 0.95f))
                                }
                            }
                            Spacer(Modifier.height(16.dp))
                            Text(
                                "AgroWallet",
                                fontSize = 30.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White,
                                letterSpacing = (-0.8).sp
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                "Tu billetera inteligente. Ingresos, comisiones y pagos.",
                                fontSize = 14.sp,
                                color = Color.White.copy(alpha = 0.75f),
                                lineHeight = 20.sp
                            )
                            Spacer(Modifier.height(20.dp))
                            Surface(
                                color = Color.White.copy(alpha = 0.12f),
                                shape = RoundedCornerShape(24.dp),
                                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.2f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.padding(24.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        "Saldo Disponible",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color.White.copy(alpha = 0.7f),
                                        letterSpacing = 0.3.sp
                                    )
                                    Spacer(Modifier.height(8.dp))
                                    Text(
                                        "\$${formatNumF(data.ingresosNetos)}",
                                        fontSize = 36.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color.White,
                                        letterSpacing = (-1.sp)
                                    )
                                    Spacer(Modifier.height(6.dp))
                                    Text(
                                        "Neto despues de comision",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = Color.White.copy(alpha = 0.6f)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            item {
                AnimatedVisibility(
                    visible = isVisible,
                    enter = fadeIn(tween(500, delayMillis = 300)) + slideInVertically(tween(500, delayMillis = 300, easing = EaseOutCubic), initialOffsetY = { it / 4 })
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            PremiumFinKpiCard(
                                label = "Ingresos Brutos",
                                value = "\$${formatNumF(data.ingresosBrutos)}",
                                icon = Icons.Filled.TrendingUp,
                                gradientColors = listOf(Green500, Teal500),
                                accentColor = Green50,
                                modifier = Modifier.weight(1f)
                            )
                            PremiumFinKpiCard(
                                label = "Comision (5%)",
                                value = "\$${formatNumF(data.comisionTotal)}",
                                icon = Icons.Filled.Percent,
                                gradientColors = listOf(Amber500, Color(0xFFF97316)),
                                accentColor = Amber50,
                                modifier = Modifier.weight(1f)
                            )
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            PremiumFinKpiCard(
                                label = "Pagos Pendientes",
                                value = "\$${formatNumF(data.pagoPendiente)}",
                                icon = Icons.Filled.Schedule,
                                gradientColors = listOf(Blue500, Indigo500),
                                accentColor = Blue50,
                                modifier = Modifier.weight(1f)
                            )
                            PremiumFinKpiCard(
                                label = "Transacciones",
                                value = "${data.totalTransacciones}",
                                icon = Icons.Filled.ReceiptLong,
                                gradientColors = listOf(Purple500, Indigo500),
                                accentColor = Purple50,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            val mensual = data.datosMensuales ?: emptyList()
            if (mensual.isNotEmpty()) {
                item {
                    AnimatedVisibility(
                        visible = isVisible,
                        enter = fadeIn(tween(500, delayMillis = 400)) + slideInVertically(tween(500, delayMillis = 400, easing = EaseOutCubic), initialOffsetY = { it / 6 })
                    ) {
                        Surface(
                            color = Color.White,
                            shape = RoundedCornerShape(28.dp),
                            shadowElevation = 0.dp,
                            border = BorderStroke(1.dp, Slate200),
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp)
                        ) {
                            Column(modifier = Modifier.padding(24.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        Modifier.size(46.dp)
                                            .clip(RoundedCornerShape(16.dp))
                                            .background(Brush.linearGradient(listOf(Green500, Teal500))),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Filled.ShowChart, null, tint = Color.White, modifier = Modifier.size(22.dp))
                                    }
                                    Spacer(Modifier.width(14.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("Flujo de Ingresos", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = Carbon, letterSpacing = (-0.3.sp))
                                        Text("Evolucion mensual", fontSize = 12.sp, color = Slate500)
                                    }
                                }
                                Spacer(Modifier.height(20.dp))
                                PremiumFinAreaChart(mensual, Modifier.fillMaxWidth().height(240.dp))
                            }
                        }
                    }
                }
            }

            item {
                AnimatedVisibility(
                    visible = isVisible,
                    enter = fadeIn(tween(500, delayMillis = 500)) + slideInVertically(tween(500, delayMillis = 500, easing = EaseOutCubic), initialOffsetY = { it / 6 })
                ) {
                    Surface(
                        color = Color.White,
                        shape = RoundedCornerShape(28.dp),
                        shadowElevation = 0.dp,
                        border = BorderStroke(1.dp, Slate200),
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp)
                    ) {
                        Column(modifier = Modifier.padding(24.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    Modifier.size(46.dp)
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(Brush.linearGradient(listOf(Indigo500, Purple500))),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Filled.Bolt, null, tint = Color.White, modifier = Modifier.size(22.dp))
                                }
                                Spacer(Modifier.width(14.dp))
                                Text("Acciones Rapidas", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = Carbon, letterSpacing = (-0.3.sp))
                            }
                            Spacer(Modifier.height(16.dp))
                            PremiumDisabledAction("Solicitar Retiro", Icons.Filled.AccountBalance, "Proximamente")
                            Spacer(Modifier.height(10.dp))
                            PremiumDisabledAction("Descargar Factura", Icons.Filled.Description, "Proximamente")
                            Spacer(Modifier.height(10.dp))
                            PremiumDisabledAction("Vincular Cuenta Bancaria", Icons.Filled.AccountBalance, "Proximamente")
                            Spacer(Modifier.height(16.dp))
                            Surface(
                                color = Green50.copy(alpha = 0.5f),
                                shape = RoundedCornerShape(20.dp),
                                border = BorderStroke(1.dp, Green500.copy(alpha = 0.2f))
                            ) {
                                Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.Top) {
                                    Box(
                                        modifier = Modifier
                                            .size(32.dp)
                                            .clip(CircleShape)
                                            .background(
                                                Brush.linearGradient(listOf(Green500, Teal500))
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Filled.Shield, null, tint = Color.White, modifier = Modifier.size(16.dp))
                                    }
                                    Spacer(Modifier.width(12.dp))
                                    Text(
                                        "Tus fondos estan seguros. AgroConecta retiene el pago hasta que el cliente confirme la entrega.",
                                        fontSize = 12.sp,
                                        color = Emerald700,
                                        lineHeight = 18.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }
                }
            }

            val historial = data.historial ?: emptyList()
            item {
                AnimatedVisibility(
                    visible = isVisible,
                    enter = fadeIn(tween(500, delayMillis = 600)) + slideInVertically(tween(500, delayMillis = 600, easing = EaseOutCubic), initialOffsetY = { it / 6 })
                ) {
                    Spacer(Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            Modifier.size(40.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(Brush.linearGradient(listOf(Blue500, Indigo500))),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Filled.SwapHoriz, null, tint = Color.White, modifier = Modifier.size(20.dp))
                        }
                        Spacer(Modifier.width(14.dp))
                        Text(
                            "Historial de Movimientos",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = Carbon,
                            letterSpacing = (-0.3.sp),
                            modifier = Modifier.weight(1f)
                        )
                        Surface(color = Slate100, shape = RoundedCornerShape(12.dp)) {
                            Text(
                                "${historial.size} movimientos",
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Slate500
                            )
                        }
                    }
                }
            }

            if (historial.isEmpty()) {
                item {
                    AnimatedVisibility(
                        visible = isVisible,
                        enter = fadeIn(tween(500, delayMillis = 700)) + scaleIn(tween(500, delayMillis = 700))
                    ) {
                        Surface(
                            color = Color.White,
                            shape = RoundedCornerShape(28.dp),
                            shadowElevation = 0.dp,
                            border = BorderStroke(1.dp, Slate200),
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(48.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                val infiniteTransition = rememberInfiniteTransition(label = "noData")
                                val floatY by infiniteTransition.animateFloat(
                                    initialValue = -6f,
                                    targetValue = 6f,
                                    animationSpec = infiniteRepeatable(tween(2500, easing = EaseInOutCubic), RepeatMode.Reverse),
                                    label = "floatY"
                                )
                                Box(
                                    modifier = Modifier
                                        .size(100.dp)
                                        .clip(CircleShape)
                                        .background(
                                            Brush.radialGradient(
                                                colors = listOf(Indigo500.copy(alpha = 0.06f), Color.Transparent)
                                            )
                                        )
                                        .graphicsLayer { translationY = floatY },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(72.dp)
                                            .clip(CircleShape)
                                            .background(
                                                Brush.linearGradient(
                                                    colors = listOf(Slate100, Color.White)
                                                )
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Filled.AccountBalanceWallet, null, tint = Slate400, modifier = Modifier.size(36.dp))
                                    }
                                }
                                Spacer(Modifier.height(24.dp))
                                Text(
                                    "Sin movimientos aun",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Carbon,
                                    letterSpacing = (-0.3.sp)
                                )
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    "Cuando tus clientes compren, cada movimiento aparecera aqui.",
                                    fontSize = 14.sp,
                                    color = Slate500,
                                    textAlign = TextAlign.Center,
                                    lineHeight = 20.sp
                                )
                            }
                        }
                    }
                }
            } else {
                items(historial) { tx -> PremiumMovimientoCard(tx) }
            }
        }
    }
}

@Composable
private fun PremiumFinKpiCard(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    gradientColors: List<Color>,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        if (isPressed) 0.96f else 1f,
        spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow)
    )

    Surface(
        color = Color.White,
        shape = RoundedCornerShape(24.dp),
        shadowElevation = 0.dp,
        border = BorderStroke(1.dp, Slate200),
        modifier = modifier
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .clickable(interactionSource = interactionSource, indication = null) {}
    ) {
        Box {
            Canvas(modifier = Modifier.matchParentSize()) {
                drawRoundRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(accentColor.copy(alpha = 0.3f), Color.Transparent),
                        startY = 0f,
                        endY = size.height * 0.4f
                    ),
                    cornerRadius = CornerRadius(24.dp.toPx(), 24.dp.toPx())
                )
            }
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Brush.linearGradient(colors = gradientColors)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(icon, null, tint = Color.White, modifier = Modifier.size(20.dp))
                    }
                    Spacer(Modifier.width(10.dp))
                    Text(
                        label,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Slate400,
                        letterSpacing = 0.3.sp
                    )
                }
                Spacer(Modifier.height(10.dp))
                Text(
                    value,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Carbon,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    letterSpacing = (-0.3.sp)
                )
            }
        }
    }
}

@Composable
private fun PremiumDisabledAction(label: String, icon: androidx.compose.ui.graphics.vector.ImageVector, badge: String) {
    Surface(
        color = Slate100.copy(alpha = 0.5f),
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.dp, Slate200)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                Modifier.size(42.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color.White),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = Slate400, modifier = Modifier.size(20.dp))
            }
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(label, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Slate500)
                Text(badge, fontSize = 11.sp, color = Slate400, fontWeight = FontWeight.Medium)
            }
            Surface(
                color = Color.White,
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.dp, Slate200)
            ) {
                Text(
                    "PRONTO",
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = Slate400
                )
            }
        }
    }
}

@Composable
private fun PremiumMovimientoCard(tx: MovimientoTx) {
    val color = when (tx.signo) {
        "+" -> Green500
        "-" -> Amber500
        "x" -> Red500
        else -> Blue500
    }
    val bg = when (tx.signo) {
        "+" -> Green50
        "-" -> Amber50
        "x" -> Red50
        else -> Blue50
    }
    val icon = when (tx.tipo) {
        "INGRESO" -> Icons.Filled.ArrowDownward
        "COMISION" -> Icons.Filled.Percent
        "CANCELADO" -> Icons.Filled.Cancel
        else -> Icons.Filled.HourglassBottom
    }
    val prefix = if (tx.signo == "+") "+" else if (tx.signo == "-") "-" else ""

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        if (isPressed) 0.98f else 1f,
        spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow)
    )

    Surface(
        color = Color.White,
        shape = RoundedCornerShape(24.dp),
        shadowElevation = 0.dp,
        border = BorderStroke(1.dp, Slate200),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 4.dp)
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .clickable(interactionSource = interactionSource, indication = null) {}
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier.size(48.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        Brush.linearGradient(listOf(bg, Color.White))
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = color, modifier = Modifier.size(22.dp))
            }
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    tx.descripcion,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Carbon,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    letterSpacing = (-0.2.sp)
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    tx.fecha,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = Slate400
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    "$prefix\$${tx.monto.toLong()}",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = color,
                    letterSpacing = (-0.2.sp)
                )
                Spacer(Modifier.height(4.dp))
                Surface(
                    color = bg,
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, color.copy(alpha = 0.2f))
                ) {
                    Text(
                        tx.estado,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = color
                    )
                }
            }
        }
    }
}

@Composable
private fun PremiumFinAreaChart(data: List<DatoMensual>, modifier: Modifier = Modifier) {
    val values = data.map { it.total }
    val maxVal = values.maxOrNull()?.coerceAtLeast(1.0) ?: 1.0
    val labels = data.map { it.mes }
    val animP by animateFloatAsState(targetValue = 1f, animationSpec = tween(1400, delayMillis = 300, easing = FastOutSlowInEasing))
    val textColor = android.graphics.Color.parseColor("#94A3B8")
    val valueColor = android.graphics.Color.parseColor("#111827")

    Canvas(modifier = modifier.padding(start = 44.dp, end = 16.dp, bottom = 28.dp, top = 12.dp)) {
        if (values.size < 2 || data.isEmpty()) return@Canvas
        val stepX = size.width / (values.size - 1).coerceAtLeast(1)
        val points = values.mapIndexed { i, v ->
            Offset(i * stepX, size.height - ((v / maxVal) * size.height * 0.82f * animP).toFloat())
        }

        for (i in 0..6 step 1) {
            val y = size.height * (1f - i / 6f)
            drawLine(
                color = Slate100,
                start = Offset(0f, y),
                end = Offset(size.width, y),
                strokeWidth = 1.dp.toPx()
            )
        }

        val areaPath = Path().apply {
            moveTo(points.first().x, size.height)
            lineTo(points.first().x, points.first().y)
            for (i in 1 until points.size) {
                val prev = points[i - 1]
                val curr = points[i]
                val cx = prev.x + (curr.x - prev.x) / 2
                cubicTo(cx, prev.y, cx, curr.y, curr.x, curr.y)
            }
            lineTo(points.last().x, size.height)
            close()
        }
        drawPath(
            areaPath,
            Brush.verticalGradient(
                listOf(
                    Teal500.copy(alpha = 0.25f),
                    Green500.copy(alpha = 0.1f),
                    Color.Transparent
                )
            )
        )

        if (points.size >= 2) {
            val lineP = Path().apply {
                moveTo(points.first().x, points.first().y)
                for (i in 1 until points.size) {
                    val prev = points[i - 1]
                    val curr = points[i]
                    val cx = prev.x + (curr.x - prev.x) / 2
                    cubicTo(cx, prev.y, cx, curr.y, curr.x, curr.y)
                }
            }
            drawPath(
                lineP,
                Brush.horizontalGradient(listOf(Teal500, Green500)),
                style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
            )
        }

        points.forEachIndexed { i, p ->
            drawCircle(Color.White, radius = 5.dp.toPx(), center = p)
            drawCircle(
                brush = Brush.linearGradient(listOf(Teal500, Green500)),
                radius = 3.5.dp.toPx(),
                center = p
            )

            drawContext.canvas.nativeCanvas.drawText(
                labels.getOrNull(i) ?: "",
                p.x,
                size.height + 18.dp.toPx(),
                android.graphics.Paint().apply {
                    color = textColor
                    textSize = 9.sp.toPx()
                    isAntiAlias = true
                    textAlign = android.graphics.Paint.Align.CENTER
                    typeface = android.graphics.Typeface.DEFAULT
                }
            )

            if (animP > 0.9f) {
                val textAlpha = ((animP - 0.9f) / 0.1f).coerceIn(0f, 1f)
                drawContext.canvas.nativeCanvas.drawText(
                    "\$${formatNumF(values.getOrNull(i) ?: 0.0)}",
                    p.x,
                    p.y - 12.dp.toPx(),
                    android.graphics.Paint().apply {
                        color = valueColor
                        textSize = 8.sp.toPx()
                        isAntiAlias = true
                        typeface = android.graphics.Typeface.DEFAULT_BOLD
                        textAlign = android.graphics.Paint.Align.CENTER
                        alpha = (textAlpha * 255).toInt()
                    }
                )
            }
        }
    }
}

private fun formatNumF(value: Double): String {
    val n = value.toLong()
    if (n >= 1000) {
        val parts = mutableListOf<String>(); var num = n
        while (num > 0) { parts.add(0, (num % 1000).toString().padStart(3, '0')); num /= 1000 }
        return parts.joinToString(".")
    }
    return n.toString()
}