package com.krew18.app.data

import com.krew18.app.data.local.ApplicationEntity
import com.krew18.app.data.local.GigEntity
import com.krew18.app.data.local.ProfileEntity
import com.krew18.app.data.remote.dto.ApplicationDto
import com.krew18.app.data.remote.dto.GigDto
import com.krew18.app.domain.model.Application
import com.krew18.app.domain.model.ApplicationStatus
import com.krew18.app.domain.model.Availability
import com.krew18.app.domain.model.EmployerType
import com.krew18.app.domain.model.Gig
import com.krew18.app.domain.model.JobRole
import com.krew18.app.domain.model.TalentProfile
import java.time.LocalDate

private const val LIST_SEPARATOR = "|"

internal fun List<String>.joinForDb(): String =
    map { it.replace(LIST_SEPARATOR, "/").trim() }.filter { it.isNotEmpty() }.joinToString(LIST_SEPARATOR)

internal fun String.splitFromDb(): List<String> =
    if (isBlank()) emptyList() else split(LIST_SEPARATOR).map { it.trim() }.filter { it.isNotEmpty() }

private inline fun <reified T : Enum<T>> enumOrDefault(name: String, default: T): T =
    enumValues<T>().firstOrNull { it.name == name } ?: default

// region Gig

fun GigDto.toEntity(): GigEntity = GigEntity(
    id = id,
    title = title,
    role = role,
    employerId = employerId,
    employerName = employerName,
    employerType = employerType,
    city = city,
    hourlyRate = hourlyRate,
    startEpochDay = LocalDate.parse(startDate).toEpochDay(),
    endEpochDay = LocalDate.parse(endDate).toEpochDay(),
    openings = openings,
    description = description,
    requirements = requirements.joinForDb(),
    postedEpochDay = LocalDate.parse(postedOn).toEpochDay(),
)

fun GigEntity.toDomain(): Gig = Gig(
    id = id,
    title = title,
    role = enumOrDefault(role, JobRole.EVENT_STAFF),
    employerId = employerId,
    employerName = employerName,
    employerType = enumOrDefault(employerType, EmployerType.HOTEL),
    city = city,
    hourlyRate = hourlyRate,
    startDate = LocalDate.ofEpochDay(startEpochDay),
    endDate = LocalDate.ofEpochDay(endEpochDay),
    openings = openings,
    description = description,
    requirements = requirements.splitFromDb(),
    postedOn = LocalDate.ofEpochDay(postedEpochDay),
)

// endregion

// region Application

fun ApplicationDto.toEntity(): ApplicationEntity = ApplicationEntity(
    id = id,
    gigId = gigId,
    talentId = talentId,
    talentName = talentName,
    talentHeadline = talentHeadline,
    talentSkills = talentSkills.joinForDb(),
    talentLanguages = talentLanguages.joinForDb(),
    yearsExperience = yearsExperience,
    coverNote = coverNote,
    status = status,
    appliedAtEpochMillis = appliedAtEpochMillis,
)

fun ApplicationEntity.toDomain(): Application = Application(
    id = id,
    gigId = gigId,
    talentId = talentId,
    talentName = talentName,
    talentHeadline = talentHeadline,
    talentSkills = talentSkills.splitFromDb(),
    talentLanguages = talentLanguages.splitFromDb(),
    yearsExperience = yearsExperience,
    coverNote = coverNote,
    status = enumOrDefault(status, ApplicationStatus.APPLIED),
    appliedAtEpochMillis = appliedAtEpochMillis,
)

// endregion

// region Profile

fun ProfileEntity.toDomain(): TalentProfile = TalentProfile(
    name = name,
    headline = headline,
    primaryRole = enumOrDefault(primaryRole, JobRole.BARTENDER),
    city = city,
    yearsExperience = yearsExperience,
    experienceSummary = experienceSummary,
    skills = skills.splitFromDb(),
    languages = languages.splitFromDb(),
    availability = availability.splitFromDb()
        .mapNotNull { name -> Availability.entries.firstOrNull { it.name == name } }
        .toSet(),
    openToTravel = openToTravel,
)

fun TalentProfile.toEntity(): ProfileEntity = ProfileEntity(
    name = name,
    headline = headline,
    primaryRole = primaryRole.name,
    city = city,
    yearsExperience = yearsExperience,
    experienceSummary = experienceSummary,
    skills = skills.joinForDb(),
    languages = languages.joinForDb(),
    availability = availability.map { it.name }.sorted().joinForDb(),
    openToTravel = openToTravel,
)

// endregion
