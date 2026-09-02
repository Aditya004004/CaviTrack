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

import androidx.paging.PagingData
import androidx.paging.cachedIn
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.combine

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val useCases: InventoryUseCases,
    private val sessionManager: com.company.cavitrack.util.SessionManager
) : ViewModel() {

    private val _selectedAction = MutableStateFlow<String?>(null)
    val selectedAction = _selectedAction.asStateFlow()

    fun setActionFilter(action: String?) {
        _selectedAction.value = action
    }

    val pagedHistoryLogs: Flow<PagingData<HistoryLog>> = combine(
        sessionManager.currentUser,
        _selectedAction
    ) { user, action ->
        Pair(user, action)
    }.flatMapLatest { (user, action) ->
        if (user != null) {
            useCases.getHistory(action).cachedIn(viewModelScope)
        } else {
            emptyFlow()
        }
    }
}




