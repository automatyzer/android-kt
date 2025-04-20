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
class CommunicationWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val communicationRepository: CommunicationRepository,
    private val contactRepository: ContactRepository,
    private val llmRepository: LlmRepository,
    private val executeSequenceUseCase: ExecuteSequenceUseCase
) : CoroutineWorker(context, params) {

    companion object {
        private const val TAG = "CommunicationWorker"
        const val KEY_ACTION = "action"
        const val KEY_CONTACT_ID = "contact_id"
        const val KEY_SEQUENCE_ID = "sequence_id"
        const val KEY_MESSAGE_ID = "message_id"
        const val KEY_MESSAGE_TYPE = "message_type"
        const val KEY_SUBJECT = "subject"
        const val KEY_CONTENT = "content"
        const val KEY_LLM_PROVIDER = "llm_provider"
        const val KEY_LLM_MODEL = "llm_model"
        const val KEY_LLM_API_KEY = "llm_api_key"

        // Typy akcji
        const val ACTION_SEND_EMAIL = "send_email"
        const val ACTION_SEND_SMS = "send_sms"
        const val ACTION_MAKE_CALL = "make_call"
        const val ACTION_EXECUTE_SEQUENCE = "execute_sequence"
        const val ACTION_CHECK_PENDING = "check_pending"
    }

    override suspend fun doWork(): Result {
        Log.d(TAG, "Starting communication worker")

        val action = inputData.getString(KEY_ACTION) ?: return Result.failure(
            createErrorData("Action is required")
        )

        return withContext(Dispatchers.IO) {
            try {
                when (action) {
                    ACTION_SEND_EMAIL -> sendEmail()
                    ACTION_SEND_SMS -> sendSms()
                    ACTION_MAKE_CALL -> makeCall()
                    ACTION_EXECUTE_SEQUENCE -> executeSequence()
                    ACTION_CHECK_PENDING -> checkPendingMessages()
                    else -> Result.failure(createErrorData("Unknown action: $action"))
                }
            } catch (e: Exception) {
                Log.e(TAG, "Exception in communication worker: ${e.message}", e)
                Result.failure(createErrorData(e.message ?: "Unknown error"))
            }
        }
    }

    private suspend fun sendEmail(): Result {
        val contactId = inputData.getLong(KEY_CONTACT_ID, -1)
        if (contactId == -1L) return Result.failure(createErrorData("Contact ID is required"))

        val subject = inputData.getString(KEY_SUBJECT) ?: return Result.failure(
            createErrorData("Subject is required")
        )

        val content = inputData.getString(KEY_CONTENT) ?: return Result.failure(
            createErrorData("Content is required")
        )

        val contact = contactRepository.getContactById(contactId) ?: return Result.failure(
            createErrorData("Contact not found")
        )

        val result = communicationRepository.sendEmail(contact, subject, content)

        return if (result.isSuccess) {
            val message = result.getOrNull()
            val outputData = Data.Builder()
                .putLong(KEY_MESSAGE_ID, message?.id ?: -1)
                .build()
            Result.success(outputData)
        } else {
            val exception = result.exceptionOrNull()
            Result.failure(createErrorData(exception?.message ?: "Failed to send email"))
        }
    }

    private suspend fun sendSms(): Result {
        val contactId = inputData.getLong(KEY_CONTACT_ID, -1)
        if (contactId == -1L) return Result.failure(createErrorData("Contact ID is required"))

        val content = inputData.getString(KEY_CONTENT) ?: return Result.failure(
            createErrorData("Content is required")
        )

        val contact = contactRepository.getContactById(contactId) ?: return Result.failure(
            createErrorData("Contact not found")
        )

        val result = communicationRepository.sendSms(contact, content)

        return if (result.isSuccess) {
            val message = result.getOrNull()
            val outputData = Data.Builder()
                .putLong(KEY_MESSAGE_ID, message?.id ?: -1)
                .build()
            Result.success(outputData)
        } else {
            val exception = result.exceptionOrNull()
            Result.failure(createErrorData(exception?.message ?: "Failed to send SMS"))
        }
    }

    private suspend fun makeCall(): Result {
        val contactId = inputData.getLong(KEY_CONTACT_ID, -1)
        if (contactId == -1L) return Result.failure(createErrorData("Contact ID is required"))

        val script = inputData.getString(KEY_CONTENT) ?: return Result.failure(
            createErrorData("Call script is required")
        )

        val contact = contactRepository.getContactById(contactId) ?: return Result.failure(
            createErrorData("Contact not found")
        )

        val result = communicationRepository.makeCall(contact, script)

        return if (result.isSuccess) {
            val message = result.getOrNull()
            val outputData = Data.Builder()
                .putLong(KEY_MESSAGE_ID, message?.id ?: -1)
                .build()
            Result.success(outputData)
        } else {
            val exception = result.exceptionOrNull()
            Result.failure(createErrorData(exception?.message ?: "Failed to prepare call"))
        }
    }

    private suspend fun executeSequence(): Result {
        val sequenceId = inputData.getLong(KEY_SEQUENCE_ID, -1)
        if (sequenceId == -1L) return Result.failure(createErrorData("Sequence ID is required"))

        val contactId = inputData.getLong(KEY_CONTACT_ID, -1)
        if (contactId == -1L) return Result.failure(createErrorData("Contact ID is required"))

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

        val result = executeSequenceUseCase.execute(sequenceId, contactId, llmConfig)

        return if (result.isSuccess) {
            val steps = result.getOrNull() ?: 0
            val outputData = Data.Builder()
                .putInt("steps_executed", steps)
                .build()
            Result.success(outputData)
        } else {
            val exception = result.exceptionOrNull()
            Result.failure(createErrorData(exception?.message ?: "Failed to execute sequence"))
        }
    }

    private suspend fun checkPendingMessages(): Result {
        val pendingMessages = communicationRepository.getPendingMessages()

        var sentCount = 0
        for (message in pendingMessages) {
            val contact = contactRepository.getContactById(message.contactId) ?: continue

            val result = when (message.type) {
                "email" -> communicationRepository.sendEmail(contact, message.subject ?: "", message.content)
                "sms" -> communicationRepository.sendSms(contact, message.content)
                else -> continue
            }

            if (result.isSuccess) {
                sentCount++
            }
        }

        val outputData = Data.Builder()
            .putInt("messages_sent", sentCount)
            .build()

        return Result.success(outputData)
    }

    private fun createErrorData(errorMessage: String): Data {
        return Data.Builder()
            .putString("error", errorMessage)
            .build()
    }
}
