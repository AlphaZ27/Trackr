package com.example.trackr.feature_tickets.ui.shared

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.material3.ExposedDropdownMenuAnchorType.Companion.PrimaryNotEditable
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.example.trackr.domain.model.Priority
import com.example.trackr.domain.model.TicketStatus
import com.example.trackr.domain.model.User
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

// A hardcoded list of categories for the app
//val ticketCategories = listOf("General", "IT Services", "Hardware", "Software", "OnBoarding", "Networking & VPN")

// Helper functions to make the enum names look nice in the UI
//fun Priority.displayName(): String = this.name
//fun TicketStatus.displayName(): String = this.name.replace("([a-z])([A-Z])".toRegex(), "$1 $2")

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
            value = selectedPriority.displayName(),
            onValueChange = {},
            readOnly = true,
            enabled = false, // Use disabled to make the whole field clickable
            label = { Text("Priority") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(
                disabledTextColor = MaterialTheme.colorScheme.onSurface,
                disabledBorderColor = MaterialTheme.colorScheme.outline,
                disabledLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
            ),
            modifier = Modifier
                .menuAnchor(PrimaryNotEditable)
                .fillMaxWidth()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            priorities.forEach { priority ->
                DropdownMenuItem(
                    text = { Text(priority.displayName()) },
                    onClick = {
                        onPrioritySelected(priority)
                        expanded = false
                    },
                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
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
    val statuses = TicketStatus.entries.toTypedArray()

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = selectedStatus.displayName(),
            onValueChange = {},
            readOnly = true,
            enabled = false,
            label = { Text("Status") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(
                disabledTextColor = MaterialTheme.colorScheme.onSurface,
                disabledBorderColor = MaterialTheme.colorScheme.outline,
                disabledLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
            ),
            modifier = Modifier
                    .menuAnchor(PrimaryNotEditable)
                .fillMaxWidth()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            statuses.forEach { status ->
                DropdownMenuItem(
                    text = { Text(status.displayName()) },
                    onClick = {
                        onStatusSelected(status)
                        expanded = false
                    },
                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                )
            }
        }
    }
}

// Category dropdown
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryDropdown(
    categories: List<String>,
    selectedCategory: String,
    onCategorySelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = selectedCategory,
            onValueChange = {},
            readOnly = true,
            enabled = false, // Must be false for the dropdown to work nicely with click
            label = { Text("Category") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(
                disabledTextColor = MaterialTheme.colorScheme.onSurface,
                disabledBorderColor = MaterialTheme.colorScheme.outline,
                disabledLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
            ),
            modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable).fillMaxWidth()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            categories.forEach { category ->
                DropdownMenuItem(
                    text = { Text(category) },
                    onClick = {
                        onCategorySelected(category)
                        expanded = false
                    }
                )
            }
        }
    }
}

// Assignee dropdown
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssigneeDropdown(
    allUsers: List<User>,
    selectedUser: User?,
    onUserSelected: (User?) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = selectedUser?.name ?: "Unassigned",
            onValueChange = {},
            readOnly = true,
            label = { Text("Assignee") },
            trailingIcon = {
                // Add a clear button
                if (selectedUser != null) {
                    IconButton(onClick = { onUserSelected(null) }) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear Selection")
                    }
                } else {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                }
            },
            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(
                disabledTextColor = MaterialTheme.colorScheme.onSurface,
                disabledBorderColor = MaterialTheme.colorScheme.outline,
                disabledLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
            ),
            modifier = Modifier
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                .fillMaxWidth()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            DropdownMenuItem(
                text = { Text("Unassigned") },
                onClick = {
                    onUserSelected(null)
                    expanded = false
                }
            )
            allUsers.forEach { user ->
                DropdownMenuItem(
                    text = { Text(user.name) },
                    onClick = {
                        onUserSelected(user)
                        expanded = false
                    }
                )
            }
        }
    }
}

// --- Helper Functions for Colors ---

@Composable
fun TicketStatus.toColor(): Color {
    return when (this) {
        TicketStatus.Open -> MaterialTheme.colorScheme.error
        TicketStatus.InProgress -> Color(0xFFFFA500) // Orange
        TicketStatus.Closed -> Color(0xFF008000) // Green
    }
}

@Composable
fun Priority.toColor(): Color {
    return when (this) {
        Priority.Low -> Color.Gray
        Priority.Medium -> MaterialTheme.colorScheme.primary
        Priority.High -> Color(0xFFFF9800) // Orange
        Priority.Urgent -> MaterialTheme.colorScheme.error
    }
}

// --- Helper Function for Display Names ---
fun TicketStatus.displayName(): String {
    return when (this) {
        TicketStatus.Open -> "Open"
        TicketStatus.InProgress -> "In Progress"
        TicketStatus.Closed -> "Closed"
    }
}

fun Priority.displayName(): String {
    return when (this) {
        Priority.Low -> "Low"
        Priority.Medium -> "Medium"
        Priority.High -> "High"
        Priority.Urgent -> "Urgent"
    }
}


// **TICKET DETAIL CARD**
@Composable
fun TicketDetailCard(
    label: String,
    value: String,
    valueColor: Color = Color.Unspecified // Default to local text color
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = if (valueColor == Color.Unspecified) {
                    MaterialTheme.colorScheme.onSurface
                } else {
                    valueColor
                }
            )
        }
    }
}