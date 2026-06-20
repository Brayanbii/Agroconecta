package com.agroconecta.app.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.ui.focus.onFocusChanged
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
import com.agroconecta.app.data.model.Usuario
import com.agroconecta.app.ui.theme.*
import com.agroconecta.app.viewmodel.AuthViewModel
import kotlinx.coroutines.delay

private val DarkSlate = Color(0xFF0F172A)
private val Slate700 = Color(0xFF334155)
private val Slate500 = Color(0xFF64748B)
private val Slate400 = Color(0xFF94A3B8)
private val Brand600 = Color(0xFF16A34A)
private val Brand500 = Color(0xFF22C55E)
private val Brand400 = Color(0xFF4ADE80)
private val Brand900 = Color(0xFF064E3B)
private val Brand50 = Color(0xFFF0FDF4)
private val SurfacePure = Color(0xFFFDFDFC)
private val Amber600 = Color(0xFFCA8A04)
private val Amber500 = Color(0xFFEAB308)
private val Amber900 = Color(0xFF422006)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    viewModel: AuthViewModel,
    onNavigateToLogin: () -> Unit
) {
    var username by remember { mutableStateOf("") }
    var nombreCompleto by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var telefono by remember { mutableStateOf("") }
    var esCampesino by remember { mutableStateOf(false) }
    var passwordVisible by remember { mutableStateOf(false) }
    var passwordFocused by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current

    val activeBrand by animateColorAsState(
        if (esCampesino) Amber600 else Brand600,
        tween(500), label = "brand"
    )
    val activeBrandLight by animateColorAsState(
        if (esCampesino) Amber500 else Brand500,
        tween(500), label = "brandLight"
    )
    val activeBrand900 by animateColorAsState(
        if (esCampesino) Amber900 else Brand900,
        tween(500), label = "brandDark"
    )

    val hasMinLength = password.length >= 8
    val hasUppercase = password.any { it.isUpperCase() }
    val hasNumber = password.any { it.isDigit() }
    val hasSpecial = password.any { !it.isLetterOrDigit() }
    val strengthScore = listOf(hasMinLength, hasUppercase, hasNumber, hasSpecial).count { it }

    var isVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(200)
        isVisible = true
    }

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
                        color = activeBrand.copy(alpha = 0.05f),
                        radius = size.width * 0.55f,
                        center = Offset(size.width * 0.85f, size.height * 0.2f)
                    )
                    drawCircle(
                        color = if (esCampesino) Brand600.copy(alpha = 0.03f) else Amber500.copy(alpha = 0.03f),
                        radius = size.width * 0.4f,
                        center = Offset(size.width * 0.1f, size.height * 0.8f)
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
            Spacer(modifier = Modifier.height(40.dp))

            AnimatedVisibility(
                visible = isVisible,
                enter = fadeIn(tween(700)) + slideInVertically(
                    tween(700, easing = EaseOutBack),
                    initialOffsetY = { -30 }
                )
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(RoundedCornerShape(18.dp))
                            .background(
                                brush = Brush.linearGradient(
                                    colors = listOf(activeBrandLight, activeBrand)
                                )
                            )
                            .shadow(
                                elevation = 16.dp,
                                shape = RoundedCornerShape(18.dp),
                                spotColor = activeBrand.copy(alpha = 0.3f)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (esCampesino) Icons.Filled.Agriculture else Icons.Filled.Eco,
                            contentDescription = "AgroConecta",
                            tint = Color.White,
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "AgroConecta",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = DarkSlate,
                        letterSpacing = (-0.5).sp
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = "Crea tu cuenta gratis",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = Slate400
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            AnimatedVisibility(
                visible = isVisible,
                enter = fadeIn(tween(600, delayMillis = 150)) + slideInVertically(
                    tween(600, delayMillis = 150, easing = EaseOutBack),
                    initialOffsetY = { 30 }
                )
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.Start
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(24.dp))
                            .background(
                                brush = Brush.verticalGradient(
                                    colors = listOf(Color.White, Color(0xFFFAFAFA))
                                )
                            )
                            .border(1.dp, Color(0xFFE8ECEF), RoundedCornerShape(24.dp))
                            .shadow(
                                elevation = 8.dp,
                                shape = RoundedCornerShape(24.dp),
                                spotColor = Color(0x12000000)
                            )
                            .padding(24.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            RegisterRoleCard(
                                title = "Comprador",
                                subtitle = "Compra directo",
                                icon = Icons.Filled.ShoppingBasket,
                                selected = !esCampesino,
                                accentColor = Brand600,
                                onClick = { esCampesino = false },
                                modifier = Modifier.weight(1f)
                            )
                            RegisterRoleCard(
                                title = "Productor",
                                subtitle = "Vende tu cosecha",
                                icon = Icons.Filled.Agriculture,
                                selected = esCampesino,
                                accentColor = Amber600,
                                onClick = { esCampesino = true },
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        RegisterFieldLabel("Nombre de usuario")
                        Spacer(modifier = Modifier.height(6.dp))
                        RegisterField(
                            value = username,
                            onValueChange = { username = it },
                            placeholder = "juan_agro",
                            leadingIcon = Icons.Outlined.Person,
                            accentColor = activeBrand,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                            keyboardActions = KeyboardActions(
                                onNext = { focusManager.moveFocus(FocusDirection.Down) }
                            )
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        RegisterFieldLabel("Nombre completo")
                        Spacer(modifier = Modifier.height(6.dp))
                        RegisterField(
                            value = nombreCompleto,
                            onValueChange = { nombreCompleto = it },
                            placeholder = "Juan Perez",
                            leadingIcon = Icons.Outlined.Badge,
                            accentColor = activeBrand,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                            keyboardActions = KeyboardActions(
                                onNext = { focusManager.moveFocus(FocusDirection.Down) }
                            )
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        RegisterFieldLabel("Correo electronico")
                        Spacer(modifier = Modifier.height(6.dp))
                        RegisterField(
                            value = email,
                            onValueChange = { email = it },
                            placeholder = "nombre@ejemplo.com",
                            leadingIcon = Icons.Outlined.Email,
                            accentColor = activeBrand,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Email,
                                imeAction = ImeAction.Next
                            ),
                            keyboardActions = KeyboardActions(
                                onNext = { focusManager.moveFocus(FocusDirection.Down) }
                            )
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        RegisterFieldLabel("Telefono")
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .height(54.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(Color(0xFFF8FAFC))
                                    .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(14.dp))
                                    .padding(horizontal = 12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Filled.Public,
                                        contentDescription = null,
                                        tint = Slate500,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "+57",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = DarkSlate
                                    )
                                }
                            }

                            RegisterField(
                                value = telefono,
                                onValueChange = { newValue ->
                                    telefono = newValue.filter { it.isDigit() }
                                },
                                placeholder = "300 123 4567",
                                leadingIcon = Icons.Outlined.Phone,
                                accentColor = activeBrand,
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Phone,
                                    imeAction = ImeAction.Next
                                ),
                                keyboardActions = KeyboardActions(
                                    onNext = { focusManager.moveFocus(FocusDirection.Down) }
                                ),
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        RegisterFieldLabel("Contrasena")
                        Spacer(modifier = Modifier.height(6.dp))
                        RegisterField(
                            value = password,
                            onValueChange = { password = it },
                            placeholder = "Minimo 8 caracteres",
                            leadingIcon = Icons.Outlined.Lock,
                            accentColor = activeBrand,
                            isPassword = true,
                            passwordVisible = passwordVisible,
                            onTogglePassword = { passwordVisible = !passwordVisible },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Password,
                                imeAction = ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = { focusManager.clearFocus() }
                            ),
                            onFocusChange = { passwordFocused = it }
                        )

                        AnimatedVisibility(
                            visible = passwordFocused || password.isNotEmpty(),
                            enter = expandVertically(tween(400)) + fadeIn(tween(400)),
                            exit = shrinkVertically(tween(300)) + fadeOut(tween(300))
                        ) {
                            Column(modifier = Modifier.padding(top = 10.dp)) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(4.dp)
                                        .clip(RoundedCornerShape(2.dp))
                                        .background(Color(0xFFE2E8F0)),
                                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                                ) {
                                    val strengthColors = listOf(
                                        Color(0xFFEF4444), Color(0xFFF59E0B),
                                        Color(0xFFF59E0B), Color(0xFF22C55E)
                                    )
                                    repeat(4) { index ->
                                        val segColor by animateColorAsState(
                                            if (index < strengthScore) strengthColors[index]
                                            else Color(0xFFE2E8F0),
                                            tween(400), label = "seg"
                                        )
                                        Box(
                                            modifier = Modifier
                                                .weight(1f).fillMaxHeight()
                                                .clip(RoundedCornerShape(2.dp))
                                                .background(segColor)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                Text(
                                    text = when (strengthScore) {
                                        0 -> "Muy debil"
                                        1 -> "Debil"
                                        2 -> "Media"
                                        3 -> "Fuerte"
                                        4 -> "Muy fuerte"
                                        else -> ""
                                    },
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = when (strengthScore) {
                                        0, 1 -> Color(0xFFEF4444)
                                        2 -> Color(0xFFF59E0B)
                                        3, 4 -> Color(0xFF22C55E)
                                        else -> Slate400
                                    }
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        PasswordRuleItem("8+ caracteres", hasMinLength)
                                        PasswordRuleItem("Una mayuscula", hasUppercase)
                                    }
                                    Column(modifier = Modifier.weight(1f)) {
                                        PasswordRuleItem("Un numero", hasNumber)
                                        PasswordRuleItem("Caracter especial", hasSpecial)
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        val canSubmit = username.isNotBlank() && nombreCompleto.isNotBlank() && email.isNotBlank() && password.isNotBlank()

                        Button(
                            onClick = {
                                val rol = if (esCampesino) "CAMPESINO" else "CLIENTE"
                                val nuevoUsuario = Usuario(
                                    userName = username, email = email,
                                    nombreCompleto = nombreCompleto,
                                    telefono = telefono.ifBlank { null }, password = password, rol = rol
                                )
                                viewModel.registrarNuevoUsuario(nuevoUsuario)
                            },
                            enabled = canSubmit && !viewModel.estaCargando,
                            modifier = Modifier.fillMaxWidth().height(52.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = activeBrand,
                                disabledContainerColor = activeBrand.copy(alpha = 0.45f)
                            )
                        ) {
                            if (viewModel.estaCargando) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    strokeWidth = 2.dp, color = Color.White
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text("Creando cuenta...", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            } else {
                                Text("Crear cuenta", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                Spacer(modifier = Modifier.width(8.dp))
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                    contentDescription = null, tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }

                        if (viewModel.registroExitoso) {
                            Spacer(modifier = Modifier.height(14.dp))
                            Box(
                                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(Brand50).border(1.dp, Brand500.copy(alpha = 0.3f), RoundedCornerShape(14.dp)).padding(12.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Filled.CheckCircle, null, tint = Brand600, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text("Cuenta creada!", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Brand900)
                                        Text("Ahora puedes iniciar sesion.", fontSize = 11.sp, color = Brand600)
                                    }
                                }
                            }
                        }

                        if (viewModel.errorOperacion != null && !viewModel.registroExitoso) {
                            Spacer(modifier = Modifier.height(14.dp))
                            Box(
                                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(Color(0xFFFEF2F2)).border(1.dp, Color(0xFFEF4444).copy(alpha = 0.3f), RoundedCornerShape(14.dp)).padding(12.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Filled.ErrorOutline, null, tint = Color(0xFFEF4444), modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(viewModel.errorOperacion ?: "Error", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF991B1B))
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 32.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Ya tienes cuenta?  ",
                            fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Slate500
                        )
                        Text(
                            text = "Iniciar sesion",
                            fontSize = 14.sp, fontWeight = FontWeight.Bold, color = activeBrand,
                            modifier = Modifier.clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) { onNavigateToLogin() }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RegisterFieldLabel(text: String) {
    Text(text = text, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Slate700)
}

@Composable
private fun RegisterRoleCard(
    title: String, subtitle: String, icon: ImageVector,
    selected: Boolean, accentColor: Color, onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bgColor by animateColorAsState(
        if (selected) accentColor.copy(alpha = 0.08f) else Color(0xFFF8FAFC),
        tween(300), label = "roleBg"
    )
    val borderColor by animateColorAsState(
        if (selected) accentColor else Color(0xFFE2E8F0),
        tween(300), label = "roleBorder"
    )
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        if (isPressed) 0.95f else 1f,
        spring(dampingRatio = Spring.DampingRatioMediumBouncy), label = "roleScale"
    )

    Box(
        modifier = modifier
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .height(80.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(bgColor)
            .border(
                width = if (selected) 2.dp else 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(18.dp)
            )
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick)
            .padding(12.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = icon, contentDescription = null,
                tint = if (selected) accentColor else Slate400,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                title, fontSize = 12.sp, fontWeight = FontWeight.Bold,
                color = if (selected) accentColor else Slate500
            )
            Text(
                subtitle, fontSize = 10.sp, fontWeight = FontWeight.Medium,
                color = if (selected) accentColor.copy(alpha = 0.7f) else Slate400
            )
        }
        if (selected) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd).size(20.dp)
                    .clip(CircleShape).background(accentColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Check,
                    contentDescription = null, tint = Color.White,
                    modifier = Modifier.size(12.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RegisterField(
    value: String, onValueChange: (String) -> Unit, placeholder: String,
    leadingIcon: ImageVector, accentColor: Color,
    modifier: Modifier = Modifier, isPassword: Boolean = false,
    passwordVisible: Boolean = false, onTogglePassword: (() -> Unit)? = null,
    onFocusChange: ((Boolean) -> Unit)? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()
    val borderColor by animateColorAsState(
        if (isFocused) accentColor else Color(0xFFE2E8F0), tween(200), label = "tfBorder"
    )
    val bgColor by animateColorAsState(
        if (isFocused) Color.White else Color(0xFFF8FAFC), tween(200), label = "tfBg"
    )

    val focusModifier = if (onFocusChange != null) {
        Modifier.onFocusChanged { onFocusChange(it.isFocused) }
    } else Modifier

    OutlinedTextField(
        value = value, onValueChange = onValueChange,
        placeholder = { Text(placeholder, color = Slate400, fontSize = 14.sp, fontWeight = FontWeight.Medium) },
        leadingIcon = {
            Icon(
                imageVector = leadingIcon, contentDescription = null,
                tint = if (isFocused) accentColor else Slate400,
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
                        contentDescription = null, tint = Slate400,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        } else null,
        visualTransformation = if (isPassword && !passwordVisible) PasswordVisualTransformation() else VisualTransformation.None,
        singleLine = true,
        interactionSource = interactionSource,
        keyboardOptions = keyboardOptions, keyboardActions = keyboardActions,
        shape = RoundedCornerShape(14.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = bgColor, unfocusedContainerColor = bgColor,
            focusedBorderColor = borderColor, unfocusedBorderColor = borderColor,
            cursorColor = accentColor,
            focusedTextColor = DarkSlate, unfocusedTextColor = DarkSlate
        ),
        modifier = modifier.then(focusModifier).fillMaxWidth().height(54.dp)
    )
}

@Composable
private fun PasswordRuleItem(text: String, met: Boolean) {
    val iconColor by animateColorAsState(
        if (met) Color(0xFF22C55E) else Color(0xFFCBD5E1), tween(300), label = "ruleIcon"
    )
    val textColor by animateColorAsState(
        if (met) Color(0xFF15803D) else Color(0xFF94A3B8), tween(300), label = "ruleText"
    )
    Row(
        modifier = Modifier.padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = if (met) Icons.Filled.CheckCircle else Icons.Filled.RadioButtonUnchecked,
            contentDescription = null, tint = iconColor, modifier = Modifier.size(14.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(text, fontSize = 11.sp, fontWeight = FontWeight.Medium, color = textColor)
    }
}
