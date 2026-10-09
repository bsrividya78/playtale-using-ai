package com.example.data.model

enum class PetMood(
    val title: String,
    val emoji: String,
    val glowColorHex: Long
) {
    ECSTATIC("Ecstatic", "🤩", 0xFFF59E0B),
    HAPPY("Happy & Cheerful", "🥰", 0xFF10B981),
    STUDIOUS("Focused Learner", "🧐", 0xFF4F46E5),
    HUNGRY("Hungry", "🍖", 0xFFEA580C),
    STARVING("Very Hungry", "🥺", 0xFFDC2626),
    COMFORTING("Supportive Buddy", "🐾", 0xFF8B5CF6),
    EXCITED("Game Master", "🎮", 0xFF06B6D4)
}

data class PetState(
    val name: String = "Buddy",
    val happiness: Int = 80, // 0 - 100%
    val hunger: Int = 40,    // 0 - 100% (lower is better fed)
    val moodMessage: String = "You're doing great! 🐶",
    val timesFed: Int = 0,
    val lastFedTimestamp: Long = System.currentTimeMillis(),
    val lastInteraction: String = "IDLE",
    val consecutiveCorrect: Int = 0
) {
    val currentMood: PetMood
        get() = when {
            hunger >= 80 -> PetMood.STARVING
            hunger >= 60 -> PetMood.HUNGRY
            lastInteraction == "QUIZ_HIGH_SCORE" || consecutiveCorrect >= 3 -> PetMood.ECSTATIC
            lastInteraction == "PLAYED_GAME" -> PetMood.EXCITED
            lastInteraction == "JUST_INCORRECT" || lastInteraction == "QUIZ_LOW_SCORE" -> PetMood.COMFORTING
            happiness >= 75 -> PetMood.HAPPY
            else -> PetMood.STUDIOUS
        }

    val moodEmoji: String
        get() = currentMood.emoji

    val statusText: String
        get() = when (currentMood) {
            PetMood.STARVING -> "Buddy's tummy is growling! Needs a treat now! 🥺🍖"
            PetMood.HUNGRY -> "Study snack time! Buddy would love some dog food 🍖"
            PetMood.ECSTATIC -> "Buddy is doing a happy dance! You're on fire! 🌟🐾"
            PetMood.EXCITED -> "Buddy loved watching you catch those coins! 🪙🎉"
            PetMood.COMFORTING -> "Buddy is nuzzling your shoulder: you can do this! 🐾💪"
            PetMood.HAPPY -> "Buddy is super happy and wagging his tail! 🥰"
            PetMood.STUDIOUS -> "Buddy has his thinking cap on with you! 📚🐶"
        }
}
