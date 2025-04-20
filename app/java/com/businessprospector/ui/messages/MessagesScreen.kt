package com.businessprospector.ui.messages

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.businessprospector.domain.model.Message
import com.businessprospector.domain.model.MessageTemplate
import com.businessprospector.ui.common.components.EmptyState
import com.businessprospector.ui.common.components.ErrorMessage
import com.businessprospector.ui.common.components.LoadingIndicator
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessagesScreen(
    navController: NavController,
    viewModel: MessagesViewModel = hiltViewModel()
) {
    val messages by viewModel.messages.collectAsState()
    val templates by viewModel.templates.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    var selectedTabIndex by remember { mutableStateOf(0) }
    val tabs = listOf("Messages", "Templates")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Messages & Templates") }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    if (selectedTabIndex == 0) {
                        // Nawigacja do wysyłania nowej wiadomości
                        // W rzeczywistej implementacji powinno to być menu z opcjami (email, SMS, itp.)
                        navController.navigate("messages/new")
                    } else {
                        // Nawigacja do tworzenia nowego szablonu
                        navController.navigate("messages/template_editor/0")
                    }
                }
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            TabRow(
                selectedTabIndex = selectedTabIndex
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = { Text(title) }
                    )
                }
            }

            when {
                isLoading -> {
                    LoadingIndicator()
                }
                error != null -> {
                    ErrorMessage(error ?: "Unknown error")
                }
                else -> {
                    when (selectedTabIndex) {
                        0 -> MessagesTab(messages, navController)
                        1 -> TemplatesTab(templates, navController)
                    }
                }
            }
        }
    }
}

@Composable
fun MessagesTab(messages: List<Message>, navController: NavController) {
    if (messages.isEmpty()) {
        EmptyState(
            message = "No messages yet",
            secondaryMessage = "Start communicating with your contacts"
        )
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            items(messages) { message ->
                MessageListItem(message) {
                    navController.navigate("messages/${message.id}")
                }
                Divider()
            }
        }
    }
}

@Composable
fun MessageListItem(message: Message, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp, horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Ikona typu wiadomości
        MessageTypeIcon(message.type)

        Spacer(modifier = Modifier.size(16.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = message.subject ?: "No subject",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = formatDate(message.sentAt ?: message.createdAt),
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = message.content,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = if (message.direction == "outgoing") "To: Contact Name" else "From: Contact Name",
                    style = MaterialTheme.typography.bodySmall
                )

                MessageStatusBadge(message.status)
            }
        }
    }
}

@Composable
fun MessageTypeIcon(type: String) {
    val (icon, tint) = when (type) {
        "email" -> Pair(Icons.Default.Email, MaterialTheme.colorScheme.primary)
        "sms" -> Pair(Icons.Default.Sms, MaterialTheme.colorScheme.secondary)
        "call" -> Pair(Icons.Default.Call, MaterialTheme.colorScheme.tertiary)
        else -> Pair(Icons.Default.Email, MaterialTheme.colorScheme.primary)
    }

    Icon(
        imageVector = icon,
        contentDescription = type,
        tint = tint,
        modifier = Modifier.size(24.dp)
    )
}

@Composable
fun MessageStatusBadge(status: String) {
    val color = when (status) {
        "draft" -> Color.Gray
        "scheduled" -> Color.Blue
        "sent" -> Color(0xFF4CAF50) // Green
        "delivered" -> Color(0xFF2196F3) // Blue
        "failed" -> Color.Red
        else -> Color.Gray
    }

    Text(
        text = status.replaceFirstChar { it.uppercase() },
        style = MaterialTheme.typography.bodySmall,
        color = color
    )
}

@Composable
fun TemplatesTab(templates: List<MessageTemplate>, navController: NavController) {
    if (templates.isEmpty()) {
        EmptyState(
            message = "No templates yet",
            secondaryMessage = "Create templates to streamline your communication"
        )
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            items(templates) { template ->
                TemplateListItem(template) {
                    navController.navigate("messages/template_editor/${template.id}")
                }
                Divider()
            }
        }
    }
}

@Composable
fun TemplateListItem(template: MessageTemplate, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
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
                    text = template.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                TemplateTypeBadge(template.type)
            }

            Spacer(modifier = Modifier.height(8.dp))

            template.subject?.let {
                Text(
                    text = "Subject: $it",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(4.dp))
            }

            Text(
                text = template.content,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )

            if (template.variables.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Variables: ${template.variables.joinToString(", ")}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
fun TemplateTypeBadge(type: String) {
    val (backgroundColor, textColor, label) = when (type) {
        "email" -> Triple(
            MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
            MaterialTheme.colorScheme.primary,
            "Email"
        )
        "sms" -> Triple(
            MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f),
            MaterialTheme.colorScheme.secondary,
            "SMS"
        )
        "call_script" -> Triple(
            MaterialTheme.colorScheme.tertiary.copy(alpha = 0.2f),
            MaterialTheme.colorScheme.tertiary,
            "Call Script"
        )
        else -> Triple(
            Color.Gray.copy(alpha = 0.2f),
            Color.Gray,
            type.replaceFirstChar { it.uppercase() }
        )
    }

    Box(
        modifier = Modifier
            .background(
                color = backgroundColor,
                shape = androidx.compose.foundation.shape.RoundedCornerShape(4.dp)
            )
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = textColor
        )
    }
}

@Composable
fun NewMessageFloatingActionButton(
    onClick: () -> Unit
) {
    FloatingActionButton(
        onClick = onClick
    ) {
        Icon(Icons.Default.Add, contentDescription = "New Message")
    }
}

private fun formatDate(date: java.util.Date): String {
    val now = java.util.Calendar.getInstance()
    val messageDate = java.util.Calendar.getInstance().apply { time = date }

    return when {
        // Dziś
        now.get(java.util.Calendar.YEAR) == messageDate.get(java.util.Calendar.YEAR) &&
        now.get(java.util.Calendar.DAY_OF_YEAR) == messageDate.get(java.util.Calendar.DAY_OF_YEAR) ->
            SimpleDateFormat("HH:mm", Locale.getDefault()).format(date)

        // Wczoraj
        now.get(java.util.Calendar.YEAR) == messageDate.get(java.util.Calendar.YEAR) &&
        now.get(java.util.Calendar.DAY_OF_YEAR) - messageDate.get(java.util.Calendar.DAY_OF_YEAR) == 1 ->
            "Yesterday, ${SimpleDateFormat("HH:mm", Locale.getDefault()).format(date)}"

        // W tym roku
        now.get(java.util.Calendar.YEAR) == messageDate.get(java.util.Calendar.YEAR) ->
            SimpleDateFormat("d MMM", Locale.getDefault()).format(date)

        // Wcześniej
        else -> SimpleDateFormat("d MMM yyyy", Locale.getDefault()).format(date)
    }
}