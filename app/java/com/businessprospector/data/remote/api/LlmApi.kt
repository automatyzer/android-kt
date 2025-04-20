package com.businessprospector.data.remote.api

import com.businessprospector.data.remote.dto.GoogleSearchResponse
import com.businessprospector.data.remote.dto.LlmResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Query

// Interfejs API dla modeli językowych (LLM)
interface LlmApi {
    // OpenAI API
    @Headers("Content-Type: application/json")
    @POST("v1/chat/completions")
    suspend fun generateOpenAiCompletion(
        @Body request: OpenAiCompletionRequest,
        @retrofit2.http.Header("Authorization") authHeader: String
    ): Response<OpenAiCompletionResponse>

    // Claude API
    @Headers("Content-Type: application/json", "anthropic-version: 2023-06-01")
    @POST("v1/messages")
    suspend fun generateClaudeCompletion(
        @Body request: ClaudeCompletionRequest,
        @retrofit2.http.Header("x-api-key") apiKey: String
    ): Response<ClaudeCompletionResponse>

    // Google Gemini API
    @Headers("Content-Type: application/json")
    @POST("v1beta/models/gemini-pro:generateContent")
    suspend fun generateGeminiCompletion(
        @Body request: GeminiCompletionRequest,
        @Query("key") apiKey: String
    ): Response<GeminiCompletionResponse>

    // Mistral API
    @Headers("Content-Type: application/json")
    @POST("v1/chat/completions")
    suspend fun generateMistralCompletion(
        @Body request: MistralCompletionRequest,
        @retrofit2.http.Header("Authorization") authHeader: String
    ): Response<MistralCompletionResponse>
}



// Data Transfer Objects dla API modeli językowych
data class OpenAiCompletionRequest(
    val model: String,
    val messages: List<OpenAiMessage>,
    val temperature: Double = 0.7,
    val max_tokens: Int? = null
)

data class OpenAiMessage(
    val role: String, // "system", "user", "assistant"
    val content: String
)

data class OpenAiCompletionResponse(
    val id: String,
    val object: String,
    val created: Long,
    val model: String,
    val choices: List<OpenAiChoice>,
    val usage: OpenAiUsage
)

data class OpenAiChoice(
    val index: Int,
    val message: OpenAiMessage,
    val finish_reason: String
)

data class OpenAiUsage(
    val prompt_tokens: Int,
    val completion_tokens: Int,
    val total_tokens: Int
)

data class ClaudeCompletionRequest(
    val model: String,
    val messages: List<ClaudeMessage>,
    val max_tokens: Int = 1000,
    val temperature: Double = 0.7
)

data class ClaudeMessage(
    val role: String, // "user" or "assistant"
    val content: String
)

data class ClaudeCompletionResponse(
    val id: String,
    val type: String,
    val model: String,
    val content: List<ClaudeContent>,
    val usage: ClaudeUsage
)

data class ClaudeContent(
    val type: String, // typically "text"
    val text: String
)

data class ClaudeUsage(
    val input_tokens: Int,
    val output_tokens: Int
)

data class GeminiCompletionRequest(
    val contents: List<GeminiContent>,
    val generationConfig: GeminiGenerationConfig
)

data class GeminiContent(
    val role: String, // "user"
    val parts: List<GeminiPart>
)

data class GeminiPart(
    val text: String
)

data class GeminiGenerationConfig(
    val temperature: Double = 0.7,
    val maxOutputTokens: Int = 1000,
    val topP: Double = 0.95
)

data class GeminiCompletionResponse(
    val candidates: List<GeminiCandidate>
)

data class GeminiCandidate(
    val content: GeminiContent,
    val finishReason: String
)

data class MistralCompletionRequest(
    val model: String,
    val messages: List<MistralMessage>,
    val temperature: Double = 0.7,
    val max_tokens: Int? = null
)

data class MistralMessage(
    val role: String, // "user" or "assistant"
    val content: String
)

data class MistralCompletionResponse(
    val id: String,
    val object: String,
    val created: Long,
    val model: String,
    val choices: List<MistralChoice>,
    val usage: MistralUsage
)

data class MistralChoice(
    val index: Int,
    val message: MistralMessage,
    val finish_reason: String
)

data class MistralUsage(
    val prompt_tokens: Int,
    val completion_tokens: Int,
    val total_tokens: Int
)

// Wspólny model odpowiedzi, który ujednolica dane z różnych LLM
data class LlmResponse(
    val text: String,
    val model: String,
    val promptTokens: Int,
    val completionTokens: Int,
    val totalTokens: Int
)
