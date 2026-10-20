package com.example.appsolicitudes_grupo12.data.repository

import com.example.appsolicitudes_grupo12.data.local.SolicitudDao
import com.example.appsolicitudes_grupo12.data.local.SolicitudEntity
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import java.util.UUID

// Error propio para cuando se busca una solicitud que no existe (por ejemplo, un id inválido).
// Sirve para distinguirlo de un fallo técnico (un problema con la base de datos):
// el ViewModel muestra un mensaje distinto en cada caso.
// Funciona igual que CredencialesInvalidasException en AuthRepository.
class SolicitudNoEncontradaException : Exception("La solicitud no existe")

// El Repository es el intermediario entre el ViewModel y la base de datos.
// El ViewModel nunca habla directo con el DAO: siempre pasa por aquí.
// Recibe el DAO por el constructor, así no crea la base de datos por su cuenta.
class SolicitudRepository(private val dao: SolicitudDao) {

    // Entrega la lista de solicitudes guardadas.
    // Es un Flow: cada vez que se guarda o se modifica una, la lista llega actualizada.
    fun obtenerSolicitudes(usuarioId: Int, rol: String): Flow<List<SolicitudEntity>> {
        return dao.obtenerVisibles(usuarioId, rol)
    }

    // Se usa solamente en el área administrativa de asignaciones.
    fun obtenerTodasParaAdministracion(): Flow<List<SolicitudEntity>> {
        return dao.obtenerTodas()
    }

    // Busca UNA solicitud por su id (para la pantalla de detalle).
    // Result: si salió bien trae la solicitud; si no existe trae SolicitudNoEncontradaException;
    // si falló la base de datos trae ese otro error.
    suspend fun obtenerSolicitud(id: Int, usuarioId: Int, rol: String): Result<SolicitudEntity> {
        return try {
            val solicitud = dao.obtenerPorId(id, usuarioId, rol)
            if (solicitud != null) {
                Result.success(solicitud)
            } else {
                Result.failure(SolicitudNoEncontradaException())
            }
        } catch (e: CancellationException) {
            // Si la corrutina se cancela, hay que dejar pasar esa señal y no tratarla como error
            throw e
        } catch (e: Exception) {
            // Cualquier otro fallo se devuelve como error controlado
            Result.failure(e)
        }
    }

    // Busca UNA solicitud por su código (por ejemplo "SOL-1A2B3C4D").
    // Es lo que usa el escáner QR: el QR trae el código y con él se identifica la solicitud.
    // Result: igual que obtenerSolicitud. Si el código no corresponde a ninguna solicitud
    // trae SolicitudNoEncontradaException; si falló la base de datos trae ese otro error.
    suspend fun buscarPorCodigo(codigo: String, usuarioId: Int, rol: String): Result<SolicitudEntity> {
        return try {
            // trim() quita espacios sobrantes y uppercase() iguala las mayúsculas,
            // porque los códigos se generan siempre en mayúsculas
            val solicitud = dao.obtenerPorCodigoVisible(codigo.trim().uppercase(), usuarioId, rol)
            if (solicitud != null) {
                Result.success(solicitud)
            } else {
                Result.failure(SolicitudNoEncontradaException())
            }
        } catch (e: CancellationException) {
            // Si la corrutina se cancela, hay que dejar pasar esa señal y no tratarla como error
            throw e
        } catch (e: Exception) {
            // Cualquier otro fallo se devuelve como error controlado
            Result.failure(e)
        }
    }

    // Crea la solicitud y la guarda en Room.
    // Aquí se aplican las reglas del negocio: código único, fecha y estado inicial.
    // Devuelve Result: si salió bien trae la solicitud completa (con su código, fecha y estado),
    // porque con esos datos se dibuja el QR; si falló, trae el error.
    // Así el ViewModel puede mostrar un mensaje en vez de que la app se cierre.
    suspend fun guardarSolicitud(
        titulo: String,
        descripcion: String,
        categoria: String,
        servicio: String,
        creadorUsuarioId: Int,
        evidenciaUri: String? = null
    ): Result<SolicitudEntity> {
        return try {
            val solicitud = SolicitudEntity(
                codigo = generarCodigo(),
                titulo = titulo.trim(),
                descripcion = descripcion.trim(),
                categoria = categoria,
                servicio = servicio.trim(),
                evidenciaUri = evidenciaUri,
                estado = "Recibida",                       // todo comienza como "Recibida"
                fechaCreacion = System.currentTimeMillis(), // fecha y hora actuales
                creadoPorUsuarioId = creadorUsuarioId
            )
            // insertar() devuelve el id que Room le asignó; se agrega a la copia que se entrega
            val idNuevo = dao.insertar(solicitud)
            Result.success(solicitud.copy(id = idNuevo.toInt()))
        } catch (e: CancellationException) {
            // Si la corrutina se cancela (por ejemplo, se cierra la pantalla),
            // hay que dejar pasar esa señal y no tratarla como un error.
            throw e
        } catch (e: Exception) {
            // Cualquier otro fallo (por ejemplo, un problema con la base de datos)
            // se devuelve como error controlado
            Result.failure(e)
        }
    }

    // Guarda los cambios de una solicitud que ya existe.
    // Solo se cambian los 4 datos básicos: título, descripción, categoría y servicio.
    // El código, la fecha, el estado y la foto se conservan tal como estaban:
    // por eso primero se lee la solicitud guardada y se le aplica copy().
    // Los datos ya llegan validados desde el ViewModel (igual que en guardarSolicitud).
    // Devuelve Result: si salió bien no trae nada (Unit); si falló, trae el error.
    suspend fun actualizarSolicitud(
        id: Int,
        usuarioId: Int,
        rol: String,
        titulo: String,
        descripcion: String,
        categoria: String,
        servicio: String
    ): Result<Unit> {
        return try {
            // Se lee la solicitud actual; si no existe, no hay nada que actualizar
            if (rol == "tecnico") {
                return Result.failure(SolicitudNoEncontradaException())
            }

            val actual = dao.obtenerPorId(id, usuarioId, rol)
                ?: return Result.failure(SolicitudNoEncontradaException())

            // copy() crea una copia con los 4 campos nuevos; el resto queda igual
            val modificada = actual.copy(
                titulo = titulo.trim(),
                descripcion = descripcion.trim(),
                categoria = categoria,
                servicio = servicio.trim()
            )

            // actualizar() devuelve cuántas filas cambió: 0 significa que no encontró el id
            val filasCambiadas = dao.actualizar(modificada)
            if (filasCambiadas > 0) {
                Result.success(Unit)
            } else {
                Result.failure(SolicitudNoEncontradaException())
            }
        } catch (e: CancellationException) {
            // Si la corrutina se cancela, hay que dejar pasar esa señal y no tratarla como error
            throw e
        } catch (e: Exception) {
            // Cualquier otro fallo se devuelve como error controlado
            Result.failure(e)
        }
    }

    // El DAO solo actualiza si el id seleccionado pertenece a un usuario técnico.
    suspend fun asignarTecnico(solicitudId: Int, tecnicoId: Int): Result<Unit> {
        return try {
            val filas = dao.asignarTecnico(solicitudId, tecnicoId)
            if (filas > 0) Result.success(Unit)
            else Result.failure(SolicitudNoEncontradaException())
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Genera un código como "SOL-1A2B3C4D".
    // UUID crea un texto aleatorio; tomamos 8 caracteres y los pasamos a mayúsculas.
    private fun generarCodigo(): String {
        return "SOL-" + UUID.randomUUID().toString().take(8).uppercase()
    }
}
