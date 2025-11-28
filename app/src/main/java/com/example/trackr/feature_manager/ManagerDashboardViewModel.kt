package com.example.trackr.feature_manager

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.trackr.domain.model.*
import com.example.trackr.domain.logic.GroupingEngine
import com.example.trackr.domain.repository.AuthRepository
import com.example.trackr.domain.repository.DashboardRepository
import com.example.trackr.domain.repository.TicketRepository
import com.google.firebase.Timestamp
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltViewModel
class ManagerDashboardViewModel @Inject constructor(
    dashboardRepository: DashboardRepository,
    private val ticketRepository: TicketRepository, // Made private to use in fixClosedTimestamps()
    authRepository: AuthRepository,
    private val groupingEngine: GroupingEngine
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    // Get the list of standard users
    private val _users = dashboardRepository.getStandardUsers()

    // Get the list of ALL tickets
    private val _allTickets = ticketRepository.getAllTicketsForReport()

    // Get the current manager's ID
    private val _managerId = authRepository.getAuthState().map { it?.uid }

    // This combined flow creates the ID set for the manager + their team
    private val _teamIds = combine(_users, _managerId) { users, managerId ->
        (users.map { it.id } + managerId).filterNotNull().toSet()
    }

    // A single source of truth for "team tickets"
    // This prevents logic duplication and ensures all charts/stats match.
    // Contains tickets Created By OR Assigned To the team
    private val _teamTickets = combine(_teamIds, _allTickets) { teamIds, tickets ->
        tickets.filter {
            it.createdBy in teamIds || it.assignee in teamIds
        }
    }

    // Combine users, tickets, and search query into the "User Activity Report"
    val userActivityReport: StateFlow<List<UserActivity>> =
        combine(_users, _allTickets, _searchQuery) { users, tickets, query ->

            val distinctUsers = users.distinctBy { it.id }

            // Filter users based on search query
            val filteredUsers = if (query.isBlank()) {
                distinctUsers
            } else {
                distinctUsers.filter {
                    it.name.contains(query, ignoreCase = true) ||
                            it.email.contains(query, ignoreCase = true)
                }
            }

            // Map each user to their ticket stats
            val reportList = filteredUsers.map { user ->
                UserActivity(
                    user = user,
                    openTickets = tickets.count {
                        (it.createdBy == user.id || it.assignee == user.id) &&
                                (it.status == TicketStatus.Open || it.status == TicketStatus.InProgress) },
                        closedTickets = tickets.count { (it.createdBy == user.id || it.assignee == user.id) &&
                                it.status == TicketStatus.Closed }
                )
            }.sortedBy { it.user.name } // Sort by name

            // Ensures the final list is distinct by user ID
            reportList.distinctBy { it.user.id }

        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val userPerformanceReport: StateFlow<List<UserPerformance>> =
        combine(_users, _allTickets) { users, tickets ->

            val distinctUsers = users.distinctBy { it.id }

            val performanceList = distinctUsers.map { user ->
                // Filter for tickets ASSIGNED TO this user that are CLOSED
                val userClosedTickets = tickets.filter {
                    it.assignee == user.id &&
                            it.status == TicketStatus.Closed &&
                            it.closedAt != null
                }

                val count = userClosedTickets.size
                val avgHours = if (count > 0) {
                    val totalDurationMillis = userClosedTickets.sumOf {
                        it.closedAt!!.toDate().time - it.createdDate.toDate().time
                    }
                    val avgMillis = totalDurationMillis.toDouble() / count
                    avgMillis / (1000 * 60 * 60) // Convert to hours
                } else {
                    0.0
                }

                UserPerformance(
                    user = user,
                    ticketsClosed = count,
                    avgResolutionHours = avgHours
                )
            }.sortedByDescending { it.ticketsClosed } // Sort by most productive

            // Ensures the final list is distinct by user ID
            performanceList.distinctBy { it.user.id }

        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // A real-time flow of all users with the 'User' role.
//    val users: StateFlow<List<User>> = dashboardRepository.getStandardUsers()
//        .stateIn(
//            scope = viewModelScope,
//            started = SharingStarted.WhileSubscribed(5000),
//            initialValue = emptyList()
//        )

    // Ticket Stats (Summary Card)
    // A real-time flow of the ticket statistics. (Total, Open, Closed)
    val stats: StateFlow<DashboardStats> = _teamTickets.map { tickets ->
        val open = tickets.count { it.status == TicketStatus.Open || it.status == TicketStatus.InProgress }
        val closed = tickets.count { it.status == TicketStatus.Closed }
        DashboardStats(open, closed, tickets.size)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DashboardStats())

    // A new filtered list that reacts to the search query
//    val filteredUsers: StateFlow<List<User>> =
//        combine(users, searchQuery) { users, query ->
//
//            val distinctUsers = users.distinctBy { it.id }
//
//            val filteredList = if (query.isBlank()) {
//                distinctUsers
//            } else {
//                distinctUsers.filter {
//                    it.name.contains(query, ignoreCase = true) ||
//                            it.email.contains(query, ignoreCase = true)
//                }
//            }
//            // Sort the list here, in the viewmodel
//            filteredList.sortedBy { it.name }
//        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // State flow for the Ticket Aging Report (Bar Chart)
    val ticketAgingStats: StateFlow<TicketAgingStats> = _teamTickets.map { tickets ->

        val openTeamTickets = tickets.filter {
            it.status == TicketStatus.Open || it.status == TicketStatus.InProgress
        }

        val now = System.currentTimeMillis()
        var bucket1 = 0;
        var bucket2 = 0;
        var bucket3 = 0

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

    // State flow for the Resolved Tickets Report (Line Chart)
    val resolvedTicketStats: StateFlow<ResolvedTicketStats> = _teamTickets.map { tickets ->

        val closedTeamTickets = tickets.filter {
            it.status == TicketStatus.Closed &&
                    it.closedAt != null // Still need this check for the date math
        }

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

    // State flow for the Open Tickets Report (Pie Chart)
    val openTicketsByCategoryStats: StateFlow<List<CategoryStat>> = _teamTickets.map { tickets ->
        val openTeamTickets = tickets.filter {
            it.status == TicketStatus.Open || it.status == TicketStatus.InProgress
        }

        openTeamTickets
            .groupBy { it.category }
            .map { (category, ticketList) ->
                CategoryStat(category = category, count = ticketList.size)
            }
            .sortedByDescending { it.count }

    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Flow for suggested ticket groups
    val suggestedGroups: StateFlow<List<TicketGroup>> = _allTickets.map { tickets ->
        // Filter only open tickets for grouping
        val openTickets = tickets.filter {
            it.status == TicketStatus.Open || it.status == TicketStatus.InProgress
        }
        // Run grouping logic
        groupingEngine.groupTickets(openTickets)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun fixClosedTimestamps() {
        viewModelScope.launch {
            val allTickets = _allTickets.first() // Get current snapshot
            val brokenTickets = allTickets.filter {
                it.status == TicketStatus.Closed && it.closedAt == null
            }

            brokenTickets.forEach { ticket ->
                // We manually set the closed time to NOW so the chart can see it
                val fixedTicket = ticket.copy(closedAt = Timestamp.now())
                ticketRepository.updateTicket(fixedTicket)
            }
        }
    }

    // Function to confirm/save a group
    fun confirmGroup(group: TicketGroup) {
        viewModelScope.launch {
            val ids = group.tickets.map { it.id }
            ticketRepository.groupTickets(ids, group.id)
        }
    }
}