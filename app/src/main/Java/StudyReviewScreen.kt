package com.example.ui.screens

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.local.FlashcardEntity
import com.example.ui.MainViewModel
import com.example.ui.util.ColorUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudyReviewScreen(
    deckId: Long?,
    viewModel: MainViewModel,
    onBack: () -> Unit
) {
    val dueCards by viewModel.dueCards.collectAsState()
    val deckCards by viewModel.selectedDeckCards.collectAsState()

    // Active card queue
    val reviewQueue = remember(dueCards, deckCards, deckId) {
        if (deckId != null) {
            val deckDue = dueCards.filter { it.deckId == deckId }
            if (deckDue.isNotEmpty()) deckDue else deckCards
        } else {
            if (dueCards.isNotEmpty()) dueCards else deckCards
        }
    }

    var currentIndex by remember { mutableStateOf(0) }
    var isFlipped by remember { mutableStateOf(false) }
    var showHint by remember { mutableStateOf(false) }
    var reviewedCount by remember { mutableStateOf(0) }

    val currentCard = reviewQueue.getOrNull(currentIndex)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Revisão Ebbinghaus",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Sair")
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
            if (currentCard == null || currentIndex >= reviewQueue.size) {
                // Completed Session View
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.padding(24.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.size(80.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.EmojiEvents,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(44.dp)
                                )
                            }
                        }

                        Text(
                            text = "Sessão Concluída!",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )

                        Text(
                            text = "Você revisou $reviewedCount cartões conforme a curva de retenção de Ebbinghaus.",
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Button(
                            onClick = onBack,
                            modifier = Modifier.fillMaxWidth(0.7f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Voltar aos Decks")
                        }
                    }
                }
            } else {
                // Header Progress
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            "Cartão ${currentIndex + 1} de ${reviewQueue.size}",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            "Ebbinghaus Spaced Repetition",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    LinearProgressIndicator(
                        progress = { (currentIndex + 1).toFloat() / reviewQueue.size },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(CircleShape)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Animated 3D Flip Card
                val rotation by animateFloatAsState(
                    targetValue = if (isFlipped) 180f else 0f,
                    animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing),
                    label = "cardFlip"
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .graphicsLayer {
                            rotationY = rotation
                            cameraDistance = 12 * density
                        }
                        .clip(RoundedCornerShape(24.dp))
                        .clickable {
                            isFlipped = !isFlipped
                            showHint = false
                        },
                    contentAlignment = Alignment.Center
                ) {
                    if (rotation <= 90f) {
                        // FRONT SIDE
                        CardFrontSide(
                            card = currentCard,
                            showHint = showHint,
                            onToggleHint = { showHint = !showHint }
                        )
                    } else {
                        // BACK SIDE (Mirrored back to fix rotation text)
                        Box(
                            modifier = Modifier.graphicsLayer { rotationY = 180f }
                        ) {
                            CardBackSide(card = currentCard)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Action Controls
                if (!isFlipped) {
                    Button(
                        onClick = { isFlipped = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Icon(Icons.Default.Flip, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Ver Resposta / Virar Cartão", style = MaterialTheme.typography.titleMedium)
                    }
                } else {
                    // Ebbinghaus Rating Buttons
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            "Como foi sua lembrança?",
                            style = MaterialTheme.typography.labelMedium,
                            modifier = Modifier.align(Alignment.CenterHorizontally),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Rating 1: Errou
                            RatingButton(
                                label = "Erro",
                                subText = "1 dia",
                                color = Color(0xFFEF4444),
                                modifier = Modifier.weight(1f),
                                onClick = {
                                    viewModel.processReview(currentCard, 1)
                                    reviewedCount++
                                    isFlipped = false
                                    currentIndex++
                                }
                            )

                            // Rating 2: Difícil
                            RatingButton(
                                label = "Difícil",
                                subText = "Poucos d",
                                color = Color(0xFFF59E0B),
                                modifier = Modifier.weight(1f),
                                onClick = {
                                    viewModel.processReview(currentCard, 2)
                                    reviewedCount++
                                    isFlipped = false
                                    currentIndex++
                                }
                            )

                            // Rating 3: Bom
                            RatingButton(
                                label = "Bom",
                                subText = "Curva OK",
                                color = Color(0xFF10B981),
                                modifier = Modifier.weight(1f),
                                onClick = {
                                    viewModel.processReview(currentCard, 3)
                                    reviewedCount++
                                    isFlipped = false
                                    currentIndex++
                                }
                            )

                            // Rating 4: Fácil
                            RatingButton(
                                label = "Fácil",
                                subText = "Expandir",
                                color = Color(0xFF6366F1),
                                modifier = Modifier.weight(1f),
                                onClick = {
                                    viewModel.processReview(currentCard, 4)
                                    reviewedCount++
                                    isFlipped = false
                                    currentIndex++
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CardFrontSide(
    card: FlashcardEntity,
    showHint: Boolean,
    onToggleHint: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxSize(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                SuggestionChip(
                    onClick = {},
                    label = { Text("Frente") }
                )

                if (!card.hint.isNullOrBlank()) {
                    IconButton(onClick = onToggleHint) {
                        Icon(
                            Icons.Default.Lightbulb,
                            contentDescription = "Dica",
                            tint = if (showHint) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.weight(1f),
            ) {
                Spacer(modifier = Modifier.weight(1f))

                if (!card.imageUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = card.imageUrl,
                        contentDescription = "Ilustração",
                        modifier = Modifier
                            .height(140.dp)
                            .fillMaxWidth(0.8f)
                            .clip(RoundedCornerShape(16.dp)),
                        contentScale = ContentScale.Crop
                    )
                }

                Text(
                    text = card.frontText,
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (showHint && !card.hint.isNullOrBlank()) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Text(
                            text = "💡 Dica: ${card.hint}",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }

                Spacer(modifier = Modifier.weight(1f))
            }

            Text(
                text = "Toque para virar e revelar a resposta 🔄",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
fun CardBackSide(card: FlashcardEntity) {
    Card(
        modifier = Modifier.fillMaxSize(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                SuggestionChip(
                    onClick = {},
                    label = { Text("Verso / Resposta") }
                )
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.weight(1f)
            ) {
                Spacer(modifier = Modifier.weight(1f))

                Text(
                    text = card.backText,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )

                if (!card.exampleSentence.isNullOrBlank()) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)
                    ) {
                        Text(
                            text = "💬 Exemplo: \"${card.exampleSentence}\"",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(12.dp),
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                Spacer(modifier = Modifier.weight(1f))
            }

            Text(
                text = "Selecione a dificuldade abaixo para atualizar o intervalo de Ebbinghaus",
                style = MaterialTheme.typography.labelSmall,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
            )
        }
    }
}

@Composable
fun RatingButton(
    label: String,
    subText: String,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(64.dp),
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(containerColor = color)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(label, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
            Text(subText, style = MaterialTheme.typography.labelSmall, fontSize = 10.sp)
        }
    }
}
