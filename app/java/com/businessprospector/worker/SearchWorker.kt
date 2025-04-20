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
class SearchWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val searchRepository: SearchRepository,
    private val contactRepository: ContactRepository
) : CoroutineWorker(context, params) {

    companion object {
        private const val TAG = "SearchWorker"
        const val KEY_QUERY = "query"
        const val KEY_API_KEY = "api_key"
        const val KEY_SEARCH_ENGINE_ID = "search_engine_id"
        const val KEY_RESULTS_PER_PAGE = "results_per_page"
        const val KEY_CONTACTS_FOUND = "contacts_found"
    }

    override suspend fun doWork(): Result {
        Log.d(TAG, "Starting search worker")

        val queryString = inputData.getString(KEY_QUERY) ?: return Result.failure(
            createErrorData("Query string is required")
        )

        val apiKey = inputData.getString(KEY_API_KEY) ?: return Result.failure(
            createErrorData("API key is required")
        )

        val searchEngineId = inputData.getString(KEY_SEARCH_ENGINE_ID) ?: return Result.failure(
            createErrorData("Search engine ID is required")
        )

        val resultsPerPage = inputData.getInt(KEY_RESULTS_PER_PAGE, 10)

        val searchQuery = SearchQuery(
            queryString = queryString,
            apiKey = apiKey,
            searchEngineId = searchEngineId,
            resultsPerPage = resultsPerPage
        )

        return withContext(Dispatchers.IO) {
            try {
                val searchResult = searchRepository.searchBusinessContacts(searchQuery)

                if (searchResult.isSuccess) {
                    val contacts = searchResult.getOrNull() ?: emptyList()

                    // Zapisz znalezione kontakty w bazie danych
                    val savedIds = contacts.map { contact ->
                        contactRepository.insertContact(contact)
                    }

                    val outputData = Data.Builder()
                        .putInt(KEY_CONTACTS_FOUND, savedIds.size)
                        .putLongArray("contact_ids", savedIds.toLongArray())
                        .build()

                    Log.d(TAG, "Search completed successfully. Found ${savedIds.size} contacts.")
                    Result.success(outputData)
                } else {
                    val exception = searchResult.exceptionOrNull()
                    Log.e(TAG, "Search failed: ${exception?.message}", exception)
                    Result.failure(createErrorData(exception?.message ?: "Unknown search error"))
                }
            } catch (e: Exception) {
                Log.e(TAG, "Exception in search worker: ${e.message}", e)
                Result.failure(createErrorData(e.message ?: "Unknown error"))
            }
        }
    }

    private fun createErrorData(errorMessage: String): Data {
        return Data.Builder()
            .putString("error", errorMessage)
            .build()
    }
}
