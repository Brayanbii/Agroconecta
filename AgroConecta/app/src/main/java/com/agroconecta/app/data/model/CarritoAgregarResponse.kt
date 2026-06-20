package com.agroconecta.app.data.model

data class CarritoAgregarResponse(
    val status: String? = null,
    val mensaje: String? = null,
    val cantidadItems: Int? = null,
    val error: String? = null,
    val stock: Int? = null
)
