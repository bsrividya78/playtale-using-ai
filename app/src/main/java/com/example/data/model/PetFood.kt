package com.example.data.model

data class PetFood(
    val id: String,
    val name: String,
    val emoji: String,
    val costCoins: Int,
    val hungerReduction: Int, // Percent hunger reduced
    val happinessBoost: Int,   // Percent happiness increased
    val description: String
) {
    companion object {
        val ALL_FOODS = listOf(
            PetFood(
                id = "dog_food",
                name = "Dog Food",
                emoji = "🍖",
                costCoins = 10,
                hungerReduction = 25,
                happinessBoost = 15,
                description = "Nutritious crunchy kibble Buddy loves!"
            ),
            PetFood(
                id = "healthy_snack",
                name = "Healthy Snack",
                emoji = "🥕",
                costCoins = 15,
                hungerReduction = 35,
                happinessBoost = 25,
                description = "Crispy vitamin-packed vegetable treat!"
            ),
            PetFood(
                id = "special_treat",
                name = "Special Treat",
                emoji = "🍎",
                costCoins = 25,
                hungerReduction = 50,
                happinessBoost = 40,
                description = "Sweet juicy apple reward for top students!"
            )
        )
    }
}
