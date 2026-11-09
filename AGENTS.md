# AskFaculty Android App — Instructions for AI Tools

Native Android client for the AskFaculty backend (single college; roles STUDENT, FACULTY, COLLEGE_ADMIN). The backend is already built. This repo contains only the Android app.

Copy this file to `.github/copilot-instructions.md` as well, so GitHub Copilot in VS Code reads it automatically.

## Read first (in this order)
1. `docs/PROGRESS.md` — current step, decisions, what is done
2. `docs/ANDROID_PLAN.md` — the step-by-step plan and acceptance criteria
3. `docs/backend/ANDROID_API_CONTRACT.md` — the API contract (source of truth for endpoints, fields, errors)
4. `docs/backend/openapi.json`, `docs/backend/ANDROID_INTEGRATION.md` (chat/socket), `docs/backend/API.md`
5. `docs/PLAY_STORE_CHECKLIST.md` — policy rules that affect design decisions

If the contract and the code disagree, tell me. Never invent endpoints, fields, or enum values. If something is missing from the contract, list it under "Backend requests" in `docs/PROGRESS.md` instead of guessing.

## Stack (defaults; record changes in docs/PROGRESS.md)
- Kotlin, Jetpack Compose, Material 3, single `app` module, package by feature
- MVVM: ViewModel + StateFlow, Repository layer, sealed UI state per screen
- Hilt for dependency injection, Navigation Compose
- Retrofit + OkHttp + kotlinx.serialization
- DataStore for settings; tokens in Android Keystore-backed encrypted storage (choose a current, non-deprecated approach and record it)
- Socket.IO Java client for chat (only in the chat steps)
- Coil for images, JUnit + MockK + Turbine for tests
- Version catalog (`libs.versions.toml`). Ask before adding any dependency not listed here.
- Do NOT add analytics, ads, or crash-reporting SDKs without asking (they change the Play Data safety form).

## Working rules
- Work on ONE step from `docs/ANDROID_PLAN.md` at a time. Start each step with a short plan, then implement.
- A step is done only when: it builds, unit tests for its ViewModels/repositories pass, it was tried on an emulator against the local backend, and `docs/PROGRESS.md` is updated. Never claim something works unless you ran it.
- After each step, update `docs/PROGRESS.md` (tick the step, note decisions, files added, known issues, next step) and suggest a git commit message. Do not commit.
- Keep screens small and composables reusable. No business logic in composables.
- Show loading, empty, and error states on every list and form screen.

## API rules (from the contract)
- Base URL comes from BuildConfig. Debug: `http://10.0.2.2:5000/api/v1` for the emulator (cleartext allowed ONLY for the debug build via a debug network security config). Release: HTTPS only, no cleartext.
- Every REST response uses the envelope `{ success, data | error, meta.requestId }`. Parse it in one place and map `error.code` to a sealed `AppError`. Show friendly messages, never raw server text for unexpected errors.
- Send `Authorization: Bearer <accessToken>`. On `401 INVALID_ACCESS_TOKEN`: refresh once (single-flight, guarded by a mutex), replace BOTH tokens, retry the request once. If refresh returns `INVALID_REFRESH_TOKEN`, clear the session and go to login.
- Pagination: page/limit for normal lists; opaque cursors for messages and replies. Never build or edit a cursor.
- File upload: presign → HTTP PUT to the returned URL with the returned headers (do NOT send the API bearer token to storage) → complete. Download URLs are short-lived; never store them.
- Chat: generate a UUID `clientMessageId` before sending, keep it in an outbox until acknowledged, reuse the same ID and payload on retry. After every reconnect, catch up over REST using `after`.
- Treat timestamps as UTC ISO strings and display them in the device time zone.

## Security and privacy rules
- Never log tokens, passwords, activation/reset tokens, or message/doubt content. Redact the Authorization header in HTTP logging; disable HTTP body logging in release.
- Never put tokens in URLs, intents, or navigation arguments.
- Do not request more permissions than needed. Use the system photo/file picker for attachments, not broad storage or media permissions.
- Role-based UI only hides things for clarity. The backend enforces permissions, so handle `403` and `404` gracefully.
- Release builds: R8 enabled, no debuggable flag, no cleartext traffic, backups configured so tokens are not backed up.

## Never
- Never hardcode a server URL, secret, or test password in source.
- Never invent API fields; never add features outside the current step.
- Never skip error handling to "finish faster".