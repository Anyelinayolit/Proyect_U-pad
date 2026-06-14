package com.example.upad.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.upad.data.FirebaseRepository
import com.example.upad.data.DataStoreManager
import com.google.firebase.FirebaseApp

class RoutineViewModelFactory(private val repository: FirebaseRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RoutineViewModel::class.java)) {
            // 💡 Obtenemos el contexto global de la app para inicializar el DataStore de forma segura usando FirebaseApp
            val context = FirebaseApp.getInstance().applicationContext
            val dataStoreManager = DataStoreManager(context)

            @Suppress("UNCHECKED_CAST")
            // ✨ Pasamos ambos parámetros al constructor para solucionar el error en rojo
            return RoutineViewModel(repository, dataStoreManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}