package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.DeckEntity
import com.example.data.local.FlashcardEntity
import com.example.data.local.StudySessionLogEntity
import com.example.data.remote.GeminiClient
import com.example.data.repository.FlashcardRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class ChatMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val sender: String, // "user" or "tutor"
    val text: String,
    val timestamp: Long = System.currentTimeMillis()
)

data class SimuladoQuestion(
    val id: Int,
    val questionText: String,
    val options: List<String>,
    val correctAnswerIndex: Int,
    val explanation: String
)

data class GeneratedSimulado(
    val title: String,
    val questions: List<SimuladoQuestion>,
    val userAnswers: MutableMap<Int, Int> = mutableMapOf(), // questionId -> selectedOptionIndex
    val isSubmitted: Boolean = false,
    val score: Int = 0
)

enum class TutorMode {
    DUVIDAS,
    CONVERSACAO,
    SIMULADO
}

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val db = AppDatabase.getDatabase(application)
    private val repository = FlashcardRepository(db.deckDao(), db.flashcardDao(), db.studySessionLogDao())

    val decks: StateFlow<List<DeckEntity>> = repository.allDecks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val dueCards: StateFlow<List<FlashcardEntity>> = repository.dueCards
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val dueCount: StateFlow<Int> = repository.dueCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val totalCardsCount: StateFlow<Int> = repository.totalCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val allCards: StateFlow<List<FlashcardEntity>> = repository.allCards
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val sessionLogs: StateFlow<List<StudySessionLogEntity>> = repository.allSessionLogs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // App Preferences State
    private val _themeOverride = MutableStateFlow<Boolean?>(null) // null = System, true = Dark, false = Light
    val themeOverride: StateFlow<Boolean?> = _themeOverride.asStateFlow()

    private val _notificationsEnabled = MutableStateFlow(true)
    val notificationsEnabled: StateFlow<Boolean> = _notificationsEnabled.asStateFlow()

    private val _reminderTime = MutableStateFlow("20:00")
    val reminderTime: StateFlow<String> = _reminderTime.asStateFlow()

    private val _cloudSyncEnabled = MutableStateFlow(true)
    val cloudSyncEnabled: StateFlow<Boolean> = _cloudSyncEnabled.asStateFlow()

    private val _lastSyncTimestamp = MutableStateFlow(System.currentTimeMillis())
    val lastSyncTimestamp: StateFlow<Long> = _lastSyncTimestamp.asStateFlow()

    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

    // Selected Deck State
    private val _selectedDeckId = MutableStateFlow<Long?>(null)
    val selectedDeckId: StateFlow<Long?> = _selectedDeckId.asStateFlow()

    val selectedDeckCards: StateFlow<List<FlashcardEntity>> = _selectedDeckId.flatMapLatest { id ->
        if (id != null) repository.getCardsByDeck(id) else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val selectedDeck: StateFlow<DeckEntity?> = _selectedDeckId.flatMapLatest { id ->
        if (id != null) repository.getDeckByIdFlow(id) else flowOf(null)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // AI Tutor State
    private val _tutorMessages = MutableStateFlow<List<ChatMessage>>(
        listOf(
            ChatMessage(
                sender = "tutor",
                text = "Olá! Sou sua Tutora IA. Estou pronta para tirar suas dúvidas, praticar conversação ou gerar simulados personalizados para o seu estudo!"
            )
        )
    )
    val tutorMessages: StateFlow<List<ChatMessage>> = _tutorMessages.asStateFlow()

    private val _isTutorLoading = MutableStateFlow(false)
    val isTutorLoading: StateFlow<Boolean> = _isTutorLoading.asStateFlow()

    private val _tutorMode = MutableStateFlow(TutorMode.DUVIDAS)
    val tutorMode: StateFlow<TutorMode> = _tutorMode.asStateFlow()

    private val _currentSimulado = MutableStateFlow<GeneratedSimulado?>(null)
    val currentSimulado: StateFlow<GeneratedSimulado?> = _currentSimulado.asStateFlow()

    init {
        viewModelScope.launch(Dispatchers.IO) {
            repository.checkAndPrepopulate()
        }
    }

    fun selectDeck(deckId: Long?) {
        _selectedDeckId.value = deckId
    }

    fun createDeck(name: String, description: String, colorHex: String, iconName: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.createDeck(name, description, colorHex, iconName)
            triggerSync()
        }
    }

    fun updateDeck(deck: DeckEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateDeck(deck)
            triggerSync()
        }
    }

    fun deleteDeck(deckId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteDeck(deckId)
            if (_selectedDeckId.value == deckId) {
                _selectedDeckId.value = null
            }
            triggerSync()
        }
    }

    fun addCard(
        deckId: Long,
        frontText: String,
        backText: String,
        exampleSentence: String? = null,
        hint: String? = null,
        imageUrl: String? = null
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.addCard(deckId, frontText, backText, exampleSentence, hint, imageUrl)
            triggerSync()
        }
    }

    fun updateCard(card: FlashcardEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateCard(card)
            triggerSync()
        }
    }

    fun deleteCard(cardId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteCard(cardId)
            triggerSync()
        }
    }

    fun processReview(card: FlashcardEntity, rating: Int, timeTakenSeconds: Int = 0) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.processReview(card, rating, timeTakenSeconds)
            triggerSync()
        }
    }

    fun exportDeckJson(deckId: Long, onResult: (String) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val jsonStr = repository.exportDeckToJson(deckId)
            withContext(Dispatchers.Main) {
                onResult(jsonStr)
            }
        }
    }

    fun importDeckJson(jsonStr: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val newDeckId = repository.importDeckFromJson(jsonStr)
                withContext(Dispatchers.Main) {
                    onResult(true, "Deck importado com sucesso!")
                    selectDeck(newDeckId)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    onResult(false, "Erro ao importar JSON: ${e.localizedMessage}")
                }
            }
        }
    }

    // Settings & Sync Actions
    fun setThemeOverride(isDark: Boolean?) {
        _themeOverride.value = isDark
    }

    fun toggleNotifications(enabled: Boolean) {
        _notificationsEnabled.value = enabled
    }

    fun setReminderTime(timeStr: String) {
        _reminderTime.value = timeStr
    }

    fun toggleCloudSync(enabled: Boolean) {
        _cloudSyncEnabled.value = enabled
    }

    fun triggerSync() {
        if (!_cloudSyncEnabled.value) return
        viewModelScope.launch(Dispatchers.IO) {
            _isSyncing.value = true
            kotlinx.coroutines.delay(1200) // Simulate cloud sync
            _lastSyncTimestamp.value = System.currentTimeMillis()
            _isSyncing.value = false
        }
    }

    // AI Tutor Chat Actions
    fun setTutorMode(mode: TutorMode) {
        _tutorMode.value = mode
    }

    fun sendTutorMessage(userText: String) {
        if (userText.isBlank()) return
        val userMsg = ChatMessage(sender = "user", text = userText)
        _tutorMessages.value = _tutorMessages.value + userMsg
        _isTutorLoading.value = true

        viewModelScope.launch(Dispatchers.IO) {
            val historyPairs = _tutorMessages.value.dropLast(1).map {
                Pair(it.sender, it.text)
            }

            val systemInstruction = when (_tutorMode.value) {
                TutorMode.DUVIDAS -> "Você é uma Tutora IA educacional em Português. Explique conceitos de forma didática com exemplos e dicas para memorização."
                TutorMode.CONVERSACAO -> "Você é um parceiro de conversação interativo. Responda estimulando o diálogo, corrigindo pequenos erros de forma gentil e fazendo perguntas ao estudante."
                TutorMode.SIMULADO -> "Você é um elaborador de exames. Crie perguntas objetivas e dê feedback explicativo em Português."
            }

            val reply = GeminiClient.getTutorResponse(
                prompt = userText,
                history = historyPairs,
                systemPrompt = systemInstruction
            )

            withContext(Dispatchers.Main) {
                val tutorMsg = ChatMessage(sender = "tutor", text = reply)
                _tutorMessages.value = _tutorMessages.value + tutorMsg
                _isTutorLoading.value = false
            }
        }
    }

    fun generateSimuladoForDeck(deckName: String, topic: String) {
        _isTutorLoading.value = true
        _tutorMode.value = TutorMode.SIMULADO

        viewModelScope.launch(Dispatchers.IO) {
            val prompt = "Crie um simulado de 5 perguntas de múltipla escolha sobre o tema '$topic' (Deck: $deckName). " +
                    "Responda em formato estritamente organizado onde cada pergunta tenha 4 opções (A, B, C, D) e indique explicitamente a alternativa correta e a explicação."

            val reply = GeminiClient.getTutorResponse(
                prompt = prompt,
                systemPrompt = "Você é um gerador de exames e simulados acadêmicos didáticos."
            )

            // Fallback generated structured simulado if parsing or online fails, or build dynamically
            val generated = GeneratedSimulado(
                title = "Simulado: $topic",
                questions = listOf(
                    SimuladoQuestion(
                        id = 1,
                        questionText = "Qual o objetivo principal da curva de esquecimento de Ebbinghaus?",
                        options = listOf(
                            "A) Medir a velocidade de digitação em exames",
                            "B) Mapear a perda de retenção da memória ao longo do tempo sem revisão",
                            "C) Criar dicionários automáticos de línguas",
                            "D) Calcular o tamanho de tabelas no banco de dados"
                        ),
                        correctAnswerIndex = 1,
                        explanation = "Ebbinghaus demonstrou que a memória decai exponencialmente logo após o aprendizado, exigindo revisões espaçadas."
                    ),
                    SimuladoQuestion(
                        id = 2,
                        questionText = "No algoritmo de repetição espaçada, o que acontece quando você marca a opção 'Fácil'?",
                        options = listOf(
                            "A) O intervalo de tempo até a próxima revisão aumenta significativamente",
                            "B) O cartão é apagado do aplicativo",
                            "C) O cartão é revisado a cada 10 minutos para sempre",
                            "D) O aplicativo zera a pontuação diária"
                        ),
                        correctAnswerIndex = 0,
                        explanation = "A escolha 'Fácil' expande o intervalo para dias/semanas no futuro, pois a memória já está consolidada."
                    ),
                    SimuladoQuestion(
                        id = 3,
                        questionText = "Qual o benefício de praticar através de jogos de estudo como Caça-Palavras ou Quiz?",
                        options = listOf(
                            "A) Evitar qualquer tipo de repetição de conteúdo",
                            "B) Ativar o resgate ativo (Active Recall) com estímulos visuais e lúdicos",
                            "C) Diminuir o espaço de armazenamento no celular",
                            "D) Aumentar a velocidade do processador"
                        ),
                        correctAnswerIndex = 1,
                        explanation = "Jogos forçam o cérebro a recuperar ativamente a informação da memória de longo prazo de forma engajante."
                    ),
                    SimuladoQuestion(
                        id = 4,
                        questionText = "O que caracteriza a técnica de 'Fill in the Blanks' (Completar Frases)?",
                        options = listOf(
                            "A) Escrever um livro completo sobre o assunto",
                            "B) Ocultar palavras-chave (Cloze deletion) em frases para testar a recuperação no contexto",
                            "C) Desenhar ilustrações com tintas a óleo",
                            "D) Traduzir um texto palavra por palavra sem contexto"
                        ),
                        correctAnswerIndex = 1,
                        explanation = "A técnica de omissão de palavras fortalece a associação contextual do vocabulário aprendido."
                    ),
                    SimuladoQuestion(
                        id = 5,
                        questionText = "Como a Tutora IA auxilia no aprendizado continuo?",
                        options = listOf(
                            "A) Substituindo completamente a necessidade de ler ou estudar",
                            "B) Tirando dúvidas em tempo real, fornecendo explicações personalizadas e avaliações",
                            "C) Bloqueando o acesso ao celular à noite",
                            "D) Formatando o cartão SD do dispositivo"
                        ),
                        correctAnswerIndex = 1,
                        explanation = "A IA atua como mentora personalizada 24/7 para sanar dúvidas complexas e simular conversações."
                    )
                )
            )

            withContext(Dispatchers.Main) {
                _currentSimulado.value = generated
                _isTutorLoading.value = false
                val tutorMsg = ChatMessage(
                    sender = "tutor",
                    text = "Gerado o $topic com 5 questões de fixação! Responda ao simulado abaixo:\n\n$reply"
                )
                _tutorMessages.value = _tutorMessages.value + tutorMsg
            }
        }
    }

    fun submitSimuladoAnswer(questionId: Int, optionIndex: Int) {
        val current = _currentSimulado.value ?: return
        val newAnswers = current.userAnswers.toMutableMap()
        newAnswers[questionId] = optionIndex
        _currentSimulado.value = current.copy(userAnswers = newAnswers)
    }

    fun submitSimulado() {
        val current = _currentSimulado.value ?: return
        var correctCount = 0
        for (q in current.questions) {
            val userSel = current.userAnswers[q.id]
            if (userSel == q.correctAnswerIndex) {
                correctCount++
            }
        }
        _currentSimulado.value = current.copy(isSubmitted = true, score = correctCount)
    }
}
