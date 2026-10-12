package com.krew18.app.ui.talent

import com.krew18.app.data.repository.ProfileRepository
import com.krew18.app.domain.model.ApplicationStatus
import com.krew18.app.domain.model.Availability
import com.krew18.app.testing.MainDispatcherRule
import com.krew18.app.testing.collectInBackground
import com.krew18.app.testing.TestEnvironment
import com.krew18.app.ui.talent.applications.MyApplicationsViewModel
import com.krew18.app.ui.talent.profile.ProfileViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class MyApplicationsAndProfileViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `my applications lists only the current talent's applications with counts`() = runTest {
        val env = TestEnvironment().seed()
        val vm = MyApplicationsViewModel(env.gigRepository)
        collectInBackground(vm.uiState)

        val state = vm.uiState.value
        assertEquals(2, state.totalCount)
        assertEquals(setOf("gig-002", "gig-008"), state.items.map { it.gig.id }.toSet())
        assertEquals(1, state.countsByStatus[ApplicationStatus.SHORTLISTED])
        assertEquals(1, state.countsByStatus[ApplicationStatus.APPLIED])
    }

    @Test
    fun `status filter and live status changes`() = runTest {
        val env = TestEnvironment().seed()
        val vm = MyApplicationsViewModel(env.gigRepository)
        collectInBackground(vm.uiState)

        vm.setStatusFilter(ApplicationStatus.SHORTLISTED)
        assertEquals(listOf("gig-002"), vm.uiState.value.items.map { it.gig.id })

        // Employer accepts the talent: the talent's list updates without any refresh.
        env.gigRepository.updateApplicationStatus("app-001", ApplicationStatus.ACCEPTED)
        assertTrue(vm.uiState.value.items.isEmpty())
        vm.setStatusFilter(ApplicationStatus.ACCEPTED)
        assertEquals(listOf("app-001"), vm.uiState.value.items.map { it.application.id })
    }

    @Test
    fun `profile edit validates and saves`() = runTest {
        val env = TestEnvironment()
        val vm = ProfileViewModel(env.profileRepository)
        collectInBackground(vm.uiState)

        assertEquals(ProfileRepository.DEFAULT_PROFILE, vm.uiState.value.profile)

        vm.startEditing()
        assertTrue(vm.uiState.value.isEditing)

        vm.updateForm { it.copy(name = " ", yearsExperience = "abc") }
        vm.save()
        assertTrue(vm.uiState.value.showValidation)
        assertTrue(vm.uiState.value.isEditing)

        vm.updateForm {
            it.copy(name = "Alex Sample", yearsExperience = "7", skills = "Sushi, sushi, Knife skills, ", languages = "English")
        }
        vm.toggleAvailability(Availability.SEASONAL)
        vm.save()

        val state = vm.uiState.value
        assertFalse(state.isEditing)
        assertNotNull(state.message)
        val saved = env.profileRepository.profile.first()
        assertEquals("Alex Sample", saved.name)
        assertEquals(7, saved.yearsExperience)
        assertEquals(listOf("Sushi", "Knife skills"), saved.skills)
        assertTrue(Availability.SEASONAL in saved.availability)
    }
}
