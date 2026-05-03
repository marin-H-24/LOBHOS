package com.marin.lobhos.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.marin.lobhos.model.LobhosViewModel
import com.marin.lobhos.ui.theme.* import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HeaderComponent(viewModel: LobhosViewModel) {
    val sdf = SimpleDateFormat("EEEE, d 'de' MMMM", Locale("es", "ES"))
    val fechaActual = sdf.format(Date()).replaceFirstChar { it.uppercase() }
    val isLocked = viewModel.isDayLocked.value

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(GrisOscuro)
            .border(1.dp, if (isLocked) VerdeOK.copy(alpha = 0.5f) else RojoPrincipal.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = fechaActual,
                color = if (isLocked) VerdeOK else RojoPrincipal,
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold
            )

            if (isLocked) {
                Text(
                    text = "JORNADA COMPLETADA",
                    color = VerdeOK,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Black
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "“La disciplina es el puente entre metas y logros.”",
            color = BlancoTexto.copy(alpha = 0.8f),
            fontSize = 14.sp,
            lineHeight = 20.sp
        )
    }
}