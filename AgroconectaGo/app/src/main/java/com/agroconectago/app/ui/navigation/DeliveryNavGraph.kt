package com.agroconectago.app.ui.navigation

import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.agroconectago.app.data.api.DeliveryRetrofitClient
import com.agroconectago.app.data.model.DeliveryUsuarioInfo
import com.agroconectago.app.data.session.DeliverySessionManager
import com.agroconectago.app.ui.DeliveryLoginScreen
import com.agroconectago.app.ui.DeliveryDashboardScreen
import com.agroconectago.app.ui.DeliveryRegisterScreen
import com.agroconectago.app.ui.DeliveryProfileCompletionScreen
import com.agroconectago.app.ui.DeliveryDocumentosScreen
import com.agroconectago.app.ui.DeliveryVerificacionScreen
import com.agroconectago.app.ui.RutasDisponiblesScreen
import com.agroconectago.app.ui.RutaDetalleScreen
import com.agroconectago.app.ui.RutaActivaScreen
import com.agroconectago.app.ui.MapaCompletoScreen
import com.agroconectago.app.ui.EntregaExitosaScreen
import com.agroconectago.app.ui.MisViajesScreen
import com.agroconectago.app.ui.DeliveryPerfilScreen
import com.agroconectago.app.ui.theme.DeliverySplashScreen
import com.agroconectago.app.viewmodel.DeliveryAuthViewModel
import kotlinx.coroutines.launch

enum class PerfilEstado {
    INCOMPLETO,       // falta ciudad o vehiculo
    COMPLETO_SIN_DOCS, // perfil completo pero docs incompletos
    EN_REVISION,      // documentos enviados, esperando aprobacion
    APROBADO          // listo para trabajar
}

@Composable
fun DeliveryNavGraph(
    navController: NavHostController,
    sessionManager: DeliverySessionManager
) {
    val authViewModel: DeliveryAuthViewModel = viewModel()
    val scope = rememberCoroutineScope()

    // Funcion que determina a donde ir segun el estado del perfil
    fun resolverRuta(perfilCompleto: Boolean, estadoVerif: String): String {
        return when {
            !perfilCompleto -> DeliveryRoute.ProfileCompletion.route
            estadoVerif == "EN_REVISION" -> DeliveryRoute.Verificacion.route
            estadoVerif == "PENDIENTE_DATOS" -> DeliveryRoute.Documentos.route
            estadoVerif == "RECHAZADO" -> DeliveryRoute.Documentos.route
            estadoVerif == "APROBADO" -> DeliveryRoute.Dashboard.route
            else -> DeliveryRoute.Documentos.route
        }
    }

    // Funcion que verifica perfil y navega a la ruta correcta
    fun verificarYNavegar(user: DeliveryUsuarioInfo, popRoute: String) {
        scope.launch {
            var perfilCompleto = false
            var estadoVerif = ""
            var sesionValida = false
            var rutaActivaId: Long? = null
            try {
                val profile = DeliveryRetrofitClient.api.getProfile()
                sesionValida = profile.success
                val ciudad = profile.municipioOrigen ?: ""
                val vehiculo = profile.tipoVehiculo ?: ""
                perfilCompleto = ciudad.isNotBlank() && vehiculo.isNotBlank()
                estadoVerif = profile.estadoVerificacion ?: ""

                // Verificar si hay una ruta activa (ASIGNADA o EN_CAMINO)
                if (estadoVerif == "APROBADO") {
                    try {
                        val rutasResp = DeliveryRetrofitClient.api.misRutas()
                        val rutas = (rutasResp["rutas"] as? List<*>)?.filterIsInstance<Map<String, Any?>>() ?: emptyList()
                        val activa = rutas.find {
                            val estado = it["estado"]?.toString() ?: ""
                            estado == "ASIGNADA" || estado == "EN_CAMINO"
                        }
                        if (activa != null) {
                            rutaActivaId = (activa["id"] as? Number)?.toLong()
                        }
                    } catch (_: Exception) {}
                }
            } catch (_: Exception) {}
            if (!sesionValida) {
                sessionManager.clearSession()
                authViewModel.limpiarEstados()
                navController.navigate(DeliveryRoute.Login.route) {
                    popUpTo(popRoute) { inclusive = true }
                }
            } else {
                val destino = if (rutaActivaId != null) {
                    DeliveryRoute.RutaActiva.createRoute(rutaActivaId)
                } else {
                    resolverRuta(perfilCompleto, estadoVerif)
                }
                navController.navigate(destino) {
                    popUpTo(popRoute) { inclusive = true }
                }
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = DeliveryRoute.Splash.route
    ) {
        composable(DeliveryRoute.Splash.route) {
            DeliverySplashScreen(
                onFinished = {
                    val user = sessionManager.getUser()
                    if (user != null && user.rol == "REPARTIDOR") {
                        authViewModel.setUsuarioInicial(user)
                        verificarYNavegar(user, DeliveryRoute.Splash.route)
                    } else {
                        navController.navigate(DeliveryRoute.Login.route) {
                            popUpTo(DeliveryRoute.Splash.route) { inclusive = true }
                        }
                    }
                }
            )
        }

        composable(DeliveryRoute.Login.route) {
            val usuarioLogueado by authViewModel.usuarioLogueado.collectAsState()

            LaunchedEffect(usuarioLogueado) {
                usuarioLogueado?.let { user ->
                    sessionManager.saveUser(user)
                    verificarYNavegar(user, DeliveryRoute.Login.route)
                }
            }

            DeliveryLoginScreen(
                viewModel = authViewModel,
                sessionManager = sessionManager,
                onNavigateToDashboard = { },
                onNavigateToRegister = {
                    navController.navigate(DeliveryRoute.Register.route)
                }
            )
        }

        composable(DeliveryRoute.Register.route) {
            DeliveryRegisterScreen(
                viewModel = authViewModel,
                onNavigateToLogin = { navController.popBackStack() },
                onProfileCompletion = {
                    navController.navigate(DeliveryRoute.ProfileCompletion.route) {
                        popUpTo(DeliveryRoute.Register.route) { inclusive = true }
                    }
                }
            )
        }

        composable(DeliveryRoute.ProfileCompletion.route) {
            DeliveryProfileCompletionScreen(
                viewModel = authViewModel,
                onProfileComplete = {
                    navController.navigate(DeliveryRoute.Documentos.route) {
                        popUpTo(DeliveryRoute.ProfileCompletion.route) { inclusive = true }
                    }
                }
            )
        }

        composable(DeliveryRoute.Documentos.route) {
            DeliveryDocumentosScreen(
                viewModel = authViewModel,
                onComplete = {
                    navController.navigate(DeliveryRoute.Verificacion.route) {
                        popUpTo(DeliveryRoute.Documentos.route) { inclusive = true }
                    }
                },
                onBack = {
                    navController.navigate(DeliveryRoute.ProfileCompletion.route) {
                        popUpTo(DeliveryRoute.Documentos.route) { inclusive = true }
                    }
                }
            )
        }

        composable(DeliveryRoute.Verificacion.route) {
            DeliveryVerificacionScreen(
                onSalir = {
                    navController.navigate(DeliveryRoute.Dashboard.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable(DeliveryRoute.Dashboard.route) {
            val usuarioLogueado by authViewModel.usuarioLogueado.collectAsState()

            DeliveryDashboardScreen(
                usuarioLogueado = usuarioLogueado,
                onLogout = {
                    sessionManager.clearSession()
                    authViewModel.limpiarEstados()
                    navController.navigate(DeliveryRoute.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onVerRutas = {
                    navController.navigate(DeliveryRoute.RutasDisponibles.route)
                },
                onMisViajes = {
                    navController.navigate(DeliveryRoute.MisViajes.route)
                },
                onMiPerfil = {
                    navController.navigate(DeliveryRoute.MiPerfil.route)
                },
                onRutaSeleccionada = { rutaId ->
                    navController.navigate(DeliveryRoute.RutaDetalle.createRoute(rutaId))
                }
            )
        }

        composable(DeliveryRoute.RutasDisponibles.route) {
            RutasDisponiblesScreen(
                onBack = { navController.popBackStack() },
                onRutaSeleccionada = { rutaId ->
                    navController.navigate(DeliveryRoute.RutaDetalle.createRoute(rutaId))
                }
            )
        }

        composable(DeliveryRoute.RutaDetalle.route) { backStackEntry ->
            val rutaId = backStackEntry.arguments?.getString("rutaId")?.toLongOrNull() ?: 0L
            RutaDetalleScreen(
                rutaId = rutaId,
                onBack = { navController.popBackStack() },
                onRutaAceptada = {
                    navController.navigate(DeliveryRoute.RutaActiva.createRoute(rutaId)) {
                        popUpTo(DeliveryRoute.RutasDisponibles.route) { inclusive = true }
                    }
                }
            )
        }

        composable(DeliveryRoute.RutaActiva.route) { backStackEntry ->
            val rutaId = backStackEntry.arguments?.getString("rutaId")?.toLongOrNull() ?: 0L
            RutaActivaScreen(
                rutaId = rutaId,
                onBack = { navController.popBackStack() },
                onLlegue = {
                    navController.navigate(DeliveryRoute.Dashboard.route) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onNavigateToMapa = { id ->
                    navController.navigate(DeliveryRoute.MapaCompleto.createRoute(id))
                },
                onEntregaExitosa = { id, dist, pago, finca, cod ->
                    navController.navigate(DeliveryRoute.EntregaExitosa.createRoute(id, dist, pago, finca, cod)) {
                        popUpTo(DeliveryRoute.RutaActiva.route) { inclusive = true }
                    }
                }
            )
        }

        composable(DeliveryRoute.MapaCompleto.route) { backStackEntry ->
            val rutaId = backStackEntry.arguments?.getString("rutaId")?.toLongOrNull() ?: 0L
            MapaCompletoScreen(
                rutaId = rutaId,
                onBack = { navController.popBackStack() }
            )
        }

        composable(DeliveryRoute.EntregaExitosa.route) { backStackEntry ->
            val rutaId = backStackEntry.arguments?.getString("rutaId")?.toLongOrNull() ?: 0L
            val dist = backStackEntry.arguments?.getString("distanciaKm")?.toDoubleOrNull() ?: 0.0
            val pago = backStackEntry.arguments?.getString("pago")?.toDoubleOrNull() ?: 0.0
            val finca = backStackEntry.arguments?.getString("finca") ?: ""
            val cod = backStackEntry.arguments?.getString("codigoRuta") ?: ""
            EntregaExitosaScreen(
                rutaId = rutaId,
                distanciaKm = dist,
                pago = pago,
                finca = finca,
                codigoRuta = cod,
                onBackToDashboard = {
                    navController.navigate(DeliveryRoute.Dashboard.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable(DeliveryRoute.MisViajes.route) {
            MisViajesScreen(onBack = { navController.popBackStack() })
        }

        composable(DeliveryRoute.MiPerfil.route) {
            DeliveryPerfilScreen(onBack = { navController.popBackStack() })
        }
    }
}
