package com.agroconectago.app.data.api

import com.agroconectago.app.data.model.DeliveryLoginResponse
import com.agroconectago.app.data.model.DeliveryProfileUpdateRequest
import com.agroconectago.app.data.model.DeliveryProfileResponse
import com.agroconectago.app.data.model.DeliveryRegisterRequest
import com.agroconectago.app.data.model.DeliveryRegisterResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Query

interface DeliveryApiService {

    @POST("api/usuarios/login")
    suspend fun login(
        @Body body: Map<String, String>
    ): DeliveryLoginResponse

    @POST("api/usuarios/registrar")
    suspend fun register(
        @Body request: DeliveryRegisterRequest
    ): DeliveryRegisterResponse

    @PUT("api/delivery/perfil")
    suspend fun updateProfile(
        @Body request: DeliveryProfileUpdateRequest
    ): DeliveryProfileResponse

    @GET("api/delivery/perfil")
    suspend fun getProfile(): DeliveryProfileResponse

    @GET("api/usuarios/check-email")
    suspend fun checkEmail(@Query("email") email: String): Map<String, Boolean>

    @GET("api/usuarios/check-username")
    suspend fun checkUsername(@Query("username") username: String): Map<String, Boolean>

    @POST("api/delivery/enviar-verificacion")
    suspend fun enviarVerificacion(): DeliveryProfileResponse

    // RUTAS
    @GET("api/rutas/disponibles")
    suspend fun rutasDisponibles(
        @Query("lat") lat: Double? = null,
        @Query("lng") lng: Double? = null,
        @Query("radioKm") radioKm: Double = 200.0
    ): Map<String, Any>

    @GET("api/rutas/{id}")
    suspend fun obtenerRuta(@retrofit2.http.Path("id") id: Long): Map<String, Any>

    @POST("api/rutas/{id}/aceptar")
    suspend fun aceptarRuta(@retrofit2.http.Path("id") id: Long): Map<String, Any>

    @POST("api/rutas/{id}/iniciar-viaje")
    suspend fun iniciarViaje(@retrofit2.http.Path("id") id: Long): Map<String, Any>

    @POST("api/rutas/{id}/recoger-pedido")
    suspend fun recogerPedido(@retrofit2.http.Path("id") id: Long): Map<String, Any>

    // PIN
    @POST("api/rutas/{id}/generar-pin-recogida")
    suspend fun generarPinRecogida(@retrofit2.http.Path("id") id: Long, @Body body: Map<String, String>): Map<String, Any>

    @POST("api/rutas/{id}/validar-pin-recogida")
    suspend fun validarPinRecogida(@retrofit2.http.Path("id") id: Long, @Body body: Map<String, String>): Map<String, Any>

    @POST("api/rutas/{id}/generar-pin-entrega")
    suspend fun generarPinEntrega(@retrofit2.http.Path("id") id: Long, @Body body: Map<String, String>): Map<String, Any>

    @POST("api/rutas/{id}/validar-pin-entrega")
    suspend fun validarPinEntrega(@retrofit2.http.Path("id") id: Long, @Body body: Map<String, String>): Map<String, Any>

    @POST("api/rutas/entrega/{ordenId}")
    suspend fun completarEntrega(@retrofit2.http.Path("ordenId") ordenId: Long): Map<String, Any>

    @GET("api/rutas/osrm-route")
    suspend fun getOsrmRoute(
        @Query("lat1") lat1: Double,
        @Query("lng1") lng1: Double,
        @Query("lat2") lat2: Double,
        @Query("lng2") lng2: Double
    ): Map<String, Any>

    @GET("api/rutas/mis-rutas")
    suspend fun misRutas(): Map<String, Any>

    // TRACKING GPS
    @POST("api/tracking/actualizar-ubicacion")
    suspend fun actualizarUbicacion(@Body body: Map<String, Double>): Map<String, Any>

    @POST("api/tracking/offline")
    suspend fun marcarOffline(): Map<String, Any>
}
