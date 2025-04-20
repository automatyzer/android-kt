package com.businessprospector.domain.usecase.analytics

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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.Card
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.businessprospector.data.model.AnalyticsReport
import com.businessprospector.data.model.SequenceMetrics
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportScreen(
    navController: NavController,
    viewModel: ReportViewModel = hiltViewModel()
) {
    val report by viewModel.report.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val sharingOptions by viewModel.sharingOptions.collectAsState()
    
    var showSharingOptions by remember { mutableStateOf(false) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Analytics Report") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showSharingOptions = true }) {
                        Icon(Icons.Default.Share, contentDescription = "Share Report")
                    }
                    
                    // Menu opcji udostępniania
                    Box {
                        DropdownMenu(
                            expanded = showSharingOptions,
                            onDismissRequest = { showSharingOptions = false }
                        ) {
                            sharingOptions.forEach { option ->
                                DropdownMenuItem(
                                    text = { Text(option) },
                                    onClick = { 
                                        viewModel.exportReport(option)
                                        showSharingOptions = false
                                    }
                                )
                            }
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (error != null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Error loading report",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.error
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = error ?: "Unknown error",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Button(
                        onClick = { viewModel.loadReport() }
                    ) {
                        Text("Retry")
                    }
                }
            }
        } else if (report != null) {
            ReportContent(
                report = report!!,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text("No report data available")
            }
        }
    }
}

@Composable
fun ReportContent(
    report: AnalyticsReport,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        // Nagłówek raportu
        Text(
            text = report.title,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(4.dp))
        
        report.description?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.bodyLarge
            )
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Okres raportu
        Text(
            text = "Period: ${formatDate(report.startDate)} to ${formatDate(report.endDate)}",
            style = MaterialTheme.typography.bodyMedium
        )
        
        Text(
            text = "Generated: ${formatDateTime(report.generatedAt)}",
            style = MaterialTheme.typography.bodySmall
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Metryki wiadomości
        ReportSection(title = "Message Metrics") {
            MessageMetricsSection(report)
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Metryki kontaktów
        ReportSection(title = "Contact Metrics") {
            ContactMetricsSection(report)
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Metryki sekwencji
        ReportSection(title = "Sequence Performance") {
            SequenceMetricsSection(report)
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Najlepsi wykonawcy
        ReportSection(title = "Top Performers") {
            TopPerformersSection(report)
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Przycisk pobierania
        Button(
            onClick = { /* Akcja pobierania raportu */ },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Download Full Report")
        }
    }
}

@Composable
fun ReportSection(
    title: String,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            content()
        }
    }
}

@Composable
fun MessageMetricsSection(report: AnalyticsReport) {
    val metrics = report.messageMetrics
    
    // Statystyki wiadomości
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        MetricItem(
            label = "Emails Sent",
            value = metrics.emailsSent.toString(),
            modifier = Modifier.weight(1f)
        )
        
        MetricItem(
            label = "Emails Opened",
            value = metrics.emailsOpened.toString(),
            modifier = Modifier.weight(1f)
        )
        
        MetricItem(
            label = "Emails Clicked",
            value = metrics.emailsClicked.toString(),
            modifier = Modifier.weight(1f)
        )
    }
    
    Spacer(modifier = Modifier.height(16.dp))
    
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        MetricItem(
            label = "SMS Sent",
            value = metrics.smsSent.toString(),
            modifier = Modifier.weight(1f)
        )
        
        MetricItem(
            label = "Calls Made",
            value = metrics.callsMade.toString(),
            modifier = Modifier.weight(1f)
        )
        
        MetricItem(
            label = "Responses",
            value = metrics.totalResponsesReceived.toString(),
            modifier = Modifier.weight(1f)
        )
    }
    
    Spacer(modifier = Modifier.height(16.dp))
    
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        MetricItem(
            label = "Open Rate",
            value = "${metrics.openRate.toInt()}%",
            modifier = Modifier.weight(1f)
        )
        
        MetricItem(
            label = "Click Rate",
            value = "${metrics.clickRate.toInt()}%",
            modifier = Modifier.weight(1f)
        )
        
        MetricItem(
            label = "Response Rate",
            value = "${metrics.responseRate.toInt()}%",
            modifier = Modifier.weight(1f)
        )
    }
    
    Spacer(modifier = Modifier.height(16.dp))
    
    // Średni czas odpowiedzi
    Text(
        text = "Average Response Time: ${formatResponseTime(metrics.averageResponseTime)}",
        style = MaterialTheme.typography.bodyMedium,
        fontWeight = FontWeight.Bold
    )
}

@Composable
fun ContactMetricsSection(report: AnalyticsReport) {
    val metrics = report.contactMetrics
    
    // Statystyki kontaktów według statusu
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        MetricItem(
            label = "New",
            value = metrics.newContacts.toString(),
            modifier = Modifier.weight(1f)
        )
        
        MetricItem(
            label = "Contacted",
            value = metrics.contactedContacts.toString(),
            modifier = Modifier.weight(1f)
        )
        
        MetricItem(
            label = "Responded",
            value = metrics.respondedContacts.toString(),
            modifier = Modifier.weight(1f)
        )
    }
    
    Spacer(modifier = Modifier.height(16.dp))
    
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        MetricItem(
            label = "Meeting",
            value = metrics.meetingScheduledContacts.toString(),
            modifier = Modifier.weight(1f)
        )
        
        MetricItem(
            label = "Deal",
            value = metrics.dealContacts.toString(),
            modifier = Modifier.weight(1f)
        )
        
        MetricItem(
            label = "Not Interested",
            value = metrics.notInterestedContacts.toString(),
            modifier = Modifier.weight(1f)
        )
    }
    
    Spacer(modifier = Modifier.height(16.dp))
    
    // Współczynnik konwersji
    Text(
        text = "Conversion Rate: ${metrics.conversionRate.toInt()}%",
        style = MaterialTheme.typography.bodyMedium,
        fontWeight = FontWeight.Bold
    )
}

@Composable
fun SequenceMetricsSection(report: AnalyticsReport) {
    // Tabela z metrykami sekwencji
    Column {
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
                text = "Contacts",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.width(70.dp)
            )
            
            Text(
                text = "Messages",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.width(70.dp)
            )
            
            Text(
                text = "Success",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.width(70.dp)
            )
        }
        
        Divider(modifier = Modifier.padding(vertical = 8.dp))
        
        // Dane sekwencji
        report.sequenceMetrics.forEach { metric ->
            SequenceMetricsRow(metric)
            Divider()
        }
    }
}

@Composable
fun SequenceMetricsRow(metric: SequenceMetrics) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = metric.sequenceName,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f)
        )
        
        Text(
            text = metric.contactsInSequence.toString(),
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.width(70.dp)
        )
        
        Text(
            text = metric.messagesSent.toString(),
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.width(70.dp)
        )
        
        Text(
            text = "${metric.successRate.toInt()}%",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.width(70.dp)
        )
    }
}

@Composable
fun TopPerformersSection(report: AnalyticsReport) {
    val topPerformers = report.topPerformers
    
    // Najlepsze kontakty
    if (topPerformers.topContacts.isNotEmpty()) {
        Text(
            text = "Top Contacts",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        topPerformers.topContacts.take(3).forEach { contact ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = contact.name,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.weight(1f)
                )
                
                Text(
                    text = "${contact.responseRate.toInt()}% response",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
    
    Spacer(modifier = Modifier.height(16.dp))
    
    // Najlepsze sekwencje
    if (topPerformers.topSequences.isNotEmpty()) {
        Text(
            text = "Top Sequences",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        topPerformers.topSequences.take(3).forEach { sequence ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = sequence.name,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.weight(1f)
                )
                
                Text(
                    text = "${sequence.successRate.toInt()}% success",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
    
    Spacer(modifier = Modifier.height(16.dp))
    
    // Najlepsze szablony wiadomości
    if (topPerformers.topMessageTemplates.isNotEmpty()) {
        Text(
            text = "Top Message Templates",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        topPerformers.topMessageTemplates.take(3).forEach { template ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${template.name} (${template.type})",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.weight(1f)
                )
                
                Text(
                    text = "${template.responseRate.toInt()}% response",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun MetricItem(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall
        )
        
        Spacer(modifier = Modifier.height(4.dp))
        
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold
        )
    }
}

/**
 * Formatuje datę do czytelnej postaci.
 */
private fun formatDate(date: java.util.Date): String {
    val format = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    return format.format(date)
}

/**
 * Formatuje datę i czas do czytelnej postaci.
 */
private fun formatDateTime(date: java.util.Date): String {
    val format = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
    return format.format(date)
}

/**
 * Formatuje czas odpowiedzi do czytelnej postaci.
 */
private fun formatResponseTime(timeMs: Long): String {
    val hours = timeMs / (1000 * 60 * 60)
    val minutes = (timeMs % (1000 * 60 * 60)) / (1000 * 60)
    
    return when {
        hours > 0 -> "$hours h $minutes min"
        minutes > 0 -> "$minutes min"
        else -> "${timeMs / 1000} sec"
    }
}