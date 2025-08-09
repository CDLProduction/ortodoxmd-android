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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import md.ortodox.ortodoxmd.R
import md.ortodox.ortodoxmd.data.model.CalendarData
import md.ortodox.ortodoxmd.domain.model.HolidayRank
import md.ortodox.ortodoxmd.domain.model.RedLetterDays
import md.ortodox.ortodoxmd.ui.design.AppCard
import md.ortodox.ortodoxmd.ui.design.AppPaddings
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(modifier: Modifier = Modifier, viewModel: CalendarViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showDatePicker by remember { mutableStateOf(false) }
    val monthFormatter = remember { SimpleDateFormat("LLLL yyyy", Locale("ro")) }

    Column(modifier = modifier.fillMaxSize().padding(AppPaddings.l)) {
        // Controalele de sus rămân neschimbate
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val monthYearTitle = monthFormatter.format(uiState.selectedDate.time).replaceFirstChar { it.uppercase() }
            Text(monthYearTitle, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.weight(1f))
            OutlinedButton(onClick = viewModel::goToToday) { Text(stringResource(R.string.calendar_today)) }
            Spacer(Modifier.width(AppPaddings.s))
            Button(onClick = { showDatePicker = true }) { Text(stringResource(R.string.calendar_select)) }
        }

        if (showDatePicker) {
            val datePickerState = rememberDatePickerState(initialSelectedDateMillis = uiState.selectedDate.timeInMillis)
            DatePickerDialog(
                onDismissRequest = { showDatePicker = false },
                confirmButton = {
                    Button(onClick = {
                        datePickerState.selectedDateMillis?.let { viewModel.updateFromPicker(it) }
                        showDatePicker = false
                    }) { Text(stringResource(R.string.calendar_ok_button)) }
                },
                dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text(stringResource(R.string.calendar_cancel_button)) } }
            ) { DatePicker(state = datePickerState) }
        }

        Spacer(modifier = Modifier.height(AppPaddings.l))

        // ADAUGAT: Am învelit calendarul într-un AppCard.
        AppCard(
            modifier = Modifier
                .fillMaxWidth()
                .pointerInput(uiState.selectedDate.get(Calendar.MONTH)) {
                    detectHorizontalDragGestures { _, dragAmount ->
                        if (dragAmount < -50) viewModel.goToNextMonth()
                        if (dragAmount > 50) viewModel.goToPreviousMonth()
                    }
                }
        ) {
            Column(modifier = Modifier.padding(AppPaddings.m)) {
                // Numele zilelor săptămânii
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                    listOf(
                        stringResource(R.string.day_monday_short),
                        stringResource(R.string.day_tuesday_short),
                        stringResource(R.string.day_wednesday_short),
                        stringResource(R.string.day_thursday_short),
                        stringResource(R.string.day_friday_short),
                        stringResource(R.string.day_saturday_short),
                        stringResource(R.string.day_sunday_short)
                    ).forEach { day ->
                        Text(day, style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Center, modifier = Modifier.weight(1f))
                    }
                }
                Spacer(modifier = Modifier.height(AppPaddings.s))

                // Animația pentru swipe între luni
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
        }
        Spacer(modifier = Modifier.height(AppPaddings.l))

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

    LazyVerticalGrid(columns = GridCells.Fixed(7), userScrollEnabled = false, modifier = Modifier.height(300.dp)) { // Înălțime fixă pentru a evita redimensionarea cardului
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

    val correctedFastingDescription = when (data.fastingDescriptionRo.lowercase(Locale.ROOT)) {
        "harti" -> "Zi fără post"
        "post" -> "Zi de post"
        else -> data.fastingDescriptionRo
    }

    AppCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(AppPaddings.l), verticalArrangement = Arrangement.spacedBy(AppPaddings.s)) {
            val annotatedTitle = buildAnnotatedString {
                if (holidayRank != null) {
                    withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.error)) {
                        append("✝️ ")
                    }
                }
                withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.primary)) {
                    append(data.summaryTitleRo)
                }
            }
            Text(text = annotatedTitle, style = MaterialTheme.typography.titleMedium)

            HorizontalDivider()

            Text(stringResource(R.string.calendar_day_details_fasting, correctedFastingDescription), style = MaterialTheme.typography.bodyLarge)

            if (data.saints.isNotEmpty()) {
                Text(stringResource(R.string.calendar_day_details_saints), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                Column {
                    data.saints.forEach { saint ->
                        Text("• ${saint.nameAndDescriptionRo}", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }
    }
}
