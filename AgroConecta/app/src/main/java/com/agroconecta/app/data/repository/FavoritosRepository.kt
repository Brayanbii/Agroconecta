package com.agroconecta.app.data.repository

import com.agroconecta.app.data.api.RetrofitClient
import com.agroconecta.app.data.model.Producto

class FavoritosRepository {

    private val api = RetrofitClient.usuarioApiService

    suspend fun obtenerFavoritosUsuario(): List<Producto> {
        return api.obtenerFavoritos()
    }

    suspend fun toggleFavoritoProducto(idProducto: Long): Map<String, Any> {
        return api.toggleFavoritoProducto(idProducto)
    }
}
