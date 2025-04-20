package com.businessprospector.ui.contacts

import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.businessprospector.domain.model.Contact
import com.businessprospector.domain.model.Message
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactDetailScreen(
    contactId: Long,
    navController: NavController,
    viewModel: ContactDetailViewModel = hiltViewModel()
) {
    // Pobierz dane kontaktu
    viewModel.loadContact(contactId)

    val contactState by viewModel.contactState.collectAsState()
    val messagesState by viewModel.messagesState.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    var showStatusOptions by remember { mutableStateOf(false) }
    var showCommunicationOptions by remember { mutableStateOf(false) }
    var showMoreOptions by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Contact Details") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showMoreOptions = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "More options")
                    }

                    DropdownMenu(
                        expanded = showMoreOptions,
                        onDismissRequest = { showMoreOptions = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Edit Contact") },
                            onClick = {
                                showMoreOptions = false
                                // Nawigacja do ekranu edycji kontaktu
                                // navController.navigate("contacts/edit/$contactId")
                            },
                            leadingIcon = {
                                Icon(Icons.Default.Edit, contentDescription = null)
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Delete Contact") },
                            onClick = {
                                showMoreOptions = false
                                viewModel.deleteContact()
                                navController.navigateUp()
                            }
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        when {
            isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            error != null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues)
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp)
                    ) {
                        // Sekcja informacji o kontakcie
                        ContactHeader(contact)

                        Spacer(modifier = Modifier.height(16.dp))

                        // Sekcja akcji
                        ActionButtons(
                            onEmailClick = { showCommunicationOptions = true },
                            onCallClick = { navController.navigate("contacts/${contact.id}/call") },
                            onChangeStatus = { showStatusOptions = true }
                        )

                        // Menu statusów
                        Box(modifier = Modifier.fillMaxWidth()) {
                            DropdownMenu(
                                expanded = showStatusOptions,
                                onDismissRequest = { showStatusOptions = false }
                            ) {
                                Text(
                                    text = "Change Status",
                                    modifier = Modifier.padding(8.dp),
                                    fontWeight = FontWeight.Bold
                                )
                                DropdownMenuItem(
                                    text = { Text("New") },
                                    onClick = {
                                        viewModel.updateContactStatus("new")
                                        showStatusOptions = false
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Contacted") },
                                    onClick = {
                                        viewModel.updateContactStatus("contacted")
                                        showStatusOptions = false
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Responded") },
                                    onClick = {
                                        viewModel.updateContactStatus("responded")
                                        showStatusOptions = false
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Meeting Scheduled") },
                                    onClick = {
                                        viewModel.updateContactStatus("meeting_scheduled")
                                        showStatusOptions = false
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Deal") },
                                    onClick = {
                                        viewModel.updateContactStatus("deal")
                                        showStatusOptions = false
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Not Interested") },
                                    onClick = {
                                        viewModel.updateContactStatus("not_interested")
                                        showStatusOptions = false
                                    }
                                )
                            }

                            // Menu komunikacji
                            DropdownMenu(
                                expanded = showCommunicationOptions,
                                onDismissRequest = { showCommunicationOptions = false }
                            ) {
                                Text(
                                    text = "Communication",
                                    modifier = Modifier.padding(8.dp),
                                    fontWeight = FontWeight.Bold
                                )
                                DropdownMenuItem(
                                    text = { Text("Send Email") },
                                    onClick = {
                                        showCommunicationOptions = false
                                        navController.navigate("contacts/${contact.id}/email")
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Send SMS") },
                                    onClick = {
                                        showCommunicationOptions = false
                                        navController.navigate("contacts/${contact.id}/sms")
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Start Sequence") },
                                    onClick = {
                                        showCommunicationOptions = false
                                        navController.navigate("contacts/${contact.id}/sequence")
                                    }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Sekcja szczegółów
                        ContactDetails(contact)

                        Spacer(modifier = Modifier.height(16.dp))

                        // Sekcja historii wiadomości
                        Text(
                            text = "Communication History",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )

                        if (messagesState.isEmpty()) {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                                )
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "No communication history yet",
                                        style = MaterialTheme.typography.bodyMedium,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        } else {
                            messagesState.forEach { message ->
                                MessageItem(message)
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                        }
                    }
                }
            }
        }
    }

    @Composable
    fun ContactHeader(contact: Contact) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            // Inicjały kontaktu
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = contact.name.take(2).uppercase(),
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Nazwa kontaktu
            Text(
                text = contact.name,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            // Stanowisko i firma
            contact.title?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.titleMedium
                )
            }

            contact.company?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.titleMedium
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Status i kategoria
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                StatusBadge(status = contact.status)
                Spacer(modifier = Modifier.padding(horizontal = 4.dp))
                CategoryBadge(category = contact.category ?: "uncategorized")
            }
        }
    }

    @Composable
    fun StatusBadge(status: String) {
        val (color, label) = when (status) {
            "new" -> Pair(Color.Blue, "New")
            "contacted" -> Pair(Color(0xFF9C27B0), "Contacted") // Purple
            "responded" -> Pair(Color(0xFF4CAF50), "Responded") // Green
            "meeting_scheduled" -> Pair(Color(0xFFFF9800), "Meeting") // Orange
            "deal" -> Pair(Color(0xFF2196F3), "Deal") // Blue
            "not_interested" -> Pair(Color.Red, "Not Interested")
            else -> Pair(Color.Gray, status.replaceFirstChar { it.uppercase() })
        }

        Box(
            modifier = Modifier
                .background(
                    color = color.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(4.dp)
                )
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = color
            )
        }
    }

    @Composable
    fun CategoryBadge(category: String) {
        val (color, label) = when (category) {
            "high_potential" -> Pair(Color.Green, "High Potential")
            "medium_potential" -> Pair(Color.Yellow, "Medium Potential")
            "low_potential" -> Pair(Color.Red, "Low Potential")
            else -> Pair(Color.Gray, "Uncategorized")
        }

        Box(
            modifier = Modifier
                .background(
                    color = color.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(4.dp)
                )
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = color
            )
        }
    }

    @Composable
    fun ActionButtons(
        onEmailClick: () -> Unit,
        onCallClick: () -> Unit,
        onChangeStatus: () -> Unit
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(
                onClick = onEmailClick,
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Default.Email, contentDescription = "Email")
                Spacer(modifier = Modifier.size(4.dp))
                Text("Message")
            }

            Spacer(modifier = Modifier.size(8.dp))

            Button(
                onClick = onCallClick,
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Default.Call, contentDescription = "Call")
                Spacer(modifier = Modifier.size(4.dp))
                Text("Call")
            }

            Spacer(modifier = Modifier.size(8.dp))

            Button(
                onClick = onChangeStatus,
                modifier = Modifier.weight(1f)
            ) {
                Text("Status")
            }
        }
    }

    @Composable
    fun ContactDetails(contact: Contact) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Contact Information",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                contact.email?.let {
                    DetailRow(icon = Icons.Default.Email, label = "Email", value = it)
                }

                contact.phone?.let {
                    DetailRow(icon = Icons.Default.Phone, label = "Phone", value = it)
                }

                contact.website?.let {
                    DetailRow(icon = null, label = "Website", value = it)
                }

                contact.linkedInUrl?.let {
                    DetailRow(icon = null, label = "LinkedIn", value = it)
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Additional Information",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                contact.source?.let {
                    DetailRow(icon = null, label = "Source", value = it)
                }

                if (contact.tags.isNotEmpty()) {
                    DetailRow(icon = null, label = "Tags", value = contact.tags.joinToString(", "))
                }

                contact.notes?.let {
                    DetailRow(icon = null, label = "Notes", value = it)
                }

                // Data utworzenia
                contact.createdAt?.let {
                    DetailRow(
                        icon = null,
                        label = "Created",
                        value = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(it)
                    )
                }
            }
        }
    }

    @Composable
    fun DetailRow(icon: androidx.compose.ui.graphics.vector.ImageVector?, label: String, value: String) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            icon?.let {
                Icon(
                    imageVector = it,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.size(8.dp))
            }

            Text(
                text = "$label:",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(0.3f)
            )

            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(0.7f)
            )
        }
    }

    @Composable
    fun MessageItem(message: Message) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = if (message.direction == "outgoing")
                    MaterialTheme.colorScheme.primaryContainer
                else
                    MaterialTheme.colorScheme.secondaryContainer
            )
        ) {
            Column(
                modifier = Modifier.padding(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = when(message.type) {
                            "email" -> "Email"
                            "sms" -> "SMS"
                            "call" -> "Call"
                            else -> message.type.replaceFirstChar { it.uppercase() }
                        },
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
                            .format(message.sentAt ?: message.createdAt),
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                message.subject?.let {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Subject: $it",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = message.content,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = message.status.replaceFirstChar { it.uppercase() },
                    style = MaterialTheme.typography.labelMedium,
                    color = when(message.status) {
                        "sent", "delivered" -> Color.Green
                        "failed" -> Color.Red
                        else -> Color.Gray
                    }
                )
            }
        }
    }
    horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Error loading contact",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.error
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = error ?: "Unknown error",
            style = MaterialTheme.typography.bodyMedium
        )
    }
}
}
contactState != null -> {
    val contact = contactState!!

    Column(