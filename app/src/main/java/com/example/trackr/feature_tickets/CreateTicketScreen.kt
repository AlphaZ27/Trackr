package com.example.trackr.feature_tickets

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import com.example.trackr.feature_tickets.ui.shared.StatusDropdown
import com.example.trackr.feature_tickets.ui.shared.PriorityDropdown
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.trackr.domain.model.Priority
import com.example.trackr.domain.model.Ticket
import com.example.trackr.domain.model.TicketStatus
import com.example.trackr.feature_kb.ArticleCard


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateTicketScreen(
    viewModel: CreateTicketViewModel = hiltViewModel(),
    onTicketCreated: () -> Unit,
    onNavigateBack: () -> Unit,
    onNavigateToArticle: (String) -> Unit
) {
//    var name by remember { mutableStateOf("") }
//    var description by remember { mutableStateOf("") }
//    var department by remember { mutableStateOf("") }
//    var assignee by remember { mutableStateOf("") }
//    var resolution by remember { mutableStateOf("") }
//    var priority by remember { mutableStateOf(Priority.Medium) }
//    var status by remember { mutableStateOf(TicketStatus.Open) }

    val suggestedArticles by viewModel.suggestedArticles.collectAsState()


    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Create New Ticket") },
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            Button(
                onClick = {
//                    val newTicket = Ticket(
//                        name = name,
//                        description = description,
//                        department = department,
//                        assignee = assignee,
//                        resolutionDescription = resolution,
//                        priority = priority,
//                        status = status
//                    )
                    viewModel.createTicket(onTicketCreated)
                    //onTicketCreated()
                },
                enabled = viewModel.name.value.isNotBlank() && viewModel.description.value.isNotBlank() && viewModel.department.value.isNotBlank()
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
                value = viewModel.name.value,
                onValueChange = { viewModel.name.value = it },
                label = { Text("Ticket Name / Title") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = viewModel.department.value,
                onValueChange = { viewModel.department.value = it },
                label = { Text("Department (e.g., IT, HR)") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = viewModel.description.value,
                onValueChange = { viewModel.description.value = it },
                label = { Text("Description") },
                modifier = Modifier.fillMaxWidth().height(150.dp)
            )
            OutlinedTextField(
                value = viewModel.assignee.value,
                onValueChange = { viewModel.assignee.value = it },
                label = { Text("Assignee") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = viewModel.resolution.value,
                onValueChange = { viewModel.resolution.value = it },
                label = { Text("Resolution") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
            )

            // Suggested Articles Section
            if (suggestedArticles.isNotEmpty()) {
                Text("Suggested Articles", style = MaterialTheme.typography.titleMedium)
                suggestedArticles.forEach { article ->
                    ArticleCard(article = article, onClick = { onNavigateToArticle(article.id) })
                }
                Spacer(modifier = Modifier.padding(vertical = 8.dp))
            }

            // Priority Dropdown
            PriorityDropdown(
                selectedPriority = viewModel.priority.value,
                onPrioritySelected = { viewModel.priority.value = it }
            )

            // Status Dropdown
            StatusDropdown(
                selectedStatus = viewModel.status.value,
                onStatusSelected = { viewModel.status.value = it }
            )
        }
    }
}
//
//fun Priority.displayName(): String = name.replaceFirstChar { it.uppercase() }
//fun TicketStatus.displayName(): String = name.replace(Regex("([a-z])([A-Z])"), "$1 $2")
//    .replaceFirstChar { it.uppercase() }
//
//
//
//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun PriorityDropdown(
//    selectedPriority: Priority,
//    onPrioritySelected: (Priority) -> Unit
//) {
//    var expanded by remember { mutableStateOf(false) }
//    val priorities = Priority.values()
//
//    ExposedDropdownMenuBox(
//        expanded = expanded,
//        onExpandedChange = { expanded = !expanded }
//    ) {
//        OutlinedTextField(
//            modifier = Modifier // .menuAnchor() by itself is deprecated and won't work
//                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable) // read-only
//                .fillMaxWidth(),
//            value = selectedPriority.name,
//            onValueChange = {},
//            enabled = false,
//            //readOnly = true,
//            label = { Text("Priority") },
//            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
//            //Prevents the OutlinedTextField from being greyed out
//            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(
//                disabledTextColor = MaterialTheme.colorScheme.onSurface,
//                disabledBorderColor = MaterialTheme.colorScheme.outline,
//                disabledLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
//                disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
//                disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
//        )
//
//            //modifier = Modifier.fillMaxWidth().clickable(onClick = { expanded = true })
//            //modifier = Modifier.menuAnchor().fillMaxWidth()
//        )
//        ExposedDropdownMenu(
//            expanded = expanded,
//            onDismissRequest = { expanded = false },
//            modifier = Modifier.exposedDropdownSize() // matches width with text field
//        ) {
//            priorities.forEach { priority ->
//                DropdownMenuItem(
//                    text = { Text(priority.displayName()) },
//                    onClick = {
//                        onPrioritySelected(priority)
//                        expanded = false
//                    }
//                )
//            }
//        }
//    }
//}
//
//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun StatusDropdown(
//    selectedStatus: TicketStatus,
//    onStatusSelected: (TicketStatus) -> Unit
//) {
//    var expanded by remember { mutableStateOf(false) }
//    val statuses = TicketStatus.values()
//
//    ExposedDropdownMenuBox(
//        expanded = expanded,
//        onExpandedChange = { expanded = !expanded }
//    ) {
//        OutlinedTextField(
//            value = selectedStatus.name,
//            onValueChange = {},
//            readOnly = true,
//            label = { Text("Status") },
//            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
//            modifier = Modifier
//                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
//                .fillMaxWidth()
//            //modifier = Modifier.fillMaxWidth().clickable(onClick = { expanded = true })
//            //modifier = Modifier.menuAnchor().fillMaxWidth()
//        )
//        ExposedDropdownMenu(
//            expanded = expanded,
//            onDismissRequest = { expanded = false },
//            modifier = Modifier.exposedDropdownSize() // matches width with text field
//        ) {
//            statuses.forEach { status ->
//                DropdownMenuItem(
//                    text = { Text(status.displayName()) },
//                    onClick = {
//                        onStatusSelected(status)
//                        expanded = false
//                    }
//                )
//            }
//        }
//    }
//}
//
//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun CreateTicketScreenPreviewable(onTicketCreated: () -> Unit = {}) {
//    var name by remember { mutableStateOf("") }
//    var description by remember { mutableStateOf("") }
//    var department by remember { mutableStateOf("") }
//    var priority by remember { mutableStateOf(Priority.Medium) }
//    var status by remember { mutableStateOf(TicketStatus.Open) }
//
//    Scaffold(
//        topBar = { TopAppBar(title = { Text("Create New Ticket") }) },
//        floatingActionButton = {
//            Button(
//                onClick = { onTicketCreated() },
//                enabled = name.isNotBlank() && description.isNotBlank() && department.isNotBlank()
//            ) {
//                Text("Save Ticket")
//            }
//        }
//    ) { paddingValues ->
//        Column(
//            modifier = Modifier.padding(paddingValues).padding(16.dp),
//            verticalArrangement = Arrangement.spacedBy(12.dp)
//        ) {
//            OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Ticket Name / Title") }, modifier = Modifier.fillMaxWidth())
//            OutlinedTextField(value = department, onValueChange = { department = it }, label = { Text("Department (e.g., IT, HR)") }, modifier = Modifier.fillMaxWidth())
//            OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Description") }, modifier = Modifier.fillMaxWidth().height(150.dp))
//
//            PriorityDropdown(selectedPriority = priority, onPrioritySelected = { priority = it })
//            StatusDropdown(selectedStatus = status, onStatusSelected = { status = it })
//        }
//    }
//}

// Doing the preview for the CreateTicketScreen this way actually allows it to build
//@Preview(showBackground = true)
//@Composable
//fun PreviewCreateTicketScreen() {
//    CreateTicketScreenPreviewable()
//}

