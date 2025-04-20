package com.businessprospector.domain.usecase.search

import com.businessprospector.data.repository.SearchRepository
import com.businessprospector.domain.model.Contact
import com.businessprospector.domain.model.SearchQuery
import javax.inject.Inject

class SearchContactsUseCase @Inject constructor(
    private val searchRepository: SearchRepository
) {
    suspend operator fun invoke(
        queryString: String,
        apiKey: String,
        searchEngineId: String,
        resultsPerPage: Int = 10,
        startIndex: Int = 1
    ): Result<List<Contact>> {
        val searchQuery = SearchQuery(
            queryString = queryString,
            apiKey = apiKey,
            searchEngineId = searchEngineId,
            resultsPerPage = resultsPerPage,
            startIndex = startIndex
        )

        return searchRepository.searchBusinessContacts(searchQuery)
    }
}

class FilterContactsUseCase @Inject constructor() {
    operator fun invoke(
        contacts: List<Contact>,
        filters: Map<String, String>
    ): List<Contact> {
        var filteredContacts = contacts

        // Filtrowanie według statusu
        filters["status"]?.let { status ->
            filteredContacts = filteredContacts.filter { it.status == status }
        }

        // Filtrowanie według kategorii
        filters["category"]?.let { category ->
            filteredContacts = filteredContacts.filter { it.category == category }
        }

        // Filtrowanie według sourcee
        filters["source"]?.let { source ->
            filteredContacts = filteredContacts.filter { it.source == source }
        }

        // Filtrowanie według tagów
        filters["tag"]?.let { tag ->
            filteredContacts = filteredContacts.filter { it.tags.contains(tag) }
        }

        // Filtrowanie po dacie utworzenia - od
        filters["created_from"]?.let { fromDate ->
            val date = java.text.SimpleDateFormat("yyyy-MM-dd").parse(fromDate)
            filteredContacts = filteredContacts.filter { it.createdAt?.after(date) ?: false }
        }

        // Filtrowanie po dacie utworzenia - do
        filters["created_to"]?.let { toDate ->
            val date = java.text.SimpleDateFormat("yyyy-MM-dd").parse(toDate)
            filteredContacts = filteredContacts.filter { it.createdAt?.before(date) ?: false }
        }

        return filteredContacts
    }
}

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
            systemPrompt = "You are a business contact evaluator. Your task is to categorize business contacts based on their potential value."
        )

        // Uzyskaj kategoryzację z LLM
        val result = llmRepository.generateContent(prompt, llmConfig)

        if (result.isSuccess) {
            val category = result.getOrNull()?.text?.trim()?.lowercase() ?: "medium_potential"

            // Upewnij się, że kategoria jest jedną z oczekiwanych wartości
            val validCategory = when (category) {
                "high_potential", "medium_potential", "low_potential" -> category
                else -> "medium_potential" // Domyślna kategoria w przypadku nieprawidłowej odpowiedzi
            }

            // Zaktualizuj kategorię kontaktu w bazie danych
            contactRepository.updateContact(contact.copy(
                category = validCategory,
                updatedAt = Date()
            ))

            return Result.success(validCategory)
        }

        return Result.failure(Exception("Failed to categorize contact"))
    }
}Prompt = "You are a business intelligence assistant specializing in B2B contact analysis."
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

class CategorizeContactsUseCase @Inject constructor(
    private val llmRepository: LlmRepository,
    private val contactRepository: ContactRepository
) {
    suspend operator fun invoke(contact: Contact, llmConfig: LlmConfig): Result<String> {
        // Utwórz prompt do kategoryzacji kontaktu
        val promptBuilder = StringBuilder()
        promptBuilder.append("Evaluate this business contact's potential value based on the information provided.\n\n")
        promptBuilder.append("CONTACT INFORMATION:\n")
        promptBuilder.append("Name: ${contact.name}\n")
        contact.title?.let { promptBuilder.append("Title: $it\n") }
        contact.company?.let { promptBuilder.append("Company: $it\n") }
        contact.email?.let { promptBuilder.append("Email: $it\n") }
        contact.website?.let { promptBuilder.append("Website: $it\n") }

        // Dodaj dodatkowe informacje kontekstowe, jeśli są dostępne
        if (contact.contextData.isNotEmpty()) {
            promptBuilder.append("\nCONTEXT DATA:\n")
            contact.contextData.forEach { (key, value) ->
                promptBuilder.append("$key: $value\n")
            }
        }

        promptBuilder.append("\nCategorize this contact into one of the following categories:\n")
        promptBuilder.append("1. high_potential: Decision makers or influencers in target companies\n")
        promptBuilder.append("2. medium_potential: Relevant contacts but not decision makers\n")
        promptBuilder.append("3. low_potential: Not relevant for business purposes\n")

        promptBuilder.append("\nReturn only the category name (high_potential, medium_potential, or low_potential) without any additional text.")

        val prompt = LlmPrompt(
            userPrompt = promptBuilder.toString(),
            system