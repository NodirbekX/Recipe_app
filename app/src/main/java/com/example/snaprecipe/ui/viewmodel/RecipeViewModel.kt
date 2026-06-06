package com.example.snaprecipe.ui.viewmodel

import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import com.example.snaprecipe.SnapRecipeApplication
import com.example.snaprecipe.data.model.Meal
import com.example.snaprecipe.data.repository.RecipeRepository
import com.example.snaprecipe.ui.state.LoadingPhase
import com.example.snaprecipe.ui.state.RecipeUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

/** Detail-screen state, kept separate from the main flow state. */
sealed interface DetailUiState {
    data object Loading : DetailUiState
    data class Success(val meal: Meal) : DetailUiState
    data class Error(val message: String) : DetailUiState
}

class RecipeViewModel(
    private val repository: RecipeRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<RecipeUiState>(RecipeUiState.Idle)
    val uiState: StateFlow<RecipeUiState> = _uiState.asStateFlow()

    private val _detailState = MutableStateFlow<DetailUiState>(DetailUiState.Loading)
    val detailState: StateFlow<DetailUiState> = _detailState.asStateFlow()

    // Held so the Error screen's "retry" can re-run the pipeline on the same photo.
    private var lastBitmap: Bitmap? = null

    /** Entry point from Home once a photo has been captured or picked. */
    fun onImageSelected(bitmap: Bitmap) {
        lastBitmap = bitmap
        runPipeline(bitmap)
    }

    /** Re-runs identification + search on the most recent photo (Error screen). */
    fun retry() {
        lastBitmap?.let { runPipeline(it) } ?: reset()
    }

    /** Returns to the Home screen and clears transient state. */
    fun reset() {
        _uiState.value = RecipeUiState.Idle
    }

    private fun runPipeline(bitmap: Bitmap) {
        viewModelScope.launch {
            _uiState.value = RecipeUiState.Loading(LoadingPhase.ANALYZING)
            try {
                val food = repository.identifyFood(bitmap)

                if (food == RecipeRepository.UNKNOWN) {
                    _uiState.value = RecipeUiState.NotFound(detectedFood = null)
                    return@launch
                }

                _uiState.value = RecipeUiState.Loading(LoadingPhase.SEARCHING)
                val meals = repository.findMeals(food)

                _uiState.value = if (meals.isEmpty()) {
                    RecipeUiState.NotFound(detectedFood = food)
                } else {
                    RecipeUiState.Results(detectedFood = food, meals = meals)
                }
            } catch (e: IOException) {
                _uiState.value = RecipeUiState.Error(
                    "No internet connection. Check your network and try again."
                )
            } catch (e: HttpException) {
                // Surface the API's actual error body — for non-2xx responses the
                // Anthropic/MealDB payload explains *why* (e.g. invalid model, image
                // too large). Without this, a 400 is opaque.
                val errorBody = e.response()?.errorBody()?.string()
                Log.e("RecipeViewModel", "HTTP ${e.code()} from API: $errorBody")
                _uiState.value = RecipeUiState.Error(
                    "Something went wrong reaching the server (${e.code()}). Please try again."
                )
            } catch (e: Exception) {
                _uiState.value = RecipeUiState.Error(
                    "Unexpected error: ${e.message ?: "please try again"}"
                )
            }
        }
    }

    /** Loads full recipe details for the Detail screen. */
    fun loadMealDetail(mealId: String) {
        viewModelScope.launch {
            _detailState.value = DetailUiState.Loading
            try {
                val meal = repository.getMealDetail(mealId)
                _detailState.value = if (meal != null) {
                    DetailUiState.Success(meal)
                } else {
                    DetailUiState.Error("Couldn't load this recipe.")
                }
            } catch (e: IOException) {
                _detailState.value = DetailUiState.Error("No internet connection.")
            } catch (e: Exception) {
                _detailState.value = DetailUiState.Error(
                    "Couldn't load this recipe: ${e.message ?: "try again"}"
                )
            }
        }
    }

    companion object {
        /** Factory that pulls the repository out of the app's manual DI container. */
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as SnapRecipeApplication
                RecipeViewModel(app.container.recipeRepository)
            }
        }
    }
}
