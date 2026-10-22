package com.example.appsolicitudes_grupo12.utils

import androidx.compose.material3.adaptive.currentWindowAdaptiveInfoV2
import androidx.compose.runtime.Composable
import androidx.window.core.layout.WindowSizeClass

// Ancho disponible de la ventana de la app, no el tamaño físico del dispositivo.
// Se mantiene este enum propio para que el resto de la app (pantallas y navegación)
// no dependa directamente de la librería de Window Size Classes.
enum class AppWindowSizeClass {
    Compact,   // menos de 600 dp: teléfonos en vertical
    Medium,    // de 600 dp a menos de 840 dp: tablets pequeñas, teléfonos plegables
    Expanded   // 840 dp o más: tablets y pantallas grandes
}

// Se actualiza sola cuando cambia el ancho de la ventana
// (rotación, pantalla dividida o redimensionamiento).
@Composable
fun rememberAppWindowSizeClass(): AppWindowSizeClass {
    val windowSizeClass = currentWindowAdaptiveInfoV2().windowSizeClass

    // Forma actual: se pregunta si el ancho alcanza cada punto de quiebre,
    // empezando por el más grande.
    return when {
        windowSizeClass.isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_EXPANDED_LOWER_BOUND) ->
            AppWindowSizeClass.Expanded

        windowSizeClass.isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_MEDIUM_LOWER_BOUND) ->
            AppWindowSizeClass.Medium

        else -> AppWindowSizeClass.Compact
    }
}