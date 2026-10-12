package com.krew18.app.ui.talent

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.krew18.app.domain.model.ApplicationStatus
import com.krew18.app.testing.MainDispatcherRule
import com.krew18.app.testing.collectInBackground
import com.krew18.app.testing.TestEnvironment
import com.krew18.app.ui.navigation.Routes
import com.krew18.app.ui.talent.detail.JobDetailViewModel
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class JobDetailViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private fun viewModel(env: TestEnvironment, gigId: String) = JobDetailViewModel(
        savedStateHandle = SavedStateHandle(mapOf(Routes.ARG_GIG_ID to gigId)),
        gigRepository = env.gigRepository,
        profileRepository = env.profileRepository,
    )

    @Test
    fun `loads the gig and allows applying when not applied yet`() = runTest {
        val env = TestEnvironment().seed()
        val vm = viewModel(env, "gig-001")
        collectInBackground(vm.uiState)

        val state = vm.uiState.value
        assertEquals("gig-001", state.gig?.id)
        assertNull(state.myApplication)
        assertTrue(state.canApply)
    }

    @Test
    fun `shows existing application status`() = runTest {
        val env = TestEnvironment().seed()
        val vm = viewModel(env, "gig-002")
        collectInBackground(vm.uiState)

        assertEquals(ApplicationStatus.SHORTLISTED, vm.uiState.value.myApplication?.status)
        assertFalse(vm.uiState.value.canApply)
    }

    @Test
    fun `submitting an application updates state and shows a message`() = runTest {
        val env = TestEnvironment().seed()
        val vm = viewModel(env, "gig-001")

        vm.uiState.test {
            val loaded = awaitItem().let { if (it.isLoading) awaitItem() else it }
            assertNotNull(loaded.gig)

            vm.openApplyDialog()
            assertTrue(awaitItem().showApplyDialog)
            vm.onCoverNoteChange("Happy to cover weekends")
            assertEquals("Happy to cover weekends", awaitItem().coverNote)

            vm.submitApplication()
            val done = expectMostRecentItem()
            assertEquals(ApplicationStatus.APPLIED, done.myApplication?.status)
            assertFalse(done.showApplyDialog)
            assertFalse(done.isSubmitting)
            assertNotNull(done.message)

            vm.messageShown()
            assertNull(awaitItem().message)
        }
    }

    @Test
    fun `missing gig results in null gig`() = runTest {
        val env = TestEnvironment().seed()
        val vm = viewModel(env, "does-not-exist")
        collectInBackground(vm.uiState)

        assertFalse(vm.uiState.value.isLoading)
        assertNull(vm.uiState.value.gig)
        assertFalse(vm.uiState.value.canApply)
    }
}
