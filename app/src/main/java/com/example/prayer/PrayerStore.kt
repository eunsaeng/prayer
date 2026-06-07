package com.example.prayer

import android.content.Context
import androidx.compose.runtime.mutableStateListOf
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.util.UUID

class PrayerStore(private val context: Context) {

    val prayers = mutableStateListOf<Prayer>()
    private val file: File get() = File(context.filesDir, "prayers.json")

    fun load() {
        prayers.clear()
        if (file.exists()) {
            runCatching { prayers.addAll(parse(file.readText())) }
        }
    }

    private fun persist() {
        runCatching { file.writeText(toJson(prayers)) }
    }

    fun add(title: String, detail: String, category: PrayerCategory) {
        prayers.add(
            0,
            Prayer(
                id = UUID.randomUUID().toString(),
                title = title, detail = detail, category = category,
                status = PrayerStatus.PRAYING,
                createdAt = System.currentTimeMillis(),
                answeredAt = null, answerNote = "", updates = emptyList()
            )
        )
        persist()
    }

    fun replace(p: Prayer) {
        val i = prayers.indexOfFirst { it.id == p.id }
        if (i >= 0) prayers[i] = p
        persist()
    }

    fun delete(id: String) {
        prayers.removeAll { it.id == id }
        persist()
    }

    fun addUpdate(id: String, text: String) {
        val i = prayers.indexOfFirst { it.id == id }
        if (i >= 0) {
            val p = prayers[i]
            prayers[i] = p.copy(
                updates = p.updates + PrayerUpdate(UUID.randomUUID().toString(), text, System.currentTimeMillis())
            )
            persist()
        }
    }

    fun replaceAll(list: List<Prayer>) {
        prayers.clear(); prayers.addAll(list); persist()
    }

    fun mergeAll(list: List<Prayer>) {
        val incomingIds = list.map { it.id }.toSet()
        val kept = prayers.filter { it.id !in incomingIds }   // 가져온 것 우선
        prayers.clear(); prayers.addAll(list + kept); persist()
    }

    fun exportJson(): String = toJson(prayers)

    companion object {
        fun toJson(list: List<Prayer>): String {
            val arr = JSONArray()
            list.forEach { p ->
                val o = JSONObject()
                o.put("id", p.id); o.put("title", p.title); o.put("detail", p.detail)
                o.put("category", p.category.key)
                o.put("status", if (p.status == PrayerStatus.ANSWERED) "answered" else "praying")
                o.put("createdAt", p.createdAt)
                o.put("answeredAt", p.answeredAt ?: JSONObject.NULL)
                o.put("answerNote", p.answerNote)
                val ua = JSONArray()
                p.updates.forEach { u ->
                    ua.put(JSONObject().put("id", u.id).put("text", u.text).put("date", u.date))
                }
                o.put("updates", ua)
                arr.put(o)
            }
            return JSONObject()
                .put("app", "kes-prayer-journal")
                .put("version", 1)
                .put("owner", OWNER_NAME)
                .put("exportedAt", System.currentTimeMillis())
                .put("prayers", arr)
                .toString(2)
        }

        fun parse(text: String): List<Prayer> {
            val root = JSONObject(text)
            val arr = root.optJSONArray("prayers") ?: JSONArray()
            val out = ArrayList<Prayer>()
            for (i in 0 until arr.length()) {
                val o = arr.getJSONObject(i)
                val ua = o.optJSONArray("updates") ?: JSONArray()
                val updates = ArrayList<PrayerUpdate>()
                for (j in 0 until ua.length()) {
                    val u = ua.getJSONObject(j)
                    updates.add(
                        PrayerUpdate(
                            u.optString("id", UUID.randomUUID().toString()),
                            u.optString("text", ""),
                            u.optLong("date", System.currentTimeMillis())
                        )
                    )
                }
                out.add(
                    Prayer(
                        id = o.optString("id", UUID.randomUUID().toString()),
                        title = o.optString("title", "(제목 없음)"),
                        detail = o.optString("detail", ""),
                        category = PrayerCategory.from(o.optString("category", "etc")),
                        status = if (o.optString("status", "praying") == "answered")
                            PrayerStatus.ANSWERED else PrayerStatus.PRAYING,
                        createdAt = o.optLong("createdAt", System.currentTimeMillis()),
                        answeredAt = if (o.isNull("answeredAt")) null else o.optLong("answeredAt"),
                        answerNote = o.optString("answerNote", ""),
                        updates = updates
                    )
                )
            }
            return out
        }
    }
}
