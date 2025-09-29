package com.example.trackr.feature_tickets

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.trackr.domain.model.Ticket
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TicketDetailScreen(
    ticketId: String,
    ticketViewModel: TicketViewModel = hiltViewModel(),
    onNavigateToUpdate: (String) -> Unit,
    onNavigateBack: () -> Unit
) {
    val ticket by ticketViewModel.selectedTicket.collectAsState()
    var showDeleteDialog by remember { mutableStateOf(false) }

    // Fetch the ticket details when the screen is first launched
    LaunchedEffect(key1 = ticketId) {
        ticketViewModel.getTicketById(ticketId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "Ticket #${ticketId.take(6).uppercase()}") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete Ticket")
                    }
                }
            )
        }
    ) { paddingValues ->
        // Show loading indicator while ticket is being fetched
        if (ticket == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            TicketDetailContent(
                modifier = Modifier.padding(paddingValues),
                ticket = ticket!!,
                onSaveChanges = { updatedTicket ->
                    ticketViewModel.updateTicket(updatedTicket)
                    onNavigateBack()
                },
                onNavigateToUpdate = onNavigateToUpdate
            )
        }
    }

    if (showDeleteDialog) {
        DeleteConfirmationDialog(
            onConfirm = {
                ticketViewModel.deleteTicket(ticketId)
                showDeleteDialog = false
                onNavigateBack()
            },
            onDismiss = { showDeleteDialog = false }
        )
    }
}

@Composable
fun TicketDetailContent(
    modifier: Modifier = Modifier,
    ticket: Ticket,
    onSaveChanges: (Ticket) -> Unit,
    onNavigateToUpdate: (String) -> Unit
) {
    var currentStatus by remember { mutableStateOf(ticket.status) }
    var currentPriority by remember { mutableStateOf(ticket.priority) }
    val dateFormatter = remember { SimpleDateFormat("MMM dd, yyyy, hh:mm a", Locale.getDefault()) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) { // Have to add assignee and resolution
        Text("Title", style = MaterialTheme.typography.labelMedium)
        Text(ticket.name, style = MaterialTheme.typography.bodyLarge)

        Text("Department", style = MaterialTheme.typography.labelMedium)
        Text(ticket.department, style = MaterialTheme.typography.bodyLarge)

        Text("Description", style = MaterialTheme.typography.labelMedium)
        Text(ticket.description, style = MaterialTheme.typography.bodyLarge)

        Text("Assignee", style = MaterialTheme.typography.labelMedium)
        Text(ticket.assignee, style = MaterialTheme.typography.bodyLarge)

        Text("Resolution", style = MaterialTheme.typography.labelMedium)
        Text(ticket.resolutionDescription, style = MaterialTheme.typography.bodyLarge)

        // The priority editable field
        PriorityDropdown(
            selectedPriority = currentPriority,
            onPrioritySelected = { currentPriority = it }
        )

        Text("Created Date", style = MaterialTheme.typography.labelMedium)
        Text(dateFormatter.format(ticket.createdDate.toDate()))

        // The status editable field
        StatusDropdown(
            selectedStatus = currentStatus,
            onStatusSelected = { currentStatus = it }
        )

        Spacer(modifier = Modifier.weight(1.0f)) // Pushes button to the bottom

        Button( // This button saves the changes to the ticket on this screen and update ticket screen
            onClick = {
                val updatedTicket = ticket.copy(
                    priority = currentPriority,
                    status = currentStatus
                )
                onSaveChanges(updatedTicket)
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = currentStatus != ticket.status || currentPriority != ticket.priority // Only enable if changes were made
        ) {
            Text("Save Quick Changes")
        }
        Button(
            onClick = { onNavigateToUpdate(ticket.id) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Edit Ticket")
        }
    }
}

@Composable
fun DeleteConfirmationDialog(onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Delete Ticket") },
        text = { Text("Are you sure you want to permanently delete this ticket? This action cannot be undone.") },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Text("Delete")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}