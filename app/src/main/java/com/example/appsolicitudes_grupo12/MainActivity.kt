package com.example.appsolicitudes_grupo12

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.appsolicitudes_grupo12.navigation.AppNavigation
import com.example.appsolicitudes_grupo12.ui.theme.AppSolicitudes_Grupo12Theme

// Punto de entrada de la app. Solo prepara el tema y entrega el control a la navegación.
// AppNavigation decide qué pantalla se muestra: primero la lista y, desde ahí, el formulario.
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()   // la app se dibuja bajo las barras del sistema
        setContent {
            AppSolicitudes_Grupo12Theme {
                AppNavigation()
            }
        }
    }
}