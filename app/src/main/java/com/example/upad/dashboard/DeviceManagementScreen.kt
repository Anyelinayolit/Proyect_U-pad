package com.example.upad.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import com.example.upad.utils.BiometricHelper
import com.example.upad.viewmodel.RoutineViewModel // 👈 Importación agregada de forma segura
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

// Usamos un modelo consistente con los datos reales guardados en Firebase
data class DispositivoNiño(
    val id: String = "",
    val nombreDispositivo: String = "Tablet/Celular Niño",
    val kioscoActivo: Boolean = false
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeviceManagementScreen(
    routineViewModel: RoutineViewModel, // 👈 SOLUCIÓN: Agregamos el parámetro exacto que MainActivity te exige inyectar
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val activity = context as FragmentActivity

    var listaDispositivos by remember { mutableStateOf(listOf<DispositivoNiño>()) }
    var cargando by remember { mutableStateOf(true) }

    val firestore = remember { FirebaseFirestore.getInstance() }
    val idPadre = remember { FirebaseAuth.getInstance().currentUser?.uid ?: "" }

    // 📡 ESCUCHA EN ESPEJO: Lee exactamente el mismo nodo y bajo la misma condición que ConnectionScreen
    DisposableEffect(idPadre) {
        val listener = firestore.collection("dispositivos_niños")
            .whereEqualTo("padreId", idPadre)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    cargando = false
                    return@addSnapshotListener
                }

                val listaTemporal = mutableListOf<DispositivoNiño>()
                if (snapshot != null) {
                    for (dispositivo in snapshot.documents) {
                        val id = dispositivo.id
                        // Si existe un nombre guardado lo toma, de lo contrario usa tu string por defecto
                        val nombre = dispositivo.getString("nombreDispositivo")
                            ?: dispositivo.getString("modelo")
                            ?: "Tablet/Celular Niño"
                        val activo = dispositivo.getBoolean("kioscoActivo") ?: false

                        listaTemporal.add(DispositivoNiño(id, nombre, activo))
                    }
                }

                listaDispositivos = listaTemporal
                cargando = false
            }

        onDispose {
            listener.remove()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Control de Bloqueo Remoto") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Regresar")
                    }
                }
            )
        }
    ) { paddingValues ->
        if (cargando) {
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (listaDispositivos.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("No tienes dispositivos vinculados.", style = MaterialTheme.typography.bodyLarge)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Ve al menú lateral de Conexión para enlazar el equipo del niño con el código de 6 dígitos.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 32.dp),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(paddingValues).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(listaDispositivos) { dispositivo ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = CardDefaults.cardElevation(4.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(dispositivo.nombreDispositivo, style = MaterialTheme.typography.titleMedium)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = if (dispositivo.kioscoActivo) "Estado: BLOQUEADO remoto 🔒" else "Estado: LIBRE 🔓",
                                    color = if (dispositivo.kioscoActivo) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }

                            Button(
                                onClick = {
                                    val nuevoEstado = !dispositivo.kioscoActivo
                                    // Invoca la huella digital para cambiar el estado de bloqueo en la base de datos
                                    BiometricHelper.cambiarEstadoKioscoConHuella(
                                        activity = activity,
                                        idDispositivoNiño = dispositivo.id,
                                        debeBloquear = nuevoEstado
                                    )
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (dispositivo.kioscoActivo) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                                )
                            ) {
                                if (dispositivo.kioscoActivo) {
                                    Icon(Icons.Default.LockOpen, contentDescription = null)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Liberar")
                                } else {
                                    Icon(Icons.Default.Lock, contentDescription = null)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Bloquear")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}