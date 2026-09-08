package com.example.stompflow.data.model

import androidx.compose.ui.graphics.Color

enum class EffectType(
    val id: String,
    val label: String,
    val color: Color,
    val paramName: String,
    val defaultParam: Int = 50
) {
    DRIVE("drive", "Drive", Color(0xFFE74C3C), "Gain", 40),
    OVERDRIVE("overdrive", "Overdrive", Color(0xFFE67E22), "Drive", 60),
    DELAY("delay", "Delay", Color(0xFF3498DB), "Feedback", 30),
    REVERB("reverb", "Reverb", Color(0xFF9B59B6), "Mix", 35),
    CHORUS("chorus", "Chorus", Color(0xFF2ECC71), "Depth", 40),
    PHASER("phaser", "Phaser", Color(0xFF1ABC9C), "Rate", 45),
    TREMOLO("tremolo", "Tremolo", Color(0xFFF39C12), "Speed", 50),
    COMPRESSOR("compressor", "Comp", Color(0xFF7F8C8D), "Ratio", 45),
    EQ("eq", "EQ", Color(0xFF34495E), "Band", 50);

    companion object {
        fun fromId(id: String): EffectType =
            entries.firstOrNull { it.id == id } ?: DRIVE
    }
}

data class EffectNode(
    val uid: String,
    val effectType: EffectType,
    val enabled: Boolean = true,
    val param: Int = 50
)
