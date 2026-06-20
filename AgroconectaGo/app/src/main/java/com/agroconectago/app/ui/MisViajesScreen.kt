package com.agroconectago.app.ui

import androidx.compose.animation.*
import androidx.compose.foundation.background
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.agroconectago.app.data.api.DeliveryRetrofitClient
import com.agroconectago.app.ui.theme.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun MisViajesScreen(onBack: () -> Unit) {
    val scope = rememberCoroutineScope()
    var rutas by remember { mutableStateOf<List<Map<String, Any?>>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var totalGanancias by remember { mutableStateOf(0.0) }
    var totalEntregas by remember { mutableStateOf(0) }
    var errorMsg by remember { mutableStateOf("") }

    fun cargar() {
        scope.launch {
            loading = true; errorMsg = ""
            try {
                val resp = DeliveryRetrofitClient.api.misRutas()
                val ok = resp["success"] as? Boolean ?: false
                if (!ok) {
                    errorMsg = "Error del servidor: " + (resp["error"]?.toString() ?: "sesion invalida")
                } else {
                    @Suppress("UNCHECKED_CAST")
                    val lista = (resp["rutas"] as? List<*>)?.filterIsInstance<Map<String, Any?>>() ?: emptyList()
                    rutas = lista
                    totalGanancias = lista.sumOf { (it["pagoTotalEstimado"] as? Number)?.toDouble() ?: 0.0 }
                    totalEntregas = lista.count { it["estado"]?.toString() == "COMPLETADA" }
                }
            } catch (e: Exception) { errorMsg = "Error de conexion: ${e.message}" }
            loading = false
        }
    }

    LaunchedEffect(Unit) { cargar() }

    Column(Modifier.fillMaxSize().background(Color.White).statusBarsPadding()) {
        // Header
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Slate700) }
            Text("Mis Envios", fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = Slate900, modifier = Modifier.weight(1f))
        }

        // Stats cards
        Row(Modifier.fillMaxWidth().padding(horizontal = 14.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Card(Modifier.weight(1f), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFFF0FDF4))) {
                Column(Modifier.padding(14.dp)) {
                    Text("Ganancias", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF166534))
                    Text("$${String.format("%,.0f", totalGanancias)}", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF15803D))
                }
            }
            Card(Modifier.weight(1f), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFFEFF6FF))) {
                Column(Modifier.padding(14.dp)) {
                    Text("Entregas", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E40AF))
                    Text("$totalEntregas", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF1D4ED8))
                }
            }
            Card(Modifier.weight(1f), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFBEB))) {
                Column(Modifier.padding(14.dp)) {
                    Text("Rating", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF92400E))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Star, null, tint = Color(0xFFF59E0B), modifier = Modifier.size(16.dp))
                        Text(String.format("%.1f", if (totalEntregas > 0) totalGanancias / totalEntregas * 0 else 0.0), fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFFB45309))
                    }
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        if (loading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = DeliveryBrand600) }
        } else if (errorMsg.isNotBlank()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(24.dp)) {
                    Icon(Icons.Filled.ErrorOutline, null, tint = ErrorRed, modifier = Modifier.size(48.dp))
                    Spacer(Modifier.height(12.dp))
                    Text(errorMsg, fontSize = 14.sp, color = Slate700, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(16.dp))
                    OutlinedButton(onClick = { cargar() }) {
                        Text("Reintentar")
                    }
                }
            }
        } else if (rutas.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Filled.Inbox, null, tint = Slate300, modifier = Modifier.size(56.dp))
                    Spacer(Modifier.height(12.dp))
                    Text("No tienes envios aun", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = Slate400)
                    Text("Acepta una ruta para comenzar", fontSize = 13.sp, color = Slate400)
                }
            }
        } else {
            LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(horizontal = 14.dp, vertical = 4.dp)) {
                items(rutas) { ruta ->
                    val estado = ruta["estado"]?.toString() ?: ""
                    val esCompletada = estado == "COMPLETADA"
                    val colorBorde = if (esCompletada) Color(0xFF16A34A) else if (estado == "ASIGNADA") Color(0xFFF59E0B) else Color(0xFF3B82F6)

                    Card(
                        Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, colorBorde.copy(alpha = 0.3f))
                    ) {
                        Column(Modifier.padding(14.dp)) {
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(Modifier.size(32.dp).clip(RoundedCornerShape(8.dp)).background(colorBorde.copy(alpha = 0.1f)), contentAlignment = Alignment.Center) {
                                        Icon(if (esCompletada) Icons.Filled.CheckCircle else Icons.Filled.LocalShipping, null, tint = colorBorde, modifier = Modifier.size(16.dp))
                                    }
                                    Spacer(Modifier.width(10.dp))
                                    Column {
                                        Text(ruta["codigoRuta"]?.toString() ?: "", fontWeight = FontWeight.ExtraBold, fontSize = 14.sp, color = Slate900)
                                        Text(ruta["zonaOrigen"]?.toString() ?: "", fontSize = 11.sp, color = Slate400)
                                    }
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text("$${String.format("%,.0f", (ruta["pagoTotalEstimado"] as? Number)?.toDouble() ?: 0.0)}", fontWeight = FontWeight.ExtraBold, fontSize = 15.sp, color = if (esCompletada) Color(0xFF16A34A) else Slate600)
                                    Text(estado, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = colorBorde)
                                }
                            }
                            Spacer(Modifier.height(6.dp))
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("${ruta["pedidosCount"]?.toString() ?: "0"} pedidos · ${String.format("%.1f", (ruta["pesoTotalKg"] as? Number)?.toDouble() ?: 0.0)} kg", fontSize = 10.sp, color = Slate500)
                                val fecha = ruta["fechaCompletada"]?.toString() ?: ruta["fechaAsignacion"]?.toString() ?: ""
                                val fechaStr = remember(fecha) {
                                    if (fecha.isNotBlank() && fecha.length >= 16) {
                                        try {
                                            val f = SimpleDateFormat("yyyy-MM-dd'T'HH:mm", Locale.getDefault()).parse(fecha.substring(0, 16))
                                            SimpleDateFormat("dd MMM HH:mm", Locale("es")).format(f!!)
                                        } catch (_: Exception) { fecha.take(16) }
                                    } else ""
                                }
                                if (fechaStr.isNotBlank()) { Text(fechaStr, fontSize = 10.sp, color = Slate400) }
                            }
                        }
                    }
                }
            }
        }
    }
}
