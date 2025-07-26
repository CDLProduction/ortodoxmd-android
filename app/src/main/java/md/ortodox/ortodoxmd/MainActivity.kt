package md.ortodox.ortodoxmd

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import md.ortodox.ortodoxmd.ui.bible.*
import md.ortodox.ortodoxmd.ui.calendar.CalendarScreen
import md.ortodox.ortodoxmd.ui.home.HomeScreen
import md.ortodox.ortodoxmd.ui.prayer.PrayerCategoriesScreen
import md.ortodox.ortodoxmd.ui.prayer.PrayerScreen
import md.ortodox.ortodoxmd.ui.theme.OrtodoxmdandroidTheme

data class DrawerItem(
    val title: String,
    val icon: ImageVector,
    val route: String,
    val subItems: List<SubDrawerItem>? = null
)

data class SubDrawerItem(val title: String, val route: String)

val prayerCategories = listOf(
    SubDrawerItem("Rugăciuni de Dimineață", "prayer/morning"),
    SubDrawerItem("Rugăciuni de Seară", "prayer/evening"),
    SubDrawerItem("Rugăciuni pentru Boală", "prayer/for_illness"),
    SubDrawerItem("Rugăciuni Generale", "prayer/general")
)

// Am modificat ruta pentru "Rugăciuni" pentru a duce la noul ecran
val drawerItems = listOf(
    DrawerItem("Acasă", Icons.Default.Home, "home"),
    DrawerItem("Calendar", Icons.Default.CalendarMonth, "calendar"),
    DrawerItem("Rugăciuni", Icons.AutoMirrored.Filled.MenuBook, "prayer_categories", subItems = prayerCategories),
    DrawerItem("Sfânta Scriptură", Icons.Default.Book, "bible_home")
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

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val topBarTitle = when (currentRoute?.split("/")?.first()) {
        "home" -> "OrtodoxMD"
        "calendar" -> "Calendar"
        "prayer_categories", "prayer" -> "Rugăciuni" // Titlu comun pentru ambele ecrane
        "bible_home", "bible_testaments", "bible" -> "Sfânta Scriptură"
        else -> "OrtodoxMD"
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = { NavigationDrawerContent(navController, drawerState) },
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(topBarTitle) },
                    navigationIcon = {
                        IconButton(onClick = { coroutineScope.launch { drawerState.open() } }) {
                            Icon(Icons.Filled.Menu, "Deschide Meniul")
                        }
                    }
                )
            }
        ) { innerPadding ->
            AppNavHost(
                navController = navController,
                modifier = Modifier.padding(innerPadding)
            )
        }
    }
}

@Composable
fun AppNavHost(navController: NavHostController, modifier: Modifier = Modifier) {
    NavHost(
        navController = navController,
        startDestination = "home",
        modifier = modifier
    ) {
        composable("home") { HomeScreen(navController = navController) }
        composable("calendar") { CalendarScreen() }

        // NOU: Rută pentru ecranul de categorii
        composable("prayer_categories") { PrayerCategoriesScreen(navController = navController) }

        // Ruta existentă pentru afișarea rugăciunilor dintr-o categorie specifică
        composable("prayer/{category}") { backStackEntry ->
            val category = backStackEntry.arguments?.getString("category") ?: "general"
            PrayerScreen(category = category)
        }

        composable("bible_home") {
            BibleHomeScreen(mainNavController = navController)
        }
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
        composable("bible/verses/{bookId}/{bookName}/{chapterNumber}") {
            VersesScreen(onBackClick = { navController.popBackStack() })
        }
        composable("search") { GlobalSearchScreen(navController = navController) }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NavigationDrawerContent(
    navController: NavHostController,
    drawerState: DrawerState
) {
    val coroutineScope = rememberCoroutineScope()
    var expandedItem by remember { mutableStateOf<String?>(null) }
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    ModalDrawerSheet {
        Spacer(Modifier.height(12.dp))
        drawerItems.forEach { item ->
            val isGroupSelected = currentRoute?.startsWith(item.route.split("/").first()) == true

            NavigationDrawerItem(
                icon = { Icon(item.icon, contentDescription = item.title) },
                label = { Text(item.title) },
                selected = isGroupSelected && item.subItems == null,
                onClick = {
                    if (item.subItems != null) {
                        // Când se apasă pe "Rugăciuni", navighează la ecranul de categorii
                        coroutineScope.launch { drawerState.close() }
                        navController.navigate(item.route) {
                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    } else {
                        coroutineScope.launch { drawerState.close() }
                        navController.navigate(item.route) {
                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },
                // Adăugăm un indicator de expandare dacă elementul are sub-meniuri
                badge = {
                    if (item.subItems != null) {
                        IconButton(onClick = { expandedItem = if (expandedItem == item.title) null else item.title }) {
                            Icon(
                                imageVector = if (expandedItem == item.title) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                contentDescription = "Expand"
                            )
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
