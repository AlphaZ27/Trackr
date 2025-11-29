package com.example.trackr.workers

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.trackr.domain.model.SLAStatus
import com.example.trackr.domain.model.TicketStatus
import com.example.trackr.domain.repository.SLARepository
import com.example.trackr.domain.repository.TicketRepository
import com.example.trackr.util.NotificationHelper
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import java.util.concurrent.TimeUnit

@HiltWorker
class SLAWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val ticketRepository: TicketRepository,
    private val slaRepository: SLARepository,
    private val notificationHelper: NotificationHelper
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            Log.d("SLAWorker", "Starting SLA Check...")

            // Fetch Rules
            val rules = slaRepository.getSLARules().first()
            val ruleMap = rules.associateBy { it.priority }

            // Fetch Open Tickets
            val openTickets = ticketRepository.getAllTicketsForReport().first()
                .filter { it.status == TicketStatus.Open || it.status == TicketStatus.InProgress }

            // Check Each Ticket
            val now = System.currentTimeMillis()
            var updatesCount = 0

            openTickets.forEach { ticket ->
                val rule = ruleMap[ticket.priority] ?: return@forEach // Skip if no rule matches

                val createdTime = ticket.createdDate.toDate().time
                val hoursElapsed = TimeUnit.MILLISECONDS.toHours(now - createdTime)

                val newSlaStatus = when {
                    hoursElapsed >= rule.maxResolutionTimeHours -> SLAStatus.Breached
                    hoursElapsed >= rule.warningThresholdHours -> SLAStatus.Warning
                    else -> SLAStatus.OK
                }

                // Only update if status has changed to reduce writes
                if (ticket.slaStatus != newSlaStatus) {
                    val updatedTicket = ticket.copy(slaStatus = newSlaStatus)
                    ticketRepository.updateTicket(updatedTicket)
                    updatesCount++
                    // Trigger Notification Logic
                    // Only notify if the status is getting worse (Warning or Breached)
                    if (newSlaStatus == SLAStatus.Warning || newSlaStatus == SLAStatus.Breached) {
                        notificationHelper.showSlaNotification(updatedTicket, newSlaStatus)
                    }
                }
            }

            Log.d("SLAWorker", "SLA Check Complete. Updated $updatesCount tickets.")
            Result.success()
        } catch (e: Exception) {
            Log.e("SLAWorker", "Error executing SLA check", e)
            Result.failure()
        }
    }
}