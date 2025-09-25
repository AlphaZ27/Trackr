package com.example.trackr.feature_tickets

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.trackr.domain.model.Priority
import com.example.trackr.domain.model.Ticket
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun TicketsScreen(
    ticketViewModel: TicketViewModel = hiltViewModel(),
    onTicketClick: (String) -> Unit // Callback to navigate to ticket detail
) {
    val tickets by ticketViewModel.tickets.collectAsState()

    if (tickets.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No open tickets found.")
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(tickets, key = { it.id }) { ticket ->
                TicketCard(ticket = ticket, onClick = { onTicketClick(ticket.id) })
            }
        }
    }
}

@Composable
fun TicketCard(ticket: Ticket, onClick: () -> Unit) {
    val priorityColor = when (ticket.priority) {
        Priority.Low -> Color.Gray
        Priority.Medium -> Color(0xFF007BFF) // A nice blue
        Priority.High -> Color(0xFFFF9800) // A nice orange
        Priority.Urgent -> Color(0xFFF44336) // A nice red
    }

    val dateFormatter = remember { SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Top Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "#${ticket.id.take(6).uppercase()}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = ticket.department.uppercase(),
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(Modifier.height(8.dp))

            // Name / Title
            Text(text = ticket.name, style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(4.dp))

            // Description
            Text(
                text = ticket.description.take(100) + if (ticket.description.length > 100) "..." else "",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(12.dp))

            // Bottom Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = ticket.priority.name,
                    style = MaterialTheme.typography.labelMedium,
                    color = priorityColor,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = dateFormatter.format(ticket.createdDate.toDate()),
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}
