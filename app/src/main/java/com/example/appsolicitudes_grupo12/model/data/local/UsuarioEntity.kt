package com.example.appsolicitudes_grupo12.model.data.local

import androidx.room3.Entity
import androidx.room3.Index
import androidx.room3.PrimaryKey

// Tabla "usuarios". El índice único impide repetir el nombre de usuario.
@Entity(
    tableName = "usuarios",
    indices = [Index(value = ["nombreUsuario"], unique = true)]
)
data class UsuarioEntity(

    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    // Lo que se escribe en el login, por ejemplo "demo"
    val nombreUsuario: String,

    // Solo válido porque son datos ficticios: una app real nunca guarda la clave en texto plano.
    val contrasena: String,

    // Nombre que se muestra en el menú
    val nombre: String,

    // "cliente", "administrador" o "tecnico". "general" solo existía antes de la versión 3.
    val rol: String = "general"
)