package com.example.trackr

import com.example.trackr.domain.model.User
import com.example.trackr.domain.model.UserRole
import com.example.trackr.domain.repository.DashboardRepository
import com.example.trackr.feature_admin.ui.AdminDashboardViewModel
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@ExperimentalCoroutinesApi
class AdminDashboardViewModelTest {

    // Create mocks for all dependencies
    private lateinit var mockDashboardRepository: DashboardRepository

    // Create a mock user
    // A special "dispatcher" for running coroutines in tests
    private lateinit var viewModel: AdminDashboardViewModel

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        // Set the main dispatcher to the test dispatcher
        Dispatchers.setMain(testDispatcher)

        // initialize the mock repository
        mockDashboardRepository = mockk()

        val fakeUsers = listOf(
            User(uid = "1", name = "Admin User", email = "admin@test.com", role = UserRole.Admin),
            User(uid = "2", name = "Manager User", email = "manager@test.com", role = UserRole.Manager),
            User(uid = "3", name = "Test User", email = "user@test.com", role = UserRole.User)
        )

        // Tells the mock repository what to do
        every { mockDashboardRepository.getAllUsers() } returns flowOf(fakeUsers)
        every { mockDashboardRepository.getTicketStats() } returns flowOf(mockk())

        // Initialize the ViewModel with the mock
        viewModel = AdminDashboardViewModel(mockDashboardRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun searchQueryFiltersUserListCorrectly() = runTest {

        // Give the viewModel time to load the initial user list
        testDispatcher.scheduler.advanceUntilIdle()

        // verify the initial state (3 users, unsorted)
        assertEquals(3, viewModel.filteredUsers.value.size)

        // Action: Perform a search
        viewModel.onSearchQueryChange("Manager")
        testDispatcher.scheduler.advanceUntilIdle()

        // Verify the search results
        val filteredList = viewModel.filteredUsers.value
        assertEquals(1, filteredList.size)
        assertEquals("Manager User", filteredList[0].name)
    }

    @Test
    fun roleFilterFiltersUserListCorrectly() = runTest {

        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals(3, viewModel.filteredUsers.value.size)

        // Action: Filter by role
        viewModel.onRoleFilterChange(UserRole.Admin)
        testDispatcher.scheduler.advanceUntilIdle()

        // Assert: Check the result
        val filteredList = viewModel.filteredUsers.value
        assertEquals(1, filteredList.size)
        assertEquals(UserRole.Admin, filteredList[0].name)
    }

    @Test
    fun deactivateUserCallsRepositoryWithCorrectUserId() = runTest {

        // We need to tell Mockk to expect a "co" (suspending) function call
        coVerify(exactly = 0) { mockDashboardRepository.updateUserStatus(any(), any()) } // Verify it hasn't been called yet

        // Action: Deactivate a user
        viewModel.deactivateUser("3")
        testDispatcher.scheduler.advanceUntilIdle()

        // Assert: Verify the repository was called *exactly once* with the correct ID
        coVerify(exactly = 1) { mockDashboardRepository.updateUserStatus("3", any()) }


    }

}