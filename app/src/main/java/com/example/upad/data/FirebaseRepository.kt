package com.example.upad.data

import android.net.Uri
import com.example.upad.viewmodel.TaskItem
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await

class FirebaseRepository {
    private val database = FirebaseDatabase.getInstance("https://u-pad-1f4a7-default-rtdb.firebaseio.com/").reference
    private val storage = FirebaseStorage.getInstance().reference

    suspend fun saveUserData(userId: String, name: String) {
        database.child("users").child(userId).child("name").setValue(name).await()
    }

    suspend fun uploadPictogram(userId: String, imageUri: Uri): String {
        val ref = storage.child("pictograms/$userId/${System.currentTimeMillis()}.jpg")
        ref.putFile(imageUri).await()
        return ref.downloadUrl.await().toString()
    }

    // --- GUARDADO CORREGIDO ---
    // Guardamos en: routines -> userId -> turn (Ej: routines/PADRE_TEST/MAÑANA)
    suspend fun saveRoutine(userId: String, turn: String, tasks: List<TaskItem>) {
        database.child("routines")
            .child(userId)
            .child(turn.uppercase()) // Asegura que se guarde en MAYÚSCULAS limpias
            .setValue(tasks)
            .await()
    }

    // --- ESCUCHA EN TIEMPO REAL CORREGIDA ---
    // Escucha EXACTAMENTE en la misma ruta donde se guarda: routines -> padreId -> turn
    fun escucharRutinasDelPadre(padreId: String, turn: String, onDataChanged: (List<TaskItem>) -> Unit) {
        database.child("routines")
            .child(padreId)
            .child(turn.uppercase()) // Buscamos en "MAÑANA", "TARDE" o "NOCHE"
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val tareas = mutableListOf<TaskItem>()

                    // Recorremos los hijos para transformar los datos de Firebase al nuevo TaskItem
                    for (child in snapshot.children) {
                        val tarea = child.getValue(TaskItem::class.java)
                        if (tarea != null) {
                            tareas.add(tarea)
                        }
                    }
                    // Le enviamos la lista real con "actividad" al ViewModel
                    onDataChanged(tareas)
                }

                override fun onCancelled(error: DatabaseError) {
                    // Manejo de errores opcional
                }
            })
    }
}