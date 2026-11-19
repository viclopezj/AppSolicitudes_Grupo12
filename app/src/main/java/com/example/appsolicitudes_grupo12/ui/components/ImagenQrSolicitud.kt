package com.example.appsolicitudes_grupo12.ui.components

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
import com.example.appsolicitudes_grupo12.model.data.local.SolicitudEntity
import com.example.appsolicitudes_grupo12.model.qr.CodigoQr

// Dibuja el QR de una solicitud. "tamano" es el lado del cuadro en pantalla.
@Composable
fun ImagenQrSolicitud(
    solicitud: SolicitudEntity,
    modifier: Modifier = Modifier,
    tamano: Dp = 200.dp
) {
    // Se vuelve a dibujar solo si la solicitud cambia (por ejemplo, tras editarla)
    val imagen = remember(solicitud) {
        val texto = CodigoQr.crearTexto(CodigoQr.desdeEntidad(solicitud))
        CodigoQr.dibujarQr(texto)
    }

    if (imagen != null) {
        // Fondo blanco fijo: el QR debe verse igual en modo oscuro o la cámara no lo lee
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
        Text(
            text = "No se pudo generar el código QR",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.error,
            modifier = modifier
        )
    }
}