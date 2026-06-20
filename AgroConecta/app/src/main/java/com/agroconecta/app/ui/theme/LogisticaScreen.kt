package com.agroconecta.app.ui.theme

import android.graphics.Color as AndroidColor
import android.graphics.Paint
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.ShapeDrawable
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.agroconecta.app.data.model.LogisticaResponse
import com.agroconecta.app.viewmodel.TiendaViewModel
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline

private val Carbon = Color(0xFF111827)
private val Slate400 = Color(0xFF94A3B8)
private val Slate500 = Color(0xFF64748B)
private val Slate200 = Color(0xFFE2E8F0)
private val AppBackground = Color(0xFFF9FBF9)
private val Emerald = Color(0xFF0E793D)
private val Blue500 = Color(0xFF3B82F6)
private val Green500 = Color(0xFF22C55E)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogisticaScreen(pedidoId: Long, tiendaVM: TiendaViewModel, onNavigateBack: () -> Unit) {
    LaunchedEffect(pedidoId) { tiendaVM.cargarLogistica(pedidoId) }

    val data = tiendaVM.logisticaResponse

    Scaffold(
        containerColor = AppBackground,
        topBar = {
            Surface(shadowElevation = 0.dp, color = Color.White) {
                Row(modifier = Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = 8.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onNavigateBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Carbon) }
                    Text("Inteligencia Logistica", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = Carbon)
                }
            }
        }
    ) { padding ->
        if (tiendaVM.logisticaCargando && data == null) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = Emerald) }
            return@Scaffold
        }
        if (data == null || data.success == false) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Filled.Satellite, null, tint = Slate400, modifier = Modifier.size(64.dp))
                    Spacer(Modifier.height(16.dp))
                    Text(data?.error ?: "Servicio de rutas inactivo", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Slate400)
                }
            }
            return@Scaffold
        }

        LazyColumn(modifier = Modifier.fillMaxSize().padding(padding)) {
            // Info card
            item {
                Surface(color = Color.White, shape = RoundedCornerShape(24.dp), shadowElevation = 1.dp, border = androidx.compose.foundation.BorderStroke(1.dp, Slate200),
                    modifier = Modifier.fillMaxWidth().padding(16.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text("Detalle del Envio", fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, color = Emerald)
                        Spacer(Modifier.height(6.dp))
                        Text(data.producto ?: "Producto", fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = Carbon)
                        Spacer(Modifier.height(8.dp))
                        Row {
                            Surface(color = Green500.copy(alpha = 0.1f), shape = RoundedCornerShape(8.dp)) {
                                Text("${data.cantidad ?: 0} Unidades", modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Green500)
                            }
                            Spacer(Modifier.width(10.dp))
                            Text("Total: \$${formatNumLog(data.total ?: 0.0)}", fontSize = 13.sp, color = Slate500)
                        }
                        Spacer(Modifier.height(16.dp))

                        // Metrics
                        Surface(color = Slate200.copy(alpha = 0.3f), shape = RoundedCornerShape(16.dp)) {
                            Row(modifier = Modifier.fillMaxWidth().padding(12.dp)) {
                                Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(Icons.Filled.Route, null, tint = Emerald, modifier = Modifier.size(18.dp))
                                    Text(data.distancia_km?.let { String.format("%.1f km", it) } ?: "--", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Carbon)
                                    Text("Distancia OSRM", fontSize = 10.sp, color = Slate400)
                                }
                                Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(Icons.Filled.Schedule, null, tint = Blue500, modifier = Modifier.size(18.dp))
                                    Text(data.duracion_min?.let { String.format("%.0f mins", it) } ?: "--", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Carbon)
                                    Text("Tiempo Est.", fontSize = 10.sp, color = Slate400)
                                }
                                Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(Icons.Filled.Person, null, tint = Slate500, modifier = Modifier.size(18.dp))
                                    Text(data.clienteNombre ?: "--", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Carbon, maxLines = 1)
                                    Text("Cliente", fontSize = 10.sp, color = Slate400)
                                }
                            }
                        }
                        Spacer(Modifier.height(12.dp))
                        Row(verticalAlignment = Alignment.Top) {
                            Icon(Icons.Filled.LocationOn, null, tint = Slate400, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text(data.direccionEnvio ?: "Sin direccion", fontSize = 13.sp, color = Slate500, lineHeight = 18.sp)
                        }
                    }
                }
            }

            // Map
            item {
                val origenLat = data.origenLat ?: 5.9317
                val origenLon = data.origenLon ?: -73.6147
                val destLat = data.destLat ?: 7.1254
                val destLon = data.destLon ?: -73.1198

                Surface(color = Color.White, shape = RoundedCornerShape(24.dp), shadowElevation = 2.dp, border = androidx.compose.foundation.BorderStroke(1.dp, Slate200),
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp)
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        Text("Mapa de Ruta", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Carbon, modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp))

                        val context = LocalContext.current
                        AndroidView(
                            factory = { ctx ->
                                MapView(ctx).apply {
                                    setTileSource(TileSourceFactory.MAPNIK)
                                    setMultiTouchControls(true)
                                    controller.setZoom(9.0)
                                    val midLat = (origenLat + destLat) / 2
                                    val midLon = (origenLon + destLon) / 2
                                    controller.setCenter(GeoPoint(midLat, midLon))

                                    // Origin marker
                                    val origenMarker = Marker(this)
                                    origenMarker.position = GeoPoint(origenLat, origenLon)
                                    origenMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                                    origenMarker.title = "Tu Finca"
                                    overlays.add(origenMarker)

                                    // Destination marker
                                    val destMarker = Marker(this)
                                    destMarker.position = GeoPoint(destLat, destLon)
                                    destMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                                    destMarker.title = "Cliente"
                                    overlays.add(destMarker)

                                    // Route line
                                    val line = Polyline()
                                    line.addPoint(GeoPoint(origenLat, origenLon))
                                    line.addPoint(GeoPoint(destLat, destLon))
                                    line.outlinePaint.color = AndroidColor.parseColor("#22C55E")
                                    line.outlinePaint.strokeWidth = 8f
                                    overlays.add(line)

                                    // Zoom to fit
                                    val points = listOf(GeoPoint(origenLat, origenLon), GeoPoint(destLat, destLon))
                                    val bbox = org.osmdroid.util.BoundingBox.fromGeoPoints(points)
                                    post { zoomToBoundingBox(bbox, true, 80) }
                                }
                            },
                            modifier = Modifier.fillMaxWidth().height(380.dp).clip(RoundedCornerShape(16.dp))
                        )
                    }
                }
            }

            item { Spacer(Modifier.height(16.dp)) }
        }
    }
}

private fun formatNumLog(value: Double): String {
    val n = value.toLong()
    if (n >= 1000) {
        val parts = mutableListOf<String>()
        var num = n
        while (num > 0) { parts.add(0, (num % 1000).toString().padStart(3, '0')); num /= 1000 }
        return parts.joinToString(".")
    }
    return n.toString()
}
