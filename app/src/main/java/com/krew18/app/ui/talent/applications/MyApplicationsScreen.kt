package com.krew18.app.ui.talent.applications

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
import androidx.compose.material.icons.filled.Description
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.krew18.app.domain.model.ApplicationStatus
import com.krew18.app.domain.model.ApplicationWithGig
import com.krew18.app.ui.components.EmptyState
import com.krew18.app.ui.components.LoadingBox
import com.krew18.app.ui.components.StatusPill
import com.krew18.app.ui.components.formatDateRange
import com.krew18.app.ui.components.formatPay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyApplicationsScreen(
    onGigClick: (String) -> Unit,
    onBrowseGigs: () -> Unit,
    viewModel: MyApplicationsViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(topBar = { TopAppBar(title = { Text("My applications") }) }) { padding ->
        if (state.isLoading) {
            LoadingBox(Modifier.padding(padding))
            return@Scaffold
        }
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
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
                        label = { Text("All (${state.totalCount})") },
                    )
                    ApplicationStatus.entries.forEach { status ->
                        FilterChip(
                            selected = state.statusFilter == status,
                            onClick = { viewModel.setStatusFilter(status) },
                            label = { Text("${status.label} (${state.countsByStatus[status] ?: 0})") },
                        )
                    }
                }
            }
            if (state.items.isEmpty()) {
                item {
                    EmptyState(
                        title = if (state.totalCount == 0) "No applications yet" else "Nothing here",
                        body = if (state.totalCount == 0) {
                            "Apply to a gig and you can follow its status here."
                        } else {
                            "No applications with this status."
                        },
                        icon = Icons.Filled.Description,
                        action = if (state.totalCount == 0) {
                            { Button(onClick = onBrowseGigs) { Text("Browse gigs") } }
                        } else {
                            null
                        },
                    )
                }
            } else {
                items(state.items, key = { it.application.id }) { item ->
                    ApplicationCard(item, onClick = { onGigClick(item.gig.id) })
                }
            }
        }
    }
}

@Composable
private fun ApplicationCard(item: ApplicationWithGig, onClick: () -> Unit) {
    val gig = item.gig
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    gig.title,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                StatusPill(item.application.status)
            }
            Text(gig.employerName, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
            Text(
                "${gig.city} · ${formatPay(gig.hourlyRate)} · ${formatDateRange(gig.startDate, gig.endDate)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                statusHint(item.application.status),
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}

private fun statusHint(status: ApplicationStatus): String = when (status) {
    ApplicationStatus.APPLIED -> "Sent - the employer hasn't reviewed it yet."
    ApplicationStatus.SHORTLISTED -> "You're on the shortlist. Expect a message soon."
    ApplicationStatus.ACCEPTED -> "You're booked for this gig."
    ApplicationStatus.REJECTED -> "The employer went with other candidates this time."
}
