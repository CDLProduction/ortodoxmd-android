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

@Composable
fun ApologeticScreen(
    viewModel: ApologeticViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {
        OutlinedTextField(
            value = uiState.searchQuery,
            onValueChange = viewModel::onSearchQueryChanged,
            modifier = Modifier
                .fillMaxWidth()
                // REFACTORIZAT: Folosim AppPaddings
                .padding(AppPaddings.l),
            placeholder = { Text(stringResource(R.string.apologetics_search_placeholder)) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = stringResource(R.string.apologetics_search_button)) },
            singleLine = true
        )

        if (uiState.isLoading) {
            // REFACTORIZAT: Folosim componenta AppLoading
            AppLoading()
        } else {
            LazyColumn(
                // REFACTORIZAT: Folosim AppPaddings
                contentPadding = PaddingValues(horizontal = AppPaddings.l),
                verticalArrangement = Arrangement.spacedBy(AppPaddings.m)
            ) {
                val grouped = uiState.apologetics.groupBy { it.category }

                grouped.forEach { (category, apologetics) ->
                    item {
                        Text(
                            text = category,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(top = AppPaddings.l, bottom = AppPaddings.s)
                        )
                    }
                    items(apologetics, key = { it.id }) { apologetic ->
                        ApologeticCard(apologetic = apologetic)
                    }
                }
                item { Spacer(modifier = Modifier.height(AppPaddings.l)) }
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

    // REFACTORIZAT: Folosim componenta AppCard cu funcționalitatea onClick
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
