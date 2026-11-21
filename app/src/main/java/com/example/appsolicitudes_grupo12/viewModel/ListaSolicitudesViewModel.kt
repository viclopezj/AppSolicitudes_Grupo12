package com.example.appsolicitudes_grupo12.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.appsolicitudes_grupo12.model.data.local.AppDatabase
import com.example.appsolicitudes_grupo12.model.data.local.SolicitudEntity
import com.example.appsolicitudes_grupo12.model.data.repository.SolicitudRepository
import com.example.appsolicitudes_grupo12.state.FiltroSolicitudes
import com.example.appsolicitudes_grupo12.state.ListaSolicitudesUiState
import com.example.appsolicitudes_grupo12.state.OrdenSolicitudes
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import com.example.appsolicitudes_grupo12.model.data.local.UsuarioEntity
import com.example.appsolicitudes_grupo12.model.data.repository.UsuarioRepository

// ViewModel de la lista: lee las solicitudes visibles para el usuario, aplica filtro y orden,
// y las convierte en Loading, Success, Empty o Error.
class ListaSolicitudesViewModel(
    private val repositorio: SolicitudRepository,
    private val repositorioUsuarios: UsuarioRepository,
    private val usuarioId: Int,
    private val rol: String
) : ViewModel() {

    // Parte en Loading mientras llegan los datos
    private val _estado = MutableStateFlow<ListaSolicitudesUiState>(
        ListaSolicitudesUiState.Loading
    )
    val estado: StateFlow<ListaSolicitudesUiState> = _estado.asStateFlow()

    // Filtro y orden elegidos (sin filtro y más recientes primero al inicio)
    private val _filtro = MutableStateFlow(FiltroSolicitudes())

    // Lectura en curso, para cancelarla antes de reintentar
    private var trabajoLectura: Job? = null

    // Cuentas para los filtros de cliente y responsable (solo las usa el administrador)
    private val _clientes = MutableStateFlow<List<UsuarioEntity>>(emptyList())
    val clientes: StateFlow<List<UsuarioEntity>> = _clientes.asStateFlow()

    private val _tecnicos = MutableStateFlow<List<UsuarioEntity>>(emptyList())
    val tecnicos: StateFlow<List<UsuarioEntity>> = _tecnicos.asStateFlow()

    init {
        cargarSolicitudes()
        cargarUsuariosParaFiltros()
    }

    // ---------- Lectura ----------

    // Lee las solicitudes de Room. También la usa el botón "Reintentar".
    fun cargarSolicitudes() {
        trabajoLectura?.cancel()
        _estado.value = ListaSolicitudesUiState.Loading

        trabajoLectura = viewModelScope.launch {
            // combine avisa cuando cambia la lista de Room o el filtro
            combine(repositorio.obtenerSolicitudes(usuarioId, rol), _filtro) { lista, filtro ->
                lista to filtro
            }
                .catch { error ->
                    Log.e("ListaSolicitudesVM", "Error al leer las solicitudes", error)
                    _estado.value = ListaSolicitudesUiState.Error(
                        "No se pudieron cargar las solicitudes. Inténtalo de nuevo."
                    )
                }
                .collect { (lista, filtro) ->
                    _estado.value = if (lista.isEmpty()) {
                        ListaSolicitudesUiState.Empty
                    } else {
                        ListaSolicitudesUiState.Success(
                            solicitudes = aplicarFiltro(lista, filtro),
                            filtro = filtro
                        )
                    }
                }
        }
    }

    // Lee clientes y técnicos para los chips de filtro (solo para el administrador).
    private fun cargarUsuariosParaFiltros() {
        if (rol != "administrador") return
        viewModelScope.launch {
            repositorioUsuarios.obtenerClientes()
                .onSuccess { _clientes.value = it }
                .onFailure { Log.e("ListaSolicitudesVM", "Error al leer los clientes", it) }
            repositorioUsuarios.obtenerTecnicos()
                .onSuccess { _tecnicos.value = it }
                .onFailure { Log.e("ListaSolicitudesVM", "Error al leer los técnicos", it) }
        }
    }

    // ---------- Eventos de los filtros ----------
    // En todos, null = sin filtro.

    fun onCategoriaFiltroChange(categoria: String?) {
        _filtro.update { it.copy(categoria = categoria) }
    }

    fun onEstadoFiltroChange(estado: String?) {
        _filtro.update { it.copy(estado = estado) }
    }

    fun onPrioridadFiltroChange(prioridad: String?) {
        _filtro.update { it.copy(prioridad = prioridad) }
    }

    fun onClienteFiltroChange(clienteId: Int?) {
        _filtro.update { it.copy(clienteId = clienteId) }
    }

    // Para "sin técnico" se usa FiltroSolicitudes.SIN_ASIGNAR.
    fun onResponsableFiltroChange(responsableId: Int?) {
        _filtro.update { it.copy(responsableId = responsableId) }
    }

    // Cambia el texto de la búsqueda.
    fun onTextoChange(texto: String) {
        _filtro.update { it.copy(texto = texto) }
    }

    // Cambia el orden por fecha.
    fun onOrdenChange(orden: OrdenSolicitudes) {
        _filtro.update { it.copy(orden = orden) }
    }

    // Quita todos los filtros y conserva el orden elegido.
    fun limpiarFiltros() {
        _filtro.update { FiltroSolicitudes(orden = it.orden) }
    }

    // ---------- Reglas del filtrado ----------

    // Deja solo las solicitudes que cumplen todos los filtros y las ordena por fecha.
    private fun aplicarFiltro(
        lista: List<SolicitudEntity>,
        filtro: FiltroSolicitudes
    ): List<SolicitudEntity> {
        val filtradas = lista.filter { solicitud ->
            (filtro.categoria == null || solicitud.categoria == filtro.categoria) &&
                    (filtro.estado == null || solicitud.estado == filtro.estado) &&
                    (filtro.prioridad == null || solicitud.prioridad == filtro.prioridad) &&
                    (filtro.clienteId == null || solicitud.creadoPorUsuarioId == filtro.clienteId) &&
                    coincideResponsable(solicitud, filtro.responsableId) &&
                    coincideTexto(solicitud, filtro.texto)
        }

        return when (filtro.orden) {
            OrdenSolicitudes.RECIENTES -> filtradas.sortedByDescending { it.fechaCreacion }
            OrdenSolicitudes.ANTIGUAS -> filtradas.sortedBy { it.fechaCreacion }
        }
    }

    // null = sin filtro; SIN_ASIGNAR = sin técnico; otro valor = ese técnico.
    private fun coincideResponsable(solicitud: SolicitudEntity, responsableId: Int?): Boolean {
        return when (responsableId) {
            null -> true
            FiltroSolicitudes.SIN_ASIGNAR -> solicitud.asignadoAUsuarioId == null
            else -> solicitud.asignadoAUsuarioId == responsableId
        }
    }

    // Sin texto no filtra. Busca en código, título, descripción y categoría, sin distinguir mayúsculas.
    private fun coincideTexto(solicitud: SolicitudEntity, texto: String): Boolean {
        val buscado = texto.trim()
        if (buscado.isEmpty()) return true
        return listOf(
            solicitud.codigo,
            solicitud.titulo,
            solicitud.descripcion,
            solicitud.categoria
        ).any { it.contains(buscado, ignoreCase = true) }
    }

    // ---------- Fábrica ----------

    companion object {
        fun factory(context: Context, usuarioId: Int, rol: String): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    val baseDatos = AppDatabase.getDatabase(context)
                    return ListaSolicitudesViewModel(
                        SolicitudRepository(baseDatos),
                        UsuarioRepository(baseDatos.usuarioDao()),
                        usuarioId,
                        rol
                    ) as T
            }
    }
}

}