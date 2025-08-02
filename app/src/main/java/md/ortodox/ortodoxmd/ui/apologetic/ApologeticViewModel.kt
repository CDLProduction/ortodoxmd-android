package md.ortodox.ortodoxmd.ui.apologetics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import md.ortodox.ortodoxmd.data.model.Apologetic
import md.ortodox.ortodoxmd.data.repository.ApologeticRepository
import javax.inject.Inject

data class ApologeticUiState(
    val isLoading: Boolean = true,
    val searchQuery: String = "",
    val apologetics: List<Apologetic> = emptyList()
)

@HiltViewModel
class ApologeticViewModel @Inject constructor(
    private val repository: ApologeticRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    private val _allApologetics = repository.getApologetics()

    val uiState: StateFlow<ApologeticUiState> = combine(
        _allApologetics,
        _searchQuery
    ) { allItems, query ->
        val filteredList = if (query.isBlank()) {
            allItems
        } else {
            allItems.filter {
                it.questionRo.contains(query, ignoreCase = true) ||
                        it.answerRo?.contains(query, ignoreCase = true) == true
            }
        }
        ApologeticUiState(
            isLoading = false,
            searchQuery = query,
            apologetics = filteredList
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ApologeticUiState()
    )

    init {
        viewModelScope.launch {
            repository.syncApologetics()
        }
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }
}