package com.businessprospector.ui.messages

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TemplateEditorScreen(
    templateId: Long?,
    navController: NavController,
    viewModel: TemplateEditorViewModel = hiltViewModel()
) {
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val saveSuccess by viewModel.saveSuccess.collectAsState()

    // Stan formularza
    var templateName by remember { mutableStateOf("") }
    var templateType by remember { mutableStateOf("email") }
    var templateSubject by remember { mutableStateOf("") }
    var templateContent by remember { mutableStateOf("") }
    var templateVariables by remember { mutableStateOf("") }
    var templateCategory by remember { mutableStateOf("") }

    var expanded by remember { mutableStateOf(false) }

    // Pobierz dane szablonu, jeśli edytujemy istniejący
    LaunchedEffect(templateId) {
        if (templateId != null && templateId > 0) {
            viewModel.loadTemplate(templateId)
        }
    }

    // Obserwuj dane
    val template by viewModel.template.collectAsState()

    // Wypełnij formularz danymi, gdy są dostępne
    LaunchedEffect(template) {
        template?.let {
            templateName = it.name
            templateType = it.type
            templateSubject = it.subject ?: ""
            templateContent = it.content
            templateVariables = it.variables.joinToString(", ")
            templateCategory = it.category ?: ""
        }
    }

    // Obsługa udanego zapisu
    LaunchedEffect(saveSuccess) {
        if (saveSuccess) {
            navController.popBackStack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(if (templateId != null && templateId > 0) "Edit Template" else "New Template")
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (templateId != null && templateId > 0) {
                        IconButton(
                            onClick = { viewModel.deleteTemplate() }
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete")
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    // Przygotuj zmienne z tekstu
                    val variablesList = templateVariables
                        .split(",")
                        .map { it.trim() }
                        .filter { it.isNotEmpty() }

                    viewModel.saveTemplate(
                        id = templateId ?: 0,
                        name = templateName,
                        type = templateType,
                        subject = templateSubject.takeIf { it.isNotEmpty() },
                        content = templateContent,
                        variables = variablesList,
                        category = templateCategory.takeIf { it.isNotEmpty() }
                    )
                }
            ) {
                Icon(Icons.Default.Check, contentDescription = "Save")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            } else {
                // Nazwa szablonu
                OutlinedTextField(
                    value = templateName,
                    onValueChange = { templateName = it },
                    label = { Text("Template Name") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = templateName.isEmpty()
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Typ szablonu
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    TextField(
                        value = when (templateType) {
                            "email" -> "Email"
                            "sms" -> "SMS"
                            "call_script" -> "Call Script"
                            else -> templateType
                        },
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Template Type") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )

                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Email") },
                            onClick = {
                                templateType = "email"
                                expanded = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("SMS") },
                            onClick = {
                                templateType = "sms"
                                expanded = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Call Script") },
                            onClick = {
                                templateType = "call_script"
                                expanded = false
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Temat (tylko dla emaili)
                if (templateType == "email") {
                    OutlinedTextField(
                        value = templateSubject,
                        onValueChange = { templateSubject = it },
                        label = { Text("Subject") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Treść szablonu
                OutlinedTextField(
                    value = templateContent,
                    onValueChange = { templateContent = it },
                    label = { Text("Content") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    isError = templateContent.isEmpty()
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Zmienne szablonu
                OutlinedTextField(
                    value = templateVariables,
                    onValueChange = { templateVariables = it },
                    label = { Text("Variables (comma-separated)") },
                    placeholder = { Text("name, company, date") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Kategoria
                OutlinedTextField(
                    value = templateCategory,
                    onValueChange = { templateCategory = it },
                    label = { Text("Category") },
                    placeholder = { Text("initial_contact, follow_up, meeting_request") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Wyświetl informację o błędzie, jeśli występuje
                error?.let {
                    Text(
                        text = it,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Przewodnik dotyczący zmiennych
                Text(
                    text = "Variables Guide",
                    style = MaterialTheme.typography.titleMedium
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Use double curly braces to include variables in your template. For example: {{name}}, {{company}}",
                    style = MaterialTheme.typography.bodyMedium
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Available system variables: {{date}}, {{time}}, {{sender_name}}, {{sender_company}}",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}