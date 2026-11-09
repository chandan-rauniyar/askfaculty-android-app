# AskFaculty Android — Progress

Update this file after every step. A new session or a different AI tool must be able to continue from here alone.

## Current step
S0.1 — completed

## Done (with date and what was verified)
- S0.1 completed: Renamed package to `com.chandan.askfaculty`, configured Gradle property-based `API_BASE_URL` for debug and release (with release build validation failure if property is missing), added debug-only network security config permitting cleartext, built debug and release APKs successfully, passed unit tests (`app:testDebugUnitTest`), and verified app running on emulator (`emulator-5554`).

## Step checklist
- [x] S0.1 Project, dependencies, build types, base URL config
- [ ] S0.2 DI, navigation, theme, API client, error mapping, health check
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
- Renamed package name, applicationId, and namespace from `com.example.askfaculty` to `com.chandan.askfaculty`.
- Placed debug network security config and `usesCleartextTraffic="true"` strictly in `app/src/debug` allowing cleartext for all hosts (supporting emulator `10.0.2.2` and physical devices on LAN IP); release build has no cleartext rule and no `usesCleartextTraffic`.
- Configured `API_BASE_URL` via Gradle properties (`askfaculty.debug.baseUrl` defaulting to `http://10.0.2.2:5000/api/v1`, and `askfaculty.release.baseUrl`) with a build check ensuring release builds fail with a clear message if the release property is missing.

## Tech setup
- Package name: `com.chandan.askfaculty`
- minSdk / targetSdk / compileSdk: 26 / 37 / 37
- Kotlin / AGP / Compose BOM versions: Kotlin 2.2.10 / AGP 9.4.1 / Compose BOM 2026.02.01
- Token storage approach: Encrypted SharedPreferences / Android Keystore (to be implemented in S0.3)
- Debug base URL: `http://10.0.2.2:5000/api/v1` (emulator) / overridable via `askfaculty.debug.baseUrl` for physical phone.

## How to run
1. Start the backend (see backend repo): `docker compose up -d`, `npm run dev`
2. Open the project in Android Studio, run the `debug` build on an emulator
3. Test accounts: see backend README (seed users)

## File map
- `app/build.gradle.kts`: build configuration, variants, BuildConfig `API_BASE_URL`, release validation check
- `gradle.properties`: default properties for debug and release API base URLs
- `app/src/debug/AndroidManifest.xml`: debug manifest overriding cleartext traffic
- `app/src/debug/res/xml/network_security_config.xml`: debug network security config permitting cleartext for all hosts
- `app/src/main/java/com/chandan/askfaculty/MainActivity.kt`: main Compose entry point with `in.chandan.askfaculty` package structure (under `com.chandan.askfaculty`)

## Backend requests / contract gaps
(things the app needs that the contract does not provide)

## Known issues
(none)

## Play Store tracker
- Developer account type and creation date:
- Privacy policy URL:
- Account deletion web URL:
- Closed test started on / tester count:
- Production access applied on / result:
