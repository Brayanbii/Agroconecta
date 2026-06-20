package com.agroconecta.app.data.model

data class Producto(
    val id: Long? = null,
    val nombre: String,
    val descripcion: String? = null,
    val precio: Double,
    val stock: Int,
    val imagenUrl: String? = "default.png",
    val imagenUrl2: String? = null,
    val imagenUrl3: String? = null,
    val imagenUrl4: String? = null,
    val categoria: String? = "Verduras y Hortalizas",
    val unidad: String? = "Kg",
    val latitudOrigen: Double? = null,
    val longitudOrigen: Double? = null,
    val municipioOrigen: String? = null,
    val distanciaKm: Double? = null,
    val fechaCreacion: String? = null,
    val promedioCalificacion: Double? = null,
    val totalResenas: Int? = null,
    val nombreCampesino: String? = null,
    val campesinoId: Long? = null,
    val nombreFinca: String? = null,
    val descripcionFinca: String? = null,
    val fotoPerfilCampesino: String? = null,
    val campesinoVerificado: Boolean = false,
    val esFavorito: Boolean = false
)
