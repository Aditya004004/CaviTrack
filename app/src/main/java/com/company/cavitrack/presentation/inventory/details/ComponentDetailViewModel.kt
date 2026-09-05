package com.company.cavitrack.presentation.inventory.details

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.company.cavitrack.domain.model.Component
import com.company.cavitrack.domain.usecase.inventory.InventoryUseCases
import com.company.cavitrack.presentation.components.UiState
import com.company.cavitrack.presentation.navigation.Route
import com.company.cavitrack.util.DataResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ComponentDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val useCases: InventoryUseCases
) : ViewModel() {

    private val route: Route.ComponentDetail = savedStateHandle.toRoute()
    val entityId: String = route.id

    private val _uiState = MutableStateFlow<UiState<Component>>(UiState.Loading)
    val uiState: StateFlow<UiState<Component>> = _uiState.asStateFlow()

    init {
        loadComponent()
    }

    fun loadComponent() {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            try {
                when (val result = useCases.getComponent(entityId)) {
                    is DataResult.Success -> _uiState.value = UiState.Success(result.data)
                    is DataResult.Error -> _uiState.value = UiState.Error(result.message)
                }
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                _uiState.value = UiState.Error(e.message ?: "Failed to load component")
            }
        }
    }

    fun retry() {
        loadComponent()
    }
}
