package com.agroconecta.app.data.model

data class CrearOrdenRequest(
    val propina: Double = 0.0,
    val direccionEnvio: String? = null,
    val latitud: Double? = null,
    val longitud: Double? = null,
    val tipoEnvio: String = "ECONOMICO"
)
