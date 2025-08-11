## Ktor Backend

### Requirements
- Kotlin 1.9+
- Ktor 2.x
- Exposed ORM
- PostgreSQL (Neon)
- Redis (Jedis)
- JWT (HS256)
- Jakarta Mail (SMTP)
- dotenv-kotlin

### Environment (.env)
Required keys:
```
DATABASE_URL=postgresql://<neon-connection-string>
REDIS_URL=redis://<redis-connection-string>
JWT_SECRET=<256-bit-secret>
JWT_ISSUER=myapp.backend
JWT_AUDIENCE=myapp.client
JWT_EXP_SECONDS=3600
GOOGLE_CLIENT_ID_ANDROID=<google-client-id>
GOOGLE_CLIENT_ID_WEB=<optional-web-client-id>
SMTP_HOST=<smtp-host>
SMTP_PORT=<smtp-port>
SMTP_USER=<smtp-username>
SMTP_PASS=<smtp-password>
EMAIL_FROM=no-reply@myapp.com
OTP_TTL_SECONDS=600
OTP_LENGTH=6
OTP_COOLDOWN_SECONDS=60
APP_BASE_URL=http://192.168.X.XXX:8080
PORT=8080
```

Neon `DATABASE_URL` is accepted as either `postgresql://...` or a JDBC url `jdbc:postgresql://...`. If using `postgresql://user:pass@host:port/db?sslmode=require`, it is converted automatically to JDBC.

### Run
- In Android Studio, open the project and select the `backend` module.
- Ensure `.env` is in `backend/` directory or environment variables are set.
- Run `ApplicationKt`.

### Endpoints
- POST `/signup` → creates inactive user, sends OTP
- POST `/verify-otp` → verify OTP, activates, returns JWT
- POST `/login` → email/password login, returns JWT + profile
- POST `/google-auth` → Google ID token auth, returns JWT
- GET `/profile` (auth) → profile
- PUT `/profile` (auth) → update username/full_name
- PATCH `/profile/password` (auth) → change password
- DELETE `/profile` (auth) → soft delete

### Google OAuth Setup
Create OAuth 2.0 Client IDs in Google Cloud:
- Android client with package and SHA-1; use `GOOGLE_CLIENT_ID_ANDROID`.
- Optional Web client; use `GOOGLE_CLIENT_ID_WEB`.

### Phone Testing on LAN
Find your LAN IP (`ipconfig` on Windows). Put it into `APP_BASE_URL` and your Android client `BASE_URL`.


