package com.example.appsolicitudes_grupo12.model.solicitud

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

// Pruebas de las reglas de validación del formulario.
class ValidadorSolicitudTest : FunSpec({

    test("con todo vacío marca título, descripción y categoría") {
        val errores = ValidadorSolicitud.validar("  ", "", "")

        errores.titulo shouldBe "El título es obligatorio"
        errores.descripcion shouldBe "La descripción es obligatoria"
        errores.categoria shouldBe "Selecciona una categoría"
    }

    test("la descripción debe tener al menos 10 caracteres sin contar espacios de los bordes") {
        ValidadorSolicitud.validar("T", " abcdefghi ", "Cat").descripcion shouldBe
                "La descripción debe tener al menos 10 caracteres"
        ValidadorSolicitud.validar("T", "abcdefghij", "Cat").descripcion shouldBe null
    }

    test("con datos correctos no hay ningún error") {
        val errores = ValidadorSolicitud.validar("Sitio web", "Necesito un sitio para mi empresa", "Desarrollo web")

        errores.titulo shouldBe null
        errores.descripcion shouldBe null
        errores.categoria shouldBe null
    }
})
