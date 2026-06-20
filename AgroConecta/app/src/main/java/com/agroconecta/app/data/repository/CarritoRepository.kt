package com.agroconecta.app.data.repository

import com.agroconecta.app.data.api.RetrofitClient
import com.agroconecta.app.data.model.ActualizarCarritoRequest
import com.agroconecta.app.data.model.AgregarCarritoRequest
import com.agroconecta.app.data.model.CarritoAgregarResponse
import com.agroconecta.app.data.model.CarritoResponse

class CarritoRepository {

    private val api = RetrofitClient.usuarioApiService

    suspend fun agregarProductoAlCarrito(idProducto: Long, cantidad: Int = 1): CarritoAgregarResponse {
        return api.agregarAlCarrito(AgregarCarritoRequest(id = idProducto, cantidad = cantidad))
    }

    suspend fun obtenerCarrito(): CarritoResponse = api.obtenerCarrito()

    suspend fun actualizarCantidad(idProducto: Long, cantidad: Int): CarritoResponse =
        api.actualizarCantidadCarrito(ActualizarCarritoRequest(id = idProducto, cantidad = cantidad))

    suspend fun eliminarDelCarrito(idProducto: Long): CarritoResponse = api.eliminarDelCarrito(idProducto)

    suspend fun limpiarCarrito(): CarritoResponse = api.limpiarCarrito()
}
