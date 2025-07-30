@file:Suppress("DEPRECATION")

package md.ortodox.ortodoxmd.ui.calendar

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(modifier: Modifier = Modifier) {
    val viewModel: CalendarViewModel = hiltViewModel()
    val calendarData by viewModel.calendarData.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val currentMonth by viewModel.currentMonth.collectAsState()
    val currentYear by viewModel.currentYear.collectAsState()
    val selectedDate by viewModel.selectedDate.collectAsState()

    var showDetails by remember { mutableStateOf(true) }
    val currentDate = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()) }
    var showDatePicker by remember { mutableStateOf(false) }

    val monthFormatter = remember { SimpleDateFormat("LLLL yyyy", Locale("ro")) }

    Column(modifier = modifier.fillMaxSize().padding(16.dp)) {

        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val calendar = Calendar.getInstance().apply {
                set(Calendar.YEAR, currentYear)
                set(Calendar.MONTH, currentMonth)
            }
            val monthYearTitle = monthFormatter.format(calendar.time).replaceFirstChar { it.uppercase() }

            Text(
                text = monthYearTitle,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.weight(1f))
            OutlinedButton(onClick = { viewModel.goToToday() }, modifier = Modifier.padding(end = 8.dp)) {
                Text("Azi")
            }
            Button(onClick = { showDatePicker = true }) {
                Text("Selectează")
            }
        }

        // Date Picker
        if (showDatePicker) {
            val datePickerState = rememberDatePickerState(
                initialSelectedDateMillis = Calendar.getInstance().apply {
                    set(currentYear, currentMonth, 1)
                }.timeInMillis
            )
            DatePickerDialog(
                onDismissRequest = { showDatePicker = false },
                confirmButton = {
                    Button(onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            viewModel.updateFromPicker(millis)
                        }
                        showDatePicker = false
                    }) { Text("OK") }
                },
                dismissButton = {
                    TextButton(onClick = { showDatePicker = false }) { Text("Anulează") }
                }
            ) {
                DatePicker(state = datePickerState)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Zilele săptămânii
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            val daysOfWeek = listOf("D", "L", "Ma", "Mi", "J", "V", "S")
            daysOfWeek.forEach { day ->
                Text(
                    text = day,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Grila calendarului cu swipe
        Crossfade(
            targetState = "$currentYear-$currentMonth",
            label = "month-transition"
        ) { key ->
            var dragAmount by remember { mutableStateOf(0f) }
            Box(
                modifier = Modifier.pointerInput(key) {
                    detectHorizontalDragGestures(
                        onDragStart = { dragAmount = 0f },
                        onHorizontalDrag = { _, drag -> dragAmount += drag },
                        onDragEnd = {
                            val swipeThreshold = 150
                            if (dragAmount > swipeThreshold) viewModel.goToPreviousMonth()
                            else if (dragAmount < -swipeThreshold) viewModel.goToNextMonth()
                        }
                    )
                }
            ) {
                CalendarGrid(
                    currentYear = currentYear,
                    currentMonth = currentMonth,
                    selectedDate = selectedDate,
                    currentDate = currentDate,
                    onDateSelected = { dateStr ->
                        viewModel.selectDate(dateStr)
                        showDetails = true
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Detaliile zilei
        AnimatedVisibility(
            visible = showDetails && (calendarData != null || errorMessage != null),
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Card(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                when {
                    errorMessage != null -> {
                        Text(
                            text = errorMessage!!,
                            modifier = Modifier.padding(16.dp),
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                    calendarData != null -> {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = calendarData!!.date,
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Divider(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f))

                            // *** CORECȚIE TEMPORARĂ DUBLĂ APLICATĂ AICI ***
                            val correctedFastingDescription = when {
                                calendarData!!.fastingDescriptionRo.equals("Harti", ignoreCase = true) -> "Zi fără post"
                                calendarData!!.fastingDescriptionRo.equals("Post", ignoreCase = true) -> "Zi de post"
                                else -> calendarData!!.fastingDescriptionRo
                            }

                            Text(
                                text = "Post: $correctedFastingDescription",
                                style = MaterialTheme.typography.bodyLarge,
                            )
                            Text(
                                text = "Rezumat: ${calendarData!!.summaryTitleRo}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.secondary
                            )
                            Text(
                                text = "Titluri: ${calendarData!!.titlesRo}",
                                style = MaterialTheme.typography.bodyMedium,
                            )
                            if (calendarData!!.saints.isNotEmpty()) {
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    text = "Sfinți:",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                calendarData!!.saints.forEach { saint ->
                                    Text(
                                        text = "• ${saint.nameAndDescriptionRo}",
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CalendarGrid(
    currentYear: Int,
    currentMonth: Int,
    selectedDate: String,
    currentDate: String,
    onDateSelected: (String) -> Unit
) {
    val viewModel: CalendarViewModel = hiltViewModel()
    val daysInMonth = viewModel.getDaysInMonth(currentYear, currentMonth)
    val firstDayOfWeek = viewModel.getFirstDayOfWeek(currentYear, currentMonth)

    LazyVerticalGrid(
        columns = GridCells.Fixed(7),
        userScrollEnabled = false
    ) {
        items(count = firstDayOfWeek) { Box(Modifier) }

        items(count = daysInMonth) { dayIndex ->
            val day = dayIndex + 1
            val dateStr = String.format("%04d-%02d-%02d", currentYear, currentMonth + 1, day)
            val isCurrentDay = dateStr == currentDate
            val isSelectedDay = dateStr == selectedDate

            Box(
                modifier = Modifier
                    .aspectRatio(1f)
                    .padding(2.dp)
                    .background(
                        color = if (isSelectedDay) MaterialTheme.colorScheme.secondary else Color.Transparent,
                        shape = CircleShape
                    )
                    .border(
                        width = if (isCurrentDay) 1.5.dp else 0.dp,
                        color = if (isCurrentDay) MaterialTheme.colorScheme.primary else Color.Transparent,
                        shape = CircleShape
                    )
                    .clickable { onDateSelected(dateStr) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = day.toString(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = when {
                        isSelectedDay -> MaterialTheme.colorScheme.onSecondary
                        else -> MaterialTheme.colorScheme.onSurface
                    }
                )
            }
        }
    }
}
