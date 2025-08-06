package md.ortodox.ortodoxmd.ui.bible

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import md.ortodox.ortodoxmd.data.repository.BibleRepository
import javax.inject.Inject

data class BibleHomeUiState(
    // null = se verifică, true = descărcată, false = nedescărcată
    val isBibleDownloaded: Boolean? = null
)

@HiltViewModel
class BibleHomeViewModel @Inject constructor(
    private val repository: BibleRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(BibleHomeUiState())
    val uiState = _uiState.asStateFlow()

    init {
        checkBibleStatus()
    }

    fun checkBibleStatus() {
        viewModelScope.launch {
            _uiState.update { it.copy(isBibleDownloaded = null) } // Afișează starea de încărcare
            val isDownloaded = repository.isBibleDownloaded()
            _uiState.update { it.copy(isBibleDownloaded = isDownloaded) }
        }
    }
}