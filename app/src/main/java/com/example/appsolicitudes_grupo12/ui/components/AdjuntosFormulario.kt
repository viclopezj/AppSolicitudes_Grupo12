package com.example.appsolicitudes_grupo12.ui.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.appsolicitudes_grupo12.model.data.files.EvidenciaArchivos
import com.example.appsolicitudes_grupo12.model.solicitud.AdjuntoPendiente
import com.example.appsolicitudes_grupo12.model.solicitud.TiposAdjunto

// Sección del formulario para adjuntar imágenes y documentos adicionales (opcional).
// Solo muestra la lista y avisa qué se eligió o se quitó: el ViewModel copia los archivos.
@Composable
fun AdjuntosFormulario(
    adjuntos: List<AdjuntoPendiente>,
    cargando: Boolean,
    habilitado: Boolean,
    onElegir: (Uri) -> Unit,
    onQuitar: (AdjuntoPendiente) -> Unit,
    modifier: Modifier = Modifier
) {
    // Selector de archivos del sistema (no necesita permisos). uri es null si se cierra sin elegir.
    val lanzadorArchivo = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) onElegir(uri)
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "Documentos y archivos adicionales (opcional)",
            style = MaterialTheme.typography.titleSmall
        )

        adjuntos.forEach { adjunto ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = adjunto.nombre,
                        style = MaterialTheme.typography.bodyLarge,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = if (adjunto.tipo == TiposAdjunto.IMAGEN) "Imagen" else "Documento",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                TextButton(onClick = { onQuitar(adjunto) }, enabled = habilitado) {
                    Text("Quitar")
                }
            }
        }

        if (cargando) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                strokeWidth = 2.dp
            )
        }

        OutlinedButton(
            onClick = { lanzadorArchivo.launch(EvidenciaArchivos.TIPOS_ADJUNTO_PERMITIDOS) },
            enabled = habilitado && !cargando && adjuntos.size < EvidenciaArchivos.MAXIMO_ADJUNTOS,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Adjuntar documento o imagen")
        }

        Text(
            text = "Hasta ${EvidenciaArchivos.MAXIMO_ADJUNTOS} archivos de 5 MB: imágenes, PDF, TXT o Word. " +
                    "Usa solo archivos de prueba.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
