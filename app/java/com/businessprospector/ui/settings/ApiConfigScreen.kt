package com.businessprospector.ui.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ApiConfigScreen(
    navController: NavController,
    viewModel: ApiConfigViewModel = hiltViewModel()
) {
    val apiType = remember { navController.currentBackStackEntry?.arguments?.getString("type") ?: "google" }
    val title = when (apiType) {
        "google" -> "Google Search API"
        "llm" -> "Language Models API"
        "email" -> "Email Settings"
        "sms" -> "SMS Settings"
        else -> "API Configuration"
    }

    val configState by viewModel.configState.collectAsState()
    val isSaving by viewModel.isSaving.collectAsState()
    val saveSuccess by viewModel.saveSuccess.collectAsState()
    val error by viewModel.error.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Obsługa zapisania i błędów
    LaunchedEffect(saveSuccess, error) {
        if (saveSuccess) {
            scope.launch {
                snackbarHostState.showSnackbar("Configuration saved successfully")
                navController.popBackStack()
            }
        }

        error?.let {
            scope.launch {
                snackbarHostState.showSnackbar("Error: $it")
            }
        }
    }

    // Pobierz konfigurację dla wybranego typu API
    LaunchedEffect(apiType) {
        viewModel.loadConfig(apiType)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            when (apiType) {
                "google" -> GoogleSearchApiConfig(
                    configState = configState,
                    onApiKeyChange = { viewModel.updateField("apiKey", it) },
                    onSearchEngineIdChange = { viewModel.updateField("searchEngineId", it) },
                    onSave = { viewModel.saveConfig(apiType) },
                    isSaving = isSaving
                )

                "llm" -> LlmApiConfig(
                    configState = configState,
                    onProviderChange = { viewModel.updateField("provider", it) },
                    onModelChange = { viewModel.updateField("model", it) },
                    onApiKeyChange = { viewModel.updateField("apiKey", it) },
                    onSave = { viewModel.saveConfig(apiType) },
                    isSaving = isSaving
                )

                "email" -> EmailConfig(
                    configState = configState,
                    onSmtpServerChange = { viewModel.updateField("smtpServer", it) },
                    onSmtpPortChange = { viewModel.updateField("smtpPort", it) },
                    onUsernameChange = { viewModel.updateField("username", it) },
                    onPasswordChange = { viewModel.updateField("password", it) },
                    onUseTlsChange = { viewModel.updateField("useTls", it) },
                    onSave = { viewModel.saveConfig(apiType) },
                    isSaving = isSaving
                )

                "sms" -> SmsConfig(
                    configState = configState,
                    onProviderChange = { viewModel.updateField("provider", it) },
                    onApiKeyChange = { viewModel.updateField("apiKey", it) },
                    onAccountSidChange = { viewModel.updateField("accountSid", it) },
                    onAuthTokenChange = { viewModel.updateField("authToken", it) },
                    onPhoneNumberChange = { viewModel.updateField("phoneNumber", it) },
                    onSave = { viewModel.saveConfig(apiType) },
                    isSaving = isSaving
                )
            }
        }
    }
}

@Composable
fun GoogleSearchApiConfig(
    configState: Map<String, Any>,
    onApiKeyChange: (String) -> Unit,
    onSearchEngineIdChange: (String) -> Unit,
    onSave: () -> Unit,
    isSaving: Boolean
) {
    val apiKey = configState["apiKey"] as? String ?: ""
    val searchEngineId = configState["searchEngineId"] as? String ?: ""

    var passwordVisible by remember { mutableStateOf(false) }

    ConfigSection(title = "Google Search API Configuration") {
        Text(
            text = "Enter your Google Search API credentials to enable contact search functionality.",
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = apiKey,
            onValueChange = onApiKeyChange,
            label = { Text("API Key") },
            modifier = Modifier.fillMaxWidth(),
            trailingIcon = {
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(
                        imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        contentDescription = if (passwordVisible) "Hide API Key" else "Show API Key"
                    )
                }
            },
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation()
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = searchEngineId,
            onValueChange = onSearchEngineIdChange,
            label = { Text("Search Engine ID (CX)") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "You can obtain these credentials from the Google Cloud Console. Create a project, enable the Custom Search API, and create credentials.",
            style = MaterialTheme.typography.bodySmall
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onSave,
            modifier = Modifier.fillMaxWidth(),
            enabled = !isSaving && apiKey.isNotBlank() && searchEngineId.isNotBlank()
        ) {
            if (isSaving) {
                Text("Saving...")
            } else {
                Icon(Icons.Default.Check, contentDescription = null)
                Spacer(modifier = Modifier.padding(horizontal = 4.dp))
                Text("Save Configuration")
            }
        }
    }
}

@Composable
fun LlmApiConfig(
    configState: Map<String, Any>,
    onProviderChange: (String) -> Unit,
    onModelChange: (String) -> Unit,
    onApiKeyChange: (String) -> Unit,
    onSave: () -> Unit,
    isSaving: Boolean
) {
    val provider = configState["provider"] as? String ?: "openai"
    val model = configState["model"] as? String ?: "gpt-4"
    val apiKey = configState["apiKey"] as? String ?: ""

    var passwordVisible by remember { mutableStateOf(false) }

    ConfigSection(title = "Language Model API Configuration") {
        Text(
            text = "Configure your language model API settings to enable text generation and analysis.",
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Provider Selection
        LlmProviderSelector(
            selectedProvider = provider,
            onProviderSelected = onProviderChange
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Model Selection (zależne od wybranego dostawcy)
        LlmModelSelector(
            provider = provider,
            selectedModel = model,
            onModelSelected = onModelChange
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = apiKey,
            onValueChange = onApiKeyChange,
            label = { Text("API Key") },
            modifier = Modifier.fillMaxWidth(),
            trailingIcon = {
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(
                        imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        contentDescription = if (passwordVisible) "Hide API Key" else "Show API Key"
                    )
                }
            },
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "You can obtain an API key from ${getProviderName(provider)}.",
            style = MaterialTheme.typography.bodySmall
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onSave,
            modifier = Modifier.fillMaxWidth(),
            enabled = !isSaving && apiKey.isNotBlank()
        ) {
            if (isSaving) {
                Text("Saving...")
            } else {
                Icon(Icons.Default.Check, contentDescription = null)
                Spacer(modifier = Modifier.padding(horizontal = 4.dp))
                Text("Save Configuration")
            }
        }
    }
}

@Composable
fun EmailConfig(
    configState: Map<String, Any>,
    onSmtpServerChange: (String) -> Unit,
    onSmtpPortChange: (String) -> Unit,
    onUsernameChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onUseTlsChange: (String) -> Unit,
    onSave: () -> Unit,
    isSaving: Boolean
) {
    val smtpServer = configState["smtpServer"] as? String ?: ""
    val smtpPort = configState["smtpPort"] as? String ?: "587"
    val username = configState["username"] as? String ?: ""
    val password = configState["password"] as? String ?: ""
    val useTls = configState["useTls"] as? Boolean ?: true

    var passwordVisible by remember { mutableStateOf(false) }

    ConfigSection(title = "Email Configuration") {
        Text(
            text = "Configure your email settings to send emails directly from the application.",
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = smtpServer,
            onValueChange = onSmtpServerChange,
            label = { Text("SMTP Server") },
            placeholder = { Text("smtp.gmail.com") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = smtpPort,
            onValueChange = onSmtpPortChange,
            label = { Text("SMTP Port") },
            placeholder = { Text("587") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = username,
            onValueChange = onUsernameChange,
            label = { Text("Email Address") },
            placeholder = { Text("your.email@example.com") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = password,
            onValueChange = onPasswordChange,
            label = { Text("Password") },
            modifier = Modifier.fillMaxWidth(),
            trailingIcon = {
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(
                        imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        contentDescription = if (passwordVisible) "Hide Password" else "Show Password"
                    )
                }
            },
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation()
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onSave,
            modifier = Modifier.fillMaxWidth(),
            enabled = !isSaving && smtpServer.isNotBlank() && smtpPort.isNotBlank() &&
                    username.isNotBlank() && password.isNotBlank()
        ) {
            if (isSaving) {
                Text("Saving...")
            } else {
                Icon(Icons.Default.Check, contentDescription = null)
                Spacer(modifier = Modifier.padding(horizontal = 4.dp))
                Text("Save Configuration")
            }
        }
    }
}

@Composable
fun SmsConfig(
    configState: Map<String, Any>,
    onProviderChange: (String) -> Unit,
    onApiKeyChange: (String) -> Unit,
    onAccountSidChange: (String) -> Unit,
    onAuthTokenChange: (String) -> Unit,
    onPhoneNumberChange: (String) -> Unit,
    onSave: () -> Unit,
    isSaving: Boolean
) {
    val provider = configState["provider"] as? String ?: "twilio"
    val apiKey = configState["apiKey"] as? String ?: ""
    val accountSid = configState["accountSid"] as? String ?: ""
    val authToken = configState["authToken"] as? String ?: ""
    val phoneNumber = configState["phoneNumber"] as? String ?: ""

    var passwordVisible by remember { mutableStateOf(false) }

    ConfigSection(title = "SMS Configuration") {
        Text(
            text = "Configure your SMS provider settings to send text messages directly from the application.",
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Provider Selection
        SmsProviderSelector(
            selectedProvider = provider,
            onProviderSelected = onProviderChange
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (provider == "twilio") {
            OutlinedTextField(
                value = accountSid,
                onValueChange = onAccountSidChange,
                label = { Text("Account SID") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = authToken,
                onValueChange = onAuthTokenChange,
                label = { Text("Auth Token") },
                modifier = Modifier.fillMaxWidth(),
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = if (passwordVisible) "Hide Token" else "Show Token"
                        )
                    }
                },
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation()
            )
        } else {
            OutlinedTextField(
                value = apiKey,
                onValueChange = onApiKeyChange,
                label = { Text("API Key") },
                modifier = Modifier.fillMaxWidth(),
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = if (passwordVisible) "Hide API Key" else "Show API Key"
                        )
                    }
                },
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation()
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = phoneNumber,
            onValueChange = onPhoneNumberChange,
            label = { Text("Sender Phone Number") },
            placeholder = { Text("+1234567890") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onSave,
            modifier = Modifier.fillMaxWidth(),
            enabled = when (provider) {
                "twilio" -> !isSaving && accountSid.isNotBlank() && authToken.isNotBlank() && phoneNumber.isNotBlank()
                else -> !isSaving && apiKey.isNotBlank() && phoneNumber.isNotBlank()
            }
        ) {
            if (isSaving) {
                Text("Saving...")
            } else {
                Icon(Icons.Default.Check, contentDescription = null)
                Spacer(modifier = Modifier.padding(horizontal = 4.dp))
                Text("Save Configuration")
            }
        }
    }
}

@Composable
fun ConfigSection(
    title: String,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            content()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LlmProviderSelector(
    selectedProvider: String,
    onProviderSelected: (String) -> Unit
) {
    val providers = listOf("openai", "claude", "gemini", "mistral")

    var expanded by remember { mutableStateOf(false) }

    OutlinedTextField(
        value = getProviderName(selectedProvider),
        onValueChange = {},
        readOnly = true,
        label = { Text("LLM Provider") },
        modifier = Modifier.fillMaxWidth(),
        trailingIcon = {
            IconButton(onClick = { expanded = !expanded }) {
                androidx.compose.material.icons.Icons.Default.ArrowDropDown
            }
        }
    )

    androidx.compose.material3.DropdownMenu(
        expanded = expanded,
        onDismissRequest = { expanded = false }
    ) {
        providers.forEach { provider ->
            androidx.compose.material3.DropdownMenuItem(
                text = { Text(getProviderName(provider)) },
                onClick = {
                    onProviderSelected(provider)
                    expanded = false
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LlmModelSelector(
    provider: String,
    selectedModel: String,
    onModelSelected: (String) -> Unit
) {
    val models = when (provider) {
        "openai" -> listOf("gpt-4", "gpt-4-turbo", "gpt-3.5-turbo")
        "claude" -> listOf("claude-3-opus", "claude-3-sonnet", "claude-3-haiku")
        "gemini" -> listOf("gemini-pro", "gemini-ultra")
        "mistral" -> listOf("mistral-7b", "mistral-large")
        else -> listOf("gpt-4")
    }

    var expanded by remember { mutableStateOf(false) }

    OutlinedTextField(
        value = selectedModel,
        onValueChange = {},
        readOnly = true,
        label = { Text("Model") },
        modifier = Modifier.fillMaxWidth(),
        trailingIcon = {
            IconButton(onClick = { expanded = !expanded }) {
                androidx.compose.material.icons.Icons.Default.ArrowDropDown
            }
        }
    )

    androidx.compose.material3.DropdownMenu(
        expanded = expanded,
        onDismissRequest = { expanded = false }
    ) {
        models.forEach { model ->
            androidx.compose.material3.DropdownMenuItem(
                text = { Text(model) },
                onClick = {
                    onModelSelected(model)
                    expanded = false
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SmsProviderSelector(
    selectedProvider: String,
    onProviderSelected: (String) -> Unit
) {
    val providers = listOf("twilio", "nexmo", "messagebird")

    var expanded by remember { mutableStateOf(false) }

    OutlinedTextField(
        value = when (selectedProvider) {
            "twilio" -> "Twilio"
            "nexmo" -> "Nexmo (Vonage)"
            "messagebird" -> "MessageBird"
            else -> "Twilio"
        },
        onValueChange = {},
        readOnly = true,
        label = { Text("SMS Provider") },
        modifier = Modifier.fillMaxWidth(),
        trailingIcon = {
            IconButton(onClick = { expanded = !expanded }) {
                androidx.compose.material.icons.Icons.Default.ArrowDropDown
            }
        }
    )

    androidx.compose.material3.DropdownMenu(
        expanded = expanded,
        onDismissRequest = { expanded = false }
    ) {
        providers.forEach { provider ->
            androidx.compose.material3.DropdownMenuItem(
                text = {
                    Text(
                        when (provider) {
                            "twilio" -> "Twilio"
                            "nexmo" -> "Nexmo (Vonage)"
                            "messagebird" -> "MessageBird"
                            else -> provider
                        }
                    )
                },
                onClick = {
                    onProviderSelected(provider)
                    expanded = false
                }
            )
        }
    }
}

private fun getProviderName(provider: String): String {
    return when (provider) {
        "openai" -> "OpenAI"
        "claude" -> "Anthropic Claude"
        "gemini" -> "Google Gemini"
        "mistral" -> "Mistral AI"
        else -> provider.replaceFirstChar { it.uppercase() }
    }
}