package com.tanjid.hisabapp.data

import android.content.Context
import android.content.SharedPreferences
import androidx.room.Room
import java.security.MessageDigest

/**
 * Simple password hashing (SHA-256). For a production app use a proper
 * salted hash (e.g. bcrypt via a server). Kept local-only here on purpose.
 */
fun sha256(input: String): String {
    val bytes = MessageDigest.getInstance("SHA-256").digest(input.toByteArray())
    return bytes.joinToString("") { "%02x".format(it) }
}

fun todayString(): String {
    val c = java.util.Calendar.getInstance()
    return "%04d-%02d-%02d".format(c.get(java.util.Calendar.YEAR), c.get(java.util.Calendar.MONTH) + 1, c.get(java.util.Calendar.DAY_OF_MONTH))
}

fun monthString(): String = todayString().substring(0, 7)

/** 90-day persistent session, stored locally. */
class SessionManager(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("hisab_session", Context.MODE_PRIVATE)

    fun saveLogin(email: String) {
        val expiry = System.currentTimeMillis() + 90L * 24 * 60 * 60 * 1000
        prefs.edit().putString("email", email).putLong("expiry", expiry).apply()
    }

    fun currentEmail(): String? {
        val email = prefs.getString("email", null) ?: return null
        if (System.currentTimeMillis() > prefs.getLong("expiry", 0)) return null
        return email
    }

    fun logout() { prefs.edit().clear().apply() }
}

class HisabRepository(context: Context) {
    val db: AppDatabase = Room.databaseBuilder(
        context.applicationContext,
        AppDatabase::class.java, "hisab-app.db"
    ).build()

    val session = SessionManager(context)

    // --- Auth ---
    suspend fun signUp(name: String, email: String, password: String): Result<Unit> {
        if (db.userDao().getByEmail(email) != null) return Result.failure(Exception("Ei Gmail diye account agei ache"))
        db.userDao().insert(User(email = email, name = name, passwordHash = sha256(password)))
        return Result.success(Unit)
    }

    suspend fun login(email: String, password: String): Result<User> {
        val user = db.userDao().getByEmail(email) ?: return Result.failure(Exception("Account pai nai"))
        if (user.passwordHash != sha256(password)) return Result.failure(Exception("Password vul"))
        session.saveLogin(email)
        return Result.success(user)
    }

    suspend fun resetPassword(email: String, newPassword: String): Result<Unit> {
        val user = db.userDao().getByEmail(email) ?: return Result.failure(Exception("Account pai nai"))
        db.userDao().updatePassword(email, sha256(newPassword))
        session.saveLogin(email)
        return Result.success(Unit)
    }

    suspend fun currentUser(): User? {
        val email = session.currentEmail() ?: return null
        return db.userDao().getByEmail(email)
    }

    // --- Derived totals (pure, testable) ---
    /** Due for one daily job: total earned - total received. Payment-only entries reduce it. */
    suspend fun dailyDue(owner: String, jobId: Long): Long =
        db.dailyDao().totalEarned(owner, jobId) - db.dailyDao().totalReceived(owner, jobId)

    suspend fun khataBalance(owner: String, partyId: Long, entries: List<KhataEntry>): Long {
        // GAVE = apni paben (+), GOT = taka pelen, paben komlo (-)
        var total = 0L
        for (e in entries) total += if (e.type == "GAVE") e.amount else -e.amount
        return total
    }
}
