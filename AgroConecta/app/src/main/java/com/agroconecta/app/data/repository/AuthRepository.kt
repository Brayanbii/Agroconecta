package com.agroconecta.app.data.repository

import com.agroconecta.app.data.api.RetrofitClient
import com.agroconecta.app.data.model.LoginResponse
import com.agroconecta.app.data.model.Usuario

class AuthRepository {

    private val api = RetrofitClient.usuarioApiService

    suspend fun verificarEmail(email: String): Map<String, Boolean> {
        return api.checkEmail(email)
    }

    suspend fun verificarUsername(username: String): Map<String, Boolean> {
        return api.checkUsername(username)
    }

    suspend fun registrarUsuario(usuario: Usuario): Map<String, Any> {
        return api.registrarUsuario(usuario)
    }

    suspend fun loginUsuario(credenciales: Map<String, String>): LoginResponse {
        return api.loginUsuario(credenciales)
    }
}
