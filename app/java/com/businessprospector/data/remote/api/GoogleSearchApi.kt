package com.businessprospector.data.remote.api

import com.businessprospector.data.remote.dto.GoogleSearchResponse
import com.businessprospector.data.remote.dto.LlmResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Query

interface GoogleSearchApi {
    @GET("customsearch/v1")
    suspend fun search(
        @Query("key") apiKey: String,
        @Query("cx") searchEngineId: String,
        @Query("q") query: String,
        @Query("num") resultsPerPage: Int = 10,
        @Query("start") startIndex: Int = 1
    ): Response<GoogleSearchResponse>
}

// Data Transfer Objects dla Google Search API
package com.businessprospector.data.remote.dto

data class GoogleSearchResponse(
    val kind: String,
    val url: SearchUrl,
    val queries: Queries,
    val context: Context,
    val searchInformation: SearchInformation,
    val items: List<SearchItem>
)

data class SearchUrl(
    val type: String,
    val template: String
)

data class Queries(
    val request: List<QueryRequest>,
    val nextPage: List<QueryRequest>?
)

data class QueryRequest(
    val title: String,
    val totalResults: String,
    val searchTerms: String,
    val count: Int,
    val startIndex: Int,
    val inputEncoding: String,
    val outputEncoding: String,
    val safe: String,
    val cx: String
)

data class Context(
    val title: String
)

data class SearchInformation(
    val searchTime: Double,
    val formattedSearchTime: String,
    val totalResults: String,
    val formattedTotalResults: String
)

data class SearchItem(
    val kind: String,
    val title: String,
    val htmlTitle: String,
    val link: String,
    val displayLink: String,
    val snippet: String,
    val htmlSnippet: String,
    val cacheId: String?,
    val formattedUrl: String,
    val htmlFormattedUrl: String,
    val pagemap: PageMap?
)

data class PageMap(
    val cse_thumbnail: List<CseThumbnail>?,
    val metatags: List<Map<String, String>>?,
    val cse_image: List<CseImage>?,
    val person: List<Person>?,
    val organization: List<Organization>?
)

data class CseThumbnail(
    val src: String,
    val width: String,
    val height: String
)

data class CseImage(
    val src: String
)

data class Person(
    val name: String?,
    val jobtitle: String?,
    val email: String?,
    val telephone: String?,
    val url: String?
)

data class Organization(
    val name: String?,
    val url: String?,
    val telephone: String?,
    val email: String?
)
