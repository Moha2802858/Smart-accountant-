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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.smartaccounting.app.data.model.AccountEntity
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
fun LedgerScreen(viewModel: AccountingViewModel) {
    val accounts by viewModel.accounts.collectAsState()
    val report by viewModel.ledgerReport.collectAsState()

    var selectedAccount by remember { mutableStateOf<AccountEntity?>(null) }
    var fromDate by remember { mutableStateOf("") }
    var toDate by remember { mutableStateOf("") }
    var showAccountPicker by remember { mutableStateOf(false) }

    LaunchedEffect(accounts) {
        if (selectedAccount == null && accounts.isNotEmpty()) {
            selectedAccount = accounts.first()
            viewModel.loadLedgerReport(accounts.first().accNo, fromDate, toDate, "")
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .padding(horizontal = 16.dp)
    ) {
        SectionHeader(
            title = "📖 دفتر الأستاذ العام (كشف حساب)",
            subtitle = "كشف تفصيلي بحركات الحساب والرصيد التراكمي"
        )

        // Account Selector Box
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { showAccountPicker = true },
            color = NavySurface,
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("الحساب المحاسبي:", fontSize = 11.sp, color = TextSecondary)
                    Text(
                        text = if (selectedAccount != null) "${selectedAccount?.accNo} — ${selectedAccount?.name}" else "اضغط لاختيار حساب...",
                        fontWeight = FontWeight.Bold,
                        color = GoldAccent,
                        fontSize = 15.sp
                    )
                }
                OutlinedButton(
                    onClick = { showAccountPicker = true },
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("تغيير الحساب", color = GoldAccent, fontSize = 12.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Date Range Filters
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedTextField(
                value = fromDate,
                onValueChange = {
                    fromDate = it
                    selectedAccount?.let { acc -> viewModel.loadLedgerReport(acc.accNo, it, toDate, "") }
                },
                label = { Text("من تاريخ (اختياري)") },
                modifier = Modifier.weight(1f),
                singleLine = true
            )
            OutlinedTextField(
                value = toDate,
                onValueChange = {
                    toDate = it
                    selectedAccount?.let { acc -> viewModel.loadLedgerReport(acc.accNo, fromDate, it, "") }
                },
                label = { Text("إلى تاريخ (اختياري)") },
                modifier = Modifier.weight(1f),
                singleLine = true
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Ledger Summary Cards
        if (report != null) {
            val r = report!!
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatCard(
                    title = "الافتتاحي",
                    value = formatCurrency(r.opening),
                    accentColor = TextSecondary,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "مدين الفترة",
                    value = formatCurrency(r.totalDebit),
                    accentColor = GreenPositive,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "دائن الفترة",
                    value = formatCurrency(r.totalCredit),
                    accentColor = RedNegative,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "الرصيد الختامي",
                    value = formatCurrency(r.finalBalance),
                    accentColor = GoldAccent,
                    modifier = Modifier.weight(1.2f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Movements Table
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 40.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (r.rows.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(40.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("لا توجد حركات مسجلة على هذا الحساب في الفترة المحددة", color = TextMuted)
                        }
                    }
                } else {
                    items(r.rows) { row ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(containerColor = NavySurface)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("حركة #${row.movementNo} • ${row.date}", fontWeight = FontWeight.Bold, color = GoldAccent, fontSize = 13.sp)
                                    Text("المنطقة: ${row.region}", color = TextSecondary, fontSize = 11.sp)
                                }

                                if (row.description.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(row.description, color = TextPrimary, fontSize = 13.sp)
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("مدين: ${formatCurrency(row.debit)}", color = if (row.debit > 0) GreenPositive else TextMuted, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                    Text("دائن: ${formatCurrency(row.credit)}", color = if (row.credit > 0) RedNegative else TextMuted, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                    Text("الرصيد: ${formatCurrency(row.balance)}", color = GoldAccent, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Account Picker Dialog
    if (showAccountPicker) {
        var pickerSearch by remember { mutableStateOf("") }
        val filtered = accounts.filter {
            it.accNo.contains(pickerSearch, ignoreCase = true) || it.name.contains(pickerSearch, ignoreCase = true)
        }

        AlertDialog(
            onDismissRequest = { showAccountPicker = false },
            title = { Text("اختر الحساب لكشف الحساب", fontWeight = FontWeight.Bold, color = GoldAccent) },
            text = {
                Column(modifier = Modifier.height(360.dp)) {
                    OutlinedTextField(
                        value = pickerSearch,
                        onValueChange = { pickerSearch = it },
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
                                        selectedAccount = acc
                                        viewModel.loadLedgerReport(acc.accNo, fromDate, toDate, "")
                                        showAccountPicker = false
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
                TextButton(onClick = { showAccountPicker = false }) {
                    Text("إلغاء")
                }
            },
            containerColor = NavySurface
        )
    }
}
