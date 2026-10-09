package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.QuizState
import com.example.ui.theme.AccentGreen
import com.example.ui.theme.AccentRed
import com.example.ui.theme.AccentYellow
import com.example.ui.theme.PrimaryIndigo

@Composable
fun QuizScreen(
    quizState: QuizState,
    onSelectOption: (Int) -> Unit,
    onSubmitAnswer: () -> Unit,
    onNextQuestion: () -> Unit,
    onSpeakQuestion: () -> Unit,
    isSpeaking: Boolean,
    onAskAi: (String?) -> Unit,
    onExitQuiz: () -> Unit,
    modifier: Modifier = Modifier
) {
    val question = quizState.currentQuestion ?: return
    val totalCount = quizState.questions.size
    val currentNumber = quizState.currentIndex + 1
    val progress = currentNumber.toFloat() / totalCount.toFloat()
    val zone = quizState.zone

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        // Top Nav & Title
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onExitQuiz,
                modifier = Modifier.testTag("exit_quiz_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Exit Quiz",
                    tint = Color(0xFF334155)
                )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Ready for the Challenge?",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A)
                )
                Text(
                    text = "${zone.title} ${zone.iconEmoji}",
                    fontSize = 12.sp,
                    color = zone.themeColor,
                    fontWeight = FontWeight.SemiBold
                )
            }

            // Ask TaleBot Quick Button
            Surface(
                onClick = { onAskAi("Can you give me a hint for this question?") },
                shape = RoundedCornerShape(14.dp),
                color = Color(0xFFE0E7FF),
                modifier = Modifier.testTag("ask_talebot_quiz_button")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "🤖", fontSize = 14.sp)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Hint",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryIndigo
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Progress Counter & Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Question $currentNumber of $totalCount",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF475569)
            )

            Text(
                text = "Score: ${quizState.score} ⭐",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = AccentYellow
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .testTag("quiz_progress_bar"),
            color = zone.themeColor,
            trackColor = Color(0xFFE2E8F0),
            strokeCap = StrokeCap.Round
        )

        Spacer(modifier = Modifier.height(18.dp))

        // Question Card with Speaker Button
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("question_card")
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        text = question.questionText,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A),
                        lineHeight = 24.sp,
                        modifier = Modifier.weight(1f)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    // 🔊 Speaker Button (TTS)
                    IconButton(
                        onClick = onSpeakQuestion,
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(if (isSpeaking) Color(0xFFE0E7FF) else Color(0xFFF1F5F9))
                            .testTag("speak_question_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                            contentDescription = "Read Question Aloud",
                            tint = if (isSpeaking) PrimaryIndigo else Color(0xFF475569)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 4 Answer Options
        Column(
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            question.options.forEachIndexed { index, optionText ->
                val isSelected = quizState.selectedOptionIndex == index
                val isSubmitted = quizState.isSubmitted
                val isCorrectAnswer = index == question.correctOptionIndex

                val cardBackground: Color
                val borderStroke: BorderStroke?
                val textColor: Color

                if (isSubmitted) {
                    if (isCorrectAnswer) {
                        cardBackground = Color(0xFFDCFCE7)
                        borderStroke = BorderStroke(2.dp, AccentGreen)
                        textColor = Color(0xFF166534)
                    } else if (isSelected && !quizState.isCorrect) {
                        cardBackground = Color(0xFFFEE2E2)
                        borderStroke = BorderStroke(2.dp, AccentRed)
                        textColor = Color(0xFF991B1B)
                    } else {
                        cardBackground = Color.White
                        borderStroke = BorderStroke(1.dp, Color(0xFFE2E8F0))
                        textColor = Color(0xFF64748B)
                    }
                } else {
                    if (isSelected) {
                        cardBackground = Color(0xFFEEF2FF)
                        borderStroke = BorderStroke(2.dp, PrimaryIndigo)
                        textColor = PrimaryIndigo
                    } else {
                        cardBackground = Color.White
                        borderStroke = BorderStroke(1.dp, Color(0xFFE2E8F0))
                        textColor = Color(0xFF1E293B)
                    }
                }

                Surface(
                    onClick = {
                        if (!isSubmitted) {
                            onSelectOption(index)
                        }
                    },
                    shape = RoundedCornerShape(18.dp),
                    color = cardBackground,
                    border = borderStroke,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("quiz_option_$index")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Letter / Indicator badge
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(
                                    when {
                                        isSubmitted && isCorrectAnswer -> AccentGreen
                                        isSubmitted && isSelected && !quizState.isCorrect -> AccentRed
                                        isSelected -> PrimaryIndigo
                                        else -> Color(0xFFF1F5F9)
                                    }
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isSubmitted && isCorrectAnswer) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = "Correct",
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            } else if (isSubmitted && isSelected && !quizState.isCorrect) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Incorrect",
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            } else {
                                Text(
                                    text = ('A' + index).toString(),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = if (isSelected) Color.White else Color(0xFF475569)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Text(
                            text = optionText,
                            fontSize = 15.sp,
                            fontWeight = if (isSelected || (isSubmitted && isCorrectAnswer)) FontWeight.Bold else FontWeight.Medium,
                            color = textColor,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Feedback / Simple Explanation when submitted
        AnimatedVisibility(
            visible = quizState.isSubmitted,
            enter = fadeIn() + slideInVertically()
        ) {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (quizState.isCorrect) Color(0xFFF0FDF4) else Color(0xFFFEF2F2)
                ),
                border = BorderStroke(
                    1.dp,
                    if (quizState.isCorrect) Color(0xFF86EFAC) else Color(0xFFFECACA)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("explanation_card")
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = if (quizState.isCorrect) "🎉 Correct! +5 XP" else "💡 Keep Learning!",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = if (quizState.isCorrect) Color(0xFF166534) else Color(0xFF991B1B)
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = question.explanation,
                        fontSize = 13.sp,
                        color = Color(0xFF334155),
                        lineHeight = 18.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // AI TaleBot Help Button
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        OutlinedButton(
                            onClick = { onAskAi("Can you explain why \"${question.questionText}\" works that way?") },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.testTag("explain_more_ai_btn")
                        ) {
                            Text("Explain with TaleBot 🤖", fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Action Button: Submit or Next Question
        if (!quizState.isSubmitted) {
            Button(
                onClick = onSubmitAnswer,
                enabled = quizState.selectedOptionIndex != null,
                colors = ButtonDefaults.buttonColors(
                    containerColor = zone.themeColor,
                    disabledContainerColor = Color(0xFFCBD5E1)
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("submit_answer_button")
            ) {
                Text(
                    text = "Submit Answer",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        } else {
            Button(
                onClick = onNextQuestion,
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryIndigo),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("next_question_button")
            ) {
                Text(
                    text = if (currentNumber < totalCount) "Next Question ➔" else "See Challenge Results 🎉",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }

        Spacer(modifier = Modifier.height(96.dp))
    }
}
