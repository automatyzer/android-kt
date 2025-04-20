package com.businessprospector.domain.usecase.analytics

import com.businessprospector.data.repository.AnalyticsRepository
import com.businessprospector.data.repository.ContactRepository
import com.businessprospector.domain.model.Contact
import com.businessprospector.domain.model.Message
import java.util.Date
import javax.inject.Inject

/**
 * Przypadek użycia odpowiedzialny za śledzenie odpowiedzi od kontaktów.
 * Gdy kontakt odpowie na wiadomość, ten przypadek użycia rejestruje zdarzenie,
 * aktualizuje status kontaktu i zapisuje odpowiednie metryki analityczne.
 */
class TrackResponseUseCase @Inject constructor(
    private val analyticsRepository: AnalyticsRepository,
    private val contactRepository: ContactRepository
) {
    /**
     * Śledzi odpowiedź od kontaktu.
     *
     * @param contact Kontakt, który odpowiedział
     * @param originalMessage Oryginalna wiadomość, na którą otrzymano odpowiedź (jeśli dostępna)
     * @param responseMessage Wiadomość odpowiedzi
     * @param responseType Typ odpowiedzi (np. "positive", "negative", "neutral")
     * @param metadata Dodatkowe metadane do zapisania
     *
     * @return Identyfikator utworzonego zdarzenia analitycznego
     */
    suspend operator fun invoke(
        contact: Contact,
        originalMessage: Message? = null,
        responseMessage: Message,
        responseType: String? = null,
        metadata: Map<String, String> = emptyMap()
    ): Long {
        // 1. Zaktualizuj status kontaktu na "responded"
        contactRepository.updateContactStatus(contact.id, "responded")

        // 2. Przygotuj metadane
        val eventMetadata = metadata.toMutableMap()

        // Dodaj informacje o czasie odpowiedzi, jeśli oryginalna wiadomość jest dostępna
        originalMessage?.sentAt?.let { sentAt ->
            val responseTime = responseMessage.createdAt.time - sentAt.time
            eventMetadata["response_time_ms"] = responseTime.toString()
        }

        // Dodaj typ odpowiedzi, jeśli podano
        responseType?.let {
            eventMetadata["response_type"] = it
        }

        // 3. Śledź zdarzenie odpowiedzi
        return analyticsRepository.trackEvent(
            event = "response_received",
            contactId = contact.id,
            messageId = responseMessage.id,
            originalMessage?.let {
                // Jeśli oryginalna wiadomość pochodzi z sekwencji, przekaż identyfikator sekwencji
                it.metadata["sequence_id"]?.toLongOrNull()
            },
            value = responseType,
            metadata = eventMetadata
        )
    }

    /**
     * Analizuje treść odpowiedzi i określa jej typ (pozytywna, negatywna, neutralna).
     * Ta implementacja jest prosta i mogłaby być rozszerzona o bardziej zaawansowaną
     * analizę sentymentu za pomocą modeli językowych.
     *
     * @param content Treść odpowiedzi
     * @return Typ odpowiedzi ("positive", "negative", "neutral")
     */
    fun analyzeResponseType(content: String): String {
        val positiveKeywords = listOf(
            "zainteresowany", "chętnie", "zgadzam się", "dobry pomysł", "porozmawiajmy",
            "tak", "oczywiście", "interesujące", "kontynuujmy", "spotkajmy się"
        )

        val negativeKeywords = listOf(
            "niezainteresowany", "nie", "odmawiam", "nie teraz", "nie dziekuję",
            "nie mamy potrzeby", "nie mamy budżetu", "przepraszam", "rezygnuję",
            "proszę nie kontaktować się", "usuń mnie"
        )

        val lowerContent = content.lowercase()

        val positiveScore = positiveKeywords.count { lowerContent.contains(it) }
        val negativeScore = negativeKeywords.count { lowerContent.contains(it) }

        return when {
            positiveScore > negativeScore -> "positive"
            negativeScore > positiveScore -> "negative"
            else -> "neutral"
        }
    }
}