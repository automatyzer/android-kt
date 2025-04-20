package com.businessprospector.data.repository

import android.util.Log
import com.businessprospector.data.local.dao.ContactDao
import com.businessprospector.data.local.entity.ContactEntity
import com.businessprospector.data.local.entity.ContactWithMessages
import com.businessprospector.data.remote.api.GoogleSearchApi
import com.businessprospector.data.remote.dto.GoogleSearchResponse
import com.businessprospector.domain.model.Contact
import com.businessprospector.domain.model.SearchQuery
import com.businessprospector.domain.service.EncryptionService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import retrofit2.HttpException
import java.io.IOException
import java.util.Date
import javax.inject.Inject


class SearchRepository @Inject constructor(
    private val googleSearchApi: GoogleSearchApi,
    private val contactRepository: ContactRepository
) {
    private val TAG = "SearchRepository"

    suspend fun searchBusinessContacts(query: SearchQuery): Result<List<Contact>> {
        return try {
            val response = googleSearchApi.search(
                apiKey = query.apiKey,
                searchEngineId = query.searchEngineId,
                query = query.queryString,
                resultsPerPage = query.resultsPerPage,
                startIndex = query.startIndex
            )

            if (response.isSuccessful) {
                val searchResponse = response.body()
                if (searchResponse != null) {
                    // Extract contacts from search results
                    val contacts = extractContactsFromSearchResults(searchResponse, query.queryString)
                    Result.success(contacts)
                } else {
                    Result.failure(IOException("Empty response body"))
                }
            } else {
                Result.failure(HttpException(response))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error searching for contacts: ${e.message}", e)
            Result.failure(e)
        }
    }

    private suspend fun extractContactsFromSearchResults(
        searchResponse: GoogleSearchResponse,
        queryString: String
    ): List<Contact> {
        val contacts = mutableListOf<Contact>()

        searchResponse.items.forEach { item ->
            try {
                // Extract basic info from search result
                val contact = Contact(
                    id = 0, // Will be assigned by Room when inserted
                    name = extractName(item.title),
                    company = extractCompany(item.title, item.snippet),
                    website = item.link,
                    source = "Google Search",
                    sourceDetails = queryString,
                    status = "new",
                    createdAt = Date(),
                    updatedAt = Date()
                )

                // Try to fetch more details by visiting the webpage
                val enrichedContact = enrichContactFromWebpage(contact, item.link)
                contacts.add(enrichedContact)

            } catch (e: Exception) {
                Log.e(TAG, "Error processing search result: ${e.message}", e)
                // Continue with next item
            }
        }

        return contacts
    }

    private suspend fun enrichContactFromWebpage(contact: Contact, url: String): Contact {
        try {
            val doc: Document = Jsoup.connect(url)
                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36")
                .timeout(5000)
                .get()

            // Extract email addresses
            val emailPattern = "[a-zA-Z0-9_.+-]+@[a-zA-Z0-9-]+\\.[a-zA-Z0-9-.]+"
            val emailMatches = Regex(emailPattern).findAll(doc.html())
            val email = emailMatches.firstOrNull()?.value

            // Extract phone numbers (simplified pattern)
            val phonePattern = "\\+?\\d{1,4}?[-.\\s]?\\(?\\d{1,3}?\\)?[-.\\s]?\\d{1,4}[-.\\s]?\\d{1,4}[-.\\s]?\\d{1,9}"
            val phoneMatches = Regex(phonePattern).findAll(doc.html())
            val phone = phoneMatches.firstOrNull()?.value

            // Look for LinkedIn URLs
            val linkedInUrls = doc.select("a[href*=linkedin.com]").map { it.attr("href") }
            val linkedInUrl = linkedInUrls.firstOrNull()

            // Extract title from meta data or content
            val title = doc.select("meta[property=og:title]").attr("content")
                ?: doc.select("meta[name=title]").attr("content")
                ?: ""

            return contact.copy(
                email = email ?: contact.email,
                phone = phone ?: contact.phone,
                linkedInUrl = linkedInUrl ?: contact.linkedInUrl,
                title = if (title.isNotEmpty()) title else contact.title
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error enriching contact from webpage: ${e.message}", e)
            return contact
        }
    }

    private fun extractName(title: String): String {
        // Simple heuristic: take first two words if they don't include company indicators
        val companyIndicators = listOf("Inc", "LLC", "Ltd", "GmbH", "Corp", "Company", "Group")
        val words = title.split(Regex("\\s+"))

        if (words.size >= 2) {
            val potentialName = "${words[0]} ${words[1]}"
            if (companyIndicators.none { potentialName.contains(it) }) {
                return potentialName
            }
        }

        return "Unknown Person"
    }

    private fun extractCompany(title: String, snippet: String): String? {
        // Try to find company name in title
        val companyIndicators = listOf("at", "from", "with", "-", "|", "·")
        for (indicator in companyIndicators) {
            if (title.contains(indicator)) {
                val parts = title.split(indicator)
                if (parts.size > 1) {
                    return parts[1].trim()
                }
            }
        }

        // Look for company patterns in the snippet
        val companyPatterns = listOf(
            "works at ([A-Za-z0-9\\s]+)",
            "employed at ([A-Za-z0-9\\s]+)",
            "([A-Za-z0-9\\s]+) Inc",
            "([A-Za-z0-9\\s]+) LLC",
            "([A-Za-z0-9\\s]+) Ltd",
            "([A-Za-z0-9\\s]+) GmbH"
        )

        for (pattern in companyPatterns) {
            val match = Regex(pattern).find(snippet)
            if (match != null) {
                return match.groupValues[1].trim()
            }
        }

        return null
    }
}
