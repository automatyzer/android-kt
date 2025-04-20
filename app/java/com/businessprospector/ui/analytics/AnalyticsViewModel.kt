package com.businessprospector.ui.analytics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.businessprospector.data.local.dao.AnalyticsDao
import com.businessprospector.data.local.dao.ContactDao
import com.businessprospector.data.local.dao.MessageDao
import com.businessprospector.data.local.dao.SequenceDao
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AnalyticsViewModel @Inject constructor(
    private val analyticsDao: AnalyticsDao,
    private val contactDao: ContactDao,
    private val messageDao: MessageDao,
    private val sequenceDao: SequenceDao
) : ViewModel() {

    private val _analyticsData = MutableStateFlow(AnalyticsData())
    val analyticsData: StateFlow<AnalyticsData> = _analyticsData

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    init {
        loadAnalyticsData()
    }

    private fun loadAnalyticsData() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                // W pełnej implementacji, te dane powinny pochodzić z bazy danych
                // poprzez wywołania do DAO
                // Tutaj symulujemy dane do celów prezentacji UI

                // Przykładowe dane statystyczne
                val mockData = AnalyticsData(
                    totalContacts = 150,
                    activeSequences = 5,
                    messagesSent = 250,
                    responseRate = 38,
                    topContacts = listOf(
                        TopContact("John Smith", "ABC Corp", 8),
                        TopContact("Alice Johnson", "Tech Solutions", 6),
                        TopContact("Michael Brown", "Global Industries", 5),
                        TopContact("Sarah Wilson", "Innovative Systems", 4),
                        TopContact("Robert Davis", "Strategic Partners", 3)
                    ),
                    topSequences = listOf(
                        TopSequence("Welcome Sequence", 68),
                        TopSequence("Follow-up Campaign", 54),
                        TopSequence("Re-engagement", 42),
                        TopSequence("Product Demo", 38),
                        TopSequence("Partnership Outreach", 35)
                    ),
                    sequencePerformance = listOf(
                        SequencePerformance("Welcome Sequence", 100, 82, 68, 68),
                        SequencePerformance("Follow-up Campaign", 85, 62, 46, 54),
                        SequencePerformance("Re-engagement", 65, 38, 27, 42),
                        SequencePerformance("Product Demo", 45, 25, 17, 38),
                        SequencePerformance("Partnership Outreach", 40, 22, 14, 35)
                    )
                )

                _analyticsData.value = mockData
            } catch (e: Exception) {
                _error.value = e.message ?: "Unknown error occurred"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun refreshData() {
        loadAnalyticsData()
    }
}