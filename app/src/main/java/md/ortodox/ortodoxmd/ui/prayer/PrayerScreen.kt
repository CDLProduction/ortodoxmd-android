package md.ortodox.ortodoxmd.ui.prayer

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import md.ortodox.ortodoxmd.data.model.Prayer

@Composable
fun PrayerScreen(category: String, modifier: Modifier = Modifier) {
    val viewModel: PrayerViewModel = hiltViewModel()
    viewModel.fetchPrayers(category)
    val prayers = viewModel.prayers.collectAsState().value

    LazyColumn(modifier = modifier.padding(16.dp)) {
        if (prayers != null) {
            items(prayers.size) { index ->
                PrayerItem(prayers[index])
            }
        } else {
            item {
                Text("Încărcare rugăciuni...", style = MaterialTheme.typography.bodyLarge)
            }
        }
    }
}

@Composable
fun PrayerItem(prayer: Prayer, isSubPrayer: Boolean = false) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = if (isSubPrayer) 16.dp else 0.dp, top = 8.dp, bottom = 8.dp)
            .clickable { expanded = !expanded },
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(
//            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(
//                alpha = 0.9f,
//                red = 30f,
//                green = 30f,
//                blue = 30f
//            )
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = prayer.titleRo,
                style = MaterialTheme.typography.titleMedium,
                fontSize = 18.sp,
                color = MaterialTheme.colorScheme.primary
            )
            AnimatedVisibility(visible = expanded) {
                Text(
                    text = prayer.textRo,  // Folosește textRo procesat
                    style = MaterialTheme.typography.bodyLarge,
                    fontSize = 16.sp,
                    lineHeight = 24.sp,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
            prayer.subPrayers.forEach { subPrayer ->
                PrayerItem(subPrayer, isSubPrayer = true)
            }
        }
    }
}