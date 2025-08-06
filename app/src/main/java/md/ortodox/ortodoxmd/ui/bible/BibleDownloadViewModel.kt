package md.ortodox.ortodoxmd.ui.bible

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import md.ortodox.ortodoxmd.data.repository.BibleRepository
import javax.inject.Inject

sealed interface DownloadState {
    object Idle : DownloadState
    data class Downloading(val progress: Float) : DownloadState
    data class Finished(val message: String) : DownloadState
    data class Error(val message: String) : DownloadState
}

@HiltViewModel
class BibleDownloadViewModel @Inject constructor(
    private val repository: BibleRepository
) : ViewModel() {

    private val _downloadState = MutableStateFlow<DownloadState>(DownloadState.Idle)
    val downloadState = _downloadState.asStateFlow()

    fun startDownload() {
        viewModelScope.launch {
            repository.downloadAndCacheEntireBible()
                .onStart { _downloadState.value = DownloadState.Downloading(0f) }
                .catch { e -> _downloadState.value = DownloadState.Error("A apărut o eroare: ${e.message}") }
                .collect { progress ->
                    _downloadState.value = DownloadState.Downloading(progress)
                    if (progress >= 1.0f) {
                        _downloadState.value = DownloadState.Finished("Descărcare finalizată!")
                    }
                }
        }
    }
}