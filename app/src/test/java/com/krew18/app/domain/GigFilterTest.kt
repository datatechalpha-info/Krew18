package com.krew18.app.domain

import com.krew18.app.domain.model.DateWindow
import com.krew18.app.domain.model.EmployerType
import com.krew18.app.domain.model.Gig
import com.krew18.app.domain.model.GigFilter
import com.krew18.app.domain.model.JobRole
import com.krew18.app.testing.TODAY
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class GigFilterTest {

    private val gig = Gig(
        id = "g1",
        title = "Cocktail bartender",
        role = JobRole.BARTENDER,
        employerId = "e1",
        employerName = "Test Hotel",
        employerType = EmployerType.HOTEL,
        city = "Lisbon",
        hourlyRate = 20,
        startDate = TODAY.plusDays(10),
        endDate = TODAY.plusDays(20),
        openings = 1,
        description = "desc",
        requirements = emptyList(),
        postedOn = TODAY,
    )

    @Test
    fun `empty filter matches everything`() {
        assertTrue(GigFilter().matches(gig, TODAY))
        assertEquals(0, GigFilter().activeCount)
    }

    @Test
    fun `role filter`() {
        assertTrue(GigFilter(role = JobRole.BARTENDER).matches(gig, TODAY))
        assertFalse(GigFilter(role = JobRole.CHEF).matches(gig, TODAY))
    }

    @Test
    fun `city filter is case insensitive`() {
        assertTrue(GigFilter(city = "lisbon").matches(gig, TODAY))
        assertFalse(GigFilter(city = "Dubai").matches(gig, TODAY))
    }

    @Test
    fun `minimum pay filter is inclusive`() {
        assertTrue(GigFilter(minHourlyRate = 20).matches(gig, TODAY))
        assertFalse(GigFilter(minHourlyRate = 21).matches(gig, TODAY))
    }

    @Test
    fun `date window keeps gigs overlapping the window`() {
        assertFalse(GigFilter(dateWindow = DateWindow.NEXT_7_DAYS).matches(gig, TODAY))
        assertTrue(GigFilter(dateWindow = DateWindow.NEXT_30_DAYS).matches(gig, TODAY))
        // Already running gig still overlaps a short window.
        val running = gig.copy(startDate = TODAY.minusDays(2), endDate = TODAY.plusDays(2))
        assertTrue(GigFilter(dateWindow = DateWindow.NEXT_7_DAYS).matches(running, TODAY))
    }

    @Test
    fun `query searches title employer and city`() {
        assertTrue(GigFilter(query = "cocktail").matches(gig, TODAY))
        assertTrue(GigFilter(query = "test hotel").matches(gig, TODAY))
        assertTrue(GigFilter(query = "LIS").matches(gig, TODAY))
        assertFalse(GigFilter(query = "safari").matches(gig, TODAY))
    }

    @Test
    fun `active count reflects set filters`() {
        val filter = GigFilter(role = JobRole.CHEF, city = "Goa", minHourlyRate = 10, dateWindow = DateWindow.NEXT_7_DAYS)
        assertEquals(4, filter.activeCount)
    }
}
