package com.example.appsolicitudes_grupo12.model.solicitud

import com.example.appsolicitudes_grupo12.model.solicitud.EstadoSolicitud.*
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

// Pruebas de las reglas de transición: quién puede pasar de qué estado a cuál.
class ReglasTransicionTest : FunSpec({

    val cliente = "cliente"
    val admin = "administrador"
    val tecnico = "tecnico"

    test("solo el administrador pasa Recibida a En revisión") {
        ReglasTransicion.puedeCambiar(admin, RECIBIDA, EN_REVISION) shouldBe true
        ReglasTransicion.puedeCambiar(tecnico, RECIBIDA, EN_REVISION) shouldBe false
        ReglasTransicion.puedeCambiar(cliente, RECIBIDA, EN_REVISION) shouldBe false
    }

    test("solo el técnico inicia el trabajo y resuelve") {
        ReglasTransicion.puedeCambiar(tecnico, ASIGNADA, EN_PROCESO) shouldBe true
        ReglasTransicion.puedeCambiar(tecnico, EN_PROCESO, RESUELTA) shouldBe true
        ReglasTransicion.puedeCambiar(admin, EN_PROCESO, RESUELTA) shouldBe false
        ReglasTransicion.puedeCambiar(cliente, ASIGNADA, EN_PROCESO) shouldBe false
    }

    test("el cliente no cierra directamente: solo confirma una solicitud Resuelta") {
        ReglasTransicion.puedeCambiar(cliente, RECIBIDA, CERRADA) shouldBe false
        ReglasTransicion.puedeCambiar(cliente, EN_PROCESO, CERRADA) shouldBe false
        ReglasTransicion.puedeCambiar(cliente, RESUELTA, CERRADA) shouldBe true
    }

    test("el cliente puede pedir reapertura solo desde Resuelta") {
        ReglasTransicion.puedeCambiar(cliente, RESUELTA, EN_REVISION) shouldBe true
        ReglasTransicion.puedeCambiar(cliente, CERRADA, EN_REVISION) shouldBe false
    }

    test("Cerrada y Cancelada no tienen salida para ningún perfil") {
        listOf(cliente, admin, tecnico).forEach { rol ->
            ReglasTransicion.destinosPermitidos(rol, CERRADA).isEmpty() shouldBe true
            ReglasTransicion.destinosPermitidos(rol, CANCELADA).isEmpty() shouldBe true
        }
    }

    test("al responder información vuelve a En proceso si hay técnico, si no a En revisión") {
        ReglasTransicion.destinoAlResponder(tieneTecnico = true) shouldBe EN_PROCESO
        ReglasTransicion.destinoAlResponder(tieneTecnico = false) shouldBe EN_REVISION
    }

    test("exigen mensaje: cancelar, pedir información, resolver, responder y reabrir") {
        ReglasTransicion.requiereMotivo(EN_REVISION, CANCELADA) shouldBe true
        ReglasTransicion.requiereMotivo(EN_REVISION, PENDIENTE_INFORMACION) shouldBe true
        ReglasTransicion.requiereMotivo(EN_PROCESO, RESUELTA) shouldBe true
        ReglasTransicion.requiereMotivo(PENDIENTE_INFORMACION, EN_REVISION) shouldBe true
        ReglasTransicion.requiereMotivo(RESUELTA, EN_REVISION) shouldBe true
        // Un cambio simple no pide mensaje
        ReglasTransicion.requiereMotivo(RECIBIDA, EN_REVISION) shouldBe false
    }

    test("prioridad y asignación son solo del administrador") {
        ReglasTransicion.puedeCambiarPrioridad(admin) shouldBe true
        ReglasTransicion.puedeCambiarPrioridad(cliente) shouldBe false
        ReglasTransicion.puedeCambiarPrioridad(tecnico) shouldBe false
        ReglasTransicion.puedeAsignar(admin, EN_REVISION) shouldBe true
        ReglasTransicion.puedeAsignar(tecnico, EN_REVISION) shouldBe false
        ReglasTransicion.puedeReasignar(admin, EN_PROCESO) shouldBe true
        ReglasTransicion.puedeReasignar(admin, RECIBIDA) shouldBe false
    }

    test("el técnico registra avances solo En proceso; edición y eliminación se limitan por estado") {        ReglasTransicion.puedeRegistrarAvance(tecnico, ASIGNADA) shouldBe false
        ReglasTransicion.puedeRegistrarAvance(tecnico, EN_PROCESO) shouldBe true
        ReglasTransicion.puedeRegistrarAvance(admin, EN_PROCESO) shouldBe false

        ReglasTransicion.puedeEditar(cliente, RECIBIDA) shouldBe true
        ReglasTransicion.puedeEditar(admin, PENDIENTE_INFORMACION) shouldBe true
        ReglasTransicion.puedeEditar(cliente, ASIGNADA) shouldBe false
        ReglasTransicion.puedeEditar(cliente, RESUELTA) shouldBe false
        ReglasTransicion.puedeEditar(tecnico, RECIBIDA) shouldBe false

        // Comentario de seguimiento: solo el administrador y no en estados finales
        ReglasTransicion.puedeComentar(admin, RECIBIDA) shouldBe true
        ReglasTransicion.puedeComentar(admin, RESUELTA) shouldBe true
        ReglasTransicion.puedeComentar(admin, CERRADA) shouldBe false
        ReglasTransicion.puedeComentar(admin, CANCELADA) shouldBe false
        ReglasTransicion.puedeComentar(cliente, RECIBIDA) shouldBe false
        ReglasTransicion.puedeComentar(tecnico, EN_PROCESO) shouldBe false

        // Eliminación lógica: solo Cerrada o Cancelada, y nunca el técnico
        ReglasTransicion.puedeEliminar(cliente, CERRADA) shouldBe true
        ReglasTransicion.puedeEliminar(admin, CANCELADA) shouldBe true
        ReglasTransicion.puedeEliminar(cliente, RESUELTA) shouldBe false
        ReglasTransicion.puedeEliminar(admin, EN_PROCESO) shouldBe false
        ReglasTransicion.puedeEliminar(tecnico, CERRADA) shouldBe false

        // Adjuntar documentos: igual que editar, y nunca el técnico
        ReglasTransicion.puedeAdjuntar(cliente, PENDIENTE_INFORMACION) shouldBe true
        ReglasTransicion.puedeAdjuntar(admin, RECIBIDA) shouldBe true
        ReglasTransicion.puedeAdjuntar(cliente, EN_PROCESO) shouldBe false
        ReglasTransicion.puedeAdjuntar(tecnico, RECIBIDA) shouldBe false
    }

    test("una solicitud está atrasada solo si sigue en manos del equipo y venció el plazo de su prioridad") {
        val dia = PlazosSolicitud.DIA_MS
        val ahora = 100 * dia

        // Prioridad Media = 5 días: a los 6 días de creada está atrasada, a los 4 no
        PlazosSolicitud.estaAtrasada("En proceso", "Media", ahora - 6 * dia, ahora) shouldBe true
        PlazosSolicitud.estaAtrasada("En proceso", "Media", ahora - 4 * dia, ahora) shouldBe false
        // Urgente = 1 día; Baja = 7 días
        PlazosSolicitud.estaAtrasada("Recibida", "Urgente", ahora - 2 * dia, ahora) shouldBe true
        PlazosSolicitud.estaAtrasada("Recibida", "Baja", ahora - 6 * dia, ahora) shouldBe false
        // Pendiente de información, Resuelta, Cerrada y Cancelada nunca se consideran atrasadas
        PlazosSolicitud.estaAtrasada("Pendiente de información", "Urgente", ahora - 30 * dia, ahora) shouldBe false
        PlazosSolicitud.estaAtrasada("Resuelta", "Urgente", ahora - 30 * dia, ahora) shouldBe false
        PlazosSolicitud.estaAtrasada("Cerrada", "Urgente", ahora - 30 * dia, ahora) shouldBe false
        PlazosSolicitud.estaAtrasada("Cancelada", "Urgente", ahora - 30 * dia, ahora) shouldBe false

        // Medición simulada: abierta cuenta hasta ahora; terminada, hasta su último cambio
        MedicionTiempos.tiempoTranscurridoMs("Asignada", 0L, 10L, 50L) shouldBe 50L
        MedicionTiempos.tiempoTranscurridoMs("Cerrada", 0L, 10L, 50L) shouldBe 10L
        MedicionTiempos.formatear(26 * 60 * 60 * 1000L) shouldBe "1 d 2 h"
    }
})
