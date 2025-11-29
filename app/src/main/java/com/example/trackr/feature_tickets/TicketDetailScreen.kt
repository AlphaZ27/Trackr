package com.example.trackr.feature_tickets

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.trackr.domain.model.KBArticle
import com.example.trackr.domain.model.Ticket
import com.example.trackr.domain.model.TicketStatus
import com.example.trackr.feature_kb.ArticleCard
import com.example.trackr.feature_tickets.TicketDetailState
import com.example.trackr.feature_tickets.TicketViewModel
import com.example.trackr.feature_tickets.ui.shared.TicketDetailCard
import com.example.trackr.feature_tickets.ui.shared.displayName
import com.example.trackr.feature_tickets.ui.shared.toColor
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TicketDetailScreen(
    ticketId: String,
    onNavigateBack: () -> Unit,
    onNavigateToEdit: (String) -> Unit,
    onNavigateToArticle: (String) -> Unit, // Added navigation for articles
    viewModel: TicketViewModel = hiltViewModel(),
    onSubmitCsat: (Int) -> Unit
) {
    val ticket by viewModel.selectedTicket.collectAsState()
    val detailState by viewModel.detailState.collectAsState()
    val linkedArticles by viewModel.linkedArticles.collectAsState()

    // State for the dialog
    var showLinkDialog by remember { mutableStateOf(false) }

    // Fetch the ticket when the screen is first composed
    LaunchedEffect(key1 = ticketId) {
        viewModel.getTicketById(ticketId)
    }

    // Navigate back if a delete/update/link was successful
    LaunchedEffect(key1 = detailState) {
        if (detailState is TicketDetailState.Success) {
            viewModel.resetDetailState() // Reset state
            // We no longer navigate back automatically,
            // as updates are handled on a different screen.
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("#${ticketId.take(6).uppercase()}") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { onNavigateToEdit(ticketId) }) {
                Icon(Icons.Default.Edit, contentDescription = "Edit Ticket")
            }
        }
    ) { paddingValues ->
        if (ticket == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            // We pass the modifier from the Scaffold
            TicketDetailContent(
                ticket = ticket!!,
                linkedArticles = linkedArticles, // Pass the list
                modifier = Modifier.padding(paddingValues),
                onLinkArticleClick = { showLinkDialog = true }, // Open dialog
                onUnlinkArticle = { articleId -> viewModel.unlinkArticle(ticketId, articleId) },
                onNavigateToArticle = onNavigateToArticle,
                onSubmitCsat = onSubmitCsat
            )
        }
        if (showLinkDialog) {
            LinkArticleDialog(
                viewModel = viewModel,
                onDismiss = { showLinkDialog = false },
                onArticleSelected = { articleId ->
                    viewModel.linkArticle(ticketId, articleId)
                    showLinkDialog = false
                }
            )
        }
    }
}

@Composable
fun CsatRatingCard(
    currentScore: Int?,
    onRate: (Int) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = if (currentScore != null) "You rated this ticket:" else "How was our service?",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Spacer(Modifier.height(8.dp))
            Row(
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                for (i in 1..5) {
                    Icon(
                        imageVector = if (i <= (currentScore ?: 0)) Icons.Default.Star else Icons.Default.StarBorder,
                        contentDescription = "Rate $i stars",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .size(40.dp)
                            .clickable { onRate(i) }
                            .padding(4.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun TicketDetailContent(
    ticket: Ticket,
    linkedArticles: List<KBArticle>, // Added parameter
    modifier: Modifier = Modifier,
    onLinkArticleClick: () -> Unit,
    onUnlinkArticle: (String) -> Unit,
    onNavigateToArticle: (String) -> Unit,
    onSubmitCsat: (Int) -> Unit
) {
    val dateFormatter = remember { SimpleDateFormat("MMM dd, yyyy 'at' hh:mm a", Locale.getDefault()) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = ticket.name,
                style = MaterialTheme.typography.headlineMedium
            )
        }

        item {
            TicketDetailCard(
                label = "Status",
                value = ticket.status.displayName(),
                valueColor = ticket.status.toColor()
            )
        }

        item {
            TicketDetailCard(
                label = "Priority",
                value = ticket.priority.displayName(),
                valueColor = ticket.priority.toColor()
            )
        }

        item {
            TicketDetailCard(
                label = "Category",
                value = ticket.category
            )
        }

        item {
            TicketDetailCard(
                label = "Department",
                value = ticket.department
            )
        }

        item {
            TicketDetailCard(
                label = "Created",
                value = dateFormatter.format(ticket.createdDate.toDate())
            )
        }

        item {
            Text(
                text = "Description",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(top = 8.dp)
            )
            Text(
                text = ticket.description.ifBlank { "No description provided." },
                style = MaterialTheme.typography.bodyLarge,
            )
        }

        if (ticket.resolutionDescription.isNotBlank()) {
            item {
                Text(
                    text = "Resolution",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(top = 8.dp)
                )
                Text(
                    text = ticket.resolutionDescription,
                    style = MaterialTheme.typography.bodyLarge,
                )
            }
        }
        item {
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
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
        }

        if (ticket.status == TicketStatus.Closed) {
            item {
                CsatRatingCard(
                    currentScore = ticket.csatScore,
                    onRate = { score ->
                        // You need to pass this callback up from the Screen
                        onSubmitCsat(score)
                    }
                )
            }
        }

        if (linkedArticles.isEmpty()) {
            item {
                Text(
                    text = "No articles linked yet.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            items(linkedArticles) { article ->
                Box(modifier = Modifier.fillMaxWidth()) {
                    // Re-use your existing ArticleCard
                    ArticleCard(
                        article = article,
                        onClick = { onNavigateToArticle(article.id) }
                    )
                    // Add Unlink button on top
                    IconButton(
                        onClick = { onUnlinkArticle(article.id) },
                        modifier = Modifier.align(Alignment.TopEnd)
                    ) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Unlink Article",
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }
            }
        }
        // Add some bottom padding for the FAB
        item {
            Spacer(Modifier.height(64.dp))
        }
    }
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
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.height(300.dp) // Limit height
                ) {
                    items(articles, key = { it.id }) { article ->
                        Text(
                            text = article.title,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onArticleSelected(article.id) }
                                .padding(vertical = 12.dp),
                            style = MaterialTheme.typography.bodyLarge
                        )
                        HorizontalDivider()
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Close") }
        }
    )
}