package com.escuelafutbol.academia.ui.academia

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.escuelafutbol.academia.R
import kotlinx.coroutines.launch

enum class ModoDialogoPin {
    CREAR,
    INGRESAR,
    CAMBIAR,
}

sealed interface PendienteTrasPin {
    data class Permisos(val prof: Boolean, val coord: Boolean, val dueno: Boolean) : PendienteTrasPin
}

@Composable
fun DialogoPinStaff(
    modo: ModoDialogoPin,
    pendiente: PendienteTrasPin?,
    configVm: AcademiaConfigViewModel,
    onEjecutarPendiente: (PendienteTrasPin) -> Unit,
    onCerrar: () -> Unit,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var pin1 by remember { mutableStateOf("") }
    var pin2 by remember { mutableStateOf("") }
    var pinViejo by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    val keyboardPin = KeyboardOptions(keyboardType = KeyboardType.NumberPassword)

    AlertDialog(
        onDismissRequest = onCerrar,
        title = {
            Text(
                when (modo) {
                    ModoDialogoPin.CREAR -> stringResource(R.string.pin_dialog_create_title)
                    ModoDialogoPin.INGRESAR -> stringResource(R.string.pin_dialog_enter_title)
                    ModoDialogoPin.CAMBIAR -> stringResource(R.string.pin_dialog_change_title)
                },
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                error?.let {
                    Text(
                        it,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(bottom = 8.dp),
                    )
                }
                when (modo) {
                    ModoDialogoPin.CREAR -> {
                        Text(
                            stringResource(R.string.pin_dialog_create_body),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Spacer(Modifier.height(12.dp))
                        OutlinedTextField(
                            value = pin1,
                            onValueChange = { pin1 = it.filter { c -> c.isDigit() }.take(6) },
                            label = { Text(stringResource(R.string.pin_field_new)) },
                            visualTransformation = PasswordVisualTransformation(),
                            keyboardOptions = keyboardPin,
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                        )
                        Spacer(Modifier.height(8.dp))
                        OutlinedTextField(
                            value = pin2,
                            onValueChange = { pin2 = it.filter { c -> c.isDigit() }.take(6) },
                            label = { Text(stringResource(R.string.pin_field_repeat)) },
                            visualTransformation = PasswordVisualTransformation(),
                            keyboardOptions = keyboardPin,
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                    ModoDialogoPin.INGRESAR -> {
                        Text(
                            stringResource(R.string.pin_dialog_enter_body),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Spacer(Modifier.height(12.dp))
                        OutlinedTextField(
                            value = pin1,
                            onValueChange = { pin1 = it.filter { c -> c.isDigit() }.take(6) },
                            label = { Text(stringResource(R.string.pin_field_current)) },
                            visualTransformation = PasswordVisualTransformation(),
                            keyboardOptions = keyboardPin,
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                    ModoDialogoPin.CAMBIAR -> {
                        Text(
                            stringResource(R.string.pin_dialog_change_body),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Spacer(Modifier.height(12.dp))
                        OutlinedTextField(
                            value = pinViejo,
                            onValueChange = { pinViejo = it.filter { c -> c.isDigit() }.take(6) },
                            label = { Text(stringResource(R.string.pin_field_current)) },
                            visualTransformation = PasswordVisualTransformation(),
                            keyboardOptions = keyboardPin,
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                        )
                        Spacer(Modifier.height(8.dp))
                        OutlinedTextField(
                            value = pin1,
                            onValueChange = { pin1 = it.filter { c -> c.isDigit() }.take(6) },
                            label = { Text(stringResource(R.string.pin_field_new)) },
                            visualTransformation = PasswordVisualTransformation(),
                            keyboardOptions = keyboardPin,
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                        )
                        Spacer(Modifier.height(8.dp))
                        OutlinedTextField(
                            value = pin2,
                            onValueChange = { pin2 = it.filter { c -> c.isDigit() }.take(6) },
                            label = { Text(stringResource(R.string.pin_field_repeat)) },
                            visualTransformation = PasswordVisualTransformation(),
                            keyboardOptions = keyboardPin,
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }
                Text(
                    stringResource(R.string.pin_forgot_hint),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 12.dp),
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    error = null
                    when (modo) {
                        ModoDialogoPin.CREAR -> {
                            scope.launch {
                                if (configVm.intentarGuardarPinNuevo(pin1, pin2)) {
                                    pendiente?.let { onEjecutarPendiente(it) }
                                    onCerrar()
                                } else {
                                    error = context.getString(R.string.pin_error_create)
                                }
                            }
                        }
                        ModoDialogoPin.INGRESAR -> {
                            scope.launch {
                                if (configVm.intentarVerificarPin(pin1)) {
                                    pendiente?.let { onEjecutarPendiente(it) }
                                    onCerrar()
                                } else {
                                    error = context.getString(R.string.pin_error_wrong)
                                }
                            }
                        }
                        ModoDialogoPin.CAMBIAR -> {
                            scope.launch {
                                if (configVm.intentarCambiarPin(pinViejo, pin1, pin2)) {
                                    onCerrar()
                                } else {
                                    error = context.getString(R.string.pin_error_change)
                                }
                            }
                        }
                    }
                },
            ) {
                Text(stringResource(R.string.pin_confirm))
            }
        },
        dismissButton = {
            TextButton(onClick = onCerrar) {
                Text(stringResource(R.string.cancel))
            }
        },
    )
}
