package com.company.cavitrack.data.export

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.pdf.PdfDocument
import com.company.cavitrack.domain.export.DataSetType
import com.company.cavitrack.domain.model.Component
import com.company.cavitrack.domain.model.Customer
import com.company.cavitrack.domain.model.HistoryLog
import com.company.cavitrack.domain.model.Mold
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PdfReportExportEngine @Inject constructor() {

    private val pageWidth = 595 // A4 width in points
    private val pageHeight = 842 // A4 height in points
    private val margin = 36f

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)

    fun generatePdfReport(
        dataSets: Set<DataSetType>,
        components: List<Component> = emptyList(),
        customers: List<Customer> = emptyList(),
        molds: List<Mold> = emptyList(),
        historyLogs: List<HistoryLog> = emptyList(),
        includeVisualCharts: Boolean = true
    ): ByteArray {
        val document = PdfDocument()
        var pageNumber = 1

        val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
        var page = document.startPage(pageInfo)
        var canvas = page.canvas

        // Paints
        val primaryPaint = Paint().apply { color = Color.parseColor("#0288D1"); style = Paint.Style.FILL }
        val darkTextPaint = Paint().apply { color = Color.parseColor("#212121"); textSize = 10f; isAntiAlias = true }
        val boldTitlePaint = Paint().apply { color = Color.WHITE; textSize = 16f; isFakeBoldText = true; isAntiAlias = true }
        val subTitlePaint = Paint().apply { color = Color.WHITE; textSize = 10f; isAntiAlias = true }
        val headerLabelPaint = Paint().apply { color = Color.parseColor("#757575"); textSize = 9f; isAntiAlias = true }
        val sectionHeaderPaint = Paint().apply { color = Color.parseColor("#0288D1"); textSize = 13f; isFakeBoldText = true; isAntiAlias = true }
        val tableHeaderPaint = Paint().apply { color = Color.WHITE; textSize = 9f; isFakeBoldText = true; isAntiAlias = true }
        val altRowPaint = Paint().apply { color = Color.parseColor("#F5F5F5"); style = Paint.Style.FILL }
        val borderPaint = Paint().apply { color = Color.parseColor("#E0E0E0"); style = Paint.Style.STROKE; strokeWidth = 1f }
        val alertPaint = Paint().apply { color = Color.parseColor("#D32F2F"); textSize = 9f; isFakeBoldText = true; isAntiAlias = true }

        var y = margin

        // 1. HEADER BANNER
        canvas.drawRect(0f, 0f, pageWidth.toFloat(), 70f, primaryPaint)
        canvas.drawText("CAVITRACK MANUFACTURING SUITE", margin, 32f, boldTitlePaint)
        canvas.drawText("Executive Business Intelligence & Operational Data Report", margin, 48f, subTitlePaint)
        
        val genDateStr = dateFormat.format(Date())
        val dateWidth = darkTextPaint.measureText("Generated: $genDateStr")
        canvas.drawText("Generated: $genDateStr", pageWidth - margin - dateWidth, 48f, subTitlePaint)

        y = 90f

        // 2. EXECUTIVE SUMMARY BOX
        val summaryBoxRect = RectF(margin, y, pageWidth - margin, y + 65f)
        canvas.drawRect(summaryBoxRect, altRowPaint)
        canvas.drawRect(summaryBoxRect, borderPaint)

        val totalRecords = components.size + customers.size + molds.size + historyLogs.size
        val lowStockCount = components.count { it.qty <= it.minStockThreshold }
        val activeMoldsCount = molds.count { it.status.name.equals("Active", ignoreCase = true) }

        canvas.drawText("EXECUTIVE SUMMARY METRICS", margin + 12f, y + 18f, sectionHeaderPaint)
        canvas.drawText("Total Items: $totalRecords", margin + 12f, y + 36f, darkTextPaint)
        canvas.drawText("Components: ${components.size} ($lowStockCount Low Stock)", margin + 130f, y + 36f, darkTextPaint)
        canvas.drawText("Molds: ${molds.size} ($activeMoldsCount Active)", margin + 300f, y + 36f, darkTextPaint)
        canvas.drawText("Customers: ${customers.size}", margin + 440f, y + 36f, darkTextPaint)
        canvas.drawText("History Audit Logs: ${historyLogs.size} records", margin + 12f, y + 52f, darkTextPaint)

        y += 80f

        // 3. VISUAL CHARTS (IF ENABLED)
        if (includeVisualCharts && components.isNotEmpty()) {
            canvas.drawText("STOCK STATUS DISTRIBUTION", margin, y, sectionHeaderPaint)
            y += 15f

            val chartBox = RectF(margin, y, margin + 240f, y + 90f)
            canvas.drawRect(chartBox, borderPaint)

            // Draw Donut Pie Chart
            val pieBounds = RectF(margin + 15f, y + 10f, margin + 85f, y + 80f)
            val normalCount = (components.size - lowStockCount).coerceAtLeast(0)
            val normalAngle = if (components.isNotEmpty()) (normalCount.toFloat() / components.size) * 360f else 360f
            val lowStockAngle = 360f - normalAngle

            val piePaintNormal = Paint().apply { color = Color.parseColor("#4CAF50"); style = Paint.Style.FILL }
            val piePaintLow = Paint().apply { color = Color.parseColor("#E53935"); style = Paint.Style.FILL }

            canvas.drawArc(pieBounds, 0f, normalAngle, true, piePaintNormal)
            if (lowStockAngle > 0f) {
                canvas.drawArc(pieBounds, normalAngle, lowStockAngle, true, piePaintLow)
            }

            // Donut hole
            val holeBounds = RectF(margin + 35f, y + 30f, margin + 65f, y + 60f)
            val holePaint = Paint().apply { color = Color.WHITE; style = Paint.Style.FILL }
            canvas.drawRect(holeBounds, holePaint)

            // Legend
            canvas.drawRect(margin + 105f, y + 25f, margin + 115f, y + 35f, piePaintNormal)
            canvas.drawText("Normal Stock: $normalCount", margin + 122f, y + 33f, darkTextPaint)

            canvas.drawRect(margin + 105f, y + 50f, margin + 115f, y + 60f, piePaintLow)
            canvas.drawText("Low Stock Alerts: $lowStockCount", margin + 122f, y + 58f, darkTextPaint)

            // Category Bar Chart
            canvas.drawText("TOP CATEGORY DISTRIBUTION", margin + 260f, y - 15f, sectionHeaderPaint)
            val barBox = RectF(margin + 260f, y, pageWidth - margin, y + 90f)
            canvas.drawRect(barBox, borderPaint)

            val categories = components.groupBy { it.category.ifBlank { "Uncategorized" } }
                .mapValues { it.value.size }
                .entries.sortedByDescending { it.value }.take(3)

            var barY = y + 20f
            val maxCatCount = categories.maxOfOrNull { it.value } ?: 1
            for (cat in categories) {
                val barWidth = ((cat.value.toFloat() / maxCatCount) * 120f).coerceAtLeast(5f)
                canvas.drawText(cat.key.take(12), margin + 268f, barY + 10f, darkTextPaint)
                canvas.drawRect(margin + 340f, barY, margin + 340f + barWidth, barY + 12f, primaryPaint)
                canvas.drawText("${cat.value}", margin + 348f + barWidth, barY + 10f, darkTextPaint)
                barY += 22f
            }

            y += 105f
        }

        // Helper function for page overflow
        fun checkPageOverflow(requiredSpace: Float): Canvas {
            if (y + requiredSpace > pageHeight - margin - 20f) {
                // Draw Footer
                canvas.drawLine(margin, pageHeight - 30f, pageWidth - margin, pageHeight - 30f, borderPaint)
                canvas.drawText("CaviTrack Enterprise Audit Document • Confidential", margin, pageHeight - 16f, headerLabelPaint)
                canvas.drawText("Page $pageNumber", pageWidth - margin - 40f, pageHeight - 16f, headerLabelPaint)

                document.finishPage(page)
                pageNumber++
                val newPageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
                page = document.startPage(newPageInfo)
                canvas = page.canvas

                // Re-draw Mini Header
                canvas.drawRect(0f, 0f, pageWidth.toFloat(), 30f, primaryPaint)
                canvas.drawText("CAVITRACK ENTERPRISE REPORT (Cont.)", margin, 20f, subTitlePaint)
                y = 45f
            }
            return canvas
        }

        // 4. INVENTORY TABLE
        if (dataSets.contains(DataSetType.Inventory) || dataSets.contains(DataSetType.Alerts)) {
            checkPageOverflow(50f)
            canvas.drawText("INVENTORY & COMPONENTS", margin, y, sectionHeaderPaint)
            y += 12f

            // Table Header
            canvas.drawRect(margin, y, pageWidth - margin, y + 20f, primaryPaint)
            canvas.drawText("SKU / Name", margin + 8f, y + 14f, tableHeaderPaint)
            canvas.drawText("Category", margin + 180f, y + 14f, tableHeaderPaint)
            canvas.drawText("Qty", margin + 280f, y + 14f, tableHeaderPaint)
            canvas.drawText("Threshold", margin + 340f, y + 14f, tableHeaderPaint)
            canvas.drawText("Status", margin + 420f, y + 14f, tableHeaderPaint)
            y += 20f

            components.forEachIndexed { idx, c ->
                checkPageOverflow(18f)
                if (idx % 2 == 1) {
                    canvas.drawRect(margin, y, pageWidth - margin, y + 18f, altRowPaint)
                }
                canvas.drawText("${c.sku} - ${c.name.take(20)}", margin + 8f, y + 13f, darkTextPaint)
                canvas.drawText(c.category.take(15), margin + 180f, y + 13f, darkTextPaint)
                canvas.drawText("${c.qty} ${c.unit}", margin + 280f, y + 13f, darkTextPaint)
                canvas.drawText("${c.minStockThreshold}", margin + 340f, y + 13f, darkTextPaint)
                
                if (c.qty <= c.minStockThreshold) {
                    canvas.drawText("LOW STOCK", margin + 420f, y + 13f, alertPaint)
                } else {
                    canvas.drawText("OK", margin + 420f, y + 13f, darkTextPaint)
                }
                y += 18f
            }
            y += 15f
        }

        // 5. MOLDS TABLE
        if (dataSets.contains(DataSetType.Molds)) {
            checkPageOverflow(50f)
            canvas.drawText("MOLDS & EQUIPMENT", margin, y, sectionHeaderPaint)
            y += 12f

            canvas.drawRect(margin, y, pageWidth - margin, y + 20f, primaryPaint)
            canvas.drawText("Mold Code", margin + 8f, y + 14f, tableHeaderPaint)
            canvas.drawText("Cavities", margin + 150f, y + 14f, tableHeaderPaint)
            canvas.drawText("Status", margin + 240f, y + 14f, tableHeaderPaint)
            canvas.drawText("Location", margin + 360f, y + 14f, tableHeaderPaint)
            y += 20f

            molds.forEachIndexed { idx, m ->
                checkPageOverflow(18f)
                if (idx % 2 == 1) {
                    canvas.drawRect(margin, y, pageWidth - margin, y + 18f, altRowPaint)
                }
                canvas.drawText(m.moldCode, margin + 8f, y + 13f, darkTextPaint)
                canvas.drawText("${m.cavityCount}", margin + 150f, y + 13f, darkTextPaint)
                canvas.drawText(m.status.name, margin + 240f, y + 13f, darkTextPaint)
                canvas.drawText(m.location, margin + 360f, y + 13f, darkTextPaint)
                y += 18f
            }
            y += 15f
        }

        // 6. CUSTOMERS TABLE
        if (dataSets.contains(DataSetType.Customers)) {
            checkPageOverflow(50f)
            canvas.drawText("CUSTOMERS DIRECTORY", margin, y, sectionHeaderPaint)
            y += 12f

            canvas.drawRect(margin, y, pageWidth - margin, y + 20f, primaryPaint)
            canvas.drawText("Name", margin + 8f, y + 14f, tableHeaderPaint)
            canvas.drawText("Email", margin + 160f, y + 14f, tableHeaderPaint)
            canvas.drawText("Phone", margin + 320f, y + 14f, tableHeaderPaint)
            y += 20f

            customers.forEachIndexed { idx, cust ->
                checkPageOverflow(18f)
                if (idx % 2 == 1) {
                    canvas.drawRect(margin, y, pageWidth - margin, y + 18f, altRowPaint)
                }
                canvas.drawText(cust.name.take(22), margin + 8f, y + 13f, darkTextPaint)
                canvas.drawText(cust.email.take(24), margin + 160f, y + 13f, darkTextPaint)
                canvas.drawText(cust.phone, margin + 320f, y + 13f, darkTextPaint)
                y += 18f
            }
            y += 15f
        }

        // Final Footer on last page
        canvas.drawLine(margin, pageHeight - 30f, pageWidth - margin, pageHeight - 30f, borderPaint)
        canvas.drawText("CaviTrack Enterprise Audit Document • Confidential", margin, pageHeight - 16f, headerLabelPaint)
        canvas.drawText("Page $pageNumber", pageWidth - margin - 40f, pageHeight - 16f, headerLabelPaint)

        document.finishPage(page)

        val baos = ByteArrayOutputStream()
        document.writeTo(baos)
        document.close()
        return baos.toByteArray()
    }
}
