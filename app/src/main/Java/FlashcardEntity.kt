package com.example.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "flashcards",
    foreignKeys = [
        ForeignKey(
            entity = DeckEntity::class,
            parentColumns = ["id"],
            childColumns = ["deckId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("deckId")]
)
data class FlashcardEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val deckId: Long,
    val frontText: String,
    val backText: String,
    val imageUrl: String? = null,
    val exampleSentence: String? = null,
    val hint: String? = null,
    val intervalDays: Int = 1,
    val repetitionCount: Int = 0,
    val easeFactor: Float = 2.5f,
    val nextReviewTimestamp: Long = System.currentTimeMillis(),
    val lastReviewedTimestamp: Long? = null,
    val masteryLevel: Int = 0 // 0 = Novo, 1 = Aprendendo, 2 = Revisando, 3 = Dominado
)
