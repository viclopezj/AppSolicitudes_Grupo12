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

    // El administrador ve todas; el cliente solo las que creó y el técnico
    // solo las que tiene asignadas. Room vuelve a emitir al cambiar los datos.
    @Query(
        "SELECT * FROM solicitudes " +
                "WHERE (:rol = 'administrador') " +
                "OR (:rol = 'cliente' AND creadoPorUsuarioId = :usuarioId) " +
                "OR (:rol = 'tecnico' AND asignadoAUsuarioId = :usuarioId) " +
                "ORDER BY fechaCreacion DESC"
    )
    fun obtenerVisibles(usuarioId: Int, rol: String): Flow<List<SolicitudEntity>>

    // Datos para la pantalla de asignación del administrador.
    @Query("SELECT * FROM solicitudes ORDER BY fechaCreacion DESC")
    fun obtenerTodas(): Flow<List<SolicitudEntity>>

    // Busca UNA solicitud por su id (el que viaja en la ruta del detalle).
    // Devuelve null (el "?" del tipo) si no existe ninguna con ese id.
    // ":id" se reemplaza por el valor del parámetro "id" de la función.
    @Query(
        "SELECT * FROM solicitudes WHERE id = :id AND " +
                "(:rol = 'administrador' " +
                "OR (:rol = 'cliente' AND creadoPorUsuarioId = :usuarioId) " +
                "OR (:rol = 'tecnico' AND asignadoAUsuarioId = :usuarioId)) LIMIT 1"
    )
    suspend fun obtenerPorId(id: Int, usuarioId: Int, rol: String): SolicitudEntity?

    // El escáner solo habilita abrir el detalle si el usuario tiene acceso a la solicitud.
    @Query(
        "SELECT * FROM solicitudes WHERE codigo = :codigo AND " +
                "(:rol = 'administrador' " +
                "OR (:rol = 'cliente' AND creadoPorUsuarioId = :usuarioId) " +
                "OR (:rol = 'tecnico' AND asignadoAUsuarioId = :usuarioId)) LIMIT 1"
    )
    suspend fun obtenerPorCodigoVisible(codigo: String, usuarioId: Int, rol: String): SolicitudEntity?

    // Guarda los cambios de una solicitud que ya existe.
    // Room busca la fila con el mismo id y reemplaza sus columnas.
    // Devuelve cuántas filas cambió: 1 si salió bien, 0 si el id no existía.
    @Update
    suspend fun actualizar(solicitud: SolicitudEntity): Int

    // Asigna una solicitud al técnico elegido y actualiza su estado.
    @Query(
        "UPDATE solicitudes SET asignadoAUsuarioId = :tecnicoId, estado = 'Asignada' " +
                "WHERE id = :solicitudId AND EXISTS " +
                "(SELECT 1 FROM usuarios WHERE id = :tecnicoId AND rol = 'tecnico')"
    )
    suspend fun asignarTecnico(solicitudId: Int, tecnicoId: Int): Int
}
