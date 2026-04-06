package com.escuelafutbol.academia.ui.util

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.escuelafutbol.academia.R

@Composable
fun FullscreenImageViewerDialog(
    titulo: String,
    imageModel: Any,
    contentDescription: String,
    onDismiss: () -> Unit,
) {
    val maxH = LocalConfiguration.current.screenHeightDp.dp * 0.78f
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
        ),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.88f))
                .clickable(onClick = onDismiss),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(0.92f),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    titulo,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White.copy(alpha = 0.95f),
                    modifier = Modifier.padding(bottom = 12.dp),
                )
                val interaction = remember { MutableInteractionSource() }
                AsyncImage(
                    model = imageModel,
                    contentDescription = contentDescription,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = maxH)
                        .clip(MaterialTheme.shapes.large)
                        .clickable(
                            interactionSource = interaction,
                            indication = null,
                        ) { },
                    contentScale = ContentScale.Fit,
                )
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.padding(top = 16.dp),
                ) {
                    Text(
                        stringResource(R.string.player_photo_viewer_close),
                        color = Color.White.copy(alpha = 0.9f),
                    )
                }
            }
        }
    }
}
