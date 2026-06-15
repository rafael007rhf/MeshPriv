package br.dev.meshpriv.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.absoluteValue

/**
 * Avatar circular com a inicial do apelido e cor derivada do [seed] (nodeId), de modo que
 * cada nó da rede tenha uma cor estável e distinta — ajuda a diferenciar peers no experimento.
 */
@Composable
fun NodeAvatar(
    nickname: String,
    seed: String,
    modifier: Modifier = Modifier,
    size: Dp = 44.dp,
    fontSize: TextUnit = 18.sp
) {
    val baseColor = remember(seed) { colorFromSeed(seed) }
    val initial = nickname.trim().firstOrNull()?.uppercase() ?: "?"

    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(
                Brush.linearGradient(
                    listOf(baseColor, baseColor.copy(alpha = 0.7f))
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = initial,
            color = Color.White,
            fontSize = fontSize,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.titleMedium
        )
    }
}

/** Gera uma cor estável a partir de uma string, variando a matiz e mantendo saturação/brilho. */
private fun colorFromSeed(seed: String): Color {
    val hue = (seed.hashCode().absoluteValue % 360).toFloat()
    return Color.hsv(hue = hue, saturation = 0.55f, value = 0.65f)
}
