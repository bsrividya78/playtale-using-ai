package com.example.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.data.model.LearningZone
import com.example.data.model.PetFood
import com.example.data.model.PetState
import com.example.data.model.Question
import com.example.data.model.UserProfile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONObject

class LearnPlayRepository(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("learn_play_prefs", Context.MODE_PRIVATE)

    private val _userProfile = MutableStateFlow(loadUserProfile())
    val userProfile: StateFlow<UserProfile> = _userProfile.asStateFlow()

    private val _petState = MutableStateFlow(loadPetState())
    val petState: StateFlow<PetState> = _petState.asStateFlow()

    init {
        // Ticking hunger / happiness decay slightly on fresh launches to encourage feeding
        checkPetDecay()
    }

    private fun loadUserProfile(): UserProfile {
        val studentName = prefs.getString("student_name", "Learner") ?: "Learner"
        val coins = prefs.getInt("coins", 50)
        val xp = prefs.getInt("xp", 35)
        val quizAttempts = prefs.getInt("quiz_attempts", 3)
        val quizzesCompleted = prefs.getInt("quizzes_completed", 2)
        val miniGameUnlocked = prefs.getBoolean("mini_game_unlocked", true)
        val gamesPlayed = prefs.getInt("games_played", 1)
        val highestCoinScore = prefs.getInt("highest_coin_score", 24)
        val isCollegeMode = prefs.getBoolean("is_college_mode", false)

        val completedJson = prefs.getString("zone_completed_json", null)
        val completedMap = mutableMapOf<String, Int>()
        if (completedJson != null) {
            try {
                val json = JSONObject(completedJson)
                json.keys().forEach { key ->
                    completedMap[key] = json.getInt(key)
                }
            } catch (_: Exception) {
                // fallback
            }
        } else {
            completedMap[LearningZone.CODE.id] = 6
            completedMap[LearningZone.MATH.id] = 4
            completedMap[LearningZone.SCIENCE.id] = 2
            completedMap[LearningZone.WORLD.id] = 8
        }

        val correctJson = prefs.getString("zone_correct_json", null)
        val correctMap = mutableMapOf<String, Int>()
        if (correctJson != null) {
            try {
                val json = JSONObject(correctJson)
                json.keys().forEach { key ->
                    correctMap[key] = json.getInt(key)
                }
            } catch (_: Exception) {
                // fallback
            }
        } else {
            correctMap[LearningZone.CODE.id] = 5
            correctMap[LearningZone.MATH.id] = 3
            correctMap[LearningZone.SCIENCE.id] = 2
            correctMap[LearningZone.WORLD.id] = 7
        }

        return UserProfile(
            studentName = studentName,
            coins = coins,
            xp = xp,
            zoneCompletedQuestions = completedMap,
            zoneCorrectAnswers = correctMap,
            quizAttempts = quizAttempts,
            quizzesCompleted = quizzesCompleted,
            miniGameUnlocked = miniGameUnlocked,
            gamesPlayed = gamesPlayed,
            highestCoinScore = highestCoinScore,
            isCollegeMode = isCollegeMode
        )
    }

    private fun saveUserProfile(profile: UserProfile) {
        val completedJson = JSONObject(profile.zoneCompletedQuestions).toString()
        val correctJson = JSONObject(profile.zoneCorrectAnswers).toString()

        prefs.edit()
            .putString("student_name", profile.studentName)
            .putInt("coins", profile.coins)
            .putInt("xp", profile.xp)
            .putInt("quiz_attempts", profile.quizAttempts)
            .putInt("quizzes_completed", profile.quizzesCompleted)
            .putBoolean("mini_game_unlocked", profile.miniGameUnlocked)
            .putInt("games_played", profile.gamesPlayed)
            .putInt("highest_coin_score", profile.highestCoinScore)
            .putBoolean("is_college_mode", profile.isCollegeMode)
            .putString("zone_completed_json", completedJson)
            .putString("zone_correct_json", correctJson)
            .apply()

        _userProfile.value = profile
    }

    private fun loadPetState(): PetState {
        val name = prefs.getString("pet_name", "Buddy") ?: "Buddy"
        val happiness = prefs.getInt("pet_happiness", 80)
        val hunger = prefs.getInt("pet_hunger", 40)
        val moodMessage = prefs.getString("pet_mood_message", "You're doing great! 🐶") ?: "You're doing great! 🐶"
        val timesFed = prefs.getInt("pet_times_fed", 1)
        val lastFedTimestamp = prefs.getLong("pet_last_fed", System.currentTimeMillis())
        val lastInteraction = prefs.getString("pet_last_interaction", "IDLE") ?: "IDLE"
        val consecutiveCorrect = prefs.getInt("pet_consecutive_correct", 0)

        return PetState(
            name = name,
            happiness = happiness,
            hunger = hunger,
            moodMessage = moodMessage,
            timesFed = timesFed,
            lastFedTimestamp = lastFedTimestamp,
            lastInteraction = lastInteraction,
            consecutiveCorrect = consecutiveCorrect
        )
    }

    private fun savePetState(pet: PetState) {
        prefs.edit()
            .putString("pet_name", pet.name)
            .putInt("pet_happiness", pet.happiness)
            .putInt("pet_hunger", pet.hunger)
            .putString("pet_mood_message", pet.moodMessage)
            .putInt("pet_times_fed", pet.timesFed)
            .putLong("pet_last_fed", pet.lastFedTimestamp)
            .putString("pet_last_interaction", pet.lastInteraction)
            .putInt("pet_consecutive_correct", pet.consecutiveCorrect)
            .apply()

        _petState.value = pet
    }

    private fun checkPetDecay() {
        val current = _petState.value
        val now = System.currentTimeMillis()
        val hoursPassed = ((now - current.lastFedTimestamp) / (1000 * 60 * 60)).coerceAtLeast(0)
        if (hoursPassed > 1) {
            val hungerIncrease = (hoursPassed * 5).toInt().coerceAtMost(30)
            val happinessDecay = (hoursPassed * 3).toInt().coerceAtMost(25)
            val newHunger = (current.hunger + hungerIncrease).coerceIn(0, 100)
            val newHappiness = (current.happiness - happinessDecay).coerceIn(10, 100)
            val newMsg = if (newHunger >= 70) "I'm hungry! 🍖" else "Need a hint? 💡"
            savePetState(
                current.copy(
                    hunger = newHunger,
                    happiness = newHappiness,
                    moodMessage = newMsg
                )
            )
        }
    }

    fun addCoins(amount: Int) {
        val current = _userProfile.value
        val updated = current.copy(coins = (current.coins + amount).coerceAtLeast(0))
        saveUserProfile(updated)
    }

    fun addXp(amount: Int) {
        val current = _userProfile.value
        val updated = current.copy(xp = current.xp + amount)
        saveUserProfile(updated)
    }

    fun feedPet(food: PetFood): Boolean {
        val profile = _userProfile.value
        if (profile.coins < food.costCoins) return false

        // Deduct coins
        addCoins(-food.costCoins)

        val currentPet = _petState.value
        val newHunger = (currentPet.hunger - food.hungerReduction).coerceIn(0, 100)
        val newHappiness = (currentPet.happiness + food.happinessBoost).coerceIn(0, 100)
        val newMsg = when {
            newHappiness >= 95 -> "Yummy! Best treat ever! 🐶❤️"
            newHunger <= 20 -> "I'm full and super happy! Woof! 🎉"
            else -> "Mmm delicious! Thank you! 🦴"
        }

        savePetState(
            currentPet.copy(
                hunger = newHunger,
                happiness = newHappiness,
                moodMessage = newMsg,
                timesFed = currentPet.timesFed + 1,
                lastFedTimestamp = System.currentTimeMillis(),
                lastInteraction = "FED"
            )
        )
        return true
    }

    fun recordQuestionAnswer(isCorrect: Boolean) {
        val currentPet = _petState.value
        val newConsecutive = if (isCorrect) currentPet.consecutiveCorrect + 1 else 0
        // Studying burns a tiny bit of pet energy
        val newHunger = (currentPet.hunger + 2).coerceIn(0, 100)
        val newHappiness = if (isCorrect) (currentPet.happiness + 2).coerceIn(0, 100) else currentPet.happiness

        val newMsg = when {
            newHunger >= 75 -> "Woof! Studying burns energy, I'm getting hungry! 🍖"
            newConsecutive >= 3 -> "🔥 $newConsecutive in a row! You're brilliant! 🐶"
            isCorrect -> "Spot on! That's correct! ⭐🐾"
            else -> "No worries! Read the hint and keep shining! 💡🐶"
        }

        savePetState(
            currentPet.copy(
                hunger = newHunger,
                happiness = newHappiness,
                moodMessage = newMsg,
                lastInteraction = if (isCorrect) "JUST_CORRECT" else "JUST_INCORRECT",
                consecutiveCorrect = newConsecutive
            )
        )
    }

    fun setPetMood(message: String) {
        val current = _petState.value
        savePetState(current.copy(moodMessage = message))
    }

    fun recordQuizResult(zone: LearningZone, score: Int, total: Int) {
        val profile = _userProfile.value
        val currentCompleted = profile.zoneCompletedQuestions[zone.id] ?: 0
        val currentCorrect = profile.zoneCorrectAnswers[zone.id] ?: 0

        val newCompleted = currentCompleted + total
        val newCorrect = currentCorrect + score

        val updatedCompletedMap = profile.zoneCompletedQuestions.toMutableMap()
        updatedCompletedMap[zone.id] = newCompleted

        val updatedCorrectMap = profile.zoneCorrectAnswers.toMutableMap()
        updatedCorrectMap[zone.id] = newCorrect

        // Reward rules: +5 XP per correct answer, +10 XP for completing quiz
        val xpEarned = (score * 5) + 10
        val unlockGame = score >= 3

        val updated = profile.copy(
            xp = profile.xp + xpEarned,
            zoneCompletedQuestions = updatedCompletedMap,
            zoneCorrectAnswers = updatedCorrectMap,
            quizAttempts = profile.quizAttempts + 1,
            quizzesCompleted = profile.quizzesCompleted + 1,
            miniGameUnlocked = profile.miniGameUnlocked || unlockGame
        )
        saveUserProfile(updated)

        // Pet reaction based on quiz history
        val currentPet = _petState.value
        val newHunger = (currentPet.hunger + 6).coerceIn(0, 100)
        if (score >= 4) {
            val newHappy = (currentPet.happiness + 15).coerceIn(0, 100)
            savePetState(
                currentPet.copy(
                    hunger = newHunger,
                    happiness = newHappy,
                    moodMessage = "Woohoo! $score/$total! You unlocked the game! 🎉🐶",
                    lastInteraction = "QUIZ_HIGH_SCORE"
                )
            )
        } else if (score >= 3) {
            val newHappy = (currentPet.happiness + 10).coerceIn(0, 100)
            savePetState(
                currentPet.copy(
                    hunger = newHunger,
                    happiness = newHappy,
                    moodMessage = "Great job! Mini-game unlocked! Let's play! 🎮🐾",
                    lastInteraction = "QUIZ_HIGH_SCORE"
                )
            )
        } else {
            savePetState(
                currentPet.copy(
                    hunger = newHunger,
                    moodMessage = "Good effort! Let's review mistakes together! 🐾💪",
                    lastInteraction = "QUIZ_LOW_SCORE"
                )
            )
        }
    }

    fun recordMiniGameComplete(coinsCaught: Int) {
        val profile = _userProfile.value
        val coinsEarned = 20 + (coinsCaught / 2) // +20 base coins plus bonus for catcher skill
        val newHighScore = maxOf(profile.highestCoinScore, coinsCaught)

        saveUserProfile(
            profile.copy(
                coins = profile.coins + coinsEarned,
                gamesPlayed = profile.gamesPlayed + 1,
                highestCoinScore = newHighScore
            )
        )

        // Boost pet happiness on game celebration!
        val currentPet = _petState.value
        val newHappy = (currentPet.happiness + 20).coerceIn(0, 100)
        val newHunger = (currentPet.hunger + 8).coerceIn(0, 100)
        savePetState(
            currentPet.copy(
                happiness = newHappy,
                hunger = newHunger,
                moodMessage = "Woohoo! Caught $coinsCaught coins! Let's get a snack! 🪙🐶",
                lastInteraction = "PLAYED_GAME"
            )
        )
    }

    fun setStudentName(name: String) {
        val profile = _userProfile.value
        saveUserProfile(profile.copy(studentName = name))
    }

    fun setCollegeMode(enabled: Boolean) {
        val profile = _userProfile.value
        saveUserProfile(profile.copy(isCollegeMode = enabled))
    }

    fun resetProgress() {
        val defaultProfile = UserProfile(
            studentName = _userProfile.value.studentName,
            coins = 30,
            xp = 0,
            zoneCompletedQuestions = mapOf(
                LearningZone.CODE.id to 0,
                LearningZone.MATH.id to 0,
                LearningZone.SCIENCE.id to 0,
                LearningZone.WORLD.id to 0
            ),
            zoneCorrectAnswers = emptyMap(),
            quizAttempts = 0,
            quizzesCompleted = 0,
            miniGameUnlocked = false,
            gamesPlayed = 0,
            highestCoinScore = 0
        )
        saveUserProfile(defaultProfile)
        savePetState(
            PetState(
                name = "Buddy",
                happiness = 80,
                hunger = 40,
                moodMessage = "Ready for our adventure! 🐶"
            )
        )
    }

    fun getQuestionsForZone(zone: LearningZone, collegeMode: Boolean = false): List<Question> {
        val allQuestions = when (zone) {
            LearningZone.CODE -> getCodeQuestions(collegeMode)
            LearningZone.MATH -> getMathQuestions(collegeMode)
            LearningZone.SCIENCE -> getScienceQuestions(collegeMode)
            LearningZone.WORLD -> getWorldQuestions(collegeMode)
        }
        return allQuestions.shuffled().take(5)
    }

    private fun getCodeQuestions(college: Boolean): List<Question> = listOf(
        Question(
            id = "c1",
            zone = LearningZone.CODE,
            questionText = "What is a variable used for in programming?",
            options = listOf(
                "A. To store data",
                "B. To draw pictures",
                "C. To play music",
                "D. To turn off the computer"
            ),
            correctOptionIndex = 0,
            explanation = "Variables are named storage containers in memory used to hold values like numbers, words, or objects.",
            hint = "Think about a labeled box you put items into for later use!"
        ),
        Question(
            id = "c2",
            zone = LearningZone.CODE,
            questionText = "Which loop repeats code until a condition becomes false?",
            options = listOf(
                "A. while loop",
                "B. jump loop",
                "C. static loop",
                "D. sleep loop"
            ),
            correctOptionIndex = 0,
            explanation = "A 'while' loop continues to execute its block of code as long as its boolean condition remains true.",
            hint = "It sounds like: 'as long as this is true, keep doing it'."
        ),
        Question(
            id = "c3",
            zone = LearningZone.CODE,
            questionText = "What does the 'if' statement do in code?",
            options = listOf(
                "A. Makes decisions based on conditions",
                "B. Deletes the entire application",
                "C. Prints paper documents",
                "D. Connects to Wi-Fi"
            ),
            correctOptionIndex = 0,
            explanation = "The 'if' statement evaluates a conditional test to decide which branch of instructions to execute.",
            hint = "If it rains, take an umbrella!"
        ),
        Question(
            id = "c4",
            zone = LearningZone.CODE,
            questionText = "What is a function in computer programming?",
            options = listOf(
                "A. A reusable block of code that performs a specific task",
                "B. A computer's battery charger",
                "C. A video game controller",
                "D. A physical keyboard key"
            ),
            correctOptionIndex = 0,
            explanation = "Functions package instructions into reusable chunks that can accept parameters and return outputs.",
            hint = "Like a recipe you can reuse whenever you want to bake a cake."
        ),
        Question(
            id = "c5",
            zone = LearningZone.CODE,
            questionText = "What is a 'bug' in computer software?",
            options = listOf(
                "A. An insect living inside the monitor",
                "B. An error, flaw, or fault in the program code",
                "C. A type of fast processor",
                "D. A security password"
            ),
            correctOptionIndex = 1,
            explanation = "A bug is an unexpected defect or glitch that causes a computer program to produce incorrect results.",
            hint = "Grace Hopper famously documented an actual moth found in a relay, giving birth to the term!"
        ),
        Question(
            id = "c6",
            zone = LearningZone.CODE,
            questionText = "What data type would you use to store whether a user is logged in (Yes/No)?",
            options = listOf(
                "A. Boolean",
                "B. Float",
                "C. Character",
                "D. Array"
            ),
            correctOptionIndex = 0,
            explanation = "Boolean represents binary logical values that can only be either true or false.",
            hint = "Named after mathematician George Boole, it has only two states!"
        ),
        Question(
            id = "c7",
            zone = LearningZone.CODE,
            questionText = "What is the index of the first element in most programming language arrays?",
            options = listOf(
                "A. 0",
                "B. 1",
                "C. -1",
                "D. 10"
            ),
            correctOptionIndex = 0,
            explanation = "Zero-based indexing is standard in languages like Python, Java, Kotlin, and C++.",
            hint = "Start counting from zero!"
        )
    )

    private fun getMathQuestions(college: Boolean): List<Question> = listOf(
        Question(
            id = "m1",
            zone = LearningZone.MATH,
            questionText = "What is the value of 8 × 7?",
            options = listOf(
                "A. 54",
                "B. 56",
                "C. 62",
                "D. 48"
            ),
            correctOptionIndex = 1,
            explanation = "8 multiplied by 7 equals 56.",
            hint = "Think 8 x 5 = 40, plus 8 x 2 = 16, so 40 + 16 = ?"
        ),
        Question(
            id = "m2",
            zone = LearningZone.MATH,
            questionText = "What is 25% of 200?",
            options = listOf(
                "A. 25",
                "B. 50",
                "C. 75",
                "D. 100"
            ),
            correctOptionIndex = 1,
            explanation = "25% is one-quarter (1/4). 200 divided by 4 equals 50.",
            hint = "25% is one quarter of the total amount."
        ),
        Question(
            id = "m3",
            zone = LearningZone.MATH,
            questionText = "How many degrees are in the angles of a triangle combined?",
            options = listOf(
                "A. 90°",
                "B. 180°",
                "C. 270°",
                "D. 360°"
            ),
            correctOptionIndex = 1,
            explanation = "The interior angles of any planar Euclidean triangle always sum to 180 degrees.",
            hint = "Half of a full 360-degree circle!"
        ),
        Question(
            id = "m4",
            zone = LearningZone.MATH,
            questionText = "If 3x + 6 = 21, what is the value of x?",
            options = listOf(
                "A. 3",
                "B. 5",
                "C. 7",
                "D. 9"
            ),
            correctOptionIndex = 1,
            explanation = "Subtract 6 from both sides: 3x = 15. Then divide by 3: x = 5.",
            hint = "Subtract 6 first, then divide by 3."
        ),
        Question(
            id = "m5",
            zone = LearningZone.MATH,
            questionText = "Which number is a prime number?",
            options = listOf(
                "A. 9",
                "B. 15",
                "C. 17",
                "D. 21"
            ),
            correctOptionIndex = 2,
            explanation = "17 has only two positive divisors: 1 and itself, making it prime.",
            hint = "It cannot be divided evenly by 2, 3, or 5!"
        ),
        Question(
            id = "m6",
            zone = LearningZone.MATH,
            questionText = "What is the perimeter of a rectangle with length 8 and width 5?",
            options = listOf(
                "A. 26",
                "B. 40",
                "C. 13",
                "D. 30"
            ),
            correctOptionIndex = 0,
            explanation = "Perimeter = 2 * (length + width) = 2 * (8 + 5) = 2 * 13 = 26.",
            hint = "Add length and width together, then multiply by 2."
        )
    )

    private fun getScienceQuestions(college: Boolean): List<Question> = listOf(
        Question(
            id = "s1",
            zone = LearningZone.SCIENCE,
            questionText = "What process do green plants use to convert sunlight into food?",
            options = listOf(
                "A. Photosynthesis",
                "B. Respiration",
                "C. Fermentation",
                "D. Evaporation"
            ),
            correctOptionIndex = 0,
            explanation = "Photosynthesis uses chlorophyll to turn sunlight, carbon dioxide, and water into glucose and oxygen.",
            hint = "'Photo' means light, and 'synthesis' means putting together!"
        ),
        Question(
            id = "s2",
            zone = LearningZone.SCIENCE,
            questionText = "What is the chemical symbol for Water?",
            options = listOf(
                "A. O2",
                "B. CO2",
                "C. H2O",
                "D. NaCl"
            ),
            correctOptionIndex = 2,
            explanation = "Water is composed of two hydrogen atoms bonded to one oxygen atom (H2O).",
            hint = "Two hydrogen and one oxygen!"
        ),
        Question(
            id = "s3",
            zone = LearningZone.SCIENCE,
            questionText = "Which planet is known as the 'Red Planet'?",
            options = listOf(
                "A. Venus",
                "B. Mars",
                "C. Jupiter",
                "D. Saturn"
            ),
            correctOptionIndex = 1,
            explanation = "Mars appears reddish due to abundant iron oxide (rust) on its dusty surface.",
            hint = "It's the fourth planet from the Sun, named after the Roman god of war."
        ),
        Question(
            id = "s4",
            zone = LearningZone.SCIENCE,
            questionText = "What is the state of matter of steam?",
            options = listOf(
                "A. Solid",
                "B. Liquid",
                "C. Gas",
                "D. Plasma"
            ),
            correctOptionIndex = 2,
            explanation = "Steam is water vapor, which is the gaseous phase of water.",
            hint = "When liquid water boils at 100°C, it becomes this state."
        ),
        Question(
            id = "s5",
            zone = LearningZone.SCIENCE,
            questionText = "What force keeps the planets orbiting around the Sun?",
            options = listOf(
                "A. Magnetism",
                "B. Gravity",
                "C. Friction",
                "D. Nuclear force"
            ),
            correctOptionIndex = 1,
            explanation = "Gravity is the attractive gravitational force exerted by the massive Sun that curves planets into orbits.",
            hint = "Isaac Newton famously studied this with an apple!"
        ),
        Question(
            id = "s6",
            zone = LearningZone.SCIENCE,
            questionText = "Which organ pumps blood throughout the human body?",
            options = listOf(
                "A. Lungs",
                "B. Brain",
                "C. Heart",
                "D. Stomach"
            ),
            correctOptionIndex = 2,
            explanation = "The heart is a muscular organ that pumps oxygenated blood through the circulatory system.",
            hint = "It beats around 60 to 100 times a minute."
        )
    )

    private fun getWorldQuestions(college: Boolean): List<Question> = listOf(
        Question(
            id = "w1",
            zone = LearningZone.WORLD,
            questionText = "How many continents are there on planet Earth?",
            options = listOf(
                "A. 5",
                "B. 6",
                "C. 7",
                "D. 8"
            ),
            correctOptionIndex = 2,
            explanation = "Earth has 7 continents: Africa, Antarctica, Asia, Europe, North America, Oceania/Australia, and South America.",
            hint = "Count: Africa, Antarctica, Asia, Europe, North America, South America, Australia!"
        ),
        Question(
            id = "w2",
            zone = LearningZone.WORLD,
            questionText = "Which is the largest ocean on Earth?",
            options = listOf(
                "A. Atlantic Ocean",
                "B. Indian Ocean",
                "C. Pacific Ocean",
                "D. Arctic Ocean"
            ),
            correctOptionIndex = 2,
            explanation = "The Pacific Ocean is the largest and deepest ocean basin, covering over 30% of Earth's surface.",
            hint = "Its name means 'peaceful'!"
        ),
        Question(
            id = "w3",
            zone = LearningZone.WORLD,
            questionText = "Which gas do trees absorb to help fight climate change?",
            options = listOf(
                "A. Oxygen",
                "B. Carbon Dioxide",
                "C. Nitrogen",
                "D. Helium"
            ),
            correctOptionIndex = 1,
            explanation = "Trees absorb carbon dioxide (CO2) from the atmosphere and release life-giving oxygen.",
            hint = "Humans exhale it, plants absorb it!"
        ),
        Question(
            id = "w4",
            zone = LearningZone.WORLD,
            questionText = "What is the longest river in the world?",
            options = listOf(
                "A. Amazon River",
                "B. Nile River",
                "C. Mississippi River",
                "D. Yangtze River"
            ),
            correctOptionIndex = 1,
            explanation = "The Nile River in northeastern Africa is traditionally recognized as the longest river at approx 6,650 km.",
            hint = "It flows through Egypt and northeastern Africa."
        ),
        Question(
            id = "w5",
            zone = LearningZone.WORLD,
            questionText = "Which renewable energy comes from moving air currents?",
            options = listOf(
                "A. Solar energy",
                "B. Wind energy",
                "C. Geothermal energy",
                "D. Coal energy"
            ),
            correctOptionIndex = 1,
            explanation = "Wind energy uses large wind turbines to convert the kinetic energy of air into electricity.",
            hint = "Think of large white windmill turbines turning in breezes!"
        ),
        Question(
            id = "w6",
            zone = LearningZone.WORLD,
            questionText = "Which animal is the largest mammal on Earth?",
            options = listOf(
                "A. African Elephant",
                "B. Blue Whale",
                "C. Giraffe",
                "D. Polar Bear"
            ),
            correctOptionIndex = 1,
            explanation = "The Blue Whale can reach lengths of up to 30 meters and weigh up to 200 metric tons.",
            hint = "It lives in oceans and is bigger than any known dinosaur!"
        )
    )
}
