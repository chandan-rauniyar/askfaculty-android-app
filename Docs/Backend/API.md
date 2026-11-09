# AskFaculty API Reference

The interactive OpenAPI explorer runs at `/api-docs`; its machine-readable contract is at
`/api-docs/openapi.json`. The contract uses OpenAPI 3.0.3. All application endpoints are
versioned under `/api/v1` except the liveness/readiness checks under `/health`.

## Authentication and authorization

Protected HTTP endpoints accept `Authorization: Bearer <accessToken>`. Access tokens are short
lived; use `POST /api/v1/auth/refresh` with the current refresh token to rotate the session.
Protected calls require an active account.

The user must also have permission for the referenced resource. Depending on the route, services
check college-admin role, group membership, enrollment, faculty assignment, or private-question
participation. Knowing an ID is not authorization. Private question content is visible only to
participants. Inaccessible groups/questions may return `404` to avoid disclosing their existence.

College-admin-only endpoints include user provisioning/management, academic structure
management, enrollment/assignment management, manual group management, and private-question
reassignment. Group chat moderation/report access is limited to assigned faculty or a college
admin, with resource-level checks in the service layer.

## Response conventions

Successful REST responses use:

```json
{
  "success": true,
  "data": {},
  "meta": { "requestId": "request-id" }
}
```

Errors use a stable code and request ID:

```json
{
  "success": false,
  "error": {
    "code": "VALIDATION_ERROR",
    "message": "Request validation failed",
    "details": []
  },
  "meta": { "requestId": "request-id" }
}
```

Common codes include `VALIDATION_ERROR`, `UNAUTHENTICATED`, `INVALID_ACCESS_TOKEN`,
`FORBIDDEN`, `NOT_FOUND`, `CONFLICT`, `IDEMPOTENCY_CONFLICT`, `GROUP_MUTED`, `RATE_LIMITED`,
and `INTERNAL_ERROR`. Do not log authorization headers, refresh tokens, passwords, private
question content, or chat message bodies. The OpenAPI explorer does not persist entered
credentials across reloads.

Collections are page/limit based unless marked cursor-based. Standard page-based collection
queries accept `page` (1-based) and `limit` (maximum 100). Chat message history uses cursor
pagination; see [Android integration](./ANDROID_INTEGRATION.md).

## Endpoint groups

The OpenAPI contract lists request bodies, query/path parameters, authentication, representative
success/error envelopes, and role notes for the implemented routes:

- **Health:** `GET /health`, `GET /health/ready`
- **Authentication:** activation, login, refresh, logout, forgot/reset password, and current user
- **Users:** admin provision, list, get, update, and resend activation
- **Structure:** departments, programs, academic years, semesters, sections, subjects,
  enrollments, faculty assignments, and group/member administration
- **Questions:** group/private questions, replies, pagination, status transitions, accepted
  replies, reassignment, eligible faculty, and faculty dashboard
- **Resources:** upload presign/complete, notes, authorized downloads, announcements, and
  user-scoped notifications
- **Chat:** message history/send/edit/delete, chat modes, read cursors, and unread counts
- **Chat moderation:** message reports and temporary group mutes

Chat also exposes Socket.IO events (`message:send`, `message:read`, `typing:start`,
`typing:stop`; server events include `message:new`, `message:updated`, `message:deleted`,
`read:updated`, `typing`, `question:reply`, and `notification:new`). Socket authentication and
mobile reconnect behavior are documented in [Android integration](./ANDROID_INTEGRATION.md).

Use the explorer's **Authorize** control to test protected requests with a development access
token. Swagger UI is an API reference/testing aid, not a substitute for application
authorization, and production deployments should expose it only in accordance with deployment
policy.
