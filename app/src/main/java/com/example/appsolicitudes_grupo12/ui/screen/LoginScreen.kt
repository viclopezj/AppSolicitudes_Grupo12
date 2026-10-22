package com.example.appsolicitudes_grupo12.ui.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.appsolicitudes_grupo12.utils.AppWindowSizeClass
import com.example.appsolicitudes_grupo12.utils.adaptiveContentWidth
import com.example.appsolicitudes_grupo12.viewModel.LoginViewModel

@Composable
fun LoginScreen(
    viewModel: LoginViewModel = viewModel(),
    windowSizeClass: AppWindowSizeClass = AppWindowSizeClass.Compact,
    onLoginExitoso: (String, Int, String) -> Unit = { _, _, _ -> }
) {
    val estado by viewModel.estado.collectAsState()

    // Si la clave se ve o está oculta (sobrevive a giros de pantalla)
    var claveVisible by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(estado.nombreAutenticado, estado.usuarioIdAutenticado, estado.rolAutenticado) {
        val nombre = estado.nombreAutenticado
        val usuarioId = estado.usuarioIdAutenticado
        val rol = estado.rolAutenticado
        if (nombre != null && usuarioId != null && rol != null) {
            onLoginExitoso(nombre, usuarioId, rol)
        }
    }

    Scaffold(containerColor = MaterialTheme.colorScheme.background) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .imePadding()
                .padding(horizontal = 24.dp, vertical = 20.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    // Limita el ancho en ventanas grandes para mantener el formulario legible.
                    .adaptiveContentWidth(windowSizeClass, 520.dp, 620.dp, 680.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Surface(
                    modifier = Modifier.size(58.dp),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "AS",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }

                Text(
                    text = "Solicitudes",
                    style = MaterialTheme.typography.displaySmall,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "Inicia sesión para continuar",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.large
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(22.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        OutlinedTextField(
                            value = estado.nombreUsuario,
                            onValueChange = viewModel::onNombreUsuarioChange,
                            label = { Text("Usuario") },
                            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                            trailingIcon = { if (estado.errorNombreUsuario != null) IconoError() },
                            singleLine = true,
                            isError = estado.errorNombreUsuario != null,
                            supportingText = {
                                estado.errorNombreUsuario?.let { Text(it) }
                            },
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = estado.contrasena,
                            onValueChange = viewModel::onContrasenaChange,
                            label = { Text("Clave") },
                            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                            // Botón para mostrar u ocultar la clave. Material3 trae pocos íconos
                            // en el paquete básico, por eso se usa un botón de texto.
                            trailingIcon = {
                                TextButton(onClick = { claveVisible = !claveVisible }) {
                                    Text(if (claveVisible) "Ocultar" else "Mostrar")
                                }
                            },
                            singleLine = true,
                            visualTransformation = if (claveVisible) {
                                VisualTransformation.None
                            } else {
                                PasswordVisualTransformation()
                            },
                            isError = estado.errorContrasena != null,
                            supportingText = {
                                estado.errorContrasena?.let { Text(it) }
                            },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Password,
                                imeAction = ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = { viewModel.iniciarSesion() }
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        // El error de credenciales aparece con animación en vez de saltar
                        AnimatedVisibility(visible = estado.errorLogin != null) {
                            Text(
                                text = estado.errorLogin.orEmpty(),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.error,
                                textAlign = TextAlign.Center
                            )
                        }

                        Button(
                            onClick = viewModel::iniciarSesion,
                            enabled = !estado.cargando,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            if (estado.cargando) {
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
                    }
                }

                Text(
                    text = "Cuentas de prueba",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Cliente demo · Admin admin · Técnico tecnico\nClave: 1234",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
