package com.example.appsolicitudes_grupo12.state

// Estado del formulario "Registrar avance" del técnico.
data class AvanceUiState(
    val texto: String = "",
    val esInterno: Boolean = false,
    val evidenciaUri: String? = null,
    val cargandoFoto: Boolean = false,
    val enviando: Boolean = false,
    val errorTexto: String? = null,
    val error: String? = null,
    val mensaje: String? = null
)