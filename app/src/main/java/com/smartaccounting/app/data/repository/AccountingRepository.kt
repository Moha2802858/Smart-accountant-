package com.smartaccounting.app.data.repository

import com.smartaccounting.app.data.dao.AccountingDao
import com.smartaccounting.app.data.db.AppDatabase
import com.smartaccounting.app.data.model.AccountEntity
import com.smartaccounting.app.data.model.AuditLogEntity
import com.smartaccounting.app.data.model.BalanceSheetReport
import com.smartaccounting.app.data.model.BudgetReport
import com.smartaccounting.app.data.model.BudgetRow
import com.smartaccounting.app.data.model.CashFlowReport
import com.smartaccounting.app.data.model.CashFlowRow
import com.smartaccounting.app.data.model.DashboardStats
import com.smartaccounting.app.data.model.FinancialAccountItem
import com.smartaccounting.app.data.model.IncomeStatementReport
import com.smartaccounting.app.data.model.JournalEntryEntity
import com.smartaccounting.app.data.model.JournalEntryWithDetails
import com.smartaccounting.app.data.model.JournalLineDetail
import com.smartaccounting.app.data.model.JournalLineEntity
import com.smartaccounting.app.data.model.LedgerReport
import com.smartaccounting.app.data.model.LedgerRow
import com.smartaccounting.app.data.model.RegionEntity
import com.smartaccounting.app.data.model.SettingEntity
import com.smartaccounting.app.data.model.TrialBalanceReport
import com.smartaccounting.app.data.model.TrialBalanceRow
import com.smartaccounting.app.data.model.UserEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.abs
import kotlin.math.round

class AccountingRepository(private val dao: AccountingDao) {

    private fun currentTimeString(): String {
        return SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date())
    }

    suspend fun audit(userName: String, action: String, details: String = "") {
        withContext(Dispatchers.IO) {
            dao.insertAuditLog(
                AuditLogEntity(
                    user = userName,
                    action = action,
                    details = details,
                    at = currentTimeString()
                )
            )
        }
    }

    // --- Authentication ---
    suspend fun login(username: String, password: String):Result<UserEntity> = withContext(Dispatchers.IO) {
        val user = dao.getUserByUsername(username.trim())
            ?: return@withContext Result.failure(Exception("اسم المستخدم أو كلمة المرور غير صحيحة"))

        val hashed = AppDatabase.hashPassword(password)
        if (user.passwordHash == hashed) {
            audit(user.fullName, "تسجيل دخول")
            Result.success(user)
        } else {
            Result.failure(Exception("اسم المستخدم أو كلمة المرور غير صحيحة"))
        }
    }

    suspend fun getSecurityQuestion(username: String): Result<String> = withContext(Dispatchers.IO) {
        val user = dao.getUserByUsername(username.trim())
            ?: return@withContext Result.failure(Exception("لا يوجد مستخدم بهذا الاسم"))
        if (user.securityQuestion.isBlank()) {
            return@withContext Result.failure(Exception("هذا الحساب لا يوجد له سؤال أمان — تواصل مع مدير النظام"))
        }
        Result.success(user.securityQuestion)
    }

    suspend fun resetPassword(username: String, answer: String, newPassword: String): Result<Unit> = withContext(Dispatchers.IO) {
        val user = dao.getUserByUsername(username.trim())
            ?: return@withContext Result.failure(Exception("لا يوجد مستخدم بهذا الاسم"))

        if (newPassword.length < 8) {
            return@withContext Result.failure(Exception("كلمة المرور الجديدة 8 أحرف على الأقل"))
        }

        val ansHash = AppDatabase.hashPassword(answer.trim())
        if (user.securityAnswerHash != ansHash) {
            return@withContext Result.failure(Exception("إجابة سؤال الأمان غير صحيحة"))
        }

        val newPassHash = AppDatabase.hashPassword(newPassword)
        dao.updateUser(user.copy(passwordHash = newPassHash))
        audit(user.fullName, "استعادة كلمة مرور", "تم تغيير كلمة المرور عبر سؤال الأمان")
        Result.success(Unit)
    }

    // --- Accounts ---
    fun getAllAccountsFlow(): Flow<List<AccountEntity>> = dao.getAllAccountsFlow()

    suspend fun getAllAccounts(): List<AccountEntity> = withContext(Dispatchers.IO) {
        dao.getAllAccounts()
    }

    suspend fun saveAccount(account: AccountEntity, currentUser: String): Result<Long> = withContext(Dispatchers.IO) {
        val accNo = account.accNo.trim()
        val name = account.name.trim()
        if (accNo.isBlank() || name.isBlank()) {
            return@withContext Result.failure(Exception("رقم الحساب واسم الحساب مطلوبان"))
        }

        val existing = dao.getAccountByNo(accNo)
        if (account.id == 0L) {
            if (existing != null) {
                return@withContext Result.failure(Exception("رقم الحساب $accNo موجود بالفعل"))
            }
            val id = dao.insertAccount(account.copy(accNo = accNo, name = name, createdAt = currentTimeString()))
            audit(currentUser, "إضافة حساب", "$accNo - $name")
            Result.success(id)
        } else {
            if (existing != null && existing.id != account.id) {
                return@withContext Result.failure(Exception("رقم الحساب $accNo مستخدم في حساب آخر"))
            }
            dao.updateAccount(account.copy(accNo = accNo, name = name))
            audit(currentUser, "تعديل حساب", "$accNo - $name")
            Result.success(account.id)
        }
    }

    suspend fun deleteAccount(id: Long, currentUser: String): Result<Unit> = withContext(Dispatchers.IO) {
        val used = dao.getFirstLineForAccount(id)
        if (used != null) {
            return@withContext Result.failure(Exception("لا يمكن حذف الحساب لوجود حركات عليه في دفتر اليومية"))
        }
        val acc = dao.getAccountById(id) ?: return@withContext Result.failure(Exception("الحساب غير موجود"))
        dao.deleteAccount(id)
        audit(currentUser, "حذف حساب", "${acc.accNo} - ${acc.name}")
        Result.success(Unit)
    }

    suspend fun wipeAllAccountsAndEntries(currentUser: String): Result<Pair<Int, Int>> = withContext(Dispatchers.IO) {
        val nAcc = dao.countAccounts()
        val nEnt = dao.countEntries()
        dao.deleteAllEntries()
        dao.deleteAllAccounts()
        audit(currentUser, "مسح الشجرة", "حذف نهائي: $nAcc حساب و $nEnt قيد")
        Result.success(Pair(nAcc, nEnt))
    }

    // --- Journal Entries ---
    fun getAllEntriesFlow(): Flow<List<JournalEntryEntity>> = dao.getAllEntriesFlow()

    suspend fun getNextMovementNo(): String = withContext(Dispatchers.IO) {
        val entries = dao.getAllEntries()
        var maxNo = 0
        for (e in entries) {
            val num = e.movementNo.replace(",", "").toIntOrNull()
            if (num != null && num > maxNo) {
                maxNo = num
            }
        }
        (maxNo + 1).toString()
    }

    suspend fun getEntryWithDetails(entryId: Long): JournalEntryWithDetails? = withContext(Dispatchers.IO) {
        val entry = dao.getEntryById(entryId) ?: return@withContext null
        val lines = dao.getLinesForEntry(entryId)
        val allAccounts = dao.getAllAccounts().associateBy { it.id }

        val lineDetails = lines.map { line ->
            val acc = allAccounts[line.accountId]
            JournalLineDetail(
                id = line.id,
                accountId = line.accountId,
                accNo = acc?.accNo ?: "",
                accountName = acc?.name ?: "",
                debit = line.debit,
                credit = line.credit
            )
        }

        val totalDebit = round(lineDetails.sumOf { it.debit } * 100) / 100
        val totalCredit = round(lineDetails.sumOf { it.credit } * 100) / 100

        JournalEntryWithDetails(entry, lineDetails, totalDebit, totalCredit)
    }

    suspend fun saveJournalEntry(
        entry: JournalEntryEntity,
        lines: List<JournalLineEntity>,
        currentUser: String
    ): Result<Long> = withContext(Dispatchers.IO) {
        if (entry.entryDate.isBlank()) return@withContext Result.failure(Exception("التاريخ مطلوب"))
        if (entry.movementNo.isBlank()) return@withContext Result.failure(Exception("رقم الحركة مطلوب"))

        val cleanLines = lines.filter { it.debit > 0 || it.credit > 0 }
        if (cleanLines.size < 2) {
            return@withContext Result.failure(Exception("القيد يحتاج سطرين على الأقل (مدين ودائن)"))
        }

        val totalDebit = round(cleanLines.sumOf { it.debit } * 100) / 100
        val totalCredit = round(cleanLines.sumOf { it.credit } * 100) / 100

        if (abs(totalDebit - totalCredit) > 0.004) {
            return@withContext Result.failure(Exception("القيد غير متوازن: مدين $totalDebit ≠ دائن $totalCredit"))
        }

        if (totalDebit <= 0) {
            return@withContext Result.failure(Exception("إجمالي القيد يجب أن يكون أكبر من صفر"))
        }

        val existing = dao.getEntryByMovementNo(entry.movementNo)
        val entryId: Long
        if (entry.id == 0L) {
            if (existing != null) {
                return@withContext Result.failure(Exception("رقم الحركة ${entry.movementNo} مستخدم بالفعل"))
            }
            entryId = dao.insertEntry(entry.copy(createdAt = currentTimeString()))
            cleanLines.forEach { dao.insertLine(it.copy(entryId = entryId)) }
            val action = if (entry.status == "posted") "إنشاء قيد ومرحلته" else "إنشاء قيد (مسودة)"
            audit(currentUser, action, "رقم الحركة ${entry.movementNo}")
        } else {
            if (existing != null && existing.id != entry.id) {
                return@withContext Result.failure(Exception("رقم الحركة ${entry.movementNo} مستخدم في قيد آخر"))
            }
            dao.updateEntry(entry)
            dao.deleteLinesForEntry(entry.id)
            cleanLines.forEach { dao.insertLine(it.copy(entryId = entry.id)) }
            entryId = entry.id
            audit(currentUser, "تعديل قيد", "رقم الحركة ${entry.movementNo}")
        }

        Result.success(entryId)
    }

    suspend fun deleteJournalEntry(id: Long, currentUser: String): Result<Unit> = withContext(Dispatchers.IO) {
        val entry = dao.getEntryById(id) ?: return@withContext Result.failure(Exception("القيد غير موجود"))
        dao.deleteLinesForEntry(id)
        dao.deleteEntry(id)
        audit(currentUser, "حذف قيد", "رقم الحركة ${entry.movementNo}")
        Result.success(Unit)
    }

    suspend fun postJournalEntry(id: Long, currentUser: String): Result<Unit> = withContext(Dispatchers.IO) {
        val entry = dao.getEntryById(id) ?: return@withContext Result.failure(Exception("القيد غير موجود"))
        val lines = dao.getLinesForEntry(id)
        val totalDebit = round(lines.sumOf { it.debit } * 100) / 100
        val totalCredit = round(lines.sumOf { it.credit } * 100) / 100

        if (abs(totalDebit - totalCredit) > 0.004) {
            return@withContext Result.failure(Exception("لا يمكن ترحيل قيد غير متوازن"))
        }

        dao.updateEntry(entry.copy(status = "posted"))
        audit(currentUser, "ترحيل قيد", "رقم الحركة ${entry.movementNo}")
        Result.success(Unit)
    }

    suspend fun unpostJournalEntry(id: Long, currentUser: String): Result<Unit> = withContext(Dispatchers.IO) {
        val entry = dao.getEntryById(id) ?: return@withContext Result.failure(Exception("القيد غير موجود"))
        dao.updateEntry(entry.copy(status = "draft"))
        audit(currentUser, "إلغاء ترحيل قيد", "رقم الحركة ${entry.movementNo}")
        Result.success(Unit)
    }

    // --- Financial Reports ---
    suspend fun getLedgerReport(
        accNo: String,
        fromDate: String = "",
        toDate: String = "",
        region: String = ""
    ): Result<LedgerReport> = withContext(Dispatchers.IO) {
        val account = dao.getAccountByNo(accNo)
            ?: return@withContext Result.failure(Exception("رقم الحساب غير موجود"))

        val allEntries = dao.getAllEntries().filter { it.status == "posted" }
        val allLines = dao.getAllJournalLines().filter { it.accountId == account.id }
        val entryMap = allEntries.associateBy { it.id }

        // Opening balance calculation
        var openingNet = account.openingBalance
        for (line in allLines) {
            val entry = entryMap[line.entryId] ?: continue
            if (region.isNotBlank() && entry.region != region) continue
            if (fromDate.isNotBlank() && entry.entryDate < fromDate) {
                openingNet += (line.debit - line.credit)
            }
        }

        // Period rows
        val periodRows = mutableListOf<LedgerRow>()
        var runningBalance = round(openingNet * 100) / 100
        var totalDebit = 0.0
        var totalCredit = 0.0

        val sortedLines = allLines.mapNotNull { line ->
            val entry = entryMap[line.entryId] ?: return@mapNotNull null
            if (region.isNotBlank() && entry.region != region) return@mapNotNull null
            if (fromDate.isNotBlank() && entry.entryDate < fromDate) return@mapNotNull null
            if (toDate.isNotBlank() && entry.entryDate > toDate) return@mapNotNull null
            Triple(entry, line, entry.entryDate)
        }.sortedWith(compareBy({ it.third }, { it.first.id }, { it.second.id }))

        for ((entry, line) in sortedLines) {
            val d = round(line.debit * 100) / 100
            val c = round(line.credit * 100) / 100
            totalDebit += d
            totalCredit += c
            runningBalance = round((runningBalance + d - c) * 100) / 100
            periodRows.add(
                LedgerRow(
                    date = entry.entryDate,
                    movementNo = entry.movementNo,
                    description = entry.description,
                    region = entry.region,
                    debit = d,
                    credit = c,
                    balance = runningBalance
                )
            )
        }

        Result.success(
            LedgerReport(
                account = account,
                opening = round(openingNet * 100) / 100,
                rows = periodRows,
                totalDebit = round(totalDebit * 100) / 100,
                totalCredit = round(totalCredit * 100) / 100,
                finalBalance = round(runningBalance * 100) / 100
            )
        )
    }

    suspend fun getTrialBalanceReport(
        fromDate: String = "",
        toDate: String = "",
        region: String = ""
    ): TrialBalanceReport = withContext(Dispatchers.IO) {
        val accounts = dao.getAllAccounts()
        val allEntries = dao.getAllEntries().filter { it.status == "posted" }
        val allLines = dao.getAllJournalLines()
        val entryMap = allEntries.associateBy { it.id }

        val rows = mutableListOf<TrialBalanceRow>()
        var totODebit = 0.0
        var totOCredit = 0.0
        var totPDebit = 0.0
        var totPCredit = 0.0
        var totFDebit = 0.0
        var totFCredit = 0.0

        for (acc in accounts) {
            val linesForAcc = allLines.filter { it.accountId == acc.id }
            var pre = 0.0
            var pd = 0.0
            var pc = 0.0

            for (l in linesForAcc) {
                val entry = entryMap[l.entryId] ?: continue
                if (region.isNotBlank() && entry.region != region) continue

                if (fromDate.isNotBlank() && entry.entryDate < fromDate) {
                    pre += (l.debit - l.credit)
                } else {
                    var inPeriod = true
                    if (fromDate.isNotBlank() && entry.entryDate < fromDate) inPeriod = false
                    if (toDate.isNotBlank() && entry.entryDate > toDate) inPeriod = false
                    if (inPeriod) {
                        pd += l.debit
                        pc += l.credit
                    }
                }
            }

            val netOpen = round((acc.openingBalance + pre) * 100) / 100
            val od = if (netOpen > 0) netOpen else 0.0
            val oc = if (netOpen < 0) -netOpen else 0.0

            pd = round(pd * 100) / 100
            pc = round(pc * 100) / 100

            val netFinal = round((netOpen + pd - pc) * 100) / 100
            val fd = if (netFinal > 0) netFinal else 0.0
            val fc = if (netFinal < 0) -netFinal else 0.0

            if (od == 0.0 && oc == 0.0 && pd == 0.0 && pc == 0.0) {
                continue
            }

            rows.add(
                TrialBalanceRow(
                    accNo = acc.accNo,
                    name = acc.name,
                    oDebit = od,
                    oCredit = oc,
                    pDebit = pd,
                    pCredit = pc,
                    fDebit = fd,
                    fCredit = fc
                )
            )

            totODebit += od
            totOCredit += oc
            totPDebit += pd
            totPCredit += pc
            totFDebit += fd
            totFCredit += fc
        }

        TrialBalanceReport(
            rows = rows,
            totalODebit = round(totODebit * 100) / 100,
            totalOCredit = round(totOCredit * 100) / 100,
            totalPDebit = round(totPDebit * 100) / 100,
            totalPCredit = round(totPCredit * 100) / 100,
            totalFDebit = round(totFDebit * 100) / 100,
            totalFCredit = round(totFCredit * 100) / 100,
            isBalanced = abs(totPDebit - totPCredit) <= 0.01
        )
    }

    suspend fun getIncomeStatementReport(
        fromDate: String = "",
        toDate: String = "",
        region: String = ""
    ): IncomeStatementReport = withContext(Dispatchers.IO) {
        val accounts = dao.getAllAccounts().filter { it.type in listOf("إيرادات", "مصروفات") }
        val allEntries = dao.getAllEntries().filter { it.status == "posted" }
        val allLines = dao.getAllJournalLines()
        val entryMap = allEntries.associateBy { it.id }

        val revenues = mutableListOf<FinancialAccountItem>()
        val expenses = mutableListOf<FinancialAccountItem>()
        var totR = 0.0
        var totE = 0.0

        for (a in accounts) {
            val linesForAcc = allLines.filter { it.accountId == a.id }
            var d = 0.0
            var c = 0.0

            for (l in linesForAcc) {
                val entry = entryMap[l.entryId] ?: continue
                if (region.isNotBlank() && entry.region != region) continue
                if (fromDate.isNotBlank() && entry.entryDate < fromDate) continue
                if (toDate.isNotBlank() && entry.entryDate > toDate) continue
                d += l.debit
                c += l.credit
            }

            if (a.type == "إيرادات") {
                val amt = round((c - d) * 100) / 100
                revenues.add(FinancialAccountItem(a.accNo, a.name, amt))
                totR += amt
            } else {
                val amt = round((d - c) * 100) / 100
                expenses.add(FinancialAccountItem(a.accNo, a.name, amt))
                totE += amt
            }
        }

        totR = round(totR * 100) / 100
        totE = round(totE * 100) / 100
        val net = round((totR - totE) * 100) / 100

        IncomeStatementReport(revenues, totR, expenses, totE, net)
    }

    suspend fun getBalanceSheetReport(
        fromDate: String = "",
        toDate: String = "",
        region: String = ""
    ): BalanceSheetReport = withContext(Dispatchers.IO) {
        val accounts = dao.getAllAccounts()
        val allEntries = dao.getAllEntries().filter { it.status == "posted" }
        val allLines = dao.getAllJournalLines()
        val entryMap = allEntries.associateBy { it.id }

        val assets = mutableListOf<FinancialAccountItem>()
        val liabEq = mutableListOf<FinancialAccountItem>()
        var totA = 0.0
        var totL = 0.0
        var netCum = 0.0

        for (a in accounts) {
            var mv = 0.0
            val linesForAcc = allLines.filter { it.accountId == a.id }
            for (l in linesForAcc) {
                val entry = entryMap[l.entryId] ?: continue
                if (region.isNotBlank() && entry.region != region) continue
                if (toDate.isNotBlank() && entry.entryDate > toDate) continue
                mv += (l.debit - l.credit)
            }

            val amount = round((a.openingBalance + mv) * 100) / 100
            when (a.type) {
                "أصول" -> {
                    assets.add(FinancialAccountItem(a.accNo, a.name, amount))
                    totA += amount
                }
                "خصوم", "حقوق ملكية" -> {
                    val reversed = round(-amount * 100) / 100
                    liabEq.add(FinancialAccountItem(a.accNo, a.name, reversed))
                    totL += reversed
                }
                "إيرادات" -> {
                    netCum += -amount
                }
                "مصروفات" -> {
                    netCum -= amount
                }
            }
        }

        netCum = round(netCum * 100) / 100
        val incomeRep = getIncomeStatementReport(fromDate, toDate, region)
        val totalWithIncome = round((totL + netCum) * 100) / 100
        val diff = round((totA - totalWithIncome) * 100) / 100

        BalanceSheetReport(
            assets = assets,
            assetsTotal = round(totA * 100) / 100,
            liabilitiesEquity = liabEq,
            liabEquityTotal = round(totL * 100) / 100,
            netCumulative = netCum,
            netPeriod = incomeRep.netIncome,
            totalWithIncome = totalWithIncome,
            difference = diff,
            isBalanced = abs(diff) <= 0.02
        )
    }

    suspend fun getCashFlowReport(
        fromDate: String = "",
        toDate: String = "",
        region: String = ""
    ): CashFlowReport = withContext(Dispatchers.IO) {
        val cashAccounts = dao.getAllAccounts().filter {
            it.type == "أصول" && (it.name.contains("صندوق") || it.name.contains("بنك") || it.name.contains("نقد"))
        }

        val allEntries = dao.getAllEntries().filter { it.status == "posted" }
        val allLines = dao.getAllJournalLines()
        val entryMap = allEntries.associateBy { it.id }

        val rows = mutableListOf<CashFlowRow>()
        var totOpen = 0.0
        var totIn = 0.0
        var totOut = 0.0
        var totClose = 0.0

        for (a in cashAccounts) {
            var opening = a.openingBalance
            var inflow = 0.0
            var outflow = 0.0

            val linesForAcc = allLines.filter { it.accountId == a.id }
            for (l in linesForAcc) {
                val entry = entryMap[l.entryId] ?: continue
                if (region.isNotBlank() && entry.region != region) continue

                if (fromDate.isNotBlank() && entry.entryDate < fromDate) {
                    opening += (l.debit - l.credit)
                } else {
                    var inPeriod = true
                    if (fromDate.isNotBlank() && entry.entryDate < fromDate) inPeriod = false
                    if (toDate.isNotBlank() && entry.entryDate > toDate) inPeriod = false
                    if (inPeriod) {
                        if (l.debit > 0) inflow += l.debit
                        if (l.credit > 0) outflow += l.credit
                    }
                }
            }

            opening = round(opening * 100) / 100
            inflow = round(inflow * 100) / 100
            outflow = round(outflow * 100) / 100
            val closing = round((opening + inflow - outflow) * 100) / 100

            rows.add(
                CashFlowRow(
                    accNo = a.accNo,
                    name = a.name,
                    opening = opening,
                    inflow = inflow,
                    outflow = outflow,
                    closing = closing
                )
            )

            totOpen += opening
            totIn += inflow
            totOut += outflow
            totClose += closing
        }

        CashFlowReport(
            rows = rows,
            totalOpening = round(totOpen * 100) / 100,
            totalInflow = round(totIn * 100) / 100,
            totalOutflow = round(totOut * 100) / 100,
            totalClosing = round(totClose * 100) / 100,
            netFlow = round((totIn - totOut) * 100) / 100
        )
    }

    suspend fun getBudgetComparisonReport(
        fromDate: String = "",
        toDate: String = "",
        region: String = "",
        type: String = ""
    ): BudgetReport = withContext(Dispatchers.IO) {
        val accounts = dao.getAllAccounts().filter { type.isBlank() || it.type == type }
        val allEntries = dao.getAllEntries().filter { it.status == "posted" }
        val allLines = dao.getAllJournalLines()
        val entryMap = allEntries.associateBy { it.id }

        val rows = mutableListOf<BudgetRow>()
        var totBudget = 0.0
        var totActual = 0.0

        for (a in accounts) {
            val linesForAcc = allLines.filter { it.accountId == a.id }
            var actDebit = 0.0
            var actCredit = 0.0

            for (l in linesForAcc) {
                val entry = entryMap[l.entryId] ?: continue
                if (region.isNotBlank() && entry.region != region) continue
                if (fromDate.isNotBlank() && entry.entryDate < fromDate) continue
                if (toDate.isNotBlank() && entry.entryDate > toDate) continue
                actDebit += l.debit
                actCredit += l.credit
            }

            var actual = when (a.type) {
                "مصروفات" -> actDebit
                "إيرادات" -> actCredit
                else -> actDebit - actCredit
            }
            actual = round(actual * 100) / 100
            val budget = a.budget
            val variance = round((budget - actual) * 100) / 100
            val pct = if (budget > 0) round((actual / budget * 100) * 10) / 10 else 0.0

            val status = when {
                budget == 0.0 -> "بدون ميزانية"
                pct <= 70.0 -> "أقل من المتوقع"
                pct <= 90.0 -> "ضمن الخطة"
                pct <= 100.0 -> "قريب من الحد"
                pct <= 110.0 -> "يزيد قليلاً"
                else -> "تجاوز الحد"
            }

            totBudget += budget
            totActual += actual

            rows.add(
                BudgetRow(
                    id = a.id,
                    accNo = a.accNo,
                    name = a.name,
                    type = a.type,
                    budget = budget,
                    actual = actual,
                    variance = variance,
                    pct = pct,
                    status = status
                )
            )
        }

        totBudget = round(totBudget * 100) / 100
        totActual = round(totActual * 100) / 100
        val totVar = round((totBudget - totActual) * 100) / 100
        val totPct = if (totBudget > 0) round((totActual / totBudget * 100) * 10) / 10 else 0.0

        BudgetReport(
            entries = rows,
            totalBudget = totBudget,
            totalActual = totActual,
            totalVariance = totVar,
            totalPct = totPct
        )
    }

    suspend fun getDashboardStats(): DashboardStats = withContext(Dispatchers.IO) {
        val accCount = dao.countAccounts()
        val entCount = dao.countEntries()
        val draftCount = dao.countDraftEntries()

        val allEntries = dao.getAllEntries()
        val postedIds = allEntries.filter { it.status == "posted" }.map { it.id }.toSet()
        val postedLines = dao.getAllJournalLines().filter { postedIds.contains(it.entryId) }

        val totalDebit = round(postedLines.sumOf { it.debit } * 100) / 100
        val totalCredit = round(postedLines.sumOf { it.credit } * 100) / 100

        val recentEntries = allEntries.take(8).mapNotNull { getEntryWithDetails(it.id) }

        DashboardStats(
            accountsCount = accCount,
            entriesCount = entCount,
            draftsCount = draftCount,
            totalDebit = totalDebit,
            totalCredit = totalCredit,
            recentEntries = recentEntries
        )
    }

    // --- Users ---
    fun getAllUsersFlow(): Flow<List<UserEntity>> = dao.getAllUsersFlow()

    suspend fun saveUser(user: UserEntity, rawPassword: String, currentUser: String): Result<Long> = withContext(Dispatchers.IO) {
        val uName = user.username.trim().lowercase()
        val fName = user.fullName.trim()
        if (uName.isBlank() || fName.isBlank()) {
            return@withContext Result.failure(Exception("اسم المستخدم والاسم الكامل مطلوبان"))
        }

        val existing = dao.getUserByUsername(uName)
        if (user.id == 0L) {
            if (rawPassword.length < 8) {
                return@withContext Result.failure(Exception("كلمة المرور 8 أحرف على الأقل"))
            }
            if (existing != null) {
                return@withContext Result.failure(Exception("اسم المستخدم موجود بالفعل"))
            }
            val hashed = AppDatabase.hashPassword(rawPassword)
            val secHash = if (user.securityAnswerHash.isNotBlank()) AppDatabase.hashPassword(user.securityAnswerHash.trim()) else ""
            val id = dao.insertUser(
                user.copy(
                    username = uName,
                    fullName = fName,
                    passwordHash = hashed,
                    securityAnswerHash = secHash,
                    createdAt = currentTimeString()
                )
            )
            audit(currentUser, "إضافة مستخدم", "$uName (${user.role})")
            Result.success(id)
        } else {
            if (existing != null && existing.id != user.id) {
                return@withContext Result.failure(Exception("اسم المستخدم موجود بالفعل"))
            }
            val current = dao.getUserById(user.id) ?: return@withContext Result.failure(Exception("المستخدم غير موجود"))
            val updatedHash = if (rawPassword.isNotBlank()) {
                if (rawPassword.length < 8) return@withContext Result.failure(Exception("كلمة المرور 8 أحرف على الأقل"))
                AppDatabase.hashPassword(rawPassword)
            } else {
                current.passwordHash
            }
            dao.updateUser(user.copy(username = uName, fullName = fName, passwordHash = updatedHash))
            audit(currentUser, "تعديل مستخدم", uName)
            Result.success(user.id)
        }
    }

    suspend fun deleteUser(id: Long, currentUserId: Long, currentUser: String): Result<Unit> = withContext(Dispatchers.IO) {
        if (id == currentUserId) {
            return@withContext Result.failure(Exception("لا يمكنك حذف نفسك"))
        }
        val target = dao.getUserById(id) ?: return@withContext Result.failure(Exception("المستخدم غير موجود"))
        if (target.role == "admin" && dao.countAdminUsers() <= 1) {
            return@withContext Result.failure(Exception("لا يمكن حذف آخر مدير نظام"))
        }
        dao.deleteUser(id)
        audit(currentUser, "حذف مستخدم", target.username)
        Result.success(Unit)
    }

    // --- Regions ---
    fun getAllRegionsFlow(): Flow<List<RegionEntity>> = dao.getAllRegionsFlow()

    suspend fun getActiveRegions(): List<String> = withContext(Dispatchers.IO) {
        dao.getActiveRegionNames()
    }

    suspend fun saveRegion(region: RegionEntity, currentUser: String): Result<Long> = withContext(Dispatchers.IO) {
        val name = region.name.trim()
        if (name.isBlank()) return@withContext Result.failure(Exception("اسم المنطقة مطلوب"))
        val all = dao.getAllRegions()
        if (region.id == 0L) {
            if (all.any { it.name == name }) return@withContext Result.failure(Exception("المنطقة موجودة بالفعل"))
            val id = dao.insertRegion(region.copy(name = name))
            audit(currentUser, "إضافة منطقة", name)
            Result.success(id)
        } else {
            if (all.any { it.id != region.id && it.name == name }) return@withContext Result.failure(Exception("الاسم مستخدم بالفعل"))
            dao.updateRegion(region.copy(name = name))
            audit(currentUser, "تعديل منطقة", name)
            Result.success(region.id)
        }
    }

    suspend fun toggleRegionActive(id: Long, currentUser: String): Result<Unit> = withContext(Dispatchers.IO) {
        val r = dao.getAllRegions().find { it.id == id } ?: return@withContext Result.failure(Exception("المنطقة غير موجودة"))
        val newActive = if (r.active == 1) 0 else 1
        dao.updateRegion(r.copy(active = newActive))
        val act = if (newActive == 1) "تفعيل منطقة" else "تعطيل منطقة"
        audit(currentUser, act, r.name)
        Result.success(Unit)
    }

    // --- Settings & Audit ---
    suspend fun getSetting(key: String, defVal: String = ""): String = withContext(Dispatchers.IO) {
        dao.getSetting(key) ?: defVal
    }

    suspend fun setSetting(key: String, value: String, currentUser: String) = withContext(Dispatchers.IO) {
        dao.setSetting(SettingEntity(key, value))
        audit(currentUser, "تعديل إعداد", "$key: $value")
    }

    fun getAuditLogsFlow(): Flow<List<AuditLogEntity>> = dao.getAuditLogsFlow()
}
