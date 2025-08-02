package md.ortodox.ortodoxmd.ui.monastery

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import md.ortodox.ortodoxmd.data.model.Monastery
import md.ortodox.ortodoxmd.data.repository.MonasteryRepository
import javax.inject.Inject

@HiltViewModel
class MonasteryDetailViewModel @Inject constructor(
    repository: MonasteryRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    // Preluăm în siguranță ID-ul pasat prin navigație
    private val monasteryId: Long = checkNotNull(savedStateHandle["monasteryId"])

    // Expunem direct un Flow cu mănăstirea cerută, care se va actualiza automat
    val monastery: StateFlow<Monastery?> = repository.getMonasteryById(monasteryId)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )
}