package com.krew18.app.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.krew18.app.data.repository.GigRepository
import com.krew18.app.data.repository.ProfileRepository
import com.krew18.app.domain.model.ThemeMode
import com.krew18.app.domain.model.UserRole
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface AppUiState {
    data object Loading : AppUiState

    /** [role] is null until the user completes onboarding. */
    data class Ready(val role: UserRole?, val themeMode: ThemeMode) : AppUiState
}

/** Activity-level state: which role is active (drives onboarding vs. main UI) and the theme. */
@HiltViewModel
class AppViewModel @Inject constructor(
    private val profileRepository: ProfileRepository,
    private val gigRepository: GigRepository,
) : ViewModel() {

    val uiState: StateFlow<AppUiState> =
        combine(profileRepository.role, profileRepository.themeMode) { role, theme ->
            AppUiState.Ready(role, theme)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AppUiState.Loading)

    init {
        viewModelScope.launch { gigRepository.syncIfEmpty() }
    }

    fun selectRole(role: UserRole) {
        viewModelScope.launch { profileRepository.setRole(role) }
    }

    /** Returns to onboarding so the user can pick the other side of the marketplace. */
    fun switchRole() {
        viewModelScope.launch { profileRepository.setRole(null) }
    }

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch { profileRepository.setThemeMode(mode) }
    }
}
