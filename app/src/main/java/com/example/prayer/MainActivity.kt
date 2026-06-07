package com.example.prayer

import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val store = PrayerStore(applicationContext).apply { load() }
        setContent {
            PrayerTheme { AppRoot(store) }
        }
    }
}

fun toast(context: Context, msg: String) =
    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()

private fun defaultBackupName(): String {
    val d = SimpleDateFormat("yyyy-MM-dd", Locale.KOREA).format(Date())
    return "${OWNER_NAME}의-기도제목-백업-$d.json"
}

@Composable
fun AppRoot(store: PrayerStore) {
    val context = LocalContext.current
    var pendingImport by remember { mutableStateOf<List<Prayer>?>(null) }

    // 백업: JSON 문서 새로 만들기 (저장 위치로 Google Drive 등 선택 가능)
    val createDoc = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        if (uri != null) {
            val ok = runCatching {
                context.contentResolver.openOutputStream(uri)?.use {
                    it.write(store.exportJson().toByteArray())
                }
            }.isSuccess
            toast(context, if (ok) "백업했습니다." else "백업에 실패했습니다.")
        }
    }

    // 복원: 기존 JSON 문서 열기
    val openDoc = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            runCatching {
                val text = context.contentResolver.openInputStream(uri)
                    ?.bufferedReader()?.use { it.readText() } ?: ""
                PrayerStore.parse(text)
            }.onSuccess { pendingImport = it }
                .onFailure { toast(context, "백업 파일을 읽을 수 없습니다.") }
        }
    }

    PrayerListScreen(
        store = store,
        onBackup = { createDoc.launch(defaultBackupName()) },
        onRestore = { openDoc.launch(arrayOf("*/*")) }
    )

    pendingImport?.let { list ->
        MergeDialog(
            count = list.size,
            onMerge = { store.mergeAll(list); pendingImport = null; toast(context, "가져온 ${list.size}개를 합쳤습니다.") },
            onReplace = { store.replaceAll(list); pendingImport = null; toast(context, "가져온 ${list.size}개로 교체했습니다.") },
            onDismiss = { pendingImport = null }
        )
    }
}
