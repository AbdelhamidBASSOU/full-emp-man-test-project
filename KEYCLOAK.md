# Keycloak Local Setup

This project uses Keycloak as its identity provider. Postgres and MinIO run natively — Keycloak does too.

## Prerequisites

- Java 21 (already required by the Spring Boot backend)

## Install & Run

1. Download Keycloak **26.x** from https://www.keycloak.org/downloads (choose the **ZIP** distribution).
2. Extract to a permanent location, e.g. `C:\keycloak`.
3. Set initial admin credentials (first run only):

```powershell
$env:KC_BOOTSTRAP_ADMIN_USERNAME = "admin"
$env:KC_BOOTSTRAP_ADMIN_PASSWORD = "admin"
```

4. Start Keycloak in dev mode on port 9090:

```powershell
C:\keycloak\bin\kc.bat start-dev --http-port=9090
```

5. Open http://localhost:9090 and log in with `admin` / `admin`.

## Import the Realm

1. In the admin console, click the realm dropdown (top-left) → **Create Realm**.
2. Click **Browse**, select `keycloak-realm-export.json` from the project root.
3. Click **Create**.

This imports:
- Realm `employee-management`
- Client `emp-frontend` (public, PKCE, redirect to `http://localhost:5173/*`)
- Client `app-backend` (confidential service account for the backend's Admin API calls)
- Realm roles `SUPER_ADMIN` and `NORMAL_USER`
- Two admin users: `admin1@company.com` / `Admin123!` and `admin2@company.com` / `Admin123!`

## After Import — Set the Backend Client Secret

The realm export contains a placeholder secret for `app-backend`. You need a real one:

1. Go to **Clients** → `app-backend` → **Credentials** tab.
2. Click **Regenerate** to get a new client secret.
3. Copy the secret into your `application.properties`:

```properties
keycloak.admin.client-secret=<paste here>
```

## Ports

| Service     | Port |
|-------------|------|
| Spring Boot | 8080 |
| Keycloak    | 9090 |
| Vue dev     | 5173 |
| PostgreSQL  | 5432 |
| MinIO       | 9000 |
