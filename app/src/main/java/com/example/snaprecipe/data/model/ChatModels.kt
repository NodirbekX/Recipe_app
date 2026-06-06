package com.example.snaprecipe.data.model

/**
 * Request/response models for an OpenAI-compatible Chat Completions API
 * (POST /chat/completions). We target OpenRouter, which fronts open-source
 * vision models (Qwen2.5-VL, Llama Vision, …) behind this same schema.
 *
 * Image input follows the OpenAI vision convention: an `image_url` content block
 * whose `url` is a data URI, e.g. `data:image/jpeg;base64,<DATA>` — NOT the
 * Anthropic-style `{"type":"image","source":{...}}`.
 *
 * Note on [ChatMessage.content]: in this schema a message's content is EITHER a
 * plain string (system/text-only messages) OR an array of content blocks
 * (multimodal user messages). We model it as [Any] and let Gson serialize by the
 * runtime type — a String becomes a JSON string, a List becomes a JSON array.
 * We only ever serialize requests (never deserialize them), so this is safe.
 */
data class ChatRequest(
    val model: String,
    val max_tokens: Int,
    val messages: List<ChatMessage>
)

data class ChatMessage(
    val role: String,
    val content: Any
) {
    companion object {
        /** A system (or any text-only) message: content is a plain string. */
        fun system(text: String) = ChatMessage(role = "system", content = text)

        /** A user message with one or more multimodal content blocks. */
        fun user(blocks: List<ChatContent>) = ChatMessage(role = "user", content = blocks)
    }
}

data class ChatContent(
    val type: String,
    // Present only for text blocks.
    val text: String? = null,
    // Present only for image blocks.
    val image_url: ChatImageUrl? = null
) {
    companion object {
        fun text(value: String) = ChatContent(type = "text", text = value)

        fun image(base64Data: String, mediaType: String = "image/jpeg") = ChatContent(
            type = "image_url",
            image_url = ChatImageUrl(url = "data:$mediaType;base64,$base64Data")
        )
    }
}

data class ChatImageUrl(val url: String)

// ---- Response ----

data class ChatResponse(
    val choices: List<ChatChoice>?
)

data class ChatChoice(
    val message: ChatResponseMessage?
)

data class ChatResponseMessage(
    val role: String?,
    val content: String?
)
