package com.example.appsolicitudes_grupo12.data.local

import android.content.Context
import androidx.room3.Database
import androidx.room3.Room
import androidx.room3.RoomDatabase
import androidx.room3.migration.Migration
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.async.executeSQL
import androidx.sqlite.driver.bundled.BundledSQLiteDriver

// Punto de entrada a Room. Conecta las tablas (Entity) con sus DAO
// y crea el archivo de base de datos en el teléfono.
// Version 3 agrega quién creó y a quién se asignó cada solicitud.
@Database(
    entities = [SolicitudEntity::class, UsuarioEntity::class],
    version = 3,
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
                    // Conserva los datos guardados en la versión 2 y agrega las columnas nuevas.
                    .addMigrations(MIGRACION_2_3)
                    // Room 3 exige indicar el driver de SQLite
                    .setDriver(BundledSQLiteDriver())
                    .build()
                INSTANCIA = instancia
                instancia
            }
        }

        // Vincula con el cliente demo las solicitudes creadas antes de tener perfiles.
        // Si esa cuenta no existe, las solicitudes conservan sus campos nulos y siguen
        // disponibles para el administrador. No se eliminan filas durante la migración.
        private val MIGRACION_2_3 = object : Migration(2, 3) {
            override suspend fun migrate(connection: SQLiteConnection) {
                connection.executeSQL(
                    "ALTER TABLE solicitudes ADD COLUMN creadoPorUsuarioId INTEGER"
                )
                connection.executeSQL(
                    "ALTER TABLE solicitudes ADD COLUMN asignadoAUsuarioId INTEGER"
                )
                connection.executeSQL(
                    "UPDATE usuarios SET rol = 'cliente', nombre = 'Cliente Demo' " +
                            "WHERE nombreUsuario = 'demo' AND rol = 'general'"
                )
                connection.executeSQL(
                    "UPDATE solicitudes SET creadoPorUsuarioId = " +
                            "(SELECT id FROM usuarios WHERE nombreUsuario = 'demo' LIMIT 1) " +
                            "WHERE creadoPorUsuarioId IS NULL AND EXISTS " +
                            "(SELECT 1 FROM usuarios WHERE nombreUsuario = 'demo')"
                )
            }
        }
    }
}
