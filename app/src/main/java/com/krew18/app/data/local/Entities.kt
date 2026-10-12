package com.krew18.app.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room entities. Dates are stored as epoch days, lists as "|"-separated strings and
 * enums by name, which keeps the schema simple and avoids TypeConverters.
 */
@Entity(tableName = "gigs")
data class GigEntity(
    @PrimaryKey val id: String,
    val title: String,
    val role: String,
    val employerId: String,
    val employerName: String,
    val employerType: String,
    val city: String,
    val hourlyRate: Int,
    val startEpochDay: Long,
    val endEpochDay: Long,
    val openings: Int,
    val description: String,
    val requirements: String,
    val postedEpochDay: Long,
)

@Entity(tableName = "applications")
data class ApplicationEntity(
    @PrimaryKey val id: String,
    val gigId: String,
    val talentId: String,
    val talentName: String,
    val talentHeadline: String,
    val talentSkills: String,
    val talentLanguages: String,
    val yearsExperience: Int,
    val coverNote: String,
    val status: String,
    val appliedAtEpochMillis: Long,
)

@Entity(tableName = "talent_profile")
data class ProfileEntity(
    @PrimaryKey val id: Int = SINGLETON_ID,
    val name: String,
    val headline: String,
    val primaryRole: String,
    val city: String,
    val yearsExperience: Int,
    val experienceSummary: String,
    val skills: String,
    val languages: String,
    val availability: String,
    val openToTravel: Boolean,
) {
    companion object {
        const val SINGLETON_ID = 0
    }
}

/** Device-level session state: the role picked during onboarding and the theme choice. */
@Entity(tableName = "session")
data class SessionEntity(
    @PrimaryKey val id: Int = SINGLETON_ID,
    val role: String? = null,
    val themeMode: String = "SYSTEM",
) {
    companion object {
        const val SINGLETON_ID = 0
    }
}
