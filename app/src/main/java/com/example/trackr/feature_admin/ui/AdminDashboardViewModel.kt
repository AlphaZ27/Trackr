package com.example.trackr.feature_admin.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.trackr.domain.model.CategoryStat
import com.example.trackr.domain.model.DashboardStats
import com.example.trackr.domain.model.User
import com.example.trackr.domain.model.UserRole
import com.example.trackr.domain.model.UserRoleStats
import com.example.trackr.domain.model.UserStatus
import com.example.trackr.domain.repository.DashboardRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
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


    // Filtered list that reacts to both search and role filters.
    val filteredUsers: StateFlow<List<User>> =
        combine(users, _searchQuery, _selectedRole) { users, query, role ->
//            users.filter { user ->
//                val queryMatch = if (query.isBlank()) {
//                    true
//                } else {
//                    user.name.contains(query, ignoreCase = true) ||
//                            user.email.contains(query, ignoreCase = true)
//                }
//                val roleMatch = role == null || user.role == role
//                queryMatch && roleMatch
//            }
            val filteredList = users.filter { user ->
                val queryMatch = if (query.isBlank()) true else {
                    user.name.contains(query, ignoreCase = true) ||
                            user.email.contains(query, ignoreCase = true)
                }
                val roleMatch = role == null || user.role == role
                queryMatch && roleMatch
            }
            // Sort the list here, in the viewmodel
            filteredList.sortedBy { it.name }
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