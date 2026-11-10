# AskFaculty Android — Progress

Update this file after every step. A new session or a different AI tool must be able to continue from here alone.

## Current step
S0.3 — completed

## Done (with date and what was verified)
- S0.3 completed: Implemented secure token storage (`TokenStorage`) using `EncryptedSharedPreferences` backed by Android Keystore (AES-256 GCM / AES-256 SIV), created `SessionManager` exposing session state as a `StateFlow`, excluded token shared preferences (`secure_tokens_prefs.xml`) from Android backups (`backup_rules.xml` and `data_extraction_rules.xml`), added unit tests (`SessionManagerTest`), passed all unit tests (8 passed), and verified successful build and test execution.

## Step checklist
- [x] S0.1 Project, dependencies, build types, base URL config
- [x] S0.2 DI, navigation, theme, API client, error mapping, health check
- [x] S0.3 Secure token storage and SessionManager
- [ ] A1 Login and role-based home shell
- [ ] A2 Auto refresh, logout, session restore
- [ ] A3 Activation, forgot/reset password
- [ ] G1 Group list with unread badges
- [ ] G2 Group detail tabs and members
- [ ] N1 Announcements
- [ ] N2 Notes list and download
- [ ] N3 Faculty note upload
- [ ] D1 Group doubts list and ask
- [ ] D2 Doubt thread, replies, accept, resolve/reopen
- [ ] D3 Private doubts
- [ ] D4 Faculty dashboard and inbox
- [ ] D5 Doubt attachments
- [ ] F1 Notifications
- [ ] C1 Chat history and REST send with outbox
- [ ] C2 Socket.IO live chat and reconnect catch-up
- [ ] C3 Chat read state, typing, edit/delete, reports, mutes
- [ ] M1 Admin users
- [ ] M2 Admin structure
- [ ] M3 Admin enrollments, assignments, groups
- [ ] P1 Profile and change password
- [ ] P2 Report content and block users
- [ ] P3 Account deletion
- [ ] P4 Release hardening
- [ ] P5 Closed test and production application

## Decisions made
- **Token storage approach:** `EncryptedSharedPreferences` from `androidx.security:security-crypto` backed by Android Keystore (AES-256 GCM for values, AES-256 SIV for keys). Chosen because it provides hardware-backed secure storage for sensitive auth tokens without using deprecated methods, adhering strictly to Android security best practices.
- Excluded `secure_tokens_prefs.xml` from full-backup (`backup_rules.xml`) and data-extraction (`data_extraction_rules.xml`) to prevent tokens from leaking via device backups or cloud sync.
- Exposed session state as a reactive `StateFlow<Session>` in `SessionManager`.

## Tech setup
- Package name: `com.chandan.askfaculty`
- minSdk / targetSdk / compileSdk: 26 / 35 / 35
- Kotlin / AGP / Compose BOM versions: Kotlin 2.1.0 / AGP 8.8.0 / Compose BOM 2025.01.00
- Token storage approach: `EncryptedSharedPreferences` via Android Keystore
- Debug base URL: `http://10.0.2.2:5000` (emulator) / overridable via `askfaculty.debug.baseUrl` for physical phone.

## How to run
1. Start the backend (see backend repo): `docker compose up -d`, `npm run dev`
2. Open the project in Android Studio, run the `debug` build on an emulator
3. Test accounts: see backend README (seed users)

## File map
- `app/src/main/res/xml/backup_rules.xml`: Excludes `secure_tokens_prefs.xml` from full backup
- `app/src/main/res/xml/data_extraction_rules.xml`: Excludes `secure_tokens_prefs.xml` from cloud backup and device transfer
- `app/src/main/java/com/chandan/askfaculty/data/local/TokenStorage.kt`: Encrypted token storage using Android Keystore
- `app/src/main/java/com/chandan/askfaculty/data/local/SessionManager.kt`: Reactive session manager exposing state flow
- `app/src/main/java/com/chandan/askfaculty/data/remote/model/User.kt`: User model
- `app/test/java/com/chandan/askfaculty/data/local/SessionManagerTest.kt`: Unit tests for session management and token operations

## Backend requests / contract gaps
(none currently)

## Known issues
(none)

## Play Store tracker
- Developer account type and creation date:
- Privacy policy URL:
- Account deletion web URL:
- Closed test started on / tester count:
- Production access applied on / result:
