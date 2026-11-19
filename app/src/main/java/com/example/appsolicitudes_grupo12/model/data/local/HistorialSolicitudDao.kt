package com.example.appsolicitudes_grupo12.model.data.local

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.Query
import kotlinx.coroutines.flow.Flow

// Operaciones sobre la tabla "historial_solicitud". Room genera el código.
@Dao
interface HistorialSolicitudDao {

    // Guarda una fila del historial y devuelve su id.
    @Insert
    suspend fun insertar(registro: HistorialSolicitudEntity): Long

    // Historial de una solicitud, del más antiguo al más reciente.
    @Query(
        "SELECT * FROM historial_solicitud WHERE solicitudId = :solicitudId " +
                "ORDER BY fecha ASC, id ASC"
    )
    fun obtenerPorSolicitud(solicitudId: Int): Flow<List<HistorialSolicitudEntity>>
}