package com.agroconectago.app.ui

import android.graphics.Color as AndroidColor
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.agroconectago.app.data.LocationTracker
import com.agroconectago.app.data.api.DeliveryRetrofitClient
import com.agroconectago.app.ui.theme.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
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
fun MapaCompletoScreen(
    rutaId: Long,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val tracker = remember { LocationTracker(context).also { it.start() } }

    var rutaData by remember { mutableStateOf<Map<String, Any?>>(emptyMap()) }
    var currentLat by remember { mutableStateOf<Double?>(null) }
    var currentLng by remember { mutableStateOf<Double?>(null) }
    var routePoints by remember { mutableStateOf<List<GeoPoint>>(emptyList()) }
    var lastRouteFetch by remember { mutableStateOf(0L) }

    val destinoLat = (rutaData["latitudOrigen"] as? Number)?.toDouble()
    val destinoLng = (rutaData["longitudOrigen"] as? Number)?.toDouble()

    suspend fun fetchOsrmRoute() {
        val clat = currentLat ?: return
        val clng = currentLng ?: return
        val dlat = destinoLat ?: return
        val dlng = destinoLng ?: return

        try {
            val resp = DeliveryRetrofitClient.api.getOsrmRoute(lat1 = clat, lng1 = clng, lat2 = dlat, lng2 = dlng)
            if (resp["success"] as? Boolean == true) {
                @Suppress("UNCHECKED_CAST")
                val puntos = resp["points"] as? List<List<Any?>> ?: return
                if (puntos.size > 1) {
                    routePoints = puntos.mapNotNull { p ->
                        val lat = (p[0] as? Number)?.toDouble() ?: return@mapNotNull null
                        val lng = (p[1] as? Number)?.toDouble() ?: return@mapNotNull null
                        GeoPoint(lat, lng)
                    }
                    lastRouteFetch = System.currentTimeMillis()
                    return
                }
            }
        } catch (_: Exception) {}

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
                } else conn.disconnect()
            } catch (_: Exception) {}
        }
    }

    LaunchedEffect(rutaId) {
        try {
            val resp = DeliveryRetrofitClient.api.obtenerRuta(rutaId)
            rutaData = (resp["ruta"] as? Map<String, Any?>) ?: emptyMap()
        } catch (_: Exception) {}
        fetchOsrmRoute()
    }

    LaunchedEffect(Unit) {
        while (true) {
            tracker.getCurrentLocation()?.let {
                currentLat = it.first; currentLng = it.second
            }
            delay(1000)
        }
    }

    LaunchedEffect(currentLat, currentLng) {
        if (currentLat != null && currentLng != null && routePoints.isEmpty()) fetchOsrmRoute()
    }

    LaunchedEffect(Unit) {
        while (true) {
            delay(4000)
            if (System.currentTimeMillis() - lastRouteFetch > 10000) fetchOsrmRoute()
        }
    }

    Box(Modifier.fillMaxSize()) {
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
                    setMinZoomLevel(4.0); setMaxZoomLevel(19.0)

                    val fLat = destinoLat ?: 4.7110; val fLng = destinoLng ?: -74.0721
                    val cLat = currentLat ?: 4.7110; val cLng = currentLng ?: -74.0721
                    controller.setZoom(15.0); controller.setCenter(GeoPoint(fLat, fLng))

                    val mOrigen = Marker(this)
                    mOrigen.position = GeoPoint(cLat, cLng)
                    mOrigen.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
                    mOrigen.title = "Tu ubicacion"
                    mOrigen.icon = ctx.resources.getDrawable(android.R.drawable.ic_menu_mylocation)
                    overlays.add(mOrigen); markerOrigen = mOrigen

                    val mDestino = Marker(this)
                    mDestino.position = GeoPoint(fLat, fLng)
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
                    ptsInit.add(GeoPoint(fLat, fLng)); poly.setPoints(ptsInit)
                    overlays.add(poly); routePolyline = poly

                    invalidate(); mapView = this
                }
            },
            modifier = Modifier.fillMaxSize(),
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

        // Overlay: Boton regresar
        Box(
            modifier = Modifier
                .align(Alignment.TopStart).statusBarsPadding().padding(12.dp)
                .size(42.dp).clip(CircleShape)
                .background(Color.White.copy(alpha = 0.9f))
                .clickable { onBack() },
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Slate700, modifier = Modifier.size(22.dp))
        }

        // Overlay: Indicador GPS
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter).padding(bottom = 16.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(Color.White.copy(alpha = 0.85f))
                .padding(horizontal = 16.dp, vertical = 6.dp)
        ) {
            Text("GPS en tiempo real · Actualiza cada 1s", fontSize = 10.sp, color = Slate500, fontWeight = FontWeight.Medium)
        }
    }
}
