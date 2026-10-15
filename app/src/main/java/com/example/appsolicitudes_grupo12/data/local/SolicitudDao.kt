package com.example.appsolicitudes_grupo12.data.local

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.Query
import kotlinx.coroutines.flow.Flow

// DAO = Data Access Object. Aquí se declaran las operaciones sobre la tabla.
// Room genera el código real; nosotros solo escribimos la interfaz.
@Dao
interface SolicitudDao {

    // Guarda una solicitud nueva y devuelve el id que se le asignó.
    // "suspend" = se ejecuta en segundo plano, sin congelar la pantalla.
    @Insert
    suspend fun insertar(solicitud: SolicitudEntity): Long

    // Devuelve todas las solicitudes, las más recientes primero.
    // Al devolver Flow, la lista se actualiza sola cada vez que se guarda
    // una solicitud nueva (lo usaremos en ListaSolicitudesScreen).
    @Query("SELECT * FROM solicitudes ORDER BY fechaCreacion DESC")
    fun obtenerTodas(): Flow<List<SolicitudEntity>>
}