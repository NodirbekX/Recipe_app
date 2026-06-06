package com.example.snaprecipe.ui.navigation

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.snaprecipe.ui.screens.DetailScreen
import com.example.snaprecipe.ui.screens.ErrorScreen
import com.example.snaprecipe.ui.screens.HomeScreen
import com.example.snaprecipe.ui.screens.LoadingScreen
import com.example.snaprecipe.ui.screens.NotFoundScreen
import com.example.snaprecipe.ui.screens.ResultsScreen
import com.example.snaprecipe.ui.state.RecipeUiState
import com.example.snaprecipe.ui.viewmodel.RecipeViewModel

/** Navigation routes. */
object Routes {
    const val MAIN = "main"
    const val DETAIL = "detail"
    const val ARG_MEAL_ID = "mealId"
    fun detail(mealId: String) = "$DETAIL/$mealId"
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    // One ViewModel shared across the whole graph (scoped to the Activity store),
    // so Home → Results state and the meal-detail flow share the same instance.
    val viewModel: RecipeViewModel = viewModel(factory = RecipeViewModel.Factory)

    NavHost(navController = navController, startDestination = Routes.MAIN) {

        composable(Routes.MAIN) {
            val state by viewModel.uiState.collectAsStateWithLifecycle()

            // Cross-fade between the main flow states (Home/Loading/Results/etc.).
            AnimatedContent(
                targetState = state,
                transitionSpec = {
                    (fadeIn(tween(300)) togetherWith fadeOut(tween(200)))
                },
                label = "main-flow"
            ) { current ->
                when (current) {
                    is RecipeUiState.Idle -> HomeScreen(
                        onImageSelected = viewModel::onImageSelected
                    )

                    is RecipeUiState.Loading -> LoadingScreen(phase = current.phase)

                    is RecipeUiState.Results -> ResultsScreen(
                        detectedFood = current.detectedFood,
                        meals = current.meals,
                        onMealClick = { mealId ->
                            navController.navigate(Routes.detail(mealId))
                        },
                        onSearchAgain = viewModel::reset
                    )

                    is RecipeUiState.NotFound -> NotFoundScreen(
                        detectedFood = current.detectedFood,
                        onTryAgain = viewModel::reset
                    )

                    is RecipeUiState.Error -> ErrorScreen(
                        message = current.message,
                        onRetry = viewModel::retry,
                        onHome = viewModel::reset
                    )
                }
            }
        }

        composable(
            route = "${Routes.DETAIL}/{${Routes.ARG_MEAL_ID}}",
            arguments = listOf(navArgument(Routes.ARG_MEAL_ID) { type = NavType.StringType })
        ) { backStackEntry ->
            val mealId = backStackEntry.arguments?.getString(Routes.ARG_MEAL_ID).orEmpty()
            DetailScreen(
                mealId = mealId,
                viewModel = viewModel,
                onBack = { navController.popBackStack() },
                modifier = Modifier
            )
        }
    }
}
