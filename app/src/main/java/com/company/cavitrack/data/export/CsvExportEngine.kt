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
                sb.append("${escapeCsv(c.id)},")
                    .append("${escapeCsv(c.name)},")
                    .append("${escapeCsv(c.sku)},")
                    .append("${escapeCsv(c.category)},")
                    .append("${c.qty},")
                    .append("${escapeCsv(c.unit)},")
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
                sb.append("${escapeCsv(cust.id)},")
                    .append("${escapeCsv(cust.name)},")
                    .append("${escapeCsv(cust.email)},")
                    .append("${escapeCsv(cust.phone)},")
                    .append("${escapeCsv(cust.address)},")
                    .append("${escapeCsv(cust.notes)},")
                    .append("${formatDate(cust.createdAt)},")
                    .append("${formatDate(cust.updatedAt)}\n")
            }
            sb.append("\n")
        }

        if (dataSets.contains(DataSetType.Molds)) {
            sb.append("=== MOLDS ===\n")
            sb.append("ID,Mold Code,Cavity Count,Status,Location,Created At,Updated At\n")
            for (m in molds) {
                sb.append("${escapeCsv(m.id)},")
                    .append("${escapeCsv(m.moldCode)},")
                    .append("${m.cavityCount},")
                    .append("${escapeCsv(m.status.name)},")
                    .append("${escapeCsv(m.location)},")
                    .append("${formatDate(m.createdAt)},")
                    .append("${formatDate(m.updatedAt)}\n")
            }
            sb.append("\n")
        }

        if (dataSets.contains(DataSetType.HistoryLogs)) {
            sb.append("=== HISTORY LOGS & AUDIT TRAIL ===\n")
            sb.append("ID,Entity Type,Entity Name,Action,Source,Performed By,Before,After,Timestamp\n")
            for (h in historyLogs) {
                sb.append("${escapeCsv(h.id)},")
                    .append("${escapeCsv(h.entityType.name)},")
                    .append("${escapeCsv(h.entityName)},")
                    .append("${escapeCsv(h.action)},")
                    .append("${escapeCsv(h.changeSource.name)},")
                    .append("${escapeCsv(h.performedBy)},")
                    .append("${escapeCsv(h.beforeValue ?: "")},")
                    .append("${escapeCsv(h.afterValue ?: "")},")
                    .append("${formatDate(h.timestamp)}\n")
            }
        }

        return sb.toString().toByteArray(Charsets.UTF_8)
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
