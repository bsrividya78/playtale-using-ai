package com.example.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.AccentGreen
import com.example.ui.theme.AccentYellow
import com.example.ui.theme.PrimaryIndigo
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt
import kotlin.random.Random

data class FallingItem(
    val id: Long,
    var xPercent: Float, // 0.05f to 0.95f
    var yPercent: Float, // 0.0f to 1.0f
    val speed: Float,
    val isStar: Boolean = false,
    var isCollected: Boolean = false
)

@Composable
fun MiniGameScreen(
    onFinishGame: (Int) -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var gameState by remember { mutableStateOf("READY") } // READY, PLAYING, FINISHED
    var timeRemaining by remember { mutableIntStateOf(30) }
    var caughtCoins by remember { mutableIntStateOf(0) }
    var basketXPercent by remember { mutableFloatStateOf(0.5f) } // Center of screen
    val fallingItems = remember { mutableStateListOf<FallingItem>() }
    var lastItemId by remember { mutableStateOf(0L) }

    // Game loop
    LaunchedEffect(gameState) {
        if (gameState == "PLAYING") {
            timeRemaining = 30
            caughtCoins = 0
            fallingItems.clear()

            // Timer ticker
            val timerJob = launch {
                while (timeRemaining > 0 && gameState == "PLAYING") {
                    delay(1000L)
                    timeRemaining -= 1
                }
                if (gameState == "PLAYING") {
                    gameState = "FINISHED"
                }
            }

            // Spawn & Physics ticker
            val physicsJob = launch {
                var tick = 0
                while (gameState == "PLAYING") {
                    delay(40L) // ~25 FPS loop
                    tick++

                    // Spawn a coin periodically
                    if (tick % 15 == 0) {
                        val isStar = Random.nextFloat() > 0.8f
                        fallingItems.add(
                            FallingItem(
                                id = ++lastItemId,
                                xPercent = Random.nextFloat() * 0.8f + 0.1f,
                                yPercent = 0.0f,
                                speed = (Random.nextFloat() * 0.012f + 0.014f),
                                isStar = isStar
                            )
                        )
                    }

                    // Update positions and check collisions
                    val basketMin = basketXPercent - 0.12f
                    val basketMax = basketXPercent + 0.12f

                    for (item in fallingItems) {
                        if (!item.isCollected) {
                            item.yPercent += item.speed

                            // Check collision with basket at y >= 0.82f and y <= 0.92f
                            if (item.yPercent in 0.80f..0.92f && item.xPercent in basketMin..basketMax) {
                                item.isCollected = true
                                caughtCoins += if (item.isStar) 3 else 1
                            }
                        }
                    }

                    // Remove off-screen items
                    fallingItems.removeAll { it.yPercent > 1.05f || it.isCollected }
                }
            }

            timerJob.join()
            physicsJob.cancel()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF0F172A), Color(0xFF1E293B), Color(0xFF334155))
                )
            )
            .testTag("mini_game_screen")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBackClick,
                    modifier = Modifier.testTag("mini_game_back_btn")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }

                Text(
                    text = "🪙 Coin Catcher",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = AccentYellow
                )

                // Timer Pill
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (timeRemaining <= 5) Color(0xFFEF4444) else Color(0xFF475569))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "⏱️ ${timeRemaining}s",
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 14.sp
                    )
                }
            }

            // Score Banner
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Caught: $caughtCoins coins",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )

                Text(
                    text = "Drag or tap arrows to catch!",
                    color = Color(0xFF94A3B8),
                    fontSize = 12.sp
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Game Play Arena
            BoxWithConstraints(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0xFF1E293B).copy(alpha = 0.7f))
                    .testTag("game_canvas_area")
            ) {
                val arenaWidthPx = constraints.maxWidth.toFloat().coerceAtLeast(1f)
                val arenaHeightPx = constraints.maxHeight.toFloat().coerceAtLeast(1f)

                // Drag detection surface
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(gameState) {
                            if (gameState == "PLAYING") {
                                detectDragGestures { change, dragAmount ->
                                    change.consume()
                                    val delta = dragAmount.x / arenaWidthPx
                                    basketXPercent = (basketXPercent + delta).coerceIn(0.1f, 0.9f)
                                }
                            }
                        }
                )

                // Falling items
                if (gameState == "PLAYING") {
                    fallingItems.forEach { item ->
                        Box(
                            modifier = Modifier
                                .offset {
                                    IntOffset(
                                        x = (item.xPercent * arenaWidthPx).roundToInt(),
                                        y = (item.yPercent * arenaHeightPx).roundToInt()
                                    )
                                }
                                .size(34.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (item.isStar) "⭐" else "🪙",
                                fontSize = if (item.isStar) 28.sp else 24.sp
                            )
                        }
                    }
                }

                // Player Basket / Catcher at bottom
                Box(
                    modifier = Modifier
                        .offset {
                            IntOffset(
                                x = (basketXPercent * arenaWidthPx - 40.dp.roundToPx()).roundToInt(),
                                y = (arenaHeightPx - 70.dp.roundToPx()).roundToInt()
                            )
                        }
                        .size(width = 80.dp, height = 48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            Brush.verticalGradient(
                                listOf(AccentYellow, Color(0xFFD97706))
                            )
                        )
                        .testTag("player_basket"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "🧺 Catcher",
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF78350F),
                        fontSize = 13.sp
                    )
                }

                // Overlay for READY state
                if (gameState == "READY") {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.65f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Card(
                            shape = RoundedCornerShape(24.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            modifier = Modifier.padding(24.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(20.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.coin_catcher_hero),
                                    contentDescription = "Coin Catcher Hero",
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(130.dp)
                                        .clip(RoundedCornerShape(16.dp)),
                                    contentScale = ContentScale.Crop
                                )

                                Spacer(modifier = Modifier.height(14.dp))

                                Text(
                                    text = "Ready to Catch Coins?",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFF0F172A)
                                )

                                Spacer(modifier = Modifier.height(6.dp))

                                Text(
                                    text = "Move your basket left and right to catch as many falling coins as you can in 30 seconds!\n\nEarn +20 🪙 to buy snacks for Buddy!",
                                    fontSize = 13.sp,
                                    color = Color(0xFF475569),
                                    textAlign = TextAlign.Center
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                Button(
                                    onClick = { gameState = "PLAYING" },
                                    colors = ButtonDefaults.buttonColors(containerColor = AccentGreen),
                                    shape = RoundedCornerShape(16.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(48.dp)
                                        .testTag("start_mini_game_btn")
                                ) {
                                    Text(
                                        text = "START GAME 🚀",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    }
                }

                // Overlay for FINISHED state
                if (gameState == "FINISHED") {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.75f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Card(
                            shape = RoundedCornerShape(24.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            modifier = Modifier
                                .padding(24.dp)
                                .testTag("game_complete_dialog")
                        ) {
                            Column(
                                modifier = Modifier.padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "🎉",
                                    fontSize = 48.sp
                                )

                                Spacer(modifier = Modifier.height(6.dp))

                                Text(
                                    text = "Game Complete! 🎉",
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFF0F172A)
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(Color(0xFFFEF3C7))
                                        .padding(horizontal = 20.dp, vertical = 12.dp)
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            text = "Coins Earned:",
                                            fontSize = 13.sp,
                                            color = Color(0xFF92400E)
                                        )
                                        Text(
                                            text = "+${20 + (caughtCoins / 2)} 🪙",
                                            fontSize = 26.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = Color(0xFFB45309)
                                        )
                                        Text(
                                            text = "($caughtCoins coins caught in 30s!)",
                                            fontSize = 12.sp,
                                            color = Color(0xFF78350F)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(18.dp))

                                Button(
                                    onClick = { onFinishGame(caughtCoins) },
                                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryIndigo),
                                    shape = RoundedCornerShape(16.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(50.dp)
                                        .testTag("continue_after_game_button")
                                ) {
                                    Text(
                                        text = "Continue 🦴",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // On-screen Accessible Controls (Left & Right Buttons)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(
                    onClick = {
                        if (gameState == "PLAYING") {
                            basketXPercent = (basketXPercent - 0.12f).coerceIn(0.1f, 0.9f)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF334155)),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .size(width = 120.dp, height = 50.dp)
                        .testTag("left_catcher_button")
                ) {
                    Text(text = "◀ LEFT", fontWeight = FontWeight.Bold, color = Color.White)
                }

                Button(
                    onClick = {
                        if (gameState == "PLAYING") {
                            basketXPercent = (basketXPercent + 0.12f).coerceIn(0.1f, 0.9f)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF334155)),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .size(width = 120.dp, height = 50.dp)
                        .testTag("right_catcher_button")
                ) {
                    Text(text = "RIGHT ▶", fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
    }
}
