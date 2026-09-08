package com.example.stompflow.data.model

import androidx.compose.ui.graphics.Color

enum class DrumVoice(
    val id: String,
    val label: String,
    val color: Color
) {
    KICK("kick", "Kick", Color(0xFFF97415)),
    SNARE("snare", "Snare", Color(0xFFDC2828)),
    HIHAT("hihat", "HiHat", Color(0xFF2F7FD6)),
    CLAP("clap", "Clap", Color(0xFF8B5CF6)),
    TOM("tom", "Tom", Color(0xFF279B74));
}

data class RhythmPattern(
    val id: String,
    val name: String,
    val bpm: Int = 96,
    val swing: Int = 0,
    val isPreset: Boolean = false,
    val steps: List<List<Boolean>> = List(5) { List(16) { false } }
) {
    companion object {
        private fun beats(vararg activeIndices: Int): List<Boolean> {
            val list = MutableList(16) { false }
            for (idx in activeIndices) {
                if (idx in 0 until 16) list[idx] = true
            }
            return list
        }

        val DEFAULT_PATTERN = RhythmPattern(
            id = "steady_beat",
            name = "Steady Beat",
            bpm = 96,
            swing = 0,
            steps = listOf(
                beats(0, 4, 8, 12),
                beats(4, 12),
                beats(0, 2, 4, 6, 8, 10, 12, 14),
                beats(),
                beats()
            )
        )

        val PRESETS = listOf(
            RhythmPattern(
                id = "four_on_floor",
                name = "Four-on-the-Floor",
                bpm = 124,
                swing = 0,
                isPreset = true,
                steps = listOf(
                    beats(0, 4, 8, 12),
                    beats(4, 12),
                    beats(0, 2, 4, 6, 8, 10, 12, 14),
                    beats(),
                    beats()
                )
            ),
            RhythmPattern(
                id = "basic_rock",
                name = "Basic Rock",
                bpm = 110,
                swing = 0,
                isPreset = true,
                steps = listOf(
                    beats(0, 8),
                    beats(4, 12),
                    beats(0, 2, 4, 6, 8, 10, 12, 14),
                    beats(),
                    beats()
                )
            ),
            RhythmPattern(
                id = "pop_backbeat",
                name = "Pop Backbeat",
                bpm = 100,
                swing = 0,
                isPreset = true,
                steps = listOf(
                    beats(0, 8),
                    beats(4, 12),
                    beats(0, 2, 4, 6, 8, 10, 12, 14),
                    beats(4, 12),
                    beats()
                )
            ),
            RhythmPattern(
                id = "shuffle_swing",
                name = "Shuffle / Swing",
                bpm = 96,
                swing = 55,
                isPreset = true,
                steps = listOf(
                    beats(0, 8),
                    beats(4, 12),
                    beats(0, 3, 4, 6, 8, 11, 12, 14),
                    beats(),
                    beats()
                )
            ),
            RhythmPattern(
                id = "funk_groove",
                name = "Funk Groove",
                bpm = 100,
                swing = 16,
                isPreset = true,
                steps = listOf(
                    beats(0, 3, 8, 11),
                    beats(4, 12),
                    beats(0, 2, 3, 4, 6, 8, 10, 11, 12, 14),
                    beats(4, 12),
                    beats()
                )
            ),
            RhythmPattern(
                id = "boom_bap",
                name = "Boom-Bap",
                bpm = 88,
                swing = 25,
                isPreset = true,
                steps = listOf(
                    beats(0, 6, 10),
                    beats(4, 12),
                    beats(0, 2, 4, 6, 8, 10, 12, 14, 15),
                    beats(),
                    beats()
                )
            ),
            RhythmPattern(
                id = "trap_hats",
                name = "Trap Hats",
                bpm = 140,
                swing = 0,
                isPreset = true,
                steps = listOf(
                    beats(0, 7, 10),
                    beats(4, 12),
                    beats(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15),
                    beats(),
                    beats(11, 14)
                )
            ),
            RhythmPattern(
                id = "house",
                name = "House",
                bpm = 124,
                swing = 0,
                isPreset = true,
                steps = listOf(
                    beats(0, 4, 8, 12),
                    beats(),
                    beats(2, 6, 10, 14),
                    beats(4, 12),
                    beats()
                )
            )
        )
    }
}
