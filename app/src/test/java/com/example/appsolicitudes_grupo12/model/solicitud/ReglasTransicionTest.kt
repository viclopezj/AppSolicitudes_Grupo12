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

    test("el técnico registra avances solo en Asignada o En proceso") {
        ReglasTransicion.puedeRegistrarAvance(tecnico, ASIGNADA) shouldBe true
        ReglasTransicion.puedeRegistrarAvance(tecnico, EN_PROCESO) shouldBe true
        ReglasTransicion.puedeRegistrarAvance(tecnico, RESUELTA) shouldBe false
        ReglasTransicion.puedeRegistrarAvance(admin, EN_PROCESO) shouldBe false
    }
})
