package com.businessprospector.ui.sequences

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowRight
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.businessprospector.domain.model.Sequence
import com.businessprospector.domain.model.SequenceStep
import com.businessprospector.ui.common.components.EmptyState
import com.businessprospector.ui.common.components.ErrorMessage
import com.businessprospector.ui.common.components.LoadingIndicator

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SequencesScreen(
    navController: NavController,
    viewModel: SequencesViewModel = hiltViewModel()
) {
    val sequences by viewModel.sequences.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Communication Sequences") }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate("sequences/editor/0") }
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Sequence")
            }
        }
    ) { paddingValues ->
        when {
            isLoading -> {
                LoadingIndicator()
            }
            error != null -> {
                ErrorMessage(error ?: "Unknown error")
            }
            sequences.isEmpty() -> {
                EmptyState(
                    message = "No sequences yet",
                    secondaryMessage = "Create automated communication sequences to streamline your outreach"
                )
            }
            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(16.dp)
                ) {
                    items(sequences) { sequence ->
                        SequenceItem(
                            sequence = sequence,
                            onSequenceClick = { navController.navigate("sequences/editor/${sequence.id}") },
                            onToggleActive = { viewModel.toggleSequenceActive(sequence.id) }
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun SequenceItem(
    sequence: Sequence,
    onSequenceClick: () -> Unit,
    onToggleActive: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onSequenceClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = sequence.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Switch(
                    checked = sequence.isActive,
                    onCheckedChange = { onToggleActive() }
                )
            }

            sequence.description?.let {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (sequence.steps.isNotEmpty()) {
                SequenceStepsPreview(sequence.steps)
            } else {
                Text(
                    text = "No steps defined yet",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                IconButton(
                    onClick = onSequenceClick
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowRight,
                        contentDescription = "Edit Sequence",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
fun SequenceStepsPreview(steps: List<SequenceStep>) {
    Column {
        steps.take(3).forEachIndexed { index, step ->
            if (index > 0) {
                // Strzałka wskazująca przepływ
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "↓ Wait ${formatDelay(step.delayDays, step.delayHours)} ↓",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Ikona typu kroku
                StepTypeIcon(step.type)

                Spacer(modifier = Modifier.width(8.dp))

                // Numer i opis kroku
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "Step ${index + 1}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = getStepDescription(step),
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                // Status aktywności
                if (!step.isActive) {
                    Box(
                        modifier = Modifier
                            .background(
                                color = Color.Gray.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(4.dp)
                            )
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "Disabled",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                    }
                }
            }
        }

        // Jeśli jest więcej kroków
        if (steps.size > 3) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "+ ${steps.size - 3} more steps",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
fun StepTypeIcon(type: String) {
    val (icon, color) = when (type.lowercase()) {
        "email" -> Pair(Icons.Default.Email, MaterialTheme.colorScheme.primary)
        "sms" -> Pair(Icons.Default.Email, MaterialTheme.colorScheme.secondary) // Można zastąpić ikoną SMS
        "call" -> Pair(Icons.Default.Phone, MaterialTheme.colorScheme.tertiary)
        else -> Pair(Icons.Default.Email, MaterialTheme.colorScheme.primary)
    }

    Box(
        modifier = Modifier
            .size(32.dp)
            .clip(CircleShape)
            .background(color.copy(alpha = 0.2f)),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = type,
            tint = color,
            modifier = Modifier.size(18.dp)
        )
    }
}

private fun getStepDescription(step: SequenceStep): String {
    return when (step.type.lowercase()) {
        "email" -> "Send email"
        "sms" -> "Send SMS"
        "call" -> "Make call"
        else -> step.type
    } + (step.condition?.let { " (if $it)" } ?: "")
}

private fun formatDelay(days: Int, hours: Int): String {
    val parts = mutableListOf<String>()

    if (days > 0) {
        parts.add("$days ${if (days == 1) "day" else "days"}")
    }

    if (hours > 0) {
        parts.add("$hours ${if (hours == 1) "hour" else "hours"}")
    }

    return if (parts.isEmpty()) "immediately" else parts.joinToString(" ")
}