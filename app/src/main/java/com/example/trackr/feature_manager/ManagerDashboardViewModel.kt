package com.example.trackr.feature_manager

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.trackr.domain.model.DashboardStats
import com.example.trackr.domain.model.ResolvedTicketStats
import com.example.trackr.domain.model.Ticket
import com.example.trackr.domain.model.TicketAgingStats
import com.example.trackr.domain.model.TicketStatus
import com.example.trackr.domain.model.User
import com.example.trackr.domain.model.UserActivity
import com.example.trackr.domain.repository.DashboardRepository
import com.example.trackr.domain.repository.TicketRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import java.util.Calendar
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltViewModel
class ManagerDashboardViewModel @Inject constructor(
    dashboardRepository: DashboardRepository,
    ticketRepository: TicketRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    // Get the list of standard users
    private val _users = dashboardRepository.getStandardUsers()

    // Get the list of ALL tickets
    private val _allTickets = ticketRepository.getAllTicketsForReport()

    // Combine users, tickets, and search query into the "User Activity Report"
    val userActivityReport: StateFlow<List<UserActivity>> =
        combine(_users, _allTickets, _searchQuery) { users, tickets, query ->

            // Filter users based on search query
            val filteredUsers = if (query.isBlank()) {
                users
            } else {
                users.filter {
                    it.name.contains(query, ignoreCase = true) ||
                            it.email.contains(query, ignoreCase = true)
                }
            }

            // Map each user to their ticket stats
            filteredUsers.map { user ->
                UserActivity(
                    user = user,
                    openTickets = tickets.count { it.createdBy == user.id && (it.status == TicketStatus.Open || it.status == TicketStatus.InProgress) },
                    closedTickets = tickets.count { it.createdBy == user.id && it.status == TicketStatus.Closed }
                )
            }.sortedBy { it.user.name } // Sort by name

        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // A real-time flow of all users with the 'User' role.
    val users: StateFlow<List<User>> = dashboardRepository.getStandardUsers()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // A real-time flow of the ticket statistics.
    val stats: StateFlow<DashboardStats> = dashboardRepository.getTicketStats()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = DashboardStats()
        )

    // A new filtered list that reacts to the search query
    val filteredUsers: StateFlow<List<User>> =
        combine(users, searchQuery) { users, query ->
            val filteredList = if (query.isBlank()) {
                users
            } else {
                users.filter {
                    it.name.contains(query, ignoreCase = true) ||
                            it.email.contains(query, ignoreCase = true)
                }
            }
            // Sort the list here, in the viewmodel
            filteredList.sortedBy { it.name }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // State flow for the Ticket Aging Report
    val ticketAgingStats: StateFlow<TicketAgingStats> =
        combine(_users, _allTickets) { users, tickets ->
            val userIds = users.map { it.id }.toSet()

            // Filter tickets to only those open and created by the manager's team
            val openTeamTickets = tickets.filter {
                it.createdBy in userIds &&
                        (it.status == TicketStatus.Open || it.status == TicketStatus.InProgress)
            }

            // Process into aging buckets
            val now = System.currentTimeMillis()
            var bucket1 = 0 // 0-3 days
            var bucket2 = 0 // 4-7 days
            var bucket3 = 0 // 8+ days

            openTeamTickets.forEach { ticket ->
                val ageInMillis = now - ticket.createdDate.toDate().time
                val ageInDays = TimeUnit.MILLISECONDS.toDays(ageInMillis)

                when {
                    ageInDays <= 3 -> bucket1++
                    ageInDays <= 7 -> bucket2++
                    else -> bucket3++
                }
            }

            TicketAgingStats(bucket1 = bucket1, bucket2 = bucket2, bucket3 = bucket3)

        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), TicketAgingStats())

    val resolvedTicketStats: StateFlow<ResolvedTicketStats> =
        combine(_users, _allTickets) { users, tickets ->
            val userIds = users.map { it.id }.toSet()

            //  Filter tickets to only those closed and created by the manager's team
            val closedTeamTickets = tickets.filter {
                it.createdBy in userIds &&
                        it.status == TicketStatus.Closed &&
                        it.closedAt != null // Ensure the timestamp exists
            }

            // Process into time buckets
            val now = Calendar.getInstance().timeInMillis
            val days7 = TimeUnit.DAYS.toMillis(7)
            val days30 = TimeUnit.DAYS.toMillis(30)
            val days90 = TimeUnit.DAYS.toMillis(90)

            val count7Days = closedTeamTickets.count { (now - it.closedAt!!.toDate().time) <= days7 }
            val count30Days = closedTeamTickets.count { (now - it.closedAt!!.toDate().time) <= days30 }
            val count90Days = closedTeamTickets.count { (now - it.closedAt!!.toDate().time) <= days90 }

            ResolvedTicketStats(
                last7Days = count7Days,
                last30Days = count30Days,
                last90Days = count90Days
            )

        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ResolvedTicketStats())

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }
}