package com.example.appsolicitudes_grupo12.model.data.local

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.Query
import androidx.room3.Update
import kotlinx.coroutines.flow.Flow

// Operaciones sobre la tabla "solicitudes". Room genera el código.
@Dao
interface SolicitudDao {

    // Guarda una solicitud nueva y devuelve su id.
    @Insert
    suspend fun insertar(solicitud: SolicitudEntity): Long

    // Administrador: todas. Cliente: las que creó. Técnico: las que tiene asignadas.
    @Query(
        "SELECT * FROM solicitudes " +
                "WHERE (:rol = 'administrador') " +
                "OR (:rol = 'cliente' AND creadoPorUsuarioId = :usuarioId) " +
                "OR (:rol = 'tecnico' AND asignadoAUsuarioId = :usuarioId) " +
                "ORDER BY fechaCreacion DESC"
    )
    fun obtenerVisibles(usuarioId: Int, rol: String): Flow<List<SolicitudEntity>>

    // Todas las solicitudes, para la pantalla de asignación del administrador.
    @Query("SELECT * FROM solicitudes ORDER BY fechaCreacion DESC")
    fun obtenerTodas(): Flow<List<SolicitudEntity>>

    // Una solicitud por id, solo si el usuario tiene acceso. null si no existe.
    @Query(
        "SELECT * FROM solicitudes WHERE id = :id AND " +
                "(:rol = 'administrador' " +
                "OR (:rol = 'cliente' AND creadoPorUsuarioId = :usuarioId) " +
                "OR (:rol = 'tecnico' AND asignadoAUsuarioId = :usuarioId)) LIMIT 1"
    )
    suspend fun obtenerPorId(id: Int, usuarioId: Int, rol: String): SolicitudEntity?

    // Una solicitud por código (lo usa el escáner QR), solo si el usuario tiene acceso.
    @Query(
        "SELECT * FROM solicitudes WHERE codigo = :codigo AND " +
                "(:rol = 'administrador' " +
                "OR (:rol = 'cliente' AND creadoPorUsuarioId = :usuarioId) " +
                "OR (:rol = 'tecnico' AND asignadoAUsuarioId = :usuarioId)) LIMIT 1"
    )
    suspend fun obtenerPorCodigoVisible(codigo: String, usuarioId: Int, rol: String): SolicitudEntity?

    // Guarda los cambios de una solicitud existente. Devuelve las filas cambiadas (0 = no existía).
    @Update
    suspend fun actualizar(solicitud: SolicitudEntity): Int

    // Asigna la solicitud a un técnico y la pasa a "Asignada". Devuelve las filas cambiadas.
    @Query(
        "UPDATE solicitudes SET asignadoAUsuarioId = :tecnicoId, estado = 'Asignada' " +
                "WHERE id = :solicitudId AND EXISTS " +
                "(SELECT 1 FROM usuarios WHERE id = :tecnicoId AND rol = 'tecnico')"
    )
    suspend fun asignarTecnico(solicitudId: Int, tecnicoId: Int): Int
}