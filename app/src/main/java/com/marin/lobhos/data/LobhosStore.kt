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

private val Context.dataStore by preferencesDataStore(name = "lobhos_prefs")

class LobhosStore(private val context: Context) {

    companion object {
        // Llaves V1
        val KEY_FECHA = stringPreferencesKey("ultima_fecha")
        val KEY_TAREAS = stringPreferencesKey("tareas_json")
        val KEY_COMPRAS = stringPreferencesKey("compras_json")
        val KEY_AGUA = intPreferencesKey("vasos_agua")
        val KEY_JEICKO = stringPreferencesKey("jeicko_json")
        val KEY_FRASE = stringPreferencesKey("frase_diaria")

        // Llaves V2
        val KEY_NOTAS = stringPreferencesKey("notas_json")
        val KEY_SUPERMARKET = stringPreferencesKey("supermarket_json")
        val KEY_DAY_LOCKED = booleanPreferencesKey("is_day_locked")
        val KEY_HISTORY = stringPreferencesKey("history_json")
    }

    // --- LÓGICA DE REINICIO DIARIO (Actualizada V2) ---
    suspend fun verificarYReiniciarSiEsNuevoDia(progresoActual: Int) {
        val sdf = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
        val hoy = sdf.format(Date())

        context.dataStore.edit { prefs ->
            val ultimaFecha = prefs[KEY_FECHA] ?: ""

            if (ultimaFecha.isNotEmpty() && ultimaFecha != hoy) {
                // 1. Guardar Historial del día anterior
                val historialJson = prefs[KEY_HISTORY] ?: "{}"
                val historial: MutableMap<String, Int> = if (historialJson.isEmpty() || historialJson == "{}") {
                    mutableMapOf()
                } else {
                    Json.decodeFromString(historialJson)
                }
                historial[ultimaFecha] = progresoActual
                prefs[KEY_HISTORY] = Json.encodeToString(historial)

                // 2. Reinicio Volátil
                prefs[KEY_AGUA] = 0
                prefs[KEY_JEICKO] = ""
                prefs[KEY_TAREAS] = ""
                prefs[KEY_SUPERMARKET] = ""
                prefs[KEY_DAY_LOCKED] = false
            }

            if (ultimaFecha != hoy) {
                prefs[KEY_FECHA] = hoy
            }
        }
    }

    // --- GUARDAR DATOS ---
    suspend fun guardarTareas(lista: List<Tarea>) = context.dataStore.edit { it[KEY_TAREAS] = Json.encodeToString(lista) }
    suspend fun guardarCompras(lista: List<Compra>) = context.dataStore.edit { it[KEY_COMPRAS] = Json.encodeToString(lista) }
    suspend fun guardarAgua(cantidad: Int) = context.dataStore.edit { it[KEY_AGUA] = cantidad }
    suspend fun guardarJeicko(lista: List<SalidaJeicko>) = context.dataStore.edit { it[KEY_JEICKO] = Json.encodeToString(lista) }
    suspend fun guardarFrase(frase: String) = context.dataStore.edit { it[KEY_FRASE] = frase }

    // Guardado V2
    suspend fun guardarNotas(lista: List<Nota>) = context.dataStore.edit { it[KEY_NOTAS] = Json.encodeToString(lista) }
    suspend fun guardarSupermarket(lista: List<SupermarketItem>) = context.dataStore.edit { it[KEY_SUPERMARKET] = Json.encodeToString(lista) }
    suspend fun setDayLocked(locked: Boolean) = context.dataStore.edit { it[KEY_DAY_LOCKED] = locked }

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

    // Lecturas V2
    val notasFlow: Flow<List<Nota>> = context.dataStore.data.map { prefs ->
        val json = prefs[KEY_NOTAS] ?: ""
        if (json.isEmpty()) emptyList() else Json.decodeFromString(json)
    }
    val supermarketFlow: Flow<List<SupermarketItem>> = context.dataStore.data.map { prefs ->
        val json = prefs[KEY_SUPERMARKET] ?: ""
        if (json.isEmpty()) emptyList() else Json.decodeFromString(json)
    }
    val dayLockedFlow: Flow<Boolean> = context.dataStore.data.map { it[KEY_DAY_LOCKED] ?: false }
    val historyFlow: Flow<Map<String, Int>> = context.dataStore.data.map { prefs ->
        val json = prefs[KEY_HISTORY] ?: "{}"
        if (json.isEmpty() || json == "{}") emptyMap() else Json.decodeFromString(json)
    }
}