package com.smartaccounting.app.ui.screens

import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
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
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.smartaccounting.app.data.model.AccountEntity
import com.smartaccounting.app.data.model.JournalEntryEntity
import com.smartaccounting.app.data.model.JournalLineEntity
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
import kotlin.math.abs
import kotlin.math.round

data class LineEditorState(
    var accountId: Long = 0L,
    var debitText: String = "0",
    var creditText: String = "0"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JournalEditorScreen(viewModel: AccountingViewModel) {
    BackHandler { viewModel.navigateBack() }

    val editing = viewModel.editingEntry.collectAsState().value
    val accounts by viewModel.accounts.collectAsState()
    val regions by viewModel.regions.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    var movementNo by remember(editing) { mutableStateOf(editing?.entry?.movementNo ?: "") }
    var entryDate by remember(editing) { mutableStateOf(editing?.entry?.entryDate ?: "") }
    var description by remember(editing) { mutableStateOf(editing?.entry?.description ?: "") }
    var region by remember(editing) { mutableStateOf(editing?.entry?.region ?: "المركز الرئيسي") }
    var entryType by remember(editing) { mutableStateOf(editing?.entry?.entryType ?: "عادي") }
    var status by remember(editing) { mutableStateOf(editing?.entry?.status ?: "draft") }

    val lines = remember(editing) {
        mutableStateListOf<LineEditorState>().apply {
            if (editing != null && editing.lines.isNotEmpty()) {
                addAll(editing.lines.map {
                    LineEditorState(
                        accountId = it.accountId,
                        debitText = if (it.debit > 0) it.debit.toString() else "0",
                        creditText = if (it.credit > 0) it.credit.toString() else "0"
                    )
                })
            } else {
                add(LineEditorState(accountId = accounts.firstOrNull()?.id ?: 0L, debitText = "0", creditText = "0"))
                add(LineEditorState(accountId = accounts.getOrNull(1)?.id ?: 0L, debitText = "0", creditText = "0"))
            }
        }
    }

    val totalDebit = lines.sumOf { it.debitText.toDoubleOrNull() ?: 0.0 }
    val totalCredit = lines.sumOf { it.creditText.toDoubleOrNull() ?: 0.0 }
    val isBalanced = abs(totalDebit - totalCredit) <= 0.004 && totalDebit > 0

    var showAccountPickerForIndex by remember { mutableStateOf<Int?>(null) }
    var regionDropdownExpanded by remember { mutableStateOf(false) }
    var typeDropdownExpanded by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            Surface(color = NavyDark) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = { viewModel.navigateBack() }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "رجوع", tint = GoldAccent)
                        }
                        Text(
                            text = if (editing?.entry?.id == 0L || editing == null) "قيد يومية جديد" else "تعديل القيد #${editing.entry.movementNo}",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }

                    Button(
                        onClick = {
                            val entity = JournalEntryEntity(
                                id = editing?.entry?.id ?: 0L,
                                movementNo = movementNo.trim(),
                                entryDate = entryDate.trim(),
                                description = description.trim(),
                                region = region,
                                status = status,
                                entryType = entryType,
                                createdBy = editing?.entry?.createdBy ?: 1L
                            )
                            val lineEntities = lines.map {
                                JournalLineEntity(
                                    entryId = editing?.entry?.id ?: 0L,
                                    accountId = it.accountId,
                                    debit = it.debitText.toDoubleOrNull() ?: 0.0,
                                    credit = it.creditText.toDoubleOrNull() ?: 0.0
                                )
                            }
                            viewModel.saveEditingEntry(entity, lineEntities) {
                                viewModel.navigateBack()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = GoldAccent, contentColor = NavyDark),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("save_journal_entry_btn")
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("حفظ القيد", fontWeight = FontWeight.Bold)
                    }
                }
            }
        },
        containerColor = BackgroundDark
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(bottom = 40.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(4.dp))
                if (!errorMessage.isNullOrBlank()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(RedNegative.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                            .padding(12.dp)
                    ) {
                        Text(
                            text = errorMessage ?: "",
                            color = RedNegative,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }

                // Entry Info Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = NavySurface)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text("بيانات القيد العامة", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = GoldAccent)

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OutlinedTextField(
                                value = movementNo,
                                onValueChange = { movementNo = it },
                                label = { Text("رقم الحركة") },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("entry_movement_no_input"),
                                singleLine = true
                            )
                            OutlinedTextField(
                                value = entryDate,
                                onValueChange = { entryDate = it },
                                label = { Text("التاريخ (YYYY-MM-DD)") },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("entry_date_input"),
                                singleLine = true
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Region Dropdown
                            ExposedDropdownMenuBox(
                                expanded = regionDropdownExpanded,
                                onExpandedChange = { regionDropdownExpanded = it },
                                modifier = Modifier.weight(1f)
                            ) {
                                OutlinedTextField(
                                    value = region,
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text("المنطقة") },
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = regionDropdownExpanded) },
                                    modifier = Modifier
                                        .menuAnchor()
                                        .fillMaxWidth()
                                )
                                ExposedDropdownMenu(
                                    expanded = regionDropdownExpanded,
                                    onDismissRequest = { regionDropdownExpanded = false }
                                ) {
                                    val activeRegions = regions.filter { it.active == 1 }
                                    activeRegions.forEach { r ->
                                        DropdownMenuItem(
                                            text = { Text(r.name) },
                                            onClick = {
                                                region = r.name
                                                regionDropdownExpanded = false
                                            }
                                        )
                                    }
                                }
                            }

                            // Type Dropdown
                            ExposedDropdownMenuBox(
                                expanded = typeDropdownExpanded,
                                onExpandedChange = { typeDropdownExpanded = it },
                                modifier = Modifier.weight(1f)
                            ) {
                                OutlinedTextField(
                                    value = entryType,
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text("نوع القيد") },
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = typeDropdownExpanded) },
                                    modifier = Modifier
                                        .menuAnchor()
                                        .fillMaxWidth()
                                )
                                ExposedDropdownMenu(
                                    expanded = typeDropdownExpanded,
                                    onDismissRequest = { typeDropdownExpanded = false }
                                ) {
                                    listOf("عادي", "تسوية", "افتتاحي", "إقفال").forEach { t ->
                                        DropdownMenuItem(
                                            text = { Text(t) },
                                            onClick = {
                                                entryType = t
                                                typeDropdownExpanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        }

                        OutlinedTextField(
                            value = description,
                            onValueChange = { description = it },
                            label = { Text("البيان / الشرح") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("entry_description_input")
                        )
                    }
                }
            }

            // Balancing Indicator Bar
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isBalanced) GreenPositive.copy(alpha = 0.15f) else AmberPending.copy(alpha = 0.15f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "إجمالي المدين: ${formatCurrency(totalDebit)}",
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary,
                                fontSize = 13.sp
                            )
                            Text(
                                text = "إجمالي الدائن: ${formatCurrency(totalCredit)}",
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary,
                                fontSize = 13.sp
                            )
                        }

                        Surface(
                            color = if (isBalanced) GreenPositive else RedNegative,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = if (isBalanced) "✔ القيد متوازن" else "✖ غير متوازن (فرق: ${formatCurrency(abs(totalDebit - totalCredit))})",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "سطور القيد (مدين / دائن)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = GoldAccent
                    )

                    OutlinedButton(
                        onClick = {
                            val defaultAccId = accounts.firstOrNull()?.id ?: 0L
                            lines.add(LineEditorState(accountId = defaultAccId, debitText = "0", creditText = "0"))
                        },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("add_entry_line_btn")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp), tint = GoldAccent)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("إضافة سطر", color = GoldAccent, fontSize = 12.sp)
                    }
                }
            }

            itemsIndexed(lines) { index, line ->
                val selectedAccount = accounts.find { it.id == line.accountId }
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = NavySurface)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "السطر #${index + 1}",
                                fontWeight = FontWeight.Bold,
                                color = TextSecondary,
                                fontSize = 12.sp
                            )
                            if (lines.size > 2) {
                                IconButton(
                                    onClick = { lines.removeAt(index) },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = "حذف السطر", tint = RedNegative, modifier = Modifier.size(18.dp))
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        // Account Selector Box
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showAccountPickerForIndex = index },
                            color = NavyDark,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("الحساب المحدد:", fontSize = 11.sp, color = TextMuted)
                                    Text(
                                        text = if (selectedAccount != null) "${selectedAccount.accNo} — ${selectedAccount.name}" else "اختر الحساب...",
                                        fontWeight = FontWeight.Bold,
                                        color = GoldAccent,
                                        fontSize = 14.sp
                                    )
                                }
                                Text("تغيير ▾", color = TextSecondary, fontSize = 12.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedTextField(
                                value = line.debitText,
                                onValueChange = {
                                    lines[index] = line.copy(debitText = it, creditText = if ((it.toDoubleOrNull() ?: 0.0) > 0) "0" else line.creditText)
                                },
                                label = { Text("مدين (Debit)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("line_debit_input_$index"),
                                singleLine = true
                            )

                            OutlinedTextField(
                                value = line.creditText,
                                onValueChange = {
                                    lines[index] = line.copy(creditText = it, debitText = if ((it.toDoubleOrNull() ?: 0.0) > 0) "0" else line.debitText)
                                },
                                label = { Text("دائن (Credit)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("line_credit_input_$index"),
                                singleLine = true
                            )
                        }
                    }
                }
            }
        }
    }

    // Account Picker Modal Dialog
    if (showAccountPickerForIndex != null) {
        val targetIdx = showAccountPickerForIndex!!
        var searchInPicker by remember { mutableStateOf("") }
        val filtered = accounts.filter {
            it.accNo.contains(searchInPicker, ignoreCase = true) || it.name.contains(searchInPicker, ignoreCase = true)
        }

        AlertDialog(
            onDismissRequest = { showAccountPickerForIndex = null },
            title = { Text("اختر الحساب المحاسبي", fontWeight = FontWeight.Bold, color = GoldAccent) },
            text = {
                Column(modifier = Modifier.height(360.dp)) {
                    OutlinedTextField(
                        value = searchInPicker,
                        onValueChange = { searchInPicker = it },
                        label = { Text("بحث برقم أو اسم الحساب...") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(filtered) { acc ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        lines[targetIdx] = lines[targetIdx].copy(accountId = acc.id)
                                        showAccountPickerForIndex = null
                                    },
                                shape = RoundedCornerShape(8.dp),
                                colors = CardDefaults.cardColors(containerColor = NavyDark)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(acc.accNo, fontWeight = FontWeight.Bold, color = GoldAccent, fontSize = 13.sp)
                                        Text(acc.name, color = TextPrimary, fontSize = 13.sp)
                                    }
                                    AccountTypeBadge(type = acc.type)
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showAccountPickerForIndex = null }) {
                    Text("إلغاء")
                }
            },
            containerColor = NavySurface
        )
    }
}
