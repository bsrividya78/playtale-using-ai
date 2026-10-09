package com.example.data.ai

import com.example.BuildConfig
import com.example.data.model.LearningZone
import com.example.data.model.Question
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class GeminiTutorService {

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .build()

    suspend fun askTutor(
        userMessage: String,
        currentQuestion: Question?,
        zone: LearningZone?,
        selectedWrongOption: String? = null
    ): String = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY

        if (!apiKey.isNullOrBlank() && apiKey != "MY_GEMINI_API_KEY") {
            try {
                val promptText = buildPrompt(userMessage, currentQuestion, zone, selectedWrongOption)
                val response = callGeminiApi(promptText, apiKey)
                if (!response.isNullOrBlank()) {
                    return@withContext response
                }
            } catch (_: Exception) {
                // Graceful fallback to smart educator rules
            }
        }

        // High quality pedagogical rule-based responses
        return@withContext generateSmartFallback(userMessage, currentQuestion, zone, selectedWrongOption)
    }

    private fun buildPrompt(
        userMessage: String,
        currentQuestion: Question?,
        zone: LearningZone?,
        selectedWrongOption: String?
    ): String {
        return buildString {
            append("You are TaleBot, an enthusiastic, friendly, kid-and-student-supportive AI Learning Agent in the PlayTale app. ")
            append("Always be encouraging, gentle, clear, and age-appropriate (simple enough for children, insightful for teens/college students). ")
            append("Keep responses concise (2 to 4 friendly sentences) with cheerful emojis.\n\n")

            if (zone != null) {
                append("Active Learning Zone: ${zone.title}\n")
            }
            if (currentQuestion != null) {
                append("Current Question: \"${currentQuestion.questionText}\"\n")
                append("Options: ${currentQuestion.options.joinToString(", ")}\n")
                append("Correct Answer: ${currentQuestion.options[currentQuestion.correctOptionIndex]}\n")
                append("Key Concept: ${currentQuestion.explanation}\n")
            }
            if (selectedWrongOption != null) {
                append("Student picked: \"$selectedWrongOption\" which was incorrect.\n")
            }
            append("\nStudent says/asks: \"$userMessage\"\n\n")
            append("TaleBot response:")
        }
    }

    private fun callGeminiApi(prompt: String, apiKey: String): String? {
        val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey"

        val partObj = JSONObject().put("text", prompt)
        val contentsArray = JSONArray().put(JSONObject().put("parts", JSONArray().put(partObj)))
        val payload = JSONObject().put("contents", contentsArray)

        val requestBody = payload.toString().toRequestBody("application/json".toMediaType())
        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .build()

        client.newCall(request).execute().use { response ->
            if (response.isSuccessful) {
                val bodyStr = response.body?.string() ?: return null
                val json = JSONObject(bodyStr)
                val candidates = json.optJSONArray("candidates") ?: return null
                if (candidates.length() > 0) {
                    val candidate = candidates.getJSONObject(0)
                    val content = candidate.optJSONObject("content")
                    val parts = content?.optJSONArray("parts")
                    if (parts != null && parts.length() > 0) {
                        return parts.getJSONObject(0).optString("text")
                    }
                }
            }
        }
        return null
    }

    private fun generateSmartFallback(
        userMessage: String,
        currentQuestion: Question?,
        zone: LearningZone?,
        selectedWrongOption: String?
    ): String {
        val lower = userMessage.lowercase()

        if (lower.contains("hint") || lower.contains("help") || lower.contains("don't understand") || lower.contains("clue")) {
            if (currentQuestion != null) {
                return "💡 Here is a friendly hint: ${currentQuestion.hint} Think carefully about that and give it another shot! 😊"
            }
            return "💡 Don't worry! Read each option carefully, eliminate the ones that clearly don't fit, and pick the best remaining answer. You've got this! 🌟"
        }

        if (lower.contains("why") || lower.contains("wrong") || lower.contains("explain")) {
            if (currentQuestion != null) {
                return "🧠 Here is why: ${currentQuestion.explanation} Mistakes are how our brain grows stronger! 💪"
            }
            return "🧠 Great curiosity! Learning happens when we figure out why something works. Review the concepts and keep going! ✨"
        }

        if (lower.contains("recommend") || lower.contains("next") || lower.contains("practice")) {
            return "🚀 Based on your learning journey, I recommend exploring the Math Zone or Science Zone next to boost your XP and unlock more mini-game coins! 🪙"
        }

        if (lower.contains("difficult") || lower.contains("hard") || lower.contains("easy")) {
            return "🎯 I'm automatically adapting the challenge to your pace! When you score 3/5 or higher, we unlock the Coin Catcher mini-game to celebrate! 🎮"
        }

        // Default warm encouragement
        if (currentQuestion != null) {
            return "🤖 Hello Learner! For this question (\"${currentQuestion.questionText}\"), remember: ${currentQuestion.hint} Take your time and believe in yourself! ✨"
        }

        return "🤖 Hi! I'm TaleBot, your personal learning coach. Feel free to ask me for hints, simple explanations, or study tips whenever you want! 🐶🌟"
    }
}
