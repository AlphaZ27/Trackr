package com.example.trackr.feature_tickets

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.trackr.domain.model.Priority
import com.example.trackr.domain.model.Ticket
import com.example.trackr.domain.model.TicketStatus
import com.example.trackr.feature_tickets.TicketViewModel
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

    Column(modifier = modifier.fillMaxSize()) {
        // The filter section with dropdowns
        FilterSection(
            searchQuery = searchQuery,
            selectedStatus = selectedStatus,
            selectedPriority = selectedPriority,
            onQueryChange = ticketViewModel::onSearchQueryChange,
            onStatusChange = ticketViewModel::onStatusSelected,
            onPriorityChange = ticketViewModel::onPrioritySelected,
            onClearFilters = ticketViewModel::clearFilters
        )

        // Row for "Share Report" button
//        Row(
//            modifier = Modifier
//                .fillMaxWidth()
//                .padding(horizontal = 16.dp, vertical = 8.dp),
//            verticalAlignment = Alignment.CenterVertically,
//            horizontalArrangement = Arrangement.End
//        ) {
//            OutlinedButton(
//                onClick = {
//                    scope.launch {
//                        val uri = withContext(Dispatchers.IO) {
//                            reportGenerator.generateTicketReport(context, filteredTickets)
//                        }
//                        if (uri != null) {
//                            shareTicketReport(context, uri)
//                        }
//                    }
//                },
//                enabled = filteredTickets.isNotEmpty()
//            ) {
//                Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
//                Spacer(Modifier.width(8.dp))
//                Text("Share Report")
//            }
//        }

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
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filteredTickets, key = { it.id }) { ticket ->
                    // **FIX**: Use TicketCard, not TicketItem
                    TicketCard(ticket = ticket, onClick = { onTicketClick(ticket.id) })
                }
            }
        }
    }
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
                    readOnly = true,
                    enabled = false,
                    label = { Text("Status") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = statusExpanded) },
                    shape = RoundedCornerShape(25.dp),
                    colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(
                        disabledTextColor = MaterialTheme.colorScheme.onSurface,
                        disabledBorderColor = MaterialTheme.colorScheme.outline,
                        disabledLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    ),
                    modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
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
                    enabled = false,
                    label = { Text("Priority") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = priorityExpanded) },
                    shape = RoundedCornerShape(25.dp),
                    colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(
                        disabledTextColor = MaterialTheme.colorScheme.onSurface,
                        disabledBorderColor = MaterialTheme.colorScheme.outline,
                        disabledLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    ),
                    modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                )
                ExposedDropdownMenu(
                    expanded = priorityExpanded,
                    onDismissRequest = { priorityExpanded = false }
                ) {
                    DropdownMenuItem(text = { Text("All Priorities") }, onClick = { onPriorityChange(null); priorityExpanded = false })
                    Priority.entries.forEach { priority ->
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

    val statusColor = when (ticket.status) {
        TicketStatus.Open -> MaterialTheme.colorScheme.primary
        TicketStatus.InProgress -> Color(0xFFFFA000) // Amber
        TicketStatus.Closed -> Color(0xFF4CAF50) // Green
    }

    val dateFormatter = remember { SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp), // Softer corners
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant) // Subtle border
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
//                Text(
//                    text = ticket.department.uppercase(),
//                    style = MaterialTheme.typography.bodySmall,
//                    fontWeight = FontWeight.Bold
//                )
                // Small Status Badge
                Surface(
                    color = statusColor.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(50),
                    border = BorderStroke(1.dp, statusColor.copy(alpha = 0.5f))
                ) {
                    Text(
                        text = ticket.status.name,
                        style = MaterialTheme.typography.labelSmall,
                        color = statusColor,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
            Spacer(Modifier.height(8.dp))

            // Name / Title
            Text(
                text = ticket.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = ticket.department,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(4.dp))

            // Description
            Text(
                text = ticket.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(12.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(8.dp))

            // Bottom Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Flag, // Or a circle shape
                        contentDescription = "Priority",
                        tint = priorityColor,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = ticket.priority.name,
                        style = MaterialTheme.typography.labelMedium,
                        color = priorityColor,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Text(
                    text = dateFormatter.format(ticket.createdDate.toDate()),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
