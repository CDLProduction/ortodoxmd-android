package md.ortodox.ortodoxmd

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.LibraryBooks
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Headset
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Radio
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.util.UnstableApi
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navigation
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import md.ortodox.ortodoxmd.ui.audiobook.AudiobookBook
import md.ortodox.ortodoxmd.ui.audiobook.AudiobookBooksScreen
import md.ortodox.ortodoxmd.ui.audiobook.AudiobookCategory
import md.ortodox.ortodoxmd.ui.audiobook.AudiobookChaptersScreen
import md.ortodox.ortodoxmd.ui.audiobook.AudiobookPlayerScreen
import md.ortodox.ortodoxmd.ui.audiobook.AudiobookTestamentsScreen
import md.ortodox.ortodoxmd.ui.audiobook.AudiobookViewModel
import md.ortodox.ortodoxmd.ui.bible.BibleHomeScreen
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

// Data classes
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
    DrawerItem("Cărți Audio", Icons.Default.Headset, "audiobook_flow") // Schimbat pentru a naviga la flux
)

@Suppress("OPT_IN_ARGUMENT_IS_NOT_MARKER")
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val mediaGranted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions[Manifest.permission.READ_MEDIA_AUDIO] ?: false
        } else {
            true // Nu este necesară permisiunea pe versiuni mai vechi
        }
        val notificationGranted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions[Manifest.permission.POST_NOTIFICATIONS] ?: false
        } else {
            true
        }
        if (!mediaGranted) {
            Log.w("MainActivity", "Permission READ_MEDIA_AUDIO denied")
        }
        if (!notificationGranted) {
            Log.w("MainActivity", "Permission POST_NOTIFICATIONS denied")
        }
    }

    private fun askPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val permissionsToRequest = mutableListOf<String>()
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(Manifest.permission.READ_MEDIA_AUDIO)
            }
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(Manifest.permission.POST_NOTIFICATIONS)
            }
            if (permissionsToRequest.isNotEmpty()) {
                requestPermissionLauncher.launch(permissionsToRequest.toTypedArray())
            }
        }
    }

    @androidx.annotation.OptIn(UnstableApi::class)
    @OptIn(UnstableApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        askPermissions()

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
        "audiobook_flow", "audiobook_player" -> "Cărți Audio"
        else -> "OrtodoxMD"
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = { NavigationDrawerContent(navController, drawerState) }
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
            AppNavHost(navController = navController, modifier = Modifier.padding(innerPadding))
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
        composable("prayer_categories") { PrayerCategoriesScreen(navController = navController) }
        composable("prayer/{category}") { backStackEntry ->
            val category = backStackEntry.arguments?.getString("category") ?: "general"
            PrayerScreen(category = category)
        }
        composable("bible_home") { BibleHomeScreen(mainNavController = navController) }
        composable("radio") { RadioScreen() }

        navigation(startDestination = "audiobook_categories", route = "audiobook_flow") {
            composable("audiobook_categories") { navBackStackEntry ->
                val parentEntry = remember(navBackStackEntry) { navController.getBackStackEntry("audiobook_flow") }
                val audiobookViewModel: AudiobookViewModel = hiltViewModel(parentEntry)
                val uiState by audiobookViewModel.uiState.collectAsStateWithLifecycle()

                Crossfade(targetState = uiState.isLoading, label = "CategoryLoading") { isLoading ->
                    if (isLoading) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    } else {
                        AudiobookCategoriesScreen(navController, uiState.categories)
                    }
                }
            }

            composable("audiobook_testaments/{categoryName}") { navBackStackEntry ->
                val parentEntry = remember(navBackStackEntry) { navController.getBackStackEntry("audiobook_flow") }
                val audiobookViewModel: AudiobookViewModel = hiltViewModel(parentEntry)
                val uiState by audiobookViewModel.uiState.collectAsStateWithLifecycle()

                val categoryName = navBackStackEntry.arguments?.getString("categoryName") ?: ""
                val category = uiState.categories.find { it.name == categoryName }
                val testaments = category?.books?.map { it.testament }?.distinct() ?: emptyList()
                AudiobookTestamentsScreen(navController, testaments)
            }

            composable("audiobook_books/{testamentName}") { navBackStackEntry ->
                val parentEntry = remember(navBackStackEntry) { navController.getBackStackEntry("audiobook_flow") }
                val audiobookViewModel: AudiobookViewModel = hiltViewModel(parentEntry)
                val uiState by audiobookViewModel.uiState.collectAsStateWithLifecycle()

                val testamentName = navBackStackEntry.arguments?.getString("testamentName")
                    ?.let { URLDecoder.decode(it, StandardCharsets.UTF_8.toString()) } ?: ""

                AudiobookBooksScreen(navController, testamentName, uiState.categories.flatMap { it.books })
            }

            composable("audiobook_chapters/{bookName}") { navBackStackEntry ->
                val parentEntry = remember(navBackStackEntry) { navController.getBackStackEntry("audiobook_flow") }
                val audiobookViewModel: AudiobookViewModel = hiltViewModel(parentEntry)

                val bookName = navBackStackEntry.arguments?.getString("bookName")
                    ?.let { URLDecoder.decode(it, StandardCharsets.UTF_8.toString()) } ?: ""

                val uiState by audiobookViewModel.uiState.collectAsStateWithLifecycle()
                val book = uiState.categories.flatMap { it.books }.find { it.name == bookName }

                AudiobookChaptersScreen(
                    navController = navController,
                    book = book,
                    viewModel = audiobookViewModel
                )
            }
        }

        composable("audiobook_player/{chapterId}") {
            AudiobookPlayerScreen(navController = navController)
        }
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
            val isGroupSelected = currentRoute?.startsWith(item.route) == true

            NavigationDrawerItem(
                icon = { Icon(item.icon, contentDescription = item.title) },
                label = { Text(item.title) },
                selected = isGroupSelected && item.subItems == null,
                onClick = {
                    coroutineScope.launch {
                        // drawerState.close() - Nu se poate apela direct pe DrawerValue
                    }
                    if (item.route != currentRoute) {
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

            if (item.subItems != null && (expandedItem == item.title || isGroupSelected)) {
                Column(modifier = Modifier.padding(start = 24.dp)) {
                    item.subItems.forEach { subItem ->
                        NavigationDrawerItem(
                            label = { Text(subItem.title) },
                            selected = currentRoute == subItem.route,
                            onClick = {
                                coroutineScope.launch {
                                    // drawerState.close()
                                }
                                if (currentRoute != subItem.route) {
                                    navController.navigate(subItem.route) {
                                        popUpTo(navController.graph.findStartDestination().id)
                                        launchSingleTop = true
                                    }
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

@Composable
fun AudiobookCategoriesScreen(navController: NavController, categories: List<AudiobookCategory>) {
    LazyColumn(
        contentPadding = PaddingValues(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        items(categories, key = { it.name }) { category ->
            ListItem(
                headlineContent = { Text(category.name) },
                leadingContent = { Icon(Icons.AutoMirrored.Filled.LibraryBooks, contentDescription = null) },
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

@Composable
fun AudiobookBooksScreen(
    navController: NavController,
    testamentName: String,
    books: List<AudiobookBook>
) {
    val booksInTestament = books.filter { it.testament == testamentName }

    LazyColumn(
        contentPadding = PaddingValues(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        items(booksInTestament, key = { it.name }) { book ->
            ListItem(
                headlineContent = { Text(book.name) },
                trailingContent = { Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null) },
                modifier = Modifier.clickable {
                    val encodedBookName = URLEncoder.encode(book.name, StandardCharsets.UTF_8.toString())
                    navController.navigate("audiobook_chapters/$encodedBookName")
                }
            )
        }
    }
}
