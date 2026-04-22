package com.marin.lobhos.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.marin.lobhos.model.LobhosViewModel
import com.marin.lobhos.model.Tarea
import com.marin.lobhos.ui.theme.BlancoTexto
import com.marin.lobhos.ui.theme.GrisOscuro
import com.marin.lobhos.ui.theme.RojoPrincipal

@Composable
fun TaskColumn(viewModel: LobhosViewModel, modifier: Modifier = Modifier) {
    var mostrarInput by remember { mutableStateOf(false) }
    var nuevaTarea by remember { mutableStateOf("") }

    Column(modifier = modifier) {
        // --- LISTA DE TAREAS ---
        viewModel.tareas.forEach { tarea ->
            TaskProItem(
                tarea = tarea,
                onToggle = { viewModel.toggleTarea(tarea.id) },
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
                        focusedIndicatorColor = Color.Transparent, // Sin línea inferior
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
}

@Composable
fun TaskProItem(tarea: Tarea, onToggle: () -> Unit, onDelete: () -> Unit) {
    // Transiciones suaves de color
    val bgColor by animateColorAsState(
        targetValue = if (tarea.completada) RojoPrincipal.copy(alpha = 0.15f) else GrisOscuro.copy(alpha = 0.4f),
        label = ""
    )
    val borderColor by animateColorAsState(
        targetValue = if (tarea.completada) RojoPrincipal.copy(alpha = 0.5f) else BlancoTexto.copy(alpha = 0.1f),
        label = ""
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(32.dp))
            .background(bgColor)
            .border(1.dp, borderColor, RoundedCornerShape(32.dp))
            .clickable { onToggle() }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Texto de la Tarea
        Text(
            text = tarea.nombre,
            color = if (tarea.completada) BlancoTexto.copy(alpha = 0.4f) else BlancoTexto,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            textDecoration = if (tarea.completada) TextDecoration.LineThrough else TextDecoration.None,
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

        // Cuadro de Verificación (Check) Redondeado
        Box(
            modifier = Modifier
                .size(22.dp)
                .border(
                    width = 1.dp,
                    color = if (tarea.completada) RojoPrincipal else BlancoTexto.copy(alpha = 0.3f),
                    shape = RoundedCornerShape(8.dp)
                )
                .background(
                    color = if (tarea.completada) RojoPrincipal else Color.Transparent,
                    shape = RoundedCornerShape(8.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            if (tarea.completada) {
                Text("✓", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Black)
            }
        }
    }
}