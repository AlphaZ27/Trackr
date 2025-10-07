package com.example.trackr.feature_tickets

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.trackr.domain.model.Priority
import com.example.trackr.domain.model.Ticket
import com.example.trackr.domain.model.TicketStatus
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun TicketsScreen(
    ticketViewModel: TicketViewModel = hiltViewModel(),
    modifier: Modifier = Modifier,
    onTicketClick: (String) -> Unit // Callback to navigate to ticket detail
) {
    //val tickets by ticketViewModel.tickets.collectAsState()

    // Observe all the necessary state from the ViewModel
    val filteredTickets by ticketViewModel.filteredTickets.collectAsState()
    val searchQuery by ticketViewModel.searchQuery.collectAsState()
    val selectedStatus by ticketViewModel.selectedStatus.collectAsState()
    val selectedPriority by ticketViewModel.selectedPriority.collectAsState()

    Column(modifier = modifier.fillMaxSize()) {
        // The new filter section at the top
        FilterSection(
            searchQuery = searchQuery,
            selectedStatus = selectedStatus,
            selectedPriority = selectedPriority,
            onQueryChange = ticketViewModel::onSearchQueryChange,
            onStatusChange = ticketViewModel::onStatusSelected,
            onPriorityChange = ticketViewModel::onPrioritySelected,
            onClearFilters = ticketViewModel::clearFilters
        )

        // The list of tickets
        if (filteredTickets.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(if (searchQuery.isNotBlank() || selectedStatus != null || selectedPriority != null) "No tickets match your filters." else "No open tickets found.")
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filteredTickets, key = { it.id }) { ticket ->
                    TicketCard(ticket = ticket, onClick = { onTicketClick(ticket.id) })
                }
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FilterSection(
    searchQuery: String,
    selectedStatus: TicketStatus?,
    selectedPriority: Priority?,
    onQueryChange: (String) -> Unit,
    onStatusChange: (TicketStatus?) -> Unit,
    onPriorityChange: (Priority?) -> Unit,
    onClearFilters: () -> Unit
) {
    var statusExpanded by remember { mutableStateOf(false) }
    var priorityExpanded by remember { mutableStateOf(false) }

    Column(modifier = Modifier.padding(16.dp)) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onQueryChange,
            label = { Text("Search by name, desc, or ID") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Status Filter Dropdown
            ExposedDropdownMenuBox(
                expanded = statusExpanded,
                onExpandedChange = { statusExpanded = !statusExpanded },
                modifier = Modifier.weight(1f)
            ) {
                OutlinedTextField(
                    value = selectedStatus?.name ?: "All Statuses",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Status") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = statusExpanded) },
                    modifier = Modifier.menuAnchor()
                )
                ExposedDropdownMenu(
                    expanded = statusExpanded,
                    onDismissRequest = { statusExpanded = false }
                ) {
                    DropdownMenuItem(text = { Text("All Statuses") }, onClick = { onStatusChange(null); statusExpanded = false })
                    TicketStatus.values().forEach { status ->
                        DropdownMenuItem(
                            text = { Text(status.name) },
                            onClick = { onStatusChange(status); statusExpanded = false }
                        )
                    }
                }
            }

            // Priority Filter Dropdown
            ExposedDropdownMenuBox(
                expanded = priorityExpanded,
                onExpandedChange = { priorityExpanded = !priorityExpanded },
                modifier = Modifier.weight(1f)
            ) {
                OutlinedTextField(
                    value = selectedPriority?.name ?: "All Priorities",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Priority") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = priorityExpanded) },
                    modifier = Modifier.menuAnchor()
                )
                ExposedDropdownMenu(
                    expanded = priorityExpanded,
                    onDismissRequest = { priorityExpanded = false }
                ) {
                    DropdownMenuItem(text = { Text("All Priorities") }, onClick = { onPriorityChange(null); priorityExpanded = false })
                    Priority.values().forEach { priority ->
                        DropdownMenuItem(
                            text = { Text(priority.name) },
                            onClick = { onPriorityChange(priority); priorityExpanded = false }
                        )
                    }
                }
            }

            IconButton(onClick = onClearFilters) {
                Icon(Icons.Default.Clear, contentDescription = "Clear Filters")
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
