package com.businessprospector.domain.usecase.analysis

import com.businessprospector.data.repository.ContactRepository
import com.businessprospector.data.repository.LlmRepository
import com.businessprospector.domain.model.Contact
import com.businessprospector.domain.model.LlmConfig
import com.businessprospector.domain.model.LlmPrompt
import java.util.Date
import javax.inject.Inject

class GenerateMessageUseCase @Inject constructor(
    private val llmRepository: LlmRepository
) {
    suspend operator fun invoke(
        contact: Contact,
        messageType: String,
        purpose: String,
        additionalInstructions: String?,
        llmConfig: LlmConfig
    ): Result<String> {
        // Utwórz prompt do wygenerowania wiadomości
        val promptBuilder = StringBuilder()

        promptBuilder.append("Generate a personalized business ${messageType.lowercase()} for the following contact.\n\n")
        promptBuilder.append("CONTACT INFORMATION:\n")
        promptBuilder.append("Name: ${contact.name}\n")
        contact.title?.let { promptBuilder.append("Title: $it\n") }
        contact.company?.let { promptBuilder.append("Company: $it\n") }
        contact.website?.let { promptBuilder.append("Website: $it\n") }

        // Dodaj dodatkowe informacje kontekstowe, jeśli są dostępne
        if (contact.contextData.isNotEmpty()) {
            promptBuilder.append("\nCONTEXT DATA:\n")
            contact.contextData.forEach { (key, value) ->
                promptBuilder.append("$key: $value\n")
            }
        }

        promptBuilder.append("\nPURPOSE OF MESSAGE:\n")
        promptBuilder.append("$purpose\n")

        additionalInstructions?.let {
            promptBuilder.append("\nADDITIONAL INSTRUCTIONS:\n")
            promptBuilder.append("$it\n")
        }

        // Dostosuj instrukcje w zależności od typu wiadomości
        when (messageType.lowercase()) {
            "email" -> {
                promptBuilder.append("\nPlease write a professional business email with subject line and body.")
                promptBuilder.append("\nFormat: Include 'Subject:' on the first line, followed by the email body.")
            }
            "sms" -> {
                promptBuilder.append("\nPlease write a concise SMS message (max 160 characters).")
            }
            "call_script" -> {
                promptBuilder.append("\nPlease write a natural-sounding call script with introduction, key points, and closing.")
            }
        }

        val prompt = LlmPrompt(
            userPrompt = promptBuilder.toString(),
            systemPrompt = "You are a professional business communication expert who crafts personalized messages."
        )

        // Uzyskaj wygenerowaną wiadomość z LLM
        val result = llmRepository.generateContent(prompt, llmConfig)

        return result.map { it.text }
    }
}