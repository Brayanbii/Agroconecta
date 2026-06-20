package com.agroconectago.app.ui.navigation

sealed class DeliveryRoute(val route: String) {
    object Splash : DeliveryRoute("splash")
    object Login : DeliveryRoute("login")
    object Register : DeliveryRoute("register")
    object ProfileCompletion : DeliveryRoute("profile_completion")
    object Documentos : DeliveryRoute("documentos")
    object Verificacion : DeliveryRoute("verificacion")
    object Dashboard : DeliveryRoute("dashboard")
    object RutasDisponibles : DeliveryRoute("rutas_disponibles")
    object RutaDetalle : DeliveryRoute("ruta_detalle/{rutaId}") {
        fun createRoute(id: Long) = "ruta_detalle/$id"
    }
    object RutaActiva : DeliveryRoute("ruta_activa/{rutaId}") {
        fun createRoute(id: Long) = "ruta_activa/$id"
    }
    object MapaCompleto : DeliveryRoute("mapa_completo/{rutaId}") {
        fun createRoute(id: Long) = "mapa_completo/$id"
    }
    object EntregaExitosa : DeliveryRoute("entrega_exitosa/{rutaId}/{distanciaKm}/{pago}/{finca}/{codigoRuta}") {
        fun createRoute(rutaId: Long, distanciaKm: Double, pago: Double, finca: String, codigoRuta: String) =
            "entrega_exitosa/$rutaId/$distanciaKm/$pago/${java.net.URLEncoder.encode(finca, "UTF-8")}/${java.net.URLEncoder.encode(codigoRuta, "UTF-8")}"
    }
    object MisViajes : DeliveryRoute("mis_viajes")
    object MiPerfil : DeliveryRoute("mi_perfil")
}
