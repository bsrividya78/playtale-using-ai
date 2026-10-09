package com.example

import com.example.data.model.LearningZone
import com.example.data.model.PetFood
import com.example.data.model.PetState
import com.example.data.model.UserProfile
import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {
  @Test
  fun addition_isCorrect() {
    assertEquals(4, 2 + 2)
  }

  @Test
  fun testUserProfile_levelCalculation() {
    val profile = UserProfile(xp = 110)
    assertEquals(3, profile.level) // 110 / 50 = 2 -> Level 3
  }

  @Test
  fun testPetFood_values() {
    val dogFood = PetFood.ALL_FOODS.find { it.id == "dog_food" }
    assertNotNull(dogFood)
    assertEquals(10, dogFood?.costCoins)
    assertEquals(25, dogFood?.hungerReduction)
  }

  @Test
  fun testLearningZones_exist() {
    assertEquals(4, LearningZone.entries.size)
    assertNotNull(LearningZone.fromId("code"))
    assertNotNull(LearningZone.fromId("math"))
    assertNotNull(LearningZone.fromId("science"))
    assertNotNull(LearningZone.fromId("world"))
  }

  @Test
  fun testPetMood_basedOnInteractionsAndHunger() {
    // Starving overrides other states
    val starvingPet = PetState(hunger = 85, happiness = 90)
    assertEquals(com.example.data.model.PetMood.STARVING, starvingPet.currentMood)

    // Hungry state
    val hungryPet = PetState(hunger = 65, happiness = 80)
    assertEquals(com.example.data.model.PetMood.HUNGRY, hungryPet.currentMood)

    // Streak / Ecstatic state
    val streakPet = PetState(hunger = 30, consecutiveCorrect = 3)
    assertEquals(com.example.data.model.PetMood.ECSTATIC, streakPet.currentMood)

    // Comforting on mistake
    val comfortPet = PetState(hunger = 30, lastInteraction = "JUST_INCORRECT")
    assertEquals(com.example.data.model.PetMood.COMFORTING, comfortPet.currentMood)

    // Excited on game completion
    val gamePet = PetState(hunger = 30, lastInteraction = "PLAYED_GAME")
    assertEquals(com.example.data.model.PetMood.EXCITED, gamePet.currentMood)
  }
}
