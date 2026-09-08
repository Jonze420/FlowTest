package com.example.stompflow.data.model

data class Patch(
    val id: String,
    val name: String,
    val isPreset: Boolean = false,
    val input: String = "device", // "device", "engine", "usb"
    val output: String = "engine", // "engine", "phones", "speakers"
    val master: Int = 80,
    val chain: List<EffectNode> = emptyList()
) {
    companion object {
        val DEFAULT_PATCH = Patch(
            id = "default",
            name = "Clean Slate",
            chain = listOf(
                EffectNode("e1", EffectType.COMPRESSOR, enabled = true, param = 45),
                EffectNode("e2", EffectType.DELAY, enabled = true, param = 30)
            )
        )

        val PRESETS = listOf(
            Patch(
                id = "p_clean",
                name = "Clean",
                isPreset = true,
                master = 80,
                chain = listOf(
                    EffectNode("c1", EffectType.COMPRESSOR, enabled = true, param = 35),
                    EffectNode("c2", EffectType.REVERB, enabled = true, param = 25)
                )
            ),
            Patch(
                id = "p_crunch",
                name = "Crunch",
                isPreset = true,
                master = 82,
                chain = listOf(
                    EffectNode("cr1", EffectType.DRIVE, enabled = true, param = 40),
                    EffectNode("cr2", EffectType.DELAY, enabled = true, param = 20)
                )
            ),
            Patch(
                id = "p_lead",
                name = "Lead",
                isPreset = true,
                master = 85,
                chain = listOf(
                    EffectNode("l1", EffectType.OVERDRIVE, enabled = true, param = 60),
                    EffectNode("l2", EffectType.DELAY, enabled = true, param = 35),
                    EffectNode("l3", EffectType.REVERB, enabled = true, param = 30)
                )
            ),
            Patch(
                id = "p_ambient",
                name = "Ambient",
                isPreset = true,
                master = 78,
                chain = listOf(
                    EffectNode("a1", EffectType.REVERB, enabled = true, param = 60),
                    EffectNode("a2", EffectType.DELAY, enabled = true, param = 45),
                    EffectNode("a3", EffectType.CHORUS, enabled = true, param = 40)
                )
            ),
            Patch(
                id = "p_acoustic",
                name = "Acoustic",
                isPreset = true,
                master = 80,
                chain = listOf(
                    EffectNode("ac1", EffectType.COMPRESSOR, enabled = true, param = 35),
                    EffectNode("ac2", EffectType.CHORUS, enabled = true, param = 20)
                )
            ),
            Patch(
                id = "p_funk",
                name = "Funk",
                isPreset = true,
                master = 82,
                chain = listOf(
                    EffectNode("f1", EffectType.COMPRESSOR, enabled = true, param = 50),
                    EffectNode("f2", EffectType.PHASER, enabled = true, param = 40)
                )
            )
        )
    }
}
