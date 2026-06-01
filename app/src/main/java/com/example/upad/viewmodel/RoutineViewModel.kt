package com.example.upad.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.upad.data.FirebaseRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import com.example.upad.data.ArasaacPictogram
import com.example.upad.data.ArasaacService
import com.example.upad.data.DataStoreManager
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.collectLatest
import java.util.Calendar
import kotlinx.coroutines.flow.asStateFlow

data class TaskItem(
    val actividad: String = "",
    val palabraClave: String = "",
    val imageUrl: String = "",
    val dias: List<String> = emptyList(),
    val duration: Int = 15,
    val estadosPorDia: Map<String, Boolean> = emptyMap()
) {
    fun estaCompletadaHoy(diaActual: String): Boolean {
        val diaKey = diaActual.uppercase().trim()
        return estadosPorDia[diaKey] ?: false
    }
}

class RoutineViewModel(
    private val repository: FirebaseRepository,
    private val dataStoreManager: DataStoreManager
) : ViewModel() {

    private val _isPremiumManual = MutableStateFlow(false)
    val isUserPremium: StateFlow<Boolean> = _isPremiumManual

    init {
        viewModelScope.launch {
            dataStoreManager.isPremiumFlow.collectLatest { estadoReal ->
                _isPremiumManual.value = estadoReal
            }
        }
    }

    // 🔥 Cambiar de plan sincronizando localmente (DataStore) y en la nube (Firebase Realtime Database)
    fun setSuscripcionManual(activarPremium: Boolean, userId: String? = null) {
        viewModelScope.launch {
            dataStoreManager.setPremiumStatus(activarPremium)
            _isPremiumManual.value = activarPremium

            if (!userId.isNullOrEmpty()) {
                try {
                    com.google.firebase.database.FirebaseDatabase
                        .getInstance("https://u-pad-1f4a7-default-rtdb.firebaseio.com/")
                        .reference
                        .child("usuarios")
                        .child(userId)
                        .child("isPremium")
                        .setValue(activarPremium)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    fun purchasePremium(userId: String? = null) { setSuscripcionManual(true, userId) }
    fun cancelPremium(userId: String? = null) { setSuscripcionManual(false, userId) }

    private val _isDarkMode = MutableStateFlow(false)
    val isDarkMode: StateFlow<Boolean> = _isDarkMode.asStateFlow()

    fun setDarkMode(enabled: Boolean) { _isDarkMode.value = enabled }

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

    fun addTask(turn: String, actividadTexto: String, imageUrl: String, userId: String = "PADRE_TEST") {
        val newTask = TaskItem(
            actividad = actividadTexto.uppercase(),
            imageUrl = imageUrl,
            dias = listOf("Lun", "Mar", "Mié", "Jue", "Vie", "Sáb", "Dom")
        )
        val turnoUpper = turn.uppercase()

        when (turnoUpper) {
            "MAÑANA" -> _tasksManana.value = _tasksManana.value + newTask
            "TARDE" -> _tasksTarde.value = _tasksTarde.value + newTask
            "NOCHE" -> _tasksNoche.value = _tasksNoche.value + newTask
        }

        saveAll(userId, turnoUpper)
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

    fun completeTaskPorNombre(userId: String, turn: String, actividadTexto: String, diaActual: String) {
        val turnoUpper = turn.uppercase()
        val diaKey = diaActual.uppercase().trim()

        val listaActual = when (turnoUpper) {
            "MAÑANA" -> _tasksManana.value.toMutableList()
            "TARDE" -> _tasksTarde.value.toMutableList()
            else -> _tasksNoche.value.toMutableList()
        }

        val indexReal = listaActual.indexOfFirst { it.actividad.uppercase() == actividadTexto.uppercase() }

        if (indexReal != -1) {
            val tareaEncontrada = listaActual[indexReal]
            val nuevosEstados = tareaEncontrada.estadosPorDia.toMutableMap()
            nuevosEstados[diaKey] = true

            val tareaModificada = tareaEncontrada.copy(estadosPorDia = nuevosEstados)
            listaActual[indexReal] = tareaModificada

            when (turnoUpper) {
                "MAÑANA" -> _tasksManana.value = listaActual
                "TARDE" -> _tasksTarde.value = listaActual
                else -> _tasksNoche.value = listaActual
            }

            val database = com.google.firebase.database.FirebaseDatabase
                .getInstance("https://u-pad-1f4a7-default-rtdb.firebaseio.com/")
                .reference

            database.child("routines").child(userId).child(turnoUpper)
                .setValue(listaActual)
        }
    }

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

            val mapaInicialEstados = diasSeleccionados.associate { it.uppercase().trim() to false }

            val nuevaTarea = TaskItem(
                actividad = textoCompleto.uppercase(),
                palabraClave = palabraClave.uppercase(),
                imageUrl = urlImagenFinal,
                dias = diasSeleccionados.map { it.trim() },
                estadosPorDia = mapaInicialEstados
            )

            when (turnoUpper) {
                "MAÑANA" -> _tasksManana.value = _tasksManana.value + nuevaTarea
                "TARDE" -> _tasksTarde.value = _tasksTarde.value + nuevaTarea
                else -> _tasksNoche.value = _tasksNoche.value + nuevaTarea
            }

            try {
                val listaActualizada = when (turnoUpper) {
                    "MAÑANA" -> _tasksManana.value
                    "TARDE" -> _tasksTarde.value
                    else -> _tasksNoche.value
                }
                repository.saveRoutine(userId, turnoUpper, listaActualizada)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}