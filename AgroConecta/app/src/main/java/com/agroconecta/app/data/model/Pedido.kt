package com.agroconecta.app.data.model

data class PedidoItem(
    val nombre: String? = null,
    val precio: Double? = null,
    val cantidad: Int? = null,
    val total: Double? = null,
    val estado: String? = null,
    val imagenUrl: String? = null,
    val unidad: String? = null
)

data class Pedido(
    val id: Long? = null,
    val numeroOrden: String? = null,
    val fechaCreacion: String? = null,
    val total: Double? = null,
    val estado: String? = null,
    val direccionEnvio: String? = null,
    val items: List<PedidoItem>? = null
)
