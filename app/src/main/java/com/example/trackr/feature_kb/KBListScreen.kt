package com.example.trackr.feature_kb

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.trackr.domain.model.KBArticle

@Composable
fun KBListScreen(
    viewModel: KBListViewModel = hiltViewModel(),
    modifier: Modifier = Modifier,
    onNavigateToCreateArticle: () -> Unit,
    onNavigateToArticle: (String) -> Unit
) {
    val articles by viewModel.filteredArticles.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = onNavigateToCreateArticle) {
                Icon(Icons.Default.Add, contentDescription = "Create New Article")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = viewModel::onSearchQueryChange,
                label = { Text("Search articles...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            )

            if (articles.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(if (searchQuery.isBlank()) "No articles found." else "No articles match your search.")
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(articles, key = { it.id }) { article ->
                        ArticleCard(article = article, onClick = { onNavigateToArticle(article.id) })
                    }
                }
            }
        }
    }

}

@Composable
fun ArticleCard(article: KBArticle, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = article.category.uppercase(),
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(8.dp))

            Text(article.title, style = MaterialTheme.typography.titleMedium)

            Spacer(Modifier.height(4.dp))

            Text(
                text = article.content.take(150) + "...",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}