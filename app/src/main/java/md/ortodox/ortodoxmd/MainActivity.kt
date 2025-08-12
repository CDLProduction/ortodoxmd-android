package md.ortodox.ortodoxmd

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ContactSupport
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.os.LocaleListCompat
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
import coil.ImageLoader
import coil.compose.LocalImageLoader
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import md.ortodox.ortodoxmd.data.language.LanguageManager
import md.ortodox.ortodoxmd.ui.MainViewModel
import md.ortodox.ortodoxmd.ui.anuar.AnuarScreen
import md.ortodox.ortodoxmd.ui.apologetic.ApologeticScreen
import md.ortodox.ortodoxmd.ui.audiobook.*
import md.ortodox.ortodoxmd.ui.bible.BibleHomeScreen
import md.ortodox.ortodoxmd.ui.calendar.CalendarScreen
import md.ortodox.ortodoxmd.ui.home.HomeScreen
import md.ortodox.ortodoxmd.ui.icons.IconDetailScreen
import md.ortodox.ortodoxmd.ui.icons.IconsScreen
import md.ortodox.ortodoxmd.ui.language.LanguageScreen
import md.ortodox.ortodoxmd.ui.monastery.MonasteryDetailScreen
import md.ortodox.ortodoxmd.ui.monastery.MonasteryListScreen
import md.ortodox.ortodoxmd.ui.onboarding.OnboardingScreen
import md.ortodox.ortodoxmd.ui.playback.PlaybackService
import md.ortodox.ortodoxmd.ui.prayer.PrayerCategoriesScreen
import md.ortodox.ortodoxmd.ui.prayer.PrayerScreen
import md.ortodox.ortodoxmd.ui.radio.RadioScreen
import md.ortodox.ortodoxmd.ui.sacrament.SacramentScreen
import md.ortodox.ortodoxmd.ui.saints.SaintLifeDetailScreen
import md.ortodox.ortodoxmd.ui.saints.SaintLivesScreen
import md.ortodox.ortodoxmd.ui.theme.OrtodoxmdandroidTheme
import java.net.URLDecoder
import java.nio.charset.StandardCharsets
import javax.inject.Inject

sealed class DrawerMenu
data class MenuItem(val item: DrawerItem) : DrawerMenu()
data class MenuDivider(val titleResId: Int) : DrawerMenu()
data class DrawerItem(
    val titleResId: Int,
    val icon: ImageVector,
    val route: String,
    val subItems: List<SubDrawerItem>? = null
)
data class SubDrawerItem(
    val titleResId: Int,
    val route: String
)

@Suppress("OPT_IN_ARGUMENT_IS_NOT_MARKER")
@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    @Inject
    lateinit var languageManager: LanguageManager

    @Inject
    lateinit var imageLoader: ImageLoader

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { /* ... */ }

    private fun askPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(arrayOf(Manifest.permission.POST_NOTIFICATIONS))
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    @androidx.annotation.OptIn(UnstableApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val lang = languageManager.getCurrentLanguageSync()
        val localeList = LocaleListCompat.forLanguageTags(lang)
        AppCompatDelegate.setApplicationLocales(localeList)

        enableEdgeToEdge()
        askPermissions()
        startService(Intent(this, PlaybackService::class.java))

        setContent {
            OrtodoxmdandroidTheme {
                var showOnboarding by remember { mutableStateOf(!languageManager.hasSeenOnboarding()) }

                CompositionLocalProvider(LocalImageLoader provides imageLoader) {
                    Crossfade(targetState = showOnboarding, label = "OnboardingCrossfade") { shouldShow ->
                        if (shouldShow) {
                            OnboardingScreen(
                                onOnboardingFinished = {
                                    languageManager.setOnboardingSeen()
                                    showOnboarding = false
                                }
                            )
                        } else {
                            val currentLanguage by languageManager.currentLanguage.collectAsStateWithLifecycle(
                                initialValue = "ro"
                            )
                            key(currentLanguage) {
                                AppScaffold(navController = rememberNavController())
                            }
                        }
                    }
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppScaffold(navController: NavHostController) {
    val mainViewModel: MainViewModel = hiltViewModel()
    val miniPlayerState by mainViewModel.miniPlayerState.collectAsStateWithLifecycle()

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val topBarTitleResId = when (currentRoute?.split("/")?.first()) {
        "home" -> R.string.title_home
        "calendar" -> R.string.menu_calendar
        "anuar" -> R.string.menu_anuar
        "monastery_list" -> R.string.menu_monasteries
        "sacraments" -> R.string.menu_sacraments
        "prayer_categories", "prayer" -> R.string.menu_prayers
        "apologetics" -> R.string.menu_apologetics
        "bible_home" -> R.string.menu_bible
        "saint_lives" -> R.string.menu_saints_lives
        "icons" -> R.string.menu_icons
        "radio" -> R.string.menu_radio
        "audiobook_flow" -> R.string.menu_audiobooks
        "audiobook_player" -> R.string.audiobook_chapter_icon_desc
        "language_selection" -> R.string.title_language
        else -> R.string.title_home
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = { NavigationDrawerContent(navController, drawerState) }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(text = stringResource(id = topBarTitleResId)) },
                    navigationIcon = {
                        IconButton(onClick = { coroutineScope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, contentDescription = stringResource(R.string.menu_open))
                        }
                    }
                )
            },
            bottomBar = {
                // --- AICI ESTE CORECTAREA 1 ---
                // Adăugăm o condiție pentru a ascunde mini-player-ul pe ecranul player-ului principal
                val isPlayerScreenVisible = currentRoute?.startsWith("audiobook_player") ?: false
                AnimatedVisibility(
                    visible = miniPlayerState.isVisible && !isPlayerScreenVisible,
                    enter = slideInVertically(initialOffsetY = { it }),
                    exit = slideOutVertically(targetOffsetY = { it })
                ) {
                    MiniPlayerBar(
                        state = miniPlayerState,
                        onPlayPause = { mainViewModel.togglePlayPause() },
                        onNavigateToPlayer = {
                            navController.navigate("audiobook_player/${miniPlayerState.currentTrackId}") {
                                launchSingleTop = true
                            }
                        }
                    )
                }
            }
        ) { innerPadding ->
            AppNavHost(
                navController = navController,
                modifier = Modifier.padding(innerPadding)
            )
        }
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
        composable("language_selection") { LanguageScreen() }
        navigation(startDestination = "audiobook_categories", route = "audiobook_flow") {
            composable("audiobook_categories") { navBackStackEntry ->
                val parentEntry = remember(navBackStackEntry) { navController.getBackStackEntry("audiobook_flow") }
                val audiobookViewModel: AudiobookViewModel = hiltViewModel(parentEntry)
                val uiState by audiobookViewModel.uiState.collectAsStateWithLifecycle()
                AudiobookCategoriesScreen(
                    navController = navController,
                    categories = uiState.categories,
                    categoryName = stringResource(R.string.menu_audiobooks)
                )
            }
            composable(
                "audiobook_testaments/{categoryName}",
                arguments = listOf(navArgument("categoryName") { defaultValue = "Cărți Audio" })
            ) { navBackStackEntry ->
                val parentEntry = remember(navBackStackEntry) { navController.getBackStackEntry("audiobook_flow") }
                val audiobookViewModel: AudiobookViewModel = hiltViewModel(parentEntry)
                val uiState by audiobookViewModel.uiState.collectAsStateWithLifecycle()
                val categoryName = navBackStackEntry.arguments?.getString("categoryName") ?: stringResource(R.string.menu_audiobooks)
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
    val prayerCategories = listOf(
        SubDrawerItem(R.string.prayer_cat_morning, "prayer/morning"),
        SubDrawerItem(R.string.prayer_cat_evening, "prayer/evening"),
        SubDrawerItem(R.string.prayer_cat_illness, "prayer/for_illness"),
        SubDrawerItem(R.string.prayer_cat_general, "prayer/general")
    )
    val menuItems = listOf(
        MenuItem(DrawerItem(R.string.menu_home, Icons.Default.Home, "home")),
        MenuDivider(R.string.menu_main_section),
        MenuItem(DrawerItem(R.string.menu_calendar, Icons.Default.CalendarMonth, "calendar")),
        MenuItem(DrawerItem(R.string.menu_anuar, Icons.Default.Today, "anuar")),
        MenuItem(DrawerItem(R.string.menu_bible, Icons.Default.Book, "bible_home")),
        MenuDivider(R.string.menu_spiritual_section),
        MenuItem(DrawerItem(R.string.menu_prayers, Icons.AutoMirrored.Filled.MenuBook, "prayer_categories", subItems = prayerCategories)),
        MenuItem(DrawerItem(R.string.menu_saints_lives, Icons.Default.Person, "saint_lives")),
        MenuItem(DrawerItem(R.string.menu_icons, Icons.Default.Image, "icons")),
        MenuItem(DrawerItem(R.string.menu_monasteries, Icons.Default.LocationCity, "monastery_list")),
        MenuItem(DrawerItem(R.string.menu_sacraments, Icons.Default.AutoStories, "sacraments")),
        MenuItem(DrawerItem(R.string.menu_apologetics, Icons.AutoMirrored.Filled.ContactSupport, "apologetics")),
        MenuDivider(R.string.menu_media_section),
        MenuItem(DrawerItem(R.string.menu_radio, Icons.Default.Radio, "radio")),
        MenuItem(DrawerItem(R.string.menu_audiobooks, Icons.Default.Headset, "audiobook_flow")),
        MenuDivider(R.string.menu_settings_section),
        MenuItem(DrawerItem(R.string.menu_language, Icons.Default.Language, "language_selection"))
    )
    ModalDrawerSheet {
        Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp),
                contentAlignment = Alignment.BottomCenter
            ) {
                Image(
                    painter = painterResource(id = R.drawable.nav_drawer_banner),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .blur(8.dp)
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.4f))
                )
                Text(
                    text = stringResource(R.string.app_title),
                    style = MaterialTheme.typography.headlineSmall,
                    color = Color.White,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }
            HorizontalDivider()
            Spacer(modifier = Modifier.height(8.dp))
            menuItems.forEach { menuItem ->
                when (menuItem) {
                    is MenuDivider -> {
                        Text(
                            text = stringResource(id = menuItem.titleResId),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 8.dp)
                        )
                    }
                    is MenuItem -> {
                        val item = menuItem.item
                        val isGroupSelected = currentRoute?.startsWith(item.route) == true
                        NavigationDrawerItem(
                            icon = { Icon(item.icon, contentDescription = stringResource(id = item.titleResId)) },
                            label = { Text(stringResource(id = item.titleResId)) },
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
                                    expandedItem = if (expandedItem == item.titleResId) null else item.titleResId
                                }
                            },
                            badge = {
                                if (item.subItems != null) {
                                    IconButton(onClick = { expandedItem = if (expandedItem == item.titleResId) null else item.titleResId }) {
                                        Icon(
                                            imageVector = if (expandedItem == item.titleResId || isGroupSelected) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                            contentDescription = stringResource(R.string.expand_icon_desc)
                                        )
                                    }
                                }
                            },
                            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                        )
                        if (item.subItems != null && (expandedItem == item.titleResId || isGroupSelected)) {
                            Column(modifier = Modifier.padding(start = 24.dp)) {
                                item.subItems.forEach { subItem ->
                                    NavigationDrawerItem(
                                        label = { Text(stringResource(id = subItem.titleResId)) },
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