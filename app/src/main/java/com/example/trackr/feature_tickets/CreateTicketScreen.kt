package com.example.trackr.feature_tickets

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.trackr.feature_kb.ArticleCard
import com.example.trackr.feature_tickets.ui.shared.CategoryDropdown
import com.example.trackr.feature_tickets.ui.shared.PriorityDropdown
import com.example.trackr.feature_tickets.ui.shared.StatusDropdown


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

    // React to save success
    LaunchedEffect(createState) {
        if (createState is CreateState.Success) {
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
            modifier = Modifier.padding(paddingValues).padding(16.dp),
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
            OutlinedTextField(
                // Assignee Field
                value = viewModel.assignee.value,
                onValueChange = { viewModel.assignee.value = it },
                label = { Text("Assignee") },
                modifier = Modifier.fillMaxWidth()
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
                Spacer(modifier = Modifier.padding(vertical = 8.dp))
            }

            // Category Dropdown
            CategoryDropdown(
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

