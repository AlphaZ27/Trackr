package com.example.trackr.util

import android.content.Context
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.os.Environment
import androidx.core.content.FileProvider
import com.example.trackr.domain.model.Ticket
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

class PdfReportGenerator @Inject constructor() {

    fun generateReport(context: Context, title: String, tickets: List<Ticket>): File? {
        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 size
        val page = document.startPage(pageInfo)
        val canvas = page.canvas
        val paint = Paint()

        // Header
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textSize = 24f
        paint.color = Color.BLACK
        canvas.drawText("Trackr IT Report", 50f, 60f, paint)

        paint.typeface = Typeface.DEFAULT
        paint.textSize = 14f
        canvas.drawText(title, 50f, 90f, paint)
        canvas.drawText("Generated on: ${SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date())}", 50f, 110f, paint)

        // Summary Stats
        val openCount = tickets.count { it.status.name == "Open" || it.status.name == "InProgress" }
        val closedCount = tickets.count { it.status.name == "Closed" }

        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 2f
        canvas.drawRect(50f, 130f, 545f, 200f, paint) // Box
        paint.style = Paint.Style.FILL
        paint.textSize = 16f
        canvas.drawText("Total Tickets: ${tickets.size}", 70f, 160f, paint)
        canvas.drawText("Open: $openCount", 250f, 160f, paint)
        canvas.drawText("Closed: $closedCount", 400f, 160f, paint)

        // Ticket List Table Header
        val startY = 240f
        var currentY = startY
        paint.textSize = 12f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)

        canvas.drawText("ID", 50f, currentY, paint)
        canvas.drawText("Title", 120f, currentY, paint)
        canvas.drawText("Status", 350f, currentY, paint)
        canvas.drawText("Priority", 450f, currentY, paint)

        // Draw Line
        paint.strokeWidth = 1f
        canvas.drawLine(50f, currentY + 10f, 545f, currentY + 10f, paint)
        currentY += 30f

        // Rows (Limit to first 20 for this single-page demo)
        paint.typeface = Typeface.DEFAULT
        tickets.take(20).forEach { ticket ->
            canvas.drawText("#${ticket.id.take(4)}", 50f, currentY, paint)

            // Truncate title
            val titleText = if (ticket.name.length > 25) ticket.name.take(25) + "..." else ticket.name
            canvas.drawText(titleText, 120f, currentY, paint)

            canvas.drawText(ticket.status.name, 350f, currentY, paint)
            canvas.drawText(ticket.priority.name, 450f, currentY, paint)

            currentY += 20f
        }

        document.finishPage(page)

        // Save File
        val directory = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
        val fileName = "Trackr_Report_${System.currentTimeMillis()}.pdf"
        val file = File(directory, fileName)

        try {
            document.writeTo(FileOutputStream(file))
            document.close()
            return file
        } catch (e: Exception) {
            e.printStackTrace()
            document.close()
            return null
        }
    }
}