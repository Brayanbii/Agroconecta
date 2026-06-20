package com.agroconecta.app.data.model

data class TicketSoporte(
    val id: Long? = null,
    val asunto: String = "",
    val estado: String = "ABIERTO",
    val fecha: String? = null
)

data class SoporteMensaje(
    val id: Long? = null,
    val contenido: String = "",
    val fecha: String? = null,
    val esMio: Boolean = false,
    val remitente: String = ""
)

data class CrearTicketResponse(
    val ticketId: Long? = null,
    val status: String? = null
)

data class StatusResponse(
    val status: String? = null
)
