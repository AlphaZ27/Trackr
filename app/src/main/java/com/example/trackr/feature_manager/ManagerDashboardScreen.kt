package com.example.trackr.feature_manager

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.material3.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.trackr.domain.model.DashboardStats
import com.example.trackr.domain.model.User
import com.example.trackr.ui.HomeScreen
import com.example.trackr.ui.charts.ResolvedTicketsLineChart
import com.example.trackr.ui.charts.TicketAgingBarChart
import com.example.trackr.domain.model.CategoryStat
import com.example.trackr.ui.charts.TicketPieChart
import com.example.trackr.util.ReportGenerator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManagerDashboardScreen(
    navController: NavController,
    viewModel: ManagerDashboardViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {

    val userActivityList by viewModel.userActivityReport.collectAsState()
    val users by viewModel.filteredUsers.collectAsState() // Use filtered users
    val stats by viewModel.stats.collectAsState()
    val agingStats by viewModel.ticketAgingStats.collectAsState()
    val resolvedStats by viewModel.resolvedTicketStats.collectAsState() // **NEW**
    val searchQuery by viewModel.searchQuery.collectAsState()

    val openCategoryStats by viewModel.openTicketsByCategoryStats.collectAsState()

    // For launching the share intent
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val reportGenerator = remember { ReportGenerator() }

    LazyColumn(
        // Apply modifier here
        modifier = modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                "Ticket Summary",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            TicketStatsSection(stats = stats)
        }

        if (openCategoryStats.isNotEmpty()) {
            item {
                Text(
                    "Open Tickets by Category (Your Team)",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                )
                Card(modifier = Modifier.fillMaxWidth()) {
                    TicketPieChart(categoryStats = openCategoryStats)
                }
            }
        }

        item {
            Text(
                "Open Ticket Aging (Your Team)",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
            )
            Card(modifier = Modifier.fillMaxWidth()) {
                TicketAgingBarChart(stats = agingStats)
            }
        }

        item {
            Text(
                "Resolved Tickets (Your Team)",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
            )
            Card(modifier = Modifier.fillMaxWidth()) {
                // **CHECK IF DATA EXISTS**:
                // The chart library crashes if all values are 0.
                val hasData = resolvedStats.last7Days > 0 ||
                        resolvedStats.last30Days > 0 ||
                        resolvedStats.last90Days > 0

                if (hasData) {
                    ResolvedTicketsLineChart(stats = resolvedStats)
                } else {
                    // Show a placeholder instead of crashing
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "No resolved tickets in the last 90 days.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Standard Users",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                // !!REMEMBER!!: The Share button is now a FAB in AppNavigation
            }
        }

        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = viewModel::onSearchQueryChange,
                label = { Text("Search users by name or email...") },
                shape = RoundedCornerShape(15.dp),
                modifier = Modifier.fillMaxWidth()
            )
        }

        if (userActivityList.isEmpty()) {
            item {
                Text(
                    "No users found.",
                    modifier = Modifier.padding(top = 16.dp)
                )
            }
        } else {
            items(userActivityList, key = { it.user.id }) { userActivity ->
                UserCard(
                    user = userActivity.user,
                    openTickets = userActivity.openTickets,
                    closedTickets = userActivity.closedTickets
                )
            }
        }
    }
}

// Helper function to create and start the share intent
private fun shareReport(context: Context, uri: Uri) {
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/csv"
        putExtra(Intent.EXTRA_STREAM, uri)
        putExtra(Intent.EXTRA_SUBJECT, "User Report")
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    context.startActivity(Intent.createChooser(intent, "Share User Report"))
}

@Composable
private fun TicketStatsSection(stats: DashboardStats) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Spacer(modifier = Modifier.width(20.dp))
            StatItem(label = "Open", count = stats.openTickets)
            Spacer(modifier = Modifier.width(100.dp))
            StatItem(label = "Closed", count = stats.closedTickets)
            Spacer(modifier = Modifier.width(100.dp))
            StatItem(label = "Total", count = stats.totalTickets)
        }
    }
}

@Composable
private fun RowScope.StatItem(label: String, count: Int) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(count.toString(), style = MaterialTheme.typography.headlineMedium)
        Text(label, style = MaterialTheme.typography.labelMedium)
    }
}

@Composable
private fun UserCard(
    user: User,
    openTickets: Int,
    closedTickets: Int)
{
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(user.name, style = MaterialTheme.typography.titleSmall)
                Text(user.email, style = MaterialTheme.typography.bodySmall)
            }
            Spacer(modifier = Modifier.width(20.dp))
            Text(user.role.name, style = MaterialTheme.typography.labelMedium)
            Spacer(modifier = Modifier.width(100.dp))
            Column(horizontalAlignment = Alignment.End) {
                Text("Open: $openTickets", style = MaterialTheme.typography.bodySmall)
                Text("Closed: $closedTickets", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}