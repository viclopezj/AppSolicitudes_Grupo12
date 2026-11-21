package com.example.appsolicitudes_grupo12.model.data.files

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.core.content.FileProvider
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