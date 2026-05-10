package com.example.upad.data

import android.net.Uri
import com.example.upad.viewmodel.TaskItem
import com.google.firebase.database.FirebaseDatabase
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

    suspend fun saveRoutine(userId: String, routineName: String, tasks: List<TaskItem>) {
        database.child("routines").child(userId).child(routineName).setValue(tasks).await()
    }
}