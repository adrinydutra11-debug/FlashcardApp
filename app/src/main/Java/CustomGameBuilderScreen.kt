package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomGameBuilderScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit,
    onStartGame: () -> Unit
) {
    val context = LocalContext.current
    val decks by viewModel.decks.collectAsState()

    var gameType by remember { mutableStateOf("Quiz do Tempo") }
    var selectedDeckId by remember { mutableStateOf<Long?>(null) }
    var timerSeconds by remember { mutableStateOf(60) }
    var filterMode by remember { mutableStateOf("Todos os Cartões") }

    val gameTypesList = listOf("Quiz do Tempo", "Associação de Pares", "Velocidade Ebbinghaus", "Desafio Impossível")
    val timerOptions = listOf(30, 60, 120, 300)
    val filterOptions = listOf("Todos os Cartões", "Apenas Pendentes (Curva)", "Cartões Difíceis")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Personalizar Jogo ⚙️", fontWeight = FontWeight.Bold) },
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
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(Icons.Default.Tune, contentDescription = null, tint = MaterialTheme.colorScheme.tertiary)
                    Column {
                        Text("Criador de Jogos Personalizados", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text("Defina regras próprias, temporizadores e selecione os decks para transportar um desafio customizado.", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }

            // 1. Game Mode Selection
            Text("1. Modo do Jogo Customizado", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                gameTypesList.forEach { type ->
                    FilterChip(
                        selected = gameType == type,
                        onClick = { gameType = type },
                        label = { Text(type) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // 2. Select Deck
            Text("2. Selecionar Deck de Origem", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            FilterChip(
                selected = selectedDeckId == null,
                onClick = { selectedDeckId = null },
                label = { Text("Todos os Decks Combinados") }
            )
            decks.forEach { d ->
                FilterChip(
                    selected = selectedDeckId == d.id,
                    onClick = { selectedDeckId = d.id },
                    label = { Text("Deck: ${d.name}") },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // 3. Timer Configuration
            Text("3. Tempo Limite por Sessão", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                timerOptions.forEach { sec ->
                    FilterChip(
                        selected = timerSeconds == sec,
                        onClick = { timerSeconds = sec },
                        label = { Text("${sec}s") }
                    )
                }
            }

            // 4. Card Filter Mode
            Text("4. Filtro de Cartões", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                filterOptions.forEach { mode ->
                    FilterChip(
                        selected = filterMode == mode,
                        onClick = { filterMode = mode },
                        label = { Text(mode) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    Toast.makeText(context, "Modo '$gameType' configurado com $timerSeconds s!", Toast.LENGTH_SHORT).show()
                    onStartGame()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(14.dp)
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Iniciar Jogo Personalizado", style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}
