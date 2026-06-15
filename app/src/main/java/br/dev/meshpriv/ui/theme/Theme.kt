package br.dev.meshpriv.ui.theme

<<<<<<< HEAD
=======
import android.app.Activity
>>>>>>> 3e40bf5f49eb6e0fe76096429607711a287e07bc
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
<<<<<<< HEAD
    primary = TealDark,
    onPrimary = OnTealDark,
    primaryContainer = TealContainerDark,
    onPrimaryContainer = OnTealContainerDark,
    secondary = TealDark,
    onSecondary = OnTealDark,
    secondaryContainer = TealContainerDark,
    onSecondaryContainer = OnTealContainerDark,
    tertiary = BlueDark,
    onTertiary = OnBlueDark,
    tertiaryContainer = BlueContainerDark,
    onTertiaryContainer = OnBlueContainerDark,
    background = BackgroundDark,
    onBackground = OnBackgroundDark,
    surface = SurfaceDark,
    onSurface = OnSurfaceDark,
    surfaceContainer = SurfaceContainerDark,
    surfaceVariant = SurfaceVariantDark,
    onSurfaceVariant = OnSurfaceVariantDark,
    outline = OutlineDark
)

private val LightColorScheme = lightColorScheme(
    primary = TealLight,
    onPrimary = OnTealLight,
    primaryContainer = TealContainerLight,
    onPrimaryContainer = OnTealContainerLight,
    secondary = TealLight,
    onSecondary = OnTealLight,
    secondaryContainer = TealContainerLight,
    onSecondaryContainer = OnTealContainerLight,
    tertiary = BlueLight,
    onTertiary = OnBlueLight,
    tertiaryContainer = BlueContainerLight,
    onTertiaryContainer = OnBlueContainerLight,
    background = BackgroundLight,
    onBackground = OnBackgroundLight,
    surface = SurfaceLight,
    onSurface = OnSurfaceLight,
    surfaceContainer = SurfaceContainerLight,
    surfaceVariant = SurfaceVariantLight,
    onSurfaceVariant = OnSurfaceVariantLight,
    outline = OutlineLight
=======
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80
)

private val LightColorScheme = lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40

    /* Other default colors to override
    background = Color(0xFFFFFBFE),
    surface = Color(0xFFFFFBFE),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F),
    */
>>>>>>> 3e40bf5f49eb6e0fe76096429607711a287e07bc
)

@Composable
fun MeshPrivTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
<<<<<<< HEAD
    // Desligado por padrão: a marca do MeshPriv deve ser idêntica em todos os aparelhos do
    // experimento (o dynamic color do Material You variaria conforme o wallpaper de cada um).
    dynamicColor: Boolean = false,
=======
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
>>>>>>> 3e40bf5f49eb6e0fe76096429607711a287e07bc
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
<<<<<<< HEAD
}
=======
}
>>>>>>> 3e40bf5f49eb6e0fe76096429607711a287e07bc
