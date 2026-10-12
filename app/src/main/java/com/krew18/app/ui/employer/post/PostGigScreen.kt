package com.krew18.app.ui.employer.post

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.krew18.app.domain.model.JobRole
import com.krew18.app.ui.components.formatDate
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

private enum class DateTarget { START, END }

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun PostGigScreen(
    onViewMyGigs: () -> Unit,
    viewModel: PostGigViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var datePickerTarget by remember { mutableStateOf<DateTarget?>(null) }

    LaunchedEffect(state.message) {
        val message = state.message ?: return@LaunchedEffect
        val posted = state.postedGigId != null
        val result = snackbarHostState.showSnackbar(
            message = message,
            actionLabel = if (posted) "View" else null,
            duration = SnackbarDuration.Short,
        )
        viewModel.messageShown()
        if (posted && result == SnackbarResult.ActionPerformed) onViewMyGigs()
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Post a gig") }) },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            FormField(
                value = state.title,
                onValueChange = { v -> viewModel.update { it.copy(title = v) } },
                label = "Gig title",
                error = state.errorFor(PostGigField.TITLE),
            )
            Text("Role", style = MaterialTheme.typography.titleSmall)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                JobRole.entries.forEach { role ->
                    FilterChip(
                        selected = state.role == role,
                        onClick = { viewModel.update { it.copy(role = role) } },
                        label = { Text(role.label) },
                    )
                }
            }
            FormField(
                value = state.city,
                onValueChange = { v -> viewModel.update { it.copy(city = v) } },
                label = "City",
                error = state.errorFor(PostGigField.CITY),
            )
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                FormField(
                    value = state.hourlyRate,
                    onValueChange = { v -> viewModel.update { it.copy(hourlyRate = v.filter(Char::isDigit).take(4)) } },
                    label = "Hourly rate (USD)",
                    error = state.errorFor(PostGigField.RATE),
                    keyboardType = KeyboardType.Number,
                    modifier = Modifier.weight(1f),
                )
                FormField(
                    value = state.openings,
                    onValueChange = { v -> viewModel.update { it.copy(openings = v.filter(Char::isDigit).take(3)) } },
                    label = "Openings",
                    error = state.errorFor(PostGigField.OPENINGS),
                    keyboardType = KeyboardType.Number,
                    modifier = Modifier.weight(1f),
                )
            }
            Text("Dates", style = MaterialTheme.typography.titleSmall)
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(onClick = { datePickerTarget = DateTarget.START }, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Filled.CalendarMonth, contentDescription = null)
                    Text("  From ${formatDate(state.startDate)}")
                }
                OutlinedButton(onClick = { datePickerTarget = DateTarget.END }, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Filled.CalendarMonth, contentDescription = null)
                    Text("  To ${formatDate(state.endDate)}")
                }
            }
            state.errorFor(PostGigField.DATES)?.let {
                Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }
            FormField(
                value = state.description,
                onValueChange = { v -> viewModel.update { it.copy(description = v) } },
                label = "Description",
                error = state.errorFor(PostGigField.DESCRIPTION),
                singleLine = false,
            )
            FormField(
                value = state.requirements,
                onValueChange = { v -> viewModel.update { it.copy(requirements = v) } },
                label = "Requirements (comma separated)",
                error = null,
                singleLine = false,
            )
            Button(
                onClick = viewModel::submit,
                enabled = !state.isSubmitting,
                modifier = Modifier.fillMaxWidth(),
            ) {
                if (state.isSubmitting) {
                    CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                } else {
                    Text("Publish gig")
                }
            }
            Spacer(Modifier.height(16.dp))
        }
    }

    datePickerTarget?.let { target ->
        val initial = if (target == DateTarget.START) state.startDate else state.endDate
        GigDatePickerDialog(
            initialDate = initial,
            onDismiss = { datePickerTarget = null },
            onDateSelected = { date ->
                if (target == DateTarget.START) viewModel.setStartDate(date) else viewModel.setEndDate(date)
                datePickerTarget = null
            },
        )
    }
}

@Composable
private fun FormField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    error: String?,
    modifier: Modifier = Modifier.fillMaxWidth(),
    keyboardType: KeyboardType = KeyboardType.Text,
    singleLine: Boolean = true,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        isError = error != null,
        supportingText = error?.let { { Text(it) } },
        singleLine = singleLine,
        minLines = if (singleLine) 1 else 3,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        modifier = modifier,
    )
}

/** Material 3 date picker. The picker works in UTC milliseconds, so convert via UTC. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GigDatePickerDialog(
    initialDate: LocalDate,
    onDismiss: () -> Unit,
    onDateSelected: (LocalDate) -> Unit,
) {
    val pickerState = rememberDatePickerState(
        initialSelectedDateMillis = initialDate.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli(),
    )
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    pickerState.selectedDateMillis?.let { millis ->
                        onDateSelected(Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate())
                    } ?: onDismiss()
                },
            ) { Text("OK") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    ) {
        DatePicker(state = pickerState)
    }
}
