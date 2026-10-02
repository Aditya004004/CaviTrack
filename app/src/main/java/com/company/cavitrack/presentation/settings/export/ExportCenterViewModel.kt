package com.company.cavitrack.presentation.settings.export

import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.company.cavitrack.data.export.ExportFileManager
import com.company.cavitrack.data.export.StreamingDataFetcher
import com.company.cavitrack.data.repository.ExportHistoryRepository
import com.company.cavitrack.domain.export.DataSetType
import com.company.cavitrack.domain.export.ExportEngineManager
import com.company.cavitrack.domain.export.ExportFilterPreset
import com.company.cavitrack.domain.export.ExportFilterSpec
import com.company.cavitrack.domain.export.ExportFormat
import com.company.cavitrack.domain.export.ExportMetadataRecord
import com.company.cavitrack.domain.export.ExportProgressState
import com.company.cavitrack.domain.export.PreFlightExportSummary
import com.company.cavitrack.domain.export.ScheduledExportConfig
import com.company.cavitrack.domain.export.ScheduledFrequency
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class ExportCenterUiState(
    val selectedTabIndex: Int = 0,
    val selectedDataSets: Set<DataSetType> = setOf(DataSetType.Inventory, DataSetType.Molds),
    val selectedFormat: ExportFormat = ExportFormat.PDF,
    val filterSpec: ExportFilterSpec = ExportFilterSpec(),
    val preFlightSummary: PreFlightExportSummary = PreFlightExportSummary(),
    val progressState: ExportProgressState = ExportProgressState.Idle,
    val isPreviewModalOpen: Boolean = false,
    val includeCharts: Boolean = true
)

@HiltViewModel
class ExportCenterViewModel @Inject constructor(
    private val fetcher: StreamingDataFetcher,
    private val exportEngineManager: ExportEngineManager,
    private val historyRepository: ExportHistoryRepository,
    private val fileManager: ExportFileManager,
    @param:ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(ExportCenterUiState())
    val uiState: StateFlow<ExportCenterUiState> = _uiState.asStateFlow()

    val historyRecords: StateFlow<List<ExportMetadataRecord>> = historyRepository.historyRecords
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val savedPresets: StateFlow<List<ExportFilterPreset>> = historyRepository.presets
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        recalculatePreFlight()
    }

    fun onTabSelected(index: Int) {
        _uiState.update { it.copy(selectedTabIndex = index) }
    }

    fun toggleDataSet(dataSet: DataSetType) {
        _uiState.update { current ->
            val updated = current.selectedDataSets.toMutableSet()
            if (updated.contains(dataSet)) {
                if (updated.size > 1) updated.remove(dataSet)
            } else {
                updated.add(dataSet)
            }
            current.copy(selectedDataSets = updated)
        }
        recalculatePreFlight()
    }

    fun selectFormat(format: ExportFormat) {
        _uiState.update { it.copy(selectedFormat = format) }
    }

    fun updateFilterSpec(filterSpec: ExportFilterSpec) {
        _uiState.update { it.copy(filterSpec = filterSpec) }
        recalculatePreFlight()
    }

    fun toggleIncludeCharts(include: Boolean) {
        _uiState.update { it.copy(includeCharts = include) }
    }

    fun applyPreset(preset: ExportFilterPreset) {
        _uiState.update { current ->
            current.copy(
                selectedDataSets = preset.targetDataSets,
                filterSpec = preset.filterSpec
            )
        }
        recalculatePreFlight()
    }

    fun saveCurrentPreset(name: String) {
        if (name.isBlank()) return
        val preset = ExportFilterPreset(
            id = UUID.randomUUID().toString(),
            name = name.trim(),
            targetDataSets = _uiState.value.selectedDataSets,
            filterSpec = _uiState.value.filterSpec
        )
        historyRepository.savePreset(preset)
    }

    fun setPreviewModalOpen(open: Boolean) {
        _uiState.update { it.copy(isPreviewModalOpen = open) }
    }

    fun recalculatePreFlight() {
        viewModelScope.launch {
            val state = _uiState.value
            val summary = fetcher.getPreFlightSummary(state.selectedDataSets, state.filterSpec)
            _uiState.update { it.copy(preFlightSummary = summary) }
        }
    }

    fun executeAdvancedExport(customFileName: String? = null) {
        val state = _uiState.value
        val name = customFileName?.ifBlank { null } ?: "Export_${state.selectedFormat.name}_${System.currentTimeMillis()}"

        viewModelScope.launch {
            exportEngineManager.executeExport(
                fileName = name,
                dataSets = state.selectedDataSets,
                format = state.selectedFormat,
                filterSpec = state.filterSpec,
                includeCharts = state.includeCharts
            ).collect { progress ->
                _uiState.update { it.copy(progressState = progress, isPreviewModalOpen = false) }
            }
        }
    }

    fun executeQuickExport(dataSet: DataSetType, format: ExportFormat) {
        val name = "Quick_${dataSet.name}_${System.currentTimeMillis()}"
        viewModelScope.launch {
            exportEngineManager.executeExport(
                fileName = name,
                dataSets = setOf(dataSet),
                format = format,
                filterSpec = ExportFilterSpec()
            ).collect { progress ->
                _uiState.update { it.copy(progressState = progress) }
            }
        }
    }

    fun shareRecord(record: ExportMetadataRecord) {
        try {
            val uri = android.net.Uri.parse(record.fileUri)
            val shareIntent = fileManager.createShareIntent(uri, record.format.mimeType, "Share Export File")
            val chooser = Intent.createChooser(shareIntent, "Share Export File via").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(chooser)
        } catch (e: Exception) {
            _uiState.update { it.copy(progressState = ExportProgressState.Error("Unable to share file: ${e.localizedMessage}")) }
        }
    }

    fun deleteHistoryRecord(id: String) {
        historyRepository.deleteRecord(id)
    }

    fun dismissProgressState() {
        _uiState.update { it.copy(progressState = ExportProgressState.Idle) }
    }
}
