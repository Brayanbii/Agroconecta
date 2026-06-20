package com.agroconecta.app.ui.theme

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.agroconecta.app.data.model.GraficoDatos
import com.agroconecta.app.data.model.GraficoVsMercado
import com.agroconecta.app.data.model.ResumenAnaliticas
import com.agroconecta.app.viewmodel.TiendaViewModel

private val Carbon = Color(0xFF111827)
private val Slate400 = Color(0xFF94A3B8)
private val Slate500 = Color(0xFF64748B)
private val Slate300 = Color(0xFFCBD5E1)
private val Slate200 = Color(0xFFE2E8F0)
private val Slate100 = Color(0xFFF1F5F9)
private val AppBackground = Color(0xFFF8FAF9)
private val Emerald = Color(0xFF0E793D)
private val EmeraldDark = Color(0xFF0A5C2E)
private val EmeraldLight = Color(0xFFE8F5E9)
private val Blue500 = Color(0xFF3B82F6)
private val Blue600 = Color(0xFF2563EB)
private val Blue50 = Color(0xFFEFF6FF)
private val Purple500 = Color(0xFF8B5CF6)
private val Purple600 = Color(0xFF7C3AED)
private val Purple50 = Color(0xFFF3E8FF)
private val Amber500 = Color(0xFFF59E0B)
private val Amber50 = Color(0xFFFFFDE7)
private val Green500 = Color(0xFF10B981)
private val Green50 = Color(0xFFECFDF5)
private val Orange500 = Color(0xFFF97316)
private val Rose500 = Color(0xFFEC4899)
private val Indigo500 = Color(0xFF6366F1)
private val Indigo600 = Color(0xFF4F46E5)
private val Teal500 = Color(0xFF14B8A6)
private val Cyan500 = Color(0xFF06B6D4)

private val ChartColors = listOf(
    Color(0xFF6366F1),
    Color(0xFF8B5CF6),
    Color(0xFFEC4899),
    Color(0xFFF59E0B),
    Color(0xFF10B981),
    Color(0xFF06B6D4)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnaliticasScreen(tiendaVM: TiendaViewModel, onNavigateBack: () -> Unit) {
    LaunchedEffect(Unit) { tiendaVM.cargarInformeAnaliticas() }

    val informe = tiendaVM.informeAnaliticas
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
                            .padding(horizontal = 20.dp, vertical = 14.dp),
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
                                "Analiticas de Negocio",
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
                                    "Metricas en tiempo real",
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
                                        colors = listOf(Indigo500, Purple600)
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.AutoMirrored.Filled.ShowChart, null, tint = Color.White, modifier = Modifier.size(20.dp))
                        }
                    }
                }
            }
        }
    ) { padding ->
        if (tiendaVM.analiticasCargando && informe == null) {
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
                                    colors = listOf(Indigo500.copy(alpha = alpha * 0.2f), Purple600.copy(alpha = alpha * 0.1f))
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
                        "Cargando analiticas...",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Slate400
                    )
                }
            }
            return@Scaffold
        }

        if (informe == null || informe.success == false) {
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
                                Icon(Icons.Filled.BarChart, null, tint = Slate400, modifier = Modifier.size(44.dp))
                            }
                        }
                        Spacer(Modifier.height(28.dp))
                        Text(
                            informe?.error ?: "Sin datos disponibles",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = Carbon,
                            letterSpacing = (-0.3).sp
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "Realiza ventas para ver tus metricas",
                            fontSize = 14.sp,
                            color = Slate500,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(bottom = 32.dp)
        ) {
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
                                brush = Brush.linearGradient(
                                    colors = listOf(Color(0xFF1E1B4B), Color(0xFF312E81), Color(0xFF4C1D95)),
                                    start = Offset(0f, 0f),
                                    end = Offset(1f, 1f)
                                )
                            )
                            .padding(horizontal = 24.dp, vertical = 28.dp)
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
                                    Text("Business Intelligence", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White.copy(alpha = 0.95f))
                                }
                            }
                            Spacer(Modifier.height(16.dp))
                            Text(
                                "Analiticas de Negocio",
                                fontSize = 30.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White,
                                letterSpacing = (-0.8).sp
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                "Metricas en tiempo real para escalar tus ventas.",
                                fontSize = 14.sp,
                                color = Color.White.copy(alpha = 0.75f),
                                lineHeight = 20.sp
                            )
                        }
                    }
                }
            }

            item {
                val resumen = informe.resumen ?: ResumenAnaliticas()
                AnimatedVisibility(
                    visible = isVisible,
                    enter = fadeIn(tween(500, delayMillis = 300)) + slideInVertically(tween(500, delayMillis = 300, easing = EaseOutCubic), initialOffsetY = { it / 4 })
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            PremiumKpiCard(
                                label = "Ingresos Totales",
                                value = "\$".plus(formatNumber(resumen.total_ingresos)),
                                icon = Icons.Filled.AttachMoney,
                                gradientColors = listOf(Green500, Teal500),
                                accentColor = Green50,
                                modifier = Modifier.weight(1f)
                            )
                            PremiumKpiCard(
                                label = "Unid. Vendidas",
                                value = "${resumen.total_unidades}",
                                icon = Icons.Filled.Inventory2,
                                gradientColors = listOf(Blue500, Indigo500),
                                accentColor = Blue50,
                                modifier = Modifier.weight(1f)
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            PremiumKpiCard(
                                label = "Catalogo",
                                value = "${resumen.total_productos}",
                                icon = Icons.Filled.Spa,
                                gradientColors = listOf(Purple500, Indigo500),
                                accentColor = Purple50,
                                modifier = Modifier.weight(1f)
                            )
                            PremiumKpiCard(
                                label = "Mejor Mes",
                                value = resumen.mejor_mes,
                                icon = Icons.Filled.CalendarMonth,
                                gradientColors = listOf(Amber500, Orange500),
                                accentColor = Amber50,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            val topProd = informe.grafico_top_productos
            if (topProd != null && topProd.labels?.isNotEmpty() == true) {
                item {
                    AnimatedVisibility(
                        visible = isVisible,
                        enter = fadeIn(tween(500, delayMillis = 400)) + slideInVertically(tween(500, delayMillis = 400, easing = EaseOutCubic), initialOffsetY = { it / 6 })
                    ) {
                        PremiumChartCard(
                            title = "Top Productos",
                            subtitle = "Por Volumen de Ventas",
                            icon = Icons.Filled.BarChart,
                            gradientColors = listOf(Indigo500, Blue500),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp, vertical = 8.dp)
                        ) {
                            PremiumHorizontalBarChart(topProd, Modifier.fillMaxWidth().height((topProd.labels?.size?.times(48)?.coerceAtLeast(200))?.dp ?: 220.dp))
                        }
                    }
                }
            }

            val dist = informe.grafico_distribucion
            if (dist != null && dist.labels?.isNotEmpty() == true) {
                item {
                    AnimatedVisibility(
                        visible = isVisible,
                        enter = fadeIn(tween(500, delayMillis = 500)) + slideInVertically(tween(500, delayMillis = 500, easing = EaseOutCubic), initialOffsetY = { it / 6 })
                    ) {
                        PremiumChartCard(
                            title = "Distribucion de Ingresos",
                            subtitle = "Porcentaje del Total",
                            icon = Icons.Filled.PieChart,
                            gradientColors = listOf(Green500, Teal500),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp, vertical = 8.dp)
                        ) {
                            PremiumDonutChart(dist, Modifier.fillMaxWidth().height(260.dp))
                        }
                    }
                }
            }

            val ingresosMes = informe.grafico_ingresos_mes
            if (ingresosMes != null && ingresosMes.labels?.isNotEmpty() == true) {
                item {
                    AnimatedVisibility(
                        visible = isVisible,
                        enter = fadeIn(tween(500, delayMillis = 600)) + slideInVertically(tween(500, delayMillis = 600, easing = EaseOutCubic), initialOffsetY = { it / 6 })
                    ) {
                        PremiumChartCard(
                            title = "Evolucion de Ingresos",
                            subtitle = "Historico Anual en COP",
                            icon = Icons.AutoMirrored.Filled.ShowChart,
                            gradientColors = listOf(Purple500, Indigo500),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp, vertical = 8.dp)
                        ) {
                            PremiumAreaLineChart(ingresosMes, Modifier.fillMaxWidth().height(240.dp))
                        }
                    }
                }
            }

            val vsMercado = informe.grafico_vs_mercado
            if (vsMercado != null && vsMercado.labels?.isNotEmpty() == true) {
                item {
                    AnimatedVisibility(
                        visible = isVisible,
                        enter = fadeIn(tween(500, delayMillis = 700)) + slideInVertically(tween(500, delayMillis = 700, easing = EaseOutCubic), initialOffsetY = { it / 6 })
                    ) {
                        Surface(
                            color = Color.White,
                            shape = RoundedCornerShape(28.dp),
                            shadowElevation = 0.dp,
                            border = BorderStroke(1.dp, Slate200),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp, vertical = 8.dp)
                        ) {
                            Column(modifier = Modifier.padding(24.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(48.dp)
                                            .clip(RoundedCornerShape(16.dp))
                                            .background(
                                                Brush.linearGradient(
                                                    colors = listOf(Amber500, Orange500)
                                                )
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Filled.Balance, null, tint = Color.White, modifier = Modifier.size(22.dp))
                                    }
                                    Spacer(Modifier.width(14.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            "Competitividad del Mercado",
                                            fontSize = 17.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Carbon,
                                            letterSpacing = (-0.3).sp
                                        )
                                        Text(
                                            "Tu Precio vs Promedio Nacional",
                                            fontSize = 12.sp,
                                            color = Slate500
                                        )
                                    }
                                    Surface(
                                        color = Slate100,
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Text(
                                            "SIPSA-DANE",
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Slate500
                                        )
                                    }
                                }
                                Spacer(Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.End)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(modifier = Modifier.size(10.dp).clip(RoundedCornerShape(3.dp)).background(Green500))
                                        Spacer(Modifier.width(6.dp))
                                        Text("Tu Precio", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Slate500)
                                    }
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(modifier = Modifier.size(10.dp).clip(RoundedCornerShape(3.dp)).background(Blue500))
                                        Spacer(Modifier.width(6.dp))
                                        Text("Mercado SIPSA", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Slate500)
                                    }
                                }
                                Spacer(Modifier.height(16.dp))
                                PremiumGroupedBarChart(vsMercado, Modifier.fillMaxWidth().height(240.dp))
                            }
                        }
                    }
                }
            }

            if (topProd == null && dist == null && ingresosMes == null && vsMercado == null) {
                item {
                    AnimatedVisibility(
                        visible = isVisible,
                        enter = fadeIn(tween(500, delayMillis = 400)) + scaleIn(tween(500, delayMillis = 400))
                    ) {
                        Surface(
                            color = Color.White,
                            shape = RoundedCornerShape(28.dp),
                            shadowElevation = 0.dp,
                            border = BorderStroke(1.dp, Slate200),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp, vertical = 24.dp)
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
                                        Icon(Icons.Filled.BarChart, null, tint = Slate400, modifier = Modifier.size(36.dp))
                                    }
                                }
                                Spacer(Modifier.height(24.dp))
                                Text(
                                    "Datos insuficientes",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Carbon,
                                    letterSpacing = (-0.3).sp
                                )
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    "Necesitas ventas registradas para ver graficos.",
                                    fontSize = 14.sp,
                                    color = Slate500,
                                    textAlign = TextAlign.Center,
                                    lineHeight = 20.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PremiumKpiCard(
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
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(
                            Brush.linearGradient(colors = gradientColors)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, null, tint = Color.White, modifier = Modifier.size(22.dp))
                }
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(
                        label,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Slate400,
                        letterSpacing = 0.3.sp
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        value,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Carbon,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        letterSpacing = (-0.3).sp
                    )
                }
            }
        }
    }
}

@Composable
private fun PremiumChartCard(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    gradientColors: List<Color>,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Surface(
        color = Color.White,
        shape = RoundedCornerShape(28.dp),
        shadowElevation = 0.dp,
        border = BorderStroke(1.dp, Slate200),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            Brush.linearGradient(colors = gradientColors)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, null, tint = Color.White, modifier = Modifier.size(22.dp))
                }
                Spacer(Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        title,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = Carbon,
                        letterSpacing = (-0.3).sp
                    )
                    Text(
                        subtitle,
                        fontSize = 12.sp,
                        color = Slate500
                    )
                }
            }
            Spacer(Modifier.height(20.dp))
            content()
        }
    }
}

@Composable
private fun PremiumHorizontalBarChart(data: GraficoDatos, modifier: Modifier = Modifier) {
    val labels = data.labels ?: emptyList()
    val seriesData = data.series?.firstOrNull()?.data ?: emptyList()
    val maxVal = seriesData.maxOrNull()?.coerceAtLeast(1.0) ?: 1.0
    val textColor = android.graphics.Color.parseColor("#64748B")
    val valueColor = android.graphics.Color.parseColor("#111827")

    val animProgress by animateFloatAsState(targetValue = 1f, animationSpec = tween(1200, delayMillis = 300, easing = FastOutSlowInEasing))

    val barColors = listOf(
        listOf(Color(0xFF6366F1), Color(0xFF818CF8)),
        listOf(Color(0xFF8B5CF6), Color(0xFFA78BFA)),
        listOf(Color(0xFFEC4899), Color(0xFFF472B6)),
        listOf(Color(0xFF10B981), Color(0xFF34D399)),
        listOf(Color(0xFFF59E0B), Color(0xFFFBBF24)),
        listOf(Color(0xFF06B6D4), Color(0xFF22D3EE))
    )

    Canvas(modifier = modifier.padding(start = 74.dp, end = 56.dp, top = 4.dp, bottom = 4.dp)) {
        val barHeight = size.height / labels.size
        val barThickness = barHeight * 0.5f
        val gap = (barHeight - barThickness) / 2f

        labels.forEachIndexed { index, label ->
            val valD = seriesData.getOrNull(index) ?: 0.0
            val barWidth = ((valD / maxVal) * size.width * animProgress).toFloat()
            val top = index * barHeight + gap
            val colors = barColors[index % barColors.size]

            drawRoundRect(
                color = Slate100,
                topLeft = Offset(0f, top),
                size = Size(size.width, barThickness),
                cornerRadius = CornerRadius(8f, 8f)
            )

            drawRoundRect(
                brush = Brush.horizontalGradient(colors = colors),
                topLeft = Offset(0f, top),
                size = Size(barWidth.coerceAtLeast(4f), barThickness),
                cornerRadius = CornerRadius(8f, 8f)
            )

            drawContext.canvas.nativeCanvas.drawText(
                label,
                -74.dp.toPx() + 4f,
                top + barThickness / 2 + 5f,
                android.graphics.Paint().apply {
                    color = textColor
                    textSize = 11.sp.toPx()
                    isAntiAlias = true
                    typeface = android.graphics.Typeface.DEFAULT_BOLD
                }
            )

            if (animProgress > 0.8f) {
                val textAlpha = ((animProgress - 0.8f) / 0.2f).coerceIn(0f, 1f)
                drawContext.canvas.nativeCanvas.drawText(
                    "${valD.toInt()} uds",
                    barWidth + 8f,
                    top + barThickness / 2 + 5f,
                    android.graphics.Paint().apply {
                        color = valueColor
                        textSize = 10.sp.toPx()
                        isAntiAlias = true
                        typeface = android.graphics.Typeface.DEFAULT_BOLD
                        alpha = (textAlpha * 255).toInt()
                    }
                )
            }
        }
    }
}

@Composable
private fun PremiumDonutChart(data: GraficoDatos, modifier: Modifier = Modifier) {
    val labels = data.labels ?: emptyList()
    val seriesData = data.series?.firstOrNull()?.data ?: emptyList()
    val total = seriesData.sum().coerceAtLeast(1.0)

    val animProgress by animateFloatAsState(targetValue = 1f, animationSpec = tween(1200, delayMillis = 300, easing = FastOutSlowInEasing))

    Box(modifier = modifier) {
        Canvas(modifier = Modifier.fillMaxSize().padding(8.dp)) {
            val radius = minOf(size.width, size.height) / 2f * 0.58f
            val center = Offset(size.width / 2f, size.height / 2f)
            val strokeWidth = radius * 0.42f
            var currentAngle = -90f

            seriesData.forEachIndexed { index, value ->
                val sweep = ((value / total) * 360f * animProgress).toFloat()
                if (sweep > 0.5f) {
                    drawArc(
                        color = ChartColors[index % ChartColors.size],
                        startAngle = currentAngle + 1f,
                        sweepAngle = (sweep - 2f).coerceAtLeast(0.5f),
                        useCenter = false,
                        topLeft = Offset(center.x - radius, center.y - radius),
                        size = Size(radius * 2, radius * 2),
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )
                }
                currentAngle += sweep
            }
        }

        Box(
            modifier = Modifier.align(Alignment.Center),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    "Total",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Slate400
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    "\$" + formatNumber(total),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Carbon,
                    letterSpacing = (-0.3).sp
                )
            }
        }

        Column(modifier = Modifier.align(Alignment.BottomStart).padding(start = 4.dp, bottom = 4.dp)) {
            labels.take(6).forEachIndexed { index, label ->
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 2.dp)) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(ChartColors[index % ChartColors.size])
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(label.take(14), fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = Carbon)
                    Spacer(Modifier.width(4.dp))
                    val pct = if (total > 0) String.format("%.1f%%", (seriesData.getOrNull(index) ?: 0.0) / total * 100) else "0%"
                    Text(pct, fontSize = 9.sp, color = Slate400, fontWeight = FontWeight.Medium)
                }
            }
        }
    }
}

@Composable
private fun PremiumAreaLineChart(data: GraficoDatos, modifier: Modifier = Modifier) {
    val labels = data.labels ?: emptyList()
    val seriesData = data.series?.firstOrNull()?.data ?: emptyList()
    val maxVal = seriesData.maxOrNull()?.coerceAtLeast(1.0) ?: 1.0

    val animProgress by animateFloatAsState(targetValue = 1f, animationSpec = tween(1400, delayMillis = 300, easing = FastOutSlowInEasing))
    val textColor = android.graphics.Color.parseColor("#94A3B8")
    val valueColor = android.graphics.Color.parseColor("#111827")

    Canvas(modifier = modifier.padding(start = 44.dp, end = 16.dp, bottom = 28.dp, top = 12.dp)) {
        if (seriesData.size < 2) return@Canvas

        val stepX = size.width / (seriesData.size - 1).coerceAtLeast(1)
        val points = seriesData.mapIndexed { index, value ->
            val x = index * stepX
            val y = size.height - ((value / maxVal) * size.height * 0.82f * animProgress).toFloat()
            Offset(x, y)
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

        if (points.size >= 2) {
            val areaPath = Path().apply {
                moveTo(points.first().x, size.height)
                lineTo(points.first().x, points.first().y)
                for (i in 1 until points.size) {
                    val prev = points[i - 1]
                    val curr = points[i]
                    val cx1 = prev.x + (curr.x - prev.x) / 2
                    cubicTo(cx1, prev.y, cx1, curr.y, curr.x, curr.y)
                }
                lineTo(points.last().x, size.height)
                close()
            }
            drawPath(
                path = areaPath,
                brush = Brush.verticalGradient(
                    listOf(
                        Indigo500.copy(alpha = 0.2f),
                        Purple500.copy(alpha = 0.08f),
                        Color.Transparent
                    )
                )
            )
        }

        if (points.size >= 2) {
            val linePath = Path().apply {
                moveTo(points.first().x, points.first().y)
                for (i in 1 until points.size) {
                    val prev = points[i - 1]
                    val curr = points[i]
                    val cx1 = prev.x + (curr.x - prev.x) / 2
                    cubicTo(cx1, prev.y, cx1, curr.y, curr.x, curr.y)
                }
            }
            drawPath(
                path = linePath,
                brush = Brush.horizontalGradient(listOf(Indigo500, Purple500)),
                style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
            )
        }

        points.forEachIndexed { index, point ->
            drawCircle(color = Color.White, radius = 5.dp.toPx(), center = point)
            drawCircle(
                brush = Brush.linearGradient(listOf(Indigo500, Purple500)),
                radius = 3.5.dp.toPx(),
                center = point
            )

            if (index % 2 == 0 || index == points.size - 1) {
                drawContext.canvas.nativeCanvas.drawText(
                    labels.getOrNull(index) ?: "",
                    point.x,
                    size.height + 18.dp.toPx(),
                    android.graphics.Paint().apply {
                        color = textColor
                        textSize = 9.sp.toPx()
                        isAntiAlias = true
                        textAlign = android.graphics.Paint.Align.CENTER
                        typeface = android.graphics.Typeface.DEFAULT
                    }
                )
            }

            if (animProgress > 0.9f) {
                val textAlpha = ((animProgress - 0.9f) / 0.1f).coerceIn(0f, 1f)
                drawContext.canvas.nativeCanvas.drawText(
                    "\$${formatNumber(seriesData.getOrNull(index) ?: 0.0)}",
                    point.x,
                    point.y - 12.dp.toPx(),
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

@Composable
private fun PremiumGroupedBarChart(data: GraficoVsMercado, modifier: Modifier = Modifier) {
    val labels = data.labels ?: emptyList()
    val series1 = data.series?.getOrNull(0)?.data ?: emptyList()
    val series2 = data.series?.getOrNull(1)?.data ?: emptyList()
    val allValues = series1 + series2
    val maxVal = allValues.maxOrNull()?.coerceAtLeast(1.0) ?: 1.0

    val animProgress by animateFloatAsState(targetValue = 1f, animationSpec = tween(1200, delayMillis = 300, easing = FastOutSlowInEasing))
    val textColor = android.graphics.Color.parseColor("#94A3B8")

    Canvas(modifier = modifier.padding(start = 16.dp, end = 16.dp, bottom = 28.dp, top = 8.dp)) {
        if (labels.isEmpty()) return@Canvas

        val groupWidth = size.width / labels.size
        val barWidth = groupWidth * 0.28f

        labels.forEachIndexed { index, label ->
            val v1 = series1.getOrNull(index) ?: 0.0
            val v2 = series2.getOrNull(index) ?: 0.0
            val groupCenterX = index * groupWidth + groupWidth / 2
            val barH1 = ((v1 / maxVal) * size.height * 0.78f * animProgress).toFloat().coerceAtLeast(1f)
            val barH2 = ((v2 / maxVal) * size.height * 0.78f * animProgress).toFloat().coerceAtLeast(1f)

            drawRoundRect(
                brush = Brush.verticalGradient(listOf(Green500, Color(0xFF059669))),
                topLeft = Offset(groupCenterX - barWidth - 2.dp.toPx(), size.height - barH1),
                size = Size(barWidth, barH1),
                cornerRadius = CornerRadius(6f, 6f)
            )
            drawRoundRect(
                brush = Brush.verticalGradient(listOf(Blue500, Color(0xFF2563EB))),
                topLeft = Offset(groupCenterX + 2.dp.toPx(), size.height - barH2),
                size = Size(barWidth, barH2),
                cornerRadius = CornerRadius(6f, 6f)
            )

            drawContext.canvas.nativeCanvas.drawText(
                label,
                groupCenterX,
                size.height + 18.dp.toPx(),
                android.graphics.Paint().apply {
                    color = textColor
                    textSize = 9.sp.toPx()
                    isAntiAlias = true
                    textAlign = android.graphics.Paint.Align.CENTER
                    typeface = android.graphics.Typeface.DEFAULT_BOLD
                }
            )

            if (animProgress > 0.85f) {
                val textAlpha = ((animProgress - 0.85f) / 0.15f).coerceIn(0f, 1f)
                drawContext.canvas.nativeCanvas.drawText(
                    "\$${v1.toInt()}",
                    groupCenterX - barWidth / 2 - 2.dp.toPx(),
                    size.height - barH1 - 6f,
                    android.graphics.Paint().apply {
                        color = android.graphics.Color.parseColor("#059669")
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

private fun formatNumber(value: Double): String {
    val integerPart = value.toLong()
    val parts = mutableListOf<String>()
    var num = integerPart
    if (num == 0L) return "0"
    while (num > 0) {
        parts.add(0, (num % 1000).toString().padStart(if (parts.isEmpty()) 0 else 3, '0'))
        num /= 1000
    }
    return parts.joinToString(".")
}