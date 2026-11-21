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
// Versión 5: agrega la eliminación lógica de solicitudes (columna "eliminada").
@Database(
    entities = [
        SolicitudEntity::class,
        UsuarioEntity::class,
        HistorialSolicitudEntity::class,
        ComentarioSolicitudEntity::class
    ],
    version = 5,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun solicitudDao(): SolicitudDao
    abstract fun usuarioDao(): UsuarioDao
    abstract fun historialSolicitudDao(): HistorialSolicitudDao
    abstract fun comentarioSolicitudDao(): ComentarioSolicitudDao

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
                    .addMigrations(MIGRACION_2_3, MIGRACION_3_4, MIGRACION_4_5)
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

        // Migración 3 -> 4: agrega prioridad y última actualización a las solicitudes,
        // crea las tablas de historial y comentarios, y deja un primer registro de historial
        // por cada solicitud existente. No se borra ningún dato.
        private val MIGRACION_3_4 = object : Migration(3, 4) {
            override suspend fun migrate(connection: SQLiteConnection) {
                // Columnas nuevas de solicitudes (las antiguas quedan con prioridad Media)
                connection.executeSQL(
                    "ALTER TABLE solicitudes ADD COLUMN prioridad TEXT NOT NULL DEFAULT 'Media'"
                )
                connection.executeSQL(
                    "ALTER TABLE solicitudes ADD COLUMN ultimaActualizacion INTEGER NOT NULL DEFAULT 0"
                )
                connection.executeSQL(
                    "UPDATE solicitudes SET ultimaActualizacion = fechaCreacion"
                )

                // Tabla del historial de cambios
                connection.executeSQL(
                    "CREATE TABLE IF NOT EXISTS historial_solicitud (" +
                            "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                            "solicitudId INTEGER NOT NULL, " +
                            "estadoAnterior TEXT, " +
                            "estadoNuevo TEXT NOT NULL, " +
                            "accion TEXT NOT NULL, " +
                            "comentario TEXT, " +
                            "rol TEXT NOT NULL, " +
                            "usuarioId INTEGER NOT NULL, " +
                            "usuarioNombre TEXT NOT NULL, " +
                            "fecha INTEGER NOT NULL)"
                )
                connection.executeSQL(
                    "CREATE INDEX IF NOT EXISTS index_historial_solicitud_solicitudId " +
                            "ON historial_solicitud (solicitudId)"
                )

                // Tabla de comentarios de seguimiento
                connection.executeSQL(
                    "CREATE TABLE IF NOT EXISTS comentarios_solicitud (" +
                            "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                            "solicitudId INTEGER NOT NULL, " +
                            "usuarioId INTEGER NOT NULL, " +
                            "usuarioNombre TEXT NOT NULL, " +
                            "rol TEXT NOT NULL, " +
                            "texto TEXT NOT NULL, " +
                            "esInterno INTEGER NOT NULL, " +
                            "evidenciaUri TEXT, " +
                            "fecha INTEGER NOT NULL)"
                )
                connection.executeSQL(
                    "CREATE INDEX IF NOT EXISTS index_comentarios_solicitud_solicitudId " +
                            "ON comentarios_solicitud (solicitudId)"
                )

                // Primer registro del historial de las solicitudes que ya existían
                connection.executeSQL(
                    "INSERT INTO historial_solicitud " +
                            "(solicitudId, estadoAnterior, estadoNuevo, accion, comentario, " +
                            "rol, usuarioId, usuarioNombre, fecha) " +
                            "SELECT s.id, NULL, 'Recibida', 'Solicitud registrada', NULL, " +
                            "'cliente', COALESCE(s.creadoPorUsuarioId, 0), " +
                            "COALESCE(u.nombre, 'Sin registro'), s.fechaCreacion " +
                            "FROM solicitudes s LEFT JOIN usuarios u ON u.id = s.creadoPorUsuarioId"
                )
            }
        }
        // Migración 4 -> 5: agrega la marca de eliminación lógica. Las solicitudes existentes
        // quedan visibles (eliminada = 0). No se borra ningún dato.
        private val MIGRACION_4_5 = object : Migration(4, 5) {
            override suspend fun migrate(connection: SQLiteConnection) {
                connection.executeSQL(
                    "ALTER TABLE solicitudes ADD COLUMN eliminada INTEGER NOT NULL DEFAULT 0"
                )
            }
        }
    }
}