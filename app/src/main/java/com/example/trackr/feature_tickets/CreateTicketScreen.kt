package com.example.trackr.feature_tickets

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.trackr.domain.model.Priority
import com.example.trackr.domain.model.Ticket
import com.example.trackr.domain.model.TicketStatus

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateTicketScreen(
    ticketViewModel: TicketViewModel = hiltViewModel(),
    onTicketCreated: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var department by remember { mutableStateOf("") }
    var priority by remember { mutableStateOf(Priority.Medium) }
    var status by remember { mutableStateOf(TicketStatus.Open) }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Create New Ticket") })
        },
        floatingActionButton = {
            Button(
                onClick = {
                    val newTicket = Ticket(
                        name = name,
                        description = description,
                        department = department,
                        priority = priority,
                        status = status
                    )
                    ticketViewModel.createTicket(newTicket)
                    onTicketCreated()
                },
                enabled = name.isNotBlank() && description.isNotBlank() && department.isNotBlank()
            ) {
                Text("Save Ticket")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier.padding(paddingValues).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Ticket Name / Title") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = department,
                onValueChange = { department = it },
                label = { Text("Department (e.g., IT, HR)") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description") },
                modifier = Modifier.fillMaxWidth().height(150.dp)
            )

            // Priority Dropdown
            PriorityDropdown(
                selectedPriority = priority,
                onPrioritySelected = { priority = it }
            )

            // Status Dropdown
            StatusDropdown(
                selectedStatus = status,
                onStatusSelected = { status = it }
            )
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PriorityDropdown(
    selectedPriority: Priority,
    onPrioritySelected: (Priority) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val priorities = Priority.values()

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = selectedPriority.name,
            onValueChange = {},
            readOnly = true,
            label = { Text("Priority") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.fillMaxWidth().clickable(onClick = { expanded = true })
            //modifier = Modifier.menuAnchor().fillMaxWidth()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            priorities.forEach { priority ->
                DropdownMenuItem(
                    text = { Text(priority.name) },
                    onClick = {
                        onPrioritySelected(priority)
                        expanded = false
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatusDropdown(
    selectedStatus: TicketStatus,
    onStatusSelected: (TicketStatus) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val statuses = TicketStatus.values()

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = selectedStatus.name,
            onValueChange = {},
            readOnly = true,
            label = { Text("Status") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.fillMaxWidth().clickable(onClick = { expanded = true })
            //modifier = Modifier.menuAnchor().fillMaxWidth()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            statuses.forEach { status ->
                DropdownMenuItem(
                    text = { Text(status.name) },
                    onClick = {
                        onStatusSelected(status)
                        expanded = false
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateTicketScreenPreviewable(onTicketCreated: () -> Unit = {}) {
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var department by remember { mutableStateOf("") }
    var priority by remember { mutableStateOf(Priority.Medium) }
    var status by remember { mutableStateOf(TicketStatus.Open) }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Create New Ticket") }) },
        floatingActionButton = {
            Button(
                onClick = { onTicketCreated() },
                enabled = name.isNotBlank() && description.isNotBlank() && department.isNotBlank()
            ) {
                Text("Save Ticket")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier.padding(paddingValues).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Ticket Name / Title") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = department, onValueChange = { department = it }, label = { Text("Department (e.g., IT, HR)") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Description") }, modifier = Modifier.fillMaxWidth().height(150.dp))

            PriorityDropdown(selectedPriority = priority, onPrioritySelected = { priority = it })
            StatusDropdown(selectedStatus = status, onStatusSelected = { status = it })
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewCreateTicketScreen() {
    CreateTicketScreenPreviewable()
}

