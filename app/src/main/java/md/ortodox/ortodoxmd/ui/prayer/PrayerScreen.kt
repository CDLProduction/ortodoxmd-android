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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import md.ortodox.ortodoxmd.data.model.Prayer

@Composable
fun PrayerScreen(category: String, modifier: Modifier = Modifier) {
    val viewModel: PrayerViewModel = hiltViewModel()

    // Fetch prayers when the category changes
    LaunchedEffect(key1 = category) {
        viewModel.fetchPrayers(category)
    }

    val uiState by viewModel.uiState.collectAsState()

    // Handle different UI states
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
        // Flatten the list of prayers and their sub-prayers into a single list
        // This is the key to fixing the performance issue.
        prayers.forEach { prayer ->
            item(key = prayer.id) { // Use a stable key for better performance
                PrayerItem(prayer = prayer, level = 0)
            }
        }
    }
}

@Composable
fun PrayerItem(prayer: Prayer, level: Int) {
    var expanded by remember { mutableStateOf(false) }
    val rotationAngle by animateFloatAsState(targetValue = if (expanded) 180f else 0f, label = "rotation")

    val hasSubPrayers = prayer.subPrayers.isNotEmpty()
    val hasContent = prayer.textRo.isNotBlank()

    // Determine if the item is clickable (if it has text or sub-prayers to show)
    val isClickable = hasContent || hasSubPrayers

    Column {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    start = (16 * level).dp, // Indent sub-prayers
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
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = if (level == 0) FontWeight.Bold else FontWeight.Normal,
                    modifier = Modifier.weight(1f)
                )
                // Show expand icon only if there is something to expand
                if (isClickable) {
                    Icon(
                        imageVector = Icons.Default.ExpandMore,
                        contentDescription = "Expand",
                        modifier = Modifier.rotate(rotationAngle)
                    )
                }
            }

            // Expanded content (prayer text)
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

        // Expanded content (sub-prayers)
        AnimatedVisibility(visible = expanded && hasSubPrayers) {
            Column {
                // IMPORTANT: We are NOT using a nested LazyColumn.
                // We just compose the sub-items directly. This is efficient
                // because the parent AnimatedVisibility handles showing/hiding them all at once.
                prayer.subPrayers.forEach { subPrayer ->
                    PrayerItem(prayer = subPrayer, level = level + 1)
                }
            }
        }
    }
}