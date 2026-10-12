package com.krew18.app.data.repository

import app.cash.turbine.test
import com.krew18.app.data.remote.fake.SeedData
import com.krew18.app.domain.model.ApplicationStatus
import com.krew18.app.domain.model.CurrentUser
import com.krew18.app.domain.model.GigDraft
import com.krew18.app.domain.model.JobRole
import com.krew18.app.testing.FailingRemoteDataSource
import com.krew18.app.testing.TODAY
import com.krew18.app.testing.TestEnvironment
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class OfflineFirstGigRepositoryTest {

    private val profile = ProfileRepository.DEFAULT_PROFILE

    @Test
    fun `syncIfEmpty loads seed gigs and applications into the database`() = runTest {
        val env = TestEnvironment()

        env.gigRepository.syncIfEmpty()

        assertEquals(SeedData.gigs(TODAY).size, env.gigDao.gigs.value.size)
        assertEquals(SeedData.applications(TODAY).size, env.gigDao.applications.value.size)
    }

    @Test
    fun `syncIfEmpty does not overwrite local data`() = runTest {
        val env = TestEnvironment().seed()
        env.gigRepository.updateApplicationStatus("app-102", ApplicationStatus.ACCEPTED)

        env.gigRepository.syncIfEmpty()

        val status = env.gigDao.applications.value.first { it.id == "app-102" }.status
        assertEquals(ApplicationStatus.ACCEPTED.name, status)
    }

    @Test
    fun `gigs are exposed sorted by start date`() = runTest {
        val env = TestEnvironment().seed()

        val gigs = env.gigRepository.observeGigs().first()

        assertEquals(gigs.sortedBy { it.startDate }.map { it.id }, gigs.map { it.id })
    }

    @Test
    fun `applying stores an APPLIED application for the current talent`() = runTest {
        val env = TestEnvironment().seed()

        env.gigRepository.observeMyApplications().test {
            val before = awaitItem()
            assertTrue(before.none { it.gig.id == "gig-001" })

            val result = env.gigRepository.applyToGig("gig-001", "  Keen to help!  ", profile)

            assertTrue(result.isSuccess)
            val after = awaitItem()
            val mine = after.single { it.gig.id == "gig-001" }.application
            assertEquals(ApplicationStatus.APPLIED, mine.status)
            assertEquals(CurrentUser.TALENT_ID, mine.talentId)
            assertEquals("Keen to help!", mine.coverNote)
            assertEquals(profile.skills, mine.talentSkills)
        }
    }

    @Test
    fun `applying twice to the same gig fails`() = runTest {
        val env = TestEnvironment().seed()
        env.gigRepository.applyToGig("gig-001", "", profile)

        val second = env.gigRepository.applyToGig("gig-001", "", profile)

        assertTrue(second.exceptionOrNull() is AlreadyAppliedException)
        assertEquals(1, env.gigDao.applications.value.count { it.gigId == "gig-001" })
    }

    @Test
    fun `posted gig belongs to the current employer`() = runTest {
        val env = TestEnvironment().seed()
        val draft = GigDraft(
            title = "Pool bar attendant",
            role = JobRole.BARTENDER,
            city = "Goa",
            hourlyRate = 15,
            startDate = TODAY.plusDays(3),
            endDate = TODAY.plusDays(10),
            openings = 2,
            description = "Serve drinks at the pool bar.",
            requirements = listOf("Friendly"),
        )

        val gig = env.gigRepository.postGig(draft).getOrThrow()

        assertEquals(CurrentUser.EMPLOYER_ID, gig.employerId)
        val summary = env.gigRepository.observeEmployerGigs().first().single { it.gig.id == gig.id }
        assertEquals(0, summary.applicantCount)
        assertEquals("Pool bar attendant", summary.gig.title)
    }

    @Test
    fun `status updates are reflected in employer summaries and applicants`() = runTest {
        val env = TestEnvironment().seed()

        env.gigRepository.updateApplicationStatus("app-102", ApplicationStatus.ACCEPTED).getOrThrow()

        val summary = env.gigRepository.observeEmployerGigs().first().single { it.gig.id == "gig-101" }
        assertEquals(3, summary.applicantCount)
        assertEquals(1, summary.acceptedCount)
        assertEquals(1, summary.shortlistedCount)
        val applicant = env.gigRepository.observeApplicants("gig-101").first().single { it.id == "app-102" }
        assertEquals(ApplicationStatus.ACCEPTED, applicant.status)
    }

    @Test
    fun `remote failures are returned as Result failure and nothing is stored`() = runTest {
        val env = TestEnvironment(remote = FailingRemoteDataSource())

        assertTrue(env.gigRepository.syncIfEmpty().isFailure)
        assertTrue(env.gigRepository.applyToGig("gig-001", "", profile).isFailure)
        assertTrue(env.gigDao.gigs.value.isEmpty())
        assertTrue(env.gigDao.applications.value.isEmpty())
    }
}
