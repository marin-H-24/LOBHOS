package com.marin.lobhos.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = RojoPrincipal,
    secondary = RojoOscuro,
    background = FondoNegro,
    surface = GrisOscuro,
    onPrimary = Color.White,
    onBackground = BlancoTexto,
    onSurface = BlancoTexto
)

@Composable
fun LobhosTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        content = content
    )
}