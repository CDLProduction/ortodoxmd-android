package md.ortodox.ortodoxmd.ui.icons

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import md.ortodox.ortodoxmd.R
import md.ortodox.ortodoxmd.ui.design.*

// OPTIMIZARE: Pas 1 - Crearea unei clase sigilate pentru a defini tipurile de conținut din listă.
private sealed class IconListItem {
    data class Header(val title: String) : IconListItem()
    data class IconItem(val icon: md.ortodox.ortodoxmd.data.model.Icon) : IconListItem()
}

@Composable
fun IconsScreen(
    navController: NavController,
    viewModel: IconsViewModel = hiltViewModel()
) {
    val icons by viewModel.icons.collectAsState(emptyList())
    val groupedIcons = icons.groupBy { it.category }.entries.sortedBy { it.key }

    // OPTIMIZARE: Pas 2 - Transformarea datelor grupate într-o singură listă plată.
    // 'remember' asigură că această transformare costisitoare se execută doar când datele se schimbă.
    val listItems = remember(groupedIcons) {
        groupedIcons.flatMap { (category, categoryIcons) ->
            listOf(IconListItem.Header(category)) + categoryIcons.sortedBy { it.nameRo }.map { IconListItem.IconItem(it) }
        }
    }

    AppScaffold(title = stringResource(id = R.string.menu_icons)) { paddingValues ->
        if (icons.isEmpty()) {
            AppLoading(modifier = Modifier.padding(paddingValues))
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = AppPaddings.content,
                verticalArrangement = Arrangement.spacedBy(AppPaddings.xs)
            ) {
                // OPTIMIZARE: Pas 3 - Folosirea listei unice cu 'key' și 'contentType'.
                items(
                    items = listItems,
                    key = { item ->
                        // Cheia trebuie să fie unică pe întreaga listă.
                        when (item) {
                            is IconListItem.Header -> item.title
                            is IconListItem.IconItem -> item.icon.id
                        }
                    },
                    contentType = { item ->
                        // Specificăm tipul de conținut pentru a ajuta la refolosirea elementelor.
                        when (item) {
                            is IconListItem.Header -> "header"
                            is IconListItem.IconItem -> "icon_item"
                        }
                    }
                ) { item ->
                    // Afișăm componenta corespunzătoare în funcție de tip.
                    when (item) {
                        is IconListItem.Header -> {
                            Text(
                                text = item.title.replaceFirstChar { it.uppercase() },
                                style = MaterialTheme.typography.headlineMedium,
                                modifier = Modifier.padding(top = AppPaddings.l, bottom = AppPaddings.s),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        is IconListItem.IconItem -> {
                            AppListItem(
                                title = item.icon.nameRo,
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
                                onClick = { navController.navigate("icon_detail/${item.icon.id}") }
                            )
                            Divider()
                        }
                    }
                }
            }
        }
    }
}
