package com.krew18.app.data.repository

import com.krew18.app.data.local.GigDao
import com.krew18.app.data.remote.KrewRemoteDataSource
import com.krew18.app.data.remote.dto.ApplyRequest
import com.krew18.app.data.remote.dto.PostGigRequest
import com.krew18.app.data.toDomain
import com.krew18.app.data.toEntity
import com.krew18.app.domain.model.Application
import com.krew18.app.domain.model.ApplicationStatus
import com.krew18.app.domain.model.ApplicationWithGig
import com.krew18.app.domain.model.CurrentUser
import com.krew18.app.domain.model.EmployerGigSummary
import com.krew18.app.domain.model.Gig
import com.krew18.app.domain.model.GigDraft
import com.krew18.app.domain.model.TalentProfile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.cancellation.CancellationException

interface GigRepository {
    fun observeGigs(): Flow<List<Gig>>
    fun observeGig(gigId: String): Flow<Gig?>
    fun observeMyApplications(): Flow<List<ApplicationWithGig>>
    fun observeEmployerGigs(): Flow<List<EmployerGigSummary>>
    fun observeApplicants(gigId: String): Flow<List<Application>>

    /** Pulls data from the backend into Room the first time the app runs. */
    suspend fun syncIfEmpty(): Result<Unit>
    suspend fun applyToGig(gigId: String, coverNote: String, profile: TalentProfile): Result<Application>
    suspend fun postGig(draft: GigDraft): Result<Gig>
    suspend fun updateApplicationStatus(applicationId: String, status: ApplicationStatus): Result<Unit>
}

class AlreadyAppliedException : IllegalStateException("You have already applied to this gig.")

/**
 * Offline-first repository: Room is the single source of truth for the UI, and writes go
 * to the backend ([KrewRemoteDataSource]) first, then are persisted locally.
 */
@Singleton
class OfflineFirstGigRepository @Inject constructor(
    private val gigDao: GigDao,
    private val remote: KrewRemoteDataSource,
) : GigRepository {

    private val gigs: Flow<List<Gig>> = gigDao.observeGigs().map { list -> list.map { it.toDomain() } }
    private val applications: Flow<List<Application>> =
        gigDao.observeApplications().map { list -> list.map { it.toDomain() } }

    override fun observeGigs(): Flow<List<Gig>> = gigs

    override fun observeGig(gigId: String): Flow<Gig?> = gigs.map { list -> list.firstOrNull { it.id == gigId } }

    override fun observeMyApplications(): Flow<List<ApplicationWithGig>> =
        combine(gigs, applications) { gigList, appList ->
            val gigsById = gigList.associateBy { it.id }
            appList
                .filter { it.talentId == CurrentUser.TALENT_ID }
                .mapNotNull { app -> gigsById[app.gigId]?.let { ApplicationWithGig(app, it) } }
        }

    override fun observeEmployerGigs(): Flow<List<EmployerGigSummary>> =
        combine(gigs, applications) { gigList, appList ->
            val byGig = appList.groupBy { it.gigId }
            gigList
                .filter { it.employerId == CurrentUser.EMPLOYER_ID }
                .map { gig ->
                    val apps = byGig[gig.id].orEmpty()
                    EmployerGigSummary(
                        gig = gig,
                        applicantCount = apps.size,
                        shortlistedCount = apps.count { it.status == ApplicationStatus.SHORTLISTED },
                        acceptedCount = apps.count { it.status == ApplicationStatus.ACCEPTED },
                    )
                }
        }

    override fun observeApplicants(gigId: String): Flow<List<Application>> =
        applications.map { list -> list.filter { it.gigId == gigId } }

    override suspend fun syncIfEmpty(): Result<Unit> = runSuspendCatching {
        if (gigDao.countGigs() == 0) {
            gigDao.upsertGigs(remote.fetchGigs().map { it.toEntity() })
            gigDao.upsertApplications(remote.fetchApplications().map { it.toEntity() })
        }
    }

    override suspend fun applyToGig(
        gigId: String,
        coverNote: String,
        profile: TalentProfile,
    ): Result<Application> = runSuspendCatching {
        val alreadyApplied = applications.first().any {
            it.gigId == gigId && it.talentId == CurrentUser.TALENT_ID
        }
        if (alreadyApplied) throw AlreadyAppliedException()

        val dto = remote.apply(
            ApplyRequest(
                gigId = gigId,
                talentId = CurrentUser.TALENT_ID,
                talentName = profile.name,
                talentHeadline = profile.headline,
                talentSkills = profile.skills,
                talentLanguages = profile.languages,
                yearsExperience = profile.yearsExperience,
                coverNote = coverNote.trim(),
            ),
        )
        val entity = dto.toEntity()
        gigDao.upsertApplications(listOf(entity))
        entity.toDomain()
    }

    override suspend fun postGig(draft: GigDraft): Result<Gig> = runSuspendCatching {
        val dto = remote.postGig(
            PostGigRequest(
                title = draft.title.trim(),
                role = draft.role.name,
                employerId = CurrentUser.EMPLOYER_ID,
                employerName = CurrentUser.EMPLOYER_NAME,
                employerType = CurrentUser.EMPLOYER_TYPE.name,
                city = draft.city.trim(),
                hourlyRate = draft.hourlyRate,
                startDate = draft.startDate.toString(),
                endDate = draft.endDate.toString(),
                openings = draft.openings,
                description = draft.description.trim(),
                requirements = draft.requirements,
            ),
        )
        val entity = dto.toEntity()
        gigDao.upsertGigs(listOf(entity))
        entity.toDomain()
    }

    override suspend fun updateApplicationStatus(
        applicationId: String,
        status: ApplicationStatus,
    ): Result<Unit> = runSuspendCatching {
        remote.updateApplicationStatus(applicationId, status.name)
        gigDao.updateApplicationStatus(applicationId, status.name)
    }
}

/** Like [runCatching] but never swallows coroutine cancellation. */
internal inline fun <T> runSuspendCatching(block: () -> T): Result<T> =
    try {
        Result.success(block())
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        Result.failure(e)
    }
