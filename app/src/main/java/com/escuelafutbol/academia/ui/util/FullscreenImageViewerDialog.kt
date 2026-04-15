package com.escuelafutbol.academia.ui.util

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.escuelafutbol.academia.R

private const val ZOOM_MIN = 1f
private const val ZOOM_MAX = 5f

@Composable
fun FullscreenImageViewerDialog(
    titulo: String,
    imageModel: Any,
    contentDescription: String,
    onDismiss: () -> Unit,
) {
    val maxH = LocalConfiguration.current.screenHeightDp.dp * 0.78f
    var scale by remember { mutableFloatStateOf(ZOOM_MIN) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    LaunchedEffect(imageModel) {
        scale = ZOOM_MIN
        offset = Offset.Zero
    }

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
                .background(Color.Black.copy(alpha = 0.88f)),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onDismiss,
                    ),
            )
            Column(
                modifier = Modifier
                    .fillMaxWidth(0.92f)
                    .align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    titulo,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White.copy(alpha = 0.95f),
                    modifier = Modifier.padding(bottom = 12.dp),
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = maxH)
                        .clip(MaterialTheme.shapes.large)
                        .pointerInput(Unit) {
                            detectTransformGestures { _, pan, zoom, _ ->
                                scale = (scale * zoom).coerceIn(ZOOM_MIN, ZOOM_MAX)
                                if (scale > ZOOM_MIN) {
                                    offset += pan
                                }
                            }
                        }
                        .pointerInput(Unit) {
                            detectTapGestures(
                                onDoubleTap = {
                                    scale = ZOOM_MIN
                                    offset = Offset.Zero
                                },
                            )
                        }
                        .graphicsLayer {
                            scaleX = scale
                            scaleY = scale
                            translationX = offset.x
                            translationY = offset.y
                            transformOrigin = TransformOrigin(0.5f, 0.5f)
                        },
                ) {
                    AsyncImage(
                        model = imageModel,
                        contentDescription = contentDescription,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Fit,
                    )
                }
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
