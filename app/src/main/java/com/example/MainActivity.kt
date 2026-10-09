package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.LearnPlayViewModel
import com.example.ui.NavTab
import com.example.ui.components.AIAssistantSheet
import com.example.ui.components.BottomNavBar
import com.example.ui.components.FloatingBuddyOverlay
import com.example.ui.components.HeaderBar
import com.example.ui.screens.GamesHubScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.LearnScreen
import com.example.ui.screens.MiniGameScreen
import com.example.ui.screens.PetScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.screens.QuizResultScreen
import com.example.ui.screens.QuizScreen
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                LearnPlayApp()
            }
        }
    }
}

@Composable
fun LearnPlayApp(
    viewModel: LearnPlayViewModel = viewModel()
) {
    val userProfile by viewModel.userProfile.collectAsState()
    val petState by viewModel.petState.collectAsState()
    val currentTab by viewModel.currentTab.collectAsState()
    val quizState by viewModel.quizState.collectAsState()
    val isMiniGameActive by viewModel.isMiniGameActive.collectAsState()
    val isAiSheetOpen by viewModel.isAiSheetOpen.collectAsState()
    val aiMessages by viewModel.aiMessages.collectAsState()
    val aiIsLoading by viewModel.aiIsLoading.collectAsState()
    val isSpeaking by viewModel.ttsManager.isSpeaking.collectAsState()
    val showFeedingCelebration by viewModel.showFeedingCelebration.collectAsState()
    val isFloatingPetExpanded by viewModel.isFloatingPetExpanded.collectAsState()
    val isFloatingPetVisible by viewModel.isFloatingPetVisible.collectAsState()

    // Back button handling
    BackHandler(enabled = isMiniGameActive || quizState != null) {
        if (isMiniGameActive) {
            viewModel.setTab(NavTab.GAMES)
            viewModel.exitQuiz()
        } else if (quizState != null) {
            viewModel.exitQuiz()
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            // Hide main header during fullscreen Mini-Game or Quiz
            if (!isMiniGameActive && quizState == null) {
                HeaderBar(
                    profile = userProfile,
                    petState = petState,
                    onPetClick = { viewModel.setTab(NavTab.PET) }
                )
            }
        },
        bottomBar = {
            // Hide bottom navigation bar during active Mini-Game or Quiz
            if (!isMiniGameActive && quizState == null) {
                BottomNavBar(
                    selectedTab = currentTab,
                    onTabSelected = { viewModel.setTab(it) }
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when {
                // 1. Playable Mini-Game Screen (Coin Catcher)
                isMiniGameActive -> {
                    MiniGameScreen(
                        onFinishGame = { caughtCoins ->
                            viewModel.finishMiniGame(caughtCoins)
                            viewModel.setTab(NavTab.PET) // Guide to feed pet with earned coins!
                        },
                        onBackClick = {
                            viewModel.exitQuiz()
                            viewModel.setTab(NavTab.GAMES)
                        }
                    )
                }

                // 2. Active Quiz Flow or Quiz Results
                quizState != null -> {
                    val currentQuiz = quizState!!
                    if (currentQuiz.isFinished) {
                        QuizResultScreen(
                            quizState = currentQuiz,
                            onPlayGame = { viewModel.openMiniGame() },
                            onRetryQuiz = { viewModel.retryQuiz() },
                            onHomeClick = { viewModel.exitQuiz() },
                            onAskAi = { query -> viewModel.openAiAssistant(query) }
                        )
                    } else {
                        QuizScreen(
                            quizState = currentQuiz,
                            onSelectOption = { viewModel.selectQuizOption(it) },
                            onSubmitAnswer = { viewModel.submitCurrentAnswer() },
                            onNextQuestion = { viewModel.nextQuestionOrFinish() },
                            onSpeakQuestion = { viewModel.speakCurrentQuestion() },
                            isSpeaking = isSpeaking,
                            onAskAi = { defaultMsg -> viewModel.openAiAssistant(defaultMsg) },
                            onExitQuiz = { viewModel.exitQuiz() }
                        )
                    }
                }

                // 3. Main Bottom Tabs
                else -> {
                    when (currentTab) {
                        NavTab.HOME -> {
                            HomeScreen(
                                profile = userProfile,
                                petState = petState,
                                onZoneClick = { zone -> viewModel.startQuiz(zone) },
                                onPetClick = { viewModel.setTab(NavTab.PET) },
                                onAskAiClick = { viewModel.openAiAssistant() },
                                onPlayGameClick = { viewModel.openMiniGame() }
                            )
                        }
                        NavTab.LEARN -> {
                            LearnScreen(
                                profile = userProfile,
                                onStartZone = { zone -> viewModel.startQuiz(zone) }
                            )
                        }
                        NavTab.GAMES -> {
                            GamesHubScreen(
                                profile = userProfile,
                                onPlayCoinCatcher = { viewModel.openMiniGame() },
                                onGoToQuiz = { viewModel.setTab(NavTab.LEARN) }
                            )
                        }
                        NavTab.PET -> {
                            PetScreen(
                                petState = petState,
                                profile = userProfile,
                                onFeedPet = { food -> viewModel.feedPet(food) },
                                showFeedingCelebration = showFeedingCelebration,
                                onAskAiHint = { viewModel.openAiAssistant("Can Buddy give me a learning tip?") },
                                onPlayGamesClick = { viewModel.setTab(NavTab.GAMES) }
                            )
                        }
                        NavTab.PROFILE -> {
                            ProfileScreen(
                                profile = userProfile,
                                onUpdateName = { viewModel.setStudentName(it) },
                                onToggleCollegeMode = { viewModel.setCollegeMode(it) },
                                onResetProgress = { viewModel.resetData() }
                            )
                        }
                    }
                }
            }

            // Persistent Floating UI Overlay for Buddy the Virtual Pet
            if (!isMiniGameActive && currentTab != NavTab.PET && isFloatingPetVisible) {
                FloatingBuddyOverlay(
                    petState = petState,
                    profile = userProfile,
                    isExpanded = isFloatingPetExpanded,
                    onToggleExpand = { viewModel.toggleFloatingPetExpanded() },
                    onDismiss = { viewModel.dismissFloatingPetExpanded() },
                    onQuickFeed = { food -> viewModel.feedPet(food) },
                    onOpenPetScreen = {
                        viewModel.dismissFloatingPetExpanded()
                        viewModel.setTab(NavTab.PET)
                    },
                    onAskHint = {
                        viewModel.dismissFloatingPetExpanded()
                        val q = quizState?.currentQuestion
                        if (q != null) {
                            viewModel.openAiAssistant("Can Buddy give me a hint for this question?")
                        } else {
                            viewModel.openAiAssistant("What learning advice does Buddy have for me right now?")
                        }
                    },
                    modifier = Modifier.align(Alignment.BottomEnd)
                )
            }
        }
    }

    // AI Learning Assistant Bottom Sheet (Ask TaleBot)
    AIAssistantSheet(
        isOpen = isAiSheetOpen,
        onDismiss = { viewModel.closeAiAssistant() },
        messages = aiMessages,
        isLoading = aiIsLoading,
        onSendMessage = { viewModel.askAi(it) }
    )
}
