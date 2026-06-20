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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Logout
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.agroconecta.app.data.model.UsuarioInfo

@Composable
fun PerfilScreen(
    usuario: UsuarioInfo,
    onNavigateBack: () -> Unit,
    onLogout: () -> Unit,
    onNavigateToDirecciones: () -> Unit,
    onNavigateToCompras: () -> Unit,
    onNavigateToFavoritos: () -> Unit
) {
    var isVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { isVisible = true }

    Column(
        modifier = Modifier.fillMaxSize().background(Color(0xFFF8FAF9))
    ) {
        // ===== HEADER GLASSMORPHISM =====
        AnimatedVisibility(
            visible = isVisible,
            enter = fadeIn(tween(600)) + slideInVertically(tween(600, easing = EaseOutCubic), initialOffsetY = { -it / 4 })
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                Color(0xFF064E3B),
                                Color(0xFF047857),
                                Color(0xFF059669)
                            ),
                            start = Offset(0f, 0f),
                            end = Offset(1f, 1f)
                        )
                    )
                    .padding(top = 48.dp, bottom = 40.dp, start = 24.dp, end = 24.dp)
            ) {
                // Animated orbs
                val infiniteTransition = rememberInfiniteTransition(label = "header")
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

                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        var backScale by remember { mutableStateOf(1f) }
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.2f))
                                .graphicsLayer { scaleX = backScale; scaleY = backScale }
                                .clickable {
                                    backScale = 0.9f
                                    onNavigateBack()
                                }
                                .padding(12.dp)
                        ) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver", tint = Color.White, modifier = Modifier.size(22.dp))
                        }
                        LaunchedEffect(backScale) {
                            if (backScale != 1f) {
                                kotlinx.coroutines.delay(100)
                                backScale = 1f
                            }
                        }

                        Text(
                            "Mi Perfil",
                            color = Color.White,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = (-0.5.sp)
                        )

                        Box(Modifier.size(46.dp))
                    }

                    Spacer(Modifier.height(32.dp))

                    // Avatar with gradient
                    val avatarScale by infiniteTransition.animateFloat(
                        initialValue = 0.98f,
                        targetValue = 1.02f,
                        animationSpec = infiniteRepeatable(tween(2000, easing = EaseInOutCubic), RepeatMode.Reverse),
                        label = "avatarScale"
                    )

                    Box(
                        modifier = Modifier
                            .size(110.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(Color.White, Color(0xFFECFDF5))
                                )
                            )
                            .padding(4.dp)
                            .graphicsLayer { scaleX = avatarScale; scaleY = avatarScale }
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape)
                                .background(
                                    Brush.linearGradient(
                                        colors = listOf(AgroVerde, AgroVerdeClaro)
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                usuario.userName.take(1).uppercase(),
                                fontSize = 44.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White,
                                letterSpacing = (-1.sp)
                            )
                        }
                    }

                    Spacer(Modifier.height(20.dp))

                    Text(
                        usuario.userName,
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        letterSpacing = (-0.5.sp)
                    )

                    Spacer(Modifier.height(6.dp))

                    Text(
                        usuario.email,
                        fontSize = 14.sp,
                        color = Color.White.copy(alpha = 0.85f),
                        fontWeight = FontWeight.Medium
                    )

                    Spacer(Modifier.height(8.dp))

                    // Status badge
                    Surface(
                        color = Color.White.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(20.dp),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.3f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val pulseAnim by infiniteTransition.animateFloat(
                                initialValue = 0.6f,
                                targetValue = 1f,
                                animationSpec = infiniteRepeatable(tween(1500), RepeatMode.Reverse)
                            )
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF34D399).copy(alpha = pulseAnim))
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                "Cuenta activa",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }

        // ===== CONTENT =====
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ===== MI CUENTA SECTION =====
            item {
                AnimatedVisibility(
                    visible = isVisible,
                    enter = fadeIn(tween(500, delayMillis = 200)) + slideInVertically(tween(500, delayMillis = 200, easing = EaseOutCubic), initialOffsetY = { it / 6 })
                ) {
                    Column {
                        Text(
                            "Mi Cuenta",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F172A),
                            letterSpacing = (-0.3.sp)
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "Gestiona tu informacion personal",
                            fontSize = 13.sp,
                            color = Color(0xFF64748B)
                        )
                    }
                }
            }

            item {
                AnimatedVisibility(
                    visible = isVisible,
                    enter = fadeIn(tween(400, delayMillis = 300)) + slideInVertically(tween(400, delayMillis = 300, easing = EaseOutCubic), initialOffsetY = { it / 6 })
                ) {
                    PremiumProfileCard(
                        icon = Icons.Outlined.ShoppingBag,
                        title = "Mis Compras",
                        subtitle = "Revisa el estado de tus pedidos",
                        gradientColors = listOf(AgroVerde, AgroVerdeClaro),
                        onClick = onNavigateToCompras
                    )
                }
            }

            item {
                AnimatedVisibility(
                    visible = isVisible,
                    enter = fadeIn(tween(400, delayMillis = 400)) + slideInVertically(tween(400, delayMillis = 400, easing = EaseOutCubic), initialOffsetY = { it / 6 })
                ) {
                    PremiumProfileCard(
                        icon = Icons.Filled.FavoriteBorder,
                        title = "Mis Favoritos",
                        subtitle = "Productos que te encantan",
                        gradientColors = listOf(Color(0xFFE11D48), Color(0xFFF43F5E)),
                        onClick = onNavigateToFavoritos
                    )
                }
            }

            item {
                AnimatedVisibility(
                    visible = isVisible,
                    enter = fadeIn(tween(400, delayMillis = 500)) + slideInVertically(tween(400, delayMillis = 500, easing = EaseOutCubic), initialOffsetY = { it / 6 })
                ) {
                    PremiumProfileCard(
                        icon = Icons.Outlined.LocationOn,
                        title = "Mis Direcciones",
                        subtitle = "Administra donde quieres recibir",
                        gradientColors = listOf(Color(0xFF2563EB), Color(0xFF3B82F6)),
                        onClick = onNavigateToDirecciones
                    )
                }
            }

            item {
                AnimatedVisibility(
                    visible = isVisible,
                    enter = fadeIn(tween(400, delayMillis = 600)) + slideInVertically(tween(400, delayMillis = 600, easing = EaseOutCubic), initialOffsetY = { it / 6 })
                ) {
                    PremiumProfileCard(
                        icon = Icons.Outlined.Person,
                        title = "Datos Personales",
                        subtitle = "Edita tu informacion de contacto",
                        gradientColors = listOf(Color(0xFF7C3AED), Color(0xFF8B5CF6)),
                        onClick = { }
                    )
                }
            }

            item { Spacer(Modifier.height(8.dp)) }

            // ===== CONFIGURACION SECTION =====
            item {
                AnimatedVisibility(
                    visible = isVisible,
                    enter = fadeIn(tween(500, delayMillis = 700)) + slideInVertically(tween(500, delayMillis = 700, easing = EaseOutCubic), initialOffsetY = { it / 6 })
                ) {
                    Column {
                        Text(
                            "Configuracion",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F172A),
                            letterSpacing = (-0.3.sp)
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "Personaliza tu experiencia",
                            fontSize = 13.sp,
                            color = Color(0xFF64748B)
                        )
                    }
                }
            }

            item {
                AnimatedVisibility(
                    visible = isVisible,
                    enter = fadeIn(tween(400, delayMillis = 800)) + slideInVertically(tween(400, delayMillis = 800, easing = EaseOutCubic), initialOffsetY = { it / 6 })
                ) {
                    PremiumProfileCard(
                        icon = Icons.Outlined.Notifications,
                        title = "Notificaciones",
                        subtitle = "Gestiona tus alertas",
                        gradientColors = listOf(Color(0xFFF59E0B), Color(0xFFFBBF24)),
                        onClick = { }
                    )
                }
            }

            item {
                AnimatedVisibility(
                    visible = isVisible,
                    enter = fadeIn(tween(400, delayMillis = 900)) + slideInVertically(tween(400, delayMillis = 900, easing = EaseOutCubic), initialOffsetY = { it / 6 })
                ) {
                    PremiumProfileCard(
                        icon = Icons.Outlined.Security,
                        title = "Seguridad",
                        subtitle = "Protege tu cuenta",
                        gradientColors = listOf(Color(0xFF14B8A6), Color(0xFF2DD4BF)),
                        onClick = { }
                    )
                }
            }

            item { Spacer(Modifier.height(8.dp)) }

            // ===== LOGOUT BUTTON =====
            item {
                AnimatedVisibility(
                    visible = isVisible,
                    enter = fadeIn(tween(500, delayMillis = 1000)) + slideInVertically(tween(500, delayMillis = 1000, easing = EaseOutCubic), initialOffsetY = { it / 6 })
                ) {
                    var btnScale by remember { mutableStateOf(1f) }
                    Button(
                        onClick = {
                            btnScale = 0.97f
                            onLogout()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .graphicsLayer { scaleX = btnScale; scaleY = btnScale },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFFEF2F2),
                            contentColor = Color(0xFFEF4444)
                        ),
                        shape = RoundedCornerShape(18.dp),
                        border = BorderStroke(1.5.dp, Color(0xFFFECACA))
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.Logout,
                            null,
                            tint = Color(0xFFEF4444),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(10.dp))
                        Text(
                            "Cerrar Sesion",
                            color = Color(0xFFEF4444),
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }
                    LaunchedEffect(btnScale) {
                        if (btnScale != 1f) {
                            kotlinx.coroutines.delay(100)
                            btnScale = 1f
                        }
                    }
                }
            }

            item { Spacer(Modifier.height(32.dp)) }

            // ===== FOOTER HECHO CON AMOR EN COLOMBIA =====
            item {
                AnimatedVisibility(
                    visible = isVisible,
                    enter = fadeIn(tween(800, delayMillis = 1200))
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Decorative wave
                        Canvas(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(60.dp)
                        ) {
                            val width = size.width
                            val height = size.height
                            val path = androidx.compose.ui.graphics.Path().apply {
                                moveTo(0f, height * 0.6f)
                                cubicTo(width * 0.25f, height * 0.3f, width * 0.5f, height * 0.9f, width * 0.75f, height * 0.5f)
                                cubicTo(width * 0.875f, height * 0.3f, width, height * 0.6f, width, height * 0.6f)
                                lineTo(width, height)
                                lineTo(0f, height)
                                close()
                            }
                            drawPath(
                                path = path,
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        AgroVerde.copy(alpha = 0.08f),
                                        AgroVerdeClaro.copy(alpha = 0.04f),
                                        Color.Transparent
                                    )
                                )
                            )
                        }

                        Spacer(Modifier.height(16.dp))

                        // "Hecho con" text
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                "Hecho con ",
                                fontSize = 14.sp,
                                color = Color(0xFF64748B),
                                fontWeight = FontWeight.Medium
                            )

                            val heartTransition = rememberInfiniteTransition(label = "heart")
                            val heartScale by heartTransition.animateFloat(
                                initialValue = 0.9f,
                                targetValue = 1.1f,
                                animationSpec = infiniteRepeatable(
                                    tween(800, easing = EaseInOutCubic),
                                    RepeatMode.Reverse
                                ),
                                label = "heartScale"
                            )

                            Icon(
                                Icons.Filled.Favorite,
                                null,
                                tint = AgroVerde,
                                modifier = Modifier
                                    .size(20.dp)
                                    .graphicsLayer { scaleX = heartScale; scaleY = heartScale }
                            )

                            Text(
                                " en Colombia",
                                fontSize = 14.sp,
                                color = Color(0xFF64748B),
                                fontWeight = FontWeight.Medium
                            )
                        }

                        Spacer(Modifier.height(8.dp))

                        Text(
                            "AgroConecta v1.0",
                            fontSize = 11.sp,
                            color = Color(0xFF94A3B8),
                            fontWeight = FontWeight.Medium
                        )

                        Spacer(Modifier.height(32.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun PremiumProfileCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    gradientColors: List<Color>,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        if (isPressed) 0.98f else 1f,
        spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow)
    )

    Surface(
        color = Color.White,
        shape = RoundedCornerShape(20.dp),
        shadowElevation = 0.dp,
        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
    ) {
        Row(
            modifier = Modifier.padding(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        Brush.linearGradient(colors = gradientColors)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    icon,
                    null,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color(0xFF0F172A),
                    letterSpacing = (-0.2.sp)
                )
                Spacer(Modifier.height(3.dp))
                Text(
                    subtitle,
                    fontSize = 13.sp,
                    color = Color(0xFF64748B),
                    fontWeight = FontWeight.Medium
                )
            }

            Icon(
                Icons.AutoMirrored.Filled.ArrowForward,
                null,
                tint = Color(0xFF94A3B8),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
