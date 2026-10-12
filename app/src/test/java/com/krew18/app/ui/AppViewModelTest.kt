package com.krew18.app.ui

import app.cash.turbine.test
import com.krew18.app.domain.model.ThemeMode
import com.krew18.app.domain.model.UserRole
import com.krew18.app.testing.MainDispatcherRule
import com.krew18.app.testing.TestEnvironment
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class AppViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `seeds data on start and walks through onboarding, theme and role switch`() = runTest {
        val env = TestEnvironment()
        val vm = AppViewModel(env.profileRepository, env.gigRepository)

        // init {} syncs the fake backend into the (fake) database.
        assertTrue(env.gigDao.gigs.value.isNotEmpty())

        vm.uiState.test {
            // The Loading placeholder may or may not be observed depending on subscription timing.
            val first = awaitItem().let { if (it == AppUiState.Loading) awaitItem() else it }
            assertEquals(AppUiState.Ready(role = null, themeMode = ThemeMode.SYSTEM), first)

            vm.selectRole(UserRole.TALENT)
            assertEquals(AppUiState.Ready(UserRole.TALENT, ThemeMode.SYSTEM), awaitItem())

            vm.setThemeMode(ThemeMode.DARK)
            assertEquals(AppUiState.Ready(UserRole.TALENT, ThemeMode.DARK), awaitItem())

            vm.switchRole()
            assertEquals(AppUiState.Ready(null, ThemeMode.DARK), awaitItem())
        }
    }
}
