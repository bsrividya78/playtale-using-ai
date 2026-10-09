package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.QuizState
import com.example.ui.theme.AccentGreen
import com.example.ui.theme.AccentYellow
import com.example.ui.theme.PrimaryIndigo
import com.example.ui.theme.PrimaryIndigoDark

@Composable
fun QuizResultScreen(
    quizState: QuizState,
    onPlayGame: () -> Unit,
    onRetryQuiz: () -> Unit,
    onHomeClick: () -> Unit,
    onAskAi: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val score = quizState.score
    val total = quizState.questions.size.coerceAtLeast(1)
    val isSuccess = score >= 3
    val mistakes = quizState.mistakes

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(10.dp))

        // Large Trophy / Star Graphic
        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
                .background(
                    if (isSuccess) {
                        Brush.linearGradient(listOf(Color(0xFFFDE68A), Color(0xFFF59E0B)))
                    } else {
                        Brush.linearGradient(listOf(Color(0xFFE2E8F0), Color(0xFF94A3B8)))
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (isSuccess) "🏆" else "💪",
                fontSize = 54.sp
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = "Challenge Complete!",
            fontSize = 24.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color(0xFF0F172A)
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "Score: $score / $total",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = if (isSuccess) AccentGreen else Color(0xFFD97706)
        )

        // Star rating: ⭐⭐⭐⭐⭐
        Row(
            modifier = Modifier.padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            for (i in 1..total) {
                Text(
                    text = if (i <= score) "⭐" else "☆",
                    fontSize = 26.sp,
                    color = AccentYellow,
                    modifier = Modifier.padding(horizontal = 2.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Outcome Card
        if (isSuccess) {
            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFECFDF5)),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("success_result_card")
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "🎉 Great job!",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF065F46)
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "🔓 Mini Game Unlocked!",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = AccentGreen
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Play 'Coin Catcher' now to earn +20 coins and tasty treats for Buddy!",
                        fontSize = 13.sp,
                        color = Color(0xFF047857),
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = onPlayGame,
                        colors = ButtonDefaults.buttonColors(containerColor = AccentGreen),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("play_game_button")
                    ) {
                        Text(
                            text = "PLAY GAME 🎮",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        } else {
            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFBEB)),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("try_again_result_card")
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Almost there!",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF92400E)
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "You need 3/5 to unlock the Mini Game. Review your mistakes below and try again!",
                        fontSize = 13.sp,
                        color = Color(0xFFB45309),
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = onRetryQuiz,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF59E0B)),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("try_again_button")
                    ) {
                        Text(
                            text = "TRY AGAIN 🔄",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }

        // Mistakes & Explanations Review
        if (mistakes.isNotEmpty()) {
            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Review Mistakes (${mistakes.size})",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0F172A),
                modifier = Modifier.align(Alignment.Start)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                mistakes.forEachIndexed { idx, review ->
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = "${idx + 1}. ${review.question.questionText}",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1E293B)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Correct answer: ${review.question.options[review.question.correctOptionIndex]}",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = AccentGreen
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Explanation: ${review.question.explanation}",
                                fontSize = 12.sp,
                                color = Color(0xFF64748B)
                            )
                        }
                    }
                }
            }
        }

        // AI Coaching & Hint Section
        Spacer(modifier = Modifier.height(18.dp))

        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFEEF2FF)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "🤖", fontSize = 28.sp)
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "AI Learning Agent Hint",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryIndigo
                    )
                    Text(
                        text = if (isSuccess) {
                            "Outstanding performance! Your cognitive skills are sharp. Let's catch those falling coins!"
                        } else {
                            "Every attempt builds new neural connections! Tap below to ask TaleBot for a custom study clue."
                        },
                        fontSize = 12.sp,
                        color = Color(0xFF334155)
                    )
                }

                Surface(
                    onClick = {
                        onAskAi("How can I improve on ${quizState.zone.title}?")
                    },
                    shape = RoundedCornerShape(10.dp),
                    color = PrimaryIndigo,
                    modifier = Modifier.testTag("ai_coach_button")
                ) {
                    Text(
                        text = "Ask AI",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        OutlinedButton(
            onClick = onHomeClick,
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(46.dp)
                .testTag("back_to_zones_button")
        ) {
            Text("Back to Learning Zones", color = Color(0xFF334155), fontWeight = FontWeight.SemiBold)
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}
