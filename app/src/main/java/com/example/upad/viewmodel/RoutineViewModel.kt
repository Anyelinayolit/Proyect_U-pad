package com.example.upad.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.upad.data.FirebaseRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

// Data class para representar cada tarea de la rutina
data class TaskItem(
    val description: String = "",
    val imageUrl: String = "",
    val duration: Int = 5
)

class RoutineViewModel(private val repository: FirebaseRepository) : ViewModel() {

    // Estado de la rutina actual (Sobrevive a la rotación)
    private val _currentRoutineName = MutableStateFlow("")
    val currentRoutineName: StateFlow<String> = _currentRoutineName

    private val _tasks = MutableStateFlow<List<TaskItem>>(emptyList())
    val tasks: StateFlow<List<TaskItem>> = _tasks

    fun updateName(newName: String) { _currentRoutineName.value = newName }

    fun addTask(description: String, imageUrl: String) {
        val newList = _tasks.value.toMutableList()
        newList.add(TaskItem(description, imageUrl))
        _tasks.value = newList
    }

    fun uploadAndAddTask(userId: String, uri: Uri, description: String) {
        viewModelScope.launch {
            val url = repository.uploadPictogram(userId, uri)
            addTask(description, url)
        }
    }

    fun saveAll(userId: String) {
        viewModelScope.launch {
            repository.saveRoutine(userId, _currentRoutineName.value, _tasks.value)
        }
    }
}