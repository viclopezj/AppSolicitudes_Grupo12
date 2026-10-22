package com.example.appsolicitudes_grupo12.utils

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.widthIn
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** Evita que formularios y detalles se estiren demasiado en ventanas grandes. */
fun Modifier.adaptiveContentWidth(
    windowSizeClass: AppWindowSizeClass,
    compactMax: Dp = 600.dp,
    mediumMax: Dp = 720.dp,
    expandedMax: Dp = 960.dp
): Modifier {
    val maxWidth = when (windowSizeClass) {
        AppWindowSizeClass.Compact -> compactMax
        AppWindowSizeClass.Medium -> mediumMax
        AppWindowSizeClass.Expanded -> expandedMax
    }
    return widthIn(max = maxWidth).fillMaxWidth()
}
