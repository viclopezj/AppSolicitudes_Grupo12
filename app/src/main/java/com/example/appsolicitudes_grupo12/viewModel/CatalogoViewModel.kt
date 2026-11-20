package com.example.appsolicitudes_grupo12.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.appsolicitudes_grupo12.model.data.remote.RetrofitInstance
import com.example.appsolicitudes_grupo12.model.data.repository.CatalogoRepository
import com.example.appsolicitudes_grupo12.state.CatalogoUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// ViewModel del catálogo: pide los servicios a la API y los convierte en Loading, Success, Empty o Error.
class CatalogoViewModel(
    private val repositorio: CatalogoRepository
) : ViewModel() {

    // Parte en Loading mientras llega la respuesta
    private val _estado = MutableStateFlow<CatalogoUiState>(CatalogoUiState.Loading)
    val estado: StateFlow<CatalogoUiState> = _estado.asStateFlow()

    init {
        cargarCatalogo()
    }

    // Pide el catálogo a la API. También la usa el botón "Reintentar".
    fun cargarCatalogo() {
        _estado.value = CatalogoUiState.Loading

        viewModelScope.launch {
            repositorio.obtenerCatalogo()
                .onSuccess { servicios ->
                    _estado.value = if (servicios.isEmpty()) {
                        CatalogoUiState.Empty
                    } else {
                        CatalogoUiState.Success(servicios)
                    }
                }
                .onFailure { error ->
                    Log.e("CatalogoVM", "Error al cargar el catálogo", error)
                    _estado.value = CatalogoUiState.Error(
                        "No se pudo cargar el catálogo. Revisa tu conexión e inténtalo de nuevo."
                    )
                }
        }
    }

    // ---------- Fábrica ----------

    companion object {
        fun factory(): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return CatalogoViewModel(CatalogoRepository(RetrofitInstance.api)) as T
                }
            }
    }
}