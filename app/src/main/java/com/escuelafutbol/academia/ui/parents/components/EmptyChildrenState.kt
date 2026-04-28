package com.escuelafutbol.academia.ui.parents.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.escuelafutbol.academia.R
import com.escuelafutbol.academia.ui.design.AppCard
import com.escuelafutbol.academia.ui.design.SectionHeader

/** Estado vacío cuando el tutor aún no tiene hijos vinculados (Padres). */
@Composable
fun EmptyChildrenState(modifier: Modifier = Modifier) {
    PadresEmptyChildrenOnboardingCard(modifier)
}

@Composable
fun PadresEmptyChildrenOnboardingCard(modifier: Modifier = Modifier) {
    AppCard(
        modifier = modifier.fillMaxWidth(),
        elevated = false,
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow.copy(alpha = 0.85f),
        borderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.22f),
    ) {
        Column(Modifier.fillMaxWidth()) {
            SectionHeader(
                title = stringResource(R.string.parent_onboarding_no_children_title),
                subtitle = stringResource(R.string.parent_onboarding_no_children_subtitle),
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}
