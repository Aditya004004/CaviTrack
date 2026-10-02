package com.company.cavitrack.data.export

import com.company.cavitrack.domain.export.DataSetType
import com.company.cavitrack.domain.model.Component
import com.company.cavitrack.domain.model.Customer
import com.company.cavitrack.domain.model.HistoryLog
import com.company.cavitrack.domain.model.Mold
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExcelExportEngine @Inject constructor() {

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US)

    fun generateExcel(
        dataSets: Set<DataSetType>,
        components: List<Component> = emptyList(),
        customers: List<Customer> = emptyList(),
        molds: List<Mold> = emptyList(),
        historyLogs: List<HistoryLog> = emptyList()
    ): ByteArray {
        val baos = ByteArrayOutputStream()
        ZipOutputStream(baos).use { zos ->
            // 1. [Content_Types].xml
            val sheetsCount = calculateSheetsCount(dataSets)
            writeZipEntry(zos, "[Content_Types].xml", buildContentTypesXml(sheetsCount))
            
            // 2. _rels/.rels
            writeZipEntry(zos, "_rels/.rels", buildRelsXml())

            // 3. xl/workbook.xml & xl/_rels/workbook.xml.rels
            writeZipEntry(zos, "xl/workbook.xml", buildWorkbookXml(dataSets))
            writeZipEntry(zos, "xl/_rels/workbook.xml.rels", buildWorkbookRelsXml(sheetsCount))

            // 4. xl/styles.xml
            writeZipEntry(zos, "xl/styles.xml", buildStylesXml())

            // 5. Worksheets
            var currentSheetIndex = 1
            if (dataSets.contains(DataSetType.Inventory) || dataSets.contains(DataSetType.Alerts)) {
                writeZipEntry(zos, "xl/worksheets/sheet$currentSheetIndex.xml", buildInventorySheetXml(components))
                currentSheetIndex++
            }
            if (dataSets.contains(DataSetType.Customers)) {
                writeZipEntry(zos, "xl/worksheets/sheet$currentSheetIndex.xml", buildCustomersSheetXml(customers))
                currentSheetIndex++
            }
            if (dataSets.contains(DataSetType.Molds)) {
                writeZipEntry(zos, "xl/worksheets/sheet$currentSheetIndex.xml", buildMoldsSheetXml(molds))
                currentSheetIndex++
            }
            if (dataSets.contains(DataSetType.HistoryLogs)) {
                writeZipEntry(zos, "xl/worksheets/sheet$currentSheetIndex.xml", buildHistorySheetXml(historyLogs))
                currentSheetIndex++
            }
        }
        return baos.toByteArray()
    }

    private fun calculateSheetsCount(dataSets: Set<DataSetType>): Int {
        var count = 0
        if (dataSets.contains(DataSetType.Inventory) || dataSets.contains(DataSetType.Alerts)) count++
        if (dataSets.contains(DataSetType.Customers)) count++
        if (dataSets.contains(DataSetType.Molds)) count++
        if (dataSets.contains(DataSetType.HistoryLogs)) count++
        return if (count == 0) 1 else count
    }

    private fun writeZipEntry(zos: ZipOutputStream, name: String, content: String) {
        zos.putNextEntry(ZipEntry(name))
        zos.write(content.toByteArray(Charsets.UTF_8))
        zos.closeEntry()
    }

    private fun buildContentTypesXml(sheetsCount: Int): String {
        val sb = StringBuilder("""<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Types xmlns="http://schemas.openxmlformats.org/package/2006/content-types">
<Default Extension="rels" ContentType="application/vnd.openxmlformats-package.relationships+xml"/>
<Default Extension="xml" ContentType="application/xml"/>
<Override PartName="/xl/workbook.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.sheet.main+xml"/>
<Override PartName="/xl/styles.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.styles+xml"/>
""")
        for (i in 1..sheetsCount) {
            sb.append("""<Override PartName="/xl/worksheets/sheet$i.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml"/>
""")
        }
        sb.append("</Types>")
        return sb.toString()
    }

    private fun buildRelsXml(): String = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
<Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument" Target="xl/workbook.xml"/>
</Relationships>"""

    private fun buildWorkbookXml(dataSets: Set<DataSetType>): String {
        val sb = StringBuilder("""<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<workbook xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main" xmlns:r="http://schemas.openxmlformats.org/officeDocument/2006/relationships">
<sheets>
""")
        var sheetId = 1
        if (dataSets.contains(DataSetType.Inventory) || dataSets.contains(DataSetType.Alerts)) {
            sb.append("""<sheet name="Inventory" sheetId="$sheetId" r:id="rId$sheetId"/>
""")
            sheetId++
        }
        if (dataSets.contains(DataSetType.Customers)) {
            sb.append("""<sheet name="Customers" sheetId="$sheetId" r:id="rId$sheetId"/>
""")
            sheetId++
        }
        if (dataSets.contains(DataSetType.Molds)) {
            sb.append("""<sheet name="Molds" sheetId="$sheetId" r:id="rId$sheetId"/>
""")
            sheetId++
        }
        if (dataSets.contains(DataSetType.HistoryLogs)) {
            sb.append("""<sheet name="History" sheetId="$sheetId" r:id="rId$sheetId"/>
""")
            sheetId++
        }
        if (sheetId == 1) {
            sb.append("""<sheet name="Export" sheetId="1" r:id="rId1"/>
""")
        }
        sb.append("</sheets></workbook>")
        return sb.toString()
    }

    private fun buildWorkbookRelsXml(sheetsCount: Int): String {
        val sb = StringBuilder("""<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
""")
        for (i in 1..sheetsCount) {
            sb.append("""<Relationship Id="rId$i" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/worksheet" Target="worksheets/sheet$i.xml"/>
""")
        }
        sb.append("""<Relationship Id="rId${sheetsCount + 1}" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/styles" Target="styles.xml"/>
</Relationships>""")
        return sb.toString()
    }

    private fun buildStylesXml(): String = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<styleSheet xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main">
<fonts count="2">
<font><sz val="11"/><name val="Calibri"/></font>
<font><b/><sz val="11"/><color rgb="FFFFFFFF"/><name val="Calibri"/></font>
</fonts>
<fills count="2">
<fill><patternFill patternType="none"/></fill>
<fill><patternFill patternType="solid"><fgColor rgb="FF0288D1"/></patternFill></fill>
</fills>
<borders count="1"><border><left/><right/><top/><bottom/></border></borders>
<cellStyleXfs count="1"><xf numFmtId="0" fontId="0" fillId="0" borderId="0"/></cellStyleXfs>
<cellXfs count="2">
<xf numFmtId="0" fontId="0" fillId="0" borderId="0" xfId="0"/>
<xf numFmtId="0" fontId="1" fillId="1" borderId="0" xfId="0" applyFont="1" applyFill="1"/>
</cellXfs>
</styleSheet>"""

    private fun buildInventorySheetXml(items: List<Component>): String {
        val sb = StringBuilder("""<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<worksheet xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main">
<sheetData>
<row r="1" s="1">
<c r="A1" t="inlineStr" s="1"><is><t>Name</t></is></c>
<c r="B1" t="inlineStr" s="1"><is><t>SKU</t></is></c>
<c r="C1" t="inlineStr" s="1"><is><t>Category</t></is></c>
<c r="D1" t="inlineStr" s="1"><is><t>Quantity</t></is></c>
<c r="E1" t="inlineStr" s="1"><is><t>Unit</t></is></c>
<c r="F1" t="inlineStr" s="1"><is><t>Min Threshold</t></is></c>
<c r="G1" t="inlineStr" s="1"><is><t>Created At</t></is></c>
</row>
""")
        items.forEachIndexed { idx, item ->
            val rowIdx = idx + 2
            sb.append("<row r=\"$rowIdx\">\n")
                .append("<c r=\"A$rowIdx\" t=\"inlineStr\"><is><t>${escapeXml(item.name)}</t></is></c>\n")
                .append("<c r=\"B$rowIdx\" t=\"inlineStr\"><is><t>${escapeXml(item.sku)}</t></is></c>\n")
                .append("<c r=\"C$rowIdx\" t=\"inlineStr\"><is><t>${escapeXml(item.category)}</t></is></c>\n")
                .append("<c r=\"D$rowIdx\"><v>${item.qty}</v></c>\n")
                .append("<c r=\"E$rowIdx\" t=\"inlineStr\"><is><t>${escapeXml(item.unit)}</t></is></c>\n")
                .append("<c r=\"F$rowIdx\"><v>${item.minStockThreshold}</v></c>\n")
                .append("<c r=\"G$rowIdx\" t=\"inlineStr\"><is><t>${formatDate(item.createdAt)}</t></is></c>\n")
                .append("</row>\n")
        }
        sb.append("</sheetData></worksheet>")
        return sb.toString()
    }

    private fun buildCustomersSheetXml(items: List<Customer>): String {
        val sb = StringBuilder("""<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<worksheet xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main">
<sheetData>
<row r="1" s="1">
<c r="A1" t="inlineStr" s="1"><is><t>Name</t></is></c>
<c r="B1" t="inlineStr" s="1"><is><t>Email</t></is></c>
<c r="C1" t="inlineStr" s="1"><is><t>Phone</t></is></c>
<c r="D1" t="inlineStr" s="1"><is><t>Address</t></is></c>
<c r="E1" t="inlineStr" s="1"><is><t>Notes</t></is></c>
<c r="F1" t="inlineStr" s="1"><is><t>Created At</t></is></c>
</row>
""")
        items.forEachIndexed { idx, item ->
            val rowIdx = idx + 2
            sb.append("<row r=\"$rowIdx\">\n")
                .append("<c r=\"A$rowIdx\" t=\"inlineStr\"><is><t>${escapeXml(item.name)}</t></is></c>\n")
                .append("<c r=\"B$rowIdx\" t=\"inlineStr\"><is><t>${escapeXml(item.email)}</t></is></c>\n")
                .append("<c r=\"C$rowIdx\" t=\"inlineStr\"><is><t>${escapeXml(item.phone)}</t></is></c>\n")
                .append("<c r=\"D$rowIdx\" t=\"inlineStr\"><is><t>${escapeXml(item.address)}</t></is></c>\n")
                .append("<c r=\"E$rowIdx\" t=\"inlineStr\"><is><t>${escapeXml(item.notes)}</t></is></c>\n")
                .append("<c r=\"F$rowIdx\" t=\"inlineStr\"><is><t>${formatDate(item.createdAt)}</t></is></c>\n")
                .append("</row>\n")
        }
        sb.append("</sheetData></worksheet>")
        return sb.toString()
    }

    private fun buildMoldsSheetXml(items: List<Mold>): String {
        val sb = StringBuilder("""<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<worksheet xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main">
<sheetData>
<row r="1" s="1">
<c r="A1" t="inlineStr" s="1"><is><t>Mold Code</t></is></c>
<c r="B1" t="inlineStr" s="1"><is><t>Cavity Count</t></is></c>
<c r="C1" t="inlineStr" s="1"><is><t>Status</t></is></c>
<c r="D1" t="inlineStr" s="1"><is><t>Location</t></is></c>
<c r="E1" t="inlineStr" s="1"><is><t>Created At</t></is></c>
</row>
""")
        items.forEachIndexed { idx, item ->
            val rowIdx = idx + 2
            sb.append("<row r=\"$rowIdx\">\n")
                .append("<c r=\"A$rowIdx\" t=\"inlineStr\"><is><t>${escapeXml(item.moldCode)}</t></is></c>\n")
                .append("<c r=\"B$rowIdx\"><v>${item.cavityCount}</v></c>\n")
                .append("<c r=\"C$rowIdx\" t=\"inlineStr\"><is><t>${escapeXml(item.status.name)}</t></is></c>\n")
                .append("<c r=\"D$rowIdx\" t=\"inlineStr\"><is><t>${escapeXml(item.location)}</t></is></c>\n")
                .append("<c r=\"E$rowIdx\" t=\"inlineStr\"><is><t>${formatDate(item.createdAt)}</t></is></c>\n")
                .append("</row>\n")
        }
        sb.append("</sheetData></worksheet>")
        return sb.toString()
    }

    private fun buildHistorySheetXml(items: List<HistoryLog>): String {
        val sb = StringBuilder("""<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<worksheet xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main">
<sheetData>
<row r="1" s="1">
<c r="A1" t="inlineStr" s="1"><is><t>Entity Type</t></is></c>
<c r="B1" t="inlineStr" s="1"><is><t>Entity Name</t></is></c>
<c r="C1" t="inlineStr" s="1"><is><t>Action</t></is></c>
<c r="D1" t="inlineStr" s="1"><is><t>Source</t></is></c>
<c r="E1" t="inlineStr" s="1"><is><t>Performed By</t></is></c>
<c r="F1" t="inlineStr" s="1"><is><t>Timestamp</t></is></c>
</row>
""")
        items.forEachIndexed { idx, item ->
            val rowIdx = idx + 2
            sb.append("<row r=\"$rowIdx\">\n")
                .append("<c r=\"A$rowIdx\" t=\"inlineStr\"><is><t>${escapeXml(item.entityType.name)}</t></is></c>\n")
                .append("<c r=\"B$rowIdx\" t=\"inlineStr\"><is><t>${escapeXml(item.entityName)}</t></is></c>\n")
                .append("<c r=\"C$rowIdx\" t=\"inlineStr\"><is><t>${escapeXml(item.action)}</t></is></c>\n")
                .append("<c r=\"D$rowIdx\" t=\"inlineStr\"><is><t>${escapeXml(item.changeSource.name)}</t></is></c>\n")
                .append("<c r=\"E$rowIdx\" t=\"inlineStr\"><is><t>${escapeXml(item.performedBy)}</t></is></c>\n")
                .append("<c r=\"F$rowIdx\" t=\"inlineStr\"><is><t>${formatDate(item.timestamp)}</t></is></c>\n")
                .append("</row>\n")
        }
        sb.append("</sheetData></worksheet>")
        return sb.toString()
    }

    private fun escapeXml(value: String): String {
        return value.replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&apos;")
    }

    private fun formatDate(timestamp: Long): String {
        if (timestamp <= 0) return ""
        return dateFormat.format(Date(timestamp))
    }
}
