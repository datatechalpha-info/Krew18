package com.krew18.app.ui.employer.gigs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Business
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.krew18.app.domain.model.CurrentUser
import com.krew18.app.domain.model.EmployerGigSummary
import com.krew18.app.ui.components.EmptyState
import com.krew18.app.ui.components.GigCard
import com.krew18.app.ui.components.LoadingBox
import com.krew18.app.ui.components.Pill

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmployerGigsScreen(
    onGigClick: (String) -> Unit,
    onPostGig: () -> Unit,
    viewModel: EmployerGigsViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = { TopAppBar(title = { Text("My gigs") }) },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onPostGig,
                icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                text = { Text("Post a gig") },
            )
        },
    ) { padding ->
        if (state.isLoading) {
            LoadingBox(Modifier.padding(padding))
            return@Scaffold
        }
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item { SummaryCard(state) }
            if (state.gigs.isEmpty()) {
                item {
                    EmptyState(
                        title = "No gigs posted yet",
                        body = "Post your first gig to start receiving applicants.",
                        icon = Icons.Filled.Business,
                        action = { Button(onClick = onPostGig) { Text("Post a gig") } },
                    )
                }
            } else {
                items(state.gigs, key = { it.gig.id }) { summary ->
                    GigCard(
                        gig = summary.gig,
                        onClick = { onGigClick(summary.gig.id) },
                        trailingLabel = { ApplicantBadge(summary) },
                    )
                }
            }
        }
    }
}

@Composable
private fun SummaryCard(state: EmployerGigsUiState) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(CurrentUser.EMPLOYER_NAME, style = MaterialTheme.typography.titleLarge)
            Text("${CurrentUser.EMPLOYER_TYPE.label} · demo employer account", style = MaterialTheme.typography.bodyMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                Stat(state.gigs.size, "Gigs")
                Stat(state.totalApplicants, "Applicants")
                Stat(state.totalAccepted, "Booked")
            }
        }
    }
}

@Composable
private fun Stat(value: Int, label: String) {
    Column {
        Text("$value", style = MaterialTheme.typography.headlineSmall)
        Text(label, style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
private fun ApplicantBadge(summary: EmployerGigSummary) {
    val text = when (summary.applicantCount) {
        0 -> "No applicants"
        1 -> "1 applicant"
        else -> "${summary.applicantCount} applicants"
    }
    Pill(text, MaterialTheme.colorScheme.tertiaryContainer, MaterialTheme.colorScheme.onTertiaryContainer)
}
