package com.chandan.askfaculty.ui.health

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun HealthScreen(
    modifier: Modifier = Modifier,
    viewModel: HealthViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "AskFaculty Backend Status",
                style = MaterialTheme.typography.titleLarge
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = when (uiState) {
                    is HealthUiState.Success -> "Backend: ok"
                    is HealthUiState.Error -> "Backend: offline"
                    else -> "Backend: unknown"
                },
                style = MaterialTheme.typography.bodyLarge,
                color = when (uiState) {
                    is HealthUiState.Success -> MaterialTheme.colorScheme.primary
                    is HealthUiState.Error -> MaterialTheme.colorScheme.error
                    else -> MaterialTheme.colorScheme.secondary
                }
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(onClick = { viewModel.checkHealth() }) {
                Text("Check Health / Retry")
            }

            Spacer(modifier = Modifier.height(24.dp))

            when (val state = uiState) {
                is HealthUiState.Idle -> {
                    Text("Click 'Check Health / Retry' to test server readiness.")
                }
                is HealthUiState.Loading -> {
                    CircularProgressIndicator()
                }
                is HealthUiState.Success -> {
                    Text(
                        text = "Status: ${state.health.status}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Checks:")
                    state.health.checks.forEach { (key, value) ->
                        Text("- $key: $value")
                    }
                }
                is HealthUiState.Error -> {
                    Text(
                        text = state.message,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}
