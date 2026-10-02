package com.company.cavitrack.data.export

import com.company.cavitrack.domain.export.DataSetType
import com.company.cavitrack.domain.model.Component
import com.company.cavitrack.domain.model.Mold
import com.company.cavitrack.domain.model.MoldStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class CsvExportEngineTest {

    private lateinit var csvEngine: CsvExportEngine

    @Before
    fun setUp() {
        csvEngine = CsvExportEngine()
    }

    @Test
    fun generateCsv_includesUtf8BomByteMarker() {
        val bytes = csvEngine.generateCsv(
            dataSets = setOf(DataSetType.Inventory),
            components = listOf(
                Component("c1", "Valve, Steel", "SKU-100", "Valves", 10, "pcs", 5, "owner1", null, 1000L, 1000L)
            )
        )

        val csvText = String(bytes, Charsets.UTF_8)
        assertTrue("CSV output should start with UTF-8 BOM marker", csvText.startsWith("\uFEFF"))
    }

    @Test
    fun generateCsv_escapesCommasAndQuotesInFields() {
        val components = listOf(
            Component("c1", "Special \"Heavy\" Bracket, Type A", "SKU-200", "Hardware", 50, "pcs", 10, "owner1", null, 1000L, 1000L)
        )

        val bytes = csvEngine.generateCsv(
            dataSets = setOf(DataSetType.Inventory),
            components = components
        )

        val csvText = String(bytes, Charsets.UTF_8)
        assertTrue("Should contain escaped double quotes", csvText.contains("\"Special \"\"Heavy\"\" Bracket, Type A\""))
    }

    @Test
    fun sanitizeCsvCell_neutralizesFormulaInjectionTriggers() {
        // CWE-1236 CSV Formula Injection Security Tests
        assertEquals("'=CMD|' /C calc'!A1", csvEngine.sanitizeCsvCell("=CMD|' /C calc'!A1"))
        assertEquals("'+1+2", csvEngine.sanitizeCsvCell("+1+2"))
        assertEquals("'-100", csvEngine.sanitizeCsvCell("-100"))
        assertEquals("'@SUM(A1:A10)", csvEngine.sanitizeCsvCell("@SUM(A1:A10)"))
        assertEquals("Normal Text", csvEngine.sanitizeCsvCell("Normal Text"))
    }

    @Test
    fun generateCsv_includesMoldsSection() {
        val molds = listOf(
            Mold("m1", "MOLD-001", 4, MoldStatus.Active, "Rack A1", "owner1", null, 1000L, 1000L)
        )

        val bytes = csvEngine.generateCsv(
            dataSets = setOf(DataSetType.Molds),
            molds = molds
        )

        val csvText = String(bytes, Charsets.UTF_8)
        assertTrue("CSV should contain Molds section header", csvText.contains("=== MOLDS ==="))
        assertTrue("CSV should contain MOLD-001", csvText.contains("MOLD-001"))
    }
}
