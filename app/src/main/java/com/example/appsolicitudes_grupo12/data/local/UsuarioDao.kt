package com.example.appsolicitudes_grupo12.data.local

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.Query

// DAO de usuarios. Aquí se declaran las operaciones sobre la tabla "usuarios".
// Room genera el código real; nosotros solo escribimos la interfaz.
@Dao
interface UsuarioDao {

    // Guarda un usuario nuevo y devuelve el id que se le asignó.
    // "suspend" = se ejecuta en segundo plano, sin congelar la pantalla.
    @Insert
    suspend fun insertar(usuario: UsuarioEntity): Long

    // Busca un usuario por su nombre de usuario.
    // Devuelve null (el "?" del tipo) si no existe ninguno con ese nombre.
    @Query("SELECT * FROM usuarios WHERE nombreUsuario = :nombreUsuario LIMIT 1")
    suspend fun buscarPorNombre(nombreUsuario: String): UsuarioEntity?

    // Cuentas disponibles para asignar solicitudes.
    @Query("SELECT * FROM usuarios WHERE rol = 'tecnico' ORDER BY nombre")
    suspend fun obtenerTecnicos(): List<UsuarioEntity>
}
