package com.example.appsolicitudes_grupo12.model.aviso

// Quien muestra los avisos. El repositorio solo conoce esta interfaz.
interface Notificador {
    fun publicar(aviso: AvisoSolicitud)
}

// No hace nada: se usa por defecto y en los tests.
object NotificadorNulo : Notificador {
    override fun publicar(aviso: AvisoSolicitud) = Unit
}