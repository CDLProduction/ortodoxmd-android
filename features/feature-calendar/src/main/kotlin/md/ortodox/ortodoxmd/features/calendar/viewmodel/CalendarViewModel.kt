package md.ortodox.ortodoxmd.features.calendar.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import md.ortodox.ortodoxmd.data.local.CalendarEntity
import md.ortodox.ortodoxmd.data.repository.CalendarRepository
import javax.inject.Inject

@HiltViewModel
class CalendarViewModel @Inject constructor(private val repository: CalendarRepository) : ViewModel() {
    private val _calendarData = MutableStateFlow<CalendarEntity?>(null)
    val calendarData = _calendarData.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()

    fun loadCalendar(date: String, lang: String = "en") {
        viewModelScope.launch {
            try {
                _calendarData.value = repository.getCalendar(date, lang)
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }
}