package com.businessprospector

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.businessprospector.ui.analytics.AnalyticsScreen
import com.businessprospector.ui.contacts.ContactsScreen
import com.businessprospector.ui.messages.MessagesScreen
import com.businessprospector.ui.search.SearchScreen
import com.businessprospector.ui.sequences.SequencesScreen
import com.businessprospector.ui.settings.SettingsScreen
import com.businessprospector.ui.theme.BusinessProspectorTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            BusinessProspectorTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainScreen()
                }
            }
        }
    }
}

@Composable
fun MainScreen() {
    val navController = rememberNavController()

    val items = listOf(
        Screen.Search,
        Screen.Contacts,
        Screen.Messages,
        Screen.Sequences,
        Screen.Analytics,
        Screen.Settings
    )

    Scaffold(
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination

                items.forEach { screen ->
                    NavigationBarItem(
                        icon = { Icon(painterResource(id = screen.iconResId), contentDescription = null) },
                        label = { Text(screen.title) },
                        selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Search.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Search.route) {
                SearchScreen(navController)
            }
            composable(Screen.Contacts.route) {
                ContactsScreen(navController)
            }
            composable(Screen.Messages.route) {
                MessagesScreen(navController)
            }
            composable(Screen.Sequences.route) {
                SequencesScreen(navController)
            }
            composable(Screen.Analytics.route) {
                AnalyticsScreen(navController)
            }
            composable(Screen.Settings.route) {
                SettingsScreen(navController)
            }
            // Dodatkowe ekrany szczegółowe
            composable("contacts/{contactId}") { backStackEntry ->
                val contactId = backStackEntry.arguments?.getString("contactId")
                ContactDetailScreen(contactId?.toLongOrNull() ?: -1, navController)
            }
            composable("messages/template_editor/{templateId}") { backStackEntry ->
                val templateId = backStackEntry.arguments?.getString("templateId")
                TemplateEditorScreen(templateId?.toLongOrNull(), navController)
            }
            composable("sequences/editor/{sequenceId}") { backStackEntry ->
                val sequenceId = backStackEntry.arguments?.getString("sequenceId")
                SequenceEditorScreen(sequenceId?.toLongOrNull(), navController)
            }
            composable("settings/api_config") {
                ApiConfigScreen(navController)
            }
        }
    }
}

sealed class Screen(val route: String, val title: String, val iconResId: Int) {
    object Search : Screen("search", "Search", R.drawable.ic_search)
    object Contacts : Screen("contacts", "Contacts", R.drawable.ic_contacts)
    object Messages : Screen("messages", "Messages", R.drawable.ic_messages)
    object Sequences : Screen("sequences", "Sequences", R.drawable.ic_sequences)
    object Analytics : Screen("analytics", "Analytics", R.drawable.ic_analytics)
    object Settings : Screen("settings", "Settings", R.drawable.ic_settings)
}

@Composable
fun ContactDetailScreen(contactId: Long, navController: androidx.navigation.NavController) {
    // Placeholder - zostanie zaimplementowane w pełnej implementacji
    Text("Contact Details $contactId")
}

@Composable
fun TemplateEditorScreen(templateId: Long?, navController: androidx.navigation.NavController) {
    // Placeholder - zostanie zaimplementowane w pełnej implementacji
    Text("Template Editor ${templateId ?: "New"}")
}

@Composable
fun SequenceEditorScreen(sequenceId: Long?, navController: androidx.navigation.NavController) {
    // Placeholder - zostanie zaimplementowane w pełnej implementacji
    Text("Sequence Editor ${sequenceId ?: "New"}")
}

@Composable
fun ApiConfigScreen(navController: androidx.navigation.NavController) {
    // Placeholder - zostanie zaimplementowane w pełnej implementacji
    Text("API Configuration")
}