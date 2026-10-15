package com.example.appsolicitudes_grupo12.data.local

import android.content.Context
import androidx.room3.Database
import androidx.room3.Room
import androidx.room3.RoomDatabase
import androidx.sqlite.driver.bundled.BundledSQLiteDriver

// Punto de entrada a Room. Conecta la tabla (Entity) con su DAO
// y crea el archivo de base de datos en el teléfono.
// version = 1: si cambias las columnas más adelante, hay que subir este número
// (o desinstalar la app mientras desarrollan).
@Database(
    entities = [SolicitudEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    // Room implementa esta función por nosotros
    abstract fun solicitudDao(): SolicitudDao

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