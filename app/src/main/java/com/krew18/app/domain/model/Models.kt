package com.krew18.app.domain.model

import java.time.LocalDate
import java.time.temporal.ChronoUnit

/** Which side of the marketplace the current user is acting as. */
enum class UserRole { TALENT, EMPLOYER }

/** Theme preference; SYSTEM follows the device light/dark setting. */
enum class ThemeMode(val label: String) { SYSTEM("System"), LIGHT("Light"), DARK("Dark") }

/** Hospitality and travel roles supported by the platform. */
enum class JobRole(val label: String) {
    CHEF("Chef"),
    BARTENDER("Bartender"),
    TOUR_GUIDE("Tour guide"),
    HOUSEKEEPING("Housekeeping"),
    FRONT_DESK("Front desk"),
    EVENT_STAFF("Event staff"),
}

enum class EmployerType(val label: String) {
    HOTEL("Hotel"),
    RESORT("Resort"),
    TRAVEL_COMPANY("Travel company"),
    EVENT_ORGANISER("Event organiser"),
}

enum class ApplicationStatus(val label: String) {
    APPLIED("Applied"),
    SHORTLISTED("Shortlisted"),
    ACCEPTED("Accepted"),
    REJECTED("Not selected"),
}

enum class Availability(val label: String) {
    WEEKDAYS("Weekdays"),
    WEEKENDS("Weekends"),
    EVENINGS("Evenings"),
    SEASONAL("Seasonal contracts"),
    SHORT_NOTICE("Short notice"),
}

/** A gig (short-term job) posted by an employer. Pay is an hourly rate in USD. */
data class Gig(
    val id: String,
    val title: String,
    val role: JobRole,
    val employerId: String,
    val employerName: String,
    val employerType: EmployerType,
    val city: String,
    val hourlyRate: Int,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val openings: Int,
    val description: String,
    val requirements: List<String>,
    val postedOn: LocalDate,
) {
    val durationDays: Long get() = ChronoUnit.DAYS.between(startDate, endDate) + 1
}

data class Application(
    val id: String,
    val gigId: String,
    val talentId: String,
    val talentName: String,
    val talentHeadline: String,
    val talentSkills: List<String>,
    val talentLanguages: List<String>,
    val yearsExperience: Int,
    val coverNote: String,
    val status: ApplicationStatus,
    val appliedAtEpochMillis: Long,
)

data class ApplicationWithGig(
    val application: Application,
    val gig: Gig,
)

data class EmployerGigSummary(
    val gig: Gig,
    val applicantCount: Int,
    val shortlistedCount: Int,
    val acceptedCount: Int,
)

data class TalentProfile(
    val name: String,
    val headline: String,
    val primaryRole: JobRole,
    val city: String,
    val yearsExperience: Int,
    val experienceSummary: String,
    val skills: List<String>,
    val languages: List<String>,
    val availability: Set<Availability>,
    val openToTravel: Boolean,
)

/** Input for posting a new gig as an employer. */
data class GigDraft(
    val title: String,
    val role: JobRole,
    val city: String,
    val hourlyRate: Int,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val openings: Int,
    val description: String,
    val requirements: List<String>,
)

/** Identifiers of the single local demo user (there is no sign-in in this demo). */
object CurrentUser {
    const val TALENT_ID = "talent-self"
    const val EMPLOYER_ID = "employer-self"
    const val EMPLOYER_NAME = "Seabreeze Boutique Hotel"
    val EMPLOYER_TYPE = EmployerType.HOTEL
}
