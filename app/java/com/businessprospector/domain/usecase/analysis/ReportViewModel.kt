package com.businessprospector.domain.usecase.analytics

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.businessprospector.data.model.AnalyticsReport
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class ReportViewModel @Inject constructor(
    private val generateReportUseCase: GenerateReportUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _report = MutableStateFlow<AnalyticsReport?>(null)
    val report: StateFlow<AnalyticsReport?> = _report

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _sharingOptions = MutableStateFlow<List<String>>(listOf("PDF", "CSV", "Email", "Share Link"))
    val sharingOptions: StateFlow<List<String>> = _sharingOptions

    // Pobierz parametry raportu z SavedStateHandle
    private val reportPeriod: String? = savedStateHandle["period"]
    private val startDateStr: String? = savedStateHandle["startDate"]
    private val endDateStr: String? = savedStateHandle["endDate"]
    private val reportTitle: String? = savedStateHandle["title"]

    init {
        loadReport()
    }

    /**
     * Ładuje raport na podstawie parametrów przekazanych do ViewModel.
     */
    fun loadReport() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                val report = if (reportPeriod != null) {
                    // Generuj raport dla predefiniowanego okresu
                    generateReportUseCase(
                        period = reportPeriod,
                        title = reportTitle ?: "Analytics Report - ${reportPeriod.capitalize()}",
                        description = "Performance report for ${reportPeriod.replace("_", " ")}"
                    )
                } else if (startDateStr != null && endDateStr != null) {
                    // Generuj raport dla niestandardowego zakresu dat
                    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    val startDate = dateFormat.parse(startDateStr) ?: Date()
                    val endDate = dateFormat.parse(endDateStr) ?: Date()

                    generateReportUseCase(
                        startDate = startDate,
                        endDate = endDate,
                        title = reportTitle ?: "Custom Analytics Report",
                        description = "Performance report from ${formatDate(startDate)} to ${formatDate(endDate)}"
                    )
                } else {
                    // Domyślnie generuj raport dla ostatnich 30 dni
                    val endDate = Date()
                    val startDate = Calendar.getInstance().apply {
                        time = endDate
                        add(Calendar.DAY_OF_YEAR, -30)
                    }.time

                    generateReportUseCase(
                        startDate = startDate,
                        endDate = endDate,
                        title = "30-Day Analytics Report",
                        description = "Performance report for the last 30 days"
                    )
                }

                _report.value = report
            } catch (e: Exception) {
                _error.value = "Failed to generate report: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Eksportuje raport w wybranym formacie.
     */
    fun exportReport(format: String) {
        val report = _report.value ?: return

        viewModelScope.launch {
            try {
                when (format.lowercase()) {
                    "pdf" -> exportToPdf(report)
                    "csv" -> exportToCsv(report)
                    "email" -> shareViaEmail(report)
                    "share link" -> generateShareableLink(report)
                }
            } catch (e: Exception) {
                _error.value = "Failed to export report: ${e.message}"
            }
        }
    }

    /**
     * Eksportuje raport do formatu PDF.
     */
    private fun exportToPdf(report: AnalyticsReport) {
        // W rzeczywistej implementacji, tutaj byłaby logika eksportu do PDF
        // Wykorzystująca bibliotekę taką jak iText lub AndroidPDF

        // Przykładowa implementacja
        _error.value = "PDF export functionality is not implemented yet"
    }

    /**
     * Eksportuje raport do formatu CSV.
     */
    private fun exportToCsv(report: AnalyticsReport) {
        // W rzeczywistej implementacji, tutaj byłaby logika eksportu do CSV

        // Przykładowa implementacja
        _error.value = "CSV export functionality is not implemented yet"
    }

    /**
     * Udostępnia raport przez email.
     */
    private fun shareViaEmail(report: AnalyticsReport) {
        // W rzeczywistej implementacji, tutaj byłaby logika wysyłania emaila
        // z załączonym raportem lub linkiem do raportu

        // Przykładowa implementacja
        _error.value = "Email sharing functionality is not implemented yet"
    }

    /**
     * Generuje link do udostępniania raportu.
     */
    private fun generateShareableLink(report: AnalyticsReport) {
        // W rzeczywistej implementacji, tutaj byłaby logika generowania linku do raportu

        // Przykładowa implementacja
        _error.value = "Share link functionality is not implemented yet"
    }

    /**
     * Formatuje datę do czytelnego formatu.
     */
    private fun formatDate(date: Date): String {
        val format = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        return format.format(date)
    }
}

// Funkcja rozszerzająca dla String
private fun String.capitalize(): String {
    return this.replaceFirstChar { it.uppercase() }
}