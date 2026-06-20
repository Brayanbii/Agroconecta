package com.agroconecta.app.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.agroconecta.app.data.model.Direccion
import com.agroconecta.app.data.model.Usuario
import com.agroconecta.app.data.model.UsuarioInfo
import com.agroconecta.app.data.model.Producto
import com.agroconecta.app.data.model.Resena
import com.agroconecta.app.data.repository.UsuarioRepository
import kotlinx.coroutines.launch

class AuthViewModel : ViewModel() {

    private val repository = UsuarioRepository()

    // ---- ESTADOS DE LA INTERFAZ ----
    var emailExiste by mutableStateOf<Boolean?>(null)
        private set

    var usernameExiste by mutableStateOf<Boolean?>(null)
        private set

    var estaCargando by mutableStateOf(false)
        private set

    var registroExitoso by mutableStateOf(false)
        private set

    var usuarioLogueado by mutableStateOf<UsuarioInfo?>(null)
        private set

    var errorLogin by mutableStateOf<String?>(null)
        private set

    // ---- ESTADOS DE PRODUCTOS REALES ----
    val listaProductosReales = mutableStateListOf<Producto>()

    var productoPublicadoExito by mutableStateOf(false)
        private set

    // ---- ESTADOS DE FAVORITOS / CARRITO / DIRECCIONES ----
    val listaFavoritos = mutableStateListOf<Producto>()
    val direccionesUsuario = mutableStateListOf<Direccion>()

    var mensajeOperacion by mutableStateOf<String?>(null)
        private set

    var errorOperacion by mutableStateOf<String?>(null)
        private set

    var cantidadItemsCarrito by mutableStateOf(0)
        private set

    // ---- ESTADOS DE RESEÑAS ----
    val resenasProducto = mutableStateListOf<Resena>()


    // ---- MÉTODOS DE ESTADO ----

    fun setUsuarioInicial(usuario: UsuarioInfo) {
        usuarioLogueado = usuario
    }


    // ---- MÉTODOS DE COMUNICACIÓN CON SPRING BOOT ----

    fun comprobarEmail(email: String) {
        viewModelScope.launch {
            estaCargando = true
            try {
                val respuesta = repository.verificarEmail(email)
                emailExiste = respuesta["exists"] ?: false
            } catch (e: Exception) {
                emailExiste = null
            } finally {
                estaCargando = false
            }
        }
    }

    fun comprobarUsername(username: String) {
        viewModelScope.launch {
            estaCargando = true
            try {
                val respuesta = repository.verificarUsername(username)
                usernameExiste = respuesta["exists"] ?: false
            } catch (e: Exception) {
                usernameExiste = null
            } finally {
                estaCargando = false
            }
        }
    }

    fun registrarNuevoUsuario(usuario: Usuario) {
        viewModelScope.launch {
            estaCargando = true
            errorOperacion = null
            try {
                val resultado = repository.registrarUsuario(usuario)
                val exito = resultado["success"] as? Boolean ?: false
                if (exito) {
                    registroExitoso = true
                } else {
                    errorOperacion = resultado["message"] as? String ?: "Error al crear cuenta"
                }
            } catch (e: Exception) {
                errorOperacion = "No se pudo conectar con AgroConecta"
                registroExitoso = false
            } finally {
                estaCargando = false
            }
        }
    }

    fun iniciarSesion(email: String, password: String) {
        viewModelScope.launch {
            estaCargando = true
            errorLogin = null
            try {
                val credenciales = mapOf("email" to email, "password" to password)
                val resultado = repository.loginUsuario(credenciales)

                if (resultado.success) {
                    usuarioLogueado = resultado.user
                } else {
                    errorLogin = resultado.message
                }
            } catch (e: Exception) {
                errorLogin = "No se pudo establecer conexión con AgroConecta"
            } finally {
                estaCargando = false
            }
        }
    }

    // ---- MÉTODOS PARA PRODUCTOS REALES ----

    fun cargarProductosDeXampp() {
        viewModelScope.launch {
            estaCargando = true
            try {
                val productos = repository.obtenerProductosReales()
                listaProductosReales.clear()
                listaProductosReales.addAll(productos)
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                estaCargando = false
            }
        }
    }

    fun cargarMisProductos() {
        viewModelScope.launch {
            estaCargando = true
            try {
                val productos = repository.obtenerMisProductos()
                listaProductosReales.clear()
                listaProductosReales.addAll(productos)
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                estaCargando = false
            }
        }
    }

    // Publicación optimizada que envía la categoría seleccionada y una imagen por defecto
    fun publicarNuevaCosecha(nombre: String, precio: Double, stock: Int, categoria: String) {
        viewModelScope.launch {
            estaCargando = true
            productoPublicadoExito = false
            try {
                val nuevoProd = Producto(
                    nombre = nombre,
                    descripcion = "Producto cosechado fresco y de alta calidad",
                    precio = precio,
                    stock = stock,
                    imagenUrl = "default.png", // ⬅️ Corregido a 'imagenUrl' para que coincida con la BD
                    categoria = categoria,
                    unidad = "Kg" // ⬅️ Enviamos una unidad por defecto segura
                )
                val respuesta = repository.registrarProductoReal(nuevoProd)
                if (respuesta.success) {
                    productoPublicadoExito = true
                    cargarProductosDeXampp() // Recarga inmediata del inventario
                }
            } catch (e: Exception) {
                e.printStackTrace()
                productoPublicadoExito = false
            } finally {
                estaCargando = false
            }
        }
    }

    fun cargarFavoritos() {
        viewModelScope.launch {
            estaCargando = true
            try {
                // Mock o llamada a repositorio para cargar favoritos
                // val resultado = repository.obtenerFavoritosUsuario()
                // listaFavoritos.clear()
                // listaFavoritos.addAll(resultado)
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                estaCargando = false
            }
        }
    }

    fun toggleFavoritoProducto(idProducto: Long) {
        viewModelScope.launch {
            estaCargando = true
            errorOperacion = null
            mensajeOperacion = null
            try {
                val resultado = repository.toggleFavoritoProducto(idProducto)
                val liked = resultado["liked"] as? Boolean ?: false
                mensajeOperacion = if (liked) "Agregado a favoritos" else "Eliminado de favoritos"
            } catch (e: Exception) {
                errorOperacion = "No se pudo actualizar favorito."
            } catch (e: Exception) {
                errorOperacion = "No se pudo actualizar favorito."
            } finally {
                estaCargando = false
            }
        }
    }

    fun agregarProductoAlCarrito(idProducto: Long, cantidad: Int = 1) {
        viewModelScope.launch {
            estaCargando = true
            errorOperacion = null
            mensajeOperacion = null
            try {
                val resultado = repository.agregarProductoAlCarrito(idProducto, cantidad)
                if (resultado.status == "ok") {
                    cantidadItemsCarrito = resultado.cantidadItems ?: cantidadItemsCarrito
                    mensajeOperacion = resultado.mensaje ?: "Producto añadido"
                } else {
                    errorOperacion = resultado.mensaje ?: resultado.error ?: "No se pudo agregar al carrito."
                }
            } catch (e: Exception) {
                errorOperacion = "No se pudo conectar para agregar al carrito."
            } finally {
                estaCargando = false
            }
        }
    }

    fun cargarDirecciones() {
        viewModelScope.launch {
            estaCargando = true
            errorOperacion = null
            try {
                val resultado = repository.obtenerDireccionesUsuario()
                direccionesUsuario.clear()
                direccionesUsuario.addAll(resultado)
            } catch (e: Exception) {
                errorOperacion = "No se pudieron cargar las direcciones."
            } finally {
                estaCargando = false
            }
        }
    }

    fun agregarDireccion(direccion: Direccion) {
        viewModelScope.launch {
            estaCargando = true
            errorOperacion = null
            mensajeOperacion = null
            try {
                repository.agregarDireccionUsuario(direccion)
                mensajeOperacion = "Dirección agregada correctamente."
                cargarDirecciones()
            } catch (e: Exception) {
                errorOperacion = "No se pudo agregar la dirección."
            } finally {
                estaCargando = false
            }
        }
    }

    fun marcarDireccionPrincipal(idDireccion: Long) {
        viewModelScope.launch {
            estaCargando = true
            errorOperacion = null
            mensajeOperacion = null
            try {
                val respuesta = repository.marcarDireccionComoPrincipal(idDireccion)
                mensajeOperacion = respuesta["mensaje"] ?: "Dirección principal actualizada."
                cargarDirecciones()
            } catch (e: Exception) {
                errorOperacion = "No se pudo actualizar la dirección principal."
            } finally {
                estaCargando = false
            }
        }
    }

    fun eliminarDireccion(idDireccion: Long) {
        viewModelScope.launch {
            estaCargando = true
            errorOperacion = null
            mensajeOperacion = null
            try {
                val respuesta = repository.eliminarDireccionUsuario(idDireccion)
                mensajeOperacion = respuesta["mensaje"] ?: "Dirección eliminada."
                cargarDirecciones()
            } catch (e: Exception) {
                errorOperacion = "No se pudo eliminar la dirección."
            } finally {
                estaCargando = false
            }
        }
    }

    fun cargarResenasDeProducto(idProducto: Long) {
        viewModelScope.launch {
            estaCargando = true
            errorOperacion = null
            try {
                val resultado = repository.obtenerResenasDeProducto(idProducto)
                resenasProducto.clear()
                resenasProducto.addAll(resultado)
            } catch (e: Exception) {
                errorOperacion = "No se pudieron cargar las reseñas."
            } finally {
                estaCargando = false
            }
        }
    }

    fun guardarResenaProducto(idProducto: Long, estrellas: Int, comentario: String? = null) {
        viewModelScope.launch {
            estaCargando = true
            errorOperacion = null
            mensajeOperacion = null
            try {
                val respuesta = repository.guardarResenaProducto(idProducto, estrellas, comentario)
                mensajeOperacion = respuesta["mensaje"]?.toString() ?: "Reseña guardada correctamente."
                cargarResenasDeProducto(idProducto)
            } catch (e: Exception) {
                errorOperacion = "No se pudo guardar la reseña."
            } finally {
                estaCargando = false
            }
        }
    }

    fun eliminarResena(idResena: Long, idProducto: Long) {
        viewModelScope.launch {
            estaCargando = true
            errorOperacion = null
            mensajeOperacion = null
            try {
                val respuesta = repository.eliminarResena(idResena)
                mensajeOperacion = respuesta["mensaje"]?.toString() ?: "Reseña eliminada."
                cargarResenasDeProducto(idProducto)
            } catch (e: Exception) {
                errorOperacion = "No se pudo eliminar la reseña."
            } finally {
                estaCargando = false
            }
        }
    }

    fun actualizarCantidadCarritoPersistida(cantidad: Int) {
        cantidadItemsCarrito = cantidad
    }

    fun finalizarCheckoutMovil() {
        mensajeOperacion = "Base de checkout móvil preparada. Continúa con el flujo de pago backend existente."
    }

    fun limpiarEstados() {
        emailExiste = null
        usernameExiste = null
        estaCargando = false
        registroExitoso = false
        usuarioLogueado = null
        errorLogin = null
        productoPublicadoExito = false
        mensajeOperacion = null
        errorOperacion = null
        cantidadItemsCarrito = 0
        direccionesUsuario.clear()
        resenasProducto.clear()
    }
}
