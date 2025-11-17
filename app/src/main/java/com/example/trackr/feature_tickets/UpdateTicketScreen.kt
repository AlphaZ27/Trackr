package com.example.trackr.feature_tickets


import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.trackr.domain.model.Priority
import com.example.trackr.domain.model.TicketStatus
import com.example.trackr.feature_tickets.ui.shared.StatusDropdown
import com.example.trackr.feature_tickets.ui.shared.PriorityDropdown

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UpdateTicketScreen(
    ticketId: String,
    onNavigateBack: () -> Unit,
    ticketViewModel: TicketViewModel = hiltViewModel()
) {
    val ticketState by ticketViewModel.selectedTicket.collectAsState()
    val detailState by ticketViewModel.detailState.collectAsState()

    var showDeleteDialog by remember { mutableStateOf(false) }

    // State variables for each editable field
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var department by remember { mutableStateOf("") }
    var assignee by remember { mutableStateOf("") }
    var resolution by remember { mutableStateOf("") }
    var priority by remember { mutableStateOf(Priority.Medium) }
    var status by remember { mutableStateOf(TicketStatus.Open) }

    // This effect runs once to fetch the ticket data
    LaunchedEffect(key1 = ticketId) {
        ticketViewModel.getTicketById(ticketId)
    }

    // This effect updates the local state whenever the fetched ticket changes
    LaunchedEffect(key1 = ticketState) {
        ticketState?.let { ticket ->
            name = ticket.name
            description = ticket.description
            department = ticket.department
            assignee = ticket.assignee
            resolution = ticket.resolutionDescription
            priority = ticket.priority
            status = ticket.status
        }
    }

    LaunchedEffect(key1 = detailState) {
        if (detailState is TicketDetailState.Success) {
            onNavigateBack()
        }
    }

    // ********** Update Ticket Form **********
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("#${ticketId.take(6).uppercase()}") },
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
        },
        floatingActionButton = {
            Button(
                onClick = {
                    ticketState?.let {
                        val updatedTicket = it.copy(
                            name = name,
                            description = description,
                            department = department,
                            assignee = assignee,
                            resolutionDescription = resolution,
                            priority = priority,
                            status = status
                        )
                        ticketViewModel.updateTicket(updatedTicket)
                    }
                },
                enabled = detailState !is TicketDetailState.Loading
            ) {
                Text("Save Changes")
            }
        }
    ) { paddingValues ->
        if (detailState is TicketDetailState.Loading && ticketState == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name / Title") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = department,
                    onValueChange = { department = it },
                    label = { Text("Department") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                )
                PriorityDropdown(
                    selectedPriority = priority,
                    onPrioritySelected = { priority = it }
                )
                StatusDropdown(
                    selectedStatus = status,
                    onStatusSelected = { status = it }
                )
                OutlinedTextField(
                    value = assignee,
                    onValueChange = { assignee = it },
                    label = { Text("Assignee") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = resolution,
                    onValueChange = { resolution = it },
                    label = { Text("Resolution") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                )
            }
        }
    }

    // ********** Delete Ticket Dialog **********
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Ticket?") },
            text = { Text("Are you sure you want to permanently delete this ticket? This action cannot be undone.") },
            confirmButton = {
                Button(
                    onClick = {
                        ticketViewModel.deleteTicket(ticketId)
                        showDeleteDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("Cancel") }
            }
        )
    }
}
