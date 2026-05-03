package com.marin.lobhos.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.marin.lobhos.model.LobhosViewModel
import com.marin.lobhos.ui.theme.BlancoTexto
import com.marin.lobhos.ui.theme.GrisOscuro
import com.marin.lobhos.ui.theme.RojoPrincipal

@Composable
fun SupermarketComponent(viewModel: LobhosViewModel) {
    var mostrarPantallaFlotante by remember { mutableStateOf(false) }

    // --- WIDGET PRINCIPAL (VISTA RESUMIDA) ---
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(32.dp))
            .background(GrisOscuro.copy(alpha = 0.4f))
            .border(1.dp, BlancoTexto.copy(alpha = 0.1f), RoundedCornerShape(32.dp))
            .pointerInput(Unit) {
                detectTapGestures(
                    onLongPress = { mostrarPantallaFlotante = true },
                    onTap = { mostrarPantallaFlotante = true }
                )
            }
            .padding(20.dp)
    ) {
        Text(
            text = "SUPERMERCADO",
            color = BlancoTexto.copy(alpha = 0.6f),
            fontSize = 12.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 1.sp,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        viewModel.supermarketItems.forEach { item ->
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(14.dp)
                        .border(1.dp, if (item.marcado) RojoPrincipal else BlancoTexto.copy(alpha = 0.3f), CircleShape)
                        .background(if (item.marcado) RojoPrincipal else Color.Transparent, CircleShape)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = item.nombre,
                    color = if (item.marcado) BlancoTexto.copy(alpha = 0.4f) else BlancoTexto,
                    fontSize = 13.sp,
                    textDecoration = if (item.marcado) TextDecoration.LineThrough else TextDecoration.None
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "Mantén presionado para editar",
            color = BlancoTexto.copy(alpha = 0.3f),
            fontSize = 10.sp,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
    }

    // --- LLAMADO AL DIÁLOGO ---
    if (mostrarPantallaFlotante) {
        Dialog(onDismissRequest = { mostrarPantallaFlotante = false }) {
            GestorSupermercadoFlotante(viewModel) { mostrarPantallaFlotante = false }
        }
    }
}

@Composable
fun GestorSupermercadoFlotante(viewModel: LobhosViewModel, onClose: () -> Unit) {
    var nuevoItem by remember { mutableStateOf("") }
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(32.dp))
            .background(Color(0xFF140000))
            .border(1.dp, RojoPrincipal.copy(alpha = 0.3f), RoundedCornerShape(32.dp))
            .padding(24.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Text(text = "GESTOR DE SÚPER", color = BlancoTexto, fontSize = 14.sp, fontWeight = FontWeight.Black)
            Text(
                text = "✕",
                color = BlancoTexto.copy(alpha = 0.5f),
                fontSize = 22.sp,
                modifier = Modifier.clickable { onClose() }
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 250.dp)
                .verticalScroll(scrollState)
        ) {
            viewModel.supermarketItems.forEach { item ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(GrisOscuro.copy(alpha = 0.3f))
                        .clickable { viewModel.toggleSupermarketItem(item.id) }
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Box(
                            modifier = Modifier
                                .size(22.dp)
                                .border(1.dp, if (item.marcado) RojoPrincipal else BlancoTexto.copy(alpha = 0.3f), CircleShape)
                                .background(if (item.marcado) RojoPrincipal else Color.Transparent, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            if (item.marcado) {
                                Text("✓", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            text = item.nombre,
                            color = if (item.marcado) BlancoTexto.copy(alpha = 0.4f) else BlancoTexto,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            textDecoration = if (item.marcado) TextDecoration.LineThrough else TextDecoration.None
                        )
                    }
                    Text(
                        text = "✕",
                        color = BlancoTexto.copy(alpha = 0.3f),
                        fontSize = 18.sp,
                        modifier = Modifier.clickable { viewModel.eliminarSupermarketItem(item.id) }.padding(8.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = nuevoItem,
                onValueChange = { nuevoItem = it },
                placeholder = { Text("Agregar al súper...", color = BlancoTexto.copy(alpha = 0.3f), fontSize = 13.sp) },
                modifier = Modifier.weight(1f).height(55.dp),
                textStyle = TextStyle(color = BlancoTexto, fontSize = 14.sp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = RojoPrincipal,
                    unfocusedIndicatorColor = BlancoTexto.copy(alpha = 0.2f)
                ),
                singleLine = true
            )
            Spacer(modifier = Modifier.width(10.dp))
            Button(
                onClick = {
                    viewModel.agregarSupermarketItem(nuevoItem)
                    nuevoItem = ""
                },
                colors = ButtonDefaults.buttonColors(containerColor = RojoPrincipal.copy(alpha = 0.9f)),
                modifier = Modifier.size(55.dp),
                contentPadding = PaddingValues(0.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("+", color = Color.White, fontSize = 26.sp, fontWeight = FontWeight.Light)
            }
        }
    }
}