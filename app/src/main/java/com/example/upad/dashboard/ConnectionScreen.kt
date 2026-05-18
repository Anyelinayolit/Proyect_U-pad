package com.example.upad.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

// Modelo de datos local para listar los dispositivos enlazados
data class DispositivoVinculado(val id: String, val modelo: String = "Tablet/Celular Niño")

@Composable
fun ConnectionScreen(
    onNavigateBack: () -> Unit,
    onLinkSuccess: () -> Unit
) {
    val colorAzulPadre = Color(0xFF0288D1)
    val colorFondo = Color(0xFFF5F5F5)
    val colorVerdeExito = Color(0xFF4CAF50)

    val idPadrePrueba = "PADRE_TEST"

    var codigoIngresado by remember { mutableStateOf("") }
    var cargandoVerificacion by remember { mutableStateOf(true) }
    var mensajeError by remember { mutableStateOf("") }

    // Lista dinámica para guardar todos los dispositivos enlazados a este padre
    val listaDispositivos = remember { mutableStateListOf<DispositivoVinculado>() }

    val database = remember { FirebaseDatabase.getInstance().reference }

    // --- ESCUCHAR MULTIDISPOSITIVOS EN TIEMPO REAL ---
    LaunchedEffect(idPadrePrueba) {
        database.child("dispositivos_niños")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    listaDispositivos.clear()
                    for (dispositivo in snapshot.children) {
                        val padreIdEnBD = dispositivo.child("padreId").getValue(String::class.java)
                        // Si el dispositivo le pertenece a este padre, lo metemos a la lista
                        if (padreIdEnBD == idPadrePrueba) {
                            listaDispositivos.add(
                                DispositivoVinculado(
                                    id = dispositivo.key ?: ""
                                )
                            )
                        }
                    }
                    cargandoVerificacion = false
                }

                override fun onCancelled(error: DatabaseError) {
                    cargandoVerificacion = false
                }
            })
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colorFondo),
        contentAlignment = Alignment.TopCenter
    ) {
        if (cargandoVerificacion) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = colorAzulPadre)
            }
        } else {
            LazyColumn(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // --- SECCIÓN 1: TÍTULO PRINCIPAL ---
                item {
                    Spacer(modifier = Modifier.height(20.dp))
                    Text(
                        text = "Gestión de Dispositivos",
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF1A237E),
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "Conecta nuevos niños o administra los equipos enlazados.",
                        fontSize = 14.sp,
                        color = Color.Gray,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                }

                // --- SECCIÓN 2: LISTA DE DISPOSITIVOS CONECTADOS (O RECOVERY) ---
                if (listaDispositivos.isNotEmpty()) {
                    item {
                        Text(
                            text = "EQUIPOS VINCULADOS (${listaDispositivos.size})",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Gray,
                            modifier = Modifier.fillMaxWidth().padding(top = 16.dp)
                        )
                    }

                    items(listaDispositivos) { dispositivo ->
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(
                                        text = dispositivo.modelo,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp,
                                        color = Color.DarkGray
                                    )
                                    Text(
                                        text = "ID: ${dispositivo.id.take(12)}...",
                                        fontSize = 12.sp,
                                        color = Color.LightGray
                                    )
                                }

                                // 🔥 BOTÓN SALVAVIDAS: Si se pierde el celular, lo borras de la base de datos aquí
                                IconButton(
                                    onClick = {
                                        database.child("dispositivos_niños").child(dispositivo.id).removeValue()
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Desvincular",
                                        tint = Color(0xFFE57373)
                                    )
                                }
                            }
                        }
                    }
                }

                // --- SECCIÓN 3: AGREGAR O CONECTAR DISPOSITIVO NUEVO ---
                item {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp), color = Color.LightGray.copy(alpha = 0.5f))

                    Text(
                        text = "Conectar nuevo dispositivo",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = colorAzulPadre,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = codigoIngresado,
                        onValueChange = { if (it.length <= 6) codigoIngresado = it },
                        label = { Text("Código de 6 dígitos del niño") },
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = colorAzulPadre,
                            focusedLabelColor = colorAzulPadre
                        )
                    )

                    if (mensajeError.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(text = mensajeError, color = Color.Red, fontSize = 14.sp, modifier = Modifier.fillMaxWidth())
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            if (codigoIngresado.length == 6) {
                                mensajeError = ""
                                database.child("codigos_vinculacion").child(codigoIngresado)
                                    .addListenerForSingleValueEvent(object : ValueEventListener {
                                        override fun onDataChange(snapshot: DataSnapshot) {
                                            if (snapshot.exists()) {
                                                val actualizaciones = mapOf(
                                                    "estado" to "enlazado",
                                                    "padreId" to idPadrePrueba
                                                )
                                                database.child("codigos_vinculacion").child(codigoIngresado)
                                                    .updateChildren(actualizaciones)

                                                codigoIngresado = "" // Limpia el input
                                                onLinkSuccess()
                                            } else {
                                                mensajeError = "El código no existe o expiró."
                                            }
                                        }
                                        override fun onCancelled(error: DatabaseError) {
                                            mensajeError = "Error de conexión."
                                        }
                                    })
                            } else {
                                mensajeError = "Introduce los 6 números."
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = colorVerdeExito),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth().height(55.dp)
                    ) {
                        Text("VINCULAR OTRO EQUIPO 🔗", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Botón para salir ordenadamente de la pestaña
                    TextButton(onClick = onNavigateBack) {
                        Text("Volver al Panel Principal", color = Color.Gray, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}