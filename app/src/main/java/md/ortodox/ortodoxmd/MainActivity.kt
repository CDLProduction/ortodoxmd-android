@file:Suppress("DEPRECATION")
package md.ortodox.ortodoxmd
import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

import androidx.media3.common.util.UnstableApi
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost

import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

import md.ortodox.ortodoxmd.ui.bible.BibleHomeScreen
import md.ortodox.ortodoxmd.ui.calendar.CalendarScreen
import md.ortodox.ortodoxmd.ui.home.HomeScreen
import md.ortodox.ortodoxmd.ui.playback.PlaybackService
import md.ortodox.ortodoxmd.ui.prayer.PrayerCategoriesScreen
import md.ortodox.ortodoxmd.ui.prayer.PrayerScreen
import md.ortodox.ortodoxmd.ui.radio.RadioScreen

import md.ortodox.ortodoxmd.ui.icons.IconDetailScreen
import md.ortodox.ortodoxmd.ui.icons.IconsScreen
import md.ortodox.ortodoxmd.ui.theme.OrtodoxmdandroidTheme

import androidx.navigation.compose.navigation
import androidx.navigation.navArgument
import md.ortodox.ortodoxmd.ui.audiobook.AudiobookBooksScreen
import md.ortodox.ortodoxmd.ui.audiobook.AudiobookCategoriesScreen
import md.ortodox.ortodoxmd.ui.audiobook.AudiobookChaptersScreen
import md.ortodox.ortodoxmd.ui.audiobook.AudiobookPlayerScreen
import md.ortodox.ortodoxmd.ui.audiobook.AudiobookTestamentsScreen
import md.ortodox.ortodoxmd.ui.audiobook.AudiobookViewModel

import md.ortodox.ortodoxmd.ui.saints.SaintLifeDetailScreen
import md.ortodox.ortodoxmd.ui.saints.SaintLivesScreen
import java.net.URLDecoder
import java.nio.charset.StandardCharsets

// --- Data classes pentru Meniu (Drawer) ---
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
    DrawerItem("Vieți Sfinți", Icons.Default.Person, "saint_lives"), // <-- RUTĂ ACTUALIZATĂ AICI
    DrawerItem("Icoane", Icons.Default.Image, "icons"),
    DrawerItem("Radio", Icons.Default.Radio, "radio"),
    DrawerItem("Cărți Audio", Icons.Default.Headset, "audiobook_flow")
)

@Suppress("OPT_IN_ARGUMENT_IS_NOT_MARKER")
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { /* ... logica pentru permisiuni ... */ }

    private fun askPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val permissionsToRequest = mutableListOf<String>()
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(Manifest.permission.POST_NOTIFICATIONS)
            }
            if (permissionsToRequest.isNotEmpty()) {
                requestPermissionLauncher.launch(permissionsToRequest.toTypedArray())
            }
        }
    }

    @androidx.annotation.OptIn(UnstableApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        askPermissions()
        startService(Intent(this, PlaybackService::class.java))
        enableEdgeToEdge()
        setContent {
            OrtodoxmdandroidTheme {
                AppScaffold(navController = rememberNavController())
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppScaffold(navController: NavHostController) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val topBarTitle = when (currentRoute?.split("/")?.first()) {
        "home" -> "OrtodoxMD"
        "calendar" -> "Calendar"
        "prayer_categories", "prayer" -> "Rugăciuni"
        "bible_home" -> "Sfânta Scriptură"
        "saint_lives" -> "Vieți Sfinți" // <-- RUTĂ ACTUALIZATĂ AICI
        "icons" -> "Icoane"
        "radio" -> "Radio Ortodox"
        "audiobook_flow" -> "Cărți Audio"
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
            },
            content = { innerPadding ->
                AppNavHost(navController = navController, modifier = Modifier.padding(innerPadding))
            }
        )
    }
}

@Composable
fun AppNavHost(navController: NavHostController, modifier: Modifier = Modifier) {
    NavHost(navController = navController, startDestination = "home", modifier = modifier) {
        composable("home") { HomeScreen(navController = navController) }
        composable("calendar") { CalendarScreen() }
        composable("prayer_categories") { PrayerCategoriesScreen(navController = navController) }
        composable("prayer/{category}") { backStackEntry ->
            val category = backStackEntry.arguments?.getString("category") ?: "general"
            PrayerScreen(category = category)
        }
        composable("bible_home") { BibleHomeScreen(mainNavController = navController) }
        composable("radio") { RadioScreen() }

        // --- RUTele PENTRU VIEȚI SFINȚI (CORECTATE) ---
        composable("saint_lives") {
            SaintLivesScreen(navController = navController)
        }
        composable("saint_life_detail/{saintLifeId}") { backStackEntry ->
            val saintLifeId = backStackEntry.arguments?.getString("saintLifeId")?.toLongOrNull() ?: 0L
            SaintLifeDetailScreen(navController = navController, saintLifeId = saintLifeId)
        }

        // --- RUTele PENTRU ICOANE ---
        composable("icons") { IconsScreen(navController = navController) }
        composable("icon_detail/{iconId}") { backStackEntry ->
            val iconId = backStackEntry.arguments?.getString("iconId")?.toLongOrNull() ?: 0L
            IconDetailScreen(navController = navController, iconId = iconId)
        }

        // --- GRAFUL DE NAVIGARE PENTRU CĂRȚI AUDIO (RESTAURAT) ---
        navigation(startDestination = "audiobook_categories", route = "audiobook_flow") {
            composable("audiobook_categories") { navBackStackEntry ->
                val parentEntry = remember(navBackStackEntry) { navController.getBackStackEntry("audiobook_flow") }
                val audiobookViewModel: AudiobookViewModel = hiltViewModel(parentEntry)
                val uiState by audiobookViewModel.uiState.collectAsStateWithLifecycle()
                AudiobookCategoriesScreen(
                    navController = navController,
                    categories = uiState.categories,
                    categoryName = "Cărți Audio"
                )
            }
            composable(
                "audiobook_testaments/{categoryName}",
                arguments = listOf(navArgument("categoryName") { defaultValue = "Cărți Audio" })
            ) { navBackStackEntry ->
                val parentEntry = remember(navBackStackEntry) { navController.getBackStackEntry("audiobook_flow") }
                val audiobookViewModel: AudiobookViewModel = hiltViewModel(parentEntry)
                val uiState by audiobookViewModel.uiState.collectAsStateWithLifecycle()
                val categoryName = navBackStackEntry.arguments?.getString("categoryName") ?: "Cărți Audio"
                val category = uiState.categories.find { it.name == categoryName }
                val testaments = category?.books?.map { it.testament }?.distinct() ?: emptyList()
                AudiobookTestamentsScreen(navController, testaments, categoryName)
            }
            composable(
                "audiobook_books/{testamentName}",
                arguments = listOf(navArgument("testamentName") { type = NavType.StringType })
            ) { navBackStackEntry ->
                val parentEntry = remember(navBackStackEntry) { navController.getBackStackEntry("audiobook_flow") }
                val audiobookViewModel: AudiobookViewModel = hiltViewModel(parentEntry)
                val uiState by audiobookViewModel.uiState.collectAsStateWithLifecycle()
                val testamentName = navBackStackEntry.arguments?.getString("testamentName")
                    ?.let { URLDecoder.decode(it, StandardCharsets.UTF_8.toString()) } ?: ""
                AudiobookBooksScreen(navController, testamentName, uiState.categories.flatMap { it.books })
            }
            composable(
                route = "audiobook_chapters/{bookName}",
                arguments = listOf(navArgument("bookName") { type = NavType.StringType })
            ) { navBackStackEntry ->
                val parentEntry = remember(navBackStackEntry) { navController.getBackStackEntry("audiobook_flow") }
                val audiobookViewModel: AudiobookViewModel = hiltViewModel(parentEntry)
                val bookName = navBackStackEntry.arguments?.getString("bookName")?.let {
                    URLDecoder.decode(it, StandardCharsets.UTF_8.toString())
                }
                LaunchedEffect(bookName) {
                    if (bookName != null) {
                        audiobookViewModel.selectBook(bookName)
                    }
                }
                AudiobookChaptersScreen(
                    viewModel = audiobookViewModel,
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToPlayer = { chapterId ->
                        navController.navigate("audiobook_player/$chapterId")
                    }
                )
            }
        }
        composable("audiobook_player/{chapterId}") { backStackEntry ->
            val chapterId = backStackEntry.arguments?.getString("chapterId")?.toLongOrNull() ?: -1L
            AudiobookPlayerScreen(navController = navController)
        }
    }
}

@Composable
fun NavigationDrawerContent(navController: NavHostController, drawerState: DrawerState) {
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
                    if (item.subItems == null) {
                        coroutineScope.launch { drawerState.close() }
                        navController.navigate(item.route) {
                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    } else {
                        expandedItem = if (expandedItem == item.title) null else item.title
                    }
                },
                badge = {
                    if (item.subItems != null) {
                        IconButton(onClick = { expandedItem = if (expandedItem == item.title) null else item.title }) {
                            Icon(
                                imageVector = if (expandedItem == item.title || isGroupSelected) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
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
                                coroutineScope.launch { drawerState.close() }
                                navController.navigate(subItem.route) {
                                    popUpTo(navController.graph.findStartDestination().id)
                                    launchSingleTop = true
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