package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GamesHubScreen(
    deckId: Long?,
    viewModel: MainViewModel,
    onNavigateToWordSearch: (Long?) -> Unit,
    onNavigateToQuiz: (Long?) -> Unit,
    onNavigateToFillBlanks: (Long?) -> Unit,
    onNavigateToCustomGame: () -> Unit
) {
    val decks by viewModel.decks.collectAsState()
    var selectedDeckId by remember { mutableStateOf(deckId) }

    val activeDeckName = remember(selectedDeckId, decks) {
        if (selectedDeckId == null) "Todos os Decks"
        else decks.find { it.id == selectedDeckId }?.name ?: "Deck Selecionado"
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Jogos de Estudo", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text(activeDeckName, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.SportsEsports,
                            contentDescription = null,
                            modifier = Modifier.size(36.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Column {
                            Text("Aprenda Jogando", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text(
                                "Os jogos ativam o resgate ativo (Active Recall) tornando a memorização da curva de Ebbinghaus mais leve e divertida.",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }

            item {
                Text("Selecione a Modalidade de Jogo", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            }

            // Game 1: Caça-Palavras
            item {
                GameModeCard(
                    title = "Caça-Palavras 🔤",
                    description = "Encontre as palavras aprendidas no grid de letras gerado a partir do seu deck.",
                    icon = Icons.Default.GridOn,
                    badgeColor = Color(0xFF10B981),
                    onClick = { onNavigateToWordSearch(selectedDeckId) }
                )
            }

            // Game 2: Quiz
            item {
                GameModeCard(
                    title = "Quiz Interativo ❓",
                    description = "Perguntas com 4 opções de resposta para testar a velocidade de raciocínio.",
                    icon = Icons.Default.Quiz,
                    badgeColor = Color(0xFF6366F1),
                    onClick = { onNavigateToQuiz(selectedDeckId) }
                )
            }

            // Game 3: Completar Frases
            item {
                GameModeCard(
                    title = "Completar Frases (Cloze) 📝",
                    description = "Preencha as lacunas que faltam nas frases de exemplo do seu vocabulário.",
                    icon = Icons.Default.EditNote,
                    badgeColor = Color(0xFFF59E0B),
                    onClick = { onNavigateToFillBlanks(selectedDeckId) }
                )
            }

            // Game 4: Criar / Personalizar Jogo
            item {
                GameModeCard(
                    title = "Criador de Jogos Personalizados ⚙️",
                    description = "Crie seu próprio modo de estudo, configure tempo limite, filtros de dificuldade e transporte suas regras.",
                    icon = Icons.Default.Tune,
                    badgeColor = Color(0xFFEC4899),
                    onClick = onNavigateToCustomGame
                )
            }

            item {
                Spacer(modifier = Modifier.height(60.dp))
            }
        }
    }
}

@Composable
fun GameModeCard(
    title: String,
    description: String,
    icon: ImageVector,
    badgeColor: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(badgeColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = badgeColor,
                    modifier = Modifier.size(26.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                Text(description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            Icon(
                imageVector = Icons.Default.PlayCircleFilled,
                contentDescription = "Jogar",
                tint = badgeColor,
                modifier = Modifier.size(32.dp)
            )
        }
    }
}
