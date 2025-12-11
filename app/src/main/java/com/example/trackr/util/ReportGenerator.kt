package com.example.trackr.util

import android.content.Context
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import androidx.core.content.FileProvider
import com.example.trackr.domain.model.Ticket
import com.example.trackr.domain.model.User
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

class ReportGenerator @Inject constructor() {

    // Generate Ticket Report PDF
    fun generateTicketReport(context: Context, tickets: List<Ticket>, userMap: Map<String, String>): Uri? {
        val fileName = "Trackr_Tickets_${System.currentTimeMillis()}.pdf"
        val title = "Ticket Summary Report"

        // Prepare data rows
        val rows = tickets.map { ticket ->
            // Assignee Name
            val assigneeName = if (ticket.assignee.isNotBlank()) {
                userMap[ticket.assignee] ?: "Unknown (${ticket.assignee.take(4)}..)"
            } else {
                "Unassigned"
            }

            listOf(
                "#${ticket.id.take(6).uppercase()}", // ID
                ticket.name.take(20),                // Title (Truncated)
                ticket.status.name,                  // Status
                ticket.priority.name,                // Priority
                assigneeName                         // Real Name
            )
        }
        // Headers
        val headers = listOf("ID", "Title", "Status", "Priority", "Assignee")

        return createPdf(context, fileName, title, headers, rows)
    }

    // Generate User Report PDF
    fun generateUserReport(context: Context, users: List<User>): Uri? {
        val fileName = "Trackr_Users_${System.currentTimeMillis()}.pdf"
        val title = "User Activity Report"

        val rows = users.map { user ->
            listOf(
                user.name,
                user.email,
                user.role.name,
                user.status?.name ?: "Active" // Handle optional status
            )
        }
        val headers = listOf("Name", "Email", "Role", "Status")

        return createPdf(context, fileName, title, headers, rows)
    }

    private fun createPdf(
        context: Context,
        fileName: String,
        reportTitle: String,
        headers: List<String>,
        data: List<List<String>>
    ): Uri? {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 size
        val page = pdfDocument.startPage(pageInfo)
        val canvas = page.canvas
        val paint = Paint()

        // 1. Draw Header
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textSize = 20f
        paint.color = Color.BLACK
        canvas.drawText("Trackr App Report", 50f, 50f, paint)

        paint.typeface = Typeface.DEFAULT
        paint.textSize = 14f
        canvas.drawText(reportTitle, 50f, 80f, paint)

        val dateStr = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date())
        paint.color = Color.DKGRAY
        canvas.drawText("Generated: $dateStr", 50f, 100f, paint)

        // 2. Draw Table Header
        var startY = 140f
        val startX = 50f
        val colWidths = floatArrayOf(60f, 180f, 80f, 80f, 100f) // Approximate column widths

        paint.color = Color.BLACK
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)

        headers.forEachIndexed { index, header ->
            // Use simple spacing logic based on index
            val xPos = startX + (index * 110f)
            canvas.drawText(header, xPos, startY, paint)
        }

        // Line under header
        paint.strokeWidth = 2f
        canvas.drawLine(startX, startY + 10f, 550f, startY + 10f, paint)

        // 3. Draw Data Rows
        startY += 30f
        paint.typeface = Typeface.DEFAULT
        paint.textSize = 12f
        paint.strokeWidth = 0f

        // Limit to 25 rows per page for simplicity in this version
        val rowsToDraw = data.take(25)

        rowsToDraw.forEach { row ->
            row.forEachIndexed { index, cell ->
                val xPos = startX + (index * 110f)
                // Basic truncation to prevent overlap
                val text = if (cell.length > 15) cell.take(12) + "..." else cell
                canvas.drawText(text, xPos, startY, paint)
            }
            startY += 20f
        }

        if (data.size > 25) {
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC)
            canvas.drawText("... ${data.size - 25} more records (truncated for preview)", startX, startY + 20f, paint)
        }

        pdfDocument.finishPage(page)

        // 4. Save to Cache
        val reportsDir = File(context.cacheDir, "reports")
        if (!reportsDir.exists()) reportsDir.mkdirs()

        val file = File(reportsDir, fileName)

        try {
            pdfDocument.writeTo(FileOutputStream(file))
            pdfDocument.close()
        } catch (e: IOException) {
            e.printStackTrace()
            pdfDocument.close()
            return null
        }

        // 5. Return URI via FileProvider
        return try {
            FileProvider.getUriForFile(
                context,
                "${context.packageName}.provider",
                file
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

//    private val dateFormatter = SimpleDateFormat("yyyy-MM-dd hh:mm a", Locale.getDefault())
//
//    /**
//     * Generates a CSV file of the given users in the app's cache directory
//     * and returns a shareable Uri.
//     */
//    fun generateUserReport(context: Context, users: List<User>): Uri? {
//        return try {
//            val csvContent = buildUserCsvContent(users)
//            val cacheDir = context.cacheDir
//            val file = File(cacheDir, "user_report.csv")
//
//            // Write the CSV content to the file
//            file.writeText(csvContent)
//
//            // Get a content Uri for the file using the FileProvider
//            FileProvider.getUriForFile(
//                context,
//                "${context.packageName}.provider",
//                file
//            )
//        } catch (e: Exception) {
//            e.printStackTrace()
//            null
//        }
//    }
//
//    private fun buildUserCsvContent(users: List<User>): String {
//        val header = "ID,Name,Email,Role,Status\n"
//        val rows = users.joinToString("\n") { user ->
//            "${user.id},${user.name},${user.email},${user.role.name},${user.status.name}"
//        }
//        return header + rows
//    }
//
//    /**
//     * Generates a CSV file of the given tickets in the app's cache directory
//     * and returns a shareable Uri.
//     */
//    fun generateTicketReport(context: Context, tickets: List<Ticket>): Uri? {
//        return try {
//            val csvContent = buildTicketCsvContent(tickets)
//            val file = File(context.cacheDir, "ticket_report.csv")
//            file.writeText(csvContent)
//            FileProvider.getUriForFile(
//                context,
//                "${context.packageName}.provider",
//                file
//            )
//        } catch (e: Exception) {
//            e.printStackTrace()
//            null
//        }
//    }
//
//    // **HELPER FUNCTION**
//    private fun buildTicketCsvContent(tickets: List<Ticket>): String {
//        val header = "ID,Name,Status,Priority,Department,Assignee,CreatedDate\n"
//        val rows = tickets.joinToString("\n") { ticket ->
//            val formattedDate = ticket.createdDate?.toDate()?.let { dateFormatter.format(it) } ?: ""
//            // Enclose in quotes to handle potential commas in names or descriptions
//            "\"${ticket.id}\",\"${ticket.name}\",\"${ticket.status.name}\",\"${ticket.priority.name}\",\"${ticket.department}\",\"${ticket.assignee}\",\"$formattedDate\""
//        }
//        return header + rows
//    }
}