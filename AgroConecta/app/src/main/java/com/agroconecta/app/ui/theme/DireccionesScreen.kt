package com.agroconecta.app.ui.theme

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.agroconecta.app.data.model.Direccion
import com.agroconecta.app.viewmodel.TiendaViewModel

private val Brand600 = Color(0xFF16A34A)
private val Brand500 = Color(0xFF22C55E)
private val Brand400 = Color(0xFF4ADE80)
private val Brand50 = Color(0xFFF0FDF4)
private val DarkSlate = Color(0xFF0F172A)
private val Slate700 = Color(0xFF334155)
private val Slate500 = Color(0xFF64748B)
private val Slate400 = Color(0xFF94A3B8)
private val Slate300 = Color(0xFFCBD5E1)
private val Slate200 = Color(0xFFE2E8F0)
private val Slate100 = Color(0xFFF1F5F9)
private val SurfacePure = Color(0xFFF8FAF9)
private val Red500 = Color(0xFFEF4444)
private val Indigo500 = Color(0xFF6366F1)
private val Purple500 = Color(0xFF8B5CF6)
private val Amber500 = Color(0xFFF59E0B)
private val Blue500 = Color(0xFF3B82F6)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DireccionesScreen(
    tiendaVM: TiendaViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToMap: () -> Unit
) {
    var showNewForm by remember { mutableStateOf(false) }
    var showMap by remember { mutableStateOf(false) }
    var alias by remember { mutableStateOf("Casa") }
    var aliasPersonalizado by remember { mutableStateOf("") }
    var direccionCompleta by remember { mutableStateOf("") }
    var detalles by remember { mutableStateOf("") }
    var latitud by remember { mutableStateOf<Double?>(null) }
    var longitud by remember { mutableStateOf<Double?>(null) }
    var eliminarId by remember { mutableStateOf<Long?>(null) }
    var isVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { isVisible = true }

    LaunchedEffect(Unit) { tiendaVM.cargarDirecciones() }

    // Show map overlay inline instead of navigation
    if (showMap) {
        MapPickerScreen(
            latitudInicial = latitud ?: 4.5709,
            longitudInicial = longitud ?: -74.2973,
            onLocationSelected = { picked ->
                latitud = picked.latitud
                longitud = picked.longitud
                direccionCompleta = picked.direccion
                showMap = false
            },
            onNavigateBack = { showMap = false }
        )
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
                                "Mis Direcciones",
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
                                    "${tiendaVM.direccionesUsuario.size} guardadas",
                                    fontSize = 12.sp,
                                    color = Slate500,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                        if (tiendaVM.direccionesUsuario.isNotEmpty() && !showNewForm) {
                            var btnScale by remember { mutableStateOf(1f) }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(
                                        Brush.linearGradient(
                                            colors = listOf(Brand600, Brand500)
                                        )
                                    )
                                    .graphicsLayer { scaleX = btnScale; scaleY = btnScale }
                                    .clickable {
                                        btnScale = 0.95f
                                        alias = "Casa"; aliasPersonalizado = ""
                                        direccionCompleta = ""; detalles = ""
                                        latitud = null; longitud = null
                                        showNewForm = true
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Filled.Add, null, tint = Color.White, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(6.dp))
                                    Text("Nueva", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                }
                            }
                            LaunchedEffect(btnScale) {
                                if (btnScale != 1f) {
                                    kotlinx.coroutines.delay(100)
                                    btnScale = 1f
                                }
                            }
                        }
                    }
                }
            }
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            if (tiendaVM.estaCargando && tiendaVM.direccionesUsuario.isEmpty() && !showNewForm) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
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
                            "Cargando direcciones...",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Slate400
                        )
                    }
                }
                return@Column
            }

            if (showNewForm) {
                AnimatedVisibility(
                    visible = isVisible,
                    enter = fadeIn(tween(400)) + slideInVertically(tween(400, easing = EaseOutCubic), initialOffsetY = { it / 6 })
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                Modifier.size(46.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(Brush.linearGradient(listOf(Brand600, Brand500))),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Filled.AddLocation, null, tint = Color.White, modifier = Modifier.size(22.dp))
                            }
                            Spacer(Modifier.width(14.dp))
                            Column {
                                Text(
                                    "Nueva direccion",
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = DarkSlate,
                                    letterSpacing = (-0.3.sp)
                                )
                                Text(
                                    "Completa los datos de entrega",
                                    fontSize = 13.sp,
                                    color = Slate500
                                )
                            }
                        }
                        Spacer(Modifier.height(24.dp))

                        // Alias selector
                        Text("Alias", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Slate500, letterSpacing = 0.3.sp)
                        Spacer(Modifier.height(10.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            listOf(
                                "Casa" to Icons.Filled.Home,
                                "Trabajo" to Icons.Filled.Business,
                                "Finca" to Icons.Filled.Agriculture,
                                "Otro" to Icons.Filled.Edit
                            ).forEach { (label, icon) ->
                                Box(modifier = Modifier.weight(1f)) {
                                    PremiumAliasChip(
                                        label = label,
                                        icon = icon,
                                        selected = alias == label,
                                        onClick = { alias = label }
                                    )
                                }
                            }
                        }

                        if (alias == "Otro") {
                            Spacer(Modifier.height(12.dp))
                            OutlinedTextField(
                                value = aliasPersonalizado,
                                onValueChange = { aliasPersonalizado = it.take(20) },
                                placeholder = { Text("Ej: Apartamento, Bodega...", color = Slate400, fontSize = 13.sp) },
                                singleLine = true,
                                shape = RoundedCornerShape(16.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Brand600,
                                    unfocusedBorderColor = Slate200,
                                    cursorColor = Brand600,
                                    focusedContainerColor = Color.White,
                                    unfocusedContainerColor = Slate100.copy(alpha = 0.3f)
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        Spacer(Modifier.height(20.dp))

                        // Map selector
                        Text("Ubicacion en el mapa", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Slate500, letterSpacing = 0.3.sp)
                        Spacer(Modifier.height(10.dp))

                        if (latitud != null && direccionCompleta.isNotBlank()) {
                            var mapScale by remember { mutableStateOf(1f) }
                            Surface(
                                color = Brand50,
                                shape = RoundedCornerShape(20.dp),
                                border = BorderStroke(1.5.dp, Brand500.copy(alpha = 0.3f)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .graphicsLayer { scaleX = mapScale; scaleY = mapScale }
                                    .clickable {
                                        mapScale = 0.98f
                                        showMap = true
                                    }
                            ) {
                                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(48.dp)
                                            .clip(RoundedCornerShape(14.dp))
                                            .background(Brush.linearGradient(listOf(Brand600, Brand500))),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Filled.LocationOn, null, tint = Color.White, modifier = Modifier.size(24.dp))
                                    }
                                    Spacer(Modifier.width(14.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            direccionCompleta,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = DarkSlate,
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis,
                                            letterSpacing = (-0.2.sp)
                                        )
                                        Spacer(Modifier.height(3.dp))
                                        Text(
                                            "$latitud, $longitud",
                                            fontSize = 11.sp,
                                            color = Slate400
                                        )
                                    }
                                    Icon(Icons.Filled.Edit, null, tint = Brand600, modifier = Modifier.size(20.dp))
                                }
                            }
                            LaunchedEffect(mapScale) {
                                if (mapScale != 1f) {
                                    kotlinx.coroutines.delay(100)
                                    mapScale = 1f
                                }
                            }
                        } else {
                            var btnScale by remember { mutableStateOf(1f) }
                            OutlinedButton(
                                onClick = {
                                    btnScale = 0.97f
                                    showMap = true
                                },
                                shape = RoundedCornerShape(16.dp),
                                border = BorderStroke(1.5.dp, Brand600),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(54.dp)
                                    .graphicsLayer { scaleX = btnScale; scaleY = btnScale }
                            ) {
                                Icon(Icons.Filled.Map, null, tint = Brand600, modifier = Modifier.size(20.dp))
                                Spacer(Modifier.width(10.dp))
                                Text(
                                    "Seleccionar en el mapa",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Brand600
                                )
                            }
                            LaunchedEffect(btnScale) {
                                if (btnScale != 1f) {
                                    kotlinx.coroutines.delay(100)
                                    btnScale = 1f
                                }
                            }
                        }

                        Spacer(Modifier.height(20.dp))

                        // Address input
                        Text("Direccion completa", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Slate500, letterSpacing = 0.3.sp)
                        Spacer(Modifier.height(8.dp))
                        OutlinedTextField(
                            value = direccionCompleta,
                            onValueChange = { direccionCompleta = it },
                            placeholder = { Text("Carrera 70 # 64 - 25", color = Slate400, fontSize = 14.sp) },
                            leadingIcon = { Icon(Icons.Outlined.LocationOn, null, tint = Slate400, modifier = Modifier.size(20.dp)) },
                            singleLine = true,
                            shape = RoundedCornerShape(16.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Brand600,
                                unfocusedBorderColor = Slate200,
                                cursorColor = Brand600,
                                focusedContainerColor = Color.White,
                                unfocusedContainerColor = Slate100.copy(alpha = 0.3f)
                            ),
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
                        )

                        Spacer(Modifier.height(16.dp))

                        // Details input
                        Text("Detalles (piso, apto, referencia)", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Slate500, letterSpacing = 0.3.sp)
                        Spacer(Modifier.height(8.dp))
                        OutlinedTextField(
                            value = detalles,
                            onValueChange = { detalles = it },
                            placeholder = { Text("Apto 301, Torre B, junto al parque", color = Slate400, fontSize = 14.sp) },
                            leadingIcon = { Icon(Icons.Outlined.Info, null, tint = Slate400, modifier = Modifier.size(20.dp)) },
                            singleLine = true,
                            shape = RoundedCornerShape(16.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Brand600,
                                unfocusedBorderColor = Slate200,
                                cursorColor = Brand600,
                                focusedContainerColor = Color.White,
                                unfocusedContainerColor = Slate100.copy(alpha = 0.3f)
                            ),
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done)
                        )

                        Spacer(Modifier.height(28.dp))

                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            var cancelScale by remember { mutableStateOf(1f) }
                            OutlinedButton(
                                onClick = {
                                    cancelScale = 0.95f
                                    showNewForm = false
                                },
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(52.dp)
                                    .graphicsLayer { scaleX = cancelScale; scaleY = cancelScale }
                            ) {
                                Text("Cancelar", color = Slate500, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            }
                            LaunchedEffect(cancelScale) {
                                if (cancelScale != 1f) {
                                    kotlinx.coroutines.delay(100)
                                    cancelScale = 1f
                                }
                            }

                            var saveScale by remember { mutableStateOf(1f) }
                            Button(
                                onClick = {
                                    saveScale = 0.97f
                                    val finalAlias = if (alias == "Otro" && aliasPersonalizado.isNotBlank()) aliasPersonalizado else alias
                                    tiendaVM.agregarDireccion(
                                        Direccion(
                                            alias = finalAlias,
                                            direccionCompleta = direccionCompleta.ifBlank { "Sin direccion" },
                                            detalles = detalles.ifBlank { null },
                                            latitud = latitud,
                                            longitud = longitud
                                        )
                                    )
                                    showNewForm = false
                                },
                                enabled = direccionCompleta.isNotBlank(),
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Brand600,
                                    contentColor = Color.White
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(52.dp)
                                    .graphicsLayer { scaleX = saveScale; scaleY = saveScale }
                            ) {
                                Icon(Icons.Filled.Save, null, tint = Color.White, modifier = Modifier.size(20.dp))
                                Spacer(Modifier.width(8.dp))
                                Text("Guardar", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 14.sp)
                            }
                            LaunchedEffect(saveScale) {
                                if (saveScale != 1f) {
                                    kotlinx.coroutines.delay(100)
                                    saveScale = 1f
                                }
                            }
                        }
                        Spacer(Modifier.height(24.dp))
                    }
                }
            } else if (tiendaVM.direccionesUsuario.isEmpty()) {
                AnimatedVisibility(
                    visible = isVisible,
                    enter = fadeIn(tween(600)) + scaleIn(tween(600))
                ) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(32.dp)) {
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
                                            colors = listOf(Brand600.copy(alpha = 0.08f), Color.Transparent)
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
                                                colors = listOf(Brand50, Color.White)
                                            )
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Filled.LocationOn, null, tint = Brand600, modifier = Modifier.size(44.dp))
                                }
                            }
                            Spacer(Modifier.height(28.dp))
                            Text(
                                "Sin direcciones",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = DarkSlate,
                                letterSpacing = (-0.3.sp)
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                "Agrega tu primera direccion de entrega",
                                fontSize = 14.sp,
                                color = Slate500,
                                textAlign = TextAlign.Center
                            )
                            Spacer(Modifier.height(28.dp))
                            var btnScale by remember { mutableStateOf(1f) }
                            Button(
                                onClick = {
                                    btnScale = 0.97f
                                    showNewForm = true
                                },
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Brand600,
                                    contentColor = Color.White
                                ),
                                contentPadding = PaddingValues(horizontal = 32.dp, vertical = 14.dp),
                                modifier = Modifier.graphicsLayer { scaleX = btnScale; scaleY = btnScale }
                            ) {
                                Icon(Icons.Filled.Add, null, tint = Color.White, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    "Agregar direccion",
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    fontSize = 14.sp
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
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        AnimatedVisibility(
                            visible = isVisible,
                            enter = fadeIn(tween(500, delayMillis = 200)) + slideInVertically(tween(500, delayMillis = 200, easing = EaseOutCubic), initialOffsetY = { -it / 6 })
                        ) {
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        Modifier.size(46.dp)
                                            .clip(RoundedCornerShape(16.dp))
                                            .background(Brush.linearGradient(listOf(Brand600, Brand500))),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Filled.LocationOn, null, tint = Color.White, modifier = Modifier.size(22.dp))
                                    }
                                    Spacer(Modifier.width(14.dp))
                                    Column {
                                        Text(
                                            "Tus direcciones guardadas",
                                            fontSize = 20.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = DarkSlate,
                                            letterSpacing = (-0.3.sp)
                                        )
                                        Text(
                                            "Selecciona una principal o agrega otra",
                                            fontSize = 13.sp,
                                            color = Slate500
                                        )
                                    }
                                }
                                Spacer(Modifier.height(16.dp))
                            }
                        }
                    }

                    itemsIndexed(
                        tiendaVM.direccionesUsuario,
                        key = { _, item -> item.id ?: item.hashCode() }
                    ) { index, dir ->
                        AnimatedVisibility(
                            visible = isVisible,
                            enter = fadeIn(tween(400, delayMillis = 300 + index * 100)) +
                                    slideInVertically(
                                        tween(400, delayMillis = 300 + index * 100, easing = EaseOutCubic),
                                        initialOffsetY = { it / 6 }
                                    )
                        ) {
                            PremiumDireccionCard(
                                dir = dir,
                                onSetPrincipal = { dir.id?.let { tiendaVM.marcarDireccionPrincipal(it) } },
                                onDelete = { eliminarId = dir.id }
                            )
                        }
                    }
                }
            }
        }
    }

    if (eliminarId != null) {
        AlertDialog(
            onDismissRequest = { eliminarId = null },
            containerColor = Color.White,
            shape = RoundedCornerShape(24.dp),
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Brush.linearGradient(listOf(Red500, Color(0xFFDC2626)))),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Filled.Delete, null, tint = Color.White, modifier = Modifier.size(20.dp))
                    }
                    Spacer(Modifier.width(12.dp))
                    Text(
                        "Eliminar direccion",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = DarkSlate,
                        letterSpacing = (-0.2.sp)
                    )
                }
            },
            text = {
                Text(
                    "Esta accion no se puede deshacer.",
                    fontSize = 14.sp,
                    color = Slate500
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        eliminarId?.let { tiendaVM.eliminarDireccion(it) }
                        eliminarId = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Red500),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text("Eliminar", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { eliminarId = null },
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text("Cancelar", fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}

@Composable
private fun PremiumAliasChip(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    selected: Boolean,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        if (isPressed) 0.95f else 1f,
        spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow)
    )

    Surface(
        color = if (selected) Brand50 else Color.White,
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(
            1.5.dp,
            if (selected) Brand600 else Slate200
        ),
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .clickable(interactionSource = interactionSource, indication = null) { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                icon,
                null,
                tint = if (selected) Brand600 else Slate400,
                modifier = Modifier.size(16.dp)
            )
            Spacer(Modifier.width(4.dp))
            Text(
                label,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = if (selected) Brand600 else Slate500
            )
        }
    }
}

@Composable
private fun PremiumDireccionCard(
    dir: Direccion,
    onSetPrincipal: () -> Unit,
    onDelete: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        if (isPressed) 0.98f else 1f,
        spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow)
    )

    val isPrincipal = dir.esPrincipal == true

    Surface(
        color = if (isPrincipal) Brand50 else Color.White,
        shape = RoundedCornerShape(24.dp),
        shadowElevation = 0.dp,
        border = BorderStroke(
            if (isPrincipal) 2.dp else 1.dp,
            if (isPrincipal) Brand600 else Slate200
        ),
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .clickable(interactionSource = interactionSource, indication = null) {}
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            if (isPrincipal)
                                Brush.linearGradient(listOf(Brand600, Brand500))
                            else
                                Brush.linearGradient(listOf(Slate100, Color.White))
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        when (dir.alias) {
                            "Trabajo" -> Icons.Filled.Business
                            "Finca" -> Icons.Filled.Agriculture
                            else -> Icons.Filled.Home
                        },
                        null,
                        tint = if (isPrincipal) Color.White else Brand600,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            dir.alias ?: "Direccion",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = DarkSlate,
                            letterSpacing = (-0.2.sp)
                        )
                        if (isPrincipal) {
                            Spacer(Modifier.width(8.dp))
                            Surface(
                                color = Brand600,
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    "Principal",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                    Spacer(Modifier.height(4.dp))
                    Text(
                        dir.direccionCompleta ?: "Sin direccion",
                        fontSize = 13.sp,
                        color = Slate500,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (!dir.detalles.isNullOrBlank()) {
                        Spacer(Modifier.height(4.dp))
                        Text(
                            dir.detalles,
                            fontSize = 12.sp,
                            color = Slate400
                        )
                    }
                }
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                listOf(Red500.copy(alpha = 0.1f), Red500.copy(alpha = 0.05f))
                            )
                        )
                ) {
                    Icon(
                        Icons.Outlined.Delete,
                        "Eliminar",
                        tint = Red500,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            if (!isPrincipal) {
                Spacer(Modifier.height(12.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            Brush.linearGradient(
                                listOf(Brand600.copy(alpha = 0.08f), Brand500.copy(alpha = 0.04f))
                            )
                        )
                        .clickable { onSetPrincipal() }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            Icons.Outlined.PushPin,
                            null,
                            tint = Brand600,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "Usar como principal",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Brand600
                        )
                    }
                }
            }
        }
    }
}
