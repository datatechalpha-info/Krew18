package com.krew18.app.ui.employer

import androidx.lifecycle.SavedStateHandle
import com.krew18.app.domain.model.ApplicationStatus
import com.krew18.app.domain.model.CurrentUser
import com.krew18.app.domain.model.JobRole
import com.krew18.app.testing.FailingRemoteDataSource
import com.krew18.app.testing.MainDispatcherRule
import com.krew18.app.testing.collectInBackground
import com.krew18.app.testing.TEST_CLOCK
import com.krew18.app.testing.TODAY
import com.krew18.app.testing.TestEnvironment
import com.krew18.app.ui.employer.applicants.ApplicantsViewModel
import com.krew18.app.ui.employer.gigs.EmployerGigsViewModel
import com.krew18.app.ui.employer.post.PostGigField
import com.krew18.app.ui.employer.post.PostGigViewModel
import com.krew18.app.ui.navigation.Routes
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class EmployerViewModelsTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `employer gigs shows only own gigs with applicant totals`() = runTest {
        val env = TestEnvironment().seed()
        val vm = EmployerGigsViewModel(env.gigRepository)
        collectInBackground(vm.uiState)

        val state = vm.uiState.value
        assertEquals(setOf("gig-101", "gig-102"), state.gigs.map { it.gig.id }.toSet())
        assertTrue(state.gigs.all { it.gig.employerId == CurrentUser.EMPLOYER_ID })
        assertEquals(5, state.totalApplicants)
        assertEquals(0, state.totalAccepted)
    }

    @Test
    fun `post gig shows validation errors for an empty form`() = runTest {
        val env = TestEnvironment().seed()
        val vm = PostGigViewModel(env.gigRepository, TEST_CLOCK)

        vm.submit()

        val state = vm.uiState.value
        assertTrue(state.showErrors)
        assertNotNull(state.errorFor(PostGigField.TITLE))
        assertNotNull(state.errorFor(PostGigField.CITY))
        assertNotNull(state.errorFor(PostGigField.RATE))
        assertNotNull(state.errorFor(PostGigField.DESCRIPTION))
        assertNull(state.errorFor(PostGigField.OPENINGS))
        assertNull(state.postedGigId)
    }

    @Test
    fun `start date moves end date forward and past dates are rejected`() = runTest {
        val vm = PostGigViewModel(TestEnvironment().gigRepository, TEST_CLOCK)

        vm.setStartDate(TODAY.plusDays(30))
        assertEquals(TODAY.plusDays(30), vm.uiState.value.endDate)

        vm.setStartDate(TODAY.minusDays(1))
        assertNotNull(vm.uiState.value.errors[PostGigField.DATES])
    }

    @Test
    fun `valid gig is posted, form resets and gig appears for employer and talent`() = runTest {
        val env = TestEnvironment().seed()
        val vm = PostGigViewModel(env.gigRepository, TEST_CLOCK)

        vm.update {
            it.copy(
                title = "Kids club host",
                role = JobRole.EVENT_STAFF,
                city = "Goa",
                hourlyRate = "14",
                openings = "2",
                description = "Run daytime activities for young resort guests.",
                requirements = "First aid, Patience",
            )
        }
        vm.submit()

        val state = vm.uiState.value
        val postedId = state.postedGigId
        assertNotNull(postedId)
        assertEquals("", state.title)
        assertFalse(state.isSubmitting)
        assertNotNull(state.message)

        val employerGigs = env.gigRepository.observeEmployerGigs().first()
        val posted = employerGigs.single { it.gig.id == postedId }.gig
        assertEquals(listOf("First aid", "Patience"), posted.requirements)
        assertTrue(env.gigRepository.observeGigs().first().any { it.id == postedId })
    }

    @Test
    fun `post gig failure keeps the form and shows a message`() = runTest {
        val env = TestEnvironment(remote = FailingRemoteDataSource())
        val vm = PostGigViewModel(env.gigRepository, TEST_CLOCK)
        vm.update {
            it.copy(title = "Night porter", city = "Dubai", hourlyRate = "16", description = "Overnight lobby and luggage support.")
        }

        vm.submit()

        assertEquals("Night porter", vm.uiState.value.title)
        assertNull(vm.uiState.value.postedGigId)
        assertNotNull(vm.uiState.value.message)
    }

    @Test
    fun `applicants can be shortlisted, accepted and filtered`() = runTest {
        val env = TestEnvironment().seed()
        val vm = ApplicantsViewModel(SavedStateHandle(mapOf(Routes.ARG_GIG_ID to "gig-101")), env.gigRepository)
        collectInBackground(vm.uiState)

        assertEquals(3, vm.uiState.value.applicants.size)
        assertEquals(8, vm.uiState.value.openingsLeft)

        vm.shortlist("app-103")
        vm.accept("app-101")

        val state = vm.uiState.value
        assertEquals(ApplicationStatus.SHORTLISTED, state.applicants.single { it.id == "app-103" }.status)
        assertEquals(ApplicationStatus.ACCEPTED, state.applicants.single { it.id == "app-101" }.status)
        assertEquals(1, state.acceptedCount)
        assertEquals(7, state.openingsLeft)
        assertNotNull(state.message)

        vm.setStatusFilter(ApplicationStatus.SHORTLISTED)
        assertEquals(listOf("app-103"), vm.uiState.value.applicants.map { it.id })

        vm.reject("app-103")
        assertTrue(vm.uiState.value.applicants.isEmpty())
    }
}
