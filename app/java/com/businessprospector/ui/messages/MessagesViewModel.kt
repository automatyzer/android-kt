package com.businessprospector.ui.messages

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.businessprospector.data.repository.CommunicationRepository
import com.businessprospector.domain.model.Message
import com.businessprospector.domain.model.MessageTemplate
import com.businessprospector.domain.usecase.communication.GetMessageTemplatesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MessagesViewModel @Inject constructor(
    private val communicationRepository: CommunicationRepository,
    private val getMessageTemplatesUseCase: GetMessageTemplatesUseCase
) : ViewModel() {

    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages: StateFlow<List<Message>> = _messages

    private val _templates = MutableStateFlow<List<MessageTemplate>>(emptyList())
    val templates: StateFlow<List<MessageTemplate>> = _templates

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                // W prawdziwej implementacji, te dane byłyby pobierane z repozytoriów
                // Tutaj korzystamy z przykładowych danych

                // Pobieranie szablonów wiadomości
                getMessageTemplatesUseCase().catch { e ->
                    _error.value = e.message ?: "Failed to load templates"
                }.collect { templates ->
                    _templates.value = templates
                }

                // W rzeczywistej implementacji, można by również pobrać wiadomości z repozytorium
                // lub stworzyć dedykowany przypadek użycia

                // Tu używamy przykładowych danych dla uproszczenia
                _messages.value = getSampleMessages()

                _isLoading.value = false
            } catch (e: Exception) {
                _error.value = e.message ?: "Unknown error occurred"
                _isLoading.value = false
            }
        }
    }

    // Ta funkcja jest tylko do celów demonstracyjnych
    // W rzeczywistej aplikacji dane byłyby pobierane z bazy danych
    private fun getSampleMessages(): List<Message> {
        val now = System.currentTimeMillis()
        val day = 24 * 60 * 60 * 1000L // milisekundy w jednym dniu

        return listOf(
            Message(
                id = 1,
                contactId = 1,
                type = "email",
                direction = "outgoing",
                content = "Hello, I would like to discuss potential collaboration opportunities with your company.",
                subject = "Collaboration Opportunity",
                status = "sent",
                sentAt = java.util.Date(now - day),
                createdAt = java.util.Date(now - day)
            ),
            Message(
                id = 2,
                contactId = 1,
                type = "email",
                direction = "incoming",
                content = "Thank you for reaching out. I would be happy to discuss this further. Could we schedule a call next week?",
                subject = "Re: Collaboration Opportunity",
                status = "received",
                sentAt = java.util.Date(now - day / 2),
                createdAt = java.util.Date(now - day / 2)
            ),
            Message(
                id = 3,
                contactId = 2,
                type = "sms",
                direction = "outgoing",
                content = "Hello, I'm following up on our discussion from last week. Are you available for a quick call tomorrow?",
                status = "sent",
                sentAt = java.util.Date(now - 2 * day),
                createdAt = java.util.Date(now - 2 * day)
            ),
            Message(
                id = 4,
                contactId = 3,
                type = "call",
                direction = "outgoing",
                content = "Introduction call to discuss project requirements and timeline.",
                status = "completed",
                sentAt = java.util.Date(now - 3 * day),
                createdAt = java.util.Date(now - 3 * day)
            )
        )
    }

    fun refreshData() {
        loadData()
    }
}