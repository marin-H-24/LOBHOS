package com.marin.lobhos

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.marin.lobhos.model.LobhosViewModel
import com.marin.lobhos.ui.components.*
import com.marin.lobhos.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory

class MainActivity : ComponentActivity() {
    private val viewModel: LobhosViewModel by viewModels {
        viewModelFactory {
            initializer {
                val application = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as android.app.Application
                LobhosViewModel(application)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            LobhosTheme {
                // --- FONDO MAESTRO ILUMINADO ---
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    Color(0xFF8B0000), // Rojo intenso y muy visible
                                    Color(0xFF3A0000), // Transición media
                                    Color(0xFF000000)  // Negro absoluto en el resto
                                ),
                                radius = 3000f, // Radio ampliado para que el rojo cubra más
                                center = Offset(0f, 0f) // Nace desde la esquina superior izquierda
                            )
                        )
                ) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = Color.Transparent
                    ) {
                        MainScreen(viewModel)
                    }
                }
            }
        }
    }
}

@Composable
fun MainScreen(viewModel: LobhosViewModel) {
    val scrollTareas = rememberScrollState()
    val scrollWidgets = rememberScrollState()

    // Fecha Numérica
    val sdf = SimpleDateFormat("dd / MM / yyyy", Locale.getDefault())
    val fechaHoy = sdf.format(Date())

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 30.dp, start = 12.dp, end = 12.dp)
    ) {
        // Cabecera glassmorphic
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = fechaHoy,
                color = BlancoTexto,
                fontSize = 22.sp,
                fontWeight = FontWeight.Black,
                modifier = Modifier
                    .clip(RoundedCornerShape(32.dp))
                    .background(GrisOscuro.copy(alpha = 0.3f))
                    .border(1.dp, BlancoTexto.copy(alpha = 0.2f), RoundedCornerShape(32.dp))
                    .padding(horizontal = 14.dp, vertical = 8.dp)
            )

            EpicProgressBar(progress = viewModel.progresoGlobal.intValue)
        }

        // Distribución Exacta: 49% | 2% | 49%
        Row(modifier = Modifier.fillMaxSize()) {

            // Columna 1: Tareas (49%)
            Column(
                modifier = Modifier
                    .weight(0.49f)
                    .fillMaxHeight()
                    .verticalScroll(scrollTareas)
            ) {
                TaskColumn(viewModel)
                Spacer(modifier = Modifier.height(100.dp))
            }

            // Separador (2%)
            Spacer(modifier = Modifier.weight(0.02f))

            // Columna 2: Widgets (49%)
            Column(
                modifier = Modifier
                    .weight(0.49f)
                    .fillMaxHeight()
                    .verticalScroll(scrollWidgets)
            ) {
                PhilosophyCard(viewModel)
                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "HIDRATACIÓN",
                    color = BlancoTexto.copy(alpha = 0.6f),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
                )

                WaterGrid3D(viewModel)

                Spacer(modifier = Modifier.height(16.dp))
                JeickoCard(viewModel)

                Spacer(modifier = Modifier.height(16.dp))
                ShoppingCard(viewModel)

                Spacer(modifier = Modifier.height(100.dp))
            }
        }
    }
}

@Composable
fun EpicProgressBar(progress: Int) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress / 100f,
        animationSpec = tween(durationMillis = 1200), label = ""
    )

    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(width = 110.dp, height = 22.dp)
                .clip(RoundedCornerShape(32.dp))
                .background(GrisOscuro.copy(alpha = 0.3f))
                .border(1.dp, BlancoTexto.copy(alpha = 0.15f), RoundedCornerShape(32.dp)),
            contentAlignment = Alignment.CenterStart
        ) {
            Canvas(modifier = Modifier.fillMaxSize().padding(horizontal = 2.dp, vertical = 2.dp)) {
                // Fondo de la barra
                drawRoundRect(
                    color = Color.White.copy(alpha = 0.1f),
                    size = size,
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(32.dp.toPx())
                )
                // Relleno animado
                drawRoundRect(
                    brush = Brush.horizontalGradient(listOf(Color(0xFF7B0000), RojoPrincipal)),
                    size = Size(width = size.width * animatedProgress, height = size.height),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(32.dp.toPx())
                )
            }
        }
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = "$progress%",
            color = RojoPrincipal,
            fontSize = 17.sp,
            fontWeight = FontWeight.ExtraBold
        )
    }
}