package com.example.trackr.ui.charts

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.trackr.domain.model.CategoryStat
import com.example.trackr.domain.model.TicketAgingStats
import com.example.trackr.domain.model.ResolvedTicketStats
// Bar chart imports
import com.github.tehras.charts.bar.BarChart
import com.github.tehras.charts.bar.BarChartData
import com.github.tehras.charts.bar.renderer.bar.SimpleBarDrawer
import com.github.tehras.charts.bar.renderer.label.SimpleValueDrawer
import com.github.tehras.charts.bar.renderer.xaxis.SimpleXAxisDrawer as BarSimpleXAxisDrawer
import com.github.tehras.charts.bar.renderer.yaxis.SimpleYAxisDrawer as BarSimpleYAxisDrawer
// Line chart imports
import com.github.tehras.charts.line.LineChart
import com.github.tehras.charts.line.LineChartData
import com.github.tehras.charts.line.renderer.line.SolidLineDrawer
import com.github.tehras.charts.line.renderer.point.FilledCircularPointDrawer
import com.github.tehras.charts.line.renderer.xaxis.SimpleXAxisDrawer as LineSimpleXAxisDrawer
import com.github.tehras.charts.line.renderer.yaxis.SimpleYAxisDrawer as LineSimpleYAxisDrawer
// Pie chart imports
import com.github.tehras.charts.piechart.PieChart
import com.github.tehras.charts.piechart.PieChartData
import com.github.tehras.charts.piechart.renderer.SimpleSliceDrawer
import com.example.trackr.domain.model.UserCreationStats
//import com.github.tehras.charts.line.renderer.point.FilledCircularPointDrawer
//import com.github.tehras.charts.line.renderer.xaxis.SimpleXAxisDrawer as LineSimpleXAxisDrawer
//import com.github.tehras.charts.line.renderer.yaxis.SimpleYAxisDrawer as LineSimpleYAxisDrawer


// Generate a list of colors for the chart
private val chartColors = listOf(
    Color(0xFF6200EE), Color(0xFF03DAC6), Color(0xFF007BFF),
    Color(0xFFFF9800), Color(0xFFF44336), Color(0xFF4CAF50),
    Color(0xFF9C27B0), Color(0xFF3F51B5)
)

@Composable
fun TicketPieChart(categoryStats: List<CategoryStat>) {

    // 1. Create the data entries for the chart
    val slices = remember(categoryStats) {
        categoryStats.mapIndexed { index, stat ->
            PieChartData.Slice(
                value = stat.count.toFloat(),
                color = chartColors[index % chartColors.size]
            )
        }
    }

    // 2. Build the chart
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        PieChart(
            pieChartData = PieChartData(slices = slices),
            modifier = Modifier.size(150.dp),
            // Use a simple slice drawer
            sliceDrawer = SimpleSliceDrawer(
                sliceThickness = 100f // This makes it a pie chart instead of a donut
            )
        )

        Spacer(Modifier.width(24.dp))

        // 3. Build the custom legend
        ChartLegend(categoryStats = categoryStats)
    }
}

@Composable
private fun ChartLegend(categoryStats: List<CategoryStat>) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        categoryStats.forEachIndexed { index, stat ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(chartColors[index % chartColors.size])
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "${stat.category} (${stat.count})",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

// **BAR CHART COMPOSABLE**
@Composable
fun TicketAgingBarChart(stats: TicketAgingStats) {
    val barChartData = BarChartData(
        bars = listOf(
            BarChartData.Bar(
                label = "0-3 Days",
                value = stats.bucket1.toFloat(),
                color = chartColors[1]
            ),
            BarChartData.Bar(
                label = "4-7 Days",
                value = stats.bucket2.toFloat(),
                color = chartColors[3]
            ),
            BarChartData.Bar(
                label = "8+ Days",
                value = stats.bucket3.toFloat(),
                color = chartColors[4]
            )
        )
    )

    BarChart(
        barChartData = barChartData,
        modifier = Modifier.fillMaxWidth().height(200.dp).padding(16.dp),
        barDrawer = SimpleBarDrawer(),
        // We use the 'BarSimpleXAxisDrawer' we imported with an 'as' alias
        xAxisDrawer = BarSimpleXAxisDrawer(
            axisLineColor = MaterialTheme.colorScheme.onSurfaceVariant
        ),
        yAxisDrawer = BarSimpleYAxisDrawer(
            labelTextColor = MaterialTheme.colorScheme.onSurfaceVariant
        ),
        labelDrawer = SimpleValueDrawer(
            labelTextSize = 12.sp,
            labelTextColor = MaterialTheme.colorScheme.onSurface
        )
    )
}

// **LINE CHART COMPOSABLE**
@Composable
fun ResolvedTicketsLineChart(stats: ResolvedTicketStats) {

    // Create the list of LineChartData
    val lineChartDataList = remember(stats) {
        listOf(
            LineChartData(
                points = listOf(
                    LineChartData.Point(stats.last90Days.toFloat(), "90 Days"),
                    LineChartData.Point(stats.last30Days.toFloat(), "30 Days"),
                    LineChartData.Point(stats.last7Days.toFloat(), "7 Days")
                ),
                // We define the line drawing style here, inside the data object
                lineDrawer = SolidLineDrawer(color = chartColors[2])
            )
        )
    }

    // Call the LineChart composable with the correct parameters
    LineChart(
        linesChartData = lineChartDataList,
        modifier = Modifier.fillMaxWidth().height(200.dp).padding(16.dp),
        // Drawers are provided as separate parameters
        pointDrawer = FilledCircularPointDrawer(color = chartColors[2]),
        xAxisDrawer = LineSimpleXAxisDrawer(
            labelTextColor = MaterialTheme.colorScheme.onSurfaceVariant
        ),
        yAxisDrawer = LineSimpleYAxisDrawer(
            labelTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
            labelValueFormatter = { value -> value.toInt().toString() } // 'value' is the Float
        )
    )
}

@Composable
fun UserCreationLineChart(stats: UserCreationStats) {

    val lineChartDataList = remember(stats) {
        listOf(
            LineChartData(
                points = listOf(
                    LineChartData.Point(stats.last90Days.toFloat(), "90 Days"),
                    LineChartData.Point(stats.last30Days.toFloat(), "30 Days"),
                    LineChartData.Point(stats.last7Days.toFloat(), "7 Days")
                ),
                lineDrawer = SolidLineDrawer(color = chartColors[4]) // Use a different color
            )
        )
    }

    LineChart(
        linesChartData = lineChartDataList,
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .padding(16.dp),
        pointDrawer = FilledCircularPointDrawer(color = chartColors[4]),
        xAxisDrawer = LineSimpleXAxisDrawer(
            labelTextColor = MaterialTheme.colorScheme.onSurfaceVariant
        ),
        yAxisDrawer = LineSimpleYAxisDrawer(
            labelTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
            labelValueFormatter = { value -> value.toInt().toString() }
        )
    )
}