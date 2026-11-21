package com.example.appsolicitudes_grupo12.model.aviso

import com.example.appsolicitudes_grupo12.model.compartir.MensajeWhatsApp
import com.example.appsolicitudes_grupo12.model.solicitud.EstadoSolicitud.*
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

// Pruebas de los avisos locales y del mensaje simulado de WhatsApp.
class AvisosYMensajesTest : FunSpec({

    test("cada cambio de estado avisa al perfil correcto o no avisa") {
        Avisos.porCambioDeEstado("X1", RECIBIDA, PENDIENTE_INFORMACION)?.destinatario shouldBe DestinatarioAviso.CLIENTE
        Avisos.porCambioDeEstado("X1", ASIGNADA, EN_PROCESO)?.destinatario shouldBe DestinatarioAviso.CLIENTE
        Avisos.porCambioDeEstado("X1", EN_PROCESO, RESUELTA)?.destinatario shouldBe DestinatarioAviso.CLIENTE
        Avisos.porCambioDeEstado("X1", RESUELTA, CERRADA)?.destinatario shouldBe DestinatarioAviso.TECNICO
        Avisos.porCambioDeEstado("X1", RESUELTA, EN_REVISION)?.destinatario shouldBe DestinatarioAviso.ADMINISTRADOR
        // Estos cambios no avisan
        Avisos.porCambioDeEstado("X1", RECIBIDA, CANCELADA) shouldBe null
        Avisos.porCambioDeEstado("X1", RECIBIDA, EN_REVISION) shouldBe null
    }

    test("el aviso incluye el código y un título según el destinatario") {
        val aviso = Avisos.nuevaSolicitud("SOL-9")

        aviso.titulo shouldBe "Aviso para el administrador"
        aviso.mensaje.contains("SOL-9") shouldBe true
    }

    test("el aviso de atrasadas usa singular y plural") {
        Avisos.atrasadasParaAdministrador(1).mensaje.contains("1 solicitud atrasada") shouldBe true
        Avisos.atrasadasParaAdministrador(3).mensaje.contains("3 solicitudes atrasadas") shouldBe true
        Avisos.atrasadasParaTecnico(1).mensaje shouldBe "Tienes 1 solicitud atrasada asignada."
        Avisos.atrasadasParaTecnico(2).mensaje shouldBe "Tienes 2 solicitudes atrasadas asignadas."
    }

    test("el mensaje de WhatsApp lleva código, título, categoría y enlace, sin la descripción") {
        val mensaje = MensajeWhatsApp.armar("SOL-9", "Sitio web", "Desarrollo web")

        mensaje.contains("Código: SOL-9") shouldBe true
        mensaje.contains("Título: Sitio web") shouldBe true
        mensaje.contains("Categoría: Desarrollo web") shouldBe true
        mensaje.contains(MensajeWhatsApp.enlace("SOL-9")) shouldBe true
    }
})
