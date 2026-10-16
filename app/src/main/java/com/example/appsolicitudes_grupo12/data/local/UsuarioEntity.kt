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

    // Perfil del usuario. Hoy todos son "general"; más adelante será
    // "cliente", "administrador" o "tecnico" para limitar lo que cada uno puede hacer.
    val rol: String = "general"
)