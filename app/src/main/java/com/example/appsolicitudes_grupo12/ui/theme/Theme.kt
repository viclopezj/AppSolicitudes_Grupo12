package com.example.appsolicitudes_grupo12.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val DarkColorScheme = darkColorScheme(
    primary = VerdeClaro,
    onPrimary = VerdeProfundo,
    primaryContainer = VerdeContenedorOscuro,
    onPrimaryContainer = VerdeClaro,
    secondary = Color(0xFFAFCBC4),
    onSecondary = Color(0xFF19332F),
    secondaryContainer = Color(0xFF304944),
    onSecondaryContainer = Color(0xFFD0E8E1),
    tertiary = Color(0xFFFFB596),
    onTertiary = Color(0xFF57200C),
    tertiaryContainer = Color(0xFF78391F),
    onTertiaryContainer = Color(0xFFFFDBCC),
    background = Color(0xFF101817),
    onBackground = Color(0xFFE1E9E6),
    surface = Color(0xFF17211F),
    onSurface = Color(0xFFE1E9E6),
    surfaceVariant = Color(0xFF293633),
    onSurfaceVariant = Color(0xFFBAC9C4),
    outline = Color(0xFF84938E),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6)
)

private val LightColorScheme = lightColorScheme(
    primary = VerdePrincipal,
    onPrimary = Color.White,
    primaryContainer = VerdeClaro,
    onPrimaryContainer = VerdeProfundo,
    secondary = Color(0xFF526762),
    onSecondary = Color.White,
    secondaryContainer = SuperficieSuave,
    onSecondaryContainer = TextoOscuro,
    tertiary = AcentoCalido,
    onTertiary = Color.White,
    tertiaryContainer = AcentoCalidoClaro,
    onTertiaryContainer = Color(0xFF411E10),
    background = FondoClaro,
    onBackground = TextoOscuro,
    surface = SuperficieClara,
    onSurface = TextoOscuro,
    surfaceVariant = SuperficieSuave,
    onSurfaceVariant = TextoSuave,
    outline = Color(0xFF82918C),
    error = ErrorColor,
    onError = Color.White,
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002)
)

private val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(18.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(30.dp)
)

@Composable
fun AppSolicitudes_Grupo12Theme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = AppShapes,
        content = content
    )
}
