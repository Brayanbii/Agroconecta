package com.agroconecta.app.ui.theme

import android.net.Uri
import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.app.ActivityCompat
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.agroconecta.app.data.api.RetrofitClient
import com.agroconecta.app.viewmodel.TiendaViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import java.io.File
import java.io.FileOutputStream

private val Carbon = Color(0xFF111827)
private val Slate400 = Color(0xFF94A3B8)
private val Slate500 = Color(0xFF64748B)
private val Slate300 = Color(0xFFCBD5E1)
private val Slate200 = Color(0xFFE2E8F0)
private val Slate100 = Color(0xFFF1F5F9)
private val AppBackground = Color(0xFFF8FAF9)
private val Emerald = Color(0xFF0E793D)
private val Purple500 = Color(0xFF7C3AED)
private val Purple600 = Color(0xFF6D28D9)
private val Purple50 = Color(0xFFF3E8FF)
private val Green500 = Color(0xFF22C55E)
private val Blue500 = Color(0xFF3B82F6)
private val Red500 = Color(0xFFEF4444)
private val Amber500 = Color(0xFFF59E0B)
private val Indigo500 = Color(0xFF6366F1)
private val Indigo600 = Color(0xFF4F46E5)
private val Teal500 = Color(0xFF14B8A6)
private val Rose500 = Color(0xFFF43F5E)

private val BASE_IMAGE_URL get() = com.agroconecta.app.data.api.ApiConfig.IMAGES_URL

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CampesinoPerfilScreen(tiendaVM: TiendaViewModel, onNavigateBack: () -> Unit) {
    LaunchedEffect(Unit) { tiendaVM.cargarMiPerfil() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    val data = tiendaVM.miPerfilResponse
    var nombreFinca by remember(data) { mutableStateOf(data?.nombreFinca ?: "") }
    var descripcion by remember(data) { mutableStateOf(data?.descripcionFinca ?: "") }
    var editNombre by remember(data) { mutableStateOf(data?.nombreCompleto ?: "") }
    var editTelefono by remember(data) { mutableStateOf(data?.telefono ?: "") }
    var editMunicipio by remember(data) { mutableStateOf(data?.municipioOrigen ?: "") }
    var editLat by remember(data) { mutableStateOf(data?.latitud) }
    var editLng by remember(data) { mutableStateOf(data?.longitud) }
    var fotoPerfilLocal by remember(data) { mutableStateOf(data?.fotoPerfil) }
    var fotoPortadaLocal by remember(data) { mutableStateOf(data?.fotoFincaUrl) }
    var subiendoFoto by remember { mutableStateOf(false) }
    var isVisible by remember { mutableStateOf(false) }
    var showSuccessToast by remember { mutableStateOf(false) }
    var toastMessage by remember { mutableStateOf("") }
    LaunchedEffect(Unit) { isVisible = true }

    fun mostrarToast(mensaje: String) {
        toastMessage = mensaje
        showSuccessToast = true
    }

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
                    val requestBody = file.asRequestBody("image/*".toMediaTypeOrNull())
                    val part = MultipartBody.Part.createFormData("file", file.name, requestBody)
                    val resp = RetrofitClient.usuarioApiService.subirImagen(part)
                    val filename = resp["filename"] as? String
                    if (filename != null) {
                        fotoPerfilLocal = filename
                        tiendaVM.actualizarPerfil(nombreFinca, descripcion, filename, fotoPortadaLocal)
                        mostrarToast("Foto de perfil actualizada")
                    }
                } catch (_: Exception) {} finally { subiendoFoto = false }
            }
        }
    }

    val coverPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
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
                    val requestBody = file.asRequestBody("image/*".toMediaTypeOrNull())
                    val part = MultipartBody.Part.createFormData("file", file.name, requestBody)
                    val resp = RetrofitClient.usuarioApiService.subirImagen(part)
                    val filename = resp["filename"] as? String
                    if (filename != null) {
                        fotoPortadaLocal = filename
                        tiendaVM.actualizarPerfil(nombreFinca, descripcion, fotoPerfilLocal, filename)
                        mostrarToast("Foto de portada actualizada")
                    }
                } catch (_: Exception) {} finally { subiendoFoto = false }
            }
        }
    }

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
                                "Tu Perfil Publico",
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
                                        .background(Purple500.copy(alpha = pulseAnim))
                                )
                                Spacer(Modifier.width(6.dp))
                                Text(
                                    "Personalizacion activa",
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
                                        colors = listOf(Purple500, Indigo500)
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Filled.Person, null, tint = Color.White, modifier = Modifier.size(20.dp))
                        }
                    }
                }
            }
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize()) {
            if (tiendaVM.perfilCargando && data == null) {
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
                                        colors = listOf(Purple500.copy(alpha = alpha * 0.2f), Indigo500.copy(alpha = alpha * 0.1f))
                                    )
                                )
                                .graphicsLayer { scaleX = scale; scaleY = scale },
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                color = Purple500,
                                strokeWidth = 3.dp,
                                modifier = Modifier.size(40.dp)
                            )
                        }
                        Spacer(Modifier.height(20.dp))
                        Text(
                            "Cargando perfil...",
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
                                            colors = listOf(Purple500.copy(alpha = 0.08f), Color.Transparent)
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
                                    Icon(Icons.Filled.Person, null, tint = Slate400, modifier = Modifier.size(44.dp))
                                }
                            }
                            Spacer(Modifier.height(28.dp))
                            Text(
                                data?.error ?: "Sin datos",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = Carbon,
                                letterSpacing = (-0.3).sp
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                "No hay informacion de perfil disponible",
                                fontSize = 14.sp,
                                color = Slate500,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
                return@Scaffold
            }

            val imgPerfil = if (!fotoPerfilLocal.isNullOrBlank() && fotoPerfilLocal != "default.jpg") {
                if (fotoPerfilLocal!!.startsWith("http")) fotoPerfilLocal else BASE_IMAGE_URL + fotoPerfilLocal
            } else null
            val imgPortada = if (!fotoPortadaLocal.isNullOrBlank() && fotoPortadaLocal != "default.jpg") {
                if (fotoPortadaLocal!!.startsWith("http")) fotoPortadaLocal else BASE_IMAGE_URL + fotoPortadaLocal
            } else null

            LazyColumn(modifier = Modifier.fillMaxSize().padding(padding), contentPadding = PaddingValues(bottom = 32.dp)) {
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

                            Column {
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
                                                .background(Color(0xFFA78BFA).copy(alpha = dotPulse))
                                        )
                                        Spacer(Modifier.width(8.dp))
                                        Text("Personalizacion", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White.copy(alpha = 0.95f))
                                    }
                                }
                                Spacer(Modifier.height(16.dp))
                                Text(
                                    "Tu Perfil Publico",
                                    fontSize = 30.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White,
                                    letterSpacing = (-0.8).sp
                                )
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    "Asi es como te ven los clientes en la tienda.",
                                    fontSize = 14.sp,
                                    color = Color.White.copy(alpha = 0.75f),
                                    lineHeight = 20.sp
                                )
                            }
                        }
                    }
                }

                item {
                    AnimatedVisibility(
                        visible = isVisible,
                        enter = fadeIn(tween(500, delayMillis = 300)) + slideInVertically(tween(500, delayMillis = 300, easing = EaseOutCubic), initialOffsetY = { it / 4 })
                    ) {
                        Surface(
                            color = Color.White,
                            shape = RoundedCornerShape(28.dp),
                            shadowElevation = 0.dp,
                            border = BorderStroke(1.dp, Slate200),
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp)
                        ) {
                            Column {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(160.dp)
                                        .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                                        .background(Slate200)
                                ) {
                                    if (imgPortada != null) {
                                        AsyncImage(
                                            model = ImageRequest.Builder(context).data(imgPortada).crossfade(true).build(),
                                            contentDescription = null,
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    }
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = 0.5f))))
                                    )
                                }

                                Row(
                                    modifier = Modifier.padding(horizontal = 20.dp).offset(y = (-50).dp),
                                    verticalAlignment = Alignment.Bottom
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(96.dp)
                                            .clip(CircleShape)
                                            .background(Color.White)
                                            .border(4.dp, Color.White, CircleShape)
                                            .shadow(12.dp, CircleShape)
                                    ) {
                                        if (imgPerfil != null) {
                                            AsyncImage(
                                                model = ImageRequest.Builder(context).data(imgPerfil).crossfade(true).build(),
                                                contentDescription = null,
                                                contentScale = ContentScale.Crop,
                                                modifier = Modifier.fillMaxSize().clip(CircleShape)
                                            )
                                        } else {
                                            Box(
                                                Modifier.fillMaxSize().background(
                                                    Brush.linearGradient(listOf(Purple500, Indigo500))
                                                ),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    data.nombreCompleto?.firstOrNull()?.uppercase() ?: "U",
                                                    fontSize = 34.sp,
                                                    fontWeight = FontWeight.ExtraBold,
                                                    color = Color.White
                                                )
                                            }
                                        }
                                    }
                                    Spacer(Modifier.width(16.dp))
                                    Column(modifier = Modifier.padding(bottom = 10.dp)) {
                                        Text(
                                            nombreFinca.ifBlank { data.nombreCompleto ?: "Campesino" },
                                            fontSize = 24.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Carbon,
                                            letterSpacing = (-0.5).sp
                                        )
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Filled.LocationOn, null, tint = Red500, modifier = Modifier.size(15.dp))
                                            Spacer(Modifier.width(4.dp))
                                            Text(
                                                data.municipioOrigen ?: "Colombia",
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = Slate500
                                            )
                                            Text("  •  ", fontSize = 13.sp, color = Slate300)
                                            Icon(Icons.Filled.Star, null, tint = Amber500, modifier = Modifier.size(13.dp))
                                            Spacer(Modifier.width(3.dp))
                                            Text(
                                                " ${data.totalProductos} productos",
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = Slate500
                                            )
                                        }
                                    }
                                }

                                Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                                    Text(
                                        "Sobre nosotros",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Slate400,
                                        letterSpacing = 0.3.sp
                                    )
                                    Spacer(Modifier.height(6.dp))
                                    Text(
                                        descripcion.ifBlank { "Productor de AgroConecta. Prueba mis productos frescos!" },
                                        fontSize = 14.sp,
                                        color = Slate500,
                                        lineHeight = 20.sp
                                    )
                                    Spacer(Modifier.height(20.dp))
                                }
                            }
                        }
                    }
                }

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
                                            .background(Brush.linearGradient(listOf(Purple500, Indigo500))),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Filled.Edit, null, tint = Color.White, modifier = Modifier.size(22.dp))
                                    }
                                    Spacer(Modifier.width(14.dp))
                                    Text(
                                        "Editar Perfil",
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Carbon,
                                        letterSpacing = (-0.3).sp
                                    )
                                }

                                if (tiendaVM.perfilGuardando) {
                                    Spacer(Modifier.height(12.dp))
                                    LinearProgressIndicator(
                                        modifier = Modifier.fillMaxWidth().height(3.dp).clip(RoundedCornerShape(2.dp)),
                                        color = Purple500,
                                        trackColor = Slate100
                                    )
                                }

                                Spacer(Modifier.height(20.dp))

                                Text(
                                    "Nombre de tu Finca o Tienda",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Slate500,
                                    letterSpacing = 0.3.sp
                                )
                                Spacer(Modifier.height(8.dp))
                                OutlinedTextField(
                                    value = nombreFinca,
                                    onValueChange = { nombreFinca = it },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true,
                                    shape = RoundedCornerShape(16.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = Purple500,
                                        unfocusedBorderColor = Slate200,
                                        focusedContainerColor = Color.White,
                                        unfocusedContainerColor = Slate100.copy(alpha = 0.3f)
                                    )
                                )

                                Spacer(Modifier.height(18.dp))

                                Text(
                                    "Foto de Perfil (Avatar)",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Slate500,
                                    letterSpacing = 0.3.sp
                                )
                                Spacer(Modifier.height(8.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(72.dp)
                                            .clip(CircleShape)
                                            .background(Slate100)
                                            .border(3.dp, Slate200, CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (imgPerfil != null) {
                                            AsyncImage(
                                                model = ImageRequest.Builder(context).data(imgPerfil).crossfade(true).build(),
                                                contentDescription = null,
                                                contentScale = ContentScale.Crop,
                                                modifier = Modifier.fillMaxSize().clip(CircleShape)
                                            )
                                        } else {
                                            Icon(Icons.Filled.Person, null, tint = Slate400, modifier = Modifier.size(32.dp))
                                        }
                                    }
                                    Spacer(Modifier.width(14.dp))
                                    PremiumOutlinedButton(
                                        onClick = { imagePicker.launch("image/*") },
                                        enabled = !subiendoFoto
                                    ) {
                                        Text("Cambiar foto", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Purple500)
                                    }
                                }

                                Spacer(Modifier.height(18.dp))

                                Text(
                                    "Foto de Portada",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Slate500,
                                    letterSpacing = 0.3.sp
                                )
                                Spacer(Modifier.height(8.dp))
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(90.dp)
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(Slate100)
                                        .border(2.dp, Slate200, RoundedCornerShape(16.dp))
                                ) {
                                    if (imgPortada != null) {
                                        AsyncImage(
                                            model = ImageRequest.Builder(context).data(imgPortada).crossfade(true).build(),
                                            contentDescription = null,
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    } else {
                                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                            Text("Sin portada", fontSize = 13.sp, color = Slate400, fontWeight = FontWeight.Medium)
                                        }
                                    }
                                }
                                Spacer(Modifier.height(8.dp))
                                PremiumOutlinedButton(
                                    onClick = { coverPicker.launch("image/*") },
                                    enabled = !subiendoFoto
                                ) {
                                    Text("Cambiar portada", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Purple500)
                                }

                                Spacer(Modifier.height(18.dp))

                                Text(
                                    "Tu Historia / Descripcion",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Slate500,
                                    letterSpacing = 0.3.sp
                                )
                                Spacer(Modifier.height(8.dp))
                                OutlinedTextField(
                                    value = descripcion,
                                    onValueChange = { descripcion = it },
                                    modifier = Modifier.fillMaxWidth().height(110.dp),
                                    maxLines = 5,
                                    shape = RoundedCornerShape(16.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = Purple500,
                                        unfocusedBorderColor = Slate200,
                                        focusedContainerColor = Color.White,
                                        unfocusedContainerColor = Slate100.copy(alpha = 0.3f)
                                    )
                                )

                                Spacer(Modifier.height(18.dp))

                                // --- DATOS PERSONALES ---
                                Text("Datos Personales", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Slate500, letterSpacing = 0.3.sp)
                                Spacer(Modifier.height(8.dp))
                                OutlinedTextField(
                                    value = editNombre,
                                    onValueChange = { editNombre = it },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true,
                                    shape = RoundedCornerShape(16.dp),
                                    label = { Text("Nombre completo") },
                                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Purple500, unfocusedBorderColor = Slate200, focusedContainerColor = Color.White, unfocusedContainerColor = Slate100.copy(alpha = 0.3f))
                                )
                                Spacer(Modifier.height(10.dp))
                                OutlinedTextField(
                                    value = editTelefono,
                                    onValueChange = { editTelefono = it },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true,
                                    shape = RoundedCornerShape(16.dp),
                                    label = { Text("Telefono") },
                                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Purple500, unfocusedBorderColor = Slate200, focusedContainerColor = Color.White, unfocusedContainerColor = Slate100.copy(alpha = 0.3f))
                                )
                                Spacer(Modifier.height(10.dp))
                                OutlinedTextField(
                                    value = editMunicipio,
                                    onValueChange = { editMunicipio = it },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true,
                                    shape = RoundedCornerShape(16.dp),
                                    label = { Text("Municipio / Vereda") },
                                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Purple500, unfocusedBorderColor = Slate200, focusedContainerColor = Color.White, unfocusedContainerColor = Slate100.copy(alpha = 0.3f))
                                )

                                Spacer(Modifier.height(20.dp))

                                // --- MAPA GPS ---
                                Text("Ubicacion GPS de la Finca", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Slate500, letterSpacing = 0.3.sp)
                                Text("Arrastra el marcador a la ubicacion exacta", fontSize = 10.sp, color = Slate400)
                                Spacer(Modifier.height(8.dp))

                                var fMapView by remember { mutableStateOf<MapView?>(null) }
                                var fMarker by remember { mutableStateOf<Marker?>(null) }

                                val defaultLat = editLat ?: 4.7110
                                val defaultLng = editLng ?: -74.0721

                                Box(
                                    Modifier.fillMaxWidth().height(260.dp).clip(RoundedCornerShape(16.dp)).background(Slate100)
                                ) {
                                    AndroidView(
                                        factory = { ctx ->
                                            Configuration.getInstance().apply {
                                                userAgentValue = ctx.packageName
                                                osmdroidBasePath = ctx.getExternalFilesDir(null)
                                                osmdroidTileCache = ctx.getExternalFilesDir("tiles")
                                            }
                                            MapView(ctx).apply {
                                                setTileSource(TileSourceFactory.MAPNIK)
                                                setMultiTouchControls(true)
                                                controller.setZoom(if (editLat != null) 16.0 else 6.0)
                                                controller.setCenter(GeoPoint(defaultLat, defaultLng))

                                                val m = Marker(this)
                                                m.position = GeoPoint(defaultLat, defaultLng)
                                                m.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                                                m.title = nombreFinca.ifBlank { "Mi Finca" }
                                                m.isDraggable = true
                                                m.setOnMarkerDragListener(object : Marker.OnMarkerDragListener {
                                                    override fun onMarkerDrag(marker: Marker?) {}
                                                    override fun onMarkerDragEnd(marker: Marker?) {
                                                        marker?.let {
                                                            editLat = it.position.latitude
                                                            editLng = it.position.longitude
                                                        }
                                                    }
                                                    override fun onMarkerDragStart(marker: Marker?) {}
                                                })
                                                overlays.add(m)
                                                fMarker = m
                                                invalidate()
                                                fMapView = this
                                            }
                                        },
                                        modifier = Modifier.fillMaxSize()
                                    ) { mv ->
                                        val lat = editLat; val lng = editLng
                                        if (lat != null && lng != null) {
                                            fMarker?.position = GeoPoint(lat, lng)
                                            fMarker?.title = nombreFinca.ifBlank { "Mi Finca" }
                                        }
                                    }
                                }

                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    OutlinedButton(
                                        onClick = {
                                            val lm = context.getSystemService(android.content.Context.LOCATION_SERVICE) as? LocationManager ?: return@OutlinedButton
                                            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) return@OutlinedButton
                                            var loc: Location? = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                                            if (loc == null) loc = lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
                                            if (loc != null) {
                                                editLat = loc.latitude; editLng = loc.longitude
                                                fMapView?.controller?.animateTo(GeoPoint(loc.latitude, loc.longitude), 18.0, 800L)
                                            } else {
                                                val listener = object : LocationListener {
                                                    override fun onLocationChanged(l: Location) {
                                                        editLat = l.latitude; editLng = l.longitude
                                                        fMapView?.controller?.animateTo(GeoPoint(l.latitude, l.longitude), 18.0, 800L)
                                                        try { lm.removeUpdates(this) } catch (_: Exception) {}
                                                    }
                                                    override fun onStatusChanged(p: String?, s: Int, e: Bundle?) {}
                                                    override fun onProviderEnabled(p: String) {}
                                                    override fun onProviderDisabled(p: String) {}
                                                }
                                                lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0L, 0f, listener)
                                            }
                                        },
                                        modifier = Modifier.weight(1f).padding(top = 4.dp),
                                        shape = RoundedCornerShape(10.dp)
                                    ) { Icon(Icons.Filled.GpsFixed, null, Modifier.size(15.dp)); Spacer(Modifier.width(4.dp)); Text("GPS", fontSize = 12.sp) }
                                    OutlinedButton(
                                        onClick = {
                                            val lat = editLat; val lng = editLng
                                            if (lat != null && lng != null) fMapView?.controller?.animateTo(GeoPoint(lat, lng), 17.0, 800L)
                                        },
                                        modifier = Modifier.weight(1f).padding(top = 4.dp),
                                        shape = RoundedCornerShape(10.dp)
                                    ) { Icon(Icons.Filled.MyLocation, null, Modifier.size(15.dp)); Spacer(Modifier.width(4.dp)); Text("Centrar", fontSize = 12.sp) }
                                }

                                if (editLat != null && editLng != null) {
                                    Row(Modifier.fillMaxWidth().padding(top = 4.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Text("📍 ${String.format("%.5f", editLat!!)}, ${String.format("%.5f", editLng!!)}", fontSize = 10.sp, color = Slate400, fontWeight = FontWeight.Medium)
                                    }
                                }

                                Spacer(Modifier.height(20.dp))

                                PremiumButton(
                                    onClick = {
                                        tiendaVM.actualizarPerfil(
                                            nombreFinca = nombreFinca,
                                            descripcionFinca = descripcion,
                                            fotoPerfil = fotoPerfilLocal,
                                            fotoFincaUrl = fotoPortadaLocal,
                                            nombreCompleto = editNombre,
                                            telefono = editTelefono,
                                            municipioOrigen = editMunicipio,
                                            latitud = editLat,
                                            longitud = editLng
                                        ) { ok -> if (ok) mostrarToast("Tus cambios han sido guardados") }
                                    },
                                    modifier = Modifier.fillMaxWidth().height(52.dp),
                                    enabled = !tiendaVM.perfilGuardando
                                ) {
                                    Icon(Icons.Filled.Save, null, tint = Color.White, modifier = Modifier.size(20.dp))
                                    Spacer(Modifier.width(10.dp))
                                    Text("Guardar Cambios", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 15.sp)
                                }
                            }
                        }
                    }
                }

                item { Spacer(Modifier.height(24.dp)) }
            }

            LaunchedEffect(showSuccessToast) {
                if (showSuccessToast) {
                    kotlinx.coroutines.delay(2500)
                    showSuccessToast = false
                }
            }

            AnimatedVisibility(
                visible = showSuccessToast,
                enter = fadeIn(tween(300)) + slideInVertically(tween(300, easing = EaseOutCubic), initialOffsetY = { -it / 3 }),
                exit = fadeOut(tween(300)) + slideOutVertically(tween(300, easing = EaseInCubic), targetOffsetY = { -it / 3 }),
                modifier = Modifier.align(Alignment.TopCenter)
            ) {
                Surface(
                    color = Color.White,
                    shape = RoundedCornerShape(20.dp),
                    shadowElevation = 12.dp,
                    border = BorderStroke(1.dp, Green500.copy(alpha = 0.2f)),
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 18.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.linearGradient(listOf(Green500, Teal500))
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Filled.CheckCircle, null, tint = Color.White, modifier = Modifier.size(20.dp))
                        }
                        Spacer(Modifier.width(12.dp))
                        Text(
                            toastMessage,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Carbon,
                            letterSpacing = (-0.2.sp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PremiumOutlinedButton(
    onClick: () -> Unit,
    enabled: Boolean,
    content: @Composable RowScope.() -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        if (isPressed) 0.96f else 1f,
        spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow)
    )

    OutlinedButton(
        onClick = onClick,
        modifier = Modifier.graphicsLayer { scaleX = scale; scaleY = scale },
        enabled = enabled,
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.5.dp, Purple500),
        interactionSource = interactionSource
    ) {
        content()
    }
}

@Composable
private fun PremiumButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean,
    content: @Composable RowScope.() -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        if (isPressed) 0.97f else 1f,
        spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow)
    )

    Button(
        onClick = onClick,
        modifier = modifier.graphicsLayer { scaleX = scale; scaleY = scale },
        enabled = enabled,
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Purple500,
            contentColor = Color.White
        ),
        interactionSource = interactionSource
    ) {
        content()
    }
}