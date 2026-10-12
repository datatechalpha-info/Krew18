package com.krew18.app.domain.model

import java.time.LocalDate

enum class DateWindow(val label: String, val days: Long?) {
    ANY("Any dates", null),
    NEXT_7_DAYS("Next 7 days", 7),
    NEXT_30_DAYS("Next 30 days", 30),
    NEXT_90_DAYS("Next 90 days", 90),
}

/** Filters applied to the talent job feed. Pure logic so it is easy to unit test. */
data class GigFilter(
    val role: JobRole? = null,
    val city: String? = null,
    val minHourlyRate: Int = 0,
    val dateWindow: DateWindow = DateWindow.ANY,
    val query: String = "",
) {
    val activeCount: Int
        get() = listOf(
            role != null,
            city != null,
            minHourlyRate > 0,
            dateWindow != DateWindow.ANY,
            query.isNotBlank(),
        ).count { it }

    fun matches(gig: Gig, today: LocalDate): Boolean {
        if (role != null && gig.role != role) return false
        if (city != null && !gig.city.equals(city, ignoreCase = true)) return false
        if (gig.hourlyRate < minHourlyRate) return false
        val days = dateWindow.days
        if (days != null) {
            val windowEnd = today.plusDays(days)
            // Keep gigs whose date range overlaps [today, windowEnd].
            if (gig.endDate.isBefore(today) || gig.startDate.isAfter(windowEnd)) return false
        }
        if (query.isNotBlank()) {
            val q = query.trim()
            val haystack = listOf(gig.title, gig.employerName, gig.city, gig.role.label)
            if (haystack.none { it.contains(q, ignoreCase = true) }) return false
        }
        return true
    }
}
