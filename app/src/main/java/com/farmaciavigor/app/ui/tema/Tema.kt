package com.farmaciavigor.app.ui.tema

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Paleta: off-white predominante com detalhes em vermelho.
val OffWhite = Color(0xFFFAF6EF)
val SuperficieClara = Color(0xFFFFFDF8)
val VermelhoVigor = Color(0xFFC62828)
val VermelhoProfundo = Color(0xFF6E1414)
val RosaSuave = Color(0xFFFBE3DF)
val Grafite = Color(0xFF37322C)
val MarromSuave = Color(0xFF6F6459)
val AreiaSuperficie = Color(0xFFF2EBE0)
val AreiaContorno = Color(0xFFCFC4B4)
val VerdeConfirmacao = Color(0xFF2E7D32)
val AmbarAviso = Color(0xFFB26A00)

private val Esquema = lightColorScheme(
    primary = VermelhoVigor,
    onPrimary = Color.White,
    primaryContainer = RosaSuave,
    onPrimaryContainer = VermelhoProfundo,
    secondary = MarromSuave,
    onSecondary = Color.White,
    secondaryContainer = AreiaSuperficie,
    onSecondaryContainer = Grafite,
    tertiary = VerdeConfirmacao,
    onTertiary = Color.White,
    background = OffWhite,
    onBackground = Grafite,
    surface = SuperficieClara,
    onSurface = Grafite,
    surfaceVariant = AreiaSuperficie,
    onSurfaceVariant = MarromSuave,
    outline = AreiaContorno,
    error = Color(0xFFB3261E),
    onError = Color.White,
    errorContainer = Color(0xFFF9DEDC),
    onErrorContainer = Color(0xFF601410),
)

// Tipografia ampliada: o app é usado por públicos variados, inclusive idosos.
private val Tipografia = Typography(
    headlineLarge = TextStyle(fontSize = 38.sp, lineHeight = 46.sp, fontWeight = FontWeight.Bold),
    headlineMedium = TextStyle(fontSize = 31.sp, lineHeight = 38.sp, fontWeight = FontWeight.Bold),
    headlineSmall = TextStyle(fontSize = 26.sp, lineHeight = 33.sp, fontWeight = FontWeight.SemiBold),
    titleLarge = TextStyle(fontSize = 25.sp, lineHeight = 32.sp, fontWeight = FontWeight.SemiBold),
    titleMedium = TextStyle(fontSize = 21.sp, lineHeight = 27.sp, fontWeight = FontWeight.SemiBold),
    titleSmall = TextStyle(fontSize = 18.sp, lineHeight = 24.sp, fontWeight = FontWeight.SemiBold),
    bodyLarge = TextStyle(fontSize = 20.sp, lineHeight = 27.sp),
    bodyMedium = TextStyle(fontSize = 17.sp, lineHeight = 23.sp),
    bodySmall = TextStyle(fontSize = 15.sp, lineHeight = 20.sp),
    labelLarge = TextStyle(fontSize = 20.sp, lineHeight = 25.sp, fontWeight = FontWeight.Medium),
    labelMedium = TextStyle(fontSize = 16.sp, lineHeight = 21.sp, fontWeight = FontWeight.Medium),
    labelSmall = TextStyle(fontSize = 14.sp, lineHeight = 18.sp, fontWeight = FontWeight.Medium),
)

private val Formas = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(22.dp),
    extraLarge = RoundedCornerShape(28.dp),
)

@Composable
fun TemaFarmaciaVigor(conteudo: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = Esquema,
        typography = Tipografia,
        shapes = Formas,
        content = conteudo,
    )
}
