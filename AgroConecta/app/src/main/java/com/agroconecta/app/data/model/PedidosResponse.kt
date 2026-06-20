package com.agroconecta.app.data.model

data class PedidosResponse(
    val success: Boolean = false,
    val error: String? = null,
    val ventas: List<VentaItem>? = null,
    val conteos: ConteosPedidos? = null
)

data class ConteosPedidos(
    val nuevos: Long = 0,
    val preparados: Long = 0,
    val listosParaRecoger: Long = 0,
    val enCamino: Long = 0,
    val entregados: Long = 0,
    val cancelados: Long = 0
)

data class VentaItem(
    val id: Long? = null,
    val nombre: String = "",
    val precio: Double? = null,
    val cantidad: Int? = null,
    val total: Double? = null,
    val estado: String = "NUEVO",
    val ordenId: Long? = null,
    val fechaOrden: String? = null,
    val clienteNombre: String? = null,
    val direccionEnvio: String? = null,
    val unidad: String? = "Kg",
    val codigoRecogida: String? = null,
    val codigoRuta: String? = null,
    val repartidor: RepartidorInfo? = null
)

data class RepartidorInfo(
    val nombre: String? = null,
    val telefono: String? = null,
    val tipoVehiculo: String? = null,
    val placa: String? = null,
    val latitud: Double? = null,
    val longitud: Double? = null,
    val fotoPerfil: String? = null,
    val calificacion: Double? = null,
    val estadoRuta: String? = null,
    val fincaLat: Double? = null,
    val fincaLng: Double? = null
)

data class LogisticaResponse(
    val success: Boolean = false,
    val error: String? = null,
    val origenLat: Double? = null,
    val origenLon: Double? = null,
    val destLat: Double? = null,
    val destLon: Double? = null,
    val producto: String? = null,
    val cantidad: Int? = null,
    val total: Double? = null,
    val direccionEnvio: String? = null,
    val clienteNombre: String? = null,
    val distancia_km: Double? = null,
    val duracion_min: Double? = null,
    val geometria: Any? = null
)
