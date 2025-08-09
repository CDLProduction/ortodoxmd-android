package md.ortodox.ortodoxmd.ui.icons

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import md.ortodox.ortodoxmd.R
import md.ortodox.ortodoxmd.data.model.Icon
import md.ortodox.ortodoxmd.ui.design.AppListItem
import md.ortodox.ortodoxmd.ui.design.AppLoading
import md.ortodox.ortodoxmd.ui.design.AppPaddings
import md.ortodox.ortodoxmd.ui.design.AppScaffold

@Composable
fun IconsScreen(
    navController: NavController,
    viewModel: IconsViewModel = hiltViewModel()
) {
    val icons by viewModel.icons.collectAsState(emptyList())
    val groupedIcons = icons.groupBy { it.category }.entries.sortedBy { it.key }

    // REFACTORIZAT: Folosim AppScaffold pentru un TopBar și o structură consistentă.
    AppScaffold(title = stringResource(id = R.string.menu_icons)) { paddingValues ->
        if (icons.isEmpty()) {
            // REFACTORIZAT: Folosim AppLoading.
            AppLoading(modifier = Modifier.padding(paddingValues))
        } else {
            LazyColumn(
                modifier = Modifier.padding(paddingValues),
                contentPadding = AppPaddings.content,
                verticalArrangement = Arrangement.spacedBy(AppPaddings.m)
            ) {
                groupedIcons.forEach { (category, categoryIcons) ->
                    item {
                        Text(
                            text = category.replaceFirstChar { it.uppercase() },
                            style = MaterialTheme.typography.headlineMedium,
                            modifier = Modifier.padding(bottom = AppPaddings.s),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    items(categoryIcons.sortedBy { it.nameRo }, key = { it.id }) { icon ->
                        // REFACTORIZAT: Folosim AppListItem pentru un cod mai curat și consistent.
                        AppListItem(
                            title = icon.nameRo,
                            leading = {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = stringResource(R.string.icons_saint_icon_desc),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            },
                            trailing = {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                    contentDescription = stringResource(R.string.icons_view_details)
                                )
                            },
                            onClick = { navController.navigate("icon_detail/${icon.id}") }
                        )
                    }
                }
            }
        }
    }
}
