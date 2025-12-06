package com.example.trackr.feature_tickets

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.trackr.domain.model.Ticket
import com.example.trackr.feature_kb.ArticleCard
import com.example.trackr.feature_tickets.ui.shared.CategoryDropdown
import com.example.trackr.feature_tickets.ui.shared.PriorityDropdown
import com.example.trackr.feature_tickets.ui.shared.StatusDropdown
import com.example.trackr.feature_tickets.ui.shared.AssigneeDropdown


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateTicketScreen(
    viewModel: CreateTicketViewModel = hiltViewModel(),
    onTicketCreated: () -> Unit,
    onNavigateBack: () -> Unit,
    onNavigateToArticle: (String) -> Unit
) {

    val suggestedArticles by viewModel.suggestedArticles.collectAsState()
    val createState by viewModel.createState.collectAsState()
    val potentialDuplicates by viewModel.potentialDuplicates.collectAsState()
    val users by viewModel.users.collectAsState()
    val categoryList by viewModel.categories.collectAsState()

    val context = LocalContext.current

    // React to save success
    LaunchedEffect(createState) {
        if (createState is CreateState.Success) {
            Toast.makeText(
                context,
                "Ticket created successfully!",
                Toast.LENGTH_SHORT).show()
            onNavigateBack()
        }
    }


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
                    viewModel.createTicket()
                },
                enabled = viewModel.name.value.isNotBlank() &&
                        viewModel.description.value.isNotBlank() &&
                        viewModel.department.value.isNotBlank()
            ) {
                Text("Save Ticket")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier.padding(paddingValues).padding(16.dp).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                // Name Field
                value = viewModel.name.value,
                onValueChange = { viewModel.name.value = it },
                label = { Text("Ticket Name / Title") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                // Department Field
                value = viewModel.department.value,
                onValueChange = { viewModel.department.value = it },
                label = { Text("Department (e.g., IT, HR)") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                // Description Field
                value = viewModel.description.value,
                onValueChange = { viewModel.description.value = it },
                label = { Text("Description") },
                modifier = Modifier.fillMaxWidth().height(150.dp)
            )

            // Potential Duplicates Warning Section
            if (potentialDuplicates.isNotEmpty()) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.onErrorContainer)
                            Spacer(Modifier.width(8.dp))
                            Text(
                                "Potential Duplicates Found!",
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(Modifier.height(8.dp))
                        potentialDuplicates.forEach { ticket ->
                            DuplicateTicketItem(ticket)
                        }
                    }
                }
            }

            // Smart Assign Row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    AssigneeDropdown(
                        allUsers = users,
                        selectedUser = viewModel.assignee.value,
                        onUserSelected = { viewModel.assignee.value = it }
                    )
                }
                Spacer(Modifier.width(8.dp))
                // The Smart Assign Button
                IconButton(
                    onClick = { viewModel.autoAssignBestTechnician() },
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                ) {
                    Icon(Icons.Default.AutoFixHigh, contentDescription = "Auto Assign Best Available")
                }
            }

            // Assignee Dropdown
            val users by viewModel.users.collectAsState()
            AssigneeDropdown(
                allUsers = users,
                selectedUser = viewModel.assignee.value,
                onUserSelected = { viewModel.assignee.value = it }
            )

            OutlinedTextField(
                // Resolution Field
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
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            }

            // Category Dropdown
            CategoryDropdown(
                categories = categoryList, // Pass the list
                selectedCategory = viewModel.category.value,
                onCategorySelected = { viewModel.category.value = it }
            )

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

@Composable
fun DuplicateTicketItem(ticket: Ticket) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .border(1.dp, MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.5f), MaterialTheme.shapes.medium)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(ticket.name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
            Text(
                "Status: ${ticket.status.name} • Assigned: ${if(ticket.assignee.isEmpty()) "None" else "..."}",
                style = MaterialTheme.typography.labelSmall
            )
        }
    }
}