package com.agroconectago.app.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.*
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.agroconectago.app.data.model.DeliveryRegisterRequest
import com.agroconectago.app.ui.theme.*
import com.agroconectago.app.viewmodel.DeliveryAuthViewModel
import kotlinx.coroutines.delay

@Composable
fun DeliveryRegisterScreen(
    viewModel: DeliveryAuthViewModel,
    onNavigateToLogin: () -> Unit,
    onProfileCompletion: () -> Unit
) {
    // ═══════════════════════════════════════════════════════════════
    // LOGIC PRESERVED - NO CHANGES
    // ═══════════════════════════════════════════════════════════════
    val estaCargando by viewModel.estaCargando.collectAsState()
    val mensajeRegistro by viewModel.mensajeRegistro.collectAsState()
    val registroExitoso by viewModel.registroExitoso.collectAsState()

    var nombreCompleto by remember { mutableStateOf("") }
    var userName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var telefono by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var passwordFocused by remember { mutableStateOf(false) }

    val hasMinLength = password.length >= 8
    val hasUppercase = password.any { it.isUpperCase() }
    val hasNumber = password.any { it.isDigit() }
    val hasSpecial = password.any { !it.isLetterOrDigit() }
    val strengthScore = listOf(hasMinLength, hasUppercase, hasNumber, hasSpecial).count { it }

    var errorCampo by remember { mutableStateOf<String?>(null) }
    val focusManager = LocalFocusManager.current

    var isVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { delay(150); isVisible = true }

    LaunchedEffect(registroExitoso) {
        if (registroExitoso) {
            delay(2500)
            viewModel.limpiarMensajeRegistro()
            onProfileCompletion()
        }
    }

    fun validar(): Boolean {
        errorCampo = when {
            nombreCompleto.isBlank() -> "Ingresa tu nombre completo"
            userName.isBlank() -> "Ingresa un nombre de usuario"
            userName.length < 4 -> "El usuario debe tener al menos 4 caracteres"
            email.isBlank() || !email.contains("@") || !email.contains(".") -> "Ingresa un correo valido"
            telefono.length < 7 -> "Ingresa un telefono valido"
            strengthScore < 2 -> "La contrasena debe tener al menos 8 caracteres, una mayuscula, un numero o un caracter especial"
            else -> null
        }
        return errorCampo == null
    }

    fun submit() {
        if (!validar()) return
        focusManager.clearFocus()
        viewModel.registrarRepartidor(
            DeliveryRegisterRequest(
                userName = userName.trim(),
                password = password,
                nombreCompleto = nombreCompleto.trim(),
                email = email.trim().lowercase(),
                telefono = telefono.trim().filter { it.isDigit() }
            )
        )
    }

    // ═══════════════════════════════════════════════════════════════
    // ANIMATIONS - ENHANCED
    // ══════════════════════════════════════════════════════════════
    val infiniteTransition = rememberInfiniteTransition(label = "reg")

    // Organic background orbs with smoother movement
    val orb1X by infiniteTransition.animateFloat(
        initialValue = 0.8f, targetValue = 0.9f,
        animationSpec = infiniteRepeatable(tween(6000, easing = EaseInOutCubic), RepeatMode.Reverse), label = "orb1X"
    )
    val orb1Y by infiniteTransition.animateFloat(
        initialValue = 0.15f, targetValue = 0.25f,
        animationSpec = infiniteRepeatable(tween(7000, easing = EaseInOutCubic), RepeatMode.Reverse), label = "orb1Y"
    )
    val orb2X by infiniteTransition.animateFloat(
        initialValue = 0.1f, targetValue = 0.2f,
        animationSpec = infiniteRepeatable(tween(8000, easing = EaseInOutCubic), RepeatMode.Reverse), label = "orb2X"
    )
    val orb2Y by infiniteTransition.animateFloat(
        initialValue = 0.8f, targetValue = 0.9f,
        animationSpec = infiniteRepeatable(tween(9000, easing = EaseInOutCubic), RepeatMode.Reverse), label = "orb2Y"
    )
    val orbAlpha by infiniteTransition.animateFloat(
        initialValue = 0.03f, targetValue = 0.06f,
        animationSpec = infiniteRepeatable(tween(4000), RepeatMode.Reverse), label = "orbAlpha"
    )
    val glowPulse by infiniteTransition.animateFloat(
        initialValue = 0.02f, targetValue = 0.04f,
        animationSpec = infiniteRepeatable(tween(3000), RepeatMode.Reverse), label = "glow"
    )

    // ═══════════════════════════════════════════════════════════════
    // UI - ULTRA PREMIUM REDESIGN
    // ═══════════════════════════════════════════════════════════════
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
        // Organic background with multiple soft orbs
        Box(
            modifier = Modifier
                .fillMaxSize()
                .drawBehind {
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(DeliveryBrand600.copy(alpha = orbAlpha * 1.5f), Color.Transparent)
                        ),
                        radius = size.width * 0.8f,
                        center = Offset(size.width * orb1X, size.height * orb1Y)
                    )
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(DeliveryAccent600.copy(alpha = orbAlpha), Color.Transparent)
                        ),
                        radius = size.width * 0.6f,
                        center = Offset(size.width * orb2X, size.height * orb2Y)
                    )
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(DeliveryBrand500.copy(alpha = glowPulse), Color.Transparent)
                        ),
                        radius = size.width * 0.4f,
                        center = Offset(size.width * 0.5f, size.height * 0.5f)
                    )
                }
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
                .statusBarsPadding()
                .navigationBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(32.dp))

            // ═══════════════════════════════════════════════════════
            // BACK BUTTON - ENHANCED
            // ═══════════════════════════════════════════════════════
            Row(Modifier.fillMaxWidth()) {
                var backScale by remember { mutableStateOf(1f) }
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(
                            Brush.linearGradient(
                                colors = listOf(Color.White, Color(0xFFFAFBFC))
                            )
                        )
                        .border(1.dp, Color(0xFFE8ECEF).copy(alpha = 0.6f), RoundedCornerShape(14.dp))
                        .shadow(8.dp, RoundedCornerShape(14.dp), spotColor = Color(0x08000000))
                        .graphicsLayer { scaleX = backScale; scaleY = backScale }
                        .clickable {
                            backScale = 0.93f
                            onNavigateToLogin()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver", tint = Slate600, modifier = Modifier.size(20.dp))
                }
                LaunchedEffect(backScale) {
                    if (backScale != 1f) { delay(100); backScale = 1f }
                }
            }

            Spacer(Modifier.height(32.dp))

            // ═══════════════════════════════════════════════════════
            // LOGO SECTION - ENHANCED ENTRANCE
            // ═══════════════════════════════════════════════════════
            AnimatedVisibility(
                visible = isVisible,
                enter = fadeIn(tween(800, easing = EaseOutCubic)) +
                        slideInVertically(tween(800, easing = EaseOutCubic), initialOffsetY = { -60 })
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    val logoGlow by infiniteTransition.animateFloat(
                        initialValue = 0.3f, targetValue = 0.5f,
                        animationSpec = infiniteRepeatable(tween(2500), RepeatMode.Reverse), label = "logoGlow"
                    )
                    val logoScale by infiniteTransition.animateFloat(
                        initialValue = 0.98f, targetValue = 1.02f,
                        animationSpec = infiniteRepeatable(tween(3000), RepeatMode.Reverse), label = "logoScale"
                    )

                    Box(
                        modifier = Modifier
                            .size(88.dp)
                            .graphicsLayer { scaleX = logoScale; scaleY = logoScale }
                            .clip(RoundedCornerShape(24.dp))
                            .background(
                                brush = Brush.linearGradient(
                                    colors = listOf(DeliveryBrand500, DeliveryBrand600, DeliveryBrand900)
                                )
                            )
                            .shadow(
                                elevation = 32.dp,
                                shape = RoundedCornerShape(24.dp),
                                spotColor = DeliveryBrand600.copy(alpha = logoGlow),
                                ambientColor = DeliveryBrand600.copy(alpha = logoGlow * 0.5f)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    brush = Brush.radialGradient(
                                        colors = listOf(Color.White.copy(alpha = 0.15f), Color.Transparent),
                                        center = Offset(55f, 25f),
                                        radius = 70f
                                    )
                                )
                        )
                        Icon(Icons.Filled.Motorcycle, "AgroConectaGo", tint = Color.White, modifier = Modifier.size(44.dp))
                    }

                    Spacer(Modifier.height(20.dp))

                    Box(
                        modifier = Modifier
                            .width(48.dp)
                            .height(2.dp)
                            .clip(RoundedCornerShape(1.dp))
                            .background(
                                brush = Brush.horizontalGradient(
                                    colors = listOf(Color.Transparent, DeliveryBrand500.copy(alpha = 0.6f), Color.Transparent)
                                )
                            )
                    )

                    Spacer(Modifier.height(20.dp))

                    Text("AgroConectaGo", fontSize = 32.sp, fontWeight = FontWeight.ExtraBold, color = Slate900, letterSpacing = (-1.0).sp)
                    Spacer(Modifier.height(8.dp))
                    Text("Registro de Agrosocio", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Slate400, letterSpacing = 0.3.sp)
                }
            }

            Spacer(Modifier.height(40.dp))

            // ═══════════════════════════════════════════════════════
            // SUCCESS STATE - ENHANCED
            // ═══════════════════════════════════════════════════════
            AnimatedVisibility(
                visible = registroExitoso,
                enter = fadeIn(tween(600)) + scaleIn(tween(600, easing = EaseOutBack)),
                exit = fadeOut(tween(200))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .animateContentSize(
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessLow
                            )
                        )
                        .clip(RoundedCornerShape(32.dp))
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.White.copy(alpha = 0.95f), Color(0xFFFAFBFC).copy(alpha = 0.9f))
                            )
                        )
                        .border(1.dp, DeliveryBrand400.copy(alpha = 0.3f), RoundedCornerShape(32.dp))
                        .shadow(20.dp, RoundedCornerShape(32.dp), spotColor = Color(0x08000000))
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(88.dp)
                            .clip(RoundedCornerShape(24.dp))
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(DeliveryBrand50, DeliveryBrand500.copy(alpha = 0.1f))
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Filled.CheckCircle, null, tint = DeliveryBrand600, modifier = Modifier.size(52.dp))
                    }
                    Spacer(Modifier.height(24.dp))
                    Text("Registro Exitoso", fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = Slate900, letterSpacing = (-0.5).sp)
                    Spacer(Modifier.height(12.dp))
                    Text(
                        "Tu cuenta ha sido creada. Ahora inicia sesion para continuar con tu perfil de repartidor.",
                        fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Slate400, lineHeight = 22.sp, textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    Spacer(Modifier.height(24.dp))
                    Text("Redirigiendo...", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = DeliveryBrand400)
                }
            }

            // ═══════════════════════════════════════════════════════
            // FORM SECTION - STAGGERED ENTRANCE
            // ═══════════════════════════════════════════════════════
            AnimatedVisibility(
                visible = !registroExitoso && isVisible,
                enter = fadeIn(tween(700, delayMillis = 200, easing = EaseOutCubic)) +
                        slideInVertically(tween(700, delayMillis = 200, easing = EaseOutCubic), initialOffsetY = { 40 })
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .animateContentSize(
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessLow
                            )
                        )
                        .clip(RoundedCornerShape(32.dp))
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.White.copy(alpha = 0.95f), Color(0xFFFAFBFC).copy(alpha = 0.9f))
                            )
                        )
                        .border(1.dp, Color(0xFFE8ECEF).copy(alpha = 0.6f), RoundedCornerShape(32.dp))
                        .shadow(20.dp, RoundedCornerShape(32.dp), spotColor = Color(0x08000000))
                        .padding(32.dp)
                ) {
                    // Header
                    Text("Crea tu cuenta", fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = Slate900, letterSpacing = (-0.5).sp)
                    Text("Completa tus datos para empezar", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Slate400)
                    Spacer(Modifier.height(28.dp))

                    // ═══════════════════════════════════════════════
                    // ERROR MESSAGES - SMOOTH TRANSITION
                    // ═══════════════════════════════════════════════
                    AnimatedVisibility(
                        visible = errorCampo != null,
                        enter = fadeIn(tween(300)) + slideInVertically(tween(300), initialOffsetY = { -8 }),
                        exit = fadeOut(tween(200))
                    ) {
                        errorCampo?.let { err ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(
                                        Brush.linearGradient(colors = listOf(ErrorBg, ErrorBg.copy(alpha = 0.8f)))
                                    )
                                    .border(1.dp, ErrorBorder, RoundedCornerShape(16.dp))
                                    .padding(16.dp)
                            ) {
                                Row(verticalAlignment = Alignment.Top) {
                                    Box(
                                        modifier = Modifier
                                            .size(32.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(Color(0xFFDC2626).copy(alpha = 0.1f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Filled.ErrorOutline, null, tint = Color(0xFFDC2626), modifier = Modifier.size(18.dp))
                                    }
                                    Spacer(Modifier.width(12.dp))
                                    Text(err, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = Color(0xFFDC2626), lineHeight = 18.sp, modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }

                    AnimatedVisibility(
                        visible = mensajeRegistro != null && !registroExitoso,
                        enter = fadeIn(tween(300)),
                        exit = fadeOut(tween(200))
                    ) {
                        mensajeRegistro?.let { msg ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = if (errorCampo != null) 12.dp else 0.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(
                                        Brush.linearGradient(colors = listOf(ErrorBg, ErrorBg.copy(alpha = 0.8f)))
                                    )
                                    .border(1.dp, ErrorBorder, RoundedCornerShape(16.dp))
                                    .padding(16.dp)
                            ) {
                                Row(verticalAlignment = Alignment.Top) {
                                    Box(
                                        modifier = Modifier
                                            .size(32.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(Color(0xFFDC2626).copy(alpha = 0.1f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Filled.ErrorOutline, null, tint = Color(0xFFDC2626), modifier = Modifier.size(18.dp))
                                    }
                                    Spacer(Modifier.width(12.dp))
                                    Text(msg, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = Color(0xFFDC2626), lineHeight = 18.sp, modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(if (errorCampo != null || mensajeRegistro != null) 20.dp else 0.dp))

                    // ═══════════════════════════════════════════════
                    // INPUT FIELDS - STAGGERED ANIMATION
                    // ═══════════════════════════════════════════════
                    RegField("Nombre Completo", nombreCompleto, { nombreCompleto = it }, "Ej: Carlos Perez", Icons.Outlined.Person, KeyboardType.Text, ImeAction.Next, focusManager, 0)
                    RegField("Nombre de Usuario", userName, { userName = it }, "carlos_moto", Icons.Outlined.AccountCircle, KeyboardType.Text, ImeAction.Next, focusManager, 100)
                    RegField("Correo Electronico", email, { email = it; viewModel.comprobarEmail(email) }, "repartidor@mail.com", Icons.Outlined.Email, KeyboardType.Email, ImeAction.Next, focusManager, 200)
                    RegField("Telefono", telefono, { telefono = it.filter { it.isDigit() } }, "3001234567", Icons.Outlined.Phone, KeyboardType.Phone, ImeAction.Next, focusManager, 300)

                    PasswordStrengthField(
                        value = password,
                        onValueChange = { password = it },
                        visible = passwordVisible,
                        onToggle = { passwordVisible = it },
                        onFocusChange = { passwordFocused = it },
                        showStrength = passwordFocused || password.isNotEmpty(),
                        hasMinLength = hasMinLength,
                        hasUppercase = hasUppercase,
                        hasNumber = hasNumber,
                        hasSpecial = hasSpecial,
                        strengthScore = strengthScore,
                        delay = 400
                    )

                    Spacer(Modifier.height(28.dp))

                    // ═══════════════════════════════════════════════
                    // SUBMIT BUTTON - ENHANCED TACTILE FEEDBACK
                    // ═══════════════════════════════════════════════
                    val interactionSource = remember { MutableInteractionSource() }
                    val isPressed by interactionSource.collectIsPressedAsState()
                    val scale by animateFloatAsState(
                        targetValue = if (isPressed) 0.95f else 1f,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessLow
                        ),
                        label = "scale"
                    )

                    Button(
                        onClick = { submit() },
                        enabled = !estaCargando,
                        interactionSource = interactionSource,
                        shape = RoundedCornerShape(18.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = DeliveryBrand600,
                            disabledContainerColor = DeliveryBrand600.copy(alpha = 0.45f)
                        ),
                        contentPadding = PaddingValues(vertical = 18.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(58.dp)
                            .graphicsLayer { scaleX = scale; scaleY = scale }
                            .shadow(
                                elevation = if (!estaCargando) 20.dp else 0.dp,
                                shape = RoundedCornerShape(18.dp),
                                spotColor = DeliveryBrand600.copy(alpha = 0.4f),
                                ambientColor = DeliveryBrand600.copy(alpha = 0.2f)
                            )
                    ) {
                        if (estaCargando) {
                            CircularProgressIndicator(Modifier.size(22.dp), strokeWidth = 2.5.dp, color = Color.White)
                            Spacer(Modifier.width(12.dp))
                            Text("Registrando...", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color.White)
                        } else {
                            Text("Crear Cuenta", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White, letterSpacing = 0.3.sp)
                            Spacer(Modifier.width(12.dp))
                            Icon(Icons.AutoMirrored.Filled.ArrowForward, null, tint = Color.White, modifier = Modifier.size(20.dp))
                        }
                    }
                }
            }

            // ══════════════════════════════════════════════════════
            // FOOTER - LOGIN LINK
            // ═══════════════════════════════════════════════════════
            if (!registroExitoso) {
                Spacer(Modifier.height(32.dp))
                Row(
                    Modifier.fillMaxWidth().padding(bottom = 32.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Ya tienes cuenta?  ", fontSize = 15.sp, fontWeight = FontWeight.Medium, color = Slate500)
                    Text(
                        "Inicia Sesion",
                        fontSize = 15.sp, fontWeight = FontWeight.Bold, color = DeliveryBrand600,
                        modifier = Modifier.clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) { onNavigateToLogin() }
                    )
                }
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}

// ═══════════════════════════════════════════════════════════════════
// TEXT FIELD - ULTRA PREMIUM REDESIGN
// ═══════════════════════════════════════════════════════════════════
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RegField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    icon: ImageVector,
    keyboardType: KeyboardType,
    imeAction: ImeAction,
    focusManager: androidx.compose.ui.focus.FocusManager,
    delay: Int = 0
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    val borderColor by animateColorAsState(
        if (isFocused) DeliveryBrand600 else Color(0xFFE2E8F0),
        tween(300, easing = EaseOutCubic), label = "border"
    )
    val bgColor by animateColorAsState(
        if (isFocused) Color.White else Color(0xFFFAFBFC),
        tween(300, easing = EaseOutCubic), label = "bg"
    )
    val iconColor by animateColorAsState(
        if (isFocused) DeliveryBrand600 else Slate400,
        tween(300, easing = EaseOutCubic), label = "icon"
    )
    val shadowColor by animateColorAsState(
        if (isFocused) DeliveryBrand600.copy(alpha = 0.15f) else Color.Transparent,
        tween(300, easing = EaseOutCubic), label = "shadow"
    )

    val isVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        if (delay > 0) kotlinx.coroutines.delay(delay.toLong())
    }

    Column(Modifier.padding(vertical = 6.dp)) {
        Text(label, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Slate700)
        Spacer(Modifier.height(10.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text(placeholder, color = Slate400, fontSize = 14.sp, fontWeight = FontWeight.Medium) },
            leadingIcon = {
                Icon(icon, null, tint = iconColor, modifier = Modifier.size(20.dp))
            },
            singleLine = true,
            interactionSource = interactionSource,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType, imeAction = imeAction),
            keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = borderColor,
                unfocusedBorderColor = borderColor,
                cursorColor = DeliveryBrand600,
                focusedContainerColor = bgColor,
                unfocusedContainerColor = bgColor,
                focusedTextColor = Slate900,
                unfocusedTextColor = Slate900
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .shadow(
                    elevation = if (isFocused) 8.dp else 0.dp,
                    shape = RoundedCornerShape(16.dp),
                    spotColor = shadowColor
                )
        )
    }
}

// ══════════════════════════════════════════════════════════════════
// PASSWORD FIELD WITH STRENGTH INDICATOR
// ═══════════════════════════════════════════════════════════════════
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PasswordStrengthField(
    value: String,
    onValueChange: (String) -> Unit,
    visible: Boolean,
    onToggle: (Boolean) -> Unit,
    onFocusChange: (Boolean) -> Unit,
    showStrength: Boolean,
    hasMinLength: Boolean,
    hasUppercase: Boolean,
    hasNumber: Boolean,
    hasSpecial: Boolean,
    strengthScore: Int,
    delay: Int = 0
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    LaunchedEffect(isFocused) { onFocusChange(isFocused) }

    val borderColor by animateColorAsState(
        if (isFocused) DeliveryBrand600 else Color(0xFFE2E8F0),
        tween(300, easing = EaseOutCubic), label = "border"
    )
    val bgColor by animateColorAsState(
        if (isFocused) Color.White else Color(0xFFFAFBFC),
        tween(300, easing = EaseOutCubic), label = "bg"
    )
    val iconColor by animateColorAsState(
        if (isFocused) DeliveryBrand600 else Slate400,
        tween(300, easing = EaseOutCubic), label = "icon"
    )
    val shadowColor by animateColorAsState(
        if (isFocused) DeliveryBrand600.copy(alpha = 0.15f) else Color.Transparent,
        tween(300, easing = EaseOutCubic), label = "shadow"
    )

    Column(Modifier.padding(vertical = 6.dp)) {
        Text("Contrasena", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Slate700)
        Spacer(Modifier.height(10.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text("Minimo 8 caracteres", color = Slate400, fontSize = 14.sp, fontWeight = FontWeight.Medium) },
            leadingIcon = {
                Icon(Icons.Outlined.Lock, null, tint = iconColor, modifier = Modifier.size(20.dp))
            },
            trailingIcon = {
                IconButton(onClick = { onToggle(!visible) }, modifier = Modifier.size(28.dp)) {
                    Icon(
                        if (visible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                        null,
                        tint = Slate400,
                        modifier = Modifier.size(18.dp)
                    )
                }
            },
            visualTransformation = if (!visible) PasswordVisualTransformation() else VisualTransformation.None,
            singleLine = true,
            interactionSource = interactionSource,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = borderColor,
                unfocusedBorderColor = borderColor,
                cursorColor = DeliveryBrand600,
                focusedContainerColor = bgColor,
                unfocusedContainerColor = bgColor,
                focusedTextColor = Slate900,
                unfocusedTextColor = Slate900
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .shadow(
                    elevation = if (isFocused) 8.dp else 0.dp,
                    shape = RoundedCornerShape(16.dp),
                    spotColor = shadowColor
                )
        )

        AnimatedVisibility(
            visible = showStrength,
            enter = expandVertically(tween(400, easing = EaseOutCubic)) + fadeIn(tween(400)),
            exit = shrinkVertically(tween(300)) + fadeOut(tween(300))
        ) {
            Column(modifier = Modifier.padding(top = 12.dp)) {
                // Strength bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(Color(0xFFE2E8F0)),
                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    val strengthColors = listOf(Color(0xFFEF4444), Color(0xFFF59E0B), Color(0xFFF59E0B), Color(0xFF22C55E))
                    repeat(4) { index ->
                        val segColor by animateColorAsState(
                            if (index < strengthScore) strengthColors[index] else Color(0xFFE2E8F0),
                            tween(400, easing = EaseOutCubic)
                        )
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(2.dp))
                                .background(segColor)
                        )
                    }
                }

                Spacer(Modifier.height(8.dp))

                Text(
                    text = when (strengthScore) {
                        0 -> "Muy debil"
                        1 -> "Debil"
                        2 -> "Media"
                        3 -> "Fuerte"
                        4 -> "Muy fuerte"
                        else -> ""
                    },
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = when (strengthScore) {
                        0, 1 -> Color(0xFFEF4444)
                        2 -> Color(0xFFF59E0B)
                        3, 4 -> Color(0xFF22C55E)
                        else -> Slate400
                    }
                )

                Spacer(Modifier.height(12.dp))

                // Password rules
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Column(Modifier.weight(1f)) {
                        PwdRule("8+ caracteres", hasMinLength)
                        PwdRule("Una mayuscula", hasUppercase)
                    }
                    Column(Modifier.weight(1f)) {
                        PwdRule("Un numero", hasNumber)
                        PwdRule("Caracter especial", hasSpecial)
                    }
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════
// PASSWORD RULE INDICATOR
// ═══════════════════════════════════════════════════════════════════
@Composable
private fun PwdRule(text: String, met: Boolean) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 3.dp)
    ) {
        Icon(
            if (met) Icons.Filled.CheckCircle else Icons.Filled.Circle,
            null,
            tint = if (met) Color(0xFF22C55E) else Slate300,
            modifier = Modifier.size(14.dp)
        )
        Spacer(Modifier.width(6.dp))
        Text(text, fontSize = 11.sp, fontWeight = FontWeight.Medium, color = if (met) Slate700 else Slate400)
    }
}
