package com.marin.lobhos.model

import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel

data class Tarea(
    val id: Long = System.currentTimeMillis(),
    val nombre: String,
    var completada: Boolean = false
)

data class Compra(
    val id: Long = System.currentTimeMillis(),
    val nombre: String,
    val precio: Double,
    var comprada: Boolean = false
)

data class SalidaJeicko(
    val id: Int,
    val etiqueta: String,
    var realizada: Boolean = false
)

class LobhosViewModel : ViewModel() {
    // Listas dinámicas
    val tareas = mutableStateListOf<Tarea>()
    val compras = mutableStateListOf<Compra>()

    // Estados de Salud y Perro
    var vasosAgua = mutableIntStateOf(0)
    val salidasJeicko = mutableStateListOf(
        SalidaJeicko(1, "SALIDA I"),
        SalidaJeicko(2, "SALIDA II"),
        SalidaJeicko(3, "SALIDA III")
    )

    // Widgets y Progreso
    var fraseDiaria = mutableStateOf("“LA DISCIPLINA ES EL PUENTE ENTRE METAS Y LOGROS.”")
    var progresoGlobal = mutableIntStateOf(0)
    var presupuestoTotal = mutableStateOf(0.0)

    init {
        // Carga inicial de tareas ejemplo
        listOf("DESPERTAR", "CORRER", "ASEOS", "CITA", "TAREAS", "COMER", "PROYECTO").forEach {
            tareas.add(Tarea(nombre = it))
        }
        actualizarTodo()
    }

    // --- Lógica de Tareas ---
    fun toggleTarea(id: Long) {
        val index = tareas.indexOfFirst { it.id == id }
        if (index != -1) {
            tareas[index] = tareas[index].copy(completada = !tareas[index].completada)
            actualizarTodo()
        }
    }

    fun agregarTarea(nombre: String) {
        if (nombre.isNotBlank()) {
            tareas.add(Tarea(nombre = nombre.uppercase()))
            actualizarTodo()
        }
    }

    fun eliminarTarea(id: Long) {
        tareas.removeIf { it.id == id }
        actualizarTodo()
    }

    // --- Lógica de Agua ---
    fun gestionarVaso(incrementar: Boolean) {
        if (incrementar && vasosAgua.intValue < 9) {
            vasosAgua.intValue++
        } else if (!incrementar && vasosAgua.intValue > 0) {
            vasosAgua.intValue--
        }
        actualizarTodo()
    }

    // --- Lógica de Jeicko ---
    fun toggleSalida(id: Int) {
        val index = salidasJeicko.indexOfFirst { it.id == id }
        if (index != -1) {
            salidasJeicko[index] = salidasJeicko[index].copy(realizada = !salidasJeicko[index].realizada)
            actualizarTodo()
        }
    }

    // --- Lógica de Compras ---
    fun agregarCompra(nombre: String, precio: Double) {
        if (nombre.isNotBlank()) {
            compras.add(Compra(nombre = nombre.uppercase(), precio = precio))
            actualizarPresupuesto()
        }
    }

    fun toggleCompra(id: Long) {
        val index = compras.indexOfFirst { it.id == id }
        if (index != -1) {
            compras[index] = compras[index].copy(comprada = !compras[index].comprada)
        }
    }

    fun eliminarCompra(id: Long) {
        compras.removeIf { it.id == id }
        actualizarPresupuesto()
    }

    // --- Cálculos de Sistema ---
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
    }
}