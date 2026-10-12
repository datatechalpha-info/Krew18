package com.krew18.app.ui.talent.feed

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.krew18.app.data.repository.GigRepository
import com.krew18.app.domain.model.DateWindow
import com.krew18.app.domain.model.Gig
import com.krew18.app.domain.model.GigFilter
import com.krew18.app.domain.model.JobRole
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import java.time.Clock
import java.time.LocalDate
import javax.inject.Inject

data class JobFeedUiState(
    val isLoading: Boolean = true,
    val gigs: List<Gig> = emptyList(),
    val totalOpenGigs: Int = 0,
    val filter: GigFilter = GigFilter(),
    val cities: List<String> = emptyList(),
    val appliedGigIds: Set<String> = emptySet(),
)

@HiltViewModel
class JobFeedViewModel @Inject constructor(
    gigRepository: GigRepository,
    private val clock: Clock,
) : ViewModel() {

    private val filter = MutableStateFlow(GigFilter())

    val uiState: StateFlow<JobFeedUiState> = combine(
        gigRepository.observeGigs(),
        gigRepository.observeMyApplications(),
        filter,
    ) { gigs, myApplications, currentFilter ->
        val today = LocalDate.now(clock)
        val openGigs = gigs.filter { !it.endDate.isBefore(today) }
        JobFeedUiState(
            isLoading = false,
            gigs = openGigs.filter { currentFilter.matches(it, today) },
            totalOpenGigs = openGigs.size,
            filter = currentFilter,
            cities = openGigs.map { it.city }.distinct().sorted(),
            appliedGigIds = myApplications.map { it.application.gigId }.toSet(),
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), JobFeedUiState())

    fun setRole(role: JobRole?) = filter.update { it.copy(role = role) }

    fun setCity(city: String?) = filter.update { it.copy(city = city) }

    fun setMinHourlyRate(rate: Int) = filter.update { it.copy(minHourlyRate = rate.coerceAtLeast(0)) }

    fun setDateWindow(window: DateWindow) = filter.update { it.copy(dateWindow = window) }

    fun setQuery(query: String) = filter.update { it.copy(query = query) }

    fun clearFilters() {
        filter.value = GigFilter()
    }
}
