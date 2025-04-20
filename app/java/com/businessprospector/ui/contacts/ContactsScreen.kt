package com.businessprospector.ui.contacts

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.businessprospector.domain.model.Contact
import com.businessprospector.ui.common.components.LoadingIndicator

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactsScreen(
    navController: NavController,
    viewModel: ContactsViewModel = hiltViewModel()
) {
    val contacts by viewModel.contacts.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var showFilter by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Contacts") },
                actions = {
                    IconButton(onClick = { showFilter = true }) {
                        Icon(Icons.Default.FilterList, contentDescription = "Filter")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { /* Implementacja dodawania nowego kontaktu */ }
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Contact")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // Pole wyszukiwania
            OutlinedTextField(
                value = searchQuery,
                onValueChange = {
                    searchQuery = it
                    viewModel.searchContacts(it)
                },
                label = { Text("Search contacts") },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search"
                    )
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Dropdownmenu do filtrowania
            Box(modifier = Modifier.fillMaxWidth()) {
                DropdownMenu(
                    expanded = showFilter,
                    onDismissRequest = { showFilter = false }
                ) {
                    Text(
                        text = "Filter by Status",
                        modifier = Modifier.padding(8.dp),
                        fontWeight = FontWeight.Bold
                    )
                    DropdownMenuItem(
                        text = { Text("All") },
                        onClick = {
                            viewModel.filterContacts(null)
                            showFilter = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("New") },
                        onClick = {
                            viewModel.filterContacts("new")
                            showFilter = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Contacted") },
                        onClick = {
                            viewModel.filterContacts("contacted")
                            showFilter = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Responded") },
                        onClick = {
                            viewModel.filterContacts("responded")
                            showFilter = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Meeting Scheduled") },
                        onClick = {
                            viewModel.filterContacts("meeting_scheduled")
                            showFilter = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Deal") },
                        onClick = {
                            viewModel.filterContacts("deal")
                            showFilter = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Not Interested") },
                        onClick = {
                            viewModel.filterContacts("not_interested")
                            showFilter = false
                        }
                    )

                    Divider()

                    Text(
                        text = "Filter by Category",
                        modifier = Modifier.padding(8.dp),
                        fontWeight = FontWeight.Bold
                    )
                    DropdownMenuItem(
                        text = { Text("High Potential") },
                        onClick = {
                            viewModel.filterContactsByCategory("high_potential")
                            showFilter = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Medium Potential") },
                        onClick = {
                            viewModel.filterContactsByCategory("medium_potential")
                            showFilter = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Low Potential") },
                        onClick = {
                            viewModel.filterContactsByCategory("low_potential")
                            showFilter = false
                        }
                    )
                }
            }

            when {
                isLoading -> {
                    LoadingIndicator()
                }
                error != null -> {
                    ErrorState(error ?: "Unknown error")
                }
                contacts.isEmpty() -> {
                    EmptyState()
                }
                else -> {
                    ContactsList(contacts, navController)
                }
            }
        }
    }
}

@Composable
fun ContactsList(
    contacts: List<Contact>,
    navController: NavController
) {
    LazyColumn {
        items(contacts) { contact ->
            ContactListItem(contact) {
                navController.navigate("contacts/${contact.id}")
            }
            Divider()
        }
    }
}

@Composable
fun ContactListItem(
    contact: Contact,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp, horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = contact.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                StatusIndicator(contact.status)
            }

            contact.title?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            contact.company?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                contact.email?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                }

                contact.phone?.let {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Phone,
                            contentDescription = "Phone",
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = it,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }

        Box(
            modifier = Modifier
                .size(12.dp)
                .background(
                    color = when (contact.category) {
                        "high_potential" -> Color.Green
                        "medium_potential" -> Color.Yellow
                        "low_potential" -> Color.Red
                        else -> Color.Gray
                    },
                    shape = RoundedCornerShape(50)
                )
        )
    }
}

@Composable
fun StatusIndicator(status: String) {
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
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = color
        )
    }
}

@Composable
fun EmptyState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "No contacts found",
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = "Add contacts or perform a search",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
fun ErrorState(errorMessage: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Error loading contacts",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.error
            )
            Text(
                text = errorMessage,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
fun LoadingIndicator() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}