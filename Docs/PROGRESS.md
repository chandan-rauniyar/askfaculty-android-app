# AskFaculty Android — Progress

Update this file after every step. A new session or a different AI tool must be able to continue from here alone.

## Current step
A1 — completed

## Done (with date and what was verified)
- A1 completed: Implemented Login screen (`LoginScreen`), `AuthApiService` (`POST /auth/login`, `GET /auth/me`), `AuthRepository`, `LoginViewModel` with specific error handling (`INVALID_CREDENTIALS` mapped to generic sign-in failure, `ACCOUNT_INACTIVE` mapped to college support contact message), and role-based home shell (`HomeShell` for `STUDENT`, `FACULTY`, `COLLEGE_ADMIN`). Added unit tests (`LoginViewModelTest`), passed all unit tests (11 passed), and tested successfully on the emulator against the local backend using seed users.

## Step checklist
- [x] S0.1 Project, dependencies, build types, base URL config
- [x] S0.2 DI, navigation, theme, API client, error mapping, health check
- [x] S0.3 Secure token storage and SessionManager
- [x] A1 Login and role-based home shell
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
- **Login error handling:** Mapped `INVALID_CREDENTIALS` to a generic failure message ("Invalid email or password.") and `ACCOUNT_INACTIVE` to college support instructions ("Your account is inactive. Please contact college support."), ensuring passwords are never stored in memory or logged.
- **Role-based navigation:** Observed `SessionManager.session` flow in `MainActivity` to switch seamlessly between `LoginScreen` and `HomeShell`.

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
- `app/src/main/java/com/chandan/askfaculty/data/remote/model/LoginRequest.kt`: Login request and auth session models
- `app/src/main/java/com/chandan/askfaculty/data/remote/AuthApiService.kt`: Auth REST endpoints (`/auth/login`, `/auth/me`)
- `app/src/main/java/com/chandan/askfaculty/data/repository/AuthRepository.kt`: Auth repository for sign-in and profile fetch
- `app/src/main/java/com/chandan/askfaculty/ui/auth/LoginViewModel.kt`: Login ViewModel with contract-compliant error mapping
- `app/src/main/java/com/chandan/askfaculty/ui/auth/LoginScreen.kt`: Material 3 login screen
- `app/src/main/java/com/chandan/askfaculty/ui/home/HomeShell.kt`: Role-based landing shell with logout action
- `app/src/main/java/com/chandan/askfaculty/MainActivity.kt`: Root activity observing session flow
- `app/test/java/com/chandan/askfaculty/ui/auth/LoginViewModelTest.kt`: Unit tests for login success, invalid credentials, and inactive account states

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
