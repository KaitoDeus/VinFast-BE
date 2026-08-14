# System Architecture & Technical Specifications

## 1. High-Level Architectural Topology

The EV Warranty System is designed as an API-first, domain-centric backend architecture with strict boundary isolation between business modules:

```
                  +-------------------------------------------------+
                  |          Client Layer (API Consumers)           |
                  |     (Mobile Apps / Dealership Web / Admin)      |
                  +-------------------------------------------------+
                                           |
                                [HTTPS / JWT Bearer]
                                           v
+---------------------------------------------------------------------------------+
|                       Spring Boot 3.2.0 API Gateway & Security                  |
|  - JwtAuthenticationFilter (Token Parsing & SecurityContext population)         |
|  - SecurityConfig (Stateless Filter Chain, RBAC endpoint routing)               |
|  - GlobalExceptionHandler (RFC 7807 ProblemDetails / Unified JSON errors)       |
|  - AuditAspect (AOP interception for @Auditable actions)                        |
+---------------------------------------------------------------------------------+
                                           |
    +-------------------+------------------+------------------+-------------------+
    |                   |                  |                  |                   |
    v                   v                  v                  v                   v
+------------+   +--------------+   +--------------+   +--------------+   +---------------+
|   User &   |   |   Vehicle    |   |   Warranty   |   |   Parts &    |   |   Analytics   |
|    Auth    |   |   & Parts    |   |    Claims    |   |  Inventory   |   |   & AI Engine |
|   Domain   |   |    Domain    |   |    Domain    |   |    Domain    |   |    Domain     |
+------------+   +--------------+   +--------------+   +--------------+   +---------------+
    |                   |                  |                  |                   |
    +-------------------+------------------+------------------+-------------------+
                                           |
                              [Spring Data JPA Repositories]
                                           |
                                           v
+---------------------------------------------------------------------------------+
|                         PostgreSQL 17+ Relational Database                      |
|  - Schema managed via Flyway Migrations (V1 Initial, V2 Add Columns, V3 ...)     |
|  - Tables: users, roles, vehicles, vehicle_parts, parts, inventory, ...          |
+---------------------------------------------------------------------------------+
```

---

## 2. Domain Model Taxonomy

### 2.1 Vehicle & Installed Parts Subsystem
- **Vehicle Entity**: Models an EV asset by its globally unique 17-character VIN. Captures battery chemistry (`LFP`, `NMC`), capacity (kWh), motor config (`Dual Motor AWD`, `Single FWD`), current mileage, and warranty lifespan dates.
- **VehiclePart Entity**: Represents serialized, trackable high-value components installed on a vehicle (e.g. Battery Packs, Power Inverters, Traction Motors, BMS modules). Maintains individual warranty terms independent of vehicle age.

### 2.2 Warranty Claims & Attachments Subsystem
- **WarrantyClaim Entity**: Core transactional document recording customer failure reports, diagnostic fault codes (DTCs), labor/part cost calculations, and lifecycle states (`DRAFT` -> `SUBMITTED` -> `UNDER_REVIEW` -> `APPROVED` / `REJECTED` -> `IN_PROGRESS` -> `COMPLETED`).
- **ClaimAttachment Entity**: Manages diagnostic evidence files (thermal scans, BMS logs, photos) stored on disk with cryptographic naming.

### 2.3 Inventory & Supply Chain Subsystem
- **Part Entity**: Catalog master data for OEM components, standard repair hours, and pricing.
- **Inventory Entity**: Stock levels per Service Center with automated reorder threshold alerting.
- **PartAllocation Entity**: Logistics requests between OEM warehouse and regional Service Centers.

---

## 3. Database Schema Migration History (Flyway)

| Version | File | Description |
|---|---|---|
| **V1** | `V1__init_schema.sql` | Base schema: users, roles, vehicles, parts, vehicle_parts, warranty_claims, service_history, inventory, campaigns. |
| **V2** | `V2__add_attachment_columns.sql` | Sync `claim_attachments` with `attachment_type` and `description`. |
| **V3** | `V3__drop_stale_warranty_claim_id.sql` | Drop duplicate legacy FK column to enforce standard `claim_id` schema. |

---

## 4. Cross-Cutting Infrastructure

### 4.1 AOP Audit Logging System
- Intercepts methods marked with `@Auditable(action = "...", resourceType = "...")`.
- Extracts current authenticated user from `SecurityContextHolder`.
- Persists audit logs to `audit_logs` table asynchronously with execution metadata.

### 4.2 Unified Exception Handling
- `GlobalExceptionHandler` captures:
  - `IllegalArgumentException` -> `400 Bad Request`
  - `BadCredentialsException` -> `401 Unauthorized`
  - `AccessDeniedException` -> `403 Forbidden`
  - `EntityNotFoundException` / 404 lookups -> `404 Not Found`
  - `DataIntegrityViolationException` -> `409 Conflict`
  - Unhandled `Exception` -> `500 Internal Server Error`
