package com.example.prayer

import androidx.compose.ui.graphics.Color
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class PrayerStatus { PRAYING, ANSWERED }

enum class PrayerCategory(val key: String, val display: String, val color: Color) {
    FAMILY("family", "가족", Color(0xFFA85A48)),
    CHURCH("church", "교회", Color(0xFF5E6E52)),
    SELF("self", "개인", Color(0xFF3E6B8A)),
    WORK("work", "직장·사역", Color(0xFF8A6420)),
    MISSION("mission", "선교", Color(0xFF7A5A8A)),
    NATION("nation", "나라·민족", Color(0xFF4A7A6A)),
    ETC("etc", "기타", Color(0xFF8A8270));

    companion object {
        fun from(key: String?): PrayerCategory = entries.firstOrNull { it.key == key } ?: ETC
    }
}

data class PrayerUpdate(val id: String, val text: String, val date: Long)

data class Prayer(
    val id: String,
    val title: String,
    val detail: String,
    val category: PrayerCategory,
    val status: PrayerStatus,
    val createdAt: Long,
    val answeredAt: Long?,
    val answerNote: String,
    val updates: List<PrayerUpdate>
)

// 날짜 헬퍼
fun Long.korDate(): String = SimpleDateFormat("yyyy. M. d.", Locale.KOREA).format(Date(this))

fun daysSince(createdAt: Long): Int {
    val day = 24L * 60 * 60 * 1000
    val tz = java.util.TimeZone.getDefault().getOffset(System.currentTimeMillis())
    val a = (createdAt + tz) / day
    val b = (System.currentTimeMillis() + tz) / day
    return (b - a).toInt()
}
