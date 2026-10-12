package com.krew18.app.ui.employer.gigs

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.krew18.app.data.repository.GigRepository
import com.krew18.app.domain.model.EmployerGigSummary
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

data class EmployerGigsUiState(
    val isLoading: Boolean = true,
    val gigs: List<EmployerGigSummary> = emptyList(),
) {
    val totalApplicants: Int get() = gigs.sumOf { it.applicantCount }
    val totalAccepted: Int get() = gigs.sumOf { it.acceptedCount }
}

@HiltViewModel
class EmployerGigsViewModel @Inject constructor(
    gigRepository: GigRepository,
) : ViewModel() {

    val uiState: StateFlow<EmployerGigsUiState> = gigRepository.observeEmployerGigs()
        .map { gigs -> EmployerGigsUiState(isLoading = false, gigs = gigs.sortedBy { it.gig.startDate }) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), EmployerGigsUiState())
}
