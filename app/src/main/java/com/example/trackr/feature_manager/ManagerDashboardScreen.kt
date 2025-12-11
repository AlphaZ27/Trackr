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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Warning
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
import com.example.trackr.domain.model.CsatResponse
import com.example.trackr.domain.model.TicketGroup
import com.example.trackr.ui.charts.TicketPieChart
import com.example.trackr.util.ReportGenerator
import com.example.trackr.domain.model.UserPerformance
import com.example.trackr.ui.charts.CsatLineChart
import com.github.tehras.charts.line.LineChart
import com.github.tehras.charts.line.LineChartData
import com.github.tehras.charts.line.renderer.line.SolidLineDrawer
import com.github.tehras.charts.line.renderer.point.FilledCircularPointDrawer
import com.github.tehras.charts.line.renderer.xaxis.SimpleXAxisDrawer
import com.github.tehras.charts.line.renderer.yaxis.SimpleYAxisDrawer
import com.github.tehras.charts.piechart.PieChart
import com.github.tehras.charts.piechart.PieChartData
import com.github.tehras.charts.piechart.renderer.SimpleSliceDrawer
import java.text.DecimalFormat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManagerDashboardScreen(
    onNavigateBack: () -> Unit,
    navController: NavController,
    viewModel: ManagerDashboardViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {

    val metrics by viewModel.dashboardMetrics.collectAsState()
    val csatTrend by viewModel.csatTrend.collectAsState()
    val recentFeedback by viewModel.recentFeedback.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()


    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Manager Dashboard") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.refreshDashboard() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh Data")
                    }
                }
            )
        }
    ) { padding ->
        if (isRefreshing) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth().padding(padding))
        }

        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Navigation to Reports
            item {
                Button(
                    onClick = { navController.navigate("reports_screen") },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("View Reports Center (PDF)")
                }
            }

            // 2. High Level Stats Row
            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    SummaryCard("Open", metrics.openTickets.toString(), Color(0xFFE53935), Modifier.weight(1f))
                    SummaryCard("In Progress", metrics.inProgressTickets.toString(), Color(0xFFFFA726), Modifier.weight(1f))
                    SummaryCard("Closed", metrics.closedTickets.toString(), Color(0xFF43A047), Modifier.weight(1f))
                }
            }

            // 3. Efficiency Stats (Resolution Time & SLA)
            item {
                Text("Efficiency Metrics", style = MaterialTheme.typography.titleMedium)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    EfficiencyCard(
                        title = "Avg Resolution",
                        value = "${String.format("%.1f", metrics.avgResolutionTimeHours)} hrs",
                        icon = Icons.Default.Timer,
                        modifier = Modifier.weight(1f)
                    )
                    EfficiencyCard(
                        title = "SLA Breach %",
                        value = "${String.format("%.1f", metrics.slaBreachRate)}%",
                        icon = Icons.Default.Warning,
                        isGood = metrics.slaBreachRate < 10.0,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // 4. Department Distribution (Pie Chart)
            item {
                Text("Tickets by Department", style = MaterialTheme.typography.titleMedium)
                Card(modifier = Modifier.fillMaxWidth().height(250.dp)) {
                    if (metrics.ticketsByDepartment.isNotEmpty()) {
                        DepartmentPieChart(metrics.ticketsByDepartment)
                    } else {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("No data") }
                    }
                }
            }

            // 5. CSAT Trend
            item {
                Text("CSAT Trend (Last 7 Days)", style = MaterialTheme.typography.titleMedium)
                Card(modifier = Modifier.fillMaxWidth().height(200.dp)) {
                    if (csatTrend.isNotEmpty()) {
                        CsatLineChart(csatTrend)
                    } else {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("No feedback data") }
                    }
                }
            }

            // 6. Recent Feedback List
            item {
                Text("Recent Feedback", style = MaterialTheme.typography.titleMedium)
            }

            if (recentFeedback.isEmpty()) {
                item { Text("No comments available.", color = MaterialTheme.colorScheme.onSurfaceVariant) }
            } else {
                items(recentFeedback) { feedback ->
                    FeedbackItemCard(feedback)
                }
            }
        }
    }
}

@Composable
fun SummaryCard(title: String, count: String, color: Color, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(count, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = color)
            Text(title, style = MaterialTheme.typography.labelMedium, color = color)
        }
    }
}

@Composable
fun EfficiencyCard(title: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector, isGood: Boolean = true, modifier: Modifier = Modifier) {
    Card(modifier = modifier, colors = CardDefaults.cardColors(containerColor = if (isGood) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.errorContainer)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(8.dp))
            Text(value, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text(title, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
fun DepartmentPieChart(data: Map<String, Int>) {
    // 1. Create a List of Pairs (Label, Slice) to associate keys with slices
    // The Slice object in this library version only accepts value and color.
    val chartData = data.entries.mapIndexed { index, entry ->
        val slice = PieChartData.Slice(
            value = entry.value.toFloat(),
            color = getColorForIndex(index)
        )
        entry.key to slice // Pair(Label, Slice)
    }

    val slices = chartData.map { it.second }

    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxSize().padding(16.dp)) {
        // Chart
        Box(modifier = Modifier.weight(1f).aspectRatio(1f)) {
            PieChart(
                pieChartData = PieChartData(slices = slices),
                // Use sliceThickness to create a donut effect (e.g. 50f)
                sliceDrawer = SimpleSliceDrawer(sliceThickness = 50f)
            )
        }
        // Legend
        Column(modifier = Modifier.weight(0.6f).padding(start = 16.dp)) {
            chartData.forEach { (label, slice) ->
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 4.dp)) {
                    Surface(modifier = Modifier.size(12.dp), color = slice.color, shape = MaterialTheme.shapes.extraSmall) {}
                    Spacer(Modifier.width(8.dp))
                    Text("$label (${slice.value.toInt()})", style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}

@Composable
fun CsatLineChart(data: List<Pair<String, Float>>) {
    val lineChartData = LineChartData(
        points = data.map { (label, value) -> LineChartData.Point(value, label) },
        lineDrawer = SolidLineDrawer(color = Color(0xFF4CAF50))
    )
    LineChart(
        linesChartData = listOf(lineChartData),
        modifier = Modifier.fillMaxSize().padding(16.dp),
        pointDrawer = FilledCircularPointDrawer(color = Color(0xFF4CAF50)),
        xAxisDrawer = SimpleXAxisDrawer(),
        yAxisDrawer = SimpleYAxisDrawer(labelValueFormatter = { "${it.toInt()}★" })
    )
}

// Helper for colors
fun getColorForIndex(index: Int): Color {
    val colors = listOf(Color(0xFF2196F3), Color(0xFF4CAF50), Color(0xFFFFC107), Color(0xFF9C27B0), Color(0xFFF44336), Color(0xFF00BCD4))
    return colors[index % colors.size]
}

@Composable
fun FeedbackItemCard(feedback: CsatResponse) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Stars
                Row {
                    repeat(5) { index ->
                        Icon(
                            imageVector = if (index < feedback.rating) Icons.Default.Star else Icons.Default.StarBorder,
                            contentDescription = null,
                            tint = Color(0xFFFFC107), // Gold star color
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
                // Date (You can format this better if needed)
                Text(
                    text = feedback.timestamp?.toDate()?.toString()?.take(10) ?: "",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(Modifier.height(8.dp))

            if (feedback.comment.isNotBlank()) {
                Text(
                    text = "\"${feedback.comment}\"",
                    style = MaterialTheme.typography.bodyMedium,
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                )
            } else {
                Text(
                    text = "No comment provided.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                )
            }

            Spacer(Modifier.height(4.dp))

            Text(
                text = "Ticket #${feedback.ticketId.take(6).uppercase()} • Technician: ${if (feedback.technicianId.isNotBlank()) "Assigned" else "Unassigned"}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}


//@Composable
//fun QualityCard(
//    title: String,
//    value: String,
//    subtext: String,
//    icon: ImageVector,
//    modifier: Modifier = Modifier,
//    isGood: Boolean = true
//) {
//    Card(
//        modifier = modifier,
//        colors = CardDefaults.cardColors(
//            containerColor = if (isGood) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.errorContainer
//        )
//    ) {
//        Column(modifier = Modifier.padding(16.dp)) {
//            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
//            Spacer(Modifier.height(8.dp))
//            Text(value, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
//            Text(title, style = MaterialTheme.typography.titleSmall)
//            Text(subtext, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
//        }
//    }
//}
//
//@Composable
//private fun UserPerformanceCard(perf: UserPerformance) {
//    val decimalFormat = remember { DecimalFormat("#.#") }
//
//    Card(
//        modifier = Modifier.fillMaxWidth(),
//        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
//    ) {
//        Row(
//            modifier = Modifier.padding(16.dp),
//            verticalAlignment = Alignment.CenterVertically,
//            horizontalArrangement = Arrangement.SpaceBetween
//        ) {
//            Column(modifier = Modifier.weight(1f)) {
//                Text(perf.user.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
//                Text("Tickets Closed: ${perf.ticketsClosed}", style = MaterialTheme.typography.bodyMedium)
//            }
//            Column(horizontalAlignment = Alignment.End) {
//                Text(
//                    text = if (perf.ticketsClosed > 0) "${decimalFormat.format(perf.avgResolutionHours)} hrs" else "N/A",
//                    style = MaterialTheme.typography.titleMedium,
//                    fontWeight = FontWeight.Bold
//                )
//                Text("Avg. Time", style = MaterialTheme.typography.labelSmall)
//            }
//        }
//    }
//}
//
//@Composable
//fun FeedbackItemCard(feedback: CsatResponse) {
//    Card(
//        modifier = Modifier
//            .fillMaxWidth()
//            .padding(bottom = 8.dp),
//        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
//    ) {
//        Column(modifier = Modifier.padding(16.dp)) {
//            Row(
//                modifier = Modifier.fillMaxWidth(),
//                horizontalArrangement = Arrangement.SpaceBetween
//            ) {
//                // Stars
//                Row {
//                    repeat(5) { index ->
//                        Icon(
//                            imageVector = if (index < feedback.rating) Icons.Default.Star else Icons.Default.StarBorder,
//                            contentDescription = null,
//                            tint = Color(0xFFFFC107), // Gold star color
//                            modifier = Modifier.size(16.dp)
//                        )
//                    }
//                }
//                // Date (You can format this better if needed)
//                Text(
//                    text = feedback.timestamp?.toDate()?.toString()?.take(10) ?: "",
//                    style = MaterialTheme.typography.labelSmall,
//                    color = MaterialTheme.colorScheme.onSurfaceVariant
//                )
//            }
//
//            Spacer(Modifier.height(8.dp))
//
//            if (feedback.comment.isNotBlank()) {
//                Text(
//                    text = "\"${feedback.comment}\"",
//                    style = MaterialTheme.typography.bodyMedium,
//                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
//                )
//            } else {
//                Text(
//                    text = "No comment provided.",
//                    style = MaterialTheme.typography.bodySmall,
//                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
//                )
//            }
//
//            Spacer(Modifier.height(4.dp))
//
//            Text(
//                text = "Ticket #${feedback.ticketId.take(6).uppercase()} • Technician: ${if (feedback.technicianId.isNotBlank()) "Assigned" else "Unassigned"}",
//                style = MaterialTheme.typography.labelSmall,
//                color = MaterialTheme.colorScheme.primary
//            )
//        }
//    }
//}

//@Composable
//private fun TicketStatsSection(stats: DashboardStats) {
//    Card(modifier = Modifier.fillMaxWidth()) {
//        Row(
//            modifier = Modifier.padding(16.dp),
//            horizontalArrangement = Arrangement.SpaceAround,
//            verticalAlignment = Alignment.CenterVertically
//        ) {
//            Spacer(modifier = Modifier.width(20.dp))
//            StatItem(label = "Open", count = stats.openTickets)
//            Spacer(modifier = Modifier.width(100.dp))
//            StatItem(label = "Closed", count = stats.closedTickets)
//            Spacer(modifier = Modifier.width(100.dp))
//            StatItem(label = "Total", count = stats.totalTickets)
//        }
//    }
//}
//
//@Composable
//private fun RowScope.StatItem(label: String, count: Int) {
//    Column(horizontalAlignment = Alignment.CenterHorizontally) {
//        Text(count.toString(), style = MaterialTheme.typography.headlineMedium)
//        Text(label, style = MaterialTheme.typography.labelMedium)
//    }
//}
//
//@Composable
//private fun UserCard(
//    user: User,
//    openTickets: Int,
//    closedTickets: Int)
//{
//    Card(modifier = Modifier.fillMaxWidth()) {
//        Row(
//            modifier = Modifier.padding(16.dp),
//            verticalAlignment = Alignment.CenterVertically,
//            horizontalArrangement = Arrangement.SpaceBetween
//        ) {
//            Column {
//                Text(user.name, style = MaterialTheme.typography.titleSmall)
//                Text(user.email, style = MaterialTheme.typography.bodySmall)
//            }
//            Spacer(modifier = Modifier.width(20.dp))
//            Text(user.role.name, style = MaterialTheme.typography.labelMedium)
//            Spacer(modifier = Modifier.width(100.dp))
//            Column(horizontalAlignment = Alignment.End) {
//                Text("Open: $openTickets", style = MaterialTheme.typography.bodySmall)
//                Text("Closed: $closedTickets", style = MaterialTheme.typography.bodySmall)
//            }
//        }
//    }
//}

