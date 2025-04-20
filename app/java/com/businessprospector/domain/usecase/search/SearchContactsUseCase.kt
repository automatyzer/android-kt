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