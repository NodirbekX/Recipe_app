package com.example.snaprecipe.data.model

/**
 * Models for TheMealDB free API (https://www.themealdb.com/api.php).
 * When a query has no results, the `meals` field comes back as JSON `null`.
 */

// filter.php?i={ingredient}  → lightweight summaries (id, name, thumb only).
data class MealFilterResponse(
    val meals: List<MealSummary>?
)

data class MealSummary(
    val idMeal: String,
    val strMeal: String,
    val strMealThumb: String
)

// lookup.php?i={id}  and  search.php?s={name}  → full meal objects.
data class MealDetailResponse(
    val meals: List<Meal>?
)

data class Meal(
    val idMeal: String,
    val strMeal: String,
    val strCategory: String?,
    val strArea: String?,
    val strInstructions: String?,
    val strMealThumb: String?,
    val strYoutube: String?,
    val strIngredient1: String?, val strMeasure1: String?,
    val strIngredient2: String?, val strMeasure2: String?,
    val strIngredient3: String?, val strMeasure3: String?,
    val strIngredient4: String?, val strMeasure4: String?,
    val strIngredient5: String?, val strMeasure5: String?,
    val strIngredient6: String?, val strMeasure6: String?,
    val strIngredient7: String?, val strMeasure7: String?,
    val strIngredient8: String?, val strMeasure8: String?,
    val strIngredient9: String?, val strMeasure9: String?,
    val strIngredient10: String?, val strMeasure10: String?,
    val strIngredient11: String?, val strMeasure11: String?,
    val strIngredient12: String?, val strMeasure12: String?,
    val strIngredient13: String?, val strMeasure13: String?,
    val strIngredient14: String?, val strMeasure14: String?,
    val strIngredient15: String?, val strMeasure15: String?,
    val strIngredient16: String?, val strMeasure16: String?,
    val strIngredient17: String?, val strMeasure17: String?,
    val strIngredient18: String?, val strMeasure18: String?,
    val strIngredient19: String?, val strMeasure19: String?,
    val strIngredient20: String?, val strMeasure20: String?
) {
    /**
     * Collapses the 20 flat ingredient/measure slots into a clean list, skipping
     * blanks/nulls (TheMealDB pads unused slots with "" or null).
     */
    fun ingredientPairs(): List<IngredientItem> {
        val ingredients = listOf(
            strIngredient1, strIngredient2, strIngredient3, strIngredient4, strIngredient5,
            strIngredient6, strIngredient7, strIngredient8, strIngredient9, strIngredient10,
            strIngredient11, strIngredient12, strIngredient13, strIngredient14, strIngredient15,
            strIngredient16, strIngredient17, strIngredient18, strIngredient19, strIngredient20
        )
        val measures = listOf(
            strMeasure1, strMeasure2, strMeasure3, strMeasure4, strMeasure5,
            strMeasure6, strMeasure7, strMeasure8, strMeasure9, strMeasure10,
            strMeasure11, strMeasure12, strMeasure13, strMeasure14, strMeasure15,
            strMeasure16, strMeasure17, strMeasure18, strMeasure19, strMeasure20
        )
        return ingredients.indices.mapNotNull { i ->
            val name = ingredients[i]?.trim()
            if (name.isNullOrEmpty()) return@mapNotNull null
            IngredientItem(name = name, measure = measures[i]?.trim().orEmpty())
        }
    }

    /** Splits the run-on instructions string into trimmed, non-empty steps. */
    fun instructionSteps(): List<String> {
        return strInstructions
            ?.split("\r\n", "\n")
            ?.map { it.trim() }
            ?.filter { it.isNotEmpty() }
            ?: emptyList()
    }
}

data class IngredientItem(
    val name: String,
    val measure: String
)
