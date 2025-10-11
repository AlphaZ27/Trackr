package com.example.trackr.util

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import com.example.trackr.domain.model.Ticket
import com.example.trackr.domain.model.User
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class ReportGenerator {

    private val dateFormatter = SimpleDateFormat("yyyy-MM-dd hh:mm a", Locale.getDefault())

    /**
     * Generates a CSV file of the given users in the app's cache directory
     * and returns a shareable Uri.
     */
    fun generateUserReport(context: Context, users: List<User>): Uri? {
        return try {
            val csvContent = buildUserCsvContent(users)
            val cacheDir = context.cacheDir
            val file = File(cacheDir, "user_report.csv")

            // Write the CSV content to the file
            file.writeText(csvContent)

            // Get a content Uri for the file using the FileProvider
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

    private fun buildUserCsvContent(users: List<User>): String {
        val header = "ID,Name,Email,Role,Status\n"
        val rows = users.joinToString("\n") { user ->
            "${user.id},${user.name},${user.email},${user.role.name},${user.status.name}"
        }
        return header + rows
    }

    /**
     * Generates a CSV file of the given tickets in the app's cache directory
     * and returns a shareable Uri.
     */
    fun generateTicketReport(context: Context, tickets: List<Ticket>): Uri? {
        return try {
            val csvContent = buildTicketCsvContent(tickets)
            val file = File(context.cacheDir, "ticket_report.csv")
            file.writeText(csvContent)
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

    // **HELPER FUNCTION**
    private fun buildTicketCsvContent(tickets: List<Ticket>): String {
        val header = "ID,Name,Status,Priority,Department,Assignee,CreatedDate\n"
        val rows = tickets.joinToString("\n") { ticket ->
            val formattedDate = ticket.createdDate?.toDate()?.let { dateFormatter.format(it) } ?: ""
            // Enclose in quotes to handle potential commas in names or descriptions
            "\"${ticket.id}\",\"${ticket.name}\",\"${ticket.status.name}\",\"${ticket.priority.name}\",\"${ticket.department}\",\"${ticket.assignee}\",\"$formattedDate\""
        }
        return header + rows
    }
}