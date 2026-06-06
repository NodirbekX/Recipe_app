package com.example.snaprecipe.data.remote

import com.example.snaprecipe.BuildConfig
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Manual DI for the two backends the app talks to. Each gets its own Retrofit
 * instance because they have different base URLs (and OpenRouter needs auth headers).
 */
object RetrofitClient {

    private const val OPENROUTER_BASE_URL = "https://openrouter.ai/api/v1/"
    private const val MEALDB_BASE_URL = "https://www.themealdb.com/api/json/v1/1/"

    private val logging = HttpLoggingInterceptor().apply {
        // BASIC keeps base64 image payloads out of the log to avoid flooding it.
        level = if (BuildConfig.DEBUG) {
            HttpLoggingInterceptor.Level.BASIC
        } else {
            HttpLoggingInterceptor.Level.NONE
        }
    }

    // Vision requests can be large/slow, so allow generous timeouts.
    private val chatClient: OkHttpClient = OkHttpClient.Builder()
        .addInterceptor { chain ->
            val request = chain.request().newBuilder()
                .addHeader("Authorization", "Bearer ${BuildConfig.OPENROUTER_API_KEY}")
                .addHeader("content-type", "application/json")
                // Optional OpenRouter attribution headers (used for ranking on
                // openrouter.ai). Harmless to send; safe to remove.
                .addHeader("HTTP-Referer", "https://github.com/snaprecipe")
                .addHeader("X-Title", "SnapRecipe")
                .build()
            chain.proceed(request)
        }
        .addInterceptor(logging)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val mealClient: OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(logging)
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .build()

    val chatApi: ChatApiService by lazy {
        Retrofit.Builder()
            .baseUrl(OPENROUTER_BASE_URL)
            .client(chatClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ChatApiService::class.java)
    }

    val mealApi: MealApiService by lazy {
        Retrofit.Builder()
            .baseUrl(MEALDB_BASE_URL)
            .client(mealClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(MealApiService::class.java)
    }
}
