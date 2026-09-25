package com.smartaccounting.app.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.smartaccounting.app.ui.theme.AmberPending
import com.smartaccounting.app.ui.theme.BackgroundDark
import com.smartaccounting.app.ui.theme.GoldAccent
import com.smartaccounting.app.ui.theme.GreenPositive
import com.smartaccounting.app.ui.theme.NavyCard
import com.smartaccounting.app.ui.theme.NavySurface
import com.smartaccounting.app.ui.theme.RedNegative
import com.smartaccounting.app.ui.theme.TealAccent
import com.smartaccounting.app.ui.theme.TextMuted
import com.smartaccounting.app.ui.theme.TextPrimary
import com.smartaccounting.app.ui.theme.TextSecondary
import com.smartaccounting.app.ui.viewmodel.AccountingViewModel

@Composable
fun IncomeStatementScreen(viewModel: AccountingViewModel) {
    var fromDate by remember { mutableStateOf("") }
    var toDate by remember { mutableStateOf("") }

    LaunchedEffect(fromDate, toDate) {
        viewModel.loadIncomeStatement(fromDate, toDate, "")
    }

    val report by viewModel.incomeReport.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .padding(horizontal = 16.dp)
    ) {
        SectionHeader(
            title = "📈 قائمة الدخل (الأرباح والخسائر)",
            subtitle = "مقارنة إجمالي الإيرادات بالمصروفات وصافي الأرباح/الخسائر"
        )

        // Date Filters
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedTextField(
                value = fromDate,
                onValueChange = { fromDate = it },
                label = { Text("من تاريخ") },
                modifier = Modifier.weight(1f),
                singleLine = true
            )
            OutlinedTextField(
                value = toDate,
                onValueChange = { toDate = it },
                label = { Text("إلى تاريخ") },
                modifier = Modifier.weight(1f),
                singleLine = true
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (report != null) {
            val r = report!!
            // Net Income Banner
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (r.netIncome >= 0) GreenPositive.copy(alpha = 0.15f) else RedNegative.copy(alpha = 0.15f)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = if (r.netIncome >= 0) "صافي الربح" else "صافي الخسارة",
                            color = TextSecondary,
                            fontSize = 12.sp
                        )
                        Text(
                            text = formatCurrency(r.netIncome),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = if (r.netIncome >= 0) GreenPositive else RedNegative
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text("إجمالي الإيرادات: ${formatCurrency(r.revenuesTotal)}", color = GreenPositive, fontSize = 12.sp)
                        Text("إجمالي المصروفات: ${formatCurrency(r.expensesTotal)}", color = RedNegative, fontSize = 12.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 40.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    Text("🟢 الإيرادات", fontWeight = FontWeight.Bold, color = GreenPositive, fontSize = 14.sp)
                }
                items(r.revenues) { item ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(containerColor = NavySurface)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("${item.accNo} — ${item.name}", color = TextPrimary, fontSize = 13.sp)
                            Text(formatCurrency(item.amount), fontWeight = FontWeight.Bold, color = GreenPositive, fontSize = 13.sp)
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("🔴 المصروفات", fontWeight = FontWeight.Bold, color = RedNegative, fontSize = 14.sp)
                }
                items(r.expenses) { item ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(containerColor = NavySurface)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("${item.accNo} — ${item.name}", color = TextPrimary, fontSize = 13.sp)
                            Text(formatCurrency(item.amount), fontWeight = FontWeight.Bold, color = RedNegative, fontSize = 13.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BalanceSheetScreen(viewModel: AccountingViewModel) {
    var toDate by remember { mutableStateOf("") }

    LaunchedEffect(toDate) {
        viewModel.loadBalanceSheet("", toDate, "")
    }

    val report by viewModel.balanceSheetReport.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .padding(horizontal = 16.dp)
    ) {
        SectionHeader(
            title = "🏦 قائمة المركز المالي (الميزانية)",
            subtitle = "الأصول مقابل الخصوم وحقوق الملكية"
        )

        OutlinedTextField(
            value = toDate,
            onValueChange = { toDate = it },
            label = { Text("حتى تاريخ (اختياري)") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(12.dp))

        if (report != null) {
            val r = report!!
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (r.isBalanced) GreenPositive.copy(alpha = 0.15f) else AmberPending.copy(alpha = 0.15f)
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
                        Text("إجمالي الأصول: ${formatCurrency(r.assetsTotal)}", fontWeight = FontWeight.Bold, color = GoldAccent, fontSize = 13.sp)
                        Text("إجمالي الخصوم والملكية: ${formatCurrency(r.totalWithIncome)}", fontWeight = FontWeight.Bold, color = TealAccent, fontSize = 13.sp)
                    }
                    Surface(
                        color = if (r.isBalanced) GreenPositive else RedNegative,
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = if (r.isBalanced) "✔ متزنة" else "✖ فرق: ${formatCurrency(r.difference)}",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 40.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    Text("🏦 الأصول (Assets)", fontWeight = FontWeight.Bold, color = GoldAccent, fontSize = 14.sp)
                }
                items(r.assets) { item ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(containerColor = NavySurface)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("${item.accNo} — ${item.name}", color = TextPrimary, fontSize = 13.sp)
                            Text(formatCurrency(item.amount), fontWeight = FontWeight.Bold, color = GoldAccent, fontSize = 13.sp)
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("🛡️ الخصوم وحقوق الملكية (Liabilities & Equity)", fontWeight = FontWeight.Bold, color = TealAccent, fontSize = 14.sp)
                }
                items(r.liabilitiesEquity) { item ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(containerColor = NavySurface)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("${item.accNo} — ${item.name}", color = TextPrimary, fontSize = 13.sp)
                            Text(formatCurrency(item.amount), fontWeight = FontWeight.Bold, color = TealAccent, fontSize = 13.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CashFlowScreen(viewModel: AccountingViewModel) {
    LaunchedEffect(Unit) {
        viewModel.loadCashFlow("", "", "")
    }

    val report by viewModel.cashFlowReport.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .padding(horizontal = 16.dp)
    ) {
        SectionHeader(
            title = "💵 قائمة التدفقات النقدية",
            subtitle = "حركة المقبوضات والمدفوعات النقدية والبنكية"
        )

        if (report != null) {
            val r = report!!
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatCard(title = "رصيد أول المدة", value = formatCurrency(r.totalOpening), modifier = Modifier.weight(1f))
                StatCard(title = "التدفقات الداخلة", value = formatCurrency(r.totalInflow), accentColor = GreenPositive, modifier = Modifier.weight(1f))
                StatCard(title = "التدفقات الخارجة", value = formatCurrency(r.totalOutflow), accentColor = RedNegative, modifier = Modifier.weight(1f))
                StatCard(title = "رصيد آخر المدة", value = formatCurrency(r.totalClosing), accentColor = GoldAccent, modifier = Modifier.weight(1.2f))
            }

            Spacer(modifier = Modifier.height(12.dp))

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 40.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(r.rows) { row ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = NavySurface)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("${row.accNo} — ${row.name}", fontWeight = FontWeight.Bold, color = GoldAccent, fontSize = 14.sp)
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("أول المدة: ${formatCurrency(row.opening)}", fontSize = 11.sp, color = TextSecondary)
                                Text("داخل (+): ${formatCurrency(row.inflow)}", fontSize = 11.sp, color = GreenPositive)
                                Text("خارج (-): ${formatCurrency(row.outflow)}", fontSize = 11.sp, color = RedNegative)
                                Text("آخر المدة: ${formatCurrency(row.closing)}", fontSize = 11.sp, color = GoldAccent, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BudgetScreen(viewModel: AccountingViewModel) {
    LaunchedEffect(Unit) {
        viewModel.loadBudgetComparison("", "", "", "")
    }

    val report by viewModel.budgetReport.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .padding(horizontal = 16.dp)
    ) {
        SectionHeader(
            title = "📊 الموازنة التقديرية والفعلية",
            subtitle = "مقارنة الخطط التقديرية بالإنفاق الفعلي ونسب الإنجاز"
        )

        if (report != null) {
            val r = report!!
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatCard(title = "إجمالي الموازنة", value = formatCurrency(r.totalBudget), modifier = Modifier.weight(1f))
                StatCard(title = "الإنفاق الفعلي", value = formatCurrency(r.totalActual), accentColor = TealAccent, modifier = Modifier.weight(1f))
                StatCard(title = "نسبة الصرف", value = "${r.totalPct}%", accentColor = GoldAccent, modifier = Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(12.dp))

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 40.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(r.entries) { item ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = NavySurface)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("${item.accNo} — ${item.name}", fontWeight = FontWeight.Bold, color = GoldAccent, fontSize = 14.sp)
                                Surface(
                                    color = when (item.status) {
                                        "ضمن الخطة" -> GreenPositive.copy(alpha = 0.2f)
                                        "تجاوز الحد" -> RedNegative.copy(alpha = 0.2f)
                                        else -> NavyCard
                                    },
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        text = item.status,
                                        fontSize = 11.sp,
                                        color = when (item.status) {
                                            "ضمن الخطة" -> GreenPositive
                                            "تجاوز الحد" -> RedNegative
                                            else -> TextSecondary
                                        },
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("المقدر: ${formatCurrency(item.budget)}", fontSize = 12.sp, color = TextSecondary)
                                Text("الفعلي: ${formatCurrency(item.actual)}", fontSize = 12.sp, color = TealAccent, fontWeight = FontWeight.SemiBold)
                                Text("الفرق: ${formatCurrency(item.variance)}", fontSize = 12.sp, color = if (item.variance >= 0) GreenPositive else RedNegative)
                                Text("النسبة: ${item.pct}%", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = GoldAccent)
                            }
                        }
                    }
                }
            }
        }
    }
}
