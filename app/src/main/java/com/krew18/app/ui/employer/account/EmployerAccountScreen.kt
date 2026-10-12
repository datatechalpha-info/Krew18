package com.krew18.app.ui.employer.account

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.krew18.app.domain.model.CurrentUser
import com.krew18.app.domain.model.ThemeMode
import com.krew18.app.ui.components.SettingsSection

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmployerAccountScreen(
    themeMode: ThemeMode,
    onThemeModeChange: (ThemeMode) -> Unit,
    onSwitchRole: () -> Unit,
) {
    Scaffold(topBar = { TopAppBar(title = { Text("Account") }) }) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
            ) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(CurrentUser.EMPLOYER_NAME, style = MaterialTheme.typography.titleLarge)
                    Text(CurrentUser.EMPLOYER_TYPE.label, style = MaterialTheme.typography.bodyMedium)
                    Text(
                        "This is a sample employer account. There is no sign-in in the demo; " +
                            "everything is stored locally on this device.",
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }
            HorizontalDivider()
            SettingsSection(
                themeMode = themeMode,
                onThemeModeChange = onThemeModeChange,
                switchRoleLabel = "Switch to talent mode",
                onSwitchRole = onSwitchRole,
            )
        }
    }
}
