package com.example.appsolicitudes_grupo12.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier

// Chip de filtro con un menú de opciones.
// opciones: pares (valor, texto). textoSinFiltro = texto de la opción que quita el filtro
// (null = no hay esa opción, como en el orden). resaltado marca el chip como activo.
@Composable
fun <T> ChipFiltro(
    titulo: String,
    seleccion: T?,
    opciones: List<Pair<T, String>>,
    onElegir: (T?) -> Unit,
    modifier: Modifier = Modifier,
    textoSinFiltro: String? = "Todos",
    resaltado: Boolean = seleccion != null
) {
    var menuAbierto by remember { mutableStateOf(false) }
    val textoElegido = opciones.firstOrNull { it.first == seleccion }?.second
        ?: textoSinFiltro
        ?: ""

    Box(modifier = modifier) {
        FilterChip(
            selected = resaltado,
            onClick = { menuAbierto = true },
            label = { Text("$titulo: $textoElegido ▾") }
        )

        DropdownMenu(
            expanded = menuAbierto,
            onDismissRequest = { menuAbierto = false }
        ) {
            if (textoSinFiltro != null) {
                DropdownMenuItem(
                    text = { Text(textoSinFiltro) },
                    onClick = {
                        onElegir(null)
                        menuAbierto = false
                    }
                )
            }
            opciones.forEach { (valor, texto) ->
                DropdownMenuItem(
                    text = { Text(texto) },
                    onClick = {
                        onElegir(valor)
                        menuAbierto = false
                    }
                )
            }
        }
    }
}