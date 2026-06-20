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
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
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
import com.agroconecta.app.data.model.ProductoPodium
import com.agroconecta.app.data.model.ReputacionResponse
import com.agroconecta.app.data.model.ResenaReputacion
import com.agroconecta.app.viewmodel.TiendaViewModel

private val Carbon = Color(0xFF111827)
private val Slate400 = Color(0xFF94A3B8)
private val Slate500 = Color(0xFF64748B)
private val Slate300 = Color(0xFFCBD5E1)
private val Slate200 = Color(0xFFE2E8F0)
private val Slate100 = Color(0xFFF1F5F9)
private val AppBackground = Color(0xFFF8FAF9)
private val Emerald = Color(0xFF0E793D)
private val EmeraldLight = Color(0xFFE8F5E9)
private val Green500 = Color(0xFF22C55E)
private val Green50 = Color(0xFFF0FDF4)
private val Blue500 = Color(0xFF3B82F6)
private val Blue50 = Color(0xFFEFF6FF)
private val Amber500 = Color(0xFFF59E0B)
private val Amber50 = Color(0xFFFFFDE7)
private val Rose500 = Color(0xFFF43F5E)
private val Red500 = Color(0xFFEF4444)
private val Purple500 = Color(0xFFA855F7)
private val Fuchsia500 = Color(0xFFD946EF)
private val Indigo500 = Color(0xFF6366F1)
private val Indigo600 = Color(0xFF4F46E5)
private val Teal500 = Color(0xFF14B8A6)

private val BASE_IMAGE_URL get() = com.agroconecta.app.data.api.ApiConfig.IMAGES_URL

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReputacionScreen(tiendaVM: TiendaViewModel, onNavigateBack: () -> Unit) {
    LaunchedEffect(Unit) { tiendaVM.cargarReputacion() }

    val data = tiendaVM.reputacionResponse
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
                                "Resenas y Reputacion",
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
                                    "Reputacion activa",
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
                                        colors = listOf(Amber500, Color(0xFFF97316))
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Filled.Star, null, tint = Color.White, modifier = Modifier.size(20.dp))
                        }
                    }
                }
            }
        }
    ) { padding ->
        if (tiendaVM.reputacionCargando && data == null) {
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
                                    colors = listOf(Amber500.copy(alpha = alpha * 0.2f), Color(0xFFF97316).copy(alpha = alpha * 0.1f))
                                )
                            )
                            .graphicsLayer { scaleX = scale; scaleY = scale },
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            color = Amber500,
                            strokeWidth = 3.dp,
                            modifier = Modifier.size(40.dp)
                        )
                    }
                    Spacer(Modifier.height(20.dp))
                    Text(
                        "Cargando reputacion...",
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
                                        colors = listOf(Amber500.copy(alpha = 0.08f), Color.Transparent)
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
                                Icon(Icons.Filled.Reviews, null, tint = Slate400, modifier = Modifier.size(44.dp))
                            }
                        }
                        Spacer(Modifier.height(28.dp))
                        Text(
                            data?.error ?: "Sin datos disponibles",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = Carbon,
                            letterSpacing = (-0.3).sp
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "No hay informacion de reputacion disponible",
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

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Column(modifier = Modifier.weight(1f)) {
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
                                        Text("Tu Reputacion", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White.copy(alpha = 0.95f))
                                    }
                                }
                                Spacer(Modifier.height(16.dp))
                                Text(
                                    "Resenas y Reputacion",
                                    fontSize = 30.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White,
                                    letterSpacing = (-0.8).sp
                                )
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    "Descubre que dicen tus clientes y haz crecer tu marca.",
                                    fontSize = 14.sp,
                                    color = Color.White.copy(alpha = 0.75f),
                                    lineHeight = 20.sp
                                )
                            }

                            Surface(
                                color = Color.White.copy(alpha = 0.12f),
                                shape = RoundedCornerShape(28.dp),
                                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.2f)),
                                modifier = Modifier.padding(start = 12.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Filled.Star, null, tint = Color(0xFFFACC15), modifier = Modifier.size(24.dp))
                                        Spacer(Modifier.width(6.dp))
                                        Text(
                                            String.format("%.1f", data.calificacionGeneral),
                                            fontSize = 42.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = Color.White,
                                            letterSpacing = (-1.sp)
                                        )
                                    }
                                    Spacer(Modifier.height(4.dp))
                                    Text(
                                        "${data.totalResenas} resenas totales",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = Color.White.copy(alpha = 0.7f)
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
                            PremiumRepKpiCard(
                                label = "Total Resenas",
                                value = "${data.totalResenas}",
                                icon = Icons.AutoMirrored.Filled.Chat,
                                gradientColors = listOf(Blue500, Indigo500),
                                accentColor = Blue50,
                                modifier = Modifier.weight(1f)
                            )
                            PremiumRepKpiCard(
                                label = "% Positivas",
                                value = "${data.porcentajePositivo}%",
                                icon = Icons.Filled.ThumbUp,
                                gradientColors = listOf(Green500, Teal500),
                                accentColor = Green50,
                                modifier = Modifier.weight(1f)
                            )
                            PremiumRepKpiCard(
                                label = "Prod. Estrella",
                                value = data.productoEstrella.take(12),
                                icon = Icons.Filled.EmojiEvents,
                                gradientColors = listOf(Amber500, Color(0xFFF97316)),
                                accentColor = Amber50,
                                modifier = Modifier.weight(1f)
                            )
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            PremiumRepKpiCard(
                                label = "Por Mejorar",
                                value = "${data.oportunidadesCount}",
                                icon = Icons.Filled.Lightbulb,
                                gradientColors = listOf(Rose500, Color(0xFFFB7185)),
                                accentColor = Color(0xFFFFF1F2),
                                modifier = Modifier.weight(1f)
                            )
                            PremiumRepKpiCard(
                                label = "Likes Perfil",
                                value = "${data.likesPerfil}",
                                icon = Icons.Filled.Favorite,
                                gradientColors = listOf(Red500, Rose500),
                                accentColor = Color(0xFFFEF2F2),
                                modifier = Modifier.weight(1f)
                            )
                            PremiumRepKpiCard(
                                label = "Likes Prod.",
                                value = "${data.likesProductos}",
                                icon = Icons.Filled.ShoppingBag,
                                gradientColors = listOf(Purple500, Fuchsia500),
                                accentColor = Color(0xFFFDF4FF),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            val dist = data.distribucion ?: listOf(0, 0, 0, 0, 0)
            val maxDist = dist.maxOrNull()?.coerceAtLeast(1) ?: 1
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
                                        .background(Brush.linearGradient(listOf(Amber500, Color(0xFFF97316)))),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Filled.Star, null, tint = Color.White, modifier = Modifier.size(22.dp))
                                }
                                Spacer(Modifier.width(14.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Distribucion de Estrellas", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = Carbon, letterSpacing = (-0.3.sp))
                                    Text("Calificaciones de clientes", fontSize = 12.sp, color = Slate500)
                                }
                            }
                            Spacer(Modifier.height(20.dp))
                            (5 downTo 1).forEach { star ->
                                val count = dist.getOrNull(5 - star) ?: 0
                                val pct = if (data.totalResenas > 0) (count.toFloat() / data.totalResenas * 100f) else 0f
                                val barColor = when (star) {
                                    5, 4 -> Brush.horizontalGradient(listOf(Color(0xFFF59E0B), Color(0xFFFBBF24)))
                                    3 -> Brush.horizontalGradient(listOf(Color(0xFF94A3B8), Color(0xFFCBD5E1)))
                                    else -> Brush.horizontalGradient(listOf(Color(0xFFF43F5E), Color(0xFFFB7185)))
                                }
                                val animProgress by animateFloatAsState(targetValue = 1f, animationSpec = tween(1000, delayMillis = 300, easing = FastOutSlowInEasing), label = "bar$star")
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 4.dp)) {
                                    Text("$star", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Slate500, modifier = Modifier.width(18.dp), textAlign = TextAlign.End)
                                    Spacer(Modifier.width(6.dp))
                                    Icon(Icons.Filled.Star, null, tint = when { star >= 4 -> Amber500; star == 3 -> Slate300; else -> Rose500 }, modifier = Modifier.size(13.dp))
                                    Spacer(Modifier.width(10.dp))
                                    Box(modifier = Modifier.weight(1f).height(24.dp).clip(RoundedCornerShape(10.dp)).background(Slate100)) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxHeight()
                                                .fillMaxWidth(fraction = (pct / 100f) * animProgress)
                                                .clip(RoundedCornerShape(10.dp))
                                                .background(barColor)
                                        )
                                    }
                                    Spacer(Modifier.width(12.dp))
                                    Text("$count", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Slate500, modifier = Modifier.width(32.dp), textAlign = TextAlign.End)
                                }
                            }
                        }
                    }
                }
            }

            val podium = data.mejoresProductos ?: emptyList()
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
                                        .background(Brush.linearGradient(listOf(Amber500, Color(0xFFF97316)))),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Filled.EmojiEvents, null, tint = Color.White, modifier = Modifier.size(22.dp))
                                }
                                Spacer(Modifier.width(14.dp))
                                Text("Podio de Productos", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = Carbon, letterSpacing = (-0.3.sp))
                            }
                            Spacer(Modifier.height(20.dp))
                            if (podium.isEmpty()) {
                                Text(
                                    "Aun no tienes productos con calificacion alta.",
                                    fontSize = 13.sp,
                                    color = Slate400,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth().padding(24.dp)
                                )
                            } else {
                                Row(
                                    modifier = Modifier.fillMaxWidth().height(220.dp),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.Bottom
                                ) {
                                    if (podium.size > 1) PremiumPodiumItem(podium[1], position = 2, height = 140.dp)
                                    Spacer(Modifier.width(10.dp))
                                    if (podium.isNotEmpty()) PremiumPodiumItem(podium[0], position = 1, height = 180.dp)
                                    Spacer(Modifier.width(10.dp))
                                    if (podium.size > 2) PremiumPodiumItem(podium[2], position = 3, height = 110.dp)
                                }
                            }
                        }
                    }
                }
            }

            val buenas = data.buenasResenas ?: emptyList()
            item {
                AnimatedVisibility(
                    visible = isVisible,
                    enter = fadeIn(tween(500, delayMillis = 600)) + slideInVertically(tween(500, delayMillis = 600, easing = EaseOutCubic), initialOffsetY = { it / 6 })
                ) {
                    PremiumSectionHeader("Les encanta", Icons.Filled.Favorite, Green50, Green500, "${buenas.size} positivas")
                }
            }
            if (buenas.isEmpty()) {
                item { PremiumEmptySection("Sin resenas positivas aun.", Icons.Filled.ChatBubble) }
            } else {
                items(buenas.take(10)) { resena -> PremiumResenaCard(resena, isPositive = true) }
            }

            val oportunidades = data.oportunidadesMejora ?: emptyList()
            item { Spacer(Modifier.height(12.dp)) }
            item {
                AnimatedVisibility(
                    visible = isVisible,
                    enter = fadeIn(tween(500, delayMillis = 700)) + slideInVertically(tween(500, delayMillis = 700, easing = EaseOutCubic), initialOffsetY = { it / 6 })
                ) {
                    PremiumSectionHeader("Puedes mejorar", Icons.Filled.Lightbulb, Blue50, Blue500, "${oportunidades.size} oportunidades")
                }
            }
            if (oportunidades.isEmpty()) {
                item {
                    AnimatedVisibility(
                        visible = isVisible,
                        enter = fadeIn(tween(500, delayMillis = 800)) + scaleIn(tween(500, delayMillis = 800))
                    ) {
                        Surface(
                            color = Color.White,
                            shape = RoundedCornerShape(28.dp),
                            shadowElevation = 0.dp,
                            border = BorderStroke(1.dp, Slate200),
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(36.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                val infiniteTransition = rememberInfiniteTransition(label = "perfect")
                                val floatY by infiniteTransition.animateFloat(
                                    initialValue = -5f,
                                    targetValue = 5f,
                                    animationSpec = infiniteRepeatable(tween(2500, easing = EaseInOutCubic), RepeatMode.Reverse),
                                    label = "floatY"
                                )
                                Box(
                                    modifier = Modifier
                                        .size(80.dp)
                                        .clip(CircleShape)
                                        .background(
                                            Brush.radialGradient(
                                                colors = listOf(Blue500.copy(alpha = 0.08f), Color.Transparent)
                                            )
                                        )
                                        .graphicsLayer { translationY = floatY },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Filled.EmojiEvents, null, tint = Blue500.copy(alpha = 0.6f), modifier = Modifier.size(40.dp))
                                }
                                Spacer(Modifier.height(16.dp))
                                Text("Impecable!", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Carbon, letterSpacing = (-0.3.sp))
                                Spacer(Modifier.height(6.dp))
                                Text("Todos tus clientes estan felices.", fontSize = 14.sp, color = Slate500)
                            }
                        }
                    }
                }
            } else {
                items(oportunidades.take(10)) { resena -> PremiumResenaCard(resena, isPositive = false) }
            }
        }
    }
}

@Composable
private fun PremiumSectionHeader(title: String, icon: androidx.compose.ui.graphics.vector.ImageVector, bg: Color, tint: Color, badge: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier.size(40.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(Brush.linearGradient(listOf(bg, Color.White))),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, tint = tint, modifier = Modifier.size(20.dp))
        }
        Spacer(Modifier.width(14.dp))
        Text(
            title,
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
            color = Carbon,
            letterSpacing = (-0.3.sp),
            modifier = Modifier.weight(1f)
        )
        Surface(
            color = bg,
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, tint.copy(alpha = 0.2f))
        ) {
            Text(
                badge,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = tint
            )
        }
    }
}

@Composable
private fun PremiumEmptySection(text: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Surface(
        color = Color.White,
        shape = RoundedCornerShape(28.dp),
        shadowElevation = 0.dp,
        border = BorderStroke(1.dp, Slate200),
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp)
    ) {
        Column(
            modifier = Modifier.padding(36.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, null, tint = Slate300, modifier = Modifier.size(40.dp))
            Spacer(Modifier.height(10.dp))
            Text(text, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Slate400)
        }
    }
}

@Composable
private fun PremiumRepKpiCard(
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
            Column(
                modifier = Modifier.padding(14.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Brush.linearGradient(colors = gradientColors)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, null, tint = Color.White, modifier = Modifier.size(20.dp))
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
                Text(
                    label,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Slate400,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    letterSpacing = 0.2.sp
                )
            }
        }
    }
}

@Composable
private fun PremiumPodiumItem(product: ProductoPodium, position: Int, height: androidx.compose.ui.unit.Dp) {
    val colors = mapOf(1 to Amber500, 2 to Slate400, 3 to Color(0xFFF97316))
    val bgGradient = mapOf(
        1 to Brush.verticalGradient(listOf(Color(0xFFFEF3C7), Color(0xFFFDE68A))),
        2 to Brush.verticalGradient(listOf(Slate100, Slate200)),
        3 to Brush.verticalGradient(listOf(Color(0xFFFFEDD5), Color(0xFFFED7AA)))
    )
    val borderColor = mapOf(1 to Color(0xFFFBBF24), 2 to Slate300, 3 to Color(0xFFFB923C))
    val crownVisibility = position == 1

    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(95.dp)) {
        if (crownVisibility) {
            val infiniteTransition = rememberInfiniteTransition(label = "crown")
            val scale by infiniteTransition.animateFloat(
                initialValue = 0.95f,
                targetValue = 1.05f,
                animationSpec = infiniteRepeatable(tween(1500, easing = EaseInOutCubic), RepeatMode.Reverse),
                label = "crownScale"
            )
            Icon(
                Icons.Filled.WorkspacePremium,
                null,
                tint = Amber500,
                modifier = Modifier
                    .size(26.dp)
                    .graphicsLayer { scaleX = scale; scaleY = scale }
            )
            Spacer(Modifier.height(4.dp))
        } else {
            Spacer(Modifier.height(30.dp))
        }

        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(Slate100)
                .border(3.dp, borderColor[position] ?: Slate300, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            val imgUrl = product.imagenUrl?.let {
                if (it.isNotBlank() && it != "default.png") {
                    if (it.startsWith("http")) it else BASE_IMAGE_URL + it
                } else null
            }
            if (imgUrl != null) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current).data(imgUrl).crossfade(true).build(),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize().clip(CircleShape)
                )
            } else {
                Icon(Icons.Filled.Eco, null, tint = Emerald, modifier = Modifier.size(26.dp))
            }
        }
        Spacer(Modifier.height(6.dp))
        Text(
            product.nombre.take(10),
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = Carbon,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            letterSpacing = (-0.2.sp)
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.Star, null, tint = Amber500, modifier = Modifier.size(12.dp))
            Spacer(Modifier.width(3.dp))
            Text(
                String.format("%.1f", product.promedioCalificacion),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = if (position == 1) Amber500 else Carbon,
                letterSpacing = (-0.2.sp)
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(height)
                .padding(top = 8.dp)
                .clip(RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp))
                .background(bgGradient[position] ?: Brush.verticalGradient(listOf(Slate100, Slate200)))
                .border(
                    BorderStroke(1.dp, borderColor[position]?.copy(alpha = 0.5f) ?: Slate300),
                    RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp)
                ),
            contentAlignment = Alignment.TopCenter
        ) {
            Text(
                "$position",
                fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold,
                color = (colors[position] ?: Slate400).copy(alpha = 0.4f),
                modifier = Modifier.padding(top = 14.dp)
            )
        }
    }
}

@Composable
private fun PremiumResenaCard(resena: ResenaReputacion, isPositive: Boolean) {
    val accentColor = if (isPositive) Green500 else Blue500
    val accentBg = if (isPositive) Green50 else Blue50
    val accentLight = if (isPositive) Color(0xFFBBF7D0) else Color(0xFFBFDBFE)

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
            .padding(horizontal = 20.dp, vertical = 5.dp)
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .clickable(interactionSource = interactionSource, indication = null) {}
    ) {
        Row(modifier = Modifier.drawWithContent {
            drawRoundRect(
                color = accentColor,
                size = androidx.compose.ui.geometry.Size(4.dp.toPx(), size.height),
                topLeft = androidx.compose.ui.geometry.Offset.Zero,
                cornerRadius = CornerRadius(24.dp.toPx(), 24.dp.toPx())
            )
            drawContent()
        }.padding(18.dp)) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(listOf(accentBg, Color.White))
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            resena.nombreAutor?.firstOrNull()?.uppercase() ?: "A",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = accentColor
                        )
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            resena.nombreAutor ?: "Anonimo",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Carbon,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            letterSpacing = (-0.2.sp)
                        )
                        Text(resena.fecha ?: "", fontSize = 11.sp, color = Slate400, fontWeight = FontWeight.Medium)
                    }
                    Row {
                        repeat(resena.estrellas) {
                            Icon(Icons.Filled.Star, null, tint = Amber500, modifier = Modifier.size(13.dp))
                        }
                    }
                }
                if (!resena.comentario.isNullOrBlank()) {
                    Spacer(Modifier.height(10.dp))
                    Text(
                        "\"${resena.comentario}\"",
                        fontSize = 13.sp,
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                        color = Slate500,
                        lineHeight = 19.sp
                    )
                }
                Spacer(Modifier.height(8.dp))
                Row {
                    if (!isPositive) {
                        Surface(
                            color = Blue50,
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, Blue500.copy(alpha = 0.2f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Filled.Lightbulb, null, tint = Blue500, modifier = Modifier.size(11.dp))
                                Spacer(Modifier.width(5.dp))
                                Text("Oportunidad", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Blue500)
                            }
                        }
                        Spacer(Modifier.width(10.dp))
                    }
                    Surface(
                        color = Slate100,
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, Slate200)
                    ) {
                        Text(
                            resena.nombreProducto?.take(16) ?: "Producto",
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Slate500
                        )
                    }
                }
            }
        }
    }
}