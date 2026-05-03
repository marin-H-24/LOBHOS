package com.marin.lobhos.model

import android.app.Application
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.marin.lobhos.data.LobhosStore
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable

// --- MODELOS DE DATOS V2 ---
@Serializable
data class Tarea(
    val id: Long = System.currentTimeMillis(),
    val nombre: String,
    var completada: Boolean = false,
    var incompleta: Boolean = false, // V2: Estado para la "X"
    var peso: Int = 1 // V2: Valor de impacto (1-5)
)

@Serializable
data class Compra(
    val id: Long = System.currentTimeMillis(),
    val nombre: String,
    val precio: Double,
    var comprada: Boolean = false
)

@Serializable
data class SalidaJeicko(
    val id: Int,
    val etiqueta: String,
    var realizada: Boolean = false
)

@Serializable
data class Nota(
    val id: Long = System.currentTimeMillis(),
    val contenido: String
)

@Serializable
data class SupermarketItem(
    val id: Long = System.currentTimeMillis(),
    val nombre: String,
    var marcado: Boolean = false
)

class LobhosViewModel(application: Application) : AndroidViewModel(application) {
    private val store = LobhosStore(application)

    // Estados de la UI (Listas)
    val tareas = mutableStateListOf<Tarea>()
    val compras = mutableStateListOf<Compra>()
    val salidasJeicko = mutableStateListOf<SalidaJeicko>()
    val notas = mutableStateListOf<Nota>() // V2
    val supermarketItems = mutableStateListOf<SupermarketItem>() // V2

    // Variables de Estado
    var vasosAgua = mutableIntStateOf(0)
    var fraseDiaria = mutableStateOf("“LA DISCIPLINA ES EL PUENTE ENTRE METAS Y LOGROS.”")
    var progresoGlobal = mutableIntStateOf(0)
    var presupuestoTotal = mutableStateOf(0.0)
    var isDayLocked = mutableStateOf(false) // V2: Control de bloqueo diario

    init {
        viewModelScope.launch {
            // Verificamos el día. Pasamos el progreso actual para guardarlo en el historial
            store.verificarYReiniciarSiEsNuevoDia(progresoGlobal.intValue)

            // Cargas desde DataStore
            store.tareasFlow.collect { if (it.isNotEmpty()) { tareas.clear(); tareas.addAll(it) }; actualizarTodo() }
        }
        viewModelScope.launch { store.comprasFlow.collect { if (it.isNotEmpty()) { compras.clear(); compras.addAll(it) }; actualizarPresupuesto() } }
        viewModelScope.launch {
            store.jeickoFlow.collect {
                salidasJeicko.clear()
                if (it.isEmpty()) {
                    salidasJeicko.addAll(listOf(SalidaJeicko(1, "SALIDA I"), SalidaJeicko(2, "SALIDA II"), SalidaJeicko(3, "SALIDA III")))
                } else {
                    salidasJeicko.addAll(it)
                }
                actualizarTodo()
            }
        }
        viewModelScope.launch { store.aguaFlow.collect { vasosAgua.intValue = it; actualizarTodo() } }
        viewModelScope.launch { store.fraseFlow.collect { fraseDiaria.value = it } }

        // Cargas V2
        viewModelScope.launch { store.notasFlow.collect { if (it.isNotEmpty()) { notas.clear(); notas.addAll(it) } } }
        viewModelScope.launch { store.supermarketFlow.collect { if (it.isNotEmpty()) { supermarketItems.clear(); supermarketItems.addAll(it) } } }
        viewModelScope.launch { store.dayLockedFlow.collect { isDayLocked.value = it } }
    }

    // --- LÓGICA DE PERSISTENCIA MAESTRA ---
    private fun save() {
        viewModelScope.launch {
            store.guardarTareas(tareas)
            store.guardarCompras(compras)
            store.guardarAgua(vasosAgua.intValue)
            store.guardarJeicko(salidasJeicko)
            store.guardarFrase(fraseDiaria.value)
            store.guardarNotas(notas)
            store.guardarSupermarket(supermarketItems)
            store.setDayLocked(isDayLocked.value)
        }
    }

    // --- CÁLCULO INTELIGENTE V2 (8% / 22% / 70%) ---
    fun actualizarTodo() {
        // 1. Agua: 9 vasos = 8%
        val progresoAgua = (vasosAgua.intValue.toFloat() / 9f) * 8f

        // 2. Jeicko: 3 salidas = 22%
        val completadasJeicko = salidasJeicko.count { it.realizada }.toFloat()
        val progresoJeicko = (completadasJeicko / 3f) * 22f

        // 3. Tareas: Ponderación = 70%
        val progresoTareas = if (tareas.isEmpty()) {
            0f
        } else {
            val pesoTotal = tareas.sumOf { it.peso }.toFloat()
            val pesoLogrado = tareas.filter { it.completada }.sumOf { it.peso }.toFloat()
            (pesoLogrado / pesoTotal) * 70f
        }

        progresoGlobal.intValue = (progresoAgua + progresoJeicko + progresoTareas).toInt().coerceIn(0, 100)
    }

    // --- BLOQUEO DE DÍA (V2) ---
    fun completarDia() {
        isDayLocked.value = true
        save()
    }

    // --- FUNCIONES DE TAREAS (Actualizadas con Bloqueo) ---
    fun agregarTarea(nombre: String) {
        if (nombre.isNotBlank()) {
            tareas.add(Tarea(nombre = nombre.uppercase()))
            actualizarTodo()
            save()
        }
    }

    fun toggleTareaCompleta(id: Long) {
        if (isDayLocked.value) return // Bloqueo de seguridad V2

        val index = tareas.indexOfFirst { it.id == id }
        if (index != -1) {
            val estadoActual = tareas[index].completada
            tareas[index] = tareas[index].copy(
                completada = !estadoActual,
                incompleta = false // Remueve la "X" si se completa
            )
            actualizarTodo()
            save()
        }
    }

    fun toggleTareaIncompleta(id: Long) {
        if (isDayLocked.value) return // Bloqueo de seguridad V2

        val index = tareas.indexOfFirst { it.id == id }
        if (index != -1) {
            val estadoX = tareas[index].incompleta
            tareas[index] = tareas[index].copy(
                incompleta = !estadoX,
                completada = false // Remueve el check si se pone "X"
            )
            actualizarTodo()
            save()
        }
    }

    fun actualizarPesoTarea(id: Long, nuevoPeso: Int) {
        val index = tareas.indexOfFirst { it.id == id }
        if (index != -1) {
            tareas[index] = tareas[index].copy(peso = nuevoPeso.coerceIn(1, 100))
            actualizarTodo()
            save()
        }
    }

    fun eliminarTarea(id: Long) {
        tareas.removeIf { it.id == id }
        actualizarTodo()
        save()
    }

    // --- AGUA (Con Bloqueo) ---
    fun gestionarVaso(incrementar: Boolean) {
        if (isDayLocked.value) return
        if (incrementar && vasosAgua.intValue < 9) {
            vasosAgua.intValue++
        } else if (!incrementar && vasosAgua.intValue > 0) {
            vasosAgua.intValue--
        }
        actualizarTodo()
        save()
    }

    // --- JEICKO (Con Bloqueo) ---
    fun toggleSalida(id: Int) {
        if (isDayLocked.value) return
        val index = salidasJeicko.indexOfFirst { it.id == id }
        if (index != -1) {
            salidasJeicko[index] = salidasJeicko[index].copy(realizada = !salidasJeicko[index].realizada)
            actualizarTodo()
            save()
        }
    }

    // --- COMPRAS ---
    fun agregarCompra(nombre: String, precio: Double) {
        if (nombre.isNotBlank()) {
            compras.add(Compra(nombre = nombre.uppercase(), precio = precio))
            actualizarPresupuesto()
            save()
        }
    }

    fun toggleCompra(id: Long) {
        val index = compras.indexOfFirst { it.id == id }
        if (index != -1) {
            compras[index] = compras[index].copy(comprada = !compras[index].comprada)
            save()
        }
    }

    fun eliminarCompra(id: Long) {
        compras.removeIf { it.id == id }
        actualizarPresupuesto()
        save()
    }

    private fun actualizarPresupuesto() {
        presupuestoTotal.value = compras.sumOf { it.precio }
    }

    fun setFrase(nueva: String) {
        fraseDiaria.value = nueva
        save()
    }

    // --- SUPERMERCADO V2 ---
    fun agregarSupermarketItem(nombre: String) {
        if (nombre.isNotBlank()) {
            supermarketItems.add(SupermarketItem(nombre = nombre.uppercase()))
            save()
        }
    }

    fun toggleSupermarketItem(id: Long) {
        val index = supermarketItems.indexOfFirst { it.id == id }
        if (index != -1) {
            supermarketItems[index] = supermarketItems[index].copy(marcado = !supermarketItems[index].marcado)
            save()
        }
    }

    fun eliminarSupermarketItem(id: Long) {
        supermarketItems.removeIf { it.id == id }
        save()
    }

    // --- NOTAS V2 ---
    fun agregarNota(contenido: String) {
        if (contenido.isNotBlank()) {
            notas.add(Nota(contenido = contenido))
            save()
        }
    }

    fun eliminarNota(id: Long) {
        notas.removeIf { it.id == id }
        save()
    }
}