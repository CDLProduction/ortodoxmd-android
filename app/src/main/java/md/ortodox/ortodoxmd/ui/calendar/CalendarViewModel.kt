package md.ortodox.ortodoxmd.ui.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import md.ortodox.ortodoxmd.data.repository.CalendarRepository
import md.ortodox.ortodoxmd.data.model.CalendarData
import android.util.Log
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

    // Use a Calendar instance for initial values, but not as a mutable state holder.
    private val initialCalendar: Calendar = Calendar.getInstance()

    // State for the currently displayed month (0-indexed, e.g., January is 0)
    private val _currentMonth = MutableStateFlow(initialCalendar.get(Calendar.MONTH))
    val currentMonth: StateFlow<Int> = _currentMonth

    // State for the currently displayed year
    private val _currentYear = MutableStateFlow(initialCalendar.get(Calendar.YEAR))
    val currentYear: StateFlow<Int> = _currentYear

    // State for the currently selected date string ("yyyy-MM-dd")
    private val _selectedDate = MutableStateFlow(dateFormat.format(initialCalendar.time))
    val selectedDate: StateFlow<String> = _selectedDate

    init {
        // Fetch data for the initial date on load
        fetchCalendarData(_selectedDate.value)
        // Pre-fetch data for the current month
        preFetchMonth(_currentMonth.value, _currentYear.value)
    }

    fun fetchCalendarData(date: String) {
        viewModelScope.launch {
            _calendarData.value = repository.getCalendarData(date)
            _errorMessage.value = null
        }
    }

    /**
     * Updates the selected date when a day is clicked in the calendar grid.
     */
    fun selectDate(date: String) {
        _selectedDate.value = date
        fetchCalendarData(date)
    }

    /**
     * Updates the calendar's month and year from the DatePicker.
     */
    fun updateFromPicker(millis: Long) {
        val pickerCalendar = Calendar.getInstance()
        // DatePicker provides UTC millis. Adjust for the local timezone to prevent date errors.
        val timezoneOffset = pickerCalendar.timeZone.getOffset(millis)
        pickerCalendar.timeInMillis = millis + timezoneOffset

        _currentMonth.value = pickerCalendar.get(Calendar.MONTH)
        _currentYear.value = pickerCalendar.get(Calendar.YEAR)

        // Also update the selected date to the first of the new month
        val newSelectedDate = dateFormat.format(pickerCalendar.time)
        _selectedDate.value = newSelectedDate

        preFetchMonth(_currentMonth.value, _currentYear.value)
        fetchCalendarData(newSelectedDate)
    }

    /**
     * Pre-fetches all data for a given month to cache it.
     * @param month The month to pre-fetch (0-indexed).
     * @param year The year to pre-fetch.
     */
    fun preFetchMonth(month: Int, year: Int) {
        viewModelScope.launch {
            val tempCalendar = Calendar.getInstance().apply {
                set(Calendar.YEAR, year)
                set(Calendar.MONTH, month)
            }
            val maxDay = tempCalendar.getActualMaximum(Calendar.DAY_OF_MONTH)
            val monthForFormat = month + 1 // Format requires 1-indexed month
            for (day in 1..maxDay) {
                val dateStr = String.format("%04d-%02d-%02d", year, monthForFormat, day)
                repository.getCalendarData(dateStr) // Pre-fetch and cache
            }
        }
    }

    /**
     * Calculates the number of days in a given month and year.
     * @param year The year.
     * @param month The month (0-indexed).
     * @return The number of days in the month.
     */
    fun getDaysInMonth(year: Int, month: Int): Int {
        val calendar = Calendar.getInstance()
        calendar.set(year, month, 1)
        return calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
    }

    /**
     * Calculates which day of the week the first day of a month falls on.
     * @param year The year.
     * @param month The month (0-indexed).
     * @return The day of the week (0 for Sunday, 1 for Monday, ..., 6 for Saturday).
     */
    fun getFirstDayOfWeek(year: Int, month: Int): Int {
        val calendar = Calendar.getInstance()
        calendar.set(year, month, 1)
        // Calendar.DAY_OF_WEEK is 1 for Sunday, 2 for Monday, etc.
        // We subtract 1 to align with a 0-indexed grid (Sunday = 0).
        return calendar.get(Calendar.DAY_OF_WEEK) - 1
    }
}