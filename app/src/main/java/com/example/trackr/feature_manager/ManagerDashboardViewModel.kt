package com.example.trackr.feature_manager

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.trackr.domain.model.*
import com.example.trackr.domain.logic.GroupingEngine
import com.example.trackr.domain.repository.AnalyticsRepository
import com.example.trackr.domain.repository.AuthRepository
import com.example.trackr.domain.repository.CsatRepository
import com.example.trackr.domain.repository.DashboardRepository
import com.example.trackr.domain.repository.QualityMetrics
import com.example.trackr.domain.repository.TicketRepository
import com.google.firebase.Timestamp
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
@HiltViewModel
class ManagerDashboardViewModel @Inject constructor(
    dashboardRepository: DashboardRepository,
    private val ticketRepository: TicketRepository, // Made private to use in fixClosedTimestamps()
    authRepository: AuthRepository,
    private val groupingEngine: GroupingEngine,
    private val analyticsRepository: AnalyticsRepository,
    private val csatRepository: CsatRepository
) : ViewModel() {


    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing = _isRefreshing.asStateFlow()

    // [Phase 8] Main Metrics Flow
    val dashboardMetrics: StateFlow<DashboardMetrics> = _isRefreshing
        .flatMapLatest { force ->
            dashboardRepository.getDashboardMetrics(force)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DashboardMetrics())

    // Outage Detection (Heuristic: >3 tickets in last hour)
    // Note: We could move this to the Repository metrics calculation, but let's keep it simple here
    // based on the metrics data if available, or keep the existing heuristic logic.
    // For now, let's derive it from the metrics if possible, or keep as a separate lightweight check.
    // To save time, we will assume outage detection is handled by the visual inspection of the volume chart.

    // CSAT Trend (Last 7 Days)
    val csatTrend: StateFlow<List<Pair<String, Float>>> = flow {
        val end = System.currentTimeMillis()
        val start = end - java.util.concurrent.TimeUnit.DAYS.toMillis(7)
        csatRepository.getCsatResponsesForTimeRange(start, end).collect { responses ->
            val grouped = responses.groupBy {
                java.text.SimpleDateFormat("MM-dd", java.util.Locale.getDefault()).format(it.timestamp?.toDate() ?: java.util.Date())
            }
            val trendData = grouped.map { (date, list) ->
                val avg = list.map { it.rating }.average().toFloat()
                date to avg
            }.sortedBy { it.first }
            emit(trendData)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Recent Feedback
    val recentFeedback: StateFlow<List<CsatResponse>> = flow {
        val end = System.currentTimeMillis()
        val start = end - java.util.concurrent.TimeUnit.DAYS.toMillis(30)
        csatRepository.getCsatResponsesForTimeRange(start, end).collect { responses ->
            emit(responses.sortedByDescending { it.timestamp }.take(10))
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun refreshDashboard() {
        viewModelScope.launch {
            _isRefreshing.value = true
            // Small delay to show visual feedback
            kotlinx.coroutines.delay(500)
            _isRefreshing.value = false
        }
    }


    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    // Get the list of standard users
    private val _users = dashboardRepository.getStandardUsers()

    // Get the list of ALL tickets
    private val _allTickets = ticketRepository.getAllTicketsForReport()
//
//    // Get the current manager's ID
//    private val _managerId = authRepository.getAuthState().map { it?.uid }
//
//    // This combined flow creates the ID set for the manager + their team
//    private val _teamIds = combine(_users, _managerId) { users, managerId ->
//        (users.map { it.id } + managerId).filterNotNull().toSet()
//    }
//
//    // A single source of truth for "team tickets"
//    // This prevents logic duplication and ensures all charts/stats match.
//    // Contains tickets Created By OR Assigned To the team
//    private val _teamTickets = combine(_teamIds, _allTickets) { teamIds, tickets ->
//        tickets.filter {
//            it.createdBy in teamIds || it.assignee in teamIds
//        }
//    }
//
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
//
//    val userPerformanceReport: StateFlow<List<UserPerformance>> =
//        combine(_users, _allTickets) { users, tickets ->
//
//            val distinctUsers = users.distinctBy { it.id }
//
//            val performanceList = distinctUsers.map { user ->
//                // Filter for tickets ASSIGNED TO this user that are CLOSED
//                val userClosedTickets = tickets.filter {
//                    it.assignee == user.id &&
//                            it.status == TicketStatus.Closed &&
//                            it.closedDate != null
//                }
//
//                val count = userClosedTickets.size
//                val avgHours = if (count > 0) {
//                    val totalDurationMillis = userClosedTickets.sumOf {
//                        it.closedDate!!.toDate().time - it.createdDate.toDate().time
//                    }
//                    val avgMillis = totalDurationMillis.toDouble() / count
//                    avgMillis / (1000 * 60 * 60) // Convert to hours
//                } else {
//                    0.0
//                }
//
//                UserPerformance(
//                    user = user,
//                    ticketsClosed = count,
//                    avgResolutionHours = avgHours
//                )
//            }.sortedByDescending { it.ticketsClosed } // Sort by most productive
//
//            // Ensures the final list is distinct by user ID
//            performanceList.distinctBy { it.user.id }
//
//        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
//
//
//    // Ticket Stats (Summary Card)
//    // A real-time flow of the ticket statistics. (Total, Open, Closed)
//    val stats: StateFlow<DashboardStats> = _teamTickets.map { tickets ->
//        val open = tickets.count { it.status == TicketStatus.Open || it.status == TicketStatus.InProgress }
//        val closed = tickets.count { it.status == TicketStatus.Closed }
//        DashboardStats(open, closed, tickets.size)
//    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DashboardStats())
//
//    // State flow for the Ticket Aging Report (Bar Chart)
//    val ticketAgingStats: StateFlow<TicketAgingStats> = _teamTickets.map { tickets ->
//
//        val openTeamTickets = tickets.filter {
//            it.status == TicketStatus.Open || it.status == TicketStatus.InProgress
//        }
//
//        val now = System.currentTimeMillis()
//        var bucket1 = 0;
//        var bucket2 = 0;
//        var bucket3 = 0
//
//        openTeamTickets.forEach { ticket ->
//            val ageInMillis = now - ticket.createdDate.toDate().time
//            val ageInDays = TimeUnit.MILLISECONDS.toDays(ageInMillis)
//
//            when {
//                ageInDays <= 3 -> bucket1++
//                ageInDays <= 7 -> bucket2++
//                else -> bucket3++
//            }
//        }
//
//        TicketAgingStats(bucket1 = bucket1, bucket2 = bucket2, bucket3 = bucket3)
//
//    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), TicketAgingStats())
//
//    // State flow for the Resolved Tickets Report (Line Chart)
//    val resolvedTicketStats: StateFlow<ResolvedTicketStats> = _teamTickets.map { tickets ->
//
//        val closedTeamTickets = tickets.filter {
//            it.status == TicketStatus.Closed &&
//                    it.closedDate != null // Still need this check for the date math
//        }
//
//        val now = Calendar.getInstance().timeInMillis
//        val days7 = TimeUnit.DAYS.toMillis(7)
//        val days30 = TimeUnit.DAYS.toMillis(30)
//        val days90 = TimeUnit.DAYS.toMillis(90)
//
//        val count7Days = closedTeamTickets.count { (now - it.closedDate!!.toDate().time) <= days7 }
//        val count30Days = closedTeamTickets.count { (now - it.closedDate!!.toDate().time) <= days30 }
//        val count90Days = closedTeamTickets.count { (now - it.closedDate!!.toDate().time) <= days90 }
//
//        ResolvedTicketStats(
//            last7Days = count7Days,
//            last30Days = count30Days,
//            last90Days = count90Days
//        )
//
//    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ResolvedTicketStats())
//
//    // State flow for the Open Tickets Report (Pie Chart)
//    val openTicketsByCategoryStats: StateFlow<List<CategoryStat>> = _teamTickets.map { tickets ->
//        val openTeamTickets = tickets.filter {
//            it.status == TicketStatus.Open || it.status == TicketStatus.InProgress
//        }
//
//        openTeamTickets
//            .groupBy { it.category }
//            .map { (category, ticketList) ->
//                CategoryStat(category = category, count = ticketList.size)
//            }
//            .sortedByDescending { it.count }
//
//    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
//
//    // Flow for suggested ticket groups
//    val suggestedGroups: StateFlow<List<TicketGroup>> = _allTickets.map { tickets ->
//        // Filter only open tickets for grouping
//        val openTickets = tickets.filter {
//            it.status == TicketStatus.Open || it.status == TicketStatus.InProgress
//        }
//        // Run grouping logic
//        groupingEngine.groupTickets(openTickets)
//    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
//
//    val qualityMetrics = analyticsRepository.getQualityMetrics()
//        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), QualityMetrics())
//
//    // Outage Detection Flow (Mass Ticket Creation)
//    // Returns true if > 3 tickets were created in the last 1 hour
//    val outageAlert: StateFlow<Boolean> = _allTickets.map { tickets ->
//        val now = System.currentTimeMillis()
//        val oneHourAgo = now - TimeUnit.HOURS.toMillis(1)
//
//        val recentCount = tickets.count { ticket ->
//            val createdTime = ticket.createdDate.toDate().time
//            createdTime > oneHourAgo
//        }
//
//        // Heuristic: If more than 3 tickets in 1 hour -> Alert
//        recentCount > 3
//    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)
//
//    // CSAT Trend Data (Last 7 Days)
//    // We fetch responses and group them by day to calculate average daily CSAT
//    val csatTrend: StateFlow<List<Pair<String, Float>>> = flow {
//        val end = System.currentTimeMillis()
//        val start = end - java.util.concurrent.TimeUnit.DAYS.toMillis(7)
//
//        csatRepository.getCsatResponsesForTimeRange(start, end).collect { responses ->
//            val grouped = responses.groupBy {
//                java.text.SimpleDateFormat("MM-dd", java.util.Locale.getDefault()).format(it.timestamp?.toDate() ?: java.util.Date())
//            }
//
//            val trendData = grouped.map { (date, list) ->
//                val avg = list.map { it.rating }.average().toFloat()
//                date to avg
//            }.sortedBy { it.first }
//
//            emit(trendData)
//        }
//    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
//
//    // Get raw feedback list for the dashboard
//    val recentFeedback: StateFlow<List<CsatResponse>> = flow {
//        // Fetch last 30 days of feedback
//        val end = System.currentTimeMillis()
//        val start = end - java.util.concurrent.TimeUnit.DAYS.toMillis(30)
//
//        csatRepository.getCsatResponsesForTimeRange(start, end).collect { responses ->
//            // Sort by newest first and take top 10
//            emit(responses.sortedByDescending { it.timestamp }.take(10))
//        }
//    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
//
//    // ** Functions ***
//    fun onSearchQueryChange(query: String) {
//        _searchQuery.value = query
//    }
//
//    fun fixClosedTimestamps() {
//        viewModelScope.launch {
//            val allTickets = _allTickets.first() // Get current snapshot
//            val brokenTickets = allTickets.filter {
//                it.status == TicketStatus.Closed && it.closedDate == null
//            }
//
//            brokenTickets.forEach { ticket ->
//                // We manually set the closed time to NOW so the chart can see it
//                val fixedTicket = ticket.copy(closedDate = Timestamp.now())
//                ticketRepository.updateTicket(fixedTicket)
//            }
//        }
//    }
//
//    // Function to confirm/save a group
//    fun confirmGroup(group: TicketGroup) {
//        viewModelScope.launch {
//            val ids = group.tickets.map { it.id }
//            ticketRepository.groupTickets(ids, group.id)
//        }
//    }
}