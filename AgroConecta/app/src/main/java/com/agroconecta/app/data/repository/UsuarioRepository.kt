package com.agroconecta.app.data.repository

import com.agroconecta.app.data.model.BaseResponse
import com.agroconecta.app.data.model.CarritoAgregarResponse
import com.agroconecta.app.data.model.Direccion
import com.agroconecta.app.data.model.FavoritoToggleResponse
import com.agroconecta.app.data.model.LoginResponse
import com.agroconecta.app.data.model.Producto
import com.agroconecta.app.data.model.Resena
import com.agroconecta.app.data.model.Usuario

class UsuarioRepository(
    private val authRepository: AuthRepository = AuthRepository(),
    private val productosRepository: ProductosRepository = ProductosRepository(),
    private val favoritosRepository: FavoritosRepository = FavoritosRepository(),
    private val direccionesRepository: DireccionesRepository = DireccionesRepository(),
    private val carritoRepository: CarritoRepository = CarritoRepository(),
    private val resenasRepository: ResenasRepository = ResenasRepository()
) {

    suspend fun verificarEmail(email: String): Map<String, Boolean> {
        return authRepository.verificarEmail(email)
    }

    suspend fun verificarUsername(username: String): Map<String, Boolean> {
        return authRepository.verificarUsername(username)
    }

    suspend fun registrarUsuario(usuario: Usuario): Map<String, Any> {
        return authRepository.registrarUsuario(usuario)
    }

    suspend fun loginUsuario(credenciales: Map<String, String>): LoginResponse {
        return authRepository.loginUsuario(credenciales)
    }

    suspend fun obtenerProductosReales(): List<Producto> {
        return productosRepository.obtenerProductosReales()
    }

    suspend fun obtenerMisProductos(): List<Producto> {
        return productosRepository.obtenerMisProductos()
    }

    suspend fun registrarProductoReal(producto: Producto): BaseResponse {
        return productosRepository.registrarProductoReal(producto)
    }

    suspend fun toggleFavoritoProducto(idProducto: Long): Map<String, Any> {
        return favoritosRepository.toggleFavoritoProducto(idProducto)
    }

    suspend fun obtenerDireccionesUsuario(): List<Direccion> {
        return direccionesRepository.obtenerDireccionesUsuario()
    }

    suspend fun agregarDireccionUsuario(direccion: Direccion): Direccion {
        return direccionesRepository.agregarDireccionUsuario(direccion)
    }

    suspend fun marcarDireccionComoPrincipal(idDireccion: Long): Map<String, String> {
        return direccionesRepository.marcarDireccionComoPrincipal(idDireccion)
    }

    suspend fun eliminarDireccionUsuario(idDireccion: Long): Map<String, String> {
        return direccionesRepository.eliminarDireccionUsuario(idDireccion)
    }

    suspend fun agregarProductoAlCarrito(idProducto: Long, cantidad: Int = 1): CarritoAgregarResponse {
        return carritoRepository.agregarProductoAlCarrito(idProducto, cantidad)
    }

    suspend fun obtenerResenasDeProducto(idProducto: Long): List<Resena> {
        return resenasRepository.obtenerResenasDeProducto(idProducto)
    }

    suspend fun guardarResenaProducto(
        productoId: Long,
        estrellas: Int,
        comentario: String? = null
    ): Map<String, Any> {
        return resenasRepository.guardarResenaProducto(productoId, estrellas, comentario)
    }

    suspend fun eliminarResena(idResena: Long): Map<String, Any> {
        return resenasRepository.eliminarResena(idResena)
    }
}
