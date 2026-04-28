package com.escuelafutbol.academia.ui.onboarding

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.escuelafutbol.academia.R
import com.escuelafutbol.academia.ui.design.AcademiaDimens
import com.escuelafutbol.academia.ui.design.AppCard
import com.escuelafutbol.academia.ui.design.PrimaryButton
import com.escuelafutbol.academia.ui.design.SectionHeader

@Composable
fun WelcomeFirstLaunchScreen(
    onComenzar: () -> Unit,
    modifier: Modifier = Modifier,
) {
    /** Misma acción que «Comenzar»: completar onboarding y cerrar (el padre persiste prefs y oculta el overlay). */
    BackHandler(onBack = onComenzar)
    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.surface,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding()
                .padding(horizontal = AcademiaDimens.paddingScreenHorizontal)
                .padding(
                    top = AcademiaDimens.paddingCard + AcademiaDimens.gapMd,
                    bottom = AcademiaDimens.paddingCard + AcademiaDimens.paddingCard +
                        AcademiaDimens.spacingDialogBlock + AcademiaDimens.buttonMinHeight,
                ),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top,
        ) {
            AppCard(
                modifier = Modifier.fillMaxWidth(),
                elevated = true,
            ) {
                SectionHeader(
                    title = stringResource(R.string.onboarding_welcome_title),
                    subtitle = stringResource(R.string.onboarding_welcome_subtitle),
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(AcademiaDimens.paddingCard))
                OnboardingBullet(stringResource(R.string.onboarding_welcome_bullet_players))
                OnboardingBullet(stringResource(R.string.onboarding_welcome_bullet_attendance))
                OnboardingBullet(stringResource(R.string.onboarding_welcome_bullet_performance))
                Spacer(Modifier.height(AcademiaDimens.paddingCard + AcademiaDimens.gapSm))
                PrimaryButton(
                    text = stringResource(R.string.onboarding_welcome_cta),
                    onClick = onComenzar,
                )
            }
        }
    }
}

@Composable
private fun OnboardingBullet(texto: String) {
    Text(
        text = "• $texto",
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = AcademiaDimens.gapSm),
    )
}
