package com.krew18.app.ui.talent.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.krew18.app.domain.model.Availability
import com.krew18.app.domain.model.JobRole
import com.krew18.app.domain.model.TalentProfile
import com.krew18.app.domain.model.ThemeMode
import com.krew18.app.ui.components.LoadingBox
import com.krew18.app.ui.components.SettingsSection
import com.krew18.app.ui.components.TagFlow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    themeMode: ThemeMode,
    onThemeModeChange: (ThemeMode) -> Unit,
    onSwitchRole: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.message) {
        state.message?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.messageShown()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (state.isEditing) "Edit profile" else "My profile") },
                actions = {
                    if (state.isEditing) {
                        IconButton(onClick = viewModel::cancelEditing) {
                            Icon(Icons.Filled.Close, contentDescription = "Cancel editing")
                        }
                    } else {
                        IconButton(onClick = viewModel::startEditing) {
                            Icon(Icons.Filled.Edit, contentDescription = "Edit profile")
                        }
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        val profile = state.profile
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            when {
                state.isLoading || profile == null -> LoadingBox()
                state.isEditing -> ProfileEditor(
                    form = state.form,
                    showValidation = state.showValidation,
                    onChange = viewModel::updateForm,
                    onToggleAvailability = viewModel::toggleAvailability,
                    onSave = viewModel::save,
                    onCancel = viewModel::cancelEditing,
                )
                else -> {
                    ProfileDetails(profile)
                    HorizontalDivider()
                    SettingsSection(
                        themeMode = themeMode,
                        onThemeModeChange = onThemeModeChange,
                        switchRoleLabel = "Switch to employer mode",
                        onSwitchRole = onSwitchRole,
                    )
                }
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun ProfileDetails(profile: TalentProfile) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(shape = CircleShape, color = MaterialTheme.colorScheme.primary, modifier = Modifier.size(56.dp)) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        profile.name.split(" ").mapNotNull { it.firstOrNull()?.uppercase() }.take(2).joinToString(""),
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onPrimary,
                    )
                }
            }
            Spacer(Modifier.width(16.dp))
            Column {
                Text(profile.name, style = MaterialTheme.typography.titleLarge)
                Text(profile.headline, style = MaterialTheme.typography.bodyMedium)
                Text(
                    "${profile.primaryRole.label} · ${profile.city} · ${profile.yearsExperience} yrs experience",
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
    }
    Section("Experience") {
        Text(
            profile.experienceSummary.ifBlank { "Tell employers about your past roles." },
            style = MaterialTheme.typography.bodyLarge,
        )
    }
    Section("Skills") { TagFlow(profile.skills) }
    Section("Languages") { TagFlow(profile.languages) }
    Section("Availability") {
        TagFlow(profile.availability.sortedBy { it.ordinal }.map { it.label }, emptyText = "Not set")
        Text(
            if (profile.openToTravel) "Open to travel and relocation for seasonal work" else "Prefers local gigs",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun Section(title: String, content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(title, style = MaterialTheme.typography.titleMedium)
        content()
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ProfileEditor(
    form: ProfileForm,
    showValidation: Boolean,
    onChange: ((ProfileForm) -> ProfileForm) -> Unit,
    onToggleAvailability: (Availability) -> Unit,
    onSave: () -> Unit,
    onCancel: () -> Unit,
) {
    val nameError = if (showValidation) form.nameError else null
    val yearsError = if (showValidation) form.yearsError else null

    OutlinedTextField(
        value = form.name,
        onValueChange = { v -> onChange { it.copy(name = v) } },
        label = { Text("Full name") },
        isError = nameError != null,
        supportingText = nameError?.let { { Text(it) } },
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
    )
    OutlinedTextField(
        value = form.headline,
        onValueChange = { v -> onChange { it.copy(headline = v) } },
        label = { Text("Headline") },
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
    )
    Text("Primary role", style = MaterialTheme.typography.titleSmall)
    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        JobRole.entries.forEach { role ->
            FilterChip(
                selected = form.primaryRole == role,
                onClick = { onChange { it.copy(primaryRole = role) } },
                label = { Text(role.label) },
            )
        }
    }
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        OutlinedTextField(
            value = form.city,
            onValueChange = { v -> onChange { it.copy(city = v) } },
            label = { Text("Home city") },
            singleLine = true,
            modifier = Modifier.weight(1f),
        )
        OutlinedTextField(
            value = form.yearsExperience,
            onValueChange = { v -> onChange { it.copy(yearsExperience = v.filter(Char::isDigit).take(2)) } },
            label = { Text("Years") },
            isError = yearsError != null,
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.width(110.dp),
        )
    }
    yearsError?.let { Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall) }
    OutlinedTextField(
        value = form.experienceSummary,
        onValueChange = { v -> onChange { it.copy(experienceSummary = v) } },
        label = { Text("Experience summary") },
        minLines = 3,
        modifier = Modifier.fillMaxWidth(),
    )
    OutlinedTextField(
        value = form.skills,
        onValueChange = { v -> onChange { it.copy(skills = v) } },
        label = { Text("Skills") },
        supportingText = { Text("Comma separated, e.g. Mixology, Food safety") },
        modifier = Modifier.fillMaxWidth(),
    )
    OutlinedTextField(
        value = form.languages,
        onValueChange = { v -> onChange { it.copy(languages = v) } },
        label = { Text("Languages") },
        supportingText = { Text("Comma separated") },
        modifier = Modifier.fillMaxWidth(),
    )
    Text("Availability", style = MaterialTheme.typography.titleSmall)
    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Availability.entries.forEach { slot ->
            FilterChip(
                selected = slot in form.availability,
                onClick = { onToggleAvailability(slot) },
                label = { Text(slot.label) },
            )
        }
    }
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text("Open to travel / relocation", modifier = Modifier.weight(1f))
        Switch(checked = form.openToTravel, onCheckedChange = { checked -> onChange { it.copy(openToTravel = checked) } })
    }
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
        TextButton(onClick = onCancel, modifier = Modifier.weight(1f)) { Text("Cancel") }
        Button(onClick = onSave, modifier = Modifier.weight(1f)) { Text("Save profile") }
    }
}
