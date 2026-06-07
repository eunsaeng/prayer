package com.example.prayer

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun AddEditDialog(
    initial: Prayer?,
    onSubmit: (String, String, PrayerCategory) -> Unit,
    onDismiss: () -> Unit
) {
    var title by remember { mutableStateOf(initial?.title ?: "") }
    var detail by remember { mutableStateOf(initial?.detail ?: "") }
    var category by remember { mutableStateOf(initial?.category ?: PrayerCategory.SELF) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Paper2,
        title = { Text(if (initial == null) "새 기도제목" else "기도제목 수정",
            fontFamily = FontFamily.Serif, fontWeight = FontWeight.SemiBold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(value = title, onValueChange = { title = it },
                    label = { Text("제목") }, maxLines = 2, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = detail, onValueChange = { detail = it },
                    label = { Text("상세 내용 (선택)") }, minLines = 2, maxLines = 5, modifier = Modifier.fillMaxWidth())
                CategorySelector(category) { category = it }
            }
        },
        confirmButton = {
            TextButton(onClick = { if (title.isNotBlank()) onSubmit(title.trim(), detail.trim(), category) }) {
                Text(if (initial == null) "추가" else "저장", fontWeight = FontWeight.SemiBold)
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("취소", color = InkSoft) } }
    )
}

@Composable
private fun CategorySelector(selected: PrayerCategory, onSelect: (PrayerCategory) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        Surface(
            shape = RoundedCornerShape(8.dp), color = CardBg, border = BorderStroke(1.dp, LineC),
            modifier = Modifier.fillMaxWidth().clickable { expanded = true }
        ) {
            Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(Modifier.size(10.dp).clip(CircleShape).background(selected.color))
                Text("분류: ${selected.display}", color = Ink, fontSize = 15.sp)
            }
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            PrayerCategory.entries.forEach { c ->
                DropdownMenuItem(
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Box(Modifier.size(10.dp).clip(CircleShape).background(c.color))
                            Text(c.display)
                        }
                    },
                    onClick = { onSelect(c); expanded = false }
                )
            }
        }
    }
}

@Composable
fun AnswerDialog(prayer: Prayer, onConfirm: (String) -> Unit, onDismiss: () -> Unit) {
    var note by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Paper2,
        title = { Text("응답을 기록합니다", fontFamily = FontFamily.Serif, fontWeight = FontWeight.SemiBold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("\"${prayer.title}\"", color = GoldDeep, fontSize = 15.sp, fontFamily = FontFamily.Serif)
                OutlinedTextField(value = note, onValueChange = { note = it },
                    label = { Text("응답의 기록 (선택)") }, minLines = 3, maxLines = 6, modifier = Modifier.fillMaxWidth())
                Text("오늘 날짜로 기록됩니다.", color = InkFaint, fontSize = 12.sp)
            }
        },
        confirmButton = { TextButton(onClick = { onConfirm(note.trim()) }) { Text("기록", fontWeight = FontWeight.SemiBold) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("취소", color = InkSoft) } }
    )
}

@Composable
fun NoteDialog(prayer: Prayer, onConfirm: (String) -> Unit, onDismiss: () -> Unit) {
    var text by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Paper2,
        title = { Text("기록 추가", fontFamily = FontFamily.Serif, fontWeight = FontWeight.SemiBold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("\"${prayer.title}\"", color = Ink, fontSize = 15.sp, fontFamily = FontFamily.Serif)
                OutlinedTextField(value = text, onValueChange = { text = it },
                    label = { Text("진행 상황이나 마음") }, minLines = 3, maxLines = 6, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            TextButton(onClick = { if (text.isNotBlank()) onConfirm(text.trim()) }) {
                Text("저장", fontWeight = FontWeight.SemiBold)
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("취소", color = InkSoft) } }
    )
}

@Composable
fun DeleteConfirmDialog(prayer: Prayer, onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Paper2,
        title = { Text("삭제할까요?", fontFamily = FontFamily.Serif, fontWeight = FontWeight.SemiBold) },
        text = { Text("\"${prayer.title}\"\n이 기도제목과 기록이 모두 삭제됩니다. 되돌릴 수 없습니다.", color = InkSoft, fontSize = 14.sp) },
        confirmButton = { TextButton(onClick = onConfirm) { Text("삭제", color = RoseC, fontWeight = FontWeight.SemiBold) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("취소", color = InkSoft) } }
    )
}

@Composable
fun SyncDialog(onBackup: () -> Unit, onRestore: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Paper2,
        title = { Text("백업 · 복원", fontFamily = FontFamily.Serif, fontWeight = FontWeight.SemiBold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("기도제목은 이 폰에 저장됩니다. ‘내보내기’로 백업 파일을 만들 때 저장 위치로 Google Drive를 선택하면 클라우드에 보관됩니다. 다른 기기에서는 ‘불러오기’로 그 파일을 선택하세요.",
                    color = InkSoft, fontSize = 13.sp)
                Button(onClick = onBackup, modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Gold)) {
                    Text("내보내기 (백업)", color = Color.White)
                }
                OutlinedButton(onClick = onRestore, modifier = Modifier.fillMaxWidth()) {
                    Text("불러오기 (복원)", color = Sage)
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("닫기", color = InkSoft) } }
    )
}

@Composable
fun MergeDialog(count: Int, onMerge: () -> Unit, onReplace: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Paper2,
        title = { Text("가져오기", fontFamily = FontFamily.Serif, fontWeight = FontWeight.SemiBold) },
        text = {
            Text("백업 파일에 ${count}개의 기도제목이 있습니다.\n\n· 합치기: 현재 기록에 더합니다(같은 항목은 가져온 것 우선)\n· 덮어쓰기: 현재 기록을 모두 지우고 교체합니다",
                color = InkSoft, fontSize = 14.sp)
        },
        confirmButton = { TextButton(onClick = onMerge) { Text("합치기", fontWeight = FontWeight.SemiBold) } },
        dismissButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                TextButton(onClick = onReplace) { Text("덮어쓰기", color = RoseC) }
                TextButton(onClick = onDismiss) { Text("취소", color = InkSoft) }
            }
        }
    )
}
