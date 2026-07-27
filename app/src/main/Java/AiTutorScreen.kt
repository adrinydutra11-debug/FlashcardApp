package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.ChatMessage
import com.example.ui.MainViewModel
import com.example.ui.TutorMode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiTutorScreen(
    viewModel: MainViewModel
) {
    val messages by viewModel.tutorMessages.collectAsState()
    val isLoading by viewModel.isTutorLoading.collectAsState()
    val activeMode by viewModel.tutorMode.collectAsState()
    val currentSimulado by viewModel.currentSimulado.collectAsState()
    val decks by viewModel.decks.collectAsState()

    var textInput by remember { mutableStateOf("") }
    var selectedSimuladoDeck by remember { mutableStateOf(decks.firstOrNull()?.name ?: "Inglês Essencial") }

    val listState = rememberLazyListState()

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.SmartToy,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                        Column {
                            Text("Tutora IA em Tempo Real", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text("Gemini AI • Atendimento Educacional", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Mode Selectors Tabs
            SingleChoiceSegmentedButtonRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                SegmentedButton(
                    selected = activeMode == TutorMode.DUVIDAS,
                    onClick = { viewModel.setTutorMode(TutorMode.DUVIDAS) },
                    shape = SegmentedButtonDefaults.itemShape(index = 0, count = 3)
                ) {
                    Text("💡 Dúvidas", style = MaterialTheme.typography.labelSmall)
                }

                SegmentedButton(
                    selected = activeMode == TutorMode.CONVERSACAO,
                    onClick = { viewModel.setTutorMode(TutorMode.CONVERSACAO) },
                    shape = SegmentedButtonDefaults.itemShape(index = 1, count = 3)
                ) {
                    Text("🗣️ Conversação", style = MaterialTheme.typography.labelSmall)
                }

                SegmentedButton(
                    selected = activeMode == TutorMode.SIMULADO,
                    onClick = { viewModel.setTutorMode(TutorMode.SIMULADO) },
                    shape = SegmentedButtonDefaults.itemShape(index = 2, count = 3)
                ) {
                    Text("✍️ Simulados", style = MaterialTheme.typography.labelSmall)
                }
            }

            // Simulado Generator Quick Action Bar if Simulado Mode
            if (activeMode == TutorMode.SIMULADO) {
                Surface(
                    color = MaterialTheme.colorScheme.tertiaryContainer,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Gerar Simulado Personalizado de Fixação", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Deck: ${decks.firstOrNull()?.name ?: "Geral"}", style = MaterialTheme.typography.bodySmall)
                            Button(
                                onClick = {
                                    val deckName = decks.firstOrNull()?.name ?: "Conceitos Gerais"
                                    viewModel.generateSimuladoForDeck(deckName, deckName)
                                },
                                enabled = !isLoading,
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Criar Simulado")
                            }
                        }
                    }
                }
            }

            // Interactive Generated Simulado Card if present
            currentSimulado?.let { simulado ->
                SimuladoCardWidget(
                    simulado = simulado,
                    onSelectAnswer = { qId, optIdx -> viewModel.submitSimuladoAnswer(qId, optIdx) },
                    onSubmit = { viewModel.submitSimulado() }
                )
            }

            // Chat Messages List
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(messages) { msg ->
                    ChatBubbleItem(message = msg)
                }

                if (isLoading) {
                    item {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.padding(8.dp)
                        ) {
                            CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                            Text("Tutora IA formulando explicação...", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }

            // Chat Input Bar
            Surface(
                tonalElevation = 3.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = textInput,
                        onValueChange = { textInput = it },
                        placeholder = {
                            Text(
                                when (activeMode) {
                                    TutorMode.DUVIDAS -> "Tire uma dúvida da matéria..."
                                    TutorMode.CONVERSACAO -> "Envie uma frase para praticar..."
                                    TutorMode.SIMULADO -> "Peça um assunto para o simulado..."
                                }
                            )
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(24.dp),
                        maxLines = 3
                    )

                    FloatingActionButton(
                        onClick = {
                            if (textInput.isNotBlank()) {
                                viewModel.sendTutorMessage(textInput.trim())
                                textInput = ""
                            }
                        },
                        modifier = Modifier.size(48.dp),
                        containerColor = MaterialTheme.colorScheme.primary
                    ) {
                        Icon(Icons.Default.Send, contentDescription = "Enviar", modifier = Modifier.size(20.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun ChatBubbleItem(message: ChatMessage) {
    val isUser = message.sender == "user"

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        Surface(
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (isUser) 16.dp else 4.dp,
                bottomEnd = if (isUser) 4.dp else 16.dp
            ),
            color = if (isUser) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.widthIn(max = 300.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = message.text,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isUser) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun SimuladoCardWidget(
    simulado: com.example.ui.GeneratedSimulado,
    onSelectAnswer: (Int, Int) -> Unit,
    onSubmit: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(simulado.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                if (simulado.isSubmitted) {
                    SuggestionChip(
                        onClick = {},
                        label = { Text("Nota: ${simulado.score} / ${simulado.questions.size}") }
                    )
                }
            }

            simulado.questions.forEach { q ->
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("${q.id}. ${q.questionText}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                    q.options.forEachIndexed { optIdx, optText ->
                        val isSelected = simulado.userAnswers[q.id] == optIdx
                        val isCorrect = q.correctAnswerIndex == optIdx

                        val optionBg = when {
                            simulado.isSubmitted && isCorrect -> Color(0xFF10B981).copy(alpha = 0.2f)
                            simulado.isSubmitted && isSelected && !isCorrect -> Color(0xFFEF4444).copy(alpha = 0.2f)
                            isSelected -> MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                            else -> MaterialTheme.colorScheme.surface
                        }

                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .clickable(enabled = !simulado.isSubmitted) {
                                    onSelectAnswer(q.id, optIdx)
                                },
                            color = optionBg,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = optText,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)
                            )
                        }
                    }

                    if (simulado.isSubmitted) {
                        Text(
                            text = "💡 Explicação: ${q.explanation}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                    }
                }
            }

            if (!simulado.isSubmitted) {
                Button(
                    onClick = onSubmit,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Entregar e Corrigir Simulado")
                }
            }
        }
    }
}
