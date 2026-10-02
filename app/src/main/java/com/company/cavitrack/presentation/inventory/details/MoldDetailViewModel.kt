package com.company.cavitrack.presentation.inventory.details

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.company.cavitrack.domain.model.Mold
import com.company.cavitrack.domain.repository.InventoryRepository
import com.company.cavitrack.presentation.components.UiState
import com.company.cavitrack.presentation.navigation.Route
import com.company.cavitrack.util.DataResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MoldDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: InventoryRepository
) : ViewModel() {

    private val route: Route.MoldDetail = savedStateHandle.toRoute()
    val entityId: String = route.id

    private val _uiState = MutableStateFlow<UiState<Mold>>(UiState.Loading)
    val uiState: StateFlow<UiState<Mold>> = _uiState.asStateFlow()

    private val _isDeleted = Channel<Unit>(Channel.BUFFERED)
    val isDeleted = _isDeleted.receiveAsFlow()

    fun loadMold(isRefresh: Boolean = false) {
        viewModelScope.launch {
            if (!isRefresh && _uiState.value !is UiState.Success) {
                _uiState.value = UiState.Loading
            }
            try {
                when (val result = repository.getMold(entityId)) {
                    is DataResult.Success -> _uiState.value = UiState.Success(result.data)
                    is DataResult.Error -> {
                        if (_uiState.value !is UiState.Success) {
                            _uiState.value = UiState.Error(result.message)
                        }
                    }
                }
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                if (_uiState.value !is UiState.Success) {
                    _uiState.value = UiState.Error(e.message ?: "Failed to load mold")
                }
            }
        }
    }

    fun retry() {
        loadMold()
    }

    fun deleteMold() {
        viewModelScope.launch {
            try {
                val result = repository.deleteMold(entityId)
                if (result is DataResult.Success) {
                    _isDeleted.send(Unit)
                } else if (result is DataResult.Error) {
                    _uiState.value = UiState.Error(result.message)
                }
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                _uiState.value = UiState.Error(e.message ?: "Failed to delete mold")
            }
        }
    }
}
