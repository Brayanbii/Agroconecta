package com.agroconectago.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.rememberNavController
import com.agroconectago.app.data.api.DeliveryRetrofitClient
import com.agroconectago.app.data.session.DeliverySessionManager
import com.agroconectago.app.ui.navigation.DeliveryNavGraph
import com.agroconectago.app.ui.theme.AgroconectaGoTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        DeliveryRetrofitClient.init(this)
        val sessionManager = DeliverySessionManager(this)
        enableEdgeToEdge()
        setContent {
            AgroconectaGoTheme {
                val navController = rememberNavController()
                DeliveryNavGraph(
                    navController = navController,
                    sessionManager = sessionManager
                )
            }
        }
    }
}
