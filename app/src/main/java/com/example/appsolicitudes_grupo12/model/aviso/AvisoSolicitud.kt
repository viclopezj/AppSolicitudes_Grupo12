package com.example.appsolicitudes_grupo12.model.aviso

import com.example.appsolicitudes_grupo12.model.solicitud.EstadoSolicitud
import com.example.appsolicitudes_grupo12.model.solicitud.EstadoSolicitud.*
import com.example.appsolicitudes_grupo12.model.solicitud.PrioridadSolicitud

// Perfil al que va dirigido el aviso (se muestra en el título).
enum class DestinatarioAviso(val etiqueta: String) {
    CLIENTE("el cliente"),
    ADMINISTRADOR("el administrador"),
    TECNICO("el técnico")
}

// Un aviso listo para mostrar.
data class AvisoSolicitud(
    val titulo: String,
    val mensaje: String,
    val destinatario: DestinatarioAviso
)

// Arma el aviso de cada evento. No usa Android, por eso se puede probar sola.
object Avisos {

    fun nuevaSolicitud(codigo: String) = aviso(
        DestinatarioAviso.ADMINISTRADOR,
        "Se recibió la solicitud $codigo."
    )

    fun asignada(codigo: String) = aviso(
        DestinatarioAviso.TECNICO,
        "Te asignaron la solicitud $codigo."
    )

    fun prioridad(codigo: String, prioridad: PrioridadSolicitud) = aviso(
        DestinatarioAviso.CLIENTE,
        "La prioridad de tu solicitud $codigo ahora es ${prioridad.texto}."
    )

    fun pendienteInformacion(codigo: String) = aviso(
        DestinatarioAviso.CLIENTE,
        "Tu solicitud $codigo está Pendiente de información. Revisa el detalle y responde."
    )

    fun nuevoComentario(codigo: String) = aviso(
        DestinatarioAviso.CLIENTE,
        "Hay un nuevo comentario en tu solicitud $codigo."
    )

    fun resuelta(codigo: String) = aviso(
        DestinatarioAviso.CLIENTE,
        "Tu solicitud $codigo fue resuelta. Confirma la resolución o pide reapertura."
    )

    fun cerrada(codigo: String) = aviso(
        DestinatarioAviso.TECNICO,
        "La solicitud $codigo fue cerrada."
    )

    fun reabierta(codigo: String) = aviso(
        DestinatarioAviso.ADMINISTRADOR,
        "El cliente pidió reabrir la solicitud $codigo."
    )

    // Aviso que corresponde a un cambio de estado; null si ese cambio no avisa.
    fun porCambioDeEstado(
        codigo: String,
        desde: EstadoSolicitud,
        hasta: EstadoSolicitud
    ): AvisoSolicitud? = when {
        hasta == PENDIENTE_INFORMACION -> pendienteInformacion(codigo)
        hasta == RESUELTA -> resuelta(codigo)
        hasta == CERRADA -> cerrada(codigo)
        desde == RESUELTA && hasta == EN_REVISION -> reabierta(codigo)
        else -> null
    }

    private fun aviso(destinatario: DestinatarioAviso, mensaje: String) = AvisoSolicitud(
        titulo = "Aviso para ${destinatario.etiqueta}",
        mensaje = mensaje,
        destinatario = destinatario
    )
}