package com.example.appsolicitudes_grupo12.model.data.repository

import com.example.appsolicitudes_grupo12.model.data.local.SolicitudDao
import com.example.appsolicitudes_grupo12.model.data.local.SolicitudEntity
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import java.util.UUID

// La solicitud buscada no existe o el usuario no tiene acceso (distinto de un fallo técnico).
class SolicitudNoEncontradaException : Exception("La solicitud no existe")

// Intermediario entre los ViewModels y el DAO de solicitudes.
class SolicitudRepository(private val dao: SolicitudDao) {

    // ---------- Lecturas ----------

    // Solicitudes visibles para el usuario. Se vuelve a emitir al cambiar los datos.
    fun obtenerSolicitudes(usuarioId: Int, rol: String): Flow<List<SolicitudEntity>> {
        return dao.obtenerVisibles(usuarioId, rol)
    }

    // Todas las solicitudes (solo para la pantalla de asignaciones).
    fun obtenerTodasParaAdministracion(): Flow<List<SolicitudEntity>> {
        return dao.obtenerTodas()
    }

    // Una solicitud por id (pantalla de detalle).
    suspend fun obtenerSolicitud(id: Int, usuarioId: Int, rol: String): Result<SolicitudEntity> {
        return try {
            val solicitud = dao.obtenerPorId(id, usuarioId, rol)
            if (solicitud != null) {
                Result.success(solicitud)
            } else {
                Result.failure(SolicitudNoEncontradaException())
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Una solicitud por código (lo usa el escáner QR).
    suspend fun buscarPorCodigo(codigo: String, usuarioId: Int, rol: String): Result<SolicitudEntity> {
        return try {
            // Los códigos se generan siempre en mayúsculas
            val solicitud = dao.obtenerPorCodigoVisible(codigo.trim().uppercase(), usuarioId, rol)
            if (solicitud != null) {
                Result.success(solicitud)
            } else {
                Result.failure(SolicitudNoEncontradaException())
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ---------- Escritura ----------

    // Crea la solicitud con código único, fecha y estado inicial, y la guarda.
    // Devuelve la solicitud completa porque con sus datos se dibuja el QR.
    suspend fun guardarSolicitud(
        titulo: String,
        descripcion: String,
        categoria: String,
        servicio: String,
        creadorUsuarioId: Int,
        evidenciaUri: String? = null
    ): Result<SolicitudEntity> {
        return try {
            val solicitud = SolicitudEntity(
                codigo = generarCodigo(),
                titulo = titulo.trim(),
                descripcion = descripcion.trim(),
                categoria = categoria,
                servicio = servicio.trim(),
                evidenciaUri = evidenciaUri,
                estado = "Recibida",
                fechaCreacion = System.currentTimeMillis(),
                creadoPorUsuarioId = creadorUsuarioId
            )
            val idNuevo = dao.insertar(solicitud)
            Result.success(solicitud.copy(id = idNuevo.toInt()))
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Cambia título, descripción, categoría y servicio. Código, fecha, estado y foto se conservan.
    suspend fun actualizarSolicitud(
        id: Int,
        usuarioId: Int,
        rol: String,
        titulo: String,
        descripcion: String,
        categoria: String,
        servicio: String
    ): Result<Unit> {
        return try {
            // El técnico no edita solicitudes
            if (rol == "tecnico") {
                return Result.failure(SolicitudNoEncontradaException())
            }

            val actual = dao.obtenerPorId(id, usuarioId, rol)
                ?: return Result.failure(SolicitudNoEncontradaException())

            val modificada = actual.copy(
                titulo = titulo.trim(),
                descripcion = descripcion.trim(),
                categoria = categoria,
                servicio = servicio.trim()
            )

            // actualizar() devuelve las filas cambiadas: 0 = no encontró el id
            val filasCambiadas = dao.actualizar(modificada)
            if (filasCambiadas > 0) {
                Result.success(Unit)
            } else {
                Result.failure(SolicitudNoEncontradaException())
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Asigna la solicitud a un técnico (el DAO verifica que el usuario sea técnico).
    suspend fun asignarTecnico(solicitudId: Int, tecnicoId: Int): Result<Unit> {
        return try {
            val filas = dao.asignarTecnico(solicitudId, tecnicoId)
            if (filas > 0) Result.success(Unit)
            else Result.failure(SolicitudNoEncontradaException())
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ---------- Utilidades ----------

    // Código como "SOL-1A2B3C4D": 8 caracteres de un UUID en mayúsculas.
    private fun generarCodigo(): String {
        return "SOL-" + UUID.randomUUID().toString().take(8).uppercase()
    }
}