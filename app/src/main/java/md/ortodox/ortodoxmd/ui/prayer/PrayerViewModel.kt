package md.ortodox.ortodoxmd.ui.prayer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import md.ortodox.ortodoxmd.data.repository.PrayerRepository
import md.ortodox.ortodoxmd.data.model.Prayer
import javax.inject.Inject

@HiltViewModel
class PrayerViewModel @Inject constructor(
    private val repository: PrayerRepository
) : ViewModel() {
    private val _prayers = MutableStateFlow<List<Prayer>?>(null)
    val prayers: StateFlow<List<Prayer>?> = _prayers

    fun fetchPrayers(category: String) {
        viewModelScope.launch {
            val data = repository.getPrayersByCategory(category)
            _prayers.value = data
        }
    }
}