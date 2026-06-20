package com.agroconectago.app.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
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
import com.agroconectago.app.data.session.DeliverySessionManager
import com.agroconectago.app.ui.theme.*
import com.agroconectago.app.viewmodel.DeliveryAuthViewModel
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeliveryLoginScreen(
    viewModel: DeliveryAuthViewModel,
    sessionManager: DeliverySessionManager,
    onNavigateToDashboard: () -> Unit,
    onNavigateToRegister: () -> Unit
) {
    // ═══════════════════════════════════════════════════════════════
    // LOGIC PRESERVED - NO CHANGES
    // ═══════════════════════════════════════════════════════════════
    val savedEmail = sessionManager.getLastEmail() ?: ""
    val savedPassword = if (sessionManager.isRememberMe()) sessionManager.getLastPassword() ?: "" else ""
    var email by remember { mutableStateOf(savedEmail) }
    var password by remember { mutableStateOf(savedPassword) }
    var passwordVisible by remember { mutableStateOf(false) }
    var rememberMe by remember { mutableStateOf(sessionManager.isRememberMe()) }
    val focusManager = LocalFocusManager.current

    var isVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(200)
        isVisible = true
    }

    val usuarioLogueado by viewModel.usuarioLogueado.collectAsState()
    val estaCargando by viewModel.estaCargando.collectAsState()
    val errorLogin by viewModel.errorLogin.collectAsState()

    LaunchedEffect(usuarioLogueado) {
        usuarioLogueado?.let { user ->
            sessionManager.saveUser(user)
            sessionManager.saveLastEmail(email)
            if (rememberMe) {
                sessionManager.saveRememberMe(true)
                sessionManager.saveLastPassword(password)
            } else {
                sessionManager.saveRememberMe(false)
                sessionManager.saveLastPassword("")
            }
            onNavigateToDashboard()
        }
    }

    // ══════════════════════════════════════════════════════════════
    // ANIMATIONS - ENHANCED
    // ═══════════════════════════════════════════════════════════════
    val infiniteTransition = rememberInfiniteTransition(label = "login")

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
                    // Primary orb - top right
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                DeliveryBrand600.copy(alpha = orbAlpha * 1.5f),
                                Color.Transparent
                            )
                        ),
                        radius = size.width * 0.8f,
                        center = Offset(size.width * orb1X, size.height * orb1Y)
                    )
                    // Secondary orb - bottom left
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                DeliveryAccent600.copy(alpha = orbAlpha),
                                Color.Transparent
                            )
                        ),
                        radius = size.width * 0.6f,
                        center = Offset(size.width * orb2X, size.height * orb2Y)
                    )
                    // Center glow
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                DeliveryBrand500.copy(alpha = glowPulse),
                                Color.Transparent
                            )
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
            Spacer(modifier = Modifier.height(48.dp))

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
                                    colors = listOf(
                                        DeliveryBrand500,
                                        DeliveryBrand600,
                                        DeliveryBrand900
                                    )
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
                        // Glass overlay effect
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    brush = Brush.radialGradient(
                                        colors = listOf(
                                            Color.White.copy(alpha = 0.15f),
                                            Color.Transparent
                                        ),
                                        center = Offset(55f, 25f),
                                        radius = 70f
                                    )
                                )
                        )
                        Icon(
                            imageVector = Icons.Filled.Motorcycle,
                            contentDescription = "AgroConectaGo",
                            tint = Color.White,
                            modifier = Modifier.size(44.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Divider line with gradient
                    Box(
                        modifier = Modifier
                            .width(48.dp)
                            .height(2.dp)
                            .clip(RoundedCornerShape(1.dp))
                            .background(
                                brush = Brush.horizontalGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        DeliveryBrand500.copy(alpha = 0.6f),
                                        Color.Transparent
                                    )
                                )
                            )
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Text(
                        text = "AgroConectaGo",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Slate900,
                        letterSpacing = (-1.0).sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Delivery · Conecta el campo con la ciudad",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Slate400,
                        letterSpacing = 0.3.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(48.dp))

            // ═══════════════════════════════════════════════════════
            // FORM SECTION - ENHANCED ENTRANCE
            // ═══════════════════════════════════════════════════════
            AnimatedVisibility(
                visible = isVisible,
                enter = fadeIn(tween(700, delayMillis = 200, easing = EaseOutCubic)) +
                        slideInVertically(tween(700, delayMillis = 200, easing = EaseOutCubic), initialOffsetY = { 40 })
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.Start
                ) {
                    // ═══════════════════════════════════════════════
                    // LOGIN CARD - ULTRA PREMIUM
                    // ═══════════════════════════════════════════════
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
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        Color.White.copy(alpha = 0.95f),
                                        Color(0xFFFAFBFC).copy(alpha = 0.9f)
                                    )
                                )
                            )
                            .border(
                                1.dp,
                                Color(0xFFE8ECEF).copy(alpha = 0.6f),
                                RoundedCornerShape(32.dp)
                            )
                            .shadow(
                                elevation = 20.dp,
                                shape = RoundedCornerShape(32.dp),
                                spotColor = Color(0x08000000),
                                ambientColor = Color(0x04000000)
                            )
                            .padding(32.dp)
                    ) {
                        // Header with icon
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(
                                        Brush.linearGradient(
                                            colors = listOf(
                                                DeliveryBrand50,
                                                DeliveryBrand500.copy(alpha = 0.1f)
                                            )
                                        )
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Motorcycle,
                                    contentDescription = null,
                                    tint = DeliveryBrand600,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text(
                                    text = "Acceso Agrosocios",
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Slate900,
                                    letterSpacing = (-0.5).sp
                                )
                                Text(
                                    text = "Ingresa con tu cuenta de repartidor",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Slate400
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(32.dp))

                        // ═══════════════════════════════════════════
                        // ERROR MESSAGE - SMOOTH TRANSITION
                        // ═══════════════════════════════════════════
                        if (errorLogin != null) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(
                                        Brush.linearGradient(
                                            colors = listOf(
                                                ErrorBg,
                                                ErrorBg.copy(alpha = 0.8f)
                                            )
                                        )
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
                                        Icon(
                                            imageVector = Icons.Filled.ErrorOutline,
                                            contentDescription = null,
                                            tint = Color(0xFFDC2626),
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "Error de acceso",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = ErrorText
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = errorLogin ?: "",
                                            fontSize = 12.sp,
                                            color = Color(0xFFDC2626).copy(alpha = 0.8f),
                                            lineHeight = 16.sp
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(24.dp))
                        }

                        // ═══════════════════════════════════════════
                        // EMAIL FIELD
                        // ══════════════════════════════════════════
                        Text(
                            text = "Correo electronico",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Slate700
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        DeliveryTextField(
                            value = email,
                            onValueChange = { email = it },
                            placeholder = "repartidor@agroconecta.com",
                            leadingIcon = Icons.Outlined.Email,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Email,
                                imeAction = ImeAction.Next
                            ),
                            keyboardActions = KeyboardActions(
                                onNext = { focusManager.moveFocus(FocusDirection.Down) }
                            )
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        // ═══════════════════════════════════════════
                        // PASSWORD FIELD
                        // ═══════════════════════════════════════════
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Contrasena",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Slate700
                            )
                            Text(
                                text = "Olvidaste tu contrasena?",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = DeliveryBrand600,
                                modifier = Modifier.clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null
                                ) { }
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))

                        DeliveryTextField(
                            value = password,
                            onValueChange = { password = it },
                            placeholder = "········",
                            leadingIcon = Icons.Outlined.Lock,
                            isPassword = true,
                            passwordVisible = passwordVisible,
                            onTogglePassword = { passwordVisible = !passwordVisible },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Password,
                                imeAction = ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = {
                                    focusManager.clearFocus()
                                    if (email.isNotBlank() && password.isNotBlank()) {
                                        viewModel.iniciarSesion(email, password)
                                    }
                                }
                            )
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        // ═══════════════════════════════════════════
                        // REMEMBER ME
                        // ═══════════════════════════════════════════
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable { rememberMe = !rememberMe }
                        ) {
                            Checkbox(
                                checked = rememberMe,
                                onCheckedChange = { rememberMe = it },
                                colors = CheckboxDefaults.colors(
                                    checkedColor = DeliveryBrand600,
                                    uncheckedColor = Slate300
                                )
                            )
                            Text(
                                "Recordarme",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = Slate500
                            )
                        }

                        Spacer(modifier = Modifier.height(28.dp))

                        // ═══════════════════════════════════════════
                        // SUBMIT BUTTON - ENHANCED
                        // ═══════════════════════════════════════════
                        DeliverySubmitButton(
                            text = "Ingresar",
                            isLoading = estaCargando,
                            loadingText = "Ingresando...",
                            enabled = email.isNotBlank() && password.isNotBlank() && !estaCargando,
                            onClick = { viewModel.iniciarSesion(email, password) }
                        )
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    // ═══════════════════════════════════════════════
                    // REGISTER LINK
                    // ═══════════════════════════════════════════════
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Quieres ser Agrosocio?  ",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium,
                            color = Slate500
                        )
                        Text(
                            text = "Registrate",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = DeliveryBrand600,
                            modifier = Modifier.clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) { onNavigateToRegister() }
                        )
                    }

                    Spacer(modifier = Modifier.height(40.dp))
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════
// TEXT FIELD - ULTRA PREMIUM REDESIGN
// ═══════════════════════════════════════════════════════════════════
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeliveryTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    leadingIcon: ImageVector,
    modifier: Modifier = Modifier,
    isPassword: Boolean = false,
    passwordVisible: Boolean = false,
    onTogglePassword: (() -> Unit)? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    // Smooth color transitions
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

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = {
            Text(
                placeholder,
                color = Slate400,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
        },
        leadingIcon = {
            Icon(
                imageVector = leadingIcon,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(20.dp)
            )
        },
        trailingIcon = if (isPassword) {
            {
                IconButton(
                    onClick = { onTogglePassword?.invoke() },
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = if (passwordVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                        contentDescription = null,
                        tint = Slate400,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        } else null,
        visualTransformation = if (isPassword && !passwordVisible) PasswordVisualTransformation() else VisualTransformation.None,
        singleLine = true,
        interactionSource = interactionSource,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        shape = RoundedCornerShape(16.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = bgColor,
            unfocusedContainerColor = bgColor,
            focusedBorderColor = borderColor,
            unfocusedBorderColor = borderColor,
            cursorColor = DeliveryBrand600,
            focusedTextColor = Slate900,
            unfocusedTextColor = Slate900,
            focusedLabelColor = DeliveryBrand600,
            unfocusedLabelColor = Slate400
        ),
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .shadow(
                elevation = if (isFocused) 8.dp else 0.dp,
                shape = RoundedCornerShape(16.dp),
                spotColor = shadowColor
            )
    )
}

// ═══════════════════════════════════════════════════════════════════
// SUBMIT BUTTON - ENHANCED TACTILE FEEDBACK
// ═══════════════════════════════════════════════════════════════════
@Composable
fun DeliverySubmitButton(
    text: String,
    isLoading: Boolean = false,
    loadingText: String = "Cargando...",
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    // Enhanced scale animation with elastic bounce
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "scale"
    )

    Button(
        onClick = onClick,
        enabled = enabled,
        interactionSource = interactionSource,
        shape = RoundedCornerShape(18.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = DeliveryBrand600,
            disabledContainerColor = DeliveryBrand600.copy(alpha = 0.45f)
        ),
        contentPadding = PaddingValues(horizontal = 28.dp, vertical = 18.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(58.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .shadow(
                elevation = if (enabled) 20.dp else 0.dp,
                shape = RoundedCornerShape(18.dp),
                spotColor = DeliveryBrand600.copy(alpha = 0.4f),
                ambientColor = DeliveryBrand600.copy(alpha = 0.2f)
            )
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(22.dp),
                strokeWidth = 2.5.dp,
                color = Color.White
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                loadingText,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        } else {
            Text(
                text,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                letterSpacing = 0.3.sp
            )
            Spacer(modifier = Modifier.width(12.dp))
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
