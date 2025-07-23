package md.ortodox.ortodoxmd.ui.calendar

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun CalendarScreen(modifier: Modifier = Modifier) {
    val viewModel: CalendarViewModel = hiltViewModel()
    val calendarData = viewModel.calendarData.collectAsState().value
    val errorMessage = viewModel.errorMessage.collectAsState().value

    Column(modifier = modifier) {
        if (errorMessage != null) {
            Text(text = errorMessage)
        } else {
            Text(text = "Calendar Ortodox - ${calendarData?.date ?: "Loading..."}")
            calendarData?.let {
                Text(text = "Fasting: ${it.fastingDescriptionEn}")
                Text(text = "Summary: ${it.summaryTitleEn}")
                Text(text = "Titles: ${it.titlesEn}")
                Text(text = "Saints:")
                it.saints.forEach { saint ->
                    Text(text = "- ${saint.nameAndDescriptionEn}")
                }
            } ?: Text(text = "Loading data...")
        }
    }
}