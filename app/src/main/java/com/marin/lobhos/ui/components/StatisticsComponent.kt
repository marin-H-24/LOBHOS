package com.marin.lobhos.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.marin.lobhos.model.LobhosViewModel
import com.marin.lobhos.ui.theme.BlancoTexto
import com.marin.lobhos.ui.theme.GrisOscuro
import com.marin.lobhos.ui.theme.RojoPrincipal

@Composable
fun StatisticsCard(viewModel: LobhosViewModel) {
    var mostrarGrafico by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(32.dp))
            .background(GrisOscuro.copy(alpha = 0.4f))
            .border(1.dp, BlancoTexto.copy(alpha = 0.1f), RoundedCornerShape(32.dp))
            .pointerInput(Unit) {
                detectTapGestures(
                    onLongPress = { mostrarGrafico = true },
                    onTap = { mostrarGrafico = true }
                )
            }
            .padding(20.dp)
    ) {
        Text(
            text = "RENDIMIENTO",
            color = BlancoTexto.copy(alpha = 0.6f),
            fontSize = 12.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 1.sp,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("🔥 RACHA", color = BlancoTexto.copy(alpha = 0.5f), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                Text("${viewModel.rachaActual.intValue} DÍAS", color = RojoPrincipal, fontSize = 18.sp, fontWeight = FontWeight.Black)
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("📊 PROMEDIO (7D)", color = BlancoTexto.copy(alpha = 0.5f), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                Text("${viewModel.promedioSemanal.intValue}%", color = RojoPrincipal, fontSize = 18.sp, fontWeight = FontWeight.Black)
            }
        }
    }

    if (mostrarGrafico) {
        Dialog(onDismissRequest = { mostrarGrafico = false }) {
            GestorGraficoFlotante(viewModel) { mostrarGrafico = false }
        }
    }
}

@Composable
fun GestorGraficoFlotante(viewModel: LobhosViewModel, onClose: () -> Unit) {
    val historial = viewModel.historialDatos.value
    val valores = historial.entries.sortedBy { it.key }.takeLast(30).map { it.value }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(32.dp))
            .background(Color(0xFF140000))
            .border(1.dp, RojoPrincipal.copy(alpha = 0.4f), RoundedCornerShape(32.dp))
            .padding(24.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Text(text = "HISTORIAL (30 DÍAS)", color = BlancoTexto, fontSize = 14.sp, fontWeight = FontWeight.Black)
            Text(
                text = "✕",
                color = BlancoTexto.copy(alpha = 0.5f),
                fontSize = 22.sp,
                modifier = Modifier.clickable { onClose() }
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .background(GrisOscuro.copy(alpha = 0.2f), RoundedCornerShape(16.dp))
                .padding(16.dp)
        ) {
            if (valores.size > 1) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val maxVal = 100f
                    val stepX = size.width / (valores.size - 1)

                    val path = Path()
                    valores.forEachIndexed { index, value ->
                        val x = index * stepX
                        val y = size.height - ((value / maxVal) * size.height)
                        if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)

                        drawCircle(
                            color = if (value == 100) Color(0xFF00C853) else RojoPrincipal,
                            radius = 4.dp.toPx(),
                            center = Offset(x, y)
                        )
                    }

                    drawPath(
                        path = path,
                        color = RojoPrincipal.copy(alpha = 0.8f),
                        style = Stroke(
                            width = 3.dp.toPx(),
                            cap = StrokeCap.Round,
                            join = StrokeJoin.Round
                        )
                    )
                }
            } else {
                Text(
                    text = "No hay suficientes datos para el gráfico.",
                    color = BlancoTexto.copy(alpha = 0.4f),
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }
    }
}