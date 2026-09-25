package com.smartaccounting.app

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AccountTree
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Help
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Scale
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.smartaccounting.app.ui.screens.AccountsScreen
import com.smartaccounting.app.ui.screens.AuditScreen
import com.smartaccounting.app.ui.screens.BalanceSheetScreen
import com.smartaccounting.app.ui.screens.BudgetScreen
import com.smartaccounting.app.ui.screens.CashFlowScreen
import com.smartaccounting.app.ui.screens.DashboardScreen
import com.smartaccounting.app.ui.screens.ForgotPasswordScreen
import com.smartaccounting.app.ui.screens.HelpScreen
import com.smartaccounting.app.ui.screens.IncomeStatementScreen
import com.smartaccounting.app.ui.screens.JournalEditorScreen
import com.smartaccounting.app.ui.screens.JournalScreen
import com.smartaccounting.app.ui.screens.LedgerScreen
import com.smartaccounting.app.ui.screens.LoginScreen
import com.smartaccounting.app.ui.screens.SettingsScreen
import com.smartaccounting.app.ui.screens.TrialBalanceScreen
import com.smartaccounting.app.ui.screens.UsersScreen
import com.smartaccounting.app.ui.theme.BackgroundDark
import com.smartaccounting.app.ui.theme.GoldAccent
import com.smartaccounting.app.ui.theme.NavyCard
import com.smartaccounting.app.ui.theme.NavyDark
import com.smartaccounting.app.ui.theme.NavySurface
import com.smartaccounting.app.ui.theme.RedNegative
import com.smartaccounting.app.ui.theme.SmartAccountingTheme
import com.smartaccounting.app.ui.theme.TealAccent
import com.smartaccounting.app.ui.theme.TextMuted
import com.smartaccounting.app.ui.theme.TextPrimary
import com.smartaccounting.app.ui.theme.TextSecondary
import com.smartaccounting.app.ui.viewmodel.AccountingViewModel
import com.smartaccounting.app.ui.viewmodel.AccountingViewModelFactory
import com.smartaccounting.app.ui.viewmodel.Screen
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val viewModel: AccountingViewModel by viewModels {
        AccountingViewModelFactory((application as AccountingApplication).repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            SmartAccountingTheme {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    MainApp(viewModel)
                }
            }
        }
    }
}

data class NavItem(
    val screen: Screen,
    val title: String,
    val icon: ImageVector,
    val adminOnly: Boolean = false
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainApp(viewModel: AccountingViewModel) {
    val currentScreen by viewModel.currentScreen.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val companyName by viewModel.companyName.collectAsState()
    val toastMessage by viewModel.toastMessage.collectAsState()

    val context = LocalContext.current
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(toastMessage) {
        toastMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearToast()
        }
    }

    if (currentScreen == Screen.LOGIN) {
        LoginScreen(viewModel)
        return
    }

    if (currentScreen == Screen.FORGOT_PASSWORD) {
        ForgotPasswordScreen(viewModel)
        return
    }

    val navItems = listOf(
        NavItem(Screen.DASHBOARD, "لوحة التحكم", Icons.Default.Home),
        NavItem(Screen.ACCOUNTS, "دليل الحسابات", Icons.Default.AccountBalance),
        NavItem(Screen.JOURNAL, "دفتر اليومية", Icons.Default.Receipt),
        NavItem(Screen.LEDGER, "الأستاذ العام", Icons.Default.MenuBook),
        NavItem(Screen.TRIAL_BALANCE, "ميزان المراجعة", Icons.Default.Scale),
        NavItem(Screen.BUDGET, "الموازنة التقديرية", Icons.Default.Assessment),
        NavItem(Screen.INCOME_STATEMENT, "قائمة الدخل", Icons.Default.TrendingUp),
        NavItem(Screen.BALANCE_SHEET, "المركز المالي", Icons.Default.AccountTree),
        NavItem(Screen.CASH_FLOW, "التدفقات النقدية", Icons.Default.MonetizationOn),
        NavItem(Screen.USERS, "المستخدمون", Icons.Default.People, adminOnly = true),
        NavItem(Screen.AUDIT, "سجل العمليات", Icons.Default.Security, adminOnly = true),
        NavItem(Screen.SETTINGS, "الإعدادات", Icons.Default.Settings),
        NavItem(Screen.HELP, "دليل الاستخدام", Icons.Default.Help)
    )

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = NavyDark,
                drawerContentColor = TextPrimary,
                modifier = Modifier.width(300.dp)
            ) {
                // Drawer Header
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(NavySurface)
                        .padding(20.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .background(GoldAccent, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = currentUser?.fullName?.take(1) ?: "ح",
                                fontWeight = FontWeight.Bold,
                                color = NavyDark,
                                fontSize = 20.sp
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = currentUser?.fullName ?: "المستخدم",
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary,
                                fontSize = 15.sp
                            )
                            Surface(
                                color = GoldAccent.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = when (currentUser?.role) {
                                        "admin" -> "مدير النظام"
                                        "accountant" -> "محاسب"
                                        else -> "مشاهدة فقط"
                                    },
                                    color = GoldAccent,
                                    fontSize = 11.sp,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(companyName, color = TextSecondary, fontSize = 12.sp, maxLines = 1)
                }

                HorizontalDivider(color = NavyCard)

                // Nav Items List
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .padding(12.dp)
                ) {
                    navItems.forEach { item ->
                        if (!item.adminOnly || currentUser?.role == "admin") {
                            val selected = currentScreen == item.screen
                            NavigationDrawerItem(
                                label = { Text(item.title, fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal) },
                                icon = { Icon(item.icon, contentDescription = null, tint = if (selected) NavyDark else GoldAccent) },
                                selected = selected,
                                onClick = {
                                    scope.launch { drawerState.close() }
                                    viewModel.navigateTo(item.screen)
                                },
                                colors = NavigationDrawerItemDefaults.colors(
                                    selectedContainerColor = GoldAccent,
                                    selectedTextColor = NavyDark,
                                    unselectedTextColor = TextPrimary
                                ),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.padding(vertical = 2.dp)
                            )
                        }
                    }

                    HorizontalDivider(color = NavyCard, modifier = Modifier.padding(vertical = 8.dp))

                    NavigationDrawerItem(
                        label = { Text("تسجيل الخروج", color = RedNegative) },
                        icon = { Icon(Icons.Default.ExitToApp, contentDescription = null, tint = RedNegative) },
                        selected = false,
                        onClick = {
                            scope.launch { drawerState.close() }
                            viewModel.logout()
                        },
                        shape = RoundedCornerShape(8.dp)
                    )
                }
            }
        }
    ) {
        Scaffold(
            topBar = {
                if (currentScreen != Screen.JOURNAL_EDITOR) {
                    TopAppBar(
                        title = {
                            Column {
                                Text(
                                    text = when (currentScreen) {
                                        Screen.DASHBOARD -> "لوحة التحكم"
                                        Screen.ACCOUNTS -> "دليل الحسابات"
                                        Screen.ACCOUNTS_TREE -> "شجرة الحسابات"
                                        Screen.JOURNAL -> "دفتر اليومية"
                                        Screen.LEDGER -> "الأستاذ العام"
                                        Screen.TRIAL_BALANCE -> "ميزان المراجعة"
                                        Screen.BUDGET -> "الموازنة التقديرية"
                                        Screen.INCOME_STATEMENT -> "قائمة الدخل"
                                        Screen.BALANCE_SHEET -> "المركز المالي"
                                        Screen.CASH_FLOW -> "التدفقات النقدية"
                                        Screen.USERS -> "المستخدمون"
                                        Screen.SETTINGS -> "الإعدادات"
                                        Screen.AUDIT -> "سجل العمليات"
                                        Screen.HELP -> "دليل الاستخدام"
                                        else -> "النظام المحاسبي"
                                    },
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Text(companyName, fontSize = 11.sp, color = TextSecondary, maxLines = 1)
                            }
                        },
                        navigationIcon = {
                            IconButton(
                                onClick = { scope.launch { drawerState.open() } },
                                modifier = Modifier.testTag("drawer_toggle_btn")
                            ) {
                                Icon(Icons.Default.Menu, contentDescription = "القائمة", tint = GoldAccent)
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(containerColor = NavyDark)
                    )
                }
            },
            snackbarHost = { SnackbarHost(snackbarHostState) },
            containerColor = BackgroundDark
        ) { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                // Secondary screens BackHandler
                if (currentScreen != Screen.DASHBOARD) {
                    BackHandler {
                        viewModel.navigateBack()
                    }
                }

                when (currentScreen) {
                    Screen.DASHBOARD -> DashboardScreen(viewModel)
                    Screen.ACCOUNTS, Screen.ACCOUNTS_TREE -> AccountsScreen(viewModel)
                    Screen.JOURNAL -> JournalScreen(viewModel)
                    Screen.JOURNAL_EDITOR -> JournalEditorScreen(viewModel)
                    Screen.LEDGER -> LedgerScreen(viewModel)
                    Screen.TRIAL_BALANCE -> TrialBalanceScreen(viewModel)
                    Screen.INCOME_STATEMENT -> IncomeStatementScreen(viewModel)
                    Screen.BALANCE_SHEET -> BalanceSheetScreen(viewModel)
                    Screen.CASH_FLOW -> CashFlowScreen(viewModel)
                    Screen.BUDGET -> BudgetScreen(viewModel)
                    Screen.USERS -> UsersScreen(viewModel)
                    Screen.SETTINGS -> SettingsScreen(viewModel)
                    Screen.AUDIT -> AuditScreen(viewModel)
                    Screen.HELP -> HelpScreen()
                    else -> DashboardScreen(viewModel)
                }
            }
        }
    }
}
