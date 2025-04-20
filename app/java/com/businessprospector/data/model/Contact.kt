
// Domain models dla kontaktów
package com.businessprospector.domain.model

import java.util.Date

data class Contact(
    val id: Long = 0,
    val name: String,
    val title: String? = null,
    val company: String? = null,
    val email: String? = null,
    val phone: String? = null,
    val website: String? = null,
    val linkedInUrl: String? = null,
    val source: String? = null,
    val sourceDetails: String? = null,
    val notes: String? = null,
    val category: String? = null,
    val status: String = "new",
    val tags: List<String> = emptyList(),
    val contextData: Map<String, String> = emptyMap(),
    val createdAt: Date? = null,
    val updatedAt: Date? = null
)

data class SearchQuery(
    val queryString: String,
    val apiKey: String,
    val searchEngineId: String,
    val resultsPerPage: Int = 10,
    val startIndex: Int = 1,
    val filters: Map<String, String> = emptyMap()
)