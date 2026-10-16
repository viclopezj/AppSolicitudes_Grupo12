package com.example.appsolicitudes_grupo12.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Button
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

// Pantalla de menú: es la que aparece justo después del login.
// Tiene un menú lateral (drawer) que se abre con el botón de las tres rayas.
// Solo MUESTRA opciones y avisa qué se pulsó: no sabe cómo se navega.
// La navegación le entrega las funciones:
// onIrALista / onIrAFormulario / onCerrarSesion.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MenuScreen(
    nombreUsuario: String,
    onIrALista: () -> Unit = {},
    onIrAFormulario: () -> Unit = {},
    onCerrarSesion: () -> Unit = {}
) {
    // Recuerda si el menú lateral está abierto o cerrado
    val estadoMenu = rememberDrawerState(initialValue = DrawerValue.Closed)

    // Abrir y cerrar el menú es una animación; por eso se hace dentro de una corrutina
    val alcance = rememberCoroutineScope()

    // ModalNavigationDrawer = el menú lateral que se desliza sobre la pantalla
    ModalNavigationDrawer(
        drawerState = estadoMenu,
        drawerContent = {
            ModalDrawerSheet {

                // ---------- Encabezado del menú ----------
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Menú",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Usuario: $nombreUsuario",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // ---------- Opciones ----------
                // En cada una: primero se cierra el menú y luego se avisa a la navegación
                NavigationDrawerItem(
                    label = { Text("Mis solicitudes") },
                    selected = false,
                    onClick = {
                        alcance.launch { estadoMenu.close() }
                        onIrALista()
                    },
                    icon = {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.List,
                            contentDescription = null   // el texto de al lado ya lo describe
                        )
                    }
                )

                NavigationDrawerItem(
                    label = { Text("Nueva solicitud") },
                    selected = false,
                    onClick = {
                        alcance.launch { estadoMenu.close() }
                        onIrAFormulario()
                    },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null
                        )
                    }
                )

                NavigationDrawerItem(
                    label = { Text("Cerrar sesión") },
                    selected = false,
                    onClick = {
                        alcance.launch { estadoMenu.close() }
                        onCerrarSesion()
                    },
                    icon = {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                            contentDescription = null
                        )
                    }
                )
            }
        }
    ) {
        // ---------- Contenido de la pantalla (lo que se ve con el menú cerrado) ----------
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Inicio") },
                    // Botón de las tres rayas: abre el menú lateral
                    navigationIcon = {
                        IconButton(onClick = { alcance.launch { estadoMenu.open() } }) {
                            Icon(
                                imageVector = Icons.Default.Menu,
                                contentDescription = "Abrir menú"   // para lectores de pantalla
                            )
                        }
                    }
                )
            }
        ) { paddingInterno ->

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingInterno)
                    .padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Hola, $nombreUsuario",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "¿Qué quieres hacer?",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp)
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Accesos rápidos a las mismas opciones del menú lateral
                Button(
                    onClick = onIrALista,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Ver mis solicitudes")
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedButton(
                    onClick = onIrAFormulario,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Registrar nueva solicitud")
                }
            }
        }
    }
}