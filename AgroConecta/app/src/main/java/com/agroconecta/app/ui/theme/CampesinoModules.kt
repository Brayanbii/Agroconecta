package com.agroconecta.app.ui.theme

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.agroconecta.app.viewmodel.TiendaViewModel

private val Emerald = Color(0xFF0E793D)
private val EmeraldLight = Color(0xFFE8F5E9)
private val AppBackground = Color(0xFFF9FBF9)
private val Carbon = Color(0xFF111827)
private val Slate400 = Color(0xFF94A3B8)
private val Slate500 = Color(0xFF64748B)
private val Slate200 = Color(0xFFE2E8F0)
private val Slate100 = Color(0xFFF1F5F9)

// ===== REUSABLE PLACEHOLDER =====
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScreenPlaceholder(title: String, icon: androidx.compose.ui.graphics.vector.ImageVector, subtitle: String, features: List<String>, onBack: () -> Unit) {
    Scaffold(
        containerColor = AppBackground,
        topBar = {
            Surface(shadowElevation = 0.dp, color = Color.White) {
                Row(modifier = Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = 8.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Carbon) }
                    Text(title, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = Carbon)
                }
            }
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Spacer(Modifier.height(40.dp))
            Box(modifier = Modifier.size(100.dp).clip(CircleShape).background(EmeraldLight), contentAlignment = Alignment.Center) {
                Icon(icon, null, tint = Emerald, modifier = Modifier.size(50.dp))
            }
            Spacer(Modifier.height(24.dp))
            Text(title, fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = Carbon)
            Spacer(Modifier.height(8.dp))
            Text(subtitle, fontSize = 14.sp, color = Slate400, textAlign = TextAlign.Center)
            Spacer(Modifier.height(32.dp))
            features.forEach { feat ->
                Surface(color = Color.White, shape = RoundedCornerShape(12.dp), border = BorderStroke(1.dp, Slate200), modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                    Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.CheckCircle, null, tint = Emerald, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(12.dp))
                        Text(feat, fontSize = 14.sp, color = Carbon)
                    }
                }
            }
            Spacer(Modifier.height(24.dp))
            Text("Modulo en desarrollo", fontSize = 13.sp, color = Slate400, textAlign = TextAlign.Center)
        }
    }
}
