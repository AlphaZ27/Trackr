package com.example.trackr.feature_manager

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.trackr.domain.repository.DashboardRepository
import com.example.trackr.domain.repository.ReportingRepository
import com.example.trackr.util.PdfReportGenerator
import com.example.trackr.util.ReportGenerator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar
import javax.inject.Inject

@HiltViewModel
class ReportsViewModel @Inject constructor(
    private val reportingRepository: ReportingRepository,
    //private val pdfGenerator: PdfReportGenerator,
    private val dashboardRepository: DashboardRepository, // Needed for user names
    private val reportGenerator: ReportGenerator // Use standard generator class
) : ViewModel() {

    private val _isGenerating = MutableStateFlow(false)
    val isGenerating = _isGenerating.asStateFlow()

    fun generateWeeklyReport(context: Context, onReady: (Uri) -> Unit) {
        val calendar = Calendar.getInstance()
        val end = calendar.timeInMillis
        calendar.add(Calendar.DAY_OF_YEAR, -7)
        val start = calendar.timeInMillis

        generateReport(context, "Weekly Summary (Last 7 Days)", start, end, onReady)
    }

    fun generateMonthlyReport(context: Context, onReady: (Uri) -> Unit) {
        val calendar = Calendar.getInstance()
        val end = calendar.timeInMillis
        calendar.add(Calendar.DAY_OF_YEAR, -30)
        val start = calendar.timeInMillis

        generateReport(context, "Monthly Summary (Last 30 Days)", start, end, onReady)
    }

    private fun generateReport(context: Context, title: String, start: Long, end: Long, onReady: (Uri) -> Unit) {
        viewModelScope.launch {
            _isGenerating.value = true
            try {
                // Fetch Data
                val tickets = reportingRepository.getTicketsInRange(start, end).first()

                // Fetch Users for Name Resolution
                val users = dashboardRepository.getAllUsers().first()
                val userMap = users.associate { it.id to it.name }

                // Generate PDF (on Background Thread)
//                val file = withContext(Dispatchers.IO) {
//                    pdfGenerator.generateReport(context, title, tickets)
//                }
                val uri = withContext(Dispatchers.IO) {
                    // Update ReportGenerator if you want custom titles passed in,
                    // or just use the standard "Ticket Summary Report" header it has.
                    reportGenerator.generateTicketReport(context, tickets, userMap)
                }

                // Get URI
//                if (file != null) {
//                    val uri = FileProvider.getUriForFile(
//                        context,
//                        "${context.packageName}.provider",
//                        file
//                    )
//                    onReady(uri)
//                }
                if (uri != null) {
                    onReady(uri)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isGenerating.value = false
            }
        }
    }
}