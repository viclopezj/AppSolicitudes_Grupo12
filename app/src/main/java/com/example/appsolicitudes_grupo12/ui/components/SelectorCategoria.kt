package com.example.appsolicitudes_grupo12.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
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
import androidx.compose.ui.unit.dp
import com.example.appsolicitudes_grupo12.state.CatalogoUiState

// Selector de la categoría de la solicitud:
//   - Catálogo cargado: lista desplegable.
//   - Cargando: campo bloqueado con indicador.
//   - Error o catálogo vacío: campo de texto manual, mensaje y botón "Reintentar".
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectorCategoria(
    categoria: String,
    error: String?,
    catalogo: CatalogoUiState,
    onCategoriaChange: (String) -> Unit,
    onReintentar: () -> Unit,
    modifier: Modifier = Modifier
) {
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
                        value = categoria,
                        onValueChange = {},          // no se escribe: solo se elige de la lista
                        readOnly = true,
                        label = { Text("Categoría *") },
                        leadingIcon = {
                            Icon(Icons.AutoMirrored.Filled.List, contentDescription = null)
                        },
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
                        catalogo.categorias.forEach { item ->
                            DropdownMenuItem(
                                text = { Text(item.nombre) },
                                onClick = {
                                    onCategoriaChange(item.nombre)
                                    menuAbierto = false
                                }
                            )
                        }
                    }
                }
            }

            CatalogoUiState.Loading -> {
                OutlinedTextField(
                    value = categoria,
                    onValueChange = {},
                    enabled = false,
                    label = { Text("Categoría *") },
                    leadingIcon = {
                        Icon(Icons.AutoMirrored.Filled.List, contentDescription = null)
                    },
                    trailingIcon = {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp
                        )
                    },
                    isError = error != null,
                    supportingText = { Text(error ?: "Cargando categorías…") },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            else -> {
                // Respaldo: sin catálogo se escribe la categoría a mano
                OutlinedTextField(
                    value = categoria,
                    onValueChange = onCategoriaChange,
                    label = { Text("Categoría *") },
                    leadingIcon = {
                        Icon(Icons.AutoMirrored.Filled.List, contentDescription = null)
                    },
                    trailingIcon = { if (error != null) IconoError() },
                    singleLine = true,
                    isError = error != null,
                    supportingText = { error?.let { Text(it) } },
                    modifier = Modifier.fillMaxWidth()
                )

                val aviso = if (catalogo is CatalogoUiState.Error) {
                    catalogo.mensaje + " Puedes escribir la categoría a mano."
                } else {
                    "El catálogo no tiene categorías. Escribe la categoría a mano."
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