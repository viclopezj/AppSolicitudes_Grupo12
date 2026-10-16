package com.example.appsolicitudes_grupo12.model

// Representa TODO lo que la pantalla de login necesita para dibujarse.
// Igual que SolicitudUiState: no es la tabla de la base de datos, sino lo que la persona
// está escribiendo, los errores y el estado del ingreso.
// Es inmutable: para cambiarlo se crea una copia con copy().
data class LoginUiState(

    // --- Lo que la persona escribe ---
    val nombreUsuario: String = "",
    val contrasena: String = "",

    // --- Errores de validación de cada campo (null = sin error) ---
    val errorNombreUsuario: String? = null,
    val errorContrasena: String? = null,

    // --- Estado del ingreso ---
    val cargando: Boolean = false,           // true mientras se revisan las credenciales (Loading)
    val errorLogin: String? = null,          // mensaje si el ingreso falló (Error)
    val nombreAutenticado: String? = null    // si trae un nombre, el login salió bien (Success)
)