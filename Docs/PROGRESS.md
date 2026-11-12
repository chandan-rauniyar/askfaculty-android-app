# AskFaculty Android — Progress

Update this file after every step. A new session or a different AI tool must be able to continue from here alone.

## Current step
G1 — completed (with Role-Based Bottom Navigation for Student, Faculty, & Admin)

## Done (with date and what was verified)
- G1 completed: Implemented `GroupApiService` (`GET /groups`, `GET /me/unread-summary`), `GroupRepository`, `GroupsViewModel`, and `GroupsScreen`.
- **Role-Based Bottom Navigation:** Configured `FloatingGlassDock` and `HomeShell` to dynamically adapt the bottom navigation bar tabs and home dashboards based on the authenticated user's role:
  - **Student:** `Home`, `Groups`, `Doubts`, `Alerts`, `Profile`
  - **Faculty:** `Home`, `Questions`, `Groups`, `Messages`, `Profile`
  - **College Admin:** `Dashboard`, `Users`, `Academics`, `Groups`, `Profile`
  All unit tests passed (13 passed) and verified successfully on emulator.

## Step checklist
- [x] S0.1 Project, dependencies, build types, base URL config
- [x] S0.2 DI, navigation, theme, API client, error mapping, health check
- [x] S0.3 Secure token storage and SessionManager
- [x] A1 Login and role-based home shell
- [x] A2 Auto refresh, logout, session restore
- [x] A3 Activation, forgot/reset password
- [x] G1 Group list with unread badges
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
- **Role-Aware Bottom Navigation Tabs:** Dynamically map `DockTab` destinations based on `user?.role` (`STUDENT`, `FACULTY`, `COLLEGE_ADMIN`).

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
- `app/src/main/java/com/chandan/askfaculty/ui/main/FloatingGlassDock.kt`: Role-aware floating glass dock with dynamic tabs for Student, Faculty, and Admin
- `app/src/main/java/com/chandan/askfaculty/ui/home/HomeShell.kt`: Main navigation shell wiring up role-specific dashboards and tabs
- `app/src/main/java/com/chandan/askfaculty/ui/groups/GroupsScreen.kt`: Group list screen with unread badges

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
