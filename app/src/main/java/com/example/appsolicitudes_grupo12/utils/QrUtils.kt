package com.example.appsolicitudes_grupo12.utils

import android.graphics.Bitmap
import android.graphics.Color
import android.util.Log
import com.example.appsolicitudes_grupo12.data.local.SolicitudEntity
import com.example.appsolicitudes_grupo12.model.SolicitudQr
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import org.json.JSONObject

// Herramientas para trabajar con el código QR de una solicitud.
// Hace tres cosas: convertir una solicitud en texto, leer ese texto de vuelta
// y dibujar el QR como imagen. No es una pantalla ni un ViewModel: son funciones
// de apoyo que usan los dos. Es un "object", así que se usa directo: QrUtils.crearTexto(...)
object QrUtils {

    // Marca propia de la app. Va dentro del texto del QR para reconocer
    // que ese QR lo generó esta aplicación y no es uno cualquiera (por ejemplo, el de un sitio web).
    private const val MARCADOR_APP = "AppSolicitudes_Grupo12"

    // Todos los códigos de solicitud empiezan con este prefijo (ver SolicitudRepository)
    private const val PREFIJO_CODIGO = "SOL-"

    // Límites de largo para que el QR no quede demasiado denso y difícil de leer.
    // Más texto = más cuadritos = más difícil de enfocar con la cámara.
    private const val LARGO_MAXIMO_DESCRIPCION = 300
    private const val LARGO_MAXIMO_TITULO_Y_SERVICIO = 100

    // ---------- De la tabla al QR ----------

    // Convierte la solicitud guardada en Room en los datos reducidos que viajan en el QR.
    // Aquí se recortan los textos largos. La foto no se incluye.
    fun desdeEntidad(solicitud: SolicitudEntity): SolicitudQr {
        return SolicitudQr(
            codigo = solicitud.codigo,
            titulo = recortar(solicitud.titulo, LARGO_MAXIMO_TITULO_Y_SERVICIO),
            descripcion = recortar(solicitud.descripcion, LARGO_MAXIMO_DESCRIPCION),
            categoria = solicitud.categoria,
            servicio = recortar(solicitud.servicio, LARGO_MAXIMO_TITULO_Y_SERVICIO),
            estado = solicitud.estado,
            fechaCreacion = solicitud.fechaCreacion
        )
    }

    // Si el texto es más largo que el máximo, lo corta y agrega "…" al final.
    private fun recortar(texto: String, maximo: Int): String {
        return if (texto.length > maximo) texto.take(maximo - 1) + "…" else texto
    }

    // ---------- Datos -> texto ----------

    // Arma el texto que se guarda dentro del QR, en formato JSON.
    // JSON es un formato de texto con "clave": valor, por ejemplo {"codigo":"SOL-1A2B3C4D"}.
    // Se usa porque maneja bien las comillas y los saltos de línea de una descripción.
    // JSONObject ya viene incluido en Android: no hace falta ninguna dependencia nueva.
    fun crearTexto(datos: SolicitudQr): String {
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

    // Lee el texto que entregó el escáner y lo convierte en datos de una solicitud.
    // Devuelve null si el QR NO es de una solicitud de esta app (texto cualquiera,
    // un enlace, un JSON de otra aplicación o datos dañados). Nunca se cae:
    // cualquier problema al leer se traduce en null y la pantalla muestra un aviso.
    fun leerTexto(texto: String): SolicitudQr? {
        return try {
            val json = JSONObject(texto)

            // Si no trae la marca de esta app, no es nuestro QR
            if (json.optString("app") != MARCADOR_APP) return null

            // El código es lo único obligatorio y debe tener el formato "SOL-xxxxxxxx"
            val codigo = json.getString("codigo")
            if (!codigo.startsWith(PREFIJO_CODIGO)) return null

            // optString/optLong devuelven un valor por defecto si falta el dato,
            // en vez de lanzar un error
            SolicitudQr(
                codigo = codigo,
                titulo = json.optString("titulo"),
                descripcion = json.optString("descripcion"),
                categoria = json.optString("categoria"),
                servicio = json.optString("servicio"),
                estado = json.optString("estado"),
                fechaCreacion = json.optLong("fechaCreacion")
            )
        } catch (e: Exception) {
            // Texto que no es JSON, o JSON sin código: simplemente no es un QR válido
            Log.w("QrUtils", "El QR leído no es una solicitud de la app", e)
            null
        }
    }

    // ---------- Texto -> imagen del QR ----------

    // Dibuja el QR como imagen (Bitmap). "tamano" es el ancho y alto en píxeles.
    // Usa ZXing, la librería que agregamos para GENERAR el QR (ML Kit lo LEE).
    // Devuelve null si el texto no cabe en un QR; la pantalla simplemente no lo muestra.
    fun dibujarQr(texto: String, tamano: Int = 512): Bitmap? {
        return try {
            val opciones = mapOf(
                // UTF-8 para que las tildes y la ñ se lean bien
                EncodeHintType.CHARACTER_SET to "UTF-8",
                // Nivel M: el QR tolera un poco de suciedad o reflejo y sigue siendo legible
                EncodeHintType.ERROR_CORRECTION to ErrorCorrectionLevel.M,
                // Borde blanco alrededor (en "cuadritos"); la cámara lo necesita para detectarlo
                EncodeHintType.MARGIN to 2
            )

            // La matriz dice, para cada punto, si es negro (true) o blanco (false)
            val matriz = QRCodeWriter().encode(texto, BarcodeFormat.QR_CODE, tamano, tamano, opciones)

            // Se pinta cada punto. Se usa negro sobre blanco SIEMPRE (también en modo oscuro),
            // porque un QR con colores invertidos puede no leerse.
            val pixeles = IntArray(tamano * tamano)
            for (y in 0 until tamano) {
                for (x in 0 until tamano) {
                    pixeles[y * tamano + x] = if (matriz.get(x, y)) Color.BLACK else Color.WHITE
                }
            }
            Bitmap.createBitmap(pixeles, tamano, tamano, Bitmap.Config.ARGB_8888)
        } catch (e: Exception) {
            Log.e("QrUtils", "No se pudo dibujar el QR", e)
            null
        }
    }
}