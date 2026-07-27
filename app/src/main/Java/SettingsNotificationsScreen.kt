package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
fun SettingsNotificationsScreen(
    viewModel: MainViewModel
) {
    val context = LocalContext.current

    val themeOverride by viewModel.themeOverride.collectAsState()
    val notificationsEnabled by viewModel.notificationsEnabled.collectAsState()
    val reminderTime by viewModel.reminderTime.collectAsState()
    val cloudSyncEnabled by viewModel.cloudSyncEnabled.collectAsState()
    val lastSync by viewModel.lastSyncTimestamp.collectAsState()
    val isSyncing by viewModel.isSyncing.collectAsState()

    var importJsonText by remember { mutableStateOf("") }
    var showImportDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Configurações & Notificações ⚙️", fontWeight = FontWeight.Bold) }
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
            // Section 1: Appearance & Night Mode
            Text("Aparência & Tema", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.DarkMode, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Column {
                                Text("Modo Noturno", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                Text("Alternar tema da interface", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = themeOverride == null,
                            onClick = { viewModel.setThemeOverride(null) },
                            label = { Text("Automático (Sistema)") },
                            modifier = Modifier.weight(1f)
                        )
                        FilterChip(
                            selected = themeOverride == false,
                            onClick = { viewModel.setThemeOverride(false) },
                            label = { Text("Claro") },
                            modifier = Modifier.weight(1f)
                        )
                        FilterChip(
                            selected = themeOverride == true,
                            onClick = { viewModel.setThemeOverride(true) },
                            label = { Text("Escuro") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Section 2: Smart Notifications System
            Text("Lembretes & Notificações Inteligentes", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.NotificationsActive, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Column {
                                Text("Notificações de Revisão", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                Text("Alertas no ponto crítico da curva de esquecimento", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        Switch(
                            checked = notificationsEnabled,
                            onCheckedChange = { viewModel.toggleNotifications(it) }
                        )
                    }

                    if (notificationsEnabled) {
                        HorizontalDivider()
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Horário do Lembrete Diário:", style = MaterialTheme.typography.bodyMedium)
                            AssistChip(
                                onClick = {
                                    viewModel.setReminderTime("20:00")
                                    Toast.makeText(context, "Lembrete agendado para às 20:00", Toast.LENGTH_SHORT).show()
                                },
                                label = { Text(reminderTime) },
                                leadingIcon = { Icon(Icons.Default.Schedule, contentDescription = null, modifier = Modifier.size(16.dp)) }
                            )
                        }
                    }
                }
            }

            // Section 3: Cloud Sync for Multiple Devices
            Text("Sincronização na Nuvem", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CloudSync, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Column {
                                Text("Sincronização Multi-Dispositivo", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                Text("Sincronizar progresso de estudo na nuvem", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        Switch(
                            checked = cloudSyncEnabled,
                            onCheckedChange = { viewModel.toggleCloudSync(it) }
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isSyncing) "Sincronizando dados..." else "Sincronizado na nuvem",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        OutlinedButton(
                            onClick = { viewModel.triggerSync() },
                            enabled = !isSyncing && cloudSyncEnabled,
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            if (isSyncing) {
                                CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                            } else {
                                Icon(Icons.Default.Sync, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Sincronizar Agora")
                            }
                        }
                    }
                }
            }

            // Section 4: Offline Backup & Import Decks
            Text("Backup Offline & Importação", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.ImportExport, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Column {
                            Text("Exportar e Importar Decks", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                            Text("Importe decks no formato JSON para revisão totalmente offline.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }

                    Button(
                        onClick = { showImportDialog = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.FileDownload, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Importar Deck de Arquivo JSON")
                    }
                }
            }

            Spacer(modifier = Modifier.height(60.dp))
        }
    }

    if (showImportDialog) {
        AlertDialog(
            onDismissRequest = { showImportDialog = false },
            title = { Text("Importar Deck Offline (JSON)") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Cole o código JSON do deck que deseja importar:")
                    OutlinedTextField(
                        value = importJsonText,
                        onValueChange = { importJsonText = it },
                        placeholder = { Text("Cole o JSON aqui...") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (importJsonText.isNotBlank()) {
                            viewModel.importDeckJson(importJsonText) { success, msg ->
                                Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                                if (success) {
                                    showImportDialog = false
                                    importJsonText = ""
                                }
                            }
                        }
                    },
                    enabled = importJsonText.isNotBlank()
                ) {
                    Text("Importar Deck")
                }
            },
            dismissButton = {
                TextButton(onClick = { showImportDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}
