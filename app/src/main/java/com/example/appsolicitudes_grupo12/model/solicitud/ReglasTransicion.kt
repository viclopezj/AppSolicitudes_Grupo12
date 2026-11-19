package com.example.appsolicitudes_grupo12.model.solicitud

import com.example.appsolicitudes_grupo12.model.solicitud.EstadoSolicitud.*

// Reglas del flujo de estados: quién puede pasar de qué estado a cuál.
// No usa la base de datos, por eso se puede probar sola.
object ReglasTransicion {

    private const val CLIENTE = "cliente"
    private const val ADMINISTRADOR = "administrador"
    private const val TECNICO = "tecnico"

    // Una regla: desde cualquiera de estos estados, hasta este estado, lo pueden hacer estos perfiles.
    private class Regla(
        val desde: Set<EstadoSolicitud>,
        val hasta: EstadoSolicitud,
        val roles: Set<String>
    )

    private val REGLAS = listOf(
        // Administrador
        Regla(setOf(RECIBIDA), EN_REVISION, setOf(ADMINISTRADOR)),
        Regla(setOf(RECIBIDA, EN_REVISION), ASIGNADA, setOf(ADMINISTRADOR)),
        Regla(setOf(EN_REVISION), PENDIENTE_INFORMACION, setOf(ADMINISTRADOR)),
        Regla(
            setOf(RECIBIDA, EN_REVISION, PENDIENTE_INFORMACION, ASIGNADA, EN_PROCESO),
            CANCELADA,
            setOf(ADMINISTRADOR)
        ),

        // Técnico
        Regla(setOf(ASIGNADA), EN_PROCESO, setOf(TECNICO)),
        Regla(setOf(ASIGNADA, EN_PROCESO), PENDIENTE_INFORMACION, setOf(TECNICO)),
        Regla(setOf(EN_PROCESO), RESUELTA, setOf(TECNICO)),

        // Cliente: al responder vuelve a En proceso (si ya hay técnico) o a En revisión
        Regla(setOf(PENDIENTE_INFORMACION), EN_PROCESO, setOf(CLIENTE)),
        Regla(setOf(PENDIENTE_INFORMACION), EN_REVISION, setOf(CLIENTE)),
        Regla(setOf(RESUELTA), CERRADA, setOf(CLIENTE, ADMINISTRADOR)),
        Regla(setOf(RESUELTA), EN_REVISION, setOf(CLIENTE))
    )

    // ¿Puede este perfil pasar la solicitud de un estado a otro?
    fun puedeCambiar(rol: String, desde: EstadoSolicitud, hasta: EstadoSolicitud): Boolean {
        return REGLAS.any { rol in it.roles && desde in it.desde && it.hasta == hasta }
    }

    // Estados a los que este perfil puede mover una solicitud que está en "desde".
    fun destinosPermitidos(rol: String, desde: EstadoSolicitud): List<EstadoSolicitud> {
        return REGLAS
            .filter { rol in it.roles && desde in it.desde }
            .map { it.hasta }
    }

    // Estado al que pasa una solicitud cuando el cliente responde la información pedida.
    fun destinoAlResponder(tieneTecnico: Boolean): EstadoSolicitud {
        return if (tieneTecnico) EN_PROCESO else EN_REVISION
    }

    // Cancelar exige escribir un motivo.
    fun requiereMotivo(hasta: EstadoSolicitud): Boolean {
        return hasta == CANCELADA
    }

    // Solo el administrador cambia la prioridad.
    fun puedeCambiarPrioridad(rol: String): Boolean {
        return rol == ADMINISTRADOR
    }

    // Asignar por primera vez: solo el administrador, desde Recibida o En revisión.
    fun puedeAsignar(rol: String, estado: EstadoSolicitud): Boolean {
        return rol == ADMINISTRADOR && (estado == RECIBIDA || estado == EN_REVISION)
    }

    // Reasignar: solo el administrador y solo mientras está Asignada o En proceso.
    fun puedeReasignar(rol: String, estado: EstadoSolicitud): Boolean {
        return rol == ADMINISTRADOR && (estado == ASIGNADA || estado == EN_PROCESO)
    }
}