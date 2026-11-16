package com.tanjid.hisabapp.ui

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// TallyKhata red
val HisabRed = Color(0xFFC62828)
val HisabRedDark = Color(0xFF9E1B1B)
val HisabGreen = Color(0xFF0E7A5F)
val HisabGreenLight = Color(0xFFE6F4EA)

private val LightColors = lightColorScheme(
    primary = HisabRed,
    onPrimary = Color.White,
    secondary = HisabGreen,
    background = Color(0xFFF6F6F6),
    surface = Color.White
)

@Composable
fun HisabTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = LightColors, content = content)
}

fun taka(amount: Long): String = "৳%,d".format(amount)
