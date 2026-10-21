package com.example.appsolicitudes_grupo12.ui.utils

import androidx.compose.runtime.Composable
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfoV2
import androidx.window.core.layout.WindowWidthSizeClass

/** Ancho disponible de la ventana de la app, no el tama\u00f1o físico del dispositivo. */
enum class AppWindowSizeClass {
    Compact,
    Medium,
    Expanded
}

/** Se actualiza cuando cambia el ancho por rotaci\u00f3n, pantalla dividida o redimensionamiento. */
@Composable
fun rememberAppWindowSizeClass(): AppWindowSizeClass {
    val widthClass = currentWindowAdaptiveInfoV2()
        .windowSizeClass
        .windowWidthSizeClass

    return when (widthClass) {
        WindowWidthSizeClass.COMPACT -> AppWindowSizeClass.Compact
        WindowWidthSizeClass.MEDIUM -> AppWindowSizeClass.Medium
        else -> AppWindowSizeClass.Expanded
    }
}
