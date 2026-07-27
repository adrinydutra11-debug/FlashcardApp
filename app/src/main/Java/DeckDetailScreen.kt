package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.local.FlashcardEntity
import com.example.ui.MainViewModel
import com.example.ui.components.AddEditCardDialog
import com.example.ui.components.AddEditDeckDialog
import com.example.ui.util.ColorUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeckDetailScreen(
    deckId: Long,
    viewModel: MainViewModel,
    onBack: () -> Unit,
    onStudy: (Long) -> Unit,
    onPlayGames: (Long) -> Unit
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    LaunchedEffect(deckId) {
        viewModel.selectDeck(deckId)
    }

    val deck by viewModel.selectedDeck.collectAsState()
    val cards by viewModel.selectedDeckCards.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var showAddCardDialog by remember { mutableStateOf(false) }
    var cardToEdit by remember { mutableStateOf<FlashcardEntity?>(null) }
    var showEditDeckDialog by remember { mutableStateOf(false) }
    var showExportDialog by remember { mutableStateOf<String?>(null) }

    val filteredCards = remember(cards, searchQuery) {
        if (searchQuery.isBlank()) cards
        else cards.filter {
            it.frontText.contains(searchQuery, ignoreCase = true) ||
                    it.backText.contains(searchQuery, ignoreCase = true)
        }
    }

    val deckColor = remember(deck) {
        ColorUtils.parseHexColor(deck?.colorHex ?: "#6366F1")
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            deck?.name ?: "Deck",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "${cards.size} cartões",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Voltar")
                    }
                },
                actions = {
                    IconButton(onClick = { showEditDeckDialog = true }) {
                        Icon(Icons.Default.Edit, contentDescription = "Editar Deck")
                    }
                    IconButton(onClick = {
                        viewModel.exportDeckJson(deckId) { json ->
                            showExportDialog = json
                        }
                    }) {
                        Icon(Icons.Default.IosShare, contentDescription = "Exportar Deck")
                    }
                    IconButton(onClick = { viewModel.deleteDeck(deckId); onBack() }) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Excluir Deck",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showAddCardDialog = true },
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("Novo Cartão") },
                containerColor = deckColor
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = deckColor.copy(alpha = 0.12f)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .clip(CircleShape)
                                    .background(deckColor)
                            )
                            Text(
                                text = deck?.description.orEmpty().ifBlank { "Sem descrição" },
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = { onStudy(deckId) },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = deckColor)
                            ) {
                                Icon(Icons.Default.Psychology, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Revisar Deck")
                            }

                            OutlinedButton(
                                onClick = { onPlayGames(deckId) },
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.SportsEsports, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Jogos")
                            }
                        }
                    }
                }
            }

            // Search Bar
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Buscar nos cartões...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    trailingIcon = if (searchQuery.isNotEmpty()) {
                        {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = "Limpar")
                            }
                        }
                    } else null,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
            }

            // Cards List
            if (filteredCards.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (searchQuery.isBlank()) "Nenhum cartão cadastrado neste deck." else "Nenhum cartão encontrado para a busca.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                items(filteredCards) { card ->
                    CardItem(
                        card = card,
                        deckColor = deckColor,
                        onEdit = { cardToEdit = card },
                        onDelete = { viewModel.deleteCard(card.id) }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }

    if (showAddCardDialog) {
        AddEditCardDialog(
            onDismiss = { showAddCardDialog = false },
            onConfirm = { front, back, sentence, hint, img ->
                viewModel.addCard(deckId, front, back, sentence, hint, img)
                showAddCardDialog = false
            }
        )
    }

    cardToEdit?.let { card ->
        AddEditCardDialog(
            card = card,
            onDismiss = { cardToEdit = null },
            onConfirm = { front, back, sentence, hint, img ->
                viewModel.updateCard(
                    card.copy(
                        frontText = front,
                        backText = back,
                        exampleSentence = sentence,
                        hint = hint,
                        imageUrl = img
                    )
                )
                cardToEdit = null
            }
        )
    }

    if (showEditDeckDialog && deck != null) {
        AddEditDeckDialog(
            deck = deck,
            onDismiss = { showEditDeckDialog = false },
            onConfirm = { name, desc, colorHex, iconName ->
                viewModel.updateDeck(
                    deck!!.copy(
                        name = name,
                        description = desc,
                        colorHex = colorHex,
                        iconName = iconName
                    )
                )
                showEditDeckDialog = false
            }
        )
    }

    showExportDialog?.let { json ->
        AlertDialog(
            onDismissRequest = { showExportDialog = null },
            title = { Text("Exportar Deck (Offline JSON)") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Copie o código JSON do deck para enviar ou salvar offline:")
                    OutlinedTextField(
                        value = json,
                        onValueChange = {},
                        readOnly = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    clipboardManager.setText(AnnotatedString(json))
                    Toast.makeText(context, "JSON copiado para a área de transferência!", Toast.LENGTH_SHORT).show()
                    showExportDialog = null
                }) {
                    Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Copiar JSON")
                }
            },
            dismissButton = {
                TextButton(onClick = { showExportDialog = null }) {
                    Text("Fechar")
                }
            }
        )
    }
}

@Composable
fun CardItem(
    card: FlashcardEntity,
    deckColor: androidx.compose.ui.graphics.Color,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = card.frontText,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = card.backText,
                        style = MaterialTheme.typography.bodyMedium,
                        color = deckColor,
                        fontWeight = FontWeight.Medium
                    )
                }

                Row {
                    IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = "Editar",
                            modifier = Modifier.size(18.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Excluir",
                            modifier = Modifier.size(18.dp),
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            if (!card.exampleSentence.isNullOrBlank()) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ) {
                    Text(
                        text = "💬 \"${card.exampleSentence}\"",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val masteryLabel = when (card.masteryLevel) {
                    0 -> "Novo"
                    1 -> "Aprendendo"
                    2 -> "Revisando"
                    else -> "Dominado ✨"
                }

                SuggestionChip(
                    onClick = {},
                    label = { Text("Repetições: ${card.repetitionCount} • $masteryLabel", style = MaterialTheme.typography.labelSmall) }
                )

                Text(
                    text = "Próxima: ${card.intervalDays}d",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
