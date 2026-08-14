# REST API Specification & Directory

## 1. Authentication & User APIs (`/api/v1/auth`)

| Method | Path | Required Role | Summary |
|---|---|---|---|
| `POST` | `/api/v1/auth/login` | *Public (PermitAll)* | Authenticate credentials and obtain JWT Bearer Token |
| `GET` | `/api/v1/auth/me` | *Any Authenticated* | Retrieve currently logged in user profile and authorities |

### Login Request / Response Payload Example:
```json
// POST /api/v1/auth/login
{
  "username": "sc_staff",
  "password": "password123"
}

// Response 200 OK
{
  "token": "eyJhbGciOiJIUzUxMiJ9...",
  "type": "Bearer",
  "user": {
    "id": 2,
    "username": "sc_staff",
    "fullName": "Le Van C",
    "roles": ["ROLE_SC_STAFF"]
  }
}
```

---

## 2. Vehicle Management APIs (`/api/v1/sc/vehicles`)

| Method | Path | Required Role | Summary |
|---|---|---|---|
| `GET` | `/api/v1/sc/vehicles` | `SC_STAFF`, `ADMIN` | List vehicles with pagination & keyword search |
| `POST` | `/api/v1/sc/vehicles` | `SC_STAFF`, `ADMIN` | Register a new electric vehicle |
| `GET` | `/api/v1/sc/vehicles/{id}` | `SC_STAFF`, `ADMIN` | Get comprehensive vehicle details by ID |
| `PUT` | `/api/v1/sc/vehicles/{id}` | `SC_STAFF`, `ADMIN` | Update vehicle technical specs and metadata |
| `DELETE` | `/api/v1/sc/vehicles/{id}` | `ADMIN` | Delete vehicle record (Admin only) |
| `GET` | `/api/v1/sc/vehicles/search?vin={vin}` | `SC_STAFF`, `ADMIN` | Find vehicle by 17-character VIN |
| `PATCH` | `/api/v1/sc/vehicles/{id}/mileage` | `SC_STAFF`, `SC_TECHNICIAN`, `ADMIN` | Update vehicle odometer reading in km |
| `GET` | `/api/v1/sc/vehicles/{id}/warranty-status` | `SC_STAFF`, `ADMIN` | Inspect active warranty validity |

---

## 3. Serial Parts Tracking APIs (`/api/v1/sc/vehicles/{vehicleId}/parts`)

| Method | Path | Required Role | Summary |
|---|---|---|---|
| `GET` | `/api/v1/sc/vehicles/{vehicleId}/parts` | `SC_STAFF`, `ADMIN` | List all serialized components installed on vehicle |
| `POST` | `/api/v1/sc/vehicles/{vehicleId}/parts` | `SC_STAFF`, `SC_TECHNICIAN`, `ADMIN` | Install serialized component with auto warranty dating |
| `GET` | `/api/v1/sc/vehicles/{vehicleId}/parts/{partId}` | `SC_STAFF`, `ADMIN` | Retrieve installed part metadata |
| `DELETE` | `/api/v1/sc/vehicles/{vehicleId}/parts/{partId}` | `SC_STAFF`, `ADMIN` | Mark component as REPLACED during warranty repair |

---

## 4. Warranty Claims & Attachments APIs (`/api/v1/claims`)

| Method | Path | Required Role | Summary |
|---|---|---|---|
| `GET` | `/api/v1/claims` | *Any Authenticated* | List warranty claims (paginated) |
| `GET` | `/api/v1/claims/{id}` | *Any Authenticated* | Get claim details by ID |
| `GET` | `/api/v1/claims/search?q={query}` | *Any Authenticated* | Search claims by claim number or VIN |
| `GET` | `/api/v1/claims/pending` | *Any Authenticated* | List claims pending EVM review |
| `POST` | `/api/v1/claims/{claimId}/attachments` | *Any Authenticated* | Upload multipart diagnostic file |
| `GET` | `/api/v1/claims/{claimId}/attachments` | *Any Authenticated* | List attachments for a specific claim |
| `GET` | `/api/v1/attachments/{id}/download` | *Any Authenticated* | Download binary attachment file |
| `DELETE` | `/api/v1/attachments/{id}` | *Any Authenticated* | Delete attachment record and file |

---

## 5. Parts Catalog & Inventory APIs (`/api/v1/evm/parts`, `/api/v1/sc/inventory`)

| Method | Path | Required Role | Summary |
|---|---|---|---|
| `GET` | `/api/v1/evm/parts` | `EVM_STAFF`, `ADMIN` | List master parts catalog |
| `POST` | `/api/v1/evm/parts` | `EVM_STAFF`, `ADMIN` | Create new part definition in catalog |
| `GET` | `/api/v1/evm/parts/{id}` | `EVM_STAFF`, `ADMIN` | Get part details |
| `PUT` | `/api/v1/evm/parts/{id}` | `EVM_STAFF`, `ADMIN` | Update part specs & standard pricing |
| `POST` | `/api/v1/evm/parts/{id}/toggle` | `EVM_STAFF`, `ADMIN` | Toggle active status of catalog part |
| `GET` | `/api/v1/sc/inventory` | `SC_STAFF`, `ADMIN` | List service center inventory levels |
| `POST` | `/api/v1/sc/inventory/add` | `SC_STAFF`, `ADMIN` | Add stock quantity for part |
| `POST` | `/api/v1/sc/inventory/{id}/adjust` | `SC_STAFF`, `ADMIN` | Adjust physical stock quantity |

---

## 6. Audit & System Monitoring APIs (`/api/v1/audit`, `/actuator`)

| Method | Path | Required Role | Summary |
|---|---|---|---|
| `GET` | `/api/v1/audit/logs` | `ADMIN` | View audit trail records (user, action, timestamp, IP) |
| `GET` | `/actuator/health` | *Public* | Application health status |
| `GET` | `/actuator/info` | *Public* | Application version and build metadata |
