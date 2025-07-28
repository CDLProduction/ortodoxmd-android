package md.ortodox.ortodoxmd

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.media3.common.util.UnstableApi
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import md.ortodox.ortodoxmd.ui.audiobook.*
import md.ortodox.ortodoxmd.ui.bible.*
import md.ortodox.ortodoxmd.ui.calendar.CalendarScreen
import md.ortodox.ortodoxmd.ui.home.HomeScreen
import md.ortodox.ortodoxmd.ui.playback.PlaybackService
import md.ortodox.ortodoxmd.ui.prayer.PrayerCategoriesScreen
import md.ortodox.ortodoxmd.ui.prayer.PrayerScreen
import md.ortodox.ortodoxmd.ui.radio.RadioScreen
import md.ortodox.ortodoxmd.ui.theme.OrtodoxmdandroidTheme
import java.net.URLDecoder
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

// ... Data classes DrawerItem, SubDrawerItem și listele rămân la fel ...
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
val drawerItems = listOf(
    DrawerItem("Acasă", Icons.Default.Home, "home"),
    DrawerItem("Calendar", Icons.Default.CalendarMonth, "calendar"),
    DrawerItem("Rugăciuni", Icons.AutoMirrored.Filled.MenuBook, "prayer_categories", subItems = prayerCategories),
    DrawerItem("Sfânta Scriptură", Icons.Default.Book, "bible_home"),
    DrawerItem("Radio", Icons.Default.Radio, "radio"),
    DrawerItem("Cărți Audio", Icons.Default.Headset, "audiobooks_entry")
)


@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @androidx.annotation.OptIn(UnstableApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val serviceIntent = Intent(this, PlaybackService::class.java)
        startService(serviceIntent)
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
        "prayer_categories", "prayer" -> "Rugăciuni"
        "bible_home", "bible" -> "Sfânta Scriptură"
        "radio" -> "Radio Ortodox"
        "audiobooks_entry", "audiobook_testaments", "audiobook_books", "audiobook_chapters", "audiobook_player" -> "Cărți Audio"
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
    val audiobookViewModel: AudiobookViewModel = hiltViewModel()
    val audiobooksState by audiobookViewModel.uiState.collectAsState()

    NavHost(
        navController = navController,
        startDestination = "home",
        modifier = modifier
    ) {
        // ... Rutele existente ...
        composable("home") { HomeScreen(navController = navController) }
        composable("calendar") { CalendarScreen() }
        composable("prayer_categories") { PrayerCategoriesScreen(navController = navController) }
        composable("prayer/{category}") { backStackEntry ->
            val category = backStackEntry.arguments?.getString("category") ?: "general"
            PrayerScreen(category = category)
        }
        composable("bible_home") { BibleHomeScreen(mainNavController = navController) }
        composable("radio") { RadioScreen() }

        // --- NOUL FLUX DE NAVIGAȚIE PENTRU CĂRȚI AUDIO ---
        composable("audiobooks_entry") {
            Crossfade(targetState = audiobooksState.isLoading) { isLoading ->
                if (isLoading) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                } else {
                    AudiobookCategoriesScreen(navController, audiobooksState.categories)
                }
            }
        }

        composable("audiobook_testaments/{categoryName}") { backStackEntry ->
            val categoryName = backStackEntry.arguments?.getString("categoryName") ?: ""
            val category = audiobooksState.categories.find { it.name == categoryName }
            val testaments = category?.books?.map { it.testament }?.distinct() ?: emptyList()
            AudiobookTestamentsScreen(navController, testaments)
        }

        composable("audiobook_books/{testamentName}") { backStackEntry ->
            val testamentName = backStackEntry.arguments?.getString("testamentName")
                ?.let { URLDecoder.decode(it, StandardCharsets.UTF_8.toString()) } ?: ""
            AudiobookBooksScreen(navController, testamentName, audiobooksState.categories.flatMap { it.books })
        }

        composable("audiobook_chapters/{bookName}") { backStackEntry ->
            val bookName = backStackEntry.arguments?.getString("bookName")
                ?.let { URLDecoder.decode(it, StandardCharsets.UTF_8.toString()) } ?: ""
            val book = audiobooksState.categories.flatMap { it.books }.find { it.name == bookName }
            AudiobookChaptersScreen(navController, book, onDownloadClick = audiobookViewModel::downloadAudiobook)
        }

        composable("audiobook_player/{chapterId}") {
            AudiobookPlayerScreen(navController = navController)
        }
        // --- SFÂRȘIT FLUX NOU ---

        // ... Restul rutelor pentru Biblie (text) ...
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

// --- ECRANE NOI PENTRU CATEGORII ȘI TESTAMENTE ---

@Composable
fun AudiobookCategoriesScreen(navController: NavController, categories: List<AudiobookCategory>) {
    LazyColumn(
        contentPadding = PaddingValues(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        items(categories, key = { it.name }) { category ->
            ListItem(
                headlineContent = { Text(category.name) },
                leadingContent = { Icon(Icons.Default.LibraryBooks, contentDescription = null) },
                trailingContent = { Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null) },
                modifier = Modifier.clickable {
                    navController.navigate("audiobook_testaments/${category.name}")
                }
            )
        }
    }
}

@Composable
fun AudiobookTestamentsScreen(navController: NavController, testaments: List<String>) {
    LazyColumn(
        contentPadding = PaddingValues(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        items(testaments, key = { it }) { testament ->
            ListItem(
                headlineContent = { Text(testament) },
                trailingContent = { Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null) },
                modifier = Modifier.clickable {
                    val encodedTestamentName = URLEncoder.encode(testament, StandardCharsets.UTF_8.toString())
                    navController.navigate("audiobook_books/$encodedTestamentName")
                }
            )
        }
    }
}


// ... NavigationDrawerContent rămâne la fel ...
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
