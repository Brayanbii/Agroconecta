package com.agroconecta.app.data.model

// Modelo genérico para respuestas simples (como el registro exitoso/fallido)
data class BaseResponse(
    val success: Boolean,
    val message: String
)