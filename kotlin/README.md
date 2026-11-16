# Hisab App — Kotlin Code Only

Native Android app in Kotlin, Jetpack Compose + Material 3.
TallyKhata style, Bengali UI, red theme #C62828.
Package: `com.tanjid.hisabapp` — App name: Hisab App.

## What is included

6 sections only — nothing extra, no QR Code, no Data Backup icon:
1. **Home** — total balance, income, expense, today's income/expense, personal vs business.
2. **Ay-Bay / Lenden** — income/expense, category, date, note, Personal/Business, Cash/bKash/Nagad/Bank.
3. **Doinik Ay** — per company per day: earned vs received, due = earned − received.
   Example: earned 100, company gave 50 → due 50.
   Payment-only entry (earned = 0, received = amount) minuses previous due.
4. **Baki Khata** — party/customer with Gmail, pawna/dena, per-party balance.
   When a Gmail is saved, a message text is generated stating pawna/dena amount.
5. **Sonchoy** — savings goals, target, deposit/withdraw, progress.
6. **Report** — monthly category-wise income/expense.

Auth (required before entering the app):
- Sign up: name, Gmail, password → 6-digit Gmail verification code (10 min expiry).
  `EmailHelper` has the placeholder where your real email-sending backend goes.
- Login: Gmail + password. Session persists 90 days.
- Forgot password: Gmail → 6-digit code → new password.
- Data is isolated per user (every table has `ownerEmail`).

## How to open in Android Studio

1. Android Studio → Open → select the `hisab-app-kotlin-code` folder.
2. Let Gradle sync finish (needs internet once for dependencies).
3. Run on a phone or emulator.
4. APK: Build → Build App Bundle(s) / APK(s) → Build APK(s).

## Project structure

```
settings.gradle.kts
build.gradle.kts
gradle.properties
app/build.gradle.kts
app/src/main/AndroidManifest.xml
app/src/main/java/com/tanjid/hisabapp/MainActivity.kt
app/src/main/java/com/tanjid/hisabapp/data/Database.kt
app/src/main/java/com/tanjid/hisabapp/data/Repository.kt
app/src/main/java/com/tanjid/hisabapp/ui/Theme.kt
app/src/main/java/com/tanjid/hisabapp/ui/Auth.kt
app/src/main/java/com/tanjid/hisabapp/ui/Screens.kt
app/src/main/java/com/tanjid/hisabapp/viewmodel/ViewModel.kt
app/src/main/java/com/tanjid/hisabapp/util/EmailHelper.kt
```

## Limitation — read this

- This is **code only**. No APK is built here.
- Gmail code sending is a **placeholder** in `EmailHelper.sendVerificationCode()`.
  A real Gmail code needs a backend (your own server / Firebase).
  The app generates a 6-digit code locally and shows the flow; wire
  `EmailHelper` to your backend to send it for real.
- Data is stored locally with Room. No cloud sync / backup is included.
