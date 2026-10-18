package com.example.appsolicitudes_grupo12.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.appsolicitudes_grupo12.viewModel.SolicitudViewModel

// Pantalla del formulario para crear una solicitud.
// Solo MUESTRA datos y ENVÍA eventos al ViewModel: no valida ni guarda por su cuenta.
// viewModel() entrega el ViewModel; sobrevive a giros de pantalla.
// onGuardado: lo que debe pasar cuando la persona acepta el aviso de éxito
// onVolver: lo que debe pasar al pulsar la flecha de la barra superior
// (la navegación lo usa para volver a la lista sin guardar).
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SolicitudFormScreen(
    viewModel: SolicitudViewModel = viewModel(),
    onGuardado: () -> Unit = {},
    onVolver: () -> Unit = {}
) {
    // La pantalla observa el estado: cada cambio redibuja lo necesario
    val estado by viewModel.estado.collectAsState()

    // Controla los mensajes emergentes (Snackbar) en la parte inferior.
    // Ahora se usa solo para errores de guardado.
    val snackbarHostState = remember { SnackbarHostState() }

    // Recuerda si el menú de categorías está abierto o cerrado
    var menuCategoriaAbierto by remember { mutableStateOf(false) }

    // Se ejecuta cuando aparece un error de guardado: lo muestra y luego lo descarta
    LaunchedEffect(estado.errorGuardado) {
        estado.errorGuardado?.let { mensaje ->
            snackbarHostState.showSnackbar(mensaje)
            viewModel.descartarErrorGuardado()
        }
    }

    // ---------- Aviso de éxito: cuadro de diálogo con el código ----------
    // Aparece solo cuando se guardó bien. No se cierra tocando fuera ni con
    // "atrás": la persona debe pulsar Aceptar, así no se pierde su código.
    if (estado.guardadoExitosamente) {
        AlertDialog(
            onDismissRequest = {},
            properties = DialogProperties(
                dismissOnBackPress = false,
                dismissOnClickOutside = false
            ),
            title = { Text("Solicitud registrada") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Guarda tu código de seguimiento:")
                    // SelectionContainer permite mantener pulsado para copiar el código
                    SelectionContainer {
                        Text(
                            text = estado.codigoGenerado ?: "",
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.reiniciarFormulario()  // cierra el diálogo y vacía el formulario
                        onGuardado()                     // avisa a la navegación para volver
                    }
                ) {
                    Text("Aceptar")
                }
            }
        )
    }

    // Scaffold = estructura base: barra superior, contenido y zona de mensajes
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Nueva solicitud") },
                // Flecha de volver a la izquierda del título
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver"   // descripción para lectores de pantalla
                        )
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }   // <- agregar esta línea
    ) { paddingInterno ->

        // Column con scroll: si el teclado ocupa espacio, se puede deslizar
        Column(
            modifier = Modifier
                .padding(paddingInterno)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
                .imePadding(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // ---------- Título ----------
            OutlinedTextField(
                value = estado.titulo,                       // lee del ViewModel
                onValueChange = viewModel::onTituloChange,   // escribe en el ViewModel
                label = { Text("Título *") },
                singleLine = true,
                isError = estado.errores.titulo != null,     // borde rojo si hay error
                supportingText = {
                    estado.errores.titulo?.let { Text(it) }
                },
                modifier = Modifier.fillMaxWidth()
            )

            // ---------- Descripción ----------
            OutlinedTextField(
                value = estado.descripcion,
                onValueChange = viewModel::onDescripcionChange,
                label = { Text("Descripción *") },
                minLines = 3,
                maxLines = 6,
                isError = estado.errores.descripcion != null,
                supportingText = {
                    // Si hay error lo muestra; si no, indica el mínimo requerido
                    Text(
                        estado.errores.descripcion
                            ?: "Mínimo ${SolicitudViewModel.DESCRIPCION_MINIMA} caracteres"
                    )
                },
                modifier = Modifier.fillMaxWidth()
            )

            // ---------- Categoría (lista desplegable) ----------
            ExposedDropdownMenuBox(
                expanded = menuCategoriaAbierto,
                onExpandedChange = { menuCategoriaAbierto = it }
            ) {
                OutlinedTextField(
                    value = estado.categoria,
                    onValueChange = {},          // no se escribe: solo se elige de la lista
                    readOnly = true,
                    label = { Text("Categoría *") },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = menuCategoriaAbierto)
                    },
                    isError = estado.errores.categoria != null,
                    supportingText = {
                        estado.errores.categoria?.let { Text(it) }
                    },
                    modifier = Modifier
                        .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                        .fillMaxWidth()
                )

                ExposedDropdownMenu(
                    expanded = menuCategoriaAbierto,
                    onDismissRequest = { menuCategoriaAbierto = false }
                ) {
                    // Una opción por cada categoría definida en el ViewModel
                    SolicitudViewModel.CATEGORIAS.forEach { categoria ->
                        DropdownMenuItem(
                            text = { Text(categoria) },
                            onClick = {
                                viewModel.onCategoriaChange(categoria)
                                menuCategoriaAbierto = false
                            }
                        )
                    }
                }
            }

            // ---------- Servicio relacionado ----------
            // Obligatorio o no según la categoría elegida (regla del ViewModel)
            val servicioObligatorio = viewModel.servicioObligatorio(estado.categoria)
            OutlinedTextField(
                value = estado.servicio,
                onValueChange = viewModel::onServicioChange,
                label = {
                    Text(
                        if (servicioObligatorio) "Servicio relacionado *"
                        else "Servicio relacionado (opcional)"
                    )
                },
                singleLine = true,
                isError = estado.errores.servicio != null,
                supportingText = {
                    estado.errores.servicio?.let { Text(it) }
                },
                modifier = Modifier.fillMaxWidth()
            )

            // ---------- Botón Registrar ----------
            Button(
                onClick = { viewModel.guardar() },
                enabled = !estado.guardando,      // se desactiva mientras guarda
                modifier = Modifier.fillMaxWidth()
            ) {
                if (estado.guardando) {
                    // Estado Loading: indicador de carga dentro del botón
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Guardando...")
                } else {
                    Text("Registrar solicitud")
                }
            }

            Text(
                text = "* Campo obligatorio",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}