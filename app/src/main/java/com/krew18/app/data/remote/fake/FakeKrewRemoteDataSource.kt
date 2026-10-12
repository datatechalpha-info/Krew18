package com.krew18.app.data.remote.fake

import com.krew18.app.data.remote.KrewRemoteDataSource
import com.krew18.app.data.remote.dto.ApplicationDto
import com.krew18.app.data.remote.dto.ApplyRequest
import com.krew18.app.data.remote.dto.GigDto
import com.krew18.app.data.remote.dto.PostGigRequest
import com.krew18.app.domain.model.ApplicationStatus
import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.time.Clock
import java.time.LocalDate

/**
 * In-app fake backend seeded with [SeedData]. Used by default so the app works fully
 * offline without any server. It simulates a little network latency and echoes writes back
 * the way a real API would.
 */
class FakeKrewRemoteDataSource(
    private val clock: Clock,
    private val latencyMillis: Long = DEFAULT_LATENCY_MILLIS,
) : KrewRemoteDataSource {

    private val mutex = Mutex()
    private var idCounter = 0
    private val gigs: MutableList<GigDto> by lazy { SeedData.gigs(LocalDate.now(clock)).toMutableList() }
    private val applications: MutableList<ApplicationDto> by lazy {
        SeedData.applications(LocalDate.now(clock)).toMutableList()
    }

    override suspend fun fetchGigs(): List<GigDto> = simulate { gigs.toList() }

    override suspend fun fetchApplications(): List<ApplicationDto> = simulate { applications.toList() }

    override suspend fun postGig(request: PostGigRequest): GigDto = simulate {
        val gig = GigDto(
            id = nextId("gig"),
            title = request.title,
            role = request.role,
            employerId = request.employerId,
            employerName = request.employerName,
            employerType = request.employerType,
            city = request.city,
            hourlyRate = request.hourlyRate,
            startDate = request.startDate,
            endDate = request.endDate,
            openings = request.openings,
            description = request.description,
            requirements = request.requirements,
            postedOn = LocalDate.now(clock).toString(),
        )
        gigs.add(gig)
        gig
    }

    override suspend fun apply(request: ApplyRequest): ApplicationDto = simulate {
        val application = ApplicationDto(
            id = nextId("app"),
            gigId = request.gigId,
            talentId = request.talentId,
            talentName = request.talentName,
            talentHeadline = request.talentHeadline,
            talentSkills = request.talentSkills,
            talentLanguages = request.talentLanguages,
            yearsExperience = request.yearsExperience,
            coverNote = request.coverNote,
            status = ApplicationStatus.APPLIED.name,
            appliedAtEpochMillis = clock.millis(),
        )
        applications.add(application)
        application
    }

    override suspend fun updateApplicationStatus(applicationId: String, status: String) {
        simulate {
            val index = applications.indexOfFirst { it.id == applicationId }
            // Items created in a previous app session only live in Room; accept the update anyway.
            if (index >= 0) applications[index] = applications[index].copy(status = status)
        }
    }

    private fun nextId(prefix: String): String = "$prefix-local-${clock.millis()}-${++idCounter}"

    private suspend fun <T> simulate(block: () -> T): T {
        if (latencyMillis > 0) delay(latencyMillis)
        return mutex.withLock { block() }
    }

    companion object {
        const val DEFAULT_LATENCY_MILLIS = 350L
    }
}
