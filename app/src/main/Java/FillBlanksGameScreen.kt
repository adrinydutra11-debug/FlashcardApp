package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.ui.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FillBlanksGameScreen(
    deckId: Long?,
    viewModel: MainViewModel,
    onBack: () -> Unit
) {
    val allCards by viewModel.allCards.collectAsState()
    val deckCards by viewModel.selectedDeckCards.collectAsState()

    val cards = remember(deckId, allCards, deckCards) {
        val filtered = if (deckId != null && deckCards.isNotEmpty()) deckCards else allCards
        filtered.filter { !it.exampleSentence.isNullOrBlank() || it.frontText.isNotBlank() }
    }

    var currentIndex by remember { mutableStateOf(0) }
    var userInput by remember { mutableStateOf("") }
    var isSubmitted by remember { mutableStateOf(false) }
    var score by remember { mutableStateOf(0) }

    val currentCard = cards.getOrNull(currentIndex)

    // Cloze sentence generation
    val (clozeSentence, targetAnswer) = remember(currentCard) {
        if (currentCard == null) Pair("", "")
        else {
            val target = currentCard.frontText.trim()
            val sentence = currentCard.exampleSentence ?: "O termo correto para '${currentCard.backText}' é ___."
            val regex = Regex("(?i)\\b${Regex.escape(target)}\\b")
            val cloze = if (sentence.contains(target, ignoreCase = true)) {
                sentence.replace(regex, "_______")
            } else {
                "___ : ${currentCard.backText}"
            }
            Pair(cloze, target)
        }
    }

    val isCorrect = remember(userInput, targetAnswer) {
        userInput.trim().equals(targetAnswer.trim(), ignoreCase = true)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Completar Frases 📝", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Voltar")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            if (currentCard == null || currentIndex >= cards.size) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text("Jogo Concluído! 🎉", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                        Text("Você completou $score frases corretamente!", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                        Button(onClick = onBack, shape = RoundedCornerShape(12.dp)) {
                            Text("Voltar aos Jogos")
                        }
                    }
                }
            } else {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Frase ${currentIndex + 1} de ${cards.size}", style = MaterialTheme.typography.labelLarge)
                        Text("Acertos: $score", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                    }

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(Icons.Default.HelpOutline, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
                            Text(
                                text = "Preencha a palavra que falta na frase:",
                                style = MaterialTheme.typography.labelMedium
                            )
                            Text(
                                text = "\"$clozeSentence\"",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text = "Significado: ${currentCard.backText}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
                            )
                        }
                    }

                    OutlinedTextField(
                        value = userInput,
                        onValueChange = { if (!isSubmitted) userInput = it },
                        label = { Text("Digite a palavra que completa a lacuna") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        enabled = !isSubmitted
                    )

                    if (isSubmitted) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isCorrect) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.errorContainer,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = if (isCorrect) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                                )
                                Column {
                                    Text(
                                        text = if (isCorrect) "Excelente! Resposta correta." else "Incorreto! A palavra correta é '$targetAnswer'.",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }

                if (!isSubmitted) {
                    Button(
                        onClick = {
                            isSubmitted = true
                            if (isCorrect) score++
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        enabled = userInput.isNotBlank(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Verificar Frase")
                    }
                } else {
                    Button(
                        onClick = {
                            userInput = ""
                            isSubmitted = false
                            currentIndex++
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Próxima Frase")
                    }
                }
            }
        }
    }
}
