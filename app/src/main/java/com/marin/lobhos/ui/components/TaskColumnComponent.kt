package com.marin.lobhos.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.marin.lobhos.model.LobhosViewModel
import com.marin.lobhos.model.Tarea
import com.marin.lobhos.ui.theme.BlancoTexto
import com.marin.lobhos.ui.theme.GrisOscuro
import com.marin.lobhos.ui.theme.RojoPrincipal

@Composable
fun TaskColumn(viewModel: LobhosViewModel, modifier: Modifier = Modifier) {
    var mostrarInput by remember { mutableStateOf(false) }
    var nuevaTarea by remember { mutableStateOf("") }

    // Estado para mostrar el diálogo selector de peso
    var tareaParaPeso by remember { mutableStateOf<Tarea?>(null) }

    Column(modifier = modifier) {
        // --- LISTA DE TAREAS ---
        viewModel.tareas.forEach { tarea ->
            TaskProItem(
                tarea = tarea,
                onToggleCompleta = { viewModel.toggleTareaCompleta(tarea.id) },
                onToggleIncompleta = { viewModel.toggleTareaIncompleta(tarea.id) },
                onConfigurarPeso = { tareaParaPeso = tarea },
                onDelete = { viewModel.eliminarTarea(tarea.id) }
            )
            Spacer(modifier = Modifier.height(10.dp))
        }

        // --- ANIMACIÓN PARA AGREGAR NUEVA TAREA ---
        AnimatedVisibility(visible = mostrarInput) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(32.dp))
                    .background(GrisOscuro.copy(alpha = 0.4f))
                    .border(1.dp, BlancoTexto.copy(alpha = 0.1f), RoundedCornerShape(32.dp))
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = nuevaTarea,
                    onValueChange = { nuevaTarea = it },
                    placeholder = { Text("Nueva tarea...", color = BlancoTexto.copy(alpha = 0.4f), fontSize = 13.sp) },
                    modifier = Modifier.weight(1f).height(50.dp),
                    textStyle = TextStyle(color = BlancoTexto, fontSize = 13.sp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    singleLine = true
                )
                Button(
                    onClick = {
                        viewModel.agregarTarea(nuevaTarea)
                        nuevaTarea = ""
                        mostrarInput = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RojoPrincipal),
                    modifier = Modifier.size(40.dp),
                    contentPadding = PaddingValues(0.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("✓", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }

        // --- BOTÓN ESTILO iPHONE PARA AGREGAR (+) ---
        if (!mostrarInput) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(32.dp))
                    .background(GrisOscuro.copy(alpha = 0.3f))
                    .border(1.dp, BlancoTexto.copy(alpha = 0.15f), RoundedCornerShape(32.dp))
                    .clickable { mostrarInput = true }
                    .padding(vertical = 14.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("+", color = BlancoTexto.copy(alpha = 0.7f), fontSize = 22.sp, fontWeight = FontWeight.Light)
            }
        }
    }

    // --- DIÁLOGO SELECTOR DE PESO (Glassmorphism) ---
    tareaParaPeso?.let { tarea ->
        Dialog(onDismissRequest = { tareaParaPeso = null }) {
            Column(
                modifier = Modifier
                    .clip(RoundedCornerShape(24.dp))
                    .background(GrisOscuro.copy(alpha = 0.98f))
                    .border(1.dp, RojoPrincipal.copy(alpha = 0.4f), RoundedCornerShape(24.dp))
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("IMPACTO DE LA TAREA", color = BlancoTexto, fontWeight = FontWeight.Black, fontSize = 15.sp)
                Text(tarea.nombre, color = BlancoTexto.copy(alpha = 0.6f), fontSize = 12.sp, modifier = Modifier.padding(bottom = 24.dp, top = 4.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    (1..5).forEach { peso ->
                        val isSelected = tarea.peso == peso
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) RojoPrincipal else Color.Transparent)
                                .border(1.dp, if (isSelected) RojoPrincipal else BlancoTexto.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                                .clickable {
                                    viewModel.actualizarPesoTarea(tarea.id, peso)
                                    tareaParaPeso = null
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(peso.toString(), color = BlancoTexto, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TaskProItem(
    tarea: Tarea,
    onToggleCompleta: () -> Unit,
    onToggleIncompleta: () -> Unit,
    onConfigurarPeso: () -> Unit,
    onDelete: () -> Unit
) {
    val haptic = LocalHapticFeedback.current

    // Transiciones suaves de color
    val bgColor by animateColorAsState(
        targetValue = when {
            tarea.completada -> RojoPrincipal.copy(alpha = 0.15f)
            tarea.incompleta -> Color(0xFF330000).copy(alpha = 0.6f) // Rojo muy oscuro
            else -> GrisOscuro.copy(alpha = 0.4f)
        },
        label = "bgAnim"
    )
    val borderColor by animateColorAsState(
        targetValue = when {
            tarea.completada -> RojoPrincipal.copy(alpha = 0.5f)
            tarea.incompleta -> Color(0xFF660000).copy(alpha = 0.8f)
            else -> BlancoTexto.copy(alpha = 0.1f)
        },
        label = "borderAnim"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(32.dp))
            .background(bgColor)
            .border(1.dp, borderColor, RoundedCornerShape(32.dp))
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = {
                        // 1 Toque: Completa
                        onToggleCompleta()
                    },
                    onDoubleTap = {
                        // Doble Toque: Configurar Peso
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onConfigurarPeso()
                    },
                    onLongPress = {
                        // Mantener presionado: Incompleta (X)
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onToggleIncompleta()
                    }
                )
            }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Indicador de Peso (Visible solo si es mayor a 1)
        if (tarea.peso > 1) {
            Text(
                text = "x${tarea.peso}",
                color = RojoPrincipal,
                fontSize = 11.sp,
                fontWeight = FontWeight.Black,
                modifier = Modifier.padding(end = 8.dp)
            )
        }

        // Texto de la Tarea
        Text(
            text = tarea.nombre,
            color = if (tarea.completada || tarea.incompleta) BlancoTexto.copy(alpha = 0.4f) else BlancoTexto,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            textDecoration = if (tarea.completada || tarea.incompleta) TextDecoration.LineThrough else TextDecoration.None,
            modifier = Modifier.weight(1f)
        )

        Spacer(modifier = Modifier.width(8.dp))

        // Botón Eliminar (✕)
        Text(
            text = "✕",
            color = BlancoTexto.copy(alpha = 0.3f),
            fontSize = 14.sp,
            modifier = Modifier
                .clickable { onDelete() }
                .padding(horizontal = 8.dp)
        )

        // Cuadro de Verificación (Check o X)
        Box(
            modifier = Modifier
                .size(22.dp)
                .border(
                    width = 1.dp,
                    color = when {
                        tarea.completada -> RojoPrincipal
                        tarea.incompleta -> Color(0xFF660000)
                        else -> BlancoTexto.copy(alpha = 0.3f)
                    },
                    shape = RoundedCornerShape(8.dp)
                )
                .background(
                    color = when {
                        tarea.completada -> RojoPrincipal
                        else -> Color.Transparent
                    },
                    shape = RoundedCornerShape(8.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            if (tarea.completada) {
                Text("✓", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Black)
            } else if (tarea.incompleta) {
                Text("✕", color = Color(0xFF880000), fontSize = 12.sp, fontWeight = FontWeight.Black)
            }
        }
    }
}