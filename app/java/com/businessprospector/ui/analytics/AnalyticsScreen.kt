package com.businessprospector.ui.analytics

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.businessprospector.ui.common.components.ErrorMessage
import com.businessprospector.ui.common.components.LoadingIndicator

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsScreen(
    navController: NavController,
    viewModel: AnalyticsViewModel = hiltViewModel()
) {
    val analyticsData by viewModel.analyticsData.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    var selectedTabIndex by remember { mutableStateOf(0) }
    val tabs = listOf("Overview", "Campaigns", "Contacts")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Analytics & Reports") }
            )
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
                        0 -> OverviewTab(analyticsData, navController)
                        1 -> CampaignsTab(analyticsData, navController)
                        2 -> ContactsTab(analyticsData, navController)
                    }
                }
            }
        }
    }
}

@Composable
fun OverviewTab(analyticsData: AnalyticsData, navController: NavController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Podsumowanie
        Text(
            text = "Summary",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            StatCard(
                title = "Total Contacts",
                value = analyticsData.totalContacts.toString(),
                modifier = Modifier.weight(1f)
            )

            StatCard(
                title = "Active Sequences",
                value = analyticsData.activeSequences.toString(),
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            StatCard(
                title = "Messages Sent",
                value = analyticsData.messagesSent.toString(),
                modifier = Modifier.weight(1f)
            )

            StatCard(
                title = "Response Rate",
                value = "${analyticsData.responseRate}%",
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Wykresy
        Text(
            text = "Activity Over Time",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        ActivityChart(analyticsData)

        Spacer(modifier = Modifier.height(24.dp))

        // Najlepsze kontakty
        Text(
            text = "Top Contacts",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        TopContactsList(analyticsData.topContacts)

        Spacer(modifier = Modifier.height(24.dp))

        // Najlepsze sekwencje
        Text(
            text = "Top Performing Sequences",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        TopSequencesList(analyticsData.topSequences)
    }
}

@Composable
fun CampaignsTab(analyticsData: AnalyticsData, navController: NavController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Statystyki sekwencji
        Text(
            text = "Sequence Performance",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        SequencePerformanceTable(analyticsData.sequencePerformance)

        Spacer(modifier = Modifier.height(24.dp))

        // Statystyki wiadomości
        Text(
            text = "Message Types Effectiveness",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        MessageTypeChart(analyticsData)
    }
}

@Composable
fun ContactsTab(analyticsData: AnalyticsData, navController: NavController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Statystyki kontaktów według kategorii
        Text(
            text = "Contacts by Category",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        ContactCategoryChart(analyticsData)

        Spacer(modifier = Modifier.height(24.dp))

        // Statystyki kontaktów według statusu
        Text(
            text = "Contacts by Status",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        ContactStatusChart(analyticsData)

        Spacer(modifier = Modifier.height(24.dp))

        // Źródła kontaktów
        Text(
            text = "Contact Sources",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        ContactSourcesChart(analyticsData)
    }
}

@Composable
fun StatCard(
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun ActivityChart(analyticsData: AnalyticsData) {
    // W rzeczywistej implementacji, użylibyśmy biblioteki do wykresów,
    // np. Compose Charts lub innej biblioteki dostępnej dla Jetpack Compose

    // Symulacja wykresu dla celów demonstracyjnych
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Bottom
        ) {
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.Bottom
            ) {
                BarChartItem(height = 0.3f, label = "Mon")
                BarChartItem(height = 0.5f, label = "Tue")
                BarChartItem(height = 0.7f, label = "Wed")
                BarChartItem(height = 0.4f, label = "Thu")
                BarChartItem(height = 0.6f, label = "Fri")
                BarChartItem(height = 0.2f, label = "Sat")
                BarChartItem(height = 0.1f, label = "Sun")
            }
        }
    }
}

@Composable
fun BarChartItem(height: Float, label: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .width(30.dp)
                .height((150 * height).dp)
                .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                .background(MaterialTheme.colorScheme.primary)
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall
        )
    }
}

@Composable
fun TopContactsList(topContacts: List<TopContact>) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            topContacts.forEachIndexed { index, contact ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${index + 1}.",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.width(24.dp)
                    )

                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = contact.name,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            text = contact.company ?: "",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    Text(
                        text = contact.interactionCount.toString(),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                if (index < topContacts.size - 1) {
                    Divider()
                }
            }
        }
    }
}

@Composable
fun TopSequencesList(topSequences: List<TopSequence>) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            topSequences.forEachIndexed { index, sequence ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${index + 1}.",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.width(24.dp)
                    )

                    Text(
                        text = sequence.name,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )

                    Text(
                        text = "${sequence.successRate}%",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                if (index < topSequences.size - 1) {
                    Divider()
                }
            }
        }
    }
}

@Composable
fun SequencePerformanceTable(sequencePerformance: List<SequencePerformance>) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Nagłówki tabeli
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Sequence",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )

                Text(
                    text = "Sent",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.width(60.dp)
                )

                Text(
                    text = "Opened",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.width(60.dp)
                )

                Text(
                    text = "Resp.",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.width(60.dp)
                )

                Text(
                    text = "Rate",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.width(60.dp)
                )
            }

            Divider(modifier = Modifier.padding(vertical = 8.dp))

            // Dane tabeli
            sequencePerformance.forEach { sequence ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = sequence.name,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.weight(1f)
                    )

                    Text(
                        text = sequence.sent.toString(),
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.width(60.dp)
                    )

                    Text(
                        text = sequence.opened.toString(),
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.width(60.dp)
                    )

                    Text(
                        text = sequence.responses.toString(),
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.width(60.dp)
                    )

                    Text(
                        text = "${sequence.responseRate}%",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.width(60.dp)
                    )
                }

                Divider()
            }
        }
    }
}

@Composable
fun MessageTypeChart(analyticsData: AnalyticsData) {
    // W rzeczywistej implementacji, użylibyśmy biblioteki do wykresów

    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Symulacja wykresu kołowego za pomocą pasków postępu
            MessageTypeItem(
                type = "Email",
                sentCount = 120,
                responseCount = 32,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(16.dp))

            MessageTypeItem(
                type = "SMS",
                sentCount = 85,
                responseCount = 41,
                color = MaterialTheme.colorScheme.secondary
            )

            Spacer(modifier = Modifier.height(16.dp))

            MessageTypeItem(
                type = "Call",
                sentCount = 45,
                responseCount = 28,
                color = MaterialTheme.colorScheme.tertiary
            )
        }
    }
}

@Composable
fun MessageTypeItem(
    type: String,
    sentCount: Int,
    responseCount: Int,
    color: Color
) {
    val responseRate = if (sentCount > 0) (responseCount.toFloat() / sentCount.toFloat()) * 100 else 0f

    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = type,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "$responseCount / $sentCount (${responseRate.toInt()}%)",
                style = MaterialTheme.typography.bodyMedium
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Pasek postępu
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(responseRate / 100f)
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(color)
            )
        }
    }
}

@Composable
fun ContactCategoryChart(analyticsData: AnalyticsData) {
    // Symulacja wykresu kołowego
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            PieChartLegendItem(
                label = "High Potential",
                value = 35,
                color = Color.Green
            )

            Spacer(modifier = Modifier.height(8.dp))

            PieChartLegendItem(
                label = "Medium Potential",
                value = 45,
                color = Color.Yellow
            )

            Spacer(modifier = Modifier.height(8.dp))

            PieChartLegendItem(
                label = "Low Potential",
                value = 20,
                color = Color.Red
            )
        }
    }
}

@Composable
fun ContactStatusChart(analyticsData: AnalyticsData) {
    // Symulacja wykresu słupkowego
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            StatusBarItem(
                label = "New",
                count = 45,
                total = 100,
                color = Color.Blue
            )

            Spacer(modifier = Modifier.height(8.dp))

            StatusBarItem(
                label = "Contacted",
                count = 30,
                total = 100,
                color = Color(0xFF9C27B0) // Purple
            )

            Spacer(modifier = Modifier.height(8.dp))

            StatusBarItem(
                label = "Responded",
                count = 15,
                total = 100,
                color = Color(0xFF4CAF50) // Green
            )

            Spacer(modifier = Modifier.height(8.dp))

            StatusBarItem(
                label = "Meeting",
                count = 7,
                total = 100,
                color = Color(0xFFFF9800) // Orange
            )

            Spacer(modifier = Modifier.height(8.dp))

            StatusBarItem(
                label = "Deal",
                count = 3,
                total = 100,
                color = Color(0xFF2196F3) // Blue
            )
        }
    }
}

@Composable
fun ContactSourcesChart(analyticsData: AnalyticsData) {
    // Symulacja wykresu źródeł
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            SourceItem(
                source = "Google Search",
                count = 65,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(8.dp))

            SourceItem(
                source = "LinkedIn",
                count = 25,
                color = MaterialTheme.colorScheme.secondary
            )

            Spacer(modifier = Modifier.height(8.dp))

            SourceItem(
                source = "Manual Entry",
                count = 10,
                color = MaterialTheme.colorScheme.tertiary
            )
        }
    }
}

@Composable
fun PieChartLegendItem(
    label: String,
    value: Int,
    color: Color
) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(16.dp)
                .background(color, shape = RoundedCornerShape(4.dp))
        )

        Spacer(modifier = Modifier.width(8.dp))

        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f)
        )

        Text(
            text = "$value%",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun StatusBarItem(
    label: String,
    count: Int,
    total: Int,
    color: Color
) {
    val percentage = if (total > 0) (count.toFloat() / total.toFloat()) else 0f

    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium
            )

            Text(
                text = "$count",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(12.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(percentage)
                    .height(12.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(color)
            )
        }
    }
}

@Composable
fun SourceItem(
    source: String,
    count: Int,
    color: Color
) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(16.dp)
                .background(color, shape = RoundedCornerShape(4.dp))
        )

        Spacer(modifier = Modifier.width(8.dp))

        Text(
            text = source,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f)
        )

        Text(
            text = count.toString(),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold
        )
    }
}

// Klasy danych używane w ekranie analityki
data class AnalyticsData(
    val totalContacts: Int = 0,
    val activeSequences: Int = 0,
    val messagesSent: Int = 0,
    val responseRate: Int = 0,
    val topContacts: List<TopContact> = emptyList(),
    val topSequences: List<TopSequence> = emptyList(),
    val sequencePerformance: List<SequencePerformance> = emptyList()
)

data class TopContact(
    val name: String,
    val company: String?,
    val interactionCount: Int
)

data class TopSequence(
    val name: String,
    val successRate: Int
)

data class SequencePerformance(
    val name: String,
    val sent: Int,
    val opened: Int,
    val responses: Int,
    val responseRate: Int
)