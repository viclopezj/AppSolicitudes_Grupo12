package com.example.appsolicitudes_grupo12.model.data.local

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.Query
import androidx.room3.Update

// Operaciones sobre la tabla "borradores_solicitud". Room genera el código.
@Dao
interface BorradorDao {

    // Borrador del cliente. null si no tiene.
    @Query("SELECT * FROM borradores_solicitud WHERE usuarioId = :usuarioId LIMIT 1")
    suspend fun obtener(usuarioId: Int): BorradorEntity?

    @Insert
    suspend fun insertar(borrador: BorradorEntity): Long

    // Devuelve las filas cambiadas (0 = el cliente aún no tenía borrador).
    @Update
    suspend fun actualizar(borrador: BorradorEntity): Int

    // Borra el borrador del cliente (se usa al registrar la solicitud o al descartarlo).
    @Query("DELETE FROM borradores_solicitud WHERE usuarioId = :usuarioId")
    suspend fun eliminar(usuarioId: Int): Int
}
