package com.agroconecta.app.data.model

data class VerificacionResponse(
    val success: Boolean = false,
    val error: String? = null,
    val estado: String? = null,
    val numeroIdentidad: String? = null,
    val nombreFinca: String? = null,
    val fotoCedulaUrl: String? = null,
    val fotoFincaUrl: String? = null,
    val descripcionFinca: String? = null,
    val municipioOrigen: String? = null,
    val latitud: Double? = null,
    val longitud: Double? = null
)
