package com.example.appsolicitudes_grupo12.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.appsolicitudes_grupo12.model.data.local.ComentarioSolicitudEntity

// Caja plegable con los avances del técnico y, si corresponde, el formulario para registrar uno.
@Composable
fun EvidenciasTrabajo(
    avances: List<ComentarioSolicitudEntity>,
    puedeRegistrar: Boolean,
    solicitudId: Int,
    usuarioId: Int,
    rol: String,
    onAvanceEnviado: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Solo aparece si hay avances o el técnico puede registrar uno
    if (avances.isEmpty() && !puedeRegistrar) return

    var abierta by rememberSaveable(solicitudId) { mutableStateOf(false) }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Encabezado: al tocarlo se abre o se cierra la caja
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(
                        onClickLabel = if (abierta) "Cerrar evidencias" else "Abrir evidencias"
                    ) { abierta = !abierta }
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Evidencias del trabajo (${avances.size})",
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = if (abierta) "▴" else "▾",
                    style = MaterialTheme.typography.titleMedium
                )
            }

            AnimatedVisibility(visible = abierta) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ComentariosSolicitud(
                        comentarios = avances,
                        titulo = null,
                        textoVacio = "Aún no hay avances registrados."
                    )
                    if (puedeRegistrar) {
                        AvanceSolicitud(
                            id = solicitudId,
                            usuarioId = usuarioId,
                            rol = rol,
                            onEnviado = onAvanceEnviado
                        )
                    }
                }
            }
        }
    }
}