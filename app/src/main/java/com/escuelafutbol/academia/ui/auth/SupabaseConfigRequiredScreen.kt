package com.escuelafutbol.academia.ui.auth



import androidx.compose.foundation.layout.Arrangement

import androidx.compose.foundation.layout.Column

import androidx.compose.foundation.layout.fillMaxSize

import androidx.compose.foundation.layout.padding

import androidx.compose.material3.MaterialTheme

import androidx.compose.material3.Text

import androidx.compose.runtime.Composable

import androidx.compose.ui.Alignment

import androidx.compose.ui.Modifier

import androidx.compose.ui.res.stringResource

import androidx.compose.ui.unit.dp

import com.escuelafutbol.academia.R



@Composable

fun SupabaseConfigRequiredScreen() {

    Column(

        modifier = Modifier

            .fillMaxSize()

            .padding(24.dp),

        verticalArrangement = Arrangement.Center,

        horizontalAlignment = Alignment.CenterHorizontally,

    ) {

        Text(

            stringResource(R.string.auth_config_title),

            style = MaterialTheme.typography.titleLarge,

        )

        Text(

            stringResource(R.string.auth_config_body),

            style = MaterialTheme.typography.bodyMedium,

            color = MaterialTheme.colorScheme.onSurfaceVariant,

            modifier = Modifier.padding(top = 12.dp),

        )

    }

}

