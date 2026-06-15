package br.dev.meshpriv.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

<<<<<<< HEAD
private val defaults = Typography()

// Mantém os defaults do Material 3, reforçando o peso dos títulos para dar hierarquia visual.
// (IDs de nó usam FontFamily.Monospace aplicada direto no Text, sem mexer no labelLarge,
// que é o estilo padrão do texto dos botões.)
val Typography = Typography(
    headlineMedium = defaults.headlineMedium.copy(fontWeight = FontWeight.Bold),
    headlineSmall = defaults.headlineSmall.copy(fontWeight = FontWeight.Bold),
    titleLarge = defaults.titleLarge.copy(fontWeight = FontWeight.SemiBold),
    titleMedium = defaults.titleMedium.copy(fontWeight = FontWeight.SemiBold),
=======
// Set of Material typography styles to start with
val Typography = Typography(
>>>>>>> 3e40bf5f49eb6e0fe76096429607711a287e07bc
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    )
<<<<<<< HEAD
)
=======
    /* Other default text styles to override
    titleLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    ),
    labelSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    )
    */
)
>>>>>>> 3e40bf5f49eb6e0fe76096429607711a287e07bc
