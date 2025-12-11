package com.example.trackr

import com.example.trackr.domain.logic.GroupingEngine
import com.example.trackr.domain.logic.SimilarityEngine
import com.example.trackr.domain.model.Priority
import com.example.trackr.domain.model.Ticket
import com.example.trackr.domain.model.TicketStatus
import com.example.trackr.domain.repository.CsatRepository
import com.example.trackr.domain.repository.DashboardRepository
import com.example.trackr.domain.repository.KBRepository
import com.example.trackr.domain.repository.TicketRepository
import com.example.trackr.feature_tickets.TicketViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations

@OptIn(ExperimentalCoroutinesApi::class)
class TicketViewModelTest {

    @Mock lateinit var ticketRepository: TicketRepository
    @Mock lateinit var kbRepository: KBRepository
    @Mock lateinit var dashboardRepository: DashboardRepository
    @Mock lateinit var csatRepository: CsatRepository

    // We can use a real engine or mock it. Real is fine for logic testing.
    private val groupingEngine = GroupingEngine(SimilarityEngine())
    private lateinit var viewModel: TicketViewModel

    //@OptIn(ExperimentalCoroutinesApi::class)
    private val testDispatcher = StandardTestDispatcher()

    //@OptIn(ExperimentalCoroutinesApi::class)
    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        Dispatchers.setMain(testDispatcher)

        // Mock Data
        val mockTickets = listOf(
            Ticket(id = "1", name = "Open Ticket", status = TicketStatus.Open),
            Ticket(id = "2", name = "In Progress Ticket", status = TicketStatus.InProgress),
            Ticket(id = "3", name = "Closed Ticket", status = TicketStatus.Closed)
        )

        // Mock Repository Responses
        `when`(ticketRepository.getAllTickets()).thenReturn(flowOf(mockTickets))
        `when`(kbRepository.getAllArticles()).thenReturn(flowOf(emptyList()))
        `when`(dashboardRepository.getAllUsers()).thenReturn(flowOf(emptyList()))

        runTest {
            `when`(ticketRepository.getTicketsForCurrentUser()).thenReturn(emptyList())
        }

        // Init ViewModels
        viewModel = TicketViewModel(
            ticketRepository,
            kbRepository,
            groupingEngine,
            csatRepository,
            dashboardRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }


    /**
     * TEST: Verify that the "Active" tab logic works.
     * Scenario: User selects Tab 0 (Active Tickets).
     * Expected Result: The list should ONLY contain 'Open' and 'In Progress' tickets.
     * The 'Closed' ticket (id="3") must be filtered out.
     */
    @Test
    fun testActiveTabFiltersOutClosedTickets() = runTest {
        // Start collecting in background to activate the StateFlow
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.filteredTickets.collect()
        }

        // Given: Active Tab
        viewModel.onTabSelected(0)

        // Let the flows emit
        advanceUntilIdle()

        // Assert on the current value
        val result = viewModel.filteredTickets.value
        assertEquals(2, result.size)
        assertEquals("Open Ticket", result[0].name)
        assertEquals("In Progress Ticket", result[1].name)
    }


    /**
     * TEST: Verify that the "Closed History" tab logic works.
     * Scenario: User selects Tab 1 (Closed History).
     * Expected Result: The list should ONLY contain tickets with 'Closed' status.
     * 'Open' and 'In Progress' tickets must be filtered out.
     */
    @Test
    fun testClosedTabShowsOnlyClosedTickets() = runTest {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.filteredTickets.collect()
        }

        // Switch to Closed Tab
        viewModel.onTabSelected(1)
        advanceUntilIdle()

        // Then: Should have 1 ticket
        val result = viewModel.filteredTickets.value
        assertEquals(1, result.size)
        assertEquals("Closed Ticket", result[0].name)
    }


    /**
     * TEST: Verify search functionality within a specific tab.
     * Scenario: User is on the Active Tab and types "Progress" into the search bar.
     * Expected Result: The list should filter down to the specific ticket matching the name query.
     */
    @Test
    fun testSearchFilterWorks() = runTest {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.filteredTickets.collect()
        }

        // Active Tab
        viewModel.onTabSelected(0)
        // Search query set
        viewModel.onSearchQueryChange("Progress")
        advanceUntilIdle()

        // Then: Should find 1 ticket matching query
        val result = viewModel.filteredTickets.value
        assertEquals(1, result.size)
        assertEquals("In Progress Ticket", result[0].name)
    }
}