package com.businessprospector.worker

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.WorkerParameters
import com.businessprospector.data.repository.CommunicationRepository
import com.businessprospector.data.repository.ContactRepository
import com.businessprospector.data.repository.LlmRepository
import com.businessprospector.data.repository.SearchRepository
import com.businessprospector.domain.model.Contact
import com.businessprospector.domain.model.LlmConfig
import com.businessprospector.domain.model.LlmPrompt
import com.businessprospector.domain.model.SearchQuery
import com.businessprospector.domain.usecase.communication.ExecuteSequenceUseCase
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Date


@HiltWorker
class AnalysisWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val contactRepository: ContactRepository,
    private val llmRepository: LlmRepository
) : CoroutineWorker(context, params) {

    companion object {
        private const val TAG = "AnalysisWorker"
        const val KEY_CONTACT_IDS = "contact_ids"
        const val KEY_LLM_PROVIDER = "llm_provider"
        const val KEY_LLM_MODEL = "llm_model"
        const val KEY_LLM_API_KEY = "llm_api_key"
        const val KEY_CONTACTS_ANALYZED = "contacts_analyzed"
    }

    override suspend fun doWork(): Result {
        Log.d(TAG, "Starting analysis worker")

        val contactIds = inputData.getLongArray(KEY_CONTACT_IDS) ?: return Result.failure(
            createErrorData("Contact IDs are required")
        )

        val llmProvider = inputData.getString(KEY_LLM_PROVIDER) ?: return Result.failure(
            createErrorData("LLM provider is required")
        )

        val llmModel = inputData.getString(KEY_LLM_MODEL) ?: return Result.failure(
            createErrorData("LLM model is required")
        )

        val llmApiKey = inputData.getString(KEY_LLM_API_KEY) ?: return Result.failure(
            createErrorData("LLM API key is required")
        )

        val llmConfig = LlmConfig(
            provider = llmProvider,
            model = llmModel,
            apiKey = llmApiKey,
            temperature = 0.7
        )

        return withContext(Dispatchers.IO) {
            try {
                var analyzedCount = 0

                // Analizuj każdy kontakt
                for (contactId in contactIds) {
                    val contact = contactRepository.getContactById(contactId) ?: continue

                    // Kategoryzuj kontakt na podstawie dostępnych informacji
                    val updatedContact = categorizeContact(contact, llmConfig)

                    // Zaktualizuj kontakt w bazie danych
                    contactRepository.updateContact(updatedContact)
                    analyzedCount++
                }

                val outputData = Data.Builder()
                    .putInt(KEY_CONTACTS_ANALYZED, analyzedCount)
                    .build()

                Log.d(TAG, "Analysis completed successfully. Analyzed $analyzedCount contacts.")
                Result.success(outputData)
            } catch (e: Exception) {
                Log.e(TAG, "Exception in analysis worker: ${e.message}", e)
                Result.failure(createErrorData(e.message ?: "Unknown error"))
            }
        }
    }

    private suspend fun categorizeContact(contact: Contact, llmConfig: LlmConfig): Contact {
        // Stwórz prompt do kategoryzacji kontaktu
        val promptBuilder = StringBuilder()
        promptBuilder.append("Evaluate this business contact's potential value based on the information provided.\n\n")
        promptBuilder.append("CONTACT INFORMATION:\n")
        promptBuilder.append("Name: ${contact.name}\n")
        contact.title?.let { promptBuilder.append("Title: $it\n") }
        contact.company?.let { promptBuilder.append("Company: $it\n") }
        contact.email?.let { promptBuilder.append("Email: $it\n") }
        contact.phone?.let { promptBuilder.append("Phone: $it\n") }
        contact.website?.let { promptBuilder.append("Website: $it\n") }
        contact.linkedInUrl?.let { promptBuilder.append("LinkedIn: $it\n") }

        promptBuilder.append("\nCategorize this contact into one of the following categories:\n")
        promptBuilder.append("1. high_potential: Decision makers or influencers in target companies\n")
        promptBuilder.append("2. medium_potential: Relevant contacts but not decision makers\n")
        promptBuilder.append("3. low_potential: Not relevant for business purposes\n")

        promptBuilder.append("\nReturn only the category name (high_potential, medium_potential, or low_potential) without any additional text.")

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

            return contact.copy(
                category = validCategory,
                updatedAt = Date()
            )
        }

        // W przypadku błędu, pozostaw kategorię bez zmian
        return contact
    }

    private fun createErrorData(errorMessage: String): Data {
        return Data.Builder()
            .putString("error", errorMessage)
            .build()
    }
}
