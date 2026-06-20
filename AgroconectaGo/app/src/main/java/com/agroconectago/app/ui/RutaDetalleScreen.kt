package com.agroconectago.app.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.agroconectago.app.data.api.DeliveryRetrofitClient
import com.agroconectago.app.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun RutaDetalleScreen(
    rutaId: Long,
    onBack: () -> Unit,
    onRutaAceptada: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var loading by remember { mutableStateOf(true) }
    var accepting by remember { mutableStateOf(false) }
    var ruta by remember { mutableStateOf<Map<String, Any?>>(emptyMap()) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(rutaId) {
        try {
            val resp = DeliveryRetrofitClient.api.obtenerRuta(rutaId)
            ruta = (resp["ruta"] as? Map<String, Any?>) ?: emptyMap()
        } catch (_: Exception) {}
        loading = false
    }

    fun aceptar() {
        scope.launch {
            accepting = true
            try {
                val resp = DeliveryRetrofitClient.api.aceptarRuta(rutaId)
                if (resp["success"] as? Boolean == true) {
                    onRutaAceptada()
                } else {
                    error = resp["message"]?.toString() ?: "Error al aceptar"
                }
            } catch (_: Exception) { error = "Error de conexion" }
            accepting = false
        }
    }

    Box(Modifier.fillMaxSize().background(DeliverySurface)) {
        if (loading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = DeliveryBrand600) }
            return@Box
        }

        Column(Modifier.fillMaxSize().statusBarsPadding()) {
            Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Slate400) }
                Text("Detalle de Ruta", fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = Slate900, modifier = Modifier.weight(1f))
            }

            Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp)) {
                Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)) {
                    Column(Modifier.padding(20.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(Modifier.size(48.dp).clip(RoundedCornerShape(14.dp)).background(DeliveryBrand600), contentAlignment = Alignment.Center) { Icon(Icons.Filled.LocalShipping, null, tint = Color.White, modifier = Modifier.size(24.dp)) }
                            Spacer(Modifier.width(14.dp))
                            Column {
                                Text(ruta["codigoRuta"]?.toString() ?: "", fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = Slate900)
                                Text("LISTA PARA SALIR", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = DeliveryBrand600)
                            }
                        }

                        Spacer(Modifier.height(20.dp))

                        DetailRow(Icons.Filled.LocationOn, "Origen", ruta["zonaOrigen"]?.toString() ?: "-")
                        DetailRow(Icons.Filled.Flag, "Destino", ruta["zonaDestino"]?.toString() ?: "-")
                        DetailRow(Icons.Filled.Inventory2, "Pedidos", "${ruta["pedidosCount"]?.toString() ?: "0"} unidades")
                        DetailRow(Icons.Filled.Scale, "Peso total", "${ruta["pesoTotalKg"]?.toString() ?: "0"} kg")
                        val pago = (ruta["pagoTotalEstimado"] as? Number)?.toDouble() ?: 0.0
                        DetailRow(Icons.Filled.Payments, "Pago estimado", "$${String.format("%.0f", pago)} COP")
                    }
                }

                Spacer(Modifier.height(12.dp))

                Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFFF0FDFA)), border = androidx.compose.foundation.BorderStroke(1.dp, DeliveryBrand400.copy(alpha = 0.2f))) {
                    Column(Modifier.padding(16.dp)) {
                        Text("Informacion importante", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Slate900)
                        Spacer(Modifier.height(6.dp))
                        Text("Al aceptar esta ruta te comprometes a recoger los productos en las fincas indicadas y entregarlos en las direcciones de los clientes.", fontSize = 11.sp, color = Slate500, lineHeight = 16.sp)
                    }
                }

                if (error != null) {
                    Spacer(Modifier.height(8.dp))
                    Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF2F2))) {
                        Text(error ?: "", Modifier.padding(12.dp), fontSize = 12.sp, color = Color(0xFFDC2626))
                    }
                }

                Spacer(Modifier.height(20.dp))

                val interactionSource = remember { MutableInteractionSource() }
                val isPressed by interactionSource.collectIsPressedAsState()
                val scale by animateFloatAsState(if (isPressed) 0.96f else 1f)
                Button(onClick = { aceptar() }, enabled = !accepting, interactionSource = interactionSource, shape = RoundedCornerShape(16.dp), colors = ButtonDefaults.buttonColors(containerColor = DeliveryBrand600), contentPadding = PaddingValues(vertical = 16.dp), modifier = Modifier.fillMaxWidth().graphicsLayer { scaleX = scale; scaleY = scale }.shadow(20.dp, RoundedCornerShape(16.dp), spotColor = DeliveryBrand600.copy(alpha = 0.4f))) {
                    if (accepting) { CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp, color = Color.White); Spacer(Modifier.width(10.dp)); Text("Aceptando...", fontWeight = FontWeight.Bold) }
                    else { Text("Aceptar Ruta", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp); Spacer(Modifier.width(10.dp)); Icon(Icons.Filled.Check, null, tint = Color.White, modifier = Modifier.size(20.dp)) }
                }

                Spacer(Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun DetailRow(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String) {
    Row(Modifier.fillMaxWidth().padding(vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = Slate400, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(10.dp))
        Text("$label:", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = Slate500)
        Spacer(Modifier.width(6.dp))
        Text(value, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Slate900)
    }
}
