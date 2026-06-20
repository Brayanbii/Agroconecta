package com.agroconecta.app.data.model

data class FavoritoToggleResponse(
    val success: Boolean,
    val isFavorito: Boolean? = null,
    val message: String
)
