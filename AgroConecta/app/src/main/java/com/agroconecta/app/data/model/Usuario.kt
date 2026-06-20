package com.agroconecta.app.data.model

// El modelo del usuario que viaja entre tu app y Spring Boot
data class Usuario(
    val id: Long? = null,
    val userName: String,
    val email: String,
    val nombreCompleto: String? = null,
    val telefono: String? = null,
    val rol: String? = null,
    val estadoVerificacion: String? = null,
    val password: String? = null
)