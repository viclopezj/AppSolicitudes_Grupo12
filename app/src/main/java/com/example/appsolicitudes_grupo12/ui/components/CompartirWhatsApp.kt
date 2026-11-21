package com.example.appsolicitudes_grupo12.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

// Botón "Compartir referencia por WhatsApp (simulado)" con un diálogo que muestra el mensaje.
// No abre WhatsApp: al enviar solo se llama a onEnviar.
@Composable
fun CompartirWhatsApp(
    mensaje: String,
    onEnviar: () -> Unit,
    modifier: Modifier = Modifier
) {
    var abierto by rememberSaveable { mutableStateOf(false) }

    OutlinedButton(
        onClick = { abierto = true },
        modifier = modifier.fillMaxWidth()
    ) {
        Text("Compartir referencia por WhatsApp (simulado)")
    }

    if (abierto) {
        AlertDialog(
            onDismissRequest = { abierto = false },
            title = { Text("Compartir por WhatsApp (simulado)") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "Este es el mensaje que se enviaría. No se abre WhatsApp real.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Surface(
                        shape = MaterialTheme.shapes.medium,
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Text(
                            text = mensaje,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        abierto = false
                        onEnviar()
                    }
                ) {
                    Text("Enviar (simulado)")
                }
            },
            dismissButton = {
                TextButton(onClick = { abierto = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}