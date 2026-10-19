package com.example.appsolicitudes_grupo12.utils

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.core.content.FileProvider
import java.io.File
import java.util.UUID

// Herramientas para guardar las fotos de evidencia dentro de la app.
// La foto puede venir de la cámara o de la galería, pero en los dos casos termina como
// un archivo PROPIO de la app, en la carpeta privada "evidencias". Así sigue disponible
// al reiniciar: la dirección que entrega la galería es temporal y dejaría de funcionar.
// No es una pantalla ni un ViewModel: son funciones de apoyo. Es un "object",
// así que se usa directo: EvidenciaUtils.crearArchivoNuevo(...)
object EvidenciaUtils {

    // Debe llamarse igual que la carpeta declarada en res/xml/file_paths.xml
    private const val CARPETA = "evidencias"

    // Devuelve la carpeta privada de fotos y la crea si todavía no existe.
    private fun carpeta(context: Context): File {
        val dir = File(context.filesDir, CARPETA)
        if (!dir.exists()) dir.mkdirs()
        return dir
    }

    // Prepara un archivo vacío con nombre único, por ejemplo "EVI_3F9A1C2B.jpg".
    // Todavía no existe en disco: la cámara o la copia de la galería lo llenará.
    fun crearArchivoNuevo(context: Context): File {
        val nombre = "EVI_" + UUID.randomUUID().toString().take(8).uppercase() + ".jpg"
        return File(carpeta(context), nombre)
    }

    // Convierte el archivo en una dirección (Uri) que la app de cámara puede usar para escribir.
    // Android no deja pasar rutas de archivo directas a otra app: el FileProvider
    // declarado en el manifest crea una dirección "content://..." segura para este archivo.
    // La autoridad debe ser idéntica a la del manifest: id de la app + ".fileprovider".
    fun uriParaCamara(context: Context, archivo: File): Uri {
        return FileProvider.getUriForFile(
            context,
            context.packageName + ".fileprovider",
            archivo
        )
    }

    // Copia la foto elegida en la galería a un archivo propio de la app.
    // Es una operación lenta (lee y escribe el archivo completo), por eso quien la llame
    // debe hacerlo en segundo plano. Devuelve el archivo nuevo, o null si falló.
    fun copiarDeGaleria(context: Context, origen: Uri): File? {
        val destino = crearArchivoNuevo(context)
        return try {
            val entrada = context.contentResolver.openInputStream(origen)
                ?: return null   // el sistema no pudo abrir la imagen elegida

            // use { } cierra los archivos solos, aunque ocurra un error
            entrada.use { lectura ->
                destino.outputStream().use { escritura ->
                    lectura.copyTo(escritura)
                }
            }
            destino
        } catch (e: Exception) {
            // Si algo falló a la mitad, no se deja un archivo incompleto
            Log.e("EvidenciaUtils", "No se pudo copiar la imagen de la galería", e)
            destino.delete()
            null
        }
    }

    // Convierte el archivo en el texto que se guarda en Room (columna evidenciaUri),
    // con la forma "file:///data/user/0/.../files/evidencias/EVI_3F9A1C2B.jpg".
    // Coil entiende este formato directamente al mostrar la foto.
    fun aTexto(archivo: File): String {
        return Uri.fromFile(archivo).toString()
    }

    // Borra una foto guardada (por ejemplo, si la persona pulsa "Quitar foto").
    // Solo borra archivos que estén dentro de la carpeta "evidencias": así no hay riesgo
    // de borrar otra cosa por error. Si no existe o falla, simplemente no hace nada.
    fun borrar(uriTexto: String?) {
        if (uriTexto == null) return
        try {
            val ruta = Uri.parse(uriTexto).path ?: return
            val archivo = File(ruta)
            if (archivo.parentFile?.name == CARPETA) {
                archivo.delete()
            }
        } catch (e: Exception) {
            Log.w("EvidenciaUtils", "No se pudo borrar la foto", e)
        }
    }
}