package com.krew18.app.ui.employer.applicants

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.krew18.app.data.repository.GigRepository
import com.krew18.app.domain.model.Application
import com.krew18.app.domain.model.ApplicationStatus
import com.krew18.app.domain.model.Gig
import com.krew18.app.ui.navigation.Routes
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ApplicantsUiState(
    val isLoading: Boolean = true,
    val gig: Gig? = null,
    val applicants: List<Application> = emptyList(),
    val statusFilter: ApplicationStatus? = null,
    val acceptedCount: Int = 0,
    val message: String? = null,
) {
    val openingsLeft: Int get() = ((gig?.openings ?: 0) - acceptedCount).coerceAtLeast(0)
}

@HiltViewModel
class ApplicantsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val gigRepository: GigRepository,
) : ViewModel() {

    private val gigId: String = checkNotNull(savedStateHandle[Routes.ARG_GIG_ID])
    private val statusFilter = MutableStateFlow<ApplicationStatus?>(null)
    private val message = MutableStateFlow<String?>(null)

    val uiState: StateFlow<ApplicantsUiState> = combine(
        gigRepository.observeGig(gigId),
        gigRepository.observeApplicants(gigId),
        statusFilter,
        message,
    ) { gig, applicants, filter, msg ->
        ApplicantsUiState(
            isLoading = false,
            gig = gig,
            applicants = if (filter == null) applicants else applicants.filter { it.status == filter },
            statusFilter = filter,
            acceptedCount = applicants.count { it.status == ApplicationStatus.ACCEPTED },
            message = msg,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ApplicantsUiState())

    fun setStatusFilter(status: ApplicationStatus?) {
        statusFilter.value = status
    }

    fun shortlist(applicationId: String) = updateStatus(applicationId, ApplicationStatus.SHORTLISTED)

    fun accept(applicationId: String) = updateStatus(applicationId, ApplicationStatus.ACCEPTED)

    fun reject(applicationId: String) = updateStatus(applicationId, ApplicationStatus.REJECTED)

    private fun updateStatus(applicationId: String, status: ApplicationStatus) {
        viewModelScope.launch {
            gigRepository.updateApplicationStatus(applicationId, status)
                .onSuccess { message.value = "Marked as ${status.label.lowercase()}" }
                .onFailure { message.value = "Could not update the applicant. Please try again." }
        }
    }

    fun messageShown() {
        message.value = null
    }
}
