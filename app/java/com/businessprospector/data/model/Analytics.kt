package com.businessprospector.data.model

import java.util.Date

/**
 * Model danych reprezentujący zdarzenie analityczne.
 * Każde zdarzenie w aplikacji, które chcemy śledzić, jest reprezentowane jako obiekt
 * tej klasy i przechowywane w bazie danych.
 */
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

/**
 * Model danych reprezentujący zbiorcze statystyki analityczne.
 * Używany do przedstawiania podsumowania danych na ekranach analitycznych.
 */
data class AnalyticsSummary(
    val totalContacts: Int = 0,
    val activeSequences: Int = 0,
    val messagesSent: Int = 0,
    val messagesOpened: Int = 0,
    val responseRate: Float = 0f,
    val averageResponseTime: Long = 0L, // w milisekundach
    val period: AnalyticsPeriod = AnalyticsPeriod.ALL_TIME
)

/**
 * Model danych reprezentujący szczegółowy raport analityczny.
 */
data class AnalyticsReport(
    val id: String = System.currentTimeMillis().toString(), // unikalny identyfikator raportu
    val title: String,
    val description: String? = null,
    val startDate: Date,
    val endDate: Date,
    val messageMetrics: MessageMetrics,
    val contactMetrics: ContactMetrics,
    val sequenceMetrics: List<SequenceMetrics>,
    val topPerformers: TopPerformers,
    val generatedAt: Date = Date()
)

/**
 * Metryki dotyczące wiadomości.
 */
data class MessageMetrics(
    val emailsSent: Int = 0,
    val emailsOpened: Int = 0,
    val emailsClicked: Int = 0,
    val smsSent: Int = 0,
    val smsDelivered: Int = 0,
    val callsMade: Int = 0,
    val callsAnswered: Int = 0,
    val totalResponsesReceived: Int = 0,
    val openRate: Float = 0f, // procent
    val clickRate: Float = 0f, // procent
    val responseRate: Float = 0f, // procent
    val averageResponseTime: Long = 0L // w milisekundach
)

/**
 * Metryki dotyczące kontaktów.
 */
data class ContactMetrics(
    val newContacts: Int = 0,
    val contactedContacts: Int = 0,
    val respondedContacts: Int = 0,
    val meetingScheduledContacts: Int = 0,
    val dealContacts: Int = 0,
    val notInterestedContacts: Int = 0,
    val conversionRate: Float = 0f // procent (deal/contacted)
)

/**
 * Metryki dotyczące jednej sekwencji.
 */
data class SequenceMetrics(
    val sequenceId: Long,
    val sequenceName: String,
    val contactsInSequence: Int = 0,
    val completedExecutions: Int = 0,
    val messagesSent: Int = 0,
    val responsesReceived: Int = 0,
    val successRate: Float = 0f // procent
)

/**
 * Najlepiej performujące elementy.
 */
data class TopPerformers(
    val topContacts: List<TopContact> = emptyList(),
    val topSequences: List<TopSequence> = emptyList(),
    val topMessageTemplates: List<TopMessageTemplate> = emptyList()
)

/**
 * Najlepiej performujący kontakt.
 */
data class TopContact(
    val contactId: Long,
    val name: String,
    val company: String?,
    val interactionCount: Int,
    val responseRate: Float
)

/**
 * Najlepiej performująca sekwencja.
 */
data class TopSequence(
    val sequenceId: Long,
    val name: String,
    val successRate: Float
)

/**
 * Najlepiej performujący szablon wiadomości.
 */
data class TopMessageTemplate(
    val templateId: Long,
    val name: String,
    val type: String,
    val openRate: Float,
    val responseRate: Float
)

/**
 * Okresy analizy danych.
 */
enum class AnalyticsPeriod {
    TODAY,
    YESTERDAY,
    LAST_7_DAYS,
    LAST_30_DAYS,
    THIS_MONTH,
    LAST_MONTH,
    THIS_QUARTER,
    LAST_QUARTER,
    THIS_YEAR,
    LAST_YEAR,
    ALL_TIME,
    CUSTOM
}