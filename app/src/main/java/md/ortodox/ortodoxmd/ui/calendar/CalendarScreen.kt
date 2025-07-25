package md.ortodox.ortodoxmd.ui.calendar

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import md.ortodox.ortodoxmd.ui.theme.Pink40
import md.ortodox.ortodoxmd.ui.theme.Purple40
import md.ortodox.ortodoxmd.ui.theme.Typography
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(modifier: Modifier = Modifier) {
    val viewModel: CalendarViewModel = hiltViewModel()
    val calendarData by viewModel.calendarData.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val currentMonth by viewModel.currentMonth.collectAsState() // 0-indexed (0=Jan)
    val currentYear by viewModel.currentYear.collectAsState()
    val selectedDate by viewModel.selectedDate.collectAsState()

    var showDetails by remember { mutableStateOf(true) }
    val currentDate = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()) }
    var showDatePicker by remember { mutableStateOf(false) }

    Column(modifier = modifier.padding(16.dp)) {
        // Header with month/year selector
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                // Display month as 1-indexed for the user
                text = "${currentMonth + 1}/$currentYear",
                style = Typography.titleLarge,
            )
            Spacer(Modifier.weight(1f))
            Button(onClick = { viewModel.goToToday() }, modifier = Modifier.padding(end = 8.dp)) {
                Text("Azi")
            }
            Button(onClick = { showDatePicker = true }) {
                Text("Selectează")
            }
        }

        // Date Picker Dialog
        if (showDatePicker) {
            val datePickerState = rememberDatePickerState(
                initialSelectedDateMillis = Calendar.getInstance().apply {
                    set(currentYear, currentMonth, 1)
                }.timeInMillis,
                selectableDates = object : SelectableDates {
                    override fun isSelectableDate(utcTimeMillis: Long): Boolean = true
                }
            )
            DatePickerDialog(
                onDismissRequest = { showDatePicker = false },
                confirmButton = {
                    Button(onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            viewModel.updateFromPicker(millis)
                        }
                        showDatePicker = false
                    }) {
                        Text("OK")
                    }
                },
                dismissButton = {
                    Button(onClick = { showDatePicker = false }) {
                        Text("Anulează")
                    }
                }
            ) {
                DatePicker(state = datePickerState)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Days of the week header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            val daysOfWeek = listOf("D", "L", "M", "M", "J", "V", "S")
            daysOfWeek.forEach { day ->
                Text(
                    text = day,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Grid with the days of the month
        val daysInMonth = viewModel.getDaysInMonth(currentYear, currentMonth)
        val firstDayOfWeek = viewModel.getFirstDayOfWeek(currentYear, currentMonth) // 0=Sun, 1=Mon...

        LazyVerticalGrid(
            columns = GridCells.Fixed(7),
        ) {
            // Add empty boxes for padding before the first day
            items(count = firstDayOfWeek) {
                Box(Modifier)
            }

            // Add the actual day cells
            items(count = daysInMonth) { dayIndex ->
                val day = dayIndex + 1
                val monthForFormat = currentMonth + 1
                val dateStr = String.format("%04d-%02d-%02d", currentYear, monthForFormat, day)
                val isCurrentDay = dateStr == currentDate
                val isSelectedDay = dateStr == selectedDate

                Box(
                    modifier = Modifier
                        .aspectRatio(1f) // Make cells square
                        .padding(4.dp)
                        .then(
                            // Add a border for the current day, regardless of selection
                            if (isCurrentDay) {
                                Modifier.border(1.dp, Purple40, CircleShape)
                            } else {
                                Modifier
                            }
                        )
                        .background(
                            // Fill the background only for the selected day
                            color = if (isSelectedDay) Pink40 else Color.Transparent,
                            shape = CircleShape
                        )
                        .clickable {
                            viewModel.selectDate(dateStr)
                            showDetails = true
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = day.toString(),
                        // Text color should be white if selected, otherwise default
                        color = if (isSelectedDay) Color.White else MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        // Details for the selected day
        AnimatedVisibility(
            visible = showDetails && calendarData != null,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                elevation = CardDefaults.cardElevation(8.dp)
            ) {
                if (errorMessage != null) {
                    Text(
                        text = errorMessage!!,
                        modifier = Modifier.padding(20.dp),
                        color = MaterialTheme.colorScheme.error
                    )
                } else if (calendarData != null) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = calendarData!!.date,
                            style = Typography.titleLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Post: ${calendarData!!.fastingDescriptionRo}",
                            style = Typography.bodyLarge
                        )
                        Text(
                            text = "Rezumat: ${calendarData!!.summaryTitleRo}",
                            style = Typography.bodyLarge,
                            color = MaterialTheme.colorScheme.tertiary,
                            fontSize = 16.sp
                        )
                        Text(
                            text = "Titluri: ${calendarData!!.titlesRo}",
                            style = Typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 16.sp
                        )
                        Text(
                            text = "Sfinți:",
                            style = Typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        calendarData!!.saints.forEach { saint ->
                            Text(
                                text = "• ${saint.nameAndDescriptionRo}",
                                style = Typography.bodyLarge
                            )
                        }
                    }
                }
            }
        }
    }
}
