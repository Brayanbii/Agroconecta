package com.agroconecta.app.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.agroconecta.app.data.model.SoporteMensaje
import com.agroconecta.app.data.model.TicketSoporte
import com.agroconecta.app.viewmodel.TiendaViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private val Orange500 = Color(0xFFF97316)
private val Orange400 = Color(0xFFFB923C)
private val Carbon = Color(0xFF111827)
private val Slate400 = Color(0xFF94A3B8)
private val Slate500 = Color(0xFF64748B)
private val Slate300 = Color(0xFFCBD5E1)
private val Slate200 = Color(0xFFE2E8F0)
private val Slate100 = Color(0xFFF1F5F9)
private val Green500 = Color(0xFF22C55E)
private val Blue500 = Color(0xFF3B82F6)
private val Red500 = Color(0xFFEF4444)
private val Indigo500 = Color(0xFF6366F1)
private val White = Color.White

enum class SoporteView { FAQ, TICKETS, NEW, CHAT }

@Composable
fun SupportWidget(tiendaVM: TiendaViewModel) {
    var visible by remember { mutableStateOf(false) }
    var currentView by remember { mutableStateOf(SoporteView.FAQ) }
    var nuevoAsunto by remember { mutableStateOf("") }
    var nuevoMensaje by remember { mutableStateOf("") }
    var replyText by remember { mutableStateOf("") }
    var ticketAsunto by remember { mutableStateOf("") }
    var ticketEstado by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    val animScale by animateFloatAsState(if (visible) 1f else 0f, tween(300, easing = FastOutSlowInEasing))

    // Polling for new messages
    LaunchedEffect(tiendaVM.soporteTicketActivoId) {
        while (tiendaVM.soporteTicketActivoId != null) {
            tiendaVM.cargarMensajesTicket(tiendaVM.soporteTicketActivoId!!)
            delay(5000)
        }
    }

    // Auto-scroll chat
    LaunchedEffect(tiendaVM.soporteMensajes.size) {
        if (tiendaVM.soporteMensajes.isNotEmpty() && currentView == SoporteView.CHAT) {
            delay(100)
            listState.animateScrollToItem(tiendaVM.soporteMensajes.size - 1)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // FAB
        androidx.compose.foundation.layout.Box(
            modifier = Modifier.align(Alignment.BottomEnd).padding(end = 16.dp, bottom = 16.dp)
        ) {
            FloatingActionButton(
                onClick = {
                    if (!visible) {
                        visible = true
                        currentView = SoporteView.FAQ
                    } else visible = false
                },
                containerColor = Orange500,
                contentColor = Color.White
            ) {
                Icon(
                    if (visible) Icons.Filled.Close else Icons.Filled.HeadsetMic,
                    null,
                    modifier = Modifier.scale(if (visible) 1f else 1f)
                )
            }
        }

        // Panel
        AnimatedVisibility(
            visible = visible,
            enter = fadeIn(tween(200)) + scaleIn(initialScale = 0.9f, animationSpec = tween(300, easing = EaseOutBack)),
            exit = fadeOut(tween(200)) + scaleOut(targetScale = 0.9f, animationSpec = tween(200)),
            modifier = Modifier.align(Alignment.BottomEnd).padding(end = 16.dp, bottom = 76.dp)
        ) {
            Surface(
                modifier = Modifier.width(340.dp).height(480.dp),
                shape = RoundedCornerShape(24.dp),
                shadowElevation = 16.dp,
                color = Color.White
            ) {
                Column {
                    // Header
                    Box(modifier = Modifier.fillMaxWidth().background(Brush.horizontalGradient(listOf(Orange500, Orange400))).padding(16.dp)) {
                        Text("AgroSoporte", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
                        Text("Hola, en que te ayudamos?", fontSize = 11.sp, color = Color.White.copy(alpha = 0.8f))
                    }

                    when (currentView) {
                        SoporteView.FAQ -> FaqView(
                            onMisTickets = {
                                tiendaVM.cargarMisTickets()
                                currentView = SoporteView.TICKETS
                            },
                            onNuevo = {
                                nuevoAsunto = ""; nuevoMensaje = ""
                                currentView = SoporteView.NEW
                            }
                        )
                        SoporteView.TICKETS -> TicketsView(
                            tickets = tiendaVM.soporteTickets,
                            onOpen = { t ->
                                tiendaVM.soporteTicketActivoId = t.id
                                ticketAsunto = t.asunto
                                ticketEstado = t.estado
                                tiendaVM.cargarMensajesTicket(t.id ?: return@TicketsView)
                                currentView = SoporteView.CHAT
                            },
                            onBack = { currentView = SoporteView.FAQ },
                            onNuevo = { currentView = SoporteView.NEW }
                        )
                        SoporteView.NEW -> NewTicketView(
                            asunto = nuevoAsunto, onAsuntoChange = { nuevoAsunto = it },
                            mensaje = nuevoMensaje, onMensajeChange = { nuevoMensaje = it },
                            onSubmit = {
                                tiendaVM.crearTicketSoporte(nuevoAsunto, nuevoMensaje) { id ->
                                    if (id != null) {
                                        tiendaVM.soporteTicketActivoId = id
                                        ticketAsunto = nuevoAsunto
                                        ticketEstado = "ABIERTO"
                                        tiendaVM.cargarMensajesTicket(id)
                                        currentView = SoporteView.CHAT
                                    }
                                }
                            },
                            onBack = {
                                tiendaVM.cargarMisTickets()
                                currentView = SoporteView.TICKETS
                            }
                        )
                        SoporteView.CHAT -> ChatView(
                            mensajes = tiendaVM.soporteMensajes,
                            asunto = ticketAsunto,
                            estado = ticketEstado,
                            replyText = replyText,
                            onReplyChange = { replyText = it },
                            onSend = {
                                tiendaVM.soporteTicketActivoId?.let { id ->
                                    tiendaVM.enviarMensajeSoporte(id, replyText) {
                                        replyText = ""
                                    }
                                }
                            },
                            onBack = {
                                tiendaVM.soporteTicketActivoId = null
                                tiendaVM.cargarMisTickets()
                                currentView = SoporteView.TICKETS
                            },
                            listState = listState
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FaqView(onMisTickets: () -> Unit, onNuevo: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize().padding(12.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Text("Preguntas Frecuentes", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Carbon, modifier = Modifier.weight(1f))
            TextButton(onClick = onMisTickets) { Text("Mis Consultas", fontSize = 12.sp, color = Orange500, fontWeight = FontWeight.Bold) }
        }
        Spacer(Modifier.height(8.dp))
        FaqItem("Mi pedido no llego, que hago?", "Verifica el estado en Mis Compras. Si aparece ENVIADO y pasaron los dias habiles, abre una consulta y nuestro equipo te ayudara.")
        FaqItem("Quiero cancelar una compra", "Puedes cancelar si el estado es PENDIENTE. Si ya fue PAGADO o ENVIADO, solicita un reembolso abriendo un ticket.")
        FaqItem("El campesino no responde", "Los campesinos pueden tardar hasta 24h. Si no obtienes respuesta, nosotros escalamos el caso.")
        Spacer(Modifier.weight(1f))
        Button(onClick = onNuevo, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = Orange500), shape = RoundedCornerShape(12.dp)) {
            Icon(Icons.Filled.HeadsetMic, null, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(8.dp))
            Text("Contactar Soporte", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun FaqItem(q: String, a: String) {
    var expanded by remember { mutableStateOf(false) }
    Surface(color = if (expanded) Color(0xFFFFF7ED) else Slate100, shape = RoundedCornerShape(10.dp), border = BorderStroke(1.dp, if (expanded) Orange500.copy(alpha = 0.3f) else Slate200), modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp).clickable { expanded = !expanded }) {
        Column(modifier = Modifier.padding(10.dp)) {
            Text(q, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Carbon)
            AnimatedVisibility(expanded) {
                Text(a, fontSize = 11.sp, color = Slate500, modifier = Modifier.padding(top = 6.dp))
            }
        }
    }
}

@Composable
private fun TicketsView(tickets: List<TicketSoporte>, onOpen: (TicketSoporte) -> Unit, onBack: () -> Unit, onNuevo: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize()) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 4.dp)) {
            TextButton(onClick = onBack) { Text("Volver", fontSize = 12.sp, color = Slate500) }
            Spacer(Modifier.weight(1f))
            Text("Tus Consultas", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Carbon)
            Spacer(Modifier.weight(1f))
            TextButton(onClick = onNuevo) { Text("Nuevo", fontSize = 12.sp, color = Orange500, fontWeight = FontWeight.Bold) }
        }
        if (tickets.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Filled.Inbox, null, tint = Slate300, modifier = Modifier.size(40.dp))
                    Text("No tienes consultas activas", fontSize = 13.sp, color = Slate400)
                }
            }
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp)) {
                items(tickets) { t ->
                    val statusColor = when (t.estado) { "ABIERTO" -> Red500; "EN_PROGRESO" -> Blue500; else -> Slate400 }
                    Surface(color = Color.White, shape = RoundedCornerShape(12.dp), border = BorderStroke(1.dp, Slate200), modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp).clickable { onOpen(t) }) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(t.asunto, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Carbon, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                Text(t.fecha?.take(10) ?: "", fontSize = 10.sp, color = Slate400)
                            }
                            Surface(color = statusColor.copy(alpha = 0.1f), shape = RoundedCornerShape(6.dp)) {
                                Text(t.estado, modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp), fontSize = 9.sp, fontWeight = FontWeight.Bold, color = statusColor)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun NewTicketView(asunto: String, onAsuntoChange: (String) -> Unit, mensaje: String, onMensajeChange: (String) -> Unit, onSubmit: () -> Unit, onBack: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize().padding(12.dp)) {
        TextButton(onClick = onBack) { Text("Volver", fontSize = 12.sp, color = Slate500) }
        Text("Crear nueva consulta", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Carbon)
        Spacer(Modifier.height(12.dp))
        Text("Asunto", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Slate400)
        OutlinedTextField(value = asunto, onValueChange = onAsuntoChange, modifier = Modifier.fillMaxWidth(), singleLine = true, shape = RoundedCornerShape(10.dp), placeholder = { Text("Ej. Problema con mi pedido") })
        Spacer(Modifier.height(8.dp))
        Text("Mensaje Inicial", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Slate400)
        OutlinedTextField(value = mensaje, onValueChange = onMensajeChange, modifier = Modifier.fillMaxWidth().height(80.dp), maxLines = 4, shape = RoundedCornerShape(10.dp), placeholder = { Text("Describe tu problema con detalle...") })
        Spacer(Modifier.weight(1f))
        Button(onClick = onSubmit, modifier = Modifier.fillMaxWidth(), enabled = asunto.isNotBlank() && mensaje.isNotBlank(), colors = ButtonDefaults.buttonColors(containerColor = Orange500), shape = RoundedCornerShape(12.dp)) {
            Text("Enviar Consulta", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun ChatView(mensajes: List<SoporteMensaje>, asunto: String, estado: String, replyText: String, onReplyChange: (String) -> Unit, onSend: () -> Unit, onBack: () -> Unit, listState: androidx.compose.foundation.lazy.LazyListState) {
    Column(modifier = Modifier.fillMaxSize()) {
        // Header
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().background(Slate100).padding(8.dp)) {
            IconButton(onClick = onBack, modifier = Modifier.size(32.dp)) { Icon(Icons.Filled.ArrowBack, null, modifier = Modifier.size(16.dp)) }
            Column(modifier = Modifier.weight(1f)) {
                Text(asunto, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Carbon, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(estado, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Slate400)
            }
        }

        // Messages
        LazyColumn(modifier = Modifier.weight(1f).fillMaxWidth(), state = listState, contentPadding = PaddingValues(8.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            items(mensajes) { msg ->
                if (msg.esMio) {
                    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp), horizontalAlignment = Alignment.End) {
                        Surface(color = Orange500, shape = RoundedCornerShape(topStart = 14.dp, bottomStart = 14.dp, bottomEnd = 4.dp), modifier = Modifier.widthIn(max = 260.dp)) {
                            Text(msg.contenido, modifier = Modifier.padding(12.dp), fontSize = 13.sp, color = Color.White)
                        }
                    }
                } else {
                    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)) {
                        Box(Modifier.size(28.dp).clip(CircleShape).background(if (msg.remitente == "Sistema") Indigo500.copy(alpha = 0.1f) else Slate100), contentAlignment = Alignment.Center) {
                            Icon(if (msg.remitente == "Sistema") Icons.Filled.SmartToy else Icons.Filled.HeadsetMic, null, modifier = Modifier.size(14.dp), tint = if (msg.remitente == "Sistema") Indigo500 else Orange500)
                        }
                        Spacer(Modifier.width(8.dp))
                        Column {
                            Text(msg.remitente, fontSize = 9.sp, color = Slate400)
                            Surface(color = if (msg.remitente == "Sistema") Color(0xFFEEF2FF) else Color.White, shape = RoundedCornerShape(topStart = 4.dp, topEnd = 14.dp, bottomEnd = 14.dp), border = BorderStroke(1.dp, Slate200), modifier = Modifier.widthIn(max = 230.dp)) {
                                Text(msg.contenido, modifier = Modifier.padding(12.dp), fontSize = 13.sp, color = Carbon)
                            }
                        }
                    }
                }
            }
        }

        // Input
        if (estado != "CERRADO") {
            Surface(color = Color.White, shadowElevation = 2.dp) {
                Row(modifier = Modifier.padding(8.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = replyText, onValueChange = onReplyChange,
                        modifier = Modifier.weight(1f).height(48.dp),
                        singleLine = true, placeholder = { Text("Escribe un mensaje...") },
                        shape = RoundedCornerShape(24.dp), colors = OutlinedTextFieldDefaults.colors(unfocusedBorderColor = Slate300, focusedBorderColor = Orange500)
                    )
                    Spacer(Modifier.width(8.dp))
                    IconButton(onClick = onSend, enabled = replyText.isNotBlank(), modifier = Modifier.size(42.dp).clip(CircleShape).background(if (replyText.isNotBlank()) Orange500 else Slate200)) {
                        Icon(Icons.Filled.Send, null, tint = if (replyText.isNotBlank()) Color.White else Slate400, modifier = Modifier.size(18.dp))
                    }
                }
            }
        } else {
            Box(modifier = Modifier.fillMaxWidth().background(Slate100).padding(10.dp), contentAlignment = Alignment.Center) {
                Text("Este ticket ha sido cerrado.", fontSize = 11.sp, color = Slate400)
            }
        }
    }
}
