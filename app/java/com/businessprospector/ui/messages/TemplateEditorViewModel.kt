package com.businessprospector.ui.messages

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.businessprospector.data.local.dao.MessageDao
import com.businessprospector.data.local.entity.MessageTemplateEntity
import com.businessprospector.domain.model.MessageTemplate
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.Date
import javax.inject.Inject

@HiltViewModel
class TemplateEditorViewModel @Inject constructor(
    private val messageDao: MessageDao
) : ViewModel() {

    private val _template = MutableStateFlow<MessageTemplate?>(null)
    val template: StateFlow<MessageTemplate?> = _template

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _saveSuccess = MutableStateFlow(false)
    val saveSuccess: StateFlow<Boolean> = _saveSuccess

    fun loadTemplate(templateId: Long) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                val templateEntity = messageDao.getMessageTemplateById(templateId)

                if (templateEntity != null) {
                    _template.value = MessageTemplate(
                        id = templateEntity.id,
                        name = templateEntity.name,
                        type = templateEntity.type,
                        subject = templateEntity.subject,
                        content = templateEntity.content,
                        variables = templateEntity.variables,
                        category = templateEntity.category,
                        createdAt = templateEntity.createdAt,
                        updatedAt = templateEntity.updatedAt
                    )
                } else {
                    _error.value = "Template not found"
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to load template"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun saveTemplate(
        id: Long = 0,
        name: String,
        type: String,
        subject: String? = null,
        content: String,
        variables: List<String> = emptyList(),
        category: String? = null
    ) {
        // Walidacja
        if (name.isBlank()) {
            _error.value = "Template name is required"
            return
        }

        if (content.isBlank()) {
            _error.value = "Template content is required"
            return
        }

        val now = Date()

        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                val templateEntity = MessageTemplateEntity(
                    id = id,
                    name = name,
                    type = type,
                    subject = subject,
                    content = content,
                    variables = variables,
                    category = category,
                    createdAt = if (id > 0) template.value?.createdAt ?: now else now,
                    updatedAt = now
                )

                val templateId = messageDao.insertMessageTemplate(templateEntity)

                if (templateId > 0) {
                    _saveSuccess.value = true
                } else {
                    _error.value = "Failed to save template"
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to save template"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteTemplate() {
        val currentTemplate = _template.value ?: return

        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                // Konwersja z domeny do encji
                val templateEntity = MessageTemplateEntity(
                    id = currentTemplate.id,
                    name = currentTemplate.name,
                    type = currentTemplate.type,
                    subject = currentTemplate.subject,
                    content = currentTemplate.content,
                    variables = currentTemplate.variables,
                    category = currentTemplate.category,
                    createdAt = currentTemplate.createdAt,
                    updatedAt = currentTemplate.updatedAt
                )

                messageDao.deleteMessageTemplate(templateEntity)
                _saveSuccess.value = true
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to delete template"
            } finally {
                _isLoading.value = false
            }
        }
    }
}