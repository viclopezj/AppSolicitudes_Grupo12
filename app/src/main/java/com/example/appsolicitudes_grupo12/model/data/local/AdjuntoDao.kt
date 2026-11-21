package com.example.appsolicitudes_grupo12.model.data.local

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.Query
import kotlinx.coroutines.flow.Flow

// Operaciones sobre la tabla "adjuntos_solicitud". Room genera el código.
@Dao
interface AdjuntoDao {

    // Guarda un adjunto y devuelve su id.
    @Insert
    suspend fun insertar(adjunto: AdjuntoEntity): Long

    // Adjuntos de una solicitud, del más antiguo al más reciente.
    @Query(
        "SELECT * FROM adjuntos_solicitud WHERE solicitudId = :solicitudId " +
                "ORDER BY fecha ASC, id ASC"
    )
    fun obtenerPorSolicitud(solicitudId: Int): Flow<List<AdjuntoEntity>>
}
