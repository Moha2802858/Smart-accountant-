package com.smartaccounting.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Undo
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.smartaccounting.app.data.model.JournalEntryEntity
import com.smartaccounting.app.ui.theme.AmberPending
import com.smartaccounting.app.ui.theme.BackgroundDark
import com.smartaccounting.app.ui.theme.GoldAccent
import com.smartaccounting.app.ui.theme.GreenPositive
import com.smartaccounting.app.ui.theme.NavyCard
import com.smartaccounting.app.ui.theme.NavyDark
import com.smartaccounting.app.ui.theme.NavySurface
import com.smartaccounting.app.ui.theme.RedNegative
import com.smartaccounting.app.ui.theme.TealAccent
import com.smartaccounting.app.ui.theme.TextMuted
import com.smartaccounting.app.ui.theme.TextPrimary
import com.smartaccounting.app.ui.theme.TextSecondary
import com.smartaccounting.app.ui.viewmodel.AccountingViewModel

@Composable
fun JournalScreen(viewModel: AccountingViewModel) {
    val entries by viewModel.journalEntries.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedStatusFilter by remember { mutableStateOf("الكل") }
    var showDeleteConfirm by remember { mutableStateOf<JournalEntryEntity?>(null) }

    val filteredEntries = entries.filter { e ->
        val matchesSearch = e.movementNo.contains(searchQuery, ignoreCase = true) ||
                e.description.contains(searchQuery, ignoreCase = true) ||
                e.region.contains(searchQuery, ignoreCase = true)
        val matchesStatus = when (selectedStatusFilter) {
            "مرحل" -> e.status == "posted"
            "مسودة" -> e.status == "draft"
            else -> true
        }
        matchesSearch && matchesStatus
    }

    Scaffold(
        floatingActionButton = {
            if (currentUser?.role in listOf("admin", "accountant")) {
                FloatingActionButton(
                    onClick = { viewModel.openNewJournalEntry() },
                    containerColor = GoldAccent,
                    contentColor = NavyDark,
                    modifier = Modifier.testTag("add_journal_entry_fab")
                ) {
                    Icon(Icons.Default.Add, contentDescription = "قيد جديد")
                }
            }
        },
        containerColor = BackgroundDark
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            SectionHeader(
                title = "📒 دفتر اليومية العامة",
                subtitle = "إجمالي ${entries.size} قيد مسجل"
            )

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("بحث برقم الحركة، البيان، أو المنطقة...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = GoldAccent) },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("search_journal_input"),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = GoldAccent,
                    focusedLabelColor = GoldAccent,
                    unfocusedBorderColor = NavyCard
                )
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Status Filter Chips
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                val filterOptions = listOf("الكل", "مرحل", "مسودة")
                items(filterOptions) { opt ->
                    FilterChip(
                        selected = selectedStatusFilter == opt,
                        onClick = { selectedStatusFilter = opt },
                        label = { Text(opt) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = GoldAccent,
                            selectedLabelColor = NavyDark
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Journal List
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 80.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (filteredEntries.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(40.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("لا توجد قيود مسجلة", color = TextMuted)
                        }
                    }
                } else {
                    items(filteredEntries, key = { it.id }) { entry ->
                        JournalEntryCard(
                            entry = entry,
                            canEdit = currentUser?.role in listOf("admin", "accountant"),
                            onEdit = { viewModel.openEditJournalEntry(entry.id) },
                            onDelete = { showDeleteConfirm = entry },
                            onTogglePost = {
                                if (entry.status == "posted") {
                                    viewModel.unpostEntry(entry.id)
                                } else {
                                    viewModel.postEntry(entry.id)
                                }
                            }
                        )
                    }
                }
            }
        }
    }

    if (showDeleteConfirm != null) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = null },
            title = { Text("حذف القيد", fontWeight = FontWeight.Bold) },
            text = {
                Text("هل أنت متأكد من حذف القيد حركة #${showDeleteConfirm?.movementNo}؟")
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteConfirm?.let { viewModel.deleteJournalEntry(it.id) }
                        showDeleteConfirm = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RedNegative)
                ) {
                    Text("حذف")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = null }) {
                    Text("إلغاء")
                }
            },
            containerColor = NavySurface
        )
    }
}

@Composable
fun JournalEntryCard(
    entry: JournalEntryEntity,
    canEdit: Boolean,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onTogglePost: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onEdit() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = NavySurface)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "حركة #${entry.movementNo}",
                        fontWeight = FontWeight.Bold,
                        color = GoldAccent,
                        fontSize = 16.sp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    StatusBadge(status = entry.status)
                    Spacer(modifier = Modifier.width(6.dp))
                    Surface(
                        color = NavyCard,
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = entry.entryType,
                            color = TextSecondary,
                            fontSize = 10.sp,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Text(
                    text = entry.entryDate,
                    color = TextSecondary,
                    fontSize = 12.sp
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = entry.description.ifBlank { "بدون بيان" },
                color = TextPrimary,
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "المنطقة: ${entry.region}",
                    color = TextMuted,
                    fontSize = 12.sp
                )

                if (canEdit) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        OutlinedButton(
                            onClick = onTogglePost,
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.height(34.dp)
                        ) {
                            Icon(
                                imageVector = if (entry.status == "posted") Icons.Default.Undo else Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = if (entry.status == "posted") AmberPending else GreenPositive,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (entry.status == "posted") "إلغاء الترحيل" else "ترحيل",
                                fontSize = 11.sp,
                                color = if (entry.status == "posted") AmberPending else GreenPositive
                            )
                        }

                        IconButton(onClick = onEdit) {
                            Icon(Icons.Default.Edit, contentDescription = "تعديل", tint = TextSecondary, modifier = Modifier.size(20.dp))
                        }

                        IconButton(onClick = onDelete) {
                            Icon(Icons.Default.Delete, contentDescription = "حذف", tint = RedNegative, modifier = Modifier.size(20.dp))
                        }
                    }
                }
            }
        }
    }
}
