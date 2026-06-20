package com.agroconectago.app.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.agroconectago.app.ui.theme.*
import kotlinx.coroutines.delay

@Composable
fun DeliveryVerificacionScreen(onSalir: () -> Unit) {
    var isVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { delay(200); isVisible = true }

    val infiniteTransition = rememberInfiniteTransition(label = "verif")
    val orbX by infiniteTransition.animateFloat(0.72f, 0.88f, infiniteRepeatable(tween(4500), RepeatMode.Reverse))
    val orbAlpha by infiniteTransition.animateFloat(0.03f, 0.05f, infiniteRepeatable(tween(3500), RepeatMode.Reverse))
    val glowPulse by infiniteTransition.animateFloat(0.6f, 1f, infiniteRepeatable(tween(2000), RepeatMode.Reverse))

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DeliverySurface)
    ) {
        Box(
            Modifier.fillMaxSize().drawBehind {
                drawCircle(DeliveryBrand600.copy(alpha = orbAlpha), radius = size.width * 0.7f, center = Offset(size.width * orbX, size.height * 0.15f))
                drawCircle(DeliveryAccent600.copy(alpha = 0.02f), radius = size.width * 0.5f, center = Offset(size.width * 0.15f, size.height * 0.8f))
            }
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 28.dp)
                .statusBarsPadding()
                .navigationBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            AnimatedVisibility(visible = isVisible, enter = fadeIn(tween(700)) + scaleIn(tween(600, delayMillis = 100))) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {

                    // Logo animado con glow
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .clip(RoundedCornerShape(30.dp))
                            .background(Brush.linearGradient(listOf(DeliveryBrand500, DeliveryBrand600, DeliveryBrand900)))
                            .shadow(30.dp, RoundedCornerShape(30.dp), spotColor = DeliveryBrand600.copy(alpha = glowPulse * 0.5f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            Modifier.fillMaxSize().background(
                                Brush.radialGradient(listOf(Color.White.copy(alpha = 0.12f), Color.Transparent), center = Offset(55f, 25f), radius = 70f)
                            )
                        )
                        Icon(Icons.Filled.VerifiedUser, null, tint = Color.White, modifier = Modifier.size(50.dp))
                    }

                    Spacer(Modifier.height(32.dp))

                    Text(
                        "Tus documentos estan\nen verificacion",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 26.sp,
                        color = Slate900,
                        textAlign = TextAlign.Center,
                        lineHeight = 33.sp,
                        letterSpacing = (-0.5).sp
                    )

                    Spacer(Modifier.height(14.dp))

                    Text(
                        "Nuestro equipo de AgroConecta esta revisando\nlos documentos que enviaste. Este proceso\ntoma entre 24 y 48 horas habiles.",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Slate500,
                        textAlign = TextAlign.Center,
                        lineHeight = 21.sp
                    )

                    Spacer(Modifier.height(36.dp))

                    // Que esperar card
                    Card(
                        Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF0FDFA)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, DeliveryBrand400.copy(alpha = 0.2f))
                    ) {
                        Column(Modifier.padding(20.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.Schedule, null, tint = DeliveryBrand600, modifier = Modifier.size(22.dp))
                                Spacer(Modifier.width(10.dp))
                                Text("Que sucede ahora?", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Slate900)
                            }
                            Spacer(Modifier.height(12.dp))
                            PasoRow("1", "Un administrador revisa tus documentos y datos del vehiculo")
                            PasoRow("2", "Si todo esta en orden, tu cuenta sera aprobada")
                            PasoRow("3", "Recibiras una notificacion cuando estes activo")
                            PasoRow("4", "Podras aceptar viajes y empezar a generar ingresos")
                        }
                    }

                    Spacer(Modifier.height(28.dp))

                    Text(
                        "Cuando seas aprobado, podras generar ingresos\nentregando la cosecha colombiana en tu ciudad.",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = Slate400,
                        textAlign = TextAlign.Center,
                        lineHeight = 19.sp
                    )

                    Spacer(Modifier.height(20.dp))
                    Text("Gracias por elegirnos", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = DeliveryBrand600)

                    Spacer(Modifier.height(36.dp))

                    val interactionSource = remember { MutableInteractionSource() }
                    val isPressed by interactionSource.collectIsPressedAsState()
                    val scale by animateFloatAsState(if (isPressed) 0.96f else 1f, spring(dampingRatio = Spring.DampingRatioMediumBouncy))

                    Button(
                        onClick = onSalir,
                        interactionSource = interactionSource,
                        shape = RoundedCornerShape(18.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = DeliveryBrand600),
                        contentPadding = PaddingValues(vertical = 18.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .graphicsLayer { scaleX = scale; scaleY = scale }
                            .shadow(20.dp, RoundedCornerShape(18.dp), spotColor = DeliveryBrand600.copy(alpha = 0.4f))
                    ) {
                        Text("Salir", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
                        Spacer(Modifier.width(10.dp))
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, null, tint = Color.White, modifier = Modifier.size(20.dp))
                    }

                    Spacer(Modifier.height(8.dp))
                    Text("Podras volver cuando tu cuenta sea aprobada", fontSize = 11.sp, color = Slate400)
                }
            }
        }
    }
}

@Composable
private fun PasoRow(numero: String, texto: String) {
    Row(
        Modifier.padding(vertical = 3.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            Modifier.size(22.dp).clip(RoundedCornerShape(6.dp)).background(DeliveryBrand50),
            contentAlignment = Alignment.Center
        ) {
            Text(numero, fontWeight = FontWeight.Bold, fontSize = 11.sp, color = DeliveryBrand600)
        }
        Spacer(Modifier.width(10.dp))
        Text(texto, fontSize = 12.sp, color = Slate500, lineHeight = 17.sp)
    }
}
