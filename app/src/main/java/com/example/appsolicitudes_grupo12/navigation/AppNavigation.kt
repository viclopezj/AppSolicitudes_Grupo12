package com.example.appsolicitudes_grupo12.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.appsolicitudes_grupo12.ui.screen.ListaSolicitudesScreen
import com.example.appsolicitudes_grupo12.ui.screen.SolicitudFormScreen

// Nombres de las rutas (las "direcciones" de cada pantalla).
// Se guardan como constantes para no equivocarse al escribirlas en varios lugares.
object Rutas {
    const val LISTA = "lista"
    const val FORMULARIO = "formulario"
}

// Define qué pantallas existen y cómo se llega de una a otra.
// MainActivity solo llama a esta función.
@Composable
fun AppNavigation() {

    // El NavController es quien ejecuta los cambios de pantalla.
    // rememberNavController lo conserva mientras la app está abierta.
    val navController = rememberNavController()

    // NavHost es el contenedor donde se dibuja la pantalla actual.
    // startDestination indica cuál se muestra primero: la lista.
    NavHost(
        navController = navController,
        startDestination = Rutas.LISTA
    ) {

        // Pantalla 1: lista de solicitudes
        composable(Rutas.LISTA) {
            ListaSolicitudesScreen(
                // El botón + lleva al formulario
                onAgregarClick = { navController.navigate(Rutas.FORMULARIO) }
            )
        }

        // Pantalla 2: formulario para crear una solicitud
        composable(Rutas.FORMULARIO) {
            SolicitudFormScreen(
                // Al guardar, se cierra el formulario y se vuelve a la lista
                onGuardado = { navController.popBackStack() },
                // La flecha de la barra superior también vuelve a la lista
                onVolver = { navController.popBackStack() }
            )
        }
    }
}