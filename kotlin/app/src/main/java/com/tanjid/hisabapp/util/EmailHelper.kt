package com.tanjid.hisabapp.util

import kotlin.random.Random

/**
 * Gmail code + party notification — PLACEHOLDER for real email sending.
 *
 * A standalone Android app cannot send a real Gmail code by itself.
 * Wire these two functions to your backend (your own server / Firebase):
 * the backend sends the code via Gmail and returns success/failure.
 * Until then the app generates the code locally and the UI shows the
 * flow honestly. NEVER log or show a real code in production logs.
 */
object EmailHelper {

    /** 6-digit code, 10-minute expiry is enforced by the caller (ViewModel). */
    fun generateCode(): String = (100000 + Random.nextInt(900000)).toString()

    /**
     * TODO (backend): POST { to, subject, body } to your server, which sends
     * the Gmail via your connected Gmail account. Return true only if sent.
     * Currently returns false so the UI shows "pathano jay ni" honestly
     * instead of pretending a mail went out.
     */
    suspend fun sendVerificationCode(toGmail: String, code: String): Boolean {
        // Real implementation goes here — see README.md.
        return false
    }

    /** Message text for a party/company when pawna/dena changes. No send here. */
    fun partyMessage(
        partyName: String,
        amount: Long,
        type: String, // "PAONA" (apni paben) or "DENA" (apni diben)
        totalBalance: Long,
        yourName: String
    ): String {
        val line = if (type == "PAONA") {
            "Apnar kache pawna ৳$amount. Mot pawna baki ৳$totalBalance."
        } else {
            "Apnake dena ৳$amount. Mot dena baki ৳$totalBalance."
        }
        return "Hisab App — $partyName, $line — $yourName"
    }

    fun fullStatementText(partyName: String, lines: List<String>, totalBalance: Long): String =
        buildString {
            appendLine("Hisab App — $partyName er puro hisab")
            lines.forEach { appendLine(it) }
            appendLine("Mot baki: ৳$totalBalance")
        }
}
