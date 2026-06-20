package com.agroconecta.app.data.model

data class InformeResponse(
    val success: Boolean = false,
    val error: String? = null,
    val resumen: ResumenAnaliticas? = null,
    val grafico_top_productos: GraficoDatos? = null,
    val grafico_distribucion: GraficoDatos? = null,
    val grafico_ingresos_mes: GraficoDatos? = null,
    val grafico_vs_mercado: GraficoVsMercado? = null
)

data class ResumenAnaliticas(
    val total_ingresos: Double = 0.0,
    val total_unidades: Int = 0,
    val total_productos: Int = 0,
    val producto_estrella: String = "N/A",
    val mejor_mes: String = "N/A"
)

data class GraficoDatos(
    val labels: List<String>? = null,
    val series: List<SerieDato>? = null
)

data class SerieDato(
    val name: String = "",
    val data: List<Double>? = null
)

data class GraficoVsMercado(
    val labels: List<String>? = null,
    val series: List<SerieDato>? = null
)
