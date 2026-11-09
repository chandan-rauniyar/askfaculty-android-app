# AskFaculty Android — Build Plan

Build in small steps. Each step has a goal, the API it uses (from `docs/backend/ANDROID_API_CONTRACT.md`), and a "done when" check. Finish and test a step before starting the next.

**Definition of done for every step:** builds, unit tests pass, tried on an emulator against the local backend, no tokens or content in logs, `docs/PROGRESS.md` updated.

---

## 1. Product summary

| Role | Main use |
|---|---|
| Student | See groups, ask group or private doubts, read notes and announcements, chat, get notifications |
| Faculty | Answer doubts, upload notes, post announcements, moderate chat in their groups |
| College Admin | Create users, academic structure, enrollments, assignments (small console; optional in the app) |

No public sign-up. Admin creates the account, the user activates it with a token from the email.

## 2. Order of work and why

Auth first, then the screens students and faculty use daily, then chat, then the admin console, then Play-readiness. The admin setup can be done with Swagger/Postman or the seed script while you build the early steps, so you are not blocked. If you prefer admin screens first, move Phase 7 right after Phase 1; nothing else depends on it.

## 3. Steps

### Phase 0 — Setup
| Step | Work | Done when |
|---|---|---|
| S0.1 | Create the project (Kotlin, Compose, Material 3), package name, version catalog, build types (debug/release), BuildConfig base URL, debug-only cleartext config for `10.0.2.2` | App runs on emulator; release config forbids cleartext |
| S0.2 | Hilt, Navigation, theme, Retrofit/OkHttp client, response-envelope parser, `AppError` mapping from `error.code`, debug-only health screen calling `GET /health/ready` | Emulator shows backend "ok" |
| S0.3 | Secure token storage and a `SessionManager` (save, read, clear tokens; current user) | Tokens survive app restart, are not readable in plain storage |

### Phase 1 — Authentication
| Step | Work | APIs | Done when |
|---|---|---|---|
| A1 | Login screen, load `/auth/me`, role-based home shell (Student, Faculty, Admin) | `POST /auth/login`, `GET /auth/me` | Each seed user lands on the right home |
| A2 | Auth interceptor + authenticator: single-flight refresh, token rotation, logout, session restore on launch | `POST /auth/refresh`, `POST /auth/logout` | Expired access token refreshes silently; failed refresh returns to login |
| A3 | Activation screen (email token + new password), forgot/reset password | `POST /auth/activate`, `/auth/forgot-password`, `/auth/reset-password` | New user activates and logs in; wrong or used token shows a clear message |

Note: activation and reset tokens arrive as text in the email and are 20–500 characters, so the screen must allow pasting. A short code or a deep link would be nicer; see backend gaps.

### Phase 2 — Groups
| Step | Work | APIs |
|---|---|---|
| G1 | Group list with unread badges, pull to refresh | `GET /groups`, `GET /me/unread-summary` |
| G2 | Group detail with tabs (Doubts, Notes, Announcements, Chat, Members), members list | `GET /groups/{id}`, `GET /groups/{id}/members` |

### Phase 3 — Announcements and notes
| Step | Work | APIs |
|---|---|---|
| N1 | Announcements list/detail; faculty and admin create, edit, publish, archive | `/announcements` endpoints |
| N2 | Notes list/detail, open/download file | `GET /groups/{groupId}/notes`, `GET /notes/{id}`, `GET /notes/{id}/download` |
| N3 | Faculty upload a note with the file picker | `POST /uploads/presign`, PUT to storage, `POST /uploads/complete` |

### Phase 4 — Doubts (core of the app)
| Step | Work | APIs |
|---|---|---|
| D1 | Group doubts list (filters: status, unanswered, mine) and ask a doubt | `GET/POST /groups/{groupId}/questions` |
| D2 | Doubt thread: replies (cursor), post reply, accept answer, resolve/reopen, status chip | `/questions/{id}`, `/replies`, `/accept-reply`, `/resolve`, `/reopen` |
| D3 | Private doubts: pick subject and eligible faculty, create, "My doubts" list | `GET /me/eligible-faculty`, `POST /questions`, `GET /questions` |
| D4 | Faculty dashboard and inbox (unanswered first) | `GET /faculty/dashboard`, `GET /questions` |
| D5 | Attachments on doubts and replies | upload flow + `GET /attachments/{id}/download` |

### Phase 5 — Notifications
| Step | Work | APIs |
|---|---|---|
| F1 | Notifications list, unread badge, mark read / read all, tap to open the target | `/notifications` endpoints |

No push yet; notifications are in-app (plus live `notification:new` once chat sockets exist).

### Phase 6 — Chat
| Step | Work | APIs |
|---|---|---|
| C1 | History (cursor), send over REST with `clientMessageId`, local outbox and retry | `GET/POST /groups/{groupId}/messages` |
| C2 | Socket.IO connect with `auth.token`, live events, reconnect + REST catch-up, token-expiry reconnect | socket events from `ANDROID_INTEGRATION.md` |
| C3 | Read state, unread counts, typing, edit/delete own message, chat mode, report message, faculty mute/reports | read, report, mute endpoints |

### Phase 7 — Admin console (minimal)
| Step | Work | APIs |
|---|---|---|
| M1 | Users: list, create, edit, resend activation, suspend | `/users` endpoints |
| M2 | Structure: departments, programs, semesters, sections, subjects | `/departments` … `/subjects` |
| M3 | Enrollments, faculty assignments, groups and members | `/enrollments`, `/faculty-assignments`, `/groups` |

### Phase 8 — Play-store readiness (needs backend gaps closed)
| Step | Work |
|---|---|
| P1 | Profile screen, change password, privacy policy and terms links, about |
| P2 | Report content (doubts, replies, notes) and block/mute users, with a visible moderation contact |
| P3 | Account deletion request in the app plus the public deletion web page link |
| P4 | Release hardening: R8, signing, no cleartext, permission audit, ANR/crash testing, accessibility, small-screen and Android-version checks |
| P5 | Closed test with testers, fix feedback, apply for production |

---

## 4. Backend gaps to close before the Play release

The contract says these are not implemented. Google Play rules make some of them necessary.

| Gap | Why it matters | Needed by |
|---|---|---|
| Account deletion request (`/users/me/deletion-request` + admin handling) | Play account-deletion policy | Before closed test review |
| Report for questions, replies and notes (not only chat) | Play user-generated-content policy: in-app reporting | Before production |
| Block user (or equivalent) | Same policy | Before production |
| `GET/PATCH /users/me`, change password | Profile screen, security | P1 |
| SMTP with login support in the mailer | Real email providers need authentication; the current mailer cannot send to them | Before real users |
| Short activation code or deep link | Pasting a long token from an email is poor UX | A3 improvement |
| Public privacy-policy and account-deletion web pages | Required Play links | Before closed test |
| Production reviewer/demo account with realistic data | Google reviewers must be able to log in | Before submission |
| Device tokens and push (FCM) | Optional, later | Later |

## 5. Test accounts

Use the seed users for local work. Never use real student data in dev. The Play reviewer account must live on the real hosted backend (the dev seed must never run in production).

## 6. Manual test script (after Phase 4)

1. Admin creates a faculty and two students; all activate by email token.
2. Admin assigns faculty, enrolls both students, group appears for all.
3. Faculty uploads a PDF and posts an announcement; student sees and opens both.
4. Student 1 posts a group doubt; faculty answers and accepts; student resolves.
5. Student 2 sends a private doubt; Student 1 cannot see it.
6. Suspend Student 2; login fails and old doubts remain.