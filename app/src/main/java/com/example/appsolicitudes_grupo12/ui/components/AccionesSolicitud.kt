package com.example.appsolicitudes_grupo12.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.appsolicitudes_grupo12.model.solicitud.EstadoSolicitud
import com.example.appsolicitudes_grupo12.model.solicitud.ReglasTransicion

// Botones de cambio de estado según el perfil y el estado actual.
// onCambiarEstado recibe el estado elegido y el mensaje (null si la acción no lo pide).
@Composable
fun AccionesSolicitud(
    rol: String,
    estadoActual: String,
    procesando: Boolean,
    onCambiarEstado: (EstadoSolicitud, String?) -> Unit,
    modifier: Modifier = Modifier
) {
    // Estado que espera un mensaje en el diálogo (null = diálogo cerrado)
    var destinoConMensaje by remember { mutableStateOf<EstadoSolicitud?>(null) }

    val estado = EstadoSolicitud.desdeTexto(estadoActual)
    // Asignar un técnico tiene su propio selector, por eso se excluye aquí
    val permitidos = if (estado == null) {
        emptyList()
    } else {
        ReglasTransicion.destinosPermitidos(rol, estado)
            .filter { it != EstadoSolicitud.ASIGNADA }
    }
    // El cliente responde con un solo botón: el destino lo decide el repositorio
    val destinos = if (rol == "cliente" && estado == EstadoSolicitud.PENDIENTE_INFORMACION) {
        permitidos.take(1)
    } else {
        permitidos
    }

    // Texto del botón y del diálogo según el perfil y el estado
    fun etiquetaAccion(destino: EstadoSolicitud): String {
        if (rol == "cliente") {
            if (estado == EstadoSolicitud.PENDIENTE_INFORMACION) {
                return "Responder solicitud de información"
            }
            if (estado == EstadoSolicitud.RESUELTA && destino == EstadoSolicitud.CERRADA) {
                return "Confirmar resolución"
            }
            if (estado == EstadoSolicitud.RESUELTA && destino == EstadoSolicitud.EN_REVISION) {
                return "Pedir reapertura"
            }
        }
        return when (destino) {
            EstadoSolicitud.EN_REVISION -> "Pasar a revisión"
            EstadoSolicitud.PENDIENTE_INFORMACION -> "Solicitar información"
            EstadoSolicitud.CANCELADA -> "Cancelar solicitud"
            EstadoSolicitud.CERRADA -> "Cerrar solicitud"
            EstadoSolicitud.EN_PROCESO -> "Iniciar trabajo"
            EstadoSolicitud.RESUELTA -> "Registrar resolución"
            else -> destino.texto
        }
    }

    // Pista del campo de texto del diálogo
    fun etiquetaMensaje(destino: EstadoSolicitud): String {
        if (rol == "cliente") {
            if (estado == EstadoSolicitud.PENDIENTE_INFORMACION) return "Escribe tu respuesta"
            if (destino == EstadoSolicitud.EN_REVISION) return "Motivo de la reapertura"
        }
        return when (destino) {
            EstadoSolicitud.CANCELADA -> "Motivo de la cancelación"
            EstadoSolicitud.PENDIENTE_INFORMACION -> "¿Qué información necesitas del cliente?"
            EstadoSolicitud.RESUELTA -> "Describe la resolución"
            else -> "Mensaje"
        }
    }

    if (destinos.isNotEmpty()) {
        Column(
            modifier = modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(text = "Acciones", style = MaterialTheme.typography.titleMedium)

            destinos.forEach { destino ->
                val alPulsar = {
                    if (estado != null && ReglasTransicion.requiereMotivo(estado, destino)) {
                        destinoConMensaje = destino
                    } else {
                        onCambiarEstado(destino, null)
                    }
                }

                if (destino == EstadoSolicitud.CANCELADA) {
                    OutlinedButton(
                        onClick = alPulsar,
                        enabled = !procesando,
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(etiquetaAccion(destino))
                    }
                } else {
                    Button(
                        onClick = alPulsar,
                        enabled = !procesando,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(etiquetaAccion(destino))
                    }
                }
            }
        }
    }

    destinoConMensaje?.let { destino ->
        DialogoMensaje(
            titulo = etiquetaAccion(destino),
            etiqueta = etiquetaMensaje(destino),
            onConfirmar = { texto ->
                destinoConMensaje = null
                onCambiarEstado(destino, texto)
            },
            onCancelar = { destinoConMensaje = null }
        )
    }
}

// Diálogo con un campo de texto obligatorio.
@Composable
private fun DialogoMensaje(
    titulo: String,
    etiqueta: String,
    onConfirmar: (String) -> Unit,
    onCancelar: () -> Unit
) {
    var texto by rememberSaveable { mutableStateOf("") }
    var mostrarError by rememberSaveable { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onCancelar,
        title = { Text(titulo) },
        text = {
            OutlinedTextField(
                value = texto,
                onValueChange = {
                    texto = it
                    mostrarError = false
                },
                label = { Text(etiqueta) },
                minLines = 3,
                maxLines = 5,
                isError = mostrarError,
                supportingText = { if (mostrarError) Text("Escribe un mensaje") },
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            TextButton(onClick = {
                if (texto.isBlank()) mostrarError = true else onConfirmar(texto.trim())
            }) {
                Text("Confirmar")
            }
        },
        dismissButton = {
            TextButton(onClick = onCancelar) { Text("Volver") }
        }
    )
}