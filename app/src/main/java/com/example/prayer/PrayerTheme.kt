package com.example.prayer

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// 묵상 노트 톤 팔레트 (iOS 버전과 동일)
val Paper = Color(0xFFF6F1E7)
val Paper2 = Color(0xFFFCF9F2)
val CardBg = Color(0xFFFBF7EE)
val CardWarm = Color(0xFFFCF6E6)
val Ink = Color(0xFF2C2922)
val InkSoft = Color(0xFF736B5C)
val InkFaint = Color(0xFFA89F8D)
val LineC = Color(0xFFE6DCC9)
val Sage = Color(0xFF5E6E52)
val Gold = Color(0xFFB0832F)
val GoldDeep = Color(0xFF8A6420)
val GoldSoft = Color(0xFFF4E9CC)
val RoseC = Color(0xFFA85A48)

private val PrayerColors = lightColorScheme(
    primary = Sage, onPrimary = Color.White,
    secondary = Gold, onSecondary = Color.White,
    background = Paper, onBackground = Ink,
    surface = CardBg, onSurface = Ink,
    surfaceVariant = Paper2, onSurfaceVariant = InkSoft,
    error = RoseC, onError = Color.White
)

@Composable
fun PrayerTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = PrayerColors, content = content)
}
