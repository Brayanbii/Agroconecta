package com.agroconecta.app.data.model

data class ReputacionResponse(
    val success: Boolean = false,
    val error: String? = null,
    val calificacionGeneral: Double = 0.0,
    val totalResenas: Int = 0,
    val porcentajePositivo: Int = 0,
    val productoEstrella: String = "—",
    val oportunidadesCount: Int = 0,
    val likesPerfil: Int = 0,
    val likesProductos: Int = 0,
    val distribucion: List<Int>? = null,
    val mejoresProductos: List<ProductoPodium>? = null,
    val buenasResenas: List<ResenaReputacion>? = null,
    val oportunidadesMejora: List<ResenaReputacion>? = null
)

data class ProductoPodium(
    val id: Long? = null,
    val nombre: String = "",
    val promedioCalificacion: Double = 0.0,
    val imagenUrl: String? = null,
    val totalResenas: Int? = null
)

data class ResenaReputacion(
    val id: Long? = null,
    val estrellas: Int = 0,
    val comentario: String? = null,
    val fecha: String? = null,
    val nombreAutor: String? = null,
    val nombreProducto: String? = null
)
