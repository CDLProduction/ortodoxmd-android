package md.ortodox.ortodoxmd.features.calendar.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import md.ortodox.ortodoxmd.features.calendar.viewmodel.CalendarViewModel

@Composable
fun CalendarScreen(date: String = "2025-07-19", lang: String = "en", viewModel: CalendarViewModel = hiltViewModel()) {
    val calendarData = viewModel.calendarData.collectAsState().value
    val error = viewModel.error.collectAsState().value

    LaunchedEffect(date) {
        viewModel.loadCalendar(date, lang)
    }

    Column(modifier = Modifier.padding(16.dp)) {
        if (error != null) {
            Text("Error: $error")
        } else if (calendarData != null) {
            Text("Date: ${calendarData.date}")
            Text("Fasting: ${calendarData.fastingDescriptionEn}")
            Text("Summary: ${calendarData.summaryTitleEn}")
            LazyColumn {
                items(calendarData.saints) { saint ->
                    Text(saint.nameAndDescriptionEn)
                }
            }
        } else {
            Text("Loading...")
        }
    }
}