package md.ortodox.ortodoxmd.ui.prayer


import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState

import androidx.compose.ui.Modifier

import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel



//@Composable
//fun PrayerListScreen(category: String, modifier: Modifier = Modifier) {
//    val viewModel: PrayerViewModel = hiltViewModel()
//    viewModel.fetchPrayers(category)
//    val prayers = viewModel.prayers.collectAsState().value
//
//    Column(modifier = modifier.padding(16.dp)) {
//        prayers?.forEach { prayer ->
//            PrayerItem(prayer)
//        } ?: Text("Încărcare rugăciuni...")
//    }
//}