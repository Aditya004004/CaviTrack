package com.company.cavitrack.data.export

import com.company.cavitrack.domain.export.DataSetType
import com.company.cavitrack.domain.model.Component
import com.company.cavitrack.domain.model.Customer
import com.company.cavitrack.domain.model.HistoryLog
import com.company.cavitrack.domain.model.Mold
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CsvExportEngine @Inject constructor() {

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)

    fun generateCsv(
        dataSets: Set<DataSetType>,
        components: List<Component> = emptyList(),
        customers: List<Customer> = emptyList(),
        molds: List<Mold> = emptyList(),
        historyLogs: List<HistoryLog> = emptyList()
    ): ByteArray {
        val sb = StringBuilder()
        
        // UTF-8 BOM byte marker so Excel opens CSV files with correct UTF-8 encoding
        sb.append("\uFEFF")

        if (dataSets.contains(DataSetType.Inventory) || dataSets.contains(DataSetType.Alerts)) {
            sb.append("=== INVENTORY & COMPONENTS ===\n")
            sb.append("ID,Name,SKU,Category,Quantity,Unit,Min Threshold,Is Low Stock,Created At,Updated At\n")
            for (c in components) {
                val isLowStock = if (c.qty <= c.minStockThreshold) "YES" else "NO"
                sb.append("${sanitizeCsvCell(c.id)},")
                    .append("${sanitizeCsvCell(c.name)},")
                    .append("${sanitizeCsvCell(c.sku)},")
                    .append("${sanitizeCsvCell(c.category)},")
                    .append("${c.qty},")
                    .append("${sanitizeCsvCell(c.unit)},")
                    .append("${c.minStockThreshold},")
                    .append("$isLowStock,")
                    .append("${formatDate(c.createdAt)},")
                    .append("${formatDate(c.updatedAt)}\n")
            }
            sb.append("\n")
        }

        if (dataSets.contains(DataSetType.Customers)) {
            sb.append("=== CUSTOMERS ===\n")
            sb.append("ID,Name,Email,Phone,Address,Notes,Created At,Updated At\n")
            for (cust in customers) {
                sb.append("${sanitizeCsvCell(cust.id)},")
                    .append("${sanitizeCsvCell(cust.name)},")
                    .append("${sanitizeCsvCell(cust.email)},")
                    .append("${sanitizeCsvCell(cust.phone)},")
                    .append("${sanitizeCsvCell(cust.address)},")
                    .append("${sanitizeCsvCell(cust.notes)},")
                    .append("${formatDate(cust.createdAt)},")
                    .append("${formatDate(cust.updatedAt)}\n")
            }
            sb.append("\n")
        }

        if (dataSets.contains(DataSetType.Molds)) {
            sb.append("=== MOLDS ===\n")
            sb.append("ID,Mold Code,Cavity Count,Status,Location,Created At,Updated At\n")
            for (m in molds) {
                sb.append("${sanitizeCsvCell(m.id)},")
                    .append("${sanitizeCsvCell(m.moldCode)},")
                    .append("${m.cavityCount},")
                    .append("${sanitizeCsvCell(m.status.name)},")
                    .append("${sanitizeCsvCell(m.location)},")
                    .append("${formatDate(m.createdAt)},")
                    .append("${formatDate(m.updatedAt)}\n")
            }
            sb.append("\n")
        }

        if (dataSets.contains(DataSetType.HistoryLogs)) {
            sb.append("=== HISTORY LOGS & AUDIT TRAIL ===\n")
            sb.append("ID,Entity Type,Entity Name,Action,Source,Performed By,Before,After,Timestamp\n")
            for (h in historyLogs) {
                sb.append("${sanitizeCsvCell(h.id)},")
                    .append("${sanitizeCsvCell(h.entityType.name)},")
                    .append("${sanitizeCsvCell(h.entityName)},")
                    .append("${sanitizeCsvCell(h.action)},")
                    .append("${sanitizeCsvCell(h.changeSource.name)},")
                    .append("${sanitizeCsvCell(h.performedBy)},")
                    .append("${sanitizeCsvCell(h.beforeValue ?: "")},")
                    .append("${sanitizeCsvCell(h.afterValue ?: "")},")
                    .append("${formatDate(h.timestamp)}\n")
            }
        }

        return sb.toString().toByteArray(Charsets.UTF_8)
    }

    /**
     * Sanitizes CSV cell values against CSV Formula Injection (CWE-1236).
     * Neutralizes formula triggers (=, +, -, @, tab, CR) by prepending a single quote.
     */
    fun sanitizeCsvCell(value: String): String {
        if (value.isEmpty()) return value
        var cleaned = value

        // Prepend single quote if field starts with formula execution triggers
        val firstChar = cleaned[0]
        if (firstChar == '=' || firstChar == '+' || firstChar == '-' || firstChar == '@' || firstChar == '\t' || firstChar == '\r') {
            cleaned = "'$cleaned"
        }

        return escapeCsv(cleaned)
    }

    private fun escapeCsv(value: String): String {
        if (value.contains(",") || value.contains("\"") || value.contains("\n") || value.contains("\r")) {
            val replaced = value.replace("\"", "\"\"")
            return "\"$replaced\""
        }
        return value
    }

    private fun formatDate(timestamp: Long): String {
        if (timestamp <= 0) return ""
        return dateFormat.format(Date(timestamp))
    }
}
