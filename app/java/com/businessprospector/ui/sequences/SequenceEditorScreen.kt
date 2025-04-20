package com.businessprospector.ui.sequences

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
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
import androidx.compose.material3.Switch
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.businessprospector.domain.model.MessageTemplate
import com.businessprospector.domain.model.SequenceStep
import com.businessprospector.ui.common.components.LoadingIndicator

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SequenceEditorScreen(
    sequenceId: Long?,
    navController: NavController,
    viewModel: SequenceEditorViewModel = hiltViewModel()
) {
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val saveSuccess by viewModel.saveSuccess.collectAsState()
    val templates by viewModel.templates.collectAsState()

    // Stan formularza
    var sequenceName by remember { mutableStateOf("") }
    var sequenceDescription by remember { mutableStateOf("") }
    var sequenceIsActive by remember { mutableStateOf(true) }

    // Stan kroków sekwencji
    var steps by remember { mutableStateOf<List<SequenceStep>>(emptyList()) }

    // Dialog do dodawania/edytowania kroków
    var showStepDialog by remember { mutableStateOf(false) }
    var editingStepIndex by remember { mutableStateOf<Int?>(null) }

    // Dialog potwierdzenia usunięcia
    var showDeleteDialog by remember { mutableStateOf(false) }

    // Pobierz dane sekwencji, jeśli edytujemy istniejącą
    LaunchedEffect(sequenceId) {
        if (sequenceId != null && sequenceId > 0) {
            viewModel.loadSequence(sequenceId)
        }

        // Zawsze pobierz dostępne szablony
        viewModel.loadTemplates()
    }

    // Obserwuj dane
    val sequence by viewModel.sequence.collectAsState()

    // Wypełnij formularz danymi, gdy są dostępne
    LaunchedEffect(sequence) {
        sequence?.let {
            sequenceName = it.name
            sequenceDescription = it.description ?: ""
            sequenceIsActive = it.isActive
            steps = it.steps
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
                    Text(if (sequenceId != null && sequenceId > 0) "Edit Sequence" else "New Sequence")
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (sequenceId != null && sequenceId > 0) {
                        IconButton(
                            onClick = { showDeleteDialog = true }
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
                    viewModel.saveSequence(
                        id = sequenceId ?: 0,
                        name = sequenceName,
                        description = sequenceDescription.takeIf { it.isNotEmpty() },
                        isActive = sequenceIsActive,
                        steps = steps
                    )
                }
            ) {
                Icon(Icons.Default.Check, contentDescription = "Save")
            }
        }
    ) { paddingValues ->
        if (isLoading) {
            LoadingIndicator()
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp)
            ) {
                // Podstawowe informacje o sekwencji
                OutlinedTextField(
                    value = sequenceName,
                    onValueChange = { sequenceName = it },
                    label = { Text("Sequence Name") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = sequenceName.isEmpty()
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = sequenceDescription,
                    onValueChange = { sequenceDescription = it },
                    label = { Text("Description (optional)") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Active",
                        style = MaterialTheme.typography.bodyLarge
                    )

                    Switch(
                        checked = sequenceIsActive,
                        onCheckedChange = { sequenceIsActive = it }
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Sekcja kroków sekwencji
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Sequence Steps",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Button(
                        onClick = {
                            editingStepIndex = null
                            showStepDialog = true
                        }
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add Step")
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add Step")
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                if (steps.isEmpty()) {
                    // Komunikat, gdy brak kroków
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp)
                            .border(
                                width = 1.dp,
                                color = MaterialTheme.colorScheme.outline,
                                shape = RoundedCornerShape(8.dp)
                            )
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No steps added yet. Add steps to create your sequence.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    // Lista kroków
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    ) {
                        itemsIndexed(steps) { index, step ->
                            SequenceStepItem(
                                step = step,
                                stepNumber = index + 1,
                                canMoveUp = index > 0,
                                canMoveDown = index < steps.size - 1,
                                onEdit = {
                                    editingStepIndex = index
                                    showStepDialog = true
                                },
                                onDelete = {
                                    steps = steps.toMutableList().apply { removeAt(index) }
                                },
                                onMoveUp = {
                                    if (index > 0) {
                                        steps = steps.toMutableList().apply {
                                            val temp = this[index]
                                            this[index] = this[index - 1]
                                            this[index - 1] = temp
                                        }
                                    }
                                },
                                onMoveDown = {
                                    if (index < steps.size - 1) {
                                        steps = steps.toMutableList().apply {
                                            val temp = this[index]
                                            this[index] = this[index + 1]
                                            this[index + 1] = temp
                                        }
                                    }
                                },
                                onToggleActive = {
                                    steps = steps.toMutableList().apply {
                                        this[index] = this[index].copy(isActive = !this[index].isActive)
                                    }
                                }
                            )

                            if (index < steps.size - 1) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 8.dp),
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    val nextStep = steps[index + 1]
                                    val delayText = if (nextStep.delayDays > 0 || nextStep.delayHours > 0) {
                                        val days = if (nextStep.delayDays > 0) "${nextStep.delayDays}d " else ""
                                        val hours = if (nextStep.delayHours > 0) "${nextStep.delayHours}h" else ""
                                        "Wait $days$hours"
                                    } else {
                                        "Immediately"
                                    }

                                    Text(
                                        text = "↓ $delayText ↓",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }

                // Wyświetl informację o błędzie, jeśli występuje
                error?.let {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = it,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            // Dialog dodawania/edytowania kroków
            if (showStepDialog) {
                StepDialog(
                    step = editingStepIndex?.let { steps[it] },
                    templates = templates,
                    onDismiss = { showStepDialog = false },
                    onSave = { step ->
                        if (editingStepIndex != null) {
                            // Edytuj istniejący krok
                            steps = steps.toMutableList().apply {
                                this[editingStepIndex!!] = step
                            }
                        } else {
                            // Dodaj nowy krok
                            steps = steps + step
                        }
                        showStepDialog = false
                    }
                )
            }

            // Dialog potwierdzenia usunięcia
            if (showDeleteDialog) {
                AlertDialog(
                    onDismissRequest = { showDeleteDialog = false },
                    title = { Text("Delete Sequence") },
                    text = { Text("Are you sure you want to delete this sequence? This action cannot be undone.") },
                    confirmButton = {
                        Button(
                            onClick = {
                                viewModel.deleteSequence()
                                showDeleteDialog = false
                            }
                        ) {
                            Text("Delete")
                        }
                    },
                    dismissButton = {
                        Button(
                            onClick = { showDeleteDialog = false }
                        ) {
                            Text("Cancel")
                        }
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StepDialog(
    step: SequenceStep?,
    templates: List<MessageTemplate>,
    onDismiss: () -> Unit,
    onSave: (SequenceStep) -> Unit
) {
    // Stan formularza kroku
    var stepType by remember { mutableStateOf(step?.type ?: "email") }
    var templateId by remember { mutableStateOf(step?.templateId) }
    var delayDays by remember { mutableStateOf(step?.delayDays?.toString() ?: "0") }
    var delayHours by remember { mutableStateOf(step?.delayHours?.toString() ?: "0") }
    var condition by remember { mutableStateOf(step?.condition ?: "") }
    var isActive by remember { mutableStateOf(step?.isActive ?: true) }

    // Dropdowny
    var typeExpanded by remember { mutableStateOf(false) }
    var templateExpanded by remember { mutableStateOf(false) }

    // Filtruj szablony według typu
    val filteredTemplates = templates.filter { it.type == stepType }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (step != null) "Edit Step" else "Add Step") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                // Typ kroku
                Text(
                    text = "Step Type",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(4.dp))

                ExposedDropdownMenuBox(
                    expanded = typeExpanded,
                    onExpandedChange = { typeExpanded = !typeExpanded }
                ) {
                    TextField(
                        value = when (stepType) {
                            "email" -> "Email"
                            "sms" -> "SMS"
                            "call" -> "Call"
                            else -> stepType
                        },
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = typeExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )

                    ExposedDropdownMenu(
                        expanded = typeExpanded,
                        onDismissRequest = { typeExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Email") },
                            onClick = {
                                stepType = "email"
                                templateId = null // Reset wybranego szablonu
                                typeExpanded = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("SMS") },
                            onClick = {
                                stepType = "sms"
                                templateId = null // Reset wybranego szablonu
                                typeExpanded = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Call") },
                            onClick = {
                                stepType = "call"
                                templateId = null // Reset wybranego szablonu
                                typeExpanded = false
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Szablon
                Text(
                    text = "Template",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(4.dp))

                ExposedDropdownMenuBox(
                    expanded = templateExpanded,
                    onExpandedChange = { templateExpanded = !templateExpanded }
                ) {
                    TextField(
                        value = templateId?.let { id ->
                            templates.find { it.id == id }?.name ?: "Select template"
                        } ?: "Select template",
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = templateExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )

                    ExposedDropdownMenu(
                        expanded = templateExpanded,
                        onDismissRequest = { templateExpanded = false }
                    ) {
                        if (filteredTemplates.isEmpty()) {
                            DropdownMenuItem(
                                text = { Text("No templates available for this type") },
                                onClick = { templateExpanded = false }
                            )
                        } else {
                            filteredTemplates.forEach { template ->
                                DropdownMenuItem(
                                    text = { Text(template.name) },
                                    onClick = {
                                        templateId = template.id
                                        templateExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Opóźnienie
                Text(
                    text = "Delay before this step",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = delayDays,
                        onValueChange = {
                            if (it.isEmpty() || it.all { char -> char.isDigit() }) {
                                delayDays = it
                            }
                        },
                        label = { Text("Days") },
                        keyboardOptions = androidx.compose.ui.text.input.KeyboardOptions(
                            keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                        ),
                        modifier = Modifier.weight(1f)
                    )

                    OutlinedTextField(
                        value = delayHours,
                        onValueChange = {
                            if (it.isEmpty() || it.all { char -> char.isDigit() }) {
                                delayHours = it
                            }
                        },
                        label = { Text("Hours") },
                        keyboardOptions = androidx.compose.ui.text.input.KeyboardOptions(
                            keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                        ),
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Warunek
                Text(
                    text = "Condition (optional)",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(4.dp))

                OutlinedTextField(
                    value = condition,
                    onValueChange = { condition = it },
                    label = { Text("e.g. no_response, email_opened") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Aktywny
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Active",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Switch(
                        checked = isActive,
                        onCheckedChange = { isActive = it }
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    // Walidacja
                    if (templateId == null) {
                        // Można by tu wyświetlić komunikat o błędzie
                        return@Button
                    }

                    val newStep = SequenceStep(
                        id = step?.id ?: 0,
                        sequenceId = step?.sequenceId ?: 0, // To zostanie zaktualizowane przy zapisie sekwencji
                        type = stepType,
                        templateId = templateId,
                        order = step?.order ?: 0, // To zostanie zaktualizowane przy zapisie sekwencji
                        delayDays = delayDays.toIntOrNull() ?: 0,
                        delayHours = delayHours.toIntOrNull() ?: 0,
                        condition = condition.takeIf { it.isNotEmpty() },
                        isActive = isActive
                    )

                    onSave(newStep)
                }
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            Button(
                onClick = onDismiss
            ) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun SequenceStepItem(
    step: SequenceStep,
    stepNumber: Int,
    canMoveUp: Boolean,
    canMoveDown: Boolean,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    onToggleActive: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (step.isActive)
                MaterialTheme.colorScheme.surfaceVariant
            else
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Numer kroku
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stepNumber.toString(),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Typ kroku i szablon
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = when (step.type.lowercase()) {
                        "email" -> "Send Email"
                        "sms" -> "Send SMS"
                        "call" -> "Make Call"
                        else -> step.type
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "Template ID: ${step.templateId}",
                    style = MaterialTheme.typography.bodySmall
                )

                if (step.delayDays > 0 || step.delayHours > 0) {
                    Text(
                        text = "Delay: ${if (step.delayDays > 0) "${step.delayDays}d " else ""}${if (step.delayHours > 0) "${step.delayHours}h" else ""}",
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                step.condition?.let {
                    Text(
                        text = "Condition: $it",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            // Przyciski akcji
            Row {
                IconButton(
                    onClick = onEdit,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit Step",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete Step",
                        tint = MaterialTheme.colorScheme.error
                    )
                }

                IconButton(
                    onClick = onToggleActive,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = if (step.isActive) Icons.Default.Check else Icons.Default.Close,
                        contentDescription = if (step.isActive) "Disable Step" else "Enable Step",
                        tint = if (step.isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                    )
                }

                if (canMoveUp) {
                    IconButton(
                        onClick = onMoveUp,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowUpward,
                            contentDescription = "Move Up",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                if (canMoveDown) {
                    IconButton(
                        onClick = onMoveDown,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowDownward,
                            contentDescription = "Move Down",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}