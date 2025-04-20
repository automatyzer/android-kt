package com.businessprospector.domain.usecase.analytics

import com.businessprospector.data.model.AnalyticsReport
import com.businessprospector.data.model.ContactMetrics
import com.businessprospector.data.model.MessageMetrics
import com.businessprospector.data.model.SequenceMetrics
import com.businessprospector.data.model.TopContact
import com.businessprospector.data.model.TopMessageTemplate
import com.businessprospector.data.model.TopPerformers
import com.businessprospector.data.model.TopSequence
import com.businessprospector.data.repository.AnalyticsRepository
import com.businessprospector.data.repository.ContactRepository
import com.businessprospector.data.repository.MessageRepository
import com.businessprospector.data.repository.SequenceRepository
import java.util.Calendar
import java.util.Date
import javax.inject.Inject

/**
 * Przypadek użycia odpowiedzialny za generowanie szczegółowych raportów analitycznych.
 * Zbiera dane z różnych repozytoriów i tworzy kompleksowy raport, który może być
 * prezentowany użytkownikowi lub eksportowany.
 */
class GenerateReportUseCase @Inject constructor(
    private val analyticsRepository: AnalyticsRepository,
    private val contactRepository: ContactRepository,
    private val messageRepository: MessageRepository,
    private val sequenceRepository: SequenceRepository
) {
    /**
     * Generuje raport analityczny dla określonego okresu.
     *
     * @param startDate Data początkowa okresu
     * @param endDate Data końcowa okresu
     * @param title Tytuł raportu
     * @param description Opcjonalny opis raportu
     *
     * @return Wygenerowany raport analityczny
     */
    suspend operator fun invoke(
        startDate: Date,
        endDate: Date,
        title: String,
        description: String? = null
    ): AnalyticsReport {
        // 1. Pobierz metryki wiadomości
        val messageMetrics = getMessageMetrics(startDate, endDate)

        // 2. Pobierz metryki kontaktów
        val contactMetrics = getContactMetrics(startDate, endDate)

        // 3. Pobierz metryki sekwencji
        val sequenceMetrics = getSequenceMetrics(startDate, endDate)

        // 4. Pobierz najlepiej performujące elementy
        val topPerformers = getTopPerformers(startDate, endDate)

        // 5. Utwórz i zwróć raport
        return AnalyticsReport(
            title = title,
            description = description,
            startDate = startDate,
            endDate = endDate,
            messageMetrics = messageMetrics,
            contactMetrics = contactMetrics,
            sequenceMetrics = sequenceMetrics,
            topPerformers = topPerformers,
            generatedAt = Date()
        )
    }

    /**
     * Generuje raport dla predefiniowanego okresu (np. ostatnie 7 dni, ostatni miesiąc).
     *
     * @param period Okres: "7days", "30days", "month", "quarter", "year"
     * @param title Tytuł raportu
     * @param description Opcjonalny opis raportu
     *
     * @return Wygenerowany raport analityczny
     */
    suspend operator fun invoke(
        period: String,
        title: String,
        description: String? = null
    ): AnalyticsReport {
        val endDate = Date()
        val startDate = calculateStartDate(period)

        return invoke(startDate, endDate, title, description)
    }

    /**
     * Oblicza datę początkową na podstawie predefiniowanego okresu.
     */
    private fun calculateStartDate(period: String): Date {
        val calendar = Calendar.getInstance()

        when (period) {
            "7days" -> calendar.add(Calendar.DAY_OF_YEAR, -7)
            "30days" -> calendar.add(Calendar.DAY_OF_YEAR, -30)
            "month" -> {
                calendar.set(Calendar.DAY_OF_MONTH, 1)
                calendar.add(Calendar.MONTH, -1)
            }
            "quarter" -> {
                calendar.set(Calendar.DAY_OF_MONTH, 1)
                calendar.add(Calendar.MONTH, -3)
            }
            "year" -> {
                calendar.set(Calendar.DAY_OF_MONTH, 1)
                calendar.set(Calendar.MONTH, Calendar.JANUARY)
                calendar.add(Calendar.YEAR, -1)
            }
            else -> calendar.add(Calendar.DAY_OF_YEAR, -7) // Domyślnie ostatnie 7 dni
        }

        return calendar.time
    }

    /**
     * Pobiera metryki związane z wiadomościami.
     */
    private suspend fun getMessageMetrics(startDate: Date, endDate: Date): MessageMetrics {
        // W rzeczywistej implementacji, te metryki byłyby pobierane z repozytoriów
        // Poniżej jest przykładowa implementacja

        val emailsSent = analyticsRepository.countEventsBetweenDates("email_sent", startDate, endDate)
        val emailsOpened = analyticsRepository.countEventsBetweenDates("email_opened", startDate, endDate)
        val emailsClicked = analyticsRepository.countEventsBetweenDates("email_clicked", startDate, endDate)
        val smsSent = analyticsRepository.countEventsBetweenDates("sms_sent", startDate, endDate)
        val smsDelivered = analyticsRepository.countEventsBetweenDates("sms_delivered", startDate, endDate)
        val callsMade = analyticsRepository.countEventsBetweenDates("call_made", startDate, endDate)
        val callsAnswered = analyticsRepository.countEventsBetweenDates("call_answered", startDate, endDate)
        val responsesReceived = analyticsRepository.countEventsBetweenDates("response_received", startDate, endDate)

        val openRate = if (emailsSent > 0) (emailsOpened.toFloat() / emailsSent) * 100 else 0f
        val clickRate = if (emailsOpened > 0) (emailsClicked.toFloat() / emailsOpened) * 100 else 0f
        val responseRate = if (emailsSent + smsSent + callsMade > 0)
            (responsesReceived.toFloat() / (emailsSent + smsSent + callsMade)) * 100
        else 0f

        val averageResponseTime = analyticsRepository.getAverageResponseTime() ?: 0L

        return MessageMetrics(
            emailsSent = emailsSent,
            emailsOpened = emailsOpened,
            emailsClicked = emailsClicked,
            smsSent = smsSent,
            smsDelivered = smsDelivered,
            callsMade = callsMade,
            callsAnswered = callsAnswered,
            totalResponsesReceived = responsesReceived,
            openRate = openRate,
            clickRate = clickRate,
            responseRate = responseRate,
            averageResponseTime = averageResponseTime
        )
    }

    /**
     * Pobiera metryki związane z kontaktami.
     */
    private suspend fun getContactMetrics(startDate: Date, endDate: Date): ContactMetrics {
        // W rzeczywistej implementacji, te metryki byłyby pobierane z repozytoriów

        // Przykładowa implementacja
        val contactsByStatus = contactRepository.getContactCountsByStatus()

        val newContacts = contactsByStatus["new"] ?: 0
        val contactedContacts = contactsByStatus["contacted"] ?: 0
        val respondedContacts = contactsByStatus["responded"] ?: 0
        val meetingScheduledContacts = contactsByStatus["meeting_scheduled"] ?: 0
        val dealContacts = contactsByStatus["deal"] ?: 0
        val notInterestedContacts = contactsByStatus["not_interested"] ?: 0

        val conversionRate = if (contactedContacts > 0)
            (dealContacts.toFloat() / contactedContacts) * 100
        else 0f

        return ContactMetrics(
            newContacts = newContacts,
            contactedContacts = contactedContacts,
            respondedContacts = respondedContacts,
            meetingScheduledContacts = meetingScheduledContacts,
            dealContacts = dealContacts,
            notInterestedContacts = notInterestedContacts,
            conversionRate = conversionRate
        )
    }

    /**
     * Pobiera metryki związane z sekwencjami.
     */
    private suspend fun getSequenceMetrics(startDate: Date, endDate: Date): List<SequenceMetrics> {
        // W rzeczywistej implementacji, te metryki byłyby pobierane z repozytoriów

        // Przykładowa implementacja
        val sequences = sequenceRepository.getAllSequences()

        return sequences.map { sequence ->
            val contactsInSequence = sequenceRepository.getContactCountInSequence(sequence.id)
            val completedExecutions = sequenceRepository.getCompletedExecutionsCount(sequence.id)
            val messagesSent = sequenceRepository.getMessagesSentInSequence(sequence.id)
            val responsesReceived = sequenceRepository.getResponsesReceivedInSequence(sequence.id)

            val successRate = if (contactsInSequence > 0)
                (completedExecutions.toFloat() / contactsInSequence) * 100
            else 0f

            SequenceMetrics(
                sequenceId = sequence.id,
                sequenceName = sequence.name,
                contactsInSequence = contactsInSequence,
                completedExecutions = completedExecutions,
                messagesSent = messagesSent,
                responsesReceived = responsesReceived,
                successRate = successRate
            )
        }
    }

    /**
     * Pobiera najlepiej performujące elementy.
     */
    private suspend fun getTopPerformers(startDate: Date, endDate: Date): TopPerformers {
        // W rzeczywistej implementacji, te dane byłyby pobierane z repozytoriów

        // Przykładowa implementacja
        val topContacts = getTopContacts(startDate, endDate)
        val topSequences = getTopSequences(startDate, endDate)
        val topTemplates = getTopMessageTemplates(startDate, endDate)

        return TopPerformers(
            topContacts = topContacts,
            topSequences = topSequences,
            topMessageTemplates = topTemplates
        )
    }

    private suspend fun getTopContacts(startDate: Date, endDate: Date): List<TopContact> {
        // Przykładowa implementacja
        return contactRepository.getTopContactsByInteractions(limit = 5)
    }

    private suspend fun getTopSequences(startDate: Date, endDate: Date): List<TopSequence> {
        // Przykładowa implementacja
        return sequenceRepository.getTopSequencesBySuccessRate(limit = 5)
    }

    private suspend fun getTopMessageTemplates(startDate: Date, endDate: Date): List<TopMessageTemplate> {
        // Przykładowa implementacja
        return messageRepository.getTopTemplatesByResponseRate(limit = 5)
    }
}