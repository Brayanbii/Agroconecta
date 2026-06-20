package com.agroconecta.app.data.model

data class Direccion(
    val id: Long? = null,
    val alias: String? = null,
    val direccionCompleta: String? = null,
    val detalles: String? = null,
    val latitud: Double? = null,
    val longitud: Double? = null,
    val esPrincipal: Boolean? = false
)
