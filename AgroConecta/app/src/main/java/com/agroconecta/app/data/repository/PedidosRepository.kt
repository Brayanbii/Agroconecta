package com.agroconecta.app.data.repository

import com.agroconecta.app.data.api.RetrofitClient
import com.agroconecta.app.data.model.CrearOrdenRequest
import com.agroconecta.app.data.model.Pedido

class PedidosRepository {
    private val api = RetrofitClient.usuarioApiService

    suspend fun obtenerMisCompras(): Map<String, Any> = api.obtenerMisCompras()

    suspend fun crearOrden(propina: Double, direccion: String?, lat: Double?, lng: Double?): Map<String, Any> {
        return api.crearOrden(CrearOrdenRequest(propina = propina, direccionEnvio = direccion, latitud = lat, longitud = lng))
    }
}
