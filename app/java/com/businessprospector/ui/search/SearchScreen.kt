package com.businessprospector.ui.search

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
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
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
import com.businessprospector.domain.model.SearchState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    navController: NavController,
    viewModel: SearchViewModel = hiltViewModel()
) {
    val searchState by viewModel.searchState.collectAsState()
    var showSearchOptions by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Search Business Contacts") }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            SearchForm(
                searchQuery = viewModel.searchQuery.value,
                onSearchQueryChange = { viewModel.searchQuery.value = it },
                onSearch = { viewModel.performSearch() },
                onOpenSearchOptions = { showSearchOptions = true }
            )

            Spacer(modifier = Modifier.height(16.dp))

            when (searchState) {
                is SearchState.Initial -> {
                    InitialSearchState()
                }
                is SearchState.Loading -> {
                    LoadingSearchState()
                }
                is SearchState.Success -> {
                    val contacts = (searchState as SearchState.Success).contacts
                    SuccessSearchState(contacts, navController)
                }
                is SearchState.Error -> {
                    val errorMessage = (searchState as SearchState.Error).message
                    ErrorSearchState(errorMessage)
                }
            }
        }

        if (showSearchOptions) {
            SearchOptionsDialog(
                onDismiss = { showSearchOptions = false },
                currentOptions = viewModel.searchOptions.value,
                onOptionsChange = { viewModel.searchOptions.value = it }
            )
        }
    }
}

@Composable
fun SearchForm(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    onOpenSearchOptions: () -> Unit
) {
    Column {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchQueryChange,
            label = { Text("Search for contacts (e.g., 'CTO energy companies Berlin')") },
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search"
                )
            }
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Button(
                onClick = onOpenSearchOptions,
                modifier = Modifier.weight(1f)
            ) {
                Text("Search Options")
            }

            Spacer(modifier = Modifier.size(8.dp))

            Button(
                onClick = onSearch,
                modifier = Modifier.weight(1f)
            ) {
                Text("Search")
            }
        }
    }
}

@Composable
fun SearchOptionsDialog(
    onDismiss: () -> Unit,
    currentOptions: SearchOptions,
    onOptionsChange: (SearchOptions) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.5f))
            .clickable { onDismiss() }
    ) {
        Card(
            modifier = Modifier
                .padding(16.dp)
                .align(Alignment.Center)
                .clickable(enabled = false) { /* Prevent click propagation */ }
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth()
            ) {
                Text(
                    text = "Search Options",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Results per page
                Text(
                    text = "Results per page: ${currentOptions.resultsPerPage}",
                    style = MaterialTheme.typography.bodyLarge
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Filtering options
                SearchFilters(currentOptions, onOptionsChange)

                Spacer(modifier = Modifier.height(16.dp))

                // API Configuration
                ApiConfiguration(currentOptions, onOptionsChange)

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Button(onClick = onDismiss) {
                        Text("Close")
                    }
                }
            }
        }
    }
}

@Composable
fun SearchFilters(
    currentOptions: SearchOptions,
    onOptionsChange: (SearchOptions) -> Unit
) {
    // W pełnej implementacji dodałbym tutaj filtry wyszukiwania
    Text(
        text = "Filtering Options",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold
    )

    Spacer(modifier = Modifier.height(8.dp))

    // Placeholder dla filtrów
    Text(
        text = "Filters will be implemented here",
        style = MaterialTheme.typography.bodyMedium
    )
}

@Composable
fun ApiConfiguration(
    currentOptions: SearchOptions,
    onOptionsChange: (SearchOptions) -> Unit
) {
    Text(
        text = "API Configuration",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold
    )

    Spacer(modifier = Modifier.height(8.dp))

    OutlinedTextField(
        value = currentOptions.googleApiKey,
        onValueChange = { onOptionsChange(currentOptions.copy(googleApiKey = it)) },
        label = { Text("Google API Key") },
        modifier = Modifier.fillMaxWidth()
    )

    Spacer(modifier = Modifier.height(8.dp))

    OutlinedTextField(
        value = currentOptions.searchEngineId,
        onValueChange = { onOptionsChange(currentOptions.copy(searchEngineId = it)) },
        label = { Text("Search Engine ID") },
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
fun InitialSearchState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Enter search criteria to find business contacts",
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

@Composable
fun LoadingSearchState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CircularProgressIndicator()
            Spacer(modifier = Modifier.height(16.dp))
            Text("Searching for contacts...")
        }
    }
}

@Composable
fun SuccessSearchState(
    contacts: List<Contact>,
    navController: NavController
) {
    if (contacts.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No contacts found. Try different search criteria.",
                style = MaterialTheme.typography.bodyLarge
            )
        }
    } else {
        Column {
            Text(
                text = "Found ${contacts.size} contacts",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn {
                items(contacts) { contact ->
                    ContactItem(contact) {
                        navController.navigate("contacts/${contact.id}")
                    }
                    Divider()
                }
            }
        }
    }
}

@Composable
fun ContactItem(
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
            Text(
                text = contact.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            contact.title?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Row {
                contact.company?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Row {
                contact.email?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        Box(
            modifier = Modifier
                .size(10.dp)
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
fun ErrorSearchState(errorMessage: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Error occurred during search",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.error
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = errorMessage,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

data class SearchOptions(
    val resultsPerPage: Int = 10,
    val googleApiKey: String = "",
    val searchEngineId: String = "",
    val filterOptions: Map<String, String> = emptyMap()
)

package com.businessprospector.ui.search

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
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
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
import com.businessprospector.domain.model.SearchState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    navController: NavController,
    viewModel: SearchViewModel = hiltViewModel()
) {
    val searchState by viewModel.searchState.collectAsState()
    var showSearchOptions by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Search Business Contacts") }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            SearchForm(
                searchQuery = viewModel.searchQuery.value,
                onSearchQueryChange = { viewModel.searchQuery.value = it },
                onSearch = { viewModel.performSearch() },
                onOpenSearchOptions = { showSearchOptions = true }
            )

            Spacer(modifier = Modifier.height(16.dp))

            when (searchState) {
                is SearchState.Initial -> {
                    InitialSearchState()
                }
                is SearchState.Loading -> {
                    LoadingSearchState()
                }
                is SearchState.Success -> {
                    val contacts = (searchState as SearchState.Success).contacts
                    SuccessSearchState(contacts, navController)
                }
                is SearchState.Error -> {
                    val errorMessage = (searchState as SearchState.Error).message
                    ErrorSearchState(errorMessage)
                }
            }
        }

        if (showSearchOptions) {
            SearchOptionsDialog(
                onDismiss = { showSearchOptions = false },
                currentOptions = viewModel.searchOptions.value,
                onOptionsChange = { viewModel.searchOptions.value = it }
            )
        }
    }
}

@Composable
fun SearchForm(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    onOpenSearchOptions: () -> Unit
) {
    Column {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchQueryChange,
            label = { Text("Search for contacts (e.g., 'CTO energy companies Berlin')") },
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search"
                )
            }
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Button(
                onClick = onOpenSearchOptions,
                modifier = Modifier.weight(1f)
            ) {
                Text("Search Options")
            }

            Spacer(modifier = Modifier.size(8.dp))

            Button(
                onClick = onSearch,
                modifier = Modifier.weight(1f)
            ) {
                Text("Search")
            }
        }
    }
}

@Composable
fun SearchOptionsDialog(
    onDismiss: () -> Unit,
    currentOptions: SearchOptions,
    onOptionsChange: (SearchOptions) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.5f))
            .clickable { onDismiss() }
    ) {
        Card(
            modifier = Modifier
                .padding(16.dp)
                .align(Alignment.Center)
                .clickable(enabled = false) { /* Prevent click propagation */ }
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth()
            ) {
                Text(
                    text = "Search Options",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Results per page
                Text(
                    text = "Results per page: ${currentOptions.resultsPerPage}",
                    style = MaterialTheme.typography.bodyLarge
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Filtering options
                SearchFilters(currentOptions, onOptionsChange)

                Spacer(modifier = Modifier.height(16.dp))

                // API Configuration
                ApiConfiguration(currentOptions, onOptionsChange)

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Button(onClick = onDismiss) {
                        Text("Close")
                    }
                }
            }
        }
    }
}

@Composable
fun SearchFilters(
    currentOptions: SearchOptions,
    onOptionsChange: (SearchOptions) -> Unit
) {
    // W pełnej implementacji dodałbym tutaj filtry wyszukiwania
    Text(
        text = "Filtering Options",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold
    )

    Spacer(modifier = Modifier.height(8.dp))

    // Placeholder dla filtrów
    Text(
        text = "Filters will be implemented here",
        style = MaterialTheme.typography.bodyMedium
    )
}

@Composable
fun ApiConfiguration(
    currentOptions: SearchOptions,
    onOptionsChange: (SearchOptions) -> Unit
) {
    Text(
        text = "API Configuration",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold
    )

    Spacer(modifier = Modifier.height(8.dp))

    OutlinedTextField(
        value = currentOptions.googleApiKey,
        onValueChange = { onOptionsChange(currentOptions.copy(googleApiKey = it)) },
        label = { Text("Google API Key") },
        modifier = Modifier.fillMaxWidth()
    )

    Spacer(modifier = Modifier.height(8.dp))

    OutlinedTextField(
        value = currentOptions.searchEngineId,
        onValueChange = { onOptionsChange(currentOptions.copy(searchEngineId = it)) },
        label = { Text("Search Engine ID") },
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
fun InitialSearchState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Enter search criteria to find business contacts",
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

@Composable
fun LoadingSearchState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CircularProgressIndicator()
            Spacer(modifier = Modifier.height(16.dp))
            Text("Searching for contacts...")
        }
    }
}

@Composable
fun SuccessSearchState(
    contacts: List<Contact>,
    navController: NavController
) {
    if (contacts.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No contacts found. Try different search criteria.",
                style = MaterialTheme.typography.bodyLarge
            )
        }
    } else {
        Column {
            Text(
                text = "Found ${contacts.size} contacts",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn {
                items(contacts) { contact ->
                    ContactItem(contact) {
                        navController.navigate("contacts/${contact.id}")
                    }
                    Divider()
                }
            }
        }
    }
}

@Composable
fun ContactItem(
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
            Text(
                text = contact.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            contact.title?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Row {
                contact.company?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Row {
                contact.email?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        Box(
            modifier = Modifier
                .size(10.dp)
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
fun ErrorSearchState(errorMessage: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Error occurred during search",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.error
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = errorMessage,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

data class SearchOptions(
    val resultsPerPage: Int = 10,
    val googleApiKey: String = "",
    val searchEngineId: String = "",
    val filterOptions: Map<String, String> = emptyMap()
)

// Stan wyszukiwania zdefiniowany w domain/model/SearchState.kt
// sealed class SearchState {
//     object Initial : SearchState()
//     object Loading : SearchState()
//     data class Success(val contacts: List<Contact>) : SearchState()
//     data class Error(val message: String) : SearchState()
// }