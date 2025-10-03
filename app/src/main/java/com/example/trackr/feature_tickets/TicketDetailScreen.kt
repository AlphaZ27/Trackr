package com.example.trackr.feature_tickets

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.trackr.domain.model.Ticket
import java.text.SimpleDateFormat
import com.example.trackr.feature_kb.ArticleCard
import com.example.trackr.feature_tickets.ui.shared.PriorityDropdown
import com.example.trackr.feature_tickets.ui.shared.StatusDropdown
import java.util.*


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TicketDetailScreen(
    ticketId: String,
    ticketViewModel: TicketViewModel = hiltViewModel(),
    onNavigateToUpdate: (String) -> Unit,
    onNavigateToArticle: (String) -> Unit,
    onNavigateBack: () -> Unit
) {
    val ticket by ticketViewModel.selectedTicket.collectAsState()
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showLinkDialog by remember { mutableStateOf(false) }

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
                    IconButton(onClick = { onNavigateToUpdate(ticketId) }) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit Ticket")
                    }
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
                onNavigateToArticle = onNavigateToArticle,
                onLinkArticleClick = { showLinkDialog = true } // Show the dialog when the link button is clicked
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

    // Show the new dialog for linking articles
    if (showLinkDialog) {
        LinkArticleDialog(
            viewModel = ticketViewModel,
            onDismiss = { showLinkDialog = false },
            onArticleSelected = { articleId ->
                ticketViewModel.linkArticle(ticketId, articleId)
                showLinkDialog = false
            }
        )
    }
}

@Composable
fun TicketDetailContent(
    modifier: Modifier = Modifier,
    ticket: Ticket,
    onSaveChanges: (Ticket) -> Unit,
    onNavigateToArticle: (String) -> Unit,
    onLinkArticleClick: () -> Unit // New callback for the button
) {

    // Inject the ViewModel to get the linked articles
    val ticketViewModel: TicketViewModel = hiltViewModel()
    val linkedArticles by ticketViewModel.linkedArticles.collectAsState()

    var currentStatus by remember { mutableStateOf(ticket.status) }
    var currentPriority by remember { mutableStateOf(ticket.priority) }
    val dateFormatter = remember { SimpleDateFormat("MMM dd, yyyy, hh:mm a", Locale.getDefault()) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
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

        Text("Created Date", style = MaterialTheme.typography.labelMedium)
        Text(dateFormatter.format(ticket.createdDate.toDate()))

        Spacer(modifier = Modifier.padding(vertical = 8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Linked Articles", style = MaterialTheme.typography.titleMedium)
            TextButton(onClick = onLinkArticleClick) {
                Text("Link Article")
            }
        }
        if (linkedArticles.isEmpty()) {
            Text("No articles linked yet.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                linkedArticles.forEach { article ->
                    ArticleCard(article = article, onClick = { onNavigateToArticle(article.id) })
                }
            }
        }
        Spacer(modifier = Modifier.padding(vertical = 8.dp))

        // The priority editable field
        PriorityDropdown(
            selectedPriority = currentPriority,
            onPrioritySelected = { currentPriority = it }
        )

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
    }

    // Linked Articles Section
//    Spacer(modifier = Modifier.padding(vertical = 8.dp))
//    Row(
//        modifier = Modifier.fillMaxWidth(),
//        horizontalArrangement = Arrangement.SpaceBetween,
//        verticalAlignment = Alignment.CenterVertically
//    ) {
//        Text("Linked Articles", style = MaterialTheme.typography.titleMedium)
//        TextButton(onClick = onLinkArticleClick) {
//            Text("Link Article")
//        }
//    }
//    if (linkedArticles.isEmpty()) {
//        Text("No articles linked yet.", style = MaterialTheme.typography.bodySmall)
//    } else {
//        linkedArticles.forEach { article ->
//            ArticleCard(article = article, onClick = { /* TODO: Navigate to article */ })
//            Spacer(modifier = Modifier.height(8.dp))
//        }
//    }
//    Spacer(modifier = Modifier.padding(vertical = 8.dp))
}

@Composable
private fun LinkArticleDialog(
    viewModel: TicketViewModel,
    onDismiss: () -> Unit,
    onArticleSelected: (String) -> Unit
) {
    val articles by viewModel.searchableArticles.collectAsState()
    val searchQuery by viewModel.kbSearchQuery.collectAsState()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Link Knowledge Base Article") },
        text = {
            Column {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = viewModel::onKbSearchQueryChange,
                    label = { Text("Search articles...") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(articles, key = { it.id }) { article ->
                        Text(
                            text = article.title,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onArticleSelected(article.id) }
                                .padding(vertical = 8.dp)
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Close") }
        }
    )
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