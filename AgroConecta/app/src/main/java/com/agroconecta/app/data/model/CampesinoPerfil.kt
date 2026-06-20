package com.agroconecta.app.data.model

data class CampesinoPerfil(
    val id: Long? = null,
    val nombreCompleto: String? = null,
    val nombreFinca: String? = null,
    val descripcionFinca: String? = null,
    val municipioOrigen: String? = null,
    val fotoPerfil: String? = null,
    val fotoFincaUrl: String? = null,
    val estadoVerificacion: String? = null,
    val totalProductos: Int = 0,
    val promedioCalificacion: Double = 0.0,
    val totalVendidos: Int = 0
)
