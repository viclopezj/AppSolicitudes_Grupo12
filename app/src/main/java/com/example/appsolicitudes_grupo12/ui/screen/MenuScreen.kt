package com.example.appsolicitudes_grupo12.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.background
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Button
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.PermanentDrawerSheet
import androidx.compose.material3.PermanentNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.appsolicitudes_grupo12.utils.AppWindowSizeClass
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
    rolUsuario: String,
    windowSizeClass: AppWindowSizeClass = AppWindowSizeClass.Compact,
    onIrALista: () -> Unit = {},
    onIrAFormulario: () -> Unit = {},
    onIrAAsignaciones: () -> Unit = {},
    onCerrarSesion: () -> Unit = {}
) {
    // Recuerda si el menú lateral está abierto o cerrado
    val estadoMenu = rememberDrawerState(initialValue = DrawerValue.Closed)

    // Abrir y cerrar el menú es una animación; por eso se hace dentro de una corrutina
    val alcance = rememberCoroutineScope()
    val esCompacto = windowSizeClass == AppWindowSizeClass.Compact

    val drawerItems: @Composable () -> Unit = {
                // El contenido del drawer se comparte entre las versiones modal y permanente.
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
                    Text(
                        text = "Perfil: ${etiquetaRol(rolUsuario)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // ---------- Opciones ----------
                // En cada una: primero se cierra el menú y luego se avisa a la navegación
                NavigationDrawerItem(
                    label = { Text(if (rolUsuario == "cliente") "Mis solicitudes" else "Solicitudes") },
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

                if (rolUsuario == "cliente") {
                    NavigationDrawerItem(
                        label = { Text("Nueva solicitud") },
                        selected = false,
                        onClick = {
                            alcance.launch { estadoMenu.close() }
                            onIrAFormulario()
                        },
                        icon = {
                            Icon(imageVector = Icons.Default.Add, contentDescription = null)
                        }
                    )
                }

                if (rolUsuario == "administrador") {
                    NavigationDrawerItem(
                        label = { Text("Asignar solicitudes") },
                        selected = false,
                        onClick = {
                            alcance.launch { estadoMenu.close() }
                            onIrAAsignaciones()
                        },
                        icon = {
                            Icon(imageVector = Icons.AutoMirrored.Filled.List, contentDescription = null)
                        }
                    )
                }

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

    val contenidoPrincipal: @Composable () -> Unit = {
        // ---------- Contenido de la pantalla (lo que se ve con el menú cerrado) ----------
        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            topBar = {
                TopAppBar(
                    title = { Text("Inicio") },
                    // Botón de las tres rayas: abre el menú lateral
                    navigationIcon = {
                        if (esCompacto) IconButton(onClick = { alcance.launch { estadoMenu.open() } }) {
                            Icon(
                                imageVector = Icons.Default.Menu,
                                contentDescription = "Abrir menú"   // para lectores de pantalla
                            )
                        }
                    }
                )
            }
        ) { paddingInterno ->

            Box(
                modifier = Modifier.fillMaxSize().padding(paddingInterno).padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                // Centra el panel y limita su ancho en ventanas amplias.
                Column(
                    modifier = Modifier
                        .widthIn(max = if (esCompacto) 520.dp else 680.dp)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.large
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(24.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Hola, $nombreUsuario",
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = etiquetaRol(rolUsuario).uppercase(),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier
                                .background(
                                    MaterialTheme.colorScheme.primaryContainer,
                                    MaterialTheme.shapes.small
                                )
                                .padding(horizontal = 12.dp, vertical = 7.dp)
                        )
                        Text(
                            text = "¿Qué quieres hacer?",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Accesos rápidos a las mismas opciones del menú lateral
                Button(
                    onClick = onIrALista,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(if (rolUsuario == "cliente") "Ver mis solicitudes" else "Ver solicitudes")
                }

                if (rolUsuario == "cliente") {
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedButton(
                        onClick = onIrAFormulario,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Registrar nueva solicitud")
                    }
                }

                if (rolUsuario == "administrador") {
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedButton(
                        onClick = onIrAAsignaciones,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Asignar solicitudes a técnicos")
                    }
                }
            }
            }
        }
    }

    if (esCompacto) {
        // ModalNavigationDrawer = el menú lateral que se desliza sobre la pantalla
        // ModalNavigationDrawer conserva el menu deslizable en ventanas compactas.
        ModalNavigationDrawer(
            drawerState = estadoMenu,
            drawerContent = { ModalDrawerSheet { drawerItems() } }
        ) {
            contenidoPrincipal()
        }
    } else {
        // PermanentNavigationDrawer mantiene las opciones visibles en ventanas amplias.
        PermanentNavigationDrawer(
            drawerContent = { PermanentDrawerSheet { drawerItems() } }
        ) {
            contenidoPrincipal()
        }
    }

}

private fun etiquetaRol(rol: String): String = when (rol) {
    "administrador" -> "Administrador"
    "tecnico" -> "Técnico"
    "cliente" -> "Cliente"
    else -> "Sin perfil"
}
