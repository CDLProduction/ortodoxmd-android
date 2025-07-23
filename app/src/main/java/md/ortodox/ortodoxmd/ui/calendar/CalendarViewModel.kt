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

    init {
        fetchCalendarData()
    }

    private fun fetchCalendarData() {
        viewModelScope.launch {
            val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            try {
                val data = repository.getCalendarData(currentDate)
                _calendarData.value = data
                _errorMessage.value = null
            } catch (e: Exception) {
                Log.e("CalendarViewModel", "Error fetching data: ${e.message}", e)
                _errorMessage.value = "Failed to load data: ${e.message}"
            }
        }
    }
}