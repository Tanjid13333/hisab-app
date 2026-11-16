package com.tanjid.hisabapp.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.tanjid.hisabapp.data.*
import com.tanjid.hisabapp.util.EmailHelper
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class HisabViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = HisabRepository(app)
    private val db = repo.db

    val currentUser = MutableStateFlow<User?>(null)
    val authError = MutableStateFlow<String?>(null)
    val authBusy = MutableStateFlow(false)

    // Gmail code state: 10-minute expiry, 5 attempts, 60s resend cooldown
    data class CodeState(val code: String, val email: String, val expiresAt: Long, var attemptsLeft: Int = 5, var lastSentAt: Long = 0L)
    private var pendingCode: CodeState? = null
    val codeStatus = MutableStateFlow<String?>(null) // "SENDING" | "SENT" | "FAILED" | null

    init { viewModelScope.launch { currentUser.value = repo.currentUser() } }

    fun logout() { repo.session.logout(); currentUser.value = null }

    // --- Sign up with Gmail code ---
    fun requestSignupCode(name: String, email: String, password: String) {
        viewModelScope.launch {
            authBusy.value = true; authError.value = null; codeStatus.value = "SENDING"
            val code = EmailHelper.generateCode()
            val sent = EmailHelper.sendVerificationCode(email, code)
            pendingCode = CodeState(code, email, System.currentTimeMillis() + 10 * 60 * 1000, lastSentAt = System.currentTimeMillis())
            // Save pending signup details in memory only for this demo flow
            pendingSignup = Triple(name, email, password)
            codeStatus.value = if (sent) "SENT" else "FAILED"
            authBusy.value = false
        }
    }
    private var pendingSignup: Triple<String, String, String>? = null

    fun verifySignupCode(input: String): Boolean {
        val state = pendingCode ?: return false
        if (System.currentTimeMillis() > state.expiresAt) { authError.value = "Code er meyad sesh"; return false }
        if (state.attemptsLeft <= 0) { authError.value = "5 bar vul code"; return false }
        if (input != state.code) {
            state.attemptsLeft--
            authError.value = "Vul code — ar ${state.attemptsLeft} bar chesta korte parben"
            return false
        }
        val (name, email, password) = pendingSignup ?: return false
        viewModelScope.launch {
            repo.signUp(name, email, password)
            db.userDao().markVerified(email)
            repo.login(email, password)
            currentUser.value = repo.currentUser()
        }
        pendingCode = null
        return true
    }

    // --- Login / Forgot password (same code flow) ---
    fun login(email: String, password: String) {
        viewModelScope.launch {
            authBusy.value = true
            repo.login(email, password)
                .onSuccess { currentUser.value = it }
                .onFailure { authError.value = it.message }
            authBusy.value = false
        }
    }

    fun requestForgotCode(email: String) {
        viewModelScope.launch {
            codeStatus.value = "SENDING"
            val code = EmailHelper.generateCode()
            val sent = EmailHelper.sendVerificationCode(email, code)
            pendingCode = CodeState(code, email, System.currentTimeMillis() + 10 * 60 * 1000)
            codeStatus.value = if (sent) "SENT" else "FAILED"
        }
    }

    fun verifyForgotCode(input: String, newPassword: String): Boolean {
        val state = pendingCode ?: return false
        if (input != state.code || System.currentTimeMillis() > state.expiresAt) return false
        viewModelScope.launch {
            repo.resetPassword(state.email, newPassword)
            currentUser.value = repo.currentUser()
        }
        pendingCode = null
        return true
    }

    private fun owner(): String = currentUser.value?.email ?: ""

    // --- Flows for screens ---
    fun transactions(): Flow<List<Transaction>> = db.transactionDao().list(owner())
    fun dailyJobs(): Flow<List<DailyJob>> = db.dailyDao().listJobs(owner())
    fun dailyEntries(jobId: Long): Flow<List<DailyEntry>> = db.dailyDao().listEntries(owner(), jobId)
    fun parties(): Flow<List<Party>> = db.khataDao().listParties(owner())
    fun khataEntries(partyId: Long): Flow<List<KhataEntry>> = db.khataDao().listEntries(owner(), partyId)
    fun goals(): Flow<List<SavingsGoal>> = db.savingsDao().listGoals(owner())
    fun savingsEntries(goalId: Long): Flow<List<SavingsEntry>> = db.savingsDao().listEntries(owner(), goalId)

    fun addTransaction(tx: Transaction) = viewModelScope.launch { db.transactionDao().insert(tx.copy(ownerEmail = owner())) }
    fun deleteTransaction(tx: Transaction) = viewModelScope.launch { db.transactionDao().delete(tx) }

    fun addDailyJob(name: String, email: String) = viewModelScope.launch {
        db.dailyDao().insertJob(DailyJob(ownerEmail = owner(), companyName = name, companyEmail = email))
    }

    /** earned=0, received=amount => minuses previous due. Both 0 is rejected. */
    fun addDailyEntry(jobId: Long, date: String, earned: Long, received: Long) {
        if (earned == 0L && received == 0L) return
        viewModelScope.launch {
            db.dailyDao().insertEntry(DailyEntry(ownerEmail = owner(), jobId = jobId, date = date, earned = earned, received = received))
        }
    }

    fun addParty(name: String, phone: String, email: String) = viewModelScope.launch {
        db.khataDao().insertParty(Party(ownerEmail = owner(), name = name, phone = phone, email = email))
    }

    fun addKhataEntry(partyId: Long, date: String, type: String, amount: Long, note: String = "") =
        viewModelScope.launch {
            db.khataDao().insertEntry(KhataEntry(ownerEmail = owner(), partyId = partyId, date = date, type = type, amount = amount, note = note))
        }

    fun addGoal(title: String, target: Long) = viewModelScope.launch {
        db.savingsDao().insertGoal(SavingsGoal(ownerEmail = owner(), title = title, target = target))
    }

    fun addSavingsEntry(goalId: Long, date: String, type: String, amount: Long) = viewModelScope.launch {
        db.savingsDao().insertEntry(SavingsEntry(ownerEmail = owner(), goalId = goalId, date = date, type = type, amount = amount))
    }

    suspend fun totals(): Pair<Long, Long> =
        db.transactionDao().totalIncome(owner()) to db.transactionDao().totalExpense(owner())

    suspend fun dailyDue(jobId: Long): Long = repo.dailyDue(owner(), jobId)
}
