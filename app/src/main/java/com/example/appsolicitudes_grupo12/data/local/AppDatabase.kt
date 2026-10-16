package com.example.appsolicitudes_grupo12.data.local

import android.content.Context
import androidx.room3.Database
import androidx.room3.Room
import androidx.room3.RoomDatabase
import androidx.sqlite.driver.bundled.BundledSQLiteDriver

// Punto de entrada a Room. Conecta las tablas (Entity) con sus DAO
// y crea el archivo de base de datos en el teléfono.
// version = 2: antes era 1. Subimos el número porque se agregó una tabla nueva
// (usuarios). Si cambias las columnas más adelante, hay que subirlo otra vez
// (o desinstalar la app mientras desarrollan).
@Database(
    entities = [SolicitudEntity::class, UsuarioEntity::class],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    // Room implementa estas funciones por nosotros
    abstract fun solicitudDao(): SolicitudDao
    abstract fun usuarioDao(): UsuarioDao

    companion object {

        // Guarda la única instancia de la base de datos
        @Volatile
        private var INSTANCIA: AppDatabase? = null

        // Devuelve la base de datos; la crea solo la primera vez.
        // Así toda la app comparte una misma base de datos.
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCIA ?: synchronized(this) {
                val instancia = Room.databaseBuilder<AppDatabase>(
                    context.applicationContext,
                    "solicitudes_database"
                )
                    // Room 3 exige indicar el driver de SQLite
                    .setDriver(BundledSQLiteDriver())
                    .build()
                INSTANCIA = instancia
                instancia
            }
        }
    }
}