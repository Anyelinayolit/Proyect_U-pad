package com.example.upad.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.upad.data.FirebaseRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import com.example.upad.data.ArasaacPictogram
import com.example.upad.data.ArasaacService

// Data class para representar cada tarea de la rutina
data class TaskItem(
    val description: String = "",
    val imageUrl: String = "",
    val duration: Int = 5
)

class RoutineViewModel(private val repository: FirebaseRepository) : ViewModel() {

    // --- ESTADOS DE LA RUTINA ---
    private val _currentRoutineName = MutableStateFlow("")
    val currentRoutineName: StateFlow<String> = _currentRoutineName

    private val _tasks = MutableStateFlow<List<TaskItem>>(emptyList())
    val tasks: StateFlow<List<TaskItem>> = _tasks

    // --- NUEVO: ESTADOS PARA ARASAAC ---
    private val _searchResults = MutableStateFlow<List<ArasaacPictogram>>(emptyList())
    val searchResults: StateFlow<List<ArasaacPictogram>> = _searchResults

    // Configuración rápida de Retrofit dentro del ViewModel
    private val arasaacService = retrofit2.Retrofit.Builder()
        .baseUrl("https://api.arasaac.org/")
        .addConverterFactory(retrofit2.converter.gson.GsonConverterFactory.create())
        .build()
        .create(ArasaacService::class.java)

    fun updateName(newName: String) { _currentRoutineName.value = newName }

    // --- NUEVO: BUSCAR EN ARASAAC ---
    fun searchArasaac(query: String) {
        viewModelScope.launch {
            try {
                if (query.length > 2) {
                    val response = arasaacService.searchPictograms(query)
                    _searchResults.value = response
                } else {
                    _searchResults.value = emptyList()
                }
            } catch (e: Exception) {
                _searchResults.value = emptyList()
            }
        }
    }

    fun addTask(description: String, imageUrl: String) {
        val newList = _tasks.value.toMutableList()
        newList.add(TaskItem(description, imageUrl))
        _tasks.value = newList
    }

    fun saveAll(userId: String) {
        viewModelScope.launch {
            // Aquí es donde realmente se conecta con el Firebase de tu compañera
            repository.saveRoutine(userId, _currentRoutineName.value, _tasks.value)
        }
    }
}