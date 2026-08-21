package com.company.cavitrack.presentation.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.company.cavitrack.domain.model.HistoryLog
import com.company.cavitrack.domain.repository.InventoryRepository
import com.company.cavitrack.presentation.components.UiState
import com.company.cavitrack.util.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val repository: InventoryRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<List<HistoryLog>>>(UiState.Loading)
    val uiState: StateFlow<UiState<List<HistoryLog>>> = _uiState.asStateFlow()
    
    private var loadJob: Job? = null

    init {
        loadData()
    }

    fun loadData() {
        loadJob?.cancel()
        _uiState.value = UiState.Loading
        loadJob = viewModelScope.launch {
            repository.getHistory().collect { result ->
                when (result) {
                    is Result.Success -> _uiState.value = UiState.Success(result.data)
                    is Result.Error -> _uiState.value = UiState.Error(result.message ?: "Unknown Error")
                }
            }
        }
    }
}
