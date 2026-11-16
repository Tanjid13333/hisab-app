package com.tanjid.hisabapp.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

// ---------- Entities: every table is isolated per user by ownerEmail ----------

@Entity(tableName = "users", indices = [Index(value = ["email"], unique = true)])
data class User(
    @PrimaryKey val email: String,
    val name: String,
    val passwordHash: String,
    val emailVerified: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "transactions")
data class Transaction(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val ownerEmail: String,
    val type: String, // "INCOME" or "EXPENSE"
    val amount: Long, // Taka, whole taka
    val category: String,
    val date: String, // yyyy-MM-dd
    val note: String = "",
    val account: String = "Personal", // Personal | Business
    val paymentMethod: String = "Cash" // Cash | bKash | Nagad | Bank
)

@Entity(tableName = "daily_jobs")
data class DailyJob(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val ownerEmail: String,
    val companyName: String,
    val companyEmail: String = ""
)

@Entity(tableName = "daily_entries")
data class DailyEntry(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val ownerEmail: String,
    val jobId: Long,
    val date: String, // yyyy-MM-dd
    val earned: Long = 0,
    val received: Long = 0
    // due for this day = earned - received.
    // Payment-only entry: earned = 0, received = amount -> minuses previous due.
)

@Entity(tableName = "parties")
data class Party(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val ownerEmail: String,
    val name: String,
    val phone: String = "",
    val email: String = "" // Gmail for pawna/dena notification
)

@Entity(tableName = "khata_entries")
data class KhataEntry(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val ownerEmail: String,
    val partyId: Long,
    val date: String,
    val type: String, // "GAVE" (baki dilen, apni paben) or "GOT" (taka pelen, apni diben komlo)
    val amount: Long,
    val note: String = ""
)

@Entity(tableName = "savings_goals")
data class SavingsGoal(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val ownerEmail: String,
    val title: String,
    val target: Long
)

@Entity(tableName = "savings_entries")
data class SavingsEntry(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val ownerEmail: String,
    val goalId: Long,
    val date: String,
    val type: String, // "DEPOSIT" or "WITHDRAW"
    val amount: Long
)

// ---------- DAOs ----------

@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun getByEmail(email: String): User?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(user: User)

    @Query("UPDATE users SET passwordHash = :hash WHERE email = :email")
    suspend fun updatePassword(email: String, hash: String)

    @Query("UPDATE users SET emailVerified = 1 WHERE email = :email")
    suspend fun markVerified(email: String)
}

@Dao
interface TransactionDao {
    @Query("SELECT * FROM transactions WHERE ownerEmail = :owner ORDER BY date DESC, id DESC")
    fun list(owner: String): Flow<List<Transaction>>

    @Insert suspend fun insert(tx: Transaction)
    @Update suspend fun update(tx: Transaction)
    @Delete suspend fun delete(tx: Transaction)

    @Query("SELECT COALESCE(SUM(amount),0) FROM transactions WHERE ownerEmail=:o AND type='INCOME'")
    suspend fun totalIncome(o: String): Long

    @Query("SELECT COALESCE(SUM(amount),0) FROM transactions WHERE ownerEmail=:o AND type='EXPENSE'")
    suspend fun totalExpense(o: String): Long

    @Query("SELECT category, type, COALESCE(SUM(amount),0) AS total FROM transactions WHERE ownerEmail=:o AND substr(date,1,7)=:month GROUP BY category, type")
    suspend fun monthlyByCategory(o: String, month: String): List<CategoryTotal>
}

data class CategoryTotal(val category: String, val type: String, val total: Long)

@Dao
interface DailyDao {
    @Query("SELECT * FROM daily_jobs WHERE ownerEmail = :owner ORDER BY id DESC")
    fun listJobs(owner: String): Flow<List<DailyJob>>

    @Insert suspend fun insertJob(job: DailyJob): Long
    @Delete suspend fun deleteJob(job: DailyJob)

    @Query("SELECT * FROM daily_entries WHERE ownerEmail=:o AND jobId=:jobId ORDER BY date DESC")
    fun listEntries(o: String, jobId: Long): Flow<List<DailyEntry>>

    @Insert suspend fun insertEntry(e: DailyEntry)
    @Delete suspend fun deleteEntry(e: DailyEntry)

    @Query("SELECT COALESCE(SUM(earned),0) FROM daily_entries WHERE ownerEmail=:o AND jobId=:jobId")
    suspend fun totalEarned(o: String, jobId: Long): Long

    @Query("SELECT COALESCE(SUM(received),0) FROM daily_entries WHERE ownerEmail=:o AND jobId=:jobId")
    suspend fun totalReceived(o: String, jobId: Long): Long
}

@Dao
interface KhataDao {
    @Query("SELECT * FROM parties WHERE ownerEmail = :owner ORDER BY name ASC")
    fun listParties(owner: String): Flow<List<Party>>

    @Insert suspend fun insertParty(p: Party): Long
    @Delete suspend fun deleteParty(p: Party)
    @Update suspend fun updateParty(p: Party)

    @Query("SELECT * FROM khata_entries WHERE ownerEmail=:o AND partyId=:partyId ORDER BY date DESC, id DESC")
    fun listEntries(o: String, partyId: Long): Flow<List<KhataEntry>>

    @Insert suspend fun insertEntry(e: KhataEntry)
    @Delete suspend fun deleteEntry(e: KhataEntry)
}

@Dao
interface SavingsDao {
    @Query("SELECT * FROM savings_goals WHERE ownerEmail = :owner ORDER BY id DESC")
    fun listGoals(owner: String): Flow<List<SavingsGoal>>

    @Insert suspend fun insertGoal(g: SavingsGoal): Long
    @Delete suspend fun deleteGoal(g: SavingsGoal)

    @Query("SELECT * FROM savings_entries WHERE ownerEmail=:o AND goalId=:goalId ORDER BY date DESC")
    fun listEntries(o: String, goalId: Long): Flow<List<SavingsEntry>>

    @Insert suspend fun insertEntry(e: SavingsEntry)
    @Delete suspend fun deleteEntry(e: SavingsEntry)
}

// ---------- Database ----------

@Database(
    entities = [User::class, Transaction::class, DailyJob::class, DailyEntry::class,
        Party::class, KhataEntry::class, SavingsGoal::class, SavingsEntry::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun transactionDao(): TransactionDao
    abstract fun dailyDao(): DailyDao
    abstract fun khataDao(): KhataDao
    abstract fun savingsDao(): SavingsDao
}
