# Hisab App — হিসাব অ্যাপ

A TallyKhata / Behishab style personal + business finance app.
Single-file web app, works 100% offline. No server, no internet needed.
Data is saved on the phone itself (localStorage).

TallyKhata / Behishab er moto ay-bay, doinik ay, baki khata ar sonchoy er app.
Ekta file ei sob cholbe, internet lagbe na. Data phone ei save thakbe.

## Files / File gulo

- `index.html` — the full app (HTML + CSS + JS in one file, ~37 KB)
- `android-webview/MainActivity.kt` — Android WebView wrapper example
- `README.md` — this guide

## Features / Ki ki ache

1. **Login / Signup (লগইন)**
   - Sign up with name, Gmail address, password.
   - Login with Gmail + password. Password is required — no passwordless login.
   - One login stays for **90 days**. After that, login again.
   - Logout button in the header.
   - Per-user data: data is saved separately for each Gmail, so a second
     account sees empty data. Data never leaves the phone.
   - Note: this is **local login only, not true Google OAuth**. There is no
     Google sign-in button and no server checks your Gmail. Anyone with the
     phone can create an account, so keep the phone locked.
2. **Dashboard (হোম)** — total balance, total income, total expense,
   today's income/expense, personal vs business totals, daily due, khata due.
3. **Lenden / Transactions (আয়-ব্যয়)** — add income/expense in Taka,
   category, date, note, account (Personal / Business), payment method
   (Cash / bKash / Nagad / Bank). Edit, delete, filter by month / category /
   account, search by note or category.
4. **Doinik Ay / Daily Income (দৈনিক আয়)** — add a company/job, then per day
   enter earned amount and received amount. Due is auto-calculated:
   `due = earned - received`. Example: earned 100, received 50 → due 50.
   Per-company totals and per-day history.
5. **Baki Khata (বাকি খাতা)** — add party/customer, entries
   "Baki dilen / Taka pelen". Shows "Apni paben" vs "Apni diben"
   per party and in total.
6. **Sonchoy / Savings (সঞ্চয়)** — create a savings goal with a target,
   add deposit / withdraw, progress bar and remaining amount.
7. **Report (রিপোর্ট)** — monthly category-wise income/expense summary.

## How to use on a phone / Phone e kivabe use korbo

### Option A — Direct (sohoj, kono build lagbe na)
1. Copy `index.html` to your phone.
2. Open it in Chrome / any browser. It works offline.
3. Sign up, then use the app. Data stays in that browser.

### Option B — Installable APK with Android Studio (WebView wrapper)
Eta diye phone e install kora jay emon app banano jabe.

1. Android Studio te ekta **Empty Views Activity** project khulun.
   Package name: `com.tanjid.hisabapp` (MainActivity.kt er sathe miliye nin).
2. Copy the app file:
   - Copy `index.html` to `app/src/main/assets/index.html`
   - Jodi `assets` folder na thake, `app/src/main/` er vitore `assets` nam e
     folder baniye tar vitore rakhun.
3. Copy the wrapper:
   - Copy `android-webview/MainActivity.kt` to
     `app/src/main/java/com/tanjid/hisabapp/MainActivity.kt`
   - Purono `MainActivity.kt` replace kore din.
4. `AndroidManifest.xml` e ei Activity ta LAUNCHER hisabe thakte hobe
   (Empty Views Activity project e emnitei thake).
5. **Internet permission lagbe na** — app ta offline cholbe.
   WebView e JavaScript ar DOM storage on kora ache MainActivity.kt te.
6. Android Studio theke **Run** korun ba **Build > Generate Signed App Bundle / APK**
   diye APK baniye phone e install korun.

### How login works / Login kivabe kaj kore
- Prothom bar **Sign Up** korun: nam, Gmail, password din.
- Tarpor theke oi Gmail + password diye **Login** korun.
- Ekbar login korle 90 din login thakbe, bar bar login korte hobe na.
- Logout korte chaile upore **লগআউট** button chapben.
- Notun Gmail diye sign up korle tar data khali thakbe — prottek user er
  data alada alada save hoy.

## Limitations / Simaboddota
- No true Google OAuth / Gmail login — password diye local login.
- Data only on that phone / browser. Phone change korle data jabe na,
  backup er kono server nei.
- Password is stored as a simple hash on the device only — eta bank-level
  security na. Phone lock kore rakhun.
- No cloud sync, no multi-device sync.
