package com.smartaccounting.app

import android.app.Application
import com.smartaccounting.app.data.db.AppDatabase
import com.smartaccounting.app.data.repository.AccountingRepository

class AccountingApplication : Application() {
    val database by lazy { AppDatabase.getDatabase(this) }
    val repository by lazy { AccountingRepository(database.accountingDao()) }

    override fun onCreate() {
        super.onCreate()
    }
}
