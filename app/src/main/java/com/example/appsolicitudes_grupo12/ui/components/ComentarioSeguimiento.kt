package com.example.appsolicitudes_grupo12.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.appsolicitudes_grupo12.viewmodel.AvanceSolicitudViewModel

// Formulario del administrador para dejar un comentario de seguimiento (sin foto).
// Reutiliza el ViewModel de avances. onEnviado refresca el detalle al guardar.
@Composable
fun ComentarioSeguimiento(
    id: Int,
    usuarioId: Int,
    rol: String,
    onEnviado: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AvanceSolicitudViewModel = viewModel(
        factory = AvanceSolicitudViewModel.factory(LocalContext.current, id, usuarioId, rol)
    )
) {
    val estado by viewModel.estado.collectAsState()

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(text = "Comentario de seguimiento", style = MaterialTheme.typography.titleMedium)

        OutlinedTextField(
            value = estado.texto,
            onValueChange = viewModel::onTextoChange,
            label = { Text("Comentario") },
            minLines = 3,
            maxLines = 6,
            enabled = !estado.enviando,
            isError = estado.errorTexto != null,
            supportingText = { estado.errorTexto?.let { Text(it) } },
            modifier = Modifier.fillMaxWidth()
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Switch(
                checked = estado.esInterno,
                onCheckedChange = viewModel::onInternoChange,
                enabled = !estado.enviando
            )
            Text(
                text = "Comentario interno (el cliente no lo ve)",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(1f)
            )
        }

        estado.error?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error
            )
        }
        estado.mensaje?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }

        Button(
            onClick = { viewModel.enviar(onEnviado) },
            enabled = !estado.enviando,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Registrar comentario")
        }
    }
}