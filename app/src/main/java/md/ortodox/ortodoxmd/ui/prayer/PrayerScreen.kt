package md.ortodox.ortodoxmd.ui.prayer

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import md.ortodox.ortodoxmd.data.model.Prayer

@Composable
fun PrayerScreen(category: String, modifier: Modifier = Modifier) {
    val viewModel: PrayerViewModel = hiltViewModel()

    LaunchedEffect(key1 = category) {
        viewModel.fetchPrayers(category)
    }

    val uiState by viewModel.uiState.collectAsState()

    Box(modifier = modifier.fillMaxSize()) {
        when (val state = uiState) {
            is PrayerUiState.Loading -> {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
            is PrayerUiState.Success -> {
                PrayerList(prayers = state.prayers)
            }
            is PrayerUiState.Empty -> {
                Text(
                    text = "Nu au fost găsite rugăciuni în această categorie.",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
            is PrayerUiState.Error -> {
                Text(
                    text = "A apărut o eroare: ${state.message}",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.align(Alignment.Center).padding(16.dp)
                )
            }
        }
    }
}

@Composable
fun PrayerList(prayers: List<Prayer>, modifier: Modifier = Modifier) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
    ) {
        // Folosim `items` pentru o implementare mai curată și performantă
        items(
            items = prayers,
            key = { it.id }
        ) { prayer ->
            PrayerItem(prayer = prayer, level = 0)
        }
    }
}

@Composable
fun PrayerItem(prayer: Prayer, level: Int) {
    var expanded by remember { mutableStateOf(false) }
    val rotationAngle by animateFloatAsState(targetValue = if (expanded) 180f else 0f, label = "rotation")

    val hasSubPrayers = prayer.subPrayers.isNotEmpty()
    val hasContent = prayer.textRo.isNotBlank()
    val isClickable = hasContent || hasSubPrayers

    // Folosim stiluri de text diferite pentru ierarhie vizuală
    val titleStyle = if (level == 0) MaterialTheme.typography.titleMedium else MaterialTheme.typography.titleSmall

    Column {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    start = (12 * level).dp, // Indentare mai subtilă
                    top = 8.dp,
                    bottom = if (expanded && hasSubPrayers) 0.dp else 8.dp
                ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(enabled = isClickable) { expanded = !expanded }
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = prayer.titleRo,
                    style = titleStyle, // Stilul se aplică direct
                    modifier = Modifier.weight(1f)
                )
                if (isClickable) {
                    Icon(
                        imageVector = Icons.Default.ExpandMore,
                        contentDescription = "Expand",
                        modifier = Modifier.rotate(rotationAngle)
                    )
                }
            }

            AnimatedVisibility(visible = expanded && hasContent) {
                Text(
                    text = prayer.textRo,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .padding(bottom = 16.dp)
                )
            }
        }

        AnimatedVisibility(visible = expanded && hasSubPrayers) {
            Column {
                prayer.subPrayers.forEach { subPrayer ->
                    PrayerItem(prayer = subPrayer, level = level + 1)
                }
            }
        }
    }
}