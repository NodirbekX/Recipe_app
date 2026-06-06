package com.example.snaprecipe.ui.state

import com.example.snaprecipe.data.model.MealSummary

/** The two phases shown on the loading screen while work is in flight. */
enum class LoadingPhase {
    ANALYZING,   // "🔍 Analyzing your photo..."
    SEARCHING    // "🍳 Searching recipes..."
}

/** Single source of truth for what the UI is currently showing. */
sealed interface RecipeUiState {

    /** Home screen — waiting for the user to take/pick a photo. */
    data object Idle : RecipeUiState

    /** Blocking, non-dismissable loading screen. */
    data class Loading(val phase: LoadingPhase) : RecipeUiState

    /** Recipes found — show the card list. */
    data class Results(
        val detectedFood: String,
        val meals: List<MealSummary>
    ) : RecipeUiState

    /**
     * Friendly empty state. [detectedFood] is null when the model returned UNKNOWN
     * (couldn't identify any food), which drives a different subtitle.
     */
    data class NotFound(
        val detectedFood: String?
    ) : RecipeUiState

    /** Recoverable failure (network, API error) — offers a retry. */
    data class Error(val message: String) : RecipeUiState
}
