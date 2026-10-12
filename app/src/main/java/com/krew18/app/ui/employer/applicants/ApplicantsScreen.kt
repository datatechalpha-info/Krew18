package com.krew18.app.ui.employer.applicants

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.krew18.app.domain.model.Application
import com.krew18.app.domain.model.ApplicationStatus
import com.krew18.app.ui.components.EmptyState
import com.krew18.app.ui.components.LoadingBox
import com.krew18.app.ui.components.StatusPill
import com.krew18.app.ui.components.TagFlow
import com.krew18.app.ui.components.formatDateRange
import com.krew18.app.ui.components.formatPay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ApplicantsScreen(
    onBack: () -> Unit,
    viewModel: ApplicantsViewModel = hiltViewModel(),
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
                title = { Text("Applicants") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        val gig = state.gig
        when {
            state.isLoading -> LoadingBox(Modifier.padding(padding))
            gig == null -> EmptyState(
                title = "Gig not found",
                body = "This gig no longer exists.",
                icon = Icons.Filled.SearchOff,
                modifier = Modifier.padding(padding),
            )
            else -> LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(gig.title, style = MaterialTheme.typography.titleLarge)
                        Text(
                            "${gig.city} · ${formatPay(gig.hourlyRate)} · ${formatDateRange(gig.startDate, gig.endDate)}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Text(
                            "${state.acceptedCount} of ${gig.openings} openings filled",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                }
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        FilterChip(
                            selected = state.statusFilter == null,
                            onClick = { viewModel.setStatusFilter(null) },
                            label = { Text("All") },
                        )
                        ApplicationStatus.entries.forEach { status ->
                            FilterChip(
                                selected = state.statusFilter == status,
                                onClick = { viewModel.setStatusFilter(status) },
                                label = { Text(status.label) },
                            )
                        }
                    }
                }
                if (state.applicants.isEmpty()) {
                    item {
                        EmptyState(
                            title = "No applicants here yet",
                            body = "New applications will show up as talent applies.",
                            icon = Icons.Filled.Groups,
                        )
                    }
                } else {
                    items(state.applicants, key = { it.id }) { applicant ->
                        ApplicantCard(
                            applicant = applicant,
                            canAccept = state.openingsLeft > 0,
                            onShortlist = { viewModel.shortlist(applicant.id) },
                            onAccept = { viewModel.accept(applicant.id) },
                            onReject = { viewModel.reject(applicant.id) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ApplicantCard(
    applicant: Application,
    canAccept: Boolean,
    onShortlist: () -> Unit,
    onAccept: () -> Unit,
    onReject: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(applicant.talentName, style = MaterialTheme.typography.titleMedium)
                    Text(
                        "${applicant.talentHeadline} · ${applicant.yearsExperience} yrs",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                StatusPill(applicant.status)
            }
            TagFlow(applicant.talentSkills + applicant.talentLanguages)
            if (applicant.coverNote.isNotBlank()) {
                Text("“${applicant.coverNote}”", style = MaterialTheme.typography.bodyMedium)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                when (applicant.status) {
                    ApplicationStatus.APPLIED -> {
                        OutlinedButton(onClick = onShortlist) { Text("Shortlist") }
                        Button(onClick = onAccept, enabled = canAccept) { Text("Accept") }
                        TextButton(onClick = onReject) { Text("Decline") }
                    }
                    ApplicationStatus.SHORTLISTED -> {
                        Button(onClick = onAccept, enabled = canAccept) { Text("Accept") }
                        TextButton(onClick = onReject) { Text("Decline") }
                    }
                    ApplicationStatus.ACCEPTED -> {
                        Text(
                            "Booked for this gig",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                    ApplicationStatus.REJECTED -> {
                        TextButton(onClick = onShortlist) { Text("Reconsider") }
                    }
                }
            }
            if (!canAccept && applicant.status in setOf(ApplicationStatus.APPLIED, ApplicationStatus.SHORTLISTED)) {
                Text(
                    "All openings are filled.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
