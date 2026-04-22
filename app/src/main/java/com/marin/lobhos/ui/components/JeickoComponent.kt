package com.marin.lobhos.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.marin.lobhos.model.LobhosViewModel
import com.marin.lobhos.ui.theme.BlancoTexto
import com.marin.lobhos.ui.theme.GrisOscuro
import com.marin.lobhos.ui.theme.RojoPrincipal

@Composable
fun PawPrintRealista(
    modifier: Modifier = Modifier,
    color: Color = BlancoTexto
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        // Dedos Superiores (Inclinados y redondeados)
        drawOval(color = color, topLeft = Offset(w * 0.05f, h * 0.22f), size = Size(w * 0.22f, h * 0.32f))
        drawOval(color = color, topLeft = Offset(w * 0.32f, h * 0.05f), size = Size(w * 0.22f, h * 0.35f))
        drawOval(color = color, topLeft = Offset(w * 0.58f, h * 0.05f), size = Size(w * 0.22f, h * 0.35f))
        drawOval(color = color, topLeft = Offset(w * 0.85f, h * 0.22f), size = Size(w * 0.22f, h * 0.32f))

        // Almohadilla Central Estilizada
        val mainPadPath = Path().apply {
            moveTo(w * 0.5f, h * 0.48f)
            cubicTo(w * 0.30f, h * 0.45f, w * 0.10f, h * 0.60f, w * 0.15f, h * 0.80f)
            cubicTo(w * 0.20f, h * 1.00f, w * 0.80f, h * 1.00f, w * 0.85f, h * 0.80f)
            cubicTo(w * 0.90f, h * 0.60f, w * 0.70f, h * 0.45f, w * 0.50f, h * 0.48f)
            close()
        }
        drawPath(path = mainPadPath, color = color, style = Fill)
    }
}

@Composable
fun JeickoCard(viewModel: LobhosViewModel) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(32.dp))
            .background(GrisOscuro.copy(alpha = 0.4f))
            .border(1.dp, BlancoTexto.copy(alpha = 0.1f), RoundedCornerShape(32.dp))
            .padding(20.dp)
    ) {
        Text(
            text = "JEICKO",
            color = BlancoTexto,
            fontSize = 18.sp,
            fontWeight = FontWeight.Black,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        // SOLUCIÓN: Pasamos el 'salida.id' exacto para que el ViewModel sepa cuál marcar
        viewModel.salidasJeicko.forEach { salida ->
            SalidaPawItem(
                label = salida.etiqueta,
                isDone = salida.realizada,
                onToggle = { viewModel.toggleSalida(salida.id) }
            )
        }
    }
}

@Composable
fun SalidaPawItem(label: String, isDone: Boolean, onToggle: () -> Unit) {
    val animatedColor by animateColorAsState(
        targetValue = if (isDone) RojoPrincipal else BlancoTexto.copy(alpha = 0.25f),
        animationSpec = tween(durationMillis = 350), label = ""
    )

    val glowOpacity by animateFloatAsState(
        targetValue = if (isDone) 0.65f else 0f,
        animationSpec = tween(durationMillis = 400), label = ""
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null // Elimina el cuadro gris/rojo al tocar
            ) { onToggle() },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(34.dp),
            contentAlignment = Alignment.Center
        ) {
            // Efecto Neón
            if (isDone) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .blur(12.dp)
                        .background(RojoPrincipal.copy(alpha = glowOpacity))
                )
            }

            PawPrintRealista(
                modifier = Modifier.fillMaxSize(),
                color = animatedColor
            )
        }

        Spacer(modifier = Modifier.width(18.dp))

        // Etiqueta Estilo Glassmorphism
        Box(
            modifier = Modifier
                .border(
                    width = 1.dp,
                    color = if (isDone) RojoPrincipal.copy(alpha = 0.5f) else BlancoTexto.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(14.dp)
                )
                .background(
                    color = if (isDone) RojoPrincipal.copy(alpha = 0.1f) else Color.Transparent,
                    shape = RoundedCornerShape(14.dp)
                )
                .padding(horizontal = 14.dp, vertical = 6.dp)
        ) {
            Text(
                text = label,
                color = if (isDone) BlancoTexto else BlancoTexto.copy(alpha = 0.35f),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}