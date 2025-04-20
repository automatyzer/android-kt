package com.businessprospector.ui.sequences

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.businessprospector.data.local.dao.SequenceDao
import com.businessprospector.data.local.entity.SequenceEntity
import com.businessprospector.domain.model.Sequence
import com.businessprospector.domain.usecase.communication.GetSequenceWithStepsUseCase
import com.businessprospector.domain.usecase.communication.GetSequencesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.Date
import javax.inject.Inject

@HiltViewModel
class SequencesViewModel @Inject constructor(
    private val getSequencesUseCase: GetSequencesUseCase,
    private val getSequenceWithStepsUseCase: GetSequenceWithStepsUseCase,
    private val sequenceDao: SequenceDao
) : ViewModel() {

    private val _sequences = MutableStateFlow<List<Sequence>>(emptyList())
    val sequences: StateFlow<List<Sequence>> = _sequences

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    init {
        loadSequences()
    }

    private fun loadSequences() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                getSequencesUseCase()
                    .catch { e ->
                        _error.value = e.message ?: "Failed to load sequences"
                        _isLoading.value = false
                    }
                    .collectLatest { sequencesList ->
                        val sequencesWithSteps = sequencesList.map { sequence ->
                            getSequenceWithStepsUseCase(sequence.id) ?: sequence
                        }
                        _sequences.value = sequencesWithSteps
                        _isLoading.value = false
                    }
            } catch (e: Exception) {
                _error.value = e.message ?: "Unknown error occurred"
                _isLoading.value = false
            }
        }
    }

    fun toggleSequenceActive(sequenceId: Long) {
        val sequence = _sequences.value.find { it.id == sequenceId } ?: return

        viewModelScope.launch {
            try {
                // Aktualizuj status aktywności
                val updatedEntity = SequenceEntity(
                    id = sequence.id,
                    name = sequence.name,
                    description = sequence.description,
                    isActive = !sequence.isActive,
                    createdAt = sequence.createdAt,
                    updatedAt = Date()
                )

                sequenceDao.updateSequence(updatedEntity)

                // Odśwież dane
                loadSequences()
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to update sequence"
            }
        }
    }

    fun refreshData() {
        loadSequences()
    }
}