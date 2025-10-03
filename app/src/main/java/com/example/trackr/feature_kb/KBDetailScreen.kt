package com.example.trackr.feature_kb

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ThumbDown
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.trackr.domain.model.Feedback


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KBDetailScreen(
    articleId: String,
    viewModel: KBDetailViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onNavigateToEdit: (String) -> Unit
) {
    val article by viewModel.article.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    val feedbackList by viewModel.feedbackList.collectAsState()
    var showDeleteDialog by remember { mutableStateOf(false) }

    LaunchedEffect(key1 = articleId) {
        viewModel.getArticleById(articleId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(article?.category ?: "Article") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { onNavigateToEdit(articleId) }) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit Article")
                    }
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete Article")
                    }
                }
            )
        }
    ) { paddingValues ->
        if (uiState is KBDetailState.Loading && article == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (article == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Article not found.")
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(article!!.title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    Text("Tags: ${article!!.tags.joinToString(", ")}", style = MaterialTheme.typography.bodySmall)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(article!!.content, style = MaterialTheme.typography.bodyLarge)
                }
                // Feedback Section
                FeedbackSection(
                    articleId = articleId,
                    feedbackList = feedbackList,
                    onSubmit = { rating, comment ->
                        viewModel.submitFeedback(articleId, rating, comment)
                    }
                )
            }
        }
    }

    // Show the delete confirmation dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Article?") },
            text = { Text("Are you sure you want to permanently delete this article?") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteArticle(articleId)
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

@Composable
fun FeedbackSection(
    articleId: String,
    feedbackList: List<Feedback>,
    onSubmit: (rating: Int, comment: String) -> Unit
) {
    var comment by remember { mutableStateOf("") }
    var selectedRating by remember { mutableStateOf(0) } // 0 = none, 1 = up, -1 = down

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Was this article helpful?", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                IconButton(onClick = { selectedRating = 1 }) {
                    Icon(
                        Icons.Default.ThumbUp,
                        contentDescription = "Helpful",
                        tint = if (selectedRating == 1) MaterialTheme.colorScheme.primary else LocalContentColor.current
                    )
                }
                IconButton(onClick = { selectedRating = -1 }) {
                    Icon(
                        Icons.Default.ThumbDown,
                        contentDescription = "Not Helpful",
                        tint = if (selectedRating == -1) MaterialTheme.colorScheme.error else LocalContentColor.current
                    )
                }
            }

            if (selectedRating != 0) {
                OutlinedTextField(
                    value = comment,
                    onValueChange = { comment = it },
                    label = { Text("Add an optional comment...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                )
                Spacer(Modifier.height(8.dp))
                Button(
                    onClick = {
                        onSubmit(selectedRating, comment)
                        // Reset form
                        selectedRating = 0
                        comment = ""
                    },
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("Submit")
                }
            }

            // Display existing feedback
            if (feedbackList.isNotEmpty()) {
                Spacer(Modifier.height(16.dp))
                Text("Comments", style = MaterialTheme.typography.titleMedium)
                feedbackList.forEach { feedback ->
                    if (!feedback.comment.isNullOrBlank()) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp)
                        ) {
                            Text(feedback.comment, modifier = Modifier.padding(12.dp))
                        }
                    }
                }
            }
        }
    }
}