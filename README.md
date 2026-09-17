# Employee Management System

A full-stack, enterprise-grade Employee Management Application built with **Spring Boot 3**, **Keycloak 26**, **Vue 3**, **PostgreSQL**, and **MinIO S3 Storage**, fully containerized with **Docker Compose**.

---

## 🚀 Quick Start (Single Command)

Ensure Docker Desktop is running, then run:

```bash
# 1. Copy environment template
cp .env.example .env

# 2. Launch the entire container stack
docker compose up -d
```

That's it! Docker will pull dependencies, build images, auto-import the Keycloak realm, auto-create the MinIO storage bucket, and start all services.

---

## 🌐 Access Links & Default Credentials

| Service | URL | Credentials |
|---|---|---|
| **Frontend Web App** | [http://localhost:5173](http://localhost:5173) | See Test Accounts below |
| **Keycloak Admin Console** | [http://localhost:9090](http://localhost:9090) | Username: `admin`<br>Password: `admin` |
| **MinIO Console** | [http://localhost:9001](http://localhost:9001) | Username: `minioadmin`<br>Password: `minioadmin` |
| **Backend REST API Docs** | [http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html) | Open API / Swagger UI |

### Pre-configured Test Accounts

| Account | Email | Password | Role | Permissions |
|---|---|---|---|---|
| **Super Admin** | `admin1@company.com` | `Admin123!` | `SUPER_ADMIN` | Full Access (Create, Read, Update, Delete, User Management) |
| **Normal User** | `user2@company.com` | `151515` | `NORMAL_USER` | Configurable Granular Permissions (`canRead`, `canUpdate`) |

---

## 🛠️ Architecture & Tech Stack

```
[ Vue 3 + Vite Frontend ] ──(HTTP / WS)──> [ Nginx Reverse Proxy ]
                                                  │
                                                  ├──> [ Spring Boot 3 API ] ──> [ PostgreSQL 16 ]
                                                  │         │
                                                  │         ├──> [ MinIO S3 Object Storage ]
                                                  │         └──> [ Mailtrap SMTP ]
                                                  │
                                                  └──> [ Keycloak 26 Identity Server ]
```

### Backend
- **Java 21** & **Spring Boot 3.4**
- **Spring Security 6** (OAuth2 Resource Server validating Keycloak JWT tokens)
- **Spring Data JPA** & **Hibernate** with PostgreSQL 16
- **MinIO Java SDK** for S3 document storage (Profile Photos & PDF Resumes)
- **iText PDF Generator** for dynamic employment contracts
- **Spring WebSocket / STOMP** over SockJS for real-time notification broadcasting

### Frontend
- **Vue 3** (Composition API) & **Vite 8**
- **Pinia** State Management & **Vue Router 4**
- **Axios** with JWT Request Interceptors & 401 Silent Token Refresh
- **Nginx Alpine** Production Reverse Proxy with Gzip compression and WebSocket upgrading

---

## ✨ Key Features

- **Authentication & Identity**: OpenID Connect (OIDC) & OAuth2 Password Direct Grant authentication managed securely by Keycloak 26.
- **Dynamic RBAC & ABAC Permissions**: Super Admins can dynamically toggle `canCreate`, `canRead`, `canUpdate`, and `canDelete` permissions for normal users in real-time.
- **Fail-Closed Security**: API endpoints strictly validate dynamic permissions via `PermissionChecker`; unauthorized actions return `403 Forbidden`.
- **Document & Media Management**: Upload, preview, and download employee profile pictures and PDF CVs stored in MinIO.
- **Employment Contract Generation**: On-the-fly PDF contract rendering with customized employee salary, position, and start dates.
- **Real-Time WebSocket Notifications**: Instant popup notifications and unread badge counters broadcast across connected client sessions.
- **Mailtrap SMTP Integration**: Password reset request workflow dispatched via Mailtrap SMTP.

---

## 📁 Repository Structure

```
├── docker-compose.yml              # Complete 5-service orchestration
├── .env.example                    # Environment secrets template
├── keycloak-realm-export.json      # Auto-imported Keycloak realm & users
│
├── back/employee-management/       # Spring Boot 3 Backend
│   ├── Dockerfile                  # Multi-stage Maven/JDK build
│   └── src/main/java/com/abdel/employee_management/
│       ├── config/                 # OpenApi, WebSocket, DataSeeder
│       ├── controller/             # REST Endpoints
│       ├── dto/                    # Request & Response Data Transfer Objects
│       ├── model/                  # JPA Entities (Employee, User, Notification)
│       ├── repository/             # Spring Data Repositories
│       ├── security/               # OAuth2 JWT Converter & PermissionChecker
│       └── service/impl/           # Service Interfaces & Implementations
│
└── front/employee-management-frontend/  # Vue 3 Frontend
    ├── Dockerfile                  # Multi-stage Node/Nginx build
    ├── nginx.conf                  # Production SPA & Reverse Proxy config
    └── src/
        ├── api/                    # Axios HTTP Client Modules
        ├── components/             # Reusable UI Components & Modals
        ├── composables/            # WebSocket STOMP Hooks
        ├── stores/                 # Pinia Auth & Toast Stores
        └── views/                  # Main Application Views (Login, Employees, Users)
```

---


