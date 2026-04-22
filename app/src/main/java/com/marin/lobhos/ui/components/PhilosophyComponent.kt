package com.marin.lobhos.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.marin.lobhos.model.LobhosViewModel
import com.marin.lobhos.ui.theme.BlancoTexto
import com.marin.lobhos.ui.theme.GrisOscuro
import com.marin.lobhos.ui.theme.RojoPrincipal

@Composable
fun PhilosophyCard(viewModel: LobhosViewModel) {
    var editando by remember { mutableStateOf(false) }
    var textoTemporal by remember { mutableStateOf(viewModel.fraseDiaria.value) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(32.dp))
            .background(GrisOscuro.copy(alpha = 0.4f))
            .border(1.dp, BlancoTexto.copy(alpha = 0.1f), RoundedCornerShape(32.dp))
            .clickable { editando = true }
            .padding(20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "FILOSOFÍA DIARIA",
                color = BlancoTexto.copy(alpha = 0.6f),
                fontSize = 12.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.sp
            )
            // Indicador de estado (Rojo si estás editando, Gris si solo estás leyendo)
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(
                        color = if (editando) RojoPrincipal else BlancoTexto.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(50)
                    )
            )
        }

        if (editando) {
            TextField(
                value = textoTemporal,
                onValueChange = {
                    textoTemporal = it
                    viewModel.setFrase(it)
                },
                textStyle = TextStyle(
                    color = BlancoTexto,
                    fontSize = 15.sp,
                    fontStyle = FontStyle.Italic,
                    lineHeight = 22.sp
                ),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    disabledContainerColor = Color.Transparent,
                    focusedIndicatorColor = RojoPrincipal,
                    unfocusedIndicatorColor = RojoPrincipal.copy(alpha = 0.5f),
                    cursorColor = RojoPrincipal
                ),
                modifier = Modifier.fillMaxWidth()
            )

            // Botón sutil para "Guardar" y salir del modo edición
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                Text(
                    text = "Listo",
                    color = RojoPrincipal,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    modifier = Modifier
                        .padding(top = 8.dp)
                        .clickable { editando = false }
                        .padding(4.dp)
                )
            }
        } else {
            Text(
                text = viewModel.fraseDiaria.value,
                color = BlancoTexto,
                fontSize = 15.sp,
                fontStyle = FontStyle.Italic,
                lineHeight = 24.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(vertical = 4.dp)
            )
        }
    }
}