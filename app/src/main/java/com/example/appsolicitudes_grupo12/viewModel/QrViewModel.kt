package com.example.appsolicitudes_grupo12.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.appsolicitudes_grupo12.model.data.local.AppDatabase
import com.example.appsolicitudes_grupo12.model.data.repository.SolicitudNoEncontradaException
import com.example.appsolicitudes_grupo12.model.data.repository.SolicitudRepository
import com.example.appsolicitudes_grupo12.model.qr.CodigoQr
import com.example.appsolicitudes_grupo12.state.QrUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// ViewModel del escáner: valida el QR leído y busca la solicitud en este teléfono.
class QrViewModel(
    private val repositorio: SolicitudRepository,
    private val usuarioId: Int,
    private val rol: String
) : ViewModel() {

    // Parte en Escaneando: la cámara espera un QR
    private val _estado = MutableStateFlow<QrUiState>(QrUiState.Escaneando)
    val estado: StateFlow<QrUiState> = _estado.asStateFlow()

    // ---------- Eventos ----------

    // La cámara detectó un QR. "texto" es su contenido.
    fun onQrDetectado(texto: String) {
        // Si ya se está procesando algo, se ignoran las lecturas nuevas
        if (_estado.value !is QrUiState.Escaneando) return

        val datos = CodigoQr.leerTexto(texto)
        if (datos == null) {
            _estado.value = QrUiState.NoValido
            return
        }

        _estado.value = QrUiState.Buscando

        viewModelScope.launch {
            repositorio.buscarPorCodigo(datos.codigo, usuarioId, rol)
                .onSuccess { solicitud ->
                    _estado.value = QrUiState.Leida(datos = datos, idLocal = solicitud.id)
                }
                .onFailure { error ->
                    // Los datos del QR se muestran igual; solo no habrá botón "Abrir detalle"
                    if (error !is SolicitudNoEncontradaException) {
                        Log.e("QrViewModel", "Error al buscar la solicitud", error)
                    }
                    _estado.value = QrUiState.Leida(datos = datos, idLocal = null)
                }
        }
    }

    // Vuelve a dejar la cámara esperando un QR.
    fun reiniciar() {
        _estado.value = QrUiState.Escaneando
    }

    // ---------- Fábrica ----------

    companion object {
        fun factory(context: Context, usuarioId: Int, rol: String): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    val baseDatos = AppDatabase.getDatabase(context)
                    return QrViewModel(SolicitudRepository(baseDatos), usuarioId, rol) as T
                }
            }
    }
}