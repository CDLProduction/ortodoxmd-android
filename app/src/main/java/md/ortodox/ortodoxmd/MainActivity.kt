package md.ortodox.ortodoxmd

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.LibraryBooks
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import md.ortodox.ortodoxmd.ui.MainViewModel
import md.ortodox.ortodoxmd.ui.bible.*
import md.ortodox.ortodoxmd.ui.calendar.CalendarScreen
import md.ortodox.ortodoxmd.ui.prayer.PrayerScreen
import md.ortodox.ortodoxmd.ui.theme.OrtodoxmdandroidTheme

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

val categories = listOf(
    SubDrawerItem("Rugăciuni de Dimineață", "prayer/morning"),
    SubDrawerItem("Rugăciuni de Seară", "prayer/evening"),
    SubDrawerItem("Rugăciuni pentru Boală", "prayer/for_illness"),
    SubDrawerItem("Rugăciuni Generale", "prayer/general")
)

// MODIFICAT: Adăugat element pentru Semn de Carte
val drawerItems = listOf(
    DrawerItem("Calendar", Icons.Default.CalendarMonth, "calendar"),
    DrawerItem("Rugăciuni", Icons.AutoMirrored.Filled.MenuBook, "prayer", subItems = categories),
    DrawerItem("Sfânta Scriptură", Icons.AutoMirrored.Filled.LibraryBooks, "bible_testaments"),
    DrawerItem("Semn de Carte", Icons.Default.Bookmark, "bookmark_route")
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
    // NOU: ViewModel pentru acțiuni globale
    val mainViewModel: MainViewModel = hiltViewModel()

    // NOU: Logica pentru a asculta evenimente de navigare de la semnul de carte
    LaunchedEffect(Unit) {
        mainViewModel.bookmarkNavigator.collect { bookmarkDetails ->
            drawerState.close()
            val route = "bible/verses/${bookmarkDetails.bookmark.bookId}/${bookmarkDetails.bookName}/${bookmarkDetails.bookmark.chapterNumber}"
            navController.navigate(route)
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = { NavigationDrawerContent(navController, drawerState, mainViewModel) },
        content = {
            Scaffold(
                modifier = Modifier.fillMaxSize(),
                topBar = {
                    TopAppBar(
                        title = { Text("OrtodoxMD") },
                        navigationIcon = {
                            IconButton(onClick = { coroutineScope.launch { drawerState.open() } }) {
                                Icon(Icons.Filled.Menu, "Deschide Meniul")
                            }
                        },
                        // NOU: Iconiță de căutare
                        actions = {
                            IconButton(onClick = { navController.navigate("search") }) {
                                Icon(
                                    Icons.Default.Search,
                                    contentDescription = "Căutare Globală",
                                    tint = MaterialTheme.colorScheme.onPrimary
                                )
                            }
                        },
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
                    composable("calendar") { CalendarScreen() }
                    composable("prayer/{category}") { backStackEntry ->
                        val category = backStackEntry.arguments?.getString("category") ?: "general"
                        PrayerScreen(category = category)
                    }

                    // NOU: Rută pentru căutare
                    composable("search") { GlobalSearchScreen(navController = navController) }

                    composable("bible_testaments") { TestamentsScreen(navController = navController) }
                    composable("bible/books/{testamentId}") { backStackEntry ->
                        val id = backStackEntry.arguments?.getString("testamentId")?.toLongOrNull()
                        BooksScreen(navController = navController, testamentId = id)
                    }
                    composable("bible/chapters/{bookId}/{bookName}") { backStackEntry ->
                        val bookId = backStackEntry.arguments?.getString("bookId")?.toLongOrNull() ?: return@composable
                        val bookName = backStackEntry.arguments?.getString("bookName") ?: "Carte"
                        ChaptersScreen(navController = navController, bookId = bookId, bookName = bookName)
                    }
                    composable("bible/verses/{bookId}/{bookName}/{chapterNumber}") { backStackEntry ->
                        val bookId = backStackEntry.arguments?.getString("bookId")?.toLongOrNull() ?: return@composable
                        val bookName = backStackEntry.arguments?.getString("bookName") ?: "Carte"
                        val chapter = backStackEntry.arguments?.getString("chapterNumber")?.toIntOrNull() ?: return@composable
                        VersesScreen(bookId = bookId, bookName = bookName, chapterNumber = chapter, onBackClick = { navController.popBackStack() })
                    }
                }
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NavigationDrawerContent(
    navController: NavHostController,
    drawerState: DrawerState,
    mainViewModel: MainViewModel // NOU: Primit ca parametru
) {
    val coroutineScope = rememberCoroutineScope()
    var expandedItem by remember { mutableStateOf<String?>(null) }
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    ModalDrawerSheet {
        Spacer(Modifier.height(12.dp))
        drawerItems.forEach { item ->
            val isSelected = currentRoute?.startsWith(item.route.split("/").first()) == true

            NavigationDrawerItem(
                icon = { Icon(item.icon, contentDescription = item.title) },
                label = { Text(item.title) },
                selected = isSelected && item.subItems == null,
                onClick = {
                    // NOU: Logică specială pentru semnul de carte
                    if (item.route == "bookmark_route") {
                        mainViewModel.onBookmarkClicked()
                    } else if (item.subItems != null) {
                        expandedItem = if (expandedItem == item.title) null else item.title
                    } else {
                        coroutineScope.launch { drawerState.close() }
                        navController.navigate(item.route) {
                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },
                modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
            )

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
                                    restoreState = true // MODIFICAT: Corectat
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