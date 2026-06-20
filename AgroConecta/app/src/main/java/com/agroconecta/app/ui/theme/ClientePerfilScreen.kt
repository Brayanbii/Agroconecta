package com.agroconecta.app.ui.theme

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.agroconecta.app.data.api.RetrofitClient
import com.agroconecta.app.data.model.ClientePerfilResponse
import com.agroconecta.app.data.model.Pedido
import com.agroconecta.app.viewmodel.TiendaViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileOutputStream

private val P900 = Color(0xFF022C22)
private val P800 = Color(0xFF064E3B)
private val P700 = Color(0xFF065F46)
private val P600 = Color(0xFF047857)
private val P500 = Color(0xFF059669)
private val G600 = Color(0xFF16A34A)
private val G500 = Color(0xFF22C55E)
private val G400 = Color(0xFF4ADE80)
private val G300 = Color(0xFF86EFAC)
private val G200 = Color(0xFFBBF7D0)
private val G100 = Color(0xFFDCFCE7)
private val G50 = Color(0xFFF0FDF4)
private val Carbon = Color(0xFF0F172A)
private val Dark = Color(0xFF1E293B)
private val S500 = Color(0xFF64748B)
private val S400 = Color(0xFF94A3B8)
private val S300 = Color(0xFFCBD5E1)
private val S200 = Color(0xFFE2E8F0)
private val S100 = Color(0xFFF1F5F9)
private val S50 = Color(0xFFF8FAFC)
private val Surface = Color(0xFFF8FAF8)
private val Red500 = Color(0xFFEF4444)
private val Red50 = Color(0xFFFEF2F2)
private val Blue500 = Color(0xFF3B82F6)
private val Blue50 = Color(0xFFEFF6FF)
private val Amber500 = Color(0xFFF59E0B)
private val Amber50 = Color(0xFFFFFBEB)
private val Orange500 = Color(0xFFF97316)
private val Purple500 = Color(0xFF7C3AED)
private val Purple50 = Color(0xFFF5F3FF)
private val Rose500 = Color(0xFFF43F5E)
private val Pink50 = Color(0xFFFDF2F8)

private val BASE_IMAGE_URL get() = com.agroconecta.app.data.api.ApiConfig.IMAGES_URL

private enum class ClienteTab(val label: String, val icon: ImageVector) {
    AJUSTES("Ajustes", Icons.Filled.Settings),
    CREDITOS("Creditos", Icons.Filled.AccountBalanceWallet),
    PAGOS("Pagos", Icons.Filled.CreditCard),
    NOTIFICACIONES("Alertas", Icons.Filled.Notifications),
    ORDENES("Ordenes", Icons.Filled.ReceiptLong),
    FAVORITOS("Favoritos", Icons.Filled.Favorite),
    SOPORTE("Soporte", Icons.Filled.HeadsetMic)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClientePerfilScreen(
    usuario: com.agroconecta.app.data.model.UsuarioInfo,
    tiendaVM: TiendaViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToDirecciones: () -> Unit,
    onNavigateToCompras: () -> Unit,
    onNavigateToFavoritos: () -> Unit,
    onLogout: () -> Unit
) {
    LaunchedEffect(Unit) { tiendaVM.cargarClientePerfil(); tiendaVM.cargarPedidos(); tiendaVM.cargarFavoritos() }
    var currentTab by remember { mutableStateOf(ClienteTab.AJUSTES) }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var subiendoFoto by remember { mutableStateOf(false) }
    var isVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { isVisible = true }

    val data = tiendaVM.clientePerfil
    var editNombre by remember(data) { mutableStateOf(data?.nombreCompleto ?: "") }
    var editTelefono by remember(data) { mutableStateOf(data?.telefono ?: "") }
    var editIdentidad by remember(data) { mutableStateOf(data?.numeroIdentidad ?: "") }
    var editFecha by remember(data) { mutableStateOf(data?.fechaNacimiento ?: "") }
    var editGenero by remember(data) { mutableStateOf(data?.genero ?: "") }
    var fotoLocal by remember(data) { mutableStateOf(data?.fotoPerfil) }

    val imagePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let { u ->
            scope.launch {
                subiendoFoto = true
                try {
                    val file = withContext(Dispatchers.IO) {
                        val input = context.contentResolver.openInputStream(u)
                        val tempFile = File(context.cacheDir, "upload_${System.currentTimeMillis()}.jpg")
                        input?.use { it.copyTo(FileOutputStream(tempFile)) }
                        tempFile
                    }
                    if (!file.exists()) return@launch
                    val reqBody = file.asRequestBody("image/*".toMediaTypeOrNull())
                    val part = MultipartBody.Part.createFormData("file", file.name, reqBody)
                    val resp = RetrofitClient.usuarioApiService.subirImagen(part)
                    val filename = resp["filename"] as? String
                    if (filename != null) {
                        fotoLocal = filename
                        tiendaVM.actualizarClientePerfil(editNombre, editTelefono, editIdentidad, editFecha, editGenero, filename)
                    }
                } catch (_: Exception) {} finally { subiendoFoto = false }
            }
        }
    }

    Scaffold(containerColor = Surface, topBar = {
        AnimatedVisibility(
            visible = isVisible,
            enter = fadeIn(tween(500)) + slideInVertically(tween(500, easing = EaseOutCubic), initialOffsetY = { -it / 3 })
        ) {
            Surface(
                color = Color.White,
                shadowElevation = 0.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        var backScale by remember { mutableStateOf(1f) }
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.linearGradient(listOf(S100, S50))
                                )
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
                            if (backScale != 1f) { kotlinx.coroutines.delay(100); backScale = 1f }
                        }
                        Spacer(Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Mi Perfil", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Carbon, letterSpacing = (-0.5).sp)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                val inf = rememberInfiniteTransition(label = "status")
                                val pulse by inf.animateFloat(0.5f, 1f, infiniteRepeatable(tween(1500), RepeatMode.Reverse), label = "p")
                                Box(modifier = Modifier.size(7.dp).clip(CircleShape).background(G500.copy(alpha = pulse)))
                                Spacer(Modifier.width(6.dp))
                                Text(data?.email ?: usuario.email ?: "", fontSize = 11.sp, color = S400, fontWeight = FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            }
                        }
                        var logoutScale by remember { mutableStateOf(1f) }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    Brush.linearGradient(listOf(Red500.copy(alpha = 0.08f), Red500.copy(alpha = 0.04f)))
                                )
                                .graphicsLayer { scaleX = logoutScale; scaleY = logoutScale }
                                .clickable {
                                    logoutScale = 0.93f
                                    onLogout()
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Row(modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.Logout, null, tint = Red500, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("Salir", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Red500)
                            }
                        }
                        LaunchedEffect(logoutScale) {
                            if (logoutScale != 1f) { kotlinx.coroutines.delay(100); logoutScale = 1f }
                        }
                    }
                }
            }
        }
    }) { padding ->
        var seccionExpandida by remember { mutableStateOf("") }

        if (seccionExpandida.isEmpty()) {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    AnimatedVisibility(
                        visible = isVisible,
                        enter = fadeIn(tween(600)) + slideInVertically(tween(600, easing = EaseOutCubic), initialOffsetY = { it / 4 })
                    ) {
                        PremiumProfileCard(data, fotoLocal, usuario)
                    }
                }

                item {
                    AnimatedVisibility(
                        visible = isVisible,
                        enter = fadeIn(tween(600, delayMillis = 100)) + slideInVertically(tween(600, delayMillis = 100, easing = EaseOutCubic), initialOffsetY = { it / 4 })
                    ) {
                        Text("Configuracion", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = S400, modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp))
                    }
                }

                item {
                    AnimatedVisibility(
                        visible = isVisible,
                        enter = fadeIn(tween(600, delayMillis = 150)) + slideInVertically(tween(600, delayMillis = 150, easing = EaseOutCubic), initialOffsetY = { it / 4 })
                    ) {
                        PremiumMenuItemCard("Ajustes de cuenta", Icons.Filled.Settings, G600, G50, "Nombre, telefono, identidad y mas") { seccionExpandida = "ajustes" }
                    }
                }

                item {
                    AnimatedVisibility(
                        visible = isVisible,
                        enter = fadeIn(tween(600, delayMillis = 200)) + slideInVertically(tween(600, delayMillis = 200, easing = EaseOutCubic), initialOffsetY = { it / 4 })
                    ) {
                        PremiumMenuItemCard("AgroCreditos", Icons.Filled.AccountBalanceWallet, Amber500, Amber50, "Saldo: \$${String.format("%,.0f", data?.creditos ?: 0.0)}") { seccionExpandida = "creditos" }
                    }
                }

                item {
                    AnimatedVisibility(
                        visible = isVisible,
                        enter = fadeIn(tween(600, delayMillis = 250)) + slideInVertically(tween(600, delayMillis = 250, easing = EaseOutCubic), initialOffsetY = { it / 4 })
                    ) {
                        PremiumMenuItemCard("Metodos de pago", Icons.Filled.CreditCard, Purple500, Purple50, "Gestiona tus tarjetas") { seccionExpandida = "pagos" }
                    }
                }

                item {
                    AnimatedVisibility(
                        visible = isVisible,
                        enter = fadeIn(tween(600, delayMillis = 300)) + slideInVertically(tween(600, delayMillis = 300, easing = EaseOutCubic), initialOffsetY = { it / 4 })
                    ) {
                        Text("Actividad", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = S400, modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp))
                    }
                }

                item {
                    AnimatedVisibility(
                        visible = isVisible,
                        enter = fadeIn(tween(600, delayMillis = 350)) + slideInVertically(tween(600, delayMillis = 350, easing = EaseOutCubic), initialOffsetY = { it / 4 })
                    ) {
                        PremiumMenuItemCard("Notificaciones", Icons.Filled.NotificationsActive, Color(0xFF14B8A6), Color(0xFFCCFBF1), "Enterate de novedades") { seccionExpandida = "notificaciones" }
                    }
                }

                item {
                    AnimatedVisibility(
                        visible = isVisible,
                        enter = fadeIn(tween(600, delayMillis = 400)) + slideInVertically(tween(600, delayMillis = 400, easing = EaseOutCubic), initialOffsetY = { it / 4 })
                    ) {
                        PremiumMenuItemCard("Mis ordenes", Icons.Filled.ReceiptLong, Blue500, Blue50, "${tiendaVM.pedidos.size} pedidos recientes") { seccionExpandida = "ordenes" }
                    }
                }

                item {
                    AnimatedVisibility(
                        visible = isVisible,
                        enter = fadeIn(tween(600, delayMillis = 450)) + slideInVertically(tween(600, delayMillis = 450, easing = EaseOutCubic), initialOffsetY = { it / 4 })
                    ) {
                        PremiumMenuItemCard("Mis Favoritos", Icons.Filled.Favorite, Rose500, Pink50, "${tiendaVM.listaFavoritos.size} productos guardados") { seccionExpandida = "favoritos" }
                    }
                }

                item {
                    AnimatedVisibility(
                        visible = isVisible,
                        enter = fadeIn(tween(600, delayMillis = 500)) + slideInVertically(tween(600, delayMillis = 500, easing = EaseOutCubic), initialOffsetY = { it / 4 })
                    ) {
                        Text("Ayuda", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = S400, modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp))
                    }
                }

                item {
                    AnimatedVisibility(
                        visible = isVisible,
                        enter = fadeIn(tween(600, delayMillis = 550)) + slideInVertically(tween(600, delayMillis = 550, easing = EaseOutCubic), initialOffsetY = { it / 4 })
                    ) {
                        PremiumMenuItemCard("Soporte", Icons.Filled.HeadsetMic, Orange500, Color(0xFFFFEDD5), "Contacta con nuestro equipo") { seccionExpandida = "soporte" }
                    }
                }

                item {
                    Spacer(Modifier.height(16.dp))
                    AnimatedVisibility(
                        visible = isVisible,
                        enter = fadeIn(tween(800, delayMillis = 600)) + slideInVertically(tween(800, delayMillis = 600, easing = EaseOutCubic), initialOffsetY = { it / 3 })
                    ) {
                        PremiumFooterColombia()
                    }
                    Spacer(Modifier.height(24.dp))
                }
            }
        } else {
            Column(Modifier.fillMaxSize().padding(padding)) {
                Surface(
                    Modifier.fillMaxWidth(),
                    color = Color.White,
                    shadowElevation = 2.dp
                ) {
                    Row(
                        Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        var backScale by remember { mutableStateOf(1f) }
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(S100)
                                .graphicsLayer { scaleX = backScale; scaleY = backScale }
                                .clickable {
                                    backScale = 0.9f
                                    seccionExpandida = ""
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Carbon, modifier = Modifier.size(20.dp))
                        }
                        LaunchedEffect(backScale) {
                            if (backScale != 1f) { kotlinx.coroutines.delay(100); backScale = 1f }
                        }
                        Spacer(Modifier.width(12.dp))
                        Text(
                            when (seccionExpandida) {
                                "ajustes" -> "Ajustes de cuenta"; "creditos" -> "AgroCreditos"
                                "pagos" -> "Metodos de pago"; "notificaciones" -> "Notificaciones"
                                "ordenes" -> "Mis ordenes"; "favoritos" -> "Mis Favoritos"
                                "soporte" -> "Soporte"; else -> ""
                            },
                            fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Carbon, letterSpacing = (-0.3).sp
                        )
                    }
                }

                Box(Modifier.weight(1f).fillMaxWidth()) {
                    when (seccionExpandida) {
                        "ajustes" -> PerfilAjustesTab(data, editNombre, editTelefono, editIdentidad, editFecha, editGenero, fotoLocal,
                            onNombreChange = { editNombre = it }, onTelefonoChange = { editTelefono = it },
                            onIdentidadChange = { editIdentidad = it }, onFechaChange = { editFecha = it },
                            onGeneroChange = { editGenero = it }, imagePicker, tiendaVM, subiendoFoto)
                        "creditos" -> CreditosTab(data)
                        "pagos" -> PagosTab()
                        "notificaciones" -> NotificacionesTab()
                        "ordenes" -> OrdenesTab(tiendaVM.pedidos, onNavigateToCompras)
                        "favoritos" -> FavoritosTab(tiendaVM.listaFavoritos, onNavigateToFavoritos)
                        "soporte" -> SoporteMiniTab(tiendaVM)
                    }
                }
            }
        }
    }
}

@Composable
private fun PremiumProfileCard(
    data: ClientePerfilResponse?,
    fotoLocal: String?,
    usuario: com.agroconecta.app.data.model.UsuarioInfo
) {
    val inf = rememberInfiniteTransition(label = "profileGlow")
    val glowAlpha by inf.animateFloat(0.3f, 0.6f, infiniteRepeatable(tween(2500), RepeatMode.Reverse), label = "glow")

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .background(
                Brush.linearGradient(
                    listOf(P800, P700, P600, G600)
                )
            )
            .drawBehind {
                drawCircle(
                    color = G400.copy(alpha = glowAlpha * 0.15f),
                    radius = size.width * 0.4f,
                    center = Offset(size.width * 0.85f, size.height * 0.2f)
                )
                drawCircle(
                    color = G300.copy(alpha = glowAlpha * 0.1f),
                    radius = size.width * 0.3f,
                    center = Offset(size.width * 0.15f, size.height * 0.8f)
                )
                drawCircle(
                    color = Color.White.copy(alpha = 0.04f),
                    radius = size.width * 0.2f,
                    center = Offset(size.width * 0.5f, size.height * 0.5f)
                )
            }
            .padding(20.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            val imgUrl = if (!fotoLocal.isNullOrBlank() && fotoLocal != "default.png") { if (fotoLocal!!.startsWith("http")) fotoLocal else BASE_IMAGE_URL + fotoLocal } else null

            Box(contentAlignment = Alignment.Center) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                listOf(G400.copy(alpha = 0.3f), Color.Transparent)
                            )
                        )
                )
                Box(
                    modifier = Modifier
                        .size(68.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(listOf(G500, G600, P600))
                        )
                        .border(2.5.dp, Color.White.copy(alpha = 0.3f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    if (imgUrl != null) {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current).data(imgUrl).crossfade(true).build(),
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize().clip(CircleShape)
                        )
                    } else {
                        Text(
                            data?.nombreCompleto?.firstOrNull()?.uppercase() ?: "U",
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }

            Spacer(Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    data?.nombreCompleto ?: usuario.userName ?: "Mi Cuenta",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    letterSpacing = (-0.3).sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(3.dp))
                Text(
                    data?.email ?: usuario.email ?: "",
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.6f),
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if ((data?.creditos ?: 0.0) > 0.0) {
                    Spacer(Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color.White.copy(alpha = 0.15f))
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.AccountBalanceWallet, null, tint = G300, modifier = Modifier.size(12.dp))
                            Spacer(Modifier.width(5.dp))
                            Text(
                                "\$${String.format("%,.0f", data?.creditos ?: 0.0)} AgroCreditos",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PremiumMenuItemCard(
    titulo: String,
    icon: ImageVector,
    color: Color,
    bgColor: Color,
    subtitulo: String,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        if (isPressed) 0.97f else 1f,
        spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "menuPress"
    )

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .clickable(interactionSource = interactionSource, indication = null) { onClick() },
        color = Color.White,
        shape = RoundedCornerShape(20.dp),
        shadowElevation = 0.dp,
        border = BorderStroke(1.dp, S200)
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(
                        Brush.linearGradient(listOf(bgColor, bgColor.copy(alpha = 0.5f)))
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = color, modifier = Modifier.size(22.dp))
            }
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(titulo, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Carbon, letterSpacing = (-0.2).sp)
                Spacer(Modifier.height(2.dp))
                Text(subtitulo, fontSize = 12.sp, color = S400, fontWeight = FontWeight.Medium)
            }
            Icon(Icons.Filled.ChevronRight, null, tint = S300, modifier = Modifier.size(22.dp))
        }
    }
}

@Composable
private fun PremiumFooterColombia() {
    val inf = rememberInfiniteTransition(label = "footerAnim")
    val heartScale by inf.animateFloat(0.85f, 1.2f, infiniteRepeatable(tween(1200), RepeatMode.Reverse), label = "heart")
    val leafRotation by inf.animateFloat(-10f, 10f, infiniteRepeatable(tween(2500), RepeatMode.Reverse), label = "leaf")
    val glowAlpha by inf.animateFloat(0.4f, 0.8f, infiniteRepeatable(tween(2000), RepeatMode.Reverse), label = "glow")
    val floatY by inf.animateFloat(-3f, 3f, infiniteRepeatable(tween(3000), RepeatMode.Reverse), label = "float")

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(32.dp))
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color.White,
                        G50.copy(alpha = 0.6f),
                        G100.copy(alpha = 0.4f),
                        G50.copy(alpha = 0.2f)
                    )
                )
            )
            .border(1.5.dp, G200.copy(alpha = 0.6f), RoundedCornerShape(32.dp))
            .drawBehind {
                drawCircle(
                    brush = Brush.radialGradient(
                        listOf(
                            G400.copy(alpha = glowAlpha * 0.1f),
                            Color.Transparent
                        )
                    ),
                    radius = size.width * 0.5f,
                    center = Offset(size.width * 0.5f, size.height * 0.5f)
                )
            }
            .padding(vertical = 32.dp, horizontal = 24.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                listOf(Red500.copy(alpha = 0.12f), Color.Transparent)
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Filled.Favorite,
                        null,
                        tint = Red500,
                        modifier = Modifier
                            .size(22.dp)
                            .graphicsLayer { scaleX = heartScale; scaleY = heartScale }
                    )
                }

                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                listOf(G600.copy(alpha = 0.12f), Color.Transparent)
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Filled.Eco,
                        null,
                        tint = G600,
                        modifier = Modifier
                            .size(22.dp)
                            .graphicsLayer { rotationZ = leafRotation }
                    )
                }
            }

            Spacer(Modifier.height(20.dp))

            Text(
                text = "Hecho con amor",
                fontSize = 24.sp,
                fontWeight = FontWeight.Black,
                style = TextStyle(
                    brush = Brush.linearGradient(
                        listOf(
                            P700,
                            G600,
                            G500,
                            G600,
                            P700
                        )
                    )
                ),
                letterSpacing = (-0.5).sp
            )

            Spacer(Modifier.height(6.dp))

            Text(
                text = "en Colombia",
                fontSize = 32.sp,
                fontWeight = FontWeight.ExtraBold,
                style = TextStyle(
                    brush = Brush.linearGradient(
                        listOf(
                            Color(0xFF064E3B),
                            Color(0xFF047857),
                            Color(0xFF059669),
                            Color(0xFF16A34A),
                            Color(0xFF22C55E),
                            Color(0xFF16A34A),
                            Color(0xFF059669),
                            Color(0xFF047857),
                            Color(0xFF064E3B)
                        )
                    )
                ),
                letterSpacing = (-0.8).sp
            )

            Spacer(Modifier.height(18.dp))

            Box(
                modifier = Modifier
                    .width(100.dp)
                    .height(3.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(
                        Brush.horizontalGradient(
                            listOf(
                                Color.Transparent,
                                G300.copy(alpha = 0.6f),
                                G500,
                                G400,
                                G300.copy(alpha = 0.6f),
                                Color.Transparent
                            )
                        )
                    )
            )

            Spacer(Modifier.height(16.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(G400)
                )
                Text(
                    "AgroConecta",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = S500,
                    letterSpacing = 0.5.sp
                )
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(G400)
                )
            }

            Spacer(Modifier.height(20.dp))

            Text(
                text = "Version 1.0",
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = S400,
                letterSpacing = 0.3.sp
            )

            Spacer(Modifier.height(6.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Hecho con ",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = S400,
                    letterSpacing = 0.3.sp
                )
                val miniHeartScale by inf.animateFloat(0.85f, 1.15f, infiniteRepeatable(tween(1000), RepeatMode.Reverse), label = "miniHeart")
                Icon(
                    Icons.Filled.Favorite,
                    null,
                    tint = G500,
                    modifier = Modifier
                        .size(12.dp)
                        .graphicsLayer { scaleX = miniHeartScale; scaleY = miniHeartScale }
                )
                Spacer(Modifier.width(2.dp))
                Text(
                    text = "en COLOMBIA",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = G600,
                    letterSpacing = 0.5.sp
                )
            }
        }
    }
}

@Composable
private fun PerfilAjustesTab(
    data: ClientePerfilResponse?, nombre: String, telefono: String, identidad: String, fecha: String, genero: String,
    foto: String?, onNombreChange: (String) -> Unit, onTelefonoChange: (String) -> Unit,
    onIdentidadChange: (String) -> Unit, onFechaChange: (String) -> Unit, onGeneroChange: (String) -> Unit,
    imagePicker: androidx.activity.result.ActivityResultLauncher<String>, tiendaVM: TiendaViewModel, subiendo: Boolean
) {
    var isVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { isVisible = true }

    LazyColumn(
        Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            AnimatedVisibility(
                visible = isVisible,
                enter = fadeIn(tween(500)) + slideInVertically(tween(500, easing = EaseOutCubic), initialOffsetY = { it / 4 })
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(28.dp))
                        .background(
                            Brush.linearGradient(
                                listOf(P800, P700, P600, G600)
                            )
                        )
                        .drawBehind {
                            drawCircle(
                                color = G400.copy(alpha = 0.08f),
                                radius = size.width * 0.35f,
                                center = Offset(size.width * 0.85f, size.height * 0.2f)
                            )
                            drawCircle(
                                color = G300.copy(alpha = 0.06f),
                                radius = size.width * 0.25f,
                                center = Offset(size.width * 0.15f, size.height * 0.8f)
                            )
                            drawCircle(
                                color = Color.White.copy(alpha = 0.03f),
                                radius = size.width * 0.15f,
                                center = Offset(size.width * 0.55f, size.height * 0.5f)
                            )
                        }
                        .padding(24.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                        val imgUrl = if (!foto.isNullOrBlank() && foto != "default.png") { if (foto.startsWith("http")) foto else BASE_IMAGE_URL + foto } else null
                        val inf = rememberInfiniteTransition(label = "avatarGlow")
                        val glowScale by inf.animateFloat(0.95f, 1.08f, infiniteRepeatable(tween(2500), RepeatMode.Reverse), label = "glow")

                        Box(contentAlignment = Alignment.Center) {
                            Box(
                                modifier = Modifier
                                    .size(110.dp)
                                    .clip(CircleShape)
                                    .background(
                                        Brush.radialGradient(
                                            listOf(G400.copy(alpha = 0.2f), Color.Transparent)
                                        )
                                    )
                                    .graphicsLayer { scaleX = glowScale; scaleY = glowScale }
                            )
                            Box(
                                modifier = Modifier
                                    .size(92.dp)
                                    .clip(CircleShape)
                                    .background(
                                        Brush.linearGradient(listOf(G500, G600, P600))
                                    )
                                    .border(3.dp, Color.White.copy(alpha = 0.25f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                if (imgUrl != null) {
                                    AsyncImage(
                                        model = ImageRequest.Builder(LocalContext.current).data(imgUrl).crossfade(true).build(),
                                        contentDescription = null,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize().clip(CircleShape)
                                    )
                                } else {
                                    Icon(Icons.Filled.Person, null, tint = Color.White.copy(alpha = 0.9f), modifier = Modifier.size(44.dp))
                                }
                            }
                        }

                        Spacer(Modifier.height(16.dp))

                        Text(
                            data?.nombreCompleto ?: "Mi Cuenta",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            letterSpacing = (-0.3).sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            data?.email ?: "",
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.6f),
                            fontWeight = FontWeight.Medium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        Spacer(Modifier.height(16.dp))

                        var photoScale by remember { mutableStateOf(1f) }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(14.dp))
                                .background(Color.White.copy(alpha = 0.15f))
                                .graphicsLayer { scaleX = photoScale; scaleY = photoScale }
                                .clickable {
                                    if (!subiendo) {
                                        photoScale = 0.93f
                                        imagePicker.launch("image/*")
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Row(modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                                if (subiendo) {
                                    CircularProgressIndicator(Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp)
                                    Spacer(Modifier.width(8.dp))
                                    Text("Subiendo...", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                } else {
                                    Icon(Icons.Filled.CameraAlt, null, tint = Color.White, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(8.dp))
                                    Text("Cambiar foto", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                }
                            }
                        }
                        LaunchedEffect(photoScale) {
                            if (photoScale != 1f) { kotlinx.coroutines.delay(100); photoScale = 1f }
                        }
                    }
                }
            }
        }

        item {
            AnimatedVisibility(
                visible = isVisible,
                enter = fadeIn(tween(500, delayMillis = 100)) + slideInVertically(tween(500, delayMillis = 100, easing = EaseOutCubic), initialOffsetY = { it / 4 })
            ) {
                PremiumSectionCard(title = "Informacion personal", icon = Icons.Filled.Badge, iconColor = G600, iconBg = G50) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        PremiumField("Nombre completo", nombre, onNombreChange, Icons.Filled.Person, G600)
                        PremiumField("Telefono", telefono, onTelefonoChange, Icons.Filled.Phone, Blue500)
                        PremiumField("Numero de identidad", identidad, onIdentidadChange, Icons.Filled.Badge, Purple500)
                    }
                }
            }
        }

        item {
            AnimatedVisibility(
                visible = isVisible,
                enter = fadeIn(tween(500, delayMillis = 200)) + slideInVertically(tween(500, delayMillis = 200, easing = EaseOutCubic), initialOffsetY = { it / 4 })
            ) {
                PremiumSectionCard(title = "Detalles adicionales", icon = Icons.Filled.DateRange, iconColor = Amber500, iconBg = Amber50) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(28.dp).clip(CircleShape).background(Amber50), contentAlignment = Alignment.Center) {
                                    Icon(Icons.Filled.Cake, null, tint = Amber500, modifier = Modifier.size(14.dp))
                                }
                                Spacer(Modifier.width(8.dp))
                                Text("Fecha de nacimiento", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = S500, letterSpacing = 0.3.sp)
                            }
                            Spacer(Modifier.height(6.dp))
                            OutlinedTextField(
                                value = fecha, onValueChange = onFechaChange,
                                modifier = Modifier.fillMaxWidth(), singleLine = true,
                                placeholder = { Text("YYYY-MM-DD", fontSize = 13.sp) },
                                shape = RoundedCornerShape(14.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = G600,
                                    unfocusedBorderColor = S200,
                                    focusedContainerColor = G50.copy(alpha = 0.3f),
                                    unfocusedContainerColor = S50
                                )
                            )
                        }

                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(28.dp).clip(CircleShape).background(Purple50), contentAlignment = Alignment.Center) {
                                    Icon(Icons.Filled.Wc, null, tint = Purple500, modifier = Modifier.size(14.dp))
                                }
                                Spacer(Modifier.width(8.dp))
                                Text("Genero", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = S500, letterSpacing = 0.3.sp)
                            }
                            Spacer(Modifier.height(8.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                listOf("Hombre" to Icons.Filled.Male, "Mujer" to Icons.Filled.Female, "Otro" to Icons.Filled.Person).forEach { (g, icon) ->
                                    val isSel = genero == g
                                    val genInteraction = remember { MutableInteractionSource() }
                                    val genPressed by genInteraction.collectIsPressedAsState()
                                    val genScale by animateFloatAsState(if (genPressed) 0.93f else 1f, spring(dampingRatio = Spring.DampingRatioMediumBouncy), label = "gen")
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(44.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(
                                                if (isSel) Brush.linearGradient(listOf(G600.copy(alpha = 0.12f), G500.copy(alpha = 0.06f)))
                                                else Brush.linearGradient(listOf(S50, S50))
                                            )
                                            .border(
                                                1.5.dp,
                                                if (isSel) G600.copy(alpha = 0.4f) else S200,
                                                RoundedCornerShape(12.dp)
                                            )
                                            .graphicsLayer { scaleX = genScale; scaleY = genScale }
                                            .clickable(interactionSource = genInteraction, indication = null) { onGeneroChange(g) },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(icon, null, tint = if (isSel) G600 else S400, modifier = Modifier.size(16.dp))
                                            Spacer(Modifier.width(6.dp))
                                            Text(g, fontSize = 11.sp, fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium, color = if (isSel) G600 else S500)
                                        }
                                    }
                                }
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
                PremiumSectionCard(title = "Cuenta", icon = Icons.Filled.Email, iconColor = S500, iconBg = S100) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(28.dp).clip(CircleShape).background(S100), contentAlignment = Alignment.Center) {
                                Icon(Icons.Filled.Lock, null, tint = S500, modifier = Modifier.size(14.dp))
                            }
                            Spacer(Modifier.width(8.dp))
                            Text("Email", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = S500, letterSpacing = 0.3.sp)
                        }
                        Spacer(Modifier.height(6.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(S100)
                                .padding(14.dp)
                        ) {
                            Text(data?.email ?: "", fontSize = 13.sp, color = S500, fontWeight = FontWeight.Medium)
                        }
                    }
                }
            }
        }

        item {
            AnimatedVisibility(
                visible = isVisible,
                enter = fadeIn(tween(500, delayMillis = 400)) + slideInVertically(tween(500, delayMillis = 400, easing = EaseOutCubic), initialOffsetY = { it / 4 })
            ) {
                var btnScale by remember { mutableStateOf(1f) }
                val btnInteraction = remember { MutableInteractionSource() }
                val btnPressed by btnInteraction.collectIsPressedAsState()
                val btnAnim by animateFloatAsState(
                    if (btnPressed) 0.96f else 1f,
                    spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
                    label = "btnSave"
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .shadow(
                            12.dp,
                            RoundedCornerShape(18.dp),
                            ambientColor = G600.copy(alpha = 0.3f),
                            spotColor = G600.copy(alpha = 0.2f)
                        )
                        .clip(RoundedCornerShape(18.dp))
                        .background(
                            Brush.linearGradient(listOf(G600, G500, P500))
                        )
                        .graphicsLayer { scaleX = btnAnim; scaleY = btnAnim }
                        .clickable(interactionSource = btnInteraction, indication = null) {
                            if (!tiendaVM.clientePerfilGuardando) {
                                btnScale = 0.96f
                                tiendaVM.actualizarClientePerfil(nombre, telefono, identidad, fecha, genero, foto) { ok ->
                                    if (ok) tiendaVM.mensajeOperacion = "Perfil guardado"
                                }
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    LaunchedEffect(btnScale) {
                        if (btnScale != 1f) { kotlinx.coroutines.delay(100); btnScale = 1f }
                    }
                    if (tiendaVM.clientePerfilGuardando) {
                        CircularProgressIndicator(Modifier.size(22.dp), color = Color.White, strokeWidth = 2.5.dp)
                    } else {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.CheckCircle, null, tint = Color.White, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(10.dp))
                            Text("Guardar Cambios", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White, letterSpacing = (-0.2).sp)
                        }
                    }
                }
            }
        }

        item {
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun PremiumSectionCard(
    title: String,
    icon: ImageVector,
    iconColor: Color,
    iconBg: Color,
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        color = Color.White,
        shape = RoundedCornerShape(22.dp),
        shadowElevation = 0.dp,
        border = BorderStroke(1.dp, S200),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(36.dp).clip(CircleShape).background(iconBg), contentAlignment = Alignment.Center) {
                    Icon(icon, null, tint = iconColor, modifier = Modifier.size(18.dp))
                }
                Spacer(Modifier.width(12.dp))
                Text(title, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Carbon, letterSpacing = (-0.2).sp)
            }
            content()
        }
    }
}

@Composable
private fun PremiumField(label: String, value: String, onChange: (String) -> Unit, icon: ImageVector, iconColor: Color) {
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(28.dp).clip(CircleShape).background(iconColor.copy(alpha = 0.08f)), contentAlignment = Alignment.Center) {
                Icon(icon, null, tint = iconColor, modifier = Modifier.size(14.dp))
            }
            Spacer(Modifier.width(8.dp))
            Text(label, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = S500, letterSpacing = 0.3.sp)
        }
        Spacer(Modifier.height(6.dp))
        OutlinedTextField(
            value = value, onValueChange = onChange,
            modifier = Modifier.fillMaxWidth(), singleLine = true,
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = G600,
                unfocusedBorderColor = S200,
                focusedContainerColor = G50.copy(alpha = 0.3f),
                unfocusedContainerColor = S50
            )
        )
    }
}

@Composable
private fun CreditosTab(data: ClientePerfilResponse?) {
    var isVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { isVisible = true }

    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        item {
            AnimatedVisibility(
                visible = isVisible,
                enter = fadeIn(tween(600)) + slideInVertically(tween(600, easing = EaseOutCubic), initialOffsetY = { it / 4 })
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(28.dp))
                        .background(
                            Brush.linearGradient(
                                listOf(P800, P700, P600, G600)
                            )
                        )
                        .drawBehind {
                            drawCircle(
                                color = G400.copy(alpha = 0.1f),
                                radius = size.width * 0.4f,
                                center = Offset(size.width * 0.85f, size.height * 0.15f)
                            )
                            drawCircle(
                                color = G300.copy(alpha = 0.07f),
                                radius = size.width * 0.3f,
                                center = Offset(size.width * 0.1f, size.height * 0.85f)
                            )
                            drawCircle(
                                color = Color.White.copy(alpha = 0.03f),
                                radius = size.width * 0.2f,
                                center = Offset(size.width * 0.5f, size.height * 0.5f)
                            )
                            drawRoundRect(
                                brush = Brush.verticalGradient(listOf(Color.White.copy(alpha = 0.06f), Color.Transparent)),
                                cornerRadius = CornerRadius(28.dp.toPx())
                            )
                        }
                        .padding(28.dp)
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color.White.copy(alpha = 0.12f))
                                    .padding(horizontal = 10.dp, vertical = 5.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Filled.AccountBalanceWallet, null, tint = G300, modifier = Modifier.size(14.dp))
                                    Spacer(Modifier.width(6.dp))
                                    Text("AGROCREDITOS", fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, color = Color.White.copy(alpha = 0.8f), letterSpacing = 1.sp)
                                }
                            }
                        }

                        Spacer(Modifier.height(20.dp))

                        Text("Saldo disponible", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White.copy(alpha = 0.5f), letterSpacing = 0.5.sp)
                        Spacer(Modifier.height(6.dp))

                        val inf = rememberInfiniteTransition(label = "creditPulse")
                        val glowAlpha by inf.animateFloat(0.8f, 1f, infiniteRepeatable(tween(2000), RepeatMode.Reverse), label = "glow")
                        Text(
                            "\$${String.format("%,.0f", data?.creditos ?: 0.0)}",
                            fontSize = 42.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White.copy(alpha = glowAlpha),
                            letterSpacing = (-1.5).sp
                        )

                        Spacer(Modifier.height(16.dp))

                        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color.White.copy(alpha = 0.1f)))

                        Spacer(Modifier.height(16.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Verified, null, tint = G300, modifier = Modifier.size(14.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Credito activo y disponible", fontSize = 11.sp, fontWeight = FontWeight.Medium, color = Color.White.copy(alpha = 0.5f))
                        }
                    }
                }
            }
        }

        item {
            AnimatedVisibility(
                visible = isVisible,
                enter = fadeIn(tween(600, delayMillis = 200)) + slideInVertically(tween(600, delayMillis = 200, easing = EaseOutCubic), initialOffsetY = { it / 4 })
            ) {
                Surface(
                    color = Color.White,
                    shape = RoundedCornerShape(22.dp),
                    shadowElevation = 0.dp,
                    border = BorderStroke(1.dp, S200),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(Modifier.padding(20.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(36.dp).clip(CircleShape).background(G50), contentAlignment = Alignment.Center) {
                                Icon(Icons.Filled.History, null, tint = G600, modifier = Modifier.size(18.dp))
                            }
                            Spacer(Modifier.width(12.dp))
                            Text("Historial de movimientos", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Carbon, letterSpacing = (-0.2).sp)
                        }
                        Spacer(Modifier.height(16.dp))
                        if ((data?.creditos ?: 0.0) > 0.0) {
                            val creditInteraction = remember { MutableInteractionSource() }
                            val creditPressed by creditInteraction.collectIsPressedAsState()
                            val creditScale by animateFloatAsState(if (creditPressed) 0.97f else 1f, spring(dampingRatio = Spring.DampingRatioMediumBouncy), label = "credit")
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(
                                        Brush.linearGradient(listOf(G50, Color.White))
                                    )
                                    .border(1.5.dp, G200, RoundedCornerShape(16.dp))
                                    .graphicsLayer { scaleX = creditScale; scaleY = creditScale }
                                    .padding(16.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(44.dp)
                                            .clip(CircleShape)
                                            .background(
                                                Brush.linearGradient(listOf(G500, G600))
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Filled.CardGiftcard, null, tint = Color.White, modifier = Modifier.size(20.dp))
                                    }
                                    Spacer(Modifier.width(14.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("Regalo de Bienvenida", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Carbon)
                                        Spacer(Modifier.height(2.dp))
                                        Text("Vence en 30 dias", fontSize = 11.sp, color = S400, fontWeight = FontWeight.Medium)
                                    }
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(G600.copy(alpha = 0.1f))
                                            .padding(horizontal = 10.dp, vertical = 6.dp)
                                    ) {
                                        Text("+${String.format("%,.0f", data?.creditos ?: 0.0)}", fontSize = 15.sp, fontWeight = FontWeight.ExtraBold, color = G600)
                                    }
                                }
                            }
                        } else {
                            Box(modifier = Modifier.fillMaxWidth().padding(vertical = 20.dp), contentAlignment = Alignment.Center) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    val inf = rememberInfiniteTransition(label = "walletFloat")
                                    val floatY by inf.animateFloat(-6f, 6f, infiniteRepeatable(tween(2500), RepeatMode.Reverse), label = "float")
                                    Box(
                                        modifier = Modifier
                                            .size(80.dp)
                                            .clip(CircleShape)
                                            .background(
                                                Brush.radialGradient(listOf(G600.copy(alpha = 0.06f), Color.Transparent))
                                            )
                                            .graphicsLayer { translationY = floatY },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(60.dp)
                                                .clip(CircleShape)
                                                .background(G50),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(Icons.Filled.AccountBalanceWallet, null, tint = G300, modifier = Modifier.size(28.dp))
                                        }
                                    }
                                    Spacer(Modifier.height(16.dp))
                                    Text("Sin movimientos", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Carbon)
                                    Spacer(Modifier.height(4.dp))
                                    Text("Tus creditos apareceran aqui", fontSize = 12.sp, color = S400)
                                }
                            }
                        }
                    }
                }
            }
        }

        item {
            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun PagosTab() {
    var isVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { isVisible = true }

    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        item {
            AnimatedVisibility(
                visible = isVisible,
                enter = fadeIn(tween(500)) + slideInVertically(tween(500, easing = EaseOutCubic), initialOffsetY = { it / 4 })
            ) {
                Text("Metodos de pago", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Carbon, letterSpacing = (-0.3).sp)
            }
        }

        item {
            AnimatedVisibility(
                visible = isVisible,
                enter = fadeIn(tween(500, delayMillis = 100)) + slideInVertically(tween(500, delayMillis = 100, easing = EaseOutCubic), initialOffsetY = { it / 4 })
            ) {
                Surface(
                    color = Color.White,
                    shape = RoundedCornerShape(22.dp),
                    shadowElevation = 0.dp,
                    border = BorderStroke(1.dp, S200),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        PaymentMethodRow(Icons.Filled.Payments, "Efectivo", "Pago contra entrega", G600, G50)
                        Box(Modifier.fillMaxWidth().height(1.dp).background(S100))
                        PaymentMethodRow(Icons.Filled.AccountBalance, "Mercado Pago", "Pago digital seguro", Blue500, Blue50)
                    }
                }
            }
        }

        item {
            AnimatedVisibility(
                visible = isVisible,
                enter = fadeIn(tween(500, delayMillis = 200)) + slideInVertically(tween(500, delayMillis = 200, easing = EaseOutCubic), initialOffsetY = { it / 4 })
            ) {
                Surface(
                    color = Color.White,
                    shape = RoundedCornerShape(22.dp),
                    shadowElevation = 0.dp,
                    border = BorderStroke(1.dp, S200),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(Modifier.padding(20.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(36.dp).clip(CircleShape).background(Amber50), contentAlignment = Alignment.Center) {
                                Icon(Icons.Filled.Receipt, null, tint = Amber500, modifier = Modifier.size(18.dp))
                            }
                            Spacer(Modifier.width(12.dp))
                            Text("Historial de pagos", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Carbon, letterSpacing = (-0.2).sp)
                        }
                        Spacer(Modifier.height(20.dp))
                        Box(Modifier.fillMaxWidth().padding(vertical = 12.dp), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                val inf = rememberInfiniteTransition(label = "payFloat")
                                val floatY by inf.animateFloat(-5f, 5f, infiniteRepeatable(tween(2500), RepeatMode.Reverse), label = "float")
                                Box(
                                    modifier = Modifier
                                        .size(72.dp)
                                        .clip(CircleShape)
                                        .background(Brush.radialGradient(listOf(Amber500.copy(alpha = 0.06f), Color.Transparent)))
                                        .graphicsLayer { translationY = floatY },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(52.dp)
                                            .clip(CircleShape)
                                            .background(Amber50),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Filled.Description, null, tint = Amber500.copy(alpha = 0.6f), modifier = Modifier.size(24.dp))
                                    }
                                }
                                Spacer(Modifier.height(14.dp))
                                Text("Sin historial", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Carbon)
                                Spacer(Modifier.height(4.dp))
                                Text("Tus pagos apareceran aqui", fontSize = 12.sp, color = S400)
                            }
                        }
                    }
                }
            }
        }

        item {
            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun PaymentMethodRow(icon: ImageVector, title: String, subtitle: String, color: Color, bg: Color) {
    val interaction = remember { MutableInteractionSource() }
    val isPressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(if (isPressed) 0.97f else 1f, spring(dampingRatio = Spring.DampingRatioMediumBouncy), label = "pay")

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(
                Brush.linearGradient(listOf(bg.copy(alpha = 0.5f), bg.copy(alpha = 0.2f)))
            )
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .padding(14.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(listOf(color.copy(alpha = 0.15f), color.copy(alpha = 0.05f)))
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = color, modifier = Modifier.size(20.dp))
            }
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Carbon)
                Text(subtitle, fontSize = 11.sp, color = S400, fontWeight = FontWeight.Medium)
            }
            Icon(Icons.Filled.CheckCircle, null, tint = color.copy(alpha = 0.4f), modifier = Modifier.size(20.dp))
        }
    }
}

@Composable
private fun NotificacionesTab() {
    var isVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { isVisible = true }

    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn(tween(600)) + scaleIn(tween(600), initialScale = 0.9f)
    ) {
        Box(Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                val inf = rememberInfiniteTransition(label = "bellFloat")
                val floatY by inf.animateFloat(-8f, 8f, infiniteRepeatable(tween(2500), RepeatMode.Reverse), label = "float")
                val glowAlpha by inf.animateFloat(0.3f, 0.6f, infiniteRepeatable(tween(2000), RepeatMode.Reverse), label = "glow")

                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(listOf(G600.copy(alpha = glowAlpha * 0.15f), Color.Transparent))
                        )
                        .graphicsLayer { translationY = floatY },
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(88.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(listOf(G50, Color.White))
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Filled.NotificationsActive, null, tint = G600, modifier = Modifier.size(40.dp))
                    }
                }
                Spacer(Modifier.height(28.dp))
                Text("Estas al dia!", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Carbon, letterSpacing = (-0.3).sp)
                Spacer(Modifier.height(8.dp))
                Text(
                    "Cuando tengas novedades sobre tus envios o alertas importantes, apareceran aqui.",
                    fontSize = 13.sp,
                    color = S400,
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp
                )
            }
        }
    }
}

@Composable
private fun OrdenesTab(pedidos: List<Pedido>, onVerTodas: () -> Unit) {
    var isVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { isVisible = true }

    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item {
            AnimatedVisibility(
                visible = isVisible,
                enter = fadeIn(tween(500)) + slideInVertically(tween(500, easing = EaseOutCubic), initialOffsetY = { it / 4 })
            ) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Ultimas ordenes", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Carbon, letterSpacing = (-0.3).sp)
                        Text("${pedidos.size} pedidos registrados", fontSize = 12.sp, color = S400, fontWeight = FontWeight.Medium)
                    }
                    var btnScale by remember { mutableStateOf(1f) }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(G50)
                            .graphicsLayer { scaleX = btnScale; scaleY = btnScale }
                            .clickable {
                                btnScale = 0.93f
                                onVerTodas()
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Row(modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text("Ver todas", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = G600)
                            Spacer(Modifier.width(4.dp))
                            Icon(Icons.Filled.ArrowForward, null, tint = G600, modifier = Modifier.size(14.dp))
                        }
                    }
                    LaunchedEffect(btnScale) {
                        if (btnScale != 1f) { kotlinx.coroutines.delay(100); btnScale = 1f }
                    }
                }
            }
        }

        if (pedidos.isEmpty()) {
            item {
                AnimatedVisibility(
                    visible = isVisible,
                    enter = fadeIn(tween(600, delayMillis = 200)) + scaleIn(tween(600, delayMillis = 200), initialScale = 0.9f)
                ) {
                    Box(Modifier.fillMaxWidth().padding(vertical = 32.dp), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            val inf = rememberInfiniteTransition(label = "orderFloat")
                            val floatY by inf.animateFloat(-6f, 6f, infiniteRepeatable(tween(2500), RepeatMode.Reverse), label = "float")
                            Box(
                                modifier = Modifier
                                    .size(80.dp)
                                    .clip(CircleShape)
                                    .background(Brush.radialGradient(listOf(Amber500.copy(alpha = 0.06f), Color.Transparent)))
                                    .graphicsLayer { translationY = floatY },
                                contentAlignment = Alignment.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(60.dp)
                                        .clip(CircleShape)
                                        .background(Amber50),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Filled.ShoppingBag, null, tint = Amber500.copy(alpha = 0.6f), modifier = Modifier.size(26.dp))
                                }
                            }
                            Spacer(Modifier.height(16.dp))
                            Text("Sin pedidos recientes", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Carbon)
                            Spacer(Modifier.height(4.dp))
                            Text("Explora la tienda y haz tu primer pedido", fontSize = 12.sp, color = S400)
                        }
                    }
                }
            }
        } else {
            items(pedidos.take(5)) { pedido ->
                AnimatedVisibility(
                    visible = isVisible,
                    enter = fadeIn(tween(500)) + slideInVertically(tween(500, easing = EaseOutCubic), initialOffsetY = { it / 6 })
                ) {
                    val orderInteraction = remember { MutableInteractionSource() }
                    val orderPressed by orderInteraction.collectIsPressedAsState()
                    val orderScale by animateFloatAsState(if (orderPressed) 0.97f else 1f, spring(dampingRatio = Spring.DampingRatioMediumBouncy), label = "order")

                    Surface(
                        color = Color.White,
                        shape = RoundedCornerShape(20.dp),
                        shadowElevation = 0.dp,
                        border = BorderStroke(1.dp, S200),
                        modifier = Modifier
                            .fillMaxWidth()
                            .graphicsLayer { scaleX = orderScale; scaleY = orderScale }
                    ) {
                        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            val estadoColor = when (pedido.estado) {
                                "ENTREGADO" -> G600
                                "CANCELADO" -> Red500
                                else -> Blue500
                            }
                            val estadoBg = when (pedido.estado) {
                                "ENTREGADO" -> G50
                                "CANCELADO" -> Red50
                                else -> Blue50
                            }
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(
                                        Brush.linearGradient(listOf(estadoBg, Color.White))
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Filled.ShoppingBag, null, tint = estadoColor, modifier = Modifier.size(20.dp))
                            }
                            Spacer(Modifier.width(14.dp))
                            Column(Modifier.weight(1f)) {
                                Text("Orden #${pedido.numeroOrden ?: pedido.id ?: "—"}", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Carbon, letterSpacing = (-0.2).sp)
                                Spacer(Modifier.height(3.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Filled.CalendarToday, null, tint = S400, modifier = Modifier.size(11.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text(pedido.fechaCreacion?.take(10) ?: "", fontSize = 11.sp, color = S400, fontWeight = FontWeight.Medium)
                                }
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("\$${String.format("%,.0f", pedido.total ?: 0.0)}", fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = estadoColor, letterSpacing = (-0.3).sp)
                                Spacer(Modifier.height(4.dp))
                                Surface(
                                    color = estadoColor.copy(alpha = 0.1f),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        pedido.estado ?: "",
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = estadoColor,
                                        letterSpacing = 0.3.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        item {
            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun FavoritosTab(favoritos: List<com.agroconecta.app.data.model.Producto>, onVerTodos: () -> Unit) {
    var isVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { isVisible = true }

    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item {
            AnimatedVisibility(
                visible = isVisible,
                enter = fadeIn(tween(500)) + slideInVertically(tween(500, easing = EaseOutCubic), initialOffsetY = { it / 4 })
            ) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Mis Favoritos", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Carbon, letterSpacing = (-0.3).sp)
                        Text("${favoritos.size} productos guardados", fontSize = 12.sp, color = S400, fontWeight = FontWeight.Medium)
                    }
                    var btnScale by remember { mutableStateOf(1f) }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(G50)
                            .graphicsLayer { scaleX = btnScale; scaleY = btnScale }
                            .clickable {
                                btnScale = 0.93f
                                onVerTodos()
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Row(modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text("Ver todos", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = G600)
                            Spacer(Modifier.width(4.dp))
                            Icon(Icons.Filled.ArrowForward, null, tint = G600, modifier = Modifier.size(14.dp))
                        }
                    }
                    LaunchedEffect(btnScale) {
                        if (btnScale != 1f) { kotlinx.coroutines.delay(100); btnScale = 1f }
                    }
                }
            }
        }

        if (favoritos.isEmpty()) {
            item {
                AnimatedVisibility(
                    visible = isVisible,
                    enter = fadeIn(tween(600, delayMillis = 200)) + scaleIn(tween(600, delayMillis = 200), initialScale = 0.9f)
                ) {
                    Box(Modifier.fillMaxWidth().padding(vertical = 32.dp), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            val inf = rememberInfiniteTransition(label = "favFloat")
                            val floatY by inf.animateFloat(-6f, 6f, infiniteRepeatable(tween(2500), RepeatMode.Reverse), label = "float")
                            Box(
                                modifier = Modifier
                                    .size(80.dp)
                                    .clip(CircleShape)
                                    .background(Brush.radialGradient(listOf(Rose500.copy(alpha = 0.06f), Color.Transparent)))
                                    .graphicsLayer { translationY = floatY },
                                contentAlignment = Alignment.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(60.dp)
                                        .clip(CircleShape)
                                        .background(Red50),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Filled.FavoriteBorder, null, tint = Rose500.copy(alpha = 0.5f), modifier = Modifier.size(26.dp))
                                }
                            }
                            Spacer(Modifier.height(16.dp))
                            Text("Aun no tienes favoritos", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Carbon)
                            Spacer(Modifier.height(4.dp))
                            Text("Guarda productos que te gusten", fontSize = 12.sp, color = S400)
                        }
                    }
                }
            }
        } else {
            items(favoritos.take(8)) { prod ->
                AnimatedVisibility(
                    visible = isVisible,
                    enter = fadeIn(tween(500)) + slideInVertically(tween(500, easing = EaseOutCubic), initialOffsetY = { it / 6 })
                ) {
                    val favInteraction = remember { MutableInteractionSource() }
                    val favPressed by favInteraction.collectIsPressedAsState()
                    val favScale by animateFloatAsState(if (favPressed) 0.97f else 1f, spring(dampingRatio = Spring.DampingRatioMediumBouncy), label = "fav")

                    Surface(
                        color = Color.White,
                        shape = RoundedCornerShape(20.dp),
                        shadowElevation = 0.dp,
                        border = BorderStroke(1.dp, S200),
                        modifier = Modifier
                            .fillMaxWidth()
                            .graphicsLayer { scaleX = favScale; scaleY = favScale }
                    ) {
                        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(
                                        Brush.linearGradient(listOf(S100, Color.White))
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                val img = if (!prod.imagenUrl.isNullOrBlank() && prod.imagenUrl != "default.png") { if (prod.imagenUrl!!.startsWith("http")) prod.imagenUrl else BASE_IMAGE_URL + prod.imagenUrl } else null
                                if (img != null) {
                                    AsyncImage(
                                        model = ImageRequest.Builder(LocalContext.current).data(img).crossfade(true).build(),
                                        contentDescription = null,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(14.dp))
                                    )
                                } else {
                                    Icon(Icons.Filled.Eco, null, tint = G600, modifier = Modifier.size(24.dp))
                                }
                            }
                            Spacer(Modifier.width(14.dp))
                            Column(Modifier.weight(1f)) {
                                Text(prod.nombre, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Carbon, maxLines = 1, overflow = TextOverflow.Ellipsis, letterSpacing = (-0.2).sp)
                                Spacer(Modifier.height(4.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(G50)
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text("\$${String.format("%,.0f", prod.precio)} / ${prod.unidad ?: "Kg"}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = G600)
                                    }
                                }
                            }
                            val heartInf = rememberInfiniteTransition(label = "heart")
                            val heartScale by heartInf.animateFloat(0.9f, 1.1f, infiniteRepeatable(tween(1200), RepeatMode.Reverse), label = "beat")
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Red50),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Filled.Favorite, null,
                                    tint = Rose500,
                                    modifier = Modifier
                                        .size(18.dp)
                                        .graphicsLayer { scaleX = heartScale; scaleY = heartScale }
                                )
                            }
                        }
                    }
                }
            }
        }

        item {
            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun SoporteMiniTab(tiendaVM: TiendaViewModel) {
    LaunchedEffect(Unit) { tiendaVM.cargarMisTickets() }
    var nuevoAsunto by remember { mutableStateOf("") }
    var nuevoMensaje by remember { mutableStateOf("") }
    var enChat by remember { mutableStateOf(false) }
    var ticketChatId by remember { mutableStateOf<Long?>(null) }
    var replyText by remember { mutableStateOf("") }
    var chatAsunto by remember { mutableStateOf("") }
    var chatEstado by remember { mutableStateOf("") }
    val listState = androidx.compose.foundation.lazy.rememberLazyListState()

    LaunchedEffect(ticketChatId) {
        while (ticketChatId != null) { tiendaVM.cargarMensajesTicket(ticketChatId!!); kotlinx.coroutines.delay(5000) }
    }

    if (enChat && ticketChatId != null) {
        Column(Modifier.fillMaxSize()) {
            Surface(
                color = Color.White,
                shadowElevation = 0.dp,
                border = BorderStroke(1.dp, S200),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    Modifier.padding(14.dp).statusBarsPadding(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    var backScale by remember { mutableStateOf(1f) }
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(S100)
                            .graphicsLayer { scaleX = backScale; scaleY = backScale }
                            .clickable {
                                backScale = 0.9f
                                enChat = false; ticketChatId = null
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Filled.ArrowBack, null, modifier = Modifier.size(18.dp), tint = Carbon)
                    }
                    LaunchedEffect(backScale) {
                        if (backScale != 1f) { kotlinx.coroutines.delay(100); backScale = 1f }
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(chatAsunto, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Carbon, maxLines = 1, overflow = TextOverflow.Ellipsis, letterSpacing = (-0.2).sp)
                        val estadoColor = when (chatEstado) { "ABIERTO" -> G600; "EN_PROGRESO" -> Blue500; else -> S400 }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(7.dp).clip(CircleShape).background(estadoColor))
                            Spacer(Modifier.width(6.dp))
                            Text(chatEstado, fontSize = 11.sp, color = estadoColor, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            LazyColumn(
                Modifier.weight(1f).fillMaxWidth().background(Surface),
                state = listState,
                contentPadding = PaddingValues(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(tiendaVM.soporteMensajes.toList()) { msg ->
                    if (msg.esMio) {
                        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.End) {
                            Box(
                                modifier = Modifier
                                    .widthIn(max = 260.dp)
                                    .clip(RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp, bottomStart = 18.dp, bottomEnd = 4.dp))
                                    .background(
                                        Brush.linearGradient(listOf(G600, G500))
                                    )
                                    .padding(14.dp)
                            ) {
                                Text(msg.contenido, fontSize = 13.sp, color = Color.White, lineHeight = 18.sp)
                            }
                        }
                    } else {
                        Row(Modifier.fillMaxWidth()) {
                            val isSystem = msg.remitente == "Sistema"
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (isSystem) Brush.linearGradient(listOf(Purple50, Color.White))
                                        else Brush.linearGradient(listOf(G50, Color.White))
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    if (isSystem) Icons.Filled.SmartToy else Icons.Filled.HeadsetMic,
                                    null,
                                    Modifier.size(16.dp),
                                    tint = if (isSystem) Purple500 else G600
                                )
                            }
                            Spacer(Modifier.width(8.dp))
                            Column {
                                Text(msg.remitente, fontSize = 10.sp, color = S400, fontWeight = FontWeight.Medium)
                                Spacer(Modifier.height(3.dp))
                                Box(
                                    modifier = Modifier
                                        .widthIn(max = 230.dp)
                                        .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 18.dp, bottomStart = 18.dp, bottomEnd = 18.dp))
                                        .background(Color.White)
                                        .border(1.dp, S200, RoundedCornerShape(topStart = 4.dp, topEnd = 18.dp, bottomStart = 18.dp, bottomEnd = 18.dp))
                                        .padding(14.dp)
                                ) {
                                    Text(msg.contenido, fontSize = 13.sp, color = Carbon, lineHeight = 18.sp)
                                }
                            }
                        }
                    }
                }
            }

            if (chatEstado != "CERRADO") {
                Surface(
                    color = Color.White,
                    shadowElevation = 8.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        Modifier.padding(horizontal = 12.dp, vertical = 10.dp).navigationBarsPadding(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            replyText, { replyText = it },
                            Modifier.weight(1f).height(46.dp),
                            singleLine = true,
                            placeholder = { Text("Escribe un mensaje...", fontSize = 13.sp) },
                            shape = RoundedCornerShape(22.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedBorderColor = S200,
                                focusedBorderColor = G600,
                                focusedContainerColor = G50.copy(alpha = 0.3f),
                                unfocusedContainerColor = S50
                            )
                        )
                        Spacer(Modifier.width(8.dp))
                        var sendScale by remember { mutableStateOf(1f) }
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(
                                    if (replyText.isNotBlank()) Brush.linearGradient(listOf(G600, G500))
                                    else Brush.linearGradient(listOf(S200, S100))
                                )
                                .graphicsLayer { scaleX = sendScale; scaleY = sendScale }
                                .clickable {
                                    if (replyText.isNotBlank()) {
                                        sendScale = 0.9f
                                        tiendaVM.enviarMensajeSoporte(ticketChatId!!, replyText) { replyText = "" }
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Filled.Send, null, tint = if (replyText.isNotBlank()) Color.White else S400, modifier = Modifier.size(18.dp))
                        }
                        LaunchedEffect(sendScale) {
                            if (sendScale != 1f) { kotlinx.coroutines.delay(100); sendScale = 1f }
                        }
                    }
                }
            }
        }
    } else {
        var isVisible by remember { mutableStateOf(false) }
        LaunchedEffect(Unit) { isVisible = true }

        LazyColumn(
            Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                AnimatedVisibility(
                    visible = isVisible,
                    enter = fadeIn(tween(500)) + slideInVertically(tween(500, easing = EaseOutCubic), initialOffsetY = { it / 4 })
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(24.dp))
                            .background(
                                Brush.linearGradient(listOf(G600, G500, P500))
                            )
                            .drawBehind {
                                drawCircle(
                                    color = G400.copy(alpha = 0.1f),
                                    radius = size.width * 0.3f,
                                    center = Offset(size.width * 0.8f, size.height * 0.2f)
                                )
                            }
                            .padding(24.dp)
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(CircleShape)
                                        .background(Color.White.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Filled.HeadsetMic, null, tint = Color.White, modifier = Modifier.size(22.dp))
                                }
                                Spacer(Modifier.width(14.dp))
                                Column {
                                    Text("AgroSoporte", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White, letterSpacing = (-0.3).sp)
                                    Text("Estamos para ayudarte", fontSize = 12.sp, color = Color.White.copy(alpha = 0.6f), fontWeight = FontWeight.Medium)
                                }
                            }
                        }
                    }
                }
            }

            item {
                AnimatedVisibility(
                    visible = isVisible,
                    enter = fadeIn(tween(500, delayMillis = 100)) + slideInVertically(tween(500, delayMillis = 100, easing = EaseOutCubic), initialOffsetY = { it / 4 })
                ) {
                    Surface(
                        color = Color.White,
                        shape = RoundedCornerShape(22.dp),
                        shadowElevation = 0.dp,
                        border = BorderStroke(1.dp, S200),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(36.dp).clip(CircleShape).background(G50), contentAlignment = Alignment.Center) {
                                    Icon(Icons.Filled.Create, null, tint = G600, modifier = Modifier.size(18.dp))
                                }
                                Spacer(Modifier.width(12.dp))
                                Text("Nueva consulta", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Carbon, letterSpacing = (-0.2).sp)
                            }

                            OutlinedTextField(
                                nuevoAsunto, { nuevoAsunto = it },
                                Modifier.fillMaxWidth(),
                                singleLine = true,
                                label = { Text("Asunto") },
                                shape = RoundedCornerShape(14.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = G600,
                                    unfocusedBorderColor = S200,
                                    focusedContainerColor = G50.copy(alpha = 0.3f),
                                    unfocusedContainerColor = S50
                                )
                            )
                            OutlinedTextField(
                                nuevoMensaje, { nuevoMensaje = it },
                                Modifier.fillMaxWidth().height(90.dp),
                                maxLines = 4,
                                label = { Text("Mensaje") },
                                shape = RoundedCornerShape(14.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = G600,
                                    unfocusedBorderColor = S200,
                                    focusedContainerColor = G50.copy(alpha = 0.3f),
                                    unfocusedContainerColor = S50
                                )
                            )

                            var sendScale by remember { mutableStateOf(1f) }
                            val enabled = nuevoAsunto.isNotBlank() && nuevoMensaje.isNotBlank()
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp)
                                    .shadow(
                                        if (enabled) 8.dp else 0.dp,
                                        RoundedCornerShape(16.dp),
                                        ambientColor = G600.copy(alpha = if (enabled) 0.25f else 0f),
                                        spotColor = G600.copy(alpha = if (enabled) 0.15f else 0f)
                                    )
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(
                                        if (enabled) Brush.linearGradient(listOf(G600, G500))
                                        else Brush.linearGradient(listOf(S200, S100))
                                    )
                                    .graphicsLayer { scaleX = sendScale; scaleY = sendScale }
                                    .clickable {
                                        if (enabled) {
                                            sendScale = 0.96f
                                            tiendaVM.crearTicketSoporte(nuevoAsunto, nuevoMensaje) { id ->
                                                if (id != null) {
                                                    ticketChatId = id; chatAsunto = nuevoAsunto; chatEstado = "ABIERTO"
                                                    tiendaVM.cargarMensajesTicket(id); enChat = true
                                                    nuevoAsunto = ""; nuevoMensaje = ""
                                                }
                                            }
                                        }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Filled.Send, null, tint = if (enabled) Color.White else S400, modifier = Modifier.size(18.dp))
                                    Spacer(Modifier.width(8.dp))
                                    Text("Enviar Consulta", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = if (enabled) Color.White else S400)
                                }
                            }
                            LaunchedEffect(sendScale) {
                                if (sendScale != 1f) { kotlinx.coroutines.delay(100); sendScale = 1f }
                            }
                        }
                    }
                }
            }

            item {
                AnimatedVisibility(
                    visible = isVisible,
                    enter = fadeIn(tween(500, delayMillis = 200)) + slideInVertically(tween(500, delayMillis = 200, easing = EaseOutCubic), initialOffsetY = { it / 4 })
                ) {
                    Surface(
                        color = Color.White,
                        shape = RoundedCornerShape(22.dp),
                        shadowElevation = 0.dp,
                        border = BorderStroke(1.dp, S200),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(Modifier.padding(20.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(36.dp).clip(CircleShape).background(Blue50), contentAlignment = Alignment.Center) {
                                    Icon(Icons.Filled.History, null, tint = Blue500, modifier = Modifier.size(18.dp))
                                }
                                Spacer(Modifier.width(12.dp))
                                Text("Mis consultas", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Carbon, letterSpacing = (-0.2).sp)
                            }
                            Spacer(Modifier.height(14.dp))
                            if (tiendaVM.soporteTickets.isEmpty()) {
                                Box(Modifier.fillMaxWidth().padding(vertical = 16.dp), contentAlignment = Alignment.Center) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Icon(Icons.Filled.Forum, null, tint = S300, modifier = Modifier.size(36.dp))
                                        Spacer(Modifier.height(10.dp))
                                        Text("No tienes consultas activas", fontSize = 12.sp, color = S400)
                                    }
                                }
                            } else {
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    tiendaVM.soporteTickets.toList().forEach { t ->
                                        val sc = when (t.estado) { "ABIERTO" -> G600; "EN_PROGRESO" -> Blue500; else -> S400 }
                                        val ticketInteraction = remember { MutableInteractionSource() }
                                        val ticketPressed by ticketInteraction.collectIsPressedAsState()
                                        val ticketScale by animateFloatAsState(if (ticketPressed) 0.97f else 1f, spring(dampingRatio = Spring.DampingRatioMediumBouncy), label = "ticket")

                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(14.dp))
                                                .background(sc.copy(alpha = 0.04f))
                                                .border(1.dp, sc.copy(alpha = 0.15f), RoundedCornerShape(14.dp))
                                                .graphicsLayer { scaleX = ticketScale; scaleY = ticketScale }
                                                .clickable(interactionSource = ticketInteraction, indication = null) {
                                                    ticketChatId = t.id; chatAsunto = t.asunto; chatEstado = t.estado
                                                    tiendaVM.cargarMensajesTicket(t.id!!); enChat = true
                                                }
                                                .padding(14.dp)
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Column(Modifier.weight(1f)) {
                                                    Text(t.asunto, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Carbon, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                                    Spacer(Modifier.height(3.dp))
                                                    Text(t.fecha?.take(10) ?: "", fontSize = 10.sp, color = S400, fontWeight = FontWeight.Medium)
                                                }
                                                Box(
                                                    modifier = Modifier
                                                        .clip(RoundedCornerShape(8.dp))
                                                        .background(sc.copy(alpha = 0.1f))
                                                        .padding(horizontal = 8.dp, vertical = 3.dp)
                                                ) {
                                                    Text(t.estado, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = sc, letterSpacing = 0.3.sp)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            item {
                Spacer(Modifier.height(16.dp))
            }
        }
    }
}
