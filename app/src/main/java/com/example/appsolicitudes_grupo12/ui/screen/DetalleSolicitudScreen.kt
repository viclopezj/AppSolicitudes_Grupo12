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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.SubcomposeAsyncImage
import com.example.appsolicitudes_grupo12.model.data.local.SolicitudEntity
import com.example.appsolicitudes_grupo12.model.solicitud.CategoriasSolicitud
import com.example.appsolicitudes_grupo12.model.solicitud.ValidadorSolicitud
import com.example.appsolicitudes_grupo12.state.DetalleSolicitudUiState
import com.example.appsolicitudes_grupo12.state.EdicionSolicitud
import com.example.appsolicitudes_grupo12.ui.adaptive.AppWindowSizeClass
import com.example.appsolicitudes_grupo12.ui.adaptive.adaptiveContentWidth
import com.example.appsolicitudes_grupo12.ui.components.EtiquetaEstado
import com.example.appsolicitudes_grupo12.ui.components.ImagenQrSolicitud
import com.example.appsolicitudes_grupo12.viewmodel.DetalleSolicitudViewModel
import com.example.appsolicitudes_grupo12.model.data.local.ComentarioSolicitudEntity
import com.example.appsolicitudes_grupo12.model.data.local.HistorialSolicitudEntity
import com.example.appsolicitudes_grupo12.ui.components.ComentariosSolicitud
import com.example.appsolicitudes_grupo12.ui.components.HistorialSolicitud
import com.example.appsolicitudes_grupo12.ui.components.formatearFecha
import com.example.appsolicitudes_grupo12.model.solicitud.EstadoSolicitud
import com.example.appsolicitudes_grupo12.ui.components.AccionesSolicitud
import com.example.appsolicitudes_grupo12.model.data.local.UsuarioEntity
import com.example.appsolicitudes_grupo12.model.solicitud.PrioridadSolicitud
import com.example.appsolicitudes_grupo12.ui.components.GestionSolicitud

// Detalle de una solicitud, con dos modos:
//   - Lectura: datos guardados, foto de evidencia, código QR y botón "Editar solicitud".
//   - Edición: campos para modificar título, descripción, categoría y servicio.
// Solo muestra el estado del ViewModel y le envía eventos.
// onVolver: flecha de la barra superior y botón "Volver".
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetalleSolicitudScreen(
    id: Int,
    usuarioId: Int,
    rol: String,
    viewModel: DetalleSolicitudViewModel = viewModel(
        factory = DetalleSolicitudViewModel.factory(LocalContext.current, id, usuarioId, rol)
    ),
    windowSizeClass: AppWindowSizeClass = AppWindowSizeClass.Compact,
    onVolver: () -> Unit = {}
) {
    val estado by viewModel.estado.collectAsState()
    val historial by viewModel.historial.collectAsState()
    val comentarios by viewModel.comentarios.collectAsState()
    val acciones by viewModel.acciones.collectAsState()
    val tecnicos by viewModel.tecnicos.collectAsState()

    // Mensajes emergentes para errores al guardar los cambios
    val snackbarHostState = remember { SnackbarHostState() }

    // Muestra el aviso (éxito o error) de las acciones y luego lo descarta
    LaunchedEffect(acciones.error, acciones.mensaje) {
        val texto = acciones.error ?: acciones.mensaje
        if (texto != null) {
            snackbarHostState.showSnackbar(texto)
            viewModel.descartarAvisosAccion()
        }
    }

    // Error de guardado del modo edición (null si no hay)
    val errorGuardado = (estado as? DetalleSolicitudUiState.Success)?.edicion?.errorGuardado

    // Muestra el error de guardado y luego lo descarta
    LaunchedEffect(errorGuardado) {
        errorGuardado?.let { mensaje ->
            snackbarHostState.showSnackbar(mensaje)
            viewModel.descartarErrorGuardado()
        }
    }

    // Para cambiar el título de la barra según el modo
    val enEdicion = (estado as? DetalleSolicitudUiState.Success)?.edicion != null

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text(if (enEdicion) "Editar solicitud" else "Detalle de solicitud") },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver"
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
            // Fundido entre estados. contentKey distingue lectura de edición para animar ese cambio,
            // pero no repite la animación cuando solo cambian los textos que se escriben.
            AnimatedContent(
                modifier = Modifier
                    // Centra el detalle y limita su ancho en ventanas amplias
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
                            DetalleLectura(
                                solicitud = actual.solicitud,
                                rol = rol,
                                puedeEditar = viewModel.puedeEditar,
                                historial = historial,
                                comentarios = comentarios,
                                tecnicos = tecnicos,
                                procesando = acciones.procesando,
                                onEditar = viewModel::iniciarEdicion,
                                onCambiarEstado = viewModel::cambiarEstado,
                                onCambiarPrioridad = viewModel::cambiarPrioridad,
                                onAsignarTecnico = viewModel::asignarTecnico
                            )
                        } else {
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

// Loading: indicador de carga al centro.
@Composable
private fun DetalleCargando() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

// NoEncontrada: el id no existe o no hay acceso.
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

// Error: mensaje y botón para reintentar.
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

// Modo lectura: los datos guardados, la foto y el QR.
@Composable
private fun DetalleLectura(
    solicitud: SolicitudEntity,
    rol: String,
    puedeEditar: Boolean,
    historial: List<HistorialSolicitudEntity>,
    comentarios: List<ComentarioSolicitudEntity>,
    tecnicos: List<UsuarioEntity>,
    procesando: Boolean,
    onEditar: () -> Unit,
    onCambiarEstado: (EstadoSolicitud, String?) -> Unit,
    onCambiarPrioridad: (PrioridadSolicitud) -> Unit,
    onAsignarTecnico: (Int) -> Unit
) {
    // Con scroll por si la descripción es larga
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
        CampoDeLectura(etiqueta = "Prioridad", valor = solicitud.prioridad)
        CampoDeLectura(
            etiqueta = "Última actualización",
            valor = formatearFecha(solicitud.ultimaActualizacion)
        )

        EvidenciaSolicitud(evidenciaUri = solicitud.evidenciaUri)

        // QR con los datos actuales: si la solicitud se edita, se actualiza solo.
        // La foto no viaja dentro del QR.
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
            // Por ahora solo el administrador (técnico y cliente llegan después)
            if (rol == "administrador") {
                GestionSolicitud(
                    solicitud = solicitud,
                    rol = rol,
                    tecnicos = tecnicos,
                    procesando = procesando,
                    onCambiarPrioridad = onCambiarPrioridad,
                    onAsignarTecnico = onAsignarTecnico
                )
                AccionesSolicitud(
                    rol = rol,
                    estadoActual = solicitud.estado,
                    procesando = procesando,
                    onCambiarEstado = onCambiarEstado
                )
            }
            ComentariosSolicitud(comentarios = comentarios)
            HistorialSolicitud(historial = historial)
        }
    }
}

// Foto de evidencia. Casos: sin foto, foto cargando/cargada, o foto que no se pudo leer.
// SubcomposeAsyncImage (Coil) permite decidir qué dibujar mientras carga o si falla.
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
                // Fit: se ve la foto completa, sin recortar
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp)
                    .clip(RoundedCornerShape(12.dp)),
                loading = {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                },
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

// Modo edición: campos para modificar los 4 datos básicos.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DetalleEdicion(
    solicitud: SolicitudEntity,
    edicion: EdicionSolicitud,
    viewModel: DetalleSolicitudViewModel
) {
    var menuCategoriaAbierto by remember { mutableStateOf(false) }

    // Con scroll por si el teclado ocupa espacio
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
            .imePadding(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Código y estado se muestran pero no se pueden editar
        EncabezadoSolicitud(solicitud)

        // Título
        OutlinedTextField(
            value = edicion.titulo,
            onValueChange = viewModel::onTituloChange,
            label = { Text("Título *") },
            singleLine = true,
            isError = edicion.errores.titulo != null,
            supportingText = {
                edicion.errores.titulo?.let { Text(it) }
            },
            modifier = Modifier.fillMaxWidth()
        )

        // Descripción
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
                        ?: "Mínimo ${CategoriasSolicitud.DESCRIPCION_MINIMA} caracteres"
                )
            },
            modifier = Modifier.fillMaxWidth()
        )

        // Categoría (lista desplegable)
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
                CategoriasSolicitud.LISTA.forEach { categoria ->
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

        // Servicio relacionado (obligatorio o no según la categoría)
        val servicioObligatorio = ValidadorSolicitud.servicioObligatorio(edicion.categoria)
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

        // Guardar cambios (se desactiva mientras guarda)
        Button(
            onClick = { viewModel.guardarCambios() },
            enabled = !edicion.guardando,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (edicion.guardando) {
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

        // Cancelar: vuelve al modo lectura sin guardar
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