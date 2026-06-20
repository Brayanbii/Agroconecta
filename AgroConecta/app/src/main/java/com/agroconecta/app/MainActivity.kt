package com.agroconecta.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.agroconecta.app.data.api.RetrofitClient
import com.agroconecta.app.data.session.SessionManager
import com.agroconecta.app.ui.navigation.AppDestination
import com.agroconecta.app.ui.navigation.AppNavGraph
import com.agroconecta.app.ui.theme.AgroConectaTheme
import com.agroconecta.app.ui.theme.SplashScreen
import com.agroconecta.app.viewmodel.AuthViewModel
import com.agroconecta.app.viewmodel.TiendaViewModel

class MainActivity : ComponentActivity() {

    private val authViewModel = AuthViewModel()
    private val tiendaViewModel = TiendaViewModel()
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        RetrofitClient.init(this) // Restaurar cookies de sesion
        sessionManager = SessionManager(this)

        val usuarioGuardado = sessionManager.getUser()
        if (usuarioGuardado != null) {
            authViewModel.setUsuarioInicial(usuarioGuardado)
        }

        setContent {
            val currentRole by remember {
                derivedStateOf {
                    authViewModel.usuarioLogueado?.rol ?: "CLIENTE"
                }
            }

            AgroConectaTheme(role = currentRole) {
                var showSplash by remember { mutableStateOf(true) }

                if (showSplash) {
                    SplashScreen(onFinished = { showSplash = false })
                } else {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        val startDestination = remember(usuarioGuardado) {
                            if (usuarioGuardado != null) {
                                if (usuarioGuardado.rol == "CAMPESINO") AppDestination.CampesinoDashboard.route
                                else AppDestination.Tienda.route
                            } else {
                                AppDestination.Login.route
                            }
                        }

                        AppNavGraph(
                            authViewModel = authViewModel,
                            tiendaViewModel = tiendaViewModel,
                            sessionManager = sessionManager,
                            startDestination = startDestination
                        )
                    }
                }
            }
        }
    }
}
