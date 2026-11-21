package com.example.appsolicitudes_grupo12.model.data.local

// Datos de empresa y teléfono FICTICIOS de las cuentas de cliente de prueba.
// Los usan las cuentas nuevas (AuthRepository) y la migración 5 -> 6 (cuentas ya creadas).
object ContactosDemo {

    data class Contacto(val empresa: String, val telefono: String)

    private val CONTACTOS = mapOf(
        "demo" to Contacto("Comercial Los Andes SpA", "+56 9 5550 0001"),
        "cliente2" to Contacto("Café del Sur Ltda.", "+56 9 5550 0002"),
        "cliente3" to Contacto("Estudio Pixel Austral", "+56 9 5550 0003")
    )

    // Nombres de usuario con datos de contacto.
    val USUARIOS: Set<String> get() = CONTACTOS.keys

    fun de(nombreUsuario: String): Contacto? = CONTACTOS[nombreUsuario]
}
