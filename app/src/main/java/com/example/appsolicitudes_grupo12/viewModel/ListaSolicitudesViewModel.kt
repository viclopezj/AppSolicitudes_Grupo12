package com.example.appsolicitudes_grupo12.viewModel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.appsolicitudes_grupo12.data.local.AppDatabase
import com.example.appsolicitudes_grupo12.data.local.SolicitudEntity
import com.example.appsolicitudes_grupo12.data.repository.SolicitudRepository
import com.example.appsolicitudes_grupo12.model.FiltroSolicitudes
import com.example.appsolicitudes_grupo12.model.ListaSolicitudesUiState
import com.example.appsolicitudes_grupo12.model.OrdenSolicitudes
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// ViewModel de la pantalla de lista. Lee las solicitudes guardadas, les aplica el filtro
// y el orden elegidos, y las convierte en uno de los 4 estados: Loading, Success, Empty o Error.
// Es independiente de SolicitudViewModel (el del formulario), pero ambos usan
// la MISMA base de datos, porque AppDatabase entrega siempre una única instancia.
class ListaSolicitudesViewModel(app: Application) : AndroidViewModel(app) {

    // Cadena de acceso a datos: Database -> DAO -> Repository
    private val repositorio = SolicitudRepository(
        AppDatabase.getDatabase(app).solicitudDao()
    )

    // Estado privado y modificable. Parte en Loading mientras llegan los datos.
    private val _estado = MutableStateFlow<ListaSolicitudesUiState>(
        ListaSolicitudesUiState.Loading
    )

    // Estado público de solo lectura: la pantalla lo observa
    val estado: StateFlow<ListaSolicitudesUiState> = _estado.asStateFlow()

    // Filtro y orden elegidos. Parte sin filtro y con las más recientes primero.
    // Es privado: la pantalla solo lo ve dentro del estado Success.
    private val _filtro = MutableStateFlow(FiltroSolicitudes())

    // Guarda la lectura en curso, para poder cancelarla antes de reintentar
    private var trabajoLectura: Job? = null

    // Se ejecuta al crear el ViewModel: empieza a leer las solicitudes
    init {
        cargarSolicitudes()
    }

    // Lee las solicitudes de Room y actualiza el estado.
    // Es pública porque el botón "Reintentar" de la pantalla también la llama.
    fun cargarSolicitudes() {
        // Si había una lectura anterior, se cancela para no tener dos al mismo tiempo
        trabajoLectura?.cancel()

        // Estado Loading mientras llegan los datos
        _estado.value = ListaSolicitudesUiState.Loading

        // launch inicia una corrutina: la lectura ocurre sin congelar la pantalla
        trabajoLectura = viewModelScope.launch {
            // combine junta dos fuentes y avisa cuando CUALQUIERA cambia:
            //   - la lista de Room (cuando se guarda o edita una solicitud)
            //   - el filtro (cuando la persona elige otra categoría u otro orden)
            // Así la lista se actualiza sola en los dos casos.
            combine(repositorio.obtenerSolicitudes(), _filtro) { lista, filtro ->
                lista to filtro
            }
                .catch { error ->
                    // Estado Error: se registra en el Logcat y se muestra un mensaje amable
                    Log.e("ListaSolicitudesVM", "Error al leer las solicitudes", error)
                    _estado.value = ListaSolicitudesUiState.Error(
                        "No se pudieron cargar las solicitudes. Inténtalo de nuevo."
                    )
                }
                .collect { (lista, filtro) ->
                    _estado.value = if (lista.isEmpty()) {
                        ListaSolicitudesUiState.Empty            // no hay ninguna guardada
                    } else {
                        ListaSolicitudesUiState.Success(
                            solicitudes = aplicarFiltro(lista, filtro),
                            filtro = filtro
                        )
                    }
                }
        }
    }

    // ---------- Eventos de los filtros ----------

    // Elige una categoría para filtrar. null = mostrar todas.
    fun onCategoriaFiltroChange(categoria: String?) {
        _filtro.update { it.copy(categoria = categoria) }
    }

    // Elige si se ven primero las más recientes o las más antiguas.
    fun onOrdenChange(orden: OrdenSolicitudes) {
        _filtro.update { it.copy(orden = orden) }
    }

    // ---------- Reglas del filtrado ----------

    // Deja solo las solicitudes de la categoría elegida (si hay una) y las ordena por fecha.
    // Es una función simple que recibe una lista y devuelve otra, sin tocar la base de datos.
    private fun aplicarFiltro(
        lista: List<SolicitudEntity>,
        filtro: FiltroSolicitudes
    ): List<SolicitudEntity> {
        val categoria = filtro.categoria

        val filtradas = if (categoria == null) {
            lista
        } else {
            lista.filter { it.categoria == categoria }
        }

        return when (filtro.orden) {
            OrdenSolicitudes.RECIENTES -> filtradas.sortedByDescending { it.fechaCreacion }
            OrdenSolicitudes.ANTIGUAS -> filtradas.sortedBy { it.fechaCreacion }
        }
    }
}