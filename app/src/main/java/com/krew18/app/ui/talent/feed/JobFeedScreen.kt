package com.krew18.app.ui.talent.feed

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.krew18.app.domain.model.DateWindow
import com.krew18.app.domain.model.JobRole
import com.krew18.app.ui.components.EmptyState
import com.krew18.app.ui.components.GigCard
import com.krew18.app.ui.components.LoadingBox
import com.krew18.app.ui.components.Pill
import com.krew18.app.ui.components.formatPay
import kotlin.math.roundToInt

private const val MAX_PAY_FILTER = 40

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JobFeedScreen(
    onGigClick: (String) -> Unit,
    viewModel: JobFeedViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var filtersExpanded by rememberSaveable { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Find gigs") },
                actions = {
                    IconButton(onClick = { filtersExpanded = !filtersExpanded }) {
                        val count = state.filter.activeCount
                        BadgedBox(badge = { if (count > 0) Badge { Text("$count") } }) {
                            Icon(Icons.Filled.Tune, contentDescription = "Filters")
                        }
                    }
                },
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
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                OutlinedTextField(
                    value = state.filter.query,
                    onValueChange = viewModel::setQuery,
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Search title, employer or city") },
                    leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                    trailingIcon = {
                        if (state.filter.query.isNotEmpty()) {
                            IconButton(onClick = { viewModel.setQuery("") }) {
                                Icon(Icons.Filled.Clear, contentDescription = "Clear search")
                            }
                        }
                    },
                    singleLine = true,
                )
            }
            item {
                ChipRow {
                    FilterChip(
                        selected = state.filter.role == null,
                        onClick = { viewModel.setRole(null) },
                        label = { Text("All roles") },
                    )
                    JobRole.entries.forEach { role ->
                        FilterChip(
                            selected = state.filter.role == role,
                            onClick = { viewModel.setRole(if (state.filter.role == role) null else role) },
                            label = { Text(role.label) },
                        )
                    }
                }
            }
            item {
                AnimatedVisibility(visible = filtersExpanded) {
                    FilterPanel(
                        state = state,
                        onCity = viewModel::setCity,
                        onDateWindow = viewModel::setDateWindow,
                        onMinPay = viewModel::setMinHourlyRate,
                        onClear = viewModel::clearFilters,
                    )
                }
            }
            item {
                Text(
                    "${state.gigs.size} of ${state.totalOpenGigs} open gigs",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            if (state.gigs.isEmpty()) {
                item {
                    EmptyState(
                        title = "No gigs match your filters",
                        body = "Try a different role, city or date range.",
                        icon = Icons.Filled.SearchOff,
                        action = { TextButton(onClick = viewModel::clearFilters) { Text("Clear filters") } },
                    )
                }
            } else {
                items(state.gigs, key = { it.id }) { gig ->
                    GigCard(
                        gig = gig,
                        onClick = { onGigClick(gig.id) },
                        trailingLabel = if (gig.id in state.appliedGigIds) {
                            {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Filled.CheckCircle,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                    )
                                    Text(
                                        " Applied",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.primary,
                                    )
                                }
                            }
                        } else {
                            null
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun ChipRow(content: @Composable () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) { content() }
}

@Composable
private fun FilterPanel(
    state: JobFeedUiState,
    onCity: (String?) -> Unit,
    onDateWindow: (DateWindow) -> Unit,
    onMinPay: (Int) -> Unit,
    onClear: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Location", style = MaterialTheme.typography.titleSmall)
        ChipRow {
            FilterChip(selected = state.filter.city == null, onClick = { onCity(null) }, label = { Text("Anywhere") })
            state.cities.forEach { city ->
                FilterChip(
                    selected = state.filter.city == city,
                    onClick = { onCity(if (state.filter.city == city) null else city) },
                    label = { Text(city) },
                )
            }
        }
        Text("Dates", style = MaterialTheme.typography.titleSmall)
        ChipRow {
            DateWindow.entries.forEach { window ->
                FilterChip(
                    selected = state.filter.dateWindow == window,
                    onClick = { onDateWindow(window) },
                    label = { Text(window.label) },
                )
            }
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Minimum pay", style = MaterialTheme.typography.titleSmall, modifier = Modifier.weight(1f))
            Pill(
                if (state.filter.minHourlyRate == 0) "Any" else formatPay(state.filter.minHourlyRate) + "+",
                MaterialTheme.colorScheme.primaryContainer,
                MaterialTheme.colorScheme.onPrimaryContainer,
            )
        }
        Slider(
            value = state.filter.minHourlyRate.toFloat(),
            onValueChange = { onMinPay(it.roundToInt()) },
            valueRange = 0f..MAX_PAY_FILTER.toFloat(),
            steps = MAX_PAY_FILTER / 2 - 1,
        )
        TextButton(onClick = onClear, modifier = Modifier.align(Alignment.End)) { Text("Reset filters") }
    }
}
