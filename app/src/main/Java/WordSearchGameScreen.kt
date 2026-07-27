package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WordSearchGameScreen(
    deckId: Long?,
    viewModel: MainViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val allCards by viewModel.allCards.collectAsState()
    val deckCards by viewModel.selectedDeckCards.collectAsState()

    val sourceCards = remember(deckId, allCards, deckCards) {
        if (deckId != null && deckCards.isNotEmpty()) deckCards else allCards
    }

    // Extract uppercase clean target words (3-8 letters)
    val targetWords = remember(sourceCards) {
        val words = sourceCards.flatMap { card ->
            val frontW = card.frontText.trim().uppercase().filter { it.isLetter() }
            val backW = card.backText.trim().uppercase().filter { it.isLetter() }
            listOf(frontW, backW)
        }.filter { it.length in 3..8 }.distinct().take(5)

        if (words.size >= 2) words else listOf("KOTLIN", "CARD", "MEMORIA", "ESTUDO", "LEMBRAR")
    }

    // Grid Generation (8x8)
    val gridSize = 8
    var gridLetters by remember { mutableStateOf(List(gridSize * gridSize) { ' ' }) }
    var foundWords by remember { mutableStateOf(setOf<String>()) }
    var selectedIndices by remember { mutableStateOf(listOf<Int>()) }

    fun generateGrid() {
        val grid = Array(gridSize) { CharArray(gridSize) { ' ' } }
        val randomLetters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"

        // Place target words horizontally or vertically
        for (word in targetWords) {
            var placed = false
            var attempts = 0
            while (!placed && attempts < 50) {
                attempts++
                val isHorizontal = (0..1).random() == 0
                val row = (0 until gridSize).random()
                val col = (0 until gridSize).random()

                if (isHorizontal && col + word.length <= gridSize) {
                    var canPlace = true
                    for (i in word.indices) {
                        if (grid[row][col + i] != ' ' && grid[row][col + i] != word[i]) {
                            canPlace = false
                            break
                        }
                    }
                    if (canPlace) {
                        for (i in word.indices) {
                            grid[row][col + i] = word[i]
                        }
                        placed = true
                    }
                } else if (!isHorizontal && row + word.length <= gridSize) {
                    var canPlace = true
                    for (i in word.indices) {
                        if (grid[row + i][col] != ' ' && grid[row + i][col] != word[i]) {
                            canPlace = false
                            break
                        }
                    }
                    if (canPlace) {
                        for (i in word.indices) {
                            grid[row + i][col] = word[i]
                        }
                        placed = true
                    }
                }
            }
        }

        // Fill remaining empty cells
        val flat = mutableListOf<Char>()
        for (r in 0 until gridSize) {
            for (c in 0 until gridSize) {
                if (grid[r][c] == ' ') {
                    grid[r][c] = randomLetters.random()
                }
                flat.add(grid[r][c])
            }
        }
        gridLetters = flat
        foundWords = emptySet()
        selectedIndices = emptyList()
    }

    LaunchedEffect(targetWords) {
        generateGrid()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Caça-Palavras 🔤", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Voltar")
                    }
                },
                actions = {
                    IconButton(onClick = { generateGrid() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Reiniciar Grid")
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Words List to Find
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "Palavras do Deck no Caça-Palavras:",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        targetWords.forEach { word ->
                            val isFound = foundWords.contains(word)
                            FilterChip(
                                selected = isFound,
                                onClick = {},
                                label = {
                                    Text(
                                        text = word,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = if (isFound) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                leadingIcon = if (isFound) {
                                    { Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp)) }
                                } else null
                            )
                        }
                    }
                }
            }

            // Word Selection String Preview
            val currentSelectedText = remember(selectedIndices, gridLetters) {
                selectedIndices.map { gridLetters.getOrElse(it) { ' ' } }.joinToString("")
            }

            Text(
                text = "Palavra selecionada: $currentSelectedText",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            // 8x8 Grid
            LazyVerticalGrid(
                columns = GridCells.Fixed(gridSize),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(8.dp)
            ) {
                itemsIndexed(gridLetters) { index, char ->
                    val isSelected = selectedIndices.contains(index)
                    Box(
                        modifier = Modifier
                            .aspectRatio(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                if (isSelected) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.surface
                            )
                            .clickable {
                                val newSelected = if (selectedIndices.contains(index)) {
                                    selectedIndices - index
                                } else {
                                    selectedIndices + index
                                }
                                selectedIndices = newSelected

                                // Check if selected form matches any target word
                                val wordFormed = newSelected.map { gridLetters[it] }.joinToString("")
                                if (targetWords.contains(wordFormed) && !foundWords.contains(wordFormed)) {
                                    foundWords = foundWords + wordFormed
                                    selectedIndices = emptyList()
                                    Toast.makeText(context, "Palavra encontrada: $wordFormed! 🎉", Toast.LENGTH_SHORT).show()
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = char.toString(),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            if (foundWords.size == targetWords.size) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.tertiaryContainer,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Parabéns! Você encontrou todas as palavras do jogo! 🏆",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(16.dp),
                        color = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                }
            }

            Button(
                onClick = { selectedIndices = emptyList() },
                modifier = Modifier.fillMaxWidth(),
                enabled = selectedIndices.isNotEmpty()
            ) {
                Text("Limpar Seleção de Letras")
            }
        }
    }
}
