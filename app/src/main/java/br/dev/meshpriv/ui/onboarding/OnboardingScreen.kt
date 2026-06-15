package br.dev.meshpriv.ui.onboarding

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp

/**
 * Onboarding mínimo de primeira execução: pede só o apelido, que identifica o nó na mesh
 * (exibido nos outros dispositivos via handshake HELLO). As chaves já foram geradas pela
 * identidade local; aqui só falta dar um nome legível ao nó.
 */
@Composable
fun OnboardingScreen(onConfirm: (String) -> Unit) {
    var nickname by remember { mutableStateOf("") }
    val trimmed = nickname.trim()
    val isValid = trimmed.isNotEmpty()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Bem-vindo ao MeshPriv",
            style = MaterialTheme.typography.headlineSmall
        )
        Text(
            text = "Escolha um apelido para identificar este dispositivo na rede mesh.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        OutlinedTextField(
            value = nickname,
            onValueChange = { if (it.length <= MAX_NICKNAME_LENGTH) nickname = it },
            label = { Text("Apelido") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            modifier = Modifier.fillMaxWidth()
        )
        Button(
            onClick = { onConfirm(trimmed) },
            enabled = isValid,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Começar")
        }
    }
}

private const val MAX_NICKNAME_LENGTH = 24
