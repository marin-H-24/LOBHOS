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

@Serializable
data class Tarea(
    val id: Long = System.currentTimeMillis(),
    val nombre: String,
    var completada: Boolean = false
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

class LobhosViewModel(application: Application) : AndroidViewModel(application) {
    private val store = LobhosStore(application)

    // Estados de la UI
    val tareas = mutableStateListOf<Tarea>()
    val compras = mutableStateListOf<Compra>()
    val salidasJeicko = mutableStateListOf<SalidaJeicko>()

    var vasosAgua = mutableIntStateOf(0)
    var fraseDiaria = mutableStateOf("“LA DISCIPLINA ES EL PUENTE ENTRE METAS Y LOGROS.”")
    var progresoGlobal = mutableIntStateOf(0)
    var presupuestoTotal = mutableStateOf(0.0)

    init {
        viewModelScope.launch {
            // 1. Verificamos si cambió el día antes de cargar nada
            store.verificarYReiniciarSiEsNuevoDia()

            // 2. Cargamos los datos desde DataStore
            store.tareasFlow.collect { if (it.isNotEmpty()) { tareas.clear(); tareas.addAll(it) }; actualizarTodo() }
        }
        viewModelScope.launch {
            store.comprasFlow.collect { if (it.isNotEmpty()) { compras.clear(); compras.addAll(it) }; actualizarPresupuesto() }
        }
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
    }

    // --- Lógica de Persistencia ---
    private fun save() {
        viewModelScope.launch {
            store.guardarTareas(tareas)
            store.guardarCompras(compras)
            store.guardarAgua(vasosAgua.intValue)
            store.guardarJeicko(salidasJeicko)
            store.guardarFrase(fraseDiaria.value)
        }
    }

    // --- Funciones de Tareas ---
    fun toggleTarea(id: Long) {
        val index = tareas.indexOfFirst { it.id == id }
        if (index != -1) {
            tareas[index] = tareas[index].copy(completada = !tareas[index].completada)
            actualizarTodo()
            save()
        }
    }

    fun agregarTarea(nombre: String) {
        if (nombre.isNotBlank()) {
            tareas.add(Tarea(nombre = nombre.uppercase()))
            actualizarTodo()
            save()
        }
    }

    fun eliminarTarea(id: Long) {
        tareas.removeIf { it.id == id }
        actualizarTodo()
        save()
    }

    // --- Lógica de Agua ---
    fun gestionarVaso(incrementar: Boolean) {
        if (incrementar && vasosAgua.intValue < 9) {
            vasosAgua.intValue++
        } else if (!incrementar && vasosAgua.intValue > 0) {
            vasosAgua.intValue--
        }
        actualizarTodo()
        save()
    }

    // --- Lógica de Jeicko ---
    fun toggleSalida(id: Int) {
        val index = salidasJeicko.indexOfFirst { it.id == id }
        if (index != -1) {
            salidasJeicko[index] = salidasJeicko[index].copy(realizada = !salidasJeicko[index].realizada)
            actualizarTodo()
            save()
        }
    }

    // --- Lógica de Compras ---
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

    // --- Cálculos ---
    private fun actualizarPresupuesto() {
        presupuestoTotal.value = compras.sumOf { it.precio }
    }

    fun actualizarTodo() {
        val totalItems = tareas.size + 9 + salidasJeicko.size
        val completados = tareas.count { it.completada } + vasosAgua.intValue + salidasJeicko.count { it.realizada }
        progresoGlobal.intValue = if (totalItems > 0) (completados * 100) / totalItems else 0
    }

    fun setFrase(nueva: String) {
        fraseDiaria.value = nueva
        save()
    }
}