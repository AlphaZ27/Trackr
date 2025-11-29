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
     * 1. Aggregates tickets that ALREADY have a persistent groupId (Confirmed groups).
     * 2. Scans remaining "ungrouped" tickets for similarity (Potential groups).
     */
    fun groupTickets(tickets: List<Ticket>): List<TicketGroup> {
        val allGroups = mutableListOf<TicketGroup>()

        // --- Handle Persisted Groups ---
        // Group by the non-null groupId
        val persistedGroups = tickets
            .filter { it.groupId != null }
            .groupBy { it.groupId!! }

        persistedGroups.forEach { (groupId, groupTickets) ->
            if (groupTickets.isNotEmpty()) {
                // Use the name of the most recent ticket as the title, or a generic one
                val primaryTicket = groupTickets.maxByOrNull { it.createdDate } ?: groupTickets.first()
                allGroups.add(
                    TicketGroup(
                        id = groupId,
                        title = "Group: ${primaryTicket.name}", // Or fetch a saved group name if you have a separate collection
                        tickets = groupTickets,
                        size = groupTickets.size
                    )
                )
            }
        }

        // --- Handle Auto-Suggestions (Similarity) ---
        // Only look at tickets that are NOT already in a group
        val ungrouppedTickets = tickets.filter { it.groupId == null }
        val processedIds = mutableSetOf<String>()

        ungrouppedTickets.forEach { currentTicket ->
            if (currentTicket.id in processedIds) return@forEach

            // Find similar tickets among the remaining ungrouped ones
            val similarTickets = ungrouppedTickets.filter { candidate ->
                candidate.id != currentTicket.id &&
                        candidate.id !in processedIds &&
                        isSimilar(currentTicket, candidate)
            }

            if (similarTickets.isNotEmpty()) {
                val groupList = similarTickets + currentTicket
                val groupTitle = "Potential Issue: ${currentTicket.name}"
                // For potential groups, we generate a temporary ID (usually the first ticket's ID)
                val tempGroupId = currentTicket.id

                allGroups.add(TicketGroup(tempGroupId, groupTitle, groupList, groupList.size))

                // Mark all as processed so we don't duplicate them
                groupList.forEach { processedIds.add(it.id) }
            }
        }

        return allGroups.sortedByDescending { it.size }
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