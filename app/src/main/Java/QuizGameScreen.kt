package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.data.local.FlashcardEntity
import com.example.ui.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizGameScreen(
    deckId: Long?,
    viewModel: MainViewModel,
    onBack: () -> Unit
) {
    val allCards by viewModel.allCards.collectAsState()
    val deckCards by viewModel.selectedDeckCards.collectAsState()

    val sourceCards = remember(deckId, allCards, deckCards) {
        if (deckId != null && deckCards.isNotEmpty()) deckCards else allCards
    }

    var currentIndex by remember { mutableStateOf(0) }
    var selectedOptionIndex by remember { mutableStateOf<Int?>(null) }
    var score by remember { mutableStateOf(0) }

    val currentCard = sourceCards.getOrNull(currentIndex)

    // Generate 4 multiple choice options for current card
    val options = remember(currentCard, sourceCards) {
        if (currentCard == null) emptyList()
        else {
            val correct = currentCard.backText
            val distractors = sourceCards
                .filter { it.id != currentCard.id }
                .map { it.backText }
                .shuffled()
                .take(3)

            val fullList = (distractors + correct).shuffled()
            fullList
        }
    }

    val correctOptionIndex = remember(options, currentCard) {
        if (currentCard == null) -1
        else options.indexOf(currentCard.backText)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Quiz de Estudo ❓", fontWeight = FontWeight.Bold) },
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
            if (currentCard == null || currentIndex >= sourceCards.size) {
                // Quiz Completed Summary
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
                        Text("Quiz Finalizado! 🎉", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                        Text(
                            "Sua pontuação: $score acertos de ${sourceCards.size}",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Button(onClick = onBack, shape = RoundedCornerShape(12.dp)) {
                            Text("Voltar ao Hub de Jogos")
                        }
                    }
                }
            } else {
                // Question Card
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Pergunta ${currentIndex + 1} / ${sourceCards.size}", style = MaterialTheme.typography.labelLarge)
                        Text("Pontuação: $score", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                    }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Qual o significado de:",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = currentCard.frontText,
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }

                // Options List
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    options.forEachIndexed { index, optionText ->
                        val isSelected = selectedOptionIndex == index
                        val isCorrect = index == correctOptionIndex
                        val hasAnswered = selectedOptionIndex != null

                        val cardColor = when {
                            hasAnswered && isCorrect -> Color(0xFF10B981)
                            hasAnswered && isSelected && !isCorrect -> Color(0xFFEF4444)
                            else -> MaterialTheme.colorScheme.surface
                        }

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(
                                    1.dp,
                                    if (hasAnswered && isCorrect) Color(0xFF10B981) else MaterialTheme.colorScheme.outlineVariant,
                                    RoundedCornerShape(14.dp)
                                )
                                .clickable(enabled = !hasAnswered) {
                                    selectedOptionIndex = index
                                    if (index == correctOptionIndex) {
                                        score++
                                    }
                                },
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (hasAnswered && (isCorrect || isSelected)) cardColor.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surface
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "${('A' + index)})  $optionText",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.weight(1f)
                                )

                                if (hasAnswered) {
                                    if (isCorrect) {
                                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF10B981))
                                    } else if (isSelected) {
                                        Icon(Icons.Default.Cancel, contentDescription = null, tint = Color(0xFFEF4444))
                                    }
                                }
                            }
                        }
                    }
                }

                // Next Button
                Button(
                    onClick = {
                        selectedOptionIndex = null
                        currentIndex++
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    enabled = selectedOptionIndex != null,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Próxima Pergunta")
                }
            }
        }
    }
}
