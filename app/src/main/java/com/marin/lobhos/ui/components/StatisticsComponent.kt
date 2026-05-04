package com.marin.lobhos.ui.components

import android.graphics.Paint
import android.graphics.Typeface
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
import androidx.compose.ui.graphics.nativeCanvas
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
    val historialOrdenado = historial.entries.sortedBy { it.key }.takeLast(30)
    val valores = historialOrdenado.map { it.value }
    val fechas = historialOrdenado.map { it.key.takeLast(2) } // Muestra solo el dia

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
                .height(280.dp)
                .background(GrisOscuro.copy(alpha = 0.2f), RoundedCornerShape(16.dp))
                .padding(start = 8.dp, end = 16.dp, top = 20.dp, bottom = 30.dp)
        ) {
            if (valores.isNotEmpty()) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val maxVal = 100f
                    val paddingX = 70f // Espacio lateral para los numeros del eje Y
                    val drawWidth = size.width - paddingX
                    val drawHeight = size.height

                    val textPaint = Paint().apply {
                        color = android.graphics.Color.argb(128, 255, 255, 255)
                        textSize = 28f
                        typeface = Typeface.DEFAULT_BOLD
                        textAlign = Paint.Align.RIGHT
                    }

                    // 1. Dibujar Eje Y
                    val niveles = listOf(0, 25, 50, 75, 100)
                    niveles.forEach { nivel ->
                        val yPos = drawHeight - ((nivel / maxVal) * drawHeight)
                        drawContext.canvas.nativeCanvas.drawText(
                            "$nivel%",
                            paddingX - 15f,
                            yPos + 10f,
                            textPaint
                        )
                        drawLine(
                            color = Color.White.copy(alpha = 0.1f),
                            start = Offset(paddingX, yPos),
                            end = Offset(size.width, yPos),
                            strokeWidth = 2f
                        )
                    }

                    // 2. Dibujar Linea del Grafico y Eje X
                    if (valores.size > 1) {
                        val stepX = drawWidth / (valores.size - 1)
                        val path = Path()

                        textPaint.textAlign = Paint.Align.CENTER

                        valores.forEachIndexed { index, value ->
                            val x = paddingX + (index * stepX)
                            val y = drawHeight - ((value / maxVal) * drawHeight)

                            if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)

                            // Etiqueta del dia (Eje X)
                            if (valores.size <= 7 || index % 3 == 0 || index == valores.size - 1) {
                                drawContext.canvas.nativeCanvas.drawText(
                                    fechas[index],
                                    x,
                                    drawHeight + 40f,
                                    textPaint
                                )
                            }

                            drawCircle(
                                color = if (value == 100) Color(0xFF00C853) else RojoPrincipal,
                                radius = 6.dp.toPx(),
                                center = Offset(x, y)
                            )
                        }

                        drawPath(
                            path = path,
                            color = RojoPrincipal.copy(alpha = 0.8f),
                            style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                        )
                    } else {
                        // Un solo dato
                        val x = paddingX + drawWidth / 2
                        val y = drawHeight - ((valores[0] / maxVal) * drawHeight)
                        drawCircle(
                            color = if (valores[0] == 100) Color(0xFF00C853) else RojoPrincipal,
                            radius = 6.dp.toPx(),
                            center = Offset(x, y)
                        )
                        drawContext.canvas.nativeCanvas.drawText(
                            fechas[0],
                            x,
                            drawHeight + 40f,
                            textPaint.apply { textAlign = Paint.Align.CENTER }
                        )
                    }
                }
            } else {
                Text(
                    text = "Aún no hay datos guardados.",
                    color = BlancoTexto.copy(alpha = 0.4f),
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }
    }
}