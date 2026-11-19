package com.example.appsolicitudes_grupo12.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.example.appsolicitudes_grupo12.model.data.local.ComentarioSolicitudEntity

// Comentarios de seguimiento. La lista ya llega filtrada según el perfil.
@Composable
fun ComentariosSolicitud(
    comentarios: List<ComentarioSolicitudEntity>,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(text = "Comentarios", style = MaterialTheme.typography.titleMedium)

        if (comentarios.isEmpty()) {
            Text(
                text = "Aún no hay comentarios.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            comentarios.forEach { comentario -> TarjetaComentario(comentario) }
        }
    }
}

// Un comentario: autor, texto, foto opcional y etiqueta si es interno.
@Composable
private fun TarjetaComentario(comentario: ComentarioSolicitudEntity) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${comentario.usuarioNombre} (${etiquetaRol(comentario.rol)})",
                    style = MaterialTheme.typography.titleSmall
                )
                if (comentario.esInterno) {
                    Text(
                        text = "Interno",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onTertiaryContainer,
                        modifier = Modifier
                            .background(
                                MaterialTheme.colorScheme.tertiaryContainer,
                                RoundedCornerShape(50)
                            )
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            }

            Text(text = comentario.texto, style = MaterialTheme.typography.bodyMedium)

            comentario.evidenciaUri?.let { uri ->
                AsyncImage(
                    model = uri,
                    contentDescription = "Evidencia del comentario",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                        .clip(RoundedCornerShape(12.dp))
                )
            }

            Text(
                text = formatearFecha(comentario.fecha),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}