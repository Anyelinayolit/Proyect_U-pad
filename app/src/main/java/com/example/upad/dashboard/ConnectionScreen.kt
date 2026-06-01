package com.example.upad.dashboard

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.PhonelinkRing
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

data class DispositivoVinculado(val id: String, val modelo: String = "Dispositivo del Niño")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConnectionScreen(
    onNavigateBack: () -> Unit,
    onLinkSuccess: () -> Unit
) {
    // 🔄 CONEXIÓN GLOBAL: Extraemos la paleta reactiva de Material 3 exactamente como en tu Perfil
    val colorAcabadoPrincipal = MaterialTheme.colorScheme.primary
    val colorFondoBase = MaterialTheme.colorScheme.background
    val colorSuperficieTarjetas = MaterialTheme.colorScheme.surface
    val colorTextoPrincipal = MaterialTheme.colorScheme.onBackground
    val colorTextoSecundario = MaterialTheme.colorScheme.onSurface

    // Colores de estado fijos que combinan bien en ambos modos
    val colorVerdePremium = Color(0xFF2E7D32)
    val colorRojoSuave = Color(0xFFEF5350)

    val idPadrePrueba = "PADRE_TEST"
    var codigoIngresado by remember { mutableStateOf("") }
    var cargandoVerificacion by remember { mutableStateOf(true) }
    var mensajeError by remember { mutableStateOf("") }

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
                        if (padreIdEnBD == idPadrePrueba) {
                            listaDispositivos.add(DispositivoVinculado(id = dispositivo.key ?: ""))
                        }
                    }
                    cargandoVerificacion = false
                }
                override fun onCancelled(error: DatabaseError) {
                    cargandoVerificacion = false
                }
            })
    }

    Scaffold(
        containerColor = colorFondoBase,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Dispositivos",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = colorTextoPrincipal
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Atrás", tint = colorAcabadoPrincipal)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = colorSuperficieTarjetas // Se acopla a la superficie de la app
                )
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (cargandoVerificacion) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = colorAcabadoPrincipal)
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 24.dp),
                    contentPadding = PaddingValues(bottom = 32.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    // --- SECCIÓN 1: ENCABEZADO ILUSTRATIVO ---
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(72.dp)
                                    .background(
                                        brush = Brush.linearGradient(
                                            colors = listOf(colorAcabadoPrincipal.copy(alpha = 0.2f), colorAcabadoPrincipal.copy(alpha = 0.05f))
                                        ),
                                        shape = RoundedCornerShape(24.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Devices,
                                    contentDescription = null,
                                    tint = colorAcabadoPrincipal,
                                    modifier = Modifier.size(36.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Gestión de Equipos",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Black,
                                color = colorTextoPrincipal,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Vincula la tablet o celular de tu hijo para sincronizar sus rutinas visuales en tiempo real.",
                                fontSize = 14.sp,
                                color = colorTextoSecundario,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 12.dp)
                            )
                        }
                    }

                    // --- SECCIÓN 2: FORMULARIO DE VINCULACIÓN ---
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(24.dp),
                            colors = CardDefaults.cardColors(containerColor = colorSuperficieTarjetas),
                            border = BorderStroke(1.dp, colorTextoSecundario.copy(alpha = 0.12f))
                        ) {
                            Column(modifier = Modifier.padding(20.dp)) {
                                Text(
                                    text = "CONECTAR NUEVO DISPOSITIVO",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = colorAcabadoPrincipal,
                                    letterSpacing = 1.sp
                                )
                                Spacer(modifier = Modifier.height(14.dp))

                                OutlinedTextField(
                                    value = codigoIngresado,
                                    onValueChange = { if (it.length <= 6) codigoIngresado = it },
                                    label = { Text("Código de 6 dígitos del niño") },
                                    singleLine = true,
                                    shape = RoundedCornerShape(14.dp),
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = colorAcabadoPrincipal,
                                        focusedLabelColor = colorAcabadoPrincipal,
                                        unfocusedBorderColor = colorTextoSecundario.copy(alpha = 0.2f),
                                        focusedTextColor = colorTextoPrincipal,
                                        unfocusedTextColor = colorTextoPrincipal,
                                        unfocusedLabelColor = colorTextoSecundario
                                    )
                                )

                                if (mensajeError.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = mensajeError,
                                        color = colorRojoSuave,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium
                                    )
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

                                                            codigoIngresado = ""
                                                            onLinkSuccess()
                                                        } else {
                                                            mensajeError = "El código no existe o ha expirado."
                                                        }
                                                    }
                                                    override fun onCancelled(error: DatabaseError) {
                                                        mensajeError = "Error de red en la vinculación."
                                                    }
                                                })
                                        } else {
                                            mensajeError = "Por favor, introduce los 6 números."
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = colorVerdePremium),
                                    shape = RoundedCornerShape(14.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(50.dp)
                                ) {
                                    Text("VINCULAR DISPOSITIVO", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                }
                            }
                        }
                    }

                    // --- SECCIÓN 3: LISTA DE DISPOSITIVOS VINCULADOS ---
                    item {
                        Text(
                            text = "EQUIPOS ENLAZADOS (${listaDispositivos.size})",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            color = colorTextoSecundario,
                            letterSpacing = 1.sp,
                            modifier = Modifier.padding(start = 4.dp, top = 8.dp)
                        )
                    }

                    if (listaDispositivos.isEmpty()) {
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = colorSuperficieTarjetas.copy(alpha = 0.5f)),
                                border = BorderStroke(1.dp, colorTextoSecundario.copy(alpha = 0.1f))
                            ) {
                                Text(
                                    text = "No hay dispositivos vinculados todavía.",
                                    fontSize = 14.sp,
                                    color = colorTextoSecundario,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(24.dp)
                                )
                            }
                        }
                    } else {
                        items(listaDispositivos) { dispositivo ->
                            Card(
                                shape = RoundedCornerShape(18.dp),
                                colors = CardDefaults.cardColors(containerColor = colorSuperficieTarjetas),
                                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                                border = BorderStroke(1.dp, colorTextoSecundario.copy(alpha = 0.12f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(44.dp)
                                            .background(colorAcabadoPrincipal.copy(alpha = 0.12f), RoundedCornerShape(12.dp)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            Icons.Default.PhonelinkRing,
                                            contentDescription = null,
                                            tint = colorAcabadoPrincipal,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(14.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = dispositivo.modelo,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp,
                                            color = colorTextoPrincipal
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = "ID: ${dispositivo.id.take(8).uppercase()}...",
                                            fontSize = 12.sp,
                                            color = colorTextoSecundario
                                        )
                                    }

                                    IconButton(
                                        onClick = {
                                            database.child("dispositivos_niños").child(dispositivo.id).removeValue()
                                        }
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Desvincular",
                                            tint = colorRojoSuave
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}