package com.tanjid.hisabapp.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.tanjid.hisabapp.viewmodel.HisabViewModel

/**
 * Login / Sign up / Forgot password.
 * Sign up and forgot password both use a 6-digit Gmail code (10 min expiry).
 * Real sending is wired in EmailHelper — see README.md.
 */
@Composable
fun AuthScreen(vm: HisabViewModel) {
    var mode by remember { mutableStateOf("login") } // login | signup | forgot
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var code by remember { mutableStateOf("") }
    var codeSent by remember { mutableStateOf(false) }
    var newPassword by remember { mutableStateOf("") }

    val error by vm.authError.collectAsState()
    val busy by vm.authBusy.collectAsState()
    val codeStatus by vm.codeStatus.collectAsState()

    Column(Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.Center) {
        Text("Hisab App", style = MaterialTheme.typography.headlineLarge, color = HisabRed)
        Text("আয়-ব্যয়, দৈনিক আয়, বাকি খাতা, সঞ্চয়", style = MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.height(24.dp))

        if (mode == "login") {
            OutlinedTextField(email, { email = it }, label = { Text("Gmail") }, modifier = Modifier.fillMaxWidth(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email))
            OutlinedTextField(password, { password = it }, label = { Text("Password") }, visualTransformation = PasswordVisualTransformation(), modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(12.dp))
            Button({ vm.login(email.trim(), password) }, modifier = Modifier.fillMaxWidth(), enabled = !busy) { Text("লগইন") }
            TextButton({ mode = "forgot" }) { Text("Password ভুলে গেছেন?") }
            TextButton({ mode = "signup" }) { Text("নতুন অ্যাকাউন্ট খুলুন") }
        }

        if (mode == "signup") {
            OutlinedTextField(name, { name = it }, label = { Text("নাম") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(email, { email = it }, label = { Text("Gmail") }, modifier = Modifier.fillMaxWidth(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email))
            OutlinedTextField(password, { password = it }, label = { Text("Password") }, visualTransformation = PasswordVisualTransformation(), modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(12.dp))
            if (!codeSent) {
                Button({ vm.requestSignupCode(name, email.trim(), password); codeSent = true }, modifier = Modifier.fillMaxWidth(), enabled = !busy) { Text("Gmail এ কোড পাঠান") }
            } else {
                Text("কোডের অবস্থা: ${codeStatus ?: "পাঠানো হচ্ছে..."} — শুধু সবশেষ কোড কাজ করবে, মেয়াদ ১০ মিনিট।")
                OutlinedTextField(code, { code = it }, label = { Text("৬ সংখ্যার কোড") }, modifier = Modifier.fillMaxWidth(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                Button({
                    if (vm.verifySignupCode(code.trim())) { /* ViewModel logs in on success */ }
                }, modifier = Modifier.fillMaxWidth()) { Text("কোড দিয়ে অ্যাকাউন্ট খুলুন") }
            }
            TextButton({ mode = "login"; codeSent = false }) { Text("লগইনে ফিরুন") }
        }

        if (mode == "forgot") {
            OutlinedTextField(email, { email = it }, label = { Text("অ্যাকাউন্টের Gmail") }, modifier = Modifier.fillMaxWidth(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email))
            Spacer(Modifier.height(12.dp))
            if (!codeSent) {
                Button({ vm.requestForgotCode(email.trim()); codeSent = true }, modifier = Modifier.fillMaxWidth()) { Text("কোড পাঠান") }
            } else {
                Text("কোডের অবস্থা: ${codeStatus ?: "পাঠানো হচ্ছে..."}")
                OutlinedTextField(code, { code = it }, label = { Text("৬ সংখ্যার কোড") }, modifier = Modifier.fillMaxWidth(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                OutlinedTextField(newPassword, { newPassword = it }, label = { Text("নতুন Password") }, visualTransformation = PasswordVisualTransformation(), modifier = Modifier.fillMaxWidth())
                Button({ vm.verifyForgotCode(code.trim(), newPassword) }, modifier = Modifier.fillMaxWidth()) { Text("নতুন Password সেট করুন") }
            }
            TextButton({ mode = "login"; codeSent = false }) { Text("লগইনে ফিরুন") }
        }

        error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
    }
}
