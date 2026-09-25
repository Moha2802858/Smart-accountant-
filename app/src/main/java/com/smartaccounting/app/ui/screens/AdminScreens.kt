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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.smartaccounting.app.data.model.RegionEntity
import com.smartaccounting.app.data.model.UserEntity
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

@Composable
fun UsersScreen(viewModel: AccountingViewModel) {
    val users by viewModel.allUsers.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()

    var showAddUserDialog by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf<UserEntity?>(null) }

    Scaffold(
        floatingActionButton = {
            if (currentUser?.role == "admin") {
                FloatingActionButton(
                    onClick = { showAddUserDialog = true },
                    containerColor = GoldAccent,
                    contentColor = NavyDark,
                    modifier = Modifier.testTag("add_user_fab")
                ) {
                    Icon(Icons.Default.Add, contentDescription = "مستخدم جديد")
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
            SectionHeader(
                title = "👥 إدارة المستخدمين والصلاحيات",
                subtitle = "التحكم في حسابات المحاسبين والمديرين"
            )

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 80.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(users, key = { it.id }) { user ->
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
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(user.fullName, fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 15.sp)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Surface(
                                        color = when (user.role) {
                                            "admin" -> GoldAccent.copy(alpha = 0.2f)
                                            "accountant" -> TealAccent.copy(alpha = 0.2f)
                                            else -> NavyCard
                                        },
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Text(
                                            text = when (user.role) {
                                                "admin" -> "مدير النظام"
                                                "accountant" -> "محاسب"
                                                else -> "مشاهدة فقط"
                                            },
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = when (user.role) {
                                                "admin" -> GoldAccent
                                                "accountant" -> TealAccent
                                                else -> TextSecondary
                                            },
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("اسم المستخدم: @${user.username}", color = TextSecondary, fontSize = 12.sp)
                            }

                            if (currentUser?.role == "admin" && user.id != currentUser?.id) {
                                IconButton(onClick = { showDeleteConfirm = user }) {
                                    Icon(Icons.Default.Delete, contentDescription = "حذف", tint = RedNegative)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddUserDialog) {
        AddUserDialog(
            onDismiss = { showAddUserDialog = false },
            onSave = { user, pass ->
                viewModel.saveUser(user, pass) {
                    showAddUserDialog = false
                }
            }
        )
    }

    if (showDeleteConfirm != null) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = null },
            title = { Text("حذف المستخدم", fontWeight = FontWeight.Bold) },
            text = { Text("هل أنت متأكد من حذف المستخدم @${showDeleteConfirm?.username}؟") },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteConfirm?.let { viewModel.deleteUser(it.id) }
                        showDeleteConfirm = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RedNegative)
                ) {
                    Text("حذف")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = null }) { Text("إلغاء") }
            },
            containerColor = NavySurface
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddUserDialog(
    onDismiss: () -> Unit,
    onSave: (UserEntity, String) -> Unit
) {
    var username by remember { mutableStateOf("") }
    var fullName by remember { mutableStateOf("") }
    var role by remember { mutableStateOf("accountant") }
    var password by remember { mutableStateOf("") }
    var question by remember { mutableStateOf("ما اسم أول مدرسة التحقت بها؟") }
    var answer by remember { mutableStateOf("") }
    var roleDropdownExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("إضافة مستخدم جديد", fontWeight = FontWeight.Bold, color = GoldAccent) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = { Text("اسم المستخدم (login)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = fullName,
                    onValueChange = { fullName = it },
                    label = { Text("الاسم الكامل") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                ExposedDropdownMenuBox(
                    expanded = roleDropdownExpanded,
                    onExpandedChange = { roleDropdownExpanded = it }
                ) {
                    OutlinedTextField(
                        value = when (role) {
                            "admin" -> "مدير النظام"
                            "accountant" -> "محاسب"
                            else -> "مشاهدة فقط"
                        },
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("الصلاحية") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = roleDropdownExpanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = roleDropdownExpanded,
                        onDismissRequest = { roleDropdownExpanded = false }
                    ) {
                        DropdownMenuItem(text = { Text("مدير النظام") }, onClick = { role = "admin"; roleDropdownExpanded = false })
                        DropdownMenuItem(text = { Text("محاسب") }, onClick = { role = "accountant"; roleDropdownExpanded = false })
                        DropdownMenuItem(text = { Text("مشاهدة فقط") }, onClick = { role = "viewer"; roleDropdownExpanded = false })
                    }
                }
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("كلمة المرور (8 أحرف على الأقل)") },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = question,
                    onValueChange = { question = it },
                    label = { Text("سؤال الأمان") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = answer,
                    onValueChange = { answer = it },
                    label = { Text("إجابة سؤال الأمان") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(
                        UserEntity(
                            username = username.trim(),
                            fullName = fullName.trim(),
                            role = role,
                            passwordHash = "",
                            securityQuestion = question.trim(),
                            securityAnswerHash = answer.trim()
                        ),
                        password
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = GoldAccent, contentColor = NavyDark)
            ) {
                Text("حفظ", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("إلغاء") }
        },
        containerColor = NavySurface
    )
}

@Composable
fun SettingsScreen(viewModel: AccountingViewModel) {
    val companyName by viewModel.companyName.collectAsState()
    val periodFrom by viewModel.periodFrom.collectAsState()
    val periodTo by viewModel.periodTo.collectAsState()
    val regions by viewModel.regions.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()

    var nameInput by remember(companyName) { mutableStateOf(companyName) }
    var fromInput by remember(periodFrom) { mutableStateOf(periodFrom) }
    var toInput by remember(periodTo) { mutableStateOf(periodTo) }

    var newRegionName by remember { mutableStateOf("") }
    var showWipeConfirm by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(bottom = 60.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            SectionHeader(
                title = "⚙️ إعدادات النظام",
                subtitle = "تخصيص بيانات المؤسسة والفترات المالية والمناطق"
            )
        }

        // Company Details Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = NavySurface)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("🏢 بيانات المؤسسة والفترة", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = GoldAccent)

                    OutlinedTextField(
                        value = nameInput,
                        onValueChange = { nameInput = it },
                        label = { Text("اسم الشركة / المؤسسة") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = fromInput,
                            onValueChange = { fromInput = it },
                            label = { Text("بداية الفترة") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = toInput,
                            onValueChange = { toInput = it },
                            label = { Text("نهاية الفترة") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }

                    if (currentUser?.role == "admin") {
                        Button(
                            onClick = { viewModel.updateSettings(nameInput, fromInput, toInput) },
                            colors = ButtonDefaults.buttonColors(containerColor = GoldAccent, contentColor = NavyDark),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("حفظ بيانات المؤسسة", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Regions Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = NavySurface)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("🗺️ الفروع والمناطق", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = GoldAccent)

                    if (currentUser?.role == "admin") {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = newRegionName,
                                onValueChange = { newRegionName = it },
                                label = { Text("اسم المنطقة / الفرع الجديد") },
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                            Button(
                                onClick = {
                                    if (newRegionName.isNotBlank()) {
                                        viewModel.saveRegion(RegionEntity(name = newRegionName.trim(), active = 1)) {
                                            newRegionName = ""
                                        }
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = GoldAccent, contentColor = NavyDark),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.height(56.dp)
                            ) {
                                Text("إضافة", fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    regions.forEach { reg ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(reg.name, color = if (reg.active == 1) TextPrimary else TextMuted, fontSize = 14.sp)
                            if (currentUser?.role == "admin") {
                                Switch(
                                    checked = reg.active == 1,
                                    onCheckedChange = { viewModel.toggleRegion(reg.id) },
                                    colors = SwitchDefaults.colors(checkedThumbColor = GoldAccent, checkedTrackColor = NavyDark)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Danger Zone: Wipe System (Admin Only)
        if (currentUser?.role == "admin") {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = RedNegative.copy(alpha = 0.1f))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("⚠️ منطقة الخطر", fontWeight = FontWeight.Bold, color = RedNegative, fontSize = 15.sp)
                        Text(
                            "مسح جميع الحسابات وجميع قيود اليومية من النظام نهائيًا.",
                            color = TextSecondary,
                            fontSize = 12.sp
                        )
                        OutlinedButton(
                            onClick = { showWipeConfirm = true },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = RedNegative),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("مسح الشجرة والقيود نهائيًا")
                        }
                    }
                }
            }
        }
    }

    if (showWipeConfirm) {
        AlertDialog(
            onDismissRequest = { showWipeConfirm = false },
            title = { Text("تأكيد مسح البيانات بالكامل", color = RedNegative, fontWeight = FontWeight.Bold) },
            text = { Text("تحذير: سيتم حذف جميع الحسابات وجميع قيود اليومية نهائيًا ولا يمكن التراجع عن هذه الخطوة!") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.wipeAccountsAndEntries()
                        showWipeConfirm = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RedNegative)
                ) {
                    Text("نعم، امسح كل شيء")
                }
            },
            dismissButton = {
                TextButton(onClick = { showWipeConfirm = false }) { Text("إلغاء") }
            },
            containerColor = NavySurface
        )
    }
}

@Composable
fun AuditScreen(viewModel: AccountingViewModel) {
    val logs by viewModel.auditLogs.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .padding(horizontal = 16.dp)
    ) {
        SectionHeader(
            title = "🛡️ سجل العمليات والرقابة",
            subtitle = "سجل توثيقي لكافة العمليات المنفذة في النظام"
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 40.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(logs) { log ->
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
                            Text(log.user, fontWeight = FontWeight.Bold, color = GoldAccent, fontSize = 13.sp)
                            Text(log.at, color = TextSecondary, fontSize = 11.sp)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("العملية: ${log.action}", fontWeight = FontWeight.SemiBold, color = TextPrimary, fontSize = 13.sp)
                        if (log.details.isNotBlank()) {
                            Text(log.details, color = TextMuted, fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun HelpScreen() {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(bottom = 40.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            SectionHeader(
                title = "📘 دليل الاستخدام والمساعدة",
                subtitle = "شرح أقسام النظام المحاسبي وآلية العمل"
            )
        }

        val guides = listOf(
            Pair("📚 دليل الحسابات", "يحتوي على كافة الحسابات مقسمة إلى أصول، خصوم، حقوق ملكية، إيرادات، ومصروفات. يمكنك إضافة وتعديل الحسابات وتحديد الأرصدة الافتتاحية والموازنات."),
            Pair("📒 دفتر اليومية العامة", "تسجيل القيود المحاسبية بنظام القيد المزدوج (مدين = دائن). الحركات تظل في حالة «مسودة» حتى يتم ترحيلها لتنعكس على الحسابات."),
            Pair("📖 الأستاذ العام", "كشف تفصيلي لحركة أي حساب مع الرصيد الافتتاحي والحركات خلال الفترة والرصيد التراكمي بعد كل عملية."),
            Pair("⚖️ ميزان المراجعة", "تقرير للتحقق من توازن الأرصدة الافتتاحية وحركات الفترة والأرصدة الختامية لجميع الحسابات."),
            Pair("📈 القوائم والتقارير المالية", "تشمل قائمة الدخل لاحتساب صافي الأرباح/الخسائر، وقائمة المركز المالي للأصول والخصوم، وقائمة التدفقات النقدية والموازنة التقديرية.")
        )

        items(guides) { (title, desc) ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = NavySurface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(title, fontWeight = FontWeight.Bold, color = GoldAccent, fontSize = 15.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(desc, color = TextPrimary, fontSize = 13.sp, lineHeight = 20.sp)
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = NavyDark)
            ) {
                Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("نظام الحسابات المتكامل — إصدار أندرويد", fontWeight = FontWeight.Bold, color = GoldAccent)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("© جميع الحقوق محفوظة — محاسب / أحمد عبدالله", color = TextSecondary, fontSize = 12.sp)
                }
            }
        }
    }
}
