package com.company.cavitrack.data.repository

import android.content.Context
import com.company.cavitrack.domain.export.DataSetType
import com.company.cavitrack.domain.export.ExportFilterPreset
import com.company.cavitrack.domain.export.ExportFilterSpec
import com.company.cavitrack.domain.export.ExportFormat
import com.company.cavitrack.domain.export.ExportMetadataRecord
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExportHistoryRepository @Inject constructor(
    @param:ApplicationContext private val context: Context
) {

    private val historyFile = File(context.filesDir, "export_history.json")
    private val presetsFile = File(context.filesDir, "export_presets.json")

    private val _historyRecords = MutableStateFlow<List<ExportMetadataRecord>>(emptyList())
    val historyRecords: Flow<List<ExportMetadataRecord>> = _historyRecords.asStateFlow()

    private val _presets = MutableStateFlow<List<ExportFilterPreset>>(emptyList())
    val presets: Flow<List<ExportFilterPreset>> = _presets.asStateFlow()

    init {
        loadFromDisk()
    }

    private fun loadFromDisk() {
        // 1. Load History Records
        if (historyFile.exists()) {
            try {
                val jsonStr = historyFile.readText(Charsets.UTF_8)
                val jsonArray = JSONArray(jsonStr)
                val records = mutableListOf<ExportMetadataRecord>()
                for (i in 0 until jsonArray.length()) {
                    val obj = jsonArray.getJSONObject(i)
                    val dataSetsArray = obj.getJSONArray("dataSets")
                    val dataSets = mutableListOf<DataSetType>()
                    for (j in 0 until dataSetsArray.length()) {
                        try { dataSets.add(DataSetType.valueOf(dataSetsArray.getString(j))) } catch (_: Exception) {}
                    }
                    records.add(
                        ExportMetadataRecord(
                            id = obj.getString("id"),
                            fileName = obj.getString("fileName"),
                            format = ExportFormat.valueOf(obj.getString("format")),
                            dataSets = dataSets,
                            recordCount = obj.getInt("recordCount"),
                            fileSizeBytes = obj.getLong("fileSizeBytes"),
                            timestamp = obj.getLong("timestamp"),
                            performedBy = obj.optString("performedBy", "User"),
                            fileUri = obj.getString("fileUri")
                        )
                    )
                }
                _historyRecords.value = records
            } catch (e: Exception) {
                _historyRecords.value = emptyList()
            }
        }

        // 2. Load Presets
        if (presetsFile.exists()) {
            try {
                val jsonStr = presetsFile.readText(Charsets.UTF_8)
                val jsonArray = JSONArray(jsonStr)
                val presetsList = mutableListOf<ExportFilterPreset>()
                for (i in 0 until jsonArray.length()) {
                    val obj = jsonArray.getJSONObject(i)
                    val dataSetsArray = obj.getJSONArray("targetDataSets")
                    val targetDataSets = mutableSetOf<DataSetType>()
                    for (j in 0 until dataSetsArray.length()) {
                        try { targetDataSets.add(DataSetType.valueOf(dataSetsArray.getString(j))) } catch (_: Exception) {}
                    }
                    val filterObj = obj.optJSONObject("filterSpec")
                    val filterSpec = ExportFilterSpec(
                        category = filterObj?.optString("category")?.ifBlank { null },
                        status = filterObj?.optString("status")?.ifBlank { null },
                        searchQuery = filterObj?.optString("searchQuery")?.ifBlank { null }
                    )
                    presetsList.add(
                        ExportFilterPreset(
                            id = obj.getString("id"),
                            name = obj.getString("name"),
                            targetDataSets = targetDataSets,
                            filterSpec = filterSpec,
                            createdAt = obj.optLong("createdAt", System.currentTimeMillis())
                        )
                    )
                }
                _presets.value = presetsList
            } catch (e: Exception) {
                _presets.value = getDefaultPresets()
            }
        } else {
            val defaults = getDefaultPresets()
            _presets.value = defaults
            savePresetsToDisk(defaults)
        }
    }

    private fun getDefaultPresets(): List<ExportFilterPreset> {
        return listOf(
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
    }

    fun addRecord(record: ExportMetadataRecord) {
        val updated = listOf(record) + _historyRecords.value.filterNot { it.id == record.id }
        _historyRecords.value = updated
        saveHistoryToDisk(updated)
    }

    fun deleteRecord(id: String) {
        val updated = _historyRecords.value.filterNot { it.id == id }
        _historyRecords.value = updated
        saveHistoryToDisk(updated)
    }

    fun savePreset(preset: ExportFilterPreset) {
        val updated = listOf(preset) + _presets.value.filterNot { it.id == preset.id }
        _presets.value = updated
        savePresetsToDisk(updated)
    }

    fun deletePreset(id: String) {
        val updated = _presets.value.filterNot { it.id == id }
        _presets.value = updated
        savePresetsToDisk(updated)
    }

    private fun saveHistoryToDisk(records: List<ExportMetadataRecord>) {
        try {
            val jsonArray = JSONArray()
            records.forEach { rec ->
                val obj = JSONObject().apply {
                    put("id", rec.id)
                    put("fileName", rec.fileName)
                    put("format", rec.format.name)
                    put("dataSets", JSONArray(rec.dataSets.map { it.name }))
                    put("recordCount", rec.recordCount)
                    put("fileSizeBytes", rec.fileSizeBytes)
                    put("timestamp", rec.timestamp)
                    put("performedBy", rec.performedBy)
                    put("fileUri", rec.fileUri)
                }
                jsonArray.put(obj)
            }
            historyFile.writeText(jsonArray.toString(), Charsets.UTF_8)
        } catch (_: Exception) {}
    }

    private fun savePresetsToDisk(presetsList: List<ExportFilterPreset>) {
        try {
            val jsonArray = JSONArray()
            presetsList.forEach { p ->
                val obj = JSONObject().apply {
                    put("id", p.id)
                    put("name", p.name)
                    put("targetDataSets", JSONArray(p.targetDataSets.map { it.name }))
                    put("createdAt", p.createdAt)
                    val filterObj = JSONObject().apply {
                        put("category", p.filterSpec.category ?: "")
                        put("status", p.filterSpec.status ?: "")
                        put("searchQuery", p.filterSpec.searchQuery ?: "")
                    }
                    put("filterSpec", filterObj)
                }
                jsonArray.put(obj)
            }
            presetsFile.writeText(jsonArray.toString(), Charsets.UTF_8)
        } catch (_: Exception) {}
    }
}
