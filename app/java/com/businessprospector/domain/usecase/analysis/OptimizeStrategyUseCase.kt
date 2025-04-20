package com.businessprospector.domain.usecase.analytics

import com.businessprospector.data.model.SequenceStep
import com.businessprospector.data.repository.AnalyticsRepository
import com.businessprospector.data.repository.SequenceRepository
import com.businessprospector.domain.model.Sequence
import java.util.Date
import javax.inject.Inject

/**
 * Przypadek użycia odpowiedzialny za analizę danych i sugerowanie optymalizacji
 * strategii komunikacji na podstawie historycznych wyników.
 */
class OptimizeStrategyUseCase @Inject constructor(
    private val analyticsRepository: AnalyticsRepository,
    private val sequenceRepository: SequenceRepository
) {
    /**
     * Analizuje dane sekwencji i sugeruje optymalizacje.
     *
     * @param sequenceId Identyfikator sekwencji do optymalizacji
     * @return Lista sugestii optymalizacyjnych
     */
    suspend operator fun invoke(sequenceId: Long): List<OptimizationSuggestion> {
        // Pobierz sekwencję z jej krokami
        val sequence = sequenceRepository.getSequenceWithSteps(sequenceId)
            ?: return listOf(OptimizationSuggestion("error", "Sequence not found"))

        val suggestions = mutableListOf<OptimizationSuggestion>()

        // Analizuj metryki sekwencji
        analyzeSequenceMetrics(sequence, suggestions)

        // Analizuj kroki sekwencji
        sequence.steps.forEachIndexed { index, step ->
            analyzeStepMetrics(sequence, step, index, suggestions)
        }

        // Analizuj czasy odpowiedzi
        analyzeResponseTimes(sequence, suggestions)

        // Analizuj typy wiadomości
        analyzeMessageTypes(sequence, suggestions)

        return suggestions
    }

    /**
     * Analizuje ogólne metryki sekwencji.
     */
    private suspend fun analyzeSequenceMetrics(
        sequence: Sequence,
        suggestions: MutableList<OptimizationSuggestion>
    ) {
        // Pobierz metryki sekwencji
        val responseRate = sequenceRepository.getSequenceResponseRate(sequence.id)
        val completionRate = sequenceRepository.getSequenceCompletionRate(sequence.id)

        // Dodaj sugestie na podstawie metryk
        if (responseRate < 10f) {
            suggestions.add(
                OptimizationSuggestion(
                    "sequence_low_response",
                    "Response rate for this sequence is very low (${responseRate.toInt()}%). " +
                            "Consider revising the messaging approach or target audience."
                )
            )
        }

        if (completionRate < 50f) {
            suggestions.add(
                OptimizationSuggestion(
                    "sequence_low_completion",
                    "Only ${completionRate.toInt()}% of contacts complete this sequence. " +
                            "Consider shortening the sequence or improving engagement in early steps."
                )
            )
        }

        // Sprawdź liczbę kroków
        if (sequence.steps.size > 5) {
            suggestions.add(
                OptimizationSuggestion(
                    "sequence_too_long",
                    "This sequence has ${sequence.steps.size} steps, which may be too long. " +
                            "Consider reducing the number of steps to 3-5 for higher completion rates."
                )
            )
        }
    }

    /**
     * Analizuje metryki dla konkretnego kroku sekwencji.
     */
    private suspend fun analyzeStepMetrics(
        sequence: Sequence,
        step: SequenceStep,
        stepIndex: Int,
        suggestions: MutableList<OptimizationSuggestion>
    ) {
        // Pobierz metryki kroku
        val stepResponseRate = sequenceRepository.getStepResponseRate(sequence.id, step.id)
        val stepOpenRate = sequenceRepository.getStepOpenRate(sequence.id, step.id)
        val stepDropoffRate = sequenceRepository.getStepDropoffRate(sequence.id, step.id)

        // Analizuj metryki
        if (stepIndex == 0 && stepResponseRate < 15f) {
            suggestions.add(
                OptimizationSuggestion(
                    "first_step_low_response",
                    "The first step has a low response rate (${stepResponseRate.toInt()}%). " +
                            "Consider improving the subject line or message content to increase initial engagement."
                )
            )
        }

        if (step.type == "email" && stepOpenRate < 20f) {
            suggestions.add(
                OptimizationSuggestion(
                    "step_low_open_rate",
                    "Step ${stepIndex + 1} has a low open rate (${stepOpenRate.toInt()}%). " +
                            "Consider testing different subject lines or sending times."
                )
            )
        }

        if (stepDropoffRate > 40f) {
            suggestions.add(
                OptimizationSuggestion(
                    "step_high_dropoff",
                    "Step ${stepIndex + 1} has a high dropoff rate (${stepDropoffRate.toInt()}%). " +
                            "This could be a good place to optimize your messaging or timing."
                )
            )
        }

        // Analizuj opóźnienia
        if ((step.delayDays > 7 || step.delayHours > 168) && stepIndex > 0) {
            suggestions.add(
                OptimizationSuggestion(
                    "step_long_delay",
                    "Step ${stepIndex + 1} has a long delay (${step.delayDays} days, ${step.delayHours} hours). " +
                            "Consider shortening this delay to maintain momentum."
                )
            )
        }

        // Analizuj typy wiadomości
        if (stepIndex > 0 && step.type == sequence.steps[stepIndex - 1].type) {
            suggestions.add(
                OptimizationSuggestion(
                    "consecutive_same_type",
                    "Steps ${stepIndex} and ${stepIndex + 1} use the same communication channel (${step.type}). " +
                            "Consider varying channels for better engagement."
                )
            )
        }
    }

    /**
     * Analizuje czasy odpowiedzi dla sekwencji.
     */
    private suspend fun analyzeResponseTimes(
        sequence: Sequence,
        suggestions: MutableList<OptimizationSuggestion>
    ) {
        // Pobierz dane o czasie odpowiedzi
        val avgResponseTime = sequenceRepository.getAverageResponseTime(sequence.id)
        val responseTimeDistribution = sequenceRepository.getResponseTimeDistribution(sequence.id)

        // Analizuj czas odpowiedzi
        if (avgResponseTime != null && avgResponseTime > 86400000) { // > 24 godziny w milisekundach
            suggestions.add(
                OptimizationSuggestion(
                    "long_response_time",
                    "Average response time is over 24 hours. Consider following up more quickly in your sequence."
                )
            )
        }

        // Analizuj rozkład czasów odpowiedzi
        val peakResponseHour = responseTimeDistribution.maxByOrNull { it.second }?.first
        if (peakResponseHour != null) {
            suggestions.add(
                OptimizationSuggestion(
                    "optimize_timing",
                    "Most responses occur around $peakResponseHour:00. Consider timing your messages to align with this peak response time."
                )
            )
        }
    }

    /**
     * Analizuje skuteczność różnych typów wiadomości.
     */
    private suspend fun analyzeMessageTypes(
        sequence: Sequence,
        suggestions: MutableList<OptimizationSuggestion>
    ) {
        // Pobierz dane o skuteczności kanałów
        val emailResponseRate = sequenceRepository.getChannelResponseRate(sequence.id, "email")
        val smsResponseRate = sequenceRepository.getChannelResponseRate(sequence.id, "sms")
        val callResponseRate = sequenceRepository.getChannelResponseRate(sequence.id, "call")

        // Porównaj skuteczność kanałów
        val channels = listOf(
            "email" to emailResponseRate,
            "sms" to smsResponseRate,
            "call" to callResponseRate
        ).filter { it.second > 0f }

        if (channels.size >= 2) {
            val mostEffective = channels.maxByOrNull { it.second }
            val leastEffective = channels.minByOrNull { it.second }

            if (mostEffective != null && leastEffective != null &&
                mostEffective.first != leastEffective.first &&
                mostEffective.second > leastEffective.second * 2) {

                suggestions.add(
                    OptimizationSuggestion(
                        "channel_effectiveness",
                        "${mostEffective.first.capitalize()} channel is ${(mostEffective.second / leastEffective.second).toInt()}x more effective than ${leastEffective.first} " +
                                "for this sequence. Consider using more ${mostEffective.first} messages."
                    )
                )
            }
        }

        // Sprawdź, czy sekwencja wykorzystuje różne kanały
        val uniqueChannels = sequence.steps.map { it.type }.distinct()
        if (uniqueChannels.size == 1 && sequence.steps.size > 2) {
            suggestions.add(
                OptimizationSuggestion(
                    "single_channel",
                    "This sequence only uses ${uniqueChannels[0]} messages. " +
                            "Consider adding other communication channels for a multi-channel approach."
                )
            )
        }
    }
}

/**
 * Model reprezentujący sugestię optymalizacyjną.
 */
data class OptimizationSuggestion(
    val code: String,  // Unikalny kod dla typu sugestii
    val suggestion: String,  // Opis sugestii dla użytkownika
    val importance: Int = 1, // Priorytet sugestii (1-3, gdzie 3 to najwyższy)
    val metadata: Map<String, Any> = emptyMap() // Dodatkowe dane
)

// Funkcja rozszerzająca dla String
private fun String.capitalize(): String {
    return this.replaceFirstChar { it.uppercase() }
}