package com.marin.lobhos.data

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.marin.lobhos.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.text.SimpleDateFormat
import java.util.*

// Extensión para crear el DataStore
private val Context.dataStore by preferencesDataStore(name = "lobhos_prefs")

class LobhosStore(private val context: Context) {

    companion object {
        // Llaves de almacenamiento
        val KEY_FECHA = stringPreferencesKey("ultima_fecha")
        val KEY_TAREAS = stringPreferencesKey("tareas_json")
        val KEY_COMPRAS = stringPreferencesKey("compras_json")
        val KEY_AGUA = intPreferencesKey("vasos_agua")
        val KEY_JEICKO = stringPreferencesKey("jeicko_json")
        val KEY_FRASE = stringPreferencesKey("frase_diaria")
    }

    // --- LÓGICA DE REINICIO DIARIO ---
    suspend fun verificarYReiniciarSiEsNuevoDia() {
        val sdf = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
        val hoy = sdf.format(Date())

        context.dataStore.edit { prefs ->
            val ultimaFecha = prefs[KEY_FECHA] ?: ""

            if (ultimaFecha != hoy) {
                // Es un nuevo día: Reiniciamos lo volátil
                prefs[KEY_AGUA] = 0
                prefs[KEY_JEICKO] = "" // Se regenerará en el ViewModel
                prefs[KEY_TAREAS] = "" // Opcional: vaciar o desmarcar
                // NOTA: No tocamos KEY_COMPRAS porque es permanente

                // Actualizamos la fecha a hoy
                prefs[KEY_FECHA] = hoy
            }
        }
    }

    // --- GUARDAR DATOS ---
    suspend fun guardarTareas(lista: List<Tarea>) {
        context.dataStore.edit { it[KEY_TAREAS] = Json.encodeToString(lista) }
    }

    suspend fun guardarCompras(lista: List<Compra>) {
        context.dataStore.edit { it[KEY_COMPRAS] = Json.encodeToString(lista) }
    }

    suspend fun guardarAgua(cantidad: Int) {
        context.dataStore.edit { it[KEY_AGUA] = cantidad }
    }

    suspend fun guardarJeicko(lista: List<SalidaJeicko>) {
        context.dataStore.edit { it[KEY_JEICKO] = Json.encodeToString(lista) }
    }

    suspend fun guardarFrase(frase: String) {
        context.dataStore.edit { it[KEY_FRASE] = frase }
    }

    // --- LEER DATOS ---
    val tareasFlow: Flow<List<Tarea>> = context.dataStore.data.map { prefs ->
        val json = prefs[KEY_TAREAS] ?: ""
        if (json.isEmpty()) emptyList() else Json.decodeFromString(json)
    }

    val comprasFlow: Flow<List<Compra>> = context.dataStore.data.map { prefs ->
        val json = prefs[KEY_COMPRAS] ?: ""
        if (json.isEmpty()) emptyList() else Json.decodeFromString(json)
    }

    val aguaFlow: Flow<Int> = context.dataStore.data.map { it[KEY_AGUA] ?: 0 }

    val jeickoFlow: Flow<List<SalidaJeicko>> = context.dataStore.data.map { prefs ->
        val json = prefs[KEY_JEICKO] ?: ""
        if (json.isEmpty()) emptyList() else Json.decodeFromString(json)
    }

    val fraseFlow: Flow<String> = context.dataStore.data.map {
        it[KEY_FRASE] ?: "“LA DISCIPLINA ES EL PUENTE ENTRE METAS Y LOGROS.”"
    }
}