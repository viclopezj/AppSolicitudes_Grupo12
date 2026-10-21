package com.example.appsolicitudes_grupo12.data.local

import androidx.room3.Entity
import androidx.room3.Index
import androidx.room3.PrimaryKey

// Representa la TABLA "usuarios". Cada propiedad es una columna.
// El índice único en "nombreUsuario" impide crear dos usuarios con el mismo nombre.
@Entity(
    tableName = "usuarios",
    indices = [Index(value = ["nombreUsuario"], unique = true)]
)
data class UsuarioEntity(

    // Identificador interno. Room lo genera solo (1, 2, 3...)
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    // Lo que la persona escribe en el login, por ejemplo "demo"
    val nombreUsuario: String,

    // Solo es válido guardarla así porque son datos ficticios de una evaluación.
    // En una app real nunca se guarda una clave en texto plano.
    val contrasena: String,

    // Nombre que se muestra en el menú, por ejemplo "Usuario Demo"
    val nombre: String,

    // Perfil del usuario: "cliente", "administrador" o "tecnico". Define qué solicitudes
    // ve y qué puede hacer. "general" solo existía antes de la versión 3 de la base de datos.
    val rol: String = "general"
)