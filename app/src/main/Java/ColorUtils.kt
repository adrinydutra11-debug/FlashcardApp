package com.example.ui.util

import androidx.compose.ui.graphics.Color

object ColorUtils {
    val PRESET_CATEGORY_COLORS = listOf(
        "#6366F1", // Indigo
        "#10B981", // Emerald
        "#EF4444", // Crimson Red
        "#F59E0B", // Amber
        "#8B5CF6", // Purple
        "#EC4899", // Pink
        "#06B6D4", // Cyan
        "#3B82F6", // Blue
        "#14B8A6", // Teal
        "#F97316"  // Orange
    )

    fun parseHexColor(hexString: String, fallback: Color = Color(0xFF6366F1)): Color {
        return try {
            val cleanHex = hexString.replace("#", "").trim()
            val colorInt = when (cleanHex.length) {
                6 -> ("FF$cleanHex").toLong(16).toInt()
                8 -> cleanHex.toLong(16).toInt()
                else -> return fallback
            }
            Color(colorInt)
        } catch (e: Exception) {
            fallback
        }
    }
}
