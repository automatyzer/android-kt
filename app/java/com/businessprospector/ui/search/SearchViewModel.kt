package com.businessprospector.ui.search

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.businessprospector.domain.model.Contact
import com.businessprospector.domain.model.SearchState
import com.businessprospector.domain.usecase.search.FilterContactsUseCase
import com.businessprospector.domain.usecase.search.SearchContactsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val searchContactsUseCase: SearchContactsUseCase,
    private val filterContactsUseCase: FilterContactsUseCase
) : ViewModel() {

    // Stan wyszukiwania
    private val _searchState = MutableStateFlow<SearchState>(SearchState.Initial)
    val searchState: StateFlow<SearchState> = _searchState

    // Zapytanie wyszukiwania
    val searchQuery = mutableStateOf("")

    // Opcje wyszukiwania
    val searchOptions = mutableStateOf(SearchOptions())

    // Wyniki wyszukiwania
    private var searchResults = listOf<Contact>()

    // Funkcja wykonująca wyszukiwanie
    fun performSearch() {
        // Sprawdź, czy zapytanie nie jest puste
        if (searchQuery.value.isBlank()) {
            _searchState.value = SearchState.Error("Search query cannot be empty")
            return
        }

        // Sprawdź, czy klucze API są skonfigurowane
        if (searchOptions.value.googleApiKey.isBlank() || searchOptions.value.searchEngineId.isBlank()) {
            _searchState.value = SearchState.Error("API Key and Search Engine ID must be configured")
            return
        }

        _searchState.value = SearchState.Loading

        viewModelScope.launch {
            try {
                val result = searchContactsUseCase(
                    queryString = searchQuery.value,
                    apiKey = searchOptions.value.googleApiKey,
                    searchEngineId = searchOptions.value.searchEngineId,
                    resultsPerPage = searchOptions.value.resultsPerPage
                )

                if (result.isSuccess) {
                    searchResults = result.getOrNull() ?: emptyList()

                    // Zastosuj filtry, jeśli są dostępne
                    val filteredResults = if (searchOptions.value.filterOptions.isNotEmpty()) {
                        filterContactsUseCase(searchResults, searchOptions.value.filterOptions)
                    } else {
                        searchResults
                    }

                    _searchState.value = SearchState.Success(filteredResults)
                } else {
                    val exception = result.exceptionOrNull()
                    _searchState.value = SearchState.Error(exception?.message ?: "Unknown error occurred")
                }
            } catch (e: Exception) {
                _searchState.value = SearchState.Error(e.message ?: "Unknown error occurred")
            }
        }
    }

    // Funkcja do filtrowania wyników
    fun applyFilters(filterOptions: Map<String, String>) {
        if (searchResults.isEmpty()) return

        searchOptions.value = searchOptions.value.copy(filterOptions = filterOptions)

        val filteredResults = filterContactsUseCase(searchResults, filterOptions)
        _searchState.value = SearchState.Success(filteredResults)
    }
}
