package com.example.appsolicitudes_grupo12.model.data.local

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.Query
import androidx.room3.Update
import kotlinx.coroutines.flow.Flow

// Operaciones sobre la tabla "solicitudes". Room genera el código.
// Las consultas ocultan las solicitudes con eliminación lógica (eliminada = 1).
@Dao
interface SolicitudDao {

    // Guarda una solicitud nueva y devuelve su id.
    @Insert
    suspend fun insertar(solicitud: SolicitudEntity): Long

    // Administrador: todas. Cliente: las que creó. Técnico: las que tiene asignadas.
    @Query(
        "SELECT * FROM solicitudes WHERE eliminada = 0 AND (" +
                "(:rol = 'administrador') " +
                "OR (:rol = 'cliente' AND creadoPorUsuarioId = :usuarioId) " +
                "OR (:rol = 'tecnico' AND asignadoAUsuarioId = :usuarioId)) " +
                "ORDER BY fechaCreacion DESC"
    )
    fun obtenerVisibles(usuarioId: Int, rol: String): Flow<List<SolicitudEntity>>

    // Todas las solicitudes no eliminadas, para el panel de asignación y el resumen del administrador.
    @Query("SELECT * FROM solicitudes WHERE eliminada = 0 ORDER BY fechaCreacion DESC")
    fun obtenerTodas(): Flow<List<SolicitudEntity>>

    // Una solicitud por id, solo si el usuario tiene acceso. null si no existe o está eliminada.
    @Query(
        "SELECT * FROM solicitudes WHERE id = :id AND eliminada = 0 AND " +
                "(:rol = 'administrador' " +
                "OR (:rol = 'cliente' AND creadoPorUsuarioId = :usuarioId) " +
                "OR (:rol = 'tecnico' AND asignadoAUsuarioId = :usuarioId)) LIMIT 1"
    )
    suspend fun obtenerPorId(id: Int, usuarioId: Int, rol: String): SolicitudEntity?

    // Una solicitud por código (lo usa el escáner QR), solo si el usuario tiene acceso y no está eliminada.
    @Query(
        "SELECT * FROM solicitudes WHERE codigo = :codigo AND eliminada = 0 AND " +
                "(:rol = 'administrador' " +
                "OR (:rol = 'cliente' AND creadoPorUsuarioId = :usuarioId) " +
                "OR (:rol = 'tecnico' AND asignadoAUsuarioId = :usuarioId)) LIMIT 1"
    )
    suspend fun obtenerPorCodigoVisible(codigo: String, usuarioId: Int, rol: String): SolicitudEntity?

    // Guarda los cambios de una solicitud existente. Devuelve las filas cambiadas (0 = no existía).
    @Update
    suspend fun actualizar(solicitud: SolicitudEntity): Int

    // Eliminación lógica: la oculta sin borrar la fila. Devuelve las filas cambiadas.
    @Query("UPDATE solicitudes SET eliminada = 1 WHERE id = :id")
    suspend fun marcarEliminada(id: Int): Int
}