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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Scale
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import com.smartaccounting.app.ui.viewmodel.Screen

@Composable
fun DashboardScreen(viewModel: AccountingViewModel) {
    LaunchedEffect(Unit) {
        viewModel.refreshDashboard()
    }

    val stats by viewModel.dashboardStats.collectAsState()
    val companyName by viewModel.companyName.collectAsState()
    val periodFrom by viewModel.periodFrom.collectAsState()
    val periodTo by viewModel.periodTo.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(4.dp))
            // Quick Actions Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = { viewModel.openNewJournalEntry() },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("quick_new_entry_btn"),
                    colors = ButtonDefaults.buttonColors(containerColor = GoldAccent, contentColor = NavyDark),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("قيد جديد", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }

                Button(
                    onClick = { viewModel.navigateTo(Screen.LEDGER) },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("quick_ledger_btn"),
                    colors = ButtonDefaults.buttonColors(containerColor = TealAccent, contentColor = Color.White),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.MenuBook, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("كشف حساب", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }

                Button(
                    onClick = { viewModel.navigateTo(Screen.TRIAL_BALANCE) },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("quick_trial_btn"),
                    colors = ButtonDefaults.buttonColors(containerColor = NavyCard, contentColor = TextPrimary),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.Scale, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("ميزان المراجعة", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
        }

        // Stats Grid
        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatCard(
                        title = "دليل الحسابات",
                        value = "${stats?.accountsCount ?: 0} حساب",
                        icon = Icons.Default.AccountBalance,
                        accentColor = GoldAccent,
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = "إجمالي القيود",
                        value = "${stats?.entriesCount ?: 0} قيد",
                        icon = Icons.Default.Receipt,
                        accentColor = TealAccent,
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatCard(
                        title = "المدين المرحل",
                        value = formatCurrency(stats?.totalDebit ?: 0.0),
                        accentColor = GreenPositive,
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = "الدائن المرحل",
                        value = formatCurrency(stats?.totalCredit ?: 0.0),
                        accentColor = RedNegative,
                        modifier = Modifier.weight(1f)
                    )
                }

                StatCard(
                    title = "قيود غير مرحلة (مسودة)",
                    value = "${stats?.draftsCount ?: 0} قيد",
                    accentColor = AmberPending,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        // Company & Period Info Panel
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = NavySurface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "ℹ️ معلومات النظام والفترة",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = GoldAccent
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "🏢 المؤسسة: $companyName",
                        color = TextPrimary,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "📅 فترة التقارير المالية: من $periodFrom إلى $periodTo",
                        color = TextSecondary,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "💡 تنبيه: القيود في حالة «مسودة» لا تؤثر على الأستاذ العام والقوائم المالية حتى ترحيلها.",
                        color = AmberPending,
                        style = MaterialTheme.typography.bodyMedium.copy(fontSize = 12.sp)
                    )
                }
            }
        }

        // Recent Entries Section
        item {
            SectionHeader(
                title = "🕒 أحدث القيود المسجلة",
                actionButton = {
                    OutlinedButton(
                        onClick = { viewModel.navigateTo(Screen.JOURNAL) },
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("عرض الكل", color = GoldAccent, fontSize = 12.sp)
                    }
                }
            )
        }

        val recent = stats?.recentEntries ?: emptyList()
        if (recent.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = NavySurface)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "لا توجد قيود مسجلة بعد. اضغط «قيد جديد» للبدء.",
                            color = TextMuted,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        } else {
            items(recent) { item ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.openEditJournalEntry(item.entry.id) },
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
                                    text = "حركة #${item.entry.movementNo}",
                                    fontWeight = FontWeight.Bold,
                                    color = GoldAccent,
                                    fontSize = 15.sp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                StatusBadge(status = item.entry.status)
                            }
                            Text(
                                text = item.entry.entryDate,
                                color = TextSecondary,
                                fontSize = 12.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = item.entry.description.ifBlank { "بدون بيان" },
                            color = TextPrimary,
                            style = MaterialTheme.typography.bodyMedium,
                            maxLines = 1
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "المنطقة: ${item.entry.region}",
                                color = TextMuted,
                                fontSize = 12.sp
                            )
                            Text(
                                text = "الإجمالي: ${formatCurrency(item.totalDebit)}",
                                fontWeight = FontWeight.Bold,
                                color = GreenPositive,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }
        }
    }
}
