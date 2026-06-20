package com.agroconecta.app.data.repository

import com.agroconecta.app.data.api.RetrofitClient
import com.agroconecta.app.data.model.BaseResponse
import com.agroconecta.app.data.model.CampesinoPerfil
import com.agroconecta.app.data.model.Producto

class ProductosRepository {

    private val api = RetrofitClient.usuarioApiService

    suspend fun obtenerProductosReales(): List<Producto> {
        return api.obtenerProductos()
    }

    suspend fun registrarProductoReal(producto: Producto): BaseResponse {
        return api.registrarProducto(producto)
    }

    suspend fun obtenerProductosPorCampesino(campesinoId: Long): List<Producto> {
        return api.obtenerProductosPorCampesino(campesinoId)
    }

    suspend fun obtenerMisProductos(): List<Producto> {
        return api.obtenerMisProductos()
    }

    suspend fun obtenerPerfilCampesino(campesinoId: Long): CampesinoPerfil {
        return api.obtenerPerfilCampesino(campesinoId)
    }
}
