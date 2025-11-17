package com.example.trackr.feature_kb

import android.widget.Toast
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import kotlinx.coroutines.launch
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
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
    val userFeedback by viewModel.userFeedback.collectAsState()
    var showDeleteDialog by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val saveSuccess by viewModel.saveSuccess.collectAsState()

    // --- UI Feedback Logic ---
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

//    LaunchedEffect(saveSuccess) {
//        if (saveSuccess) {
//            Toast.makeText(context, "Saved", Toast.LENGTH_SHORT).show()
//            onNavigateBack()
//            viewModel.resetSaveState()
//        }
//    }
//
//    LaunchedEffect(key1 = uiState) {
//        if (uiState is KBDetailState.Success) {
//            onNavigateBack()
//        }
//    }

    LaunchedEffect(uiState) {
        when (val state = uiState) {
            is KBDetailState.Success -> {
                // For delete, we navigate back
                onNavigateBack()
            }
            is KBDetailState.Error -> {
                scope.launch {
                    snackbarHostState.showSnackbar("Error: ${state.message}")
                }
                viewModel.resetUiState()
            }
            else -> {}
        }
    }

    LaunchedEffect(key1 = articleId) {
        viewModel.getArticleById(articleId)
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }, // The snackbar host
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
                    userFeedback = userFeedback,
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
    userFeedback: Feedback?,
    feedbackList: List<Feedback>,
    onSubmit: (rating: Int, comment: String) -> Unit
) {
    var comment by remember { mutableStateOf("") }
    var selectedRating by remember { mutableStateOf(0) } // 0 = none, 1 = up, -1 = down

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceVariant,
        tonalElevation = 1.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Check if the user has already provided feedback.
            if (userFeedback != null) {
                // --- Show "Thank You" state ---
                Text("Thank you for your feedback!", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Icon(
                        Icons.Default.ThumbUp,
                        contentDescription = "Helpful",
                        tint = if (userFeedback.rating == 1) MaterialTheme.colorScheme.primary else LocalContentColor.current.copy(alpha = 0.5f)
                    )
                    Icon(
                        Icons.Default.ThumbDown,
                        contentDescription = "Not Helpful",
                        tint = if (userFeedback.rating == -1) MaterialTheme.colorScheme.error else LocalContentColor.current.copy(alpha = 0.5f)
                    )
                }

                // Display the user's submitted comment.
                if (!userFeedback.comment.isNullOrBlank()) {
                    Spacer(Modifier.height(16.dp))
                    Text("Your comment:", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Text(userFeedback.comment, modifier = Modifier.padding(12.dp))
                    }
                }
            } else {
                // --- Show Interactive Rating state ---
                Text("Was this article helpful?", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    IconToggleButton(
                        checked = selectedRating == 1,
                        onCheckedChange = { selectedRating = if (it) 1 else 0 }
                    ) {
                        Icon(
                            Icons.Default.ThumbUp,
                            contentDescription = "Helpful",
                            tint = if (selectedRating == 1) MaterialTheme.colorScheme.primary else LocalContentColor.current
                        )
                    }
                    IconToggleButton(
                        checked = selectedRating == -1,
                        onCheckedChange = { selectedRating = if (it) -1 else 0 }
                    ) {
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
                            scope.launch {
                                snackbarHostState.showSnackbar("Feedback submitted!")
                            }
                            // Reset form
                            selectedRating = 0
                            comment = ""
                        },
                        // ...
                    ) {
                        Text("Submit")
                    }
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
                                    .padding(top = 8.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                            ) {
                                Text(feedback.comment, modifier = Modifier.padding(12.dp))
                            }
                        }
                    }
                }
        }
    }
}