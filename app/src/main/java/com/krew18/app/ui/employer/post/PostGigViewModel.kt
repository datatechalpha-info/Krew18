package com.krew18.app.ui.employer.post

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.krew18.app.data.repository.GigRepository
import com.krew18.app.domain.model.GigDraft
import com.krew18.app.domain.model.JobRole
import com.krew18.app.ui.talent.profile.toTagList
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Clock
import java.time.LocalDate
import javax.inject.Inject

enum class PostGigField { TITLE, CITY, RATE, OPENINGS, DATES, DESCRIPTION }

data class PostGigUiState(
    val title: String = "",
    val role: JobRole = JobRole.CHEF,
    val city: String = "",
    val hourlyRate: String = "",
    val openings: String = "1",
    val startDate: LocalDate,
    val endDate: LocalDate,
    val description: String = "",
    val requirements: String = "",
    val showErrors: Boolean = false,
    val isSubmitting: Boolean = false,
    val postedGigId: String? = null,
    val message: String? = null,
    val today: LocalDate,
) {
    val errors: Map<PostGigField, String>
        get() = buildMap {
            if (title.isBlank()) put(PostGigField.TITLE, "Give the gig a title")
            if (city.isBlank()) put(PostGigField.CITY, "Where is the gig?")
            val rate = hourlyRate.toIntOrNull()
            if (rate == null || rate !in 1..1_000) put(PostGigField.RATE, "Enter an hourly rate between 1 and 1000")
            val spots = openings.toIntOrNull()
            if (spots == null || spots !in 1..500) put(PostGigField.OPENINGS, "Openings must be between 1 and 500")
            if (startDate.isBefore(today)) {
                put(PostGigField.DATES, "Start date cannot be in the past")
            } else if (endDate.isBefore(startDate)) {
                put(PostGigField.DATES, "End date must be on or after the start date")
            }
            if (description.trim().length < 20) put(PostGigField.DESCRIPTION, "Add a short description (20+ characters)")
        }

    fun errorFor(field: PostGigField): String? = if (showErrors) errors[field] else null
}

@HiltViewModel
class PostGigViewModel @Inject constructor(
    private val gigRepository: GigRepository,
    private val clock: Clock,
) : ViewModel() {

    private val _uiState = MutableStateFlow(emptyState())
    val uiState: StateFlow<PostGigUiState> = _uiState.asStateFlow()

    private fun emptyState(): PostGigUiState {
        val today = LocalDate.now(clock)
        return PostGigUiState(
            startDate = today.plusDays(7),
            endDate = today.plusDays(13),
            today = today,
        )
    }

    fun update(transform: (PostGigUiState) -> PostGigUiState) = _uiState.update(transform)

    fun setStartDate(date: LocalDate) = _uiState.update { state ->
        state.copy(startDate = date, endDate = if (state.endDate.isBefore(date)) date else state.endDate)
    }

    fun setEndDate(date: LocalDate) = _uiState.update { it.copy(endDate = date) }

    fun submit() {
        val state = _uiState.value
        if (state.isSubmitting) return
        if (state.errors.isNotEmpty()) {
            _uiState.update { it.copy(showErrors = true) }
            return
        }
        _uiState.update { it.copy(isSubmitting = true) }
        viewModelScope.launch {
            val draft = GigDraft(
                title = state.title,
                role = state.role,
                city = state.city,
                hourlyRate = state.hourlyRate.toInt(),
                startDate = state.startDate,
                endDate = state.endDate,
                openings = state.openings.toInt(),
                description = state.description,
                requirements = state.requirements.toTagList(),
            )
            gigRepository.postGig(draft).fold(
                onSuccess = { gig ->
                    _uiState.value = emptyState().copy(postedGigId = gig.id, message = "Gig posted: ${gig.title}")
                },
                onFailure = {
                    _uiState.update { it.copy(isSubmitting = false, message = "Could not post the gig. Please try again.") }
                },
            )
        }
    }

    fun messageShown() = _uiState.update { it.copy(message = null) }
}
