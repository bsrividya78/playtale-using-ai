package com.example.data.model

import androidx.compose.ui.graphics.Color
import com.example.ui.theme.ZoneCodeColor
import com.example.ui.theme.ZoneMathColor
import com.example.ui.theme.ZoneScienceColor
import com.example.ui.theme.ZoneWorldColor

enum class LearningZone(
    val id: String,
    val title: String,
    val subtitle: String,
    val iconEmoji: String,
    val themeColor: Color,
    val totalAvailable: Int = 10
) {
    CODE(
        id = "code",
        title = "Code Zone",
        subtitle = "Learn coding and programming",
        iconEmoji = "💻",
        themeColor = ZoneCodeColor
    ),
    MATH(
        id = "math",
        title = "Math Zone",
        subtitle = "Learn numbers and mathematics",
        iconEmoji = "🔢",
        themeColor = ZoneMathColor
    ),
    SCIENCE(
        id = "science",
        title = "Science Zone",
        subtitle = "Explore science and discover how things work",
        iconEmoji = "🔬",
        themeColor = ZoneScienceColor
    ),
    WORLD(
        id = "world",
        title = "World Zone",
        subtitle = "Explore nature, environment and everyday life",
        iconEmoji = "🌎",
        themeColor = ZoneWorldColor
    );

    companion object {
        fun fromId(id: String): LearningZone {
            return entries.find { it.id.equals(id, ignoreCase = true) } ?: CODE
        }
    }
}
