package com.example.appsolicitudes_grupo12.model.data.repository

import com.example.appsolicitudes_grupo12.model.data.local.BorradorDao
import com.example.appsolicitudes_grupo12.model.data.local.BorradorEntity
import kotlinx.coroutines.CancellationException

// Intermediario entre el formulario y la tabla de borradores (uno por cliente).
class BorradorRepository(private val dao: BorradorDao) {

    // Borrador del cliente; null si no tiene.
    suspend fun obtener(usuarioId: Int): Result<BorradorEntity?> {
        return try {
            Result.success(dao.obtener(usuarioId))
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Guarda el borrador (reemplaza el anterior si ya había uno).
    suspend fun guardar(
        usuarioId: Int,
        titulo: String,
        descripcion: String,
        categoria: String,
        servicio: String
    ): Result<Unit> {
        return try {
            val borrador = BorradorEntity(
                usuarioId = usuarioId,
                titulo = titulo,
                descripcion = descripcion,
                categoria = categoria,
                servicio = servicio,
                fecha = System.currentTimeMillis()
            )
            // actualizar() devuelve 0 si todavía no había borrador: entonces se inserta
            if (dao.actualizar(borrador) == 0) {
                dao.insertar(borrador)
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Borra el borrador del cliente (si no había, no pasa nada).
    suspend fun eliminar(usuarioId: Int): Result<Unit> {
        return try {
            dao.eliminar(usuarioId)
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
