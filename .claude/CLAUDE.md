# EV Warranty System — AI Agent Master Guide & System Context

> **Target Audience**: AI Coding Assistants (Claude 3.7 Sonnet / Opus, OpenAI GPT-4o / o1, Gemini 2.0 / 1.5 Pro, Cursor Agent).
> **Purpose**: Provides authoritative context, architectural directives, domain taxonomy, security boundaries, and coding standards for the EV Warranty System backend.

---

## 1. Project Identity & Technical Profile

| Characteristic | Specification |
|---|---|
| **System Name** | Electric Vehicle Warranty Management System (EV Warranty System) |
| **Architecture** | **Pure Headless Backend REST API** (Strictly NO Frontend / Monolith UI) |
| **Primary Language** | Java 21 LTS (Records, Pattern Matching, Virtual Threads ready) |
| **Framework** | Spring Boot 3.2.0 (Spring Framework 6.1.1, Spring Security 6.2.0) |
| **Database Engine** | PostgreSQL 17 / 18 (`jdbc:postgresql://localhost:5432/postgres`) |
| **Database Migration** | Flyway (`org.flywaydb:flyway-core`, `flyway-database-postgresql`) |
| **Authentication** | Stateless JWT Bearer Token (`io.jsonwebtoken:jjwt-*` 0.11.5) |
| **Build & Packaging** | Apache Maven 3.9+, Multi-stage Docker, GitHub Actions CI/CD |
| **API Specification** | OpenAPI 3.0 / Swagger UI (`springdoc-openapi-starter-webmvc-ui` 2.3.0) |
| **Testing Stack** | JUnit 5 Jupiter, Mockito, Spring Boot Test (`@WebMvcTest`, `@SpringBootTest`, `MockMvc`) |

---

## 2. Directory Layout & Architectural Boundaries

```
src/
├── main/
│   ├── java/com/oem/evwarranty/
│   │   ├── EvWarrantyApplication.java          # Spring Boot main bootstrap class
│   │   ├── common/                             # Cross-cutting concerns & infrastructure
│   │   │   ├── annotation/                     # Custom metadata annotations (e.g. @Auditable)
│   │   │   ├── aspect/                         # AOP aspects (AuditAspect, performance monitors)
│   │   │   ├── config/                         # SecurityConfig, JwtTokenProvider, OpenApiConfig
│   │   │   └── exception/                      # GlobalExceptionHandler, Custom Exceptions
│   │   └── domain/                             # Bounded Contexts (DDD-structured)
│   │       ├── user/                           # User, Role, AuthController, CustomUserDetailsService
│   │       ├── vehicle/                        # Vehicle, VehiclePart, VehicleController, VehicleMapper
│   │       ├── claim/                          # WarrantyClaim, ClaimAttachment, AttachmentController
│   │       ├── inventory/                      # Part, Inventory, PartAllocation, InventoryController
│   │       ├── customer/                       # Customer profiles, CustomerRepository
│   │       ├── campaign/                       # ServiceCampaign (Recalls, TSBs), CampaignController
│   │       └── analytics/                      # AI FailurePredictionService, ReportController
│   └── resources/
│       ├── application.properties              # Spring Boot configuration (PostgreSQL, Flyway, Logging)
│       └── db/migration/                       # Flyway Versioned SQL Scripts (V1, V2, V3, ...)
└── test/
    └── java/com/oem/evwarranty/
        ├── domain/                             # Unit tests per bounded domain
        └── integration/                        # E2E MockMvc Spring Boot Integration tests
```

---

## 3. Core Architectural Directives for AI Assistants

### 3.1 Headless API Mandate
- Never generate HTML templates, JSP, Thymeleaf, or static web pages.
- Every Controller must be annotated with `@RestController` and return `ResponseEntity<DTO>` or `ResponseEntity<Page<DTO>>`.
- Always serialize output through explicit DTOs. Never expose raw JPA entities directly to the client layer.

### 3.2 Security & Role-Based Access Control (RBAC)
- All requests outside `/api/v1/auth/**`, `/v3/api-docs/**`, `/swagger-ui/**`, and `/actuator/**` require a valid JWT Bearer header: `Authorization: Bearer <token>`.
- Method-level security is enforced via `@PreAuthorize("hasRole(...)")` or `@PreAuthorize("hasAnyRole(...)")`.
- Roles are prefixed with `ROLE_` inside Spring Security context:
  - `ROLE_ADMIN`: Root administrator with destructive permissions.
  - `ROLE_EVM_STAFF`: OEM/Manufacturer headquarters personnel (policies, part catalogs, recalls, claims approval).
  - `ROLE_SC_STAFF`: Service Center operational staff (vehicle check-in, claim submission, parts stock management).
  - `ROLE_SC_TECHNICIAN`: Service Center mechanics (diagnostic reports, repair tasks, parts installation).

### 3.3 Database & Flyway Migration Rules
- **Never edit an already executed Flyway migration script** (it alters the recorded checksum and breaks application bootstrap).
- Any schema adjustments (adding columns, creating indices, modifying constraints) must be placed in a new versioned file: `V{N}__{description}.sql`.
- In JPA entities, verify that `@JoinColumn(name = "...")` exactly matches column identifiers established in Flyway SQL.

### 3.4 Bidirectional JPA Relations & Jackson Infinite Recursion
- Any `@OneToMany` or `@ManyToMany` bidirectional relationship MUST be annotated with `@JsonIgnore` on the non-owning side to prevent infinite recursive serialization (`StackOverflowError`).
- Sensitive fields (e.g. `User.password`) must ALWAYS have `@JsonIgnore`.

---

## 4. Development Commands Reference

```bash
# Execute entire test suite
mvn test

# Execute single test class
mvn test -Dtest=VehicleControllerTest

# Clean compile & package application jar
mvn clean package -DskipTests

# Run Spring Boot application locally
mvn spring-boot:run
```
