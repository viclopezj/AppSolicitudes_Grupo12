package com.example.appsolicitudes_grupo12.ui.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.appsolicitudes_grupo12.data.local.SolicitudEntity
import com.example.appsolicitudes_grupo12.utils.QrUtils

// Dibuja el código QR de una solicitud. Dentro del QR viaja el código, título, descripción,
// categoría, servicio, estado y fecha (la descripción se corta a 300 caracteres).
// "tamano" es el lado del cuadro en pantalla.
@Composable
fun ImagenQrSolicitud(
    solicitud: SolicitudEntity,
    modifier: Modifier = Modifier,
    tamano: Dp = 200.dp
) {
    // remember evita volver a dibujar el QR en cada redibujado de la pantalla.
    // Si la solicitud cambia (por ejemplo, tras editarla), se dibuja de nuevo con los datos nuevos.
    val imagen = remember(solicitud) {
        val texto = QrUtils.crearTexto(QrUtils.desdeEntidad(solicitud))
        QrUtils.dibujarQr(texto)
    }

    if (imagen != null) {
        // Fondo blanco fijo: el QR es negro sobre blanco y debe verse igual
        // también en modo oscuro, o la cámara no lo lee.
        Box(
            modifier = modifier
                .background(Color.White)
                .padding(8.dp)
        ) {
            Image(
                bitmap = imagen.asImageBitmap(),
                contentDescription = "Código QR de la solicitud ${solicitud.codigo}",
                modifier = Modifier.size(tamano)
            )
        }
    } else {
        // Si no se pudo dibujar, se avisa en vez de dejar un espacio vacío
        Text(
            text = "No se pudo generar el código QR",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.error,
            modifier = modifier
        )
    }
}