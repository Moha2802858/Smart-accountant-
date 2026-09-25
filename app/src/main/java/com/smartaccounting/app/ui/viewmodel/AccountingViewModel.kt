package com.smartaccounting.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.smartaccounting.app.data.model.AccountEntity
import com.smartaccounting.app.data.model.AuditLogEntity
import com.smartaccounting.app.data.model.BalanceSheetReport
import com.smartaccounting.app.data.model.BudgetReport
import com.smartaccounting.app.data.model.CashFlowReport
import com.smartaccounting.app.data.model.DashboardStats
import com.smartaccounting.app.data.model.IncomeStatementReport
import com.smartaccounting.app.data.model.JournalEntryEntity
import com.smartaccounting.app.data.model.JournalEntryWithDetails
import com.smartaccounting.app.data.model.JournalLineDetail
import com.smartaccounting.app.data.model.JournalLineEntity
import com.smartaccounting.app.data.model.LedgerReport
import com.smartaccounting.app.data.model.RegionEntity
import com.smartaccounting.app.data.model.TrialBalanceReport
import com.smartaccounting.app.data.model.UserEntity
import com.smartaccounting.app.data.repository.AccountingRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class Screen {
    LOGIN,
    FORGOT_PASSWORD,
    DASHBOARD,
    ACCOUNTS,
    ACCOUNTS_TREE,
    JOURNAL,
    JOURNAL_EDITOR,
    LEDGER,
    TRIAL_BALANCE,
    BUDGET,
    INCOME_STATEMENT,
    BALANCE_SHEET,
    CASH_FLOW,
    USERS,
    SETTINGS,
    AUDIT,
    HELP
}

class AccountingViewModel(private val repository: AccountingRepository) : ViewModel() {

    // Auth state
    private val _currentUser = MutableStateFlow<UserEntity?>(null)
    val currentUser: StateFlow<UserEntity?> = _currentUser.asStateFlow()

    private val _currentScreen = MutableStateFlow(Screen.LOGIN)
    val currentScreen: StateFlow<Screen> = _currentScreen.asStateFlow()

    private val _screenHistory = mutableListOf<Screen>()

    // Toast/Message state
    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage: StateFlow<String?> = _toastMessage.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    // Data Flows
    val accounts: StateFlow<List<AccountEntity>> = repository.getAllAccountsFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val journalEntries: StateFlow<List<JournalEntryEntity>> = repository.getAllEntriesFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allUsers: StateFlow<List<UserEntity>> = repository.getAllUsersFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val regions: StateFlow<List<RegionEntity>> = repository.getAllRegionsFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val auditLogs: StateFlow<List<AuditLogEntity>> = repository.getAuditLogsFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Dashboard State
    private val _dashboardStats = MutableStateFlow<DashboardStats?>(null)
    val dashboardStats: StateFlow<DashboardStats?> = _dashboardStats.asStateFlow()

    // Active Journal Entry for Editing
    private val _editingEntry = MutableStateFlow<JournalEntryWithDetails?>(null)
    val editingEntry: StateFlow<JournalEntryWithDetails?> = _editingEntry.asStateFlow()

    // Reports State
    private val _ledgerReport = MutableStateFlow<LedgerReport?>(null)
    val ledgerReport: StateFlow<LedgerReport?> = _ledgerReport.asStateFlow()

    private val _trialBalanceReport = MutableStateFlow<TrialBalanceReport?>(null)
    val trialBalanceReport: StateFlow<TrialBalanceReport?> = _trialBalanceReport.asStateFlow()

    private val _incomeReport = MutableStateFlow<IncomeStatementReport?>(null)
    val incomeReport: StateFlow<IncomeStatementReport?> = _incomeReport.asStateFlow()

    private val _balanceSheetReport = MutableStateFlow<BalanceSheetReport?>(null)
    val balanceSheetReport: StateFlow<BalanceSheetReport?> = _balanceSheetReport.asStateFlow()

    private val _cashFlowReport = MutableStateFlow<CashFlowReport?>(null)
    val cashFlowReport: StateFlow<CashFlowReport?> = _cashFlowReport.asStateFlow()

    private val _budgetReport = MutableStateFlow<BudgetReport?>(null)
    val budgetReport: StateFlow<BudgetReport?> = _budgetReport.asStateFlow()

    // Settings
    private val _companyName = MutableStateFlow("مؤسسة الأعمال الذكية التجارية")
    val companyName: StateFlow<String> = _companyName.asStateFlow()

    private val _periodFrom = MutableStateFlow("2026-01-01")
    val periodFrom: StateFlow<String> = _periodFrom.asStateFlow()

    private val _periodTo = MutableStateFlow("2026-12-31")
    val periodTo: StateFlow<String> = _periodTo.asStateFlow()

    init {
        loadSettings()
    }

    fun showToast(msg: String) {
        _toastMessage.value = msg
    }

    fun clearToast() {
        _toastMessage.value = null
    }

    fun clearError() {
        _errorMessage.value = null
    }

    fun navigateTo(screen: Screen) {
        if (_currentScreen.value != screen) {
            _screenHistory.add(_currentScreen.value)
            _currentScreen.value = screen
        }
    }

    fun navigateBack(): Boolean {
        if (_screenHistory.isNotEmpty()) {
            _currentScreen.value = _screenHistory.removeAt(_screenHistory.lastIndex)
            return true
        }
        return false
    }

    // --- Authentication ---
    fun login(username: String, pass: String) {
        viewModelScope.launch {
            val res = repository.login(username, pass)
            res.onSuccess { user ->
                _currentUser.value = user
                _currentScreen.value = Screen.DASHBOARD
                _screenHistory.clear()
                refreshDashboard()
            }.onFailure { err ->
                _errorMessage.value = err.message
            }
        }
    }

    fun logout() {
        _currentUser.value?.let { user ->
            viewModelScope.launch { repository.audit(user.fullName, "تسجيل خروج") }
        }
        _currentUser.value = null
        _screenHistory.clear()
        _currentScreen.value = Screen.LOGIN
    }

    fun resetPassword(username: String, ans: String, newPass: String, onDone: () -> Unit) {
        viewModelScope.launch {
            val res = repository.resetPassword(username, ans, newPass)
            res.onSuccess {
                showToast("تم تغيير كلمة المرور بنجاح — يمكنك الآن تسجيل الدخول")
                onDone()
            }.onFailure {
                _errorMessage.value = it.message
            }
        }
    }

    // --- Dashboard ---
    fun refreshDashboard() {
        viewModelScope.launch {
            _dashboardStats.value = repository.getDashboardStats()
        }
    }

    // --- Accounts CRUD ---
    fun saveAccount(acc: AccountEntity, onDone: () -> Unit) {
        val user = _currentUser.value?.fullName ?: "النظام"
        viewModelScope.launch {
            val res = repository.saveAccount(acc, user)
            res.onSuccess {
                showToast("تم حفظ الحساب بنجاح")
                refreshDashboard()
                onDone()
            }.onFailure {
                _errorMessage.value = it.message
            }
        }
    }

    fun deleteAccount(id: Long) {
        val user = _currentUser.value?.fullName ?: "النظام"
        viewModelScope.launch {
            val res = repository.deleteAccount(id, user)
            res.onSuccess {
                showToast("تم حذف الحساب بنجاح")
                refreshDashboard()
            }.onFailure {
                _errorMessage.value = it.message
            }
        }
    }

    fun wipeAccountsAndEntries() {
        val user = _currentUser.value?.fullName ?: "النظام"
        viewModelScope.launch {
            val res = repository.wipeAllAccountsAndEntries(user)
            res.onSuccess {
                showToast("تم مسح جميع الحسابات والقيود")
                refreshDashboard()
            }.onFailure {
                _errorMessage.value = it.message
            }
        }
    }

    // --- Journal Entries CRUD ---
    fun openNewJournalEntry() {
        viewModelScope.launch {
            val nextNo = repository.getNextMovementNo()
            val activeRegions = repository.getActiveRegions()
            val defRegion = activeRegions.firstOrNull() ?: "المركز الرئيسي"
            val today = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US).format(java.util.Date())

            _editingEntry.value = JournalEntryWithDetails(
                entry = JournalEntryEntity(
                    movementNo = nextNo,
                    entryDate = today,
                    region = defRegion,
                    status = "draft",
                    entryType = "عادي"
                ),
                lines = listOf(
                    JournalLineDetail(0, 0, "", "", 0.0, 0.0),
                    JournalLineDetail(0, 0, "", "", 0.0, 0.0)
                ),
                totalDebit = 0.0,
                totalCredit = 0.0
            )
            navigateTo(Screen.JOURNAL_EDITOR)
        }
    }

    fun openEditJournalEntry(entryId: Long) {
        viewModelScope.launch {
            val full = repository.getEntryWithDetails(entryId)
            if (full != null) {
                if (full.entry.status == "posted") {
                    showToast("تنبيه: القيد مرحل — قم بإلغاء الترحيل أولاً لتعديله")
                }
                _editingEntry.value = full
                navigateTo(Screen.JOURNAL_EDITOR)
            }
        }
    }

    fun saveEditingEntry(entry: JournalEntryEntity, lines: List<JournalLineEntity>, onDone: () -> Unit) {
        val user = _currentUser.value?.fullName ?: "النظام"
        viewModelScope.launch {
            val res = repository.saveJournalEntry(entry, lines, user)
            res.onSuccess {
                showToast("تم حفظ القيد بنجاح")
                refreshDashboard()
                onDone()
            }.onFailure {
                _errorMessage.value = it.message
            }
        }
    }

    fun deleteJournalEntry(entryId: Long) {
        val user = _currentUser.value?.fullName ?: "النظام"
        viewModelScope.launch {
            val res = repository.deleteJournalEntry(entryId, user)
            res.onSuccess {
                showToast("تم حذف القيد")
                refreshDashboard()
            }.onFailure {
                _errorMessage.value = it.message
            }
        }
    }

    fun postEntry(entryId: Long) {
        val user = _currentUser.value?.fullName ?: "النظام"
        viewModelScope.launch {
            val res = repository.postJournalEntry(entryId, user)
            res.onSuccess {
                showToast("تم ترحيل القيد بنجاح")
                refreshDashboard()
            }.onFailure {
                _errorMessage.value = it.message
            }
        }
    }

    fun unpostEntry(entryId: Long) {
        val user = _currentUser.value?.fullName ?: "النظام"
        viewModelScope.launch {
            val res = repository.unpostJournalEntry(entryId, user)
            res.onSuccess {
                showToast("تم إلغاء ترحيل القيد وتحويله لمسودة")
                refreshDashboard()
            }.onFailure {
                _errorMessage.value = it.message
            }
        }
    }

    // --- Reports Execution ---
    fun loadLedgerReport(accNo: String, fromDate: String, toDate: String, region: String) {
        viewModelScope.launch {
            val res = repository.getLedgerReport(accNo, fromDate, toDate, region)
            res.onSuccess {
                _ledgerReport.value = it
            }.onFailure {
                _errorMessage.value = it.message
            }
        }
    }

    fun loadTrialBalance(fromDate: String, toDate: String, region: String) {
        viewModelScope.launch {
            _trialBalanceReport.value = repository.getTrialBalanceReport(fromDate, toDate, region)
        }
    }

    fun loadIncomeStatement(fromDate: String, toDate: String, region: String) {
        viewModelScope.launch {
            _incomeReport.value = repository.getIncomeStatementReport(fromDate, toDate, region)
        }
    }

    fun loadBalanceSheet(fromDate: String, toDate: String, region: String) {
        viewModelScope.launch {
            _balanceSheetReport.value = repository.getBalanceSheetReport(fromDate, toDate, region)
        }
    }

    fun loadCashFlow(fromDate: String, toDate: String, region: String) {
        viewModelScope.launch {
            _cashFlowReport.value = repository.getCashFlowReport(fromDate, toDate, region)
        }
    }

    fun loadBudgetComparison(fromDate: String, toDate: String, region: String, type: String) {
        viewModelScope.launch {
            _budgetReport.value = repository.getBudgetComparisonReport(fromDate, toDate, region, type)
        }
    }

    // --- Users & Settings ---
    fun saveUser(user: UserEntity, rawPass: String, onDone: () -> Unit) {
        val adminName = _currentUser.value?.fullName ?: "المدير"
        viewModelScope.launch {
            val res = repository.saveUser(user, rawPass, adminName)
            res.onSuccess {
                showToast("تم حفظ المستخدم بنجاح")
                onDone()
            }.onFailure {
                _errorMessage.value = it.message
            }
        }
    }

    fun deleteUser(id: Long) {
        val adminId = _currentUser.value?.id ?: 0L
        val adminName = _currentUser.value?.fullName ?: "المدير"
        viewModelScope.launch {
            val res = repository.deleteUser(id, adminId, adminName)
            res.onSuccess {
                showToast("تم حذف المستخدم")
            }.onFailure {
                _errorMessage.value = it.message
            }
        }
    }

    fun saveRegion(region: RegionEntity, onDone: () -> Unit) {
        val user = _currentUser.value?.fullName ?: "النظام"
        viewModelScope.launch {
            val res = repository.saveRegion(region, user)
            res.onSuccess {
                showToast("تم حفظ المنطقة")
                onDone()
            }.onFailure {
                _errorMessage.value = it.message
            }
        }
    }

    fun toggleRegion(id: Long) {
        val user = _currentUser.value?.fullName ?: "النظام"
        viewModelScope.launch {
            repository.toggleRegionActive(id, user)
        }
    }

    private fun loadSettings() {
        viewModelScope.launch {
            _companyName.value = repository.getSetting("company_name", "مؤسسة الأعمال الذكية التجارية")
            _periodFrom.value = repository.getSetting("period_from", "2026-01-01")
            _periodTo.value = repository.getSetting("period_to", "2026-12-31")
        }
    }

    fun updateSettings(name: String, from: String, to: String) {
        val user = _currentUser.value?.fullName ?: "النظام"
        viewModelScope.launch {
            repository.setSetting("company_name", name, user)
            repository.setSetting("period_from", from, user)
            repository.setSetting("period_to", to, user)
            _companyName.value = name
            _periodFrom.value = from
            _periodTo.value = to
            showToast("تم حفظ الإعدادات بنجاح")
        }
    }
}

class AccountingViewModelFactory(private val repository: AccountingRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AccountingViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AccountingViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
