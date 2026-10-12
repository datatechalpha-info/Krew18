package com.krew18.app.data.repository

import com.krew18.app.domain.model.Availability
import com.krew18.app.domain.model.ThemeMode
import com.krew18.app.domain.model.UserRole
import com.krew18.app.testing.TestEnvironment
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class LocalProfileRepositoryTest {

    @Test
    fun `defaults before anything is saved`() = runTest {
        val repo = TestEnvironment().profileRepository

        assertNull(repo.role.first())
        assertEquals(ThemeMode.SYSTEM, repo.themeMode.first())
        assertEquals(ProfileRepository.DEFAULT_PROFILE, repo.profile.first())
    }

    @Test
    fun `role and theme are stored independently`() = runTest {
        val repo = TestEnvironment().profileRepository

        repo.setThemeMode(ThemeMode.DARK)
        repo.setRole(UserRole.EMPLOYER)

        assertEquals(UserRole.EMPLOYER, repo.role.first())
        assertEquals(ThemeMode.DARK, repo.themeMode.first())

        repo.setRole(null)
        assertNull(repo.role.first())
        assertEquals(ThemeMode.DARK, repo.themeMode.first())
    }

    @Test
    fun `saved profile round-trips through the database entity`() = runTest {
        val repo = TestEnvironment().profileRepository
        val edited = ProfileRepository.DEFAULT_PROFILE.copy(
            name = "Sam Example",
            skills = listOf("Sushi", "Knife skills"),
            languages = listOf("Japanese"),
            availability = setOf(Availability.SEASONAL, Availability.WEEKDAYS),
            openToTravel = false,
        )

        repo.saveProfile(edited)

        assertEquals(edited, repo.profile.first())
    }
}
