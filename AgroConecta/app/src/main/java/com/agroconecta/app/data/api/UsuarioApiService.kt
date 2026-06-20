package com.agroconecta.app.data.api

import com.agroconecta.app.data.model.AgregarCarritoRequest
import com.agroconecta.app.data.model.ActualizarCarritoRequest
import com.agroconecta.app.data.model.BaseResponse
import com.agroconecta.app.data.model.CampesinoPerfil
import com.agroconecta.app.data.model.CarritoAgregarResponse
import com.agroconecta.app.data.model.CarritoResponse
import com.agroconecta.app.data.model.CrearOrdenRequest
import com.agroconecta.app.data.model.Direccion
import com.agroconecta.app.data.model.FavoritoToggleResponse
import com.agroconecta.app.data.model.InformeResponse
import com.agroconecta.app.data.model.FinanzasResponse
import com.agroconecta.app.data.model.LogisticaResponse
import com.agroconecta.app.data.model.LoginResponse
import com.agroconecta.app.data.model.MiPerfilResponse
import com.agroconecta.app.data.model.Pedido
import com.agroconecta.app.data.model.PedidosResponse
import com.agroconecta.app.data.model.VerificacionResponse
import com.agroconecta.app.data.model.Producto
import com.agroconecta.app.data.model.ReputacionResponse
import com.agroconecta.app.data.model.Resena
import com.agroconecta.app.data.model.ResenaGuardarRequest
import com.agroconecta.app.data.model.SoporteMensaje
import com.agroconecta.app.data.model.StatusResponse
import com.agroconecta.app.data.model.TicketSoporte
import com.agroconecta.app.data.model.ClientePerfilResponse
import com.agroconecta.app.data.model.CrearTicketResponse
import com.agroconecta.app.data.model.Usuario
import retrofit2.http.*

interface UsuarioApiService {

    @GET("api/usuarios/check-email")
    suspend fun checkEmail(
        @Query("email") email: String
    ): Map<String, Boolean>

    @GET("api/usuarios/check-username")
    suspend fun checkUsername(
        @Query("username") username: String
    ): Map<String, Boolean>

    @POST("api/usuarios/registrar")
    suspend fun registrarUsuario(
        @Body usuario: Usuario
    ): Map<String, Any>

    @POST("api/usuarios/login")
    suspend fun loginUsuario(
        @Body credenciales: Map<String, String>
    ): LoginResponse

    // ---- ENDPOINTS DE PRODUCTOS REALES ----

    @GET("api/productos")
    suspend fun obtenerProductos(): List<Producto>

    @GET("api/productos/campesino/{id}")
    suspend fun obtenerProductosPorCampesino(
        @Path("id") campesinoId: Long
    ): List<Producto>

    @GET("api/productos/mis-productos")
    suspend fun obtenerMisProductos(): List<Producto>

    @GET("api/campesino/{id}/perfil")
    suspend fun obtenerPerfilCampesino(
        @Path("id") campesinoId: Long
    ): CampesinoPerfil

    @POST("api/productos/registrar")
    suspend fun registrarProducto(
        @Body producto: Producto
    ): BaseResponse

    // ---- CRUD PRODUCTOS ----

    @GET("api/productos/{id}")
    suspend fun obtenerProducto(
        @Path("id") productoId: Long
    ): Producto

    @PUT("api/productos/{id}")
    suspend fun actualizarProducto(
        @Path("id") productoId: Long,
        @Body producto: Producto
    ): Map<String, Any>

    @DELETE("api/productos/{id}")
    suspend fun eliminarProducto(
        @Path("id") productoId: Long
    ): Map<String, Any>

    @Multipart
    @POST("api/productos/upload-image")
    suspend fun subirImagen(
        @Part file: okhttp3.MultipartBody.Part
    ): Map<String, Any>

    // ---- ENDPOINTS DE FAVORITOS ----

    @GET("api/favoritos")
    suspend fun obtenerFavoritos(): List<Producto>

    @POST("api/favoritos/toggle/{id}")
    suspend fun toggleFavoritoProducto(
        @Path("id") idProducto: Long
    ): Map<String, Any>

    // ---- ENDPOINTS DE DIRECCIONES ----

    @GET("api/direcciones")
    suspend fun obtenerDirecciones(): List<Direccion>

    @POST("api/direcciones")
    suspend fun agregarDireccion(
        @Body direccion: Direccion
    ): Direccion

    @PUT("api/direcciones/{id}/principal")
    suspend fun marcarDireccionPrincipal(
        @Path("id") idDireccion: Long
    ): Map<String, String>

    @DELETE("api/direcciones/{id}")
    suspend fun eliminarDireccion(
        @Path("id") idDireccion: Long
    ): Map<String, String>

    // ---- ENDPOINTS DE CARRITO ----

    @GET("api/carrito")
    suspend fun obtenerCarrito(): CarritoResponse

    @PUT("api/carrito/actualizar")
    suspend fun actualizarCantidadCarrito(
        @Body payload: ActualizarCarritoRequest
    ): CarritoResponse

    @DELETE("api/carrito/eliminar/{id}")
    suspend fun eliminarDelCarrito(
        @Path("id") idProducto: Long
    ): CarritoResponse

    @POST("api/carrito/limpiar")
    suspend fun limpiarCarrito(): CarritoResponse

    @POST("api/carrito/agregar")
    suspend fun agregarAlCarrito(
        @Body payload: AgregarCarritoRequest
    ): CarritoAgregarResponse

    // ---- ENDPOINTS DE RESEÑAS ----

    @GET("api/resenas/puede-comentar/{productoId}")
    suspend fun puedeComentar(
        @Path("productoId") productoId: Long
    ): Map<String, Any>

    @GET("api/resenas/producto/{id}")
    suspend fun obtenerResenasPorProducto(
        @Path("id") idProducto: Long
    ): List<Resena>

    @POST("api/resenas")
    suspend fun guardarResena(
        @Body request: ResenaGuardarRequest
    ): Map<String, Any>

    @DELETE("api/resenas/{id}")
    suspend fun eliminarResena(
        @Path("id") idResena: Long
    ): Map<String, Any>

    // ---- ENDPOINTS SIPSA ----

    @GET("api/sipsa/catalogo")
    suspend fun obtenerCatalogoSipsa(): List<Map<String, Any>>

    @GET("api/sipsa/precio")
    suspend fun consultarPrecioSipsa(
        @Query("producto") producto: String
    ): Map<String, Any>

    // ---- ENDPOINTS DE PEDIDOS ----

    @GET("api/ordenes/mis-compras")
    suspend fun obtenerMisCompras(): Map<String, Any>

    @POST("api/ordenes/crear")
    suspend fun crearOrden(
        @Body body: CrearOrdenRequest
    ): Map<String, Any>

    @POST("api/productos/{id}/actualizar-stock")
    suspend fun actualizarStockProducto(
        @Path("id") productoId: Long,
        @Body body: Map<String, @JvmSuppressWildcards Any>
    ): Map<String, Any>

    @GET("api/analiticas/informe")
    suspend fun obtenerInformeCampesino(): InformeResponse

    @GET("api/reputacion/informe")
    suspend fun obtenerReputacionCampesino(): ReputacionResponse

    @GET("api/pedidos/mis-ventas")
    suspend fun obtenerMisVentas(): PedidosResponse

    @POST("api/pedidos/{id}/estado")
    suspend fun cambiarEstadoPedido(
        @Path("id") pedidoId: Long,
        @Body body: Map<String, @JvmSuppressWildcards Any>
    ): Map<String, Any>

    @GET("api/pedidos/logistica/{id}")
    suspend fun obtenerLogisticaPedido(
        @Path("id") pedidoId: Long
    ): LogisticaResponse

    @GET("api/campesino/mi-perfil")
    suspend fun obtenerMiPerfil(): MiPerfilResponse

    @PUT("api/campesino/mi-perfil")
    suspend fun actualizarMiPerfil(
        @Body body: Map<String, @JvmSuppressWildcards Any>
    ): Map<String, Any>

    @GET("api/finanzas/informe")
    suspend fun obtenerFinanzas(): FinanzasResponse

    @GET("api/soporte/mis-tickets")
    suspend fun obtenerMisTickets(): List<TicketSoporte>

    @FormUrlEncoded
    @POST("api/soporte/crear-ticket")
    suspend fun crearTicket(
        @Field("asunto") asunto: String,
        @Field("mensaje") mensaje: String
    ): CrearTicketResponse

    @GET("api/soporte/ticket/{id}/mensajes")
    suspend fun obtenerMensajesTicket(
        @Path("id") ticketId: Long
    ): List<SoporteMensaje>

    @FormUrlEncoded
    @POST("api/soporte/ticket/{id}/enviar")
    suspend fun enviarMensajeTicket(
        @Path("id") ticketId: Long,
        @Field("mensaje") mensaje: String
    ): StatusResponse

    @GET("api/cliente/mi-perfil")
    suspend fun obtenerClientePerfil(): ClientePerfilResponse

    @PUT("api/cliente/mi-perfil")
    suspend fun actualizarClientePerfil(
        @Body body: Map<String, @JvmSuppressWildcards Any>
    ): Map<String, Any>

    @GET("api/campesino/verificacion/estado")
    suspend fun obtenerEstadoVerificacion(): VerificacionResponse

    @POST("api/campesino/verificacion/enviar")
    suspend fun enviarVerificacion(
        @Body body: Map<String, @JvmSuppressWildcards Any>
    ): Map<String, Any>
}
