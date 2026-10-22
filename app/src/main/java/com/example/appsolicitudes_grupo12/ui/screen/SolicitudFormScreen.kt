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
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
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
import com.example.appsolicitudes_grupo12.utils.CameraPermissionHelper
import com.example.appsolicitudes_grupo12.utils.EvidenciaUtils
import com.example.appsolicitudes_grupo12.utils.AppWindowSizeClass
import com.example.appsolicitudes_grupo12.utils.adaptiveContentWidth
import com.example.appsolicitudes_grupo12.viewModel.SolicitudViewModel
import java.io.File

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
    windowSizeClass: AppWindowSizeClass = AppWindowSizeClass.Compact,
    onGuardado: () -> Unit = {},
    onVolver: () -> Unit = {}
) {
    // La pantalla observa el estado: cada cambio redibuja lo necesario
    val estado by viewModel.estado.collectAsState()
    val context = LocalContext.current

    // Controla los mensajes emergentes (Snackbar) en la parte inferior.
    // Se usa para errores de guardado y errores de la foto.
    val snackbarHostState = remember { SnackbarHostState() }

    // Recuerda si el menú de categorías está abierto o cerrado
    var menuCategoriaAbierto by remember { mutableStateOf(false) }

    // Ruta del archivo donde la cámara está escribiendo la foto. Mientras la cámara está abierta
    // la app puede reiniciarse, por eso se usa rememberSaveable: así no se pierde el dato.
    // Se guarda como texto (la ruta) porque un File no se puede guardar ahí.
    var rutaFotoPendiente by rememberSaveable { mutableStateOf<String?>(null) }

    // ---------- Lanzadores de cámara, galería y permiso ----------

    // Abre la app de cámara del teléfono. Al volver, "exito" dice si se tomó la foto.
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

    // Prepara un archivo vacío y abre la cámara para que escriba la foto en él.
    val abrirCamara = {
        val archivo = viewModel.crearArchivoParaCamara()
        rutaFotoPendiente = archivo.absolutePath
        lanzadorCamara.launch(EvidenciaUtils.uriParaCamara(context, archivo))
    }

    // Abre el cuadro "¿Permitir usar la cámara?". Si la persona acepta, se abre la cámara.
    val lanzadorPermiso = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { concedido ->
        if (concedido) abrirCamara() else viewModel.onPermisoCamaraDenegado()
    }

    // Abre el selector de fotos del sistema. No necesita permisos.
    // "uri" es null si la persona cierra el selector sin elegir nada.
    val lanzadorGaleria = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) viewModel.onFotoElegida(uri)
    }

    // Se ejecuta cuando aparece un error de guardado: lo muestra y luego lo descarta
    LaunchedEffect(estado.errorGuardado) {
        estado.errorGuardado?.let { mensaje ->
            snackbarHostState.showSnackbar(mensaje)
            viewModel.descartarErrorGuardado()
        }
    }

    // Se ejecuta cuando aparece un error de la foto: lo muestra y luego lo descarta
    LaunchedEffect(estado.errorFoto) {
        estado.errorFoto?.let { mensaje ->
            snackbarHostState.showSnackbar(mensaje)
            viewModel.descartarErrorFoto()
        }
    }

    // ---------- Aviso de éxito: cuadro de diálogo con el código y su QR ----------
    // Aparece solo cuando se guardó bien. No se cierra tocando fuera ni con
    // "atrás": la persona debe pulsar Aceptar, así no se pierde su código.
    // solicitudCreada trae la solicitud recién guardada; de ahí salen el código y el QR.
    val solicitudCreada = estado.solicitudCreada
    if (estado.guardadoExitosamente && solicitudCreada != null) {
        AlertDialog(
            onDismissRequest = {},
            properties = DialogProperties(
                dismissOnBackPress = false,
                dismissOnClickOutside = false
            ),
            title = { Text("Solicitud registrada") },
            text = {
                // Con scroll: en pantallas pequeñas o en horizontal el QR no se corta
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Guarda tu código de seguimiento:")
                    // SelectionContainer permite mantener pulsado para copiar el código
                    SelectionContainer {
                        Text(
                            text = solicitudCreada.codigo,
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    // QR con los datos de la solicitud, para consultarla con la cámara.
                    // También se puede ver después en el detalle de la solicitud.
                    ImagenQrSolicitud(solicitud = solicitudCreada)

                    Text(
                        text = "Escanéalo desde la lista de solicitudes para ver sus datos.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
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
        containerColor = MaterialTheme.colorScheme.background,
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
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingInterno ->

        Box(
            modifier = Modifier.fillMaxSize().padding(paddingInterno).imePadding(),
            contentAlignment = Alignment.TopCenter
        ) {
            // El ancho se limita en ventanas amplias para mantener legible el formulario.
            // Column con scroll: si el teclado ocupa espacio, se puede deslizar
            Column(
                modifier = Modifier
                    .adaptiveContentWidth(windowSizeClass, 600.dp, 680.dp, 720.dp)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {

            // ---------- Título ----------
            OutlinedTextField(
                value = estado.titulo,                       // lee del ViewModel
                onValueChange = viewModel::onTituloChange,   // escribe en el ViewModel
                label = { Text("Título *") },
                leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) },
                trailingIcon = { if (estado.errores.titulo != null) IconoError() },
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
                leadingIcon = { Icon(Icons.Default.Create, contentDescription = null) },
                trailingIcon = { if (estado.errores.descripcion != null) IconoError() },
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
                    leadingIcon = {
                        Icon(Icons.AutoMirrored.Filled.List, contentDescription = null)
                    },
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
                leadingIcon = { Icon(Icons.Default.Build, contentDescription = null) },
                trailingIcon = { if (estado.errores.servicio != null) IconoError() },
                singleLine = true,
                isError = estado.errores.servicio != null,
                supportingText = {
                    estado.errores.servicio?.let { Text(it) }
                },
                modifier = Modifier.fillMaxWidth()
            )

            // ---------- Evidencia (foto opcional) ----------
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Evidencia (opcional)",
                    style = MaterialTheme.typography.titleSmall
                )

                // Vista previa: Loading mientras se copia la foto, la foto si ya hay una.
                // AnimatedVisibility la despliega con fundido al elegir la foto y la pliega al quitarla.
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
                        // AsyncImage (de Coil) lee el archivo en segundo plano y lo reduce
                        // al tamaño necesario, para que una foto grande no gaste memoria.
                        AsyncImage(
                            model = estado.evidenciaUri,
                            contentDescription = "Foto de evidencia de la solicitud",
                            contentScale = ContentScale.Crop,   // llena el recuadro recortando los bordes
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                                .clip(RoundedCornerShape(12.dp))
                        )
                    }
                }

                // Los dos botones comparten el ancho. Se desactivan mientras se copia o se guarda.
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = {
                            // Si ya hay permiso se abre la cámara; si no, se pide primero
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

                // Recordatorio de la regla del caso: solo datos ficticios
                Text(
                    text = "Usa solo imágenes de prueba, sin datos personales.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // ---------- Botón Registrar ----------
            Button(
                onClick = { viewModel.guardar() },
                // se desactiva mientras guarda o mientras se copia una foto
                enabled = !estado.guardando && !estado.cargandoFoto,
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
}
