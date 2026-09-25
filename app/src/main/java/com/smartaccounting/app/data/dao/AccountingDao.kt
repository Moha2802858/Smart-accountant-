package com.smartaccounting.app.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.smartaccounting.app.data.model.AccountEntity
import com.smartaccounting.app.data.model.AuditLogEntity
import com.smartaccounting.app.data.model.JournalEntryEntity
import com.smartaccounting.app.data.model.JournalLineEntity
import com.smartaccounting.app.data.model.RegionEntity
import com.smartaccounting.app.data.model.SettingEntity
import com.smartaccounting.app.data.model.UserEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AccountingDao {

    // --- Users ---
    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    suspend fun getUserById(id: Long): UserEntity?

    @Query("SELECT * FROM users WHERE LOWER(username) = LOWER(:username) LIMIT 1")
    suspend fun getUserByUsername(username: String): UserEntity?

    @Query("SELECT * FROM users ORDER BY id ASC")
    fun getAllUsersFlow(): Flow<List<UserEntity>>

    @Query("SELECT * FROM users ORDER BY id ASC")
    suspend fun getAllUsers(): List<UserEntity>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertUser(user: UserEntity): Long

    @Update
    suspend fun updateUser(user: UserEntity)

    @Query("DELETE FROM users WHERE id = :id")
    suspend fun deleteUser(id: Long)

    @Query("SELECT COUNT(*) FROM users WHERE role = 'admin'")
    suspend fun countAdminUsers(): Int

    // --- Accounts ---
    @Query("SELECT * FROM accounts ORDER BY CAST(accNo AS INTEGER), accNo ASC")
    fun getAllAccountsFlow(): Flow<List<AccountEntity>>

    @Query("SELECT * FROM accounts ORDER BY CAST(accNo AS INTEGER), accNo ASC")
    suspend fun getAllAccounts(): List<AccountEntity>

    @Query("SELECT * FROM accounts WHERE id = :id LIMIT 1")
    suspend fun getAccountById(id: Long): AccountEntity?

    @Query("SELECT * FROM accounts WHERE accNo = :accNo LIMIT 1")
    suspend fun getAccountByNo(accNo: String): AccountEntity?

    @Query("SELECT * FROM accounts WHERE accNo LIKE '%' || :query || '%' OR name LIKE '%' || :query || '%' ORDER BY CAST(accNo AS INTEGER), accNo ASC")
    suspend fun searchAccounts(query: String): List<AccountEntity>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertAccount(account: AccountEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAccounts(accounts: List<AccountEntity>)

    @Update
    suspend fun updateAccount(account: AccountEntity)

    @Query("DELETE FROM accounts WHERE id = :id")
    suspend fun deleteAccount(id: Long)

    @Query("SELECT COUNT(*) FROM accounts")
    suspend fun countAccounts(): Int

    @Query("DELETE FROM accounts")
    suspend fun deleteAllAccounts()

    // --- Journal Entries ---
    @Query("SELECT * FROM journal_entries ORDER BY entryDate DESC, id DESC")
    fun getAllEntriesFlow(): Flow<List<JournalEntryEntity>>

    @Query("SELECT * FROM journal_entries ORDER BY entryDate DESC, id DESC")
    suspend fun getAllEntries(): List<JournalEntryEntity>

    @Query("SELECT * FROM journal_entries WHERE id = :id LIMIT 1")
    suspend fun getEntryById(id: Long): JournalEntryEntity?

    @Query("SELECT * FROM journal_entries WHERE movementNo = :movementNo LIMIT 1")
    suspend fun getEntryByMovementNo(movementNo: String): JournalEntryEntity?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertEntry(entry: JournalEntryEntity): Long

    @Update
    suspend fun updateEntry(entry: JournalEntryEntity)

    @Query("DELETE FROM journal_entries WHERE id = :id")
    suspend fun deleteEntry(id: Long)

    @Query("SELECT COUNT(*) FROM journal_entries")
    suspend fun countEntries(): Int

    @Query("SELECT COUNT(*) FROM journal_entries WHERE status = 'draft'")
    suspend fun countDraftEntries(): Int

    @Query("DELETE FROM journal_entries WHERE strftime('%Y-%m', entryDate) = :month")
    suspend fun deleteEntriesForMonth(month: String): Int

    @Query("DELETE FROM journal_entries")
    suspend fun deleteAllEntries(): Int

    // --- Journal Lines ---
    @Query("SELECT * FROM journal_lines WHERE entryId = :entryId ORDER BY id ASC")
    suspend fun getLinesForEntry(entryId: Long): List<JournalLineEntity>

    @Query("SELECT * FROM journal_lines WHERE entryId = :entryId ORDER BY id ASC")
    fun getLinesForEntryFlow(entryId: Long): Flow<List<JournalLineEntity>>

    @Query("SELECT * FROM journal_lines")
    suspend fun getAllJournalLines(): List<JournalLineEntity>

    @Query("SELECT * FROM journal_lines WHERE accountId = :accountId LIMIT 1")
    suspend fun getFirstLineForAccount(accountId: Long): JournalLineEntity?

    @Insert
    suspend fun insertLine(line: JournalLineEntity): Long

    @Insert
    suspend fun insertLines(lines: List<JournalLineEntity>)

    @Query("DELETE FROM journal_lines WHERE entryId = :entryId")
    suspend fun deleteLinesForEntry(entryId: Long)

    // --- Settings ---
    @Query("SELECT value FROM settings WHERE `key` = :key LIMIT 1")
    suspend fun getSetting(key: String): String?

    @Query("SELECT * FROM settings")
    suspend fun getAllSettings(): List<SettingEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun setSetting(setting: SettingEntity)

    // --- Audit Log ---
    @Query("SELECT * FROM audit_log ORDER BY id DESC LIMIT 300")
    fun getAuditLogsFlow(): Flow<List<AuditLogEntity>>

    @Query("SELECT * FROM audit_log ORDER BY id DESC LIMIT 300")
    suspend fun getAuditLogs(): List<AuditLogEntity>

    @Insert
    suspend fun insertAuditLog(log: AuditLogEntity): Long

    // --- Regions ---
    @Query("SELECT * FROM regions ORDER BY id ASC")
    fun getAllRegionsFlow(): Flow<List<RegionEntity>>

    @Query("SELECT * FROM regions ORDER BY id ASC")
    suspend fun getAllRegions(): List<RegionEntity>

    @Query("SELECT name FROM regions WHERE active = 1 ORDER BY id ASC")
    suspend fun getActiveRegionNames(): List<String>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRegion(region: RegionEntity): Long

    @Update
    suspend fun updateRegion(region: RegionEntity)

    @Query("DELETE FROM regions WHERE id = :id")
    suspend fun deleteRegion(id: Long)
}
