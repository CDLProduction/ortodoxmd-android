package md.ortodox.ortodoxmd.ui.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import md.ortodox.ortodoxmd.data.model.CalendarData
import md.ortodox.ortodoxmd.data.repository.CalendarRepository
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

data class CalendarUiState(
    val selectedDate: Calendar = Calendar.getInstance(),
    val dataForSelectedDay: CalendarData? = null,
    val dataForVisibleMonth: Map<String, CalendarData> = emptyMap(),
    val isLoading: Boolean = true
)

@HiltViewModel
class CalendarViewModel @Inject constructor(
    private val repository: CalendarRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CalendarUiState())
    val uiState: StateFlow<CalendarUiState> = _uiState.asStateFlow()

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    init {
        selectDate(Calendar.getInstance())
    }

    fun selectDate(calendar: Calendar) {
        viewModelScope.launch {
            _uiState.update { it.copy(selectedDate = calendar, isLoading = true) }
            loadDataForMonth(calendar)
        }
    }

    private suspend fun loadDataForMonth(calendar: Calendar) {
        val monthCalendar = calendar.clone() as Calendar
        monthCalendar.set(Calendar.DAY_OF_MONTH, 1)
        val daysInMonth = monthCalendar.getActualMaximum(Calendar.DAY_OF_MONTH)
        val monthData = mutableMapOf<String, CalendarData>()

        for (i in 1..daysInMonth) {
            monthCalendar.set(Calendar.DAY_OF_MONTH, i)
            val dateStr = dateFormat.format(monthCalendar.time)
            repository.getCalendarData(dateStr)?.let { data ->
                monthData[dateStr] = data
            }
        }

        val selectedDateStr = dateFormat.format(calendar.time)
        val selectedDayData = monthData[selectedDateStr]

        _uiState.update {
            it.copy(
                dataForVisibleMonth = monthData,
                dataForSelectedDay = selectedDayData,
                isLoading = false
            )
        }
    }

    fun goToToday() = selectDate(Calendar.getInstance())
    fun goToNextMonth() {
        val newDate = (_uiState.value.selectedDate.clone() as Calendar).apply { add(Calendar.MONTH, 1) }
        selectDate(newDate)
    }
    fun goToPreviousMonth() {
        val newDate = (_uiState.value.selectedDate.clone() as Calendar).apply { add(Calendar.MONTH, -1) }
        selectDate(newDate)
    }
    fun updateFromPicker(millis: Long) {
        val pickerCalendar = Calendar.getInstance().apply { timeInMillis = millis }
        selectDate(pickerCalendar)
    }
}