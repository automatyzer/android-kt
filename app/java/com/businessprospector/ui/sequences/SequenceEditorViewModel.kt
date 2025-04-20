package com.businessprospector.ui.sequences

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.businessprospector.data.local.dao.MessageDao
import com.businessprospector.data.local.dao.SequenceDao
import com.businessprospector.data.local.entity.SequenceEntity
import com.businessprospector.data.local.entity.SequenceStepEntity
import com.businessprospector.domain.model.MessageTemplate
import com.businessprospector.domain.model.Sequence
import com.businessprospector.domain.model.SequenceStep
import com.businessprospector.domain.usecase.communication.GetMessageTemplatesUseCase
import com.businessprospector.domain.usecase.communication.GetSequenceWithStepsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import java.util.Date
import javax.inject.Inject

@HiltViewModel
class SequenceEditorViewModel @Inject constructor(
    private val sequenceDao: SequenceDao,
    private val getSequenceWithStepsUseCase: GetSequenceWithStepsUseCase,
    private val getMessageTemplatesUseCase: GetMessageTemplatesUseCase
) : ViewModel() {

    private val _sequence = MutableStateFlow<Sequence?>(null)
    val sequence: StateFlow<Sequence?> = _sequence

    private val _templates = MutableStateFlow<List<MessageTemplate>>(emptyList())
    val templates: StateFlow<List<MessageTemplate>> = _templates

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _saveSuccess = MutableStateFlow(false)
    val saveSuccess: StateFlow<Boolean> = _saveSuccess

    fun loadSequence(sequenceId: Long) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                val sequence = getSequenceWithStepsUseCase(sequenceId)

                if (sequence != null) {
                    _sequence.value = sequence
                } else {
                    _error.value = "Sequence not found"
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to load sequence"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadTemplates() {
        viewModelScope.launch {
            try {
                getMessageTemplatesUseCase()
                    .catch { e ->
                        _error.value = e.message ?: "Failed to load templates"
                    }
                    .collect { templatesList ->
                        _templates.value = templatesList
                    }
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to load templates"
            }
        }
    }

    fun saveSequence(
        id: Long = 0,
        name: String,
        description: String? = null,
        isActive: Boolean = true,
        steps: List<SequenceStep>
    ) {
        // Walidacja
        if (name.isBlank()) {
            _error.value = "Sequence name is required"
            return
        }

        if (steps.isEmpty()) {
            _error.value = "At least one step is required"
            return
        }

        val now = Date()

        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                // Zapisz sekwencję
                val sequenceEntity = SequenceEntity(
                    id = id,
                    name = name,
                    description = description,
                    isActive = isActive,
                    createdAt = if (id > 0) sequence.value?.createdAt ?: now else now,
                    updatedAt = now
                )

                val sequenceId = sequenceDao.insertSequence(sequenceEntity)

                // Przekształć kroki do encji i zapisz
                val stepEntities = steps.mapIndexed { index, step ->
                    SequenceStepEntity(
                        id = step.id,
                        sequenceId = sequenceId,
                        type = step.type,
                        templateId = step.templateId,
                        order = index, // Aktualizuj kolejność
                        delayDays = step.delayDays,
                        delayHours = step.delayHours,
                        condition = step.condition,
                        isActive = step.isActive,
                        createdAt = if (step.id > 0) step.createdAt else now,
                        updatedAt = now
                    )
                }

                // Najpierw usuń istniejące kroki
                if (id > 0) {
                    sequenceDao.deleteAllStepsForSequence(id)
                }

                // Dodaj nowe kroki
                stepEntities.forEach { step ->
                    sequenceDao.insertSequenceStep(step)
                }

                _saveSuccess.value = true
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to save sequence"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteSequence() {
        val currentSequence = _sequence.value ?: return

        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                // Konwersja z domeny do encji
                val sequenceEntity = SequenceEntity(
                    id = currentSequence.id,
                    name = currentSequence.name,
                    description = currentSequence.description,
                    isActive = currentSequence.isActive,
                    createdAt = currentSequence.createdAt,
                    updatedAt = currentSequence.updatedAt
                )

                // Usuń sekwencję - kroki zostaną usunięte automatycznie dzięki relacji z CASCADE
                sequenceDao.deleteSequence(sequenceEntity)
                _saveSuccess.value = true
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to delete sequence"
            } finally {
                _isLoading.value = false
            }
        }
    }
}