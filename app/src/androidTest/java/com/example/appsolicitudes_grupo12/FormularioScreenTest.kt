package com.example.appsolicitudes_grupo12

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.appsolicitudes_grupo12.ui.screen.FormularioScreen
import com.example.appsolicitudes_grupo12.ui.theme.AppSolicitudes_Grupo12Theme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

// Prueba de UI: el formulario vacío no se registra y muestra los errores.
@RunWith(AndroidJUnit4::class)
class FormularioScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun formularioVacio_muestraErroresDeValidacion() {
        composeRule.setContent {
            AppSolicitudes_Grupo12Theme {
                FormularioScreen(usuarioId = 1, rol = "cliente")
            }
        }

        // El botón queda al final del formulario, que se desplaza
        composeRule.onNodeWithText("Registrar solicitud").performScrollTo().performClick()

        composeRule.onNodeWithText("El título es obligatorio").assertExists()
        composeRule.onNodeWithText("La descripción es obligatoria").assertExists()
        composeRule.onNodeWithText("Selecciona una categoría").assertExists()
        // No se llegó a guardar: no aparece el diálogo de éxito
        composeRule.onNodeWithText("Solicitud registrada").assertDoesNotExist()
    }
}
