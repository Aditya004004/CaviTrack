package com.company.cavitrack.data.export

import org.junit.Assert.assertEquals
import org.junit.Test

class ExportFileManagerTest {

    @Test
    fun sanitizeFileName_stripsPathTraversalSequences() {
        // CWE-22 Path Traversal Security Tests
        assertEquals("Export_Q3_Report", ExportFileManager.sanitizeFileName("../../Export_Q3_Report"))
        assertEquals("Audit_Logs.pdf", ExportFileManager.sanitizeFileName("../../../Audit_Logs.pdf"))
        assertEquals("C_Windows_System32_Sensitive_File", ExportFileManager.sanitizeFileName("C:\\Windows\\System32\\Sensitive_File"))
        assertEquals("Normal_Report_2026", ExportFileManager.sanitizeFileName("Normal Report 2026"))
    }
}
