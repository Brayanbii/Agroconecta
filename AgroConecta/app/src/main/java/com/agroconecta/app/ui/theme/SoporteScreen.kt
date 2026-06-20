package com.agroconecta.app.ui.theme

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.agroconecta.app.data.model.SoporteMensaje
import com.agroconecta.app.data.model.TicketSoporte
import com.agroconecta.app.viewmodel.TiendaViewModel
import kotlinx.coroutines.delay

private val Carbon = Color(0xFF111827)
private val Slate400 = Color(0xFF94A3B8)
private val Slate500 = Color(0xFF64748B)
private val Slate300 = Color(0xFFCBD5E1)
private val Slate200 = Color(0xFFE2E8F0)
private val Slate100 = Color(0xFFF1F5F9)
private val AppBackground = Color(0xFFF8FAF9)
private val Orange500 = Color(0xFFF97316)
private val Orange400 = Color(0xFFFB923C)
private val Green500 = Color(0xFF22C55E)
private val Blue500 = Color(0xFF3B82F6)
private val Red500 = Color(0xFFEF4444)
private val Indigo500 = Color(0xFF6366F1)
private val Indigo600 = Color(0xFF4F46E5)
private val Purple500 = Color(0xFF8B5CF6)
private val Teal500 = Color(0xFF14B8A6)

private enum class SoporteView { FAQ, TICKETS, NEW, CHAT }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SoporteScreen(tiendaVM: TiendaViewModel, onNavigateBack: () -> Unit) {
    var currentView by remember { mutableStateOf(SoporteView.FAQ) }
    var nuevoAsunto by remember { mutableStateOf("") }
    var nuevoMensaje by remember { mutableStateOf("") }
    var replyText by remember { mutableStateOf("") }
    var ticketAsunto by remember { mutableStateOf("") }
    var ticketEstado by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    var isVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { isVisible = true }

    LaunchedEffect(Unit) { tiendaVM.cargarMisTickets() }

    LaunchedEffect(tiendaVM.soporteTicketActivoId) {
        while (tiendaVM.soporteTicketActivoId != null) {
            tiendaVM.cargarMensajesTicket(tiendaVM.soporteTicketActivoId!!)
            delay(5000)
        }
    }

    LaunchedEffect(tiendaVM.soporteMensajes.size) {
        if (tiendaVM.soporteMensajes.isNotEmpty() && currentView == SoporteView.CHAT) {
            delay(100)
            listState.animateScrollToItem(tiendaVM.soporteMensajes.size - 1)
        }
    }

    Scaffold(
        containerColor = AppBackground,
        topBar = {
            AnimatedVisibility(
                visible = isVisible,
                enter = fadeIn(tween(400)) + slideInVertically(tween(400, easing = EaseOutCubic), initialOffsetY = { -it / 4 })
            ) {
                Surface(
                    color = Color.White,
                    shadowElevation = 0.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .statusBarsPadding()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        var backScale by remember { mutableStateOf(1f) }
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(Slate100)
                                .graphicsLayer { scaleX = backScale; scaleY = backScale }
                                .clickable {
                                    backScale = 0.9f
                                    if (currentView == SoporteView.CHAT || currentView == SoporteView.TICKETS) {
                                        tiendaVM.soporteTicketActivoId = null
                                        currentView = SoporteView.FAQ
                                    } else onNavigateBack()
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Carbon, modifier = Modifier.size(22.dp))
                        }
                        LaunchedEffect(backScale) {
                            if (backScale != 1f) {
                                kotlinx.coroutines.delay(100)
                                backScale = 1f
                            }
                        }
                        Spacer(Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "AgroSoporte",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = Carbon,
                                letterSpacing = (-0.5).sp
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                val infiniteTransition = rememberInfiniteTransition(label = "topbar")
                                val pulseAnim by infiniteTransition.animateFloat(
                                    initialValue = 0.6f,
                                    targetValue = 1f,
                                    animationSpec = infiniteRepeatable(tween(1500), RepeatMode.Reverse)
                                )
                                Box(
                                    modifier = Modifier
                                        .size(7.dp)
                                        .clip(CircleShape)
                                        .background(Orange500.copy(alpha = pulseAnim))
                                )
                                Spacer(Modifier.width(6.dp))
                                Text(
                                    "Soporte activo",
                                    fontSize = 12.sp,
                                    color = Slate500,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                        if (currentView == SoporteView.FAQ || currentView == SoporteView.TICKETS) {
                            var btnScale by remember { mutableStateOf(1f) }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(
                                        Brush.linearGradient(
                                            colors = listOf(Orange500, Orange400)
                                        )
                                    )
                                    .graphicsLayer { scaleX = btnScale; scaleY = btnScale }
                                    .clickable {
                                        btnScale = 0.95f
                                        nuevoAsunto = ""
                                        nuevoMensaje = ""
                                        currentView = SoporteView.NEW
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "Nuevo",
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                            LaunchedEffect(btnScale) {
                                if (btnScale != 1f) {
                                    kotlinx.coroutines.delay(100)
                                    btnScale = 1f
                                }
                            }
                        }
                    }
                }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            item {
                val infiniteTransition = rememberInfiniteTransition(label = "hero")
                val orbX by infiniteTransition.animateFloat(
                    initialValue = 0f,
                    targetValue = 20f,
                    animationSpec = infiniteRepeatable(tween(4000, easing = EaseInOutCubic), RepeatMode.Reverse),
                    label = "orbX"
                )
                val orbY by infiniteTransition.animateFloat(
                    initialValue = 0f,
                    targetValue = 15f,
                    animationSpec = infiniteRepeatable(tween(3500, easing = EaseInOutCubic), RepeatMode.Reverse),
                    label = "orbY"
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.linearGradient(
                                colors = listOf(Color(0xFF1E1B4B), Color(0xFF312E81), Color(0xFF4C1D95)),
                                start = Offset(0f, 0f),
                                end = Offset(1f, 1f)
                            )
                        )
                        .padding(24.dp)
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        drawCircle(
                            color = Color.White.copy(alpha = 0.04f),
                            radius = size.width * 0.35f,
                            center = Offset(size.width * 0.8f + orbX, size.height * 0.3f + orbY)
                        )
                        drawCircle(
                            color = Color.White.copy(alpha = 0.03f),
                            radius = size.width * 0.22f,
                            center = Offset(size.width * 0.15f - orbX * 0.5f, size.height * 0.7f - orbY * 0.3f)
                        )
                        drawCircle(
                            color = Color.White.copy(alpha = 0.02f),
                            radius = size.width * 0.15f,
                            center = Offset(size.width * 0.5f + orbX * 0.3f, size.height * 0.1f)
                        )
                    }

                    Column {
                        Surface(
                            color = Color.White.copy(alpha = 0.12f),
                            shape = RoundedCornerShape(20.dp),
                            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.2f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                val dotPulse by infiniteTransition.animateFloat(
                                    initialValue = 0.5f,
                                    targetValue = 1f,
                                    animationSpec = infiniteRepeatable(tween(1200), RepeatMode.Reverse),
                                    label = "dotPulse"
                                )
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFFB923C).copy(alpha = dotPulse))
                                )
                                Spacer(Modifier.width(8.dp))
                                Text("Centro de Ayuda", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White.copy(alpha = 0.95f))
                            }
                        }
                        Spacer(Modifier.height(16.dp))
                        Text(
                            "Hola, en que te ayudamos?",
                            fontSize = 30.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White,
                            letterSpacing = (-0.8).sp
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "Nuestro equipo de soporte esta listo para ayudarte.",
                            fontSize = 14.sp,
                            color = Color.White.copy(alpha = 0.75f),
                            lineHeight = 20.sp
                        )
                    }
                }
            }

            item {
                when (currentView) {
                    SoporteView.FAQ -> SoporteFaqView(
                        onMisTickets = { currentView = SoporteView.TICKETS },
                        onNuevo = { nuevoAsunto = ""; nuevoMensaje = ""; currentView = SoporteView.NEW }
                    )
                    SoporteView.TICKETS -> SoporteTicketsView(
                        tickets = tiendaVM.soporteTickets,
                        onOpen = { t ->
                            val tid = t.id
                            if (tid != null) {
                                tiendaVM.soporteTicketActivoId = tid
                                ticketAsunto = t.asunto
                                ticketEstado = t.estado
                                tiendaVM.cargarMensajesTicket(tid)
                                currentView = SoporteView.CHAT
                            }
                        },
                        onNuevo = { currentView = SoporteView.NEW }
                    )
                    SoporteView.NEW -> SoporteNewTicketView(
                        asunto = nuevoAsunto, onAsuntoChange = { nuevoAsunto = it },
                        mensaje = nuevoMensaje, onMensajeChange = { nuevoMensaje = it },
                        onSubmit = {
                            tiendaVM.crearTicketSoporte(nuevoAsunto, nuevoMensaje) { id ->
                                if (id != null) {
                                    tiendaVM.soporteTicketActivoId = id
                                    ticketAsunto = nuevoAsunto; ticketEstado = "ABIERTO"
                                    tiendaVM.cargarMensajesTicket(id)
                                    currentView = SoporteView.CHAT
                                }
                            }
                        },
                        onBack = { currentView = SoporteView.TICKETS }
                    )
                    SoporteView.CHAT -> SoporteChatView(
                        mensajes = tiendaVM.soporteMensajes,
                        asunto = ticketAsunto, estado = ticketEstado,
                        replyText = replyText, onReplyChange = { replyText = it },
                        onSend = {
                            tiendaVM.soporteTicketActivoId?.let {
                                tiendaVM.enviarMensajeSoporte(it, replyText) { replyText = "" }
                            }
                        },
                        onBack = {
                            tiendaVM.soporteTicketActivoId = null
                            currentView = SoporteView.TICKETS
                        },
                        listState = listState
                    )
                }
            }
        }
    }
}

@Composable
private fun SoporteFaqView(onMisTickets: () -> Unit, onNuevo: () -> Unit) {
    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Preguntas Frecuentes", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Carbon, letterSpacing = (-0.3.sp))
            Text("Respuestas rapidas a las dudas mas comunes", fontSize = 13.sp, color = Slate500)
        }
        SoporteFaqItem("Mi pedido no llego, que hago?", "Verifica el estado en Mis Compras. Si aparece ENVIADO y pasaron los dias habiles, abre una consulta y te ayudamos.")
        SoporteFaqItem("Quiero cancelar una compra", "Puedes cancelar si el estado es PENDIENTE. Si fue PAGADO o ENVIADO, abre un ticket para solicitar reembolso.")
        SoporteFaqItem("El campesino no responde", "Los campesinos pueden tardar hasta 24h. Si no obtienes respuesta, escalamos el caso.")
        SoporteFaqItem("Mi pago fue rechazado", "Suele pasar por fondos insuficientes o bloqueos bancarios. El dinero no fue descontado.")
        SoporteFaqItem("No puedo abrir mi cuenta (Bloqueado)", "Si fuiste vetado, abre un ticket para revisar tu caso y solicitar reactivacion.")
        SoporteFaqItem("Como retirar mi dinero?", "Los pagos son procesados quincenalmente hacia Mercado Pago. Revisa AgroWallet.")
        Spacer(Modifier.height(4.dp))
        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            PremiumOutlinedButton(
                onClick = onMisTickets,
                modifier = Modifier.weight(1f).height(50.dp)
            ) {
                Icon(Icons.Filled.Forum, null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Mis Consultas", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
            PremiumActionButton(
                onClick = onNuevo,
                modifier = Modifier.weight(1f).height(50.dp)
            ) {
                Icon(Icons.Filled.HeadsetMic, null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Contactar Soporte", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
        }
        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun SoporteFaqItem(q: String, a: String) {
    var expanded by remember { mutableStateOf(false) }
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        if (isPressed) 0.98f else 1f,
        spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow)
    )

    Surface(
        color = if (expanded) Color(0xFFFFF7ED) else Color.White,
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, if (expanded) Orange500.copy(alpha = 0.3f) else Slate200),
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .clickable(interactionSource = interactionSource, indication = null) { expanded = !expanded }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (expanded) Brush.linearGradient(listOf(Orange500, Orange400))
                            else Brush.linearGradient(listOf(Slate100, Color.White))
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Filled.HelpOutline,
                        null,
                        tint = if (expanded) Color.White else Orange500,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(Modifier.width(12.dp))
                Text(q, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Carbon, modifier = Modifier.weight(1f), letterSpacing = (-0.2.sp))
                Icon(
                    if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                    null,
                    tint = if (expanded) Orange500 else Slate400,
                    modifier = Modifier.size(22.dp)
                )
            }
            AnimatedVisibility(expanded) {
                Text(a, fontSize = 13.sp, color = Slate500, modifier = Modifier.padding(top = 12.dp, start = 48.dp), lineHeight = 19.sp)
            }
        }
    }
}

@Composable
private fun SoporteTicketsView(tickets: List<TicketSoporte>, onOpen: (TicketSoporte) -> Unit, onNuevo: () -> Unit) {
    if (tickets.isEmpty()) {
        Box(Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                val infiniteTransition = rememberInfiniteTransition(label = "empty")
                val floatY by infiniteTransition.animateFloat(
                    initialValue = -6f,
                    targetValue = 6f,
                    animationSpec = infiniteRepeatable(tween(2500, easing = EaseInOutCubic), RepeatMode.Reverse),
                    label = "floatY"
                )
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(Orange500.copy(alpha = 0.06f), Color.Transparent)
                            )
                        )
                        .graphicsLayer { translationY = floatY },
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(Slate100, Color.White)
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Filled.Inbox, null, tint = Slate400, modifier = Modifier.size(36.dp))
                    }
                }
                Spacer(Modifier.height(24.dp))
                Text("No tienes consultas activas", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Carbon, letterSpacing = (-0.3.sp))
                Spacer(Modifier.height(8.dp))
                Text("Crea una nueva consulta para recibir ayuda", fontSize = 14.sp, color = Slate500)
                Spacer(Modifier.height(24.dp))
                PremiumActionButton(
                    onClick = onNuevo,
                    modifier = Modifier.height(50.dp).padding(horizontal = 40.dp)
                ) {
                    Icon(Icons.Filled.Add, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Crear nueva consulta", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }
        }
    } else {
        Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("Tus Consultas", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Carbon, letterSpacing = (-0.3.sp), modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp))
            tickets.forEach { t ->
                val statusColor = when (t.estado) { "ABIERTO" -> Red500; "EN_PROGRESO" -> Blue500; else -> Slate400 }
                val interactionSource = remember { MutableInteractionSource() }
                val isPressed by interactionSource.collectIsPressedAsState()
                val scale by animateFloatAsState(
                    if (isPressed) 0.98f else 1f,
                    spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow)
                )

                Surface(
                    color = Color.White,
                    shape = RoundedCornerShape(24.dp),
                    shadowElevation = 0.dp,
                    border = BorderStroke(1.dp, Slate200),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 4.dp)
                        .graphicsLayer { scaleX = scale; scaleY = scale }
                        .clickable(interactionSource = interactionSource, indication = null) { onOpen(t) }
                ) {
                    Row(modifier = Modifier.padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            Modifier.size(48.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(
                                    Brush.linearGradient(listOf(statusColor.copy(alpha = 0.15f), statusColor.copy(alpha = 0.05f)))
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Filled.ChatBubble, null, tint = statusColor, modifier = Modifier.size(22.dp))
                        }
                        Spacer(Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(t.asunto, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Carbon, maxLines = 1, overflow = TextOverflow.Ellipsis, letterSpacing = (-0.2.sp))
                            Spacer(Modifier.height(4.dp))
                            Text(t.fecha?.take(10) ?: "", fontSize = 12.sp, color = Slate400, fontWeight = FontWeight.Medium)
                        }
                        Surface(
                            color = statusColor.copy(alpha = 0.12f),
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, statusColor.copy(alpha = 0.25f))
                        ) {
                            Text(t.estado, modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = statusColor)
                        }
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun SoporteNewTicketView(asunto: String, onAsuntoChange: (String) -> Unit, mensaje: String, onMensajeChange: (String) -> Unit, onSubmit: () -> Unit, onBack: () -> Unit) {
    Column(modifier = Modifier.fillMaxWidth().padding(24.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier.size(46.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Brush.linearGradient(listOf(Orange500, Orange400))),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.Edit, null, tint = Color.White, modifier = Modifier.size(22.dp))
            }
            Spacer(Modifier.width(14.dp))
            Text("Crear nueva consulta", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Carbon, letterSpacing = (-0.3.sp))
        }
        Spacer(Modifier.height(24.dp))
        Text("Asunto", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Slate500, letterSpacing = 0.3.sp)
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = asunto,
            onValueChange = onAsuntoChange,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
            placeholder = { Text("Ej. Problema con mi pedido", color = Slate400) },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Orange500,
                unfocusedBorderColor = Slate200,
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Slate100.copy(alpha = 0.3f)
            )
        )
        Spacer(Modifier.height(18.dp))
        Text("Mensaje Inicial", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Slate500, letterSpacing = 0.3.sp)
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = mensaje,
            onValueChange = onMensajeChange,
            modifier = Modifier.fillMaxWidth().height(130.dp),
            maxLines = 5,
            shape = RoundedCornerShape(16.dp),
            placeholder = { Text("Describe tu problema con detalle...", color = Slate400) },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Orange500,
                unfocusedBorderColor = Slate200,
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Slate100.copy(alpha = 0.3f)
            )
        )
        Spacer(Modifier.height(24.dp))
        PremiumActionButton(
            onClick = onSubmit,
            enabled = asunto.isNotBlank() && mensaje.isNotBlank(),
            modifier = Modifier.fillMaxWidth().height(54.dp)
        ) {
            Icon(Icons.AutoMirrored.Filled.Send, null, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(10.dp))
            Text("Enviar Consulta", fontWeight = FontWeight.Bold, fontSize = 15.sp)
        }
        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun SoporteChatView(mensajes: List<SoporteMensaje>, asunto: String, estado: String, replyText: String, onReplyChange: (String) -> Unit, onSend: () -> Unit, onBack: () -> Unit, listState: androidx.compose.foundation.lazy.LazyListState) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Surface(
            color = Color.White,
            shape = RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp),
            shadowElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier.size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            Brush.linearGradient(listOf(Orange500, Orange400))
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.HeadsetMic, null, tint = Color.White, modifier = Modifier.size(20.dp))
                }
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(asunto, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Carbon, maxLines = 1, overflow = TextOverflow.Ellipsis, letterSpacing = (-0.2.sp))
                    Spacer(Modifier.height(2.dp))
                    Text(estado, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Slate500)
                }
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxWidth().height(500.dp),
            state = listState,
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(mensajes) { msg ->
                if (msg.esMio) {
                    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.End) {
                        Surface(
                            color = Orange500,
                            shape = RoundedCornerShape(topStart = 18.dp, bottomStart = 18.dp, bottomEnd = 6.dp),
                            modifier = Modifier.widthIn(max = 280.dp)
                        ) {
                            Text(msg.contenido, modifier = Modifier.padding(16.dp), fontSize = 14.sp, color = Color.White, lineHeight = 20.sp)
                        }
                        Text(msg.fecha?.take(16)?.replace("T", " ") ?: "", fontSize = 10.sp, color = Slate400, modifier = Modifier.padding(end = 4.dp, top = 4.dp))
                    }
                } else {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(
                                    if (msg.remitente == "Sistema")
                                        Brush.linearGradient(listOf(Indigo500.copy(alpha = 0.2f), Indigo500.copy(alpha = 0.1f)))
                                    else
                                        Brush.linearGradient(listOf(Orange500.copy(alpha = 0.15f), Orange500.copy(alpha = 0.05f)))
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                if (msg.remitente == "Sistema") Icons.Filled.SmartToy else Icons.Filled.HeadsetMic,
                                null,
                                modifier = Modifier.size(18.dp),
                                tint = if (msg.remitente == "Sistema") Indigo500 else Orange500
                            )
                        }
                        Spacer(Modifier.width(10.dp))
                        Column {
                            Text(msg.remitente, fontSize = 12.sp, color = Slate500, fontWeight = FontWeight.SemiBold)
                            Spacer(Modifier.height(4.dp))
                            Surface(
                                color = if (msg.remitente == "Sistema") Color(0xFFEEF2FF) else Color.White,
                                shape = RoundedCornerShape(topStart = 6.dp, topEnd = 18.dp, bottomEnd = 18.dp),
                                border = BorderStroke(1.dp, Slate200),
                                modifier = Modifier.widthIn(max = 260.dp)
                            ) {
                                Text(msg.contenido, modifier = Modifier.padding(16.dp), fontSize = 14.sp, color = Carbon, lineHeight = 20.sp)
                            }
                            Text(msg.fecha?.take(16)?.replace("T", " ") ?: "", fontSize = 10.sp, color = Slate400, modifier = Modifier.padding(start = 4.dp, top = 4.dp))
                        }
                    }
                }
            }
        }

        if (estado != "CERRADO") {
            Surface(
                color = Color.White,
                shadowElevation = 4.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(modifier = Modifier.padding(12.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = replyText,
                        onValueChange = onReplyChange,
                        modifier = Modifier.weight(1f).height(52.dp),
                        singleLine = true,
                        placeholder = { Text("Escribe un mensaje...", color = Slate400) },
                        shape = RoundedCornerShape(26.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = Slate200,
                            focusedBorderColor = Orange500,
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Slate100.copy(alpha = 0.3f)
                        )
                    )
                    Spacer(Modifier.width(10.dp))
                    var sendScale by remember { mutableStateOf(1f) }
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(
                                if (replyText.isNotBlank())
                                    Brush.linearGradient(listOf(Orange500, Orange400))
                                else
                                    Brush.linearGradient(listOf(Slate200, Slate300))
                            )
                            .graphicsLayer { scaleX = sendScale; scaleY = sendScale }
                            .clickable(enabled = replyText.isNotBlank()) {
                                sendScale = 0.9f
                                onSend()
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.Send,
                            null,
                            tint = if (replyText.isNotBlank()) Color.White else Slate400,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    LaunchedEffect(sendScale) {
                        if (sendScale != 1f) {
                            kotlinx.coroutines.delay(100)
                            sendScale = 1f
                        }
                    }
                }
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Slate100)
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Lock, null, tint = Slate400, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Este ticket ha sido cerrado.", fontSize = 13.sp, color = Slate500, fontWeight = FontWeight.Medium)
                }
            }
        }
    }
}

@Composable
private fun PremiumOutlinedButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable RowScope.() -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        if (isPressed) 0.96f else 1f,
        spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow)
    )

    OutlinedButton(
        onClick = onClick,
        modifier = modifier.graphicsLayer { scaleX = scale; scaleY = scale },
        enabled = enabled,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.5.dp, Orange500),
        interactionSource = interactionSource
    ) {
        content()
    }
}

@Composable
private fun PremiumActionButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable RowScope.() -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        if (isPressed) 0.97f else 1f,
        spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow)
    )

    Button(
        onClick = onClick,
        modifier = modifier.graphicsLayer { scaleX = scale; scaleY = scale },
        enabled = enabled,
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Orange500,
            contentColor = Color.White
        ),
        interactionSource = interactionSource
    ) {
        content()
    }
}