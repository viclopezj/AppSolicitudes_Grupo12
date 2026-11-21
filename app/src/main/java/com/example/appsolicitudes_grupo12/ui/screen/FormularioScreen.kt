package com.example.appsolicitudes_grupo12.ui.screen

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import com.example.appsolicitudes_grupo12.model.data.files.EvidenciaArchivos
import com.example.appsolicitudes_grupo12.model.data.local.SolicitudEntity
import com.example.appsolicitudes_grupo12.model.solicitud.CategoriasSolicitud
import com.example.appsolicitudes_grupo12.ui.adaptive.AppWindowSizeClass
import com.example.appsolicitudes_grupo12.ui.adaptive.adaptiveContentWidth
import com.example.appsolicitudes_grupo12.ui.camera.CameraPermissionHelper
import com.example.appsolicitudes_grupo12.ui.components.AdjuntosFormulario
import com.example.appsolicitudes_grupo12.ui.components.IconoError
import com.example.appsolicitudes_grupo12.ui.components.ImagenQrSolicitud
import com.example.appsolicitudes_grupo12.ui.components.SelectorCategoria
import com.example.appsolicitudes_grupo12.viewmodel.CatalogoViewModel
import com.example.appsolicitudes_grupo12.viewmodel.FormularioViewModel
import java.io.File

// Formulario para crear una solicitud. Solo muestra datos y envía eventos al ViewModel.
// onGuardado: se llama al aceptar el aviso de éxito. onVolver: flecha de la barra superior.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FormularioScreen(
    usuarioId: Int,
    rol: String,
    viewModel: FormularioViewModel = viewModel(
        factory = FormularioViewModel.factory(LocalContext.current, usuarioId, rol)
    ),
    catalogoViewModel: CatalogoViewModel = viewModel(factory = CatalogoViewModel.factory()),
    windowSizeClass: AppWindowSizeClass = AppWindowSizeClass.Compact,
    onGuardado: () -> Unit = {},
    onVolver: () -> Unit = {}
) {
    val estado by viewModel.estado.collectAsState()
    val catalogo by catalogoViewModel.estado.collectAsState()
    val context = LocalContext.current

    // Mensajes emergentes para errores de guardado y de la foto
    val snackbarHostState = remember { SnackbarHostState() }

    // Ruta del archivo donde la cámara escribe la foto. Se guarda como texto con rememberSaveable
    // para no perderla si la app se reinicia mientras la cámara está abierta.
    var rutaFotoPendiente by rememberSaveable { mutableStateOf<String?>(null) }

    // ---------- Lanzadores de cámara, galería y permiso ----------

    // Abre la cámara. Al volver, "exito" dice si se tomó la foto.
    val lanzadorCamara = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { exito ->
        val ruta = rutaFotoPendiente
        rutaFotoPendiente = null
        if (ruta != null) {
            val archivo = File(ruta)
            if (exito) viewModel.onFotoTomada(archivo) else viewModel.onFotoCancelada(archivo)
        }
    }

    // Prepara un archivo vacío y abre la cámara para que escriba la foto en él
    val abrirCamara = {
        val archivo = viewModel.crearArchivoParaCamara()
        rutaFotoPendiente = archivo.absolutePath
        lanzadorCamara.launch(EvidenciaArchivos.uriParaCamara(context, archivo))
    }

    // Pide el permiso de cámara; si se concede, abre la cámara
    val lanzadorPermiso = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { concedido ->
        if (concedido) abrirCamara() else viewModel.onPermisoCamaraDenegado()
    }

    // Selector de fotos del sistema (no necesita permisos). uri es null si se cierra sin elegir.
    val lanzadorGaleria = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) viewModel.onFotoElegida(uri)
    }

    // ---------- Avisos ----------

    // Muestra el error de guardado y luego lo descarta
    LaunchedEffect(estado.errorGuardado) {
        estado.errorGuardado?.let { mensaje ->
            snackbarHostState.showSnackbar(mensaje)
            viewModel.descartarErrorGuardado()
        }
    }

    // Muestra el error de la foto y luego lo descarta
    LaunchedEffect(estado.errorFoto) {
        estado.errorFoto?.let { mensaje ->
            snackbarHostState.showSnackbar(mensaje)
            viewModel.descartarErrorFoto()
        }
    }

    // Muestra el error del adjunto y luego lo descarta
    LaunchedEffect(estado.errorAdjunto) {
        estado.errorAdjunto?.let { mensaje ->
            snackbarHostState.showSnackbar(mensaje)
            viewModel.descartarErrorAdjunto()
        }
    }

    // Muestra los avisos del borrador (guardado, recuperado o descartado) y luego los descarta
    LaunchedEffect(estado.mensaje) {
        estado.mensaje?.let { mensaje ->
            snackbarHostState.showSnackbar(mensaje)
            viewModel.descartarMensaje()
        }
    }

    // Aviso de éxito con el código y el QR
    val solicitudCreada = estado.solicitudCreada
    if (estado.guardadoExitosamente && solicitudCreada != null) {
        DialogoSolicitudRegistrada(
            solicitud = solicitudCreada,
            onAceptar = {
                viewModel.reiniciarFormulario()   // cierra el diálogo y vacía el formulario
                onGuardado()                      // avisa a la navegación para volver
            }
        )
    }

    // ---------- Pantalla ----------

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("Nueva solicitud") },
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
            modifier = Modifier.fillMaxSize().padding(paddingInterno).imePadding(),
            contentAlignment = Alignment.TopCenter
        ) {
            // Ancho limitado en ventanas amplias; con scroll por si el teclado ocupa espacio
            Column(
                modifier = Modifier
                    .adaptiveContentWidth(windowSizeClass, 600.dp, 680.dp, 720.dp)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {

                // Datos de contacto ficticios del cliente (solo lectura)
                if (estado.empresa.isNotBlank() || estado.telefono.isNotBlank()) {
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            text = "Datos de contacto (ficticios)",
                            style = MaterialTheme.typography.titleSmall
                        )
                        Text(
                            text = "Empresa: ${estado.empresa.ifBlank { "No indicada" }}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = "Teléfono: ${estado.telefono.ifBlank { "No indicado" }}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }

                // Título
                OutlinedTextField(
                    value = estado.titulo,
                    onValueChange = viewModel::onTituloChange,
                    label = { Text("Título *") },
                    leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) },
                    trailingIcon = { if (estado.errores.titulo != null) IconoError() },
                    singleLine = true,
                    isError = estado.errores.titulo != null,
                    supportingText = {
                        estado.errores.titulo?.let { Text(it) }
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                // Descripción
                OutlinedTextField(
                    value = estado.descripcion,
                    onValueChange = viewModel::onDescripcionChange,
                    label = { Text("Descripción *") },
                    leadingIcon = { Icon(Icons.Default.Create, contentDescription = null) },
                    trailingIcon = { if (estado.errores.descripcion != null) IconoError() },
                    minLines = 3,
                    maxLines = 6,
                    isError = estado.errores.descripcion != null,
                    supportingText = {
                        // Si hay error lo muestra; si no, indica el mínimo requerido
                        Text(
                            estado.errores.descripcion
                                ?: "Mínimo ${CategoriasSolicitud.DESCRIPCION_MINIMA} caracteres"
                        )
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                // Categoría: lista del catálogo (API) o texto manual si la API falla
                SelectorCategoria(
                    categoria = estado.categoria,
                    error = estado.errores.categoria,
                    catalogo = catalogo,
                    onCategoriaChange = viewModel::onCategoriaChange,
                    onReintentar = catalogoViewModel::cargarCatalogo
                )

                // Servicio relacionado: texto libre y opcional, según la categoría elegida
                OutlinedTextField(
                    value = estado.servicio,
                    onValueChange = viewModel::onServicioChange,
                    label = { Text("Servicio relacionado (opcional)") },
                    leadingIcon = { Icon(Icons.Default.Build, contentDescription = null) },
                    minLines = 3,
                    maxLines = 6,
                    supportingText = {
                        Text("Cuéntanos qué servicio o sitio se relaciona con la categoría elegida")
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                // Evidencia (foto opcional)
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Evidencia (opcional)",
                        style = MaterialTheme.typography.titleSmall
                    )

                    // Vista previa: indicador mientras se copia la foto, y la foto cuando ya hay una
                    AnimatedVisibility(
                        visible = estado.cargandoFoto || estado.evidenciaUri != null,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        if (estado.cargandoFoto) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator()
                            }
                        } else if (estado.evidenciaUri != null) {
                            // AsyncImage (Coil) lee y reduce la foto en segundo plano
                            AsyncImage(
                                model = estado.evidenciaUri,
                                contentDescription = "Foto de evidencia de la solicitud",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp)
                                    .clip(RoundedCornerShape(12.dp))
                            )
                        }
                    }

                    // Botones de foto: se desactivan mientras se copia o se guarda
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(
                            onClick = {
                                // Con permiso abre la cámara; sin permiso lo pide primero
                                if (CameraPermissionHelper.hasCameraPermission(context)) {
                                    abrirCamara()
                                } else {
                                    lanzadorPermiso.launch(Manifest.permission.CAMERA)
                                }
                            },
                            enabled = !estado.cargandoFoto && !estado.guardando,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Tomar foto")
                        }

                        OutlinedButton(
                            onClick = {
                                lanzadorGaleria.launch(
                                    PickVisualMediaRequest(
                                        ActivityResultContracts.PickVisualMedia.ImageOnly
                                    )
                                )
                            },
                            enabled = !estado.cargandoFoto && !estado.guardando,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Elegir de galería")
                        }
                    }

                    // Solo aparece si ya hay una foto elegida
                    AnimatedVisibility(visible = estado.evidenciaUri != null) {
                        TextButton(
                            onClick = viewModel::quitarFoto,
                            enabled = !estado.cargandoFoto && !estado.guardando
                        ) {
                            Text("Quitar foto")
                        }
                    }

                    // Regla del caso: solo datos ficticios
                    Text(
                        text = "Usa solo imágenes de prueba, sin datos personales.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Imágenes y documentos adicionales (opcionales)
                AdjuntosFormulario(
                    adjuntos = estado.adjuntos,
                    cargando = estado.cargandoAdjunto,
                    habilitado = !estado.guardando,
                    onElegir = viewModel::onAdjuntoElegido,
                    onQuitar = viewModel::quitarAdjunto
                )

                // Borrador: guarda el texto para seguir después (sin validar)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = viewModel::guardarBorrador,
                        enabled = !estado.guardando,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Guardar borrador")
                    }
                    if (estado.hayBorrador) {
                        TextButton(
                            onClick = viewModel::descartarBorrador,
                            enabled = !estado.guardando
                        ) {
                            Text("Descartar borrador")
                        }
                    }
                }

                // Botón Registrar (se desactiva mientras guarda o copia una foto o un archivo)
                Button(
                    onClick = { viewModel.guardar() },
                    enabled = !estado.guardando && !estado.cargandoFoto && !estado.cargandoAdjunto,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (estado.guardando) {
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
}

// Aviso de éxito con el código y el QR. No se cierra tocando fuera ni con "atrás":
// hay que pulsar Aceptar, así no se pierde el código.
@Composable
private fun DialogoSolicitudRegistrada(
    solicitud: SolicitudEntity,
    onAceptar: () -> Unit
) {
    AlertDialog(
        onDismissRequest = {},
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false
        ),
        title = { Text("Solicitud registrada") },
        text = {
            // Con scroll para que el QR no se corte en pantallas pequeñas u horizontales
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Guarda tu código de seguimiento:")
                // Permite mantener pulsado para copiar el código
                SelectionContainer {
                    Text(
                        text = solicitud.codigo,
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                ImagenQrSolicitud(solicitud = solicitud)

                Text(
                    text = "Escanéalo desde la lista de solicitudes para ver sus datos.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onAceptar) {
                Text("Aceptar")
            }
        }
    )
}