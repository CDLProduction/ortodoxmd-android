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
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
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
import androidx.navigation.navArgument
import androidx.navigation.navigation
import androidx.annotation.StringRes
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import md.ortodox.ortodoxmd.ui.anuar.AnuarScreen
import md.ortodox.ortodoxmd.ui.apologetics.ApologeticScreen
import md.ortodox.ortodoxmd.ui.audiobook.*
import md.ortodox.ortodoxmd.ui.bible.BibleHomeScreen
import md.ortodox.ortodoxmd.ui.calendar.CalendarScreen
import md.ortodox.ortodoxmd.ui.home.HomeScreen
import md.ortodox.ortodoxmd.ui.icons.IconDetailScreen
import md.ortodox.ortodoxmd.ui.icons.IconsScreen
import md.ortodox.ortodoxmd.ui.monastery.MonasteryDetailScreen
import md.ortodox.ortodoxmd.ui.monastery.MonasteryListScreen
import md.ortodox.ortodoxmd.ui.playback.PlaybackService
import md.ortodox.ortodoxmd.ui.prayer.PrayerCategoriesScreen
import md.ortodox.ortodoxmd.ui.prayer.PrayerScreen
import md.ortodox.ortodoxmd.ui.radio.RadioScreen
import md.ortodox.ortodoxmd.ui.sacrament.SacramentScreen
import md.ortodox.ortodoxmd.ui.saints.SaintLifeDetailScreen
import md.ortodox.ortodoxmd.ui.saints.SaintLivesScreen
import md.ortodox.ortodoxmd.ui.theme.OrtodoxmdandroidTheme
import md.ortodox.ortodoxmd.ui.settings.LanguageSettingsScreen
import md.ortodox.ortodoxmd.data.preferences.LanguagePreferences
import md.ortodox.ortodoxmd.util.LocaleHelper
import java.net.URLDecoder
import java.nio.charset.StandardCharsets

// --- Structuri noi pentru Meniu (Drawer) ---
sealed class DrawerMenu
data class MenuItem(val item: DrawerItem) : DrawerMenu()
data class MenuDivider(@StringRes val titleRes: Int) : DrawerMenu()

data class DrawerItem(
    @StringRes val titleRes: Int,
    val icon: ImageVector,
    val route: String,
    val subItems: List<SubDrawerItem>? = null
)
data class SubDrawerItem(@StringRes val titleRes: Int, val route: String)

val prayerCategories = listOf(
    SubDrawerItem(R.string.prayer_morning, "prayer/morning"),
    SubDrawerItem(R.string.prayer_evening, "prayer/evening"),
    SubDrawerItem(R.string.prayer_illness, "prayer/for_illness"),
    SubDrawerItem(R.string.prayer_general, "prayer/general")
)

val menuItems = listOf(
    MenuItem(DrawerItem(R.string.menu_home, Icons.Default.Home, "home")),
    MenuDivider(R.string.menu_main),
    MenuItem(DrawerItem(R.string.menu_calendar, Icons.Default.CalendarMonth, "calendar")),
    MenuItem(DrawerItem(R.string.menu_anuar, Icons.Default.Today, "anuar")),
    MenuItem(DrawerItem(R.string.menu_bible, Icons.Default.Book, "bible_home")),
    MenuDivider(R.string.menu_spiritual_resources),
    MenuItem(DrawerItem(R.string.menu_prayers, Icons.AutoMirrored.Filled.MenuBook, "prayer_categories", subItems = prayerCategories)),
    MenuItem(DrawerItem(R.string.menu_saint_lives, Icons.Default.Person, "saint_lives")),
    MenuItem(DrawerItem(R.string.menu_icons, Icons.Default.Image, "icons")),
    MenuItem(DrawerItem(R.string.menu_monasteries, Icons.Default.LocationCity, "monastery_list")),
    MenuItem(DrawerItem(R.string.menu_sacraments, Icons.Default.AutoStories, "sacraments")),
    MenuItem(DrawerItem(R.string.menu_apologetics, Icons.Default.ContactSupport, "apologetics")),
    MenuDivider(R.string.menu_media),
    MenuItem(DrawerItem(R.string.menu_radio, Icons.Default.Radio, "radio")),
    MenuItem(DrawerItem(R.string.menu_audiobooks, Icons.Default.Headset, "audiobook_flow")),
    MenuItem(DrawerItem(R.string.menu_language, Icons.Default.Language, "language_settings"))
)

@Suppress("OPT_IN_ARGUMENT_IS_NOT_MARKER")
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject lateinit var languagePreferences: LanguagePreferences
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

    @RequiresApi(Build.VERSION_CODES.O)
    @androidx.annotation.OptIn(UnstableApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        runBlocking {
            val lang = languagePreferences.language.first()
            LocaleHelper.applyLanguage(this@MainActivity, lang)
        }
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

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppScaffold(navController: NavHostController) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val topBarTitle = when (currentRoute?.split("/")?.first()) {
        "home" -> "Ortodox Moldova"
        "calendar" -> "Calendar"
        "anuar" -> "Anuar Bisericesc"
        "monastery_list" -> "Mănăstiri"
        "sacraments" -> "Taine și Slujbe"
        "prayer_categories", "prayer" -> "Rugăciuni"
        "apologetics" -> "Apologetică"
        "bible_home" -> "Sfânta Scriptură"
        "saint_lives" -> "Vieți Sfinți"
        "icons" -> "Icoane"
        "radio" -> "Radio Ortodox"
        "audiobook_flow", "audiobook_player" -> "Cărți Audio"
        else -> "Ortodox Moldova"
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

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AppNavHost(navController: NavHostController, modifier: Modifier = Modifier) {
    NavHost(navController = navController, startDestination = "home", modifier = modifier) {
        composable("home") { HomeScreen(navController = navController) }
        composable("calendar") { CalendarScreen() }
        composable("anuar") { AnuarScreen() }
        composable("sacraments") { SacramentScreen() }
        composable("apologetics") { ApologeticScreen() }
        composable("prayer_categories") { PrayerCategoriesScreen(navController = navController) }
        composable("prayer/{category}") { backStackEntry ->
            val category = backStackEntry.arguments?.getString("category") ?: "general"
            PrayerScreen(category = category)
        }
        composable("bible_home") { BibleHomeScreen(mainNavController = navController) }
        composable("radio") { RadioScreen() }
        composable("saint_lives") { SaintLivesScreen(navController = navController) }
        composable(
            route = "saint_life_detail/{saintLifeId}",
            arguments = listOf(navArgument("saintLifeId") { type = NavType.LongType })
        ) { backStackEntry ->
            SaintLifeDetailScreen(
                navController = navController,
                saintLifeId = backStackEntry.arguments?.getLong("saintLifeId") ?: 0L
            )
        }
        composable("icons") { IconsScreen(navController = navController) }
        composable("language_settings") { LanguageSettingsScreen() }
        composable(
            route = "icon_detail/{iconId}",
            arguments = listOf(navArgument("iconId") { type = NavType.LongType })
        ) { backStackEntry ->
            IconDetailScreen(
                navController = navController,
                iconId = backStackEntry.arguments?.getLong("iconId") ?: 0L
            )
        }
        composable("monastery_list") { MonasteryListScreen(navController = navController) }
        composable(
            route = "monastery_detail/{monasteryId}",
            arguments = listOf(navArgument("monasteryId") { type = NavType.LongType })
        ) { backStackEntry ->
            MonasteryDetailScreen(
                navController = navController,
                monasteryId = backStackEntry.arguments?.getLong("monasteryId") ?: 0L
            )
        }
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
        composable(
            route = "audiobook_player/{chapterId}",
            arguments = listOf(navArgument("chapterId") { type = NavType.LongType })
        ) {
            AudiobookPlayerScreen(navController = navController)
        }
    }
}

@Composable
fun NavigationDrawerContent(navController: NavHostController, drawerState: DrawerState) {
    val coroutineScope = rememberCoroutineScope()
    var expandedItem by remember { mutableStateOf<Int?>(null) }
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    ModalDrawerSheet {
        Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
            val bannerBackgroundColor = MaterialTheme.colorScheme.primaryContainer
            val drawerBackgroundColor = MaterialTheme.colorScheme.surface

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    // **AICI ESTE CORECȚIA**
                    .background(
                        Brush.horizontalGradient(
                            // Am eliminat parametrul 'stops' care cauza eroarea
                            colors = listOf(
                                drawerBackgroundColor,
                                bannerBackgroundColor,
                                bannerBackgroundColor,
                                drawerBackgroundColor
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(horizontal = 16.dp)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.nav_drawer_banner),
                        contentDescription = stringResource(R.string.banner_description),
                        modifier = Modifier.size(90.dp),
                        contentScale = ContentScale.Fit
                    )
                    Text(
                        text = stringResource(R.string.app_name),
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            HorizontalDivider()

            menuItems.forEach { menuItem ->
                when (menuItem) {
                    is MenuDivider -> {
                        Text(
                            text = stringResource(menuItem.titleRes),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 8.dp)
                        )
                    }
                    is MenuItem -> {
                        val item = menuItem.item
                        val isGroupSelected = currentRoute?.startsWith(item.route) == true
                        NavigationDrawerItem(
                            icon = { Icon(item.icon, contentDescription = stringResource(item.titleRes)) },
                            label = { Text(stringResource(item.titleRes)) },
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
                                    expandedItem = if (expandedItem == item.titleRes) null else item.titleRes
                                }
                            },
                            badge = {
                                if (item.subItems != null) {
                                    IconButton(onClick = { expandedItem = if (expandedItem == item.titleRes) null else item.titleRes }) {
                                        Icon(
                                            imageVector = if (expandedItem == item.titleRes || isGroupSelected) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                            contentDescription = stringResource(R.string.expand)
                                        )
                                    }
                                }
                            },
                            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                        )
                        if (item.subItems != null && (expandedItem == item.titleRes || isGroupSelected)) {
                            Column(modifier = Modifier.padding(start = 24.dp)) {
                                item.subItems.forEach { subItem ->
                                    NavigationDrawerItem(
                                        label = { Text(stringResource(subItem.titleRes)) },
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
        }
    }
}