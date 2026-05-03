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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
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
import androidx.compose.ui.window.Dialog
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
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    Color(0xFF8B0000),
                                    Color(0xFF3A0000),
                                    Color(0xFF000000)
                                ),
                                radius = 3000f,
                                center = Offset(0f, 0f)
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

    var showConfirm1 by remember { mutableStateOf(false) }
    var showConfirm2 by remember { mutableStateOf(false) }

    val sdf = SimpleDateFormat("dd / MM / yyyy", Locale.getDefault())
    val fechaHoy = sdf.format(Date())

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 33.dp, start = 12.dp, end = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
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

                if (viewModel.isDayLocked.value) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "🔒", fontSize = 18.sp)
                }
            }

            EpicProgressBar(progress = viewModel.progresoGlobal.intValue)
        }

        Row(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .weight(0.49f)
                    .fillMaxHeight()
                    .verticalScroll(scrollTareas)
            ) {
                TaskColumn(viewModel)
                Spacer(modifier = Modifier.height(100.dp))
            }

            Spacer(modifier = Modifier.weight(0.02f))

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
                Spacer(modifier = Modifier.height(16.dp))

                SupermarketComponent(viewModel)
                Spacer(modifier = Modifier.height(16.dp))

                NotesComponent(viewModel)
                Spacer(modifier = Modifier.height(16.dp))

                StatisticsCard(viewModel)
                Spacer(modifier = Modifier.height(24.dp))

                HorizontIAComponent()
                Spacer(modifier = Modifier.height(24.dp))

                if (!viewModel.isDayLocked.value) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(24.dp))
                            .background(GrisOscuro.copy(alpha = 0.5f))
                            .border(1.dp, RojoPrincipal.copy(alpha = 0.6f), RoundedCornerShape(24.dp))
                            .clickable { showConfirm1 = true }
                            .padding(vertical = 16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "COMPLETAR DÍA",
                            color = BlancoTexto,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp,
                            fontSize = 14.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(100.dp))
            }
        }
    }

    ManejadorDialogosCierre(
        show1 = showConfirm1,
        show2 = showConfirm2,
        onDismiss = {
            showConfirm1 = false
            showConfirm2 = false
        },
        onNext = {
            showConfirm1 = false
            showConfirm2 = true
        },
        onFinalConfirm = {
            viewModel.completarDia()
            showConfirm2 = false
        }
    )
}

@Composable
fun ManejadorDialogosCierre(
    show1: Boolean,
    show2: Boolean,
    onDismiss: () -> Unit,
    onNext: () -> Unit,
    onFinalConfirm: () -> Unit
) {
    if (show1) {
        AlertDialog(
            onDismissRequest = onDismiss,
            containerColor = GrisOscuro,
            title = { Text("¿FINALIZAR JORNADA?", color = BlancoTexto, fontWeight = FontWeight.Bold) },
            text = { Text("Si cierras el día, no podrás modificar el progreso actual.", color = BlancoTexto.copy(alpha = 0.7f)) },
            confirmButton = {
                TextButton(onClick = onNext) {
                    Text("CONTINUAR", color = RojoPrincipal, fontWeight = FontWeight.Black)
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text("CANCELAR", color = BlancoTexto)
                }
            }
        )
    }

    if (show2) {
        Dialog(onDismissRequest = onDismiss) {
            Column(
                modifier = Modifier
                    .clip(RoundedCornerShape(32.dp))
                    .background(GrisOscuro)
                    .border(2.dp, RojoPrincipal, RoundedCornerShape(32.dp))
                    .padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("⚠️ ADVERTENCIA FINAL", color = RojoPrincipal, fontWeight = FontWeight.Black, fontSize = 18.sp)
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "Esta acción bloqueará el progreso hasta mañana de forma irreversible. ¿Deseas proceder?",
                    color = BlancoTexto,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(32.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                        border = androidx.compose.foundation.BorderStroke(1.dp, BlancoTexto.copy(alpha = 0.3f))
                    ) {
                        Text("VOLVER")
                    }
                    Button(
                        onClick = onFinalConfirm,
                        colors = ButtonDefaults.buttonColors(containerColor = RojoPrincipal)
                    ) {
                        Text("SÍ, CERRAR", fontWeight = FontWeight.Bold)
                    }
                }
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
                drawRoundRect(
                    color = Color.White.copy(alpha = 0.1f),
                    size = size,
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(32.dp.toPx())
                )
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