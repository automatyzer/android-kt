package com.businessprospector.ui.contacts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.businessprospector.data.repository.ContactRepository
import com.businessprospector.domain.model.Contact
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ContactsViewModel @Inject constructor(
    private val contactRepository: ContactRepository
) : ViewModel() {

    private val _contacts = MutableStateFlow<List<Contact>>(emptyList())
    val contacts: StateFlow<List<Contact>> = _contacts

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private var allContacts = listOf<Contact>()
    private var currentFilter: String? = null
    private var currentCategory: String? = null
    private var currentSearchQuery: String = ""

    init {
        loadContacts()
    }

    private fun loadContacts() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            contactRepository.getAllContacts()
                .catch { e ->
                    _error.value = e.message ?: "Unknown error occurred"
                    _isLoading.value = false
                }
                .collectLatest { contacts ->
                    allContacts = contacts
                    applyFilters()
                    _isLoading.value = false
                }
        }
    }

    fun searchContacts(query: String) {
        currentSearchQuery = query
        applyFilters()
    }

    fun filterContacts(status: String?) {
        currentFilter = status
        applyFilters()
    }

    fun filterContactsByCategory(category: String?) {
        currentCategory = category
        applyFilters()
    }

    private fun applyFilters() {
        var filteredContacts = allContacts

        // Filtruj według statusu
        if (!currentFilter.isNullOrBlank()) {
            filteredContacts = filteredContacts.filter { it.status == currentFilter }
        }

        // Filtruj według kategorii
        if (!currentCategory.isNullOrBlank()) {
            filteredContacts = filteredContacts.filter { it.category == currentCategory }
        }

        // Filtruj według wyszukiwanego tekstu
        if (currentSearchQuery.isNotBlank()) {
            filteredContacts = filteredContacts.filter { contact ->
                contact.name.contains(currentSearchQuery, ignoreCase = true) ||
                        (contact.company?.contains(currentSearchQuery, ignoreCase = true) ?: false) ||
                        (contact.email?.contains(currentSearchQuery, ignoreCase = true) ?: false) ||
                        (contact.phone?.contains(currentSearchQuery, ignoreCase = true) ?: false) ||
                        (contact.title?.contains(currentSearchQuery, ignoreCase = true) ?: false)
            }
        }

        _contacts.value = filteredContacts
    }

    fun refreshContacts() {
        loadContacts()
    }

    fun updateContactStatus(contactId: Long, newStatus: String) {
        viewModelScope.launch {
            try {
                contactRepository.updateContactStatus(contactId, newStatus)
                // Kontakt zostanie automatycznie zaktualizowany przez Flow
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to update contact status"
            }
        }
    }

    fun deleteContact(contact: Contact) {
        viewModelScope.launch {
            try {
                contactRepository.deleteContact(contact)
                // Kontakt zostanie automatycznie usunięty z listy przez Flow
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to delete contact"
            }
        }
    }
}