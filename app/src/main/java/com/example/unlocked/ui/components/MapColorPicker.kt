package com.example.unlocked.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

data class ColorOption(val name: String, val hex: String)

@Composable
fun MapColorPicker(
    selectedColor: String,
    onColorSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    // Define a list of color options with descriptive names
    val colorOptions = listOf(
        ColorOption("Red", "#FF0000"),
        ColorOption("Blue", "#0000FF"),
        ColorOption("Green", "#00FF00"),
        ColorOption("Yellow", "#FFFF00"),
        ColorOption("Orange", "#FFA500"),
        ColorOption("Purple", "#800080"),
        ColorOption("Pink", "#FFC0CB"),
        ColorOption("Cyan", "#00FFFF"),
        ColorOption("Magenta", "#FF00FF"),
        ColorOption("Teal", "#008080"),
        ColorOption("Lime", "#32CD32"),
        ColorOption("Brown", "#A52A2A")
    )

    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "Map Pin Color",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Color selection grid
        LazyVerticalGrid(
            columns = GridCells.Fixed(4),
            contentPadding = PaddingValues(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.height(160.dp) // Fixed height to fit 3 rows
        ) {
            items(colorOptions) { colorOption ->
                val isSelected = selectedColor == colorOption.hex
                val parsedColor = try {
                    Color(android.graphics.Color.parseColor(colorOption.hex))
                } catch (e: Exception) {
                    Color.Red
                }

                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(parsedColor)
                        .border(
                            width = 2.dp,
                            color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                            shape = CircleShape
                        )
                        .clickable {
                            onColorSelected(colorOption.hex)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    if (isSelected) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Selected",
                            tint = if (isDarkColor(colorOption.hex)) Color.White else Color.Black
                        )
                    }
                }
            }
        }

        // Show the name of the selected color
        val selectedColorName = colorOptions.find { it.hex == selectedColor }?.name ?: "Custom"
        Text(
            text = "Selected: $selectedColorName",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(top = 8.dp, start = 8.dp)
        )
    }
}

// Helper function to determine if a color is dark (for contrast)
private fun isDarkColor(colorHex: String): Boolean {
    try {
        val color = android.graphics.Color.parseColor(colorHex)
        val darkness = 1 - (0.299 * android.graphics.Color.red(color) +
                0.587 * android.graphics.Color.green(color) +
                0.114 * android.graphics.Color.blue(color)) / 255
        return darkness >= 0.5
    } catch (e: Exception) {
        return false
    }
}