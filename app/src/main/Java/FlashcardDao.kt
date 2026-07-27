package com.example.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface FlashcardDao {
    @Query("SELECT * FROM flashcards WHERE deckId = :deckId")
    fun getCardsByDeck(deckId: Long): Flow<List<FlashcardEntity>>

    @Query("SELECT * FROM flashcards WHERE deckId = :deckId")
    suspend fun getCardsByDeckList(deckId: Long): List<FlashcardEntity>

    @Query("SELECT * FROM flashcards")
    fun getAllCards(): Flow<List<FlashcardEntity>>

    @Query("SELECT * FROM flashcards")
    suspend fun getAllCardsList(): List<FlashcardEntity>

    @Query("SELECT * FROM flashcards WHERE nextReviewTimestamp <= :currentTime")
    fun getDueCards(currentTime: Long = System.currentTimeMillis()): Flow<List<FlashcardEntity>>

    @Query("SELECT * FROM flashcards WHERE deckId = :deckId AND nextReviewTimestamp <= :currentTime")
    fun getDueCardsByDeck(deckId: Long, currentTime: Long = System.currentTimeMillis()): Flow<List<FlashcardEntity>>

    @Query("SELECT * FROM flashcards WHERE id = :cardId")
    suspend fun getCardById(cardId: Long): FlashcardEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCard(card: FlashcardEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCards(cards: List<FlashcardEntity>)

    @Update
    suspend fun updateCard(card: FlashcardEntity)

    @Delete
    suspend fun deleteCard(card: FlashcardEntity)

    @Query("DELETE FROM flashcards WHERE id = :cardId")
    suspend fun deleteCardById(cardId: Long)

    @Query("SELECT COUNT(*) FROM flashcards WHERE nextReviewTimestamp <= :currentTime")
    fun getDueCountFlow(currentTime: Long = System.currentTimeMillis()): Flow<Int>

    @Query("SELECT COUNT(*) FROM flashcards")
    fun getTotalCountFlow(): Flow<Int>
}
