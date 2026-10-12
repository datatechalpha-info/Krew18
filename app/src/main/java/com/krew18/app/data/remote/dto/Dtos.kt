package com.krew18.app.data.remote.dto

import kotlinx.serialization.Serializable

/** Wire format for the (hypothetical) Krew18 REST API. Dates are ISO-8601 strings. */
@Serializable
data class GigDto(
    val id: String,
    val title: String,
    val role: String,
    val employerId: String,
    val employerName: String,
    val employerType: String,
    val city: String,
    val hourlyRate: Int,
    val startDate: String,
    val endDate: String,
    val openings: Int,
    val description: String,
    val requirements: List<String> = emptyList(),
    val postedOn: String,
)

@Serializable
data class ApplicationDto(
    val id: String,
    val gigId: String,
    val talentId: String,
    val talentName: String,
    val talentHeadline: String,
    val talentSkills: List<String> = emptyList(),
    val talentLanguages: List<String> = emptyList(),
    val yearsExperience: Int,
    val coverNote: String,
    val status: String,
    val appliedAtEpochMillis: Long,
)

@Serializable
data class PostGigRequest(
    val title: String,
    val role: String,
    val employerId: String,
    val employerName: String,
    val employerType: String,
    val city: String,
    val hourlyRate: Int,
    val startDate: String,
    val endDate: String,
    val openings: Int,
    val description: String,
    val requirements: List<String>,
)

@Serializable
data class ApplyRequest(
    val gigId: String,
    val talentId: String,
    val talentName: String,
    val talentHeadline: String,
    val talentSkills: List<String>,
    val talentLanguages: List<String>,
    val yearsExperience: Int,
    val coverNote: String,
)

@Serializable
data class StatusUpdateRequest(
    val status: String,
)
