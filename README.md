# GigForce 🚀

> **Multi-tenant SaaS CRM for freelancers** — a portfolio project demonstrating
> enterprise-grade Java engineering for production-ready SaaS platforms.

---
## The concept
GigForce applies the architecture of leading enterprise SaaS platforms to the
gig economy. It is a fully functional CRM where multiple freelancer teams (tenants)
manage contacts, track deals through a 6-stage sales pipeline, issue invoices,
and view revenue analytics — all isolated from each other via a
**multi-tenant architecture** backed by JWT authentication and event-driven
notifications.

Built on the architectural patterns found in best-in-class CRM and cloud platforms:
multi-tenancy, API-first design, event-driven microservice communication, and
layered domain separation.

---

## Tech stack
| Layer            | Technology                                       |
|------------------|--------------------------------------------------|
| Language         | Java 21 (records, sealed types, text blocks)     |
| Framework        | Spring Boot 3.2 · Spring Security · Spring JPA   |
| Auth             | JWT (jjwt 0.12) + BCrypt                         |
| Database         | H2 (embedded — zero install)                     |
| Events           | Spring ApplicationEvent (async)                  |
| Cache            | Spring simple cache (in-memory)                  |
| API docs         | SpringDoc OpenAPI 2 / Swagger UI                 |
| Build            | Maven 3.9+                                       |

---

## Prerequisites

- **Java 21** — `java -version`
- **Maven 3.9+** — `mvn -version`

That is all. H2 is embedded — no database setup required.

---

## Quick start

```bash
# 1. Unzip and enter the project
cd gigforce

# 2. Run
mvn spring-boot:run

# 3. Open Swagger UI (interactive API explorer)
open http://localhost:8080/swagger-ui.html

# 4. H2 console (inspect live data)
open http://localhost:8080/h2-console
#   JDBC URL : jdbc:h2:mem:gigforcedb
#   User     : sa
#   Password : (leave blank)
```

---

## Seeded credentials

Two tenants are pre-loaded on every startup:

| Tenant | Email              | Password    | Plan    |
|--------|--------------------|-------------|---------|
| acme   | admin@acme.com     | password123 | PRO     |
| solo   | admin@solo.com     | password123 | STARTER |

---

## Authentication flow

```
1. POST /api/auth/login         → { token, expiresIn, tenantId }
2. Copy the token
3. All protected requests need:
      Authorization: Bearer <token>
      X-Tenant-ID:   acme
```

In Swagger UI: click **Authorize**, paste the token, then set `X-Tenant-ID` as
a header on each request.

---

## API modules

| Module        | Base path            | Description                          |
|---------------|----------------------|--------------------------------------|
| Auth          | /api/auth            | Register, login                      |
| Tenants       | /api/tenants         | Tenant management                    |
| Contacts      | /api/contacts        | CRM contacts (tenant-scoped)         |
| Deals         | /api/deals           | Sales pipeline (6 stages)            |
| Invoices      | /api/invoices        | Invoice lifecycle (DRAFT to PAID)    |
| Analytics     | /api/analytics       | Dashboard stats, revenue             |
| Notifications | /api/notifications   | In-app event log                     |

---

## Project structure

```
gigforce/
├── pom.xml
├── README.md
├── DESIGN.md
├── API.md
└── src/
    ├── main/
    │   ├── java/com/gigforce/
    │   │   ├── GigForceApplication.java     Entry point
    │   │   ├── DataSeeder.java              Demo data on startup
    │   │   ├── config/                      Security, JWT filter, OpenAPI
    │   │   ├── multitenancy/                TenantContext + TenantFilter
    │   │   ├── common/                      BaseEntity, ApiResponse, ExceptionHandler
    │   │   ├── auth/                        JWT auth, User model
    │   │   ├── tenant/                      Tenant CRUD
    │   │   ├── crm/
    │   │   │   ├── contact/                 Contact CRUD
    │   │   │   └── deal/                    6-stage pipeline
    │   │   ├── invoice/                     Invoice lifecycle
    │   │   ├── analytics/                   Dashboard stats
    │   │   └── notification/                Async event log
    │   └── resources/
    │       └── application.yml
    └── test/
        └── java/com/gigforce/
            ├── GigForceApplicationTests.java
            ├── auth/AuthServiceTest.java
            ├── crm/ContactServiceTest.java
            └── invoice/InvoiceServiceTest.java
```

---

## Running tests

```bash
mvn test
```

---

## Production extension points

| Feature       | Local (this project)          | Production on Azure                           |
|---------------|-------------------------------|-----------------------------------------------|
| Database      | H2 embedded                   | Azure PostgreSQL Flexible Server              |
| Multi-tenancy | Row-level (tenant_id col)     | Schema-per-tenant + DataSourceRouter          |
| Events        | Spring ApplicationEvent       | Azure Service Bus / Kafka on HDInsight        |
| Cache         | Spring in-memory              | Azure Cache for Redis                         |
| Storage       | Not applicable                | Azure Blob Storage                            |
| Auth          | Local JWT                     | Azure AD B2C / OAuth2                         |

See **DESIGN.md** for the full architecture walkthrough.
