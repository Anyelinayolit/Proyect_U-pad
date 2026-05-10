package com.example.upad.auth

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.upad.data.FirebaseRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

@Composable
fun RegisterScreen(
    onRegisterComplete: (String, String) -> Unit
) {
    var step by remember { mutableStateOf(1) }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    // Herramientas necesarias para Firebase y alertas
    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()
    val scope = rememberCoroutineScope()
    val repository = remember { FirebaseRepository() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (step == 1) {
            Text(
                text = "¿Cuál es tu correo electrónico?",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.Start)
            )

            Spacer(modifier = Modifier.height(32.dp))

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("EMAIL") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = { if (email.isNotBlank()) step = 2 },
                modifier = Modifier.align(Alignment.End)
            ) {
                Text("Siguiente")
            }
        } else {
            Text(
                text = "Crea una contraseña",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.Start)
            )

            Spacer(modifier = Modifier.height(32.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("CONTRASEÑA") },
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = PasswordVisualTransformation(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {
                    Log.d("UPAD_DEBUG", "Botón Hecho presionado")
                    if (email.isNotBlank() && password.isNotBlank()) {
                        auth.createUserWithEmailAndPassword(email, password)
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    Log.d("UPAD_DEBUG", "Usuario creado en Auth")
                                    val userId = task.result?.user?.uid
                                    if (userId != null) {
                                        scope.launch {
                                            try {
                                                val defaultName = email.substringBefore("@")
                                                repository.saveUserData(userId, defaultName)
                                                Log.d("UPAD_DEBUG", "Datos guardados en DB, navegando...")
                                                onRegisterComplete(email, password)
                                            } catch (e: Exception) {
                                                Log.e("UPAD_DEBUG", "Error en DB: ${e.message}")
                                                // Aun si falla la DB, navegamos para no trabar al usuario
                                                onRegisterComplete(email, password)
                                            }
                                        }
                                    }
                                } else {
                                    Log.e("UPAD_DEBUG", "Error Auth: ${task.exception?.message}")
                                    Toast.makeText(context, "Error: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                                }
                            }
                    } else {
                        Log.w("UPAD_DEBUG", "Email o Password vacíos")
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Hecho")
            }
        }
    }
}