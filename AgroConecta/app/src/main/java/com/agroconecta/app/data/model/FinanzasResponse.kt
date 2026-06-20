package com.agroconecta.app.data.model

data class FinanzasResponse(
    val success: Boolean = false,
    val error: String? = null,
    val ingresosBrutos: Double = 0.0,
    val ingresosNetos: Double = 0.0,
    val comisionTotal: Double = 0.0,
    val pagoPendiente: Double = 0.0,
    val totalTransacciones: Int = 0,
    val transaccionesCompletadas: Int = 0,
    val historial: List<MovimientoTx>? = null,
    val datosMensuales: List<DatoMensual>? = null
)

data class MovimientoTx(
    val fecha: String = "",
    val tipo: String = "",
    val descripcion: String = "",
    val monto: Double = 0.0,
    val signo: String = "+",
    val estado: String = ""
)

data class DatoMensual(
    val mes: String = "",
    val total: Double = 0.0
)
