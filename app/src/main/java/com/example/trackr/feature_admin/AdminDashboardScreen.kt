package com.example.trackr.feature_admin

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.trackr.domain.model.DashboardStats
import com.example.trackr.domain.model.User
import com.example.trackr.domain.model.UserRole
import com.example.trackr.domain.model.UserRoleStats
import com.example.trackr.feature_admin.ui.AdminDashboardViewModel
import com.example.trackr.ui.HomeScreen
import com.example.trackr.util.ReportGenerator
import com.example.trackr.domain.model.CategoryStat
import com.example.trackr.domain.model.ResolutionTimeStats
import com.example.trackr.domain.model.UserStatus
import com.example.trackr.ui.charts.TicketPieChart
import com.example.trackr.ui.charts.UserCreationLineChart
import java.text.DecimalFormat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun AdminDashboardScreen(
    navController: NavController,
    viewModel: AdminDashboardViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {
    val users by viewModel.filteredUsers.collectAsState() // Use filtered users
    val filteredUsers by viewModel.filteredUsers.collectAsState()
    val stats by viewModel.stats.collectAsState()
    val userStats by viewModel.userRoleStats.collectAsState()
    val categoryStats by viewModel.categoryStats.collectAsState()
    val resolutionStats by viewModel.resolutionTimeStats.collectAsState()
    val userCreationStats by viewModel.userCreationStats.collectAsState() // **NEW**
    val inactiveUsers by viewModel.inactiveUsers.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedRole by viewModel.selectedRole.collectAsState()

    var userToDeactivate by remember { mutableStateOf<User?>(null) }


    LazyColumn(
        modifier = modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (inactiveUsers.isNotEmpty()) {
            item {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.onErrorContainer
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
                                "Inactive Accounts Detected",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                "${inactiveUsers.size} users haven't logged in for 30+ days.",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }
        }

        item {
            Text(
                "System Ticket Summary",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            TicketStatsSection(stats = stats)
        }

        item {
            Text(
                "Ticket Resolution Time (System-Wide)",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
            )
            ResolutionStatsSection(stats = resolutionStats)
        }

        item {
            Text(
                "New Users (Last 90 Days)",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
            )
            Card(modifier = Modifier.fillMaxWidth()) {
                // Check if there's any data, otherwise the chart might crash
                val hasData = userCreationStats.last7Days > 0 ||
                        userCreationStats.last30Days > 0 ||
                        userCreationStats.last90Days > 0
                if (hasData) {
                    UserCreationLineChart(stats = userCreationStats)
                } else {
                    Box(
                        modifier = Modifier.fillMaxWidth().height(200.dp).padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "No new users in the last 90 days.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        if (categoryStats.isNotEmpty()) {
            item {
                Text(
                    "Ticket Category Distribution",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                )
                Card(modifier = Modifier.fillMaxWidth()) {
                    TicketPieChart(categoryStats = categoryStats)
                }
            }
        }

        item {
            Text(
                "System User Report",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
            )
            UserStatsSection(stats = userStats)
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "User Management",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                // **NOTE**: The Share button is now a FAB, controlled by AppNavigation
            }
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = viewModel::onSearchQueryChange,
                    label = { Text("Search users by name or email...") },
                    shape = RoundedCornerShape(15.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                RoleFilterDropdown(
                    selectedRole = selectedRole,
                    onRoleSelected = viewModel::onRoleFilterChange
                )
            }
        }

        if (filteredUsers.isEmpty()) {
            item { Text("No users found.") }
        } else {
            items(filteredUsers, key = { it.id }) { user ->
                UserManagementItem(
                    user = user,
                    onRoleChange = { newRole -> viewModel.updateUserRole(user.id, newRole) },
                    onStatusToggle = {
                        if (user.status == UserStatus.Active) viewModel.deactivateUser(user.id)
                        else viewModel.reactivateUser(user.id)
                    }
                )
            }
        }

//        if (users.isEmpty()) {
//            item {
//                Text(
//                    "No users found.",
//                    modifier = Modifier.padding(top = 16.dp)
//                )
//            }
//        } else {
//            items(users, key = { it.id }) { user ->
//                UserRoleCard(
//                    user = user,
//                    onRoleChange = { newRole ->
//                        viewModel.updateUserRole(user.id, newRole)
//                    },
//                    onDeactivate = { userToDeactivate = user }
//                )
//            }
//        }
    }
    // Confirmation dialog for deactivation
    if (userToDeactivate != null) {
        AlertDialog(
            onDismissRequest = { userToDeactivate = null },
            title = { Text("Deactivate User?") },
            text = { Text("Are you sure you want to deactivate ${userToDeactivate?.name}? They will be hidden from lists and will not be able to log in.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deactivateUser(userToDeactivate!!.id)
                        userToDeactivate = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text("Deactivate") }
            },
            dismissButton = {
                TextButton(onClick = { userToDeactivate = null }) { Text("Cancel") }
            }
        )
    }
}

@Composable
fun UserManagementItem(
    user: User,
    onRoleChange: (UserRole) -> Unit,
    onStatusToggle: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(user.name, style = MaterialTheme.typography.titleSmall)
                Text(user.email, style = MaterialTheme.typography.bodySmall)
                Text(
                    text = if (user.status == UserStatus.Active) "Active" else "Deactivated",
                    color = if (user.status == UserStatus.Active) Color(0xFF4CAF50) else Color.Red,
                    style = MaterialTheme.typography.labelSmall
                )
            }

            // Role Dropdown
            Box {
                TextButton(onClick = { expanded = true }) {
                    Text(user.role.name)
                }
                DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    UserRole.values().forEach { role ->
                        DropdownMenuItem(
                            text = { Text(role.name) },
                            onClick = {
                                onRoleChange(role)
                                expanded = false
                            }
                        )
                    }
                }
            }

            // Status Toggle
            Switch(
                checked = user.status == UserStatus.Active,
                onCheckedChange = { onStatusToggle() }
            )
        }
    }
}

// Helper function to create and start the share intent
private fun shareReport(context: Context, uri: Uri) {
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/csv"
        putExtra(Intent.EXTRA_STREAM, uri)
        putExtra(Intent.EXTRA_SUBJECT, "Admin User Report")
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    context.startActivity(Intent.createChooser(intent, "Share User Report"))
}

@Composable
private fun TicketStatsSection(stats: DashboardStats) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Spacer(modifier = Modifier.width(25.dp))
            StatItem(label = "Open", count = stats.openTickets)
            Spacer(modifier = Modifier.width(100.dp))
            StatItem(label = "Closed", count = stats.closedTickets)
            Spacer(modifier = Modifier.width(100.dp))
            StatItem(label = "Total", count = stats.totalTickets)
        }
    }
}

// Display the user role stats
@Composable
private fun UserStatsSection(stats: UserRoleStats) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Spacer(modifier = Modifier.width(20.dp))
            StatItem(label = "Admins", count = stats.adminCount)
            Spacer(modifier = Modifier.width(50.dp))
            StatItem(label = "Managers", count = stats.managerCount)
            Spacer(modifier = Modifier.width(50.dp))
            StatItem(label = "Users", count = stats.userCount)
            Spacer(modifier = Modifier.width(50.dp))
            StatItem(label = "Total", count = stats.totalUsers)
            Spacer(modifier = Modifier.width(10.dp))
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
private fun ResolutionStatsSection(stats: ResolutionTimeStats) {
    val decimalFormat = remember { DecimalFormat("#.#") }

    fun formatHours(hours: Double?): String {
        return hours?.let { "${decimalFormat.format(it)}h" } ?: "N/A"
    }

    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            StatItem(label = "Avg Resolve", value = formatHours(stats.averageResolutionHours))
            Spacer(modifier = Modifier.width(50.dp))
            StatItem(label = "Fastest", value = formatHours(stats.fastestResolutionHours))
            Spacer(modifier = Modifier.width(50.dp))
            StatItem(label = "Slowest", value = formatHours(stats.slowestResolutionHours))
        }
    }
}

// **OVERLOADED** StatItem to accept a String value for flexibility
@Composable
private fun RowScope.StatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = MaterialTheme.typography.headlineMedium)
        Text(label, style = MaterialTheme.typography.labelMedium)
    }
}

@Composable
private fun UserRoleCard(
    user: User,
    onRoleChange: (UserRole) -> Unit,
    onDeactivate: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {

            // Top Row
            Row(
                modifier = Modifier.padding(start = 16.dp, end = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(user.name, style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.width(8.dp))
                Text(user.email, style = MaterialTheme.typography.bodyLarge)
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.padding(start = 16.dp, end = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {

                Row(verticalAlignment = Alignment.CenterVertically) {
                    RoleDropdown(
                        selectedRole = user.role,
                        onRoleSelected = onRoleChange
                    )
                    // Deactivate Button
                    IconButton(onClick = onDeactivate) {
                        Icon(
                            Icons.Default.Block,
                            contentDescription = "Deactivate User",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RoleFilterDropdown(
    selectedRole: UserRole?,
    onRoleSelected: (UserRole?) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = selectedRole?.name ?: "All Roles",
            onValueChange = {},
            readOnly = true,
            enabled = false,
            label = { Text("Filter by Role") },
            trailingIcon = {
                // Add a clear button to the filter
                if (selectedRole != null) {
                    IconButton(onClick = { onRoleSelected(null) }) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear Filter")
                    }
                } else {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                }
            },
            shape = RoundedCornerShape(15.dp),
            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(
                disabledTextColor = MaterialTheme.colorScheme.onSurface,
                disabledBorderColor = MaterialTheme.colorScheme.outline,
                disabledLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
            ),
            modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable).fillMaxWidth()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            DropdownMenuItem(
                text = { Text("All Roles") },
                onClick = {
                    onRoleSelected(null)
                    expanded = false
                }
            )
            UserRole.values().forEach { role ->
                DropdownMenuItem(
                    text = { Text(role.name) },
                    onClick = {
                        onRoleSelected(role)
                        expanded = false
                    }
                )
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RoleDropdown(
    selectedRole: UserRole,
    onRoleSelected: (UserRole) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = selectedRole.name,
            onValueChange = {},
            readOnly = true,
            enabled = false,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            shape = RoundedCornerShape(15.dp),
            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(
                disabledTextColor = MaterialTheme.colorScheme.onSurface,
                disabledBorderColor = MaterialTheme.colorScheme.outline,
                disabledLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
            ),
            modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable).width(250.dp)
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            UserRole.entries.forEach { role ->
                DropdownMenuItem(
                    text = { Text(role.name) },
                    onClick = {
                        onRoleSelected(role)
                        expanded = false
                    }
                )
            }
        }
    }
}