package com.example.appsolicitudes_grupo12.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.appsolicitudes_grupo12.state.CatalogoUiState

// Selector del servicio relacionado:
//   - Catálogo cargado: lista desplegable.
//   - Cargando: campo bloqueado con indicador.
//   - Error o catálogo vacío: campo de texto manual, mensaje y botón "Reintentar".
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectorServicio(
    servicio: String,
    obligatorio: Boolean,
    error: String?,
    catalogo: CatalogoUiState,
    onServicioChange: (String) -> Unit,
    onReintentar: () -> Unit,
    modifier: Modifier = Modifier
) {
    val etiqueta = if (obligatorio) "Servicio relacionado *" else "Servicio relacionado (opcional)"
    var menuAbierto by remember { mutableStateOf(false) }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        when (catalogo) {
            is CatalogoUiState.Success -> {
                ExposedDropdownMenuBox(
                    expanded = menuAbierto,
                    onExpandedChange = { menuAbierto = it }
                ) {
                    OutlinedTextField(
                        value = servicio,
                        onValueChange = {},          // no se escribe: solo se elige de la lista
                        readOnly = true,
                        label = { Text(etiqueta) },
                        leadingIcon = { Icon(Icons.Default.Build, contentDescription = null) },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = menuAbierto)
                        },
                        isError = error != null,
                        supportingText = { Text(error ?: "Catálogo de prueba (datos ficticios)") },
                        modifier = Modifier
                            .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                            .fillMaxWidth()
                    )

                    ExposedDropdownMenu(
                        expanded = menuAbierto,
                        onDismissRequest = { menuAbierto = false }
                    ) {
                        // Si el servicio es opcional, se puede dejar sin elegir
                        if (!obligatorio) {
                            DropdownMenuItem(
                                text = { Text("Sin servicio relacionado") },
                                onClick = {
                                    onServicioChange("")
                                    menuAbierto = false
                                }
                            )
                        }

                        catalogo.servicios.forEach { item ->
                            DropdownMenuItem(
                                text = {
                                    Column {
                                        Text("${item.id}. ${item.servicio}")
                                        Text(
                                            text = "${item.categoria} · ${item.descripcion}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                },
                                onClick = {
                                    onServicioChange(item.servicio)
                                    menuAbierto = false
                                }
                            )
                        }
                    }
                }
            }

            CatalogoUiState.Loading -> {
                OutlinedTextField(
                    value = servicio,
                    onValueChange = {},
                    enabled = false,
                    label = { Text(etiqueta) },
                    leadingIcon = { Icon(Icons.Default.Build, contentDescription = null) },
                    trailingIcon = {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp
                        )
                    },
                    supportingText = { Text("Cargando catálogo…") },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            else -> {
                // Respaldo: sin catálogo se escribe el servicio a mano
                OutlinedTextField(
                    value = servicio,
                    onValueChange = onServicioChange,
                    label = { Text(etiqueta) },
                    leadingIcon = { Icon(Icons.Default.Build, contentDescription = null) },
                    trailingIcon = { if (error != null) IconoError() },
                    singleLine = true,
                    isError = error != null,
                    supportingText = { error?.let { Text(it) } },
                    modifier = Modifier.fillMaxWidth()
                )

                val aviso = if (catalogo is CatalogoUiState.Error) {
                    catalogo.mensaje + " Puedes escribir el servicio a mano."
                } else {
                    "El catálogo no tiene servicios. Escribe el servicio a mano."
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = aviso,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f)
                    )
                    TextButton(onClick = onReintentar) { Text("Reintentar") }
                }
            }
        }
    }
}