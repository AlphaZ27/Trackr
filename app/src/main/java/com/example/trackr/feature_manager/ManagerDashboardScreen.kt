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
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Warning
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.trackr.domain.model.DashboardStats
import com.example.trackr.domain.model.User
import com.example.trackr.ui.HomeScreen
import com.example.trackr.ui.charts.ResolvedTicketsLineChart
import com.example.trackr.ui.charts.TicketAgingBarChart
import com.example.trackr.domain.model.CategoryStat
import com.example.trackr.domain.model.TicketGroup
import com.example.trackr.ui.charts.TicketPieChart
import com.example.trackr.util.ReportGenerator
import com.example.trackr.domain.model.UserPerformance
import java.text.DecimalFormat
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
    val userPerformanceList by viewModel.userPerformanceReport.collectAsState()
    val stats by viewModel.stats.collectAsState()
    val agingStats by viewModel.ticketAgingStats.collectAsState()
    val resolvedStats by viewModel.resolvedTicketStats.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()

    val openCategoryStats by viewModel.openTicketsByCategoryStats.collectAsState()

    val suggestedGroups by viewModel.suggestedGroups.collectAsState()

    val qualityMetrics by viewModel.qualityMetrics.collectAsState()

    val outageDetected by viewModel.outageAlert.collectAsState()

    // Ensure lists are unique to prevent crashes
    val uniqueUserActivityList = remember(userActivityList) {
        userActivityList.distinctBy { it.user.id }
    }
    val uniqueUserPerformanceList = remember(userPerformanceList) {
        userPerformanceList.distinctBy { it.user.id }
    }


    LazyColumn(
        // Apply modifier here
        modifier = modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        // Outage Banner (Mass Ticket Creation Alert)
        if (outageDetected) {
            item {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Warning, contentDescription = null)
                        Spacer(Modifier.width(16.dp))
                        Column {
                            Text(
                                "Potential Outage or Large Scale Issue Detected",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                "High volume of tickets created in the last hour.",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }
        }

        // Top Ticket Summary Cards
        item {
            Text(
                "Ticket Summary",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            TicketStatsSection(stats = stats)
        }

        item {
            Button(
                onClick = { navController.navigate("reports_screen") },
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
            ) {
                Text("View Reports Center")
            }
        }

        item {
            Text(
                "Quality & Health",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
            )
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                // CSAT Card
                QualityCard(
                    title = "CSAT Score",
                    value = String.format("%.1f", qualityMetrics.averageCsat),
                    subtext = "${qualityMetrics.csatResponseRate.toInt()}% response rate",
                    icon = Icons.Default.Star,
                    modifier = Modifier.weight(1f)
                )
                // Reopen Rate Card
                QualityCard(
                    title = "Reopen Rate",
                    value = "${String.format("%.1f", qualityMetrics.reopenRate)}%",
                    subtext = "Target: < 5%",
                    icon = Icons.Default.Refresh, // Make sure to import or use a suitable icon
                    modifier = Modifier.weight(1f),
                    isGood = qualityMetrics.reopenRate < 5.0
                )
            }
        }

        // Charts Section
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
                ResolvedTicketsLineChart(stats = resolvedStats)
            }
        }

        // Suggested Groups Section
//        if (suggestedGroups.isNotEmpty()) {
//            item {
//                Text(
//                    "Suggested Ticket Groups",
//                    style = MaterialTheme.typography.titleMedium,
//                    modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
//                )
//            }
//            items(suggestedGroups) { group ->
//                TicketGroupCard(group = group, onConfirm = { viewModel.confirmGroup(group) })
//            }
//        }

        // User Performance Section
        if (userPerformanceList.isNotEmpty()) {
            item {
                Text(
                    "Team Performance (Tickets Assigned)",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                )
            }
            items(userPerformanceList, key = { "perf_${it.user.id}" }) { perf ->
                UserPerformanceCard(perf)
            }
        }

        // User Activity Section
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Standard Users (Tickets Created)",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
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
            items(userActivityList, key = { "activity_${it.user.id}" }) { userActivity ->
                UserCard(
                    user = userActivity.user,
                    openTickets = userActivity.openTickets,
                    closedTickets = userActivity.closedTickets
                )
            }
        }

    }
}

@Composable
fun QualityCard(
    title: String,
    value: String,
    subtext: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    isGood: Boolean = true
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = if (isGood) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(8.dp))
            Text(value, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Text(title, style = MaterialTheme.typography.titleSmall)
            Text(subtext, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun UserPerformanceCard(perf: UserPerformance) {
    val decimalFormat = remember { DecimalFormat("#.#") }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(perf.user.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                Text("Tickets Closed: ${perf.ticketsClosed}", style = MaterialTheme.typography.bodyMedium)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = if (perf.ticketsClosed > 0) "${decimalFormat.format(perf.avgResolutionHours)} hrs" else "N/A",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text("Avg. Time", style = MaterialTheme.typography.labelSmall)
            }
        }
    }
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

//@Composable
//fun TicketGroupCard(group: TicketGroup, onConfirm: () -> Unit) {
//    Card(
//        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer),
//        modifier = Modifier.fillMaxWidth()
//    ) {
//        Column(modifier = Modifier.padding(16.dp)) {
//            Text(group.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
//            Text("${group.size} similar tickets found", style = MaterialTheme.typography.bodyMedium)
//            Spacer(Modifier.height(8.dp))
//            Button(onClick = onConfirm, modifier = Modifier.align(Alignment.End)) {
//                Text("Group Tickets")
//            }
//        }
//    }
//}