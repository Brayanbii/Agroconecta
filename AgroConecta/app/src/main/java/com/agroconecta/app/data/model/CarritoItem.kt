package com.agroconecta.app.data.model

data class CarritoItem(
    val id: Long? = null,
    val nombre: String? = null,
    val precio: Double? = null,
    val categoria: String? = null,
    val unidad: String? = null,
    val stock: Int? = null,
    val imagenUrl: String? = null,
    val cantidad: Int? = null,
    val total: Double? = null
)

data class CarritoResponse(
    val items: List<CarritoItem>? = null,
    val subtotal: Double? = null,
    val total: Double? = null,
    val cantidad: Int? = null,
    val status: String? = null,
    val error: String? = null,
    val cantidadItems: Int? = null
)
