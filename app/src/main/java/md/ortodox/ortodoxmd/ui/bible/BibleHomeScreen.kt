package md.ortodox.ortodoxmd.ui.bible

import androidx.compose.foundation.layout.Column
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

private enum class BibleTab(val title: String, val icon: ImageVector) {
    BROWSE("Răsfoire", Icons.AutoMirrored.Filled.MenuBook),
    SEARCH("Căutare", Icons.Default.Search),
    BOOKMARKS("Semne de Carte", Icons.Default.Bookmark),
    OFFLINE("Offline", Icons.Default.CloudDownload) // NOU: Tab pentru descărcare
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
                        bibleNavController.navigate(tab.name) {
                            // Corecția este aici: launchSingleTop trebuie să fie în acest bloc,
                            // nu în interiorul lui popUpTo.
                            launchSingleTop = true
                            popUpTo(bibleNavController.graph.startDestinationId) {
                                // Opțiunile specifice pentru popUpTo vin aici, dacă sunt necesare
                            }
                        }
                    },
                    text = { Text(tab.title) },
                    icon = { Icon(tab.icon, contentDescription = tab.title) }
                )
            }
        }

        NavHost(
            navController = bibleNavController,
            startDestination = BibleTab.BROWSE.name
        ) {
            composable(BibleTab.BROWSE.name) {
                TestamentsScreen(navController = mainNavController)
            }
            composable(BibleTab.SEARCH.name) {
                GlobalSearchScreen(navController = mainNavController)
            }
            composable(BibleTab.BOOKMARKS.name) {
                BookmarksScreen(navController = mainNavController)
            }
            // NOU: Rută pentru ecranul de descărcare
            composable(BibleTab.OFFLINE.name) {
                BibleDownloadScreen()
            }
        }
    }
}
