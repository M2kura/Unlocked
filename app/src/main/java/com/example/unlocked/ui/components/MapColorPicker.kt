package com.example.unlocked.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun MapColorPicker(
    selectedColor: String,
    onColorSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    // Convert hex to hue value (0-360)
    val currentHue = remember(selectedColor) {
        hexToHue(selectedColor)
    }

    var hue by remember { mutableFloatStateOf(currentHue) }

    // Update hue when selectedColor changes externally
    LaunchedEffect(selectedColor) {
        hue = hexToHue(selectedColor)
    }

    val currentColor = Color.hsv(hue, 1f, 1f)
    val animatedColor by animateColorAsState(
        targetValue = currentColor,
        animationSpec = tween(200),
        label = "color_animation"
    )

    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "Pin Color",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Color preview
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(animatedColor)
                .border(
                    2.dp,
                    MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                    CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                tint = if (isDarkColor(hueToHex(hue))) Color.White else Color.Black,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Rainbow gradient track
        Box(
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                Color.hsv(0f, 1f, 1f),      // Red
                                Color.hsv(60f, 1f, 1f),     // Yellow
                                Color.hsv(120f, 1f, 1f),    // Green
                                Color.hsv(180f, 1f, 1f),    // Cyan
                                Color.hsv(240f, 1f, 1f),    // Blue
                                Color.hsv(300f, 1f, 1f),    // Magenta
                                Color.hsv(360f, 1f, 1f)     // Red again
                            )
                        )
                    )
            )

            Slider(
                value = hue,
                onValueChange = { newHue ->
                    hue = newHue
                    onColorSelected(hueToHex(newHue))
                },
                valueRange = 0f..360f,
                modifier = Modifier.fillMaxWidth(),
                colors = SliderDefaults.colors(
                    thumbColor = animatedColor,
                    activeTrackColor = Color.Transparent,
                    inactiveTrackColor = Color.Transparent
                )
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Show hex value
        Text(
            text = hueToHex(hue).uppercase(),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
    }
}

private fun hexToHue(hex: String): Float {
    return try {
        val color = android.graphics.Color.parseColor(hex)
        val hsv = FloatArray(3)
        android.graphics.Color.colorToHSV(color, hsv)
        hsv[0] // Return hue (0-360)
    } catch (e: Exception) {
        0f // Default to red
    }
}

private fun hueToHex(hue: Float): String {
    val color = Color.hsv(hue, 1f, 1f)
    return String.format("#%06X", 0xFFFFFF and color.hashCode())
}

private fun isDarkColor(colorHex: String): Boolean {
    return try {
        val color = android.graphics.Color.parseColor(colorHex)
        val darkness = 1 - (0.299 * android.graphics.Color.red(color) +
                0.587 * android.graphics.Color.green(color) +
                0.114 * android.graphics.Color.blue(color)) / 255
        darkness >= 0.5
    } catch (e: Exception) {
        false
    }
}