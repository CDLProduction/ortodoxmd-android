package md.ortodox.ortodoxmd.ui.prayer

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavHostController
import md.ortodox.ortodoxmd.R
import md.ortodox.ortodoxmd.SubDrawerItem
import md.ortodox.ortodoxmd.ui.design.AppListItem
import md.ortodox.ortodoxmd.ui.design.AppPaddings
import md.ortodox.ortodoxmd.ui.design.AppScaffold

private val prayerCategoriesForScreen = listOf(
    SubDrawerItem(R.string.prayer_cat_morning, "prayer/morning"),
    SubDrawerItem(R.string.prayer_cat_evening, "prayer/evening"),
    SubDrawerItem(R.string.prayer_cat_illness, "prayer/for_illness"),
    SubDrawerItem(R.string.prayer_cat_general, "prayer/general")
)

@Composable
fun PrayerCategoriesScreen(
    navController: NavHostController
) {
    // REFACTORIZAT: Folosim AppScaffold pentru o structură consistentă.
    AppScaffold(
        title = stringResource(R.string.prayer_categories_title),
        onBack = { navController.navigateUp() }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize(),
            contentPadding = AppPaddings.content
        ) {
            items(prayerCategoriesForScreen) { category ->
                // REFACTORIZAT: Folosim AppListItem pentru un aspect standardizat.
                AppListItem(
                    title = stringResource(id = category.titleResId),
                    trailing = {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = stringResource(R.string.common_navigate)
                        )
                    },
                    onClick = { navController.navigate(category.route) }
                )
                Divider()
            }
        }
    }
}
