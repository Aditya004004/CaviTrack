package com.company.cavitrack.data.repository

import com.company.cavitrack.domain.export.DataSetType
import com.company.cavitrack.domain.export.ExportFilterPreset
import com.company.cavitrack.domain.export.ExportFilterSpec
import com.company.cavitrack.domain.export.ExportFormat
import com.company.cavitrack.domain.export.ExportMetadataRecord
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExportHistoryRepository @Inject constructor() {

    private val _historyRecords = MutableStateFlow<List<ExportMetadataRecord>>(emptyList())
    val historyRecords: Flow<List<ExportMetadataRecord>> = _historyRecords.asStateFlow()

    private val _presets = MutableStateFlow<List<ExportFilterPreset>>(
        listOf(
            ExportFilterPreset(
                id = "preset_default_inv",
                name = "Full Inventory Audit",
                targetDataSets = setOf(DataSetType.Inventory),
                filterSpec = ExportFilterSpec()
            ),
            ExportFilterPreset(
                id = "preset_low_stock",
                name = "Critical Low Stock Alerts",
                targetDataSets = setOf(DataSetType.Alerts, DataSetType.Inventory),
                filterSpec = ExportFilterSpec()
            ),
            ExportFilterPreset(
                id = "preset_molds_status",
                name = "Mold Fleet Status",
                targetDataSets = setOf(DataSetType.Molds),
                filterSpec = ExportFilterSpec()
            )
        )
    )
    val presets: Flow<List<ExportFilterPreset>> = _presets.asStateFlow()

    fun addRecord(record: ExportMetadataRecord) {
        _historyRecords.update { current ->
            listOf(record) + current.filterNot { it.id == record.id }
        }
    }

    fun deleteRecord(id: String) {
        _historyRecords.update { current ->
            current.filterNot { it.id == id }
        }
    }

    fun savePreset(preset: ExportFilterPreset) {
        _presets.update { current ->
            listOf(preset) + current.filterNot { it.id == preset.id }
        }
    }

    fun deletePreset(id: String) {
        _presets.update { current ->
            current.filterNot { it.id == id }
        }
    }
}
