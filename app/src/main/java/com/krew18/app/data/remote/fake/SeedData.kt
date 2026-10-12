package com.krew18.app.data.remote.fake

import com.krew18.app.data.remote.dto.ApplicationDto
import com.krew18.app.data.remote.dto.GigDto
import com.krew18.app.domain.model.ApplicationStatus
import com.krew18.app.domain.model.CurrentUser
import com.krew18.app.domain.model.EmployerType
import com.krew18.app.domain.model.JobRole
import java.time.LocalDate
import java.time.ZoneOffset

/**
 * Synthetic demo data. All employers, people and numbers below are made up for the demo.
 * Dates are generated relative to "today" so the feed always looks current.
 */
object SeedData {

    private data class SeedGig(
        val id: String,
        val title: String,
        val role: JobRole,
        val employerId: String,
        val employerName: String,
        val employerType: EmployerType,
        val city: String,
        val hourlyRate: Int,
        val startInDays: Long,
        val lengthDays: Long,
        val openings: Int,
        val description: String,
        val requirements: List<String>,
    )

    private val seedGigs = listOf(
        SeedGig(
            "gig-001", "Sous chef for summer terrace", JobRole.CHEF,
            "emp-azure", "Azure Bay Resort", EmployerType.RESORT, "Lisbon", 28, 5, 60, 2,
            "Support the head chef on a Mediterranean small-plates menu for a busy seasonal terrace.",
            listOf("3+ years in a professional kitchen", "Food safety certificate", "Comfortable with 120+ covers"),
        ),
        SeedGig(
            "gig-002", "Cocktail bartender - rooftop bar", JobRole.BARTENDER,
            "emp-meridian", "Grand Meridian Hotel", EmployerType.HOTEL, "Dubai", 24, 3, 30, 3,
            "Craft classic and signature cocktails for hotel guests on a rooftop lounge.",
            listOf("Cocktail knowledge", "Guest-facing English", "Evening availability"),
        ),
        SeedGig(
            "gig-003", "Old town walking tour guide", JobRole.TOUR_GUIDE,
            "emp-oldtown", "Old Town Walking Tours", EmployerType.TRAVEL_COMPANY, "Barcelona", 20, 10, 45, 4,
            "Lead small-group history walks through the gothic quarter, twice daily.",
            listOf("Fluent English plus one more language", "Storytelling skills", "Local history knowledge"),
        ),
        SeedGig(
            "gig-004", "Housekeeping attendant", JobRole.HOUSEKEEPING,
            "emp-palm", "Palm Cove Resort & Spa", EmployerType.RESORT, "Goa", 12, 2, 90, 6,
            "Keep villas and suites guest-ready during peak season.",
            listOf("Attention to detail", "Previous hotel housekeeping a plus"),
        ),
        SeedGig(
            "gig-005", "Front desk agent - night shift", JobRole.FRONT_DESK,
            "emp-harbour", "Harbour Lane Hotel", EmployerType.HOTEL, "Cape Town", 18, 7, 30, 1,
            "Handle late check-ins, guest requests and night audit for a 90-room city hotel.",
            listOf("PMS experience", "Calm under pressure", "Night availability"),
        ),
        SeedGig(
            "gig-006", "Event crew - music festival", JobRole.EVENT_STAFF,
            "emp-lumen", "Lumen Events Co.", EmployerType.EVENT_ORGANISER, "Lisbon", 16, 20, 4, 25,
            "Wristband scanning, crowd guidance and hospitality tent support across a 4-day festival.",
            listOf("Standing for long shifts", "Team player"),
        ),
        SeedGig(
            "gig-007", "Breakfast line cook", JobRole.CHEF,
            "emp-harbour", "Harbour Lane Hotel", EmployerType.HOTEL, "Cape Town", 19, 1, 21, 2,
            "Run the egg station and hot buffet for hotel breakfast service.",
            listOf("Early mornings", "Speed and consistency"),
        ),
        SeedGig(
            "gig-008", "Beach bar bartender", JobRole.BARTENDER,
            "emp-palm", "Palm Cove Resort & Spa", EmployerType.RESORT, "Goa", 14, 12, 75, 2,
            "Serve guests at the beach bar: fresh juices, beer, wine and simple cocktails.",
            listOf("Friendly and outgoing", "Basic mixology"),
        ),
        SeedGig(
            "gig-009", "Desert safari tour guide", JobRole.TOUR_GUIDE,
            "emp-dunes", "Golden Dunes Travel", EmployerType.TRAVEL_COMPANY, "Dubai", 26, 15, 60, 2,
            "Host evening desert safari groups, from pickup briefing to dinner camp.",
            listOf("Arabic or Russian is a plus", "First-aid certificate"),
        ),
        SeedGig(
            "gig-010", "Conference registration staff", JobRole.EVENT_STAFF,
            "emp-lumen", "Lumen Events Co.", EmployerType.EVENT_ORGANISER, "Barcelona", 17, 35, 3, 10,
            "Badge printing, registration desk and attendee help for a 3-day tech conference.",
            listOf("Tidy appearance", "Comfortable with tablets"),
        ),
        SeedGig(
            "gig-011", "Pastry chef - wedding season", JobRole.CHEF,
            "emp-azure", "Azure Bay Resort", EmployerType.RESORT, "Lisbon", 30, 40, 120, 1,
            "Plated desserts and wedding cakes for resort weddings, Thursday to Sunday.",
            listOf("Pastry training", "Portfolio of past work"),
        ),
        SeedGig(
            "gig-012", "Guest relations / concierge", JobRole.FRONT_DESK,
            "emp-meridian", "Grand Meridian Hotel", EmployerType.HOTEL, "Dubai", 22, 21, 90, 1,
            "Restaurant bookings, tours and special requests for premium-floor guests.",
            listOf("Two or more languages", "Luxury hospitality background"),
        ),
        SeedGig(
            "gig-013", "Room attendant - weekends", JobRole.HOUSEKEEPING,
            "emp-riverside", "Riverside City Hostel", EmployerType.HOTEL, "Bangkok", 9, 4, 60, 3,
            "Weekend turnover of dorms and private rooms in a social hostel.",
            listOf("Weekend availability"),
        ),
        SeedGig(
            "gig-014", "Street food tour host", JobRole.TOUR_GUIDE,
            "emp-riverside", "Riverside City Hostel", EmployerType.HOTEL, "Bangkok", 15, 8, 30, 2,
            "Lead evening street-food walks for hostel guests.",
            listOf("Knows the local food scene", "Good English"),
        ),
        // Gigs owned by the local demo employer, so the employer side has data to show.
        SeedGig(
            "gig-101", "Banquet servers for gala dinner", JobRole.EVENT_STAFF,
            CurrentUser.EMPLOYER_ID, CurrentUser.EMPLOYER_NAME, CurrentUser.EMPLOYER_TYPE, "Lisbon", 18, 14, 2, 8,
            "Silver-service banquet support for a 250-guest gala in the hotel ballroom.",
            listOf("Silver service experience", "Black trousers and shoes"),
        ),
        SeedGig(
            "gig-102", "Lobby bar bartender", JobRole.BARTENDER,
            CurrentUser.EMPLOYER_ID, CurrentUser.EMPLOYER_NAME, CurrentUser.EMPLOYER_TYPE, "Lisbon", 21, 6, 45, 1,
            "Evening bartender for a 30-seat lobby bar focused on Portuguese wines and spritzes.",
            listOf("Wine knowledge", "Evenings and weekends"),
        ),
    )

    fun gigs(today: LocalDate): List<GigDto> = seedGigs.map { seed ->
        val start = today.plusDays(seed.startInDays)
        GigDto(
            id = seed.id,
            title = seed.title,
            role = seed.role.name,
            employerId = seed.employerId,
            employerName = seed.employerName,
            employerType = seed.employerType.name,
            city = seed.city,
            hourlyRate = seed.hourlyRate,
            startDate = start.toString(),
            endDate = start.plusDays(seed.lengthDays - 1).toString(),
            openings = seed.openings,
            description = seed.description,
            requirements = seed.requirements,
            postedOn = today.minusDays(seed.id.takeLast(1).toLong() % 5).toString(),
        )
    }

    fun applications(today: LocalDate): List<ApplicationDto> {
        fun millis(daysAgo: Long) =
            today.minusDays(daysAgo).atStartOfDay().toInstant(ZoneOffset.UTC).toEpochMilli()

        return listOf(
            // The local talent's own applications.
            ApplicationDto(
                "app-001", "gig-002", CurrentUser.TALENT_ID, "Demo Talent", "Bartender and line cook",
                listOf("Mixology", "Cash handling"), listOf("English", "Spanish"), 4,
                "I have run a hotel bar for two summers and love rooftop service.",
                ApplicationStatus.SHORTLISTED.name, millis(3),
            ),
            ApplicationDto(
                "app-002", "gig-008", CurrentUser.TALENT_ID, "Demo Talent", "Bartender and line cook",
                listOf("Mixology", "Cash handling"), listOf("English", "Spanish"), 4,
                "Available for the full season.",
                ApplicationStatus.APPLIED.name, millis(1),
            ),
            // Applicants for the demo employer's gigs (fictional people).
            ApplicationDto(
                "app-101", "gig-101", "talent-maya", "Maya R.", "Banquet captain",
                listOf("Silver service", "Team lead"), listOf("English", "Portuguese"), 6,
                "Led banquet teams of 20+ at conference hotels.",
                ApplicationStatus.SHORTLISTED.name, millis(2),
            ),
            ApplicationDto(
                "app-102", "gig-101", "talent-diego", "Diego L.", "Waiter, fine dining",
                listOf("Wine service", "Plated service"), listOf("Spanish", "English"), 3,
                "Fine-dining background, happy to work the full gala.",
                ApplicationStatus.APPLIED.name, millis(2),
            ),
            ApplicationDto(
                "app-103", "gig-101", "talent-aisha", "Aisha K.", "Event hostess",
                listOf("Guest greeting", "Seating plans"), listOf("English", "French", "Arabic"), 2,
                "Can also help with guest arrival and seating.",
                ApplicationStatus.APPLIED.name, millis(1),
            ),
            ApplicationDto(
                "app-104", "gig-102", "talent-tomas", "Tomas P.", "Bartender",
                listOf("Natural wines", "Classic cocktails"), listOf("Portuguese", "English"), 5,
                "Ran the wine list at a neighbourhood bar for three years.",
                ApplicationStatus.APPLIED.name, millis(1),
            ),
            ApplicationDto(
                "app-105", "gig-102", "talent-leila", "Leila N.", "Barista and bartender",
                listOf("Coffee", "Spritzes"), listOf("English", "Italian"), 2,
                "Evenings and weekends work well for me.",
                ApplicationStatus.REJECTED.name, millis(4),
            ),
        )
    }
}
