package com.example.trackr.feature_tickets


import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import com.example.trackr.feature_tickets.ui.shared.AssigneeDropdown
import com.example.trackr.feature_tickets.ui.shared.CategoryDropdown
import com.example.trackr.feature_tickets.ui.shared.PriorityDropdown
import com.example.trackr.feature_tickets.ui.shared.StatusDropdown

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UpdateTicketScreen(
    ticketId: String,
    onNavigateBack: () -> Unit,
    viewModel: UpdateTicketViewModel = hiltViewModel()
) {
    val updateState by viewModel.updateState.collectAsState()
    val users by viewModel.users.collectAsState()
    val categoryList by viewModel.categories.collectAsState() // Observe categories

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
//    LaunchedEffect(key1 = ticketId) {
//        viewModel.getTicketById(ticketId)
//    }
//
//    // This effect updates the local state whenever the fetched ticket changes
//    LaunchedEffect(key1 = ticketState) {
//        ticketState?.let { ticket ->
//            name = ticket.name
//            description = ticket.description
//            department = ticket.department
//            assignee = ticket.assignee
//            resolution = ticket.resolutionDescription
//            priority = ticket.priority
//            status = ticket.status
//        }
//    }

    LaunchedEffect(key1 = updateState) {
        if (updateState is UpdateState.Success) {
            onNavigateBack()
        }
        // You can also add a toast for UpdateState.Error here
    }

    // ********** Update Ticket Form **********
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Update Ticket") },
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
                onClick = { viewModel.updateTicket() },
                enabled = updateState !is UpdateState.Loading
            ) {
                Text("Save Changes")
            }
        }
    ) { paddingValues ->
        if (updateState is UpdateState.Loading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = viewModel.name.value,
                onValueChange = { viewModel.name.value = it },
                label = { Text("Name / Title") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = viewModel.department.value,
                onValueChange = { viewModel.department.value = it },
                label = { Text("Department") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = viewModel.description.value,
                onValueChange = { viewModel.description.value = it },
                label = { Text("Description") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
            )
            CategoryDropdown(
                categories = categoryList, // Pass the list
                selectedCategory = viewModel.category.value,
                onCategorySelected = { viewModel.category.value = it }
            )
            PriorityDropdown(
                selectedPriority = viewModel.priority.value,
                onPrioritySelected = { viewModel.priority.value = it }
            )
            StatusDropdown(
                selectedStatus = viewModel.status.value,
                onStatusSelected = { viewModel.status.value = it }
            )
            AssigneeDropdown(
                allUsers = users,
                selectedUser = viewModel.assignee.value,
                onUserSelected = { viewModel.assignee.value = it }
            )
            OutlinedTextField(
                value = viewModel.resolution.value,
                onValueChange = { viewModel.resolution.value = it },
                label = { Text("Resolution") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
            )
        }
    }

    // ********** Delete Ticket Dialog **********
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Ticket?") },
            text = { Text("Are you sure you want to permanently delete this ticket?") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteTicket()
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
