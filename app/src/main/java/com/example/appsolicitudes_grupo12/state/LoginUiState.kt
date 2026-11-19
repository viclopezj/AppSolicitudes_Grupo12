package com.example.appsolicitudes_grupo12.state

// Todo lo que necesita la pantalla de login: campos, errores y resultado del ingreso.
data class LoginUiState(

    // Campos
    val nombreUsuario: String = "",
    val contrasena: String = "",

    // Errores de validación (null = sin error)
    val errorNombreUsuario: String? = null,
    val errorContrasena: String? = null,

    // Estado del ingreso
    val cargando: Boolean = false,
    val errorLogin: String? = null,

    // Si trae nombre, el login salió bien
    val nombreAutenticado: String? = null,
    val usuarioIdAutenticado: Int? = null,
    val rolAutenticado: String? = null
)