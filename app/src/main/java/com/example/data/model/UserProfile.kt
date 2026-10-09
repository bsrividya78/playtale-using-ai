package com.example.data.model

data class UserProfile(
    val studentName: String = "Learner",
    val coins: Int = 30,
    val xp: Int = 0,
    val zoneCompletedQuestions: Map<String, Int> = mapOf(
        LearningZone.CODE.id to 3,
        LearningZone.MATH.id to 2,
        LearningZone.SCIENCE.id to 1,
        LearningZone.WORLD.id to 4
    ),
    val zoneCorrectAnswers: Map<String, Int> = mapOf(
        LearningZone.CODE.id to 3,
        LearningZone.MATH.id to 2,
        LearningZone.SCIENCE.id to 1,
        LearningZone.WORLD.id to 4
    ),
    val quizAttempts: Int = 4,
    val quizzesCompleted: Int = 2,
    val miniGameUnlocked: Boolean = false,
    val gamesPlayed: Int = 0,
    val highestCoinScore: Int = 0,
    val isCollegeMode: Boolean = false
) {
    val level: Int
        get() = (xp / 50) + 1

    val currentLevelProgress: Float
        get() = ((xp % 50).toFloat() / 50f).coerceIn(0f, 1f)

    fun getZoneProgressPercent(zoneId: String, totalQuestions: Int = 10): Float {
        val completed = zoneCompletedQuestions[zoneId] ?: 0
        return (completed.toFloat() / totalQuestions.toFloat()).coerceIn(0f, 1f)
    }

    fun getRecommendedZone(): LearningZone {
        // Recommend the zone with lowest progress or lowest accuracy
        var minProgress = Float.MAX_VALUE
        var recommended = LearningZone.CODE
        for (zone in LearningZone.entries) {
            val progress = getZoneProgressPercent(zone.id)
            if (progress < minProgress) {
                minProgress = progress
                recommended = zone
            }
        }
        return recommended
    }
}
