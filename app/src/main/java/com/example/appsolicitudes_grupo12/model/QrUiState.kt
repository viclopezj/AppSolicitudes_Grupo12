package com.example.appsolicitudes_grupo12.model

// Representa los estados posibles de la pantalla del escáner QR.
// Igual que ListaSolicitudesUiState y DetalleSolicitudUiState: es una "sealed class",
// así que solo puede ser UNO de estos estados a la vez y la pantalla siempre sabe qué mostrar.
sealed class QrUiState {

    // La cámara está activa esperando que aparezca un código QR.
    // Es el estado inicial y también al que se vuelve para escanear otro.
    data object Escaneando : QrUiState()

    // Se leyó un QR de solicitud y se revisa si esa solicitud también existe en este teléfono.
    // La pantalla muestra un indicador de carga (estado Loading).
    data object Buscando : QrUiState()

    // El QR es válido (estado Success). Trae los datos que venían dentro del QR.
    // "idLocal" dice si la solicitud también está guardada en ESTE teléfono:
    //   - idLocal = un número -> existe aquí, así que se puede abrir su detalle
    //   - idLocal = null      -> solo se conocen los datos del QR (viene de otro teléfono)
    // Si la consulta a Room falla, también queda en null: los datos del QR
    // se pueden mostrar igual, así que no hace falta un estado de error aparte.
    data class Leida(
        val datos: SolicitudQr,
        val idLocal: Int?
    ) : QrUiState()

    // Se leyó un QR, pero no es de una solicitud de esta app (por ejemplo, el QR de un sitio web).
    // La pantalla muestra un mensaje amable y permite escanear otro (estado Error).
    data object NoValido : QrUiState()
}