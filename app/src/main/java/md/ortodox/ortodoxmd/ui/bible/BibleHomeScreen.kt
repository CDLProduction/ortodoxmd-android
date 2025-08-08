package md.ortodox.ortodoxmd.ui.bible
import md.ortodox.ortodoxmd.R

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.annotation.StringRes
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import java.net.URLDecoder
import java.nio.charset.StandardCharsets

private enum class BibleTab(@StringRes val titleRes: Int, val icon: ImageVector, val route: String) {
    BROWSE(R.string.tab_browse, Icons.AutoMirrored.Filled.MenuBook, "bible_browse"),
    SEARCH(R.string.tab_search, Icons.Default.Search, "bible_search"),
    BOOKMARKS(R.string.tab_bookmarks, Icons.Default.Bookmark, "bible_bookmarks")
}

@Composable
fun BibleHomeScreen(mainNavController: NavHostController) {
    val viewModel: BibleHomeViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Verificăm starea la fiecare (re)intrare pe ecran pentru a prinde momentul post-descărcare.
    LaunchedEffect(Unit) {
        viewModel.checkBibleStatus()
    }

    // Folosim un Box ca layout stabil care nu dispare niciodată.
    // În interior, comutăm vizibilitatea componentelor cu animații.
    Box(modifier = Modifier.fillMaxSize()) {

        // Afișăm interfața Bibliei doar dacă este descărcată.
        AnimatedVisibility(
            visible = uiState.isBibleDownloaded == true,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            BibleInterfaceWithTabs()
        }

        // Afișăm ecranul de descărcare doar dacă NU este descărcată.
        AnimatedVisibility(
            visible = uiState.isBibleDownloaded == false,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            BibleDownloadScreen(onDownloadComplete = {
                // După ce descărcarea e gata, reîmprospătăm starea.
                // AnimatedVisibility se va ocupa de tranziția la BibleInterfaceWithTabs.
                viewModel.checkBibleStatus()
            })
        }

        // Afișăm indicatorul de încărcare doar la început.
        AnimatedVisibility(
            visible = uiState.isBibleDownloaded == null,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
    }
}

@Composable
private fun BibleInterfaceWithTabs() {
    val bibleNavController = rememberNavController()
    var selectedTab by remember { mutableStateOf(BibleTab.BROWSE) }

    Column {
        TabRow(selectedTabIndex = selectedTab.ordinal) {
            BibleTab.values().forEach { tab ->
                Tab(
                    selected = selectedTab == tab,
                    onClick = {
                        selectedTab = tab
                        bibleNavController.navigate(tab.route) {
                            popUpTo(BibleTab.BROWSE.route) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    text = { Text(stringResource(tab.titleRes)) },
                    icon = { Icon(tab.icon, contentDescription = stringResource(tab.titleRes)) }
                )
            }
        }

        NavHost(navController = bibleNavController, startDestination = BibleTab.BROWSE.route) {
            composable(BibleTab.BROWSE.route) { TestamentsScreen(navController = bibleNavController) }
            composable(BibleTab.SEARCH.route) { GlobalSearchScreen(navController = bibleNavController) }
            composable(BibleTab.BOOKMARKS.route) { BookmarksScreen(navController = bibleNavController) }

            composable(
                route = "bible/books/{testamentId}",
                arguments = listOf(navArgument("testamentId") { type = NavType.LongType })
            ) { backStackEntry ->
                BooksScreen(
                    navController = bibleNavController,
                    testamentId = backStackEntry.arguments?.getLong("testamentId")
                )
            }
            composable(
                route = "bible/chapters/{bookId}/{bookName}",
                arguments = listOf(
                    navArgument("bookId") { type = NavType.LongType },
                    navArgument("bookName") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val bookId = backStackEntry.arguments?.getLong("bookId") ?: 0L
                val bookName = backStackEntry.arguments?.getString("bookName")?.let {
                    URLDecoder.decode(it, StandardCharsets.UTF_8.toString())
                } ?: ""
                ChaptersScreen(navController = bibleNavController, bookId = bookId, bookName = bookName)
            }
            composable(
                route = "bible/verses/{bookId}/{bookName}/{chapterNumber}",
                arguments = listOf(
                    navArgument("bookId") { type = NavType.LongType },
                    navArgument("bookName") { type = NavType.StringType },
                    navArgument("chapterNumber") { type = NavType.IntType }
                )
            ) {
                VersesScreen(onBackClick = { bibleNavController.popBackStack() })
            }
        }
    }
}