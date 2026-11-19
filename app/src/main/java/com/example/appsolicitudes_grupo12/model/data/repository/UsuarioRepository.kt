package com.example.appsolicitudes_grupo12.model.data.repository

import com.example.appsolicitudes_grupo12.model.data.local.UsuarioDao
import com.example.appsolicitudes_grupo12.model.data.local.UsuarioEntity
import kotlinx.coroutines.CancellationException

// Intermediario entre el ViewModel y la tabla de usuarios (consultas que no son de login).
class UsuarioRepository(private val dao: UsuarioDao) {

    // Cuentas técnicas disponibles para asignar solicitudes.
    suspend fun obtenerTecnicos(): Result<List<UsuarioEntity>> {
        return try {
            Result.success(dao.obtenerTecnicos())
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Cuentas de clientes disponibles para filtrar.
    suspend fun obtenerClientes(): Result<List<UsuarioEntity>> {
        return try {
            Result.success(dao.obtenerClientes())
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
