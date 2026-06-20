package com.agroconecta.app.data.model

data class MiPerfilResponse(
    val success: Boolean = false,
    val error: String? = null,
    val id: Long? = null,
    val nombreCompleto: String? = null,
    val userName: String? = null,
    val email: String? = null,
    val telefono: String? = null,
    val numeroIdentidad: String? = null,
    val nombreFinca: String? = null,
    val descripcionFinca: String? = null,
    val municipioOrigen: String? = null,
    val fotoPerfil: String? = null,
    val fotoFincaUrl: String? = null,
    val estadoVerificacion: String? = null,
    val latitud: Double? = null,
    val longitud: Double? = null,
    val fechaNacimiento: String? = null,
    val totalProductos: Int = 0
)
