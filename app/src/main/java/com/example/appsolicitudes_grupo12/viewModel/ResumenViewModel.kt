package com.example.appsolicitudes_grupo12.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.appsolicitudes_grupo12.model.data.local.AppDatabase
import com.example.appsolicitudes_grupo12.model.data.local.SolicitudEntity
import com.example.appsolicitudes_grupo12.model.data.local.UsuarioEntity
import com.example.appsolicitudes_grupo12.model.data.repository.SolicitudRepository
import com.example.appsolicitudes_grupo12.model.data.repository.UsuarioRepository
import com.example.appsolicitudes_grupo12.model.exportacion.ExportacionResumen
import com.example.appsolicitudes_grupo12.model.exportacion.ReporteSimulado
import com.example.appsolicitudes_grupo12.model.solicitud.EstadoSolicitud
import com.example.appsolicitudes_grupo12.model.solicitud.MedicionTiempos
import com.example.appsolicitudes_grupo12.model.solicitud.PlazosSolicitud
import com.example.appsolicitudes_grupo12.model.solicitud.PrioridadSolicitud
import com.example.appsolicitudes_grupo12.state.ResumenUiState
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

// ViewModel del panel resumen (solo administrador): cuenta las solicitudes por estado, prioridad
// y responsable, las atrasadas y los tiempos simulados, y arma la exportación ficticia.
class ResumenViewModel(
    private val repositorio: SolicitudRepository,
    private val repositorioUsuarios: UsuarioRepository,
    private val rol: String
) : ViewModel() {

    private val _estado = MutableStateFlow<ResumenUiState>(ResumenUiState.Loading)
    val estado: StateFlow<ResumenUiState> = _estado.asStateFlow()

    // Reporte de la exportación ficticia (null = no hay ninguno que mostrar)
    private val _reporte = MutableStateFlow<ReporteSimulado?>(null)
    val reporte: StateFlow<ReporteSimulado?> = _reporte.asStateFlow()

    // Lectura en curso, para cancelarla antes de reintentar
    private var trabajoLectura: Job? = null

    // Últimos datos leídos, para la exportación
    private var solicitudesActuales: List<SolicitudEntity> = emptyList()
    private var tecnicos: List<UsuarioEntity> = emptyList()

    init {
        cargarResumen()
    }

    // ---------- Lectura ----------

    // Lee todas las solicitudes y cuenta por estado. También la usa "Reintentar".
    fun cargarResumen() {
        if (rol != "administrador") {
            _estado.value = ResumenUiState.Error("Solo el administrador puede ver el panel resumen.")
            return
        }

        trabajoLectura?.cancel()
        _estado.value = ResumenUiState.Loading

        trabajoLectura = viewModelScope.launch {
            // Nombres de los técnicos para el indicador por responsable
            tecnicos = repositorioUsuarios.obtenerTecnicos().getOrDefault(emptyList())

            repositorio.obtenerTodasParaAdministracion()
                .catch { error ->
                    Log.e("ResumenVM", "Error al leer las solicitudes", error)
                    _estado.value = ResumenUiState.Error(
                        "No se pudo cargar el resumen. Inténtalo de nuevo."
                    )
                }
                .collect { solicitudes ->
                    solicitudesActuales = solicitudes
                    val ahora = System.currentTimeMillis()
                    _estado.value = ResumenUiState.Success(
                        total = solicitudes.size,
                        porEstado = EstadoSolicitud.entries.map { estado ->
                            estado to solicitudes.count { it.estado == estado.texto }
                        },
                        atrasadas = solicitudes.count { PlazosSolicitud.estaAtrasada(it, ahora) },
                        porPrioridad = PrioridadSolicitud.entries.map { prioridad ->
                            prioridad to solicitudes.count { it.prioridad == prioridad.texto }
                        },
                        porResponsable = contarPorResponsable(solicitudes),
                        tiempos = MedicionTiempos.resumir(solicitudes, ahora)
                    )
                }
        }
    }

    // Cantidad de solicitudes de cada técnico, más las que aún no tienen responsable.
    private fun contarPorResponsable(solicitudes: List<SolicitudEntity>): List<Pair<String, Int>> {
        val porTecnico = solicitudes.mapNotNull { it.asignadoAUsuarioId }.groupingBy { it }.eachCount()
        val sinAsignar = solicitudes.count { it.asignadoAUsuarioId == null }

        val conocidos = tecnicos.map { it.nombre to (porTecnico[it.id] ?: 0) }
        val desconocidos = porTecnico
            .filterKeys { id -> tecnicos.none { it.id == id } }
            .map { (id, cantidad) -> "Técnico #$id" to cantidad }

        return conocidos + desconocidos + ("Sin asignar" to sinAsignar)
    }

    // ---------- Exportación ficticia ----------

    // Arma el reporte en memoria y lo deja para mostrarlo. No se envía ni se guarda en ninguna parte.
    fun exportarResumen() {
        _reporte.value = ExportacionResumen.generar(
            solicitudes = solicitudesActuales,
            nombreResponsable = { id ->
                when (id) {
                    null -> "Sin asignar"
                    else -> tecnicos.firstOrNull { it.id == id }?.nombre ?: "Técnico #$id"
                }
            }
        )
    }

    // Cierra el aviso de la exportación.
    fun cerrarReporte() {
        _reporte.value = null
    }

    // ---------- Fábrica ----------

    companion object {
        fun factory(context: Context, rol: String): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    val baseDatos = AppDatabase.getDatabase(context)
                    return ResumenViewModel(
                        SolicitudRepository(baseDatos),
                        UsuarioRepository(baseDatos.usuarioDao()),
                        rol
                    ) as T
                }
            }
    }
}