package com.example.appsolicitudes_grupo12.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.appsolicitudes_grupo12.model.aviso.NotificadorAndroid
import com.example.appsolicitudes_grupo12.model.data.local.AppDatabase
import com.example.appsolicitudes_grupo12.model.data.repository.SolicitudRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// ViewModel del menú de inicio: al entrar revisa las solicitudes atrasadas (plazo vencido)
// y avisa con una notificación local. Administrador y técnico; el cliente no recibe este aviso.
class MenuViewModel(
    private val repositorio: SolicitudRepository,
    private val usuarioId: Int,
    private val rol: String
) : ViewModel() {

    // Cantidad de solicitudes atrasadas que ve este usuario (también se muestra en el menú)
    private val _atrasadas = MutableStateFlow(0)
    val atrasadas: StateFlow<Int> = _atrasadas.asStateFlow()

    init {
        revisarAtrasadas()
    }

    private fun revisarAtrasadas() {
        if (usuarioId <= 0 || (rol != "administrador" && rol != "tecnico")) return

        viewModelScope.launch {
            repositorio.avisarSolicitudesAtrasadas(usuarioId, rol)
                .onSuccess { _atrasadas.value = it }
                .onFailure { Log.e("MenuVM", "Error al revisar las solicitudes atrasadas", it) }
        }
    }

    // ---------- Fábrica ----------

    companion object {
        fun factory(context: Context, usuarioId: Int, rol: String): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    val appContext = context.applicationContext
                    val baseDatos = AppDatabase.getDatabase(appContext)
                    return MenuViewModel(
                        SolicitudRepository(baseDatos, notificador = NotificadorAndroid(appContext)),
                        usuarioId,
                        rol
                    ) as T
                }
            }
    }
}
