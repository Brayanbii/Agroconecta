package com.agroconecta.app.ui.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.agroconecta.app.data.session.SessionManager
import com.agroconecta.app.ui.LoginScreen
import com.agroconecta.app.ui.RegisterScreen
import com.agroconecta.app.ui.theme.ClienteCarritoScreen
import com.agroconecta.app.ui.theme.*
import com.agroconecta.app.viewmodel.AuthViewModel
import com.agroconecta.app.viewmodel.TiendaViewModel

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun AppNavGraph(
    authViewModel: AuthViewModel,
    tiendaViewModel: TiendaViewModel,
    sessionManager: SessionManager,
    startDestination: String,
    navController: NavHostController = rememberNavController()
) {
    val usuarioLogueado by remember { derivedStateOf { authViewModel.usuarioLogueado } }

    LaunchedEffect(usuarioLogueado) {
        usuarioLogueado?.let { user ->
            sessionManager.saveUser(user)
            val dest = if (user.rol.equals("CAMPESINO", ignoreCase = true)) AppDestination.CampesinoDashboard.route
                       else AppDestination.Tienda.route
            navController.navigate(dest) {
                popUpTo(AppDestination.Login.route) { inclusive = true }
                launchSingleTop = true
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = startDestination,
        enterTransition = {
            slideInVertically(
                initialOffsetY = { it / 4 },
                animationSpec = tween(400, easing = FastOutSlowInEasing)
            ) + fadeIn(animationSpec = tween(350))
        },
        exitTransition = {
            fadeOut(animationSpec = tween(250))
        },
        popEnterTransition = {
            fadeIn(animationSpec = tween(350))
        },
        popExitTransition = {
            slideOutVertically(
                targetOffsetY = { it / 4 },
                animationSpec = tween(400, easing = FastOutSlowInEasing)
            ) + fadeOut(animationSpec = tween(300))
        }
    ) {
        composable(
            route = AppDestination.Login.route,
            enterTransition = {
                scaleIn(initialScale = 0.92f, animationSpec = tween(450, easing = FastOutSlowInEasing)) +
                        fadeIn(animationSpec = tween(400))
            },
            exitTransition = {
                scaleOut(targetScale = 0.92f, animationSpec = tween(350)) +
                        fadeOut(animationSpec = tween(300))
            }
        ) {
            LoginScreen(
                viewModel = authViewModel,
                sessionManager = sessionManager,
                onNavigateToRegister = { navController.navigate(AppDestination.Register.route) }
            )
        }

        composable(
            route = AppDestination.Register.route,
            enterTransition = {
                slideInHorizontally(
                    initialOffsetX = { it },
                    animationSpec = tween(450, easing = FastOutSlowInEasing)
                ) + fadeIn(animationSpec = tween(400))
            },
            exitTransition = {
                slideOutHorizontally(
                    targetOffsetX = { -it / 3 },
                    animationSpec = tween(400, easing = FastOutSlowInEasing)
                ) + fadeOut(animationSpec = tween(300))
            }
        ) {
            RegisterScreen(
                viewModel = authViewModel,
                onNavigateToLogin = { navController.popBackStack() }
            )
        }

        composable(
            route = AppDestination.Tienda.route,
            enterTransition = {
                fadeIn(animationSpec = tween(500, easing = FastOutSlowInEasing))
            },
            exitTransition = {
                fadeOut(animationSpec = tween(300))
            }
        ) {
            val usuario = authViewModel.usuarioLogueado ?: sessionManager.getUser()
            if (usuario != null) {
                TiendaScreen(
                    usuario = usuario,
                    tiendaVM = tiendaViewModel,
                    onNavigateToPerfil = { navController.navigate(AppDestination.Perfil.route) },
                    onNavigateToDirecciones = { navController.navigate(AppDestination.Direcciones.route) },
                    onNavigateToFavoritos = { navController.navigate(AppDestination.Favoritos.route) },
                    onProductoClick = { producto ->
                        navController.navigate("${AppDestination.ProductoDetail.route}/${producto.id}")
                    },
                    onCarritoClick = { navController.navigate(AppDestination.Carrito.route) }
                )
            }
        }

        composable(
            route = "${AppDestination.ProductoDetail.route}/{productoId}",
            arguments = listOf(navArgument("productoId") { type = NavType.LongType }),
            enterTransition = {
                scaleIn(
                    initialScale = 0.85f,
                    animationSpec = tween(500, easing = FastOutSlowInEasing)
                ) + fadeIn(animationSpec = tween(400))
            },
            exitTransition = {
                scaleOut(
                    targetScale = 0.85f,
                    animationSpec = tween(400)
                ) + fadeOut(animationSpec = tween(300))
            },
            popEnterTransition = {
                scaleIn(
                    initialScale = 0.9f,
                    animationSpec = tween(450, easing = FastOutSlowInEasing)
                ) + fadeIn(animationSpec = tween(350))
            },
            popExitTransition = {
                scaleOut(
                    targetScale = 0.9f,
                    animationSpec = tween(400)
                ) + fadeOut(animationSpec = tween(300))
            }
        ) { backStackEntry ->
            val productoId = backStackEntry.arguments?.getLong("productoId") ?: 0L
            tiendaViewModel.obtenerProductoPorId(productoId)?.let { producto ->
                ProductDetailScreen(
                    producto = producto,
                    tiendaVM = tiendaViewModel,
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToCampesino = { id -> navController.navigate("${AppDestination.CampesinoPerfil.route}/$id") },
                    onProductoClick = { prod -> navController.navigate("${AppDestination.ProductoDetail.route}/${prod.id}") }
                )
            }
        }

        composable(
            route = "${AppDestination.CampesinoPerfil.route}/{campesinoId}",
            arguments = listOf(navArgument("campesinoId") { type = NavType.LongType }),
            enterTransition = {
                slideInVertically(
                    initialOffsetY = { it / 3 },
                    animationSpec = tween(450, easing = FastOutSlowInEasing)
                ) + fadeIn(animationSpec = tween(400))
            },
            exitTransition = {
                slideOutVertically(
                    targetOffsetY = { it / 3 },
                    animationSpec = tween(400)
                ) + fadeOut(animationSpec = tween(300))
            }
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getLong("campesinoId") ?: 0L
            if (id <= 0L) {
                LaunchedEffect(Unit) { navController.popBackStack() }
                return@composable
            }
            CampesinoPerfilScreen(
                campesinoId = id,
                tiendaVM = tiendaViewModel,
                onNavigateBack = { navController.popBackStack() },
                onProductoClick = { prod -> navController.navigate("${AppDestination.ProductoDetail.route}/${prod.id}") }
            )
        }

        composable(
            route = AppDestination.Carrito.route,
            enterTransition = {
                slideInVertically(
                    initialOffsetY = { it / 5 },
                    animationSpec = tween(450, easing = FastOutSlowInEasing)
                ) + fadeIn(animationSpec = tween(400))
            },
            exitTransition = {
                slideOutVertically(
                    targetOffsetY = { it / 5 },
                    animationSpec = tween(400)
                ) + fadeOut(animationSpec = tween(300))
            }
        ) {
            CarritoScreen(
                tiendaVM = tiendaViewModel,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToCheckout = { navController.navigate(AppDestination.Checkout.route) },
                onNavigateToTienda = { navController.navigate(AppDestination.Tienda.route) { popUpTo(AppDestination.Tienda.route) { inclusive = true } } }
            )
        }

        composable(
            route = AppDestination.Checkout.route,
            enterTransition = {
                slideInHorizontally(
                    initialOffsetX = { it },
                    animationSpec = tween(500, easing = FastOutSlowInEasing)
                ) + fadeIn(animationSpec = tween(400))
            },
            exitTransition = {
                slideOutHorizontally(
                    targetOffsetX = { -it / 4 },
                    animationSpec = tween(400)
                ) + fadeOut(animationSpec = tween(300))
            }
        ) {
            CheckoutScreen(
                items = tiendaViewModel.carritoItems,
                subtotal = tiendaViewModel.carritoSubtotal,
                tiendaVM = tiendaViewModel,
                onNavigateBack = { navController.popBackStack() },
                onPaymentSuccess = { navController.navigate(AppDestination.CompraExitosa.route) { popUpTo(AppDestination.Checkout.route) { inclusive = true } } }
            )
        }

        composable(
            route = AppDestination.CompraExitosa.route,
            enterTransition = {
                scaleIn(
                    initialScale = 0.7f,
                    animationSpec = tween(600, easing = EaseOutBack)
                ) + fadeIn(animationSpec = tween(500))
            }
        ) {
            CompraExitosaScreen(
                onNavigateToTienda = { navController.navigate(AppDestination.Tienda.route) { popUpTo(0) { inclusive = true } } },
                onNavigateToCompras = { navController.navigate(AppDestination.MisCompras.route) { popUpTo(0) { inclusive = true } } }
            )
        }

        composable(
            route = AppDestination.Favoritos.route,
            enterTransition = {
                slideInVertically(
                    initialOffsetY = { it / 4 },
                    animationSpec = tween(450, easing = FastOutSlowInEasing)
                ) + fadeIn(animationSpec = tween(400))
            },
            exitTransition = {
                slideOutVertically(
                    targetOffsetY = { it / 4 },
                    animationSpec = tween(400)
                ) + fadeOut(animationSpec = tween(300))
            }
        ) {
            FavoritosScreen(
                tiendaVM = tiendaViewModel,
                onProductoClick = { producto ->
                    navController.navigate("${AppDestination.ProductoDetail.route}/${producto.id}")
                },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = AppDestination.Perfil.route,
            enterTransition = {
                slideInHorizontally(
                    initialOffsetX = { it / 2 },
                    animationSpec = tween(450, easing = FastOutSlowInEasing)
                ) + fadeIn(animationSpec = tween(400))
            },
            exitTransition = {
                slideOutHorizontally(
                    targetOffsetX = { it / 2 },
                    animationSpec = tween(400)
                ) + fadeOut(animationSpec = tween(300))
            }
        ) {
            val usuario = authViewModel.usuarioLogueado ?: sessionManager.getUser()
            if (usuario != null) {
                ClientePerfilScreen(
                    usuario = usuario,
                    tiendaVM = tiendaViewModel,
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToDirecciones = { navController.navigate(AppDestination.Direcciones.route) },
                    onNavigateToCompras = { navController.navigate(AppDestination.MisCompras.route) },
                    onNavigateToFavoritos = { navController.navigate(AppDestination.Favoritos.route) },
                    onLogout = {
                        sessionManager.clearSession()
                        authViewModel.limpiarEstados()
                        navController.navigate(AppDestination.Login.route) {
                            popUpTo(AppDestination.Tienda.route) { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                )
            }
        }

        composable(
            route = AppDestination.Direcciones.route,
            enterTransition = {
                slideInVertically(
                    initialOffsetY = { it / 3 },
                    animationSpec = tween(450, easing = FastOutSlowInEasing)
                ) + fadeIn(animationSpec = tween(400))
            },
            exitTransition = {
                slideOutVertically(
                    targetOffsetY = { it / 3 },
                    animationSpec = tween(400)
                ) + fadeOut(animationSpec = tween(300))
            }
        ) {
            DireccionesScreen(
                tiendaVM = tiendaViewModel,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToMap = { /* Map opens inline, no nav needed */ }
            )
        }

        composable(
            route = AppDestination.Resenas.route,
            enterTransition = {
                slideInVertically(
                    initialOffsetY = { it / 4 },
                    animationSpec = tween(450, easing = FastOutSlowInEasing)
                ) + fadeIn(animationSpec = tween(400))
            },
            exitTransition = {
                slideOutVertically(
                    targetOffsetY = { it / 4 },
                    animationSpec = tween(400)
                ) + fadeOut(animationSpec = tween(300))
            }
        ) {
            ResenasScreen(onNavigateBack = { navController.popBackStack() })
        }

        composable(
            route = AppDestination.MisCompras.route,
            enterTransition = {
                slideInVertically(
                    initialOffsetY = { it / 4 },
                    animationSpec = tween(450, easing = FastOutSlowInEasing)
                ) + fadeIn(animationSpec = tween(400))
            },
            exitTransition = {
                slideOutVertically(
                    targetOffsetY = { it / 4 },
                    animationSpec = tween(400)
                ) + fadeOut(animationSpec = tween(300))
            }
        ) {
            MisComprasScreen(
                tiendaVM = tiendaViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(AppDestination.CampesinoDashboard.route) {
            val usuario = authViewModel.usuarioLogueado ?: sessionManager.getUser()
            if (usuario != null) {
                LaunchedEffect(Unit) { tiendaViewModel.cargarEstadoVerificacion() }
                val resp = tiendaViewModel.verificacionResponse
                val cargando = tiendaViewModel.verificacionCargando

                if (cargando && resp == null) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Color(0xFF16A34A))
                    }
                } else if (resp != null && resp.success == true && resp.estado != null && resp.estado != "APROBADO") {
                    CampesinoVerificacionScreen(
                        tiendaVM = tiendaViewModel,
                        onVerificacionCompleta = { tiendaViewModel.cargarEstadoVerificacion() },
                        onLogout = {
                            sessionManager.clearSession()
                            authViewModel.limpiarEstados()
                            navController.navigate(AppDestination.Login.route) {
                                popUpTo(AppDestination.CampesinoDashboard.route) { inclusive = true }
                                launchSingleTop = true
                            }
                        }
                    )
                } else {
                    CampesinoPanel(
                    usuario = usuario,
                    viewModel = authViewModel,
                    tiendaVM = tiendaViewModel,
                    onLogout = {
                        sessionManager.clearSession()
                        authViewModel.limpiarEstados()
                        navController.navigate(AppDestination.Login.route) {
                            popUpTo(AppDestination.CampesinoDashboard.route) { inclusive = true }
                            launchSingleTop = true
                        }
                    },
                    onNavigate = { route ->
                        when (route) {
                            "productos" -> navController.navigate(AppDestination.CampesinoProductos.route)
                            "inventario" -> navController.navigate(AppDestination.Inventario.route)
                            "pedidos" -> navController.navigate(AppDestination.PedidosCampesino.route)
                            "form" -> navController.navigate("${AppDestination.ProductoForm.route}/0")
                            "sipsa" -> navController.navigate(AppDestination.Sipsa.route)
                            "analiticas" -> navController.navigate(AppDestination.Analiticas.route)
                            "reputacion" -> navController.navigate(AppDestination.Reputacion.route)
                            "finanzas" -> navController.navigate(AppDestination.Finanzas.route)
                            "logistica" -> navController.navigate(AppDestination.Logistica.route)
                            "perfil" -> navController.navigate(AppDestination.CampesinoPerfilEdit.route)
                            "soporte" -> navController.navigate(AppDestination.Soporte.route)
                        }
                    }
                )
            }
                    }
            }

        composable(AppDestination.CampesinoProductos.route) {
            CampesinoProductosScreen(
                authVM = authViewModel, tiendaVM = tiendaViewModel,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToForm = { id -> navController.navigate("${AppDestination.ProductoForm.route}/${id ?: 0L}") },
                onNavigateToSipsa = { navController.navigate(AppDestination.Sipsa.route) }
            )
        }

        composable(AppDestination.Inventario.route) { InventarioScreen(tiendaVM = tiendaViewModel, onNavigateBack = { navController.popBackStack() }) }
        composable(AppDestination.PedidosCampesino.route) { PedidosCampesinoScreen(tiendaVM = tiendaViewModel, onNavigateBack = { navController.popBackStack() }, onNavigateToLogistica = { id -> navController.navigate("${AppDestination.Logistica.route}/$id") }) }
        composable(AppDestination.Analiticas.route) { AnaliticasScreen(tiendaVM = tiendaViewModel, onNavigateBack = { navController.popBackStack() }) }
        composable(AppDestination.Reputacion.route) { ReputacionScreen(tiendaVM = tiendaViewModel, onNavigateBack = { navController.popBackStack() }) }
        composable(AppDestination.Finanzas.route) { FinanzasScreen(tiendaVM = tiendaViewModel, onNavigateBack = { navController.popBackStack() }) }
        composable(
            route = "${AppDestination.Logistica.route}/{pedidoId}",
            arguments = listOf(navArgument("pedidoId") { type = NavType.LongType })
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getLong("pedidoId") ?: 0L
            LogisticaScreen(pedidoId = id, tiendaVM = tiendaViewModel, onNavigateBack = { navController.popBackStack() })
        }
        composable(AppDestination.CampesinoPerfilEdit.route) {
            CampesinoPerfilScreen(tiendaVM = tiendaViewModel, onNavigateBack = { navController.popBackStack() })
        }
        composable(AppDestination.Soporte.route) {
            SoporteScreen(tiendaVM = tiendaViewModel, onNavigateBack = { navController.popBackStack() })
        }

        composable(
            route = "${AppDestination.ProductoForm.route}/{productoId}",
            arguments = listOf(navArgument("productoId") { type = NavType.LongType; defaultValue = 0L })
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getLong("productoId") ?: 0L
            ProductoFormScreen(productoId = if (id > 0L) id else null, tiendaVM = tiendaViewModel, onNavigateBack = { navController.popBackStack() }, onSipsaExplorar = { term -> tiendaViewModel.consultarSipsa(term) })
        }

        composable(AppDestination.Sipsa.route) {
            SipsaExplorerScreen(tiendaVM = tiendaViewModel, onNavigateBack = { navController.popBackStack() })
        }
    }
}
