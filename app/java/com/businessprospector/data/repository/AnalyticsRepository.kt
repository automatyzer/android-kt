package com.businessprospector.data.repository

import com.businessprospector.data.local.dao.AnalyticsDao
import com.businessprospector.data.local.entity.AnalyticsEntity
import com.businessprospector.domain.model.AnalyticsEvent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repozytorium odpowiedzialne za zarządzanie danymi analitycznymi.
 * Zapewnia interfejs do zapisywania zdarzeń, pobierania danych analitycznych i
 * generowania raportów na podstawie zgromadzonych danych.
 */
@Singleton
class AnalyticsRepository @Inject constructor(
    private val analyticsDao: AnalyticsDao
) {
    /**
     * Zapisuje nowe zdarzenie analityczne w bazie danych.
     */
    suspend fun trackEvent(
        event: String,
        contactId: Long? = null,
        sequenceId: Long? = null,
        messageId: Long? = null,
        value: String? = null,
        metadata: Map<String, String> = emptyMap()
    ): Long {
        val analyticsEntity = AnalyticsEntity(
            event = event,
            contactId = contactId,
            sequenceId = sequenceId,
            messageId = messageId,
            value = value,
            metadata = metadata,
            timestamp = Date()
        )

        return analyticsDao.insertAnalyticsEvent(analyticsEntity)
    }

    /**
     * Pobiera zdarzenia analityczne dla określonego kontaktu.
     */
    fun getAnalyticsForContact(contactId: Long): Flow<List<AnalyticsEvent>> {
        return analyticsDao.getAnalyticsForContact(contactId).map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    /**
     * Pobiera zdarzenia analityczne dla określonej sekwencji.
     */
    fun getAnalyticsForSequence(sequenceId: Long): Flow<List<AnalyticsEvent>> {
        return analyticsDao.getAnalyticsForSequence(sequenceId).map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    /**
     * Pobiera zdarzenia analityczne określonego typu.
     */
    fun getAnalyticsByEventType(eventType: String): Flow<List<AnalyticsEvent>> {
        return analyticsDao.getAnalyticsByEventType(eventType).map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    /**
     * Zlicza wystąpienia określonego typu zdarzenia w podanym zakresie dat.
     */
    suspend fun countEventsBetweenDates(eventType: String, startDate: Date, endDate: Date): Int {
        return analyticsDao.countEventsBetweenDates(eventType, startDate, endDate)
    }

    /**
     * Oblicza współczynnik odpowiedzi na wiadomości.
     */
    suspend fun getResponseRate(): Int {
        return analyticsDao.getResponseRate()
    }

    /**
     * Oblicza średni czas odpowiedzi na wiadomości.
     */
    suspend fun getAverageResponseTime(): Long? {
        return analyticsDao.getAverageResponseTime()
    }

    /**
     * Generuje raport analityczny dla podanego okresu.
     */
    suspend fun generateAnalyticsReport(startDate: Date, endDate: Date): AnalyticsReport {
        val emailsSent = countEventsBetweenDates("email_sent", startDate, endDate)
        val emailsOpened = countEventsBetweenDates("email_opened", startDate, endDate)
        val emailsClicked = countEventsBetweenDates("email_clicked", startDate, endDate)
        val responsesReceived = countEventsBetweenDates("response_received", startDate, endDate)

        val openRate = if (emailsSent > 0) (emailsOpened.toFloat() / emailsSent) * 100 else 0f
        val clickRate = if (emailsOpened > 0) (emailsClicked.toFloat() / emailsOpened) * 100 else 0f
        val responseRate = if (emailsSent > 0) (responsesReceived.toFloat() / emailsSent) * 100 else 0f

        return AnalyticsReport(
            emailsSent = emailsSent,
            emailsOpened = emailsOpened,
            emailsClicked = emailsClicked,
            responsesReceived = responsesReceived,
            openRate = openRate,
            clickRate = clickRate,
            responseRate = responseRate,
            averageResponseTime = getAverageResponseTime() ?: 0L
        )
    }

    // Konwersja z encji do modelu domeny
    private fun AnalyticsEntity.toDomainModel(): AnalyticsEvent {
        return AnalyticsEvent(
            id = id,
            contactId = contactId,
            sequenceId = sequenceId,
            messageId = messageId,
            event = event,
            value = value,
            metadata = metadata,
            timestamp = timestamp
        )
    }
}

/**
 * Model raportu analitycznego zawierający kluczowe metryki.
 */
data class AnalyticsReport(
    val emailsSent: Int,
    val emailsOpened: Int,
    val emailsClicked: Int,
    val responsesReceived: Int,
    val openRate: Float, // procent
    val clickRate: Float, // procent
    val responseRate: Float, // procent
    val averageResponseTime: Long // w milisekundach
)

// Model domeny dla zdarzenia analitycznego
package com.businessprospector.domain.model

import java.util.Date

data class AnalyticsEvent(
    val id: Long = 0,
    val contactId: Long? = null,
    val sequenceId: Long? = null,
    val messageId: Long? = null,
    val event: String,
    val value: String? = null,
    val metadata: Map<String, String> = emptyMap(),
    val timestamp: Date = Date()
)