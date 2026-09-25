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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountTree
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.smartaccounting.app.data.model.AccountEntity
import com.smartaccounting.app.ui.theme.BackgroundDark
import com.smartaccounting.app.ui.theme.GoldAccent
import com.smartaccounting.app.ui.theme.NavyCard
import com.smartaccounting.app.ui.theme.NavyDark
import com.smartaccounting.app.ui.theme.NavySurface
import com.smartaccounting.app.ui.theme.RedNegative
import com.smartaccounting.app.ui.theme.TealAccent
import com.smartaccounting.app.ui.theme.TextMuted
import com.smartaccounting.app.ui.theme.TextPrimary
import com.smartaccounting.app.ui.theme.TextSecondary
import com.smartaccounting.app.ui.viewmodel.AccountingViewModel
import com.smartaccounting.app.ui.viewmodel.Screen

val ACCOUNT_TYPES = listOf("أصول", "خصوم", "حقوق ملكية", "إيرادات", "مصروفات", "أخرى")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountsScreen(viewModel: AccountingViewModel) {
    val accounts by viewModel.accounts.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedTypeFilter by remember { mutableStateOf("الكل") }

    var showEditDialog by remember { mutableStateOf(false) }
    var editingAccount by remember { mutableStateOf<AccountEntity?>(null) }
    var showDeleteConfirm by remember { mutableStateOf<AccountEntity?>(null) }
    var isTreeView by remember { mutableStateOf(false) }

    val filteredAccounts = accounts.filter { acc ->
        val matchesQuery = acc.accNo.contains(searchQuery, ignoreCase = true) ||
                acc.name.contains(searchQuery, ignoreCase = true)
        val matchesType = selectedTypeFilter == "الكل" || acc.type == selectedTypeFilter
        matchesQuery && matchesType
    }

    Scaffold(
        floatingActionButton = {
            if (currentUser?.role in listOf("admin", "accountant")) {
                FloatingActionButton(
                    onClick = {
                        editingAccount = null
                        showEditDialog = true
                    },
                    containerColor = GoldAccent,
                    contentColor = NavyDark,
                    modifier = Modifier.testTag("add_account_fab")
                ) {
                    Icon(Icons.Default.Add, contentDescription = "إضافة حساب")
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
            // Header & View Mode Switch
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isTreeView) "🌳 شجرة الحسابات" else "📚 دليل الحسابات",
                    style = MaterialTheme.typography.titleLarge,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                )

                Row {
                    OutlinedButton(
                        onClick = { isTreeView = !isTreeView },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("toggle_tree_view_btn")
                    ) {
                        Icon(
                            Icons.Default.AccountTree,
                            contentDescription = null,
                            tint = GoldAccent,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (isTreeView) "عرض كقائمة" else "عرض كشجرة", color = GoldAccent, fontSize = 12.sp)
                    }
                }
            }

            if (!isTreeView) {
                // Search Field
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = { Text("بحث برقم أو اسم الحساب...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = GoldAccent) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("search_account_input"),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GoldAccent,
                        focusedLabelColor = GoldAccent,
                        unfocusedBorderColor = NavyCard
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Type Filter Chips
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    item {
                        FilterChip(
                            selected = selectedTypeFilter == "الكل",
                            onClick = { selectedTypeFilter = "الكل" },
                            label = { Text("الكل") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = GoldAccent,
                                selectedLabelColor = NavyDark
                            )
                        )
                    }
                    items(ACCOUNT_TYPES) { type ->
                        FilterChip(
                            selected = selectedTypeFilter == type,
                            onClick = { selectedTypeFilter = type },
                            label = { Text(type) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = GoldAccent,
                                selectedLabelColor = NavyDark
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Accounts List
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 80.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (filteredAccounts.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(40.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("لا توجد حسابات مطابقة", color = TextMuted)
                            }
                        }
                    } else {
                        items(filteredAccounts, key = { it.id }) { acc ->
                            AccountCard(
                                account = acc,
                                canEdit = currentUser?.role in listOf("admin", "accountant"),
                                onEdit = {
                                    editingAccount = acc
                                    showEditDialog = true
                                },
                                onDelete = { showDeleteConfirm = acc }
                            )
                        }
                    }
                }
            } else {
                // Accounts Tree View
                AccountsTreeViewer(accounts = accounts)
            }
        }
    }

    // Add / Edit Account Dialog
    if (showEditDialog) {
        AccountEditDialog(
            account = editingAccount,
            onDismiss = { showEditDialog = false },
            onSave = { acc ->
                viewModel.saveAccount(acc) {
                    showEditDialog = false
                }
            }
        )
    }

    // Delete Confirmation
    if (showDeleteConfirm != null) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = null },
            title = { Text("حذف الحساب", fontWeight = FontWeight.Bold) },
            text = {
                Text("هل أنت متأكد من حذف الحساب «${showDeleteConfirm?.accNo} - ${showDeleteConfirm?.name}»؟ لا يمكن التراجع.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteConfirm?.let { viewModel.deleteAccount(it.id) }
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
fun AccountCard(
    account: AccountEntity,
    canEdit: Boolean,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = NavySurface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = account.accNo,
                        fontWeight = FontWeight.Bold,
                        color = GoldAccent,
                        fontSize = 15.sp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    AccountTypeBadge(type = account.type)
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = account.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text(
                        text = "الافتتاحي: ${formatCurrency(account.openingBalance)}",
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                    if (account.budget > 0) {
                        Text(
                            text = "الموازنة: ${formatCurrency(account.budget)}",
                            color = TealAccent,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            if (canEdit) {
                Row {
                    IconButton(onClick = onEdit) {
                        Icon(Icons.Default.Edit, contentDescription = "تعديل", tint = TextSecondary)
                    }
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Default.Delete, contentDescription = "حذف", tint = RedNegative)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountEditDialog(
    account: AccountEntity?,
    onDismiss: () -> Unit,
    onSave: (AccountEntity) -> Unit
) {
    var accNo by remember { mutableStateOf(account?.accNo ?: "") }
    var name by remember { mutableStateOf(account?.name ?: "") }
    var type by remember { mutableStateOf(account?.type ?: "أصول") }
    var openingBalance by remember { mutableStateOf(account?.openingBalance?.toString() ?: "0") }
    var budget by remember { mutableStateOf(account?.budget?.toString() ?: "0") }

    var typeDropdownExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (account == null) "إضافة حساب جديد" else "تعديل الحساب",
                fontWeight = FontWeight.Bold,
                color = GoldAccent
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = accNo,
                    onValueChange = { accNo = it },
                    label = { Text("رقم الحساب (مثال: 1105)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("اسم الحساب (مثال: بنك الراجحي)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                ExposedDropdownMenuBox(
                    expanded = typeDropdownExpanded,
                    onExpandedChange = { typeDropdownExpanded = it }
                ) {
                    OutlinedTextField(
                        value = type,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("نوع الحساب") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = typeDropdownExpanded) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = typeDropdownExpanded,
                        onDismissRequest = { typeDropdownExpanded = false }
                    ) {
                        ACCOUNT_TYPES.forEach { item ->
                            DropdownMenuItem(
                                text = { Text(item) },
                                onClick = {
                                    type = item
                                    typeDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = openingBalance,
                    onValueChange = { openingBalance = it },
                    label = { Text("الرصيد الافتتاحي") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = budget,
                    onValueChange = { budget = it },
                    label = { Text("الموازنة السنوية التقديرية") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val ob = openingBalance.toDoubleOrNull() ?: 0.0
                    val bg = budget.toDoubleOrNull() ?: 0.0
                    onSave(
                        account?.copy(
                            accNo = accNo.trim(),
                            name = name.trim(),
                            type = type,
                            openingBalance = ob,
                            budget = bg
                        ) ?: AccountEntity(
                            accNo = accNo.trim(),
                            name = name.trim(),
                            type = type,
                            openingBalance = ob,
                            budget = bg
                        )
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = GoldAccent, contentColor = NavyDark)
            ) {
                Text("حفظ الحساب", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("إلغاء")
            }
        },
        containerColor = NavySurface
    )
}

@Composable
fun AccountsTreeViewer(accounts: List<AccountEntity>) {
    val subGroupLabels = mapOf(
        "أصول" to mapOf("11" to "أصول متداولة", "12" to "حسابات مدينة", "13" to "مخزون", "14" to "أصول ثابتة", "15" to "أخرى"),
        "خصوم" to mapOf("21" to "خصوم متداولة", "22" to "خصوم طويلة الأجل", "25" to "أخرى"),
        "حقوق ملكية" to mapOf("31" to "رأس المال والأرباح", "39" to "أخرى"),
        "إيرادات" to mapOf("41" to "إيرادات التشغيل", "42" to "إيرادات أخرى", "49" to "أخرى"),
        "مصروفات" to mapOf("51" to "رواتب وأجور", "52" to "خدمات", "53" to "مصاريف إدارية", "54" to "سفر ومهمات", "55" to "صرفيات أخرى", "59" to "أخرى")
    )

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(ACCOUNT_TYPES) { type ->
            val typeAccounts = accounts.filter { it.type == type }
            if (typeAccounts.isNotEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = NavySurface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "📁 $type",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = GoldAccent
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "(${typeAccounts.size} حساب)",
                                color = TextSecondary,
                                fontSize = 12.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        val grouped = typeAccounts.groupBy { it.accNo.take(2) }
                        grouped.forEach { (prefix, accList) ->
                            val groupName = subGroupLabels[type]?.get(prefix) ?: "مجموعة حسابات ($prefix)"
                            Text(
                                text = "  📂 $prefix - $groupName",
                                fontWeight = FontWeight.SemiBold,
                                color = TealAccent,
                                fontSize = 13.sp,
                                modifier = Modifier.padding(vertical = 4.dp)
                            )

                            accList.forEach { acc ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(start = 24.dp, top = 2.dp, bottom = 2.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "• ${acc.accNo} - ${acc.name}",
                                        color = TextPrimary,
                                        fontSize = 13.sp
                                    )
                                    Text(
                                        text = formatCurrency(acc.openingBalance),
                                        color = TextSecondary,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                        }
                    }
                }
            }
        }
    }
}
