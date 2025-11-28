package com.example.trackr.domain.logic

import com.example.trackr.domain.model.Ticket
import com.example.trackr.domain.model.TicketGroup
import javax.inject.Inject

class GroupingEngine @Inject constructor(
    private val similarityEngine: SimilarityEngine
) {

    /**
     * Groups a list of tickets based on similarity.
     * Returns a list of TicketGroups.
     */
    fun groupTickets(tickets: List<Ticket>): List<TicketGroup> {
        val groups = mutableListOf<TicketGroup>()
        val processedIds = mutableSetOf<String>()

        // Only group tickets that don't already have a group ID
        val ungrouppedTickets = tickets.filter { it.groupId == null }

        ungrouppedTickets.forEach { currentTicket ->
            if (currentTicket.id in processedIds) return@forEach

            // Find similar tickets
            val similarTickets = ungrouppedTickets.filter { candidate ->
                candidate.id != currentTicket.id &&
                        candidate.id !in processedIds &&
                        isSimilar(currentTicket, candidate)
            }

            if (similarTickets.isNotEmpty()) {
                val groupList = similarTickets + currentTicket
                val groupTitle = "Issue: ${currentTicket.name}" // Simple title generation
                val groupId = currentTicket.id // Use the first ticket's ID as the group ID

                groups.add(TicketGroup(groupId, groupTitle, groupList, groupList.size))

                // Mark all as processed
                groupList.forEach { processedIds.add(it.id) }
            }
        }

        return groups
    }

    private fun isSimilar(t1: Ticket, t2: Ticket): Boolean {
        // 1. Check Category match (Hard requirement for this logic)
        if (t1.category != t2.category) return false

        // 2. Check text similarity
        val score = similarityEngine.calculateSimilarity(
            "${t1.name} ${t1.description}",
            "${t2.name} ${t2.description}"
        )

        return score > 0.4 // Threshold
    }
}