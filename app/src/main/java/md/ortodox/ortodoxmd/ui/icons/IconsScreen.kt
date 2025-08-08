package md.ortodox.ortodoxmd.ui.icons

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.AutoAwesome // O pictogramă generică pentru sfințenie
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import md.ortodox.ortodoxmd.data.model.Icon

@Composable
fun IconsScreen(
    navController: NavController,
    viewModel: IconsViewModel = hiltViewModel()
) {
    val icons by viewModel.icons.collectAsState(emptyList())
    val groupedIcons = icons.groupBy { it.category }.entries.sortedBy { it.key }

    if (icons.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else {
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            groupedIcons.forEach { (category, categoryIcons) ->
                item {
                    Text(
                        text = category.replaceFirstChar { it.uppercase() },
                        style = MaterialTheme.typography.headlineMedium,
                        modifier = Modifier.padding(bottom = 8.dp),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                items(categoryIcons.sortedBy { it.nameRo }, key = { it.id }) { icon ->
                    IconCardItem(
                        icon = icon,
                        onClick = {
                            navController.navigate("icon_detail/${icon.id}")
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun IconCardItem(icon: Icon, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // **NOU: Pictograma decorativă din stânga**
            Icon(
                imageVector = Icons.Default.AutoAwesome, // Poți schimba cu orice altă pictogramă (ex: Icons.Default.Person)
                contentDescription = stringResource(R.string.saint_icon_desc),
                tint = MaterialTheme.colorScheme.primary
            )

            // Numele icoanei
            Text(
                text = icon.nameRo,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.weight(1f),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            // Pictograma de navigare din dreapta
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = stringResource(R.string.view_details),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}