package com.example.appsolicitudes_grupo12.ui.screen

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.SubcomposeAsyncImage
import com.example.appsolicitudes_grupo12.data.local.SolicitudEntity
import com.example.appsolicitudes_grupo12.model.DetalleSolicitudUiState
import com.example.appsolicitudes_grupo12.model.EdicionSolicitud
import com.example.appsolicitudes_grupo12.utils.AppWindowSizeClass
import com.example.appsolicitudes_grupo12.utils.adaptiveContentWidth
import com.example.appsolicitudes_grupo12.viewModel.DetalleSolicitudViewModel
import com.example.appsolicitudes_grupo12.viewModel.SolicitudViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Pantalla de detalle de una solicitud. Tiene dos modos:
//   - Lectura: muestra los datos guardados, la foto de evidencia, su código QR y el botón "Editar solicitud".
//   - Edición: muestra los campos para modificar título, descripción, categoría y servicio.
// Solo MUESTRA el estado que le entrega el ViewModel y ENVÍA eventos: no lee ni guarda por su cuenta.
// onVolver: lo que debe pasar al pulsar la flecha o el botón "Volver"
// (la navegación lo usa para regresar a la pantalla anterior).
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetalleSolicitudScreen(
    viewModel: DetalleSolicitudViewModel = viewModel(),
    windowSizeClass: AppWindowSizeClass = AppWindowSizeClass.Compact,
    onVolver: () -> Unit = {}
) {
    // La pantalla observa el estado: cada cambio redibuja lo necesario
    val estado by viewModel.estado.collectAsState()

    // Controla los mensajes emergentes (Snackbar). Se usa para errores al guardar los cambios.
    val snackbarHostState = remember { SnackbarHostState() }

    // Si estamos en modo edición y hubo un error al guardar, aquí queda el mensaje (o null)
    val errorGuardado = (estado as? DetalleSolicitudUiState.Success)?.edicion?.errorGuardado

    // Se ejecuta cuando aparece un error de guardado: lo muestra y luego lo descarta
    LaunchedEffect(errorGuardado) {
        errorGuardado?.let { mensaje ->
            snackbarHostState.showSnackbar(mensaje)
            viewModel.descartarErrorGuardado()
        }
    }

    // Para cambiar el título de la barra según el modo
    val enEdicion = (estado as? DetalleSolicitudUiState.Success)?.edicion != null

    // Scaffold = estructura base: barra superior, contenido y zona de mensajes
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text(if (enEdicion) "Editar solicitud" else "Detalle de solicitud") },
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
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingInterno ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingInterno)
        ) {
            // Según el estado del ViewModel, se dibuja una cosa u otra.
            // Kotlin obliga a cubrir los 4 casos.
            // AnimatedContent: fundido suave entre Loading, lectura, edición, error, etc.
            // contentKey distingue lectura de edición para que también esa transición se anime,
            // pero no repite la animación cuando solo cambian los textos que se escriben.
            AnimatedContent(
                modifier = Modifier
                    // Centra el detalle y limita su ancho en ventanas amplias.
                    .align(Alignment.TopCenter)
                    .adaptiveContentWidth(windowSizeClass, 640.dp, 760.dp, 1000.dp),
                targetState = estado,
                contentKey = { actual ->
                    if (actual is DetalleSolicitudUiState.Success) {
                        if (actual.edicion != null) "edicion" else "lectura"
                    } else {
                        actual::class
                    }
                },
                transitionSpec = { fadeIn(tween(300)) togetherWith fadeOut(tween(200)) },
                label = "estadoDetalle"
            ) { actual ->
            when (actual) {
                is DetalleSolicitudUiState.Loading -> DetalleCargando()

                is DetalleSolicitudUiState.Success -> {
                    val edicion = actual.edicion
                    if (edicion == null) {
                        // Modo lectura
                        DetalleLectura(
                            solicitud = actual.solicitud,
                            puedeEditar = viewModel.puedeEditar,
                            onEditar = viewModel::iniciarEdicion
                        )
                    } else {
                        // Modo edición
                        DetalleEdicion(
                            solicitud = actual.solicitud,
                            edicion = edicion,
                            viewModel = viewModel
                        )
                    }
                }

                is DetalleSolicitudUiState.NoEncontrada -> DetalleNoEncontrada(onVolver = onVolver)

                is DetalleSolicitudUiState.Error -> DetalleError(
                    mensaje = actual.mensaje,
                    onReintentar = viewModel::cargarSolicitud
                )
            }
            }
        }
    }
}

// ---------- Estado Loading: indicador de carga al centro ----------
@Composable
private fun DetalleCargando() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

// ---------- Estado NoEncontrada: el id no existe ----------
@Composable
private fun DetalleNoEncontrada(onVolver: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "No se encontró la solicitud",
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center
        )
        Button(
            onClick = onVolver,
            modifier = Modifier.padding(top = 16.dp)
        ) {
            Text("Volver")
        }
    }
}

// ---------- Estado Error: mensaje y botón para reintentar ----------
@Composable
private fun DetalleError(
    mensaje: String,
    onReintentar: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = mensaje,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.error,
            textAlign = TextAlign.Center
        )
        Button(
            onClick = onReintentar,
            modifier = Modifier.padding(top = 16.dp)
        ) {
            Text("Reintentar")
        }
    }
}

// ---------- Modo lectura: los datos guardados ----------
@Composable
private fun DetalleLectura(
    solicitud: SolicitudEntity,
    puedeEditar: Boolean,
    onEditar: () -> Unit
) {
    // Column con scroll: si la descripción es larga, se puede deslizar
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        EncabezadoSolicitud(solicitud)

        Text(
            text = solicitud.titulo,
            style = MaterialTheme.typography.headlineSmall
        )

        CampoDeLectura(etiqueta = "Descripción", valor = solicitud.descripcion)
        CampoDeLectura(etiqueta = "Categoría", valor = solicitud.categoria)
        // El servicio puede estar vacío (en "Consulta" y "Otro requerimiento" es opcional)
        CampoDeLectura(
            etiqueta = "Servicio relacionado",
            valor = solicitud.servicio.ifBlank { "No indicado" }
        )
        CampoDeLectura(
            etiqueta = "Fecha de creación",
            valor = formatearFecha(solicitud.fechaCreacion)
        )

        // ---------- Evidencia: la foto asociada a la solicitud ----------
        EvidenciaSolicitud(evidenciaUri = solicitud.evidenciaUri)

        // ---------- Código QR de la solicitud ----------
        // Se dibuja con los datos guardados en este momento. Si la solicitud se edita,
        // este QR se actualiza solo; un QR que ya se imprimió o capturó antes, no.
        // La foto NO viaja dentro del QR: solo existe en este teléfono.
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Código QR",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            ImagenQrSolicitud(solicitud = solicitud)
            Text(
                text = "Se puede escanear desde la lista de solicitudes.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }

        if (puedeEditar) {
            Button(
                onClick = onEditar,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Editar solicitud")
            }
        }
    }
}

// Muestra la foto de evidencia de la solicitud. Cubre tres casos:
//   - No hay foto (evidenciaUri es null): dice "Sin foto adjunta".
//   - Hay foto: la carga y la muestra (mientras carga, un indicador).
//   - La foto no se pudo leer (por ejemplo, el archivo ya no existe): avisa en vez de cerrar la app.
// SubcomposeAsyncImage es de Coil: lee el archivo en segundo plano y lo reduce al tamaño
// necesario, y permite decidir qué dibujar mientras carga o si falla.
@Composable
private fun EvidenciaSolicitud(evidenciaUri: String?) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "Evidencia",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        if (evidenciaUri == null) {
            Text(
                text = "Sin foto adjunta",
                style = MaterialTheme.typography.bodyLarge
            )
        } else {
            SubcomposeAsyncImage(
                model = evidenciaUri,
                contentDescription = "Foto de evidencia de la solicitud",
                // Fit: se ve la foto completa, sin recortar los bordes
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp)
                    .clip(RoundedCornerShape(12.dp)),
                // Estado Loading: indicador mientras se lee la foto
                loading = {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                },
                // Estado Error: mensaje amable si el archivo no se puede leer
                error = {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No se pudo cargar la foto",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.error,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            )
        }
    }
}

// ---------- Modo edición: campos para modificar los 4 datos básicos ----------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DetalleEdicion(
    solicitud: SolicitudEntity,
    edicion: EdicionSolicitud,
    viewModel: DetalleSolicitudViewModel
) {
    // Recuerda si el menú de categorías está abierto o cerrado
    var menuCategoriaAbierto by remember { mutableStateOf(false) }

    // Column con scroll: si el teclado ocupa espacio, se puede deslizar
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
            .imePadding(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // El código y el estado se muestran pero NO se pueden editar
        EncabezadoSolicitud(solicitud)

        // ---------- Título ----------
        OutlinedTextField(
            value = edicion.titulo,                      // lee del ViewModel
            onValueChange = viewModel::onTituloChange,   // escribe en el ViewModel
            label = { Text("Título *") },
            singleLine = true,
            isError = edicion.errores.titulo != null,    // borde rojo si hay error
            supportingText = {
                edicion.errores.titulo?.let { Text(it) }
            },
            modifier = Modifier.fillMaxWidth()
        )

        // ---------- Descripción ----------
        OutlinedTextField(
            value = edicion.descripcion,
            onValueChange = viewModel::onDescripcionChange,
            label = { Text("Descripción *") },
            minLines = 3,
            maxLines = 6,
            isError = edicion.errores.descripcion != null,
            supportingText = {
                // Si hay error lo muestra; si no, indica el mínimo requerido
                Text(
                    edicion.errores.descripcion
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
                value = edicion.categoria,
                onValueChange = {},          // no se escribe: solo se elige de la lista
                readOnly = true,
                label = { Text("Categoría *") },
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = menuCategoriaAbierto)
                },
                isError = edicion.errores.categoria != null,
                supportingText = {
                    edicion.errores.categoria?.let { Text(it) }
                },
                modifier = Modifier
                    .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                    .fillMaxWidth()
            )

            ExposedDropdownMenu(
                expanded = menuCategoriaAbierto,
                onDismissRequest = { menuCategoriaAbierto = false }
            ) {
                // Mismas categorías del formulario
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
        val servicioObligatorio = viewModel.servicioObligatorio(edicion.categoria)
        OutlinedTextField(
            value = edicion.servicio,
            onValueChange = viewModel::onServicioChange,
            label = {
                Text(
                    if (servicioObligatorio) "Servicio relacionado *"
                    else "Servicio relacionado (opcional)"
                )
            },
            singleLine = true,
            isError = edicion.errores.servicio != null,
            supportingText = {
                edicion.errores.servicio?.let { Text(it) }
            },
            modifier = Modifier.fillMaxWidth()
        )

        // ---------- Botón Guardar cambios ----------
        Button(
            onClick = { viewModel.guardarCambios() },
            enabled = !edicion.guardando,      // se desactiva mientras guarda
            modifier = Modifier.fillMaxWidth()
        ) {
            if (edicion.guardando) {
                // Estado Loading: indicador de carga dentro del botón
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Guardando...")
            } else {
                Text("Guardar cambios")
            }
        }

        // ---------- Botón Cancelar: vuelve al modo lectura sin guardar ----------
        OutlinedButton(
            onClick = { viewModel.cancelarEdicion() },
            enabled = !edicion.guardando,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Cancelar")
        }

        Text(
            text = "* Campo obligatorio",
            style = MaterialTheme.typography.bodySmall
        )
    }
}

// ---------- Piezas reutilizadas por los dos modos ----------

// Fila superior: código a la izquierda, etiqueta de estado a la derecha.
@Composable
private fun EncabezadoSolicitud(solicitud: SolicitudEntity) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = solicitud.codigo,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary
        )
        // EtiquetaEstado está en ListaSolicitudesScreen.kt
        EtiquetaEstado(solicitud.estado)
    }
}

// Un dato de solo lectura: etiqueta pequeña arriba y el valor debajo.
@Composable
private fun CampoDeLectura(etiqueta: String, valor: String) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = etiqueta,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = valor,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

// Convierte los milisegundos guardados en Room a un texto legible,
// por ejemplo "17/10/2026 14:35".
private fun formatearFecha(milisegundos: Long): String {
    val formato = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    return formato.format(Date(milisegundos))
}
