package com.agroconecta.app.ui.theme

import android.Manifest
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.agroconecta.app.data.api.RetrofitClient
import com.agroconecta.app.viewmodel.TiendaViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileOutputStream

private val Carbon = Color(0xFF111827)
private val Slate400 = Color(0xFF94A3B8)
private val Slate500 = Color(0xFF64748B)
private val Slate300 = Color(0xFFCBD5E1)
private val Slate200 = Color(0xFFE2E8F0)
private val Slate100 = Color(0xFFF1F5F9)
private val AppBg = Color(0xFFF9FBF9)
private val Emerald = Color(0xFF0E793D)
private val Green500 = Color(0xFF22C55E)
private val Green50 = Color(0xFFF0FDF4)
private val Blue500 = Color(0xFF3B82F6)
private val Blue50 = Color(0xFFEFF6FF)
private val Amber500 = Color(0xFFF59E0B)
private val Amber50 = Color(0xFFFFFDE7)
private val Red500 = Color(0xFFEF4444)
private val Red50 = Color(0xFFFEF2F2)
private val Orange500 = Color(0xFFF97316)

private val BASE_URL get() = com.agroconecta.app.data.api.ApiConfig.IMAGES_URL

@Composable
fun CampesinoVerificacionScreen(tiendaVM: TiendaViewModel, onVerificacionCompleta: () -> Unit, onLogout: () -> Unit = {}) {
    LaunchedEffect(Unit) { tiendaVM.cargarEstadoVerificacion() }
    val data = tiendaVM.verificacionResponse
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    if (tiendaVM.verificacionCargando && data == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = Emerald) }
        return
    }

    val estado = data?.estado ?: "PENDIENTE_DATOS"

    when (estado) {
        "APROBADO" -> {
            LaunchedEffect(Unit) { onVerificacionCompleta() }
        }
        "EN_REVISION" -> EstadoRevision(onLogout)
        "RECHAZADO" -> EstadoRechazado { tiendaVM.cargarEstadoVerificacion() }
        "VETADO" -> EstadoVetado()
        else -> FormularioVerificacion(tiendaVM, data, scope, context)
    }
}

@Composable
private fun FormularioVerificacion(tiendaVM: TiendaViewModel, data: com.agroconecta.app.data.model.VerificacionResponse?, scope: kotlinx.coroutines.CoroutineScope, context: android.content.Context) {
    var step by remember { mutableStateOf(1) }
    var cedula by remember { mutableStateOf(data?.numeroIdentidad ?: "") }
    var nombreFinca by remember { mutableStateOf(data?.nombreFinca ?: "") }
    var historiaFinca by remember { mutableStateOf("") }
    var dirFinca by remember { mutableStateOf(data?.descripcionFinca ?: "") }
    var municipio by remember { mutableStateOf(data?.municipioOrigen ?: "") }
    var fotoCedulaLocal by remember { mutableStateOf(data?.fotoCedulaUrl) }
    var fotoFincaLocal by remember { mutableStateOf(data?.fotoFincaUrl) }
    var subiendoFoto by remember { mutableStateOf(false) }
    var lat by remember { mutableStateOf(data?.latitud) }
    var lng by remember { mutableStateOf(data?.longitud) }
    var enviado by remember { mutableStateOf(false) }
    var errorMsg by remember { mutableStateOf("") }

    val cedulaPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let { u -> scope.launch { subiendoFoto = true
            try { val file = withContext(Dispatchers.IO) { val input = context.contentResolver.openInputStream(u); val temp = File(context.cacheDir, "cedula_${System.currentTimeMillis()}.jpg"); input?.use { it.copyTo(FileOutputStream(temp)) }; temp }
                if (file.exists()) { val part = MultipartBody.Part.createFormData("file", file.name, file.asRequestBody("image/*".toMediaTypeOrNull())); val resp = RetrofitClient.usuarioApiService.subirImagen(part); (resp["filename"] as? String)?.let { fotoCedulaLocal = it } }
            } catch (_: Exception) {} finally { subiendoFoto = false } }
        }
    }
    val fincaPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let { u -> scope.launch { subiendoFoto = true
            try { val file = withContext(Dispatchers.IO) { val input = context.contentResolver.openInputStream(u); val temp = File(context.cacheDir, "finca_${System.currentTimeMillis()}.jpg"); input?.use { it.copyTo(FileOutputStream(temp)) }; temp }
                if (file.exists()) { val part = MultipartBody.Part.createFormData("file", file.name, file.asRequestBody("image/*".toMediaTypeOrNull())); val resp = RetrofitClient.usuarioApiService.subirImagen(part); (resp["filename"] as? String)?.let { fotoFincaLocal = it } }
            } catch (_: Exception) {} finally { subiendoFoto = false } }
        }
    }

    var mostrarMapa by remember { mutableStateOf(false) }

    Box(Modifier.fillMaxSize()) {
        Scaffold(containerColor = AppBg, topBar = {
        Surface(shadowElevation = 0.dp, color = Color.White) {
            Row(Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = 16.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                Text("Verificacion de Productor", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = Carbon)
            }
        }
    }) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            // Step indicators
            Surface(Modifier.fillMaxWidth(), color = Color.White) {
                Row(Modifier.fillMaxWidth().padding(horizontal = 40.dp, vertical = 16.dp), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                    StepDot(1, step, "Identidad")
                    Box(Modifier.width(40.dp).height(2.dp).background(if (step >= 2) Emerald else Slate300))
                    StepDot(2, step, "Finca")
                }
            }

            if (enviado) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Filled.CheckCircle, null, tint = Green500, modifier = Modifier.size(64.dp))
                        Spacer(Modifier.height(16.dp))
                        Text("Verificacion enviada!", fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = Carbon)
                        Text("Un administrador revisara tus datos.", fontSize = 14.sp, color = Slate400)
                    }
                }
            } else if (step == 1) {
                // STEP 1: Identidad
                Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text("Paso 1: Verifica tu Identidad", fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = Carbon)
                    Text("Necesitamos tu numero de cedula y una foto del documento para validar que eres un productor real.", fontSize = 13.sp, color = Slate500)

                    OutlinedTextField(
                        value = cedula,
                        onValueChange = { v -> if (v.all { it.isDigit() } && v.length <= 10) cedula = v },
                        Modifier.fillMaxWidth(),
                        label = { Text("Numero de Cedula") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Emerald),
                        supportingText = { if (cedula.isNotEmpty() && cedula.length < 6) Text("Minimo 6 digitos", fontSize = 11.sp, color = Red500) }
                    )
                    Text("Foto de tu Cedula (frontal)", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Slate400)

                    Surface(Modifier.fillMaxWidth().height(180.dp).clip(RoundedCornerShape(16.dp)).border(2.dp, if (fotoCedulaLocal != null) Emerald else Slate300, RoundedCornerShape(16.dp)).clickable { cedulaPicker.launch("image/*") }, color = Slate100) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            if (fotoCedulaLocal != null) {
                                val url = if (fotoCedulaLocal!!.startsWith("http")) fotoCedulaLocal else BASE_URL + fotoCedulaLocal
                                AsyncImage(model = ImageRequest.Builder(context).data(url).crossfade(true).build(), contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                            } else {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) { Icon(Icons.Filled.CameraAlt, null, tint = Slate300, modifier = Modifier.size(40.dp)); Spacer(Modifier.height(8.dp)); Text("Toca para subir foto", fontSize = 13.sp, color = Slate400) }
                            }
                        }
                    }
                    if (subiendoFoto) LinearProgressIndicator(Modifier.fillMaxWidth(), color = Emerald)

                    // Error de validacion
                    if (errorMsg.isNotBlank()) {
                        Surface(Modifier.fillMaxWidth(), color = Red50, shape = RoundedCornerShape(12.dp), border = BorderStroke(1.dp, Red500.copy(alpha = 0.2f))) {
                            Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.ErrorOutline, null, tint = Red500, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(8.dp))
                                Text(errorMsg, fontSize = 13.sp, color = Red500, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Spacer(Modifier.weight(1f))
                    Button(onClick = {
                        errorMsg = ""
                        when {
                            cedula.isBlank() -> errorMsg = "Debes ingresar tu numero de cedula"
                            cedula.length < 6 -> errorMsg = "La cedula debe tener al menos 6 digitos"
                            fotoCedulaLocal == null -> errorMsg = "Debes subir una foto de tu cedula"
                            else -> step = 2
                        }
                    }, Modifier.fillMaxWidth().height(52.dp), enabled = true, colors = ButtonDefaults.buttonColors(containerColor = Emerald), shape = RoundedCornerShape(14.dp)) {
                        Text("Siguiente", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White)
                    }
                }
            } else {
                // STEP 2: Finca
                Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Text("Paso 2: Tu Finca", fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = Carbon)
                    Text("Cuentanos sobre tu finca, tu historia como productor y sube fotos de tu terreno.", fontSize = 13.sp, color = Slate500)

                    OutlinedTextField(nombreFinca, { nombreFinca = it }, Modifier.fillMaxWidth(), label = { Text("Nombre de tu Finca") }, placeholder = { Text("Ej: Finca La Esperanza") }, singleLine = true, shape = RoundedCornerShape(12.dp), colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Emerald))

                    // Nuestra historia
                    Text("Nuestra historia", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Slate400)
                    OutlinedTextField(historiaFinca, { historiaFinca = it }, Modifier.fillMaxWidth().height(100.dp), placeholder = { Text("Describe brevemente tu finca, que cultivas, hace cuanto, tu historia como productor...") }, shape = RoundedCornerShape(12.dp), colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Emerald))

                    Text("Foto de tu Finca", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Slate400)
                    Surface(Modifier.fillMaxWidth().height(140.dp).clip(RoundedCornerShape(16.dp)).border(2.dp, if (fotoFincaLocal != null) Emerald else Slate300, RoundedCornerShape(16.dp)).clickable { fincaPicker.launch("image/*") }, color = Slate100) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            if (fotoFincaLocal != null) {
                                val url = if (fotoFincaLocal!!.startsWith("http")) fotoFincaLocal else BASE_URL + fotoFincaLocal
                                AsyncImage(model = ImageRequest.Builder(context).data(url).crossfade(true).build(), contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                            } else {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) { Icon(Icons.Filled.Landscape, null, tint = Slate300, modifier = Modifier.size(40.dp)); Spacer(Modifier.height(8.dp)); Text("Toca para subir foto", fontSize = 13.sp, color = Slate400) }
                            }
                        }
                    }
                    if (subiendoFoto) LinearProgressIndicator(Modifier.fillMaxWidth(), color = Emerald)

                    // UBICACION
                    Text("Ubicacion de tu Finca", fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = Carbon)
                    
                    Surface(
                        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).border(1.dp, Slate200).clickable { mostrarMapa = true },
                        color = Color.White, shadowElevation = 2.dp
                    ) {
                        Column(Modifier.padding(16.dp)) {
                            if (lat != null && lng != null) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(Modifier.size(48.dp).clip(RoundedCornerShape(14.dp)).background(Green50), contentAlignment = Alignment.Center) {
                                        Icon(Icons.Filled.PinDrop, null, tint = Green500, modifier = Modifier.size(26.dp))
                                    }
                                    Spacer(Modifier.width(14.dp))
                                    Column(Modifier.weight(1f)) {
                                        Text("Ubicacion seleccionada", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Green500)
                                        Spacer(Modifier.height(2.dp))
                                        Text("📍 ${"%.4f".format(lat!!)}, ${"%.4f".format(lng!!)}", fontSize = 12.sp, color = Slate500, fontWeight = FontWeight.Bold)
                                    }
                                    Surface(color = Green50, shape = RoundedCornerShape(8.dp), border = BorderStroke(1.dp, Green500.copy(alpha = 0.2f))) {
                                        Text("Cambiar", Modifier.padding(horizontal = 10.dp, vertical = 4.dp), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Green500)
                                    }
                                }
                            } else {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(Modifier.size(48.dp).clip(RoundedCornerShape(14.dp)).background(Slate100), contentAlignment = Alignment.Center) {
                                        Icon(Icons.Filled.PinDrop, null, tint = Slate400, modifier = Modifier.size(26.dp))
                                    }
                                    Spacer(Modifier.width(14.dp))
                                    Column(Modifier.weight(1f)) {
                                        Text("Sin ubicacion", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Slate500)
                                        Text("Toca para buscarla en el mapa", fontSize = 12.sp, color = Slate400)
                                    }
                                    Icon(Icons.Filled.ChevronRight, null, tint = Slate300, modifier = Modifier.size(20.dp))
                                }
                            }
                        }
                    }

                    // Tarjeta de direccion obtenida del mapa
                    if (dirFinca.isNotBlank() && lat != null) {
                        Surface(
                            Modifier.fillMaxWidth(), color = Green50, shape = RoundedCornerShape(14.dp), border = BorderStroke(1.dp, Green500.copy(alpha = 0.15f))
                        ) {
                            Row(Modifier.padding(12.dp), verticalAlignment = Alignment.Top) {
                                Icon(Icons.Filled.MyLocation, null, tint = Green500, modifier = Modifier.size(18.dp).padding(top = 2.dp))
                                Spacer(Modifier.width(10.dp))
                                Column {
                                    Text("Direccion encontrada", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Green500)
                                    Spacer(Modifier.height(2.dp))
                                    Text(dirFinca, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = Carbon, lineHeight = 18.sp)
                                }
                            }
                        }
                    }

                    // Error de validacion step 2
                    if (errorMsg.isNotBlank()) {
                        Surface(Modifier.fillMaxWidth(), color = Red50, shape = RoundedCornerShape(12.dp), border = BorderStroke(1.dp, Red500.copy(alpha = 0.2f))) {
                            Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.ErrorOutline, null, tint = Red500, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(8.dp))
                                Text(errorMsg, fontSize = 13.sp, color = Red500, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Spacer(Modifier.weight(1f))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedButton(onClick = { step = 1; errorMsg = "" }, Modifier.weight(1f).height(52.dp), shape = RoundedCornerShape(14.dp)) { Text("Atras", color = Slate500) }
                        Button(onClick = {
                            errorMsg = ""
                            when {
                                nombreFinca.isBlank() -> errorMsg = "Debes poner el nombre de tu finca"
                                fotoFincaLocal == null -> errorMsg = "Debes subir una foto de tu finca"
                                lat == null || lng == null -> errorMsg = "Debes seleccionar la ubicacion en el mapa"
                                else -> tiendaVM.enviarVerificacion(cedula, nombreFinca, historiaFinca, lat, lng, municipio, fotoCedulaLocal, fotoFincaLocal) { ok -> if (ok) enviado = true }
                            }
                        }, Modifier.weight(1f).height(52.dp), enabled = !tiendaVM.verificacionEnviando, colors = ButtonDefaults.buttonColors(containerColor = Emerald), shape = RoundedCornerShape(14.dp)) {
                            if (tiendaVM.verificacionEnviando) CircularProgressIndicator(Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                            else Text("Enviar", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White)
                        }
                    }
                }
            }
        }
    }
    // MAPA OVERLAY - cubre toda la pantalla
    if (mostrarMapa) {
        Surface(Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.5f)).clickable { mostrarMapa = false }) {
            Box(Modifier.fillMaxSize()) {
                MapPickerScreen(
                    latitudInicial = lat ?: 4.5709,
                    longitudInicial = lng ?: -74.2973,
                    onLocationSelected = { loc ->
                        lat = loc.latitud; lng = loc.longitud
                        dirFinca = loc.direccion
                        municipio = loc.ciudad
                        mostrarMapa = false
                    },
                    onNavigateBack = { mostrarMapa = false }
                )
            }
        }
    }
    }
}

@Composable
private fun StepDot(num: Int, current: Int, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(Modifier.size(32.dp).clip(CircleShape).background(if (current >= num) Emerald else Slate200), contentAlignment = Alignment.Center) {
            if (current > num) Icon(Icons.Filled.Check, null, tint = Color.White, modifier = Modifier.size(16.dp))
            else Text("$num", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = if (current >= num) Color.White else Slate500)
        }
        Spacer(Modifier.height(4.dp))
        Text(label, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = if (current >= num) Emerald else Slate400)
    }
}

@Composable
private fun EstadoRevision(onLogout: () -> Unit) {
    Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color.White, AppBg)))) {
        Column(
            Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(56.dp))

            // Icono animado
            val infiniteTransition = rememberInfiniteTransition(label = "pulse")
            val scale by infiniteTransition.animateFloat(1f, 1.12f, infiniteRepeatable(tween(1800, easing = EaseInOutCubic), RepeatMode.Reverse), label = "s")
            val glow by infiniteTransition.animateFloat(0.4f, 0.8f, infiniteRepeatable(tween(1800, easing = EaseInOutCubic), RepeatMode.Reverse), label = "glow")

            Box(Modifier.size(100.dp), contentAlignment = Alignment.Center) {
                Surface(Modifier.size(100.dp).clip(CircleShape).background(Amber500.copy(alpha = glow * 0.1f)), shape = CircleShape) {}
                Surface(Modifier.size((72 * scale).dp).clip(CircleShape).background(Amber50), color = Amber50, shape = CircleShape, shadowElevation = (glow * 8).dp, tonalElevation = 4.dp) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Icon(Icons.Filled.HourglassTop, null, tint = Amber500, modifier = Modifier.size((32 * scale).dp))
                    }
                }
            }

            Spacer(Modifier.height(32.dp))

            // Titulo
            Text("Tus datos estan en revision", fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = Carbon, textAlign = TextAlign.Center, letterSpacing = (-0.5).sp)
            Spacer(Modifier.height(12.dp))
            Text("Un administrador revisara tu informacion. Este proceso suele tardar menos de 24 horas.", fontSize = 15.sp, color = Slate400, textAlign = TextAlign.Center, lineHeight = 22.sp, letterSpacing = 0.1.sp)

            Spacer(Modifier.height(28.dp))

            // Tarjeta de agradecimiento
            Surface(Modifier.fillMaxWidth(), color = Color.White, shape = RoundedCornerShape(20.dp), shadowElevation = 2.dp, border = BorderStroke(1.dp, Slate200)) {
                Column(Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(Modifier.size(48.dp).clip(CircleShape).background(Green50), contentAlignment = Alignment.Center) {
                        Icon(Icons.Filled.VolunteerActivism, null, tint = Green500, modifier = Modifier.size(24.dp))
                    }
                    Spacer(Modifier.height(14.dp))
                    Text("Gracias por confiar en nosotros", fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = Carbon, textAlign = TextAlign.Center)
                    Spacer(Modifier.height(8.dp))
                    Text("Tus datos estan seguros y seran revisados por nuestro equipo. Te notificaremos cuando tu cuenta este verificada para que puedas empezar a vender tus productos del campo.", fontSize = 13.sp, color = Slate500, textAlign = TextAlign.Center, lineHeight = 19.sp)
                }
            }

            Spacer(Modifier.height(40.dp))

            // Logo centrado
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Surface(
                    Modifier.size(64.dp).clip(RoundedCornerShape(18.dp)),
                    shape = RoundedCornerShape(18.dp),
                    shadowElevation = 8.dp,
                    tonalElevation = 4.dp
                ) {
                    Box(Modifier.fillMaxSize().background(Brush.linearGradient(listOf(Color(0xFF047857), Color(0xFF059669)))), contentAlignment = Alignment.Center) {
                        Icon(Icons.Filled.Spa, null, tint = Color.White, modifier = Modifier.size(32.dp))
                    }
                }
                Spacer(Modifier.height(10.dp))
                Text("AgroConecta", fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = Emerald, letterSpacing = (-0.5).sp)
                Text("Del campo a tu mesa", fontSize = 13.sp, color = Slate400, fontWeight = FontWeight.Medium)
            }

            Spacer(Modifier.height(32.dp))

            // Boton salir
            OutlinedButton(
                onClick = onLogout,
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Red500),
                border = BorderStroke(1.dp, Red500.copy(alpha = 0.25f))
            ) {
                Icon(Icons.Filled.Logout, null, tint = Red500, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(10.dp))
                Text("Cerrar sesion", fontSize = 15.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(Modifier.height(40.dp))
        }
    }
}

@Composable
private fun EstadoRechazado(onReintentar: () -> Unit) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(40.dp)) {
            Surface(Modifier.size(80.dp).clip(CircleShape).background(Red50), color = Red50) { Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Icon(Icons.Filled.Cancel, null, tint = Red500, modifier = Modifier.size(40.dp)) } }
            Spacer(Modifier.height(24.dp))
            Text("Verificacion Rechazada", fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = Carbon)
            Spacer(Modifier.height(8.dp))
            Text("Tus datos no fueron aprobados. Puedes corregir la informacion y volver a intentarlo.", fontSize = 14.sp, color = Slate400, textAlign = TextAlign.Center)
            Spacer(Modifier.height(24.dp))
            Button(onClick = onReintentar, colors = ButtonDefaults.buttonColors(containerColor = Emerald), shape = RoundedCornerShape(14.dp)) { Text("Volver a intentarlo", fontWeight = FontWeight.Bold, color = Color.White) }
        }
    }
}

@Composable
private fun EstadoVetado() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(40.dp)) {
            Surface(Modifier.size(80.dp).clip(CircleShape).background(Color(0xFFFEF2F2)), color = Color(0xFFFEF2F2)) { Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Icon(Icons.Filled.GppBad, null, tint = Red500, modifier = Modifier.size(40.dp)) } }
            Spacer(Modifier.height(24.dp))
            Text("Cuenta Suspendida", fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = Carbon)
            Spacer(Modifier.height(8.dp))
            Text("Tu cuenta ha sido suspendida por infringir nuestras politicas.\n\nContacta a soporte@agroconecta.com para mas informacion.", fontSize = 14.sp, color = Slate400, textAlign = TextAlign.Center, lineHeight = 20.sp)
        }
    }
}
