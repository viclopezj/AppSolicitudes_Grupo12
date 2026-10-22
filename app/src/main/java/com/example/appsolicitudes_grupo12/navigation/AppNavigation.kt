package com.example.appsolicitudes_grupo12.navigation

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.appsolicitudes_grupo12.ui.screen.AsignarSolicitudesScreen
import com.example.appsolicitudes_grupo12.ui.screen.DetalleSolicitudScreen
import com.example.appsolicitudes_grupo12.ui.screen.ListaSolicitudesScreen
import com.example.appsolicitudes_grupo12.ui.screen.LoginScreen
import com.example.appsolicitudes_grupo12.ui.screen.MenuScreen
import com.example.appsolicitudes_grupo12.ui.screen.QrScannerScreen
import com.example.appsolicitudes_grupo12.ui.screen.SolicitudFormScreen
import com.example.appsolicitudes_grupo12.utils.rememberAppWindowSizeClass

object Rutas {
    const val LOGIN = "login"
    const val MENU = "menu/{nombre}/{usuarioId}/{rol}"
    const val LISTA = "lista/{usuarioId}/{rol}"
    const val FORMULARIO = "formulario/{usuarioId}/{rol}"
    const val QR = "qr/{usuarioId}/{rol}"
    const val ASIGNACIONES = "asignaciones/{usuarioId}/{rol}"
    const val DETALLE = "detalle/{id}/{usuarioId}/{rol}"

    fun menu(nombre: String, usuarioId: Int, rol: String) =
        "menu/${Uri.encode(nombre)}/$usuarioId/${Uri.encode(rol)}"

    fun lista(usuarioId: Int, rol: String) = "lista/$usuarioId/${Uri.encode(rol)}"
    fun formulario(usuarioId: Int, rol: String) = "formulario/$usuarioId/${Uri.encode(rol)}"
    fun qr(usuarioId: Int, rol: String) = "qr/$usuarioId/${Uri.encode(rol)}"
    fun asignaciones(usuarioId: Int, rol: String) =
        "asignaciones/$usuarioId/${Uri.encode(rol)}"

    fun detalle(id: Int, usuarioId: Int, rol: String) =
        "detalle/$id/$usuarioId/${Uri.encode(rol)}"
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    // Detecta el ancho disponible y se actualiza al cambiar el tamaño de ventana.
    val windowSizeClass = rememberAppWindowSizeClass()

    NavHost(navController = navController, startDestination = Rutas.LOGIN) {
        composable(Rutas.LOGIN) {
            LoginScreen(
                windowSizeClass = windowSizeClass,
                onLoginExitoso = { nombre, usuarioId, rol ->
                    navController.navigate(Rutas.menu(nombre, usuarioId, rol)) {
                        popUpTo(Rutas.LOGIN) { inclusive = true }
                    }
                }
            )
        }

        composable(
            route = Rutas.MENU,
            arguments = listOf(
                navArgument("nombre") { type = NavType.StringType },
                navArgument("usuarioId") { type = NavType.IntType },
                navArgument("rol") { type = NavType.StringType }
            )
        ) { entrada ->
            val nombre = entrada.arguments?.getString("nombre").orEmpty()
            val usuarioId = entrada.arguments?.getInt("usuarioId") ?: -1
            val rol = entrada.arguments?.getString("rol").orEmpty()

            MenuScreen(
                nombreUsuario = nombre,
                rolUsuario = rol,
                windowSizeClass = windowSizeClass,
                onIrALista = { navController.navigate(Rutas.lista(usuarioId, rol)) },
                onIrAFormulario = {
                    if (rol == "cliente") {
                        navController.navigate(Rutas.formulario(usuarioId, rol))
                    }
                },
                onIrAAsignaciones = {
                    if (rol == "administrador") {
                        navController.navigate(Rutas.asignaciones(usuarioId, rol))
                    }
                },
                onCerrarSesion = {
                    navController.navigate(Rutas.LOGIN) {
                        popUpTo(Rutas.MENU) { inclusive = true }
                    }
                }
            )
        }

        composable(
            route = Rutas.LISTA,
            arguments = listOf(
                navArgument("usuarioId") { type = NavType.IntType },
                navArgument("rol") { type = NavType.StringType }
            )
        ) { entrada ->
            val usuarioId = entrada.arguments?.getInt("usuarioId") ?: -1
            val rol = entrada.arguments?.getString("rol").orEmpty()
            ListaSolicitudesScreen(
                rolUsuario = rol,
                windowSizeClass = windowSizeClass,
                onAgregarClick = {
                    if (rol == "cliente") {
                        navController.navigate(Rutas.formulario(usuarioId, rol))
                    }
                },
                onSolicitudClick = { id ->
                    navController.navigate(Rutas.detalle(id, usuarioId, rol))
                },
                onEscanearClick = { navController.navigate(Rutas.qr(usuarioId, rol)) }
            )
        }

        composable(
            route = Rutas.FORMULARIO,
            arguments = listOf(
                navArgument("usuarioId") { type = NavType.IntType },
                navArgument("rol") { type = NavType.StringType }
            )
        ) { entrada ->
            val usuarioId = entrada.arguments?.getInt("usuarioId") ?: -1
            val rol = entrada.arguments?.getString("rol").orEmpty()
            SolicitudFormScreen(
                windowSizeClass = windowSizeClass,
                onGuardado = {
                    navController.navigate(Rutas.lista(usuarioId, rol)) {
                        popUpTo(Rutas.FORMULARIO) { inclusive = true }
                        launchSingleTop = true
                    }
                },
                onVolver = { navController.popBackStack() }
            )
        }

        composable(
            route = Rutas.DETALLE,
            arguments = listOf(
                navArgument("id") { type = NavType.IntType },
                navArgument("usuarioId") { type = NavType.IntType },
                navArgument("rol") { type = NavType.StringType }
            )
        ) {
            DetalleSolicitudScreen(
                windowSizeClass = windowSizeClass,
                onVolver = { navController.popBackStack() }
            )
        }

        composable(
            route = Rutas.QR,
            arguments = listOf(
                navArgument("usuarioId") { type = NavType.IntType },
                navArgument("rol") { type = NavType.StringType }
            )
        ) { entrada ->
            val usuarioId = entrada.arguments?.getInt("usuarioId") ?: -1
            val rol = entrada.arguments?.getString("rol").orEmpty()
            QrScannerScreen(
                windowSizeClass = windowSizeClass,
                onVolver = { navController.popBackStack() },
                onAbrirDetalle = { id ->
                    navController.navigate(Rutas.detalle(id, usuarioId, rol))
                }
            )
        }

        composable(
            route = Rutas.ASIGNACIONES,
            arguments = listOf(
                navArgument("usuarioId") { type = NavType.IntType },
                navArgument("rol") { type = NavType.StringType }
            )
        ) { entrada ->
            val rol = entrada.arguments?.getString("rol").orEmpty()
            AsignarSolicitudesScreen(
                windowSizeClass = windowSizeClass,
                puedeAdministrar = rol == "administrador",
                onVolver = { navController.popBackStack() }
            )
        }
    }
}
