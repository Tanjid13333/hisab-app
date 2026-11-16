package com.tanjid.hisabapp.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.tanjid.hisabapp.data.*
import com.tanjid.hisabapp.util.EmailHelper
import com.tanjid.hisabapp.viewmodel.HisabViewModel
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch

// ---------- Shared small UI ----------

@Composable
fun RedHeader(title: String, userName: String?, onLogout: () -> Unit) {
    Surface(color = HisabRed, modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("ONLINE BUSINESS", color = MaterialTheme.colorScheme.onPrimary, style = MaterialTheme.typography.labelLarge)
                TextButton(onClick = onLogout) { Text("লগআউট", color = MaterialTheme.colorScheme.onPrimary) }
            }
            Text(title, color = MaterialTheme.colorScheme.onPrimary, style = MaterialTheme.typography.headlineMedium)
            if (userName != null) Text(userName, color = MaterialTheme.colorScheme.onPrimary)
        }
    }
}

@Composable
fun AmountField(value: String, onChange: (String) -> Unit, label: String) {
    OutlinedTextField(value, onChange, label = { Text(label) }, modifier = Modifier.fillMaxWidth(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
}

// ---------- 1. Home ----------

@Composable
fun HomeScreen(vm: HisabViewModel, modifier: Modifier = Modifier) {
    val user by vm.currentUser.collectAsState()
    val txs by vm.transactions().collectAsState(initial = emptyList())
    var totals by remember { mutableStateOf(0L to 0L) }
    LaunchedEffect(user) { totals = vm.totals() }
    val (income, expense) = totals
    val today = todayString()
    val todayIncome = txs.filter { it.date == today && it.type == "INCOME" }.sumOf { it.amount }
    val todayExpense = txs.filter { it.date == today && it.type == "EXPENSE" }.sumOf { it.amount }

    LazyColumn(modifier) {
        item {
            RedHeader("মোট হিসাব", user?.name) { vm.logout() }
            Card(Modifier.fillMaxWidth().padding(16.dp)) {
                Column(Modifier.padding(16.dp)) {
                    Text("মোট ব্যালেন্স", style = MaterialTheme.typography.labelLarge)
                    Text(taka(income - expense), style = MaterialTheme.typography.headlineLarge, color = HisabRed)
                    Text("আয়: ${taka(income)}   ব্যয়: ${taka(expense)}")
                    Text("আজকের আয়: ${taka(todayIncome)}   আজকের ব্যয়: ${taka(todayExpense)}")
                    Text("ব্যক্তিগত: ${taka(txs.filter { it.account == "Personal" && it.type == "INCOME" }.sumOf { it.amount } - txs.filter { it.account == "Personal" && it.type == "EXPENSE" }.sumOf { it.amount })}   ব্যবসা: ${taka(txs.filter { it.account == "Business" && it.type == "INCOME" }.sumOf { it.amount } - txs.filter { it.account == "Business" && it.type == "EXPENSE" }.sumOf { it.amount })}")
                }
            }
            Text("সাম্প্রতিক লেনদেন", Modifier.padding(horizontal = 16.dp), style = MaterialTheme.typography.titleMedium)
        }
        items(txs.take(20)) { tx ->
            ListItem(
                headlineContent = { Text("${tx.category} — ${taka(tx.amount)}") },
                supportingContent = { Text("${tx.date} • ${tx.account} • ${tx.paymentMethod} ${tx.note}") },
                trailingContent = { Text(if (tx.type == "INCOME") "+" else "−", color = if (tx.type == "INCOME") HisabGreen else HisabRed) }
            )
        }
    }
}

// ---------- 2. Ay-Bay / Lenden ----------

@Composable
fun TransactionScreen(vm: HisabViewModel, modifier: Modifier = Modifier) {
    val txs by vm.transactions().collectAsState(initial = emptyList())
    var amount by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("বিক্রি") }
    var isIncome by remember { mutableStateOf(true) }
    var note by remember { mutableStateOf("") }
    var query by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    LazyColumn(modifier.padding(16.dp)) {
        item {
            Text("আয়-ব্যয়", style = MaterialTheme.typography.headlineMedium, color = HisabRed)
            Row { FilterChip(isIncome, { isIncome = true }, { Text("আয়") }); Spacer(Modifier.width(8.dp)); FilterChip(!isIncome, { isIncome = false }, { Text("ব্যয়") }) }
            AmountField(amount, { amount = it }, "টাকা")
            OutlinedTextField(category, { category = it }, label = { Text("ক্যাটাগরি") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(note, { note = it }, label = { Text("নোট") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(query, { query = it }, label = { Text("খোঁজ") }, modifier = Modifier.fillMaxWidth())
            Button({
                val a = amount.toLongOrNull() ?: return@Button
                scope.launch {
                    vm.addTransaction(Transaction(ownerEmail = "", type = if (isIncome) "INCOME" else "EXPENSE", amount = a, category = category, date = todayString(), note = note))
                    amount = ""; note = ""
                }
            }, modifier = Modifier.fillMaxWidth()) { Text("যোগ করুন") }
            Spacer(Modifier.height(12.dp))
        }
        val filtered = txs.filter { query.isBlank() || it.category.contains(query) || it.note.contains(query) }
        items(filtered) { tx ->
            ListItem(
                headlineContent = { Text("${tx.category} — ${taka(tx.amount)}") },
                supportingContent = { Text("${tx.date} • ${tx.account} • ${tx.paymentMethod}") },
                trailingContent = { TextButton({ scope.launch { vm.deleteTransaction(tx) } }) { Text("মুছুন") } }
            )
        }
    }
}

// ---------- 3. Doinik Ay ----------

@Composable
fun DailyIncomeScreen(vm: HisabViewModel, modifier: Modifier = Modifier) {
    val jobs by vm.dailyJobs().collectAsState(initial = emptyList())
    var company by remember { mutableStateOf("") }
    var companyEmail by remember { mutableStateOf("") }
    var selectedJob by remember { mutableStateOf<DailyJob?>(null) }
    var earned by remember { mutableStateOf("") }
    var received by remember { mutableStateOf("") }
    var due by remember { mutableStateOf<Long?>(null) }
    val scope = rememberCoroutineScope()
    val entries by (selectedJob?.let { vm.dailyEntries(it.id) } ?: flowOf(emptyList<DailyEntry>())).collectAsState(initial = emptyList())

    LazyColumn(modifier.padding(16.dp)) {
        item {
            Text("দৈনিক আয়", style = MaterialTheme.typography.headlineMedium, color = HisabRed)
            Text("আয় ১০০, কোম্পানি দিল ৫০ → বাকি ৫০। আয় ০ রেখে শুধু পাওয়া টাকা লিখলে আগের বাকি থেকে মাইনাস হবে।")
            OutlinedTextField(company, { company = it }, label = { Text("কোম্পানি / কাজের নাম") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(companyEmail, { companyEmail = it }, label = { Text("কোম্পানির Gmail (ঐচ্ছিক)") }, modifier = Modifier.fillMaxWidth())
            Button({ scope.launch { vm.addDailyJob(company, companyEmail); company = ""; companyEmail = "" } }, modifier = Modifier.fillMaxWidth()) { Text("কোম্পানি যোগ করুন") }
            Spacer(Modifier.height(12.dp))
        }
        items(jobs) { job ->
            Card(Modifier.fillMaxWidth().padding(vertical = 4.dp), onClick = {
                selectedJob = job
                scope.launch { due = vm.dailyDue(job.id) }
            }) {
                Column(Modifier.padding(12.dp)) {
                    Text(job.companyName, style = MaterialTheme.typography.titleMedium)
                    Text("Gmail: ${job.companyEmail.ifBlank { "দেওয়া হয়নি" }}")
                    Text("মোট বাকি: ${taka(due ?: 0)}", color = HisabRed)
                }
            }
        }
        selectedJob?.let { job ->
            item {
                Text("দিনের হিসাব — ${job.companyName}", style = MaterialTheme.typography.titleMedium)
                AmountField(earned, { earned = it }, "আয় করলেন (টাকা)")
                AmountField(received, { received = it }, "কোম্পানি দিল (টাকা)")
                val e = earned.toLongOrNull() ?: 0L
                val r = received.toLongOrNull() ?: 0L
                Text("আজকের বাকি: ${taka(e - r)}  •  আগের বাকি ছিল: ${taka(due ?: 0)}")
                Row {
                    Button({ scope.launch { vm.addDailyEntry(job.id, todayString(), e, r); earned = ""; received = ""; due = vm.dailyDue(job.id) } }) { Text("সেভ করুন") }
                    Spacer(Modifier.width(8.dp))
                    OutlinedButton({ received = (due ?: 0).toString() }) { Text("পুরো বাকি বসান") }
                }
                Text("ইতিহাস", style = MaterialTheme.typography.titleMedium)
            }
            items(entries) { en ->
                val isPaymentOnly = en.earned == 0L
                ListItem(
                    headlineContent = { Text("${en.date} — আয় ${taka(en.earned)}, পেলেন ${taka(en.received)}") },
                    supportingContent = { Text(if (isPaymentOnly) "আগের বাকি থেকে পরিশোধ পেয়েছেন • বাকি কমল" else "বাকি: ${taka(en.earned - en.received)}") }
                )
            }
        }
    }
}

// ---------- 4. Baki Khata ----------

@Composable
fun KhataScreen(vm: HisabViewModel, modifier: Modifier = Modifier) {
    val parties by vm.parties().collectAsState(initial = emptyList())
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var selected by remember { mutableStateOf<Party?>(null) }
    var amount by remember { mutableStateOf("") }
    var isGave by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()
    val entries by (selected?.let { vm.khataEntries(it.id) } ?: flowOf(emptyList<KhataEntry>())).collectAsState(initial = emptyList())
    val balance = entries.sumOf { if (it.type == "GAVE") it.amount else -it.amount }

    LazyColumn(modifier.padding(16.dp)) {
        item {
            Text("বাকি খাতা", style = MaterialTheme.typography.headlineMedium, color = HisabRed)
            Text("মোট পাবো / মোট দেবো — Gmail দিলে পাওনা/দেনার মেসেজ যাবে।")
            OutlinedTextField(name, { name = it }, label = { Text("নাম") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(phone, { phone = it }, label = { Text("ফোন") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(email, { email = it }, label = { Text("Gmail (ঐচ্ছিক)") }, modifier = Modifier.fillMaxWidth())
            Button({ scope.launch { vm.addParty(name, phone, email); name = ""; phone = ""; email = "" } }, modifier = Modifier.fillMaxWidth()) { Text("পার্টি যোগ করুন") }
        }
        items(parties) { p ->
            ListItem(headlineContent = { Text(p.name) }, supportingContent = { Text(p.email.ifBlank { "Gmail দেওয়া হয়নি" }) },
                modifier = Modifier, trailingContent = { TextButton({ selected = p }) { Text("খুলুন") } })
        }
        selected?.let { p ->
            item {
                Text("${p.name} — ${if (balance >= 0) "আপনি পাবেন ${taka(balance)}" else "আপনি দেবেন ${taka(-balance)}"}", style = MaterialTheme.typography.titleMedium)
                if (p.email.isBlank()) Text("লাল সতর্কতা: Gmail সেভ নেই — কোনো ইমেইল যাবে না।", color = HisabRed)
                Row { FilterChip(isGave, { isGave = true }, { Text("বাকি দিলেন") }); Spacer(Modifier.width(8.dp)); FilterChip(!isGave, { isGave = false }, { Text("টাকা পেলেন") }) }
                AmountField(amount, { amount = it }, "টাকা")
                Button({
                    val a = amount.toLongOrNull() ?: return@Button
                    scope.launch {
                        vm.addKhataEntry(p.id, todayString(), if (isGave) "GAVE" else "GOT", a)
                        if (p.email.isNotBlank()) {
                            // Message text is generated here; real send is via EmailHelper backend.
                            EmailHelper.partyMessage(p.name, a, if (isGave) "PAONA" else "DENA", balance, "Hisab App")
                        }
                        amount = ""
                    }
                }, modifier = Modifier.fillMaxWidth()) { Text("সেভ করুন") }
                Text("ইতিহাস", style = MaterialTheme.typography.titleMedium)
            }
            items(entries) { e ->
                ListItem(headlineContent = { Text("${e.date} — ${if (e.type == "GAVE") "বাকি দিলেন" else "টাকা পেলেন"} ${taka(e.amount)}") })
            }
        }
    }
}

// ---------- 5. Sonchoy ----------

@Composable
fun SavingsScreen(vm: HisabViewModel, modifier: Modifier = Modifier) {
    val goals by vm.goals().collectAsState(initial = emptyList())
    var title by remember { mutableStateOf("") }
    var target by remember { mutableStateOf("") }
    var selected by remember { mutableStateOf<SavingsGoal?>(null) }
    var amount by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    val entries by (selected?.let { vm.savingsEntries(it.id) } ?: flowOf(emptyList<SavingsEntry>())).collectAsState(initial = emptyList())
    val saved = entries.sumOf { if (it.type == "DEPOSIT") it.amount else -it.amount }

    LazyColumn(modifier.padding(16.dp)) {
        item {
            Text("সঞ্চয়", style = MaterialTheme.typography.headlineMedium, color = HisabRed)
            OutlinedTextField(title, { title = it }, label = { Text("লক্ষ্যের নাম") }, modifier = Modifier.fillMaxWidth())
            AmountField(target, { target = it }, "লক্ষ্য (টাকা)")
            Button({ val t = target.toLongOrNull() ?: return@Button; scope.launch { vm.addGoal(title, t); title = ""; target = "" } }, modifier = Modifier.fillMaxWidth()) { Text("লক্ষ্য তৈরি করুন") }
        }
        items(goals) { g ->
            Card(Modifier.fillMaxWidth().padding(vertical = 4.dp), onClick = { selected = g }) {
                Column(Modifier.padding(12.dp)) { Text(g.title); Text("লক্ষ্য: ${taka(g.target)}") }
            }
        }
        selected?.let { g ->
            item {
                Text("${g.title} — জমা: ${taka(saved)} / ${taka(g.target)}", style = MaterialTheme.typography.titleMedium)
                LinearProgressIndicator(progress = { (saved.toFloat() / g.target.coerceAtLeast(1).toFloat()).coerceIn(0f, 1f) }, modifier = Modifier.fillMaxWidth())
                AmountField(amount, { amount = it }, "টাকা")
                Row {
                    Button({ val a = amount.toLongOrNull() ?: return@Button; scope.launch { vm.addSavingsEntry(g.id, todayString(), "DEPOSIT", a); amount = "" } }) { Text("জমা") }
                    Spacer(Modifier.width(8.dp))
                    OutlinedButton({ val a = amount.toLongOrNull() ?: return@OutlinedButton; if (a <= saved) scope.launch { vm.addSavingsEntry(g.id, todayString(), "WITHDRAW", a); amount = "" } }) { Text("তোলা") }
                }
            }
            items(entries) { e -> ListItem(headlineContent = { Text("${e.date} — ${if (e.type == "DEPOSIT") "জমা" else "তোলা"} ${taka(e.amount)}") }) }
        }
    }
}

// ---------- 6. Report ----------

@Composable
fun ReportScreen(vm: HisabViewModel, modifier: Modifier = Modifier) {
    val txs by vm.transactions().collectAsState(initial = emptyList())
    var month by remember { mutableStateOf(monthString()) }
    val monthTxs = txs.filter { it.date.startsWith(month) }
    val byCategory = monthTxs.groupBy { it.category to it.type }.mapValues { it.value.sumOf { t -> t.amount } }

    LazyColumn(modifier.padding(16.dp)) {
        item {
            Text("রিপোর্ট", style = MaterialTheme.typography.headlineMedium, color = HisabRed)
            OutlinedTextField(month, { month = it }, label = { Text("মাস (yyyy-MM)") }, modifier = Modifier.fillMaxWidth())
            Text("মোট আয়: ${taka(monthTxs.filter { it.type == "INCOME" }.sumOf { it.amount })}")
            Text("মোট ব্যয়: ${taka(monthTxs.filter { it.type == "EXPENSE" }.sumOf { it.amount })}")
        }
        items(byCategory.entries.toList()) { (key, total) ->
            ListItem(headlineContent = { Text("${key.first} — ${if (key.second == "INCOME") "আয়" else "ব্যয়"}") }, trailingContent = { Text(taka(total)) })
        }
    }
}
