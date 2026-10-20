package com.example.appsolicitudes_grupo12.viewModel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.example.appsolicitudes_grupo12.data.local.AppDatabase
import com.example.appsolicitudes_grupo12.data.repository.SolicitudNoEncontradaException
import com.example.appsolicitudes_grupo12.data.repository.SolicitudRepository
import com.example.appsolicitudes_grupo12.model.QrUiState
import com.example.appsolicitudes_grupo12.utils.QrUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// ViewModel del escáner QR. Recibe el texto que leyó la cámara, comprueba que sea
// el QR de una solicitud de esta app y revisa si esa solicitud existe en este teléfono.
// No conoce nada de Compose: la pantalla solo lo observa y le envía eventos.
// Funciona igual que los demás ViewModels: UiState + StateFlow.
class QrViewModel(app: Application, savedStateHandle: SavedStateHandle) : AndroidViewModel(app) {

    private val usuarioId = savedStateHandle.get<Int>("usuarioId") ?: -1
    private val rol = savedStateHandle.get<String>("rol").orEmpty()

    // Cadena de acceso a datos: Database -> DAO -> Repository
    private val repositorio = SolicitudRepository(
        AppDatabase.getDatabase(app).solicitudDao()
    )

    // Estado privado y modificable. Parte en Escaneando: la cámara espera un QR.
    private val _estado = MutableStateFlow<QrUiState>(QrUiState.Escaneando)

    // Estado público de solo lectura: la pantalla lo observa con collectAsState()
    val estado: StateFlow<QrUiState> = _estado.asStateFlow()

    // ---------- Eventos ----------

    // La pantalla llama a esta función cuando la cámara detecta un QR.
    // "texto" es el contenido leído del QR.
    fun onQrDetectado(texto: String) {
        // Si ya se está procesando o mostrando un resultado, se ignora cualquier lectura
        // nueva. Así un mismo QR no se procesa dos veces.
        if (_estado.value !is QrUiState.Escaneando) return

        // Se intenta convertir el texto en los datos de una solicitud.
        // Si no es un QR de esta app, leerTexto devuelve null.
        val datos = QrUtils.leerTexto(texto)
        if (datos == null) {
            _estado.value = QrUiState.NoValido
            return
        }

        // Estado Loading mientras se revisa si la solicitud existe en este teléfono
        _estado.value = QrUiState.Buscando

        // launch inicia una corrutina: la consulta ocurre sin congelar la pantalla
        viewModelScope.launch {
            repositorio.buscarPorCodigo(datos.codigo, usuarioId, rol)
                .onSuccess { solicitud ->
                    // Existe en este teléfono: se podrá abrir su detalle con este id
                    _estado.value = QrUiState.Leida(datos = datos, idLocal = solicitud.id)
                }
                .onFailure { error ->
                    // En ambos casos los datos del QR igual se muestran: solo cambia
                    // que no habrá botón "Abrir detalle" (idLocal = null).
                    if (error !is SolicitudNoEncontradaException) {
                        // Fallo técnico: se registra en el Logcat. No hace falta un estado
                        // de error, porque el QR ya trae toda la información.
                        Log.e("QrViewModel", "Error al buscar la solicitud", error)
                    }
                    // Si simplemente no existe aquí, el QR viene de otro teléfono: es normal
                    _estado.value = QrUiState.Leida(datos = datos, idLocal = null)
                }
        }
    }

    // Vuelve a dejar la cámara esperando un QR (botón "Escanear otro código").
    fun reiniciar() {
        _estado.value = QrUiState.Escaneando
    }
}
