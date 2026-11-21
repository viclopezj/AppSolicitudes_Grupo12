package com.example.appsolicitudes_grupo12.model.exportacion

import com.example.appsolicitudes_grupo12.model.data.local.SolicitudEntity
import com.example.appsolicitudes_grupo12.model.solicitud.MedicionTiempos
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

// Pruebas de la medición simulada de tiempos y de la exportación ficticia (CSV).
class ResumenYExportacionTest : FunSpec({

    val hora = 3_600_000L

    fun solicitud(estado: String, creada: Long, ultima: Long = creada, titulo: String = "Título") = SolicitudEntity(
        codigo = "SOL-1", titulo = titulo, descripcion = "Descripción larga", categoria = "Desarrollo web",
        servicio = "", estado = estado, fechaCreacion = creada, ultimaActualizacion = ultima
    )

    test("resume el promedio de atención y el de abiertas, sin contar canceladas") {
        val resumen = MedicionTiempos.resumir(
            listOf(
                solicitud("Resuelta", 0L, 2 * hora),
                solicitud("Cerrada", 0L, 4 * hora),
                solicitud("En proceso", 0L),
                solicitud("Pendiente de información", 4 * hora),
                solicitud("Cancelada", 0L, 9 * hora)
            ),
            ahora = 10 * hora
        )

        resumen.atendidas shouldBe 2
        resumen.promedioAtencionMs shouldBe 3 * hora
        resumen.abiertas shouldBe 2
        resumen.promedioAbiertasMs shouldBe 8 * hora
    }

    test("sin solicitudes los promedios quedan sin datos") {
        val resumen = MedicionTiempos.resumir(emptyList(), ahora = 10 * hora)

        resumen.promedioAtencionMs shouldBe null
        resumen.promedioAbiertasMs shouldBe null
    }

    test("formatea la duración en días, horas y minutos") {
        MedicionTiempos.formatear(12 * 60_000L) shouldBe "12 min"
        MedicionTiempos.formatear(5 * hora + 10 * 60_000L) shouldBe "5 h 10 min"
        MedicionTiempos.formatear(51 * hora) shouldBe "2 d 3 h"
        MedicionTiempos.formatear(-5L) shouldBe "0 min"
    }

    test("el CSV tiene encabezado, una fila por solicitud, comillas escapadas y marca las atrasadas") {
        val reporte = ExportacionResumen.generar(
            solicitudes = listOf(solicitud("En proceso", 0L, titulo = "Falla \"grave\", urgente")),
            nombreResponsable = { "Técnico Demo" },
            ahora = 10 * 24 * hora
        )
        val lineas = reporte.contenido.lines()

        reporte.filas shouldBe 1
        reporte.nombreArchivo.startsWith("solicitudes_") shouldBe true
        reporte.nombreArchivo.endsWith(".csv") shouldBe true
        lineas[0] shouldBe "codigo,titulo,categoria,estado,prioridad,responsable,atrasada,creada,tiempo"
        lineas[1].contains("\"Falla \"\"grave\"\", urgente\"") shouldBe true
        lineas[1].contains("\"Técnico Demo\"") shouldBe true
        lineas[1].contains("\"si\"") shouldBe true
    }
})
