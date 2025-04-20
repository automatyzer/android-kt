package com.businessprospector.ui.contacts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.businessprospector.data.repository.ContactRepository
import com.businessprospector.data.repository.CommunicationRepository
import com.businessprospector.domain.model.Contact
import com.businessprospector.domain.model.Message
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ContactDetailViewModel @Inject constructor(
    private val contactRepository: ContactRepository,
    private val communicationRepository: CommunicationRepository
) : ViewModel() {

    private val _contactState = MutableStateFlow<Contact?>(null)
    val contactState: StateFlow<Contact?> = _contactState

    private val _messagesState = MutableStateFlow<List<Message>>(emptyList())
    val messagesState: StateFlow<List<Message>> = _messagesState

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    fun loadContact(contactId: Long) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                // Załaduj dane kontaktu
                val contact = contactRepository.getContactById(contactId)
                if (contact != null) {
                    _contactState.value = contact

                    // Załaduj historię wiadomości
                    loadMessages(contactId)
                } else {
                    _error.value = "Contact not found"
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to load contact"
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun loadMessages(contactId: Long) {
        viewModelScope.launch {
            try {
                communicationRepository.getMessagesForContact(contactId)
                    .catch { e ->
                        _error.value = e.message ?: "Failed to load messages"
                    }
                    .collectLatest { messages ->
                        _messagesState.value = messages
                    }
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to load messages"
            }
        }
    }

    fun updateContactStatus(newStatus: String) {
        val contactId = _contactState.value?.id ?: return

        viewModelScope.launch {
            try {
                contactRepository.updateContactStatus(contactId, newStatus)

                // Odśwież dane kontaktu
                val updatedContact = contactRepository.getContactById(contactId)
                _contactState.value = updatedContact
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to update contact status"
            }
        }
    }

    fun deleteContact() {
        val contact = _contactState.value ?: return

        viewModelScope.launch {
            try {
                contactRepository.deleteContact(contact)
                // Nie trzeba nic robić - nawigacja do poprzedniego ekranu jest obsługiwana w UI
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to delete contact"
            }
        }
    }

    fun sendEmail(subject: String, content: String) {
        val contact = _contactState.value ?: return

        viewModelScope.launch {
            try {
                communicationRepository.sendEmail(contact, subject, content)
                // Odśwież historię wiadomości
                loadMessages(contact.id)
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to send email"
            }
        }
    }

    fun sendSms(content: String) {
        val contact = _contactState.value ?: return

        viewModelScope.launch {
            try {
                communicationRepository.sendSms(contact, content)
                // Odśwież historię wiadomości
                loadMessages(contact.id)
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to send SMS"
            }
        }
    }

    fun makeCall(script: String) {
        val contact = _contactState.value ?: return

        viewModelScope.launch {
            try {
                communicationRepository.makeCall(contact, script)
                // Odśwież historię wiadomości
                loadMessages(contact.id)
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to prepare call"
            }
        }
    }
}