package com.krew18.app.ui.talent

import com.krew18.app.data.repository.ProfileRepository
import com.krew18.app.domain.model.DateWindow
import com.krew18.app.domain.model.JobRole
import com.krew18.app.testing.MainDispatcherRule
import com.krew18.app.testing.collectInBackground
import com.krew18.app.testing.TEST_CLOCK
import com.krew18.app.testing.TestEnvironment
import com.krew18.app.ui.talent.feed.JobFeedViewModel
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class JobFeedViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `initial state is loading`() = runTest {
        val viewModel = JobFeedViewModel(TestEnvironment().gigRepository, TEST_CLOCK)
        assertTrue(viewModel.uiState.value.isLoading)
    }

    @Test
    fun `shows all open gigs with cities and applied markers`() = runTest {
        val env = TestEnvironment().seed()
        val viewModel = JobFeedViewModel(env.gigRepository, TEST_CLOCK)
        collectInBackground(viewModel.uiState)

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals(state.totalOpenGigs, state.gigs.size)
        assertTrue(state.gigs.isNotEmpty())
        assertTrue("Lisbon" in state.cities)
        assertEquals(state.cities.sorted(), state.cities)
        // Seed data contains two applications from the demo talent.
        assertEquals(setOf("gig-002", "gig-008"), state.appliedGigIds)
    }

    @Test
    fun `role, city, pay and date filters narrow the list`() = runTest {
        val env = TestEnvironment().seed()
        val viewModel = JobFeedViewModel(env.gigRepository, TEST_CLOCK)
        collectInBackground(viewModel.uiState)

        viewModel.setRole(JobRole.CHEF)
        assertTrue(viewModel.uiState.value.gigs.all { it.role == JobRole.CHEF })

        viewModel.setCity("Lisbon")
        assertTrue(viewModel.uiState.value.gigs.all { it.city == "Lisbon" && it.role == JobRole.CHEF })

        viewModel.setMinHourlyRate(29)
        assertTrue(viewModel.uiState.value.gigs.all { it.hourlyRate >= 29 })
        assertEquals(listOf("gig-011"), viewModel.uiState.value.gigs.map { it.id })

        viewModel.setDateWindow(DateWindow.NEXT_7_DAYS)
        assertTrue(viewModel.uiState.value.gigs.isEmpty())
        assertEquals(4, viewModel.uiState.value.filter.activeCount)

        viewModel.clearFilters()
        assertEquals(viewModel.uiState.value.totalOpenGigs, viewModel.uiState.value.gigs.size)
    }

    @Test
    fun `newly applied gig is marked as applied`() = runTest {
        val env = TestEnvironment().seed()
        val viewModel = JobFeedViewModel(env.gigRepository, TEST_CLOCK)
        collectInBackground(viewModel.uiState)

        env.gigRepository.applyToGig("gig-003", "", ProfileRepository.DEFAULT_PROFILE)

        assertTrue("gig-003" in viewModel.uiState.value.appliedGigIds)
    }
}
