# 🔐 Spring Boot Authentication Server

A **production-grade authentication system** built with Spring Boot 3, featuring JWT authentication, refresh token rotation, role-based access control, and Google OAuth2 login.

![Java](https://img.shields.io/badge/Java-17-orange?style=flat-square&logo=openjdk)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.2-brightgreen?style=flat-square&logo=springboot)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-15-blue?style=flat-square&logo=postgresql)
![Redis](https://img.shields.io/badge/Redis-7-red?style=flat-square&logo=redis)
![Docker](https://img.shields.io/badge/Docker-Compose-2496ED?style=flat-square&logo=docker)
![License](https://img.shields.io/badge/License-MIT-yellow?style=flat-square)

---

## 📋 Table of Contents

- [Features](#-features)
- [Tech Stack](#-tech-stack)
- [Architecture](#-architecture)
- [Project Structure](#-project-structure)
- [Getting Started](#-getting-started)
- [Environment Variables](#-environment-variables)
- [API Reference](#-api-reference)
- [Auth Flows](#-auth-flows)
- [Security](#-security)
- [Running with Docker](#-running-with-docker)

---

## ✨ Features

- ✅ Email & password registration and login
- ✅ JWT access tokens (1 hour expiry)
- ✅ Refresh tokens stored in Redis (7 day expiry)
- ✅ Refresh token rotation on every use
- ✅ Secure logout — invalidates refresh token in Redis
- ✅ Role-based access control (`ROLE_USER` / `ROLE_ADMIN`)
- ✅ Google OAuth2 login
- ✅ Global exception handling with clean error responses
- ✅ Request logging with duration tracking
- ✅ Fully Dockerized (app + PostgreSQL + Redis)
- ✅ Input validation on all endpoints

---

## 🛠 Tech Stack

| Layer | Technology |
|-------|-----------|
| Language | Java 17 |
| Framework | Spring Boot 3.2 |
| Security | Spring Security 6 |
| Database | PostgreSQL 15 |
| Cache / Tokens | Redis 7 |
| ORM | Spring Data JPA (Hibernate) |
| JWT Library | JJWT 0.12.3 |
| OAuth2 | Google OAuth2 via Spring OAuth2 Client |
| Build Tool | Maven |
| Containerization | Docker & Docker Compose |

---

## 🏗 Architecture

```
┌─────────────────────────────────────────────────┐
│                  Client (HTTP)                   │
└─────────────────────┬───────────────────────────┘
                      │
                      ▼
┌─────────────────────────────────────────────────┐
│              JwtAuthenticationFilter             │
│         (validates Bearer token on every req)    │
└─────────────────────┬───────────────────────────┘
                      │
                      ▼
┌─────────────────────────────────────────────────┐
│               Spring Security                    │
│      (RBAC, endpoint protection, OAuth2)         │
└──────┬──────────────────────────────┬───────────┘
       │                              │
       ▼                              ▼
┌─────────────┐               ┌──────────────┐
│AuthController│               │UserController│
└──────┬──────┘               └──────┬───────┘
       │                              │
       ▼                              │
┌─────────────┐                       │
│  AuthService│◄──────────────────────┘
└──────┬──────┘
       │
  ┌────┴────┐
  ▼          ▼
Redis     PostgreSQL
(refresh  (users,
 tokens)   roles)
```

**Authentication Flows:**
- **Local Auth** → Register/Login → Returns JWT access token + refresh token
- **Google OAuth2** → Redirect to Google → Callback → Returns JWT + refresh token
- **Token Refresh** → Validate refresh token in Redis → Issue new pair (rotation)
- **Logout** → Delete refresh token from Redis

---

## 📁 Project Structure

```
src/main/java/com/project/auth/
├── AuthServerApplication.java
├── config/
│   ├── RedisConfig.java          # StringRedisTemplate bean
│   └── SecurityConfig.java       # Security filter chain, OAuth2, RBAC
├── controller/
│   ├── AuthController.java       # /api/auth/** endpoints
│   └── UserController.java       # /api/user/** protected endpoints
├── dto/
│   ├── request/
│   │   ├── LoginRequest.java
│   │   ├── RegisterRequest.java
│   │   └── RefreshTokenRequest.java
│   └── response/
│       ├── AuthResponse.java
│       └── ErrorResponse.java
├── entity/
│   ├── User.java                 # JPA entity
│   ├── Role.java                 # ROLE_USER, ROLE_ADMIN
│   └── AuthProvider.java         # LOCAL, GOOGLE
├── exception/
│   ├── GlobalExceptionHandler.java
│   ├── UserAlreadyExistsException.java
│   └── InvalidTokenException.java
├── repository/
│   └── UserRepository.java
├── security/
│   ├── CustomUserDetails.java
│   ├── CustomUserDetailsService.java
│   ├── JwtAuthenticationEntryPoint.java
│   ├── JwtAuthenticationFilter.java
│   ├── JwtTokenProvider.java
│   └── oauth2/
│       ├── CustomOAuth2User.java
│       ├── CustomOAuth2UserService.java
│       ├── OAuth2AuthenticationSuccessHandler.java
│       └── OAuth2AuthenticationFailureHandler.java
├── service/
│   ├── AuthService.java
│   └── RefreshTokenService.java
└── util/
    └── RequestLoggingFilter.java
```

---

## 🚀 Getting Started

### Prerequisites

- [Java 17+](https://adoptium.net/)
- [Maven 3.9+](https://maven.apache.org/)
- [Docker Desktop](https://www.docker.com/products/docker-desktop/)
- [Google OAuth2 credentials](https://console.cloud.google.com/) *(for OAuth2 login)*

---

### 1 — Clone the Repository

```bash
git clone https://github.com/pnkjxmwl/SpringBootAuthServer.git
cd SpringBootAuthServer
```

---

### 2 — Create Your `.env` File

Create a `.env` file in the project root (this file is git-ignored):

```env
JWT_SECRET=your-base64-encoded-secret-minimum-32-bytes
GOOGLE_CLIENT_ID=your-google-client-id
GOOGLE_CLIENT_SECRET=your-google-client-secret
```

> **Generate a JWT secret:**
> ```bash
> openssl rand -base64 32
> ```

---

### 3 — Start Infrastructure (PostgreSQL + Redis)

```bash
docker-compose up -d postgres redis
```

---

### 4 — Run the Application

```bash
mvn spring-boot:run
```

Application starts at `http://localhost:8080`

---

## 🔑 Environment Variables

| Variable | Required | Description |
|----------|----------|-------------|
| `JWT_SECRET` | ✅ | Base64-encoded secret, minimum 32 bytes |
| `GOOGLE_CLIENT_ID` | ✅ for OAuth2 | From Google Cloud Console |
| `GOOGLE_CLIENT_SECRET` | ✅ for OAuth2 | From Google Cloud Console |

> ⚠️ **Never hardcode secrets in `application.yml` or commit them to Git.**

---

## 📡 API Reference

### Auth Endpoints — Public

| Method | Endpoint | Description |
|--------|----------|-------------|
| `POST` | `/api/auth/register` | Register a new user |
| `POST` | `/api/auth/login` | Login, returns JWT + refresh token |
| `POST` | `/api/auth/refresh` | Refresh access token (rotation) |
| `POST` | `/api/auth/logout` | Logout, invalidates refresh token |
| `GET`  | `/oauth2/authorize/google` | Initiate Google OAuth2 login |

### User Endpoints — Protected

| Method | Endpoint | Role Required | Description |
|--------|----------|---------------|-------------|
| `GET` | `/api/user/profile` | `USER` or `ADMIN` | Get current user profile |
| `GET` | `/api/user/admin/dashboard` | `ADMIN` only | Admin dashboard |
| `GET` | `/api/user/admin/users` | `ADMIN` only | User management |

---

### Register

```http
POST /api/auth/register
Content-Type: application/json

{
  "email": "user@example.com",
  "password": "password123"
}
```

**Response `201 Created`:**
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
  "refreshToken": "550e8400-e29b-41d4-a716-446655440000",
  "tokenType": "Bearer",
  "email": "user@example.com",
  "role": "ROLE_USER"
}
```

---

### Login

```http
POST /api/auth/login
Content-Type: application/json

{
  "email": "user@example.com",
  "password": "password123"
}
```

**Response `200 OK`:** Same as register response.

---

### Refresh Token

```http
POST /api/auth/refresh
Content-Type: application/json

{
  "refreshToken": "550e8400-e29b-41d4-a716-446655440000"
}
```

**Response `200 OK`:** New `accessToken` + new `refreshToken` (old one is deleted).

---

### Logout

```http
POST /api/auth/logout
Content-Type: application/json

{
  "refreshToken": "550e8400-e29b-41d4-a716-446655440000"
}
```

**Response `200 OK`:**
```json
{
  "message": "Logged out successfully"
}
```

---

### Accessing Protected Endpoints

Include the access token in every request header:

```http
GET /api/user/profile
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

---

### Error Response Format

All errors follow a consistent format:

```json
{
  "status": 401,
  "message": "Invalid or expired refresh token",
  "timestamp": "2026-06-09 05:30:00"
}
```

| Status | Meaning |
|--------|---------|
| `400` | Validation failed (bad input) |
| `401` | Unauthorized (missing/invalid/expired token) |
| `403` | Forbidden (insufficient role) |
| `404` | User not found |
| `409` | Email already registered |
| `500` | Internal server error |

---

## 🔄 Auth Flows

### Local Registration & Login

```
Client                    Server                   PostgreSQL       Redis
  │                          │                          │             │
  │── POST /auth/register ──►│                          │             │
  │                          │── save user ────────────►│             │
  │                          │── generate JWT ──────────────────────► store refresh token
  │◄── accessToken + ────────│                          │             │
  │    refreshToken          │                          │             │
  │                          │                          │             │
  │── POST /auth/login ─────►│                          │             │
  │                          │── verify password ──────►│             │
  │                          │── generate JWT ──────────────────────► store refresh token
  │◄── accessToken + ────────│                          │             │
  │    refreshToken          │                          │             │
```

### Token Refresh (Rotation)

```
Client                    Server                              Redis
  │                          │                                  │
  │── POST /auth/refresh ───►│                                  │
  │   { refreshToken: X }    │── validate token X ─────────────►│
  │                          │◄─ returns email ─────────────────│
  │                          │── delete token X ───────────────►│
  │                          │── store new token Y ────────────►│
  │◄── new accessToken ──────│                                  │
  │    new refreshToken Y    │                                  │
```

### Google OAuth2

```
Client          Server              Google            Server
  │                │                   │                 │
  │── GET /oauth2/authorize/google ───►│                 │
  │                │──── redirect ─────►│                 │
  │◄───────────────────── Google login page              │
  │── user logs in with Google ────────►│                 │
  │                │◄── auth code ──────│                 │
  │                │── exchange for user info ───────────►│
  │                │◄── email, profile ──────────────────│
  │                │── find or create user               │
  │                │── generate JWT                      │
  │◄── redirect to frontend with accessToken + refreshToken
```

---

## 🛡 Security

### What's Protected

- Passwords hashed with **BCrypt** (strength 10) — never stored in plain text
- JWTs signed with **HMAC-SHA256** using a secret key
- Refresh tokens are **random UUIDs** stored in Redis — not JWTs
- **Token rotation** — every refresh issues a new pair and deletes the old one
- **Logout** immediately invalidates the refresh token in Redis
- `@PreAuthorize` enforces role checks at the method level
- All error responses sanitized — no stack traces exposed to clients
- Non-root user inside Docker container

### Token Expiry

| Token | Expiry | Storage |
|-------|--------|---------|
| Access Token (JWT) | 1 hour | Client only |
| Refresh Token (UUID) | 7 days | Redis (server-side) |

### Redis Key Format

```
refresh_token:<uuid>  →  user@email.com  (TTL: 7 days)
```

---

## 🐳 Running with Docker

### Full Stack (App + PostgreSQL + Redis)

```bash
# Build and start everything
docker-compose --env-file .env up --build -d

# View logs
docker-compose logs -f app

# Check status
docker-compose ps
```

### Useful Commands

```bash
# Stop everything (keep data)
docker-compose down

# Stop and wipe all data
docker-compose down -v

# Rebuild only the app
docker-compose up --build -d app

# Restart app only
docker-compose restart app
```

### Google OAuth2 Setup

1. Go to [Google Cloud Console](https://console.cloud.google.com/)
2. Create a project → **APIs & Services → Credentials**
3. Create **OAuth 2.0 Client ID** → Web application
4. Add authorized redirect URI:
   ```
   http://localhost:8080/login/oauth2/code/google
   ```
5. Copy **Client ID** and **Client Secret** into your `.env` file
6. After login, user is redirected to:
   ```
   http://localhost:3000/oauth2/callback?accessToken=...&refreshToken=...
   ```

> Update `app.oauth2.redirect-uri` in `application.yml` to match your frontend URL.

---

## 📄 License

This project is licensed under the MIT License.

---

<div align="center">
  Built with ☕ Java and Spring Boot
</div>