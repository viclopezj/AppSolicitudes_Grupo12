package com.example.appsolicitudes_grupo12.model.data.files

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Log
import androidx.core.content.FileProvider
import com.example.appsolicitudes_grupo12.model.solicitud.AdjuntoPendiente
import com.example.appsolicitudes_grupo12.model.solicitud.TiposAdjunto
import java.io.File
import java.io.IOException
import java.util.UUID

// Guarda las fotos de evidencia (cámara o galería) como archivos propios de la app,
// en la carpeta privada "evidencias". Así siguen disponibles al reiniciar.
object EvidenciaArchivos {

    // Debe llamarse igual que la carpeta de res/xml/file_paths.xml
    private const val CARPETA = "evidencias"

    // Límites de toda evidencia (cliente y técnico)
    const val TAMANO_MAXIMO_BYTES = 5L * 1024 * 1024
    const val MENSAJE_FORMATO = "Solo se permiten imágenes JPG, PNG o WEBP."
    const val MENSAJE_TAMANO = "La imagen supera el máximo de 5 MB."
    private val FORMATOS_PERMITIDOS = setOf("image/jpeg", "image/png", "image/webp")

    // Adjuntos adicionales de la solicitud (imágenes y documentos): máximo y formatos.
    const val MAXIMO_ADJUNTOS = 5
    const val MENSAJE_FORMATO_ADJUNTO = "Solo se permiten imágenes (JPG, PNG, WEBP), PDF, TXT o Word (DOC, DOCX)."
    const val MENSAJE_TAMANO_ADJUNTO = "El archivo supera el máximo de 5 MB."
    const val MENSAJE_MAXIMO_ADJUNTOS = "Solo se pueden adjuntar hasta 5 archivos."
    private val EXTENSION_IMAGEN = mapOf(
        "image/jpeg" to "jpg",
        "image/png" to "png",
        "image/webp" to "webp"
    )
    private val EXTENSION_DOCUMENTO = mapOf(
        "application/pdf" to "pdf",
        "text/plain" to "txt",
        "application/msword" to "doc",
        "application/vnd.openxmlformats-officedocument.wordprocessingml.document" to "docx"
    )

    // Tipos que ofrece el selector de archivos del sistema.
    val TIPOS_ADJUNTO_PERMITIDOS: Array<String>
        get() = (EXTENSION_IMAGEN.keys + EXTENSION_DOCUMENTO.keys).toTypedArray()

    // Carpeta privada de fotos (se crea si no existe).
    private fun carpeta(context: Context): File {
        val dir = File(context.filesDir, CARPETA)
        if (!dir.exists()) dir.mkdirs()
        return dir
    }

    // Archivo vacío con nombre único, por ejemplo "EVI_3F9A1C2B.jpg".
    // Todavía no existe en disco: lo llena la cámara o la copia de la galería.
    fun crearArchivoNuevo(context: Context): File {
        val nombre = "EVI_" + UUID.randomUUID().toString().take(8).uppercase() + ".jpg"
        return File(carpeta(context), nombre)
    }

    // Dirección "content://" segura para que la app de cámara escriba en el archivo.
    // La autoridad debe ser igual a la del manifest: id de la app + ".fileprovider".
    fun uriParaCamara(context: Context, archivo: File): Uri {
        return FileProvider.getUriForFile(
            context,
            context.packageName + ".fileprovider",
            archivo
        )
    }

    // Copia la foto elegida en la galería a un archivo propio de la app.
    // Es lenta: llamarla en segundo plano. Falla si el formato o el tamaño no sirven.
    fun copiarDeGaleria(context: Context, origen: Uri): Result<File> {
        val tipo = context.contentResolver.getType(origen)
        if (tipo == null || tipo !in FORMATOS_PERMITIDOS) {
            return Result.failure(EvidenciaInvalidaException(MENSAJE_FORMATO))
        }

        val destino = crearArchivoNuevo(context)
        return try {
            val entrada = context.contentResolver.openInputStream(origen)
                ?: return Result.failure(IOException("No se pudo abrir la imagen"))

            entrada.use { lectura ->
                destino.outputStream().use { escritura ->
                    lectura.copyTo(escritura)
                }
            }

            if (excedeTamano(destino)) {
                destino.delete()
                Result.failure(EvidenciaInvalidaException(MENSAJE_TAMANO))
            } else {
                Result.success(destino)
            }
        } catch (e: Exception) {
            // No se deja un archivo incompleto
            Log.e("EvidenciaArchivos", "No se pudo copiar la imagen de la galería", e)
            destino.delete()
            Result.failure(e)
        }
    }

    // Copia una imagen o un documento elegido por la persona a un archivo propio de la app.
    // Es lenta: llamarla en segundo plano. Falla si el formato o el tamaño no sirven.
    fun copiarAdjunto(context: Context, origen: Uri): Result<AdjuntoPendiente> {
        val tipoMime = context.contentResolver.getType(origen)
        val extension = if (tipoMime == null) {
            null
        } else {
            EXTENSION_IMAGEN[tipoMime] ?: EXTENSION_DOCUMENTO[tipoMime]
        }
        if (tipoMime == null || extension == null) {
            return Result.failure(EvidenciaInvalidaException(MENSAJE_FORMATO_ADJUNTO))
        }

        val nombreArchivo = "ADJ_" + UUID.randomUUID().toString().take(8).uppercase() + "." + extension
        val destino = File(carpeta(context), nombreArchivo)
        return try {
            val entrada = context.contentResolver.openInputStream(origen)
                ?: return Result.failure(IOException("No se pudo abrir el archivo"))

            entrada.use { lectura ->
                destino.outputStream().use { escritura ->
                    lectura.copyTo(escritura)
                }
            }

            if (excedeTamano(destino)) {
                destino.delete()
                Result.failure(EvidenciaInvalidaException(MENSAJE_TAMANO_ADJUNTO))
            } else {
                Result.success(
                    AdjuntoPendiente(
                        nombre = nombreVisible(context, origen) ?: "archivo.$extension",
                        tipo = if (tipoMime in EXTENSION_IMAGEN) TiposAdjunto.IMAGEN else TiposAdjunto.DOCUMENTO,
                        uri = aTexto(destino)
                    )
                )
            }
        } catch (e: Exception) {
            // No se deja un archivo incompleto
            Log.e("EvidenciaArchivos", "No se pudo copiar el adjunto", e)
            destino.delete()
            Result.failure(e)
        }
    }

    // Nombre del archivo que ve la persona (el que tiene en su teléfono); null si no se puede leer.
    private fun nombreVisible(context: Context, origen: Uri): String? {
        return try {
            context.contentResolver
                .query(origen, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)
                ?.use { cursor -> if (cursor.moveToFirst()) cursor.getString(0) else null }
        } catch (e: Exception) {
            null
        }
    }

    // Dirección "content://" segura para abrir un archivo guardado con otra app.
    // null si el archivo no existe o no está en la carpeta de la app.
    fun uriParaAbrir(context: Context, uriTexto: String): Uri? {
        return try {
            val ruta = Uri.parse(uriTexto).path ?: return null
            val archivo = File(ruta)
            if (archivo.parentFile?.name != CARPETA || !archivo.exists()) {
                null
            } else {
                FileProvider.getUriForFile(context, context.packageName + ".fileprovider", archivo)
            }
        } catch (e: Exception) {
            null
        }
    }

    // ¿La imagen pasa el tamaño máximo? También se usa con las fotos de cámara.
    fun excedeTamano(archivo: File): Boolean = archivo.length() > TAMANO_MAXIMO_BYTES

    // Texto que se guarda en Room (columna evidenciaUri); Coil lo entiende al mostrar la foto.
    fun aTexto(archivo: File): String {
        return Uri.fromFile(archivo).toString()
    }

    // Borra una foto guardada. Solo borra dentro de la carpeta "evidencias";
    // si no existe o falla, no hace nada.
    fun borrar(uriTexto: String?) {
        if (uriTexto == null) return
        try {
            val ruta = Uri.parse(uriTexto).path ?: return
            val archivo = File(ruta)
            if (archivo.parentFile?.name == CARPETA) {
                archivo.delete()
            }
        } catch (e: Exception) {
            Log.w("EvidenciaArchivos", "No se pudo borrar la foto", e)
        }
    }
}

// Evidencia rechazada por formato o tamaño; el mensaje se muestra tal cual.
class EvidenciaInvalidaException(mensaje: String) : Exception(mensaje)