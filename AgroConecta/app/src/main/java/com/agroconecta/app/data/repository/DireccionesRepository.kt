package com.agroconecta.app.data.repository

import com.agroconecta.app.data.api.RetrofitClient
import com.agroconecta.app.data.model.Direccion

class DireccionesRepository {

    private val api = RetrofitClient.usuarioApiService

    suspend fun obtenerDireccionesUsuario(): List<Direccion> {
        return api.obtenerDirecciones()
    }

    suspend fun agregarDireccionUsuario(direccion: Direccion): Direccion {
        return api.agregarDireccion(direccion)
    }

    suspend fun marcarDireccionComoPrincipal(idDireccion: Long): Map<String, String> {
        return api.marcarDireccionPrincipal(idDireccion)
    }

    suspend fun eliminarDireccionUsuario(idDireccion: Long): Map<String, String> {
        return api.eliminarDireccion(idDireccion)
    }
}
