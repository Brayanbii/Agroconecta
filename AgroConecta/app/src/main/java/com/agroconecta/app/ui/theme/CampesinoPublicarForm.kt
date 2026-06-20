package com.agroconecta.app.ui.theme

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.agroconecta.app.viewmodel.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CampesinoPublicarForm(viewModel: AuthViewModel) {
    var nombreProducto by remember { mutableStateOf("") }
    var precioProducto by remember { mutableStateOf("") }
    var cantidadInventario by remember { mutableStateOf("") }
    var categoriaSeleccionada by remember { mutableStateOf("Verduras") }
    var isVisible by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        isVisible = true
    }

    val categoriasDisponibles = listOf(
        "Verduras" to Icons.Filled.Eco,
        "Frutas" to Icons.Filled.LocalDining,
        "Tubérculos" to Icons.Filled.Agriculture,
        "Lácteos" to Icons.Filled.WaterDrop,
        "Granos" to Icons.Filled.Grass,
        "Café" to Icons.Filled.Coffee,
        "Huevos" to Icons.Filled.Egg
    )

    val gradientGreen = Brush.linearGradient(
        colors = listOf(Color(0xFF16A34A), Color(0xFF22C55E))
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 16.dp)
    ) {
        // Header con animación
        AnimatedVisibility(
            visible = isVisible,
            enter = fadeIn(tween(600)) + slideInVertically(tween(600, easing = EaseOutCubic), initialOffsetY = { -it / 3 })
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(gradientGreen)
                        .shadow(12.dp, CircleShape, spotColor = Color(0xFF16A34A).copy(alpha = 0.3f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Filled.AddShoppingCart,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(40.dp)
                    )
                }
                
                Spacer(Modifier.height(20.dp))
                
                Text(
                    text = "Publicar Producto",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF111827),
                    letterSpacing = (-0.5).sp,
                    textAlign = TextAlign.Center
                )
                
                Spacer(Modifier.height(8.dp))
                
                Text(
                    text = "Completa la información de tu producto",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF64748B),
                    textAlign = TextAlign.Center
                )
            }
        }

        Spacer(Modifier.height(32.dp))

        // Formulario en card premium
        AnimatedVisibility(
            visible = isVisible,
            enter = fadeIn(tween(600, delayMillis = 200)) + slideInVertically(tween(600, delayMillis = 200, easing = EaseOutCubic), initialOffsetY = { it / 4 })
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    // Nombre del producto
                    Text(
                        text = "Nombre del producto",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF334155),
                        letterSpacing = 0.3.sp
                    )
                    Spacer(Modifier.height(8.dp))
                    
                    OutlinedTextField(
                        value = nombreProducto,
                        onValueChange = { nombreProducto = it },
                        placeholder = { 
                            Text(
                                "Ej: Papa Criolla",
                                fontSize = 14.sp,
                                color = Color(0xFF94A3B8)
                            ) 
                        },
                        leadingIcon = {
                            Icon(
                                Icons.Filled.Label,
                                contentDescription = null,
                                tint = Color(0xFF16A34A),
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(14.dp),
                        enabled = !viewModel.estaCargando,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF16A34A),
                            unfocusedBorderColor = Color(0xFFE2E8F0),
                            focusedContainerColor = Color(0xFFF8FAFC),
                            unfocusedContainerColor = Color(0xFFF8FAFC),
                            cursorColor = Color(0xFF16A34A)
                        ),
                        singleLine = true
                    )

                    Spacer(Modifier.height(20.dp))

                    // Precio
                    Text(
                        text = "Precio por unidad",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF334155),
                        letterSpacing = 0.3.sp
                    )
                    Spacer(Modifier.height(8.dp))
                    
                    OutlinedTextField(
                        value = precioProducto,
                        onValueChange = { precioProducto = it },
                        placeholder = { 
                            Text(
                                "Ej: 3500",
                                fontSize = 14.sp,
                                color = Color(0xFF94A3B8)
                            ) 
                        },
                        leadingIcon = {
                            Icon(
                                Icons.Filled.AttachMoney,
                                contentDescription = null,
                                tint = Color(0xFF16A34A),
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(14.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        enabled = !viewModel.estaCargando,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF16A34A),
                            unfocusedBorderColor = Color(0xFFE2E8F0),
                            focusedContainerColor = Color(0xFFF8FAFC),
                            unfocusedContainerColor = Color(0xFFF8FAFC),
                            cursorColor = Color(0xFF16A34A)
                        ),
                        singleLine = true
                    )

                    Spacer(Modifier.height(20.dp))

                    // Cantidad
                    Text(
                        text = "Cantidad disponible",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF334155),
                        letterSpacing = 0.3.sp
                    )
                    Spacer(Modifier.height(8.dp))
                    
                    OutlinedTextField(
                        value = cantidadInventario,
                        onValueChange = { cantidadInventario = it },
                        placeholder = { 
                            Text(
                                "Ej: 50",
                                fontSize = 14.sp,
                                color = Color(0xFF94A3B8)
                            ) 
                        },
                        leadingIcon = {
                            Icon(
                                Icons.Filled.Inventory,
                                contentDescription = null,
                                tint = Color(0xFF16A34A),
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(14.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        enabled = !viewModel.estaCargando,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF16A34A),
                            unfocusedBorderColor = Color(0xFFE2E8F0),
                            focusedContainerColor = Color(0xFFF8FAFC),
                            unfocusedContainerColor = Color(0xFFF8FAFC),
                            cursorColor = Color(0xFF16A34A)
                        ),
                        singleLine = true
                    )

                    Spacer(Modifier.height(24.dp))

                    // Categoría
                    Text(
                        text = "Categoría",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF334155),
                        letterSpacing = 0.3.sp
                    )
                    Spacer(Modifier.height(12.dp))
                    
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.height(220.dp)
                    ) {
                        items(categoriasDisponibles.size) { index ->
                            val (categoria, icon) = categoriasDisponibles[index]
                            val isSelected = categoriaSeleccionada == categoria
                            val interactionSource = remember { MutableInteractionSource() }
                            val isPressed by interactionSource.collectIsPressedAsState()
                            val scale by animateFloatAsState(
                                if (isPressed) 0.95f else 1f,
                                spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                                label = "scale"
                            )
                            
                            Card(
                                modifier = Modifier
                                    .graphicsLayer { scaleX = scale; scaleY = scale }
                                    .clickable(interactionSource = interactionSource, indication = null) {
                                        categoriaSeleccionada = categoria
                                    },
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isSelected) Color(0xFFF0FDF4) else Color(0xFFF8FAFC)
                                ),
                                elevation = CardDefaults.cardElevation(
                                    defaultElevation = if (isSelected) 4.dp else 0.dp
                                ),
                                border = CardDefaults.outlinedCardBorder().copy(
                                    width = if (isSelected) 2.dp else 1.dp,
                                    brush = if (isSelected) Brush.linearGradient(listOf(Color(0xFF16A34A), Color(0xFF22C55E))) else Brush.linearGradient(listOf(Color(0xFFE2E8F0), Color(0xFFE2E8F0)))
                                )
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(
                                        icon,
                                        contentDescription = null,
                                        tint = if (isSelected) Color(0xFF16A34A) else Color(0xFF64748B),
                                        modifier = Modifier.size(28.dp)
                                    )
                                    Spacer(Modifier.height(8.dp))
                                    Text(
                                        text = categoria,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) Color(0xFF16A34A) else Color(0xFF64748B),
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(24.dp))

        // Botón de publicación
        AnimatedVisibility(
            visible = isVisible,
            enter = fadeIn(tween(600, delayMillis = 400)) + slideInVertically(tween(600, delayMillis = 400, easing = EaseOutCubic), initialOffsetY = { it / 3 })
        ) {
            val interactionSource = remember { MutableInteractionSource() }
            val isPressed by interactionSource.collectIsPressedAsState()
            val scale by animateFloatAsState(
                if (isPressed) 0.97f else 1f,
                spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                label = "scale"
            )
            
            Button(
                onClick = {
                    val precio = precioProducto.toDoubleOrNull() ?: 0.0
                    val stock = cantidadInventario.toIntOrNull() ?: 0
                    viewModel.publicarNuevaCosecha(nombreProducto, precio, stock, categoriaSeleccionada)
                    nombreProducto = ""
                    precioProducto = ""
                    cantidadInventario = ""
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(58.dp)
                    .graphicsLayer { scaleX = scale; scaleY = scale }
                    .shadow(
                        elevation = 12.dp,
                        shape = RoundedCornerShape(16.dp),
                        spotColor = Color(0xFF16A34A).copy(alpha = 0.3f)
                    ),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent,
                    disabledContainerColor = Color(0xFFE2E8F0)
                ),
                enabled = nombreProducto.isNotEmpty() && precioProducto.isNotEmpty() && cantidadInventario.isNotEmpty() && !viewModel.estaCargando,
                interactionSource = interactionSource,
                contentPadding = PaddingValues(0.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            if (nombreProducto.isNotEmpty() && precioProducto.isNotEmpty() && cantidadInventario.isNotEmpty() && !viewModel.estaCargando)
                                gradientGreen
                            else
                                Brush.linearGradient(listOf(Color(0xFFCBD5E1), Color(0xFFCBD5E1)))
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (viewModel.estaCargando) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Row(
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.Send,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(Modifier.width(10.dp))
                            Text(
                                text = "Publicar Producto",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                letterSpacing = 0.3.sp
                            )
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(20.dp))

        // Mensaje de éxito
        AnimatedVisibility(
            visible = viewModel.productoPublicadoExito,
            enter = expandVertically(animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)) + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF0FDF4)),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Row(
                    modifier = Modifier.padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF16A34A)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Filled.CheckCircle,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    Spacer(Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "¡Producto publicado!",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF111827)
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = "Tu producto ya está disponible en la tienda",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF64748B)
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(40.dp))
    }
}
