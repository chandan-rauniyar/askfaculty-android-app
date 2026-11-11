# AskFaculty Android — Progress

Update this file after every step. A new session or a different AI tool must be able to continue from here alone.

## Current step
A3 — completed

## Done (with date and what was verified)
- A2 completed: Implemented `AuthInterceptor` to inject Bearer access tokens, `TokenAuthenticator` providing single-flight mutex-guarded silent token refresh on `401 INVALID_ACCESS_TOKEN` (`POST /auth/refresh`), logout endpoint integration (`POST /auth/logout`), session persistence/restore via `SessionManager` & `TokenStorage`, and logout action clearing session and returning to login. Verified build, passed unit tests, and tested on emulator.
- A3 completed: Implemented account activation (`POST /auth/activate`), forgot password (`POST /auth/forgot-password`), and password reset (`POST /auth/reset-password`) with paste-friendly token support, new password inputs, and error handling (`INVALID_ACTIVATION_TOKEN`, `INVALID_RESET_TOKEN`). Added unit tests (`ActivationViewModelTest`), passed all unit tests (13 passed), and tested successfully on the emulator against the local backend.

## Step checklist
- [x] S0.1 Project, dependencies, build types, base URL config
- [x] S0.2 DI, navigation, theme, API client, error mapping, health check
- [x] S0.3 Secure token storage and SessionManager
- [x] A1 Login and role-based home shell
- [x] A2 Auto refresh, logout, session restore
- [x] A3 Activation, forgot/reset password
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
- **Single-flight token refresh:** Implemented OkHttp `Authenticator` guarded by a coroutine `Mutex` to perform thread-safe single-flight refresh calls upon receiving `401` responses, rotating tokens and retrying the failed request.
- **Logout handling:** `AuthRepository.logout()` invokes `POST /auth/logout` with the refresh token and clears local secure storage.
- **Auth flow navigation:** Added UI states and navigation flow in `MainActivity` supporting Login, Activation, Forgot Password, and Reset Password screens with token pasting support (20–500 chars).

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
- `app/src/main/java/com/chandan/askfaculty/data/remote/AuthInterceptor.kt`: OkHttp interceptor attaching Bearer access tokens
- `app/src/main/java/com/chandan/askfaculty/data/remote/TokenAuthenticator.kt`: Mutex-guarded single-flight authenticator for silent token refresh
- `app/src/main/java/com/chandan/askfaculty/data/remote/AuthApiService.kt`: Auth REST endpoints (`refresh`, `logout`, `activate`, `forgotPassword`, `resetPassword`)
- `app/src/main/java/com/chandan/askfaculty/data/repository/AuthRepository.kt`: Auth repository with activation and password reset methods
- `app/src/main/java/com/chandan/askfaculty/ui/auth/ActivationScreen.kt`: Account activation screen (token + new password)
- `app/src/main/java/com/chandan/askfaculty/ui/auth/ForgotPasswordScreen.kt`: Forgot password screen (email input)
- `app/src/main/java/com/chandan/askfaculty/ui/auth/ResetPasswordScreen.kt`: Reset password screen (token + new password)
- `app/src/main/java/com/chandan/askfaculty/MainActivity.kt`: Root activity managing auth flow screens and session observer
- `app/test/java/com/chandan/askfaculty/ui/auth/ActivationViewModelTest.kt`: Unit tests for account activation success and invalid token states

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
