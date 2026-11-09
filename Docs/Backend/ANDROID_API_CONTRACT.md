# AskFaculty Android API Contract

This guide describes the implemented backend, not planned features. The source of truth is the
running application: Express routes, Zod request schemas, service authorization, Socket.IO
handlers, and Prisma models. The OpenAPI document is checked in at [openapi.json](./openapi.json)
and can be regenerated with `npm run docs:openapi`. Each REST operation has request/response
examples and schema references there.

## 1. Overview

- **API version:** `/api/v1` (REST). Health endpoints are `/health` and `/health/ready`.
- **Base URL:** configurable by deployment; `PUBLIC_API_BASE_URL` is used by the backend.
- **Local host:** `http://localhost:5000`.
- **Android emulator:** use `http://10.0.2.2:5000` to reach the development host.
- **Physical Android device:** use `http://<developer-computer-LAN-IP>:5000`; connect both
  devices to the same trusted network and allow inbound port 5000 on the private network.
- **Android cleartext:** HTTP may require a debug-only network security configuration for the
  app's local development build. Do not enable cleartext in release builds.
- **Production:** HTTPS only; never use cleartext HTTP for production credentials or tokens.
- **Swagger UI:** `/api-docs`; machine-readable contract: `/api-docs/openapi.json`.

Seeded development identities are `admin@college.test`, `faculty@college.test`, and
`student1@college.test` / `student2@college.test`. Their passwords are controlled by the local
`SEED_*_PASSWORD` environment variables; this guide intentionally does not include passwords.
Seed users are development-only and may not have academic memberships until the academic
structure is provisioned.

REST JSON requests use `Content-Type: application/json`. Protected REST endpoints use
`Authorization: Bearer <accessToken>`. Never put a token in a URL.

## 2. Response format and errors

Successful REST responses:

```json
{
  "success": true,
  "data": {},
  "meta": { "requestId": "req-id" }
}
```

Error responses:

```json
{
  "success": false,
  "error": {
    "code": "VALIDATION_ERROR",
    "message": "Invalid request",
    "details": []
  },
  "meta": { "requestId": "req-id" }
}
```

`details` is present on validation and selected business errors; otherwise it is an empty array
or omitted for an unexpected server error. Use `meta.requestId` for diagnostics, not the error
message as a programmatic discriminator.

| HTTP | Error code(s) | Meaning | Android action |
|---|---|---|---|
| 400 | `VALIDATION_ERROR` | Body/query is invalid, cursor is invalid, or the requested transition is invalid. | Show field-level messages from `details`; preserve user input. |
| 400 | `INVALID_ACTIVATION_TOKEN`, `INVALID_RESET_TOKEN` | One-time token is invalid, expired, used, or no longer applicable. | Ask for a new activation/reset email. |
| 400 | `UNSUPPORTED_FILE_TYPE`, `EXTERNAL_IDENTITY_UNSUPPORTED` | File declaration is unsupported, or local auth cannot verify an external proof. | Select an allowed file; do not retry an unsupported provider operation. |
| 401 | `UNAUTHENTICATED` | Required access token is missing. | Authenticate or attach the current access token. |
| 401 | `INVALID_ACCESS_TOKEN` | Access token is invalid/expired or user no longer exists. | Attempt refresh once; if refresh fails, clear credentials and sign in. |
| 401 | `INVALID_CREDENTIALS` | Email/password combination is invalid. | Show a generic sign-in failure; do not reveal which field was wrong. |
| 401 | `INVALID_REFRESH_TOKEN` | Refresh token expired, was revoked, reused, or is unknown. | Clear both tokens and require sign-in. |
| 403 | `ACCOUNT_INACTIVE` | Account is suspended/deactivated or not active. | Stop authenticated calls and direct the user to college support. |
| 403 | `FORBIDDEN` | Role or operation is not allowed. | Do not retry unchanged; hide or disable the action. |
| 403 | `GROUP_MUTED` | User may read the group but cannot send because an active mute applies. | Show the returned expiry/retry details if present; allow read-only access. |
| 404 | `NOT_FOUND` | Resource is absent or deliberately hidden because the caller lacks access. | Treat as unavailable; do not infer existence or retry another ID. |
| 409 | `CONFLICT`, `IDEMPOTENCY_CONFLICT`, `ADMIN_EXISTS`, `ATTACHMENT_LIMIT` | State changed, duplicate operation conflicts, client message ID was reused with a different payload, first admin already exists, or attachment capacity is reached. | Refresh affected state. For message idempotency, retry only the exact original payload and ID. |
| 409 | `UPLOAD_INCOMPLETE` | Object storage did not find the uploaded object during completion. | Retry the signed `PUT` while the URL is valid, then call complete again. |
| 413 | `FILE_TOO_LARGE` | File exceeds configured maximum. | Ask the user to choose a smaller file. |
| 429 | `RATE_LIMITED` | General, auth, chat-send, socket, or typing limit exceeded. | Back off; use `details.retryAfterSeconds` where returned. |
| 500 | `INTERNAL_ERROR` | Unexpected failure. | Show a generic failure and include request ID in support logs; retry only safe/idempotent calls. |
| 501 | `NOT_CONFIGURED` | College/OIDC auth provider is a scaffold and not implemented. | Do not fall back to local auth. Tell the user this sign-in method is unavailable. |
| 503 | `NOT_READY`, `STORAGE_UNAVAILABLE`, `EMAIL_NOT_CONFIGURED`, `EMAIL_DELIVERY_FAILED` | Dependency/readiness, object storage, or SMTP failure. | Retry later where appropriate; do not treat as success. |

`ADMIN_EXISTS` and `EXTERNAL_IDENTITY_UNSUPPORTED` are also used by bootstrap/provider code and
are not ordinary Android REST flows. Error codes are defined in the OpenAPI `ErrorCode` enum.

## 3. Authentication and sessions

Account creation is provisioned by a college administrator; there is no public registration
endpoint. Activation links/tokens are sent using configured SMTP.

| Operation | Method and path | Request |
|---|---|---|
| Activate provisioned account | `POST /api/v1/auth/activate` | `{ "token": "<activation-token>", "password": "<new-password>" }` |
| Log in | `POST /api/v1/auth/login` | `{ "email": "student1@college.test", "password": "<local-password>" }` |
| Rotate session | `POST /api/v1/auth/refresh` | `{ "refreshToken": "<current-refresh-token>" }` |
| Log out | `POST /api/v1/auth/logout` | `{ "refreshToken": "<current-refresh-token>" }` |
| Request reset email | `POST /api/v1/auth/forgot-password` | `{ "email": "student1@college.test" }` |
| Reset password | `POST /api/v1/auth/reset-password` | `{ "token": "<reset-token>", "password": "<new-password>" }` |
| Current account | `GET /api/v1/auth/me` | Bearer access token |

Activation and reset passwords must be 8–128 characters. Email is trimmed, lowercased, valid,
and at most 320 characters. Tokens supplied in activation/reset requests are 20–500 characters;
refresh token is 20–500 characters.

The access JWT lifetime is configured by `JWT_ACCESS_EXPIRES_IN` (default `15m`). The response
includes `expiresIn` seconds. Refresh tokens are opaque, stored hashed by the server, and have a
configured lifetime (`JWT_REFRESH_EXPIRES_IN`, default `7d`). Each refresh consumes the old
refresh token and returns a replacement. Reuse of a rotated/revoked token revokes the token
family. Password reset revokes active refresh tokens. Activation tokens last 7 days; reset tokens
last 1 hour; each is single-use.

Store both tokens only in Android Keystore-backed encrypted storage (e.g. encrypted
preferences backed by Keystore). Never log or put them in URLs. Attach access token to REST
requests and to Socket.IO `auth.token`. On 401, attempt one refresh with the latest refresh
token, replace both tokens, then retry a request only when safe; on refresh 401 clear tokens and
sign in again. A 403 `ACCOUNT_INACTIVE` means stop and do not refresh-loop. A generic 403 is an
authorization result, not an expired token.

## 4. Roles and access

Roles are assigned by the backend and never accepted as proof from the client:

- **`COLLEGE_ADMIN`** provisions accounts and manages academic structure, enrollments,
  assignments, manual groups, and group membership. It can view group questions but cannot read
  private-question content.
- **`FACULTY`** accesses assigned groups and assigned private questions; may reply, manage
  group notes/announcements, and moderate assigned group chat.
- **`STUDENT`** accesses active groups from enrollment and their own private questions; may ask
  questions/reply where authorized and send messages unless group chat is faculty-only or the
  user is muted.

Knowing a resource ID never grants access. Services check active account, role, enrollment,
faculty assignment, group membership, and question participation. Private doubt visibility is
limited to asker and currently assigned faculty; inaccessible IDs may return 404 rather than
403. Detailed per-operation role metadata is also in the OpenAPI `x-required-roles` field; those
     roles do not replace resource-level checks.

## 5. REST endpoint reference

All body and query constraints are fully described by the named schema and examples in
[openapi.json](./openapi.json). `id` path values are strings (1–64 characters where validated).
Unless noted, protected routes return the standard success envelope and can return validation,
authentication, authorization, not-found, conflict, rate-limit, or internal-error envelopes
where applicable.

### Auth

| Method | Path | Roles | Request/query | Operation-specific errors |
|---|---|---|---|---|
| POST | `/api/v1/auth/activate` | Public | `ActivateRequest` | `INVALID_ACTIVATION_TOKEN`, `VALIDATION_ERROR` |
| POST | `/api/v1/auth/login` | Public | `LoginRequest` | `INVALID_CREDENTIALS`, `ACCOUNT_INACTIVE`, `NOT_CONFIGURED` |
| POST | `/api/v1/auth/refresh` | Public | `RefreshRequest` | `INVALID_REFRESH_TOKEN`, `ACCOUNT_INACTIVE` |
| POST | `/api/v1/auth/logout` | Public (refresh token required) | `RefreshRequest` | `INVALID_REFRESH_TOKEN` |
| POST | `/api/v1/auth/forgot-password` | Public | `EmailRequest` | SMTP may fail; response is intentionally non-enumerating |
| POST | `/api/v1/auth/reset-password` | Public | `ResetPasswordRequest` | `INVALID_RESET_TOKEN` |
| GET | `/api/v1/auth/me` | Any active role | Bearer token | `UNAUTHENTICATED`, `INVALID_ACCESS_TOKEN` |

`forgot-password` returns the same message whether the email exists or not.

### Users

| Method | Path | Roles | Request/query | Operation-specific errors |
|---|---|---|---|---|
| POST | `/api/v1/users` | `COLLEGE_ADMIN` | `CreateUserRequest` | `CONFLICT`, `EMAIL_NOT_CONFIGURED`, `EMAIL_DELIVERY_FAILED` |
| GET | `/api/v1/users` | `COLLEGE_ADMIN` | `page` 1+, `limit` 1–100 (default 20), optional `role`, `status` | `FORBIDDEN` |
| GET | `/api/v1/users/{id}` | `COLLEGE_ADMIN` | Path ID | `NOT_FOUND` |
| PATCH | `/api/v1/users/{id}` | `COLLEGE_ADMIN` | `UpdateUserRequest` (at least one field) | `CONFLICT`, `FORBIDDEN`, `NOT_FOUND` |
| POST | `/api/v1/users/{id}/resend-activation` | `COLLEGE_ADMIN` | Path ID; no body | `CONFLICT`, `NOT_FOUND` |

User creation takes email (max 320), name (1–200), role enum, and optional nullable
`rollNumber`/`employeeId` (max 64). Updates accept name, role, status, and identifiers. Password
hashes and tokens are never returned.

### Structure and groups

All GETs below require an active token. Structure creation/updates and enrollment/assignment
management require `COLLEGE_ADMIN`. Page queries use `page` ≥1, `limit` 1–100, default 20.

| Method | Path | Roles | Request/query |
|---|---|---|---|
| GET / POST | `/api/v1/departments` | Any active / Admin | Page; `DepartmentRequest` on POST |
| PATCH | `/api/v1/departments/{id}` | Admin | `DepartmentPatchRequest` |
| GET / POST | `/api/v1/programs` | Any active / Admin | Page; `ProgramRequest` |
| PATCH | `/api/v1/programs/{id}` | Admin | `ProgramPatchRequest` |
| GET / POST | `/api/v1/academic-years` | Any active / Admin | Page; `AcademicYearRequest` |
| PATCH | `/api/v1/academic-years/{id}` | Admin | `AcademicYearPatchRequest` |
| GET / POST | `/api/v1/semesters` | Any active / Admin | Page; `SemesterRequest` |
| PATCH | `/api/v1/semesters/{id}` | Admin | `SemesterPatchRequest` |
| GET / POST | `/api/v1/sections` | Any active / Admin | Page; `SectionRequest` |
| PATCH | `/api/v1/sections/{id}` | Admin | `SectionPatchRequest` |
| GET / POST | `/api/v1/subjects` | Any active / Admin | Page; `SubjectRequest` |
| PATCH | `/api/v1/subjects/{id}` | Admin | `SubjectPatchRequest` |
| GET / POST | `/api/v1/enrollments` | Admin | Page; `EnrollmentRequest` |
| POST | `/api/v1/enrollments/{id}/end` | Admin | Optional `EndRecordRequest` (`endedAt`) |
| GET / POST | `/api/v1/faculty-assignments` | Admin | Page; `AssignmentRequest` |
| POST | `/api/v1/faculty-assignments/{id}/end` | Admin | Optional `EndRecordRequest` |
| GET | `/api/v1/groups` | Admin sees all; others see accessible groups | Page; optional `status=ACTIVE\|ARCHIVED` |
| POST | `/api/v1/groups` | Admin | `GroupRequest` (`GENERAL` or `CUSTOM`) |
| GET | `/api/v1/groups/{id}` | Admin or active group member | Path ID |
| PATCH | `/api/v1/groups/{id}` | Admin | `GroupPatchRequest` |
| GET | `/api/v1/groups/{id}/members` | Admin or active group member | Page |
| POST | `/api/v1/groups/{id}/members` | Admin | `GroupMemberRequest` |
| DELETE | `/api/v1/groups/{id}/members/{userId}` | Admin | Path IDs; no body |

Structure schemas enforce names 1–160 characters, codes 1–40, program duration 1–12 years,
semester sequence 1–20, group description ≤1,000, and ordered start/end dates. PATCH bodies
must contain at least one field. CLASS/SUBJECT memberships are synchronized from academic
records, not manually managed. See OpenAPI for each property and request example.

### Questions and replies

| Method | Path | Roles | Request/query | Operation-specific errors |
|---|---|---|---|---|
| POST | `/api/v1/questions` | Student | `PrivateQuestionRequest` | `FORBIDDEN`, `NOT_FOUND`, `RATE_LIMITED` |
| GET | `/api/v1/questions` | Any active role; visibility-filtered | Page plus optional `status`, `type`, `groupId`, `subjectId`, `mine`, `unanswered`, `from`, `until`, `sort` | `VALIDATION_ERROR` |
| GET / POST | `/api/v1/groups/{groupId}/questions` | Active member; post also admin | Page; `GroupQuestionRequest` | `NOT_FOUND`, `CONFLICT`, `RATE_LIMITED` |
| GET | `/api/v1/questions/{questionId}` | Visible question participant/member; admin only for group doubts | Path ID | `NOT_FOUND` |
| PATCH / DELETE | `/api/v1/questions/{questionId}` | Author for edits/deletes; service workflow for status | PATCH `QuestionPatchRequest`; DELETE no body | `NOT_FOUND`, `CONFLICT`, `FORBIDDEN` |
| GET / POST | `/api/v1/questions/{questionId}/replies` | Authorized question reader/writer | GET `before` reply-ID cursor and `limit` 1–100 (default 30); POST `ReplyRequest` | `NOT_FOUND`, `CONFLICT`, `RATE_LIMITED` |
| PATCH / DELETE | `/api/v1/replies/{replyId}` | Reply author within edit policy | PATCH `ReplyRequest`; DELETE no body | `NOT_FOUND`, `FORBIDDEN` |
| POST | `/api/v1/questions/{questionId}/accept-reply` | Asker or eligible faculty per service rules | `AcceptReplyRequest` | `NOT_FOUND`, `CONFLICT` |
| POST | `/api/v1/questions/{questionId}/resolve` | Asker or eligible faculty; admin for group question only | No body | `NOT_FOUND`, `CONFLICT` |
| POST | `/api/v1/questions/{questionId}/reopen` | Asker or eligible faculty within reopen window | No body | `NOT_FOUND`, `CONFLICT` |
| PATCH | `/api/v1/questions/{questionId}/status` | Authorized participant/faculty/admin per service rules | `QuestionStatusRequest` (`OPEN`, `RESOLVED`, `CLOSED`) | `NOT_FOUND`, `CONFLICT` |
| POST | `/api/v1/questions/{questionId}/reassign` | Admin | `ReassignQuestionRequest` | `NOT_FOUND`, `FORBIDDEN`, `CONFLICT` |
| GET | `/api/v1/me/eligible-faculty` | Student | Optional `subjectId` | `FORBIDDEN` |
| GET | `/api/v1/faculty/dashboard` | Faculty | No query/body | `FORBIDDEN` |

Question title is 1–200 characters, description 1–10,000, tags are at most 8 strings of
1–32 characters, and reply body is 1–5,000. Page limit is at most 100. Reply cursor is a reply
ID, not an encoded cursor. Replies are returned chronologically with `items` and `nextBefore`;
there is no `hasMore` property in the implemented reply response.
The daily student limit defaults to 10 questions. Editing defaults to 15 minutes; reopen
defaults to 7 days; current settings are in `AppSettings.policySettings`.

### Notes and uploads

| Method | Path | Roles | Request/query | Operation-specific errors |
|---|---|---|---|---|
| GET | `/api/v1/groups/{groupId}/notes` | Active group member | Page (`limit` 1–100, default 20) | `NOT_FOUND` |
| GET | `/api/v1/notes/{noteId}` | Active group member | Path ID | `NOT_FOUND` |
| PATCH | `/api/v1/notes/{noteId}` | Admin or assigned faculty member | `NotePatchRequest` | `NOT_FOUND`, `FORBIDDEN` |
| POST | `/api/v1/notes/{noteId}/archive` | Admin or assigned faculty member | No body | `NOT_FOUND`, `FORBIDDEN` |
| POST | `/api/v1/uploads/presign` | Active user; target-specific permissions | Discriminated `UploadPresignRequest` | `NOT_FOUND`, `FORBIDDEN`, `UNSUPPORTED_FILE_TYPE`, `FILE_TOO_LARGE`, `ATTACHMENT_LIMIT` |
| POST | `/api/v1/uploads/complete` | Upload owner with current target access | `{ "uploadId": "<id>" }` | `NOT_FOUND`, `FORBIDDEN`, `UPLOAD_INCOMPLETE`, `CONFLICT`, `STORAGE_UNAVAILABLE` |
| GET | `/api/v1/notes/{noteId}/download` | Active group member | Path ID | `NOT_FOUND` |
| GET | `/api/v1/attachments/{attachmentId}/download` | Current target-authorized user | Path ID | `NOT_FOUND`, `FORBIDDEN` |

Upload workflow:

1. Call `POST /uploads/presign` with a target and file metadata. For notes, use
   `{ "purpose":"NOTE", "groupId":"...", "title":"...", "filename":"notes.pdf",
   "mimeType":"application/pdf", "size":12345 }`; `replaceNoteId` is optional.
2. Upload raw bytes to the returned `url` using the returned `method` (`PUT`) and exact
   `headers` (including `Content-Type`). Do not send the API bearer token to object storage.
3. Call `POST /uploads/complete` with the returned `uploadId`. The backend checks object
   existence, declared size, signature/type, and checksum, then creates a Note or Attachment.
4. For attachments, use the resulting attachment ID on the corresponding question/reply/
   announcement or message flow. For message attachments, `purpose` is `MESSAGE` with `groupId`.
5. To download, call the authorized API endpoint and open the short-lived returned URL; current
   code returns a 60-second object-storage URL.

Supported MIME/extension pairs: PDF `.pdf`; Word `.doc`, `.docx`; PowerPoint `.ppt`, `.pptx`;
JPEG `.jpg`/`.jpeg`; PNG `.png`; GIF `.gif`; WebP `.webp`. Default max size is 20 MiB via
`MAX_UPLOAD_MB`. Filenames are 1–180 chars and may not contain path separators/control
characters; declared MIME is 1–120 chars. Attachment limits default to 5 per question/reply/
announcement; message uploads have separate staging limits.

### Announcements

| Method | Path | Roles | Request/query |
|---|---|---|---|
| GET | `/api/v1/announcements` | Any active role; visibility-filtered | Page, optional `type` |
| POST | `/api/v1/announcements` | Admin or faculty | `AnnouncementRequest` |
| GET / PATCH | `/api/v1/announcements/{announcementId}` | Visible reader / author or admin | PATCH `AnnouncementPatchRequest` |
| POST | `/api/v1/announcements/{announcementId}/publish` | Author or admin (faculty restricted to assigned group) | No body |
| POST | `/api/v1/announcements/{announcementId}/archive` | Author or admin | No body |

Title is 1–200; body is 1–20,000; audience can be a group or a department, never both.
Faculty can only create/publish for assigned groups. Students see published notices for
authorized groups/departments and college-wide notices; drafts are not public.

### Notifications

| Method | Path | Roles | Request/query |
|---|---|---|---|
| GET | `/api/v1/notifications` | Any active role; own records only | Page, optional `unread=true\|false` |
| GET | `/api/v1/notifications/unread-count` | Any active role | No query/body |
| GET | `/api/v1/notifications/{notificationId}` | Owner only | Path ID |
| POST | `/api/v1/notifications/{notificationId}/read` | Owner only | Path ID; no body |
| POST | `/api/v1/notifications/read-all` | Any active role | No body |
| GET | `/api/v1/me/unread-summary` | Any active role | Per accessible group chat unread counts |

Notification type enum: `QUESTION`, `REPLY`, `NOTE`, `MESSAGE`, `ANNOUNCEMENT`,
`QUESTION_STATUS`, `REPORT`. Notifications use generic text and reference IDs; private question
or chat content is not put into notification text.

### Messages and chat moderation

| Method | Path | Roles | Request/query | Operation-specific errors |
|---|---|---|---|---|
| GET | `/api/v1/groups/{groupId}/messages` | Active group member | `before` opaque cursor OR `after` message ID; `limit` 1–100, default 30 | `NOT_FOUND`, `VALIDATION_ERROR` |
| POST | `/api/v1/groups/{groupId}/messages` | Active member; students blocked in faculty-only mode | `MessageSendRequest` | `NOT_FOUND`, `FORBIDDEN`, `GROUP_MUTED`, `IDEMPOTENCY_CONFLICT`, `RATE_LIMITED` |
| PATCH | `/api/v1/groups/{groupId}/chat-mode` | Assigned faculty or admin | `ChatModeRequest` | `NOT_FOUND`, `FORBIDDEN` |
| POST | `/api/v1/groups/{groupId}/messages/read` | Active group member | `ReadMessageRequest` | `NOT_FOUND` |
| GET | `/api/v1/groups/{groupId}/unread-count` | Active group member | No body | `NOT_FOUND` |
| PATCH / DELETE | `/api/v1/messages/{messageId}` | Author for edit/delete; assigned moderator/admin may hide | PATCH `MessagePatchRequest`; DELETE no body | `NOT_FOUND`, `FORBIDDEN`, `CONFLICT` |
| POST | `/api/v1/messages/{messageId}/reports` | Active group member | `MessageReportRequest` | `NOT_FOUND`, `CONFLICT` |
| GET | `/api/v1/groups/{groupId}/reports` | Assigned faculty or admin | Page (`limit` ≤100, default 30), optional report `status` | `NOT_FOUND`, `FORBIDDEN` |
| PATCH | `/api/v1/reports/{reportId}` | Assigned faculty or admin | `MessageReportStatusRequest` | `NOT_FOUND`, `FORBIDDEN`, `CONFLICT` |
| POST | `/api/v1/groups/{groupId}/mutes` | Assigned faculty or admin | `GroupMuteRequest` | `NOT_FOUND`, `FORBIDDEN`, `CONFLICT` |
| DELETE | `/api/v1/groups/{groupId}/mutes/{userId}` | Assigned faculty or admin | Path IDs; no body | `NOT_FOUND`, `FORBIDDEN`, `CONFLICT` |

Message body is 1–4,000 chars; `clientMessageId` is 8–128 chars matching
`^[A-Za-z0-9._:-]+$`; `attachmentIds` is unique and max 5. Exact retry with same
`clientMessageId` and identical payload returns the prior record (REST 200 on retry, 201 on
creation); changing payload returns `IDEMPOTENCY_CONFLICT`. `before` and `after` cannot be
combined. `before` is an opaque base64url cursor; `after` is the last seen message ID.
Default send limits are 20 per user and 200 per group per 10 seconds (policy-configurable).
Typing is limited to 20 updates per socket per 10 seconds. Message edit window defaults to
15 minutes. Mute duration request is 1–10,080 minutes, subject to the configured policy cap.

## 6. Enums and constants

| Name | Values |
|---|---|
| Role | `COLLEGE_ADMIN`, `FACULTY`, `STUDENT` |
| Account status | `PENDING_ACTIVATION`, `ACTIVE`, `SUSPENDED`, `DEACTIVATED` |
| Question type | `GROUP`, `PRIVATE` |
| Question status | `OPEN`, `ANSWERED`, `RESOLVED`, `CLOSED` |
| Group type | `CLASS`, `SUBJECT`, `GENERAL`, `CUSTOM` |
| Group status | `ACTIVE`, `ARCHIVED` |
| Group membership role | `MEMBER`, `FACULTY`, `MODERATOR` |
| Chat mode | `OPEN`, `FACULTY_ONLY` |
| Announcement type | `GENERAL`, `CLASS`, `SUBJECT`, `EXAM`, `ASSIGNMENT`, `IMPORTANT_NOTICE` |
| Announcement status | `DRAFT`, `PUBLISHED`, `ARCHIVED` |
| Note status | `ACTIVE`, `ARCHIVED` |
| Notification type | `QUESTION`, `REPLY`, `NOTE`, `MESSAGE`, `ANNOUNCEMENT`, `QUESTION_STATUS`, `REPORT` |
| Report status | `OPEN`, `RESOLVED`, `DISMISSED` |
| Upload purpose | `NOTE`, `QUESTION`, `REPLY`, `ANNOUNCEMENT`, `MESSAGE` |
| Password length | 8–128 characters |
| Page size | default 20; minimum 1; maximum 100 |
| Message/reply history size | default 30; minimum 1; maximum 100 |

## 7. Pagination

Page-based lists return `{ items, page, limit, total }`. `page` starts at 1; default `limit` is
20, maximum 100. Do not infer more pages solely from the current `items` length; compare
`page * limit` with `total`.

Question reply history uses a reply ID cursor:

```http
GET /api/v1/questions/{questionId}/replies?limit=30
GET /api/v1/questions/{questionId}/replies?before={nextBefore}&limit=30
```

The backend returns `{ items, nextBefore }`. `nextBefore` is a reply ID (or null); use it as an
opaque token and do not construct it. Request another page while it is non-null.

Chat history returns chronological `{ items, hasMore, nextCursor }`. For older messages:

```http
GET /api/v1/groups/{groupId}/messages?limit=30
GET /api/v1/groups/{groupId}/messages?before={nextCursor}&limit=30
```

After a reconnect, catch up using the last message ID already applied locally:

```http
GET /api/v1/groups/{groupId}/messages?after={lastSeenMessageId}&limit=30
```

Repeat with the last returned message ID while `hasMore` is true. Merge socket events and
catch-up results by message ID. `before` and `after` cannot be used together. The before cursor
is encoded from message ID + creation timestamp; only the backend should create/decode it.

## 8. File upload and download

Use the 5-minute presigned upload intent flow in **Notes and uploads**. Upload object bytes with
HTTP PUT and the returned Content-Type header, then complete using `uploadId`. The backend
does not return object keys. Upload completion checks the object and actual file signature.
Abandoned upload intents are cleaned periodically.

Download endpoints are authorization-gated and return short-lived URLs; never persist these URLs
as permanent public file links. Re-request a URL when it expires. File binaries are stored in
S3-compatible storage, not PostgreSQL.

## 9. Realtime chat

Socket.IO uses the same server host/port as REST. Connect with the access token in the Socket.IO
auth object, not the query string:

```javascript
io(baseUrl, { auth: { token: accessToken } });
```

The server joins the user's personal room and currently active group rooms, then emits
`chat:ready`:

```json
{ "groupIds": ["<group-id>"] }
```

Client-to-server events (all acknowledgements are callbacks):

| Event | Payload | Success acknowledgement |
|---|---|---|
| `group:join` | `{ "groupId": "<id>" }` | `{ "success": true, "groupId": "<id>" }`; membership rechecked |
| `group:leave` | `{ "groupId": "<id>" }` | `{ "success": true, "groupId": "<id>" }` |
| `message:send` | Same fields as `MessageSendRequest`, including required `groupId` on socket | `{ "success": true, "messageId": "...", "createdAt": "..." }` |
| `message:read` | `{ "groupId": "<id>", "lastReadMessageId": "<id>" }` | `{ "success": true, "groupId": "...", "lastReadMessageId": "..." }` |
| `typing:start` / `typing:stop` | `{ "groupId": "<id>" }` | `{ "success": true }` |
| `message:delete` | `{ "messageId": "<id>" }` | `{ "success": true, "messageId": "..." }` |

Server-to-client events:

| Event | Payload |
|---|---|
| `chat:ready` | `{ "groupIds": ["..."] }` |
| `message:new` | Persisted public message object (`id`, `groupId`, `sender`, `body`, reply/attachment metadata, timestamps) |
| `message:updated` | Updated persisted public message object |
| `message:deleted` | `{ "groupId": "...", "messageId": "..." }` |
| `read:updated` | `{ "groupId": "...", "userId": "...", "lastReadMessageId": "...", "updatedAt": "..." }` |
| `typing` | `{ "groupId": "...", "userId": "...", "active": true\|false }` |
| `question:reply` | `{ "questionId": "...", "reply": { id, sender, body, attachments, createdAt } }`; private replies are delivered only to participants |
| `notification:new` | Public notification fields: `id`, `type`, `title`, `body`, `referenceId`, `createdAt` |
| `group:access-revoked` | `{ "groupId": "..." }`; stop displaying/sending to that group |
| `error` | Standard error envelope; also used with failed acknowledgements |

The Socket.IO handshake token is checked against active account state; expired access tokens
emit `INVALID_ACCESS_TOKEN` and disconnect. Refresh both tokens over REST, update `auth.token`,
and reconnect. Socket authentication failures arrive as `connect_error` with structured `data`.
Redis is optional for single instance; with Redis configured, the Socket.IO Redis adapter fans
events across instances.

The API configures a 64 KiB Socket.IO message buffer, 25-second ping interval, 20-second ping
timeout, 10-second connect timeout, max connections per IP from
`MAX_SOCKET_CONNECTIONS_PER_IP` (default 100), connection attempts per IP per minute from
`MAX_SOCKET_CONNECTION_ATTEMPTS_PER_MINUTE` (default 60), and max sockets per user from policy
settings (default 5, bounded to 1–20). Typing rate is 20 events per 10 seconds per socket.

## 10. Notifications

Use `GET /api/v1/notifications?page=1&limit=20&unread=true`, unread count, per-notification
read, or read-all. Notifications are user-scoped. The code creates notifications for:

- Private question creation: assigned faculty.
- Question replies/status changes: question participants and, for group questions, active
  assigned group faculty as applicable.
- Note upload: active group members other than uploader.
- Announcement publication: recipients of the announcement audience.
- Faculty chat messages: offline active group members (not every message to connected members).

The exact emitted event depends on transaction/service flow; use persisted notification list as
the source of truth. `REPORT` exists in the notification enum but the current report-status
workflow does not create report notifications. Notifications never embed private question/
message text.

## 11. Main call sequences

### Admin setup

1. Start Docker services; configure `.env`; run `npm install`, `npm run db:generate`,
   `npm run db:migrate`, `npm run db:seed`.
2. For first production admin, use `npm run admin:create` with administrator environment
   variables; for development, sign in with the locally configured seeded admin account.
3. `POST /auth/login`, then provision users with `POST /users`; deliver activation email.
4. Create department, program, academic year, semester, section, and subject using their admin
   APIs; enroll students and assign faculty. Active memberships create CLASS/SUBJECT groups.
5. Review group membership via `GET /groups` and `/groups/{id}/members`.

### Activate and log in

1. User receives activation token out-of-band and calls `POST /auth/activate`.
2. Call `POST /auth/login`; securely store `accessToken` and `refreshToken`.
3. Call `GET /auth/me` to load identity/role. Refresh before/after 401 using the rules above.

### Ask a group doubt

1. Student loads `GET /groups`, then `GET /groups/{groupId}/questions`.
2. `POST /groups/{groupId}/questions` with title, description, optional subject/tags.
3. Load `/questions/{questionId}/replies`; post follow-ups through its `/replies`.
4. Ask or eligible faculty resolves/accepts an answer; reload question after transitions.

### Ask a private doubt

1. Student calls `GET /me/eligible-faculty?subjectId=...`.
2. `POST /questions` with `type: PRIVATE`, `assignedFacultyId`, `subjectId`, title, description.
3. Use `/questions/{questionId}` and `/replies`. Do not show the question to any nonparticipant,
   including admin.

### Upload a note

1. Assigned faculty obtains `POST /uploads/presign` for `purpose: NOTE`.
2. PUT file bytes to returned URL with returned headers.
3. `POST /uploads/complete` with `uploadId`.
4. Students in group load `GET /groups/{groupId}/notes` and request a download URL.

### Post an announcement

1. Admin or assigned faculty creates draft using `POST /announcements`.
2. Faculty may target only assigned group; admin may select group, department, or college-wide.
3. `POST /announcements/{announcementId}/publish`; clients refresh visible announcements.

### Group chat

1. Connect Socket.IO with access token; wait for `chat:ready`.
2. Load latest history with REST. Persist a unique `clientMessageId` before sending.
3. Send `message:send`; on timeout retry identical payload and ID.
4. On reconnect, refresh tokens if needed and catch up with `?after=<lastAppliedMessageId>`.

## 12. Suggested Android screens

| Screen | APIs/events |
|---|---|
| Activation / sign-in / reset | Auth activation, login, refresh, forgot/reset, `/auth/me` |
| Home/group list | `/groups`, `/me/unread-summary`, `/notifications/unread-count` |
| Group details/members | `/groups/{id}`, `/groups/{id}/members`, `/groups/{id}/chat-mode` (faculty/admin) |
| Group doubt feed/detail | Group questions, `/questions`, question detail and reply cursor APIs |
| Private doubts | `/me/eligible-faculty`, private question create/list/detail/replies |
| Faculty dashboard | `/faculty/dashboard`, group/question APIs |
| Notes | Group notes, note detail/download, upload presign/complete for faculty |
| Announcements | List/detail/create/update/publish/archive |
| Notifications | List/unread count/read/read-all and `notification:new` |
| Group chat | REST history/send/read/unread; Socket.IO message/read/typing/update/delete/revocation |
| Chat moderation | Report message, group reports, report status, mute/revoke mute |
| Admin console screens in Android (optional) | User provisioning, academic structure, enrollment/assignment, groups/members |

## 13. Not implemented yet and known limits

- P1 scope is intentionally not built here: search, general question/reply/note reporting and
  moderation, profile/password-change/account-deletion flows, bulk import, device-token
  registration/preferences, term rollover, promoting chat messages into questions, and chat
  mentions.
- Current reports/moderation cover **chat messages only**. There is no REST report API for
  questions, replies, or notes, and no block-list API.
- College API and OIDC auth providers are scaffolds until official provider documentation is
  provided; they fail explicitly and do not fall back to local auth.
- No push notification delivery; current notifications are in-app and optional live Socket.IO
  events. Device-token endpoints are not implemented.
- `GET /search`, `/users/me`, `/users/me/password`, `/users/me/deletion-request`, and
  `/devices` are not implemented.
- Read/unread chat state is one cursor per user/group, not per-message receipts. The `after`
  message query uses an ID; the `before` query uses an opaque encoded cursor.
- Upload storage requires configured S3-compatible service; supported types and limits are listed
  above. Antivirus scanning is not implemented.
- External identity proof verification is not available in local auth; provider configuration
  does not make the scaffold functional.
- REST API errors use standard envelopes; successful Socket.IO acknowledgements use event-specific
  shapes, while failed acknowledgements and `error` events use the standard error envelope.
- Chat scale/load results are local single-instance measurements, not production capacity
  guarantees.

## Differences between implementation, previous Swagger/API docs, and SPEC

Implementation takes precedence. In particular:

1. The previous OpenAPI document used `Authentication`, `Resources`, `Chat`, and `Chat
   moderation` tags, omitted separate `Replies`, `Notes`, `Uploads`, and `Messages` groupings,
   and had incomplete patch schemas and example/status details. This contract changes those
   metadata/documentation gaps; runtime endpoints are unchanged.
2. SPEC’s suggested general moderation API includes reports for questions, replies, and notes;
   code currently implements chat-message reports only at `/messages/{messageId}/reports`,
   `/groups/{groupId}/reports`, and `/reports/{reportId}`.
3. SPEC lists P1 profile/deletion, search, bulk import, device tokens/preferences, and rollover.
   Those routes/models are not part of this implemented API.
4. Chat history’s `before` value is an opaque cursor, while `after` is a message ID. Reply
   pagination uses a reply ID in `nextBefore`; it is not the same cursor format as chat.
5. The chat REST send returns a full message envelope and uses 201 for new sends / 200 for an
   idempotent retry. Socket `message:send` acknowledges only `messageId` and `createdAt`.
6. User/admin private-question visibility follows code: admins cannot read private content. The
   SPEC test intent mentions an admin exception for reports, but no question-report workflow is
   implemented, so there is no such access path.
7. Actual upload type support is PDF, DOC/DOCX, PPT/PPTX, JPEG, PNG, GIF, and WebP; upload size
   is controlled by `MAX_UPLOAD_MB` (default 20 MiB). This guide does not infer extra formats
   from the product spec.
8. External college/OIDC authentication is not implemented without provider specs. Selecting it
   produces `NOT_CONFIGURED` rather than local-auth fallback.
9. `REPORT` is a defined notification type, but there is currently no report-status notification
   emission in the moderation service.
10. The chat addendum marks `read:updated` optional/P1; the implementation emits it now. Chat
    mention parsing and message-to-question promotion remain unimplemented.
11. The addendum permits offline notifications when needed; implementation creates them for
    offline active group members on faculty messages, but not for mentions or every message.
12. Reply pagination actually returns `{ items, nextBefore }`, without `hasMore`; clients should
    use a non-null `nextBefore` to determine whether another page can be requested.

## Verification run

The API was run on `127.0.0.1:5000` against the dedicated `askfaculty_test` database using the
seeded admin, faculty, and two student accounts. The verification run made 37 real HTTP requests
that passed their expected status checks (including the expected denial responses). Test-created academic
records/questions/messages remain in that test database only. Tokens, passwords, and generated
IDs are omitted below.

| Role | Real request | HTTP |
|---|---|---:|
| Admin | `POST /auth/login`, `GET /auth/me`, `GET /users`, `GET /departments`, `GET /groups` | 200 |
| Admin | Create department, program, academic year, semester, section, subject, enrollment, and faculty assignment | 201 each |
| Student 1 | `POST /auth/login`, `GET /groups`, `GET /notifications`, `GET /groups/{id}/questions` | 200 |
| Student 1 | `POST /questions` (private), `GET /questions/{id}` | 201, 200 |
| Student 2 | `GET /questions/{student-1-private-question-id}` | **404 `NOT_FOUND`** |
| Admin | `GET /questions/{student-1-private-question-id}` | **404 `NOT_FOUND`** |
| Student | `GET /users` (admin-only) | **403 `FORBIDDEN`** |
| Faculty | `GET /faculty/dashboard`, `GET /groups`, `POST /questions/{id}/replies` | 200, 200, 201 |
| Student / admin | Create and publish a general announcement; student lists announcements | 201, 200, 200 |
| Student | `POST /groups/{id}/messages`, history, unread count, and read cursor | 201, 200, 200, 200 |

The following `data` excerpts are sanitized from actual successful responses; each was wrapped
in `{"success":true,"data":...,"meta":{"requestId":"<redacted>"}}`. Values such as IDs and
timestamps are replaced with placeholders where they are generated. These examples supplement
the complete request/response examples on each operation in [openapi.json](./openapi.json).

| Role | Real request | Sanitized response `data` excerpt |
|---|---|---|
| Admin | `GET /auth/me` | `{"user":{"email":"admin@college.test","name":"Dev Admin","role":"COLLEGE_ADMIN","status":"ACTIVE","rollNumber":null,"employeeId":null}}` |
| Admin | `GET /users?page=1&limit=1` | `{"items":[{"email":"student2@college.test","name":"Dev Student Two","role":"STUDENT","status":"ACTIVE","rollNumber":"ROLL002","employeeId":null}],"page":1,"limit":1,"total":20}` |
| Admin | `GET /departments?page=1&limit=1` | `{"items":[{"name":"<test department>","code":"<test code>"}],"page":1,"limit":1,"total":3}` |
| Admin | `GET /programs?page=1&limit=1` | `{"items":[{"name":"<test program>","durationYears":4,"active":true,"department":{"name":"<test department>"}}],"page":1,"limit":1,"total":3}` |
| Admin | `GET /academic-years?page=1&limit=1` | `{"items":[{"name":"<test year>","startsAt":"<timestamp>","endsAt":"<timestamp>","active":true}],"page":1,"limit":1,"total":3}` |
| Admin | `GET /semesters?page=1&limit=1` | `{"items":[{"name":"<test semester>","sequence":1,"active":true,"academicYear":{"name":"<test year>"}}],"page":1,"limit":1,"total":3}` |
| Admin | `GET /sections?page=1&limit=1` | `{"items":[{"name":"<test section>","program":{"name":"<test program>"},"academicYear":{"name":"<test year>"}}],"page":1,"limit":1,"total":3}` |
| Admin | `GET /subjects?page=1&limit=1` | `{"items":[{"name":"<test subject>","code":"<test code>","active":true,"department":{"name":"<test department>"}}],"page":1,"limit":1,"total":3}` |
| Admin | `GET /enrollments?page=1&limit=1` | `{"items":[{"startedAt":"<timestamp>","endedAt":null,"student":{"name":"Dev Student One","rollNumber":"ROLL001"},"section":{"name":"<test section>"}}],"page":1,"limit":1,"total":2}` |
| Admin | `GET /faculty-assignments?page=1&limit=1` | `{"items":[{"startsAt":"<timestamp>","endsAt":null,"faculty":{"name":"Dev Faculty","employeeId":"EMP001"},"subject":{"name":"<test subject>"}}],"page":1,"limit":1,"total":2}` |
| Student | `GET /groups?page=1&limit=1` | `{"items":[{"name":"<test group>","type":"SUBJECT","status":"ACTIVE","chatMode":"OPEN","section":{"name":"<test section>"},"subject":{"name":"<test subject>"}}],"page":1,"limit":1,"total":2}` |
| Student | `GET /groups/{groupId}/members?page=1&limit=1` | `{"items":[{"role":"MEMBER","active":true,"source":"AUTO","user":{"name":"Dev Student One","role":"STUDENT","email":"student1@college.test"}}],"page":1,"limit":1,"total":2}` |
| Student | `GET /groups/{groupId}/questions?page=1&limit=1` | `{"items":[{"type":"GROUP","title":"API docs group question","status":"ANSWERED","tags":["docs"],"_count":{"replies":1}}],"page":1,"limit":1,"total":1}` |
| Student | `GET /questions?type=PRIVATE&mine=true&page=1&limit=1` | `{"items":[{"type":"PRIVATE","title":"API docs private question","status":"OPEN","tags":["docs"],"group":null,"subject":{"code":"<test code>","name":"<test subject>"},"_count":{"replies":0}}],"page":1,"limit":1,"total":1}` |
| Student | `GET /questions/{questionId}/replies?limit=1` | `{"items":[],"nextBefore":null}` |
| Faculty | `GET /faculty/dashboard` | `{"unansweredCount":1,"oldestUnanswered":{"title":"API docs private question","group":null,"subject":{"code":"<test code>","name":"<test subject>"}},"byGroup":[],"bySubject":[{"count":1}]}` |
| Student | `GET /announcements?page=1&limit=1` | `{"items":[{"type":"GENERAL","title":"API docs verification","body":"Temporary test-database verification announcement.","status":"PUBLISHED","pinned":false,"author":{"name":"Dev Admin","role":"COLLEGE_ADMIN"},"attachments":[]}],"page":1,"limit":1,"total":2}` |
| Student | `GET /notifications?page=1&limit=1` | `{"items":[],"page":1,"limit":1,"total":0}` |
| Student | `GET /groups/{groupId}/messages?limit=1` | `{"items":[{"sender":{"name":"Dev Student One","role":"STUDENT"},"body":"API docs verification message","replyToId":null,"attachments":[]}],"hasMore":false,"nextCursor":"<opaque cursor>"}` |
| Student | `GET /groups/{groupId}/unread-count` | `{"groupId":"<redacted>","unreadCount":0}` |
| Student | `GET /groups/{groupId}/notes?page=1&limit=1` | `{"items":[],"page":1,"limit":1,"total":0}` |

Sanitized error examples copied from actual permission checks:

```json
{
  "success": true,
  "data": {
    "user": {
      "id": "<redacted>",
      "email": "admin@college.test",
      "name": "Dev Admin",
      "role": "COLLEGE_ADMIN",
      "status": "ACTIVE",
      "rollNumber": null,
      "employeeId": null,
      "createdAt": "2026-11-07T16:55:44.969Z",
      "updatedAt": "2026-11-07T17:11:13.279Z",
      "lastLoginAt": "2026-11-07T17:11:13.277Z"
    }
  },
  "meta": { "requestId": "<redacted>" }
}
```

```json
{
  "success": false,
  "error": {
    "code": "NOT_FOUND",
    "message": "Question not found",
    "details": []
  },
  "meta": { "requestId": "<redacted>" }
}
```

```json
{
  "success": false,
  "error": {
    "code": "FORBIDDEN",
    "message": "You do not have permission to perform this action",
    "details": []
  },
  "meta": { "requestId": "<redacted>" }
}
```
