package com.example.appsolicitudes_grupo12.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.appsolicitudes_grupo12.viewModel.LoginViewModel

// Pantalla de inicio de sesión.
// Solo MUESTRA datos y ENVÍA eventos al ViewModel: no valida ni revisa credenciales por su cuenta.
// onLoginExitoso: lo que debe pasar cuando el ingreso salió bien. Recibe el nombre
// de la persona (la navegación lo usa para ir al menú).
@Composable
fun LoginScreen(
    viewModel: LoginViewModel = viewModel(),
    onLoginExitoso: (String) -> Unit = {}
) {
    // La pantalla observa el estado: cada cambio redibuja lo necesario
    val estado by viewModel.estado.collectAsState()

    // Se ejecuta cuando el ViewModel guarda un nombre autenticado (login correcto):
    // avisa a la navegación para pasar al menú
    LaunchedEffect(estado.nombreAutenticado) {
        estado.nombreAutenticado?.let { nombre -> onLoginExitoso(nombre) }
    }

    Scaffold { paddingInterno ->

        // Box centra el contenido en la pantalla
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingInterno)
                .padding(24.dp)
                .imePadding(),
            contentAlignment = Alignment.Center
        ) {

            // Column con scroll: si el teclado ocupa espacio, se puede deslizar
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                // ---------- Encabezado ----------
                Text(
                    text = "Gestión de solicitudes",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "Inicia sesión para continuar",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(8.dp))

                // ---------- Usuario ----------
                OutlinedTextField(
                    value = estado.nombreUsuario,                    // lee del ViewModel
                    onValueChange = viewModel::onNombreUsuarioChange, // escribe en el ViewModel
                    label = { Text("Usuario") },
                    singleLine = true,
                    isError = estado.errorNombreUsuario != null,     // borde rojo si hay error
                    supportingText = {
                        estado.errorNombreUsuario?.let { Text(it) }
                    },
                    // Next: el botón del teclado pasa al siguiente campo
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    modifier = Modifier.fillMaxWidth()
                )

                // ---------- Clave ----------
                OutlinedTextField(
                    value = estado.contrasena,
                    onValueChange = viewModel::onContrasenaChange,
                    label = { Text("Clave") },
                    singleLine = true,
                    // Muestra puntitos en vez de las letras
                    visualTransformation = PasswordVisualTransformation(),
                    isError = estado.errorContrasena != null,
                    supportingText = {
                        estado.errorContrasena?.let { Text(it) }
                    },
                    // Done: el botón del teclado ejecuta el ingreso, igual que "Ingresar"
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = { viewModel.iniciarSesion() }
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                // ---------- Error general del ingreso ----------
                // Solo aparece si las credenciales no coinciden o falló la base de datos
                estado.errorLogin?.let { mensaje ->
                    Text(
                        text = mensaje,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center
                    )
                }

                // ---------- Botón Ingresar ----------
                Button(
                    onClick = { viewModel.iniciarSesion() },
                    enabled = !estado.cargando,       // se desactiva mientras revisa
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (estado.cargando) {
                        // Estado Loading: indicador de carga dentro del botón
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Ingresando...")
                    } else {
                        Text("Ingresar")
                    }
                }

                // Ayuda para la demostración. Son datos ficticios.
                Text(
                    text = "Cuenta de prueba: demo / 1234",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}