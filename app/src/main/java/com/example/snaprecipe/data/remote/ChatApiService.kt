package com.example.snaprecipe.data.remote

import com.example.snaprecipe.data.model.ChatRequest
import com.example.snaprecipe.data.model.ChatResponse
import retrofit2.http.Body
import retrofit2.http.POST

/**
 * OpenAI-compatible Chat Completions API (served by OpenRouter). Auth + content-type
 * headers (`Authorization: Bearer …`, `content-type`) are attached centrally by the
 * auth interceptor in [RetrofitClient], so they aren't declared per-method here.
 */
interface ChatApiService {

    @POST("chat/completions")
    suspend fun createChatCompletion(@Body request: ChatRequest): ChatResponse
}
