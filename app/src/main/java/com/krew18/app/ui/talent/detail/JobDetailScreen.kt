package com.krew18.app.ui.talent.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
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
import com.krew18.app.domain.model.Gig
import com.krew18.app.ui.components.EmptyState
import com.krew18.app.ui.components.InfoRow
import com.krew18.app.ui.components.LoadingBox
import com.krew18.app.ui.components.Pill
import com.krew18.app.ui.components.StatusPill
import com.krew18.app.ui.components.formatDate
import com.krew18.app.ui.components.formatDateRange
import com.krew18.app.ui.components.formatPay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JobDetailScreen(
    onBack: () -> Unit,
    viewModel: JobDetailViewModel = hiltViewModel(),
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
                title = { Text("Gig details") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            val gig = state.gig
            if (gig != null) {
                ApplyBar(state = state, onApply = viewModel::openApplyDialog)
            }
        },
    ) { padding ->
        val gig = state.gig
        when {
            state.isLoading -> LoadingBox(Modifier.padding(padding))
            gig == null -> EmptyState(
                title = "Gig not found",
                body = "It may have been removed by the employer.",
                icon = Icons.Filled.SearchOff,
                modifier = Modifier.padding(padding),
            )
            else -> GigDetailContent(gig, Modifier.padding(padding))
        }
    }

    if (state.showApplyDialog) {
        ApplyDialog(
            coverNote = state.coverNote,
            isSubmitting = state.isSubmitting,
            onCoverNoteChange = viewModel::onCoverNoteChange,
            onSubmit = viewModel::submitApplication,
            onDismiss = viewModel::dismissApplyDialog,
        )
    }
}

@Composable
private fun GigDetailContent(gig: Gig, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Pill(gig.role.label, MaterialTheme.colorScheme.secondaryContainer, MaterialTheme.colorScheme.onSecondaryContainer)
        Text(gig.title, style = MaterialTheme.typography.headlineSmall)
        InfoRow(Icons.Filled.Business, "${gig.employerName} · ${gig.employerType.label}")
        InfoRow(Icons.Filled.Place, gig.city)
        InfoRow(Icons.Filled.Payments, "${formatPay(gig.hourlyRate)} (USD)")
        InfoRow(Icons.Filled.CalendarMonth, "${formatDateRange(gig.startDate, gig.endDate)} · ${gig.durationDays} days")
        InfoRow(Icons.Filled.Groups, if (gig.openings == 1) "1 opening" else "${gig.openings} openings")
        HorizontalDivider()
        Text("About the gig", style = MaterialTheme.typography.titleMedium)
        Text(gig.description, style = MaterialTheme.typography.bodyLarge)
        if (gig.requirements.isNotEmpty()) {
            Text("What they're looking for", style = MaterialTheme.typography.titleMedium)
            gig.requirements.forEach { requirement ->
                Text("•  $requirement", style = MaterialTheme.typography.bodyLarge)
            }
        }
        Text(
            "Posted ${formatDate(gig.postedOn)}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun ApplyBar(state: JobDetailUiState, onApply: () -> Unit) {
    Surface(tonalElevation = 3.dp, shadowElevation = 3.dp) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            val application = state.myApplication
            if (application != null) {
                Text("Your application", style = MaterialTheme.typography.titleSmall, modifier = Modifier.weight(1f))
                StatusPill(application.status)
            } else {
                Column(Modifier.weight(1f)) {
                    state.gig?.let {
                        Text(formatPay(it.hourlyRate), style = MaterialTheme.typography.titleMedium)
                        Text(
                            it.city,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                Button(onClick = onApply, enabled = state.canApply) { Text("Apply now") }
            }
        }
    }
}

@Composable
private fun ApplyDialog(
    coverNote: String,
    isSubmitting: Boolean,
    onCoverNoteChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = { if (!isSubmitting) onDismiss() },
        title = { Text("Apply for this gig") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    "Your profile (skills, experience, languages) is shared with the employer.",
                    style = MaterialTheme.typography.bodyMedium,
                )
                OutlinedTextField(
                    value = coverNote,
                    onValueChange = onCoverNoteChange,
                    label = { Text("Short note (optional)") },
                    minLines = 3,
                    maxLines = 6,
                    supportingText = { Text("${coverNote.length}/${JobDetailViewModel.MAX_NOTE_LENGTH}") },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
        confirmButton = {
            Button(onClick = onSubmit, enabled = !isSubmitting) {
                if (isSubmitting) {
                    CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                } else {
                    Text("Send application")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isSubmitting) { Text("Cancel") }
        },
    )
}
