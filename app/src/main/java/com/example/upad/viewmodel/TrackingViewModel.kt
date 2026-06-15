package com.example.upad.viewmodel

import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

// ✅ Anotación para ignorar campos como 'ultimaActualizacion' si Firebase los maneja como Timestamp
@IgnoreExtraProperties
data class UbicacionHijo(
    val latitud: Double = 0.0,
    val longitud: Double = 0.0
)

class TrackingViewModel : ViewModel() {

    private val firestore = FirebaseFirestore.getInstance()
    private var ubicacionListener: ListenerRegistration? = null

    private val _ubicacion = MutableStateFlow<UbicacionHijo?>(null)
    val ubicacion: StateFlow<UbicacionHijo?> = _ubicacion

    // Escucha en tiempo real la ubicación del niño desde la colección correcta
    fun iniciarRastreoHijo(hijoId: String) {
        // Cancelamos un listener previo si existía para evitar duplicados o fugas de memoria
        ubicacionListener?.remove()

        // ✅ Apuntando perfectamente a "dispositivos_niños"
        ubicacionListener = firestore.collection("dispositivos_niños")
            .document(hijoId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    error.printStackTrace()
                    return@addSnapshotListener
                }

                if (snapshot != null && snapshot.exists()) {
                    // Mapea automáticamente el documento al objeto Kotlin seguro
                    val data = snapshot.toObject(UbicacionHijo::class.java)
                    _ubicacion.value = data
                }
            }
    }

    // Limpieza al destruir el ciclo de vida de la pantalla
    override fun onCleared() {
        super.onCleared()
        ubicacionListener?.remove()
    }
}