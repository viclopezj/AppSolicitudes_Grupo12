package com.example.appsolicitudes_grupo12.ui.adaptive

import androidx.compose.material3.adaptive.currentWindowAdaptiveInfoV2
import androidx.compose.runtime.Composable
import androidx.window.core.layout.WindowSizeClass

// Ancho disponible de la ventana de la app (no el tamaño físico del dispositivo).
// Enum propio para que pantallas y navegación no dependan de la librería directamente.
enum class AppWindowSizeClass {
    Compact,   // menos de 600 dp: teléfonos en vertical
    Medium,    // 600 dp a menos de 840 dp: tablets pequeñas y plegables
    Expanded   // 840 dp o más: tablets y pantallas grandes
}

// Se actualiza sola al rotar, dividir la pantalla o redimensionar la ventana.
@Composable
fun rememberAppWindowSizeClass(): AppWindowSizeClass {
    val windowSizeClass = currentWindowAdaptiveInfoV2().windowSizeClass

    // Se revisa el punto de quiebre más grande primero
    return when {
        windowSizeClass.isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_EXPANDED_LOWER_BOUND) ->
            AppWindowSizeClass.Expanded

        windowSizeClass.isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_MEDIUM_LOWER_BOUND) ->
            AppWindowSizeClass.Medium

        else -> AppWindowSizeClass.Compact
    }
}