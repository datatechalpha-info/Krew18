package com.krew18.app.ui.talent.applications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.krew18.app.data.repository.GigRepository
import com.krew18.app.domain.model.ApplicationStatus
import com.krew18.app.domain.model.ApplicationWithGig
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

data class MyApplicationsUiState(
    val isLoading: Boolean = true,
    val items: List<ApplicationWithGig> = emptyList(),
    val statusFilter: ApplicationStatus? = null,
    val countsByStatus: Map<ApplicationStatus, Int> = emptyMap(),
    val totalCount: Int = 0,
)

@HiltViewModel
class MyApplicationsViewModel @Inject constructor(
    gigRepository: GigRepository,
) : ViewModel() {

    private val statusFilter = MutableStateFlow<ApplicationStatus?>(null)

    val uiState: StateFlow<MyApplicationsUiState> = combine(
        gigRepository.observeMyApplications(),
        statusFilter,
    ) { all, filter ->
        MyApplicationsUiState(
            isLoading = false,
            items = if (filter == null) all else all.filter { it.application.status == filter },
            statusFilter = filter,
            countsByStatus = all.groupingBy { it.application.status }.eachCount(),
            totalCount = all.size,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), MyApplicationsUiState())

    fun setStatusFilter(status: ApplicationStatus?) {
        statusFilter.value = status
    }
}
