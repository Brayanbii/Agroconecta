package com.agroconectago.app.ui

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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.DirectionsBike
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.automirrored.outlined.Help
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.agroconectago.app.data.api.DeliveryRetrofitClient
import com.agroconectago.app.data.model.DeliveryProfileResponse
import com.agroconectago.app.data.model.DeliveryProfileUpdateRequest
import com.agroconectago.app.data.model.DeliveryUsuarioInfo
import com.agroconectago.app.ui.theme.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileOutputStream

@Composable
fun DeliveryPerfilScreen(
    onBack: () -> Unit,
    usuarioLogueado: DeliveryUsuarioInfo? = null,
    onLogout: () -> Unit = {},
    onMisViajes: () -> Unit = {},
    onEditarDocumentos: () -> Unit = {},
    onBilletera: () -> Unit = {},
    onSoporte: () -> Unit = {}
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var profile by remember { mutableStateOf<DeliveryProfileResponse?>(null) }
    var loading by remember { mutableStateOf(true) }
    var saving by remember { mutableStateOf(false) }
    var toastMsg by remember { mutableStateOf("") }
    var showToast by remember { mutableStateOf(false) }
    var errorMsg by remember { mutableStateOf("") }

    var editCiudad by remember { mutableStateOf("") }
    var editVehiculo by remember { mutableStateOf("MOTO") }
    var editPlaca by remember { mutableStateOf("") }
    var editMarca by remember { mutableStateOf("") }
    var editModelo by remember { mutableStateOf("") }
    var editAnio by remember { mutableStateOf("") }
    var editCapacidad by remember { mutableStateOf("") }
    var editLicencia by remember { mutableStateOf("") }
    var editColor by remember { mutableStateOf("") }
    var fotoPerfilLocal by remember { mutableStateOf<String?>(null) }
    var mostrarEdicion by remember { mutableStateOf(false) }

    val nombre = usuarioLogueado?.userName ?: profile?.municipioOrigen?.let { "Agrosocio" } ?: "Agrosocio"
    val email = usuarioLogueado?.email ?: ""

    fun cargarPerfil() {
        scope.launch {
            loading = true
            try {
                val p = DeliveryRetrofitClient.api.getProfile()
                if (p.success) {
                    profile = p
                    editCiudad = p.municipioOrigen ?: ""
                    editVehiculo = p.tipoVehiculo ?: "MOTO"
                    editPlaca = p.placaVehiculo ?: ""
                    editMarca = p.marcaVehiculo ?: ""
                    editModelo = p.modeloVehiculo ?: ""
                    editAnio = (p.anioVehiculo ?: 0).toString()
                    editCapacidad = (p.capacidadCargaKg ?: 0.0).toString()
                    editLicencia = p.licenciaConduccion ?: ""
                    editColor = p.colorVehiculo ?: ""
                    fotoPerfilLocal = p.fotoPerfil
                } else {
                    errorMsg = "No se pudo cargar el perfil"
                }
            } catch (_: Exception) { errorMsg = "Error de conexion" }
            loading = false
        }
    }

    fun guardar() {
        scope.launch {
            saving = true
            try {
                val req = DeliveryProfileUpdateRequest(
                    municipioOrigen = editCiudad,
                    tipoVehiculo = editVehiculo,
                    placaVehiculo = editPlaca,
                    marcaVehiculo = editMarca,
                    modeloVehiculo = editModelo,
                    anioVehiculo = editAnio.toIntOrNull() ?: 0,
                    capacidadCargaKg = editCapacidad.toDoubleOrNull() ?: 0.0,
                    licenciaConduccion = editLicencia,
                    colorVehiculo = editColor
                )
                val resp = DeliveryRetrofitClient.api.updateProfile(req)
                if (resp.success) {
                    toastMsg = "Perfil actualizado"
                    showToast = true
                } else {
                    errorMsg = resp.message ?: "Error al guardar"
                }
            } catch (_: Exception) { errorMsg = "Error de conexion" }
            saving = false
        }
    }

    val fotoPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let { u ->
            scope.launch {
                try {
                    val file = withContext(Dispatchers.IO) {
                        val input = context.contentResolver.openInputStream(u)
                        val tempFile = File(context.cacheDir, "perfil_${System.currentTimeMillis()}.jpg")
                        input?.use { it.copyTo(FileOutputStream(tempFile)) }
                        tempFile
                    }
                    if (!file.exists()) return@launch
                    val body = file.asRequestBody("image/*".toMediaTypeOrNull())
                    val part = MultipartBody.Part.createFormData("file", file.name, body)
                    val resp = DeliveryRetrofitClient.httpClient.newCall(
                        okhttp3.Request.Builder().url(com.agroconectago.app.data.api.ApiConfig.BASE_URL + "api/productos/upload-image").post(
                            okhttp3.MultipartBody.Builder().setType(okhttp3.MultipartBody.FORM).addPart(part).build()
                        ).build()
                    ).execute()
                    val filename = resp.body?.string()?.trim()
                    if (!filename.isNullOrBlank()) {
                        fotoPerfilLocal = filename
                        val req = DeliveryProfileUpdateRequest(
                            municipioOrigen = editCiudad, tipoVehiculo = editVehiculo,
                            placaVehiculo = editPlaca, marcaVehiculo = editMarca,
                            modeloVehiculo = editModelo, anioVehiculo = editAnio.toIntOrNull() ?: 0,
                            capacidadCargaKg = editCapacidad.toDoubleOrNull() ?: 0.0,
                            licenciaConduccion = editLicencia, colorVehiculo = editColor
                        )
                        DeliveryRetrofitClient.api.updateProfile(req)
                        val fotoBody = mapOf("fotoPerfil" to filename)
                        "application/json".toMediaTypeOrNull()?.let { mt ->
                            DeliveryRetrofitClient.httpClient.newCall(
                                okhttp3.Request.Builder().url(com.agroconectago.app.data.api.ApiConfig.BASE_URL + "api/delivery/perfil")
                                    .put(okhttp3.RequestBody.create(mt, "{\"fotoPerfil\":\"$filename\"}"))
                                    .build()
                            ).execute()
                        }
                        toastMsg = "Foto actualizada"
                        showToast = true
                    }
                } catch (_: Exception) {}
            }
        }
    }

    LaunchedEffect(Unit) { cargarPerfil() }

    var isVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { kotlinx.coroutines.delay(80); isVisible = true }

    if (loading) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color(0xFFFAFBFA), Color(0xFFF5F7F5))
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                CircularProgressIndicator(color = DeliveryBrand600, modifier = Modifier.size(40.dp), strokeWidth = 3.dp)
                Spacer(Modifier.height(16.dp))
                Text("Cargando perfil...", fontSize = 14.sp, color = Slate400, fontWeight = FontWeight.Medium)
            }
        }
        return
    }

    val infiniteTransition = rememberInfiniteTransition(label = "perfil")
    val orb1X by infiniteTransition.animateFloat(
        0.7f, 0.85f,
        infiniteRepeatable(tween(7000, easing = EaseInOutCubic), RepeatMode.Reverse),
        label = "orb1X"
    )
    val orb1Y by infiniteTransition.animateFloat(
        0.1f, 0.2f,
        infiniteRepeatable(tween(8000, easing = EaseInOutCubic), RepeatMode.Reverse),
        label = "orb1Y"
    )
    val orbAlpha by infiniteTransition.animateFloat(
        0.02f, 0.05f,
        infiniteRepeatable(tween(5000), RepeatMode.Reverse),
        label = "orbAlpha"
    )

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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .drawBehind {
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(DeliveryBrand600.copy(alpha = orbAlpha), Color.Transparent)
                        ),
                        radius = size.width * 0.5f,
                        center = Offset(size.width * orb1X, size.height * orb1Y)
                    )
                }
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 32.dp)
        ) {
            // ═══════════════════════════════════════════════════════
            // HEADER - BACK BUTTON + TITLE
            // ═══════════════════════════════════════════════════════
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, tint = Slate700)
                }
                Text(
                    "Mi Perfil",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 20.sp,
                    color = Slate900,
                    modifier = Modifier.weight(1f),
                    letterSpacing = (-0.3).sp
                )
                if (profile?.estadoVerificacion == "APROBADO") {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFDCFCE7))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            "Verificado",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF166534)
                        )
                    }
                }
            }

            if (errorMsg.isNotBlank()) {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = ErrorBg),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(errorMsg, modifier = Modifier.padding(12.dp), fontSize = 13.sp, color = ErrorText)
                }
            }

            // ═══════════════════════════════════════════════════════
            // PROFILE IDENTITY CARD - STAGGERED ENTRY 1
            // ═══════════════════════════════════════════════════════
            AnimatedVisibility(
                visible = isVisible,
                enter = fadeIn(tween(600, easing = EaseOutCubic)) +
                        slideInVertically(tween(600, easing = EaseOutCubic), initialOffsetY = { -30 })
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(top = 20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier.size(110.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(100.dp)
                                .clip(CircleShape)
                                .background(Slate100)
                                .border(3.dp, DeliveryBrand600.copy(alpha = 0.25f), CircleShape)
                                .shadow(20.dp, CircleShape, spotColor = DeliveryBrand600.copy(alpha = 0.15f))
                                .clickable { fotoPicker.launch("image/*") },
                            contentAlignment = Alignment.Center
                        ) {
                            val imgUrl = fotoPerfilLocal?.let {
                                if (it.startsWith("http")) it else com.agroconectago.app.data.api.ApiConfig.IMAGES_URL + it
                            }
                            if (imgUrl != null) {
                                AsyncImage(
                                    model = ImageRequest.Builder(context).data(imgUrl).crossfade(true).build(),
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            } else {
                                Icon(
                                    Icons.Filled.Person,
                                    contentDescription = null,
                                    tint = Slate400,
                                    modifier = Modifier.size(44.dp)
                                )
                            }
                        }
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(DeliveryBrand600)
                                .shadow(6.dp, CircleShape, spotColor = DeliveryBrand600.copy(alpha = 0.3f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Filled.CameraAlt,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    Spacer(Modifier.height(14.dp))

                    Text(
                        nombre,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Slate900,
                        letterSpacing = (-0.3).sp
                    )

                    if (email.isNotBlank()) {
                        Spacer(Modifier.height(2.dp))
                        Text(
                            email,
                            fontSize = 13.sp,
                            color = Slate400,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Spacer(Modifier.height(10.dp))

                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color.White.copy(alpha = 0.7f))
                            .border(1.dp, Color(0xFFFDE68A).copy(alpha = 0.5f), RoundedCornerShape(20.dp))
                            .shadow(6.dp, RoundedCornerShape(20.dp), spotColor = Color(0xFFF59E0B).copy(alpha = 0.08f))
                            .padding(horizontal = 14.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Filled.Star,
                            contentDescription = null,
                            tint = Color(0xFFF59E0B),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            "4.9",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 15.sp,
                            color = Slate900,
                            letterSpacing = (-0.2).sp
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            "\u00B7 Socio Verificado",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = Slate400
                        )
                    }
                }
            }

            Spacer(Modifier.height(28.dp))

            Spacer(Modifier.height(20.dp))

            // ═══════════════════════════════════════════════════════
            // EDIT PROFILE SECTION - STAGGERED ENTRY 2
            // ═══════════════════════════════════════════════════════
            AnimatedVisibility(
                visible = isVisible,
                enter = fadeIn(tween(600, delayMillis = 150, easing = EaseOutCubic)) +
                        slideInVertically(tween(600, delayMillis = 150, easing = EaseOutCubic), initialOffsetY = { 20 })
            ) {
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .clickable { mostrarEdicion = !mostrarEdicion }
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(Color(0xFFFAFBFC), Color.White)
                                )
                            )
                            .border(1.dp, Color(0xFFE8ECEF).copy(alpha = 0.5f), RoundedCornerShape(20.dp))
                            .padding(horizontal = 20.dp, vertical = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    Brush.linearGradient(
                                        colors = listOf(DeliveryBrand500, DeliveryBrand600)
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Filled.Edit,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "Editar Perfil",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 15.sp,
                                color = Slate900,
                                letterSpacing = (-0.2).sp
                            )
                            Text(
                                "Ciudad de entrega y datos del vehiculo",
                                fontSize = 12.sp,
                                color = Slate400
                            )
                        }
                        Icon(
                            if (mostrarEdicion) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                            contentDescription = null,
                            tint = Slate400,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    AnimatedVisibility(
                        visible = mostrarEdicion,
                        enter = expandVertically(tween(400, easing = EaseOutCubic)) + fadeIn(tween(400)),
                        exit = shrinkVertically(tween(300)) + fadeOut(tween(300))
                    ) {
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp),
                            shape = RoundedCornerShape(24.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.95f)),
                            border = BorderStroke(1.dp, Color(0xFFE8ECEF).copy(alpha = 0.6f)),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                        ) {
                            Column(modifier = Modifier.padding(20.dp)) {

                                // ═══════════ UBICACION (EDITABLE) ═══════════
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(DeliveryBrand600)
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        "UBICACION",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = DeliveryBrand600,
                                        letterSpacing = 1.5.sp
                                    )
                                }
                                Spacer(Modifier.height(10.dp))

                                var ciudadBusqueda by remember { mutableStateOf("") }
                                var mostrarLista by remember { mutableStateOf(false) }
                                val ciudadesFiltradas = remember(ciudadBusqueda) {
                                    if (ciudadBusqueda.isBlank()) emptyList()
                                    else ciudadesColombia.filter {
                                        it.contains(ciudadBusqueda, ignoreCase = true)
                                    }.take(20)
                                }

                                Box(modifier = Modifier.fillMaxWidth()) {
                                    Column {
                                        OutlinedTextField(
                                            value = if (mostrarLista) ciudadBusqueda else editCiudad,
                                            onValueChange = {
                                                ciudadBusqueda = it
                                                if (!mostrarLista) editCiudad = it
                                                mostrarLista = true
                                            },
                                            placeholder = { Text("Buscar ciudad...", fontSize = 13.sp) },
                                            leadingIcon = {
                                                Icon(
                                                    Icons.Filled.LocationOn,
                                                    contentDescription = null,
                                                    tint = DeliveryBrand600,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            },
                                            trailingIcon = {
                                                if (editCiudad.isNotEmpty() && !mostrarLista) {
                                                    IconButton(onClick = {
                                                        editCiudad = ""; ciudadBusqueda = ""
                                                    }) {
                                                        Icon(
                                                            Icons.Filled.Close,
                                                            contentDescription = null,
                                                            tint = Slate400,
                                                            modifier = Modifier.size(18.dp)
                                                        )
                                                    }
                                                }
                                            },
                                            singleLine = true,
                                            shape = RoundedCornerShape(14.dp),
                                            modifier = Modifier.fillMaxWidth().height(52.dp),
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedBorderColor = DeliveryBrand600,
                                                unfocusedBorderColor = Color(0xFFE2E8F0)
                                            )
                                        )
                                        if (mostrarLista && ciudadesFiltradas.isNotEmpty()) {
                                            Card(
                                                modifier = Modifier.fillMaxWidth().heightIn(max = 200.dp),
                                                shape = RoundedCornerShape(14.dp),
                                                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                                            ) {
                                                LazyColumn {
                                                    items(ciudadesFiltradas) { ciudad ->
                                                        Row(
                                                            modifier = Modifier
                                                                .fillMaxWidth()
                                                                .clickable {
                                                                    editCiudad = ciudad
                                                                    ciudadBusqueda = ""
                                                                    mostrarLista = false
                                                                }
                                                                .padding(horizontal = 14.dp, vertical = 12.dp),
                                                            verticalAlignment = Alignment.CenterVertically
                                                        ) {
                                                            Icon(
                                                                Icons.Filled.LocationOn,
                                                                contentDescription = null,
                                                                tint = Slate300,
                                                                modifier = Modifier.size(16.dp)
                                                            )
                                                            Spacer(Modifier.width(10.dp))
                                                            Text(
                                                                ciudad,
                                                                fontSize = 14.sp,
                                                                fontWeight = FontWeight.Medium,
                                                                color = Slate700
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }

                                Spacer(Modifier.height(22.dp))

                                // ═══════════ DATOS DEL VEHICULO (SOLO LECTURA) ═══════════
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(Slate400)
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        "DATOS DEL VEHICULO",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Slate400,
                                        letterSpacing = 1.5.sp
                                    )
                                    Spacer(Modifier.weight(1f))
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(Slate100)
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            "Solo lectura",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = Slate400
                                        )
                                    }
                                }
                                Spacer(Modifier.height(10.dp))

                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(18.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = Color(0xFFF8FAFC)
                                    ),
                                    border = BorderStroke(1.dp, Color(0xFFE8ECEF).copy(alpha = 0.5f))
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        // Row 1: Tipo + Placa
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                                        ) {
                                            VehiculoDatoPremium(
                                                modifier = Modifier.weight(1f),
                                                icon = Icons.Filled.LocalShipping,
                                                label = "Tipo",
                                                value = editVehiculo.ifBlank { "—" }
                                            )
                                            VehiculoDatoPremium(
                                                modifier = Modifier.weight(1f),
                                                icon = Icons.Filled.Numbers,
                                                label = "Placa",
                                                value = editPlaca.ifBlank { "—" }
                                            )
                                        }
                                        Spacer(Modifier.height(10.dp))
                                        // Row 2: Marca + Modelo
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                                        ) {
                                            VehiculoDatoPremium(
                                                modifier = Modifier.weight(1f),
                                                icon = Icons.Filled.Warehouse,
                                                label = "Marca",
                                                value = editMarca.ifBlank { "—" }
                                            )
                                            VehiculoDatoPremium(
                                                modifier = Modifier.weight(1f),
                                                icon = Icons.AutoMirrored.Filled.DirectionsBike,
                                                label = "Modelo",
                                                value = editModelo.ifBlank { "—" }
                                            )
                                        }
                                        Spacer(Modifier.height(10.dp))
                                        // Row 3: Ano + Color
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                                        ) {
                                            VehiculoDatoPremium(
                                                modifier = Modifier.weight(1f),
                                                icon = Icons.Filled.CalendarMonth,
                                                label = "Ano",
                                                value = editAnio.ifBlank { "—" }
                                            )
                                            VehiculoDatoPremium(
                                                modifier = Modifier.weight(1f),
                                                icon = Icons.Filled.Palette,
                                                label = "Color",
                                                value = editColor.ifBlank { "—" }
                                            )
                                        }
                                        Spacer(Modifier.height(10.dp))
                                        // Row 4: Capacidad + Licencia
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                                        ) {
                                            VehiculoDatoPremium(
                                                modifier = Modifier.weight(1f),
                                                icon = Icons.Filled.FitnessCenter,
                                                label = "Capacidad",
                                                value = if (editCapacidad.isNotBlank()) "${editCapacidad} kg" else "—"
                                            )
                                            VehiculoDatoPremium(
                                                modifier = Modifier.weight(1f),
                                                icon = Icons.Filled.Badge,
                                                label = "Licencia",
                                                value = editLicencia.ifBlank { "—" }
                                            )
                                        }
                                    }
                                }

                                Spacer(Modifier.height(22.dp))

                                Button(
                                    onClick = { guardar() },
                                    modifier = Modifier.fillMaxWidth().height(50.dp),
                                    enabled = !saving,
                                    shape = RoundedCornerShape(16.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = DeliveryBrand600,
                                        contentColor = Color.White,
                                        disabledContainerColor = DeliveryBrand600.copy(alpha = 0.45f),
                                        disabledContentColor = Color.White.copy(alpha = 0.7f)
                                    )
                                ) {
                                    if (saving) {
                                        CircularProgressIndicator(
                                            color = Color.White,
                                            modifier = Modifier.size(20.dp),
                                            strokeWidth = 2.dp
                                        )
                                        Spacer(Modifier.width(8.dp))
                                    } else {
                                        Icon(
                                            Icons.Filled.Save,
                                            contentDescription = null,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(Modifier.width(6.dp))
                                    }
                                    Text(
                                        if (saving) "Guardando..." else "Guardar cambios",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            // ═══════════════════════════════════════════════════════
            // LOGOUT BUTTON - STAGGERED ENTRY 3
            // ═══════════════════════════════════════════════════════
            AnimatedVisibility(
                visible = isVisible,
                enter = fadeIn(tween(600, delayMillis = 300, easing = EaseOutCubic)) +
                        slideInVertically(tween(600, delayMillis = 300, easing = EaseOutCubic), initialOffsetY = { 20 })
            ) {
                val logoutInteraction = remember { MutableInteractionSource() }
                val logoutPressed by logoutInteraction.collectIsPressedAsState()
                val logoutScale by animateFloatAsState(
                    targetValue = if (logoutPressed) 0.97f else 1f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow
                    ),
                    label = "logoutScale"
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .graphicsLayer { scaleX = logoutScale; scaleY = logoutScale }
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color.White.copy(alpha = 0.7f))
                        .border(1.dp, Color(0xFFFECACA).copy(alpha = 0.5f), RoundedCornerShape(20.dp))
                        .clickable(
                            interactionSource = logoutInteraction,
                            indication = null
                        ) { onLogout() }
                        .padding(horizontal = 24.dp, vertical = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.AutoMirrored.Filled.Logout,
                            contentDescription = null,
                            tint = ErrorRed.copy(alpha = 0.7f),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(10.dp))
                        Text(
                            "Cerrar sesion",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 15.sp,
                            color = ErrorRed.copy(alpha = 0.8f),
                            letterSpacing = (-0.1).sp
                        )
                    }
                }
            }

            Spacer(Modifier.height(32.dp))
        }

        // ═══════════════════════════════════════════════════════════
        // TOAST
        // ═══════════════════════════════════════════════════════════
        if (showToast) {
            LaunchedEffect(Unit) { kotlinx.coroutines.delay(2000); showToast = false }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 24.dp),
                contentAlignment = Alignment.Center
            ) {
                Surface(shape = RoundedCornerShape(16.dp), color = Color(0xFF166534), shadowElevation = 8.dp) {
                    Row(
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Filled.CheckCircle,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(toastMsg, color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun VehiculoDatoPremium(
    modifier: Modifier,
    icon: ImageVector,
    label: String,
    value: String
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White)
            .border(1.dp, Color(0xFFE8ECEF).copy(alpha = 0.5f), RoundedCornerShape(12.dp))
            .padding(horizontal = 12.dp, vertical = 10.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                icon,
                contentDescription = null,
                tint = Slate400,
                modifier = Modifier.size(14.dp)
            )
            Spacer(Modifier.width(5.dp))
            Text(
                label,
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
                color = Slate400,
                letterSpacing = 0.3.sp
            )
        }
        Spacer(Modifier.height(2.dp))
        Text(
            value,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = Slate900,
            letterSpacing = (-0.1).sp,
            maxLines = 1
        )
    }
}

@Composable
private fun ProfileNavRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "navScale"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .clip(RoundedCornerShape(18.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) { onClick() }
            .padding(horizontal = 18.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(
                    Brush.linearGradient(
                        colors = listOf(DeliveryBrand50, Color(0xFFECFEFF))
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = DeliveryBrand600, modifier = Modifier.size(20.dp))
        }
        Spacer(Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Slate700, letterSpacing = (-0.1).sp)
            Text(subtitle, fontSize = 11.sp, color = Slate400)
        }
        Icon(
            Icons.AutoMirrored.Filled.ArrowForward,
            contentDescription = null,
            tint = Slate300,
            modifier = Modifier.size(18.dp)
        )
    }
}
