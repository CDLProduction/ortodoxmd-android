package md.ortodox.ortodoxmd.ui.bible

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import md.ortodox.ortodoxmd.data.repository.BibleRepository
import javax.inject.Inject

sealed interface DownloadState {
    object Idle : DownloadState
    data class Downloading(val progress: Float) : DownloadState
    object Success : DownloadState
    data class Error(val message: String) : DownloadState
}

@HiltViewModel
class BibleDownloadViewModel @Inject constructor(
    private val repository: BibleRepository
) : ViewModel() {

    private val _downloadState = MutableStateFlow<DownloadState>(DownloadState.Idle)
    val downloadState = _downloadState.asStateFlow()

    init {
        checkDownloadStatus()
    }

    private fun checkDownloadStatus() {
        viewModelScope.launch {
            if (repository.isBibleDownloaded()) {
                _downloadState.value = DownloadState.Success
            } else {
                _downloadState.value = DownloadState.Idle
            }
        }
    }

    fun startDownload() {
        viewModelScope.launch {
            repository.downloadAndCacheEntireBible()
                .onStart { _downloadState.value = DownloadState.Downloading(0f) }
                .onCompletion {
                    // Verifică starea finală, deoarece onCompletion rulează și la eroare
                    if (_downloadState.value !is DownloadState.Error) {
                        _downloadState.value = DownloadState.Success
                    }
                }
                .catch { e ->
                    _downloadState.value = DownloadState.Error(e.localizedMessage ?: "A apărut o eroare.")
                }
                .collect { progress ->
                    _downloadState.value = DownloadState.Downloading(progress)
                }
        }
    }
}
