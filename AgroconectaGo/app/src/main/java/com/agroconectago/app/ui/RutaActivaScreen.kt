package com.agroconectago.app.ui

import android.graphics.Color as AndroidColor
import android.content.Intent
import android.net.Uri
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.agroconectago.app.data.LocationTracker
import com.agroconectago.app.data.api.DeliveryRetrofitClient
import com.agroconectago.app.ui.theme.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import org.json.JSONArray
import org.json.JSONObject
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline
import java.net.HttpURLConnection
import java.net.URL

@Composable
fun RutaActivaScreen(
    rutaId: Long,
    onBack: () -> Unit,
    onLlegue: () -> Unit,
    onNavigateToMapa: (Long) -> Unit = {},
    onEntregaExitosa: (Long, Double, Double, String, String) -> Unit = { _, _, _, _, _ -> }
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val tracker = remember { LocationTracker(context).also { it.start() } }

    var rutaData by remember { mutableStateOf<Map<String, Any?>>(emptyMap()) }
    var currentLat by remember { mutableStateOf<Double?>(null) }
    var currentLng by remember { mutableStateOf<Double?>(null) }
    var loading by remember { mutableStateOf(true) }
    var haLlegado by remember { mutableStateOf(false) }
    var entregando by remember { mutableStateOf(false) }
    var recogiendo by remember { mutableStateOf(false) }
    var entregandoAction by remember { mutableStateOf(false) }
    var pedidosExpandidos by remember { mutableStateOf(false) }
    // PIN
    var mostrarPinRecogida by remember { mutableStateOf(false) }
    var mostrarPinEntrega by remember { mutableStateOf(false) }
    var pinInput by remember { mutableStateOf("") }
    var pinError by remember { mutableStateOf("") }
    var validandoPin by remember { mutableStateOf(false) }

    val destinoLat = (rutaData["latitudOrigen"] as? Number)?.toDouble()
    val destinoLng = (rutaData["longitudOrigen"] as? Number)?.toDouble()
    val entregaLat = (rutaData["latitudDestino"] as? Number)?.toDouble()
    val entregaLng = (rutaData["longitudDestino"] as? Number)?.toDouble()
    val rutaDestLat: Double? = if (entregando) entregaLat else destinoLat
    val rutaDestLng: Double? = if (entregando) entregaLng else destinoLng

    // GPS tracking loop - cada 1 segundo
    LaunchedEffect(Unit) {
        while (true) {
            tracker.getCurrentLocation()?.let { loc ->
                currentLat = loc.first
                currentLng = loc.second
            }
            delay(1000)
        }
    }

    // Ruta: intenta backend proxy, si falla OSRM directo
    var routePoints by remember { mutableStateOf<List<GeoPoint>>(emptyList()) }
    var lastRouteFetch by remember { mutableStateOf(0L) }
    var distanciaKm by remember { mutableStateOf(0.0) }
    var duracionMin by remember { mutableStateOf(0.0) }

    suspend fun fetchOsrmRoute() {
        val clat = currentLat ?: return
        val clng = currentLng ?: return
        val dlat = rutaDestLat ?: return
        val dlng = rutaDestLng ?: return

        // Intento 1: Backend proxy (mismo que usa la pagina de logistica)
        try {
            val resp = DeliveryRetrofitClient.api.getOsrmRoute(lat1 = clat, lng1 = clng, lat2 = dlat, lng2 = dlng)
            val ok = resp["success"] as? Boolean ?: false
            if (ok) {
                @Suppress("UNCHECKED_CAST")
                val puntos = resp["points"] as? List<List<Any?>> ?: return
                if (puntos.size > 1) {
                    routePoints = puntos.mapNotNull { p ->
                        val lat = (p[0] as? Number)?.toDouble() ?: return@mapNotNull null
                        val lng = (p[1] as? Number)?.toDouble() ?: return@mapNotNull null
                        GeoPoint(lat, lng)
                    }
                    distanciaKm = (resp["distancia_km"] as? Number)?.toDouble() ?: 0.0
                    duracionMin = (resp["duracion_min"] as? Number)?.toDouble() ?: 0.0
                    lastRouteFetch = System.currentTimeMillis()
                    return
                }
            }
        } catch (_: Exception) {}

        // Intento 2: OSRM directo como fallback
        withContext(Dispatchers.IO) {
            try {
                val url = URL("https://router.project-osrm.org/route/v1/driving/$clng,$clat;$dlng,$dlat?overview=full&geometries=geojson")
                val conn = url.openConnection() as HttpURLConnection
                conn.setRequestProperty("User-Agent", "AgroConectaGo/2.0")
                conn.connectTimeout = 8000; conn.readTimeout = 8000
                if (conn.responseCode == 200) {
                    val text = conn.inputStream.bufferedReader().readText()
                    conn.disconnect()
                    val json = JSONObject(text)
                    val routes = json.optJSONArray("routes")
                    if (routes != null && routes.length() > 0) {
                        val geom = routes.getJSONObject(0).optJSONObject("geometry")
                        if (geom != null) {
                            val coords = geom.optJSONArray("coordinates")
                            if (coords != null && coords.length() > 1) {
                                val pts = mutableListOf<GeoPoint>()
                                for (i in 0 until coords.length()) {
                                    val pair = coords.getJSONArray(i)
                                    pts.add(GeoPoint(pair.getDouble(1), pair.getDouble(0)))
                                }
                                routePoints = pts
                                lastRouteFetch = System.currentTimeMillis()
                            }
                        }
                    }
                } else { conn.disconnect() }
            } catch (_: Exception) {}
        }
    }

    // Cargar datos de la ruta desde API y restaurar modo desde BD
    LaunchedEffect(rutaId) {
        try {
            val resp = DeliveryRetrofitClient.api.obtenerRuta(rutaId)
            rutaData = (resp["ruta"] as? Map<String, Any?>) ?: emptyMap()
            // Restaurar estado real desde BD
            val rutaEstado = rutaData["estado"]?.toString() ?: ""
            @Suppress("UNCHECKED_CAST")
            val pedidos = rutaData["pedidos"] as? List<Map<String, Any?>> ?: emptyList()
            if (rutaEstado == "EN_CAMINO" || rutaEstado == "ASIGNADA") {
                val primerPedido = pedidos.firstOrNull()
                val pedidoEstado = primerPedido?.get("estado")?.toString() ?: ""
                val tieneCodigoEntrega = (primerPedido?.get("codigoEntrega") as? String)?.isNotBlank() == true
                if (tieneCodigoEntrega) {
                    haLlegado = false
                    entregando = true
                } else if (pedidoEstado == "RECOGIDO") {
                    haLlegado = true
                }
            }
            if (rutaEstado == "COMPLETADA") {
                onLlegue(); return@LaunchedEffect
            }
        } catch (_: Exception) {}
        loading = false
        fetchOsrmRoute()
    }

    // Disparar OSRM cuando el GPS obtenga fix por primera vez
    LaunchedEffect(currentLat, currentLng) {
        if (currentLat != null && currentLng != null && routePoints.isEmpty()) {
            fetchOsrmRoute()
        }
    }

    LaunchedEffect(Unit) {
        fetchOsrmRoute()
        while (true) {
            delay(4000)
            if (System.currentTimeMillis() - lastRouteFetch > 10000) fetchOsrmRoute()
        }
    }

    fun abrirGoogleMaps() {
        val dlat = destinoLat ?: return
        val dlng = destinoLng ?: return
        val uri = "http://maps.google.com/maps?daddr=$dlat,$dlng"
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uri))
        intent.setPackage("com.google.android.apps.maps")
        context.startActivity(intent)
    }

    fun marcarLlegada() {
        mostrarPinRecogida = true
        pinInput = ""
        pinError = ""
        scope.launch {
            try {
                DeliveryRetrofitClient.api.iniciarViaje(rutaId)
                try {
                    DeliveryRetrofitClient.api.generarPinRecogida(rutaId, mapOf("accion" to "generar"))
                    try {
                        val r = DeliveryRetrofitClient.api.obtenerRuta(rutaId)
                        rutaData = (r["ruta"] as? Map<String, Any?>) ?: emptyMap()
                    } catch (_: Exception) {}
                } catch (_: Exception) {}
            } catch (_: Exception) {}
        }
    }

    fun validarPinRecogida() {
        scope.launch {
            validandoPin = true
            pinError = ""
            try {
                val resp = DeliveryRetrofitClient.api.validarPinRecogida(rutaId, mapOf("pin" to pinInput))
                val ok = resp["success"] as? Boolean ?: false
                if (ok) {
                    mostrarPinRecogida = false
                    haLlegado = true
                } else {
                    pinError = resp["message"]?.toString() ?: "PIN incorrecto"
                    val intentos = resp["intentosRestantes"] as? Int
                    if (intentos != null && intentos <= 0) {
                        mostrarPinRecogida = false
                    }
                }
            } catch (_: Exception) {
                pinError = "Error de conexion"
            }
            validandoPin = false
        }
    }

    fun recogerPedido() {
        scope.launch {
            recogiendo = true
            try {
                val resp = withTimeout(15000) {
                    DeliveryRetrofitClient.api.recogerPedido(rutaId)
                }
                val ok = resp["success"] as? Boolean ?: false
                if (ok) {
                    // Generar PIN de entrega para el cliente
                    try {
                        DeliveryRetrofitClient.api.generarPinEntrega(rutaId, mapOf("accion" to "generar"))
                    } catch (_: Exception) {}
                    try {
                        val r = DeliveryRetrofitClient.api.obtenerRuta(rutaId)
                        rutaData = (r["ruta"] as? Map<String, Any?>) ?: emptyMap()
                    } catch (_: Exception) {}
                    haLlegado = false
                    entregando = true
                } else {
                    val msg = resp["message"]?.toString() ?: ""
                    android.widget.Toast.makeText(context, "Error: ${msg.ifBlank { "No se pudo recoger el pedido" }}", android.widget.Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                android.widget.Toast.makeText(context, "Error: ${e.message ?: "Sin conexion"}", android.widget.Toast.LENGTH_LONG).show()
            }
            recogiendo = false
        }
    }

    fun marcarEntrega() {
        mostrarPinEntrega = true
        pinInput = ""
        pinError = ""
    }

    fun validarPinEntrega() {
        scope.launch {
            validandoPin = true
            pinError = ""
            try {
                val resp = DeliveryRetrofitClient.api.validarPinEntrega(rutaId, mapOf("pin" to pinInput))
                val ok = resp["success"] as? Boolean ?: false
                if (ok) {
                    mostrarPinEntrega = false
                    val pago = (rutaData["pagoTotalEstimado"] as? Number)?.toDouble() ?: 0.0
                    val finca = rutaData["nombreFinca"]?.toString() ?: ""
                    val rutaCod = rutaData["codigoRuta"]?.toString() ?: ""
                    onEntregaExitosa(rutaId, distanciaKm, pago, finca, rutaCod)
                } else {
                    pinError = resp["message"]?.toString() ?: "PIN incorrecto"
                    val intentos = resp["intentosRestantes"] as? Int
                    if (intentos != null && intentos <= 0) {
                        mostrarPinEntrega = false
                    }
                }
            } catch (_: Exception) {
                pinError = "Error de conexion"
            }
            validandoPin = false
        }
    }

    @Suppress("UNCHECKED_CAST")
    val pedidos = (rutaData["pedidos"] as? List<Map<String, Any?>>) ?: emptyList()

    Box(Modifier.fillMaxSize().background(DeliverySurface)) {
        if (loading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = DeliveryBrand600) }
        } else if (haLlegado) {
            // ===== PANTALLA RETIRAR PEDIDO =====
            Column(Modifier.fillMaxSize().statusBarsPadding()) {
                // Header
                Row(Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Slate400) }
                    Text("Retirar Pedido", fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, color = Slate900, modifier = Modifier.weight(1f))
                    Box(Modifier.clip(RoundedCornerShape(8.dp)).background(Color(0xFF6366F1)).padding(horizontal = 10.dp, vertical = 4.dp)) {
                        Text("EN FINCA", fontWeight = FontWeight.ExtraBold, fontSize = 10.sp, color = Color.White)
                    }
                }

                Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 14.dp)) {
                    // Finca info card
                    Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFFF0FDF4)), border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFBBF7D0))) {
                        Column(Modifier.padding(14.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(Modifier.size(44.dp).clip(RoundedCornerShape(12.dp)).background(Color(0xFF16A34A)), contentAlignment = Alignment.Center) {
                                    Icon(Icons.Filled.LocationOn, null, tint = Color.White, modifier = Modifier.size(24.dp))
                                }
                                Spacer(Modifier.width(12.dp))
                                Column {
                                    Text("Finca ${rutaData["nombreFinca"]?.toString() ?: ""}", fontWeight = FontWeight.ExtraBold, fontSize = 17.sp, color = Slate900)
                                    val mun = rutaData["municipioFinca"]?.toString() ?: ""
                                    if (mun.isNotBlank()) Text(mun, fontSize = 12.sp, color = Slate500)
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(12.dp))

                    // Pedidos - uno por cada campesino
                    pedidos.forEachIndexed { idx, pedido ->
                        val campesino = pedido["campesinoNombre"]?.toString() ?: "Campesino"
                        val finca = pedido["campesinoFinca"]?.toString() ?: ""
                        val detalles = pedido["detalles"] as? List<Map<String, Any?>> ?: emptyList()

                        Card(
                            Modifier.fillMaxWidth().padding(bottom = 8.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(Modifier.padding(14.dp)) {
                                // Header del pedido
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(Modifier.size(40.dp).clip(RoundedCornerShape(10.dp)).background(Color(0xFFEEF2FF)), contentAlignment = Alignment.Center) {
                                        Text("#${idx + 1}", fontWeight = FontWeight.ExtraBold, fontSize = 14.sp, color = Color(0xFF6366F1))
                                    }
                                    Spacer(Modifier.width(10.dp))
                                    Column(Modifier.weight(1f)) {
                                        Text("Pedido #${pedido["numeroOrden"]?.toString()?.takeLast(6) ?: ""}", fontWeight = FontWeight.ExtraBold, fontSize = 14.sp, color = Slate900)
                                        Text("${campesino}${if (finca.isNotBlank()) " · $finca" else ""}", fontSize = 12.sp, color = Slate500)
                                    }
                                }

                                Spacer(Modifier.height(8.dp))

                                // Boton expandir/colapsar productos
                                val estaExpandido = pedidosExpandidos
                                Surface(
                                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).clickable { pedidosExpandidos = !pedidosExpandidos },
                                    color = Color(0xFFF8FAFC),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Row(Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            if (estaExpandido) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                                            null, tint = Slate400, modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(Modifier.width(6.dp))
                                        Text(
                                            if (estaExpandido) "Ocultar detalle" else "${detalles.size} productos a retirar",
                                            fontSize = 12.sp, fontWeight = FontWeight.Medium, color = Slate500
                                        )
                                        Spacer(Modifier.weight(1f))
                                        Text("Ver", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = DeliveryBrand600)
                                    }
                                }

                                // Detalle expandido
                                if (estaExpandido) {
                                    Spacer(Modifier.height(8.dp))
                                    Surface(Modifier.fillMaxWidth(), color = Color(0xFFF1F5F9), shape = RoundedCornerShape(10.dp)) {
                                        Column(Modifier.padding(10.dp)) {
                                            detalles.forEach { det ->
                                                val nombre = det["nombre"]?.toString() ?: ""
                                                val cantidad = (det["cantidad"] as? Number)?.toInt() ?: 0
                                                val unidad = det["unidad"]?.toString() ?: ""
                                                Row(Modifier.fillMaxWidth().padding(vertical = 3.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                                    Text("$cantidad $unidad $nombre", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = Slate700, modifier = Modifier.weight(1f))
                                                    Icon(Icons.Filled.CheckCircleOutline, null, tint = Color(0xFF16A34A), modifier = Modifier.size(16.dp))
                                                }
                                                if (det != detalles.last()) {
                                                    HorizontalDivider(Modifier.padding(vertical = 1.dp), color = Color(0xFFE2E8F0))
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(12.dp))

                    // Resumen
                    Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF7ED)), border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFED7AA))) {
                        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Info, null, tint = Color(0xFFEA580C), modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(8.dp))
                            Column(Modifier.weight(1f)) {
                                Text("Total a retirar", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFFEA580C))
                                Text("${rutaData["pedidosCount"]?.toString() ?: "0"} pedidos · ${rutaData["pesoTotalKg"]?.toString() ?: "0"} kg", fontSize = 13.sp, fontWeight = FontWeight.ExtraBold, color = Slate900)
                            }
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    // Boton Tengo el pedido
                    val interactionSource = remember { MutableInteractionSource() }
                    val isPressed by interactionSource.collectIsPressedAsState()
                    val scale by animateFloatAsState(if (isPressed) 0.97f else 1f, spring(dampingRatio = Spring.DampingRatioMediumBouncy))

                    Button(
                        onClick = { recogerPedido() },
                        enabled = !recogiendo,
                        interactionSource = interactionSource,
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A)),
                        contentPadding = PaddingValues(vertical = 16.dp),
                        modifier = Modifier.fillMaxWidth().graphicsLayer { scaleX = scale; scaleY = scale }.shadow(12.dp, RoundedCornerShape(16.dp), spotColor = Color(0xFF16A34A).copy(alpha = 0.3f)).padding(bottom = 20.dp)
                    ) {
                        if (recogiendo) {
                            CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp, color = Color.White)
                            Spacer(Modifier.width(10.dp))
                            Text("Procesando...", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, color = Color.White)
                        } else {
                            Icon(Icons.Filled.ShoppingCart, null, tint = Color.White, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Tengo el pedido", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, color = Color.White)
                        }
                    }
                }
            }
        } else if (entregando) {
            // ===== PANTALLA ENTREGAR AL CLIENTE =====
            val clienteNombre = rutaData["clienteNombre"]?.toString() ?: "Cliente"
            val clienteDir = rutaData["clienteDireccion"]?.toString() ?: rutaData["zonaDestino"]?.toString() ?: ""
            val zonaDest = rutaData["zonaDestino"]?.toString() ?: ""

            Column(Modifier.fillMaxSize().statusBarsPadding()) {
                Row(Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Slate400) }
                    Text("Entregar Pedido", fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, color = Slate900, modifier = Modifier.weight(1f))
                    Box(Modifier.clip(RoundedCornerShape(8.dp)).background(Color(0xFF2563EB)).padding(horizontal = 10.dp, vertical = 4.dp)) {
                        Text("EN RUTA", fontWeight = FontWeight.ExtraBold, fontSize = 10.sp, color = Color.White)
                    }
                }

                Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 14.dp)) {
                    // Tarjeta cliente
                    Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFFEFF6FF)), border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFBFDBFE))) {
                        Column(Modifier.padding(14.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(Modifier.size(44.dp).clip(RoundedCornerShape(12.dp)).background(Color(0xFF2563EB)), contentAlignment = Alignment.Center) {
                                    Icon(Icons.Filled.Person, null, tint = Color.White, modifier = Modifier.size(24.dp))
                                }
                                Spacer(Modifier.width(12.dp))
                                Column {
                                    Text("Entregar a:", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2563EB))
                                    Text(clienteNombre, fontWeight = FontWeight.ExtraBold, fontSize = 17.sp, color = Slate900)
                                }
                            }
                            if (clienteDir.isNotBlank()) {
                                Spacer(Modifier.height(8.dp))
                                Row(verticalAlignment = Alignment.Top) {
                                    Icon(Icons.Filled.LocationOn, null, tint = Color(0xFF2563EB), modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(6.dp))
                                    Column {
                                        Text(clienteDir, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = Slate700)
                                        if (zonaDest.isNotBlank()) Text(zonaDest, fontSize = 12.sp, color = Slate500)
                                    }
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(10.dp))

                    // Resumen pedido
                    Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFFF0FDF4)), border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFBBF7D0))) {
                        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.CheckCircle, null, tint = Color(0xFF16A34A), modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(8.dp))
                            Column(Modifier.weight(1f)) {
                                Text("Pedido recogido", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF16A34A))
                                Text("${rutaData["pedidosCount"]?.toString() ?: "0"} pedidos · ${rutaData["pesoTotalKg"]?.toString() ?: "0"} kg listos para entregar", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = Slate700)
                            }
                        }
                    }

                    Spacer(Modifier.height(10.dp))

                    // Boton Como llegar
                    OutlinedButton(
                        onClick = {
                            val elat = entregaLat
                            val elng = entregaLng
                            if (elat != null && elng != null) {
                                val uri = "http://maps.google.com/maps?daddr=$elat,$elng"
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uri))
                                intent.setPackage("com.google.android.apps.maps")
                                context.startActivity(intent)
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF2563EB))
                    ) { Icon(Icons.Filled.Map, null, Modifier.size(18.dp)); Spacer(Modifier.width(6.dp)); Text("Como llegar con Google Maps", fontWeight = FontWeight.Bold, fontSize = 14.sp) }

                    Spacer(Modifier.height(8.dp))

                    // Mapa hacia el cliente
                    Card(Modifier.fillMaxWidth().height(220.dp), shape = RoundedCornerShape(14.dp), elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)) {
                        var mapaEntrega by remember { mutableStateOf<MapView?>(null) }
                        var polyEntrega by remember { mutableStateOf<Polyline?>(null) }

                        // Actualizar polyline reactivamente cuando llega la ruta OSRM
                        LaunchedEffect(routePoints) {
                            if (routePoints.size > 1) {
                                polyEntrega?.let { poly ->
                                    val cLat = currentLat; val cLng = currentLng
                                    val eLat = entregaLat; val eLng = entregaLng
                                    if (cLat != null && cLng != null && eLat != null && eLng != null) {
                                        val pts = mutableListOf(GeoPoint(cLat, cLng))
                                        pts.addAll(routePoints)
                                        pts.add(GeoPoint(eLat, eLng))
                                        poly.setPoints(pts)
                                    }
                                }
                            }
                        }

                        if (currentLat == null || currentLng == null || entregaLat == null || entregaLng == null) {
                            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text("Esperando coordenadas...", fontSize = 13.sp, color = Slate400)
                            }
                        } else {
                            AndroidView(
                                factory = { ctx ->
                                    val cLat = currentLat ?: 0.0
                                    val cLng = currentLng ?: 0.0
                                    val eLat = entregaLat ?: 0.0
                                    val eLng = entregaLng ?: 0.0

                                    Configuration.getInstance().apply {
                                        userAgentValue = ctx.packageName
                                        osmdroidBasePath = ctx.getExternalFilesDir(null)
                                        osmdroidTileCache = ctx.getExternalFilesDir("tiles")
                                    }
                                    MapView(ctx).apply {
                                        setTileSource(TileSourceFactory.MAPNIK)
                                        setMultiTouchControls(true)
                                        controller.setZoom(14.0)
                                        controller.setCenter(GeoPoint(cLat, cLng))

                                        val mOrigen = Marker(this)
                                        mOrigen.position = GeoPoint(cLat, cLng)
                                        mOrigen.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
                                        mOrigen.title = "Tu ubicacion"
                                        mOrigen.icon = ctx.getDrawable(android.R.drawable.ic_menu_mylocation)
                                        overlays.add(mOrigen)

                                        val mDest = Marker(this)
                                        mDest.position = GeoPoint(eLat, eLng)
                                        mDest.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                                        mDest.title = clienteNombre
                                        overlays.add(mDest)

                                        val poly = Polyline()
                                        poly.outlinePaint.color = AndroidColor.rgb(37, 99, 235)
                                        poly.outlinePaint.strokeWidth = 10f
                                        poly.outlinePaint.alpha = 200
                                        val ptsInit = mutableListOf(GeoPoint(cLat, cLng))
                                        if (routePoints.size > 1) ptsInit.addAll(routePoints)
                                        ptsInit.add(GeoPoint(eLat, eLng))
                                        poly.setPoints(ptsInit)
                                        overlays.add(poly)
                                        polyEntrega = poly

                                        invalidate()
                                        mapaEntrega = this
                                    }
                                },
                                modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(14.dp)),
                                update = { mv ->
                                    val cLat = currentLat
                                    val cLng = currentLng
                                    val eLat = entregaLat
                                    val eLng = entregaLng
                                    if (cLat != null && cLng != null && eLat != null && eLng != null) {
                                        polyEntrega?.let { poly ->
                                            val pts = mutableListOf(GeoPoint(cLat, cLng))
                                            if (routePoints.size > 1) pts.addAll(routePoints)
                                            pts.add(GeoPoint(eLat, eLng))
                                            poly.setPoints(pts)
                                        }
                                        mv.controller.animateTo(GeoPoint(cLat, cLng), mv.zoomLevelDouble, 800L)
                                        mv.invalidate()
                                    }
                                }
                            )
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    // Boton Llegue al destino
                    val interactionSource = remember { MutableInteractionSource() }
                    val isPressed by interactionSource.collectIsPressedAsState()
                    val scale by animateFloatAsState(if (isPressed) 0.97f else 1f, spring(dampingRatio = Spring.DampingRatioMediumBouncy))

                    Button(
                        onClick = { marcarEntrega() },
                        enabled = !entregandoAction,
                        interactionSource = interactionSource,
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                        contentPadding = PaddingValues(vertical = 16.dp),
                        modifier = Modifier.fillMaxWidth().graphicsLayer { scaleX = scale; scaleY = scale }.shadow(12.dp, RoundedCornerShape(16.dp), spotColor = Color(0xFF2563EB).copy(alpha = 0.3f)).padding(bottom = 20.dp)
                    ) {
                        if (entregandoAction) {
                            CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp, color = Color.White)
                            Spacer(Modifier.width(10.dp))
                            Text("Procesando...", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, color = Color.White)
                        } else {
                            Icon(Icons.Filled.Flag, null, tint = Color.White, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Llegue al destino", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, color = Color.White)
                        }
                    }
                }
            }
        } else {
            // ===== PANTALLA NORMAL CON MAPA =====
            Scaffold(
                modifier = Modifier.fillMaxSize().statusBarsPadding(),
                containerColor = Color.White,
                bottomBar = {
                    Column {
                        // Indicador GPS
                        if (!haLlegado && !loading) {
                            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                                Text("GPS en tiempo real · Actualiza cada 1s", fontSize = 9.sp, color = Slate400, modifier = Modifier.background(DeliverySurface).padding(horizontal = 8.dp, vertical = 2.dp))
                            }
                        }
                        // Boton fijo inferior
                        Button(
                            onClick = { marcarLlegada() },
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 10.dp).height(52.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = DeliveryBrand600)
                        ) {
                            Icon(Icons.Filled.Check, null, Modifier.size(20.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Llegue a la finca", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, color = Color.White)
                        }
                    }
                }
            ) { padding ->
                Column(Modifier.fillMaxSize().padding(padding)) {
                    // Header
                    Row(Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Slate400) }
                        Text("Ruta Activa", fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, color = Slate900, modifier = Modifier.weight(1f))
                        Box(Modifier.clip(RoundedCornerShape(8.dp)).background(Color(0xFF22C55E)).padding(horizontal = 10.dp, vertical = 4.dp)) {
                            Text("EN VIVO", fontWeight = FontWeight.ExtraBold, fontSize = 10.sp, color = Color.White)
                        }
                    }

                    if (loading) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = DeliveryBrand600) }
                    } else {
                        // Info card
                        Card(Modifier.fillMaxWidth().padding(horizontal = 14.dp), shape = RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)) {
                            Column(Modifier.padding(14.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(Modifier.size(36.dp).clip(RoundedCornerShape(10.dp)).background(DeliveryBrand600), contentAlignment = Alignment.Center) {
                                        Text(rutaData["codigoRuta"]?.toString()?.takeLast(3) ?: "---", fontWeight = FontWeight.ExtraBold, fontSize = 11.sp, color = Color.White)
                                    }
                                    Spacer(Modifier.width(10.dp))
                                    Column(Modifier.weight(1f)) {
                                        Text(rutaData["codigoRuta"]?.toString() ?: "Ruta", fontWeight = FontWeight.ExtraBold, fontSize = 15.sp, color = Slate900)
                                        Text("Dirigete a recoger los productos", fontSize = 11.sp, color = Slate400)
                                    }
                                }
                                Spacer(Modifier.height(10.dp))

                                // RECOGER EN
                                Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFFF0FDF4)), border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFBBF7D0))) {
                                    Column(Modifier.padding(10.dp)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Filled.LocationOn, null, tint = Color(0xFF16A34A), modifier = Modifier.size(16.dp))
                                            Spacer(Modifier.width(6.dp))
                                            Text("Recoger en:", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF16A34A))
                                        }
                                        Spacer(Modifier.height(2.dp))
                                        Text("Finca ${rutaData["nombreFinca"]?.toString() ?: rutaData["zonaOrigen"]?.toString() ?: ""}", fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = Slate900)
                                        val dirFinca = rutaData["direccionFinca"]?.toString() ?: ""
                                        if (dirFinca.isNotBlank()) { Text(dirFinca, fontSize = 11.sp, color = Slate500, maxLines = 1) }
                                        val munFinca = rutaData["municipioFinca"]?.toString() ?: ""
                                        if (munFinca.isNotBlank()) { Text(munFinca, fontSize = 11.sp, fontWeight = FontWeight.Medium, color = Slate500) }
                                    }
                                }

                                Spacer(Modifier.height(8.dp))
                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("${rutaData["pedidosCount"]?.toString() ?: "0"} pedidos · ${rutaData["pesoTotalKg"]?.toString() ?: "0"} kg", fontSize = 10.sp, color = Slate500)
                                    val pago = (rutaData["pagoTotalEstimado"] as? Number)?.toDouble() ?: 0.0
                                    Text("$${String.format("%.0f", pago)}", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = DeliveryBrand600)
                                }

                                Spacer(Modifier.height(8.dp))
                                Button(
                                    onClick = { onNavigateToMapa(rutaId) },
                                    modifier = Modifier.fillMaxWidth().height(44.dp),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = DeliveryBrand600)
                                ) {
                                    Icon(Icons.Filled.Map, null, Modifier.size(16.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text("Como llegar", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                }
                            }
                        }

                        Spacer(Modifier.height(8.dp))

                        // Mapa preview estatico
                        Card(Modifier.fillMaxWidth().weight(1f).padding(horizontal = 14.dp), shape = RoundedCornerShape(14.dp), elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)) {
                            var mapView by remember { mutableStateOf<MapView?>(null) }
                            var markerOrigen by remember { mutableStateOf<Marker?>(null) }
                            var markerDestino by remember { mutableStateOf<Marker?>(null) }
                            var routePolyline by remember { mutableStateOf<Polyline?>(null) }

                            LaunchedEffect(routePoints) {
                                if (routePoints.size > 1) {
                                    routePolyline?.let { poly ->
                                        val cLat = currentLat; val cLng = currentLng
                                        val dLat = destinoLat; val dLng = destinoLng
                                        if (cLat != null && cLng != null && dLat != null && dLng != null) {
                                            val pts = mutableListOf(GeoPoint(cLat, cLng))
                                            pts.addAll(routePoints)
                                            pts.add(GeoPoint(dLat, dLng))
                                            poly.setPoints(pts)
                                        }
                                    }
                                }
                            }

                            if (currentLat == null || currentLng == null || destinoLat == null || destinoLng == null) {
                                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Icon(Icons.Filled.Map, contentDescription = null, tint = Slate300, modifier = Modifier.size(48.dp))
                                        Spacer(Modifier.height(8.dp))
                                        Text("Esperando ubicacion...", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Slate400)
                                    }
                                }
                            } else {
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
                                            setBuiltInZoomControls(false)
                                            val fLat = destinoLat ?: 0.0; val fLng = destinoLng ?: 0.0
                                            val cLat = currentLat ?: 0.0; val cLng = currentLng ?: 0.0
                                            val fincaGeo = GeoPoint(fLat, fLng)
                                            controller.setZoom(14.0); controller.setCenter(fincaGeo)

                                            val mOrigen = Marker(this)
                                            mOrigen.position = GeoPoint(cLat, cLng)
                                            mOrigen.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
                                            mOrigen.title = "Tu ubicacion"
                                            mOrigen.icon = ctx.resources.getDrawable(android.R.drawable.ic_menu_mylocation)
                                            overlays.add(mOrigen); markerOrigen = mOrigen

                                            val mDestino = Marker(this)
                                            mDestino.position = fincaGeo
                                            mDestino.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                                            mDestino.title = rutaData["nombreFinca"]?.toString() ?: "Finca"
                                            mDestino.snippet = rutaData["municipioFinca"]?.toString() ?: ""
                                            mDestino.icon = ctx.resources.getDrawable(android.R.drawable.ic_menu_compass)
                                            overlays.add(mDestino); markerDestino = mDestino

                                            val poly = Polyline()
                                            poly.outlinePaint.color = AndroidColor.rgb(22, 163, 74)
                                            poly.outlinePaint.strokeWidth = 10f; poly.outlinePaint.alpha = 200
                                            val ptsInit = mutableListOf(GeoPoint(cLat, cLng))
                                            if (routePoints.size > 1) ptsInit.addAll(routePoints)
                                            ptsInit.add(fincaGeo); poly.setPoints(ptsInit)
                                            overlays.add(poly); routePolyline = poly
                                            invalidate(); mapView = this
                                        }
                                    },
                                    modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(14.dp)),
                                    update = { mv ->
                                        val cLat = currentLat; val cLng = currentLng
                                        val dLat = destinoLat; val dLng = destinoLng
                                        if (cLat != null && cLng != null && dLat != null && dLng != null) {
                                            val nuevoCentro = GeoPoint(cLat, cLng)
                                            val fincaGeo = GeoPoint(dLat, dLng)
                                            markerOrigen?.position = nuevoCentro
                                            markerDestino?.position = fincaGeo
                                            markerDestino?.title = rutaData["nombreFinca"]?.toString() ?: "Finca"
                                            markerDestino?.snippet = rutaData["municipioFinca"]?.toString() ?: ""
                                            routePolyline?.let { poly ->
                                                val pts = mutableListOf(GeoPoint(nuevoCentro))
                                                if (routePoints.size > 1) pts.addAll(routePoints)
                                                pts.add(fincaGeo); poly.setPoints(pts)
                                            }
                                            mv.controller.animateTo(nuevoCentro, mv.zoomLevelDouble.coerceIn(14.0, 18.0), 800L)
                                            mv.invalidate()
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }

        // Dialogo PIN Recogida
        if (mostrarPinRecogida) {
            AlertDialog(
                onDismissRequest = { if (!validandoPin) mostrarPinRecogida = false },
                title = { Text("Verificacion de Recogida", fontWeight = FontWeight.ExtraBold, color = Slate900) },
                text = {
                    Column {
                        Text("Pide al campesino el PIN de recogida de 4 a 6 digitos.", fontSize = 14.sp, color = Slate500)
                        Spacer(Modifier.height(12.dp))
                        OutlinedTextField(
                            value = pinInput,
                            onValueChange = { v -> if (v.all { it.isDigit() } && v.length <= 6) pinInput = v },
                            label = { Text("PIN de recogida") },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
                            isError = pinError.isNotBlank(),
                            supportingText = if (pinError.isNotBlank()) {{ Text(pinError, color = Color(0xFFEF4444)) }} else null,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = { validarPinRecogida() },
                        enabled = pinInput.length >= 4 && !validandoPin,
                        colors = ButtonDefaults.buttonColors(containerColor = DeliveryBrand600)
                    ) {
                        if (validandoPin) CircularProgressIndicator(Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp)
                        else Text("Validar", fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = { TextButton(onClick = { if (!validandoPin) mostrarPinRecogida = false }) { Text("Cancelar") } },
                shape = RoundedCornerShape(20.dp),
                containerColor = Color.White
            )
        }

        // Dialogo PIN Entrega
        if (mostrarPinEntrega) {
            AlertDialog(
                onDismissRequest = { if (!validandoPin) mostrarPinEntrega = false },
                title = { Text("Verificacion de Entrega", fontWeight = FontWeight.ExtraBold, color = Slate900) },
                text = {
                    Column {
                        Text("Pide al cliente el PIN de entrega de 4 a 6 digitos.", fontSize = 14.sp, color = Slate500)
                        Spacer(Modifier.height(12.dp))
                        OutlinedTextField(
                            value = pinInput,
                            onValueChange = { v -> if (v.all { it.isDigit() } && v.length <= 6) pinInput = v },
                            label = { Text("PIN de entrega") },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
                            isError = pinError.isNotBlank(),
                            supportingText = if (pinError.isNotBlank()) {{ Text(pinError, color = Color(0xFFEF4444)) }} else null,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = { validarPinEntrega() },
                        enabled = pinInput.length >= 4 && !validandoPin,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB))
                    ) {
                        if (validandoPin) CircularProgressIndicator(Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp)
                        else Text("Validar", fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = { TextButton(onClick = { if (!validandoPin) mostrarPinEntrega = false }) { Text("Cancelar") } },
                shape = RoundedCornerShape(20.dp),
                containerColor = Color.White
            )
        }
    }
}