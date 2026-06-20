package com.agroconecta.app.viewmodel

import android.content.Context
import android.location.LocationManager
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.agroconecta.app.data.api.RetrofitClient
import com.agroconecta.app.data.model.CampesinoPerfil
import com.agroconecta.app.data.model.CarritoItem
import com.agroconecta.app.data.model.CarritoResponse
import com.agroconecta.app.data.model.CrearOrdenRequest
import com.agroconecta.app.data.model.Direccion
import com.agroconecta.app.data.model.ClientePerfilResponse
import com.agroconecta.app.data.model.FinanzasResponse
import com.agroconecta.app.data.model.InformeResponse
import com.agroconecta.app.data.model.LogisticaResponse
import com.agroconecta.app.data.model.MiPerfilResponse
import com.agroconecta.app.data.model.Pedido
import com.agroconecta.app.data.model.PedidoItem
import com.agroconecta.app.data.model.PedidosResponse
import com.agroconecta.app.data.model.Producto
import com.agroconecta.app.data.model.ReputacionResponse
import com.agroconecta.app.data.model.Resena
import com.agroconecta.app.data.model.SoporteMensaje
import com.agroconecta.app.data.model.TicketSoporte
import com.agroconecta.app.data.model.CrearTicketResponse
import com.agroconecta.app.data.model.VerificacionResponse
import com.agroconecta.app.data.repository.CarritoRepository
import com.agroconecta.app.data.repository.DireccionesRepository
import com.agroconecta.app.data.repository.FavoritosRepository
import com.agroconecta.app.data.repository.PedidosRepository
import com.agroconecta.app.data.repository.ProductosRepository
import com.agroconecta.app.data.repository.ResenasRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class TiendaViewModel : ViewModel() {

    private val productosRepo = ProductosRepository()
    private val carritoRepo = CarritoRepository()
    private val favoritosRepo = FavoritosRepository()
    private val resenasRepo = ResenasRepository()
    private val direccionesRepo = DireccionesRepository()
    private val pedidosRepo = PedidosRepository()

    val todosLosProductos = mutableStateListOf<Producto>()
    val productosFiltrados = mutableStateListOf<Producto>()

    var estaCargando by mutableStateOf(false)
    var conectado by mutableStateOf(false)
    var refrescando by mutableStateOf(false)
    var categoriaSeleccionada by mutableStateOf<String?>(null)
    var terminoBusqueda by mutableStateOf("")
    var cantidadItemsCarrito by mutableStateOf(0)
    var mensajeOperacion by mutableStateOf<String?>(null)
    var errorOperacion by mutableStateOf<String?>(null)
    var toastEvent by mutableStateOf<com.agroconecta.app.ui.components.ToastData?>(null)
    
    fun dismissToast() { toastEvent = null }

    val resenasProductoSeleccionado = mutableStateListOf<Resena>()
    val productosDelCampesino = mutableStateListOf<Producto>()
    var campesinoPerfil by mutableStateOf<CampesinoPerfil?>(null)
    var productoEditando by mutableStateOf<Producto?>(null)
    var sipsaPrecio by mutableStateOf<String?>(null)
    var catalogoSipsa by mutableStateOf<List<String>>(emptyList())
    var sipsaProductoSeleccionado by mutableStateOf<String?>(null)
    var pedidos by mutableStateOf<List<Pedido>>(emptyList())
    var ahorroMes by mutableStateOf(0.0)
    var carritoItems by mutableStateOf<List<CarritoItem>>(emptyList())
    var carritoSubtotal by mutableStateOf(0.0)
    var mpUrl by mutableStateOf<String?>(null)
    val direccionesUsuario = mutableStateListOf<Direccion>()

    var direccionEntrega by mutableStateOf<String?>(null)
    var direccionSeleccionada by mutableStateOf<Direccion?>(null)
    var userLat by mutableStateOf<Double?>(null)
    var userLng by mutableStateOf<Double?>(null)

    val listaFavoritos = mutableStateListOf<Producto>()
    val idsFavoritos = mutableStateListOf<Long>()
    val distancias = mutableStateMapOf<Long, Double>()

    init { cargarProductos() }

    fun cargarProductos() {
        viewModelScope.launch {
            estaCargando = true
            try {
                val productos = productosRepo.obtenerProductosReales()
                todosLosProductos.clear()
                todosLosProductos.addAll(productos)
                aplicarFiltros()
                conectado = true
            } catch (e: Exception) {
                errorOperacion = "Error al cargar productos"
                conectado = false
            } finally { estaCargando = false }
        }
    }

    fun refrescarTienda() {
        if (refrescando) return
        refrescando = true
        viewModelScope.launch {
            cargarProductos()
            cargarFavoritos()
            delay(800)
            refrescando = false
        }
    }

    fun seleccionarCategoria(categoria: String?) {
        categoriaSeleccionada = categoria
        aplicarFiltros()
    }

    fun buscarProductos(termino: String) {
        terminoBusqueda = termino
        aplicarFiltros()
    }

    private fun aplicarFiltros() {
        productosFiltrados.clear()
        val resultado = todosLosProductos.filter { p ->
            val coincideCategoria = categoriaSeleccionada == null ||
                p.categoria?.equals(categoriaSeleccionada, ignoreCase = true) == true
            val coincideBusqueda = terminoBusqueda.isBlank() ||
                p.nombre.contains(terminoBusqueda, ignoreCase = true) ||
                p.categoria?.contains(terminoBusqueda, ignoreCase = true) == true ||
                p.nombreCampesino?.contains(terminoBusqueda, ignoreCase = true) == true
            coincideCategoria && coincideBusqueda
        }
        productosFiltrados.addAll(resultado)
    }

    // =============================================================
    // ALGORITMOS DE SECCIONES (MATCHING Spring Boot DashboardController)
    // =============================================================

    // Destacados: ordenar por promedioCalificacion DESC + totalResenas DESC. Top 8.
    fun getDestacados(): List<Producto> =
        todosLosProductos
            .sortedWith(compareByDescending<Producto> { it.promedioCalificacion ?: 0.0 }
                .thenByDescending { it.totalResenas ?: 0 })
            .take(8)

    // Recien Cosechados: ordenar por fechaCreacion DESC. Top 8.
    fun getRecienCosechados(): List<Producto> =
        todosLosProductos
            .sortedByDescending { it.fechaCreacion ?: "" }
            .take(8)

    // Ofertas: filtrar id % 3 == 0 (mismo que la web), top 8
    fun getOfertas(): List<Producto> =
        todosLosProductos
            .filter { p -> (p.id ?: 0L) % 3 == 0L }
            .take(8)

    // Recomendados: mejores calificados que NO esten en Destacados, top 8
    fun getRecomendados(): List<Producto> {
        val destacadosIds = getDestacados().mapNotNull { it.id }.toSet()
        val ofertaIds = getOfertas().mapNotNull { it.id }.toSet()
        return todosLosProductos
            .filter { it.id !in destacadosIds && it.id !in ofertaIds }
            .sortedByDescending { it.promedioCalificacion ?: 0.0 }
            .take(8)
    }

    // Cerca de Ti: Haversine, solo productos con lat/lon, ordenados por distancia
    fun getCercaDeTi(): List<Producto> {
        if (userLat == null || userLng == null) return emptyList()
        return todosLosProductos
            .filter { it.latitudOrigen != null && it.longitudOrigen != null }
            .map { p ->
                val d = haversine(userLat!!, userLng!!, p.latitudOrigen!!, p.longitudOrigen!!)
                p.id?.let { distancias[it] = d }
                p to d
            }
            .filter { it.second < 150.0 }
            .sortedBy { it.second }
            .take(8)
            .map { it.first }
    }

    fun getCercaDeTiFull(): List<Producto> {
        if (userLat == null || userLng == null) return emptyList()
        return todosLosProductos
            .filter { it.latitudOrigen != null && it.longitudOrigen != null }
            .map { p ->
                val d = haversine(userLat!!, userLng!!, p.latitudOrigen!!, p.longitudOrigen!!)
                p.id?.let { distancias[it] = d }
                p to d
            }
            .filter { it.second < 150.0 }
            .sortedBy { it.second }
            .map { it.first }
    }

    // ===== FULL LISTS (sin limite) =====
    fun getDestacadosFull(): List<Producto> = todosLosProductos
        .sortedWith(compareByDescending<Producto> { it.promedioCalificacion ?: 0.0 }.thenByDescending { it.totalResenas ?: 0 })

    fun getRecienCosechadosFull(): List<Producto> = todosLosProductos
        .sortedByDescending { it.fechaCreacion ?: "" }

    fun getOfertasFull(): List<Producto> = todosLosProductos
        .filter { (it.id ?: 0L) % 3 == 0L }

    fun getRecomendadosFull(): List<Producto> {
        val excluir = (getDestacados().mapNotNull { it.id } + getOfertas().mapNotNull { it.id }).toSet()
        return todosLosProductos.filter { it.id !in excluir }.sortedByDescending { it.promedioCalificacion ?: 0.0 }
    }

    private fun haversine(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val R = 6371.0
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                Math.sin(dLon / 2) * Math.sin(dLon / 2)
        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
        return Math.round(R * c * 10.0) / 10.0
    }

    fun calcularCosechadoHace(fechaStr: String?): String {
        if (fechaStr == null) return ""
        return try {
            val formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME
            val fecha = LocalDateTime.parse(fechaStr, formatter)
            val ahora = LocalDateTime.now()
            val diffMins = java.time.Duration.between(fecha, ahora).toMinutes()
            when {
                diffMins < 60 -> if (diffMins <= 1) "Cosechado hace 1 min" else "Cosechado hace $diffMins mins"
                diffMins < 1440 -> {
                    val hrs = diffMins / 60
                    if (hrs == 1L) "Cosechado hace 1 hora" else "Cosechado hace $hrs horas"
                }
                diffMins < 43200 -> {
                    val dias = diffMins / 1440
                    if (dias == 1L) "Cosechado hace 1 dia" else "Cosechado hace $dias dias"
                }
                else -> {
                    val meses = diffMins / 43200
                    if (meses == 1L) "Cosechado hace 1 mes" else "Cosechado hace $meses meses"
                }
            }
        } catch (e: Exception) { "" }
    }

    fun gpsEnabled(context: Context): Boolean {
        val lm = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager ?: return false
        return lm.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
               lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    // =============================================================
    // CARRITO, FAVORITOS, RESENAS
    // =============================================================

    fun agregarAlCarrito(productoId: Long, cantidad: Int = 1) {
        viewModelScope.launch {
            try {
                val resp = carritoRepo.agregarProductoAlCarrito(productoId, cantidad)
                if (resp.status == "ok") {
                    cantidadItemsCarrito = resp.cantidadItems ?: cantidadItemsCarrito
                    val totalEnCarrito = resp.cantidadItems ?: cantidad
                    val msg = if (cantidad > 1) "$cantidad unidades agregadas al carrito"
                              else "Producto agregado al carrito"
                    mensajeOperacion = msg
                    toastEvent = com.agroconecta.app.ui.components.ToastData(
                        message = msg,
                        type = com.agroconecta.app.ui.components.ToastType.CART,
                        duration = 1500L
                    )
                } else {
                    val errorMsg = resp.mensaje ?: resp.error ?: "Error al agregar"
                    errorOperacion = errorMsg
                    toastEvent = com.agroconecta.app.ui.components.ToastData(
                        message = errorMsg,
                        type = com.agroconecta.app.ui.components.ToastType.ERROR,
                        duration = 1500L
                    )
                }
            } catch (e: retrofit2.HttpException) {
                val errorMsg = when (e.code()) {
                    401 -> "Inicia sesion para usar el carrito"
                    400 -> "Stock insuficiente"
                    404 -> "Producto no encontrado"
                    else -> "Error del servidor (${e.code()})"
                }
                errorOperacion = errorMsg
                toastEvent = com.agroconecta.app.ui.components.ToastData(
                    message = errorMsg,
                    type = com.agroconecta.app.ui.components.ToastType.ERROR,
                    duration = 1500L
                )
            } catch (e: java.net.ConnectException) {
                val errorMsg = "No hay conexion con el servidor"
                errorOperacion = errorMsg
                toastEvent = com.agroconecta.app.ui.components.ToastData(
                    message = errorMsg,
                    type = com.agroconecta.app.ui.components.ToastType.ERROR,
                    duration = 1500L
                )
            } catch (e: java.net.UnknownHostException) {
                val errorMsg = "Servidor no encontrado"
                errorOperacion = errorMsg
                toastEvent = com.agroconecta.app.ui.components.ToastData(
                    message = errorMsg,
                    type = com.agroconecta.app.ui.components.ToastType.ERROR,
                    duration = 1500L
                )
            } catch (e: java.net.SocketTimeoutException) {
                val errorMsg = "Tiempo de espera agotado"
                errorOperacion = errorMsg
                toastEvent = com.agroconecta.app.ui.components.ToastData(
                    message = errorMsg,
                    type = com.agroconecta.app.ui.components.ToastType.ERROR,
                    duration = 1500L
                )
            } catch (e: Exception) {
                val errorMsg = "Error: ${e.message?.take(60) ?: "desconocido"}"
                errorOperacion = errorMsg
                toastEvent = com.agroconecta.app.ui.components.ToastData(
                    message = errorMsg,
                    type = com.agroconecta.app.ui.components.ToastType.ERROR,
                    duration = 1500L
                )
            }
        }
    }

    fun toggleFavorito(productoId: Long) {
        viewModelScope.launch {
            try {
                val resp = favoritosRepo.toggleFavoritoProducto(productoId)
                val liked = resp["isFavorito"] as? Boolean ?: false
                val msg = if (liked) "Agregado a favoritos" else "Eliminado de favoritos"
                mensajeOperacion = msg
                toastEvent = com.agroconecta.app.ui.components.ToastData(
                    message = msg,
                    type = if (liked) com.agroconecta.app.ui.components.ToastType.FAVORITE else com.agroconecta.app.ui.components.ToastType.INFO,
                    duration = 1500L
                )
                if (!liked) {
                    idsFavoritos.remove(productoId)
                    listaFavoritos.removeAll { it.id == productoId }
                } else {
                    idsFavoritos.add(productoId)
                }
            } catch (e: retrofit2.HttpException) {
                val errorMsg = when (e.code()) { 401 -> "Inicia sesion para favoritos"; else -> "Error (${e.code()})" }
                errorOperacion = errorMsg
                    } catch (e: Exception) { 
                val errorMsg = "Error: ${e.message?.take(50)}"
                errorOperacion = errorMsg
                    }
        }
    }

    fun cargarFavoritos() {
        viewModelScope.launch {
            estaCargando = true
            try {
                val favs = favoritosRepo.obtenerFavoritosUsuario()
                listaFavoritos.clear()
                idsFavoritos.clear()
                listaFavoritos.addAll(favs)
                idsFavoritos.addAll(favs.mapNotNull { it.id })
            } catch (e: retrofit2.HttpException) {
                // Silencioso - no es critico si no hay sesion
            } catch (e: Exception) {
                errorOperacion = "Error al cargar favoritos"
            } finally { estaCargando = false }
        }
    }

    fun esFavorito(productoId: Long): Boolean = idsFavoritos.contains(productoId)

    fun cargarResenas(productoId: Long) {
        viewModelScope.launch {
            try {
                val resenas = resenasRepo.obtenerResenasDeProducto(productoId)
                resenasProductoSeleccionado.clear()
                resenasProductoSeleccionado.addAll(resenas)
            } catch (e: Exception) { }
        }
    }

    fun cargarProductosDelCampesino(campesinoId: Long) {
        viewModelScope.launch {
            try {
                val prods = productosRepo.obtenerProductosPorCampesino(campesinoId)
                productosDelCampesino.clear()
                productosDelCampesino.addAll(prods)
            } catch (e: Exception) { }
        }
    }

    fun cargarPerfilCampesino(campesinoId: Long) {
        viewModelScope.launch {
            estaCargando = true
            errorOperacion = null
            try {
                campesinoPerfil = productosRepo.obtenerPerfilCampesino(campesinoId)
                val prods = productosRepo.obtenerProductosPorCampesino(campesinoId)
                productosDelCampesino.clear()
                productosDelCampesino.addAll(prods)
            } catch (e: retrofit2.HttpException) {
                errorOperacion = "Error del servidor (${e.code()})"
            } catch (e: Exception) {
                errorOperacion = if (e.message?.contains("Unable to resolve host") == true) "Sin conexion" 
                                else "Error: ${e.message ?: "desconocido"}"
            } finally { estaCargando = false }
        }
    }

    fun cargarPedidos() {
        viewModelScope.launch {
            estaCargando = true; errorOperacion = null
            try {
                val resp = pedidosRepo.obtenerMisCompras()
                val exito = resp["success"] as? Boolean ?: false
                if (exito) {
                    val lista = resp["pedidos"] as? List<*>
                    if (lista != null) {
                        pedidos = lista.mapNotNull { item ->
                            val map = item as? Map<*, *> ?: return@mapNotNull null
                            val itemsRaw = map["items"] as? List<*>
                            Pedido(
                                id = (map["id"] as? Number)?.toLong(),
                                numeroOrden = map["numeroOrden"] as? String,
                                fechaCreacion = map["fechaCreacion"] as? String,
                                total = (map["total"] as? Number)?.toDouble(),
                                estado = map["estado"] as? String,
                                direccionEnvio = map["direccionEnvio"] as? String,
                                items = itemsRaw?.mapNotNull { di ->
                                    val dm = di as? Map<*, *> ?: return@mapNotNull null
                                    PedidoItem(
                                        nombre = dm["nombre"] as? String,
                                        precio = (dm["precio"] as? Number)?.toDouble(),
                                        cantidad = (dm["cantidad"] as? Number)?.toInt(),
                                        total = (dm["total"] as? Number)?.toDouble(),
                                        estado = dm["estado"] as? String,
                                        imagenUrl = dm["imagenUrl"] as? String,
                                        unidad = dm["unidad"] as? String
                                    )
                                }
                            )
                        }
                        ahorroMes = pedidos.sumOf { (it.total ?: 0.0) * 0.05 }
                    }
                    conectado = true
                } else {
                    errorOperacion = resp["error"] as? String ?: "Error desconocido"
                }
            } catch (e: Exception) {
                errorOperacion = "Error: ${e.message?.take(60)}"
                conectado = false
            }
            finally { estaCargando = false }
        }
    }

    fun cargarCarrito() {
        viewModelScope.launch {
            estaCargando = true
            try {
                val resp = carritoRepo.obtenerCarrito()
                carritoItems = resp.items ?: emptyList()
                carritoSubtotal = resp.subtotal ?: 0.0
                cantidadItemsCarrito = resp.cantidad ?: 0
            } catch (e: Exception) { }
            finally { estaCargando = false }
        }
    }

    fun actualizarCantidadCarrito(id: Long, cantidad: Int) {
        // Optimista: actualizar ViewModel al instante
        val updated = carritoItems.map { if (it.id == id) it.copy(cantidad = cantidad, total = (it.precio ?: 0.0) * cantidad) else it }
        carritoItems = updated
        carritoSubtotal = updated.sumOf { it.total ?: 0.0 }
        cantidadItemsCarrito = updated.sumOf { it.cantidad ?: 0 }
        // API en segundo plano
        viewModelScope.launch {
            try {
                val resp = carritoRepo.actualizarCantidad(id, cantidad)
                if (resp.status == "ok") {
                    cargarCarrito()
                    mensajeOperacion = "Cantidad actualizada"
                } else {
                    cargarCarrito()
                    errorOperacion = resp.error ?: "Error"
                }
            } catch (e: Exception) {
                cargarCarrito()
                errorOperacion = "Error al actualizar"
            }
        }
    }

    fun eliminarDelCarrito(id: Long) {
        // Optimista
        var updated = carritoItems.filter { it.id != id }
        carritoItems = updated
        carritoSubtotal = updated.sumOf { it.total ?: 0.0 }
        cantidadItemsCarrito = updated.sumOf { it.cantidad ?: 0 }
        // API
        viewModelScope.launch {
            try { carritoRepo.eliminarDelCarrito(id); cargarCarrito(); mensajeOperacion = "Producto eliminado" }
            catch (e: Exception) { cargarCarrito(); errorOperacion = "Error al eliminar" }
        }
    }

    fun limpiarCarritoCompleto() {
        carritoItems = emptyList()
        carritoSubtotal = 0.0
        cantidadItemsCarrito = 0
        viewModelScope.launch {
            try { carritoRepo.limpiarCarrito(); cargarCarrito(); mensajeOperacion = "Carrito vaciado" }
            catch (e: Exception) { cargarCarrito() }
        }
    }

    fun crearOrdenPago(propina: Double, direccion: String? = null, lat: Double? = null, lng: Double? = null, tipoEnvio: String = "ECONOMICO") {
        viewModelScope.launch {
            estaCargando = true
            errorOperacion = null
            try {
                val req = CrearOrdenRequest(propina = propina, direccionEnvio = direccion, latitud = lat, longitud = lng, tipoEnvio = tipoEnvio)
                val resp = RetrofitClient.usuarioApiService.crearOrden(req)
                val success = resp["success"] as? Boolean ?: false
                if (success) {
                    mpUrl = resp["initPoint"] as? String
                    if (mpUrl == null) errorOperacion = "No se recibio URL de pago"
                    cargarCarrito()
                } else {
                    errorOperacion = resp["error"] as? String ?: "Error desconocido"
                }
            } catch (e: retrofit2.HttpException) {
                val code = e.code()
                errorOperacion = when (code) {
                    401 -> "Inicia sesion para pagar"
                    404 -> "Endpoint no encontrado. Reinicia Spring Boot"
                    500 -> "Error interno del servidor"
                    else -> "Error del servidor ($code): ${e.message?.take(40)}"
                }
            } catch (e: java.net.ConnectException) {
                errorOperacion = "Sin conexion al servidor"
            } catch (e: Exception) {
                errorOperacion = "${e.message?.take(60)}"
            } finally { estaCargando = false }
        }
    }
    
    var puedeComentar by mutableStateOf(false)
    var mensajeReview by mutableStateOf<String?>(null)
    
    fun verificarPuedeComentar(productoId: Long) {
        viewModelScope.launch {
            try {
                val resp = resenasRepo.puedeComentar(productoId)
                puedeComentar = resp["puedeComentar"] as? Boolean ?: false
                mensajeReview = resp["mensaje"] as? String
            } catch (e: Exception) {
                puedeComentar = false
                mensajeReview = "Error al verificar permisos"
            }
        }
    }

    fun guardarResena(productoId: Long, estrellas: Int, comentario: String?) {
        viewModelScope.launch {
            try {
                resenasRepo.guardarResenaProducto(productoId, estrellas, comentario)
                mensajeOperacion = "Resena guardada"
                cargarResenas(productoId)
            } catch (e: Exception) { errorOperacion = "Error al guardar resena" }
        }
    }

    fun obtenerCategorias(): List<String> {
        val base = listOf("Verduras y Hortalizas", "Frutas", "Tuberculos y Raices", "Lacteos", "Huevos", "Granos y Cereales", "Cafe y Cacao")
        val dinamicas = todosLosProductos.mapNotNull { it.categoria }.distinct()
        return (base + dinamicas).distinctBy { it.lowercase().trim() }
    }

    fun limpiarMensaje() { mensajeOperacion = null; errorOperacion = null }

    fun obtenerProductoPorId(id: Long): Producto? =
        todosLosProductos.find { it.id == id }

    // =============================================================
    // DIRECCIONES
    // =============================================================

    fun cargarDirecciones() {
        viewModelScope.launch {
            estaCargando = true
            try {
                val resultado = direccionesRepo.obtenerDireccionesUsuario()
                direccionesUsuario.clear()
                direccionesUsuario.addAll(resultado)
                val principal = resultado.find { it.esPrincipal == true }
                if (principal != null) {
                    direccionSeleccionada = principal
                    direccionEntrega = "${principal.alias}: ${principal.direccionCompleta}"
                }
            } catch (e: retrofit2.HttpException) {
                if (e.code() == 401) errorOperacion = "Debes iniciar sesion para ver tus direcciones"
            } catch (e: Exception) {
                errorOperacion = "Error al cargar direcciones"
            } finally { estaCargando = false }
        }
    }

    fun agregarDireccion(direccion: Direccion) {
        viewModelScope.launch {
            estaCargando = true
            try {
                direccionesRepo.agregarDireccionUsuario(direccion)
                mensajeOperacion = "Direccion guardada"
                cargarDirecciones()
            } catch (e: retrofit2.HttpException) {
                val code = e.code()
                errorOperacion = when (code) {
                    401 -> "Debes iniciar sesion para guardar direcciones"
                    400 -> "Datos incompletos"
                    else -> "Error del servidor ($code)"
                }
            } catch (e: Exception) {
                errorOperacion = "Error de conexion: ${e.message}"
            } finally { estaCargando = false }
        }
    }

    fun marcarDireccionPrincipal(id: Long) {
        viewModelScope.launch {
            estaCargando = true
            try {
                direccionesRepo.marcarDireccionComoPrincipal(id)
                mensajeOperacion = "Direccion principal actualizada"
                cargarDirecciones()
            } catch (_: Exception) { errorOperacion = "Error al actualizar" }
            finally { estaCargando = false }
        }
    }

    fun eliminarDireccion(id: Long) {
        viewModelScope.launch {
            estaCargando = true
            try {
                direccionesRepo.eliminarDireccionUsuario(id)
                mensajeOperacion = "Direccion eliminada"
                cargarDirecciones()
            } catch (_: Exception) { errorOperacion = "Error al eliminar" }
            finally { estaCargando = false }
        }
    }

    // ===== SIPSA =====

    fun cargarCatalogoSipsa() {
        viewModelScope.launch {
            try {
                val resp: List<Map<String, Any>> = RetrofitClient.usuarioApiService.obtenerCatalogoSipsa()
                catalogoSipsa = resp.mapNotNull { it["nombre"] as? String ?: it["producto"] as? String }
            } catch (e: Exception) { catalogoSipsa = emptyList() }
        }
    }

    fun consultarSipsa(producto: String) {
        viewModelScope.launch {
            try {
                val resp = RetrofitClient.usuarioApiService.consultarPrecioSipsa(producto)
                val precio = resp["precio_promedio"] as? Double
                    ?: resp["precio"] as? Double
                    ?: resp["precio_mayorista"] as? Double
                if (precio != null) {
                    sipsaPrecio = "Precio promedio: $$precio / Kg"
                } else {
                    val msg = resp["mensaje"] as? String ?: resp["message"] as? String ?: "Sin datos"
                    sipsaPrecio = msg
                }
            } catch (e: Exception) { sipsaPrecio = "Error al consultar" }
        }
    }

    // ===== CRUD PRODUCTOS CAMPESINO =====

    suspend fun subirImagenProducto(part: okhttp3.MultipartBody.Part): Map<String, Any> {
        return try { RetrofitClient.usuarioApiService.subirImagen(part) } catch (e: Exception) { mapOf("success" to false) }
    }

    fun cargarProductoParaEditar(id: Long) {
        viewModelScope.launch {
            try {
                productoEditando = RetrofitClient.usuarioApiService.obtenerProducto(id)
            } catch (e: Exception) { }
        }
    }

    fun publicarProductoCampesino(producto: Producto, onSuccess: () -> Unit) {
        viewModelScope.launch {
            estaCargando = true
            try {
                val resp = productosRepo.registrarProductoReal(producto)
                if (resp.success) {
                    mensajeOperacion = "Producto publicado exitosamente"
                    cargarProductos()
                    onSuccess()
                } else {
                    errorOperacion = resp.message ?: "Error al publicar"
                }
            } catch (e: Exception) { errorOperacion = "Error: ${e.message?.take(60)}" }
            finally { estaCargando = false }
        }
    }

    fun actualizarProductoCampesino(producto: Producto, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            estaCargando = true
            try {
                RetrofitClient.usuarioApiService.actualizarProducto(producto.id ?: 0L, producto)
                mensajeOperacion = "Producto actualizado"
                onSuccess()
            } catch (e: Exception) { errorOperacion = "Error: ${e.message?.take(50)}" }
            finally { estaCargando = false }
        }
    }

    fun eliminarProductoCampesino(id: Long, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            try {
                RetrofitClient.usuarioApiService.eliminarProducto(id)
                mensajeOperacion = "Producto eliminado"
                onSuccess()
            } catch (e: Exception) { errorOperacion = "Error: ${e.message?.take(50)}" }
        }
    }

    // =============================================================
    // INVENTARIO
    // =============================================================

    val misProductosInventario = mutableStateListOf<Producto>()
    var inventarioCargando by mutableStateOf(false)

    fun cargarMisProductosInventario() {
        viewModelScope.launch {
            inventarioCargando = true
            try {
                val prods = productosRepo.obtenerMisProductos()
                misProductosInventario.clear()
                misProductosInventario.addAll(prods)
            } catch (e: Exception) {
                errorOperacion = "Error al cargar inventario"
            } finally { inventarioCargando = false }
        }
    }

    fun actualizarStockProducto(id: Long, accion: String, valor: Int, onResult: ((Boolean, Int?) -> Unit)? = null) {
        viewModelScope.launch {
            try {
                val body = mapOf<String, Any>("accion" to accion, "valor" to valor)
                val resp = RetrofitClient.usuarioApiService.actualizarStockProducto(id, body)
                val success = resp["success"] as? Boolean ?: false
                val nuevoStock = (resp["nuevoStock"] as? Number)?.toInt()
                if (success && nuevoStock != null) {
                    val idx = misProductosInventario.indexOfFirst { it.id == id }
                    if (idx >= 0) {
                        misProductosInventario[idx] = misProductosInventario[idx].copy(stock = nuevoStock)
                    }
                }
                onResult?.invoke(success, nuevoStock)
            } catch (e: Exception) {
                onResult?.invoke(false, null)
            }
        }
    }

    // =============================================================
    // ANALITICAS
    // =============================================================

    var informeAnaliticas by mutableStateOf<InformeResponse?>(null)
    var analiticasCargando by mutableStateOf(false)

    fun cargarInformeAnaliticas() {
        viewModelScope.launch {
            analiticasCargando = true
            errorOperacion = null
            try {
                informeAnaliticas = RetrofitClient.usuarioApiService.obtenerInformeCampesino()
            } catch (e: Exception) {
                errorOperacion = "Error al cargar analiticas: ${e.message?.take(50)}"
            } finally { analiticasCargando = false }
        }
    }

    // =============================================================
    // REPUTACION
    // =============================================================

    var reputacionResponse by mutableStateOf<ReputacionResponse?>(null)
    var reputacionCargando by mutableStateOf(false)

    fun cargarReputacion() {
        viewModelScope.launch {
            reputacionCargando = true
            errorOperacion = null
            try {
                reputacionResponse = RetrofitClient.usuarioApiService.obtenerReputacionCampesino()
            } catch (e: Exception) {
                errorOperacion = "Error al cargar reputacion: ${e.message?.take(50)}"
            } finally { reputacionCargando = false }
        }
    }

    // =============================================================
    // PEDIDOS CAMPESINO
    // =============================================================

    var pedidosResponse by mutableStateOf<PedidosResponse?>(null)
    var pedidosCargando by mutableStateOf(false)
    var logisticaResponse by mutableStateOf<LogisticaResponse?>(null)
    var logisticaCargando by mutableStateOf(false)

    fun cargarPedidosCampesino() {
        viewModelScope.launch {
            pedidosCargando = true
            errorOperacion = null
            try {
                pedidosResponse = RetrofitClient.usuarioApiService.obtenerMisVentas()
            } catch (e: Exception) {
                errorOperacion = "Error al cargar pedidos: ${e.message?.take(50)}"
            } finally { pedidosCargando = false }
        }
    }

    fun cambiarEstadoPedido(id: Long, nuevoEstado: String, onResult: ((Boolean) -> Unit)? = null) {
        viewModelScope.launch {
            try {
                val body = mapOf<String, Any>("estado" to nuevoEstado)
                val resp = RetrofitClient.usuarioApiService.cambiarEstadoPedido(id, body)
                val success = resp["success"] as? Boolean ?: false
                if (success) cargarPedidosCampesino()
                onResult?.invoke(success)
            } catch (e: Exception) {
                onResult?.invoke(false)
            }
        }
    }

    fun cargarLogistica(id: Long) {
        viewModelScope.launch {
            logisticaCargando = true
            errorOperacion = null
            try {
                logisticaResponse = RetrofitClient.usuarioApiService.obtenerLogisticaPedido(id)
            } catch (e: Exception) {
                errorOperacion = "Error al cargar logistica: ${e.message?.take(50)}"
            } finally { logisticaCargando = false }
        }
    }

    // =============================================================
    // PERFIL CAMPESINO
    // =============================================================

    var miPerfilResponse by mutableStateOf<MiPerfilResponse?>(null)
    var perfilCargando by mutableStateOf(false)
    var perfilGuardando by mutableStateOf(false)

    fun cargarMiPerfil() {
        viewModelScope.launch {
            perfilCargando = true
            errorOperacion = null
            try {
                miPerfilResponse = RetrofitClient.usuarioApiService.obtenerMiPerfil()
            } catch (e: Exception) {
                errorOperacion = "Error al cargar perfil: ${e.message?.take(50)}"
            } finally { perfilCargando = false }
        }
    }

    fun actualizarPerfil(
        nombreFinca: String? = null,
        descripcionFinca: String? = null,
        fotoPerfil: String? = null,
        fotoFincaUrl: String? = null,
        nombreCompleto: String? = null,
        telefono: String? = null,
        numeroIdentidad: String? = null,
        municipioOrigen: String? = null,
        latitud: Double? = null,
        longitud: Double? = null,
        fechaNacimiento: String? = null,
        onResult: ((Boolean) -> Unit)? = null
    ) {
        viewModelScope.launch {
            perfilGuardando = true
            try {
                val body = mutableMapOf<String, Any>()
                nombreCompleto?.let { body["nombreCompleto"] = it }
                telefono?.let { body["telefono"] = it }
                numeroIdentidad?.let { body["numeroIdentidad"] = it }
                nombreFinca?.let { body["nombreFinca"] = it }
                descripcionFinca?.let { body["descripcionFinca"] = it }
                municipioOrigen?.let { body["municipioOrigen"] = it }
                latitud?.let { body["latitud"] = it }
                longitud?.let { body["longitud"] = it }
                fotoPerfil?.let { body["fotoPerfil"] = it }
                fotoFincaUrl?.let { body["fotoFincaUrl"] = it }
                fechaNacimiento?.let { body["fechaNacimiento"] = it }
                val resp = RetrofitClient.usuarioApiService.actualizarMiPerfil(body)
                val success = resp["success"] as? Boolean ?: false
                if (success) cargarMiPerfil()
                onResult?.invoke(success)
            } catch (e: Exception) {
                onResult?.invoke(false)
            } finally { perfilGuardando = false }
        }
    }

    // =============================================================
    // AGROWALLET / FINANZAS
    // =============================================================

    var finanzasResponse by mutableStateOf<FinanzasResponse?>(null)
    var finanzasCargando by mutableStateOf(false)

    fun cargarFinanzas() {
        viewModelScope.launch {
            finanzasCargando = true
            errorOperacion = null
            try {
                finanzasResponse = RetrofitClient.usuarioApiService.obtenerFinanzas()
            } catch (e: Exception) {
                errorOperacion = "Error al cargar finanzas: ${e.message?.take(50)}"
            } finally { finanzasCargando = false }
        }
    }

    // =============================================================
    // SOPORTE
    // =============================================================

    var soporteTickets = mutableStateListOf<TicketSoporte>()
    var soporteMensajes = mutableStateListOf<SoporteMensaje>()
    var soporteTicketActivoId by mutableStateOf<Long?>(null)
    var soporteCargando by mutableStateOf(false)

    fun cargarMisTickets() {
        viewModelScope.launch {
            try {
                val lista = RetrofitClient.usuarioApiService.obtenerMisTickets()
                soporteTickets.clear()
                soporteTickets.addAll(lista)
            } catch (_: Exception) {}
        }
    }

    fun crearTicketSoporte(asunto: String, mensaje: String, onResult: (Long?) -> Unit) {
        viewModelScope.launch {
            try {
                val resp = RetrofitClient.usuarioApiService.crearTicket(asunto, mensaje)
                onResult(resp.ticketId)
            } catch (_: Exception) { onResult(null) }
        }
    }

    fun cargarMensajesTicket(ticketId: Long) {
        viewModelScope.launch {
            try {
                val lista = RetrofitClient.usuarioApiService.obtenerMensajesTicket(ticketId)
                soporteMensajes.clear()
                soporteMensajes.addAll(lista)
            } catch (_: Exception) {}
        }
    }

    fun enviarMensajeSoporte(ticketId: Long, mensaje: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                RetrofitClient.usuarioApiService.enviarMensajeTicket(ticketId, mensaje)
                cargarMensajesTicket(ticketId)
                onResult(true)
            } catch (_: Exception) { onResult(false) }
        }
    }

    // =============================================================
    // PERFIL CLIENTE
    // =============================================================

    var clientePerfil by mutableStateOf<ClientePerfilResponse?>(null)
    var clientePerfilCargando by mutableStateOf(false)
    var clientePerfilGuardando by mutableStateOf(false)

    fun cargarClientePerfil() {
        viewModelScope.launch {
            clientePerfilCargando = true; errorOperacion = null
            try { clientePerfil = RetrofitClient.usuarioApiService.obtenerClientePerfil() }
            catch (e: Exception) { errorOperacion = "Error: ${e.message?.take(50)}" }
            finally { clientePerfilCargando = false }
        }
    }

    fun actualizarClientePerfil(nombre: String?, telefono: String?, identidad: String?, fecha: String?, genero: String?, foto: String?, onResult: ((Boolean) -> Unit)? = null) {
        viewModelScope.launch {
            clientePerfilGuardando = true
            try {
                val body = mutableMapOf<String, Any>()
                nombre?.let { body["nombreCompleto"] = it }
                telefono?.let { body["telefono"] = it }
                identidad?.let { body["numeroIdentidad"] = it }
                fecha?.let { body["fechaNacimiento"] = it }
                genero?.let { body["genero"] = it }
                foto?.let { body["fotoPerfil"] = it }
                val resp = RetrofitClient.usuarioApiService.actualizarClientePerfil(body)
                val ok = resp["success"] as? Boolean ?: false
                if (ok) cargarClientePerfil()
                onResult?.invoke(ok)
            } catch (e: Exception) { onResult?.invoke(false) }
            finally { clientePerfilGuardando = false }
        }
    }

    // =============================================================
    // VERIFICACION KYC CAMPESINO
    // =============================================================

    var verificacionResponse by mutableStateOf<VerificacionResponse?>(null)
    var verificacionCargando by mutableStateOf(true)
    var verificacionEnviando by mutableStateOf(false)

    fun cargarEstadoVerificacion() {
        viewModelScope.launch {
            verificacionCargando = true
            try { verificacionResponse = RetrofitClient.usuarioApiService.obtenerEstadoVerificacion() }
            catch (_: Exception) {}
            finally { verificacionCargando = false }
        }
    }

    fun enviarVerificacion(numeroId: String, nombreFinca: String, descFinca: String, lat: Double?, lng: Double?, municipio: String?, fotoCedula: String?, fotoFinca: String?, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            verificacionEnviando = true
            try {
                val body = mutableMapOf<String, Any>("numeroIdentidad" to numeroId, "nombreFinca" to nombreFinca, "descripcionFinca" to descFinca)
                lat?.let { body["latitud"] = it }; lng?.let { body["longitud"] = it }
                municipio?.let { if (it.isNotBlank()) body["municipioOrigen"] = it }
                fotoCedula?.let { body["fotoCedulaUrl"] = it }; fotoFinca?.let { body["fotoFincaUrl"] = it }
                val resp = RetrofitClient.usuarioApiService.enviarVerificacion(body)
                val ok = resp["success"] as? Boolean ?: false
                if (ok) cargarEstadoVerificacion()
                onResult(ok)
            } catch (_: Exception) { onResult(false) }
            finally { verificacionEnviando = false }
        }
    }
}
