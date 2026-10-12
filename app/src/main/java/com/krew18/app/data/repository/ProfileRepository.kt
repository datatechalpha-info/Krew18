package com.krew18.app.data.repository

import com.krew18.app.data.local.ProfileDao
import com.krew18.app.data.local.SessionEntity
import com.krew18.app.data.toDomain
import com.krew18.app.data.toEntity
import com.krew18.app.domain.model.Availability
import com.krew18.app.domain.model.JobRole
import com.krew18.app.domain.model.TalentProfile
import com.krew18.app.domain.model.ThemeMode
import com.krew18.app.domain.model.UserRole
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

interface ProfileRepository {
    /** The role picked during onboarding, or null if onboarding has not happened yet. */
    val role: Flow<UserRole?>
    val themeMode: Flow<ThemeMode>
    val profile: Flow<TalentProfile>

    suspend fun setRole(role: UserRole?)
    suspend fun setThemeMode(mode: ThemeMode)
    suspend fun saveProfile(profile: TalentProfile)

    companion object {
        /** Starter profile shown until the user edits it. Synthetic demo values. */
        val DEFAULT_PROFILE = TalentProfile(
            name = "Demo Talent",
            headline = "Bartender and line cook",
            primaryRole = JobRole.BARTENDER,
            city = "Lisbon",
            yearsExperience = 4,
            experienceSummary = "Two summer seasons behind a hotel bar, plus a year on a breakfast line.",
            skills = listOf("Mixology", "Cash handling", "Food safety"),
            languages = listOf("English", "Spanish"),
            availability = setOf(Availability.EVENINGS, Availability.WEEKENDS),
            openToTravel = true,
        )
    }
}

@Singleton
class LocalProfileRepository @Inject constructor(
    private val profileDao: ProfileDao,
) : ProfileRepository {

    override val role: Flow<UserRole?> = profileDao.observeSession().map { session ->
        session?.role?.let { name -> UserRole.entries.firstOrNull { it.name == name } }
    }

    override val themeMode: Flow<ThemeMode> = profileDao.observeSession().map { session ->
        ThemeMode.entries.firstOrNull { it.name == session?.themeMode } ?: ThemeMode.SYSTEM
    }

    override val profile: Flow<TalentProfile> = profileDao.observeProfile().map { entity ->
        entity?.toDomain() ?: ProfileRepository.DEFAULT_PROFILE
    }

    override suspend fun setRole(role: UserRole?) {
        val current = profileDao.getSession() ?: SessionEntity()
        profileDao.upsertSession(current.copy(role = role?.name))
    }

    override suspend fun setThemeMode(mode: ThemeMode) {
        val current = profileDao.getSession() ?: SessionEntity()
        profileDao.upsertSession(current.copy(themeMode = mode.name))
    }

    override suspend fun saveProfile(profile: TalentProfile) {
        profileDao.upsertProfile(profile.toEntity())
    }
}
