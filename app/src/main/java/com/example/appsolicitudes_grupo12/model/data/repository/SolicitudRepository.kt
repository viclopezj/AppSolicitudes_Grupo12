package com.example.appsolicitudes_grupo12.model.data.repository

import androidx.room3.withWriteTransaction
import com.example.appsolicitudes_grupo12.model.data.local.AppDatabase
import com.example.appsolicitudes_grupo12.model.data.local.ComentarioSolicitudDao
import com.example.appsolicitudes_grupo12.model.data.local.ComentarioSolicitudEntity
import com.example.appsolicitudes_grupo12.model.data.local.HistorialSolicitudDao
import com.example.appsolicitudes_grupo12.model.data.local.HistorialSolicitudEntity
import com.example.appsolicitudes_grupo12.model.data.local.SolicitudDao
import com.example.appsolicitudes_grupo12.model.data.local.SolicitudEntity
import com.example.appsolicitudes_grupo12.model.data.local.UsuarioDao
import com.example.appsolicitudes_grupo12.model.solicitud.EstadoSolicitud
import com.example.appsolicitudes_grupo12.model.solicitud.PrioridadSolicitud
import com.example.appsolicitudes_grupo12.model.solicitud.ReglasTransicion
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import com.example.appsolicitudes_grupo12.model.aviso.AvisoSolicitud
import com.example.appsolicitudes_grupo12.model.aviso.Avisos
import com.example.appsolicitudes_grupo12.model.aviso.Notificador
import com.example.appsolicitudes_grupo12.model.aviso.NotificadorNulo
import kotlinx.coroutines.flow.flow
import java.util.UUID

// La solicitud buscada no existe o el usuario no tiene acceso (distinto de un fallo técnico).
class SolicitudNoEncontradaException : Exception("La solicitud no existe")

// El perfil no puede hacer ese cambio en el estado actual.
class TransicionNoPermitidaException : Exception("Ese cambio no está permitido")

// Cancelar, pedir información y resolver exigen escribir un mensaje.
class MotivoObligatorioException : Exception("Debes escribir un mensaje")

// Intermediario entre los ViewModels y la base de datos de solicitudes.
// Los cambios y su fila de historial se guardan juntos en una transacción.
class SolicitudRepository(
    private val db: AppDatabase,
    private val dao: SolicitudDao = db.solicitudDao(),
    private val historialDao: HistorialSolicitudDao = db.historialSolicitudDao(),
    private val comentarioDao: ComentarioSolicitudDao = db.comentarioSolicitudDao(),
    private val usuarioDao: UsuarioDao = db.usuarioDao(),
    private val notificador: Notificador = NotificadorNulo
) {

    // ---------- Lecturas ----------

    // Solicitudes visibles para el usuario. Se vuelve a emitir al cambiar los datos.
    fun obtenerSolicitudes(usuarioId: Int, rol: String): Flow<List<SolicitudEntity>> {
        return dao.obtenerVisibles(usuarioId, rol)
    }

    // Todas las solicitudes (solo para la pantalla de asignaciones).
    fun obtenerTodasParaAdministracion(): Flow<List<SolicitudEntity>> {
        return dao.obtenerTodas()
    }

    // Una solicitud por id (pantalla de detalle).
    suspend fun obtenerSolicitud(id: Int, usuarioId: Int, rol: String): Result<SolicitudEntity> {
        return try {
            val solicitud = dao.obtenerPorId(id, usuarioId, rol)
            if (solicitud != null) {
                Result.success(solicitud)
            } else {
                Result.failure(SolicitudNoEncontradaException())
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Una solicitud por código (lo usa el escáner QR).
    suspend fun buscarPorCodigo(codigo: String, usuarioId: Int, rol: String): Result<SolicitudEntity> {
        return try {
            // Los códigos se generan siempre en mayúsculas
            val solicitud = dao.obtenerPorCodigoVisible(codigo.trim().uppercase(), usuarioId, rol)
            if (solicitud != null) {
                Result.success(solicitud)
            } else {
                Result.failure(SolicitudNoEncontradaException())
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Historial de una solicitud, del más antiguo al más reciente. Vacío si no hay acceso.
    fun obtenerHistorial(
        solicitudId: Int,
        usuarioId: Int,
        rol: String
    ): Flow<List<HistorialSolicitudEntity>> = flow {
        if (dao.obtenerPorId(solicitudId, usuarioId, rol) != null) {
            emitAll(historialDao.obtenerPorSolicitud(solicitudId))
        } else {
            emit(emptyList())
        }
    }

    // Comentarios de una solicitud. El cliente no recibe los internos. Vacío si no hay acceso.
    fun obtenerComentarios(
        solicitudId: Int,
        usuarioId: Int,
        rol: String
    ): Flow<List<ComentarioSolicitudEntity>> = flow {
        if (dao.obtenerPorId(solicitudId, usuarioId, rol) != null) {
            emitAll(comentarioDao.obtenerVisibles(solicitudId, rol))
        } else {
            emit(emptyList())
        }
    }

    // ---------- Escritura ----------

    // Crea la solicitud con código único, fecha y estado inicial, y deja su primer registro de historial.
    // Devuelve la solicitud completa porque con sus datos se dibuja el QR.
    suspend fun guardarSolicitud(
        titulo: String,
        descripcion: String,
        categoria: String,
        servicio: String,
        creadorUsuarioId: Int,
        evidenciaUri: String? = null
    ): Result<SolicitudEntity> {
        return enTransaccion {
            val ahora = System.currentTimeMillis()
            val solicitud = SolicitudEntity(
                codigo = generarCodigo(),
                titulo = titulo.trim(),
                descripcion = descripcion.trim(),
                categoria = categoria,
                servicio = servicio.trim(),
                evidenciaUri = evidenciaUri,
                estado = EstadoSolicitud.RECIBIDA.texto,
                fechaCreacion = ahora,
                creadoPorUsuarioId = creadorUsuarioId
            )
            val idNuevo = dao.insertar(solicitud).toInt()

            registrarHistorial(
                solicitudId = idNuevo,
                estadoAnterior = null,
                estadoNuevo = EstadoSolicitud.RECIBIDA.texto,
                accion = ACCION_REGISTRO,
                comentario = null,
                usuarioId = creadorUsuarioId,
                rol = ROL_CLIENTE,
                fecha = ahora
            )
            solicitud.copy(id = idNuevo)
        }.avisar { Avisos.nuevaSolicitud(it.codigo) }
    }

    // Cambia título, descripción, categoría y servicio. Código, fecha, estado y foto se conservan.
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
            // El técnico no edita solicitudes
            if (rol == ROL_TECNICO) {
                return Result.failure(SolicitudNoEncontradaException())
            }

            val actual = dao.obtenerPorId(id, usuarioId, rol)
                ?: return Result.failure(SolicitudNoEncontradaException())

            val modificada = actual.copy(
                titulo = titulo.trim(),
                descripcion = descripcion.trim(),
                categoria = categoria,
                servicio = servicio.trim(),
                ultimaActualizacion = System.currentTimeMillis()
            )

            // actualizar() devuelve las filas cambiadas: 0 = no encontró el id
            val filasCambiadas = dao.actualizar(modificada)
            if (filasCambiadas > 0) {
                Result.success(Unit)
            } else {
                Result.failure(SolicitudNoEncontradaException())
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Asigna o reasigna un técnico. Solo el administrador.
    // Desde Recibida o En revisión pasa a Asignada; desde Asignada o En proceso solo cambia el técnico.
    suspend fun asignarTecnico(
        solicitudId: Int,
        tecnicoId: Int,
        usuarioId: Int,
        rol: String
    ): Result<Unit> {
        return enTransaccion<AvisoSolicitud?> {
            if (rol != ROL_ADMINISTRADOR) throw TransicionNoPermitidaException()

            val solicitud = dao.obtenerPorId(solicitudId, usuarioId, rol)
                ?: throw SolicitudNoEncontradaException()
            val tecnico = usuarioDao.buscarPorId(tecnicoId)
            if (tecnico == null || tecnico.rol != ROL_TECNICO) {
                throw IllegalArgumentException("El usuario elegido no es técnico")
            }

            val estadoActual = EstadoSolicitud.desdeTexto(solicitud.estado)
                ?: throw TransicionNoPermitidaException()
            val esAsignacionInicial = estadoActual == EstadoSolicitud.RECIBIDA ||
                    estadoActual == EstadoSolicitud.EN_REVISION

            if (!esAsignacionInicial && !ReglasTransicion.puedeReasignar(rol, estadoActual)) {
                throw TransicionNoPermitidaException()
            }

            // Aviso para el técnico (solo si algo cambió)
            var aviso: AvisoSolicitud? = null

            // Si elige el mismo técnico en una reasignación, no hay nada que guardar
            if (esAsignacionInicial || solicitud.asignadoAUsuarioId != tecnicoId) {
                val estadoNuevo = if (esAsignacionInicial) EstadoSolicitud.ASIGNADA else estadoActual
                val ahora = System.currentTimeMillis()

                dao.actualizar(
                    solicitud.copy(
                        asignadoAUsuarioId = tecnicoId,
                        estado = estadoNuevo.texto,
                        ultimaActualizacion = ahora
                    )
                )
                registrarHistorial(
                    solicitudId = solicitudId,
                    estadoAnterior = estadoActual.texto,
                    estadoNuevo = estadoNuevo.texto,
                    accion = if (esAsignacionInicial) ACCION_ASIGNACION else ACCION_REASIGNACION,
                    comentario = "Asignada a ${tecnico.nombre}",
                    usuarioId = usuarioId,
                    rol = rol,
                    fecha = ahora
                )
                aviso = Avisos.asignada(solicitud.codigo)
            }
            aviso
        }.avisar { it }.map { }
    }

    // Cambia el estado si el perfil puede hacerlo (ReglasTransicion).
    // El mensaje queda en el historial y, salvo al cancelar, también como comentario visible.
    // Asignar un técnico se hace con asignarTecnico, no con esta función.
    suspend fun cambiarEstado(
        id: Int,
        usuarioId: Int,
        rol: String,
        nuevoEstado: EstadoSolicitud,
        comentario: String? = null,
        evidenciaUri: String? = null
    ): Result<Unit> {
        return enTransaccion<AvisoSolicitud?> {
            val solicitud = dao.obtenerPorId(id, usuarioId, rol)
                ?: throw SolicitudNoEncontradaException()
            val estadoActual = EstadoSolicitud.desdeTexto(solicitud.estado)
                ?: throw TransicionNoPermitidaException()

            if (nuevoEstado == EstadoSolicitud.ASIGNADA ||
                !ReglasTransicion.puedeCambiar(rol, estadoActual, nuevoEstado)
            ) {
                throw TransicionNoPermitidaException()
            }

            // El cliente responde la información con responderInformacion, que guarda su respuesta
            if (rol == ROL_CLIENTE && estadoActual == EstadoSolicitud.PENDIENTE_INFORMACION) {
                throw TransicionNoPermitidaException()
            }

            val motivo = comentario?.trim()?.takeIf { it.isNotEmpty() }
            if (ReglasTransicion.requiereMotivo(estadoActual, nuevoEstado) && motivo == null) {
                throw MotivoObligatorioException()
            }

            val ahora = System.currentTimeMillis()
            dao.actualizar(solicitud.copy(estado = nuevoEstado.texto, ultimaActualizacion = ahora))

            registrarHistorial(
                solicitudId = id,
                estadoAnterior = estadoActual.texto,
                estadoNuevo = nuevoEstado.texto,
                accion = if (nuevoEstado == EstadoSolicitud.CANCELADA) ACCION_CANCELACION else ACCION_ESTADO,
                comentario = motivo,
                usuarioId = usuarioId,
                rol = rol,
                fecha = ahora
            )

            if (motivo != null && nuevoEstado != EstadoSolicitud.CANCELADA) {
                comentarioDao.insertar(
                    ComentarioSolicitudEntity(
                        solicitudId = id,
                        usuarioId = usuarioId,
                        usuarioNombre = nombreDe(usuarioId),
                        rol = rol,
                        texto = motivo,
                        esInterno = false,
                        evidenciaUri = evidenciaUri,
                        fecha = ahora
                    )
                )
            }

            // Aviso que corresponde a este cambio (null si ese cambio no avisa)
            Avisos.porCambioDeEstado(solicitud.codigo, estadoActual, nuevoEstado)
        }.avisar { it }.map { }
    }

    // Cambia la prioridad. Solo el administrador.
    suspend fun cambiarPrioridad(
        id: Int,
        usuarioId: Int,
        rol: String,
        nuevaPrioridad: PrioridadSolicitud
    ): Result<Unit> {
        return enTransaccion<AvisoSolicitud?> {
            if (!ReglasTransicion.puedeCambiarPrioridad(rol)) throw TransicionNoPermitidaException()

            val solicitud = dao.obtenerPorId(id, usuarioId, rol)
                ?: throw SolicitudNoEncontradaException()

            // Cerrada y Cancelada ya no se modifican
            if (EstadoSolicitud.desdeTexto(solicitud.estado)?.esFinal == true) {
                throw TransicionNoPermitidaException()
            }

            val anterior = PrioridadSolicitud.desdeTexto(solicitud.prioridad)

            // Aviso para el cliente (solo si la prioridad cambió)
            var aviso: AvisoSolicitud? = null

            if (anterior != nuevaPrioridad) {
                val ahora = System.currentTimeMillis()
                dao.actualizar(
                    solicitud.copy(prioridad = nuevaPrioridad.texto, ultimaActualizacion = ahora)
                )
                registrarHistorial(
                    solicitudId = id,
                    estadoAnterior = solicitud.estado,
                    estadoNuevo = solicitud.estado,
                    accion = ACCION_PRIORIDAD,
                    comentario = "${anterior.texto} → ${nuevaPrioridad.texto}",
                    usuarioId = usuarioId,
                    rol = rol,
                    fecha = ahora
                )
                aviso = Avisos.prioridad(solicitud.codigo, nuevaPrioridad)
            }
            aviso
        }.avisar { it }.map { }
    }

    // El cliente responde la información pedida: su respuesta queda como comentario visible
    // y la solicitud pasa a En proceso (si ya tiene técnico) o a En revisión.
    suspend fun responderInformacion(
        id: Int,
        usuarioId: Int,
        rol: String,
        respuesta: String
    ): Result<Unit> {
        return enTransaccion<Unit> {
            if (rol != ROL_CLIENTE) throw TransicionNoPermitidaException()

            val solicitud = dao.obtenerPorId(id, usuarioId, rol)
                ?: throw SolicitudNoEncontradaException()
            if (EstadoSolicitud.desdeTexto(solicitud.estado) != EstadoSolicitud.PENDIENTE_INFORMACION) {
                throw TransicionNoPermitidaException()
            }

            val texto = respuesta.trim()
            if (texto.isEmpty()) throw MotivoObligatorioException()

            val destino = ReglasTransicion.destinoAlResponder(solicitud.asignadoAUsuarioId != null)
            val ahora = System.currentTimeMillis()
            dao.actualizar(solicitud.copy(estado = destino.texto, ultimaActualizacion = ahora))

            registrarHistorial(
                solicitudId = id,
                estadoAnterior = EstadoSolicitud.PENDIENTE_INFORMACION.texto,
                estadoNuevo = destino.texto,
                accion = ACCION_ESTADO,
                comentario = texto,
                usuarioId = usuarioId,
                rol = rol,
                fecha = ahora
            )

            comentarioDao.insertar(
                ComentarioSolicitudEntity(
                    solicitudId = id,
                    usuarioId = usuarioId,
                    usuarioNombre = nombreDe(usuarioId),
                    rol = rol,
                    texto = texto,
                    esInterno = false,
                    fecha = ahora
                )
            )
        }
    }

    // Agrega un comentario de seguimiento. El cliente nunca puede crear uno interno.
    suspend fun agregarComentario(
        id: Int,
        usuarioId: Int,
        rol: String,
        texto: String,
        esInterno: Boolean = false,
        evidenciaUri: String? = null
    ): Result<Unit> {
        return enTransaccion<AvisoSolicitud?> {
            val solicitud = dao.obtenerPorId(id, usuarioId, rol)
                ?: throw SolicitudNoEncontradaException()

            // El técnico solo registra avances mientras trabaja la solicitud
            if (rol == ROL_TECNICO) {
                val estadoActual = EstadoSolicitud.desdeTexto(solicitud.estado)
                    ?: throw TransicionNoPermitidaException()
                if (!ReglasTransicion.puedeRegistrarAvance(rol, estadoActual)) {
                    throw TransicionNoPermitidaException()
                }
            }

            val interno = esInterno && rol != ROL_CLIENTE
            val ahora = System.currentTimeMillis()

            comentarioDao.insertar(
                ComentarioSolicitudEntity(
                    solicitudId = id,
                    usuarioId = usuarioId,
                    usuarioNombre = nombreDe(usuarioId),
                    rol = rol,
                    texto = texto.trim(),
                    esInterno = interno,
                    evidenciaUri = evidenciaUri,
                    fecha = ahora
                )
            )

            // Un comentario interno no cambia lo que ve el cliente
            if (!interno) {
                dao.actualizar(solicitud.copy(ultimaActualizacion = ahora))
            }

            // Solo el comentario visible del técnico avisa al cliente
            if (!interno && rol == ROL_TECNICO) Avisos.nuevoComentario(solicitud.codigo) else null
        }.avisar { it }.map { }
    }

    // ---------- Utilidades ----------

    // Publica el aviso solo si el cambio se guardó bien. Devuelve el mismo resultado.
    private fun <T> Result<T>.avisar(aviso: (T) -> AvisoSolicitud?): Result<T> {
        onSuccess { valor -> aviso(valor)?.let(notificador::publicar) }
        return this
    }

    // Ejecuta el bloque en una transacción: si algo falla, no se guarda nada.
    private suspend fun <T> enTransaccion(bloque: suspend () -> T): Result<T> {
        return try {
            Result.success(db.withWriteTransaction { bloque() })
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Guarda una fila del historial con el nombre de quien hizo el cambio.
    private suspend fun registrarHistorial(
        solicitudId: Int,
        estadoAnterior: String?,
        estadoNuevo: String,
        accion: String,
        comentario: String?,
        usuarioId: Int,
        rol: String,
        fecha: Long
    ) {
        historialDao.insertar(
            HistorialSolicitudEntity(
                solicitudId = solicitudId,
                estadoAnterior = estadoAnterior,
                estadoNuevo = estadoNuevo,
                accion = accion,
                comentario = comentario,
                rol = rol,
                usuarioId = usuarioId,
                usuarioNombre = nombreDe(usuarioId),
                fecha = fecha
            )
        )
    }

    // Nombre visible del usuario; "Sin registro" si no existe.
    private suspend fun nombreDe(usuarioId: Int): String {
        return usuarioDao.buscarPorId(usuarioId)?.nombre ?: "Sin registro"
    }

    // Código como "SOL-1A2B3C4D": 8 caracteres de un UUID en mayúsculas.
    private fun generarCodigo(): String {
        return "SOL-" + UUID.randomUUID().toString().take(8).uppercase()
    }

    private companion object {
        const val ROL_CLIENTE = "cliente"
        const val ROL_ADMINISTRADOR = "administrador"
        const val ROL_TECNICO = "tecnico"

        // Textos de la columna "accion" del historial
        const val ACCION_REGISTRO = "Solicitud registrada"
        const val ACCION_ESTADO = "Cambio de estado"
        const val ACCION_ASIGNACION = "Técnico asignado"
        const val ACCION_REASIGNACION = "Técnico reasignado"
        const val ACCION_PRIORIDAD = "Prioridad modificada"
        const val ACCION_CANCELACION = "Solicitud cancelada"
    }
}