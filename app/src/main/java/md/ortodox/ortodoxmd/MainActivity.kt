package md.ortodox.ortodoxmd

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope

import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import md.ortodox.ortodoxmd.ui.calendar.CalendarScreen
import md.ortodox.ortodoxmd.ui.prayer.PrayerScreen
import md.ortodox.ortodoxmd.ui.bible.BibleScreen
import md.ortodox.ortodoxmd.ui.theme.OrtodoxmdandroidTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.automirrored.filled.LibraryBooks
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.DrawerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.rememberDrawerState
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.compose.currentBackStackEntryAsState

data class DrawerItem(
    val title: String,
    val icon: ImageVector,
    val route: String,
    val subItems: List<SubDrawerItem>? = null
)

data class SubDrawerItem(
    val title: String,
    val route: String
)

// Am schimbat pictogramele pentru a fi mai descriptive
val categories = listOf(
    SubDrawerItem("Rugăciuni de Dimineață", "prayer/morning"),
    SubDrawerItem("Rugăciuni de Seară", "prayer/evening"),
    SubDrawerItem("Rugăciuni pentru Boală", "prayer/for_illness"),
    SubDrawerItem("Rugăciuni Generale", "prayer/general")
)

val drawerItems = listOf(
    DrawerItem("Calendar", Icons.Default.CalendarMonth, "calendar"),
    DrawerItem("Rugăciuni", Icons.AutoMirrored.Filled.MenuBook, "prayer", subItems = categories),
    DrawerItem("Sfânta Scriptură", Icons.AutoMirrored.Filled.LibraryBooks, "bible")
)

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            OrtodoxmdandroidTheme {
                AppScaffold()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppScaffold() {
    val navController = rememberNavController()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = { NavigationDrawerContent(navController, drawerState) },
        scrimColor = MaterialTheme.colorScheme.scrim.copy(alpha = 0.8f),  // Crește opacitatea pentru un efect mai opac
        content = {
            Scaffold(
                modifier = Modifier.fillMaxSize(),
                topBar = {
                    TopAppBar(
                        title = { Text("OrtodoxMD") },
                        navigationIcon = {
                            IconButton(onClick = {
                                coroutineScope.launch { drawerState.open() }
                            }) {
                                Icon(Icons.Filled.Menu, contentDescription = "Deschide Meniul")
                            }
                        },
                        // Am actualizat culorile pentru a se potrivi cu tema ta
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            titleContentColor = MaterialTheme.colorScheme.onPrimary,
                            navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    )
                }
            ) { innerPadding ->
                NavHost(
                    navController = navController,
                    startDestination = "calendar",
                    modifier = Modifier.padding(innerPadding)
                ) {
                    composable("calendar") {
                        CalendarScreen()
                    }
                    composable("prayer/{category}") { backStackEntry ->
                        val category = backStackEntry.arguments?.getString("category") ?: "general"
                        PrayerScreen(category = category)
                    }
                    composable("bible") {
                        BibleScreen()
                    }
                }
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NavigationDrawerContent(navController: NavHostController, drawerState: DrawerState) {
    val coroutineScope = rememberCoroutineScope()
    var expandedItem by remember { mutableStateOf<String?>(null) }
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Folosim ModalDrawerSheet pentru un design standard și corect
    ModalDrawerSheet {
        Spacer(Modifier.height(12.dp))
        drawerItems.forEach { item ->
            val isSelected = currentRoute == item.route || (item.subItems?.any { sub -> currentRoute == sub.route } == true)
            NavigationDrawerItem(
                icon = { Icon(item.icon, contentDescription = item.title) },
                label = { Text(item.title) },
                selected = isSelected && item.subItems == null, // Selectează doar item-urile fără sub-meniuri
                onClick = {
                    if (item.subItems != null) {
                        expandedItem = if (expandedItem == item.title) null else item.title
                    } else {
                        coroutineScope.launch { drawerState.close() }
                        navController.navigate(item.route) {
                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                            restoreState = false // Am schimbat în true pentru o navigare mai fluidă
                        }
                    }
                },
                modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
            )

            // Sub-meniuri expandabile
            if (item.subItems != null && expandedItem == item.title) {
                Column(modifier = Modifier.padding(start = 24.dp)) {
                    item.subItems.forEach { subItem ->
                        NavigationDrawerItem(
                            label = { Text(subItem.title) },
                            selected = currentRoute == subItem.route,
                            onClick = {
                                coroutineScope.launch { drawerState.close() }
                                navController.navigate(subItem.route) {
                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = false
                                }
                            },
                            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                        )
                    }
                }
            }
        }
    }
}