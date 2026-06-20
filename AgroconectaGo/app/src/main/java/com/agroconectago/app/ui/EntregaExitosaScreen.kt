package com.agroconectago.app.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.agroconectago.app.ui.theme.*
import kotlinx.coroutines.delay

@Composable
fun EntregaExitosaScreen(
    rutaId: Long,
    distanciaKm: Double,
    pago: Double,
    finca: String,
    codigoRuta: String,
    onBackToDashboard: () -> Unit
) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    val comision = pago * 0.05
    val propina = 0.0
    val gananciaNeta = pago - comision + propina

    Box(
        Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF0F766E), Color(0xFF134E4A), Color(0xFF0F172A))
                )
            )
    ) {
        Column(
            Modifier.fillMaxSize().statusBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header
            Row(
                Modifier.fillMaxWidth().padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBackToDashboard) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.White)
                }
                Spacer(Modifier.width(8.dp))
                Text("Resumen de Entrega", fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, color = Color.White)
            }

            Spacer(Modifier.height(16.dp))

            // CHECK ANIMADO
            val scale by animateFloatAsState(
                targetValue = if (visible) 1f else 0f,
                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow)
            )
            Box(
                modifier = Modifier.size(100.dp).graphicsLayer { scaleX = scale; scaleY = scale },
                contentAlignment = Alignment.Center
            ) {
                Box(
                    Modifier
                        .size(90.dp)
                        .clip(CircleShape)
                        .background(Brush.radialGradient(listOf(Color(0xFF34D399), Color(0xFF059669)))),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.CheckCircle, null, tint = Color.White, modifier = Modifier.size(56.dp))
                }
            }

            Spacer(Modifier.height(8.dp))
            Text("Entrega Completada!", fontWeight = FontWeight.ExtraBold, fontSize = 22.sp, color = Color.White)
            Text(codigoRuta, fontSize = 13.sp, color = Color.White.copy(alpha = 0.7f))

            Spacer(Modifier.height(20.dp))

            // TARJETA GANANCIAS
            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(tween(500, delayMillis = 300)) + slideInVertically(tween(500, delayMillis = 300, easing = EaseOutCubic))
            ) {
                Card(
                    Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.95f)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Column(Modifier.padding(24.dp)) {
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Ganancias", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, color = Slate900)
                            Box(
                                Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFFD1FAE5))
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text("+$${String.format("%.0f", gananciaNeta)}", fontWeight = FontWeight.ExtraBold, fontSize = 14.sp, color = Color(0xFF065F46))
                            }
                        }
                        Spacer(Modifier.height(16.dp))
                        FilaDetalle("Pago por envio", "$${String.format("%.0f", pago)}")
                        FilaDetalle("Comision AgroConecta (5%)", "-$${String.format("%.0f", comision)}")
                        FilaDetalle("Propina", "$${String.format("%.0f", propina)}")
                        Divider(Modifier.padding(vertical = 8.dp), color = Slate200)
                        FilaDetalle("Ganancia Neta", "$${String.format("%.0f", gananciaNeta)}", bold = true)
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            // TARJETA DETALLES
            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(tween(500, delayMillis = 500)) + slideInVertically(tween(500, delayMillis = 500, easing = EaseOutCubic))
            ) {
                Card(
                    Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.9f)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(Modifier.padding(20.dp)) {
                        Text("Detalles del Pedido", fontWeight = FontWeight.ExtraBold, fontSize = 14.sp, color = Slate700)
                        Spacer(Modifier.height(12.dp))
                        FilaIcono(Icons.Filled.Numbers, "ID Ruta", codigoRuta)
                        FilaIcono(Icons.Filled.Home, "Finca", finca)
                        FilaIcono(Icons.Filled.Route, "Distancia total", "${String.format("%.1f", distanciaKm)} km")
                        FilaIcono(Icons.Filled.CalendarToday, "Fecha", java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.getDefault()).format(java.util.Date()))
                    }
                }
            }

            Spacer(Modifier.weight(1f))

            // BOTON VOLVER
            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(tween(500, delayMillis = 700)) + slideInVertically(tween(500, delayMillis = 700, easing = EaseOutCubic))
            ) {
                Button(
                    onClick = onBackToDashboard,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 20.dp).height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White)
                ) {
                    Icon(Icons.Filled.Home, null, tint = Slate900, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Volver al inicio", fontWeight = FontWeight.ExtraBold, fontSize = 15.sp, color = Slate900)
                }
            }
        }
    }
}

@Composable
private fun FilaDetalle(label: String, valor: String, bold: Boolean = false) {
    Row(Modifier.fillMaxWidth().padding(vertical = 3.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, fontSize = 13.sp, color = Slate500, fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal)
        Text(valor, fontSize = 13.sp, color = if (bold) Slate900 else Slate700, fontWeight = if (bold) FontWeight.ExtraBold else FontWeight.SemiBold)
    }
}

@Composable
private fun FilaIcono(icono: androidx.compose.ui.graphics.vector.ImageVector, label: String, valor: String) {
    Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(icono, null, tint = Slate400, modifier = Modifier.size(16.dp))
        Spacer(Modifier.width(10.dp))
        Text(label, fontSize = 12.sp, color = Slate500, modifier = Modifier.width(100.dp))
        Text(valor, fontSize = 13.sp, color = Slate700, fontWeight = FontWeight.SemiBold)
    }
}
