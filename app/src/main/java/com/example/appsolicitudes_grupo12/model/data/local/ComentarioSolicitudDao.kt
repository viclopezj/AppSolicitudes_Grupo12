package com.example.appsolicitudes_grupo12.model.data.local

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.Query
import kotlinx.coroutines.flow.Flow

// Operaciones sobre la tabla "comentarios_solicitud". Room genera el código.
@Dao
interface ComentarioSolicitudDao {

    // Guarda un comentario y devuelve su id.
    @Insert
    suspend fun insertar(comentario: ComentarioSolicitudEntity): Long

    // Comentarios de una solicitud, del más antiguo al más reciente.
    // El cliente no recibe los internos; administrador y técnico ven todos.
    @Query(
        "SELECT * FROM comentarios_solicitud WHERE solicitudId = :solicitudId " +
                "AND (:rol <> 'cliente' OR esInterno = 0) " +
                "ORDER BY fecha ASC, id ASC"
    )
    fun obtenerVisibles(solicitudId: Int, rol: String): Flow<List<ComentarioSolicitudEntity>>
}