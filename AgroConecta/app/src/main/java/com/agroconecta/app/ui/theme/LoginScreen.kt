package com.agroconecta.app.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
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
import androidx.compose.foundation.shape.CircleShape
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
import com.agroconecta.app.ui.theme.*
import com.agroconecta.app.viewmodel.AuthViewModel
import com.agroconecta.app.data.session.SessionManager
import kotlinx.coroutines.delay

private val DarkSlate = Color(0xFF0F172A)
private val Slate700 = Color(0xFF334155)
private val Slate500 = Color(0xFF64748B)
private val Slate400 = Color(0xFF94A3B8)
private val Slate200 = Color(0xFFE2E8F0)
private val Slate100 = Color(0xFFF1F5F9)
private val Brand600 = Color(0xFF16A34A)
private val Brand500 = Color(0xFF22C55E)
private val Brand400 = Color(0xFF4ADE80)
private val Brand900 = Color(0xFF064E3B)
private val Brand50 = Color(0xFFF0FDF4)
private val SurfacePure = Color(0xFFFDFDFC)
private val ErrorRed = Color(0xFFEF4444)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    viewModel: AuthViewModel,
    sessionManager: SessionManager,
    onNavigateToRegister: () -> Unit
) {
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

    // Auto-save credentials on successful login
    LaunchedEffect(viewModel.usuarioLogueado) {
        viewModel.usuarioLogueado?.let {
            sessionManager.saveLastEmail(email)
            if (rememberMe) {
                sessionManager.saveRememberMe(true)
                sessionManager.saveLastPassword(password)
            } else {
                sessionManager.saveRememberMe(false)
                sessionManager.saveLastPassword("")
            }
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "login")
    val orbX by infiniteTransition.animateFloat(
        initialValue = 0.75f, targetValue = 0.85f,
        animationSpec = infiniteRepeatable(tween(4000), RepeatMode.Reverse), label = "orbX"
    )
    val orbY by infiniteTransition.animateFloat(
        initialValue = 0.12f, targetValue = 0.18f,
        animationSpec = infiniteRepeatable(tween(5000), RepeatMode.Reverse), label = "orbY"
    )
    val orbAlpha by infiniteTransition.animateFloat(
        initialValue = 0.04f, targetValue = 0.07f,
        animationSpec = infiniteRepeatable(tween(3000), RepeatMode.Reverse), label = "orbAlpha"
    )
    val glowPulse by infiniteTransition.animateFloat(
        initialValue = 0.03f, targetValue = 0.05f,
        animationSpec = infiniteRepeatable(tween(2500), RepeatMode.Reverse), label = "glow"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SurfacePure)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .drawBehind {
                    drawCircle(
                        color = Brand600.copy(alpha = orbAlpha),
                        radius = size.width * 0.7f,
                        center = Offset(size.width * orbX, size.height * orbY)
                    )
                    drawCircle(
                        color = Color(0xFFEAB308).copy(alpha = 0.03f),
                        radius = size.width * 0.5f,
                        center = Offset(size.width * 0.1f, size.height * 0.85f)
                    )
                    drawCircle(
                        color = Brand500.copy(alpha = glowPulse),
                        radius = size.width * 0.35f,
                        center = Offset(size.width * 0.5f, size.height * 0.5f)
                    )
                }
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 28.dp)
                .statusBarsPadding()
                .navigationBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(56.dp))

            AnimatedVisibility(
                visible = isVisible,
                enter = fadeIn(tween(700)) + slideInVertically(
                    tween(700, easing = EaseOutBack),
                    initialOffsetY = { -40 }
                )
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    val logoGlow by infiniteTransition.animateFloat(
                        initialValue = 0.25f, targetValue = 0.4f,
                        animationSpec = infiniteRepeatable(tween(2000), RepeatMode.Reverse), label = "logoGlow"
                    )
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(
                                brush = Brush.linearGradient(
                                    colors = listOf(Brand500, Brand600, Color(0xFF065F46))
                                )
                            )
                            .shadow(
                                elevation = 24.dp,
                                shape = RoundedCornerShape(20.dp),
                                spotColor = Brand600.copy(alpha = logoGlow)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    brush = Brush.radialGradient(
                                        colors = listOf(
                                            Color.White.copy(alpha = 0.12f),
                                            Color.Transparent
                                        ),
                                        center = Offset(50f, 20f),
                                        radius = 60f
                                    )
                                )
                        )
                        Icon(
                            imageVector = Icons.Filled.Eco,
                            contentDescription = "AgroConecta",
                            tint = Color.White,
                            modifier = Modifier.size(36.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Box(
                        modifier = Modifier
                            .width(40.dp)
                            .height(2.dp)
                            .clip(RoundedCornerShape(1.dp))
                            .background(
                                brush = Brush.horizontalGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        Brand500.copy(alpha = 0.5f),
                                        Color.Transparent
                                    )
                                )
                            )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "AgroConecta",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = DarkSlate,
                        letterSpacing = (-0.8).sp
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Del campo colombiano a tu mesa",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = Slate400,
                        letterSpacing = 0.2.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(40.dp))

            AnimatedVisibility(
                visible = isVisible,
                enter = fadeIn(tween(600, delayMillis = 200)) + slideInVertically(
                    tween(600, delayMillis = 200, easing = EaseOutBack),
                    initialOffsetY = { 30 }
                )
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.Start
                ) {
                    // ===== FORM CARD (Glassmorphism) =====
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(28.dp))
                            .background(
                                brush = Brush.verticalGradient(
                                    colors = listOf(Color.White, Color(0xFFFAFBFC))
                                )
                            )
                            .border(1.dp, Color(0xFFE8ECEF).copy(alpha = 0.8f), RoundedCornerShape(28.dp))
                            .shadow(
                                elevation = 12.dp,
                                shape = RoundedCornerShape(28.dp),
                                spotColor = Color(0x0A000000)
                            )
                            .padding(28.dp)
                    ) {
                        Text(
                            text = "Bienvenido de vuelta",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = DarkSlate,
                            letterSpacing = (-0.5).sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Ingresa tus datos para continuar",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = Slate400,
                            lineHeight = 18.sp
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        if (viewModel.errorLogin != null) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(Color(0xFFFEF2F2))
                                    .border(1.dp, Color(0xFFFECACA), RoundedCornerShape(14.dp))
                                    .padding(12.dp)
                            ) {
                                Row(verticalAlignment = Alignment.Top) {
                                    Icon(
                                        imageVector = Icons.Filled.ErrorOutline,
                                        contentDescription = null,
                                        tint = Color(0xFFDC2626),
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = "Error de acceso",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF991B1B)
                                        )
                                        Text(
                                            text = viewModel.errorLogin ?: "",
                                            fontSize = 11.sp,
                                            color = Color(0xFFDC2626)
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                        }

                        Text(
                            text = "Correo electronico",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Slate700
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        AuthTextField(
                            value = email,
                            onValueChange = { email = it },
                            placeholder = "nombre@ejemplo.com",
                            leadingIcon = Icons.Outlined.Email,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Email,
                                imeAction = ImeAction.Next
                            ),
                            keyboardActions = KeyboardActions(
                                onNext = { focusManager.moveFocus(FocusDirection.Down) }
                            )
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Contrasena",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Slate700
                            )
                            Text(
                                text = "Olvidaste tu contrasena?",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Brand600,
                                modifier = Modifier.clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null
                                ) { }
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))

                        AuthTextField(
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

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { rememberMe = !rememberMe }) {
                            Checkbox(checked = rememberMe, onCheckedChange = { rememberMe = it }, colors = CheckboxDefaults.colors(checkedColor = Brand600))
                            Text("Recordarme", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = Slate500)
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        SubmitButton(
                            text = "Ingresar",
                            isLoading = viewModel.estaCargando,
                            loadingText = "Ingresando...",
                            enabled = email.isNotBlank() && password.isNotBlank() && !viewModel.estaCargando,
                            onClick = { viewModel.iniciarSesion(email, password) }
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "No tienes cuenta?  ",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = Slate500
                        )
                        Text(
                            text = "Registrate",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Brand600,
                            modifier = Modifier.clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) { onNavigateToRegister() }
                        )
                    }

                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthTextField(
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
    val borderColor by animateColorAsState(
        if (isFocused) Brand600 else Color(0xFFE2E8F0),
        tween(200), label = "border"
    )
    val bgColor by animateColorAsState(
        if (isFocused) Color.White else Color(0xFFF8FAFC),
        tween(200), label = "bg"
    )

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = {
            Text(placeholder, color = Slate400, fontSize = 14.sp, fontWeight = FontWeight.Medium)
        },
        leadingIcon = {
            Icon(
                imageVector = leadingIcon,
                contentDescription = null,
                tint = if (isFocused) Brand600 else Slate400,
                modifier = Modifier.size(20.dp)
            )
        },
        trailingIcon = if (isPassword) {
            {
                IconButton(
                    onClick = { onTogglePassword?.invoke() },
                    modifier = Modifier.size(24.dp)
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
        shape = RoundedCornerShape(14.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = bgColor,
            unfocusedContainerColor = bgColor,
            focusedBorderColor = borderColor,
            unfocusedBorderColor = borderColor,
            cursorColor = Brand600,
            focusedTextColor = DarkSlate,
            unfocusedTextColor = DarkSlate
        ),
        modifier = modifier
            .fillMaxWidth()
            .height(54.dp)
    )
}

@Composable
fun SubmitButton(
    text: String,
    isLoading: Boolean = false,
    loadingText: String = "Cargando...",
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy), label = "scale"
    )

    Button(
        onClick = onClick,
        enabled = enabled,
        interactionSource = interactionSource,
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Brand600,
            disabledContainerColor = Brand600.copy(alpha = 0.45f)
        ),
        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(54.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .shadow(
                elevation = 16.dp,
                shape = RoundedCornerShape(16.dp),
                spotColor = Brand600.copy(alpha = 0.35f)
            )
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                strokeWidth = 2.dp,
                color = Color.White
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(loadingText, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
        } else {
            Text(text, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White, letterSpacing = 0.2.sp)
            Spacer(modifier = Modifier.width(10.dp))
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}
