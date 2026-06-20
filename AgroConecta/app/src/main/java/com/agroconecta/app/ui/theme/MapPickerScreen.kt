package com.agroconecta.app.ui.theme

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.events.MapListener
import org.osmdroid.events.ScrollEvent
import org.osmdroid.events.ZoomEvent
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import org.json.JSONArray

private val Brand600 = Color(0xFF16A34A)
private val Slate400 = Color(0xFF94A3B8)
private val DarkSlate = Color(0xFF0F172A)

data class PickedLocation(
    val latitud: Double,
    val longitud: Double,
    val direccion: String,
    val ciudad: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapPickerScreen(
    latitudInicial: Double = 4.5709,
    longitudInicial: Double = -74.2973,
    onLocationSelected: (PickedLocation) -> Unit,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    var currentAddress by remember { mutableStateOf("Selecciona un punto en el mapa...") }
    var currentCity by remember { mutableStateOf("") }
    var currentLat by remember { mutableStateOf(latitudInicial) }
    var currentLng by remember { mutableStateOf(longitudInicial) }
    var isLocating by remember { mutableStateOf(false) }
    var hasPermissions by remember { mutableStateOf(false) }
    var mapViewRef by remember { mutableStateOf<MapView?>(null) }
    var lastGeocodeTime by remember { mutableStateOf(0L) }
    val scope = rememberCoroutineScope()

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { grants ->
        hasPermissions = grants.values.all { it }
    }

    fun checkAndRequestPermissions() {
        val fine = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
        val coarse = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)
        if (fine == PackageManager.PERMISSION_GRANTED || coarse == PackageManager.PERMISSION_GRANTED) {
            hasPermissions = true
        } else {
            permissionLauncher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
        }
    }

    // Check permissions on screen open
    LaunchedEffect(Unit) {
        Configuration.getInstance().userAgentValue = context.packageName
        Configuration.getInstance().osmdroidBasePath = context.getExternalFilesDir(null)
        Configuration.getInstance().osmdroidTileCache = context.getExternalFilesDir("tiles")
        checkAndRequestPermissions()
        reverseGeocode(latitudInicial, longitudInicial) { addr, city ->
            currentAddress = addr; currentCity = city
        }
    }

    // Center marker overlay composable at screen center
    Box(modifier = Modifier.fillMaxSize()) {
        // Map fills the entire screen
        AndroidView(
            factory = { ctx ->
                MapView(ctx).apply {
                    setTileSource(TileSourceFactory.MAPNIK)
                    setMultiTouchControls(true)
                    controller.setZoom(16.0)
                    controller.setCenter(GeoPoint(latitudInicial, longitudInicial))
                    mapViewRef = this

                    // Update address when map is dragged
                    addMapListener(object : MapListener {
                        override fun onScroll(event: ScrollEvent?): Boolean {
                            val now = System.currentTimeMillis()
                            if (now - lastGeocodeTime > 1500) {
                                lastGeocodeTime = now
                                val center = mapCenter
                                currentLat = center.latitude; currentLng = center.longitude
                                scope.launch {
                                    reverseGeocode(currentLat, currentLng) { addr, city ->
                                        currentAddress = addr; currentCity = city
                                    }
                                }
                            }
                            return true
                        }
                        override fun onZoom(event: ZoomEvent?): Boolean = true
                    })

                    overlays.clear()
                    invalidate()
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        // ===== CROSSHAIR PIN OVERLAY (Center of screen, always visible) =====
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            // Subtle ring + center dot crosshair
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.offset(y = (-12).dp) // Offset up to account for pin bottom
            ) {
                // Pin icon
                Canvas(modifier = Modifier.size(40.dp)) {
                    // Outer glow ring
                    drawCircle(
                        color = Brand600.copy(alpha = 0.25f),
                        radius = size.minDimension / 2f,
                        center = Offset(size.width / 2f, size.height / 2f)
                    )
                    drawCircle(
                        color = Brand600.copy(alpha = 0.4f),
                        radius = size.minDimension / 3f,
                        center = Offset(size.width / 2f, size.height / 2f),
                        style = Stroke(width = 3f, cap = StrokeCap.Round)
                    )
                    // Center dot
                    drawCircle(
                        color = Brand600,
                        radius = 6f,
                        center = Offset(size.width / 2f, size.height / 2f)
                    )
                }
            }
        }

        // ===== TOP BAR WITH SEARCH =====
        var searchQuery by remember { mutableStateOf("") }
        var searchResults by remember { mutableStateOf<List<Pair<String, Pair<Double, Double>>>>(emptyList()) }
        var isSearching by remember { mutableStateOf(false) }

        Surface(
            shadowElevation = 8.dp,
            color = Color.White,
            modifier = Modifier.align(Alignment.TopCenter).fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.statusBarsPadding().padding(horizontal = 8.dp, vertical = 6.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver", tint = DarkSlate)
                    }
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { q ->
                            searchQuery = q
                            if (q.length >= 3) {
                                isSearching = true
                                scope.launch {
                                    searchResults = searchCity(q)
                                    isSearching = false
                                }
                            } else searchResults = emptyList()
                        },
                        placeholder = { Text("Buscar municipio, ciudad...", fontSize = 14.sp, color = Slate400) },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.weight(1f).height(50.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Brand600, unfocusedBorderColor = Slate400.copy(alpha = 0.3f),
                            cursorColor = Brand600, focusedTextColor = DarkSlate, unfocusedTextColor = DarkSlate,
                            focusedContainerColor = Color.White, unfocusedContainerColor = Color.White
                        ),
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = ""; searchResults = emptyList() }) {
                                    Icon(Icons.Filled.Close, null, tint = Slate400, modifier = Modifier.size(18.dp))
                                }
                            } else {
                                Icon(Icons.Filled.Search, null, tint = Slate400, modifier = Modifier.size(20.dp))
                            }
                        }
                    )
                }

                // Search results dropdown
                if (searchResults.isNotEmpty()) {
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth().heightIn(max = 200.dp)
                    ) {
                        items(searchResults) { (name, coords) ->
                            Surface(
                                color = Color.White,
                                modifier = Modifier.fillMaxWidth().clickable {
                                    val (lat, lng) = coords
                                    currentLat = lat; currentLng = lng
                                    lastGeocodeTime = System.currentTimeMillis()
                                    mapViewRef?.controller?.animateTo(GeoPoint(lat, lng), 14.0, 1000L)
                                    scope.launch {
                                        delay(1200)
                                        reverseGeocode(lat, lng) { addr, city ->
                                            currentAddress = addr; currentCity = city
                                        }
                                    }
                                    searchQuery = name
                                    searchResults = emptyList()
                                }
                            ) {
                                Row(modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Filled.LocationOn, null, tint = Brand600, modifier = Modifier.size(18.dp))
                                    Spacer(Modifier.width(8.dp))
                                    Text(name, fontSize = 13.sp, color = DarkSlate)
                                }
                            }
                        }
                    }
                }

                if (isSearching) {
                    LinearProgressIndicator(
                        modifier = Modifier.fillMaxWidth().height(2.dp),
                        color = Brand600
                    )
                }
            }
        }

        // ===== GPS BUTTON =====
        Surface(
            color = Color.White,
            shape = RoundedCornerShape(14.dp),
            shadowElevation = 6.dp,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .statusBarsPadding()
                .padding(top = 60.dp, end = 12.dp)
        ) {
            IconButton(
                onClick = {
                    if (!hasPermissions) {
                        checkAndRequestPermissions()
                    } else {
                        isLocating = true
                        scope.launch {
                            try {
                                val loc = getCurrentGPSLocation(context)
                                if (loc != null) {
                                    currentLat = loc.first; currentLng = loc.second
                                    mapViewRef?.controller?.animateTo(GeoPoint(currentLat, currentLng), 17.0, 1000L)
                                    reverseGeocode(currentLat, currentLng) { addr, city ->
                                        currentAddress = addr; currentCity = city
                                    }
                                }
                            } catch (_: Exception) {}
                            isLocating = false
                        }
                    }
                },
                modifier = Modifier.size(48.dp)
            ) {
                if (isLocating) {
                    CircularProgressIndicator(modifier = Modifier.size(22.dp), strokeWidth = 2.dp, color = Brand600)
                } else {
                    Icon(Icons.Filled.MyLocation, if (hasPermissions) "Mi ubicacion" else "Activar GPS", tint = Brand600, modifier = Modifier.size(24.dp))
                }
            }
        }

        // ===== BOTTOM PANEL =====
        Surface(
            shadowElevation = 16.dp,
            color = Color.White,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.navigationBarsPadding().padding(16.dp)
            ) {
                Row(verticalAlignment = Alignment.Top) {
                    Icon(Icons.Filled.LocationOn, null, tint = Brand600, modifier = Modifier.size(22.dp))
                    Spacer(Modifier.width(8.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            currentAddress,
                            fontSize = 14.sp, fontWeight = FontWeight.Bold,
                            color = DarkSlate, maxLines = 2
                        )
                        if (currentCity.isNotEmpty()) {
                            Spacer(Modifier.height(2.dp))
                            Text(currentCity, fontSize = 12.sp, color = Slate400)
                        }
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "Arrastra el mapa para ajustar el punto",
                            fontSize = 11.sp, color = Slate400
                        )
                    }
                }
                Spacer(Modifier.height(12.dp))
                Button(
                    onClick = {
                        // Get current center of map
                        val center = mapViewRef?.mapCenter
                        val lat = center?.latitude ?: currentLat
                        val lng = center?.longitude ?: currentLng
                        onLocationSelected(PickedLocation(lat, lng, currentAddress, currentCity))
                    },
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Brand600),
                    contentPadding = PaddingValues(vertical = 14.dp),
                    modifier = Modifier.fillMaxWidth().height(50.dp)
                ) {
                    Icon(Icons.Filled.Check, null, tint = Color.White, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Confirmar ubicacion", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
    }
}

// ===== GEOCODE WITH CACHE =====
private val geoCache = java.util.concurrent.ConcurrentHashMap<String, Pair<String, String>>()

private suspend fun searchCity(query: String): List<Pair<String, Pair<Double, Double>>> {
    return withContext(Dispatchers.IO) {
        try {
            val encoded = URLEncoder.encode(query, "UTF-8")
            val url = URL("https://nominatim.openstreetmap.org/search?q=$encoded&format=json&countrycodes=co&accept-language=es&limit=20")
            val conn = url.openConnection() as HttpURLConnection
            conn.setRequestProperty("User-Agent", "AgroConectaApp/2.0")
            conn.connectTimeout = 5000; conn.readTimeout = 5000
            if (conn.responseCode != 200) { conn.disconnect(); return@withContext emptyList() }
            val response = conn.inputStream.bufferedReader().readText(); conn.disconnect()
            val results = mutableListOf<Pair<String, Pair<Double, Double>>>()
            val jsonArray = org.json.JSONArray(response)
            for (i in 0 until jsonArray.length()) {
                val item = jsonArray.getJSONObject(i)
                val dn = item.optString("display_name", "")
                val lat = item.optDouble("lat", Double.NaN); val lng = item.optDouble("lon", Double.NaN)
                if (!lat.isNaN() && !lng.isNaN() && dn.isNotBlank()) results.add(dn to (lat to lng))
            }
            results
        } catch (e: Exception) { emptyList() }
    }
}

private suspend fun reverseGeocode(lat: Double, lng: Double, callback: (addr: String, city: String) -> Unit) {
    val key = "${"%.4f".format(lat)},${"%.4f".format(lng)}"
    geoCache[key]?.let {
        withContext(Dispatchers.Main) { callback(it.first, it.second) }
        return
    }
    withContext(Dispatchers.IO) {
        try {
            val url = URL("https://nominatim.openstreetmap.org/reverse?lat=$lat&lon=$lng&format=json&accept-language=es")
            val conn = url.openConnection() as HttpURLConnection
            conn.setRequestProperty("User-Agent", "AgroConectaApp/2.0")
            conn.connectTimeout = 6000; conn.readTimeout = 6000
            if (conn.responseCode != 200) { conn.disconnect(); withContext(Dispatchers.Main) { callback("$lat, $lng", "") }; return@withContext }
            val response = conn.inputStream.bufferedReader().readText(); conn.disconnect()
            val displayName = Regex("\"display_name\"\\s*:\\s*\"([^\"]+)\"").find(response)?.groupValues?.get(1) ?: "Ubicacion seleccionada"
            val cityName = Regex("\"city\"\\s*:\\s*\"([^\"]+)\"").find(response)
                ?: Regex("\"town\"\\s*:\\s*\"([^\"]+)\"").find(response)
                ?: Regex("\"municipality\"\\s*:\\s*\"([^\"]+)\"").find(response)
            val city = cityName?.groupValues?.get(1) ?: ""
            val result = displayName to city
            geoCache[key] = result
            withContext(Dispatchers.Main) { callback(displayName, city) }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) { callback("$lat, $lng", "") }
        }
    }
}

private suspend fun getCurrentGPSLocation(context: android.content.Context): Pair<Double, Double>? {
    return withContext(Dispatchers.IO) {
        try {
            val locManager = context.getSystemService(android.content.Context.LOCATION_SERVICE) as android.location.LocationManager
            val providers = listOf(
                android.location.LocationManager.GPS_PROVIDER,
                android.location.LocationManager.NETWORK_PROVIDER
            )
            for (provider in providers) {
                try {
                    @Suppress("MissingPermission")
                    val loc = locManager.getLastKnownLocation(provider)
                    if (loc != null) return@withContext Pair(loc.latitude, loc.longitude)
                } catch (_: Exception) {}
            }
            null
        } catch (e: Exception) {
            null
        }
    }
}
