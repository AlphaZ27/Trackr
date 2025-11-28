package com.example.trackr.feature_kb

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.trackr.domain.model.KBArticle

@Composable
fun KBListScreen(
    viewModel: KBListViewModel = hiltViewModel(),
    modifier: Modifier = Modifier,
    onNavigateToCreateArticle: () -> Unit,
    onNavigateToArticle: (String) -> Unit
) {
    val articles by viewModel.displayedArticles.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedTab by viewModel.selectedTab.collectAsState()


    Column(
        modifier = modifier // Use the modifier passed from HomeScreen
            .fillMaxSize()
        // Padding is now handled by HomeScreen's Scaffold
    ) {
        PrimaryTabRow(selectedTabIndex = selectedTab) {
            Tab(
                selected = selectedTab == 0,
                onClick = { viewModel.onTabSelected(0) },
                text = { Text("All Articles") }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { viewModel.onTabSelected(1) },
                text = { Text("Frequent Issues") }
            )
        }

        OutlinedTextField(
            value = searchQuery,
            onValueChange = viewModel::onSearchQueryChange,
            label = { Text("Search articles...") },
            shape = RoundedCornerShape(25.dp),
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
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 16.dp), // Added bottom padding
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(articles, key = { it.id }) { article ->
                    ArticleCard(article = article, onClick = { onNavigateToArticle(article.id) })
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = article.category.uppercase(),
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                // Shows the view count
                Text(
                    text = "${article.viewCount} views",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(Modifier.height(8.dp))

            Text(
                article.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(Modifier.height(4.dp))

            Text(
                text = article.content.take(150) + "...",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}