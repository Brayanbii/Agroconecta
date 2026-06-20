package com.agroconecta.app.data.repository

import com.agroconecta.app.data.api.RetrofitClient
import com.agroconecta.app.data.model.Resena
import com.agroconecta.app.data.model.ResenaGuardarRequest

class ResenasRepository {

    private val api = RetrofitClient.usuarioApiService

    suspend fun puedeComentar(productoId: Long): Map<String, Any> {
        return api.puedeComentar(productoId)
    }

    suspend fun obtenerResenasDeProducto(idProducto: Long): List<Resena> {
        return api.obtenerResenasPorProducto(idProducto)
    }

    suspend fun guardarResenaProducto(
        idProducto: Long,
        estrellas: Int,
        comentario: String? = null
    ): Map<String, Any> {
        val payload = ResenaGuardarRequest(
            productoId = idProducto,
            estrellas = estrellas,
            comentario = comentario
        )
        return api.guardarResena(payload)
    }

    suspend fun eliminarResena(idResena: Long): Map<String, Any> {
        return api.eliminarResena(idResena)
    }
}
