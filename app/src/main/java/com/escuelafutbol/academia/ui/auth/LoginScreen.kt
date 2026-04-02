package com.escuelafutbol.academia.ui.auth



import androidx.compose.foundation.layout.Arrangement

import androidx.compose.foundation.layout.Column

import androidx.compose.foundation.layout.Row

import androidx.compose.foundation.layout.Spacer

import androidx.compose.foundation.layout.fillMaxSize

import androidx.compose.foundation.layout.fillMaxWidth

import androidx.compose.foundation.layout.height

import androidx.compose.foundation.layout.padding

import androidx.compose.foundation.layout.size

import androidx.compose.foundation.layout.width

import androidx.compose.foundation.rememberScrollState

import androidx.compose.foundation.text.KeyboardOptions

import androidx.compose.foundation.verticalScroll

import androidx.compose.material3.Button

import androidx.compose.material3.CircularProgressIndicator

import androidx.compose.material3.MaterialTheme

import androidx.compose.material3.OutlinedTextField

import androidx.compose.material3.Text

import androidx.compose.material3.TextButton

import androidx.compose.runtime.Composable

import androidx.compose.runtime.collectAsState

import androidx.compose.runtime.getValue

import androidx.compose.runtime.mutableStateOf

import androidx.compose.runtime.saveable.rememberSaveable

import androidx.compose.runtime.setValue

import androidx.compose.ui.Alignment

import androidx.compose.ui.Modifier

import androidx.compose.ui.res.stringResource

import androidx.compose.ui.text.input.KeyboardType

import androidx.compose.ui.text.input.PasswordVisualTransformation

import androidx.compose.ui.unit.dp

import com.escuelafutbol.academia.R



@Composable

fun LoginScreen(viewModel: AuthViewModel) {

    val busy by viewModel.busy.collectAsState()

    val err by viewModel.errorMessage.collectAsState()

    val info by viewModel.infoMessage.collectAsState()



    var email by rememberSaveable { mutableStateOf("") }

    var password by rememberSaveable { mutableStateOf("") }

    var modoRegistro by rememberSaveable { mutableStateOf(false) }



    Column(

        modifier = Modifier

            .fillMaxSize()

            .verticalScroll(rememberScrollState())

            .padding(24.dp),

        verticalArrangement = Arrangement.Center,

        horizontalAlignment = Alignment.CenterHorizontally,

    ) {

        Text(
            stringResource(
                if (modoRegistro) R.string.auth_register_title else R.string.auth_login_title,
            ),
            style = MaterialTheme.typography.headlineSmall,
        )

        Spacer(Modifier.height(8.dp))

        Text(
            stringResource(
                if (modoRegistro) R.string.auth_register_subtitle else R.string.auth_login_subtitle,
            ),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(Modifier.height(24.dp))

        OutlinedTextField(

            value = email,

            onValueChange = { email = it },

            label = { Text(stringResource(R.string.auth_email)) },

            singleLine = true,

            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),

            modifier = Modifier.fillMaxWidth(),

        )

        Spacer(Modifier.height(12.dp))

        OutlinedTextField(

            value = password,

            onValueChange = { password = it },

            label = { Text(stringResource(R.string.auth_password)) },

            singleLine = true,

            visualTransformation = PasswordVisualTransformation(),

            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),

            modifier = Modifier.fillMaxWidth(),

        )

        err?.let { msg ->

            Spacer(Modifier.height(12.dp))

            Text(

                msg,

                color = MaterialTheme.colorScheme.error,

                style = MaterialTheme.typography.bodySmall,

            )

        }

        info?.let { msg ->

            Spacer(Modifier.height(12.dp))

            Text(

                msg,

                color = MaterialTheme.colorScheme.primary,

                style = MaterialTheme.typography.bodySmall,

            )

        }

        Spacer(Modifier.height(20.dp))

        Button(

            onClick = {

                viewModel.clearError()

                viewModel.clearInfo()

                if (modoRegistro) {

                    viewModel.signUp(email, password)

                } else {

                    viewModel.signIn(email, password)

                }

            },

            enabled = !busy && email.isNotBlank() && password.isNotBlank(),

            modifier = Modifier.fillMaxWidth(),

        ) {

            Row(

                verticalAlignment = Alignment.CenterVertically,

                horizontalArrangement = Arrangement.Center,

            ) {

                if (busy) {

                    CircularProgressIndicator(

                        modifier = Modifier.size(20.dp),

                        strokeWidth = 2.dp,

                    )

                    Spacer(Modifier.width(10.dp))

                }

                Text(

                    if (modoRegistro) {

                        stringResource(R.string.auth_create_account)

                    } else {

                        stringResource(R.string.auth_sign_in)

                    },

                )

            }

        }

        TextButton(

            onClick = {

                viewModel.clearError()

                viewModel.clearInfo()

                modoRegistro = !modoRegistro

            },

            enabled = !busy,

            modifier = Modifier.fillMaxWidth(),

        ) {

            Text(

                if (modoRegistro) {

                    stringResource(R.string.auth_already_have_account)

                } else {

                    stringResource(R.string.auth_need_account)

                },

            )

        }

    }

}

