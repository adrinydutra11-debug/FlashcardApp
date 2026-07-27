package com.example.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.data.local.FlashcardEntity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditCardDialog(
    card: FlashcardEntity? = null,
    onDismiss: () -> Unit,
    onConfirm: (frontText: String, backText: String, exampleSentence: String?, hint: String?, imageUrl: String?) -> Unit
) {
    var frontText by remember { mutableStateOf(card?.frontText ?: "") }
    var backText by remember { mutableStateOf(card?.backText ?: "") }
    var exampleSentence by remember { mutableStateOf(card?.exampleSentence ?: "") }
    var hint by remember { mutableStateOf(card?.hint ?: "") }
    var imageUrl by remember { mutableStateOf(card?.imageUrl ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (card == null) "Novo Flashcard" else "Editar Flashcard") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = frontText,
                    onValueChange = { frontText = it },
                    label = { Text("Frente (Palavra / Pergunta)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = backText,
                    onValueChange = { backText = it },
                    label = { Text("Verso (Tradução / Resposta)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = exampleSentence,
                    onValueChange = { exampleSentence = it },
                    label = { Text("Frase de Exemplo (Opcional)") },
                    placeholder = { Text("Ex: She sounded ebullient on the phone.") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = hint,
                    onValueChange = { hint = it },
                    label = { Text("Dica Mnemônica (Opcional)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = imageUrl,
                    onValueChange = { imageUrl = it },
                    label = { Text("URL de Imagem / Ilustração (Opcional)") },
                    placeholder = { Text("https://exemplo.com/imagem.jpg") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (frontText.isNotBlank() && backText.isNotBlank()) {
                        onConfirm(
                            frontText.trim(),
                            backText.trim(),
                            exampleSentence.trim().ifBlank { null },
                            hint.trim().ifBlank { null },
                            imageUrl.trim().ifBlank { null }
                        )
                    }
                },
                enabled = frontText.isNotBlank() && backText.isNotBlank()
            ) {
                Text(if (card == null) "Adicionar" else "Salvar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
