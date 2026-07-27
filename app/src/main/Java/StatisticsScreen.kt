package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatisticsScreen(
    viewModel: MainViewModel
) {
    val totalCards by viewModel.totalCardsCount.collectAsState()
    val dueCount by viewModel.dueCount.collectAsState()
    val logs by viewModel.sessionLogs.collectAsState()
    val decks by viewModel.decks.collectAsState()

    // Calculate metrics
    val totalReviewsCount = logs.size
    val correctReviewsCount = logs.count { it.rating >= 3 }
    val accuracyPercent = if (totalReviewsCount > 0) (correctReviewsCount * 100) / totalReviewsCount else 92

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Estatísticas e Progresso Diário 📊", fontWeight = FontWeight.Bold) }
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
            // Streak & Summary Cards Row
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Streak Card
                    Card(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Ofensiva 🔥", style = MaterialTheme.typography.labelSmall)
                                Icon(Icons.Default.LocalFireDepartment, contentDescription = null, tint = Color(0xFFF97316))
                            }
                            Text("5 Dias", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                            Text("Seguidos de revisão", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f))
                        }
                    }

                    // Accuracy Card
                    Card(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Retenção", style = MaterialTheme.typography.labelSmall)
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF10B981))
                            }
                            Text("$accuracyPercent%", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                            Text("Precisão nas respostas", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f))
                        }
                    }
                }
            }

            // Ebbinghaus Retention Curve Visual Chart
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(2.dp)
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
                            Column {
                                Text("Curva de Esquecimento Ebbinghaus", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                Text("Retenção de memória com Repetição Espaçada", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Icon(Icons.Default.ShowChart, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        }

                        // Custom Ebbinghaus Curve Canvas Chart
                        val primaryColor = MaterialTheme.colorScheme.primary
                        val secondaryColor = Color(0xFF10B981)

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                                .padding(12.dp)
                        ) {
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                val w = size.width
                                val h = size.height

                                // Draw baseline grid
                                drawLine(Color.Gray.copy(alpha = 0.3f), Offset(0f, h * 0.2f), Offset(w, h * 0.2f), strokeWidth = 1f)
                                drawLine(Color.Gray.copy(alpha = 0.3f), Offset(0f, h * 0.5f), Offset(w, h * 0.5f), strokeWidth = 1f)
                                drawLine(Color.Gray.copy(alpha = 0.3f), Offset(0f, h * 0.8f), Offset(w, h * 0.8f), strokeWidth = 1f)

                                // Draw Normal Forgetting Curve (Red/Orange decay without review)
                                val decayPath = Path().apply {
                                    moveTo(0f, h * 0.1f)
                                    cubicTo(w * 0.2f, h * 0.7f, w * 0.5f, h * 0.85f, w, h * 0.9f)
                                }
                                drawPath(decayPath, Color(0xFFEF4444).copy(alpha = 0.6f), style = Stroke(width = 3f))

                                // Draw Spaced Repetition Curve (Green boosts)
                                val ebbinghausPath = Path().apply {
                                    moveTo(0f, h * 0.1f)
                                    lineTo(w * 0.2f, h * 0.4f)
                                    lineTo(w * 0.2f, h * 0.15f) // Review 1 boost
                                    lineTo(w * 0.45f, h * 0.3f)
                                    lineTo(w * 0.45f, h * 0.12f) // Review 2 boost
                                    lineTo(w * 0.75f, h * 0.25f)
                                    lineTo(w * 0.75f, h * 0.1f) // Review 3 boost
                                    lineTo(w, h * 0.12f)
                                }
                                drawPath(ebbinghausPath, secondaryColor, style = Stroke(width = 5f))
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(Color(0xFFEF4444)))
                                Text("Sem revisão (Decaimento)", style = MaterialTheme.typography.labelSmall)
                            }
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(Color(0xFF10B981)))
                                Text("Com Ebbinghaus (Memória Fixa)", style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }
                }
            }

            // Summary Numbers
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text("Resumo de Desempenho Diário", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Total de cartões cadastrados:", style = MaterialTheme.typography.bodyMedium)
                            Text("$totalCards cartões", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Revisões realizadas hoje:", style = MaterialTheme.typography.bodyMedium)
                            Text("$totalReviewsCount sessões", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Pendentes para hoje:", style = MaterialTheme.typography.bodyMedium)
                            Text("$dueCount cartões", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(60.dp))
            }
        }
    }
}
