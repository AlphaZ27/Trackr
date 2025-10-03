package com.example.trackr.feature_kb


import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.trackr.domain.model.ArticleStatus


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KBEditScreen(
    articleId: String?, // Nullable for creating a new article
    viewModel: KBEditViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val editState by viewModel.editState.collectAsState()
    var showDeleteDialog by remember { mutableStateOf(false) }


    // Load the article if an ID is provided
    LaunchedEffect(key1 = articleId) {
        viewModel.loadArticle(articleId)
    }

    // Navigate back on successful save
    LaunchedEffect(key1 = editState) {
        if (editState is EditState.Success) {
            onNavigateBack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (articleId == null) "New Article" else "Edit Article") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (articleId != null) {
                        IconButton(onClick = { showDeleteDialog = true }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete Article")
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            Button(
                onClick = viewModel::saveArticle,
                enabled = viewModel.title.value.isNotBlank() && viewModel.content.value.isNotBlank()
            ) {
                Text("Save Article")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = viewModel.title.value,
                onValueChange = { viewModel.title.value = it },
                label = { Text("Title") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = viewModel.category.value,
                onValueChange = { viewModel.category.value = it },
                label = { Text("Category") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = viewModel.tags.value,
                onValueChange = { viewModel.tags.value = it },
                label = { Text("Tags (comma separated)") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = viewModel.content.value,
                onValueChange = { viewModel.content.value = it },
                label = { Text("Content") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp)
            )

            // Status Dropdown
            ArticleStatusDropdown(
                selectedStatus = viewModel.status.value,
                onStatusSelected = { viewModel.status.value = it }
            )
        }
    }
    // Confirmation dialog for deletion
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Article?") },
            text = { Text("Are you sure you want to permanently delete this article?") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteArticle()
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


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ArticleStatusDropdown(
    selectedStatus: ArticleStatus,
    onStatusSelected: (ArticleStatus) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            modifier = Modifier.menuAnchor().fillMaxWidth(),
            value = selectedStatus.name,
            onValueChange = {},
            readOnly = true,
            label = { Text("Status") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            ArticleStatus.values().forEach { status ->
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