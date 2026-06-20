package com.agroconecta.app.ui.theme

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.agroconecta.app.data.model.Producto
import com.agroconecta.app.viewmodel.TiendaViewModel

private val DarkSlate = Color(0xFF0F172A)
private val Slate500 = Color(0xFF64748B)
private val Slate400 = Color(0xFF94A3B8)
private val Slate200 = Color(0xFFE2E8F0)
private val Slate100 = Color(0xFFF1F5F9)
private val Brand600 = Color(0xFF16A34A)
private val SurfacePure = Color(0xFFFDFDFC)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchOverlay(
    tiendaVM: TiendaViewModel,
    onDismiss: () -> Unit,
    onProductoClick: (Producto) -> Unit,
    onVerTodos: (String) -> Unit,
    onCategoryClick: (String) -> Unit
) {
    var query by remember { mutableStateOf("") }
    var recentSearches by remember { mutableStateOf(listOf<String>()) }
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current

    // Búsqueda mejorada: nombre + categoría + campesino
    val resultados = if (query.isNotBlank()) {
        tiendaVM.todosLosProductos.filter { p ->
            p.nombre.contains(query, ignoreCase = true) ||
            p.categoria?.contains(query, ignoreCase = true) == true ||
            p.nombreCampesino?.contains(query, ignoreCase = true) == true
        }
    } else {
        emptyList()
    }

    val categoriasPopulares = tiendaVM.obtenerCategorias().take(6)

    LaunchedEffect(Unit) { focusRequester.requestFocus() }

    Box(modifier = Modifier.fillMaxSize().background(SurfacePure)) {
        Column(modifier = Modifier.fillMaxSize()) {
            // ===== SEARCH BAR =====
            Surface(shadowElevation = 4.dp, color = Color.White) {
                Row(
                    modifier = Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onDismiss, modifier = Modifier.size(40.dp)) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = DarkSlate, modifier = Modifier.size(22.dp))
                    }
                    OutlinedTextField(
                        value = query,
                        onValueChange = { query = it },
                        placeholder = { Text("Buscar frutas, verduras, lacteos...", fontSize = 14.sp, color = Slate400) },
                        leadingIcon = { Icon(Icons.Filled.Search, null, tint = Slate400, modifier = Modifier.size(20.dp)) },
                        trailingIcon = {
                            if (query.isNotEmpty()) {
                                IconButton(onClick = { query = "" }, modifier = Modifier.size(28.dp)) {
                                    Icon(Icons.Filled.Close, null, tint = Slate400, modifier = Modifier.size(16.dp))
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Slate100, unfocusedContainerColor = Slate100,
                            focusedBorderColor = Brand600, unfocusedBorderColor = Color.Transparent,
                            cursorColor = Brand600, focusedTextColor = DarkSlate, unfocusedTextColor = DarkSlate
                        ),
                        modifier = Modifier.weight(1f).height(52.dp).focusRequester(focusRequester),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(onSearch = {
                            if (query.isNotBlank()) {
                                recentSearches = (listOf(query) + recentSearches).distinct().take(10)
                                tiendaVM.buscarProductos(query)
                                focusManager.clearFocus()
                            }
                        })
                    )
                }
            }

            if (query.isNotEmpty()) {
                // ===== SEARCH RESULTS =====
                if (resultados.isEmpty()) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(32.dp)) {
                            Icon(Icons.Filled.SearchOff, null, tint = Slate400, modifier = Modifier.size(56.dp))
                            Spacer(Modifier.height(16.dp))
                            Text("No encontramos \"$query\"", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = DarkSlate)
                            Spacer(Modifier.height(4.dp))
                            Text("Intenta con otros terminos", fontSize = 13.sp, color = Slate400)
                        }
                    }
                } else {
                    Row(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("${resultados.size} resultado${if (resultados.size != 1) "s" else ""}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Slate500)
                        TextButton(onClick = {
                            recentSearches = (listOf(query) + recentSearches).distinct().take(10)
                            tiendaVM.buscarProductos(query)
                            onVerTodos(query)
                        }) {
                            Text("Ver todos", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Brand600)
                            Icon(Icons.AutoMirrored.Filled.ArrowForward, null, tint = Brand600, modifier = Modifier.size(16.dp))
                        }
                    }
                    LazyColumn(contentPadding = PaddingValues(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(resultados.size) { index ->
                            val producto = resultados[index]
                            Surface(
                                color = Color.White, shape = RoundedCornerShape(16.dp),
                                shadowElevation = 2.dp,
                                modifier = Modifier.fillMaxWidth().clickable {
                                    recentSearches = (listOf(query) + recentSearches).distinct().take(10)
                                    onDismiss()
                                    onProductoClick(producto)
                                }
                            ) {
                                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                    // Imagen del producto
                                    AsyncImage(
                                        model = ImageRequest.Builder(LocalContext.current)
                                            .data(imageUrl(producto))
                                            .crossfade(true)
                                            .build(),
                                        contentDescription = producto.nombre,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier
                                            .size(56.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                    )
                                    Spacer(Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(producto.nombre, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = DarkSlate, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                        Spacer(Modifier.height(2.dp))
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            if (!producto.categoria.isNullOrBlank()) {
                                                Text(producto.categoria, fontSize = 11.sp, color = Brand600, fontWeight = FontWeight.Medium)
                                                Spacer(Modifier.width(6.dp))
                                            }
                                            Text("$${producto.precio.toInt()} / ${producto.unidad ?: "Kg"}", fontSize = 12.sp, color = Slate500)
                                        }
                                    }
                                    Icon(Icons.AutoMirrored.Filled.ArrowForward, null, tint = Slate400, modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                    }
                }
            } else {
                // ===== SUGGESTIONS (empty query) =====
                Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)) {
                    // Recent searches
                    if (recentSearches.isNotEmpty()) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Busquedas recientes", fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = DarkSlate)
                            TextButton(onClick = { recentSearches = emptyList() }) {
                                Text("Limpiar", fontSize = 11.sp, color = Slate400)
                            }
                        }
                        Spacer(Modifier.height(8.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            recentSearches.take(4).forEach { term ->
                                Surface(
                                    color = Slate100, shape = RoundedCornerShape(20.dp),
                                    modifier = Modifier.clickable { query = term }
                                ) {
                                    Row(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Filled.History, null, tint = Slate400, modifier = Modifier.size(14.dp))
                                        Spacer(Modifier.width(4.dp))
                                        Text(term, fontSize = 12.sp, color = Slate500)
                                    }
                                }
                            }
                        }
                        Spacer(Modifier.height(24.dp))
                    }

                    // Categorias
                    Text("Categorias", fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = DarkSlate)
                    Spacer(Modifier.height(10.dp))
                    val catRows = categoriasPopulares.chunked(2)
                    catRows.forEach { row ->
                        Row(modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            row.forEach { cat ->
                                val (icon, color) = getCategoryIconAndColor(cat)
                                Surface(
                                    color = Slate100, shape = RoundedCornerShape(16.dp),
                                    modifier = Modifier.weight(1f).height(80.dp).clickable {
                                        onCategoryClick(cat)
                                    }
                                ) {
                                    Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                                        Icon(icon, null, tint = color, modifier = Modifier.size(28.dp))
                                        Spacer(Modifier.height(4.dp))
                                        Text(cat, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = DarkSlate)
                                    }
                                }
                            }
                            if (row.size == 1) Spacer(Modifier.weight(1f))
                        }
                    }

                    Spacer(Modifier.height(24.dp))

                    // Populares
                    Text("Lo mas buscado", fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = DarkSlate)
                    Spacer(Modifier.height(8.dp))
                    val topProductos = tiendaVM.getDestacados().take(6)
                    topProductos.forEach { p ->
                        Surface(
                            color = Color.White, shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, Slate200),
                            modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp).clickable { onProductoClick(p); onDismiss() }
                        ) {
                            Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.AutoMirrored.Filled.TrendingUp, null, tint = Brand600, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(10.dp))
                                Text(p.nombre, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = DarkSlate, modifier = Modifier.weight(1f))
                                Text("$${p.precio.toInt()}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Brand600)
                            }
                        }
                    }
                }
            }
        }
    }
}
