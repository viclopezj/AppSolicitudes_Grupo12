package com.example.appsolicitudes_grupo12

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.appsolicitudes_grupo12.ui.components.AccionesSolicitud
import com.example.appsolicitudes_grupo12.ui.theme.AppSolicitudes_Grupo12Theme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

// Prueba de UI: los botones del cliente cambian según el estado de la solicitud.
@RunWith(AndroidJUnit4::class)
class AccionesSolicitudTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun cliente_veBotonesSegunElEstado() {
        var estado by mutableStateOf("Pendiente de información")

        composeRule.setContent {
            AppSolicitudes_Grupo12Theme {
                AccionesSolicitud(
                    rol = "cliente",
                    estadoActual = estado,
                    procesando = false,
                    onCambiarEstado = { _, _ -> }
                )
            }
        }

        // Pendiente de información: un solo botón para responder
        composeRule.onNodeWithText("Responder solicitud de información").assertIsDisplayed()

        // Resuelta: confirmar o pedir reapertura
        estado = "Resuelta"
        composeRule.waitForIdle()
        composeRule.onNodeWithText("Confirmar resolución").assertIsDisplayed()
        composeRule.onNodeWithText("Pedir reapertura").assertIsDisplayed()
        composeRule.onNodeWithText("Responder solicitud de información").assertDoesNotExist()

        // Recibida: el cliente no tiene acciones
        estado = "Recibida"
        composeRule.waitForIdle()
        composeRule.onNodeWithText("Acciones").assertDoesNotExist()
    }
}
