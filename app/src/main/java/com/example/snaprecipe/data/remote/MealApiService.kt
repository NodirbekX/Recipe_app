package com.example.snaprecipe.data.remote

import com.example.snaprecipe.data.model.MealDetailResponse
import com.example.snaprecipe.data.model.MealFilterResponse
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * TheMealDB free API (no key required).
 * Base URL: https://www.themealdb.com/api/json/v1/1/
 */
interface MealApiService {

    /** Search by main ingredient → lightweight summaries. */
    @GET("filter.php")
    suspend fun filterByIngredient(@Query("i") ingredient: String): MealFilterResponse

    /** Search by meal name → full meal objects. */
    @GET("search.php")
    suspend fun searchByName(@Query("s") name: String): MealDetailResponse

    /** Look up a single meal's full details by id. */
    @GET("lookup.php")
    suspend fun lookupById(@Query("i") id: String): MealDetailResponse
}
