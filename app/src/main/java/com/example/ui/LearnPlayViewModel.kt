package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.audio.TtsManager
import com.example.data.ai.GeminiTutorService
import com.example.data.model.LearningZone
import com.example.data.model.PetFood
import com.example.data.model.PetState
import com.example.data.model.Question
import com.example.data.model.UserProfile
import com.example.data.repository.LearnPlayRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class NavTab(val title: String, val iconLabel: String) {
    HOME("Home", "🏠"),
    LEARN("Learn", "📚"),
    GAMES("Games", "🎮"),
    PET("Pet", "🐶"),
    PROFILE("Profile", "👤")
}

data class MistakeReview(
    val question: Question,
    val chosenOptionIndex: Int
)

data class QuizState(
    val zone: LearningZone,
    val questions: List<Question> = emptyList(),
    val currentIndex: Int = 0,
    val selectedOptionIndex: Int? = null,
    val isSubmitted: Boolean = false,
    val isCorrect: Boolean = false,
    val score: Int = 0,
    val mistakes: List<MistakeReview> = emptyList(),
    val isFinished: Boolean = false
) {
    val currentQuestion: Question?
        get() = questions.getOrNull(currentIndex)
}

data class AiMessage(
    val sender: String, // "Learner" or "TaleBot"
    val text: String,
    val isBot: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)

class LearnPlayViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = LearnPlayRepository(application)
    private val tutorService = GeminiTutorService()
    val ttsManager = TtsManager(application)

    val userProfile: StateFlow<UserProfile> = repository.userProfile
    val petState: StateFlow<PetState> = repository.petState

    private val _currentTab = MutableStateFlow(NavTab.HOME)
    val currentTab: StateFlow<NavTab> = _currentTab.asStateFlow()

    private val _quizState = MutableStateFlow<QuizState?>(null)
    val quizState: StateFlow<QuizState?> = _quizState.asStateFlow()

    private val _isMiniGameActive = MutableStateFlow(false)
    val isMiniGameActive: StateFlow<Boolean> = _isMiniGameActive.asStateFlow()

    private val _isAiSheetOpen = MutableStateFlow(false)
    val isAiSheetOpen: StateFlow<Boolean> = _isAiSheetOpen.asStateFlow()

    private val _aiMessages = MutableStateFlow<List<AiMessage>>(
        listOf(
            AiMessage(
                sender = "TaleBot",
                text = "Hello Learner! 🤖 I'm TaleBot, your personal AI study buddy. Ask me for hints, simple explanations, or which zone to tackle next!",
                isBot = true
            )
        )
    )
    val aiMessages: StateFlow<List<AiMessage>> = _aiMessages.asStateFlow()

    private val _aiIsLoading = MutableStateFlow(false)
    val aiIsLoading: StateFlow<Boolean> = _aiIsLoading.asStateFlow()

    private val _showFeedingCelebration = MutableStateFlow(false)
    val showFeedingCelebration: StateFlow<Boolean> = _showFeedingCelebration.asStateFlow()

    private val _isFloatingPetExpanded = MutableStateFlow(false)
    val isFloatingPetExpanded: StateFlow<Boolean> = _isFloatingPetExpanded.asStateFlow()

    private val _isFloatingPetVisible = MutableStateFlow(true)
    val isFloatingPetVisible: StateFlow<Boolean> = _isFloatingPetVisible.asStateFlow()

    override fun onCleared() {
        super.onCleared()
        ttsManager.shutdown()
    }

    fun toggleFloatingPetExpanded() {
        _isFloatingPetExpanded.value = !_isFloatingPetExpanded.value
    }

    fun dismissFloatingPetExpanded() {
        _isFloatingPetExpanded.value = false
    }

    fun setFloatingPetVisible(visible: Boolean) {
        _isFloatingPetVisible.value = visible
    }

    fun setTab(tab: NavTab) {
        _currentTab.value = tab
    }

    fun startQuiz(zone: LearningZone) {
        val questions = repository.getQuestionsForZone(
            zone = zone,
            collegeMode = userProfile.value.isCollegeMode
        )
        _quizState.value = QuizState(
            zone = zone,
            questions = questions,
            currentIndex = 0,
            selectedOptionIndex = null,
            isSubmitted = false,
            score = 0,
            mistakes = emptyList(),
            isFinished = false
        )
        _currentTab.value = NavTab.LEARN
    }

    fun selectQuizOption(index: Int) {
        val current = _quizState.value ?: return
        if (current.isSubmitted) return
        _quizState.value = current.copy(selectedOptionIndex = index)
    }

    fun submitCurrentAnswer() {
        val current = _quizState.value ?: return
        val question = current.currentQuestion ?: return
        val selected = current.selectedOptionIndex ?: return
        if (current.isSubmitted) return

        val isCorrect = selected == question.correctOptionIndex
        val newScore = if (isCorrect) current.score + 1 else current.score
        val newMistakes = if (!isCorrect) {
            current.mistakes + MistakeReview(question, selected)
        } else {
            current.mistakes
        }

        if (isCorrect) {
            // Reward +5 XP immediately
            repository.addXp(5)
        }
        repository.recordQuestionAnswer(isCorrect)

        _quizState.value = current.copy(
            isSubmitted = true,
            isCorrect = isCorrect,
            score = newScore,
            mistakes = newMistakes
        )
    }

    fun nextQuestionOrFinish() {
        val current = _quizState.value ?: return
        ttsManager.stop()

        if (current.currentIndex + 1 < current.questions.size) {
            _quizState.value = current.copy(
                currentIndex = current.currentIndex + 1,
                selectedOptionIndex = null,
                isSubmitted = false,
                isCorrect = false
            )
        } else {
            // Quiz finished!
            val finalScore = current.score
            val total = current.questions.size
            repository.recordQuizResult(current.zone, finalScore, total)
            _quizState.value = current.copy(isFinished = true)
        }
    }

    fun retryQuiz() {
        val current = _quizState.value ?: return
        startQuiz(current.zone)
    }

    fun exitQuiz() {
        ttsManager.stop()
        _quizState.value = null
        _currentTab.value = NavTab.HOME
    }

    fun openMiniGame() {
        ttsManager.stop()
        _isMiniGameActive.value = true
        _currentTab.value = NavTab.GAMES
    }

    fun finishMiniGame(coinsCaught: Int) {
        repository.recordMiniGameComplete(coinsCaught)
        _isMiniGameActive.value = false
    }

    fun feedPet(food: PetFood): Boolean {
        val success = repository.feedPet(food)
        if (success) {
            viewModelScope.launch {
                _showFeedingCelebration.value = true
                delay(2200)
                _showFeedingCelebration.value = false
            }
        }
        return success
    }

    fun openAiAssistant(defaultQuery: String? = null) {
        _isAiSheetOpen.value = true
        if (!defaultQuery.isNullOrBlank()) {
            askAi(defaultQuery)
        }
    }

    fun closeAiAssistant() {
        _isAiSheetOpen.value = false
    }

    fun askAi(query: String) {
        if (query.isBlank()) return
        val currentQuiz = _quizState.value
        val activeQ = currentQuiz?.currentQuestion
        val activeZone = currentQuiz?.zone

        val userMsg = AiMessage(sender = "Learner", text = query, isBot = false)
        _aiMessages.value = _aiMessages.value + userMsg
        _aiIsLoading.value = true

        viewModelScope.launch {
            val responseText = tutorService.askTutor(
                userMessage = query,
                currentQuestion = activeQ,
                zone = activeZone,
                selectedWrongOption = if (currentQuiz?.isSubmitted == true && !currentQuiz.isCorrect) {
                    currentQuiz.selectedOptionIndex?.let { idx -> activeQ?.options?.getOrNull(idx) }
                } else null
            )
            _aiIsLoading.value = false
            _aiMessages.value = _aiMessages.value + AiMessage(
                sender = "TaleBot",
                text = responseText,
                isBot = true
            )
        }
    }

    fun speakCurrentQuestion() {
        val question = _quizState.value?.currentQuestion ?: return
        val textToSpeak = buildString {
            append(question.questionText)
            append(". Options: ")
            question.options.forEach { opt ->
                append("$opt. ")
            }
        }
        ttsManager.speak(textToSpeak)
    }

    fun setStudentName(name: String) {
        repository.setStudentName(name)
    }

    fun setCollegeMode(enabled: Boolean) {
        repository.setCollegeMode(enabled)
    }

    fun resetData() {
        repository.resetProgress()
        _quizState.value = null
        _isMiniGameActive.value = false
    }
}
