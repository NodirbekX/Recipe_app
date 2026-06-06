package com.example.snaprecipe.data.repository

import android.graphics.Bitmap
import com.example.snaprecipe.data.model.ChatContent
import com.example.snaprecipe.data.model.ChatMessage
import com.example.snaprecipe.data.model.ChatRequest
import com.example.snaprecipe.data.model.Meal
import com.example.snaprecipe.data.model.MealSummary
import com.example.snaprecipe.data.remote.ChatApiService
import com.example.snaprecipe.data.remote.MealApiService
import com.example.snaprecipe.util.ImageUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Coordinates the two backends:
 *   1. An open-source vision model (via OpenRouter) identifies the food in the photo.
 *   2. TheMealDB turns that food name into recipe results.
 *
 * Methods are intentionally granular (identify, then find) so the ViewModel can
 * drive the two-phase loading UI between the steps. Network/parse failures are
 * surfaced as exceptions for the ViewModel to translate into an Error state.
 */
class RecipeRepository(
    private val chatApi: ChatApiService,
    private val mealApi: MealApiService
) {

    /** Sentinel returned by the model when it can't identify any food. */
    val unknownToken: String get() = UNKNOWN

    /**
     * Sends the photo to the vision model and returns the detected food name, or
     * [UNKNOWN]. Runs on the IO dispatcher (image encoding + network).
     */
    suspend fun identifyFood(bitmap: Bitmap): String = withContext(Dispatchers.IO) {
        val base64 = ImageUtils.resizeAndEncode(bitmap)

        val request = ChatRequest(
            model = MODEL,
            max_tokens = 64,
            messages = listOf(
                ChatMessage.system(SYSTEM_PROMPT),
                ChatMessage.user(
                    listOf(
                        ChatContent.image(base64, mediaType = "image/jpeg"),
                        ChatContent.text("Identify the food in this image.")
                    )
                )
            )
        )

        val response = chatApi.createChatCompletion(request)
        val raw = response.choices
            ?.firstOrNull()
            ?.message
            ?.content
            ?.trim()
            .orEmpty()

        normalizeFoodName(raw)
    }

    /**
     * Resolves a food name into recipe summaries. Tries ingredient search first,
     * then falls back to a name search. Returns an empty list when nothing matches.
     */
    suspend fun findMeals(food: String): List<MealSummary> = withContext(Dispatchers.IO) {
        // 1. Search by ingredient.
        val byIngredient = mealApi.filterByIngredient(food).meals
        if (!byIngredient.isNullOrEmpty()) return@withContext byIngredient

        // 2. Fall back to a name search, mapping full meals down to summaries.
        val byName = mealApi.searchByName(food).meals
        if (!byName.isNullOrEmpty()) {
            return@withContext byName.map { it.toSummary() }
        }

        emptyList()
    }

    /** Fetches the full recipe for a meal id (used by the Detail screen). */
    suspend fun getMealDetail(id: String): Meal? = withContext(Dispatchers.IO) {
        mealApi.lookupById(id).meals?.firstOrNull()
    }

    // ---- helpers ----

    private fun normalizeFoodName(raw: String): String {
        if (raw.isEmpty()) return UNKNOWN
        // The model is told to answer with exactly UNKNOWN when it can't tell.
        if (raw.uppercase().contains(UNKNOWN)) return UNKNOWN
        // Strip surrounding quotes/punctuation and trailing period the model may add.
        return raw.trim().trim('"', '\'', '.', ' ')
            .ifEmpty { UNKNOWN }
    }

    private fun Meal.toSummary() = MealSummary(
        idMeal = idMeal,
        strMeal = strMeal,
        strMealThumb = strMealThumb.orEmpty()
    )

    companion object {
        const val UNKNOWN = "UNKNOWN"

        // Open-source vision model served via OpenRouter. Qwen2.5-VL is currently
        // one of the strongest open-weight vision models. Alternatives you can drop
        // in (see https://openrouter.ai/models?modality=image):
        //   "meta-llama/llama-3.2-11b-vision-instruct:free"  (free tier)
        //   "qwen/qwen-2.5-vl-7b-instruct"                   (cheaper/faster)
        private const val MODEL = "qwen/qwen-2.5-vl-72b-instruct"

        private const val SYSTEM_PROMPT =
            "You are a food recognition assistant. Look at the image and respond with " +
                "ONLY the name of the main food ingredient or dish you see. Be specific but " +
                "concise. Single words or short phrases only. Examples: 'chicken', 'pasta', " +
                "'tomato', 'beef stew'. If you cannot identify any food, respond with exactly: " +
                "UNKNOWN"
    }
}
