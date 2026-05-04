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
import java.text.SimpleDateFormat
import java.util.*

@Serializable
data class Tarea(
    val id: Long = System.currentTimeMillis(),
    val nombre: String,
    var completada: Boolean = false,
    var incompleta: Boolean = false,
    var peso: Int = 1
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

    val tareas = mutableStateListOf<Tarea>()
    val compras = mutableStateListOf<Compra>()
    val salidasJeicko = mutableStateListOf<SalidaJeicko>()
    val notas = mutableStateListOf<Nota>()
    val supermarketItems = mutableStateListOf<SupermarketItem>()

    var vasosAgua = mutableIntStateOf(0)
    var fraseDiaria = mutableStateOf("“LA DISCIPLINA ES EL PUENTE ENTRE METAS Y LOGROS.”")
    var progresoGlobal = mutableIntStateOf(0)
    var presupuestoTotal = mutableStateOf(0.0)
    var isDayLocked = mutableStateOf(false)

    var rachaActual = mutableIntStateOf(0)
    var promedioSemanal = mutableIntStateOf(0)
    var historialDatos = mutableStateOf<Map<String, Int>>(emptyMap())

    init {
        viewModelScope.launch {
            store.verificarYReiniciarSiEsNuevoDia()
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
        viewModelScope.launch { store.notasFlow.collect { if (it.isNotEmpty()) { notas.clear(); notas.addAll(it) } } }
        viewModelScope.launch { store.supermarketFlow.collect { if (it.isNotEmpty()) { supermarketItems.clear(); supermarketItems.addAll(it) } } }
        viewModelScope.launch { store.dayLockedFlow.collect { isDayLocked.value = it; actualizarTodo() } }

        viewModelScope.launch {
            store.historyFlow.collect { historial ->
                historialDatos.value = historial
                calcularEstadisticas(historial)
            }
        }
    }

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

            // Asegura que la racha y el gráfico se actualicen en tiempo real
            val sdf = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
            val hoy = sdf.format(Date())
            store.guardarProgresoHistorial(hoy, progresoGlobal.intValue)
        }
    }

    private fun calcularEstadisticas(historial: Map<String, Int>) {
        if (historial.isEmpty()) return

        val datosOrdenados = historial.entries.sortedByDescending { it.key }

        var racha = 0
        for (dia in datosOrdenados) {
            if (dia.value > 0) racha++ else break
        }
        rachaActual.intValue = racha

        val ultimos7 = datosOrdenados.take(7)
        val promedio = if (ultimos7.isNotEmpty()) ultimos7.sumOf { it.value } / ultimos7.size else 0
        promedioSemanal.intValue = promedio
    }

    fun actualizarTodo() {
        if (isDayLocked.value) return

        val progresoAgua = (vasosAgua.intValue.toFloat() / 9f) * 8f
        val completadasJeicko = salidasJeicko.count { it.realizada }.toFloat()
        val progresoJeicko = (completadasJeicko / 3f) * 22f

        val progresoTareas = if (tareas.isEmpty()) {
            0f
        } else {
            val pesoTotal = tareas.sumOf { it.peso }.toFloat()
            val pesoLogrado = tareas.filter { it.completada }.sumOf { it.peso }.toFloat()
            (pesoLogrado / pesoTotal) * 70f
        }

        progresoGlobal.intValue = (progresoAgua + progresoJeicko + progresoTareas).toInt().coerceIn(0, 100)
    }

    fun completarDia() {
        isDayLocked.value = true
        save()
    }

    fun agregarTarea(nombre: String) {
        if (nombre.isNotBlank()) {
            tareas.add(Tarea(nombre = nombre.uppercase()))
            actualizarTodo()
            save()
        }
    }

    fun toggleTareaCompleta(id: Long) {
        if (isDayLocked.value) return
        val index = tareas.indexOfFirst { it.id == id }
        if (index != -1) {
            val estadoActual = tareas[index].completada
            tareas[index] = tareas[index].copy(completada = !estadoActual, incompleta = false)
            actualizarTodo()
            save()
        }
    }

    fun toggleTareaIncompleta(id: Long) {
        if (isDayLocked.value) return
        val index = tareas.indexOfFirst { it.id == id }
        if (index != -1) {
            val estadoX = tareas[index].incompleta
            tareas[index] = tareas[index].copy(incompleta = !estadoX, completada = false)
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

    fun toggleSalida(id: Int) {
        if (isDayLocked.value) return
        val index = salidasJeicko.indexOfFirst { it.id == id }
        if (index != -1) {
            salidasJeicko[index] = salidasJeicko[index].copy(realizada = !salidasJeicko[index].realizada)
            actualizarTodo()
            save()
        }
    }

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