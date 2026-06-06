package com.example.snaprecipe.di

import com.example.snaprecipe.data.remote.RetrofitClient
import com.example.snaprecipe.data.repository.RecipeRepository

/**
 * Tiny hand-rolled DI container (no Hilt, by design). Holds app-scoped singletons
 * and wires the repository to its API services.
 */
class AppContainer {
    val recipeRepository: RecipeRepository by lazy {
        RecipeRepository(
            chatApi = RetrofitClient.chatApi,
            mealApi = RetrofitClient.mealApi
        )
    }
}
