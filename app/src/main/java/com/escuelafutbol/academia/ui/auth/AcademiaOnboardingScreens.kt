package com.escuelafutbol.academia.ui.auth

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.escuelafutbol.academia.R
import com.escuelafutbol.academia.data.sync.AcademiaBindingOption

@Composable
fun AcademiaBindingLoadingScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        CircularProgressIndicator()
        Spacer(Modifier.height(16.dp))
        Text(
            stringResource(R.string.binding_loading),
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

@Composable
fun AcademiaBindingErrorScreen(
    message: String,
    onRetry: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(message, style = MaterialTheme.typography.bodyLarge)
        Spacer(Modifier.height(16.dp))
        Button(onClick = onRetry) {
            Text(stringResource(R.string.binding_retry))
        }
    }
}

@Composable
fun AcademiaOnboardingScreen(
    viewModel: AcademiaBindingViewModel,
) {
    val context = LocalContext.current
    var code by remember { mutableStateOf("") }
    var rol by remember { mutableStateOf("coach") }
    var creating by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            stringResource(R.string.onboarding_academia_title),
            style = MaterialTheme.typography.headlineSmall,
        )
        Text(
            stringResource(R.string.onboarding_academia_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(8.dp))
        Button(
            onClick = {
                creating = true
                viewModel.createOwnedAcademia { result ->
                    creating = false
                    result.onFailure { e ->
                        Toast.makeText(context, e.message, Toast.LENGTH_LONG).show()
                    }
                }
            },
            enabled = !creating,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(stringResource(R.string.onboarding_create_academy))
        }
        Text(
            "— ${stringResource(R.string.onboarding_join_button)} —",
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.padding(top = 8.dp),
        )
        OutlinedTextField(
            value = code,
            onValueChange = { code = it.uppercase() },
            label = { Text(stringResource(R.string.onboarding_join_code_hint)) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
        Text(stringResource(R.string.onboarding_join_as), style = MaterialTheme.typography.labelMedium)
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            FilterChip(
                selected = rol == "coach",
                onClick = { rol = "coach" },
                label = { Text(stringResource(R.string.onboarding_role_coach)) },
            )
            FilterChip(
                selected = rol == "coordinator",
                onClick = { rol = "coordinator" },
                label = { Text(stringResource(R.string.onboarding_role_coordinator)) },
            )
            FilterChip(
                selected = rol == "parent",
                onClick = { rol = "parent" },
                label = { Text(stringResource(R.string.onboarding_role_parent)) },
            )
        }
        Button(
            onClick = {
                viewModel.joinByCode(code, rol) { result ->
                    result.onFailure { e ->
                        Toast.makeText(context, e.message, Toast.LENGTH_LONG).show()
                    }
                }
            },
            enabled = code.trim().length >= 4,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(stringResource(R.string.onboarding_join_button))
        }
    }
}

@Composable
fun AcademiaPickAcademyScreen(
    viewModel: AcademiaBindingViewModel,
    options: List<AcademiaBindingOption>,
) {
    val context = LocalContext.current
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
    ) {
        Text(
            stringResource(R.string.pick_academy_title),
            style = MaterialTheme.typography.headlineSmall,
        )
        Spacer(Modifier.height(16.dp))
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(bottom = 24.dp),
        ) {
            items(options, key = { it.academiaId }) { opt ->
                OutlinedButton(
                    onClick = {
                        viewModel.selectAcademia(opt.academiaId) { result ->
                            result.onFailure { e ->
                                Toast.makeText(context, e.message, Toast.LENGTH_LONG).show()
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(opt.nombre, style = MaterialTheme.typography.titleSmall)
                        Text(opt.rol, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }
}
