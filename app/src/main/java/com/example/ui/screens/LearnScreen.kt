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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.LearningZone
import com.example.data.model.UserProfile
import com.example.ui.theme.PrimaryIndigo

@Composable
fun LearnScreen(
    profile: UserProfile,
    onStartZone: (LearningZone) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Hero / Concept Flow Card
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFEEF2FF)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "📚 Learn & Unlock Flow",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryIndigo
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Learn ➔ Answer ➔ Unlock Game ➔ Play ➔ Earn Coins ➔ Feed Pet ➔ Level Up!",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF334155),
                        lineHeight = 18.sp
                    )
                }
            }
        }

        item {
            Text(
                text = "All Learning Zones",
                fontSize = 20.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFF0F172A)
            )
        }

        items(LearningZone.entries) { zone ->
            val completed = profile.zoneCompletedQuestions[zone.id] ?: 0
            val progressPercent = profile.getZoneProgressPercent(zone.id, zone.totalAvailable)

            ZoneCard(
                zone = zone,
                completedQuestions = completed,
                totalQuestions = zone.totalAvailable,
                progressPercent = progressPercent,
                isUnlocked = true,
                onStartClick = { onStartZone(zone) }
            )
        }

        item {
            Spacer(modifier = Modifier.height(96.dp))
        }
    }
}
