package md.ortodox.ortodoxmd.ui.prayer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import md.ortodox.ortodoxmd.data.repository.PrayerRepository
import md.ortodox.ortodoxmd.data.model.Prayer
import javax.inject.Inject
import android.util.Log

// Sealed class to represent the different states of the UI
sealed class PrayerUiState {
    object Loading : PrayerUiState()
    data class Success(val prayers: List<Prayer>) : PrayerUiState()
    data class Error(val message: String) : PrayerUiState()
    object Empty : PrayerUiState()
}

@HiltViewModel
class PrayerViewModel @Inject constructor(
    private val repository: PrayerRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<PrayerUiState>(PrayerUiState.Loading)
    val uiState: StateFlow<PrayerUiState> = _uiState.asStateFlow()

    fun fetchPrayers(category: String) {
        viewModelScope.launch {
            Log.d("PrayerViewModel", "Fetching prayers for category: $category")
            _uiState.value = PrayerUiState.Loading // Set loading state immediately
            try {
                val data = repository.getPrayersByCategory(category)
                if (data.isNullOrEmpty()) {
                    _uiState.value = PrayerUiState.Empty
                } else {
                    _uiState.value = PrayerUiState.Success(data)
                }
            } catch (e: Exception) {
                // In a real app, use a more user-friendly error message
                _uiState.value = PrayerUiState.Error(e.message ?: "An unknown error occurred")
                Log.e("PrayerViewModel", "Error fetching prayers", e)
            }
        }
    }
}