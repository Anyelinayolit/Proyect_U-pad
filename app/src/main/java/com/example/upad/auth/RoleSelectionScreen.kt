package com.example.upad.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.upad.R
import com.example.upad.utils.BiometricHelper
import com.google.firebase.auth.FirebaseAuth

@Composable
fun RoleSelectionScreen(
    onRoleSelected: (String) -> Unit
) {
    val colorAzulTEA = Color(0xFF4FC3F7)
    val colorFondoBase = Color(0xFFF0F4F8)

    // --- ELEMENTOS CONTEXTUALES PARA BIOMETRÍA Y SEGURIDAD ---
    val context = LocalContext.current
    val activity = context as? androidx.fragment.app.FragmentActivity
    val usuarioFirebase = remember { FirebaseAuth.getInstance().currentUser }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colorFondoBase)
    ) {
        // --- CABECERA SUPERIOR ---
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(bottomStart = 45.dp, bottomEnd = 45.dp))
                .background(Color.White)
                .padding(top = 60.dp, bottom = 40.dp, start = 24.dp, end = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "BIENVENIDO A U-PAD",
                fontSize = 28.sp,
                fontWeight = FontWeight.Black,
                color = colorAzulTEA,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "¿Quién usará el dispositivo hoy?",
                fontSize = 18.sp,
                color = Color.Gray,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center
            )
        }

        // --- CUERPO DE SELECCIÓN ---
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Opción Padre / Tutor con Validación de Huella Digital Interna
                RoleOptionCard(
                    modifier = Modifier.weight(1f),
                    title = "SOY PADRE\nO TUTOR",
                    imageRes = R.drawable.tutor,
                    colorTheme = Color(0xFF9575CD),
                    onClick = {
                        if (usuarioFirebase != null) {
                            // Si ya inició sesión antes y el dispositivo soporta biometría
                            if (activity != null && BiometricHelper.esBiometriaDisponible(context)) {
                                BiometricHelper.lanzarLectorHuella(
                                    activity = activity,
                                    onSuccess = {
                                        // 🔓 Huella correcta: viaja al Dashboard directamente
                                        onRoleSelected("padre_directo")
                                    },
                                    onError = { error ->
                                        // Cancelado o erróneo: lo enviamos al login tradicional
                                        android.util.Log.d("Biometria", "Fallo o cancelación: $error")
                                        onRoleSelected("padre")
                                    }
                                )
                            } else {
                                // Logueado pero sin hardware de huella: pasa directo al Dashboard
                                onRoleSelected("padre_directo")
                            }
                        } else {
                            // 🚪 Primera vez: directo al login ordinario
                            onRoleSelected("padre")
                        }
                    }
                )

                // Opción Menor
                RoleOptionCard(
                    modifier = Modifier.weight(1f),
                    title = "SOY EL\nMENOR",
                    imageRes = R.drawable.menor,
                    colorTheme = colorAzulTEA,
                    onClick = { onRoleSelected("menor") }
                )
            }
        }

        // --- DECORACIÓN INFERIOR ---
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
                .clip(RoundedCornerShape(topStart = 45.dp, topEnd = 45.dp))
                .background(Color.White)
        )
    }
}

@Composable
fun RoleOptionCard(
    modifier: Modifier = Modifier,
    title: String,
    imageRes: Int,
    colorTheme: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .aspectRatio(0.8f)
            .clickable { onClick() },
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp)),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = imageRes),
                    contentDescription = title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Black,
                color = colorTheme,
                textAlign = TextAlign.Center,
                lineHeight = 18.sp
            )
        }
    }
}