package com.smartaccounting.app.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "users",
    indices = [Index(value = ["username"], unique = true)]
)
data class UserEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val username: String,
    val passwordHash: String,
    val fullName: String,
    val role: String = "accountant", // admin, accountant, viewer
    val securityQuestion: String = "",
    val securityAnswerHash: String = "",
    val createdAt: String = ""
)

@Entity(
    tableName = "accounts",
    indices = [Index(value = ["accNo"], unique = true)]
)
data class AccountEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val accNo: String,
    val name: String,
    val type: String = "", // أصول, خصوم, حقوق ملكية, إيرادات, مصروفات, أخرى
    val openingBalance: Double = 0.0,
    val budget: Double = 0.0,
    val createdAt: String = ""
)

@Entity(
    tableName = "journal_entries",
    indices = [Index(value = ["movementNo"], unique = true)]
)
data class JournalEntryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val movementNo: String,
    val entryDate: String,
    val description: String = "",
    val region: String = "المركز الرئيسي",
    val status: String = "draft", // draft, posted
    val entryType: String = "عادي", // عادي, تسوية, افتتاحي, إقفال
    val createdBy: Long = 1,
    val createdAt: String = ""
)

@Entity(
    tableName = "journal_lines",
    foreignKeys = [
        ForeignKey(
            entity = JournalEntryEntity::class,
            parentColumns = ["id"],
            childColumns = ["entryId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = AccountEntity::class,
            parentColumns = ["id"],
            childColumns = ["accountId"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [Index(value = ["entryId"]), Index(value = ["accountId"])]
)
data class JournalLineEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val entryId: Long,
    val accountId: Long,
    val debit: Double = 0.0,
    val credit: Double = 0.0
)

@Entity(tableName = "settings")
data class SettingEntity(
    @PrimaryKey val key: String,
    val value: String
)

@Entity(tableName = "audit_log")
data class AuditLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val user: String,
    val action: String,
    val details: String = "",
    val at: String = ""
)

@Entity(tableName = "regions")
data class RegionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val active: Int = 1
)

// UI and Business models
data class JournalLineDetail(
    val id: Long = 0,
    val accountId: Long,
    val accNo: String,
    val accountName: String,
    val debit: Double,
    val credit: Double
)

data class JournalEntryWithDetails(
    val entry: JournalEntryEntity,
    val lines: List<JournalLineDetail>,
    val totalDebit: Double,
    val totalCredit: Double
)

data class LedgerRow(
    val date: String,
    val movementNo: String,
    val description: String,
    val region: String,
    val debit: Double,
    val credit: Double,
    val balance: Double
)

data class LedgerReport(
    val account: AccountEntity,
    val opening: Double,
    val rows: List<LedgerRow>,
    val totalDebit: Double,
    val totalCredit: Double,
    val finalBalance: Double
)

data class TrialBalanceRow(
    val accNo: String,
    val name: String,
    val oDebit: Double,
    val oCredit: Double,
    val pDebit: Double,
    val pCredit: Double,
    val fDebit: Double,
    val fCredit: Double
)

data class TrialBalanceReport(
    val rows: List<TrialBalanceRow>,
    val totalODebit: Double,
    val totalOCredit: Double,
    val totalPDebit: Double,
    val totalPCredit: Double,
    val totalFDebit: Double,
    val totalFCredit: Double,
    val isBalanced: Boolean
)

data class FinancialAccountItem(
    val accNo: String,
    val name: String,
    val amount: Double
)

data class IncomeStatementReport(
    val revenues: List<FinancialAccountItem>,
    val revenuesTotal: Double,
    val expenses: List<FinancialAccountItem>,
    val expensesTotal: Double,
    val netIncome: Double
)

data class BalanceSheetReport(
    val assets: List<FinancialAccountItem>,
    val assetsTotal: Double,
    val liabilitiesEquity: List<FinancialAccountItem>,
    val liabEquityTotal: Double,
    val netCumulative: Double,
    val netPeriod: Double,
    val totalWithIncome: Double,
    val difference: Double,
    val isBalanced: Boolean
)

data class CashFlowRow(
    val accNo: String,
    val name: String,
    val opening: Double,
    val inflow: Double,
    val outflow: Double,
    val closing: Double
)

data class CashFlowReport(
    val rows: List<CashFlowRow>,
    val totalOpening: Double,
    val totalInflow: Double,
    val totalOutflow: Double,
    val totalClosing: Double,
    val netFlow: Double
)

data class BudgetRow(
    val id: Long,
    val accNo: String,
    val name: String,
    val type: String,
    val budget: Double,
    val actual: Double,
    val variance: Double,
    val pct: Double,
    val status: String
)

data class BudgetReport(
    val entries: List<BudgetRow>,
    val totalBudget: Double,
    val totalActual: Double,
    val totalVariance: Double,
    val totalPct: Double
)

data class DashboardStats(
    val accountsCount: Int,
    val entriesCount: Int,
    val draftsCount: Int,
    val totalDebit: Double,
    val totalCredit: Double,
    val recentEntries: List<JournalEntryWithDetails>
)
