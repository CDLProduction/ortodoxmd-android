package md.ortodox.ortodoxmd.ui.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import md.ortodox.ortodoxmd.data.repository.CalendarRepository
import md.ortodox.ortodoxmd.data.model.CalendarData
import javax.inject.Inject
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@HiltViewModel
class CalendarViewModel @Inject constructor(
    private val repository: CalendarRepository
) : ViewModel() {
    private val _calendarData = MutableStateFlow<CalendarData?>(null)
    val calendarData: StateFlow<CalendarData?> = _calendarData
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val initialCalendar: Calendar = Calendar.getInstance()
    private val _currentMonth = MutableStateFlow(initialCalendar.get(Calendar.MONTH))
    val currentMonth: StateFlow<Int> = _currentMonth
    private val _currentYear = MutableStateFlow(initialCalendar.get(Calendar.YEAR))
    val currentYear: StateFlow<Int> = _currentYear
    private val _selectedDate = MutableStateFlow(dateFormat.format(initialCalendar.time))
    val selectedDate: StateFlow<String> = _selectedDate
    init {
        fetchCalendarData(_selectedDate.value)
        preFetchMonth(_currentMonth.value, _currentYear.value)
    }
    fun fetchCalendarData(date: String) {
        viewModelScope.launch {
            _calendarData.value = repository.getCalendarData(date)
            _errorMessage.value = null
        }
    }
    fun selectDate(date: String) {
        _selectedDate.value = date
        fetchCalendarData(date)
    }
    fun goToToday() {
        val todayCalendar = Calendar.getInstance()
        _currentMonth.value = todayCalendar.get(Calendar.MONTH)
        _currentYear.value = todayCalendar.get(Calendar.YEAR)
        val todayDateStr = dateFormat.format(todayCalendar.time)
        _selectedDate.value = todayDateStr
        fetchCalendarData(todayDateStr)
    }
    fun updateFromPicker(millis: Long) {
        val pickerCalendar = Calendar.getInstance()
        val timezoneOffset = pickerCalendar.timeZone.getOffset(millis)
        pickerCalendar.timeInMillis = millis + timezoneOffset
        _currentMonth.value = pickerCalendar.get(Calendar.MONTH)
        _currentYear.value = pickerCalendar.get(Calendar.YEAR)
        val newSelectedDate = dateFormat.format(pickerCalendar.time)
        _selectedDate.value = newSelectedDate
        preFetchMonth(_currentMonth.value, _currentYear.value)
        fetchCalendarData(newSelectedDate)
    }
    // --- NOU: Funcții pentru navigarea prin swipe ---
    fun goToNextMonth() {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.YEAR, _currentYear.value)
            set(Calendar.MONTH, _currentMonth.value)
        }
        calendar.add(Calendar.MONTH, 1)
        _currentMonth.value = calendar.get(Calendar.MONTH)
        _currentYear.value = calendar.get(Calendar.YEAR)
        preFetchMonth(_currentMonth.value, _currentYear.value)
    }
    fun goToPreviousMonth() {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.YEAR, _currentYear.value)
            set(Calendar.MONTH, _currentMonth.value)
        }
        calendar.add(Calendar.MONTH, -1)
        _currentMonth.value = calendar.get(Calendar.MONTH)
        _currentYear.value = calendar.get(Calendar.YEAR)
        preFetchMonth(_currentMonth.value, _currentYear.value)
    }
    // ------------------------------------------------
    fun preFetchMonth(month: Int, year: Int) {
        viewModelScope.launch {
            val tempCalendar = Calendar.getInstance().apply {
                set(Calendar.YEAR, year)
                set(Calendar.MONTH, month)
            }
            val maxDay = tempCalendar.getActualMaximum(Calendar.DAY_OF_MONTH)
            val monthForFormat = month + 1
            for (day in 1..maxDay) {
                val dateStr = String.format("%04d-%02d-%02d", year, monthForFormat, day)
                repository.getCalendarData(dateStr)
            }
        }
    }
    fun getDaysInMonth(year: Int, month: Int): Int {
        val calendar = Calendar.getInstance()
        calendar.set(year, month, 1)
        return calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
    }
    fun getFirstDayOfWeek(year: Int, month: Int): Int {
        val calendar = Calendar.getInstance()
        calendar.set(year, month, 1)
        return calendar.get(Calendar.DAY_OF_WEEK) - 1
    }
}