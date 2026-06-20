package com.agroconectago.app.data.repository

import com.agroconectago.app.data.api.DeliveryRetrofitClient
import com.agroconectago.app.data.model.DeliveryLoginResponse
import com.agroconectago.app.data.model.DeliveryProfileUpdateRequest
import com.agroconectago.app.data.model.DeliveryProfileResponse
import com.agroconectago.app.data.model.DeliveryRegisterRequest
import com.agroconectago.app.data.model.DeliveryRegisterResponse

class DeliveryAuthRepository {
    private val api = DeliveryRetrofitClient.api

    suspend fun login(email: String, password: String): DeliveryLoginResponse {
        return api.login(mapOf("email" to email, "password" to password))
    }

    suspend fun register(request: DeliveryRegisterRequest): DeliveryRegisterResponse {
        return api.register(request)
    }

    suspend fun updateProfile(request: DeliveryProfileUpdateRequest): DeliveryProfileResponse {
        return api.updateProfile(request)
    }

    suspend fun getProfile(): DeliveryProfileResponse {
        return api.getProfile()
    }

    suspend fun checkEmail(email: String): Boolean {
        return api.checkEmail(email)["exists"] ?: false
    }

    suspend fun checkUsername(username: String): Boolean {
        return api.checkUsername(username)["exists"] ?: false
    }
}
