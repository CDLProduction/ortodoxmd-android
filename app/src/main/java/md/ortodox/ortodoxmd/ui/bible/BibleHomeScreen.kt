package md.ortodox.ortodoxmd.ui.bible

import androidx.compose.foundation.layout.Column
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import java.net.URLDecoder
import java.nio.charset.StandardCharsets

private enum class BibleTab(val title: String, val icon: ImageVector, val route: String) {
    BROWSE("Răsfoire", Icons.AutoMirrored.Filled.MenuBook, "bible_testaments_entry"),
    SEARCH("Căutare", Icons.Default.Search, "bible_search"),
    BOOKMARKS("Semne de Carte", Icons.Default.Bookmark, "bible_bookmarks"),
    OFFLINE("Offline", Icons.Default.CloudDownload, "bible_offline")
}

@Composable
fun BibleHomeScreen(mainNavController: NavHostController) {
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
                            launchSingleTop = true
                            popUpTo(bibleNavController.graph.startDestinationId)
                        }
                    },
                    text = { Text(tab.title) },
                    icon = { Icon(tab.icon, contentDescription = tab.title) }
                )
            }
        }

        NavHost(
            navController = bibleNavController,
            startDestination = BibleTab.BROWSE.route
        ) {
            composable(BibleTab.BROWSE.route) {
                TestamentsScreen(navController = bibleNavController)
            }
            composable(BibleTab.SEARCH.route) {
                GlobalSearchScreen(navController = bibleNavController)
            }
            composable(BibleTab.BOOKMARKS.route) {
                BookmarksScreen(navController = bibleNavController)
            }
            composable(BibleTab.OFFLINE.route) {
                BibleDownloadScreen()
            }
            composable(
                route = "bible/books/{testamentId}",
                arguments = listOf(navArgument("testamentId") { type = NavType.LongType })
            ) { backStackEntry ->
                val testamentId = backStackEntry.arguments?.getLong("testamentId")
                BooksScreen(navController = bibleNavController, testamentId = testamentId)
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