package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.data.local.DeckEntity
import com.example.ui.util.ColorUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditDeckDialog(
    deck: DeckEntity? = null,
    onDismiss: () -> Unit,
    onConfirm: (name: String, description: String, colorHex: String, iconName: String) -> Unit
) {
    var name by remember { mutableStateOf(deck?.name ?: "") }
    var description by remember { mutableStateOf(deck?.description ?: "") }
    var selectedColorHex by remember { mutableStateOf(deck?.colorHex ?: ColorUtils.PRESET_CATEGORY_COLORS.first()) }
    var selectedIcon by remember { mutableStateOf(deck?.iconName ?: "book") }

    val iconsList = listOf(
        "language" to Icons.Default.Language,
        "code" to Icons.Default.Code,
        "science" to Icons.Default.Science,
        "book" to Icons.Default.MenuBook,
        "history" to Icons.Default.History,
        "school" to Icons.Default.School
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (deck == null) "Novo Deck de Estudo" else "Editar Deck") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nome do Deck (ex: Inglês, Kotlin)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Descrição do Estudo") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Text("Cor da Categoria", style = MaterialTheme.typography.labelLarge)
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(ColorUtils.PRESET_CATEGORY_COLORS) { colorHex ->
                        val color = ColorUtils.parseHexColor(colorHex)
                        val isSelected = colorHex.equals(selectedColorHex, ignoreCase = true)
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(color)
                                .clickable { selectedColorHex = colorHex }
                                .then(
                                    if (isSelected) Modifier.border(
                                        3.dp,
                                        MaterialTheme.colorScheme.onSurface,
                                        CircleShape
                                    ) else Modifier
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Selecionado",
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }

                Text("Ícone do Deck", style = MaterialTheme.typography.labelLarge)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    iconsList.forEach { (iconKey, iconVector) ->
                        val isSelected = selectedIcon == iconKey
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedIcon = iconKey },
                            label = {
                                Icon(
                                    imageVector = iconVector,
                                    contentDescription = iconKey,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        onConfirm(name.trim(), description.trim(), selectedColorHex, selectedIcon)
                    }
                },
                enabled = name.isNotBlank()
            ) {
                Text(if (deck == null) "Criar" else "Salvar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
