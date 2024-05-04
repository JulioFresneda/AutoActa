package com.logicallynx.autoacta

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TileMode


object Brushes{
    @Composable
    fun gradient1(center: Offset, radius: Float): Brush {
        return Brush.radialGradient(colors = listOf(
            MaterialTheme.colorScheme.primary,
            MaterialTheme.colorScheme.primaryContainer,
            // Bottom color
        ),
        center = center,
        radius = radius)
    }



    @Composable
    fun gradient1animated(primaryColor: Color, center: Offset, radius: Float): Brush {
        return Brush.radialGradient(colors = listOf(
            primaryColor,
            MaterialTheme.colorScheme.primaryContainer,
            // Bottom color
        ),
            center = center,
            radius = radius)
    }

    @Composable
    fun gradient2animated(tertiaryColor: Color, center: Offset, radius: Float): Brush {
        return Brush.radialGradient(colors = listOf(
            MaterialTheme.colorScheme.primary,
            tertiaryColor  // Bottom color
        ),
            center = center,
            radius = radius)
    }

    @Composable
    fun gradient2(center: Offset, radius: Float): Brush {
        return Brush.radialGradient(colors = listOf(
            MaterialTheme.colorScheme.primary,
            MaterialTheme.colorScheme.tertiary  // Bottom color
        ),
            center = center,
            radius = radius)
    }

    @Composable
    fun gradient3(start: Offset, end:Offset, alpha: Float = 1.0f): Brush {
        return Brush.linearGradient(colors = listOf(
            MaterialTheme.colorScheme.primary.copy(alpha = alpha),
            MaterialTheme.colorScheme.secondary.copy(alpha = alpha),
            MaterialTheme.colorScheme.tertiary.copy(alpha = alpha),


            // Bottom color
        ), start = start, end = end)
    }

    @Composable
    fun gradient4(start: Offset, end:Offset, alpha: Float = 1.0f): Brush {
        return Brush.linearGradient(colors = listOf(
            MaterialTheme.colorScheme.primary.copy(alpha = alpha),
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = alpha),
            MaterialTheme.colorScheme.tertiary.copy(alpha = alpha),


            // Bottom color
        ), start = start, end = end)
    }

    @Composable
    fun gradient5(start: Offset, end:Offset, alpha: Float = 1.0f): Brush {
        return Brush.linearGradient(colors = listOf(
            MaterialTheme.colorScheme.secondary.copy(alpha = alpha),
            MaterialTheme.colorScheme.secondary,
            MaterialTheme.colorScheme.secondary.copy(alpha = alpha),


            // Bottom color
        ), start = start, end = end)
    }

    @Composable
    fun gradient1white(start: Offset, end:Offset): Brush {
        return Brush.linearGradient(colors = listOf(
            MaterialTheme.colorScheme.primary,
            Color.White,
            MaterialTheme.colorScheme.primaryContainer,


            // Bottom color
        ), start = start, end = end)
    }

    @Composable
    fun gradient2white(center: Offset, radius: Float): Brush {
        return Brush.radialGradient(colors = listOf(
            MaterialTheme.colorScheme.primary,
            MaterialTheme.colorScheme.tertiary,
            MaterialTheme.colorScheme.primaryContainer,
            Color.White,// Bottom color


        ),
            center = center,
            radius = radius)
    }

    @Composable
    fun gradient3white(start: Offset, end:Offset, lateralColor: Color = MaterialTheme.colorScheme.primaryContainer, centerColor: Color = Color.White): Brush {
        return Brush.linearGradient(colors = listOf(
            lateralColor,
            centerColor,
            lateralColor,


            // Bottom color
        ), start = start, end = end)
    }

    @Composable
    fun gradient8(start: Offset, end:Offset, alpha: Float = 1.0f): Brush {
        return Brush.linearGradient(colors = listOf(
            MaterialTheme.colorScheme.primary.copy(alpha = alpha),
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = alpha),
            MaterialTheme.colorScheme.primary.copy(alpha = alpha),
            MaterialTheme.colorScheme.tertiary.copy(alpha = alpha),


            // Bottom color
        ), start = start, end = end, tileMode = TileMode.Decal)
    }


}
