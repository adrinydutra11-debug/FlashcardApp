package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "study_session_logs")
data class StudySessionLogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val deckId: Long,
    val cardId: Long,
    val rating: Int, // 1 = Errou, 2 = Difícil, 3 = Bom, 4 = Fácil
    val timestamp: Long = System.currentTimeMillis(),
    val timeTakenSeconds: Int = 0
)
