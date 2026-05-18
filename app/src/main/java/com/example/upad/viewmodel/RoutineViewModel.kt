package com.example.upad.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.upad.data.FirebaseRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import com.example.upad.data.ArasaacPictogram
import com.example.upad.data.ArasaacService

data class TaskItem(
    val actividad: String = "",
    val palabraClave: String = "",
    val imageUrl: String = "",
    val dias: List<String> = emptyList(),
    val duration: Int = 15,
    @field:JvmField val isCompleted: Boolean = false
)

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

    // --- CONEXIÓN RETROFIT CORREGIDA ---
    private val arasaacService = retrofit2.Retrofit.Builder()
        .baseUrl("https://api.arasaac.org/")
        .addConverterFactory(retrofit2.converter.gson.GsonConverterFactory.create())
        .build()
        .create(ArasaacService::class.java)

    fun updateName(newName: String) { _currentRoutineName.value = newName }

    // --- ESCUCHA EN TIEMPO REAL ---
    fun cargarRutinasDesdeFirebase(userId: String) {
        viewModelScope.launch {
            repository.escucharRutinasDelPadre(userId, "MAÑANA") { lista ->
                _tasksManana.value = lista
            }
            repository.escucharRutinasDelPadre(userId, "TARDE") { lista ->
                _tasksTarde.value = lista
            }
            repository.escucharRutinasDelPadre(userId, "NOCHE") { lista ->
                _tasksNoche.value = lista
            }
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

    // --- MARCAR COMPLETADO DESDE EL NIÑO USANDO EL USER ID ---
    fun completeTask(userId: String, turn: String, index: Int) {
        val turnoUpper = turn.uppercase()
        val listaActual = when (turnoUpper) {
            "MAÑANA" -> _tasksManana.value.toMutableList()
            "TARDE" -> _tasksTarde.value.toMutableList()
            else -> _tasksNoche.value.toMutableList()
        }

        if (index in listaActual.indices) {
            val tareaModificada = listaActual[index].copy(isCompleted = true)
            listaActual[index] = tareaModificada

            when (turnoUpper) {
                "MAÑANA" -> _tasksManana.value = listaActual
                "TARDE" -> _tasksTarde.value = listaActual
                else -> _tasksNoche.value = listaActual
            }

            // Guarda directo el cambio del niño
            val database = com.google.firebase.database.FirebaseDatabase
                .getInstance("https://u-pad-1f4a7-default-rtdb.firebaseio.com/")
                .reference

            database.child("routines").child(userId).child(turnoUpper)
                .setValue(listaActual)
                .addOnSuccessListener {
                    android.util.Log.d("FirebaseUpdate", "¡Progreso sincronizado en la nube!")
                }
        }
    }

    fun getCompletedCount(turn: String): Int {
        val targetList = when (turn.uppercase()) {
            "MAÑANA" -> _tasksManana.value
            "TARDE" -> _tasksTarde.value
            else -> _tasksNoche.value
        }
        return targetList.count { it.isCompleted }
    }

    fun getTotalCount(turn: String): Int {
        return when (turn.uppercase()) {
            "MAÑANA" -> _tasksManana.value.size
            "TARDE" -> _tasksTarde.value.size
            else -> _tasksNoche.value.size
        }
    }

    // --- GUARDADO GENERAL DESDE EL ADULTO ---
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

    // --- CORREGIDO: AHORA AGREGA LA TAREA Y DE UNA VEZ LA SUBE A FIREBASE RE REAL ---
    fun agregarActividadAutomatica(userId: String, turn: String, textoCompleto: String, diasSeleccionados: List<String>) {
        val turnoUpper = turn.uppercase()
        val palabraClave = textoCompleto.trim().split(" ").firstOrNull() ?: "rutina"

        viewModelScope.launch {
            var urlImagenFinal = ""
            try {
                // Corrección del endpoint de la API de ARASAAC
                val resultados = arasaacService.searchPictograms(palabraClave)
                if (resultados.isNotEmpty()) {
                    val idImagen = resultados.first()._id
                    urlImagenFinal = "https://api.arasaac.org/api/pictograms/$idImagen"
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

            val nuevaTarea = TaskItem(
                actividad = textoCompleto.uppercase(),
                palabraClave = palabraClave.uppercase(),
                imageUrl = urlImagenFinal,
                dias = diasSeleccionados
            )

            // 1. Actualizamos el estado local
            val listaNueva = when (turnoUpper) {
                "MAÑANA" -> { _tasksManana.value = _tasksManana.value + nuevaTarea; _tasksManana.value }
                "TARDE" -> { _tasksTarde.value = _tasksTarde.value + nuevaTarea; _tasksTarde.value }
                else -> { _tasksNoche.value = _tasksNoche.value + nuevaTarea; _tasksNoche.value }
            }

            // 2. 🔥 SOLUCIÓN DE RAÍZ: Empujamos los datos inmediatamente a tu Firebase oficial
            try {
                repository.saveRoutine(userId, turnoUpper, listaNueva)
                android.util.Log.d("FirebaseSave", "¡Tarea de la $turnoUpper guardada exitosamente en la base de datos!")
            } catch (e: Exception) {
                android.util.Log.e("FirebaseSave", "Error al guardar en el repositorio: ${e.message}")
            }
        }
    }
}