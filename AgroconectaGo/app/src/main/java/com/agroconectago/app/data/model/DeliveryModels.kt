package com.agroconectago.app.data.model

data class DeliveryLoginResponse(
    val success: Boolean,
    val message: String?,
    val user: DeliveryUsuarioInfo?
)

data class DeliveryUsuarioInfo(
    val id: Long,
    val userName: String,
    val email: String,
    val rol: String
)

data class DeliveryRegisterRequest(
    val userName: String,
    val password: String,
    val rol: String = "REPARTIDOR",
    val nombreCompleto: String,
    val email: String,
    val telefono: String,
    val numeroIdentidad: String = "",
    val tipoVehiculo: String = "",
    val placaVehiculo: String = "",
    val marcaVehiculo: String = "",
    val modeloVehiculo: String = "",
    val anioVehiculo: Int = 0,
    val capacidadCargaKg: Double = 0.0,
    val licenciaConduccion: String = "",
    val latitud: Double = 0.0,
    val longitud: Double = 0.0,
    val municipioOrigen: String = ""
)

data class DeliveryRegisterResponse(
    val success: Boolean,
    val message: String?,
    val user: DeliveryUsuarioInfo?
)

data class DeliveryProfileUpdateRequest(
    val municipioOrigen: String = "",
    val tipoVehiculo: String = "",
    val placaVehiculo: String = "",
    val marcaVehiculo: String = "",
    val modeloVehiculo: String = "",
    val anioVehiculo: Int = 0,
    val capacidadCargaKg: Double = 0.0,
    val licenciaConduccion: String = "",
    val colorVehiculo: String = "",
    val numeroIdentidad: String = "",
    val fechaNacimiento: String = ""
)

data class DeliveryProfileResponse(
    val success: Boolean,
    val message: String? = null,
    val municipioOrigen: String? = null,
    val tipoVehiculo: String? = null,
    val placaVehiculo: String? = null,
    val marcaVehiculo: String? = null,
    val modeloVehiculo: String? = null,
    val anioVehiculo: Int? = null,
    val capacidadCargaKg: Double? = null,
    val licenciaConduccion: String? = null,
    val colorVehiculo: String? = null,
    val estadoVerificacion: String? = null,
    val fotoLicenciaFrontalUrl: String? = null,
    val fotoLicenciaTraseraUrl: String? = null,
    val fotoTarjetaPropiedadUrl: String? = null,
    val fotoSOATUrl: String? = null,
    val fotoTecnomecanicaUrl: String? = null,
    val fotoPerfil: String? = null,
    val fotoCedulaUrl: String? = null,
    val numeroIdentidad: String? = null,
    val fechaNacimiento: String? = null,
    val motivoRechazo: String? = null
)
