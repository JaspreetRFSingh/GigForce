# GigForce REST API Reference

**Base URL:** `http://localhost:8080`  
**Interactive docs:** `http://localhost:8080/swagger-ui.html`

All protected endpoints require:
```
Authorization: Bearer <jwt_token>
X-Tenant-ID:   acme
Content-Type:  application/json
```

All responses follow the envelope:
```json
{ "success": true, "message": "...", "data": { }, "timestamp": "..." }
```

---

## Auth  `/api/auth`

### POST /register
```json
{
  "name":     "Jaspreet Singh",
  "email":    "jaspreet@acme.com",
  "password": "secret123",
  "tenantId": "acme"
}
```

### POST /login
```json
{ "email": "admin@acme.com", "password": "password123" }
```
Response `data`:
```json
{
  "token":     "eyJ...",
  "expiresIn": 86400,
  "email":     "admin@acme.com",
  "name":      "Jaspreet Singh",
  "tenantId":  "acme",
  "role":      "OWNER"
}
```

---

## Tenants  `/api/tenants`

| Method | Path    | Description       |
|--------|---------|-------------------|
| GET    | /       | List all tenants  |
| GET    | /{id}   | Get a tenant      |
| POST   | /       | Create tenant     |
| PUT    | /{id}   | Update tenant     |

### Tenant payload
```json
{ "id": "startup", "name": "My Startup", "plan": "PRO", "active": true }
```

---

## Contacts  `/api/contacts`

| Method | Path    | Description              |
|--------|---------|--------------------------|
| GET    | /       | List (tenant-scoped)     |
| POST   | /       | Create contact           |
| GET    | /{id}   | Get contact              |
| PUT    | /{id}   | Update contact           |
| DELETE | /{id}   | Delete contact           |

### Contact payload
```json
{
  "name":    "Alice Sharma",
  "email":   "alice@client.com",
  "phone":   "+91-9876543210",
  "company": "Client Corp",
  "notes":   "Met at DevFest 2024"
}
```

---

## Deals  `/api/deals`

| Method | Path          | Description              |
|--------|---------------|--------------------------|
| GET    | /             | List deals               |
| POST   | /             | Create deal              |
| GET    | /{id}         | Get deal                 |
| PUT    | /{id}         | Update deal              |
| PATCH  | /{id}/stage   | Advance stage            |
| DELETE | /{id}         | Delete deal              |

### Deal payload
```json
{
  "title":     "API Integration Project",
  "value":     150000.00,
  "stage":     "PROPOSAL",
  "contactId": "uuid-of-contact",
  "notes":     "Client wants delivery by Q2"
}
```

### Stage values
```
LEAD  ->  QUALIFIED  ->  PROPOSAL  ->  NEGOTIATION  ->  CLOSED_WON
                                                    ->  CLOSED_LOST
```

### Advance stage
```
PATCH /api/deals/{id}/stage?stage=NEGOTIATION
```

---

## Invoices  `/api/invoices`

| Method | Path              | Description                |
|--------|-------------------|----------------------------|
| GET    | /                 | List invoices              |
| POST   | /                 | Create (DRAFT)             |
| GET    | /{id}             | Get invoice                |
| PATCH  | /{id}/send        | DRAFT -> SENT              |
| PATCH  | /{id}/pay         | SENT -> PAID               |
| PATCH  | /{id}/overdue     | Mark OVERDUE               |
| DELETE | /{id}             | Delete DRAFT only          |

### Invoice payload
```json
{
  "clientName":  "Client Corp",
  "clientEmail": "billing@client.com",
  "dueDate":     "2025-03-31",
  "taxRate":     18.0,
  "items": [
    { "description": "API Integration", "quantity": 10, "unitPrice": 5000.00 },
    { "description": "Code Review",     "quantity": 2,  "unitPrice": 2500.00 }
  ]
}
```

### Server-computed totals (returned in response)
```
subtotal   = SUM(quantity * unitPrice)     = 55,000
taxAmount  = subtotal * (taxRate / 100)    = 9,900
total      = subtotal + taxAmount          = 64,900
```

---

## Analytics  `/api/analytics`

| Method | Path        | Description                              |
|--------|-------------|------------------------------------------|
| GET    | /dashboard  | Contacts, deal pipeline, revenue totals  |

### Dashboard response
```json
{
  "totalContacts":   12,
  "totalDeals":      8,
  "openDeals":       5,
  "wonRevenue":      950000.00,
  "totalInvoices":   6,
  "paidInvoices":    3,
  "overdueInvoices": 1,
  "paidRevenue":     185000.00,
  "dealsByStage": {
    "LEAD":        1,
    "QUALIFIED":   2,
    "PROPOSAL":    1,
    "NEGOTIATION": 1,
    "CLOSED_WON":  2,
    "CLOSED_LOST": 1
  }
}
```

---

## Notifications  `/api/notifications`

| Method | Path | Description                         |
|--------|------|-------------------------------------|
| GET    | /    | Last 20 events for this tenant      |

### Notification log entry
```json
{
  "id":        "uuid",
  "tenantId":  "acme",
  "type":      "DEAL",
  "message":   "Deal 'Cloud Migration' moved: PROPOSAL -> CLOSED_WON",
  "createdAt": "2024-01-15T10:30:00Z"
}
```

Event types: `DEAL` · `INVOICE` · `CONTACT` · `SYSTEM`
