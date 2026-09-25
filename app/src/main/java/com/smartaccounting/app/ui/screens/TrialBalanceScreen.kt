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
import androidx.compose.foundation.layout.width
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
import com.smartaccounting.app.ui.theme.BackgroundDark
import com.smartaccounting.app.ui.theme.GoldAccent
import com.smartaccounting.app.ui.theme.GreenPositive
import com.smartaccounting.app.ui.theme.NavyDark
import com.smartaccounting.app.ui.theme.NavySurface
import com.smartaccounting.app.ui.theme.RedNegative
import com.smartaccounting.app.ui.theme.TextMuted
import com.smartaccounting.app.ui.theme.TextPrimary
import com.smartaccounting.app.ui.theme.TextSecondary
import com.smartaccounting.app.ui.viewmodel.AccountingViewModel

@Composable
fun TrialBalanceScreen(viewModel: AccountingViewModel) {
    var fromDate by remember { mutableStateOf("") }
    var toDate by remember { mutableStateOf("") }

    LaunchedEffect(fromDate, toDate) {
        viewModel.loadTrialBalance(fromDate, toDate, "")
    }

    val report by viewModel.trialBalanceReport.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .padding(horizontal = 16.dp)
    ) {
        SectionHeader(
            title = "⚖️ ميزان المراجعة بالمجاميع والأرصدة",
            subtitle = "التحقق من توازن الحسابات للأرصدة الافتتاحية وحركات الفترة والأرصدة الختامية"
        )

        // Date Filters
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedTextField(
                value = fromDate,
                onValueChange = { fromDate = it },
                label = { Text("من تاريخ (اختياري)") },
                modifier = Modifier.weight(1f),
                singleLine = true
            )
            OutlinedTextField(
                value = toDate,
                onValueChange = { toDate = it },
                label = { Text("إلى تاريخ (اختياري)") },
                modifier = Modifier.weight(1f),
                singleLine = true
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (report != null) {
            val r = report!!
            // Balanced Status Banner
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (r.isBalanced) GreenPositive.copy(alpha = 0.15f) else RedNegative.copy(alpha = 0.15f)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (r.isBalanced) "✔ ميزان المراجعة متوازن (مدين = دائن)" else "✖ تنبيه: ميزان المراجعة غير متوازن",
                        color = if (r.isBalanced) GreenPositive else RedNegative,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )

                    Text(
                        text = "إجمالي حركة الفترة: ${formatCurrency(r.totalPDebit)}",
                        color = TextPrimary,
                        fontSize = 12.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Trial Balance Rows
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
                            Text("لا توجد حركات في الفترة المحددة", color = TextMuted)
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
                                    Text("${row.accNo} — ${row.name}", fontWeight = FontWeight.Bold, color = GoldAccent, fontSize = 14.sp)
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text("الافتتاحي", fontSize = 10.sp, color = TextMuted)
                                        Text("مد: ${formatCurrency(row.oDebit)} | دائن: ${formatCurrency(row.oCredit)}", fontSize = 11.sp, color = TextSecondary)
                                    }
                                    Column {
                                        Text("حركة الفترة", fontSize = 10.sp, color = TextMuted)
                                        Text("مد: ${formatCurrency(row.pDebit)} | دائن: ${formatCurrency(row.pCredit)}", fontSize = 11.sp, color = TextSecondary)
                                    }
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text("الرصيد الختامي", fontSize = 10.sp, color = TextMuted)
                                        Text(
                                            text = if (row.fDebit > 0) "مدين ${formatCurrency(row.fDebit)}" else "دائن ${formatCurrency(row.fCredit)}",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (row.fDebit > 0) GreenPositive else RedNegative
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
