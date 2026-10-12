package com.krew18.app.testing

import com.krew18.app.data.local.ApplicationEntity
import com.krew18.app.data.local.GigDao
import com.krew18.app.data.local.GigEntity
import com.krew18.app.data.local.ProfileDao
import com.krew18.app.data.local.ProfileEntity
import com.krew18.app.data.local.SessionEntity
import com.krew18.app.data.remote.KrewRemoteDataSource
import com.krew18.app.data.remote.dto.ApplicationDto
import com.krew18.app.data.remote.dto.ApplyRequest
import com.krew18.app.data.remote.dto.GigDto
import com.krew18.app.data.remote.dto.PostGigRequest
import com.krew18.app.data.remote.fake.FakeKrewRemoteDataSource
import com.krew18.app.data.repository.LocalProfileRepository
import com.krew18.app.data.repository.OfflineFirstGigRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.rules.TestWatcher
import org.junit.runner.Description
import java.io.IOException
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

/** Fixed "now" for deterministic tests: 1 March 2026, UTC. */
val TEST_CLOCK: Clock = Clock.fixed(Instant.parse("2026-03-01T10:00:00Z"), ZoneOffset.UTC)
val TODAY: LocalDate = LocalDate.now(TEST_CLOCK)

/**
 * Keeps a WhileSubscribed StateFlow active for the duration of the test so that `.value`
 * reflects upstream changes immediately.
 */
@OptIn(ExperimentalCoroutinesApi::class)
fun TestScope.collectInBackground(flow: Flow<*>): Job =
    backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { flow.collect {} }

@OptIn(ExperimentalCoroutinesApi::class)
class MainDispatcherRule(
    val testDispatcher: TestDispatcher = UnconfinedTestDispatcher(),
) : TestWatcher() {
    override fun starting(description: Description) = Dispatchers.setMain(testDispatcher)
    override fun finished(description: Description) = Dispatchers.resetMain()
}

/** In-memory [GigDao] mirroring the ordering of the real Room queries. */
class FakeGigDao : GigDao {
    val gigs = MutableStateFlow<List<GigEntity>>(emptyList())
    val applications = MutableStateFlow<List<ApplicationEntity>>(emptyList())

    override fun observeGigs(): Flow<List<GigEntity>> =
        gigs.map { list -> list.sortedWith(compareBy<GigEntity> { it.startEpochDay }.thenBy { it.id }) }

    override suspend fun countGigs(): Int = gigs.value.size

    override suspend fun upsertGigs(gigs: List<GigEntity>) {
        this.gigs.update { current -> (current.associateBy { it.id } + gigs.associateBy { it.id }).values.toList() }
    }

    override fun observeApplications(): Flow<List<ApplicationEntity>> = applications.map { list ->
        list.sortedWith(compareByDescending<ApplicationEntity> { it.appliedAtEpochMillis }.thenBy { it.id })
    }

    override suspend fun upsertApplications(applications: List<ApplicationEntity>) {
        this.applications.update { current ->
            (current.associateBy { it.id } + applications.associateBy { it.id }).values.toList()
        }
    }

    override suspend fun updateApplicationStatus(applicationId: String, status: String) {
        applications.update { list -> list.map { if (it.id == applicationId) it.copy(status = status) else it } }
    }
}

class FakeProfileDao : ProfileDao {
    val profile = MutableStateFlow<ProfileEntity?>(null)
    val session = MutableStateFlow<SessionEntity?>(null)

    override fun observeProfile(): Flow<ProfileEntity?> = profile
    override suspend fun upsertProfile(profile: ProfileEntity) {
        this.profile.value = profile
    }

    override fun observeSession(): Flow<SessionEntity?> = session
    override suspend fun getSession(): SessionEntity? = session.value
    override suspend fun upsertSession(session: SessionEntity) {
        this.session.value = session
    }
}

/** Remote that fails every call, for error-path tests. */
class FailingRemoteDataSource : KrewRemoteDataSource {
    override suspend fun fetchGigs(): List<GigDto> = throw IOException("offline")
    override suspend fun fetchApplications(): List<ApplicationDto> = throw IOException("offline")
    override suspend fun postGig(request: PostGigRequest): GigDto = throw IOException("offline")
    override suspend fun apply(request: ApplyRequest): ApplicationDto = throw IOException("offline")
    override suspend fun updateApplicationStatus(applicationId: String, status: String) = throw IOException("offline")
}

/** A repository wired to in-memory fakes, plus handles to inspect them. */
class TestEnvironment(
    remote: KrewRemoteDataSource = FakeKrewRemoteDataSource(TEST_CLOCK, latencyMillis = 0),
) {
    val gigDao = FakeGigDao()
    val profileDao = FakeProfileDao()
    val gigRepository = OfflineFirstGigRepository(gigDao, remote)
    val profileRepository = LocalProfileRepository(profileDao)

    /** Loads the synthetic seed data, as the app does on first launch. */
    suspend fun seed(): TestEnvironment {
        gigRepository.syncIfEmpty().getOrThrow()
        return this
    }
}
