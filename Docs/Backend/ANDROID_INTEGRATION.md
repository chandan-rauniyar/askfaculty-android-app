# Android Realtime Chat Integration

The backend exposes chat history and fallback sends over REST and live events over Socket.IO.
PostgreSQL is the source of truth: treat socket events as timely updates, and use REST to catch
up after a disconnect.

## Authentication and connection

Use the HTTPS API base URL for REST and the same host for Socket.IO. Keep access and refresh
tokens in Android Keystore-backed secure storage; never put tokens in URLs or logs.

REST requests use:

```http
Authorization: Bearer <accessToken>
```

Connect Socket.IO with the access token in the connection `auth` payload:

```text
auth = { "token": accessToken }
```

Do not set a query-string token. On authentication success, the server joins the user's
personal room and re-verifies their active group memberships before joining those group rooms.
It then emits `chat:ready` with `{ "groupIds": [...] }`. Wait for this event before considering
the initial room list ready. `group:join` and `group:leave` accept `{ "groupId": "..." }`; a
client-supplied ID is never sufficient authorization.

Listen for `message:new`, `message:updated`, `message:deleted`, `read:updated`, `typing`,
`question:reply`, `notification:new`, and `group:access-revoked`. Apply events idempotently by
server message/notification ID. The server sends private question replies only to their
participants; do not broaden their visibility in the app.

## Sending messages

Generate and persist a UUID `clientMessageId` before sending. Keep it in the local outbox until
the server acknowledges it. Reuse that same ID and exact payload if a request times out; this
prevents duplicate messages. A retry that changes the body, reply target, attachments, or group
returns `IDEMPOTENCY_CONFLICT`.

Socket event:

```json
{
  "groupId": "group-id",
  "clientMessageId": "client-generated-uuid",
  "body": "Message text",
  "replyToId": "optional-message-id",
  "attachmentIds": []
}
```

An acknowledgement on success is `{ "success": true, "messageId": "...", "createdAt": "..." }`.
The persisted message is also broadcast as `message:new` to the group. The REST fallback is:

```http
POST /api/v1/groups/{groupId}/messages
Authorization: Bearer <accessToken>
Content-Type: application/json
```

```json
{
  "clientMessageId": "client-generated-uuid",
  "body": "Message text",
  "replyToId": "optional-message-id",
  "attachmentIds": []
}
```

Messages have a 4,000-character limit and accept at most five attachments. Upload attachments
through the resource upload-intent API before sending the message. In `FACULTY_ONLY` mode,
students may read but cannot send. The REST endpoint and socket event use the same authorization,
validation, idempotency, and rate limits.

## Reconnect and catch-up

Access tokens expire. The server sends an `INVALID_ACCESS_TOKEN` error and disconnects an
expired socket. Refresh through the existing endpoint:

```http
POST /api/v1/auth/refresh
Content-Type: application/json
```

```json
{ "refreshToken": "<currentRefreshToken>" }
```

The success response contains new `accessToken` and `refreshToken` values; replace both stored
tokens because refresh tokens rotate. Update the Socket.IO auth payload and reconnect. Do not
replay a queued message with a new `clientMessageId`.

After every reconnect, fetch missed messages from REST before resuming the live conversation:

```http
GET /api/v1/groups/{groupId}/messages?after={lastSeenMessageId}&limit=30
Authorization: Bearer <accessToken>
```

The response contains `{ "items": [...], "hasMore": false, "nextCursor": "..." }`. `after`
starts with the last message ID already applied locally. If `hasMore` is true, repeat using the
ID of the last returned item as the new `after` value. Merge results by message ID with any
concurrent socket events.

## History pagination and read state

The default history response is chronological. Fetch the newest page with:

```http
GET /api/v1/groups/{groupId}/messages?limit=30
```

For older history, pass the opaque `nextCursor` returned by the previous page:

```http
GET /api/v1/groups/{groupId}/messages?before={nextCursor}&limit=30
```

The maximum page size is 100. Treat cursors as opaque and do not construct or modify them.
Deleted and moderated-hidden messages are omitted from ordinary history.

When a user has read through a message, record that message ID using the `message:read` socket
event with `{ "groupId": "...", "lastReadMessageId": "..." }`, or
`POST /api/v1/groups/{groupId}/messages/read` with `{ "lastReadMessageId": "..." }`. Use
`GET /api/v1/groups/{groupId}/unread-count` for one group and
`GET /api/v1/me/unread-summary` for the group list. Read state is one cursor per user/group,
not per-message receipts.

## Errors and limits

REST and socket errors use the standard envelope:

```json
{
  "success": false,
  "error": {
    "code": "RATE_LIMITED",
    "message": "Too many messages; try again shortly",
    "details": []
  },
  "meta": { "requestId": "..." }
}
```

Check `success` on every socket acknowledgement. Socket failures are also emitted as `error`;
connection authentication errors are exposed as the Socket.IO `connect_error` data. Handle
stable error codes such as `UNAUTHENTICATED`, `INVALID_ACCESS_TOKEN`, `FORBIDDEN`,
`GROUP_MUTED`, `NOT_FOUND`, `VALIDATION_ERROR`, `IDEMPOTENCY_CONFLICT`, and `RATE_LIMITED`. Show safe,
user-facing messages and use `meta.requestId` for support diagnostics; never log tokens or
message content.

The default send limits are 20 messages per user and 200 per group per 10 seconds. Typing events
are transient and rate limited. Socket connections are limited per user and IP. A membership
revocation emits `group:access-revoked` and removes the socket from that group's room; stop
showing and sending to the group immediately.

Active group members can report a visible message with
`POST /api/v1/messages/{messageId}/reports` and `{ "reason": "..." }`. Assigned faculty and
administrators can review group reports with `GET /api/v1/groups/{groupId}/reports` and update a
report to `RESOLVED` or `DISMISSED` with `PATCH /api/v1/reports/{reportId}`. Assigned faculty
and administrators can also mute an active student with
`POST /api/v1/groups/{groupId}/mutes` and `{ "userId": "...", "durationMinutes": 60 }`, then
revoke it with `DELETE /api/v1/groups/{groupId}/mutes/{userId}`. A mute blocks sends but does not
remove the student's read access; expired mutes stop applying automatically.

For Android library setup, platform-specific Socket.IO APIs, upload details, and the complete
REST surface, see the project [README](../README.md).
