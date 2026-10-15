package com.example.appsolicitudes_grupo12

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.appsolicitudes_grupo12.ui.screen.SolicitudFormScreen
import com.example.appsolicitudes_grupo12.ui.theme.AppSolicitudes_Grupo12Theme

// Punto de entrada de la app. Solo prepara el tema y muestra la primera pantalla.
// Por ahora muestra el formulario; cuando exista la lista, aquí se llamará
// a la navegación (AppNavigation).
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()   // la app se dibuja bajo las barras del sistema
        setContent {
            AppSolicitudes_Grupo12Theme {
                SolicitudFormScreen()
            }
        }
    }
}