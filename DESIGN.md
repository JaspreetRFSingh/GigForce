# GigForce — Architecture & Design Document

---

## 1. Multi-tenancy strategy

GigForce uses **row-level tenant isolation** with a `tenant_id` discriminator
column on every tenant-scoped entity. All queries are automatically scoped
via the `TenantFilter` → `TenantContext` → repository pattern.

```
Incoming HTTP request
       |
       v
 TenantFilter (OncePerRequestFilter)
       |  reads X-Tenant-ID header
       |  validates tenant is active in DB
       |  calls TenantContext.set(tenantId)   <-- ThreadLocal
       v
 JwtAuthFilter
       |  validates Bearer token
       |  sets SecurityContextHolder
       v
 Controller  -->  Service  -->  Repository
                                    |
                              WHERE tenant_id = TenantContext.get()
       |
       v  (finally block)
 TenantContext.clear()   <-- prevents ThreadLocal leaks in pooled threads
```

**Why row-level for local dev?**
Schema-per-tenant requires DDL execution per tenant at startup and complicates
H2 in-memory setup. Row-level isolation provides the same conceptual
demonstration with full correctness guarantees. The production extension
point (see `TenantContext` Javadoc) describes the `DataSourceRouter` pattern
for true schema isolation on Azure PostgreSQL Flexible Server.

**Tenant ID as slug:** tenants are identified by a slug string (e.g. `acme`)
rather than a UUID, which makes headers readable and mirrors how real SaaS
platforms expose tenant identifiers in subdomains and API paths.

---

## 2. JWT authentication

```
Client                          GigForce
  |                                 |
  |-- POST /api/auth/login -------->|
  |   { email, password }           |  AuthService.login()
  |                                 |    -> BCrypt.verify(password)
  |                                 |    -> JwtUtil.generateToken(user, tenantId, role)
  |<-- { token, expiresIn, ... } ---|
  |                                 |
  |-- GET /api/contacts ----------->|
  |   Authorization: Bearer <token> |  JwtAuthFilter
  |   X-Tenant-ID: acme             |    -> JwtUtil.extractUsername()
  |                                 |    -> UserDetailsService.loadByUsername()
  |                                 |    -> SecurityContextHolder.set()
  |                                 |  TenantFilter
  |                                 |    -> TenantContext.set("acme")
  |                                 |  ContactController
  |<-- [ { id, name, ... }, ... ] --|    -> contactService.findAll()
```

**JWT claims structure:**
```json
{
  "sub":      "admin@acme.com",
  "tenantId": "acme",
  "role":     "OWNER",
  "iat":      1704067200,
  "exp":      1704153600
}
```

Default expiry: 24 hours (configured via `app.jwt.expiration`).

---

## 3. Domain model overview

```
Tenant 1---* User
Tenant 1---* Contact
Tenant 1---* Deal  *---1 Contact (optional reference)
Tenant 1---* Invoice 1---* InvoiceItem
Tenant 1---* NotificationLog
```

All tenant-scoped entities extend `BaseEntity`, which provides:
- UUID primary key (auto-generated via JPA)
- `tenantId` column (set by service layer from TenantContext)
- `createdAt` and `updatedAt` (managed by Spring JPA auditing)

---

## 4. Sales pipeline

Deal stages form a directed graph:

```
LEAD  -->  QUALIFIED  -->  PROPOSAL  -->  NEGOTIATION  -->  CLOSED_WON
                                                       -->  CLOSED_LOST
```

Stage transitions are handled by `DealService.updateStage()`. When a deal
reaches `CLOSED_WON` or `CLOSED_LOST`, the `closedAt` date is set automatically.
Every transition publishes a `NotificationEvent` consumed by the async listener.

---

## 5. Invoice lifecycle

```
DRAFT  -->  SENT  -->  PAID
             |
             v
           OVERDUE
```

State transitions are enforced by `InvoiceService`:
- Only `DRAFT` invoices can be sent
- Only `SENT` invoices can be marked paid
- `PAID` invoices cannot be marked overdue
- Only `DRAFT` invoices can be deleted

Invoice totals are computed server-side on creation:
```
subtotal   = SUM(item.quantity * item.unitPrice)
taxAmount  = subtotal * (taxRate / 100)
total      = subtotal + taxAmount
```

Invoice numbers are auto-generated per tenant:
`INV-{TENANT_UPPER}-{padded-sequence}` e.g. `INV-ACME-0001`

---

## 6. Event-driven notification pipeline

GigForce replaces Kafka with Spring's `ApplicationEventPublisher` for local
development. The design is isomorphic — swapping the transport is a one-line
annotation change.

**Local (this project):**
```java
// Publisher
events.publishEvent(new NotificationEvent(this, tenantId, "DEAL", message));

// Consumer
@EventListener
@Async
public void onNotification(NotificationEvent event) { ... }
```

**Production (Kafka / Azure Service Bus):**
```java
// Publisher
kafkaTemplate.send("gigforce-events", event);

// Consumer
@KafkaListener(topics = "gigforce-events", groupId = "notification-service")
public void onNotification(NotificationEvent event) { ... }
```

The `@Async` annotation ensures the listener runs on a separate thread pool
(configured via `spring.task.execution.pool`), not blocking the HTTP response.

---

## 7. Caching

`AnalyticsService.getDashboardStats()` is annotated `@Cacheable` with a
composite key including the tenant ID:

```java
@Cacheable(value = "dashboard",
           key = "#root.methodName + '_' + T(com.gigforce.multitenancy.TenantContext).get()")
```

This prevents tenant A's cached dashboard from being served to tenant B.
In production, replace Spring's in-memory cache with Azure Cache for Redis
by adding the `spring-boot-starter-data-redis` dependency and updating
`spring.cache.type=redis` in application.yml.

---

## 8. API design principles

- **REST conventions** — nouns for resources, HTTP verbs for actions
- **Uniform envelope** — every response is `ApiResponse<T>` with success, message, data, timestamp
- **Validation at the boundary** — `@Valid` + Bean Validation annotations on all request DTOs
- **Structured error responses** — `GlobalExceptionHandler` maps exceptions to HTTP status codes
- **Tenant-scoped 404s** — a resource from another tenant returns 404 (not 403) to avoid
  leaking resource existence — the same security pattern Salesforce applies in their API

---

## 9. Layered architecture

```
Controller layer         HTTP request/response, validation, routing
      |
      v
Service layer            Business logic, transactions, event publishing
      |
      v
Repository layer         JPA queries, always scoped to TenantContext.get()
      |
      v
Entity / Domain layer    JPA entities, value objects, enums
```

Cross-cutting concerns (auth, tenancy, caching, auditing) are handled by
filters, AOP proxies, and Spring Boot auto-configuration — keeping the
domain layers clean.

---

## 10. Production architecture on Azure

```
Azure Front Door (CDN + WAF)
       |
Azure API Management (rate limiting, API keys)
       |
Azure Kubernetes Service (AKS)
       |-- gigforce-crm-service          (Spring Boot)
       |-- gigforce-invoice-service      (Spring Boot)
       |-- gigforce-analytics-service    (Spring Boot)
       |-- gigforce-notification-service (Spring Boot)
       |
Azure PostgreSQL Flexible Server (schema-per-tenant)
Azure Cache for Redis (multi-tenant session + query cache)
Azure Service Bus (event bus replacing ApplicationEventPublisher)
Azure Blob Storage (invoice PDFs, attachments)
Azure AD B2C (OAuth2 / OIDC for tenant SSO)
Azure Monitor + Application Insights (distributed tracing)
```
