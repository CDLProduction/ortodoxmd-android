package md.ortodox.ortodoxmd.ui.sacrament

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import md.ortodox.ortodoxmd.R
import md.ortodox.ortodoxmd.data.model.Sacrament
import md.ortodox.ortodoxmd.ui.design.AppCard
import md.ortodox.ortodoxmd.ui.design.AppLoading
import md.ortodox.ortodoxmd.ui.design.AppPaddings
import md.ortodox.ortodoxmd.ui.design.AppScaffold

@Composable
fun SacramentScreen(
    viewModel: SacramentViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    // REFACTORIZAT: Folosim AppScaffold.
    AppScaffold(title = stringResource(id = R.string.menu_sacraments)) { paddingValues ->
        if (uiState.isLoading) {
            // REFACTORIZAT: Folosim AppLoading.
            AppLoading(modifier = Modifier.padding(paddingValues))
        } else {
            LazyColumn(
                modifier = Modifier.padding(paddingValues),
                contentPadding = AppPaddings.content,
                verticalArrangement = Arrangement.spacedBy(AppPaddings.l)
            ) {
                val groupedSacraments = uiState.sacraments.groupBy { it.category }

                groupedSacraments.forEach { (category, sacraments) ->
                    item {
                        Text(
                            text = category.replaceFirstChar { it.titlecase() },
                            style = MaterialTheme.typography.headlineSmall,
                            modifier = Modifier.padding(bottom = AppPaddings.s),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    items(sacraments, key = { it.id }) { sacrament ->
                        SacramentCard(sacrament = sacrament)
                    }
                }
            }
        }
    }
}

@Composable
private fun SacramentCard(sacrament: Sacrament) {
    var isExpanded by remember { mutableStateOf(false) }
    val rotationAngle by animateFloatAsState(targetValue = if (isExpanded) 180f else 0f, label = "expansion_arrow")

    // REFACTORIZAT: Folosim AppCard.
    AppCard(
        onClick = { isExpanded = !isExpanded },
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(horizontal = AppPaddings.l, vertical = AppPaddings.m)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(AppPaddings.l)
            ) {
                Icon(
                    imageVector = Icons.Default.Bookmark,
                    contentDescription = stringResource(R.string.sacrament_icon_desc),
                    tint = MaterialTheme.colorScheme.secondary
                )
                Text(
                    text = sacrament.titleRo,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    imageVector = Icons.Default.ExpandMore,
                    contentDescription = stringResource(R.string.sacrament_expand),
                    modifier = Modifier.rotate(rotationAngle)
                )
            }

            AnimatedVisibility(visible = isExpanded) {
                Column {
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = AppPaddings.m),
                        thickness = DividerDefaults.Thickness,
                        color = DividerDefaults.color
                    )
                    Text(
                        text = sacrament.formattedDescriptionRo,
                        style = MaterialTheme.typography.bodyLarge.copy(lineHeight = 24.sp)
                    )
                }
            }
        }
    }
}
