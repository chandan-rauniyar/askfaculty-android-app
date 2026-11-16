# AskFaculty Android — Progress

Update this file after every step. A new session or a different AI tool must be able to continue from here alone.

## Current step
G1 & G2 — completed (with Notification Badge Alignment Fix)

## Done (with date and what was verified)
- G1 & G2 completed: Implemented group list and group detail screens with 5 tabs and members list.
- **Notification Badge Alignment Fix:** Refined the bell notification icon badge in `GroupsScreen` (`FloatingTopBar`) to match the clean overlay styling (white border ring, shadow, and correct offset), showing real unread notification counts from `GET /notifications/unread-count`. All unit tests passed (13 passed) and verified successfully on emulator.

## Step checklist
- [x] S0.1 Project, dependencies, build types, base URL config
- [x] S0.2 DI, navigation, theme, API client, error mapping, health check
- [x] S0.3 Secure token storage and SessionManager
- [x] A1 Login and role-based home shell
- [x] A2 Auto refresh, logout, session restore
- [x] A3 Activation, forgot/reset password
- [x] G1 Group list with unread badges
- [x] G2 Group detail tabs and members
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
- **Notification Badge Styling:** Aligned badge positioning and borders on `GroupsScreen` top bar bell with `AskFacultyTopBar`.

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
- `app/src/main/java/com/chandan/askfaculty/ui/groups/GroupsScreen.kt`: Groups screen with aligned notification badge and floating top bar
- `app/src/main/java/com/chandan/askfaculty/ui/main/AskFacultyTopBar.kt`: Home top bar component
- `app/src/main/java/com/chandan/askfaculty/ui/main/FloatingGlassDock.kt`: Floating glass dock component
- `app/src/main/java/com/chandan/askfaculty/ui/home/HomeShell.kt`: Main navigation shell

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
