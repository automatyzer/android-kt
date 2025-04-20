package com.businessprospector.data.remote.api

import com.businessprospector.data.remote.dto.GoogleSearchResponse
import com.businessprospector.data.remote.dto.LlmResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Query

// Interfejs API dla usług komunikacyjnych (email, SMS)
interface EmailSmsApi {
    // Można zaimplementować interfejsy dla usług jak SendGrid, Twilio, itp.
    // lub wykorzystać lokalne implementacje
}