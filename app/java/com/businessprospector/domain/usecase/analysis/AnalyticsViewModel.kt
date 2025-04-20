package com.businessprospector.domain.usecase.analytics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.businessprospector.data.model.AnalyticsPeriod
import com.businessprospector.data.model.AnalyticsReport
import com.businessprospector.data.model.AnalyticsSummary
import com.businessprospector.data.model.SequenceMetrics
import com.businessprospector.data.model.TopContact
import com.businessprospector.data.model.TopPerformers
import com.businessprospector.data.model.TopSequence
import com.businessprospector.data.repository.AnalyticsRepository
import com.businessprospector.data.repository.ContactRepository
import com.businessprospector.data.repository.MessageRepository
import com.businessprospector.data.repository.SequenceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Date
import javax.inject.Inject

@HiltViewModel
class AnalyticsViewModel @Inject constructor(
    private val analyticsRepository: AnalyticsRepository,
    private val contactRepository: ContactRepository,
    private val messageRepository: MessageRepository,
    private val sequenceRepository: SequenceRepository,
    private val generateReportUseCase: GenerateReportUseCase
) : ViewModel() {

    private val _analyticsSummary = MutableStateFlow(AnalyticsSummary())
    val analyticsSummary: StateFlow<AnalyticsSummary> = _analyticsSummary

    private val _topContacts = MutableStateFlow<List<TopContact>>(emptyList())
    val topContacts: StateFlow<List<TopContact>> = _topContacts

    private val _topSequences = MutableStateFlow<List<TopSequence>>(emptyList())
    val topSequences: StateFlow<List<TopSequence>> = _topSequences

    private val _sequenceMetrics = MutableStateFlow<List<SequenceMetrics>>(emptyList())
    val sequenceMetrics: StateFlow<List<SequenceMetrics>> = _sequenceMetrics

    private val _selectedPeriod = MutableStateFlow(AnalyticsPeriod.LAST_30_DAYS)
    val selectedPeriod: StateFlow<AnalyticsPeriod> = _selectedPeriod

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    init {
        loadAnalytics()
    }

    /**
     * Zmienia wybrany okres analizy i odświeża dane.
     */
    fun setPeriod(period: AnalyticsPeriod) {
        if (_selectedPeriod.value != period) {
            _selectedPeriod.value = period
            loadAnalytics()
        }
    }

    /**
     * Generuje i zwraca raport dla wybranego okresu.
     */
    fun generateReport(): AnalyticsReport? {
        val (startDate, endDate) = getDateRangeForPeriod(_selectedPeriod.value)

        return try {
            AnalyticsReport(
                title = "Analytics Report - ${_selectedPeriod.value.name.replace("_", " ").lowercase().capitalize()}",
                description = "Performance report from ${formatDate(startDate)} to ${formatDate(endDate)}",
                startDate = startDate,
                endDate = endDate,
                messageMetrics = _analyticsSummary.value.run {
                    com.businessprospector.data.model.MessageMetrics(
                        emailsSent = messagesSent,
                        emailsOpened = messagesOpened,
                        responseRate = responseRate,
                        averageResponseTime = averageResponseTime
                    )
                },
                contactMetrics = com.businessprospector.data.model.ContactMetrics(
                    // Dane do uzupełnienia w rzeczywistej implementacji
                ),
                sequenceMetrics = _sequenceMetrics.value,
                topPerformers = TopPerformers(
                    topContacts = _topContacts.value,
                    topSequences = _topSequences.value
                )
            )
        } catch (e: Exception) {
            _error.value = "Failed to generate report: ${e.message}"
            null
        }
    }

    /**
     * Wczytuje dane analityczne dla wybranego okresu.
     */
    fun loadAnalytics() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                val (startDate, endDate) = getDateRangeForPeriod(_selectedPeriod.value)

                // Pobierz podsumowanie analityczne
                _analyticsSummary.value = getAnalyticsSummary(startDate, endDate)

                // Pobierz najlepsze kontakty
                _topContacts.value = contactRepository.getTopContactsByInteractions(5)

                // Pobierz najlepsze sekwencje
                _topSequences.value = sequenceRepository.getTopSequencesBySuccessRate(5)

                // Pobierz metryki sekwencji
                _sequenceMetrics.value = getSequenceMetrics(startDate, endDate)

            } catch (e: Exception) {
                _error.value = "Failed to load analytics: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Oblicza zakres dat dla wybranego okresu.
     */
    private fun getDateRangeForPeriod(period: AnalyticsPeriod): Pair<Date, Date> {
        val endDate = Date()
        val startDate = when (period) {
            AnalyticsPeriod.TODAY -> {
                Calendar.getInstance().apply {
                    time = endDate
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                }.time
            }
            AnalyticsPeriod.YESTERDAY -> {
                Calendar.getInstance().apply {
                    time = endDate
                    add(Calendar.DAY_OF_YEAR, -1)
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                }.time
            }
            AnalyticsPeriod.LAST_7_DAYS -> {
                Calendar.getInstance().apply {
                    time = endDate
                    add(Calendar.DAY_OF_YEAR, -7)
                }.time
            }
            AnalyticsPeriod.LAST_30_DAYS -> {
                Calendar.getInstance().apply {
                    time = endDate
                    add(Calendar.DAY_OF_YEAR, -30)
                }.time
            }
            AnalyticsPeriod.THIS_MONTH -> {
                Calendar.getInstance().apply {
                    time = endDate
                    set(Calendar.DAY_OF_MONTH, 1)
                }.time
            }
            AnalyticsPeriod.LAST_MONTH -> {
                Calendar.getInstance().apply {
                    time = endDate
                    add(Calendar.MONTH, -1)
                    set(Calendar.DAY_OF_MONTH, 1)
                }.time
            }
            AnalyticsPeriod.THIS_QUARTER -> {
                Calendar.getInstance().apply {
                    time = endDate
                    set(Calendar.MONTH, (get(Calendar.MONTH) / 3) * 3)
                    set(Calendar.DAY_OF_MONTH, 1)
                }.time
            }
            AnalyticsPeriod.LAST_QUARTER -> {
                Calendar.getInstance().apply {
                    time = endDate
                    add(Calendar.MONTH, -3)
                    set(Calendar.MONTH, (get(Calendar.MONTH) / 3) * 3)
                    set(Calendar.DAY_OF_MONTH, 1)
                }.time
            }
            AnalyticsPeriod.THIS_YEAR -> {
                Calendar.getInstance().apply {
                    time = endDate
                    set(Calendar.DAY_OF_YEAR, 1)
                }.time
            }
            AnalyticsPeriod.LAST_YEAR -> {
                Calendar.getInstance().apply {
                    time = endDate
                    add(Calendar.YEAR, -1)
                    set(Calendar.DAY_OF_YEAR, 1)
                }.time
            }
            AnalyticsPeriod.ALL_TIME -> {
                // Bardzo wczesna data jako początek "wszystkich czasów"
                Date(0)
            }
            AnalyticsPeriod.CUSTOM -> {
                // Dla niestandardowego okresu używamy ostatnich 30 dni jako domyślnego
                Calendar.getInstance().apply {
                    time = endDate
                    add(Calendar.DAY_OF_YEAR, -30)
                }.time
            }
        }

        return Pair(startDate, endDate)
    }

    /**
     * Pobiera podsumowanie analityczne dla podanego okresu.
     */
    private suspend fun getAnalyticsSummary(startDate: Date, endDate: Date): AnalyticsSummary {
        // W rzeczywistej implementacji dane byłyby pobierane z repozytoriów
        // Poniżej przykładowa implementacja

        val totalContacts = contactRepository.getTotalContactsCount()
        val activeSequences = sequenceRepository.getActiveSequencesCount()
        val messagesSent = analyticsRepository.countEventsBetweenDates("message_sent", startDate, endDate)
        val messagesOpened = analyticsRepository.countEventsBetweenDates("message_opened", startDate, endDate)
        val responses = analyticsRepository.countEventsBetweenDates("response_received", startDate, endDate)

        val responseRate = if (messagesSent > 0) (responses.toFloat() / messagesSent) * 100 else 0f
        val averageResponseTime = analyticsRepository.getAverageResponseTime() ?: 0L

        return AnalyticsSummary(
            totalContacts = totalContacts,
            activeSequences = activeSequences,
            messagesSent = messagesSent,
            messagesOpened = messagesOpened,
            responseRate = responseRate,
            averageResponseTime = averageResponseTime,
            period = _selectedPeriod.value
        )
    }

    /**
     * Pobiera metryki sekwencji dla podanego okresu.
     */
    private suspend fun getSequenceMetrics(startDate: Date, endDate: Date): List<SequenceMetrics> {
        // W rzeczywistej implementacji dane byłyby pobierane z repozytoriów
        // Poniżej przykładowa implementacja
        return sequenceRepository.getSequencesWithMetrics(startDate, endDate)
    }

    /**
     * Formatuje datę do czytelnej postaci.
     */
    private fun formatDate(date: Date): String {
        val format = java.text.SimpleDateFormat("dd MMM yyyy", java.util.Locale.getDefault())
        return format.format(date)
    }
}

// Funkcja rozszerzająca dla String
private fun String.capitalize(): String {
    return this.replaceFirstChar { it.uppercase() }
}