package com.example.trackr.feature_tickets

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.trackr.domain.model.Priority
import com.example.trackr.domain.model.Ticket
import com.example.trackr.domain.model.TicketStatus
import com.example.trackr.util.ReportGenerator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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

    // For launching the share intent
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val reportGenerator = remember { ReportGenerator() }

    LazyColumn(
        modifier = modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "All Tickets",
                    style = MaterialTheme.typography.titleMedium,
                )
                OutlinedButton(
                    onClick = {
                        scope.launch {
                            val uri = withContext(Dispatchers.IO) {
                                // Use filteredTickets to call the tickets
                                reportGenerator.generateTicketReport(context, filteredTickets)
                            }
                            if (uri != null) {
                                shareTicketReport(context, uri)
                            }
                        }
                    },
                    // Use filteredTickets to call the tickets
                    enabled = filteredTickets.isNotEmpty()
                ) {
                    Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Share Report")
                }
            }
        }

        // Filter Section
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = searchQuery,
                    // **FIX**: Use ticketViewModel
                    onValueChange = ticketViewModel::onSearchQueryChange,
                    label = { Text("Search by name, desc, or ID...") },
                    modifier = Modifier.fillMaxWidth()
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        modifier = Modifier.weight(1f),
                        selected = selectedStatus != null,
                        // Use ticketViewModel
                        onClick = { ticketViewModel.onStatusSelected(null) },
                        label = { Text(selectedStatus?.name ?: "All Statuses") },
                        trailingIcon = if (selectedStatus != null) {
                            { Icon(Icons.Default.Clear, "Clear") }
                        } else null
                    )
                    FilterChip(
                        modifier = Modifier.weight(1f),
                        selected = selectedPriority != null,
                        // Use ticketViewModel
                        onClick = { ticketViewModel.onPrioritySelected(null) },
                        label = { Text(selectedPriority?.name ?: "All Priorities") },
                        trailingIcon = if (selectedPriority != null) {
                            { Icon(Icons.Default.Clear, "Clear") }
                        } else null
                    )
                }
            }
        }

        // Ticket List
        if (filteredTickets.isEmpty()) {
            item {
                Text(
                    "No tickets found.",
                    modifier = Modifier.padding(top = 16.dp)
                )
            }
        } else {
            items(filteredTickets, key = { it.id }) { ticket ->
                TicketCard(ticket = ticket, onClick = { onTicketClick(ticket.id) })
            }
        }
    }

//    Column(modifier = modifier.fillMaxSize()) {
//        // The new filter section at the top
//        FilterSection(
//            searchQuery = searchQuery,
//            selectedStatus = selectedStatus,
//            selectedPriority = selectedPriority,
//            onQueryChange = ticketViewModel::onSearchQueryChange,
//            onStatusChange = ticketViewModel::onStatusSelected,
//            onPriorityChange = ticketViewModel::onPrioritySelected,
//            onClearFilters = ticketViewModel::clearFilters
//        )
//
//        // The list of tickets
//        if (filteredTickets.isEmpty()) {
//            Box(
//                modifier = Modifier.fillMaxSize(),
//                contentAlignment = Alignment.Center
//            ) {
//                Text(if (searchQuery.isNotBlank() || selectedStatus != null || selectedPriority != null) "No tickets match your filters." else "No open tickets found.")
//            }
//        } else {
//            LazyColumn(
//                modifier = Modifier.fillMaxSize(),
//                contentPadding = PaddingValues(16.dp),
//                verticalArrangement = Arrangement.spacedBy(12.dp)
//            ) {
//                items(filteredTickets, key = { it.id }) { ticket ->
//                    TicketCard(ticket = ticket, onClick = { onTicketClick(ticket.id) })
//                }
//            }
//        }
//    }
}

// Helper function to create and start the share intent
private fun shareTicketReport(context: Context, uri: Uri) {
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/csv"
        putExtra(Intent.EXTRA_STREAM, uri)
        putExtra(Intent.EXTRA_SUBJECT, "Ticket Report")
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    context.startActivity(Intent.createChooser(intent, "Share Ticket Report"))
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
            shape = RoundedCornerShape(25.dp),
            modifier = Modifier.fillMaxWidth().testTag("searchField")
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
                    enabled = false,
                    //readOnly = true,
                    label = { Text("Status") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = statusExpanded) },
                    shape = RoundedCornerShape(25.dp),
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
                    enabled = false,
                    //readOnly = true,
                    label = { Text("Priority") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = priorityExpanded) },
                    shape = RoundedCornerShape(25.dp),
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
