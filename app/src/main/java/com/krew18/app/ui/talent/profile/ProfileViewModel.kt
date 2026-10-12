package com.krew18.app.ui.talent.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.krew18.app.data.repository.ProfileRepository
import com.krew18.app.domain.model.Availability
import com.krew18.app.domain.model.JobRole
import com.krew18.app.domain.model.TalentProfile
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

/** Editable copy of the profile. Skills and languages are edited as comma-separated text. */
data class ProfileForm(
    val name: String = "",
    val headline: String = "",
    val primaryRole: JobRole = JobRole.BARTENDER,
    val city: String = "",
    val yearsExperience: String = "",
    val experienceSummary: String = "",
    val skills: String = "",
    val languages: String = "",
    val availability: Set<Availability> = emptySet(),
    val openToTravel: Boolean = false,
) {
    val nameError: String? get() = if (name.isBlank()) "Name is required" else null
    val yearsError: String?
        get() = yearsExperience.toIntOrNull().let {
            if (it == null || it !in 0..60) "Enter a number between 0 and 60" else null
        }
    val isValid: Boolean get() = nameError == null && yearsError == null

    fun toProfile(): TalentProfile = TalentProfile(
        name = name.trim(),
        headline = headline.trim(),
        primaryRole = primaryRole,
        city = city.trim(),
        yearsExperience = yearsExperience.toIntOrNull() ?: 0,
        experienceSummary = experienceSummary.trim(),
        skills = skills.toTagList(),
        languages = languages.toTagList(),
        availability = availability,
        openToTravel = openToTravel,
    )

    companion object {
        fun from(profile: TalentProfile) = ProfileForm(
            name = profile.name,
            headline = profile.headline,
            primaryRole = profile.primaryRole,
            city = profile.city,
            yearsExperience = profile.yearsExperience.toString(),
            experienceSummary = profile.experienceSummary,
            skills = profile.skills.joinToString(", "),
            languages = profile.languages.joinToString(", "),
            availability = profile.availability,
            openToTravel = profile.openToTravel,
        )
    }
}

internal fun String.toTagList(): List<String> =
    split(',').map { it.trim() }.filter { it.isNotEmpty() }.distinctBy { it.lowercase() }

data class ProfileUiState(
    val isLoading: Boolean = true,
    val profile: TalentProfile? = null,
    val isEditing: Boolean = false,
    val form: ProfileForm = ProfileForm(),
    val showValidation: Boolean = false,
    val message: String? = null,
)

private data class EditState(
    val isEditing: Boolean = false,
    val form: ProfileForm = ProfileForm(),
    val showValidation: Boolean = false,
    val message: String? = null,
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val profileRepository: ProfileRepository,
) : ViewModel() {

    private val edit = MutableStateFlow(EditState())

    val uiState: StateFlow<ProfileUiState> = combine(profileRepository.profile, edit) { profile, editState ->
        ProfileUiState(
            isLoading = false,
            profile = profile,
            isEditing = editState.isEditing,
            form = editState.form,
            showValidation = editState.showValidation,
            message = editState.message,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ProfileUiState())

    fun startEditing() {
        viewModelScope.launch {
            val profile = profileRepository.profile.first()
            edit.value = EditState(isEditing = true, form = ProfileForm.from(profile))
        }
    }

    fun cancelEditing() = edit.update { it.copy(isEditing = false, showValidation = false) }

    fun updateForm(transform: (ProfileForm) -> ProfileForm) = edit.update { it.copy(form = transform(it.form)) }

    fun toggleAvailability(slot: Availability) = updateForm { form ->
        form.copy(availability = if (slot in form.availability) form.availability - slot else form.availability + slot)
    }

    fun save() {
        val form = edit.value.form
        if (!form.isValid) {
            edit.update { it.copy(showValidation = true) }
            return
        }
        viewModelScope.launch {
            profileRepository.saveProfile(form.toProfile())
            edit.value = EditState(message = "Profile saved")
        }
    }

    fun messageShown() = edit.update { it.copy(message = null) }
}
