package org.jellyfin.mobile.ui.utils

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Shapes
import androidx.compose.material.darkColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun AppTheme(content: @Composable () -> Unit) {
    val colors = remember {
        @Suppress("MagicNumber")
        darkColors(
            primary = Color(0xFFF6C66A),
            primaryVariant = Color(0xFF261636),
            secondary = Color(0xFF67D6CC),
            background = Color(0xFF160D22),
            surface = Color(0xFF30203F),
            error = Color(0xFFCF6679),
            onPrimary = Color(0xFF160D22),
            onSecondary = Color.White,
            onBackground = Color.White,
            onSurface = Color.White,
            onError = Color.White,
        )
    }
    MaterialTheme(
        colors = colors,
        shapes = Shapes(
            small = RoundedCornerShape(4.dp),
            medium = RoundedCornerShape(8.dp),
            large = RoundedCornerShape(0.dp),
        ),
        content = content,
    )
}
