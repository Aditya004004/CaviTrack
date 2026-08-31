package com.company.cavitrack.presentation.history


import kotlinx.coroutines.flow.MutableStateFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.company.cavitrack.domain.model.HistoryLog
import com.company.cavitrack.domain.usecase.inventory.InventoryUseCases
import com.company.cavitrack.presentation.components.UiState
import com.company.cavitrack.util.DataResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val useCases: InventoryUseCases,
    sessionManager: com.company.cavitrack.util.SessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<List<HistoryLog>>>(UiState.Loading)
    val uiState: StateFlow<UiState<List<HistoryLog>>> = _uiState.asStateFlow()
    
    private var loadJob: Job? = null

    init {
        viewModelScope.launch {
            sessionManager.currentUser.collect { user ->
                if (user != null) {
                    loadData()
                } else {
                    loadJob?.cancel()
                    _uiState.value = UiState.Loading
                }
            }
        }
    }

    fun loadData() {
        loadJob?.cancel()
        _uiState.value = UiState.Loading
        loadJob = viewModelScope.launch {
            useCases.getHistory().collect { result ->
                when (result) {
                    is DataResult.Success -> _uiState.value = UiState.Success(result.data)
                    is DataResult.Error -> _uiState.value = UiState.Error(result.message)
                }
            }
        }
    }
}




