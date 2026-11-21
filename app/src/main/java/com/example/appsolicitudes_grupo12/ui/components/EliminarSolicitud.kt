package com.example.appsolicitudes_grupo12.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier

// Botón "Eliminar solicitud" con diálogo de confirmación. Solo se muestra si la regla lo permite.
@Composable
fun EliminarSolicitud(
    codigo: String,
    esAdministrador: Boolean,
    procesando: Boolean,
    onConfirmar: () -> Unit,
    modifier: Modifier = Modifier
) {
    var confirmando by rememberSaveable { mutableStateOf(false) }

    OutlinedButton(
        onClick = { confirmando = true },
        enabled = !procesando,
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = MaterialTheme.colorScheme.error
        ),
        modifier = modifier.fillMaxWidth()
    ) {
        Text("Eliminar solicitud")
    }

    if (confirmando) {
        AlertDialog(
            onDismissRequest = { confirmando = false },
            title = { Text("¿Eliminar la solicitud $codigo?") },
            text = {
                Text(
                    if (esAdministrador) {
                        "Dejará de mostrarse en la app y se avisará al cliente. " +
                                "Su historial y evidencias quedan guardados."
                    } else {
                        "Dejará de mostrarse en la app. " +
                                "Su historial y evidencias quedan guardados."
                    }
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        confirmando = false
                        onConfirmar()
                    }
                ) {
                    Text("Eliminar")
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmando = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}