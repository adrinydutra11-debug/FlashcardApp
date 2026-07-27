package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "decks")
data class DeckEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val description: String,
    val colorHex: String = "#6366F1", // Default purple indigo
    val iconName: String = "book", // e.g., "language", "code", "science", "history", "book"
    val createdAt: Long = System.currentTimeMillis()
)
