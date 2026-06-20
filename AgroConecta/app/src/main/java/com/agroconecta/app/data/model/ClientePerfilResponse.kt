package com.agroconecta.app.data.model

data class ClientePerfilResponse(
    val success: Boolean = false,
    val error: String? = null,
    val id: Long? = null,
    val nombreCompleto: String? = null,
    val email: String? = null,
    val userName: String? = null,
    val rol: String? = null,
    val telefono: String? = null,
    val numeroIdentidad: String? = null,
    val fechaNacimiento: String? = null,
    val genero: String? = null,
    val fotoPerfil: String? = null,
    val creditos: Double = 0.0,
    val estadoVerificacion: String? = null
)
