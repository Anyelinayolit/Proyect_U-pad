package com.example.upad.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.upad.data.FirebaseRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import com.example.upad.data.ArasaacPictogram
import com.example.upad.data.ArasaacService
import java.util.Calendar

data class TaskItem(
    val actividad: String = "",
    val palabraClave: String = "",
    val imageUrl: String = "",
    val dias: List<String> = emptyList(),
    val duration: Int = 15,
    // 🔥 SOLUCIÓN 1: Mapeo inteligente. Guarda el estado de completado por cada día de forma independiente
    // Ejemplo en Firebase: {"LUN": false, "DOM": true}
    val estadosPorDia: Map<String, Boolean> = emptyMap()
) {
    // Función ultra-inteligente para saber si está completada HOY
    fun estaCompletadaHoy(diaActual: String): Boolean {
        val diaKey = diaActual.uppercase().trim()
        return estadosPorDia[diaKey] ?: false
    }
}

class RoutineViewModel(private val repository: FirebaseRepository) : ViewModel() {

    private val _currentRoutineName = MutableStateFlow("")
    val currentRoutineName: StateFlow<String> = _currentRoutineName

    private val _tasksManana = MutableStateFlow<List<TaskItem>>(emptyList())
    val tasksManana: StateFlow<List<TaskItem>> = _tasksManana

    private val _tasksTarde = MutableStateFlow<List<TaskItem>>(emptyList())
    val tasksTarde: StateFlow<List<TaskItem>> = _tasksTarde

    private val _tasksNoche = MutableStateFlow<List<TaskItem>>(emptyList())
    val tasksNoche: StateFlow<List<TaskItem>> = _tasksNoche

    private val _searchResults = MutableStateFlow<List<ArasaacPictogram>>(emptyList())
    val searchResults: StateFlow<List<ArasaacPictogram>> = _searchResults

    private val arasaacService = retrofit2.Retrofit.Builder()
        .baseUrl("https://api.arasaac.org/")
        .addConverterFactory(retrofit2.converter.gson.GsonConverterFactory.create())
        .build()
        .create(ArasaacService::class.java)

    fun updateName(newName: String) { _currentRoutineName.value = newName }

    fun cargarRutinasDesdeFirebase(userId: String) {
        viewModelScope.launch {
            repository.escucharRutinasDelPadre(userId, "MAÑANA") { lista -> _tasksManana.value = lista }
            repository.escucharRutinasDelPadre(userId, "TARDE") { lista -> _tasksTarde.value = lista }
            repository.escucharRutinasDelPadre(userId, "NOCHE") { lista -> _tasksNoche.value = lista }
        }
    }

    fun searchArasaac(query: String) {
        viewModelScope.launch {
            try {
                if (query.length > 2) {
                    _searchResults.value = arasaacService.searchPictograms(query)
                } else {
                    _searchResults.value = emptyList()
                }
            } catch (e: Exception) {
                _searchResults.value = emptyList()
            }
        }
    }

    fun addTask(turn: String, actividadTexto: String, imageUrl: String) {
        val newTask = TaskItem(actividad = actividadTexto.uppercase(), imageUrl = imageUrl)
        when (turn.uppercase()) {
            "MAÑANA" -> _tasksManana.value = _tasksManana.value + newTask
            "TARDE" -> _tasksTarde.value = _tasksTarde.value + newTask
            "NOCHE" -> _tasksNoche.value = _tasksNoche.value + newTask
        }
    }

    fun removeTask(turn: String, index: Int) {
        when (turn.uppercase()) {
            "MAÑANA" -> {
                val newList = _tasksManana.value.toMutableList()
                if (index in newList.indices) { newList.removeAt(index); _tasksManana.value = newList }
            }
            "TARDE" -> {
                val newList = _tasksTarde.value.toMutableList()
                if (index in newList.indices) { newList.removeAt(index); _tasksTarde.value = newList }
            }
            "NOCHE" -> {
                val newList = _tasksNoche.value.toMutableList()
                if (index in newList.indices) { newList.removeAt(index); _tasksNoche.value = newList }
            }
        }
    }

    // --- 🔥 SOLUCIÓN 2: MARCAR COMPLETADO SIN ÍNDICES (POR NOMBRE Y DÍA ACTUAL PRECISO) ---
    fun completeTaskPorNombre(userId: String, turn: String, actividadTexto: String, diaActual: String) {
        val turnoUpper = turn.uppercase()
        val diaKey = diaActual.uppercase().trim()

        val listaActual = when (turnoUpper) {
            "MAÑANA" -> _tasksManana.value.toMutableList()
            "TARDE" -> _tasksTarde.value.toMutableList()
            else -> _tasksNoche.value.toMutableList()
        }

        // Buscamos de manera inequívoca por el nombre de la actividad
        val indexReal = listaActual.indexOfFirst { it.actividad.uppercase() == actividadTexto.uppercase() }

        if (indexReal != -1) {
            val tareaEncontrada = listaActual[indexReal]

            // Actualizamos el mapa de estados mutando únicamente el día de hoy
            val nuevosEstados = tareaEncontrada.estadosPorDia.toMutableMap()
            nuevosEstados[diaKey] = true

            val tareaModificada = tareaEncontrada.copy(estadosPorDia = nuevosEstados)
            listaActual[indexReal] = tareaModificada

            // Notificamos a la UI reactiva de Compose
            when (turnoUpper) {
                "MAÑANA" -> _tasksManana.value = listaActual
                "TARDE" -> _tasksTarde.value = listaActual
                else -> _tasksNoche.value = listaActual
            }

            // Guardamos directamente en tu ruta de Firebase Realtime Database
            val database = com.google.firebase.database.FirebaseDatabase
                .getInstance("https://u-pad-1f4a7-default-rtdb.firebaseio.com/")
                .reference

            database.child("routines").child(userId).child(turnoUpper)
                .setValue(listaActual)
        }
    }

    // --- CONTADORES ADAPTADOS DE FORMA INTELIGENTE POR DÍA ---
    fun getCompletedCount(turn: String, diaActual: String): Int {
        val targetList = when (turn.uppercase()) {
            "MAÑANA" -> _tasksManana.value
            "TARDE" -> _tasksTarde.value
            else -> _tasksNoche.value
        }
        return targetList.count { it.estaCompletadaHoy(diaActual) }
    }

    fun getTotalCount(turn: String, diaActual: String): Int {
        val targetList = when (turn.uppercase()) {
            "MAÑANA" -> _tasksManana.value
            "TARDE" -> _tasksTarde.value
            else -> _tasksNoche.value
        }
        return targetList.count { tarea ->
            tarea.dias.isEmpty() || tarea.dias.any { it.uppercase().trim().startsWith(diaActual.uppercase().trim()) }
        }
    }

    fun saveAll(userId: String, turn: String) {
        viewModelScope.launch {
            val listToSave = when (turn.uppercase()) {
                "MAÑANA" -> _tasksManana.value
                "TARDE" -> _tasksTarde.value
                else -> _tasksNoche.value
            }
            repository.saveRoutine(userId, turn.uppercase(), listToSave)
        }
    }

    fun agregarActividadAutomatica(userId: String, turn: String, textoCompleto: String, diasSeleccionados: List<String>) {
        val turnoUpper = turn.uppercase()
        val palabraClave = textoCompleto.trim().split(" ").firstOrNull() ?: "rutina"

        viewModelScope.launch {
            var urlImagenFinal = ""
            try {
                val resultados = arasaacService.searchPictograms(palabraClave)
                if (resultados.isNotEmpty()) {
                    val idImagen = resultados.first()._id
                    urlImagenFinal = "https://api.arasaac.org/api/pictograms/$idImagen"
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

            // Inicializamos los estados de los días seleccionados como 'false' (sin completar)
            val mapaInicialEstados = diasSeleccionados.associate { it.uppercase().trim() to false }

            val nuevaTarea = TaskItem(
                actividad = textoCompleto.uppercase(),
                palabraClave = palabraClave.uppercase(),
                imageUrl = urlImagenFinal,
                dias = diasSeleccionados.map { it.uppercase().trim() },
                estadosPorDia = mapaInicialEstados
            )

            val listaNueva = when (turnoUpper) {
                "MAÑANA" -> { _tasksManana.value = _tasksManana.value + nuevaTarea; _tasksManana.value }
                "TARDE" -> { _tasksTarde.value = _tasksTarde.value + nuevaTarea; _tasksTarde.value }
                else -> { _tasksNoche.value = _tasksNoche.value + nuevaTarea; _tasksNoche.value }
            }

            try {
                repository.saveRoutine(userId, turnoUpper, listaNueva)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}