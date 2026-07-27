package com.example.data.repository

import com.example.data.local.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import org.json.JSONArray
import org.json.JSONObject
import kotlin.math.max

data class ExportedDeck(
    val deckName: String,
    val description: String,
    val colorHex: String,
    val cards: List<ExportedCard>
)

data class ExportedCard(
    val frontText: String,
    val backText: String,
    val exampleSentence: String?,
    val hint: String?,
    val imageUrl: String?
)

class FlashcardRepository(
    private val deckDao: DeckDao,
    private val flashcardDao: FlashcardDao,
    private val logDao: StudySessionLogDao
) {
    val allDecks: Flow<List<DeckEntity>> = deckDao.getAllDecks()
    val allCards: Flow<List<FlashcardEntity>> = flashcardDao.getAllCards()
    val dueCards: Flow<List<FlashcardEntity>> = flashcardDao.getDueCards()
    val dueCount: Flow<Int> = flashcardDao.getDueCountFlow()
    val totalCount: Flow<Int> = flashcardDao.getTotalCountFlow()
    val allSessionLogs: Flow<List<StudySessionLogEntity>> = logDao.getAllLogs()

    fun getCardsByDeck(deckId: Long): Flow<List<FlashcardEntity>> = flashcardDao.getCardsByDeck(deckId)
    fun getDueCardsByDeck(deckId: Long): Flow<List<FlashcardEntity>> = flashcardDao.getDueCardsByDeck(deckId)
    fun getDeckByIdFlow(deckId: Long): Flow<DeckEntity?> = deckDao.getDeckByIdFlow(deckId)

    suspend fun createDeck(name: String, description: String, colorHex: String, iconName: String): Long {
        return deckDao.insertDeck(
            DeckEntity(
                name = name,
                description = description,
                colorHex = colorHex,
                iconName = iconName
            )
        )
    }

    suspend fun updateDeck(deck: DeckEntity) {
        deckDao.updateDeck(deck)
    }

    suspend fun deleteDeck(deckId: Long) {
        deckDao.deleteDeckById(deckId)
    }

    suspend fun addCard(
        deckId: Long,
        frontText: String,
        backText: String,
        exampleSentence: String? = null,
        hint: String? = null,
        imageUrl: String? = null
    ): Long {
        return flashcardDao.insertCard(
            FlashcardEntity(
                deckId = deckId,
                frontText = frontText,
                backText = backText,
                exampleSentence = exampleSentence,
                hint = hint,
                imageUrl = imageUrl
            )
        )
    }

    suspend fun updateCard(card: FlashcardEntity) {
        flashcardDao.updateCard(card)
    }

    suspend fun deleteCard(cardId: Long) {
        flashcardDao.deleteCardById(cardId)
    }

    /**
     * Ebbinghaus Forgetting Curve Spaced Repetition Algorithm
     * Rating: 1 = Errou/Esqueci, 2 = Difícil, 3 = Bom, 4 = Fácil
     */
    suspend fun processReview(card: FlashcardEntity, rating: Int, timeTakenSeconds: Int = 0) {
        val now = System.currentTimeMillis()
        var newInterval = card.intervalDays
        var newRepetition = card.repetitionCount
        var newEase = card.easeFactor
        var newMastery = card.masteryLevel

        when (rating) {
            1 -> { // Errou - Reset interval to 1 day
                newInterval = 1
                newRepetition = 0
                newEase = max(1.3f, newEase - 0.2f)
                newMastery = 1 // Aprendendo
            }
            2 -> { // Difícil - Slight increase
                newInterval = max(1, (newInterval * 1.2f).toInt())
                newRepetition += 1
                newEase = max(1.3f, newEase - 0.15f)
                newMastery = if (newRepetition > 3) 2 else 1
            }
            3 -> { // Bom - Standard Ebbinghaus curve growth (1, 2, 6, 14, 30 days)
                newInterval = when (newRepetition) {
                    0 -> 1
                    1 -> 2
                    2 -> 6
                    3 -> 14
                    else -> max(30, (newInterval * newEase).toInt())
                }
                newRepetition += 1
                newMastery = when {
                    newRepetition >= 4 -> 3 // Dominado
                    newRepetition >= 2 -> 2 // Revisando
                    else -> 1
                }
            }
            4 -> { // Fácil - Accelerated growth
                newInterval = when (newRepetition) {
                    0 -> 2
                    1 -> 5
                    2 -> 12
                    else -> max(45, (newInterval * newEase * 1.3f).toInt())
                }
                newRepetition += 1
                newEase += 0.15f
                newMastery = if (newRepetition >= 2) 3 else 2
            }
        }

        val nextReview = now + (newInterval.toLong() * 24 * 60 * 60 * 1000L)

        val updatedCard = card.copy(
            intervalDays = newInterval,
            repetitionCount = newRepetition,
            easeFactor = newEase,
            nextReviewTimestamp = nextReview,
            lastReviewedTimestamp = now,
            masteryLevel = newMastery
        )

        flashcardDao.updateCard(updatedCard)

        // Log the study session
        logDao.insertLog(
            StudySessionLogEntity(
                deckId = card.deckId,
                cardId = card.id,
                rating = rating,
                timestamp = now,
                timeTakenSeconds = timeTakenSeconds
            )
        )
    }

    /**
     * Export deck and its cards as JSON String
     */
    suspend fun exportDeckToJson(deckId: Long): String {
        val deck = deckDao.getDeckById(deckId) ?: return "{}"
        val cards = flashcardDao.getCardsByDeckList(deckId)

        val rootJson = JSONObject()
        rootJson.put("deckName", deck.name)
        rootJson.put("description", deck.description)
        rootJson.put("colorHex", deck.colorHex)
        rootJson.put("iconName", deck.iconName)

        val cardsArray = JSONArray()
        for (c in cards) {
            val cardObj = JSONObject()
            cardObj.put("frontText", c.frontText)
            cardObj.put("backText", c.backText)
            cardObj.put("exampleSentence", c.exampleSentence ?: "")
            cardObj.put("hint", c.hint ?: "")
            cardObj.put("imageUrl", c.imageUrl ?: "")
            cardsArray.put(cardObj)
        }
        rootJson.put("cards", cardsArray)

        return rootJson.toString(2)
    }

    /**
     * Import deck and cards from JSON String
     */
    suspend fun importDeckFromJson(jsonStr: String): Long {
        val root = JSONObject(jsonStr)
        val deckName = root.optString("deckName", "Deck Importado")
        val description = root.optString("description", "Importado em formato offline")
        val colorHex = root.optString("colorHex", "#10B981")
        val iconName = root.optString("iconName", "book")

        val newDeckId = createDeck(deckName, description, colorHex, iconName)

        val cardsArray = root.optJSONArray("cards") ?: JSONArray()
        for (i in 0 until cardsArray.length()) {
            val cardObj = cardsArray.getJSONObject(i)
            val front = cardObj.optString("frontText", "")
            val back = cardObj.optString("backText", "")
            val sentence = cardObj.optString("exampleSentence").ifBlank { null }
            val hint = cardObj.optString("hint").ifBlank { null }
            val img = cardObj.optString("imageUrl").ifBlank { null }

            if (front.isNotBlank() && back.isNotBlank()) {
                addCard(newDeckId, front, back, sentence, hint, img)
            }
        }
        return newDeckId
    }

    /**
     * Pre-populate database with default rich decks if empty on first launch
     */
    suspend fun checkAndPrepopulate() {
        val currentDecks = allDecks.first()
        if (currentDecks.isEmpty()) {
            // Deck 1: Inglês Vocabulário
            val deck1Id = createDeck(
                name = "Inglês Essencial (Ebbinghaus)",
                description = "Expressões e vocabulário avançado com sentenças de contexto.",
                colorHex = "#6366F1", // Indigo
                iconName = "language"
            )
            addCard(deck1Id, "Ebullient", "Cheio de energia e entusiasmo; efervescente.", "She sounded ebullient on the phone.", "Alegre / Entusiasmado")
            addCard(deck1Id, "Serendipity", "Acaso feliz; encontrar algo bom por sorte sem procurar.", "Finding this rare book was pure serendipity.", "Sorte inesperada")
            addCard(deck1Id, "Resilient", "Capaz de se recuperar rapidamente de dificuldades; resiliente.", "He is a resilient person who never gives up.", "Firme / Resiliente")
            addCard(deck1Id, "Ephemeral", "Durando por um tempo muito curto; efêmero.", "Fame in the digital age is often ephemeral.", "Passageiro")
            addCard(deck1Id, "Eloquent", "Fluente e persuasivo ao falar ou escrever.", "The speaker gave an eloquent presentation.", "Expressivo / Persuasivo")

            // Deck 2: Programação Kotlin & Android
            val deck2Id = createDeck(
                name = "Kotlin & Android Jetpack",
                description = "Conceitos fundamentais de State, Coroutines e Room.",
                colorHex = "#10B981", // Emerald Green
                iconName = "code"
            )
            addCard(deck2Id, "StateFlow", "Um flow observável e com estado que emite atualizações do estado atual e novo.", "val state = _uiState.asStateFlow()", "Reativo com valor inicial")
            addCard(deck2Id, "Coroutines Dispatchers.IO", "Otimizado para operações de E/S fora da thread principal (disco e rede).", "withContext(Dispatchers.IO) { ... }", "E/S em segundo plano")
            addCard(deck2Id, "Room DAO", "Interface contendo os métodos usados para acessar o banco de dados SQLite.", "@Dao interface ItemDao { ... }", "Data Access Object")
            addCard(deck2Id, "Remember in Compose", "Guarda um valor na memória durante a recomposição do componente.", "var count by remember { mutableStateOf(0) }", "Preserva estado no Compose")

            // Deck 3: Anatomia & Saúde
            val deck3Id = createDeck(
                name = "Anatomia e Fisiologia",
                description = "Termos médicos, sistemas do corpo humano e funções de órgãos.",
                colorHex = "#EF4444", // Red/Pink
                iconName = "science"
            )
            addCard(deck3Id, "Miocárdio", "Tecido muscular espesso que forma a camada intermediária da parede do coração.", "O miocárdio bombardeia o sangue para todo o organismo.", "Músculo cardíaco")
            addCard(deck3Id, "Sinapse", "Região de contato quase direto entre a extremidade de um neurônio e outra célula.", "Os neurotransmissores transmitem o sinal através da sinapse.", "Comunicação neuronal")
            addCard(deck3Id, "Hemoglobina", "Proteína contida nos glóbulos vermelhos responsável pelo transporte de oxigênio.", "Níveis normais de hemoglobina indicam boa capacidade de transporte de O2.", "Transporta O2 no sangue")

            // Deck 4: Espanhol Básico
            val deck4Id = createDeck(
                name = "Español Práctico",
                description = "Falsos cognatos e frases diárias de conversação.",
                colorHex = "#F59E0B", // Amber
                iconName = "language"
            )
            addCard(deck4Id, "Embarazada", "Grávida (e não envergonhada!).", "Ella está embarazada de seis meses.", "Falso amigo: quer dizer grávida")
            addCard(deck4Id, "Acorralar", "Cercar, encurralar ou deixar sem saída.", "El perro acorraló al gato en la esquina.", "Encurralar")
            addCard(deck4Id, "Polvo", "Poeira / Pó (e não polvo bicho de mar que é pulpo).", "Hay mucho polvo sobre la mesa.", "Poeira")
        }
    }
}
