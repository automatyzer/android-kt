// Domain model dla sekwencji
package com.businessprospector.domain.model

import java.util.Date

data class Sequence(
    val id: Long = 0,
    val name: String,
    val description: String? = null,
    val isActive: Boolean = true,
    val steps: List<SequenceStep> = emptyList(),
    val createdAt: Date = Date(),
    val updatedAt: Date = Date()
)

data class SequenceStep(
    val id: Long = 0,
    val sequenceId: Long,
    val type: String, // "email", "sms", "call"
    val templateId: Long? = null,
    val order: Int,
    val delayDays: Int = 0,
    val delayHours: Int = 0,
    val condition: String? = null,
    val isActive: Boolean = true,
    val createdAt: Date = Date(),
    val updatedAt: Date = Date()
)

// Domain model dla konfiguracji LLM
data class LlmConfig(
    val provider: String, // "openai", "claude", "gemini", "mistral"
    val model: String,
    val apiKey: String,
    val temperature: Double = 0.7,
    val maxTokens: Int? = null
)

data class LlmPrompt(
    val userPrompt: String,
    val systemPrompt: String? = null
)

data class LlmResult(
    val text: String,
    val provider: String,
    val model: String,
    val promptTokens: Int,
    val completionTokens: Int,
    val totalTokens: Int,
    val finishReason: String? = null
)



sealed class SearchState {
    object Initial : SearchState()
    object Loading : SearchState()
    data class Success(val contacts: List<Contact>) : SearchState()
    data class Error(val message: String) : SearchState()
}