package com.example.appsolicitudes_grupo12.navigation

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.appsolicitudes_grupo12.ui.screen.ListaSolicitudesScreen
import com.example.appsolicitudes_grupo12.ui.screen.LoginScreen
import com.example.appsolicitudes_grupo12.ui.screen.MenuScreen
import com.example.appsolicitudes_grupo12.ui.screen.SolicitudFormScreen

// Nombres de las rutas (las "direcciones" de cada pantalla).
// Se guardan como constantes para no equivocarse al escribirlas en varios lugares.
object Rutas {
    const val LOGIN = "login"
    const val LISTA = "lista"
    const val FORMULARIO = "formulario"

    // La ruta del menú lleva un dato: el nombre de la persona. {nombre} es el hueco.
    const val MENU = "menu/{nombre}"

    // Arma la ruta real del menú, por ejemplo "menu/Usuario%20Demo".
    // Uri.encode convierte los espacios y símbolos para que la ruta sea válida.
    fun menuConNombre(nombre: String): String = "menu/${Uri.encode(nombre)}"
}

// Define qué pantallas existen y cómo se llega de una a otra.
// MainActivity solo llama a esta función.
// Flujo: Login -> Menú -> Lista -> Formulario.
@Composable
fun AppNavigation() {

    // El NavController es quien ejecuta los cambios de pantalla.
    // rememberNavController lo conserva mientras la app está abierta.
    val navController = rememberNavController()

    // NavHost es el contenedor donde se dibuja la pantalla actual.
    // startDestination indica cuál se muestra primero: el login.
    NavHost(
        navController = navController,
        startDestination = Rutas.LOGIN
    ) {

        // Pantalla 1: login
        composable(Rutas.LOGIN) {
            LoginScreen(
                // Si las credenciales son correctas, se va al menú con el nombre de la persona
                onLoginExitoso = { nombre ->
                    navController.navigate(Rutas.menuConNombre(nombre)) {
                        // Se borra el login del historial: con "atrás" no se vuelve a él
                        popUpTo(Rutas.LOGIN) { inclusive = true }
                    }
                }
            )
        }

        // Pantalla 2: menú (con menú lateral)
        composable(
            route = Rutas.MENU,
            arguments = listOf(navArgument("nombre") { type = NavType.StringType })
        ) { entrada ->
            // Se lee el nombre que viajaba en la ruta
            val nombre = entrada.arguments?.getString("nombre") ?: ""

            MenuScreen(
                nombreUsuario = nombre,
                onIrALista = { navController.navigate(Rutas.LISTA) },
                onIrAFormulario = { navController.navigate(Rutas.FORMULARIO) },
                // Cerrar sesión: vuelve al login y borra el menú del historial
                onCerrarSesion = {
                    navController.navigate(Rutas.LOGIN) {
                        popUpTo(Rutas.MENU) { inclusive = true }
                    }
                }
            )
        }

        // Pantalla 3: lista de solicitudes
        composable(Rutas.LISTA) {
            ListaSolicitudesScreen(
                // El botón + lleva al formulario
                onAgregarClick = { navController.navigate(Rutas.FORMULARIO) }
            )
        }

        // Pantalla 4: formulario para crear una solicitud
        composable(Rutas.FORMULARIO) {
            SolicitudFormScreen(
                // Al guardar, se cierra el formulario y se muestra la lista.
                // Funciona igual si se entró desde el menú o desde la lista.
                onGuardado = {
                    navController.navigate(Rutas.LISTA) {
                        popUpTo(Rutas.FORMULARIO) { inclusive = true }
                        launchSingleTop = true   // si la lista ya está arriba, no la duplica
                    }
                },
                // La flecha de la barra superior vuelve a la pantalla anterior
                onVolver = { navController.popBackStack() }
            )
        }
    }
}