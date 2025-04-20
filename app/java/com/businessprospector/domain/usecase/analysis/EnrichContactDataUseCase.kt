package com.businessprospector.domain.usecase.analysis

import com.businessprospector.data.repository.ContactRepository
import com.businessprospector.data.repository.LlmRepository
import com.businessprospector.domain.model.Contact
import com.businessprospector.domain.model.LlmConfig
import com.businessprospector.domain.model.LlmPrompt
import java.util.Date
import javax.inject.Inject

class EnrichContactDataUseCase @Inject constructor(
    private val contactRepository: ContactRepository,
    private val llmRepository: LlmRepository
) {
    suspend operator fun invoke(contact: Contact, llmConfig: LlmConfig): Result<Contact> {
        // Jeśli kontakt ma już wystarczająco dużo informacji, może nie być potrzeby wzbogacania
        if (!needsEnrichment(contact)) {
            return Result.success(contact)
        }

        // Utwórz prompt do LLM w celu analizy i wzbogacenia danych
        val promptBuilder = StringBuilder()
        promptBuilder.append("Analyze this business contact and suggest likely additional information based on available data.\n\n")
        promptBuilder.append("CONTACT INFORMATION:\n")
        promptBuilder.append("Name: ${contact.name}\n")
        contact.title?.let { promptBuilder.append("Title: $it\n") }
        contact.company?.let { promptBuilder.append("Company: $it\n") }
        contact.email?.let { promptBuilder.append("Email: $it\n") }
        contact.phone?.let { promptBuilder.append("Phone: $it\n") }
        contact.website?.let { promptBuilder.append("Website: $it\n") }
        contact.linkedInUrl?.let { promptBuilder.append("LinkedIn: $it\n") }

        promptBuilder.append("\nPlease provide the following information in JSON format:\n")
        promptBuilder.append("1. Likely industry sector\n")
        promptBuilder.append("2. Company size estimate (small, medium, large, enterprise)\n")
        promptBuilder.append("3. Decision-making level (low, medium, high)\n")
        promptBuilder.append("4. Suggested tags (comma-separated)\n")
        promptBuilder.append("5. Potential business needs\n\n")

        promptBuilder.append("Example format:\n")
        promptBuilder.append("""
            {
              "industry": "Technology",
              "companySize": "large",
              "decisionLevel": "high",
              "tags": ["software", "b2b", "enterprise"],
              "businessNeeds": "Likely interested in scaling operations and automation solutions."
            }
        """.trimIndent())

        val prompt = LlmPrompt(
            userPrompt = promptBuilder.toString(),
            systemPrompt = "You are a business intelligence assistant specializing in B2B contact analysis."
        )

        // Uzyskaj analizę z LLM
        val result = llmRepository.generateContent(prompt, llmConfig)

        if (result.isSuccess) {
            val analysisText = result.getOrNull()?.text ?: return Result.success(contact)

            try {
                // Znajdź JSON w tekście odpowiedzi
                val jsonPattern = """\{.*\}"""
                val regex = Regex(jsonPattern, RegexOption.DOT_MATCHES_ALL)
                val matchResult = regex.find(analysisText)
                val jsonStr = matchResult?.value ?: return Result.success(contact)

                // Parsuj JSON
                val gson = com.google.gson.Gson()
                val analysis = gson.fromJson(jsonStr, EnrichmentAnalysis::class.java)

                // Wzbogać dane kontaktu
                val contextData = contact.contextData.toMutableMap()
                contextData["industry"] = analysis.industry
                contextData["companySize"] = analysis.companySize
                contextData["decisionLevel"] = analysis.decisionLevel
                contextData["businessNeeds"] = analysis.businessNeeds

                val tags = (contact.tags + analysis.tags).distinct()

                val enrichedContact = contact.copy(
                    tags = tags,
                    contextData = contextData,
                    updatedAt = Date()
                )

                // Zapisz zaktualizowany kontakt w bazie danych
                contactRepository.updateContact(enrichedContact)

                return Result.success(enrichedContact)
            } catch (e: Exception) {
                return Result.success(contact) // W przypadku błędu, zwróć oryginalny kontakt
            }
        }

        return Result.success(contact)
    }

    private fun needsEnrichment(contact: Contact): Boolean {
        // Sprawdź, czy kontakt ma już wzbogacone dane
        if (contact.contextData.containsKey("industry") &&
            contact.contextData.containsKey("companySize") &&
            contact.tags.isNotEmpty()) {
            return false
        }

        return true
    }

    private data class EnrichmentAnalysis(
        val industry: String,
        val companySize: String,
        val decisionLevel: String,
        val tags: List<String>,
        val businessNeeds: String
    )
}
