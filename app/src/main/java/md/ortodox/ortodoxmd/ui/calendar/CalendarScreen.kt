package md.ortodox.ortodoxmd.ui.calendar

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.ui.tooling.preview.Preview
import md.ortodox.ortodoxmd.ui.theme.Pink40
import md.ortodox.ortodoxmd.ui.theme.Purple40
import md.ortodox.ortodoxmd.ui.theme.OrtodoxmdandroidTheme
import md.ortodox.ortodoxmd.ui.theme.Typography
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun CalendarScreen(modifier: Modifier = Modifier) {
    val viewModel: CalendarViewModel = hiltViewModel()
    val calendarData = viewModel.calendarData.collectAsState().value
    val errorMessage = viewModel.errorMessage.collectAsState().value
    val currentMonth = remember { mutableStateOf(Calendar.getInstance()) }
    var selectedDate by remember { mutableStateOf(viewModel.getCurrentDate()) }
    var showDetails by remember { mutableStateOf(true) }
    val currentDate = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()) }

    Column(modifier = modifier.padding(16.dp)) {
        // Header cu navigare lună
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = {
                currentMonth.value.add(Calendar.MONTH, -1)
                showDetails = false
            }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Previous Month")
            }
            Text(
                text = "Calendar Ortodox - ${currentMonth.value.get(Calendar.MONTH) + 1}/${currentMonth.value.get(Calendar.YEAR)}",
                modifier = Modifier.weight(1f),
                style = Typography.titleLarge
            )
            IconButton(onClick = {
                currentMonth.value.add(Calendar.MONTH, 1)
                showDetails = false
            }) {
                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Next Month")
            }
        }

        // Card pentru calendar
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            elevation = CardDefaults.cardElevation(4.dp)
        ) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(7),
                modifier = Modifier.padding(16.dp)
            ) {
                items(42) { index ->
                    val day = index - currentMonth.value.get(Calendar.DAY_OF_WEEK) + 1
                    if (day > 0 && day <= currentMonth.value.getActualMaximum(Calendar.DAY_OF_MONTH)) {
                        val month = currentMonth.value.get(Calendar.MONTH) + 1
                        val year = currentMonth.value.get(Calendar.YEAR)
                        val dateStr = String.format("%04d-%02d-%02d", year, month, day)
                        val isCurrentDay = dateStr == currentDate
                        val isSelectedDay = dateStr == selectedDate
                        Box(
                            modifier = Modifier
                                .clickable {
                                    selectedDate = dateStr
                                    viewModel.fetchCalendarData(selectedDate)
                                    showDetails = true
                                }
                                .padding(8.dp)
                                .run {
                                    when {
                                        isCurrentDay -> background(Purple40, shape = androidx.compose.foundation.shape.CircleShape)
                                        isSelectedDay -> background(Pink40, shape = androidx.compose.foundation.shape.CircleShape)
                                        else -> this
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = day.toString(),
                                color = if (isCurrentDay || isSelectedDay) Color.White else Color.Unspecified
                            )
                        }
                    } else {
                        Box(modifier = Modifier.padding(8.dp)) // Zile goale
                    }
                }
            }
        }

        // Detalii pentru ziua selectată cu animație și design estetic
        AnimatedVisibility(
            visible = showDetails,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                elevation = CardDefaults.cardElevation(8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (errorMessage != null) {
                        Text(
                            text = errorMessage,
                            style = Typography.bodyLarge,
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 16.sp
                        )
                    } else {
                        calendarData?.let {
                            // Data centrată
                            Text(
                                text = it.date,
                                style = Typography.titleLarge,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp)
                                    .align(Alignment.CenterHorizontally)
                            )
                            // Post
                            Text(
                                text = it.fastingDescriptionRo,
                                style = Typography.bodyLarge,
                                color = MaterialTheme.colorScheme.secondary,
                                fontSize = 24.sp,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp)
                                    .align(Alignment.CenterHorizontally)

                            )
                            // Rezumat
                            Text(
                                text = "Rezumat: ${it.summaryTitleRo}",
                                style = Typography.bodyLarge,
                                color = MaterialTheme.colorScheme.tertiary,
                                fontSize = 16.sp
                            )
                            // Titluri
                            Text(
                                text = "Titluri: ${it.titlesRo}",
                                style = Typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontSize = 16.sp
                            )
                            // Sfinți
                            Text(
                                text = "Sfinți:",
                                style = Typography.titleMedium,
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 18.sp
                            )
                            it.saints.forEach { saint ->
                                Text(
                                    text = "• ${saint.nameAndDescriptionRo}",
                                    style = Typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontSize = 16.sp
                                )
                            }
                        } ?: Text(
                            text = "Încărcare date...",
                            style = Typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 16.sp
                        )
                    }
                }
            }
        }
    }
}