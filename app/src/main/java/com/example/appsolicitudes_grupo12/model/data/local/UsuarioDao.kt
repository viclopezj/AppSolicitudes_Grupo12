package com.example.appsolicitudes_grupo12.model.data.local

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.Query

// Operaciones sobre la tabla "usuarios". Room genera el código.
@Dao
interface UsuarioDao {

    // Guarda un usuario nuevo y devuelve su id.
    @Insert
    suspend fun insertar(usuario: UsuarioEntity): Long

    // Busca por nombre de usuario. null si no existe.
    @Query("SELECT * FROM usuarios WHERE nombreUsuario = :nombreUsuario LIMIT 1")
    suspend fun buscarPorNombre(nombreUsuario: String): UsuarioEntity?

    // Técnicos disponibles para asignar solicitudes.
    @Query("SELECT * FROM usuarios WHERE rol = 'tecnico' ORDER BY nombre")
    suspend fun obtenerTecnicos(): List<UsuarioEntity>
}