package com.example.appsolicitudes_grupo12.state

import com.example.appsolicitudes_grupo12.model.qr.DatosQr

// Estados posibles del escáner QR (solo uno a la vez).
sealed class QrUiState {

    // La cámara espera un QR. Es el estado inicial.
    data object Escaneando : QrUiState()

    // Se leyó un QR de la app y se busca la solicitud en este teléfono.
    data object Buscando : QrUiState()

    // QR válido. idLocal es el id de la solicitud en este teléfono;
    // null si no existe aquí, no hay acceso o falló la consulta (se muestran solo los datos del QR).
    data class Leida(
        val datos: DatosQr,
        val idLocal: Int?
    ) : QrUiState()

    // El QR no es de una solicitud de esta app.
    data object NoValido : QrUiState()
}