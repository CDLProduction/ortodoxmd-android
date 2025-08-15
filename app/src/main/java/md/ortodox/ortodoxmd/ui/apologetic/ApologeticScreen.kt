package md.ortodox.ortodoxmd.ui.apologetic

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import md.ortodox.ortodoxmd.R
import md.ortodox.ortodoxmd.data.model.Apologetic
import md.ortodox.ortodoxmd.ui.apologetics.ApologeticViewModel
import md.ortodox.ortodoxmd.ui.design.AppCard
import md.ortodox.ortodoxmd.ui.design.AppLoading
import md.ortodox.ortodoxmd.ui.design.AppPaddings
import md.ortodox.ortodoxmd.ui.design.AppScaffold

// OPTIMIZARE: Pas 1 - Crearea unei clase sigilate pentru a defini tipurile de conținut.
private sealed class ApologeticListItem {
    data class Header(val title: String) : ApologeticListItem()
    data class Item(val apologetic: Apologetic) : ApologeticListItem()
}

@Composable
fun ApologeticScreen(
    viewModel: ApologeticViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    // OPTIMIZARE: Pas 2 - Transformarea datelor grupate într-o singură listă plată.
    val listItems = remember(uiState.apologetics) {
        uiState.apologetics.groupBy { it.category }.entries.flatMap { (category, items) ->
            listOf(ApologeticListItem.Header(category)) + items.map { ApologeticListItem.Item(it) }
        }
    }

    // REFACTORIZAT: Am adăugat AppScaffold pentru consistență.
    AppScaffold(title = stringResource(id = R.string.menu_apologetics)) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = viewModel::onSearchQueryChanged,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(AppPaddings.l),
                placeholder = { Text(stringResource(R.string.apologetics_search_placeholder)) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = stringResource(R.string.apologetics_search_button)) },
                singleLine = true
            )

            if (uiState.isLoading) {
                AppLoading()
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = AppPaddings.l),
                    verticalArrangement = Arrangement.spacedBy(AppPaddings.l)
                ) {
                    // OPTIMIZARE: Pas 3 - Folosirea listei unice cu 'key' și 'contentType'.
                    items(
                        items = listItems,
                        key = { item ->
                            when (item) {
                                is ApologeticListItem.Header -> item.title
                                is ApologeticListItem.Item -> item.apologetic.id
                            }
                        },
                        contentType = { item ->
                            when (item) {
                                is ApologeticListItem.Header -> "header"
                                is ApologeticListItem.Item -> "apologetic_item"
                            }
                        }
                    ) { item ->
                        when (item) {
                            is ApologeticListItem.Header -> {
                                Text(
                                    text = item.title,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(top = AppPaddings.l, bottom = AppPaddings.s)
                                )
                            }
                            is ApologeticListItem.Item -> {
                                ApologeticCard(apologetic = item.apologetic)
                            }
                        }
                    }
                    item { Spacer(modifier = Modifier.height(AppPaddings.l)) }
                }
            }
        }
    }
}

@Composable
private fun ApologeticCard(apologetic: Apologetic) {
    var isExpanded by remember { mutableStateOf(false) }
    val rotationAngle by animateFloatAsState(
        targetValue = if (isExpanded) 180f else 0f,
        label = "expansion_arrow"
    )

    AppCard(
        onClick = { isExpanded = !isExpanded },
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(AppPaddings.l)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(AppPaddings.l)
            ) {
                Icon(
                    imageVector = Icons.Default.HelpOutline,
                    contentDescription = stringResource(R.string.apologetics_question_icon_desc),
                    tint = MaterialTheme.colorScheme.secondary
                )
                Text(
                    text = apologetic.questionRo,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    imageVector = Icons.Default.ExpandMore,
                    contentDescription = stringResource(R.string.apologetics_expand_icon_desc),
                    modifier = Modifier.rotate(rotationAngle)
                )
            }

            AnimatedVisibility(visible = isExpanded) {
                Column {
                    Divider(modifier = Modifier.padding(vertical = AppPaddings.m))
                    Text(
                        text = apologetic.formattedAnswerRo,
                        style = MaterialTheme.typography.bodyLarge.copy(lineHeight = 24.sp)
                    )
                }
            }
        }
    }
}
