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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import md.ortodox.ortodoxmd.R
import md.ortodox.ortodoxmd.data.model.Prayer
import md.ortodox.ortodoxmd.ui.design.*

@Composable
fun PrayerScreen(category: String, modifier: Modifier = Modifier) {
    val viewModel: PrayerViewModel = hiltViewModel()

    LaunchedEffect(key1 = category) {
        viewModel.fetchPrayers(category)
    }

    val uiState by viewModel.uiState.collectAsState()

    // REFACTORIZAT: Adăugăm AppScaffold pentru consistență.
    // Titlul este derivat din cheia categoriei.
    val title = category.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }

    AppScaffold(
        title = title,
        onBack = { /* Handled by main NavHost */ }
    ) { paddingValues ->
        Box(modifier = modifier.fillMaxSize().padding(paddingValues)) {
            when (val state = uiState) {
                is PrayerUiState.Loading -> AppLoading()
                is PrayerUiState.Success -> PrayerList(prayers = state.prayers)
                is PrayerUiState.Empty -> AppEmpty(message = stringResource(R.string.prayer_no_prayers_found))
                is PrayerUiState.Error -> AppError(message = stringResource(R.string.prayer_error_message, state.message))
            }
        }
    }
}

@Composable
fun PrayerList(prayers: List<Prayer>, modifier: Modifier = Modifier) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = AppPaddings.l, vertical = AppPaddings.s)
    ) {
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

    val titleStyle = if (level == 0) MaterialTheme.typography.titleMedium else MaterialTheme.typography.titleSmall

    Column {
        // REFACTORIZAT: Folosim AppCard.
        AppCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    start = (12 * level).dp,
                    top = AppPaddings.s,
                    bottom = if (expanded && hasSubPrayers) 0.dp else AppPaddings.s
                ),
            onClick = if (isClickable) { { expanded = !expanded } } else null
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(AppPaddings.l),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = prayer.titleRo,
                    style = titleStyle,
                    modifier = Modifier.weight(1f)
                )
                if (isClickable) {
                    Icon(
                        imageVector = Icons.Default.ExpandMore,
                        contentDescription = stringResource(R.string.prayer_expand_icon_desc),
                        modifier = Modifier.rotate(rotationAngle)
                    )
                }
            }

            AnimatedVisibility(visible = expanded && hasContent) {
                Text(
                    text = prayer.textRo,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier
                        .padding(horizontal = AppPaddings.l)
                        .padding(bottom = AppPaddings.l)
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
