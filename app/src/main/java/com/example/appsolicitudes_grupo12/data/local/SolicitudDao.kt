package com.example.appsolicitudes_grupo12.data.local

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.Query
import androidx.room3.Update
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
    // o se modifica una solicitud (lo usa ListaSolicitudesScreen).
    @Query("SELECT * FROM solicitudes ORDER BY fechaCreacion DESC")
    fun obtenerTodas(): Flow<List<SolicitudEntity>>

    // Busca UNA solicitud por su id (el que viaja en la ruta del detalle).
    // Devuelve null (el "?" del tipo) si no existe ninguna con ese id.
    // ":id" se reemplaza por el valor del parámetro "id" de la función.
    @Query("SELECT * FROM solicitudes WHERE id = :id LIMIT 1")
    suspend fun obtenerPorId(id: Int): SolicitudEntity?

    // Guarda los cambios de una solicitud que ya existe.
    // Room busca la fila con el mismo id y reemplaza sus columnas.
    // Devuelve cuántas filas cambió: 1 si salió bien, 0 si el id no existía.
    @Update
    suspend fun actualizar(solicitud: SolicitudEntity): Int
}