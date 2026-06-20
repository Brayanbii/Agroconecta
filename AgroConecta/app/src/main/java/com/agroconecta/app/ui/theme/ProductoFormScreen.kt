package com.agroconecta.app.ui.theme

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import com.agroconecta.app.data.model.Producto
import com.agroconecta.app.viewmodel.TiendaViewModel

private val Emerald = Color(0xFF0E793D)
private val EmeraldDark = Color(0xFF0A5C2E)
private val EmeraldLight = Color(0xFFE8F5E9)
private val AppBackground = Color(0xFFF8FAF9)
private val Carbon = Color(0xFF111827)
private val Slate700 = Color(0xFF334155)
private val Slate400 = Color(0xFF94A3B8)
private val Slate500 = Color(0xFF64748B)
private val Slate200 = Color(0xFFE2E8F0)
private val Slate100 = Color(0xFFF1F5F9)
private val Red500 = Color(0xFFEF4444)
private val Green500 = Color(0xFF22C55E)
private val Blue500 = Color(0xFF3B82F6)
private val Blue600 = Color(0xFF2563EB)
private val Blue50 = Color(0xFFEFF6FF)
private val Blue100 = Color(0xFFDBEAFE)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductoFormScreen(
    productoId: Long? = null,
    tiendaVM: TiendaViewModel,
    onNavigateBack: () -> Unit,
    onSipsaExplorar: (String) -> Unit
) {
    var paso by remember { mutableIntStateOf(1) }
    val totalPasos = 3
    var isVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { isVisible = true }

    var nombre by remember { mutableStateOf("") }
    var categoria by remember { mutableStateOf("Verduras y Hortalizas") }
    var unidad by remember { mutableStateOf("Kg") }

    var imagenUrl by remember { mutableStateOf("") }
    var imagenUrl2 by remember { mutableStateOf("") }
    var imagenUrl3 by remember { mutableStateOf("") }
    var imagenUrl4 by remember { mutableStateOf("") }
    var showUrlSection by remember { mutableStateOf(false) }

    var precio by remember { mutableStateOf("") }
    var stock by remember { mutableStateOf("") }
    var descripcion by remember { mutableStateOf("") }
    var municipio by remember { mutableStateOf("") }

    var isEditing by remember { mutableStateOf(false) }
    var guardando by remember { mutableStateOf(false) }
    var showSipsa by remember { mutableStateOf(false) }
    var showMapPicker by remember { mutableStateOf(false) }

    LaunchedEffect(tiendaVM.sipsaProductoSeleccionado) {
        tiendaVM.sipsaProductoSeleccionado?.let { prod ->
            nombre = prod
            categoria = when {
                prod.contains("papa", ignoreCase = true) || prod.contains("yuca", ignoreCase = true) || prod.contains("ñame", ignoreCase = true) -> "Tuberculos y Raices"
                prod.contains("tomate", ignoreCase = true) || prod.contains("cebolla", ignoreCase = true) || prod.contains("lechuga", ignoreCase = true) || prod.contains("zanahoria", ignoreCase = true) || prod.contains("pimenton", ignoreCase = true) -> "Verduras y Hortalizas"
                prod.contains("manzana", ignoreCase = true) || prod.contains("naranja", ignoreCase = true) || prod.contains("mango", ignoreCase = true) || prod.contains("fresa", ignoreCase = true) || prod.contains("limon", ignoreCase = true) || prod.contains("banano", ignoreCase = true) || prod.contains("aguacate", ignoreCase = true) -> "Frutas"
                prod.contains("leche", ignoreCase = true) || prod.contains("queso", ignoreCase = true) || prod.contains("yogur", ignoreCase = true) -> "Lacteos"
                prod.contains("huevo", ignoreCase = true) -> "Huevos"
                prod.contains("frijol", ignoreCase = true) || prod.contains("lenteja", ignoreCase = true) || prod.contains("arveja", ignoreCase = true) || prod.contains("maiz", ignoreCase = true) -> "Granos y Cereales"
                prod.contains("cafe", ignoreCase = true) -> "Cafe y Cacao"
                else -> categoria
            }
            val sipsaPrice = extractSipsaPrice(tiendaVM.sipsaPrecio)
            if (sipsaPrice != null && precio.isBlank()) {
                precio = sipsaPrice.toInt().toString()
            }
            tiendaVM.sipsaProductoSeleccionado = null
        }
    }

    LaunchedEffect(productoId) {
        productoId?.let { tiendaVM.cargarProductoParaEditar(it) }
    }
    LaunchedEffect(tiendaVM.productoEditando) {
        tiendaVM.productoEditando?.let { p ->
            nombre = p.nombre; categoria = p.categoria ?: "Verduras y Hortalizas"; unidad = p.unidad ?: "Kg"
            precio = p.precio.toInt().toString(); stock = p.stock.toString()
            descripcion = p.descripcion ?: ""; imagenUrl = p.imagenUrl ?: ""
            imagenUrl2 = p.imagenUrl2 ?: ""; imagenUrl3 = p.imagenUrl3 ?: ""; imagenUrl4 = p.imagenUrl4 ?: ""
            municipio = p.municipioOrigen ?: ""
            isEditing = true
        }
    }

    val categorias = listOf("Verduras y Hortalizas", "Frutas", "Tuberculos y Raices", "Lacteos", "Huevos", "Granos y Cereales", "Cafe y Cacao")
    val unidades = listOf("Kg", "Libra", "Bulto", "Unidad", "Litro", "Docena")

    if (showSipsa) {
        SipsaExplorerScreen(
            tiendaVM = tiendaVM,
            productoInicial = nombre,
            onNavigateBack = { showSipsa = false },
            onProductoSeleccionado = { prod ->
                tiendaVM.sipsaProductoSeleccionado = prod
                showSipsa = false
            }
        )
        return
    }

    if (showMapPicker) {
        MapPickerScreen(
            latitudInicial = 4.5709, longitudInicial = -74.2973,
            onLocationSelected = { picked ->
                municipio = picked.ciudad.ifBlank { picked.direccion.take(50) }
                showMapPicker = false
            },
            onNavigateBack = { showMapPicker = false }
        )
        return
    }

    Scaffold(
        containerColor = AppBackground,
        topBar = {
            AnimatedVisibility(
                visible = isVisible,
                enter = fadeIn(tween(400)) + slideInVertically(tween(400, easing = EaseOutCubic), initialOffsetY = { -it / 4 })
            ) {
                Surface(
                    color = Color.White,
                    shadowElevation = 0.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .statusBarsPadding()
                            .padding(horizontal = 20.dp, vertical = 16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(
                                onClick = onNavigateBack,
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(Slate100)
                            ) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Carbon, modifier = Modifier.size(22.dp))
                            }
                            Spacer(Modifier.width(16.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    if (isEditing) "Editar Producto" else "Nuevo Producto",
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Carbon,
                                    letterSpacing = (-0.5).sp
                                )
                                Text(
                                    "Paso $paso de $totalPasos",
                                    fontSize = 14.sp,
                                    color = Slate500,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        Spacer(Modifier.height(20.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally)
                        ) {
                            repeat(totalPasos) { i ->
                                val isActive = i + 1 == paso
                                val isCompleted = i + 1 < paso

                                AnimatedVisibility(
                                    visible = true,
                                    enter = fadeIn(tween(300, delayMillis = i * 100)) + scaleIn(tween(300, delayMillis = i * 100))
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(CircleShape)
                                            .then(
                                                if (isActive) Modifier.background(Brush.linearGradient(colors = listOf(Emerald, EmeraldDark)))
                                                else if (isCompleted) Modifier.background(EmeraldLight)
                                                else Modifier.background(Slate100)
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (isCompleted) {
                                            Icon(
                                                Icons.Filled.Check,
                                                null,
                                                tint = Emerald,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        } else {
                                            Text(
                                                "${i + 1}",
                                                fontSize = 15.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isActive) Color.White else Slate500
                                            )
                                        }
                                    }
                                }

                                if (i < totalPasos - 1) {
                                    Box(
                                        modifier = Modifier
                                            .width(40.dp)
                                            .height(3.dp)
                                            .clip(RoundedCornerShape(2.dp))
                                            .background(
                                                if (i + 1 < paso) Emerald else Slate200
                                            )
                                            .align(Alignment.CenterVertically)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        bottomBar = {
            AnimatedVisibility(
                visible = isVisible,
                enter = fadeIn(tween(400, delayMillis = 200)) + slideInVertically(tween(400, delayMillis = 200, easing = EaseOutCubic), initialOffsetY = { it / 4 })
            ) {
                Surface(
                    shadowElevation = 20.dp,
                    color = Color.White,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .navigationBarsPadding()
                            .padding(horizontal = 20.dp, vertical = 16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            if (paso > 1) {
                                Surface(
                                    color = Slate100,
                                    shape = RoundedCornerShape(18.dp),
                                    modifier = Modifier
                                        .height(60.dp)
                                        .weight(1f)
                                        .clickable { paso-- }
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxSize(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Slate500, modifier = Modifier.size(20.dp))
                                        Spacer(Modifier.width(8.dp))
                                        Text("Atras", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = Slate500)
                                    }
                                }
                            }

                            Surface(
                                color = if (when (paso) {
                                    1 -> nombre.isNotBlank()
                                    2 -> imagenUrl.isNotBlank() && imagenUrl != "default.png"
                                    3 -> precio.isNotBlank() && stock.isNotBlank()
                                    else -> false
                                } && !guardando) Emerald else Slate200,
                                shape = RoundedCornerShape(18.dp),
                                modifier = Modifier
                                    .height(60.dp)
                                    .weight(if (paso > 1) 1.5f else 1f)
                                    .clickable(
                                        enabled = when (paso) {
                                            1 -> nombre.isNotBlank()
                                            2 -> imagenUrl.isNotBlank() && imagenUrl != "default.png"
                                            3 -> precio.isNotBlank() && stock.isNotBlank()
                                            else -> false
                                        } && !guardando
                                    ) {
                                        if (paso < totalPasos) paso++
                                        else {
                                            guardando = true
                                            val p = Producto(id = productoId, nombre = nombre, descripcion = descripcion,
                                                precio = precio.toDoubleOrNull() ?: 0.0, stock = stock.toIntOrNull() ?: 0,
                                                categoria = categoria, unidad = unidad,
                                                imagenUrl = imagenUrl.ifBlank { "default.png" },
                                                imagenUrl2 = imagenUrl2.ifBlank { null }, imagenUrl3 = imagenUrl3.ifBlank { null },
                                                imagenUrl4 = imagenUrl4.ifBlank { null }, municipioOrigen = municipio.ifBlank { null })
                                            if (productoId != null) tiendaVM.actualizarProductoCampesino(p) { onNavigateBack() }
                                            else tiendaVM.publicarProductoCampesino(p) { onNavigateBack() }
                                        }
                                    }
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxSize(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    if (guardando && paso == totalPasos) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(22.dp),
                                            strokeWidth = 2.dp,
                                            color = Color.White
                                        )
                                        Spacer(Modifier.width(10.dp))
                                    }
                                    Text(
                                        if (paso < totalPasos) "Siguiente" else if (isEditing) "Guardar Cambios" else "Publicar Producto",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                    if (paso < totalPasos) {
                                        Spacer(Modifier.width(10.dp))
                                        Icon(Icons.AutoMirrored.Filled.ArrowForward, null, tint = Color.White, modifier = Modifier.size(20.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    ) { padding ->
        AnimatedVisibility(
            visible = isVisible,
            enter = fadeIn(tween(500, delayMillis = 300)) + slideInVertically(tween(500, delayMillis = 300, easing = EaseOutCubic), initialOffsetY = { it / 6 })
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 24.dp)
            ) {
                when (paso) {
                    1 -> {
                        AnimatedVisibility(
                            visible = true,
                            enter = fadeIn(tween(400)) + slideInVertically(tween(400, easing = EaseOutCubic), initialOffsetY = { it / 4 })
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text(
                                        "¿Que producto vas a vender hoy?",
                                        fontSize = 26.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Carbon,
                                        letterSpacing = (-0.5).sp
                                    )
                                    Text(
                                        "Elige un nombre claro para que los compradores lo encuentren facilmente",
                                        fontSize = 15.sp,
                                        color = Slate500,
                                        lineHeight = 22.sp
                                    )
                                }

                                Surface(
                                    color = Color.White,
                                    shape = RoundedCornerShape(24.dp),
                                    shadowElevation = 4.dp,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(24.dp)) {
                                        Text("Nombre del producto", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = Carbon)
                                        Spacer(Modifier.height(12.dp))
                                        OutlinedTextField(
                                            value = nombre, onValueChange = { nombre = it },
                                            placeholder = { Text("Ej: Tomate Chonto, Papa Pastusa...", color = Slate400, fontSize = 16.sp) },
                                            modifier = Modifier.fillMaxWidth().height(64.dp),
                                            shape = RoundedCornerShape(18.dp),
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedBorderColor = Emerald,
                                                unfocusedBorderColor = Slate200,
                                                focusedContainerColor = Color.White,
                                                unfocusedContainerColor = Color.White
                                            ),
                                            leadingIcon = {
                                                Icon(Icons.Filled.Label, null, tint = if (nombre.isNotBlank()) Emerald else Slate400, modifier = Modifier.size(22.dp))
                                            },
                                            textStyle = LocalTextStyle.current.copy(fontSize = 16.sp)
                                        )
                                    }
                                }

                                Surface(
                                    color = Color.White,
                                    shape = RoundedCornerShape(24.dp),
                                    shadowElevation = 4.dp,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
                                        Column {
                                            Text("Categoria", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = Carbon)
                                            Spacer(Modifier.height(12.dp))
                                            var catExp by remember { mutableStateOf(false) }
                                            ExposedDropdownMenuBox(expanded = catExp, onExpandedChange = { catExp = it }) {
                                                OutlinedTextField(
                                                    value = categoria, onValueChange = {}, readOnly = true,
                                                    modifier = Modifier.fillMaxWidth().height(64.dp).menuAnchor(),
                                                    shape = RoundedCornerShape(18.dp),
                                                    colors = OutlinedTextFieldDefaults.colors(
                                                        focusedBorderColor = Emerald,
                                                        unfocusedBorderColor = Slate200,
                                                        focusedContainerColor = Color.White,
                                                        unfocusedContainerColor = Color.White
                                                    ),
                                                    leadingIcon = { Icon(Icons.Filled.Category, null, tint = Emerald, modifier = Modifier.size(22.dp)) },
                                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = catExp) },
                                                    textStyle = LocalTextStyle.current.copy(fontSize = 16.sp)
                                                )
                                                ExposedDropdownMenu(expanded = catExp, onDismissRequest = { catExp = false }) {
                                                    categorias.forEach { c -> DropdownMenuItem(text = { Text(c, fontWeight = FontWeight.Medium, fontSize = 15.sp) }, onClick = { categoria = c; catExp = false }) }
                                                }
                                            }
                                        }

                                        Column {
                                            Text("Se vende por", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = Carbon)
                                            Spacer(Modifier.height(12.dp))
                                            var uniExp by remember { mutableStateOf(false) }
                                            ExposedDropdownMenuBox(expanded = uniExp, onExpandedChange = { uniExp = it }) {
                                                OutlinedTextField(
                                                    value = unidad, onValueChange = {}, readOnly = true,
                                                    modifier = Modifier.fillMaxWidth().height(64.dp).menuAnchor(),
                                                    shape = RoundedCornerShape(18.dp),
                                                    colors = OutlinedTextFieldDefaults.colors(
                                                        focusedBorderColor = Emerald,
                                                        unfocusedBorderColor = Slate200,
                                                        focusedContainerColor = Color.White,
                                                        unfocusedContainerColor = Color.White
                                                    ),
                                                    leadingIcon = { Icon(Icons.Filled.Scale, null, tint = Emerald, modifier = Modifier.size(22.dp)) },
                                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = uniExp) },
                                                    textStyle = LocalTextStyle.current.copy(fontSize = 16.sp)
                                                )
                                                ExposedDropdownMenu(expanded = uniExp, onDismissRequest = { uniExp = false }) {
                                                    unidades.forEach { u -> DropdownMenuItem(text = { Text(u, fontWeight = FontWeight.Medium, fontSize = 15.sp) }, onClick = { unidad = u; uniExp = false }) }
                                                }
                                            }
                                        }
                                    }
                                }

                                Surface(
                                    color = Blue50,
                                    shape = RoundedCornerShape(24.dp),
                                    border = BorderStroke(1.5.dp, Blue500.copy(alpha = 0.2f)),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            tiendaVM.consultarSipsa(nombre.ifBlank { "Tomate" })
                                            showSipsa = true
                                        }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(24.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(56.dp)
                                                .clip(CircleShape)
                                                .background(
                                                    Brush.linearGradient(
                                                        colors = listOf(Blue500, Blue600)
                                                    )
                                                ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(Icons.Filled.QueryStats, null, tint = Color.White, modifier = Modifier.size(28.dp))
                                        }
                                        Spacer(Modifier.width(20.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text("Explorar precios SIPSA", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = Blue600)
                                            Text("Auto-llenar nombre, categoria y precio", fontSize = 13.sp, color = Slate500, lineHeight = 18.sp)
                                        }
                                        Icon(Icons.AutoMirrored.Filled.ArrowForward, null, tint = Blue500, modifier = Modifier.size(24.dp))
                                    }
                                }

                                Surface(
                                    color = Slate100,
                                    shape = RoundedCornerShape(24.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(24.dp),
                                        verticalAlignment = Alignment.Top
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(44.dp)
                                                .clip(CircleShape)
                                                .background(
                                                    Brush.linearGradient(
                                                        colors = listOf(Color(0xFFFBBF24), Color(0xFFF59E0B))
                                                    )
                                                ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(Icons.Filled.Lightbulb, null, tint = Color.White, modifier = Modifier.size(22.dp))
                                        }
                                        Spacer(Modifier.width(16.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text("Consejo", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Carbon)
                                            Spacer(Modifier.height(6.dp))
                                            Text(
                                                "Un nombre descriptivo como \"Tomate Chonto organico\" tiene 3x mas visitas que nombres genericos.",
                                                fontSize = 14.sp,
                                                color = Slate500,
                                                lineHeight = 20.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    2 -> {
                        AnimatedVisibility(
                            visible = true,
                            enter = fadeIn(tween(400)) + slideInVertically(tween(400, easing = EaseOutCubic), initialOffsetY = { it / 4 })
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text("Fotos del producto", fontSize = 26.sp, fontWeight = FontWeight.Bold, color = Carbon, letterSpacing = (-0.5).sp)
                                    Text("Los productos con fotos venden hasta 5x mas", fontSize = 15.sp, color = Slate500, lineHeight = 22.sp)
                                }

                                Surface(
                                    color = Color.White,
                                    shape = RoundedCornerShape(24.dp),
                                    shadowElevation = 4.dp,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(24.dp)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Box(
                                                modifier = Modifier
                                                    .size(44.dp)
                                                    .clip(CircleShape)
                                                    .background(
                                                        Brush.linearGradient(
                                                            colors = listOf(Emerald, EmeraldDark)
                                                        )
                                                    ),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(Icons.Filled.CloudUpload, null, tint = Color.White, modifier = Modifier.size(22.dp))
                                            }
                                            Spacer(Modifier.width(14.dp))
                                            Column {
                                                Text("Subir desde el celular", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Carbon)
                                                Text("Toca cada cuadro para elegir una foto", fontSize = 13.sp, color = Slate500)
                                            }
                                        }

                                        Spacer(Modifier.height(20.dp))

                                        val context = LocalContext.current
                                        val scope = rememberCoroutineScope()

                                        val imageSlots = listOf(
                                            "Portada" to { imgUrl: String -> imagenUrl = imgUrl },
                                            "Imagen 2" to { imgUrl: String -> imagenUrl2 = imgUrl },
                                            "Imagen 3" to { imgUrl: String -> imagenUrl3 = imgUrl },
                                            "Imagen 4" to { imgUrl: String -> imagenUrl4 = imgUrl }
                                        )

                                        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                                            imageSlots.chunked(2).forEach { row ->
                                                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                                    row.forEach { (label, setter) ->
                                                        val currentUrl = when (label) {
                                                            "Portada" -> imagenUrl; "Imagen 2" -> imagenUrl2
                                                            "Imagen 3" -> imagenUrl3; else -> imagenUrl4
                                                        }
                                                        val hasImage = currentUrl.isNotBlank() && currentUrl != "default.png"
                                                        val picker = androidx.activity.compose.rememberLauncherForActivityResult(
                                                            androidx.activity.result.contract.ActivityResultContracts.GetContent()
                                                        ) { uri ->
                                                            uri?.let { u ->
                                                                scope.launch {
                                                                    try {
                                                                        val inputStream = context.contentResolver.openInputStream(u)
                                                                        val bytes = inputStream?.readBytes()
                                                                        inputStream?.close()
                                                                        if (bytes != null) {
                                                                            val body = okhttp3.MultipartBody.Part.createFormData(
                                                                                "file", "foto_${System.currentTimeMillis()}.jpg",
                                                                                okhttp3.RequestBody.create("image/*".toMediaType(), bytes)
                                                                            )
                                                                            val resp = tiendaVM.subirImagenProducto(body)
                                                                            val filename = resp["filename"] as? String
                                                                            if (filename != null) setter(filename)
                                                                        }
                                                                    } catch (_: Exception) {}
                                                                }
                                                            }
                                                        }

                                                        Surface(
                                                            color = if (hasImage) EmeraldLight else Slate100,
                                                            shape = RoundedCornerShape(20.dp),
                                                            border = if (hasImage) BorderStroke(2.dp, Emerald.copy(alpha = 0.4f)) else BorderStroke(1.5.dp, Slate200.copy(alpha = 0.5f)),
                                                            modifier = Modifier
                                                                .weight(1f)
                                                                .height(140.dp)
                                                                .clickable { picker.launch("image/*") }
                                                        ) {
                                                            Column(
                                                                modifier = Modifier.fillMaxSize().padding(16.dp),
                                                                horizontalAlignment = Alignment.CenterHorizontally,
                                                                verticalArrangement = Arrangement.Center
                                                            ) {
                                                                if (hasImage) {
                                                                    Box(
                                                                        modifier = Modifier
                                                                            .size(36.dp)
                                                                            .clip(CircleShape)
                                                                            .background(
                                                                                Brush.linearGradient(
                                                                                    colors = listOf(Emerald, EmeraldDark)
                                                                                )
                                                                            ),
                                                                        contentAlignment = Alignment.Center
                                                                    ) {
                                                                        Icon(Icons.Filled.Check, null, tint = Color.White, modifier = Modifier.size(20.dp))
                                                                    }
                                                                    Spacer(Modifier.height(8.dp))
                                                                    Text(label, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Emerald)
                                                                    Text(currentUrl.take(15), fontSize = 11.sp, color = Slate500, maxLines = 1)
                                                                } else {
                                                                    val isPortada = label == "Portada"
                                                                    Box(
                                                                        modifier = Modifier
                                                                            .size(40.dp)
                                                                            .clip(CircleShape)
                                                                            .background(
                                                                                if (isPortada) Brush.linearGradient(colors = listOf(Emerald, EmeraldDark))
                                                                                else Brush.linearGradient(colors = listOf(Slate400, Slate500))
                                                                            ),
                                                                        contentAlignment = Alignment.Center
                                                                    ) {
                                                                        Icon(
                                                                            if (isPortada) Icons.Filled.CropOriginal else Icons.Filled.AddAPhoto,
                                                                            null,
                                                                            tint = Color.White,
                                                                            modifier = Modifier.size(20.dp)
                                                                        )
                                                                    }
                                                                    Spacer(Modifier.height(8.dp))
                                                                    Text(label, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = if (isPortada) Emerald else Slate500)
                                                                    Text(
                                                                        if (isPortada) "Foto principal" else "Toca para elegir",
                                                                        fontSize = 12.sp, color = Slate500
                                                                    )
                                                                }
                                                            }
                                                        }
                                                    }
                                                    if (row.size == 1) Spacer(Modifier.weight(1f))
                                                }
                                            }
                                        }
                                    }
                                }

                                Surface(
                                    color = Color.White,
                                    shape = RoundedCornerShape(24.dp),
                                    shadowElevation = 4.dp,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    var showUrlSection by remember { mutableStateOf(false) }
                                    Column(modifier = Modifier.padding(24.dp)) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable { showUrlSection = !showUrlSection }
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(44.dp)
                                                    .clip(CircleShape)
                                                    .background(
                                                        Brush.linearGradient(
                                                            colors = listOf(Blue500, Blue600)
                                                        )
                                                    ),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(Icons.Filled.Link, null, tint = Color.White, modifier = Modifier.size(22.dp))
                                            }
                                            Spacer(Modifier.width(14.dp))
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text("O usar enlaces URL", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Carbon)
                                                Text("Pega enlaces de internet", fontSize = 13.sp, color = Slate500)
                                            }
                                            Icon(
                                                if (showUrlSection) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                                                null,
                                                tint = Slate500,
                                                modifier = Modifier.size(28.dp)
                                            )
                                        }

                                        AnimatedVisibility(visible = showUrlSection) {
                                            Column(
                                                modifier = Modifier.padding(top = 20.dp),
                                                verticalArrangement = Arrangement.spacedBy(16.dp)
                                            ) {
                                                listOf(
                                                    "Imagen principal" to { v: String -> imagenUrl = v },
                                                    "Imagen 2" to { v: String -> imagenUrl2 = v },
                                                    "Imagen 3" to { v: String -> imagenUrl3 = v },
                                                    "Imagen 4" to { v: String -> imagenUrl4 = v }
                                                ).forEach { (label, setter) ->
                                                    val value = when (label) {
                                                        "Imagen principal" -> imagenUrl; "Imagen 2" -> imagenUrl2
                                                        "Imagen 3" -> imagenUrl3; else -> imagenUrl4
                                                    }
                                                    OutlinedTextField(
                                                        value = value, onValueChange = { setter(it) },
                                                        placeholder = { Text(label, color = Slate400, fontSize = 15.sp) },
                                                        modifier = Modifier.fillMaxWidth().height(64.dp),
                                                        shape = RoundedCornerShape(18.dp),
                                                        colors = OutlinedTextFieldDefaults.colors(
                                                            focusedBorderColor = Emerald,
                                                            unfocusedBorderColor = Slate200,
                                                            focusedContainerColor = Color.White,
                                                            unfocusedContainerColor = Color.White
                                                        ),
                                                        textStyle = LocalTextStyle.current.copy(fontSize = 15.sp)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    3 -> {
                        AnimatedVisibility(
                            visible = true,
                            enter = fadeIn(tween(400)) + slideInVertically(tween(400, easing = EaseOutCubic), initialOffsetY = { it / 4 })
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text("Precio y ubicacion", fontSize = 26.sp, fontWeight = FontWeight.Bold, color = Carbon, letterSpacing = (-0.5).sp)
                                    Text("Define cuanto cuesta y donde se cultiva", fontSize = 15.sp, color = Slate500, lineHeight = 22.sp)
                                }

                                Surface(
                                    color = Color.White,
                                    shape = RoundedCornerShape(24.dp),
                                    shadowElevation = 4.dp,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(24.dp)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Box(
                                                modifier = Modifier
                                                    .size(44.dp)
                                                    .clip(CircleShape)
                                                    .background(
                                                        Brush.linearGradient(
                                                            colors = listOf(Emerald, EmeraldDark)
                                                        )
                                                    ),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(Icons.Filled.AttachMoney, null, tint = Color.White, modifier = Modifier.size(22.dp))
                                            }
                                            Spacer(Modifier.width(14.dp))
                                            Column {
                                                Text("Precio por $unidad", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Carbon)
                                                Text("Ajusta el precio de tu producto", fontSize = 13.sp, color = Slate500)
                                            }
                                        }

                                        Spacer(Modifier.height(20.dp))

                                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(0.dp)) {
                                            Surface(
                                                color = Slate100,
                                                shape = RoundedCornerShape(topStart = 18.dp, bottomStart = 18.dp),
                                                modifier = Modifier.height(64.dp).clickable {
                                                    val p = (precio.toIntOrNull() ?: 0) - 500
                                                    if (p >= 0) precio = p.toString()
                                                }
                                            ) {
                                                Box(contentAlignment = Alignment.Center, modifier = Modifier.width(64.dp).fillMaxHeight()) {
                                                    Icon(Icons.Filled.Remove, null, tint = Carbon, modifier = Modifier.size(28.dp))
                                                }
                                            }
                                            OutlinedTextField(
                                                value = precio, onValueChange = { p -> precio = p.filter { c -> c.isDigit() } },
                                                placeholder = { Text("0", color = Slate400, textAlign = TextAlign.Center) },
                                                modifier = Modifier.weight(1f).height(64.dp),
                                                shape = RoundedCornerShape(0.dp),
                                                textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Center, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Carbon),
                                                colors = OutlinedTextFieldDefaults.colors(
                                                    focusedBorderColor = Emerald,
                                                    unfocusedBorderColor = Slate200,
                                                    focusedContainerColor = Color.White,
                                                    unfocusedContainerColor = Color.White
                                                ),
                                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                                            )
                                            Surface(
                                                color = Emerald,
                                                shape = RoundedCornerShape(topEnd = 18.dp, bottomEnd = 18.dp),
                                                modifier = Modifier.height(64.dp).clickable {
                                                    val p = (precio.toIntOrNull() ?: 0) + 500
                                                    precio = p.toString()
                                                }
                                            ) {
                                                Box(contentAlignment = Alignment.Center, modifier = Modifier.width(64.dp).fillMaxHeight()) {
                                                    Icon(Icons.Filled.Add, null, tint = Color.White, modifier = Modifier.size(28.dp))
                                                }
                                            }
                                            Spacer(Modifier.width(12.dp))
                                            Text("/ $unidad", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = Carbon)
                                        }

                                        if (!tiendaVM.sipsaPrecio.isNullOrBlank() && precio.isNotBlank()) {
                                            val myPrice = precio.toDoubleOrNull()
                                            val sipsaPrice = extractSipsaPrice(tiendaVM.sipsaPrecio)
                                            if (myPrice != null && sipsaPrice != null) {
                                                val ratio = myPrice / sipsaPrice
                                                val (label, color) = when {
                                                    ratio < 0.8 -> "Muy bajo vs SIPSA" to Red500
                                                    ratio > 1.3 -> "Por encima del promedio" to Color(0xFFF59E0B)
                                                    else -> "Precio competitivo" to Green500
                                                }
                                                Spacer(Modifier.height(16.dp))
                                                Surface(color = color.copy(alpha = 0.1f), shape = RoundedCornerShape(16.dp)) {
                                                    Row(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                                                        Icon(Icons.Filled.QueryStats, null, tint = color, modifier = Modifier.size(20.dp))
                                                        Spacer(Modifier.width(10.dp))
                                                        Text("$label - Promedio SIPSA: $$sipsaPrice/$unidad", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = color)
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }

                                Surface(
                                    color = Color.White,
                                    shape = RoundedCornerShape(24.dp),
                                    shadowElevation = 4.dp,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(24.dp)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Box(
                                                modifier = Modifier
                                                    .size(44.dp)
                                                    .clip(CircleShape)
                                                    .background(
                                                        Brush.linearGradient(
                                                            colors = listOf(Emerald, EmeraldDark)
                                                        )
                                                    ),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(Icons.Filled.Inventory2, null, tint = Color.White, modifier = Modifier.size(22.dp))
                                            }
                                            Spacer(Modifier.width(14.dp))
                                            Column {
                                                Text("Cantidad disponible", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Carbon)
                                                Text("¿Cuanto tienes para vender?", fontSize = 13.sp, color = Slate500)
                                            }
                                        }

                                        Spacer(Modifier.height(20.dp))

                                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(0.dp)) {
                                            Surface(
                                                color = Slate100,
                                                shape = RoundedCornerShape(topStart = 18.dp, bottomStart = 18.dp),
                                                modifier = Modifier.height(64.dp).clickable {
                                                    val s = (stock.toIntOrNull() ?: 0) - 5
                                                    if (s >= 0) stock = s.toString()
                                                }
                                            ) {
                                                Box(contentAlignment = Alignment.Center, modifier = Modifier.width(64.dp).fillMaxHeight()) {
                                                    Icon(Icons.Filled.Remove, null, tint = Carbon, modifier = Modifier.size(28.dp))
                                                }
                                            }
                                            OutlinedTextField(
                                                value = stock, onValueChange = { s -> stock = s.filter { c -> c.isDigit() } },
                                                placeholder = { Text("0", color = Slate400, textAlign = TextAlign.Center) },
                                                modifier = Modifier.weight(1f).height(64.dp),
                                                shape = RoundedCornerShape(0.dp),
                                                textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Center, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Carbon),
                                                colors = OutlinedTextFieldDefaults.colors(
                                                    focusedBorderColor = Emerald,
                                                    unfocusedBorderColor = Slate200,
                                                    focusedContainerColor = Color.White,
                                                    unfocusedContainerColor = Color.White
                                                ),
                                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                                            )
                                            Surface(
                                                color = Emerald,
                                                shape = RoundedCornerShape(topEnd = 18.dp, bottomEnd = 18.dp),
                                                modifier = Modifier.height(64.dp).clickable {
                                                    val s = (stock.toIntOrNull() ?: 0) + 5
                                                    stock = s.toString()
                                                }
                                            ) {
                                                Box(contentAlignment = Alignment.Center, modifier = Modifier.width(64.dp).fillMaxHeight()) {
                                                    Icon(Icons.Filled.Add, null, tint = Color.White, modifier = Modifier.size(28.dp))
                                                }
                                            }
                                            Spacer(Modifier.width(12.dp))
                                            Text(unidad, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = Carbon)
                                        }
                                    }
                                }

                                Surface(
                                    color = Color.White,
                                    shape = RoundedCornerShape(24.dp),
                                    shadowElevation = 4.dp,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(24.dp)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Box(
                                                modifier = Modifier
                                                    .size(44.dp)
                                                    .clip(CircleShape)
                                                    .background(
                                                        Brush.linearGradient(
                                                            colors = listOf(Color(0xFF8B5CF6), Color(0xFF7C3AED))
                                                        )
                                                    ),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(Icons.Filled.Description, null, tint = Color.White, modifier = Modifier.size(22.dp))
                                            }
                                            Spacer(Modifier.width(14.dp))
                                            Column {
                                                Text("Descripcion", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Carbon)
                                                Text("Cuentanos sobre tu producto (opcional)", fontSize = 13.sp, color = Slate500)
                                            }
                                        }

                                        Spacer(Modifier.height(20.dp))

                                        OutlinedTextField(
                                            value = descripcion, onValueChange = { descripcion = it },
                                            placeholder = { Text("Describe tu producto...", color = Slate400, fontSize = 15.sp) },
                                            minLines = 5,
                                            modifier = Modifier.fillMaxWidth(),
                                            shape = RoundedCornerShape(18.dp),
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedBorderColor = Emerald,
                                                unfocusedBorderColor = Slate200,
                                                focusedContainerColor = Color.White,
                                                unfocusedContainerColor = Color.White
                                            ),
                                            textStyle = LocalTextStyle.current.copy(fontSize = 15.sp, lineHeight = 22.sp)
                                        )
                                    }
                                }

                                Surface(
                                    color = Color.White,
                                    shape = RoundedCornerShape(24.dp),
                                    shadowElevation = 4.dp,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(24.dp)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Box(
                                                modifier = Modifier
                                                    .size(44.dp)
                                                    .clip(CircleShape)
                                                    .background(
                                                        Brush.linearGradient(
                                                            colors = listOf(Blue500, Blue600)
                                                        )
                                                    ),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(Icons.Filled.LocationOn, null, tint = Color.White, modifier = Modifier.size(22.dp))
                                            }
                                            Spacer(Modifier.width(14.dp))
                                            Column {
                                                Text("Ubicacion del cultivo", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Carbon)
                                                Text("¿Donde cultivas tu producto?", fontSize = 13.sp, color = Slate500)
                                            }
                                        }

                                        Spacer(Modifier.height(20.dp))

                                        OutlinedTextField(
                                            value = municipio, onValueChange = { municipio = it },
                                            placeholder = { Text("Ej: Barbosa, Santander", color = Slate400, fontSize = 15.sp) },
                                            singleLine = true,
                                            modifier = Modifier.fillMaxWidth().height(64.dp),
                                            shape = RoundedCornerShape(18.dp),
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedBorderColor = Emerald,
                                                unfocusedBorderColor = Slate200,
                                                focusedContainerColor = Color.White,
                                                unfocusedContainerColor = Color.White
                                            ),
                                            textStyle = LocalTextStyle.current.copy(fontSize = 15.sp)
                                        )

                                        Spacer(Modifier.height(16.dp))

                                        Surface(
                                            color = Blue50,
                                            shape = RoundedCornerShape(18.dp),
                                            border = BorderStroke(1.5.dp, Blue500.copy(alpha = 0.2f)),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(64.dp)
                                                .clickable { showMapPicker = true }
                                        ) {
                                            Row(
                                                modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(Icons.Filled.Map, null, tint = Blue500, modifier = Modifier.size(22.dp))
                                                Spacer(Modifier.width(12.dp))
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text("Seleccionar en el mapa", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Carbon)
                                                    Text("Marca la zona aproximada", fontSize = 12.sp, color = Slate500)
                                                }
                                            }
                                        }

                                        if (municipio.isNotBlank()) {
                                            Spacer(Modifier.height(16.dp))
                                            Surface(
                                                color = EmeraldLight,
                                                shape = RoundedCornerShape(18.dp),
                                                border = BorderStroke(1.5.dp, Emerald.copy(alpha = 0.3f)),
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(64.dp)
                                            ) {
                                                Row(
                                                    modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Box(
                                                        modifier = Modifier
                                                            .size(32.dp)
                                                            .clip(CircleShape)
                                                            .background(
                                                                Brush.linearGradient(
                                                                    colors = listOf(Emerald, EmeraldDark)
                                                                )
                                                            ),
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        Icon(Icons.Filled.CheckCircle, null, tint = Color.White, modifier = Modifier.size(18.dp))
                                                    }
                                                    Spacer(Modifier.width(12.dp))
                                                    Column(modifier = Modifier.weight(1f)) {
                                                        Text("Ubicacion seleccionada", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Emerald)
                                                        Text(municipio, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Carbon)
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun extractSipsaPrice(info: String?): Double? {
    if (info == null) return null
    val regex = Regex("\\$([\\d,.]+)").find(info) ?: return null
    return regex.groupValues[1].replace(",", "").toDoubleOrNull()
}
