package com.example.appsolicitudes_grupo12.model.data.local

import android.content.Context
import androidx.room3.Database
import androidx.room3.Room
import androidx.room3.RoomDatabase
import androidx.room3.migration.Migration
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.async.executeSQL
import androidx.sqlite.driver.bundled.BundledSQLiteDriver

// Base de datos Room: conecta las tablas con sus DAO.
// Versión 3: agrega quién creó y a quién se asignó cada solicitud.
@Database(
    entities = [SolicitudEntity::class, UsuarioEntity::class],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun solicitudDao(): SolicitudDao
    abstract fun usuarioDao(): UsuarioDao

    companion object {

        @Volatile
        private var INSTANCIA: AppDatabase? = null

        // Devuelve la única instancia de la base de datos (la crea la primera vez).
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCIA ?: synchronized(this) {
                val instancia = Room.databaseBuilder<AppDatabase>(
                    context.applicationContext,
                    "solicitudes_database"
                )
                    .addMigrations(MIGRACION_2_3)
                    // Room 3 exige indicar el driver de SQLite
                    .setDriver(BundledSQLiteDriver())
                    .build()
                INSTANCIA = instancia
                instancia
            }
        }

        // Migración 2 -> 3: agrega las columnas nuevas conservando los datos existentes.
        // Las solicitudes antiguas se vinculan al cliente "demo"; si no existe, quedan en null
        // y las sigue viendo el administrador.
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