package com.example.trackr.feature_admin.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.trackr.domain.model.*
import com.example.trackr.domain.repository.DashboardRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import com.google.firebase.Timestamp
import java.util.Calendar
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltViewModel
class AdminDashboardViewModel @Inject constructor(
    private val dashboardRepository: DashboardRepository
) : ViewModel() {

    // State for the user search query
    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    // State for the role filter
    private val _selectedRole = MutableStateFlow<UserRole?>(null)
    val selectedRole = _selectedRole.asStateFlow()

    // A real-time flow of ALL users.
    val users: StateFlow<List<User>> = dashboardRepository.getAllUsers()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // A real-time flow of inactive users.
    val inactiveUsers: StateFlow<List<User>> = users.map { users ->
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, -30) // 30 days ago
        val inactivityThreshold = Timestamp(calendar.time)

        users.filter { user ->
            // User is inactive if lastLogin is not null AND it is before the threshold
            user.lastLogin != null && user.lastLogin < inactivityThreshold
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Filtered list that reacts to both search and role filters.
    val filteredUsers: StateFlow<List<User>> =
        combine(users, _searchQuery, _selectedRole) { users, query, role ->

            val distinctUsers = users.distinctBy { it.id }

            val filteredList = distinctUsers.filter { user ->
                val queryMatch = if (query.isBlank()) true else {
                    user.name.contains(query, ignoreCase = true) ||
                            user.email.contains(query, ignoreCase = true)
                }
                val roleMatch = role == null || user.role == role
                queryMatch && roleMatch
            }
            // Sort the list here, in the viewmodel
            filteredList.distinctBy { it.id }.sortedBy { it.name }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())


    // A real-time flow of the ticket statistics.
    val stats: StateFlow<DashboardStats> = dashboardRepository.getTicketStats()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = DashboardStats()
        )

    // User role stats
    val userRoleStats: StateFlow<UserRoleStats> = dashboardRepository.getUserRoleStats()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = UserRoleStats()
        )

    // Ticket category stats
    val categoryStats: StateFlow<List<CategoryStat>> = dashboardRepository.getTicketCategoryStats()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Ticket resolution time stats
    val resolutionTimeStats: StateFlow<ResolutionTimeStats> = dashboardRepository.getTicketResolutionStats()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ResolutionTimeStats()
        )

    val userCreationStats: StateFlow<UserCreationStats> = users.map { users ->
        val now = Calendar.getInstance().timeInMillis
        val days7 = TimeUnit.DAYS.toMillis(7)
        val days30 = TimeUnit.DAYS.toMillis(30)
        val days90 = TimeUnit.DAYS.toMillis(90)

        val distinctUsers = users.distinctBy { it.id }


        // We can just filter the list we already have
        val count7Days = distinctUsers.count { it.createdAt != null && (now - it.createdAt.toDate().time) <= days7 }
        val count30Days = distinctUsers.count { it.createdAt != null && (now - it.createdAt.toDate().time) <= days30 }
        val count90Days = distinctUsers.count { it.createdAt != null && (now - it.createdAt.toDate().time) <= days90 }

        UserCreationStats(
            last7Days = count7Days,
            last30Days = count30Days,
            last90Days = count90Days
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UserCreationStats())

    /* *
    Functions
     */
    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    // Function to update the role filter
    fun onRoleFilterChange(role: UserRole?) {
        _selectedRole.value = role
    }

    // Function to update a user's role.
    fun updateUserRole(userId: String, newRole: UserRole) {
        viewModelScope.launch {
            dashboardRepository.updateUserRole(userId, newRole)
                // Error handling can be added here (e.g., updating a UI state for a snackbar)
                .onFailure { /* Handle error */ }
        }
    }

    // Function to deactivate a user
    fun deactivateUser(userId: String) {
        viewModelScope.launch {
            dashboardRepository.updateUserStatus(userId, UserStatus.Deactivated)
                .onFailure { /* Handle error */ }
        }
    }
}