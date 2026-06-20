package com.agroconecta.app.ui.theme

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.agroconecta.app.data.model.UsuarioInfo
import com.agroconecta.app.ui.components.PullToRefreshWrapper
import com.agroconecta.app.viewmodel.AuthViewModel
import com.agroconecta.app.viewmodel.TiendaViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private val PrimaryGreen = Color(0xFF10B981)
private val PrimaryDark = Color(0xFF059669)
private val PrimaryLight = Color(0xFFD1FAE5)
private val Background = Color(0xFFF8FAF9)
private val Surface = Color.White
private val TextPrimary = Color(0xFF111827)
private val TextSecondary = Color(0xFF6B7280)
private val TextTertiary = Color(0xFF9CA3AF)
private val Border = Color(0xFFE5E7EB)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CampesinoPanel(
    usuario: UsuarioInfo,
    viewModel: AuthViewModel,
    tiendaVM: TiendaViewModel,
    onLogout: () -> Unit,
    onNavigate: (String) -> Unit
) {
    var isVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { isVisible = true }
    var isRefreshing by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    fun doRefresh() {
        if (isRefreshing) return
        isRefreshing = true
        scope.launch {
            viewModel.cargarProductosDeXampp()
            tiendaVM.refrescarTienda()
            delay(400)
            isRefreshing = false
        }
    }

    val listState = rememberLazyListState()
    val isAtTop by remember { derivedStateOf { listState.firstVisibleItemIndex == 0 && listState.firstVisibleItemScrollOffset == 0 } }

    Scaffold(
        containerColor = Background,
        topBar = {
            AnimatedVisibility(
                visible = isVisible,
                enter = fadeIn(tween(400)) + slideInVertically(tween(400, easing = EaseOutCubic), initialOffsetY = { -it / 4 })
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                ) {
                    // Header con curva decorativa
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .background(
                                brush = Brush.linearGradient(
                                    colors = listOf(PrimaryGreen, PrimaryDark),
                                    start = Offset(0f, 0f),
                                    end = Offset(1f, 1f)
                                )
                            )
                    ) {
                        // Círculos decorativos
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

                        // Contenido del header
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 24.dp, vertical = 24.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(52.dp)
                                            .clip(CircleShape)
                                            .background(
                                                Brush.linearGradient(
                                                    colors = listOf(Color.White.copy(alpha = 0.3f), Color.White.copy(alpha = 0.15f))
                                                )
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            usuario.userName.take(1).uppercase(),
                                            fontSize = 22.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    }
                                    Spacer(Modifier.width(14.dp))
                                    Column {
                                        Text(
                                            "Buenos días,",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = Color.White.copy(alpha = 0.8f)
                                        )
                                        Spacer(Modifier.height(2.dp))
                                        Text(
                                            usuario.userName,
                                            fontSize = 22.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White,
                                            letterSpacing = (-0.3).sp
                                        )
                                    }
                                }
                                Spacer(Modifier.weight(1f))
                                val dotColor = if (tiendaVM.conectado) Color(0xFF22C55E) else Color(0xFFEF4444)
                                Box(modifier = Modifier.size(32.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.15f)), contentAlignment = Alignment.Center) {
                                    Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(dotColor))
                                }
                            }

                            Spacer(Modifier.height(20.dp))

                            // Card de estado
                            Surface(
                                color = Color.White.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Icon(
                                        Icons.Filled.Verified,
                                        null,
                                        tint = Color(0xFF34D399),
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            "Productor verificado",
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                        Text(
                                            "Tu finca está activa y recibiendo pedidos",
                                            fontSize = 11.sp,
                                            color = Color.White.copy(alpha = 0.8f)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Curva inferior
                    Canvas(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(30.dp)
                            .align(Alignment.BottomCenter)
                    ) {
                        val path = Path().apply {
                            moveTo(0f, 0f)
                            quadraticBezierTo(size.width / 2, -20.dp.toPx(), size.width, 0f)
                            lineTo(size.width, size.height)
                            lineTo(0f, size.height)
                            close()
                        }
                        drawPath(path, color = Background, style = Fill)
                    }
                }
            }
        }
    ) { padding ->
        PullToRefreshWrapper(
            isRefreshing = isRefreshing,
            onRefresh = { doRefresh() },
            isAtTop = isAtTop,
            modifier = Modifier.fillMaxSize().padding(padding)
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 24.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
            // Stats row
            item {
                AnimatedVisibility(
                    visible = isVisible,
                    enter = fadeIn(tween(500, delayMillis = 200)) + slideInVertically(tween(500, delayMillis = 200, easing = EaseOutCubic), initialOffsetY = { it / 3 })
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        PremiumStatCard(
                            value = "${viewModel.listaProductosReales.size}",
                            label = "Productos",
                            icon = Icons.Filled.Inventory2,
                            gradientColors = listOf(Color(0xFF10B981), Color(0xFF059669)),
                            modifier = Modifier.weight(1f)
                        )
                        PremiumStatCard(
                            value = "0",
                            label = "Pedidos",
                            icon = Icons.Filled.LocalShipping,
                            gradientColors = listOf(Color(0xFF3B82F6), Color(0xFF2563EB)),
                            modifier = Modifier.weight(1f)
                        )
                        PremiumStatCard(
                            value = "Nuevo",
                            label = "Rating",
                            icon = Icons.Filled.Star,
                            gradientColors = listOf(Color(0xFFF59E0B), Color(0xFFD97706)),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Quick Actions
            item {
                AnimatedVisibility(
                    visible = isVisible,
                    enter = fadeIn(tween(500, delayMillis = 300)) + slideInVertically(tween(500, delayMillis = 300, easing = EaseOutCubic), initialOffsetY = { it / 3 })
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            "Acciones rápidas",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            letterSpacing = (-0.3).sp
                        )
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            QuickActionButton(
                                title = "Nuevo Producto",
                                icon = Icons.Filled.AddCircle,
                                gradientColors = listOf(Color(0xFFF97316), Color(0xFFEA580C)),
                                onClick = { onNavigate("form") },
                                modifier = Modifier.weight(1f)
                            )
                            QuickActionButton(
                                title = "Ver SIPSA",
                                icon = Icons.Filled.QueryStats,
                                gradientColors = listOf(Color(0xFFF59E0B), Color(0xFFD97706)),
                                onClick = { onNavigate("sipsa") },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            // Gestión Section
            item {
                AnimatedVisibility(
                    visible = isVisible,
                    enter = fadeIn(tween(500, delayMillis = 400)) + slideInVertically(tween(500, delayMillis = 400, easing = EaseOutCubic), initialOffsetY = { it / 3 })
                ) {
                    PremiumSectionHeader(
                        title = "Gestión",
                        subtitle = "Administra tu operación diaria",
                        icon = Icons.Filled.Settings
                    )
                }
            }

            val gestionModules = listOf(
                Module("productos", "Mis Productos", Icons.Filled.Inventory2, "Administra", PrimaryGreen),
                Module("inventario", "Inventario", Icons.Filled.Warehouse, "Gestiona stock", Color(0xFF3B82F6)),
                Module("pedidos", "Pedidos", Icons.Filled.ReceiptLong, "Gestiona órdenes", Color(0xFF8B5CF6)),
                Module("form", "Publicar", Icons.Filled.AddCircle, "Nueva cosecha", Color(0xFFF97316))
            )

            val gestionRows = gestionModules.chunked(2)
            gestionRows.forEachIndexed { rowIndex, row ->
                item {
                    AnimatedVisibility(
                        visible = isVisible,
                        enter = fadeIn(tween(500, delayMillis = 500 + rowIndex * 100)) + 
                               slideInVertically(tween(500, delayMillis = 500 + rowIndex * 100, easing = EaseOutCubic), initialOffsetY = { it / 3 })
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            row.forEach { module ->
                                PremiumModuleCard(
                                    module = module,
                                    onClick = { onNavigate(module.route) },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            if (row.size == 1) {
                                Spacer(Modifier.weight(1f))
                            }
                        }
                    }
                }
            }

            // Finanzas Section
            item {
                AnimatedVisibility(
                    visible = isVisible,
                    enter = fadeIn(tween(500, delayMillis = 700)) + slideInVertically(tween(500, delayMillis = 700, easing = EaseOutCubic), initialOffsetY = { it / 3 })
                ) {
                    PremiumSectionHeader(
                        title = "Finanzas & Mercado",
                        subtitle = "Precios, ventas y analíticas",
                        icon = Icons.Filled.AccountBalanceWallet
                    )
                }
            }

            val finanzasModules = listOf(
                Module("sipsa", "SIPSA", Icons.Filled.QueryStats, "Precios mercado", Color(0xFFF59E0B)),
                Module("analiticas", "Analíticas", Icons.Filled.BarChart, "Ventas", Color(0xFF0284C7)),
                Module("finanzas", "AgroWallet", Icons.Filled.AccountBalanceWallet, "Finanzas", Color(0xFF0F766E))
            )

            val finanzasRows = finanzasModules.chunked(2)
            finanzasRows.forEachIndexed { rowIndex, row ->
                item {
                    AnimatedVisibility(
                        visible = isVisible,
                        enter = fadeIn(tween(500, delayMillis = 800 + rowIndex * 100)) + 
                               slideInVertically(tween(500, delayMillis = 800 + rowIndex * 100, easing = EaseOutCubic), initialOffsetY = { it / 3 })
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            row.forEach { module ->
                                PremiumModuleCard(
                                    module = module,
                                    onClick = { onNavigate(module.route) },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            if (row.size == 1) {
                                Spacer(Modifier.weight(1f))
                            }
                        }
                    }
                }
            }

            // Información Section
            item {
                AnimatedVisibility(
                    visible = isVisible,
                    enter = fadeIn(tween(500, delayMillis = 1000)) + slideInVertically(tween(500, delayMillis = 1000, easing = EaseOutCubic), initialOffsetY = { it / 3 })
                ) {
                    PremiumSectionHeader(
                        title = "Información",
                        subtitle = "Tu reputación y datos",
                        icon = Icons.Filled.Info
                    )
                }
            }

            val infoModules = listOf(
                Module("reputacion", "Reputación", Icons.Filled.Reviews, "Reseñas", Color(0xFFEC4899)),
                Module("logistica", "Logística", Icons.Filled.Route, "Entregas", Color(0xFFBE185D)),
                Module("perfil", "Mi Perfil", Icons.Filled.Person, "Datos", Color(0xFF334155)),
                Module("soporte", "Soporte", Icons.Filled.HeadsetMic, "Ayuda", Color(0xFFF97316))
            )

            val infoRows = infoModules.chunked(2)
            infoRows.forEachIndexed { rowIndex, row ->
                item {
                    AnimatedVisibility(
                        visible = isVisible,
                        enter = fadeIn(tween(500, delayMillis = 1100 + rowIndex * 100)) + 
                               slideInVertically(tween(500, delayMillis = 1100 + rowIndex * 100, easing = EaseOutCubic), initialOffsetY = { it / 3 })
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            row.forEach { module ->
                                PremiumModuleCard(
                                    module = module,
                                    onClick = { onNavigate(module.route) },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            if (row.size == 1) {
                                Spacer(Modifier.weight(1f))
                            }
                        }
                    }
                }
            }

            // Logout button
            item {
                AnimatedVisibility(
                    visible = isVisible,
                    enter = fadeIn(tween(500, delayMillis = 1400)) + slideInVertically(tween(500, delayMillis = 1400, easing = EaseOutCubic), initialOffsetY = { it / 3 })
                ) {
                    Surface(
                        color = Surface,
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, Color(0xFFFEE2E2)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .clickable { onLogout() }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 20.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                Icons.Filled.Logout,
                                null,
                                tint = Color(0xFFEF4444),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(Modifier.width(10.dp))
                            Text(
                                "Cerrar sesión",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFFEF4444)
                            )
                        }
                    }
                }
            }

            item { Spacer(Modifier.height(40.dp)) }
        }
    }
    }
}

@Composable
fun PremiumStatCard(
    value: String,
    label: String,
    icon: ImageVector,
    gradientColors: List<Color>,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        if (isPressed) 0.96f else 1f,
        spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow)
    )

    Surface(
        color = Surface,
        shape = RoundedCornerShape(20.dp),
        shadowElevation = 4.dp,
        modifier = modifier
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .clickable(interactionSource = interactionSource, indication = null) {}
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            colors = gradientColors
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    icon,
                    null,
                    tint = Color.White,
                    modifier = Modifier.size(22.dp)
                )
            }
            
            Text(
                value,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                letterSpacing = (-0.5).sp
            )
            
            Text(
                label,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = TextSecondary
            )
        }
    }
}

@Composable
fun QuickActionButton(
    title: String,
    icon: ImageVector,
    gradientColors: List<Color>,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        if (isPressed) 0.95f else 1f,
        spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow)
    )

    Surface(
        shape = RoundedCornerShape(20.dp),
        shadowElevation = 4.dp,
        modifier = modifier
            .height(80.dp)
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.linearGradient(
                        colors = gradientColors
                    )
                )
                .padding(horizontal = 16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        icon,
                        null,
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
                
                Text(
                    title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
fun PremiumSectionHeader(
    title: String,
    subtitle: String,
    icon: ImageVector
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFFE5E7EB)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                icon,
                null,
                tint = TextSecondary,
                modifier = Modifier.size(18.dp)
            )
        }
        
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                title,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                letterSpacing = (-0.3).sp
            )
            Text(
                subtitle,
                fontSize = 12.sp,
                color = TextSecondary
            )
        }
    }
}

@Composable
fun PremiumModuleCard(
    module: Module,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        if (isPressed) 0.95f else 1f,
        spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow)
    )

    Surface(
        color = Surface,
        shape = RoundedCornerShape(22.dp),
        shadowElevation = 4.dp,
        modifier = modifier
            .height(130.dp)
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            module.color.copy(alpha = 0.08f),
                            module.color.copy(alpha = 0.03f)
                        ),
                        start = Offset(0f, 0f),
                        end = Offset(1f, 1f)
                    )
                )
                .padding(14.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(
                            Brush.linearGradient(
                                colors = listOf(module.color, module.color.copy(alpha = 0.8f))
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        module.icon,
                        null,
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }
                
                Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Text(
                        module.title,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        maxLines = 1
                    )
                    Text(
                        module.subtitle,
                        fontSize = 10.sp,
                        color = TextSecondary,
                        maxLines = 1
                    )
                }
            }
        }
    }
}

data class Module(
    val route: String,
    val title: String,
    val icon: ImageVector,
    val subtitle: String,
    val color: Color
)
