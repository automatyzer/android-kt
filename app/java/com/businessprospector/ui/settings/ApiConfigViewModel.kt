package com.businessprospector.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ApiConfigViewModel @Inject constructor() : ViewModel() {

    private val _configState = MutableStateFlow<Map<String, Any>>(emptyMap())
    val configState: StateFlow<Map<String, Any>> = _configState

    private val _isSaving = MutableStateFlow(false)
    val isSaving: StateFlow<Boolean> = _isSaving

    private val _saveSuccess = MutableStateFlow(false)
    val saveSuccess: StateFlow<Boolean> = _saveSuccess

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    fun loadConfig(type: String) {
        viewModelScope.launch {
            // W rzeczywistej implementacji, konfiguracja byłaby ładowana
            // z SharedPreferences, DataStore lub innego źródła

            // Przykładowe dane konfiguracyjne dla celów demonstracyjnych
            val config = when (type) {
                "google" -> mapOf(
                    "apiKey" to "AIzaSyDxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx",
                    "searchEngineId" to "123456789:abcdefghij"
                )
                "llm" -> mapOf(
                    "provider" to "openai",
                    "model" to "gpt-4",
                    "apiKey" to "sk-xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx"
                )
                "email" -> mapOf(
                    "smtpServer" to "smtp.gmail.com",
                    "smtpPort" to "587",
                    "username" to "youremail@gmail.com",
                    "password" to "yourapppassword",
                    "useTls" to true
                )
                "sms" -> mapOf(
                    "provider" to "twilio",
                    "accountSid" to "ACxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx",
                    "authToken" to "xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx",
                    "phoneNumber" to "+1234567890"
                )
                else -> emptyMap()
            }

            _configState.value = config
        }
    }

    fun updateField(field: String, value: Any) {
        val currentConfig = _configState.value.toMutableMap()
        currentConfig[field] = value
        _configState.value = currentConfig
    }

    fun saveConfig(type: String) {
        viewModelScope.launch {
            _isSaving.value = true
            _error.value = null
            _saveSuccess.value = false

            try {
                // Symulacja zapisywania konfiguracji
                delay(1000) // Symulacja opóźnienia sieciowego

                // W rzeczywistej implementacji, konfiguracja byłaby zapisywana
                // do SharedPreferences, DataStore lub innego źródła

                // Symulacja walidacji
                validateConfig(type)

                // Jeśli walidacja przeszła pomyślnie, oznacz sukces
                _saveSuccess.value = true
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isSaving.value = false
            }
        }
    }

    private fun validateConfig(type: String) {
        val config = _configState.value

        when (type) {
            "google" -> {
                val apiKey = config["apiKey"] as? String ?: ""
                val searchEngineId = config["searchEngineId"] as? String ?: ""

                if (apiKey.isBlank()) {
                    throw IllegalArgumentException("API Key is required")
                }

                if (searchEngineId.isBlank()) {
                    throw IllegalArgumentException("Search Engine ID is required")
                }
            }
            "llm" -> {
                val provider = config["provider"] as? String ?: ""
                val model = config["model"] as? String ?: ""
                val apiKey = config["apiKey"] as? String ?: ""

                if (provider.isBlank()) {
                    throw IllegalArgumentException("Provider is required")
                }

                if (model.isBlank()) {
                    throw IllegalArgumentException("Model is required")
                }

                if (apiKey.isBlank()) {
                    throw IllegalArgumentException("API Key is required")
                }
            }
            "email" -> {
                val smtpServer = config["smtpServer"] as? String ?: ""
                val smtpPort = config["smtpPort"] as? String ?: ""
                val username = config["username"] as? String ?: ""
                val password = config["password"] as? String ?: ""

                if (smtpServer.isBlank()) {
                    throw IllegalArgumentException("SMTP Server is required")
                }

                if (smtpPort.isBlank()) {
                    throw IllegalArgumentException("SMTP Port is required")
                }

                if (username.isBlank()) {
                    throw IllegalArgumentException("Username is required")
                }

                if (password.isBlank()) {
                    throw IllegalArgumentException("Password is required")
                }
            }