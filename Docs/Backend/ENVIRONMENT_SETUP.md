# AskFaculty Environment Setup

This guide explains the local development settings and what must be supplied by the production
environment. Values such as hostnames, database names, college details, and sender addresses are
environment-specific. **Do not copy example credentials into production or commit a real `.env`
file.**

## Development

Start from the repository's `.env.example`:

```powershell
Copy-Item .env.example .env
```

The sample is configured for the local Docker services. Use these values for local development:

| Variable | Development value | Purpose |
|---|---|---|
| `NODE_ENV` | `development` | Enables development behavior. |
| `PORT` | `5000` | Local API port. |
| `PUBLIC_API_BASE_URL` | `http://localhost:5000` | Used in activation/reset email text. On an Android emulator, the app reaches the host through `http://10.0.2.2:5000`; do not use that emulator-only hostname as the deployment URL. |
| `DATABASE_URL` | Local PostgreSQL URL for database `askfaculty` | Main development database. Keep the local credentials from your Compose setup; do not use this database for integration tests. |
| `TEST_DATABASE_URL` | Local PostgreSQL URL for database `askfaculty_test` | Integration-test database only. Never point it at development or production data. |
| `JWT_ACCESS_SECRET`, `JWT_REFRESH_SECRET` | Replace the sample values with two different random strings, each at least 32 characters | Local signing secrets. |
| `JWT_ACCESS_EXPIRES_IN`, `JWT_REFRESH_EXPIRES_IN` | `15m`, `7d` | Access and refresh token lifetimes. |
| `AUTH_PROVIDER` | `local` | Uses the implemented local account/password provider. |
| `ALLOW_SELF_REGISTRATION` | `false` | Accounts are provisioned by an admin. |
| `CORS_ORIGINS` | `http://localhost:5000` | Allowed browser origins. Native Android clients do not use browser CORS. |
| `COLLEGE_TIMEZONE` | `Asia/Kolkata` | College timezone. |
| `S3_ENDPOINT`, `S3_REGION`, `S3_BUCKET`, `S3_ACCESS_KEY`, `S3_SECRET_KEY` | Local MinIO values from `.env.example` | Local object storage. Leave all five unset to run without storage, or configure all five together. |
| `S3_FORCE_PATH_STYLE` | `true` for the local MinIO setup | S3 URL addressing style. |
| `MAX_UPLOAD_MB` | `20` | Maximum upload size in MiB. |
| `MAIL_HOST`, `MAIL_PORT`, `MAIL_FROM` | `localhost`, `1025`, `noreply@localhost` | Sends development email to Mailpit, not to real mailboxes. View messages at `http://localhost:8025`. |
| `REDIS_URL` | Empty | Redis is optional for local, single-instance development. |

`docker compose up -d` starts PostgreSQL, MinIO, and Mailpit. The development SMTP settings are
deliberately local: a message accepted by Mailpit is captured in its inbox and is **not delivered
to the recipient's real email provider**.

Generate separate random local JWT secrets in PowerShell, then copy each result into `.env`:

```powershell
node -e "console.log(require('node:crypto').randomBytes(32).toString('base64url'))"
node -e "console.log(require('node:crypto').randomBytes(32).toString('base64url'))"
```

Use the first output for `JWT_ACCESS_SECRET` and the second for `JWT_REFRESH_SECRET`. Do not
reuse production secrets locally.

## Sending activation and password-reset emails to real users

The mailer reads `MAIL_HOST`, `MAIL_PORT`, and `MAIL_FROM` and sends through SMTP. Configure those
values for the production mail relay, then restart the API process. The relay hostname and sender
must come from the email/IT provider responsible for the college domain; they are not values the
Android app supplies.

The current implementation configures SMTP with `secure: false` and does not configure SMTP
authentication. It can send through a trusted relay that accepts mail from the backend using
network access controls (for example, a private relay or source-IP allowlisting; use the relay's
supported STARTTLS policy). It **cannot** send directly through a provider that requires SMTP
username/password authentication, and implicit-TLS-only port 465 is not supported by this
configuration. Do not add undocumented `MAIL_USER` or `MAIL_PASSWORD` values: the application
does not read them. If the chosen provider requires authenticated submission, the mailer must be
extended to support that provider before production use.

### What data is needed?

- The operator needs the SMTP relay hostname, port, and a sender address/domain approved by the
  mail provider (for example, `noreply@your-college-domain.example`). The sender domain may need
  SPF/DKIM/DMARC and provider-side verification.
- The admin provisioning a user supplies that person's real, reachable email address, name, and
  role. Optional roll/employee identifiers are account data, not mail-server configuration.
- No initial password is needed for a newly provisioned user. The user receives a single-use
  activation token and chooses their password through `POST /api/v1/auth/activate`. The token
  expires after 7 days.
- The first administrator is created separately with `npm run admin:create`; that account is
  provisioned directly and does not receive an activation email from this command.
- Password reset email is sent only for an active account. The endpoint returns the same
  response whether or not an account exists; reset tokens expire after 1 hour.

With local authentication enabled, create a student/faculty account from the admin-authenticated
`POST /api/v1/users` endpoint. The response includes `emailSent`. If it is `false`, the account
still exists in `PENDING_ACTIVATION`; fix SMTP delivery and have an admin call
`POST /api/v1/users/{id}/resend-activation`. A successful API response alone does not prove that
the recipient's mailbox accepted or displayed the message. Check the relay/provider delivery
logs and test first with a mailbox you control.

Activation and reset messages contain tokens in their text. Treat them as credentials: use
HTTPS for public API traffic, keep SMTP relays private/restricted, do not paste tokens into logs
or support tickets, and never use real recipient addresses in local Mailpit tests.

## Production settings

Set production configuration in the hosting platform's secret/environment manager, not in a
committed file. `.env` is intended for local development. The API requires the following core
settings; replace every example/placeholder with values for your deployment.

| Variable | Production value to configure | Important notes |
|---|---|---|
| `NODE_ENV` | `production` | Enables production validation. |
| `PORT` | Port provided by the hosting platform (or `5000`) | Expose the service only through the intended HTTPS ingress/reverse proxy. |
| `PUBLIC_API_BASE_URL` | Public HTTPS API origin, e.g. `https://api.your-domain.example` | Must be HTTPS in production; email text uses this URL. |
| `DATABASE_URL` | Production PostgreSQL connection URL | Use a production database and managed secret storage; never use `TEST_DATABASE_URL`. Require encrypted database connections when supported by the host. |
| `JWT_ACCESS_SECRET` | Independently generated random secret, at least 32 characters | Replace sample values. Do not share with refresh secret. Changing it invalidates issued access tokens. |
| `JWT_REFRESH_SECRET` | A second independently generated random secret, at least 32 characters | Changing it invalidates refresh sessions. |
| `JWT_ACCESS_EXPIRES_IN` | Normally `15m` | Use the intended token policy; accepted format is an integer plus `s`, `m`, `h`, or `d`. |
| `JWT_REFRESH_EXPIRES_IN` | Normally `7d` | Same accepted duration format. |
| `AUTH_PROVIDER` | `local` unless a supported provider is implemented | `college_api` and `oidc` are scaffolds; their environment settings do not make those providers functional. |
| `ALLOW_SELF_REGISTRATION` | `false` | Keep admin provisioning as the account-creation path. |
| `CORS_ORIGINS` | Exact comma-separated browser origins that need access | Do not use a wildcard when credentials are involved. Android native apps do not need CORS allowlisting. |
| `COLLEGE_TIMEZONE` | IANA timezone for the college | For example, `Asia/Kolkata`. |
| `TRUST_PROXY_HOPS` | Exact count of trusted reverse-proxy hops, or `0` if none | Only trust forwarding headers from proxies you control. The API must not be reachable by bypassing that proxy. |
| `LOG_LEVEL` | `info` or stricter | Do not enable verbose logging in production without a need; tokens, passwords, and message content must not be logged. |
| `MAIL_HOST` | SMTP relay hostname from the mail/IT provider | Must be reachable from the API host. The SMTP authentication limitation above applies. |
| `MAIL_PORT` | Port supported by the relay and current client, commonly `587` for STARTTLS | Port `465` implicit TLS is not supported by the current `secure: false` mailer. |
| `MAIL_FROM` | Verified sender address/domain, e.g. `AskFaculty <noreply@your-domain.example>` | Configure sender-domain authentication and delivery policy with the provider. |
| `S3_ENDPOINT` | HTTPS endpoint reachable by both the API and clients uploading via presigned URLs | Presigned URLs use this endpoint; a private-only hostname will not work for off-network mobile clients. |
| `S3_REGION` | Region expected by the S3-compatible service | Use the actual bucket region/provider setting. |
| `S3_BUCKET` | Production bucket name | Create it and apply least-privilege access controls before enabling uploads. |
| `S3_ACCESS_KEY`, `S3_SECRET_KEY` | Dedicated application credentials with least privilege | Keep them in secret storage; do not use MinIO defaults or administrator credentials. |
| `S3_FORCE_PATH_STYLE` | `false` for most AWS S3 endpoints; set according to the S3-compatible provider | Local MinIO commonly needs `true`. |
| `MAX_UPLOAD_MB` | Chosen operational upload limit (default `20`) | Must be a positive number; align ingress limits and storage policy. |
| `REDIS_URL` | Private Redis URL when using multiple API instances or shared rate limits | Optional for a single instance. Do not expose Redis publicly. |
| `MAX_SOCKET_CONNECTIONS_PER_IP`, `MAX_SOCKET_CONNECTION_ATTEMPTS_PER_MINUTE` | Set only when overriding defaults (`100`, `60`) | Tune based on deployment and abuse monitoring. |

Set all five S3 variables together or leave all unset. If they are unset, file upload/download
features are unavailable. Make sure presigned upload URLs resolve from the Android device, not
only from inside the backend network.

`TEST_DATABASE_URL` is only for local integration tests and must never point to production.
`ADMIN_EMAIL`, `ADMIN_NAME`, and `ADMIN_PASSWORD` are one-time inputs to `npm run admin:create`,
not normal server settings. Supply them securely to that command and remove them from the shell
environment afterward. Never run the development seed against production.

`COLLEGE_AUTH_BASE_URL`, `COLLEGE_AUTH_CLIENT_ID`, `COLLEGE_AUTH_CLIENT_SECRET`, `OIDC_ISSUER_URL`,
`OIDC_CLIENT_ID`, and `OIDC_CLIENT_SECRET` are only read when selecting those providers, but both
provider integrations remain unimplemented scaffolds. Do not select them for production sign-in.

## Before production launch

1. Replace local database, object-storage, mail, URL, CORS, timezone, and secret values with
   deployment-specific values.
2. Generate and store separate JWT secrets in the hosting secret manager. Verify they are at
   least 32 characters, differ from one another, and are not sample `change-me` values.
3. Use HTTPS for the public API and presigned storage endpoint. Set the exact trusted proxy count.
4. Configure a real SMTP relay supported by the current mailer, or implement authenticated SMTP
   support before choosing a provider that requires it.
5. Create the first admin, create a test student with a mailbox you control, verify activation
   and password reset end to end, then confirm relay delivery logs. Do not use real student
   records until these checks succeed.
6. Confirm the application cannot connect to test databases, local Mailpit, or development
   MinIO from its production configuration.

See [`.env.example`](../.env.example) for the local template, [README.md](../README.md) for
startup/migration commands, and [ANDROID_API_CONTRACT.md](./ANDROID_API_CONTRACT.md) for the
client-facing activation and password-reset API contract.
