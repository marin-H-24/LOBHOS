package com.marin.lobhos.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.unit.dp
import com.marin.lobhos.model.LobhosViewModel
import com.marin.lobhos.ui.theme.BlancoTexto
import com.marin.lobhos.ui.theme.GrisOscuro
import com.marin.lobhos.ui.theme.RojoPrincipal

@Composable
fun WaterGrid3D(viewModel: LobhosViewModel) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(32.dp))
            .background(GrisOscuro.copy(alpha = 0.4f))
            .border(1.dp, BlancoTexto.copy(alpha = 0.1f), RoundedCornerShape(32.dp))
            .padding(20.dp)
    ) {
        repeat(3) { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                repeat(3) { col ->
                    val index = row * 3 + col
                    WaterGlassPerfect3D(
                        isFilled = viewModel.vasosAgua.intValue > index,
                        onClick = {
                            if (viewModel.vasosAgua.intValue == index) {
                                viewModel.gestionarVaso(incrementar = true)
                            }
                        }
                    )
                }
            }
            if (row < 2) Spacer(modifier = Modifier.height(14.dp))
        }
    }
}

@Composable
fun WaterGlassPerfect3D(isFilled: Boolean, onClick: () -> Unit) {
    val fillLevel = remember { Animatable(0f) }

    // Animación ultrasuave desde abajo hacia arriba
    LaunchedEffect(isFilled) {
        fillLevel.animateTo(
            targetValue = if (isFilled) 0.85f else 0f,
            animationSpec = tween(
                durationMillis = 2000,
                easing = LinearOutSlowInEasing // Empieza rápido, termina muy suave
            )
        )
    }

    Canvas(
        modifier = Modifier
            .size(width = 44.dp, height = 66.dp)
            .clickable { onClick() }
    ) {
        val w = size.width
        val h = size.height

        // Coordenadas ajustadas: Base un poco más ancha (25% a 75%)
        val topL = w * 0.05f
        val topR = w * 0.95f
        val botL = w * 0.25f
        val botR = w * 0.75f

        // 1. Crear la silueta 3D del vaso
        val glassPath = Path().apply {
            // Boca (curva 3D superior)
            moveTo(topL, h * 0.05f)
            quadraticBezierTo(w * 0.5f, -h * 0.02f, topR, h * 0.05f)

            // Lado derecho
            lineTo(botR, h * 0.95f)

            // Base (curva 3D inferior)
            quadraticBezierTo(w * 0.5f, h * 1.02f, botL, h * 0.95f)

            // Lado izquierdo cerrando el trazo
            close()
        }

        // 2. Llenado del líquido desde abajo usando Máscara (clipPath)
        if (fillLevel.value > 0f) {
            val waterHeight = h * fillLevel.value

            clipPath(glassPath) {
                drawRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            RojoPrincipal.copy(alpha = 0.6f),
                            RojoPrincipal.copy(alpha = 0.95f)
                        ),
                        startY = h - waterHeight,
                        endY = h
                    ),
                    topLeft = Offset(0f, h - waterHeight),
                    size = Size(w, waterHeight)
                )
            }

            // Tensión Superficial 3D (El óvalo del agua arriba)
            val currentLeft = botL + (topL - botL) * fillLevel.value
            val currentRight = botR + (topR - botR) * fillLevel.value
            val surfaceY = h - waterHeight

            val surfacePath = Path().apply {
                moveTo(currentLeft, surfaceY)
                quadraticBezierTo(w * 0.5f, surfaceY + h * 0.06f, currentRight, surfaceY)
                quadraticBezierTo(w * 0.5f, surfaceY - h * 0.06f, currentLeft, surfaceY)
                close()
            }

            // Relleno de la superficie
            drawPath(surfacePath, color = RojoPrincipal.copy(alpha = 0.8f))
            // Borde brillante de la superficie del agua
            drawPath(surfacePath, color = Color.White.copy(alpha = 0.4f), style = Stroke(width = 1.dp.toPx()))
        }

        // 3. Dibujar el vidrio (Bordes externos y brillos)
        drawPath(
            path = glassPath,
            color = Color.White.copy(alpha = 0.25f),
            style = Stroke(width = 1.5.dp.toPx())
        )

        // Brillo cónico del lado izquierdo simulando reflexión de luz
        drawLine(
            brush = Brush.verticalGradient(
                colors = listOf(Color.White.copy(0.5f), Color.Transparent)
            ),
            start = Offset(w * 0.15f, h * 0.1f),
            end = Offset(w * 0.32f, h - 10f),
            strokeWidth = 2.dp.toPx()
        )

        // Borde superior de la boca del vaso
        val mouthPath = Path().apply {
            moveTo(topL, h * 0.05f)
            quadraticBezierTo(w * 0.5f, h * 0.12f, topR, h * 0.05f)
        }
        drawPath(
            path = mouthPath,
            color = Color.White.copy(alpha = 0.15f),
            style = Stroke(width = 1.dp.toPx())
        )
    }
}