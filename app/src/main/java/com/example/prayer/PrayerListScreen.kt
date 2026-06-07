package com.example.prayer

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

enum class FilterTab(val label: String) { ALL("전체"), PRAYING("기도 중"), ANSWERED("응답받음") }

@Composable
fun PrayerListScreen(
    store: PrayerStore,
    onBackup: () -> Unit,
    onRestore: () -> Unit
) {
    var filter by remember { mutableStateOf(FilterTab.ALL) }
    var catFilter by remember { mutableStateOf<PrayerCategory?>(null) }

    var showAdd by remember { mutableStateOf(false) }
    var editTarget by remember { mutableStateOf<Prayer?>(null) }
    var answerTarget by remember { mutableStateOf<Prayer?>(null) }
    var noteTarget by remember { mutableStateOf<Prayer?>(null) }
    var deleteTarget by remember { mutableStateOf<Prayer?>(null) }
    var showSync by remember { mutableStateOf(false) }

    val all = store.prayers
    val praying = all.count { it.status == PrayerStatus.PRAYING }
    val answered = all.count { it.status == PrayerStatus.ANSWERED }

    val list = all
        .filter {
            when (filter) {
                FilterTab.ALL -> true
                FilterTab.PRAYING -> it.status == PrayerStatus.PRAYING
                FilterTab.ANSWERED -> it.status == PrayerStatus.ANSWERED
            }
        }
        .filter { catFilter == null || it.category == catFilter }
        .sortedWith(compareByDescending<Prayer> { it.status == PrayerStatus.PRAYING }
            .thenByDescending { if (it.status == PrayerStatus.ANSWERED) (it.answeredAt ?: 0L) else it.createdAt })

    Box(Modifier.fillMaxSize().background(Paper)) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(20.dp, 28.dp, 20.dp, 110.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item { TopBar(onSync = { showSync = true }) }
            item { Header() }
            item { Stats(praying, answered) }
            item { StatusSegment(filter) { filter = it } }
            item { CategoryChips(catFilter) { catFilter = it } }

            if (list.isEmpty()) {
                item { EmptyState(all.isEmpty()) }
            } else {
                items(list, key = { it.id }) { p ->
                    PrayerCard(
                        p = p,
                        onAnswer = { answerTarget = p },
                        onNote = { noteTarget = p },
                        onEdit = { editTarget = p },
                        onReopen = { store.replace(p.copy(status = PrayerStatus.PRAYING, answeredAt = null, answerNote = "")) },
                        onDelete = { deleteTarget = p }
                    )
                }
            }
        }

        FloatingActionButton(
            onClick = { showAdd = true },
            containerColor = Sage,
            contentColor = Color.White,
            modifier = Modifier.align(Alignment.BottomEnd).padding(22.dp)
        ) { Icon(Icons.Default.Add, contentDescription = "추가") }
    }

    // 다이얼로그들
    if (showAdd) {
        AddEditDialog(
            initial = null,
            onSubmit = { t, d, c -> store.add(t, d, c); showAdd = false },
            onDismiss = { showAdd = false }
        )
    }
    editTarget?.let { p ->
        AddEditDialog(
            initial = p,
            onSubmit = { t, d, c -> store.replace(p.copy(title = t, detail = d, category = c)); editTarget = null },
            onDismiss = { editTarget = null }
        )
    }
    answerTarget?.let { p ->
        AnswerDialog(
            prayer = p,
            onConfirm = { note -> store.replace(p.copy(status = PrayerStatus.ANSWERED, answerNote = note, answeredAt = System.currentTimeMillis())); answerTarget = null },
            onDismiss = { answerTarget = null }
        )
    }
    noteTarget?.let { p ->
        NoteDialog(
            prayer = p,
            onConfirm = { text -> store.addUpdate(p.id, text); noteTarget = null },
            onDismiss = { noteTarget = null }
        )
    }
    deleteTarget?.let { p ->
        DeleteConfirmDialog(
            prayer = p,
            onConfirm = { store.delete(p.id); deleteTarget = null },
            onDismiss = { deleteTarget = null }
        )
    }
    if (showSync) {
        SyncDialog(
            onBackup = { showSync = false; onBackup() },
            onRestore = { showSync = false; onRestore() },
            onDismiss = { showSync = false }
        )
    }
}

@Composable
private fun TopBar(onSync: () -> Unit) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
        IconButton(onClick = onSync) {
            Icon(Icons.Default.Share, contentDescription = "백업", tint = Sage)
        }
    }
}

@Composable
private fun Header() {
    Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        Text("PRAYER JOURNAL", color = GoldDeep, fontSize = 11.sp, fontWeight = FontWeight.Medium, letterSpacing = 4.sp)
        Spacer(Modifier.height(8.dp))
        Text("${OWNER_NAME}의", color = InkSoft, fontSize = 18.sp, fontWeight = FontWeight.Medium, fontFamily = FontFamily.Serif)
        Text("기도제목", color = Ink, fontSize = 38.sp, fontWeight = FontWeight.SemiBold, fontFamily = FontFamily.Serif)
        Spacer(Modifier.height(8.dp))
        Text("마음에 품은 기도를 적어 두고, 응답하심을 기록하세요",
            color = InkSoft, fontSize = 14.sp, fontStyle = FontStyle.Italic,
            fontFamily = FontFamily.Serif, textAlign = TextAlign.Center)
        Spacer(Modifier.height(12.dp))
        Box(Modifier.width(46.dp).height(1.dp).background(Gold))
    }
}

@Composable
private fun Stats(praying: Int, answered: Int) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        StatCard("기도 중", praying, Sage, Modifier.weight(1f))
        StatCard("응답받음", answered, Gold, Modifier.weight(1f))
    }
}

@Composable
private fun StatCard(label: String, num: Int, color: Color, modifier: Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = Paper2,
        border = BorderStroke(1.dp, LineC)
    ) {
        Column(Modifier.padding(vertical = 16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("$num", color = color, fontSize = 30.sp, fontWeight = FontWeight.SemiBold, fontFamily = FontFamily.Serif)
            Spacer(Modifier.height(6.dp))
            Text(label, color = InkSoft, fontSize = 12.sp)
        }
    }
}

@Composable
private fun StatusSegment(selected: FilterTab, onSelect: (FilterTab) -> Unit) {
    Row(
        Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Paper2)
            .border(1.dp, LineC, RoundedCornerShape(12.dp))
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        FilterTab.entries.forEach { tab ->
            val on = tab == selected
            Box(
                Modifier
                    .clip(RoundedCornerShape(9.dp))
                    .background(if (on) Ink else Color.Transparent)
                    .clickable { onSelect(tab) }
                    .padding(horizontal = 15.dp, vertical = 8.dp)
            ) {
                Text(tab.label, color = if (on) Paper2 else InkSoft, fontSize = 13.sp, fontWeight = FontWeight.Medium)
            }
        }
    }
}

@Composable
private fun CategoryChips(selected: PrayerCategory?, onSelect: (PrayerCategory?) -> Unit) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        item { Chip("전체 분류", null, selected == null) { onSelect(null) } }
        items(PrayerCategory.entries) { c ->
            Chip(c.display, c.color, selected == c) { onSelect(if (selected == c) null else c) }
        }
    }
}

@Composable
private fun Chip(label: String, dot: Color?, on: Boolean, onClick: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = if (on) CardBg else Paper2,
        border = BorderStroke(1.dp, if (on) Ink else LineC),
        modifier = Modifier.clickable { onClick() }
    ) {
        Row(
            Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            if (dot != null) Box(Modifier.size(8.dp).clip(CircleShape).background(dot))
            Text(label, fontSize = 12.sp, color = if (on) Ink else InkSoft,
                fontWeight = if (on) FontWeight.Medium else FontWeight.Normal)
        }
    }
}

@Composable
private fun EmptyState(noData: Boolean) {
    Column(
        Modifier.fillMaxWidth().padding(vertical = 50.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("🕊️", fontSize = 34.sp)
        Spacer(Modifier.height(10.dp))
        Text(if (noData) "아직 기록된 기도제목이 없습니다" else "해당하는 기도제목이 없습니다",
            color = InkSoft, fontSize = 17.sp, fontFamily = FontFamily.Serif)
        Spacer(Modifier.height(4.dp))
        Text(if (noData) "아래 + 버튼으로 첫 기도제목을 적어 보세요" else "다른 필터를 선택해 보세요",
            color = InkFaint, fontSize = 13.sp)
    }
}

@Composable
private fun PrayerCard(
    p: Prayer,
    onAnswer: () -> Unit,
    onNote: () -> Unit,
    onEdit: () -> Unit,
    onReopen: () -> Unit,
    onDelete: () -> Unit
) {
    val answered = p.status == PrayerStatus.ANSWERED
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = if (answered) CardWarm else CardBg,
        border = BorderStroke(1.dp, if (answered) GoldSoft else LineC),
        shadowElevation = 2.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(Modifier.height(IntrinsicSize.Min)) {
            Box(Modifier.width(3.dp).fillMaxHeight().background(if (answered) Gold else Sage))
            Column(Modifier.padding(16.dp).weight(1f)) {
                // 제목 + 분류
                Row(verticalAlignment = Alignment.Top) {
                    Text(p.title, color = if (answered) GoldDeep else Ink, fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold, fontFamily = FontFamily.Serif,
                        modifier = Modifier.weight(1f))
                    Spacer(Modifier.width(8.dp))
                    CategoryTag(p.category)
                }
                if (p.detail.isNotBlank()) {
                    Spacer(Modifier.height(8.dp))
                    Text(p.detail, color = InkSoft, fontSize = 14.sp)
                }
                Spacer(Modifier.height(12.dp))
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (answered) {
                        Text("✓ 응답받음", color = Gold, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    } else {
                        val d = daysSince(p.createdAt)
                        Text(if (d <= 0) "오늘 시작" else "${d}일째 기도", color = Sage, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                    }
                    Text("·", color = InkFaint, fontSize = 12.sp)
                    Text("${p.createdAt.korDate()} 시작", color = InkFaint, fontSize = 12.sp)
                }
                if (answered && p.answerNote.isNotBlank()) AnswerBox(p)
                if (p.updates.isNotEmpty()) UpdatesList(p)
                Spacer(Modifier.height(14.dp))
                ActionsRow(answered, onAnswer, onNote, onEdit, onReopen, onDelete)
            }
        }
    }
}

@Composable
private fun CategoryTag(c: PrayerCategory) {
    Surface(shape = RoundedCornerShape(20.dp), color = Paper, border = BorderStroke(1.dp, LineC)) {
        Row(Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
            Box(Modifier.size(7.dp).clip(CircleShape).background(c.color))
            Text(c.display, fontSize = 11.sp, color = InkSoft, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
private fun AnswerBox(p: Prayer) {
    Spacer(Modifier.height(12.dp))
    Surface(shape = RoundedCornerShape(13.dp), color = GoldSoft, border = BorderStroke(1.dp, Color(0xFFEAD9AC)),
        modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(12.dp)) {
            Text("응답의 기록", color = GoldDeep, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
            Spacer(Modifier.height(6.dp))
            Text(p.answerNote, color = Color(0xFF5F4818), fontSize = 14.sp, fontFamily = FontFamily.Serif)
            p.answeredAt?.let {
                Spacer(Modifier.height(6.dp))
                Text("${it.korDate()} 응답", color = GoldDeep, fontSize = 11.sp)
            }
        }
    }
}

@Composable
private fun UpdatesList(p: Prayer) {
    Spacer(Modifier.height(12.dp))
    Box(Modifier.fillMaxWidth().height(1.dp).background(LineC))
    Spacer(Modifier.height(10.dp))
    Column(verticalArrangement = Arrangement.spacedBy(9.dp)) {
        p.updates.sortedByDescending { it.date }.forEach { u ->
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Box(Modifier.padding(top = 6.dp).size(7.dp).clip(CircleShape).background(InkFaint))
                Column {
                    Text(u.text, color = InkSoft, fontSize = 13.sp)
                    Text(u.date.korDate(), color = InkFaint, fontSize = 11.sp)
                }
            }
        }
    }
}

@Composable
private fun ActionsRow(
    answered: Boolean,
    onAnswer: () -> Unit,
    onNote: () -> Unit,
    onEdit: () -> Unit,
    onReopen: () -> Unit,
    onDelete: () -> Unit
) {
    var menu by remember { mutableStateOf(false) }
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        if (answered) {
            PillButton("기록 추가", filled = false, onClick = onNote)
        } else {
            PillButton("응답으로 기록", filled = true, onClick = onAnswer)
        }
        Spacer(Modifier.weight(1f))
        Box {
            Surface(
                shape = RoundedCornerShape(9.dp), color = Paper, border = BorderStroke(1.dp, LineC),
                modifier = Modifier.clickable { menu = true }
            ) {
                Icon(Icons.Default.MoreVert, contentDescription = "더보기", tint = InkSoft,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp).size(18.dp))
            }
            DropdownMenu(expanded = menu, onDismissRequest = { menu = false }) {
                DropdownMenuItem(text = { Text("기록 추가") }, onClick = { menu = false; onNote() },
                    leadingIcon = { Icon(Icons.Default.Edit, null) })
                if (answered) {
                    DropdownMenuItem(text = { Text("다시 기도") }, onClick = { menu = false; onReopen() },
                        leadingIcon = { Icon(Icons.Default.Refresh, null) })
                } else {
                    DropdownMenuItem(text = { Text("수정") }, onClick = { menu = false; onEdit() },
                        leadingIcon = { Icon(Icons.Default.Edit, null) })
                }
                DropdownMenuItem(text = { Text("삭제", color = RoseC) }, onClick = { menu = false; onDelete() },
                    leadingIcon = { Icon(Icons.Default.Delete, null, tint = RoseC) })
            }
        }
    }
}

@Composable
private fun PillButton(label: String, filled: Boolean, onClick: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(9.dp),
        color = if (filled) Gold else Paper,
        border = if (filled) null else BorderStroke(1.dp, LineC),
        modifier = Modifier.clickable { onClick() }
    ) {
        Row(Modifier.padding(horizontal = 12.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp)) {
            if (filled) Icon(Icons.Default.Check, null, tint = Color.White, modifier = Modifier.size(14.dp))
            Text(label, fontSize = 12.5f.sp, color = if (filled) Color.White else InkSoft, fontWeight = FontWeight.Medium)
        }
    }
}
