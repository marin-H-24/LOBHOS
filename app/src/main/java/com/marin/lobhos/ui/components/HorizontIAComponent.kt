package com.marin.lobhos.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.marin.lobhos.ui.theme.BlancoTexto
import com.marin.lobhos.ui.theme.GrisOscuro

@Composable
fun HorizontIAComponent() {
    val gradientBorder = Brush.horizontalGradient(
        colors = listOf(Color(0xFFED1C24), Color(0xFFFF5722))
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(32.dp))
            .background(GrisOscuro.copy(alpha = 0.6f))
            .border(2.dp, gradientBorder, RoundedCornerShape(32.dp))
            .clickable { /* MVP: Espacio reservado para la IA */ }
            .padding(vertical = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "🧠",
                fontSize = 18.sp
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "HORIZONT IA",
                color = BlancoTexto,
                fontWeight = FontWeight.Black,
                letterSpacing = 2.sp,
                fontSize = 14.sp
            )
        }
    }
}