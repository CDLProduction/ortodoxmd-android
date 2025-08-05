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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import md.ortodox.ortodoxmd.data.model.CalendarData
import md.ortodox.ortodoxmd.domain.model.HolidayRank
import md.ortodox.ortodoxmd.domain.model.RedLetterDays
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(modifier: Modifier = Modifier, viewModel: CalendarViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showDatePicker by remember { mutableStateOf(false) }
    val monthFormatter = remember { SimpleDateFormat("LLLL yyyy", Locale("ro")) }

    Column(modifier = modifier.fillMaxSize().padding(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val monthYearTitle = monthFormatter.format(uiState.selectedDate.time).replaceFirstChar { it.uppercase() }
            Text(monthYearTitle, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.weight(1f))
            OutlinedButton(onClick = viewModel::goToToday) { Text("Azi") }
            Spacer(Modifier.width(8.dp))
            Button(onClick = { showDatePicker = true }) { Text("Selectează") }
        }

        if (showDatePicker) {
            val datePickerState = rememberDatePickerState(initialSelectedDateMillis = uiState.selectedDate.timeInMillis)
            DatePickerDialog(
                onDismissRequest = { showDatePicker = false },
                confirmButton = {
                    Button(onClick = {
                        datePickerState.selectedDateMillis?.let { viewModel.updateFromPicker(it) }
                        showDatePicker = false
                    }) { Text("OK") }
                },
                dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text("Anulează") } }
            ) { DatePicker(state = datePickerState) }
        }

        Spacer(modifier = Modifier.height(16.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
            listOf("L", "Ma", "Mi", "J", "V", "S", "D").forEach { day ->
                Text(day, style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Center, modifier = Modifier.weight(1f))
            }
        }
        Spacer(modifier = Modifier.height(8.dp))

        Box(
            modifier = Modifier.pointerInput(uiState.selectedDate.get(Calendar.MONTH)) {
                detectHorizontalDragGestures { _, dragAmount ->
                    if (dragAmount < -50) viewModel.goToNextMonth()
                    if (dragAmount > 50) viewModel.goToPreviousMonth()
                }
            }
        ) {
            AnimatedContent(
                targetState = "${uiState.selectedDate.get(Calendar.YEAR)}-${uiState.selectedDate.get(Calendar.MONTH)}",
                transitionSpec = {
                    if (targetState > initialState) {
                        slideInHorizontally { width -> -width } togetherWith slideOutHorizontally { width -> width }
                    } else {
                        slideInHorizontally { width -> width } togetherWith slideOutHorizontally { width -> -width }
                    }
                }, label = "month_swipe"
            ) {
                CalendarGrid(
                    calendar = uiState.selectedDate,
                    dataForMonth = uiState.dataForVisibleMonth,
                    onDateSelected = { day ->
                        val newDate = (uiState.selectedDate.clone() as Calendar).apply { set(Calendar.DAY_OF_MONTH, day) }
                        viewModel.selectDate(newDate)
                    }
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))

        AnimatedVisibility(visible = !uiState.isLoading, enter = fadeIn(), exit = fadeOut()) {
            DayDetails(data = uiState.dataForSelectedDay)
        }
    }
}

@Composable
private fun CalendarGrid(
    calendar: Calendar,
    dataForMonth: Map<String, CalendarData>,
    onDateSelected: (Int) -> Unit
) {
    val tempCalendar = calendar.clone() as Calendar
    tempCalendar.set(Calendar.DAY_OF_MONTH, 1)

    var firstDayOfWeek = tempCalendar.get(Calendar.DAY_OF_WEEK) - Calendar.MONDAY
    if (firstDayOfWeek < 0) firstDayOfWeek += 7

    val daysInMonth = tempCalendar.getActualMaximum(Calendar.DAY_OF_MONTH)
    val today = Calendar.getInstance()
    val dateFormat = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) }

    LazyVerticalGrid(columns = GridCells.Fixed(7), userScrollEnabled = false) {
        items(firstDayOfWeek) { Box(Modifier.aspectRatio(1f)) }
        items(daysInMonth) { dayIndex ->
            val day = dayIndex + 1
            tempCalendar.set(Calendar.DAY_OF_MONTH, day)
            val dateStr = dateFormat.format(tempCalendar.time)

            val isToday = today.get(Calendar.YEAR) == tempCalendar.get(Calendar.YEAR) &&
                    today.get(Calendar.DAY_OF_YEAR) == tempCalendar.get(Calendar.DAY_OF_YEAR)
            val isSelected = calendar.get(Calendar.DAY_OF_MONTH) == day

            val holidayRank = RedLetterDays.getHolidayInfo(dataForMonth[dateStr], tempCalendar)
            val isRedHoliday = holidayRank != null

            val dayColor = when {
                isSelected -> MaterialTheme.colorScheme.onPrimary
                isRedHoliday -> MaterialTheme.colorScheme.error
                else -> MaterialTheme.colorScheme.onSurface
            }
            val fontWeight = when {
                holidayRank == HolidayRank.GREAT_FEAST -> FontWeight.ExtraBold
                isRedHoliday -> FontWeight.Bold
                else -> FontWeight.Normal
            }
            val backgroundColor = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent
            val borderModifier = if (isToday) Modifier.border(1.5.dp, MaterialTheme.colorScheme.primary, CircleShape) else Modifier

            Box(
                modifier = Modifier.aspectRatio(1f).padding(2.dp).then(borderModifier)
                    .background(backgroundColor, CircleShape)
                    .clickable { onDateSelected(day) },
                contentAlignment = Alignment.Center
            ) {
                Text(text = day.toString(), color = dayColor, fontWeight = fontWeight)
            }
        }
    }
}

@Composable
private fun DayDetails(data: CalendarData?) {
    if (data == null) return

    val calendar = remember(data.date) {
        Calendar.getInstance().apply {
            time = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(data.date) ?: Date()
        }
    }
    val holidayRank = RedLetterDays.getHolidayInfo(data, calendar)

    // **AICI ESTE CORECȚIA**
    // Reintroducem logica pentru a înlocui "Harti" cu un text mai clar.
    val correctedFastingDescription = when (data.fastingDescriptionRo.lowercase(Locale.ROOT)) {
        "harti" -> "Zi fără post"
        "post" -> "Zi de post"
        else -> data.fastingDescriptionRo
    }

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = data.summaryTitleRo,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                if (holidayRank != null) {
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "✝️",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
            Divider()

            // Folosim textul corectat
            Text("Post: $correctedFastingDescription", style = MaterialTheme.typography.bodyLarge)

            if (data.saints.isNotEmpty()) {
                Text("Sfinții zilei:", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                Column {
                    data.saints.forEach { saint ->
                        Text("• ${saint.nameAndDescriptionRo}", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }
    }
}