package com.example.appsolicitudes_grupo12.model.qr

import android.graphics.Bitmap
import android.graphics.Color
import android.util.Log
import com.example.appsolicitudes_grupo12.model.data.local.SolicitudEntity
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import org.json.JSONObject

// Convierte una solicitud en el texto del QR, lee ese texto de vuelta y dibuja el QR.
object CodigoQr {

    // Marca que identifica un QR generado por esta app
    private const val MARCADOR_APP = "AppSolicitudes_Grupo12"

    // Todos los códigos de solicitud empiezan así (ver SolicitudRepository)
    private const val PREFIJO_CODIGO = "SOL-"

    // Límites de largo para que el QR no quede demasiado denso
    private const val LARGO_MAXIMO_DESCRIPCION = 300
    private const val LARGO_MAXIMO_TITULO_Y_SERVICIO = 100

    // ---------- De la solicitud a los datos del QR ----------

    // Copia reducida de la solicitud: recorta los textos largos y no incluye la foto.
    fun desdeEntidad(solicitud: SolicitudEntity): DatosQr {
        return DatosQr(
            codigo = solicitud.codigo,
            titulo = recortar(solicitud.titulo, LARGO_MAXIMO_TITULO_Y_SERVICIO),
            descripcion = recortar(solicitud.descripcion, LARGO_MAXIMO_DESCRIPCION),
            categoria = solicitud.categoria,
            servicio = recortar(solicitud.servicio, LARGO_MAXIMO_TITULO_Y_SERVICIO),
            estado = solicitud.estado,
            fechaCreacion = solicitud.fechaCreacion
        )
    }

    // Corta el texto al máximo y agrega "…" si era más largo.
    private fun recortar(texto: String, maximo: Int): String {
        return if (texto.length > maximo) texto.take(maximo - 1) + "…" else texto
    }

    // ---------- Datos -> texto ----------

    // Arma el texto del QR en formato JSON (JSONObject ya viene incluido en Android).
    fun crearTexto(datos: DatosQr): String {
        return JSONObject().apply {
            put("app", MARCADOR_APP)
            put("codigo", datos.codigo)
            put("titulo", datos.titulo)
            put("descripcion", datos.descripcion)
            put("categoria", datos.categoria)
            put("servicio", datos.servicio)
            put("estado", datos.estado)
            put("fechaCreacion", datos.fechaCreacion)
        }.toString()
    }

    // ---------- Texto -> datos ----------

    // Lee el texto del escáner. Devuelve null si no es un QR de solicitud de esta app.
    fun leerTexto(texto: String): DatosQr? {
        return try {
            val json = JSONObject(texto)

            if (json.optString("app") != MARCADOR_APP) return null

            // El código es lo único obligatorio y debe tener el formato "SOL-xxxxxxxx"
            val codigo = json.getString("codigo")
            if (!codigo.startsWith(PREFIJO_CODIGO)) return null

            DatosQr(
                codigo = codigo,
                titulo = json.optString("titulo"),
                descripcion = json.optString("descripcion"),
                categoria = json.optString("categoria"),
                servicio = json.optString("servicio"),
                estado = json.optString("estado"),
                fechaCreacion = json.optLong("fechaCreacion")
            )
        } catch (e: Exception) {
            // No es JSON o no trae código: simplemente no es un QR válido
            Log.w("CodigoQr", "El QR leído no es una solicitud de la app", e)
            null
        }
    }

    // ---------- Texto -> imagen ----------

    // Dibuja el QR como imagen ("tamano" en píxeles). Usa ZXing para generarlo.
    // Devuelve null si el texto no cabe en un QR.
    fun dibujarQr(texto: String, tamano: Int = 512): Bitmap? {
        return try {
            val opciones = mapOf(
                EncodeHintType.CHARACTER_SET to "UTF-8",
                // Nivel M: tolera un poco de suciedad o reflejo
                EncodeHintType.ERROR_CORRECTION to ErrorCorrectionLevel.M,
                // Borde blanco que la cámara necesita para detectarlo
                EncodeHintType.MARGIN to 2
            )

            val matriz = QRCodeWriter().encode(texto, BarcodeFormat.QR_CODE, tamano, tamano, opciones)

            // Siempre negro sobre blanco (también en modo oscuro), o la cámara puede no leerlo
            val pixeles = IntArray(tamano * tamano)
            for (y in 0 until tamano) {
                for (x in 0 until tamano) {
                    pixeles[y * tamano + x] = if (matriz.get(x, y)) Color.BLACK else Color.WHITE
                }
            }
            Bitmap.createBitmap(pixeles, tamano, tamano, Bitmap.Config.ARGB_8888)
        } catch (e: Exception) {
            Log.e("CodigoQr", "No se pudo dibujar el QR", e)
            null
        }
    }
}