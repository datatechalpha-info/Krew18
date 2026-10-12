package com.krew18.app.ui.talent.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.krew18.app.data.repository.AlreadyAppliedException
import com.krew18.app.data.repository.GigRepository
import com.krew18.app.data.repository.ProfileRepository
import com.krew18.app.domain.model.Application
import com.krew18.app.domain.model.Gig
import com.krew18.app.ui.navigation.Routes
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class JobDetailUiState(
    val isLoading: Boolean = true,
    val gig: Gig? = null,
    val myApplication: Application? = null,
    val showApplyDialog: Boolean = false,
    val coverNote: String = "",
    val isSubmitting: Boolean = false,
    val message: String? = null,
) {
    val canApply: Boolean get() = gig != null && myApplication == null && !isSubmitting
}

private data class ApplyFormState(
    val showDialog: Boolean = false,
    val coverNote: String = "",
    val isSubmitting: Boolean = false,
    val message: String? = null,
)

@HiltViewModel
class JobDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val gigRepository: GigRepository,
    private val profileRepository: ProfileRepository,
) : ViewModel() {

    private val gigId: String = checkNotNull(savedStateHandle[Routes.ARG_GIG_ID])
    private val form = MutableStateFlow(ApplyFormState())

    val uiState: StateFlow<JobDetailUiState> = combine(
        gigRepository.observeGig(gigId),
        gigRepository.observeMyApplications(),
        form,
    ) { gig, myApplications, formState ->
        JobDetailUiState(
            isLoading = false,
            gig = gig,
            myApplication = myApplications.firstOrNull { it.application.gigId == gigId }?.application,
            showApplyDialog = formState.showDialog,
            coverNote = formState.coverNote,
            isSubmitting = formState.isSubmitting,
            message = formState.message,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), JobDetailUiState())

    fun openApplyDialog() = form.update { it.copy(showDialog = true) }

    fun dismissApplyDialog() = form.update { it.copy(showDialog = false) }

    fun onCoverNoteChange(note: String) = form.update { it.copy(coverNote = note.take(MAX_NOTE_LENGTH)) }

    fun submitApplication() {
        if (form.value.isSubmitting) return
        form.update { it.copy(isSubmitting = true) }
        viewModelScope.launch {
            val profile = profileRepository.profile.first()
            val result = gigRepository.applyToGig(gigId, form.value.coverNote, profile)
            form.update { state ->
                result.fold(
                    onSuccess = {
                        ApplyFormState(message = "Application sent. Track it under My applications.")
                    },
                    onFailure = { error ->
                        state.copy(
                            isSubmitting = false,
                            showDialog = false,
                            message = if (error is AlreadyAppliedException) {
                                error.message
                            } else {
                                "Could not send your application. Please try again."
                            },
                        )
                    },
                )
            }
        }
    }

    fun messageShown() = form.update { it.copy(message = null) }

    companion object {
        const val MAX_NOTE_LENGTH = 500
    }
}
