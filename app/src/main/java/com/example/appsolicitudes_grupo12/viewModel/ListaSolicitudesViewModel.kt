package com.example.appsolicitudes_grupo12.viewModel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.appsolicitudes_grupo12.data.local.AppDatabase
import com.example.appsolicitudes_grupo12.data.repository.SolicitudRepository
import com.example.appsolicitudes_grupo12.model.ListaSolicitudesUiState
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

// ViewModel de la pantalla de lista. Lee las solicitudes guardadas y las convierte
// en uno de los 4 estados: Loading, Success, Empty o Error.
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
            repositorio.obtenerSolicitudes()
                .catch { error ->
                    // Estado Error: se registra en el Logcat y se muestra un mensaje amable
                    Log.e("ListaSolicitudesVM", "Error al leer las solicitudes", error)
                    _estado.value = ListaSolicitudesUiState.Error(
                        "No se pudieron cargar las solicitudes. Inténtalo de nuevo."
                    )
                }
                .collect { lista ->
                    // Cada vez que cambia la tabla (por ejemplo, al registrar una
                    // solicitud nueva), Room envía la lista actualizada y llegamos aquí.
                    _estado.value = if (lista.isEmpty()) {
                        ListaSolicitudesUiState.Empty            // no hay ninguna
                    } else {
                        ListaSolicitudesUiState.Success(lista)   // hay solicitudes
                    }
                }
        }
    }
}