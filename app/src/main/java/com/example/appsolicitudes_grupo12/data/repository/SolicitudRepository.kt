package com.example.appsolicitudes_grupo12.data.repository

import com.example.appsolicitudes_grupo12.data.local.SolicitudDao
import com.example.appsolicitudes_grupo12.data.local.SolicitudEntity
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import java.util.UUID

// El Repository es el intermediario entre el ViewModel y la base de datos.
// El ViewModel nunca habla directo con el DAO: siempre pasa por aquí.
// Recibe el DAO por el constructor, así no crea la base de datos por su cuenta.
class SolicitudRepository(private val dao: SolicitudDao) {

    // Entrega la lista de solicitudes guardadas.
    // Es un Flow: cada vez que se guarda una nueva, la lista llega actualizada.
    fun obtenerSolicitudes(): Flow<List<SolicitudEntity>> {
        return dao.obtenerTodas()
    }

    // Crea la solicitud y la guarda en Room.
    // Aquí se aplican las reglas del negocio: código único, fecha y estado inicial.
    // Devuelve Result: si salió bien trae el código generado; si falló, trae el error.
    // Así el ViewModel puede mostrar un mensaje en vez de que la app se cierre.
    suspend fun guardarSolicitud(
        titulo: String,
        descripcion: String,
        categoria: String,
        servicio: String,
        evidenciaUri: String? = null
    ): Result<String> {
        return try {
            val codigo = generarCodigo()
            val solicitud = SolicitudEntity(
                codigo = codigo,
                titulo = titulo.trim(),
                descripcion = descripcion.trim(),
                categoria = categoria,
                servicio = servicio.trim(),
                evidenciaUri = evidenciaUri,
                estado = "Recibida",                       // todo comienza como "Recibida"
                fechaCreacion = System.currentTimeMillis() // fecha y hora actuales
            )
            dao.insertar(solicitud)
            Result.success(codigo)
        } catch (e: CancellationException) {
            // Si la corrutina se cancela (por ejemplo, se cierra la pantalla),
            // hay que dejar pasar esa señal y no tratarla como un error.
            throw e
        } catch (e: Exception) {
            // Cualquier otro fallo (por ejemplo, un problema con la base de datos)
            // se devuelve como error controlado
            Result.failure(e)
        }
    }

    // Genera un código como "SOL-1A2B3C4D".
    // UUID crea un texto aleatorio; tomamos 8 caracteres y los pasamos a mayúsculas.
    private fun generarCodigo(): String {
        return "SOL-" + UUID.randomUUID().toString().take(8).uppercase()
    }
}