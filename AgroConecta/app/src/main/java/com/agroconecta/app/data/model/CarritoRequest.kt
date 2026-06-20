package com.agroconecta.app.data.model

data class AgregarCarritoRequest(
    val id: Long,
    val cantidad: Int = 1
)

data class ActualizarCarritoRequest(
    val id: Long,
    val cantidad: Int
)
