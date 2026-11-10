# AskFaculty Android — Progress

Update this file after every step. A new session or a different AI tool must be able to continue from here alone.

## Current step
S0.2 — completed

## Done (with date and what was verified)
- S0.2 completed: Implemented Hilt DI, Navigation Compose, Retrofit + OkHttp with debug HTTP logging (headers with redacted auth), kotlinx.serialization, centralized response envelope parser (`unwrap()`), robust `AppError` mapping conforming to API contract section 2, and debug-only Health Screen calling `GET /health/ready`. Added `android.permission.INTERNET` to `AndroidManifest.xml` to resolve `EPERM (Operation not permitted)` socket exception. Verified app launch stability and successful backend health checks (`Backend: ok`, `- postgres: up`, `- storage: up`) on emulator (`emulator-5554`) when backend is running, and graceful offline handling (`Backend: offline`, "Cannot reach the server") when backend is stopped. Passed all unit tests (`ApiEnvelopeTest`).

## Step checklist
- [x] S0.1 Project, dependencies, build types, base URL config
- [x] S0.2 DI, navigation, theme, API client, error mapping, health check
- [ ] S0.3 Secure token storage and SessionManager
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
- Configured Hilt DI with `@HiltAndroidApp` application and `@AndroidEntryPoint` MainActivity.
- Created root and API Retrofit instances using `@Named("root")` and `@Named("api")` qualifiers to resolve Dagger duplicate binding issues.
- Added `android.permission.INTERNET` to `AndroidManifest.xml` to permit network operations.
- Made the health check lazy/on-demand in `HealthViewModel` so the app opens successfully even when backend is offline.
- Mapped all API contract error codes to a typed `AppError` hierarchy (`NetworkError`, `ParseError`, `ValidationError`, `NotReady`, `Unauthenticated`, etc.).

## Tech setup
- Package name: `com.chandan.askfaculty`
- minSdk / targetSdk / compileSdk: 26 / 35 / 35
- Kotlin / AGP / Compose BOM versions: Kotlin 2.1.0 / AGP 8.8.0 / Compose BOM 2025.01.00
- Token storage approach: Encrypted SharedPreferences / Android Keystore (to be implemented in S0.3)
- Debug base URL: `http://10.0.2.2:5000` (emulator) / overridable via `askfaculty.debug.baseUrl` for physical phone.

## How to run
1. Start the backend (see backend repo): `docker compose up -d`, `npm run dev`
2. Open the project in Android Studio, run the `debug` build on an emulator
3. Test accounts: see backend README (seed users)

## File map
- `app/src/main/AndroidManifest.xml`: Android manifest with `INTERNET` permission and Hilt Application registration
- `app/src/main/java/com/chandan/askfaculty/AskFacultyApp.kt`: Hilt `@HiltAndroidApp` Application class
- `app/src/main/java/com/chandan/askfaculty/data/remote/model/ApiResponse.kt`: Standard response envelope model
- `app/src/main/java/com/chandan/askfaculty/data/remote/model/HealthResponse.kt`: Health response model
- `app/src/main/java/com/chandan/askfaculty/data/remote/ApiError.kt`: `AppError` mapping and envelope `unwrap()` extension
- `app/src/main/java/com/chandan/askfaculty/data/remote/ApiService.kt`: REST API service
- `app/src/main/java/com/chandan/askfaculty/data/remote/HealthApiService.kt`: Health readiness service (`GET health/ready`)
- `app/src/main/java/com/chandan/askfaculty/di/NetworkModule.kt`: Hilt network module providing OkHttpClient, Retrofit instances, and services
- `app/src/main/java/com/chandan/askfaculty/ui/health/HealthViewModel.kt`: ViewModel handling health check with error handling
- `app/src/main/java/com/chandan/askfaculty/ui/health/HealthScreen.kt`: Health status UI screen with retry support
- `app/test/java/com/chandan/askfaculty/data/remote/ApiEnvelopeTest.kt`: Unit tests for envelope parsing, error mapping, and network/parse exceptions

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
