package com.smartaccounting.app.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.smartaccounting.app.data.dao.AccountingDao
import com.smartaccounting.app.data.model.AccountEntity
import com.smartaccounting.app.data.model.AuditLogEntity
import com.smartaccounting.app.data.model.JournalEntryEntity
import com.smartaccounting.app.data.model.JournalLineEntity
import com.smartaccounting.app.data.model.RegionEntity
import com.smartaccounting.app.data.model.SettingEntity
import com.smartaccounting.app.data.model.UserEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Database(
    entities = [
        UserEntity::class,
        AccountEntity::class,
        JournalEntryEntity::class,
        JournalLineEntity::class,
        SettingEntity::class,
        AuditLogEntity::class,
        RegionEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun accountingDao(): AccountingDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "accounting_database"
                )
                    .addCallback(DatabaseCallback())
                    .build()
                INSTANCE = instance
                instance
            }
        }

        fun hashPassword(password: String): String {
            val bytes = MessageDigest.getInstance("SHA-256").digest(password.toByteArray())
            return bytes.joinToString("") { "%02x".format(it) }
        }

        private class DatabaseCallback : Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    CoroutineScope(Dispatchers.IO).launch {
                        populateDatabase(database.accountingDao())
                    }
                }
            }

            suspend fun populateDatabase(dao: AccountingDao) {
                val now = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date())

                // 1. Default Admin User
                val adminPass = hashPassword("admin123")
                val secAns = hashPassword("الأمل")
                dao.insertUser(
                    UserEntity(
                        username = "admin",
                        passwordHash = adminPass,
                        fullName = "مدير النظام",
                        role = "admin",
                        securityQuestion = "ما اسم أول مدرسة التحقت بها؟",
                        securityAnswerHash = secAns,
                        createdAt = now
                    )
                )

                // 2. Default Starter Accounts
                val starterAccounts = listOf(
                    AccountEntity(accNo = "1101", name = "الصندوق", type = "أصول", openingBalance = 0.0, budget = 0.0, createdAt = now),
                    AccountEntity(accNo = "1102", name = "البنك الأهلي", type = "أصول", openingBalance = 0.0, budget = 0.0, createdAt = now),
                    AccountEntity(accNo = "1103", name = "بنك مصر", type = "أصول", openingBalance = 0.0, budget = 0.0, createdAt = now),
                    AccountEntity(accNo = "1201", name = "العملاء (المدينون)", type = "أصول", openingBalance = 0.0, budget = 0.0, createdAt = now),
                    AccountEntity(accNo = "1301", name = "المخزون", type = "أصول", openingBalance = 0.0, budget = 0.0, createdAt = now),
                    AccountEntity(accNo = "1401", name = "الأصول الثابتة", type = "أصول", openingBalance = 0.0, budget = 0.0, createdAt = now),
                    AccountEntity(accNo = "1402", name = "مجمع الإهلاك", type = "أصول", openingBalance = 0.0, budget = 0.0, createdAt = now),
                    AccountEntity(accNo = "2101", name = "الموردون (الدائنون)", type = "خصوم", openingBalance = 0.0, budget = 0.0, createdAt = now),
                    AccountEntity(accNo = "2102", name = "مصروفات مستحقة", type = "خصوم", openingBalance = 0.0, budget = 0.0, createdAt = now),
                    AccountEntity(accNo = "2201", name = "قروض طويلة الأجل", type = "خصوم", openingBalance = 0.0, budget = 0.0, createdAt = now),
                    AccountEntity(accNo = "3101", name = "رأس المال", type = "حقوق ملكية", openingBalance = 0.0, budget = 0.0, createdAt = now),
                    AccountEntity(accNo = "3102", name = "المسحوبات الشخصية", type = "حقوق ملكية", openingBalance = 0.0, budget = 0.0, createdAt = now),
                    AccountEntity(accNo = "3103", name = "الأرباح المحتجزة", type = "حقوق ملكية", openingBalance = 0.0, budget = 0.0, createdAt = now),
                    AccountEntity(accNo = "4101", name = "إيرادات المبيعات", type = "إيرادات", openingBalance = 0.0, budget = 0.0, createdAt = now),
                    AccountEntity(accNo = "4102", name = "إيرادات أخرى", type = "إيرادات", openingBalance = 0.0, budget = 0.0, createdAt = now),
                    AccountEntity(accNo = "5101", name = "رواتب وأجور", type = "مصروفات", openingBalance = 0.0, budget = 0.0, createdAt = now),
                    AccountEntity(accNo = "5102", name = "إيجارات", type = "مصروفات", openingBalance = 0.0, budget = 0.0, createdAt = now),
                    AccountEntity(accNo = "5103", name = "كهرباء ومياه", type = "مصروفات", openingBalance = 0.0, budget = 0.0, createdAt = now),
                    AccountEntity(accNo = "5104", name = "هاتف وإنترنت", type = "مصروفات", openingBalance = 0.0, budget = 0.0, createdAt = now),
                    AccountEntity(accNo = "5105", name = "صيانة", type = "مصروفات", openingBalance = 0.0, budget = 0.0, createdAt = now),
                    AccountEntity(accNo = "5106", name = "وقود ومواصلات", type = "مصروفات", openingBalance = 0.0, budget = 0.0, createdAt = now),
                    AccountEntity(accNo = "5107", name = "قرطاسية ومطبوعات", type = "مصروفات", openingBalance = 0.0, budget = 0.0, createdAt = now),
                    AccountEntity(accNo = "5108", name = "إعلانات ودعاية", type = "مصروفات", openingBalance = 0.0, budget = 0.0, createdAt = now),
                    AccountEntity(accNo = "5109", name = "ضيافة", type = "مصروفات", openingBalance = 0.0, budget = 0.0, createdAt = now),
                    AccountEntity(accNo = "5110", name = "مصروفات بنكية", type = "مصروفات", openingBalance = 0.0, budget = 0.0, createdAt = now),
                    AccountEntity(accNo = "5111", name = "إهلاك", type = "مصروفات", openingBalance = 0.0, budget = 0.0, createdAt = now),
                    AccountEntity(accNo = "5112", name = "ضرائب ورسوم", type = "مصروفات", openingBalance = 0.0, budget = 0.0, createdAt = now),
                    AccountEntity(accNo = "5113", name = "مصروفات متنوعة", type = "مصروفات", openingBalance = 0.0, budget = 0.0, createdAt = now),
                    AccountEntity(accNo = "9901", name = "حساب وسيط - سداد", type = "أخرى", openingBalance = 0.0, budget = 0.0, createdAt = now),
                    AccountEntity(accNo = "9902", name = "فروقات تسوية", type = "أخرى", openingBalance = 0.0, budget = 0.0, createdAt = now)
                )
                dao.insertAccounts(starterAccounts)

                // 3. Default Regions
                val defaultRegions = listOf(
                    RegionEntity(name = "المركز الرئيسي", active = 1),
                    RegionEntity(name = "المنطقة المركزية", active = 1),
                    RegionEntity(name = "المنطقة الشمالية", active = 1),
                    RegionEntity(name = "المنطقة الشرقية", active = 1),
                    RegionEntity(name = "المنطقة الجنوبية", active = 1)
                )
                defaultRegions.forEach { dao.insertRegion(it) }

                // 4. Default Settings
                dao.setSetting(SettingEntity("company_name", "مؤسسة الأعمال الذكية التجارية"))
                dao.setSetting(SettingEntity("period_from", "2026-01-01"))
                dao.setSetting(SettingEntity("period_to", "2026-12-31"))
                dao.setSetting(
                    SettingEntity(
                        "signatures",
                        "[{\"title\":\"المحاسب\",\"name\":\"أحمد عبدالله\"},{\"title\":\"المدير المالي\",\"name\":\"سارة محمد\"}]"
                    )
                )

                // 5. Initial Audit Log
                dao.insertAuditLog(
                    AuditLogEntity(
                        user = "النظام",
                        action = "تهيئة النظام",
                        details = "تم إنشاء الجداول والحسابات الافتراضية بنجاح",
                        at = now
                    )
                )
            }
        }
    }
}
