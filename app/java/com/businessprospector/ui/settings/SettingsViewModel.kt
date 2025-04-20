package com.businessprospector.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor() : ViewModel() {

    private val _settings = MutableStateFlow(AppSettings())
    val settings: StateFlow<AppSettings> = _settings

    init {
        loadSettings()
    }

    private fun loadSettings() {
        viewModelScope.launch {
            // W rzeczywistej implementacji, te ustawienia byłyby ładowane
            // z SharedPreferences, DataStore lub innego źródła

            // Przykładowe ustawienia
            _settings.value = AppSettings(
                googleApiConfigured = true,
                llmApiConfigured = true,
                emailConfigured = true,
                smsConfigured = false,
                dataEncryptionEnabled = true,
                gdprCompliance = true,
                notificationsEnabled = true
            )
        }
    }

    fun updateDataEncryption(enabled: Boolean) {
        _settings.value = _settings.value.copy(dataEncryptionEnabled = enabled)
        saveSettings()
    }

    fun updateGdprCompliance(enabled: Boolean) {
        _settings.value = _settings.value.copy(gdprCompliance = enabled)
        saveSettings()
    }

    fun updateNotifications(enabled: Boolean) {
        _settings.value = _settings.value.copy(notificationsEnabled = enabled)
        saveSettings()
    }

    private fun saveSettings() {
        viewModelScope.launch {
            // W rzeczywistej implementacji, zapisywalibyśmy ustawienia
            // do SharedPreferences, DataStore lub innego źródła
        }
    }
}

data class AppSettings(
    val googleApiConfigured: Boolean = false,
    val llmApiConfigured: Boolean = false,
    val emailConfigured: Boolean = false,
    val smsConfigured: Boolean = false,
    val dataEncryptionEnabled: Boolean = true,
    val gdprCompliance: Boolean = true,
    val notificationsEnabled: Boolean = true
)