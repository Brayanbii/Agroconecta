package com.agroconecta.app.data.model

// Estructura de respuesta del inicio de sesión
data class LoginResponse(
    val success: Boolean,
    val message: String,
    val user: UsuarioInfo? = null
)

// Estructura interna con los datos seguros del usuario logueado
data class UsuarioInfo(
    val id: Long,
    val userName: String,
    val email: String,
    val rol: String
)