package com.agroconecta.app.data.model

data class ResenaGuardarRequest(
    val productoId: Long,
    val estrellas: Int,
    val comentario: String? = null
)
